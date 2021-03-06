package com.flozano.s3mavenproxy.domain;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

@Service("s3Backend")
public class S3RepositoryBackend implements MavenRepositoryBackend {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(S3RepositoryBackend.class);
	private static final long CACHE_EXPIRATION_DIFFERENCE = -2_000;
	private final long expirationMilliseconds;
	private final long cacheExpirationMilliseconds;

	private final String bucket;

	private final AmazonS3 s3;
	private final ExecutorService executorService;
	private final LoadingCache<Artifact, RetrievalResult> cache;

	@Autowired
	public S3RepositoryBackend(
			@Value("${backend.s3.bucket:com.flozano.maven}") String bucket,
			@Value("${backend.s3.get-expiration:6}") long expiration,
			@Value("${backend.s3.get-expiration-time-unit:HOURS}") TimeUnit timeUnit,
			@Value("${backend.s3.cache-spec:maximumSize=10000}") String cacheSpec,
			AmazonS3 s3, ExecutorService executorService) {
		this.bucket = requireNonNull(bucket);
		this.s3 = requireNonNull(s3);
		this.executorService = requireNonNull(executorService);

		this.expirationMilliseconds = requireNonNull(timeUnit).toMillis(
				expiration);
		this.cacheExpirationMilliseconds = expirationMilliseconds
				+ CACHE_EXPIRATION_DIFFERENCE;

		this.cache = CacheBuilder
				.from(cacheSpec)
				.expireAfterWrite(cacheExpirationMilliseconds,
						TimeUnit.MILLISECONDS)
				.build(CacheLoader.from((artifact) -> getFromS3(artifact)));
	}

	@Override
	public CompletableFuture<RetrievalResult> get(Artifact artifact)
			throws NotFoundException {
		RetrievalResult cachedResult = cache.getIfPresent(artifact);
		CompletableFuture<RetrievalResult> r;
		if (cachedResult != null) {
			r = CompletableFuture.completedFuture(cachedResult);
		} else {
			r = new CompletableFuture<>();
			executorService.submit(() -> {
				try {
					r.complete(cache.get(artifact));
				} catch (ExecutionException | UncheckedExecutionException e) {
					r.completeExceptionally(e.getCause());
				} catch (Exception e) {
					r.completeExceptionally(e);
				}
			});
		}
		return r;
	}

	private RetrievalResult getFromS3(Artifact artifact) {
		try {
			Date expirationDate = getNewExpirationDateForGet();
			Date cacheExpirationDate = getExpirationDateForCache(expirationDate);
			s3.getObjectMetadata(bucket, artifact.getPath());
			URL result = requireNonNull(s3.generatePresignedUrl(bucket,
					artifact.getPath(), expirationDate, HttpMethod.GET));
			return new RetrievalResult(result.toURI(), cacheExpirationDate);
		} catch (AmazonS3Exception e) {
			if (isNotFoundKey(e)) {
				throw new NotFoundException();
			} else {
				throw e;
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private Date getExpirationDateForCache(Date expirationDate) {
		return new Date(expirationDate.getTime() + CACHE_EXPIRATION_DIFFERENCE);
	}

	private static boolean isNotFoundKey(AmazonS3Exception e) {
		return e.getStatusCode() == 404;
	}

	private Date getNewExpirationDateForGet() {
		return new Date(System.currentTimeMillis() + expirationMilliseconds);
	}

	@Override
	public CompletableFuture<Void> put(Artifact artifact,
			ContentInformation contentInformation, InputStream content) {
		CompletableFuture<Void> cf = new CompletableFuture<>();

		executorService.submit(() -> {
			try {
				if (contentInformation.isChunked()) {
					File temp = File.createTempFile("s3mavenproxy", "tmp");
					long newLength = copyContentToFile(content, temp);
					uploadFileToS3(artifact, contentInformation, temp,
							newLength);
				} else {
					uploadToS3(artifact, contentInformation, content);
					cf.complete(null);
				}
			} catch (Exception e) {
				cf.completeExceptionally(e);
			}
		});
		return cf;
	}

	private long copyContentToFile(InputStream content, File temp)
			throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(temp)) {
			return IOUtils.copyLarge(content, fos);
		}
	}

	private void uploadFileToS3(Artifact artifact,
			ContentInformation contentInformation, File temp, long newLength)
			throws FileNotFoundException, IOException {
		try (FileInputStream fis = new FileInputStream(temp)) {
			uploadToS3(artifact, new ContentInformation(contentInformation,
					newLength), fis);
		}
	}

	private void uploadToS3(Artifact artifact,
			ContentInformation contentInformation, InputStream inputStream) {
		ObjectMetadata md = new ObjectMetadata();
		md.setContentType(contentInformation.getContentType());
		md.setContentLength(contentInformation.getLength());

		s3.putObject(bucket, artifact.getPath(), inputStream, md);
	}
}
