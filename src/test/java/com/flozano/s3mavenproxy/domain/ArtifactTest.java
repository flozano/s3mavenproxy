package com.flozano.s3mavenproxy.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ArtifactTest {

	@Test
	public void testNonSnapshot() {
		String path = "com/flozano/s3mavenproxy/s3mavenproxy-1.0.jar";
		Artifact f = Artifact.fromPath(path);
		assertEquals(path, f.getPath());
		assertEquals("com.flozano.s3mavenproxy", f.getGroupId());
		assertEquals("s3mavenproxy-1.0.jar", f.getArtifactName());
		assertFalse(f.isSnapshot());
	}

	@Test
	public void testNonSnapshotStartsWithSlash() {
		String path = "com/flozano/s3mavenproxy/s3mavenproxy-1.0.jar";
		Artifact f = Artifact.fromPath("/" + path);
		assertEquals(path, f.getPath());
		assertEquals("com.flozano.s3mavenproxy", f.getGroupId());
		assertEquals("s3mavenproxy-1.0.jar", f.getArtifactName());
		assertFalse(f.isSnapshot());
	}

	@Test
	public void testSnapshot() {
		String path = "com/flozano/s3mavenproxy/s3mavenproxy-1.0-SNAPSHOT.jar";

		Artifact f = Artifact.fromPath(path);
		assertEquals(path, f.getPath());
		assertEquals("com.flozano.s3mavenproxy", f.getGroupId());
		assertEquals("s3mavenproxy-1.0-SNAPSHOT.jar", f.getArtifactName());
		assertTrue(f.isSnapshot());
	}
}
