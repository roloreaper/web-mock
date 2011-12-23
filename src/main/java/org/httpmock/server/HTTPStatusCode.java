package org.httpmock.server;

/**
 * Some HTTP response status codes
 */
public enum HTTPStatusCode {
	HTTP_OK(200, "OK"),
	HTTP_REDIRECT(301, "Moved Permanently"),
	HTTP_FORBIDDEN(403, "Forbidden"),
	HTTP_NOTFOUND(404, "Not Found"),
	HTTP_BADREQUEST(400, "Bad Request"),
	HTTP_INTERNALERROR(500, "Internal Server Error");
	//HTTP_NOTIMPLEMENTED(501,"Not Implemented");
	private int code;
	private String description;

	HTTPStatusCode(int code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * This will concatenate the code and the description
	 *
	 * @return "{code} {description}"
	 */

	@Override
	public String toString() {
		return code + " " + description;
	}

	/**
	 * Returns Error Code for enum
	 *
	 * @return the Http Status Code eg 400
	 */

	public int getCode() {
		return code;
	}

	/**
	 * User to Return the Correct Enum for the code
	 *
	 * @param code use to Find the Enum
	 * @return the Enum Assosciated with the code
	 */

	public static HTTPStatusCode instanceOf(int code) {
		for (HTTPStatusCode httpStatusCode : HTTPStatusCode.values()) {
			if (httpStatusCode.getCode() == code) {
				return httpStatusCode;
			}
		}
		throw new EnumConstantNotPresentException(HTTPStatusCode.class, "for " + code);
	}


}
