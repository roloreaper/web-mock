package org.httpmock;

import com.meterware.httpunit.*;
import org.httpmock.server.MockHTTPServer;
import org.jmock.api.ExpectationError;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class MockHttpAcceptanceTest {
	private final String testUri = "/service/doSomething";
	public final int port = 7666;
	private final String serverUrl = "http://localhost:" + port + "/";

	@Test()
	public void shouldFailIfUriExpectedNotInvoked() throws IOException, SAXException {
		assertThrows(ExpectationError.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
				builder.createNewExpectation().withExpectedURI(testUri);
				MockHTTPServer server = builder.build(port);
				server.assertThatAllExpectationsAreMet();
			}
		});

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
	public void shouldHandlesGetWebRequestURIAndMatcherBody() throws IOException, SAXException {
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		builder.createNewExpectation().withExpectedURI(testUri).withBodyMatching(containsString("bob"));

		MockHTTPServer server = builder.build(port);
		byte[] buff ="bob".getBytes();
		PostMethodWebRequest postMethodWebRequest = new PostMethodWebRequest(serverUrl + testUri,new ByteArrayInputStream(buff), "text/plain");
		WebConversation wc = new WebConversation();
		wc.getResponse(postMethodWebRequest);
		server.assertThatAllExpectationsAreMet();
	}

	@Test
	public void shouldHandlesGetWebRequestWithParameters() throws IOException, SAXException {
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		String returnValue = "theReturnValue";
		builder.createNewExpectation().withExpectedURI(testUri).withExpectedParam("why", List.of("yes")).willReturn(returnValue);
		MockHTTPServer server = builder.build(port);
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
		builder.createNewExpectation().withExpectedURI(testUri).withExpectedParam("why", List.of("yes")).willReturn(returnValue);
		MockHTTPServer server =builder.build(port);
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
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		builder.createNewExpectation().withExpectedURI("/tester").withExpectedParam("testField", List.of("hello")).willReturn("");
		MockHTTPServer server =builder.build(8082);
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
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		builder.createNewExpectation().withExpectedURI("/tester").withExpectedParam("testField", List.of("hello")).willReturn(returnValue, 400);
		MockHTTPServer server = builder.build(8082);
		WebResponse response = wc.getResponse(webRequest);
		assertThat(response.getResponseCode(), is(400));
		server.assertThatAllExpectationsAreMet();
	}

	@Test
	public void shouldHandlesGetWebRequestWithErrorCode() throws IOException, SAXException {
		MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
		String returnValue = "theReturnValue";
		builder.createNewExpectation().withExpectedURI(testUri).withExpectedParam("why", List.of("yes")).willReturn(returnValue, 400);
		MockHTTPServer server = builder.build(port);
		GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(serverUrl + testUri);
		getMethodWebRequest.setParameter("why", "yes");
		WebConversation wc = new WebConversation();
		wc.setExceptionsThrownOnErrorStatus(false);
		WebResponse response = wc.getResponse(getMethodWebRequest);
		assertThat(response.getResponseCode(), is(400));
		server.assertThatAllExpectationsAreMet();
	}

	@Test()
	public void shouldThrowErrorWhenCallCountIsOverStepped() throws IOException, SAXException {
		assertThrows(AssertionError.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				MockHTTPServerBuilder builder = new MockHTTPServerBuilder();
				String returnValue = "theReturnValue";
				builder.createNewExpectation().withExpectedURI(testUri).withExpectedParam("why", List.of("yes")).willReturn(returnValue, 400);
				MockHTTPServer server =  builder.build(port);
				GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(serverUrl + testUri);
				getMethodWebRequest.setParameter("why", "yes");
				WebConversation wc = new WebConversation();
				wc.setExceptionsThrownOnErrorStatus(false);
				wc.getResponse(getMethodWebRequest);
				wc.getResponse(getMethodWebRequest);
				server.assertThatAllExpectationsAreMet();
			}
		});

	}

	@Test
	@Disabled("This server now takes too long to stop. So Not a feature anymore")
	public void shouldBeAbleToStopItSelfIfThePortIsStillInUseBuyMockHttpServer
			() throws IOException {
		new MockHTTPServerBuilder().build(port);
		new MockHTTPServerBuilder().build(port);

	}
}
