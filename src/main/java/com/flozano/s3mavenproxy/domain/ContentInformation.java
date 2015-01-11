package com.flozano.s3mavenproxy.domain;

import javax.servlet.http.HttpServletRequest;

public class ContentInformation {
	private static final long CHUNKED = -1;
	private final String contentType;
	private final String contentEncoding;
	private final long length;

	public static ContentInformation fromRequest(HttpServletRequest request) {
		return new ContentInformation(request.getContentType(),
				request.getHeader("content-encoding"),
				request.getContentLengthLong());
	}

	public ContentInformation(String contentType, String contentEncoding,
			long length) {
		super();
		this.contentType = contentType;
		this.contentEncoding = contentEncoding;
		this.length = length;
	}

	public ContentInformation(ContentInformation contentInformation,
			long newLength) {
		this.contentEncoding = contentInformation.getContentEncoding();
		this.contentType = contentInformation.getContentType();
		this.length = newLength;
	}

	public String getContentType() {
		return contentType;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public long getLength() {
		return length;

	}

	public boolean isChunked() {
		return length == CHUNKED;
	}

}
