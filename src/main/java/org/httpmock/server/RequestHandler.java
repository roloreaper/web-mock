package org.httpmock.server;

import java.util.List;

/**
 * Interface for internalHttpMockUseOnly
 */
public interface RequestHandler {
	void url(String url);

	void param(String param, List<String> value);

	String returnValue();

	int getResponseStatus();

	void bodyMatching(String body);
}
