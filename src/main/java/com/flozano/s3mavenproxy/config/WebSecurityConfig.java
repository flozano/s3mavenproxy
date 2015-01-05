package com.flozano.s3mavenproxy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private enum AuthenticationBackend {
		PLAIN, LDAP
	}

	@Value("${s3mavenproxy.auth:PLAIN}")
	private AuthenticationBackend backend;

	@Value("${s3mavenproxy.auth.plain.user:test}")
	private String plainUser;

	@Value("${s3mavenproxy.auth.plain.password:test}")
	private String plainPassword;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests() //
				.anyRequest() //
				.authenticated() //
				.and() //
				.httpBasic();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth)
			throws Exception {
		if (AuthenticationBackend.PLAIN.equals(backend)) {
			auth.inMemoryAuthentication().withUser(plainUser)
					.password(plainPassword);
		} else if (AuthenticationBackend.LDAP.equals(backend)) {
			// TODO implement LDAP
		} else {
			throw new IllegalStateException();
		}
	}
}
