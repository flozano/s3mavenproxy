package com.flozano.s3mavenproxy.web;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;

import com.flozano.s3mavenproxy.config.Config;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Config.class)
@WebIntegrationTest(randomPort = true)
@Ignore
public class IntegrationWebTest {

	@Value("${local.server.port}")
	int port;

	RestOperations restOperations = new TestRestTemplate("test", "test");

	String path = "/com/flozano/s3mavenproxy/dummy-artifact-1.0.txt";

	@Test
	public void test() {
		String url = "http://127.0.0.1:" + port + path;
		ResponseEntity<Void> putResult = restOperations.exchange(url,
				HttpMethod.PUT, new HttpEntity<InputStream>(resource()),
				Void.class);
		assertEquals(HttpStatus.CREATED, putResult.getStatusCode());
		ResponseEntity<Void> getResult = restOperations.getForEntity(url,
				Void.class);
		assertEquals(HttpStatus.FOUND, getResult.getStatusCode());
		System.err.println(getResult.getHeaders());
	}

	private InputStream resource() {
		return getClass().getResourceAsStream("loremipsum.txt");
	}
}
