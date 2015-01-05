package com.flozano.s3mavenproxy.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;

public class RemoteRepositoryBackendTest {

	final String base = "http://uk.maven.org/maven2/";
	final String path = "com/flozano/s3mavenproxy/s3mavenproxy-1.0.jar";
	RemoteRepositoryBackend backend;

	@Before
	public void setUp() throws URISyntaxException {
		backend = new RemoteRepositoryBackend(new URI(base));
	}

	@Test
	public void testGet() throws NotFoundException, InterruptedException,
			ExecutionException, URISyntaxException {
		RetrievalResult result = backend.get(Artifact.fromPath(path)).get();
		assertEquals(new URI(base + path), result.getTargetURI());
	}

	@Test
	public void testPut() throws InterruptedException, ExecutionException {
		try {
			backend.put(Artifact.fromPath(path), "text/plain", 100,
					mock(InputStream.class)).get();
			fail();
		} catch (ExecutionException e) {
			assertThat(e.getCause(),
					IsInstanceOf.instanceOf(ForbiddenException.class));
		}
	}

}
