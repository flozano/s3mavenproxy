package com.flozano.s3mavenproxy.domain;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service("remoteBackend")
public class RemoteRepositoryBackend implements MavenRepositoryBackend {

	private final URI targetBaseURI;

	@Autowired
	public RemoteRepositoryBackend(
			@Value("${backend.remote.redirect-prefix:http://uk.maven.org/maven2/}") URI targetBaseURI) {
		this.targetBaseURI = requireNonNull(targetBaseURI);
	}

	@Override
	public CompletableFuture<RetrievalResult> get(Artifact artifact)
			throws NotFoundException {

		return CompletableFuture.completedFuture(new RetrievalResult(
				UriComponentsBuilder.fromUri(targetBaseURI)
						.path(artifact.getPath()).build().toUri(), null));
	}

	@Override
	public CompletableFuture<Void> put(Artifact artifact, ContentInformation contentInformation,
			InputStream content) {
		CompletableFuture<Void> cf = new CompletableFuture<>();
		cf.completeExceptionally(new ForbiddenException());
		return cf;
	}

}
