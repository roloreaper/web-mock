package org.httpmock.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * HTTP response.
 * Return one of these from serve().
 */
class Response {
	Response(String status, String mimeType, String txt) throws UnsupportedEncodingException {
		this.status = status;
		this.mimeType = mimeType;
		this.data = new ByteArrayInputStream(txt.getBytes("UTF-8"));

	}

	/**
	 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
	 */
	private String status;

	/**
	 * MIME type of content, e.g. "text/html"
	 */
	private String mimeType;

	/**
	 * Data of the response, may be null.
	 */
	private InputStream data;

	/**
	 * Headers for the HTTP response. Use addHeader()
	 * to add lines.
	 */
	private Properties header = new Properties();

	String getStatus() {
		return status;
	}

	String getMimeType() {
		return mimeType;
	}

	Properties getHeader() {
		return header;
	}

	InputStream getData() {
		return data;
	}
}
