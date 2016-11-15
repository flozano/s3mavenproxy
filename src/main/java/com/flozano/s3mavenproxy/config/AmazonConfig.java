package com.flozano.s3mavenproxy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

@Configuration
public class AmazonConfig {

	@Value("${aws.iam:true}")
	private boolean iamEnabled;

	@Value("${aws.access-key:}")
	private String accessKey;

	@Value("${aws.secret-key:}")
	private String secretKey;

	@Bean
	public AWSCredentialsProvider awsCredentialsProvider() {
		if (iamEnabled) {
			// disabled async because uses Object#finalize() method for
			// clean-up...
			return new InstanceProfileCredentialsProvider(false);
		} else {
			if ("".equals(accessKey) || "".equals(secretKey)) {
				throw new IllegalStateException("Unconfigured AWS credentials");
			}
			return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
		}
	}

	@Bean
	public AmazonS3 s3() {
		return new AmazonS3Client(awsCredentialsProvider());
	}
}
