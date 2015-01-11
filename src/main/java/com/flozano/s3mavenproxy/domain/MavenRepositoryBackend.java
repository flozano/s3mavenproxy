package com.flozano.s3mavenproxy.domain;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface MavenRepositoryBackend {
	CompletableFuture<RetrievalResult> get(Artifact artifact)
			throws NotFoundException;

	CompletableFuture<Void> put(Artifact artifact,
			ContentInformation contentInformation, InputStream content);
}
