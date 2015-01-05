package com.flozano.s3mavenproxy.web;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.flozano.s3mavenproxy.domain.Artifact;
import com.flozano.s3mavenproxy.domain.MavenRepositoryBackend;
import com.flozano.s3mavenproxy.domain.NotFoundException;
import com.flozano.s3mavenproxy.domain.RetrievalResult;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = MockedConfig.class)
public class WebTest {

	@Autowired
	WebApplicationContext wac;

	@Autowired
	MavenRepositoryBackend mockedBackend;

	ScheduledExecutorService executorService;

	MockMvc mockMvc;

	String url = "http://127.0.0.2/go/here";

	RetrievalResult retrievalResult = new RetrievalResult(URI.create(url));

	String path = "/com/flozano/s3mavenrepo/s3mavenrepo-1.0-SNAPSHOT.jar";

	CountDownLatch latch;

	@Before
	public void setup() {
		Mockito.reset(mockedBackend);
		this.executorService = Executors.newScheduledThreadPool(5);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		latch = new CountDownLatch(1);
	}

	@After
	public void tearDown() {
		executorService.shutdownNow();
	}

	@Test
	public void testGet() throws Exception {

		mockCompletable((cf) -> cf.complete(retrievalResult));

		MvcResult mvcResult = mockMvc.perform(get(path))
				.andExpect(request().asyncStarted()).andReturn();
		latch.await();
		mockMvc.perform(asyncDispatch(mvcResult))
				.andExpect(status().is3xxRedirection())
				.andExpect(content().contentType("text/plain"))
				.andExpect(header().string("location", url))
				.andExpect(content().string("Artifact found in " + url));
	}

	@Test
	public void testGetNotFound() throws Exception {

		mockCompletable((cf) -> cf
				.completeExceptionally(new NotFoundException()));

		MvcResult mvcResult = mockMvc.perform(get(path))
				.andExpect(request().asyncStarted()).andReturn();
		latch.await();
		mockMvc.perform(asyncDispatch(mvcResult)).andExpect(
				status().is4xxClientError());
	}

	private void mockCompletable(Consumer<CompletableFuture<RetrievalResult>> c) {
		when(mockedBackend.get(any(Artifact.class))).then(invocation -> {
			CompletableFuture<RetrievalResult> cf = new CompletableFuture<>();
			executorService.schedule(() -> {
				c.accept(cf);
				latch.countDown();
			}, 1, TimeUnit.SECONDS);
			return cf;
		});
	}
}
