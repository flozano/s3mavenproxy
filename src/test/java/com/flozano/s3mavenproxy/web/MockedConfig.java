package com.flozano.s3mavenproxy.web;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.flozano.s3mavenproxy.domain.MavenRepositoryBackend;

@Configuration
@ComponentScan
@EnableWebMvc
public class MockedConfig {
	@Bean
	@Qualifier("backend")
	MavenRepositoryBackend backend() {
		return Mockito.mock(MavenRepositoryBackend.class);
	}
}
