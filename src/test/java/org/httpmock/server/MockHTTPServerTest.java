package org.httpmock.server;

import fi.iki.elonen.NanoHTTPD;
import org.httpmock.MockHTTPServerBuilder;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MockHTTPServerTest {
	public final int port = 7666;

	@Test(expected = java.net.BindException.class)
	public void shouldStillFailToStartIfPortIsInUseByNonMockHttpServer() throws IOException {
		int portInUse = 5432;
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(portInUse);
		} catch (BindException e) {
			System.out.println("Socket Already in use so ignoring");
		}

		try {
			new MockHTTPServerBuilder().build(portInUse);
		} finally {
			if (serverSocket != null) {
				serverSocket.close();
			}
		}
	}

	@Test
	public void testParameterPassingOnPost() throws IOException, NanoHTTPD.ResponseException {
		Mockery mockery = new Mockery();
		RequestHandler requestHandler = mockery.mock(RequestHandler.class);
		MockHTTPServer mockHTTPServer = new MockHTTPServer(10, requestHandler, mockery);
		NanoHTTPD.IHTTPSession session = mockery.mock(NanoHTTPD.IHTTPSession.class);
		mockery.checking(new Expectations() {{
			atLeast(1).of(session).getMethod();
			will(returnValue(NanoHTTPD.Method.POST));
			oneOf(session).parseBody(with(equal(new HashMap<String, String>())));
			oneOf(session).getUri();
			String uri = "uri";
			will(returnValue(uri));
			oneOf(requestHandler).url(uri);
			oneOf(session).getHeaders();
			will(returnValue(new HashMap<String, String>()));
			oneOf(session).getParms();
			will(returnValue(new HashMap<String,String>()));
			oneOf(requestHandler).getResponseStatus();
			will(returnValue(200));
			oneOf(requestHandler).returnValue();
			will(returnValue("TEST"));
		}
		});
		mockHTTPServer.serve(session);
		mockHTTPServer.assertThatAllExpectationsAreMet();
	}

	@Test
	public void testParameterPassingOnPostThrowNanoHTTPDResponseException() throws IOException, NanoHTTPD.ResponseException {
		Mockery mockery = new Mockery();
		RequestHandler requestHandler = mockery.mock(RequestHandler.class);
		MockHTTPServer mockHTTPServer = new MockHTTPServer(10, requestHandler, mockery);
		NanoHTTPD.IHTTPSession session = mockery.mock(NanoHTTPD.IHTTPSession.class);
		mockery.checking(new Expectations() {{
			atLeast(1).of(session).getMethod();
			will(returnValue(NanoHTTPD.Method.POST));
			oneOf(session).parseBody(with(equal(new HashMap<String, String>())));
			will(throwException(new NanoHTTPD.ResponseException(NanoHTTPD.Response.Status.BAD_REQUEST, "TEST")));
			oneOf(session).getUri();
			String uri = "uri";
			will(returnValue(uri));
			oneOf(requestHandler).url(uri);
			oneOf(session).getHeaders();
			will(returnValue(new HashMap<String, String>()));
			oneOf(session).getParms();
			will(returnValue(new HashMap<String, String>()));
			oneOf(requestHandler).getResponseStatus();
			will(returnValue(200));
			oneOf(requestHandler).returnValue();
			will(returnValue("TEST"));
		}
		});
		mockHTTPServer.serve(session);
		mockHTTPServer.assertThatAllExpectationsAreMet();
	}

	@Test
	public void testInvalidReturnCode() throws IOException, NanoHTTPD.ResponseException {
		Mockery mockery = new Mockery();
		RequestHandler requestHandler = mockery.mock(RequestHandler.class);
		MockHTTPServer mockHTTPServer = new MockHTTPServer(10, requestHandler, mockery);
		NanoHTTPD.IHTTPSession session = mockery.mock(NanoHTTPD.IHTTPSession.class);
		mockery.checking(new Expectations() {{
			atLeast(1).of(session).getMethod();
			will(returnValue(NanoHTTPD.Method.POST));
			oneOf(session).parseBody(with(equal(new HashMap<String, String>())));
			will(throwException(new NanoHTTPD.ResponseException(NanoHTTPD.Response.Status.BAD_REQUEST,"TEST")));
			oneOf(session).getUri();
			String uri = "uri";
			will(returnValue(uri));
			oneOf(requestHandler).url(uri);
			oneOf(session).getHeaders();
			will(returnValue(new HashMap<String, String>()));
			oneOf(session).getParms();
			will(returnValue(new HashMap<String, String>()));
			oneOf(requestHandler).getResponseStatus();
			will(returnValue(2));
			oneOf(requestHandler).returnValue();
			will(returnValue("TEST"));
		}
		});
		mockHTTPServer.serve(session);
		mockHTTPServer.assertThatAllExpectationsAreMet();
	}

	@Test
	public void shouldBeAbleToAccessServersStarted() throws IOException {
		MockHTTPServer mockHTTPServer1 = new MockHTTPServerBuilder().build(8089);
		assertThat(mockHTTPServer1, is(MockHTTPServer.getServerOnPort(8089)));
	}

	@Test
	public void shouldBeAbleToAssertExpectationsOnOneServerWithoutStoppingOtherServers() throws IOException {
		Mockery context = new Mockery();
		RequestHandler requestHandler = context.mock(RequestHandler.class);
		MockHTTPServer mockServerNotStopped = MockHTTPServer.startServer(port, requestHandler, context);
		MockHTTPServer mockServer = MockHTTPServer.startServer(port + 1, requestHandler, context);
		mockServer.assertThatAllExpectationsAreMet();
		new Socket("localhost", port);
		mockServerNotStopped.assertThatAllExpectationsAreMet();

	}

	@Test
	public void shouldBeAbleToStopAllServers() throws IOException {
		Mockery context = new Mockery();
		RequestHandler requestHandler = context.mock(RequestHandler.class);
		MockHTTPServer mockServerNotStopped = MockHTTPServer.startServer(port, requestHandler, context);
		MockHTTPServer mockServer = MockHTTPServer.startServer(port + 1, requestHandler, context);
		MockHTTPServer.stopAllServers();
		ServerSocket reusingPort = new ServerSocket(port + 1);
		reusingPort.close();
		reusingPort = new ServerSocket(port);
		reusingPort.close();
	}
}
