package com.flozano.s3mavenproxy.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RepositoryChecker {

	private final Set<String> internalPackageIdPrefixes;
	private final Set<String> internalPathPrefixes;

	@Autowired
	public RepositoryChecker(
			@Value("${s3mavenproxy.allowed-prefixes:com.flozano}") String[] internalPackageIdPrefixes) {
		this.internalPackageIdPrefixes = new HashSet<>(
				Arrays.asList(internalPackageIdPrefixes));
		this.internalPathPrefixes = Arrays.asList(internalPackageIdPrefixes)
				.stream().map((x) -> x.replace('.', '/'))
				.collect(Collectors.toSet());
	}

	public boolean isInternal(Artifact artifact) {
		for (String packageId : internalPackageIdPrefixes) {
			if (artifact.getGroupId().startsWith(packageId)) {
				return true;
			}
		}
		return false;
	}

	public boolean isInternalPath(String path) {
		for (String prefix : internalPathPrefixes) {
			if (path.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

}
