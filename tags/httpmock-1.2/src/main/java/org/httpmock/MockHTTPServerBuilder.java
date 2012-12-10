package org.httpmock;

import org.httpmock.server.MockHTTPServer;
import org.httpmock.server.RequestHandler;
import org.jmock.Expectations;
import org.jmock.Mockery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockHTTPServerBuilder {
	private RequestHandler requestHandler;
	private List<RequestExpectation> requestExpectations = new ArrayList<RequestExpectation>();
	private Mockery context;
	private Expectations expectations;

	public MockHTTPServerBuilder() {
		this.context = new Mockery();
		expectations = new Expectations();
		this.requestHandler = context.mock(RequestHandler.class);
	}

	/**
	 * This is used to Start the Http Server with the Configured Expectation on the port specified
	 *
	 * @param port eg 8080 for normal tomcat emulation
	 * @return The Started MockHTTPServer instance;
	 * @throws IOException if the Port is in use by other processes
	 */

	public MockHTTPServer build(int port) throws IOException {
		setUpExpectations();
        return MockHTTPServer.startServer(port, this.requestHandler, this.context);
	}

	private void setUpExpectations() {

		for (RequestExpectation requestExpectation : requestExpectations) {
			requestExpectation.initialiseExpectationsForHandler(requestHandler);
		}
		context.checking(expectations);

	}

	/**
	 * This will return the Next RequestExpectation Object for this server
	 * Example of use is
	 * MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
	 * mockHTTPServerBuilder.createNewExpectation().withExpectedUri("testing").willReturn("");
	 * mockHTTPServerBuilder.createNewExpectation().withExpectedUri("testing2").willReturn("");
	 * mockHTTPServerBuilder.build(8080);
	 *
	 * @return a requestExpectation object to add your request information to
	 */

	public RequestExpectation createNewExpectation() {
		RequestExpectation expectation = new RequestExpectation(this);
		requestExpectations.add(expectation);
		return expectation;
	}

	Mockery getContext() {
		return context;
	}

	Expectations getExpectations() {
		return expectations;
	}

	RequestHandler getRequestHandler() {
		return requestHandler;
	}
}
