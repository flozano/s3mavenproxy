package com.flozano.s3mavenproxy.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;

import com.flozano.s3mavenproxy.Integration;
import com.flozano.s3mavenproxy.config.Config;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Config.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@Category(Integration.class)
public class IntegrationWebTest {

	@Value("${local.server.port}")
	int port;
	@Autowired
	RestTemplateBuilder restTemplateBuilder;
	RestOperations restOperations;

	String path = "/com/flozano/s3mavenproxy/dummy-artifact-1.0.txt";

	@Test
	public void test() {
		restOperations = restTemplateBuilder.additionalInterceptors(new BasicAuthorizationInterceptor("test", "test"))
				.build();
		String url = "http://127.0.0.1:" + port + path;
		ResponseEntity<Void> putResult = restOperations.exchange(url, HttpMethod.PUT, new HttpEntity<>(resource()),
				Void.class);
		assertEquals(HttpStatus.CREATED, putResult.getStatusCode());
		ResponseEntity<Void> getResult = restOperations.getForEntity(url, Void.class);
		assertEquals(HttpStatus.FOUND, getResult.getStatusCode());

		ResponseEntity<Void> getResult2 = restOperations.getForEntity(url, Void.class);
		assertEquals(HttpStatus.FOUND, getResult2.getStatusCode());

		assertNotNull(getResult.getHeaders().get("location"));
		assertEquals(getResult.getHeaders().get("location"), getResult2.getHeaders().get("location"));
	}

	private InputStream resource() {
		return getClass().getResourceAsStream("loremipsum.txt");
	}
}
