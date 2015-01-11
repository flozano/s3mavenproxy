package com.flozano.s3mavenproxy.domain;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;

public class S3RepositoryBackendTest {

	@Mock
	AmazonS3 s3;

	ExecutorService executorService;

	String bucket = "com.mybucket";

	String cacheSpec = "maximumSize=10";

	long expiration = 5;
	long waitTime = 3;
	TimeUnit timeUnit = TimeUnit.SECONDS;

	S3RepositoryBackend backend;

	Artifact artifact1 = Artifact
			.fromPath("com/flozano/s3mavenproxy/s3mavenproxy-1.0.jar");

	Artifact artifact2 = Artifact
			.fromPath("org/flozano/s3mavenproxy/s3mavenproxy-1.0.jar");

	URL url1;

	URL url2;

	@Before
	public void setUp() throws MalformedURLException {
		MockitoAnnotations.initMocks(this);
		executorService = Executors.newCachedThreadPool();
		backend = new S3RepositoryBackend(bucket, expiration, timeUnit,
				cacheSpec, s3, executorService);
		url1 = new URL("http://127.0.0.1/artifact1");
		url2 = new URL("http://127.0.0.1/artifact2");
	}

	@After
	public void tearDown() {
		executorService.shutdownNow();
	}

	@Test
	public void testGetCacheMiss() throws NotFoundException,
			InterruptedException, ExecutionException, URISyntaxException {
		when(
				s3.generatePresignedUrl(eq(bucket), eq(artifact1.getPath()),
						any(Date.class), eq(HttpMethod.GET))).thenReturn(url1);
		RetrievalResult result = backend.get(artifact1).get();
		assertEquals(url1.toURI(), result.getTargetURI());
		assertNotNull(result);
	}

	@Test
	public void testGetCacheHit() throws NotFoundException,
			InterruptedException, ExecutionException, URISyntaxException {
		when(
				s3.generatePresignedUrl(eq(bucket), eq(artifact1.getPath()),
						any(Date.class), eq(HttpMethod.GET))).thenReturn(url1);
		RetrievalResult result = backend.get(artifact1).get();
		RetrievalResult result2 = backend.get(artifact1).get();
		assertEquals(result, result2);

		verify(s3, times(1)).generatePresignedUrl(any(String.class),
				any(String.class), any(Date.class), any(HttpMethod.class));

	}

	@Test
	public void testGetCacheExpiration() throws NotFoundException,
			InterruptedException, ExecutionException, URISyntaxException {
		when(
				s3.generatePresignedUrl(eq(bucket), eq(artifact1.getPath()),
						any(Date.class), eq(HttpMethod.GET))).thenReturn(url1);
		RetrievalResult result = backend.get(artifact1).get();
		Thread.sleep(timeUnit.toMillis(expiration + waitTime));
		RetrievalResult result2 = backend.get(artifact1).get();

		assertNotSame(result, result2);
		assertNotEquals(result, result2);
		verify(s3, times(2)).generatePresignedUrl(any(String.class),
				any(String.class), any(Date.class), any(HttpMethod.class));

	}

	@Test
	public void testGetSomeOtherException() throws NotFoundException,
			InterruptedException, ExecutionException {
		when(s3.getObjectMetadata(eq(bucket), eq(artifact1.getPath())))
				.thenThrow(anAmazonS3ExceptionWithStatus(401));
		try {
			backend.get(artifact1).get();
			fail("Exception expected");
		} catch (ExecutionException e) {
			assertThat(e.getCause(),
					is(not(IsInstanceOf.instanceOf(NotFoundException.class))));
		}
	}

	@Test
	public void testGetNotFound() throws NotFoundException,
			InterruptedException, ExecutionException {
		when(s3.getObjectMetadata(eq(bucket), eq(artifact1.getPath())))
				.thenThrow(anAmazonS3ExceptionWithStatus(404));
		try {
			backend.get(artifact1).get();
			fail("NotFound expected");
		} catch (ExecutionException e) {
			assertThat(e.getCause(),
					IsInstanceOf.instanceOf(NotFoundException.class));
		}
	}

	private static Throwable anAmazonS3ExceptionWithStatus(int i) {
		AmazonS3Exception e = new AmazonS3Exception("not f0und");
		e.setStatusCode(i);
		return e;
	}
}
