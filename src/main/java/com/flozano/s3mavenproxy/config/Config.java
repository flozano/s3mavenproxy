package com.flozano.s3mavenproxy.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableAutoConfiguration
@ComponentScan("com.flozano.s3mavenproxy")
@EnableWebMvc
public class Config {
	
	@Bean(destroyMethod="shutdownNow")
	public ExecutorService executorService() {
		return Executors.newCachedThreadPool();
	}
}
