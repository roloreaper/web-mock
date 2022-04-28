package org.httpmock.server;

import fi.iki.elonen.NanoHTTPD;
import org.jmock.Mockery;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		MockHTTPServer currentServer = mockServers.get(port);
		if (currentServer == null) {
			currentServer = new MockHTTPServer(port, requestHandler, context);
			mockServers.put(port, currentServer);
			currentServer.start();
		}

		return currentServer;
	}

	MockHTTPServer(int port, RequestHandler requestHandler, Mockery context) throws IOException {
		super(port);
		this.requestHandler = requestHandler;
		this.context = context;
	}

    @Override
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
		String body =null;
		if (session.getMethod().equals(Method.POST)) {
			body = getBody(session);
        }
        return serve(session.getUri(), session.getMethod(), session.getHeaders(), session.getParameters(), body);
    }

    private String getBody(IHTTPSession session) {
        HashMap<String, String> stuff = new HashMap<>();
        try {
            session.parseBody(stuff);
			if (stuff.get("postData") != null) {
				return stuff.get("postData");
			}
        } catch (IOException|ResponseException e) {
            e.printStackTrace();
        }
        return null;
    }



    private NanoHTTPD.Response serve(String uri, Method method, Map<String, String> headers, Map<String, List<String>> params, String body) {
		try {
			requestHandler.url(uri);
			for (String param : params.keySet()) {
				requestHandler.param(param, params.get(param));
			}

			if (body!=null) {
				requestHandler.bodyMatching(body);
			}
			Response response = NanoHTTPD.newFixedLengthResponse(getStatus(requestHandler.getResponseStatus()), null, requestHandler.returnValue().toString());
            return response;
		} catch (java.lang.Throwable e) {
			this.thrown = e;
			//stop();
		}

		return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, null, "Unexpected event :" + thrown.getMessage());

	}

    private Response.IStatus getStatus(int responseStatus) {

        for (Response.Status status : Response.Status.values()) {
            if (status.getRequestStatus()==responseStatus) {
                return status;
            }
        }
        return Response.Status.INTERNAL_ERROR;
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

		mockServers.remove(this.getListeningPort());
    }


}
