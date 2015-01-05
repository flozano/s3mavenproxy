package com.flozano.s3mavenproxy;

import org.springframework.boot.builder.SpringApplicationBuilder;

import com.flozano.s3mavenproxy.config.Config;

public final class Main {
	private Main() {
		throw new IllegalStateException("Can't be instantiated");
	}

	public static void main(String[] argz) {
		new SpringApplicationBuilder(Config.class).showBanner(false).run(argz);
	}
}
