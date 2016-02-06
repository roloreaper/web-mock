package org.httpmock.server;

import fi.iki.elonen.NanoHTTPD;
import org.jmock.Mockery;
import org.omg.IOP.TAG_JAVA_CODEBASE;

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

		MockHTTPServer currentServer = mockServers.get(port);
		if (currentServer == null) {
			currentServer = new MockHTTPServer(port, requestHandler, context);
			mockServers.put(port, currentServer);
			currentServer.start();
		} else {
			if (currentServer.isAlive()) {
				throw new RuntimeException("Server is alreadyRunning");
			}
			if (currentServer.wasStarted()) {
				throw new RuntimeException("Server been started");
			}

				currentServer.start();
		}

		while (!currentServer.isAlive()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
        if (session.getMethod().equals(Method.POST)) {
            return serve(session.getUri(), session.getMethod(), session.getHeaders(), getBodyParameters(session));
        }
        return serve(session.getUri(),session.getMethod(),session.getHeaders(),session.getParms());
    }

    private Map<String, String> getBodyParameters(IHTTPSession session) {
        HashMap<String, String> stuff = new HashMap<String ,String>();
        try {
            session.parseBody(stuff);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        return session.getParms();
    }



    private NanoHTTPD.Response serve(String uri, Method method, Map<String,String> headers, Map<String,String> params) {
		try {
			requestHandler.url(uri);
			for (String param : params.keySet()) {
				requestHandler.param(param, params.get(param));
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
		boolean isAlive = true;
		while (isAlive) {
			System.out.println("this.isAlive() = " + this.isAlive());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			isAlive = this.isAlive();

		}

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
