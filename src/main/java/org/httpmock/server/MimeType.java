package org.httpmock.server;

public enum MimeType {
	MIME_PLAINTEXT("text/plain"),
	MIME_HTML("text/html");
	private String code;

	MimeType(String code) {
		this.code = code;
	}

	public String toString() {
		return code;
	}
}
