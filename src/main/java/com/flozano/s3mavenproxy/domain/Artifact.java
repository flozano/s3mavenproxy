package com.flozano.s3mavenproxy.domain;

import java.io.Serializable;
import java.util.Arrays;

import com.google.common.base.Joiner;

/**
 * Extremely trivial, incomplete representation of an artifactName.
 * 
 * @author flozano
 *
 */
public class Artifact implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String groupId;
	private final String artifactName;
	private final String path;

	public Artifact(String path, String groupId, String artifact) {
		this.path = path;
		this.groupId = groupId;
		this.artifactName = artifact;
	}

	public static Artifact fromPath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		String[] partz = path.split("/");
		if (partz.length < 2) {
			throw new IllegalArgumentException("Invalid path " + path);
		}
		String groupId = Joiner.on('.').join(
				Arrays.copyOfRange(partz, 0, partz.length - 1));
		String artifact = partz[partz.length - 1];
		return new Artifact(path, groupId, artifact);
	}

	public boolean isSnapshot() {
		return artifactName.contains("-SNAPSHOT");
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public String getPath() {
		return path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (artifactName == null ? 0 : artifactName.hashCode());
		result = prime * result + (groupId == null ? 0 : groupId.hashCode());
		result = prime * result + (path == null ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Artifact other = (Artifact) obj;
		if (artifactName == null) {
			if (other.artifactName != null) {
				return false;
			}
		} else if (!artifactName.equals(other.artifactName)) {
			return false;
		}
		if (groupId == null) {
			if (other.groupId != null) {
				return false;
			}
		} else if (!groupId.equals(other.groupId)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}

}
