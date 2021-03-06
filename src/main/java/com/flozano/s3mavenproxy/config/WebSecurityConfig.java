package com.flozano.s3mavenproxy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private static final String USER_AUTHORITY = "USER";

	@Value("${auth:PLAIN}")
	private AuthenticationBackend backend;

	@Value("${auth.plain.user:test}")
	private String plainUser;

	@Value("${auth.plain.password:test}")
	private String plainPassword;

	@Autowired
	LdapSettings ldapSettings;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

		http.headers().xssProtection().disable();

		http.sessionManagement().sessionCreationPolicy(
				SessionCreationPolicy.IF_REQUIRED);

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
					.password(plainPassword).authorities(USER_AUTHORITY);
		} else if (AuthenticationBackend.LDAP.equals(backend)) {
			LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder> ldapAuth = auth
					.ldapAuthentication()
					.userDnPatterns(ldapSettings.userDnPattern)
					.groupSearchBase(ldapSettings.groupSearchBase);
			if (ldapSettings.url == null || "".equals(ldapSettings.url)) {
				ldapAuth.contextSource().ldif("classpath:test-ldap-data.ldif")
						.root("dc=flozano,dc=com");
			} else {
				ldapAuth.contextSource(ldapSettings.ldapContextSource());
			}
		} else {
			throw new IllegalStateException();
		}
	}
}
