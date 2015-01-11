package com.flozano.s3mavenproxy.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

public class ArtifactTest {
	static final String FQ_URL = "http://flozano.com/whatever";

	URI baseURI;
	URI baseURI2;

	@Before
	public void setUp() throws URISyntaxException {
		baseURI = new URI("/");
		baseURI2 = new URI(FQ_URL);
	}

	@Test
	public void testNonSnapshot() throws URISyntaxException {
		String path = "com/flozano/s3mavenproxy/s3mavenproxy-1.0.jar";
		Artifact f = Artifact.fromPath(path);
		assertEquals(path, f.getPath());
		assertEquals("com.flozano.s3mavenproxy", f.getGroupId());
		assertEquals("s3mavenproxy-1.0.jar", f.getArtifactName());
		assertEquals(new URI("/" + path), f.getURI(baseURI));
		assertEquals(new URI(FQ_URL + "/" + path), f.getURI(baseURI2));
		assertFalse(f.isSnapshot());
	}

	@Test
	public void testNonSnapshotStartsWithSlash() throws URISyntaxException {
		String path = "com/flozano/s3mavenproxy/s3mavenproxy-1.0.jar";
		Artifact f = Artifact.fromPath("/" + path);
		assertEquals(path, f.getPath());
		assertEquals("com.flozano.s3mavenproxy", f.getGroupId());
		assertEquals("s3mavenproxy-1.0.jar", f.getArtifactName());
		assertEquals(new URI("/" + path), f.getURI(baseURI));
		assertEquals(new URI(FQ_URL + "/" + path), f.getURI(baseURI2));

		assertFalse(f.isSnapshot());
	}

	@Test
	public void testSnapshot() throws URISyntaxException {
		String path = "com/flozano/s3mavenproxy/s3mavenproxy-1.0-SNAPSHOT.jar";

		Artifact f = Artifact.fromPath(path);
		assertEquals(path, f.getPath());
		assertEquals("com.flozano.s3mavenproxy", f.getGroupId());
		assertEquals("s3mavenproxy-1.0-SNAPSHOT.jar", f.getArtifactName());
		assertEquals(new URI("/" + path), f.getURI(baseURI));
		assertEquals(new URI(FQ_URL + "/" + path), f.getURI(baseURI2));
		assertTrue(f.isSnapshot());
	}
}
