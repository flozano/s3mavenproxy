package com.flozano.s3mavenproxy.domain;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;

public class RetrievalResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private final URI targetURI;

	private final Date expires;

	public RetrievalResult(URI targetURI) {
		this(targetURI, null);
	}

	public RetrievalResult(URI targetURI, Date expires) {
		this.targetURI = requireNonNull(targetURI);
		this.expires = expires;
	}

	public boolean isNotFound() {
		return targetURI == null;
	}

	public URI getTargetURI() {
		return targetURI;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (expires == null ? 0 : expires.hashCode());
		result = prime * result
				+ (targetURI == null ? 0 : targetURI.hashCode());
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
		RetrievalResult other = (RetrievalResult) obj;
		if (expires == null) {
			if (other.expires != null) {
				return false;
			}
		} else if (!expires.equals(other.expires)) {
			return false;
		}
		if (targetURI == null) {
			if (other.targetURI != null) {
				return false;
			}
		} else if (!targetURI.equals(other.targetURI)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("RetrievalResult [targetURI=%s, expires=%s]",
				targetURI, expires);
	}

}
