package com.flozano.s3mavenproxy.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RepositoryCheckerTest {
	RepositoryChecker checker = new RepositoryChecker(
			new String[] { "com.flozano.s3mavenproxy" });

	@Test
	public void testLocal() {
		String path = "com/flozano/s3mavenproxy/s3mavenproxy-1.0.jar";
		assertTrue(checker.isInternal(Artifact.fromPath(path)));
		assertTrue(checker.isInternalPath(path));
	}

	@Test
	public void testRemote() {
		String path = "org/flozano/s3mavenproxy/s3mavenproxy-1.0.jar";
		assertFalse(checker.isInternal(Artifact.fromPath(path)));
		assertFalse(checker.isInternalPath(path));
	}
}
