package org.httpmock.server;

import org.jmock.Mockery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MockHTTPServer extends NanoHTTPD {
	private static Map<Integer, MockHTTPServer> mockServers = new HashMap<Integer, MockHTTPServer>();
	private RequestHandler requestHandler;
	private Mockery context;
	private Throwable thrown;

	public static MockHTTPServer getServerOnPort(int port) {
		return mockServers.get(port);
	}

	public static void stopAllServers() {
		for (Integer port : mockServers.keySet()) {
			mockServers.get(port).stop();
		}
		mockServers.clear();
	}


	public static MockHTTPServer startServer(int port, RequestHandler requestHandler, Mockery context) throws IOException {

		if (mockServers.get(port) == null) {
			mockServers.put(port, new MockHTTPServer(port, requestHandler, context));
		} else {
			mockServers.get(port).stop();
			mockServers.put(port, new MockHTTPServer(port, requestHandler, context));
		}

		return mockServers.get(port);
	}

	MockHTTPServer(int port, RequestHandler requestHandler, Mockery context) throws IOException {
		super(port);
		this.requestHandler = requestHandler;
		this.context = context;
	}


	protected Response serve(String uri, String method, Properties header, Properties params, Properties multiPartSegments) {
		try {
			requestHandler.url(uri);
			for (Object param : params.keySet()) {
				requestHandler.param(param.toString(), params.getProperty(param.toString()));
			}
			return new Response(requestHandler.getResponseStatus(), MimeType.MIME_HTML.toString(), requestHandler.returnValue().toString());
		} catch (java.lang.Throwable e) {
			this.thrown = e;
			stop();
		}

		return null;

	}

	public void assertThatAllExpectationsAreMet() {
		stop();
		releaseServerInstance();
		if (thrown != null) {
			throw new AssertionError(thrown);
		}
		context.assertIsSatisfied();
	}

    private void releaseServerInstance() {
        mockServers.remove(this.getMyTcpPort());
    }


}
