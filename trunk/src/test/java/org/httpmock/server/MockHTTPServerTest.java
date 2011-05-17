package org.httpmock.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import org.httpmock.MockHTTPServerBuilder;
import org.jmock.Mockery;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class MockHTTPServerTest {
  public final int port = 7666;

  @Test(expected = java.net.BindException.class)
  public void shouldStillFailToStartIfPortIsInUseByNonMockHttpServer() throws IOException {
    int portInUse = 5432;
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(portInUse);
    }
    catch (BindException e) {
      System.out.println("Socket Already in use so ignoring");
    }

    try {
      new MockHTTPServerBuilder().build(portInUse);
    }
    finally {
      if (serverSocket != null) {
        serverSocket.close();
      }
    }
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
    MockHTTPServer mockServerNotStopped = MockHTTPServer.startServer(port, requestHandler,context);
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
    ServerSocket reusingPort = new ServerSocket( port + 1);
    reusingPort.close();
    reusingPort = new ServerSocket( port);
    reusingPort.close();
  }
}
