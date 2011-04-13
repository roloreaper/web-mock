package org.httpmock;

import com.meterware.httpunit.*;
import org.httpmock.server.MockHTTPServer;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MockHttpServerTestCase {
  private final String testUri =  "/service/doSomething";
  public final int port = 7666;
  private final String serverUrl = "http://localhost:"+port+"/";

  @Test
    public void shouldHandlesGetWebRequest() throws IOException, SAXException {
        MockHTTPServer.RequestExpectationBuilder builder = new MockHTTPServer.RequestExpectationBuilder();
        String returnValue = "theReturnValue";
        MockHTTPServer server = builder.withExpectedURI(testUri).withExpectedParam("why","yes").willReturn(returnValue).build(port);
        GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(serverUrl +testUri);
        getMethodWebRequest.setParameter("why","yes");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getMethodWebRequest);
        assertThat(response.getText(),is(returnValue));
        server.assertAllExpectationsAreMet();
    }

    @Test
    public void shouldHandlesPostWebRequest() throws IOException, SAXException {
        WebConversation wc = new WebConversation();
        WebRequest webRequest = new PostMethodWebRequest("http://localhost:8082/tester");
        webRequest.setParameter("testField","hello");
        MockHTTPServer server = new MockHTTPServer.RequestExpectationBuilder().withExpectedURI("/tester").withExpectedParam("testField", "hello").willReturn("").build(8082);
        WebResponse wr = wc.getResponse(webRequest);
        assertThat(wr.getText(), is(""));
        server.assertAllExpectationsAreMet();
    }

    @Test
    public void shouldHandlesPostWebRequestWithErrorCode() throws IOException, SAXException {
        WebConversation wc = new WebConversation();
        wc.setExceptionsThrownOnErrorStatus(false);
        String returnValue = "theReturnValue";
        WebRequest webRequest = new PostMethodWebRequest("http://localhost:8082/tester");
        webRequest.setParameter("testField", "hello");
        MockHTTPServer server = new MockHTTPServer.RequestExpectationBuilder().withExpectedURI("/tester").withExpectedParam("testField", "hello").willReturn(returnValue,400).build(8082);
        WebResponse response = wc.getResponse(webRequest);
        assertThat(response.getResponseCode(), is(400));
        server.assertAllExpectationsAreMet();
    }

    @Test
    public void shouldHandlesGetWebRequestWithErrorCode() throws IOException, SAXException {
        MockHTTPServer.RequestExpectationBuilder builder = new MockHTTPServer.RequestExpectationBuilder();
        String returnValue = "theReturnValue";
        MockHTTPServer server = builder.withExpectedURI(testUri).withExpectedParam("why", "yes").willReturn(returnValue, 400).build(port);
        GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(serverUrl +testUri);
        getMethodWebRequest.setParameter("why", "yes");
        WebConversation wc = new WebConversation();
        wc.setExceptionsThrownOnErrorStatus(false);
        WebResponse response = wc.getResponse(getMethodWebRequest);
        assertThat(response.getResponseCode(),is(400));
        server.assertAllExpectationsAreMet();
    }

  @Test
  public void shouldThrowErrorWhenCallCountIsOverStepped() throws IOException, SAXException {
    MockHTTPServer.RequestExpectationBuilder builder = new MockHTTPServer.RequestExpectationBuilder();
    String returnValue = "theReturnValue";
    MockHTTPServer server = builder.withExpectedURI(testUri,1).withExpectedParam("why", "yes").willReturn(returnValue, 400).build(port);
    GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(serverUrl +testUri);
    getMethodWebRequest.setParameter("why", "yes");
    WebConversation wc = new WebConversation();
    wc.setExceptionsThrownOnErrorStatus(false);
    WebResponse response = wc.getResponse(getMethodWebRequest);
    assertThat(response.getResponseCode(), is(400));
    server.assertAllExpectationsAreMet();
  }

  @Test(expected = java.net.BindException.class)
  public void shouldStillFailToStartIfPortIsInUseBuyNonMockHttpServer() throws IOException {
    int portInUseByPostgres = 5432;
    new MockHTTPServer.RequestExpectationBuilder().build(portInUseByPostgres);
  }

  @Test
  public void shouldBeAbleToAccessServersStarted() throws IOException {
    MockHTTPServer mockHTTPServer1 = new MockHTTPServer.RequestExpectationBuilder().build(8089);
    assertThat(mockHTTPServer1, is(MockHTTPServer.getServerOnPort(8089)));
  }

  @Test
  public void shouldBeAbleToStopItSelfIfThePortIsStillInUseBuyMockHttpServer() throws IOException {
    new MockHTTPServer.RequestExpectationBuilder().build(port);
    new MockHTTPServer.RequestExpectationBuilder().build(port);

  }
}
