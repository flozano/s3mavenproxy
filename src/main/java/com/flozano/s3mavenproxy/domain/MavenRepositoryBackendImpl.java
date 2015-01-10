package com.flozano.s3mavenproxy.domain;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("backend")
public class MavenRepositoryBackendImpl implements MavenRepositoryBackend {

	private final RepositoryChecker repositoryChecker;

	private final MavenRepositoryBackend s3Backend;

	private final MavenRepositoryBackend remoteBackend;

	@Autowired
	public MavenRepositoryBackendImpl(RepositoryChecker repositoryChecker,
			@Qualifier("s3Backend") MavenRepositoryBackend s3Backend,
			@Qualifier("remoteBackend") MavenRepositoryBackend remoteBackend) {
		this.repositoryChecker = requireNonNull(repositoryChecker);
		this.s3Backend = requireNonNull(s3Backend);
		this.remoteBackend = requireNonNull(remoteBackend);
	}

	@Override
	public CompletableFuture<RetrievalResult> get(Artifact artifact)
			throws NotFoundException {
		if (repositoryChecker.isInternal(artifact)) {
			return s3Backend.get(artifact);
		} else {
			return remoteBackend.get(artifact);
		}
	}

	@Override
	public CompletableFuture<Void> put(Artifact artifact, String contentType,
			long length, InputStream content) {
		if (repositoryChecker.isInternal(artifact)) {
			return s3Backend.put(artifact, contentType, length, content);
		} else {
			throw new ForbiddenException();
		}
	}

}
