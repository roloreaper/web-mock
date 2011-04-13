package org.httpmock.server;

interface RequestHandler {
    void url(String url);
    void param(String param, String value);

    Object returnValue();

    String getResponseStatus();
}
