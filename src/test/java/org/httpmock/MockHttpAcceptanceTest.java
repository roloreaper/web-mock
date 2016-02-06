package org.httpmock;

import com.meterware.httpunit.*;
import org.httpmock.server.MockHTTPServer;
import org.jmock.api.ExpectationError;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MockHttpAcceptanceTest {
	private final String testUri = "/service/doSomething";
	public final int port = 7666;
	private final String serverUrl = "http://localhost:" + port + "/";

	@Test(expected = ExpectationError.class)
	public void shouldFailIfUriExpectedNotInvoked() throws IOException, SAXException {
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		builder.createNewExpectation().withExpectedURI(testUri);
		MockHTTPServer server = builder.build(port);
		server.assertThatAllExpectationsAreMet();
	}


	@Test
	public void shouldHandlesGetWebRequestURI() throws IOException, SAXException {
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		builder.createNewExpectation().withExpectedURI(testUri);
		MockHTTPServer server = builder.build(port);
		GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(serverUrl + testUri);
		WebConversation wc = new WebConversation();
		wc.getResponse(getMethodWebRequest);
		server.assertThatAllExpectationsAreMet();
	}

	@Test
	public void shouldHandlesGetWebRequestWithParameters() throws IOException, SAXException {
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		String returnValue = "theReturnValue";
		MockHTTPServer server = builder.createNewExpectation().withExpectedURI(testUri).withExpectedParam("why", "yes").willReturn(returnValue).getMockHTTPServerBuilder().build(port);
		GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(serverUrl + testUri);
		getMethodWebRequest.setParameter("why", "yes");
		WebConversation wc = new WebConversation();
		wc.getResponse(getMethodWebRequest);
		server.assertThatAllExpectationsAreMet();
	}

	@Test
	public void shouldHandlesGetWebRequestWithParametersAndReturnValue() throws IOException, SAXException {
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		String returnValue = "theReturnValue";
		MockHTTPServer server = builder.createNewExpectation().withExpectedURI(testUri).withExpectedParam("why", "yes").willReturn(returnValue).getMockHTTPServerBuilder().build(port);
		GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(serverUrl + testUri);
		getMethodWebRequest.setParameter("why", "yes");
		WebConversation wc = new WebConversation();
		WebResponse response = wc.getResponse(getMethodWebRequest);
		assertThat(response.getText(), is(returnValue));
		server.assertThatAllExpectationsAreMet();
	}

	@Test
	public void shouldHandlesPostWebRequest() throws IOException, SAXException {
		WebConversation wc = new WebConversation();
		WebRequest webRequest = new PostMethodWebRequest("http://localhost:8082/tester");
		webRequest.setParameter("testField", "hello");
		MockHTTPServer server = new MockHTTPServerBuilder().createNewExpectation().withExpectedURI("/tester").withExpectedParam("testField", "hello").willReturn("").getMockHTTPServerBuilder().build(8082);
		WebResponse wr = wc.getResponse(webRequest);
		assertThat(wr.getText(), is(""));
		server.assertThatAllExpectationsAreMet();
	}

	@Test
	public void shouldHandlesPostWebRequestWithErrorCode() throws IOException, SAXException {
		WebConversation wc = new WebConversation();
		wc.setExceptionsThrownOnErrorStatus(false);
		String returnValue = "theReturnValue";
		WebRequest webRequest = new PostMethodWebRequest("http://localhost:8082/tester");
		webRequest.setParameter("testField", "hello");
		MockHTTPServer server = new MockHTTPServerBuilder().createNewExpectation().withExpectedURI("/tester").withExpectedParam("testField", "hello").willReturn(returnValue, 400).getMockHTTPServerBuilder().build(8082);
		WebResponse response = wc.getResponse(webRequest);
		assertThat(response.getResponseCode(), is(400));
		server.assertThatAllExpectationsAreMet();
	}

	@Test
	public void shouldHandlesGetWebRequestWithErrorCode() throws IOException, SAXException {
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		String returnValue = "theReturnValue";
		MockHTTPServer server = builder.createNewExpectation().withExpectedURI(testUri).withExpectedParam("why", "yes").willReturn(returnValue, 400).getMockHTTPServerBuilder().build(port);
		GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(serverUrl + testUri);
		getMethodWebRequest.setParameter("why", "yes");
		WebConversation wc = new WebConversation();
		wc.setExceptionsThrownOnErrorStatus(false);
		WebResponse response = wc.getResponse(getMethodWebRequest);
		assertThat(response.getResponseCode(), is(400));
		server.assertThatAllExpectationsAreMet();
	}

	@Test(expected = AssertionError.class)
	public void shouldThrowErrorWhenCallCountIsOverStepped() throws IOException, SAXException {
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		String returnValue = "theReturnValue";
		MockHTTPServer server = builder.createNewExpectation().withExpectedURI(testUri).withExpectedParam("why", "yes").willReturn(returnValue, 400).getMockHTTPServerBuilder().build(port);
		GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(serverUrl + testUri);
		getMethodWebRequest.setParameter("why", "yes");
		WebConversation wc = new WebConversation();
		wc.setExceptionsThrownOnErrorStatus(false);
		wc.getResponse(getMethodWebRequest);
		wc.getResponse(getMethodWebRequest);
		server.assertThatAllExpectationsAreMet();
	}

	@Test
	@Ignore("This server now takes too long to stop. So Not a feature anymore")
	public void shouldBeAbleToStopItSelfIfThePortIsStillInUseBuyMockHttpServer
			() throws IOException {
		new MockHTTPServerBuilder().build(port);
		new MockHTTPServerBuilder().build(port);

	}
}
