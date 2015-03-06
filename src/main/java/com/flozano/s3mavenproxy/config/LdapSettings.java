package com.flozano.s3mavenproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@ConfigurationProperties(prefix = "s3mavenproxy.auth.ldap")
public class LdapSettings {

	String url;
	String bindDn;
	String bindPassword;
	String baseDn;
	String userDnPattern = "uid={0},ou=people";
	String groupSearchBase = "ou=groups";

	@Bean
	@Lazy
	public LdapContextSource ldapContextSource() {
		LdapContextSource source = new LdapContextSource();
		source.setUrl(url);
		// source.setBase(baseDn);
		source.setUserDn(bindDn);
		source.setPassword(bindPassword);
		return source;
	}
}
