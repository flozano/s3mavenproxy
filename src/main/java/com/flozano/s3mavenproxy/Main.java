package com.flozano.s3mavenproxy;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.flozano.s3mavenproxy.config.Config;

@SpringBootApplication
public class Main {

	public static void main(String[] argz) {
		new SpringApplicationBuilder(Config.class).run(argz);
	}
}
