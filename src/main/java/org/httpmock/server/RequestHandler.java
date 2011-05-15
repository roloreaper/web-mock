package org.httpmock.server;

/***
 * Interface for internalHttpMockUseOnly
 */
public interface RequestHandler {
    void url(String url);
    void param(String param, String value);

    Object returnValue();

    String getResponseStatus();
}
