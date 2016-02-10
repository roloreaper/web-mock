package org.httpmock;

import org.hamcrest.Matcher;
import org.httpmock.server.HTTPStatusCode;
import org.httpmock.server.RequestHandler;
import org.jmock.Expectations;

import java.util.HashMap;
import java.util.Map;

public class RequestExpectation {

	private MockHTTPServerBuilder mockHTTPServerBuilder;
	private String uri;
	private int numberTimeExpectationMustBeMet = 1;
	private String returnValue;
	private Map<String, String> params = new HashMap<String, String>();
	private int statusCodeReturned = HTTPStatusCode.HTTP_OK.getCode();
	private Matcher<String> matcher;
	private String method = "GET";

	RequestExpectation(MockHTTPServerBuilder mockHTTPServerBuilder) {
		this.mockHTTPServerBuilder = mockHTTPServerBuilder;
	}

	public RequestExpectation withPOSTBodyMatching(Matcher<String> matcher) {
		this.method="POST";
		this.matcher = matcher;
		return this;
	}

	/**
	 * This is the way u express a call to a resource say http://testserver:8080/resource/doSomthing
	 * example MockHTTPServerBuilder.createRequestExpectation().withExpectedURI("resource/doSomthing").getMockHTTPServerBuilder().build(8080);
	 *
	 * @param uri resource/doSomthing
	 * @return returns this for chaining and readability
	 */

	public RequestExpectation withExpectedURI(String uri) {
		this.uri = uri;
		return this;
	}

	/**
	 * This is the way u express a call to a resource say http://testserver:8080/resource/doSomthing
	 * with post or get parameters
	 * example MockHTTPServerBuilder.createRequestExpectation().withExpectedURI("resource/doSomthing",2).withExpectedParam("id","1").getMockHTTPServerBuilder().build(8080);
	 *
	 * @param param the name of the form-param
	 * @param value the string value for that param
	 * @return returns this for chaining and readability
	 */
	public RequestExpectation withExpectedParam(String param, String value) {
		this.params.put(param, value);
		return this;
	}

	/**
	 *
	 * @param params Map of expected parameters where the first is the Parameter Name the second the value
	 * @return returns this for chaining and readability;
	 */
	public RequestExpectation withExpectedParameters(Map<String, String> params) {
		this.params.putAll(params);
		return this;
	}

	/**
	 *
	 */
	public RequestExpectation willBeInvoked(int numberTimeExpectationMustBeMet) {
		this.numberTimeExpectationMustBeMet = numberTimeExpectationMustBeMet;
		return this;
	}

	/**
	 * This is the way u express a call to a resource say http://testserver:8080/resource/doSomthing will return a html,xml or string
	 * example MockHTTPServerBuilder.createRequestExpectation().withExpectedURI("resource/doSomthing",2).willReturn("test string").getMockHTTPServerBuilder().build(8080);
	 * Then the Server when asked for http://testserver:8080/resource/doSomthing the dataSpecified will be returned
	 *
	 * @param returnValue the Exact return of the call
	 * @return returns this for chaining and readability
	 */
	public RequestExpectation willReturn(String returnValue) {
		return willReturn(returnValue, HTTPStatusCode.HTTP_OK.getCode());
	}

	/**
	 * This is the way u express a call to a resource say http://testserver:8080/resource/doSomthing will return a html,xml or string
	 * example MockHTTPServerBuilder.createRequestExpectation().withExpectedURI("resource/doSomthing",2).willReturn("test string",200).getMockHTTPServerBuilder().build(8080);
	 * Then the Server when asked for http://testserver:8080/resource/doSomthing the dataSpecified will be returned and it will be returned useing the HTTP Status code specified
	 *
	 * @param returnValue the Exact return of the call
	 * @return returns this for chaining and readability
	 */
	public RequestExpectation willReturn(String returnValue, int statusCode) {
		this.returnValue = returnValue;
		this.statusCodeReturned = statusCode;
		return this;
	}

	/**
	 * The MockHTTPServerBuilder the Expectation belongs to
	 *
	 * @return will return the mockHTTPServerBuilder
	 */

	public MockHTTPServerBuilder getMockHTTPServerBuilder() {
		return mockHTTPServerBuilder;
	}

	void initialiseExpectationsForHandler(RequestHandler requestHandler) {
		Expectations expectations = mockHTTPServerBuilder.getExpectations();
		for (int executionCount = 0; executionCount < numberTimeExpectationMustBeMet; executionCount++) {
			if (uri != null) {
				expectations.oneOf(requestHandler).url(expectations.with(uri));
				if (returnValue != null) {
					expectations.oneOf(requestHandler).returnValue();
					expectations.will(expectations.returnValue(returnValue));

				} else {
					expectations.oneOf(requestHandler).returnValue();
				}

				expectations.oneOf(requestHandler).getResponseStatus();
				expectations.will(expectations.returnValue(statusCodeReturned));
				if (matcher!=null) {
					expectations.oneOf(requestHandler).bodyMatching(expectations.with(matcher));
				}

			}

			if (!params.isEmpty()) {
				for (String param : params.keySet()) {
					expectations.oneOf(requestHandler).param(expectations.with(param), expectations.with(params.get(param)));
				}
			}
		}
	}
}