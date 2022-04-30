package org.webmock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.webmock.server.RequestHandler;
import org.jmock.Mockery;
import org.jmock.api.ExpectationError;

import java.util.List;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RequestExpectationTest {
	@Test
	public void assertThatWithExpectedURIAddsAnExpectationforUrl() throws Exception {
		MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
		Mockery context = mockHTTPServerBuilder.getContext();
		RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
		RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
		requestExpectation.withExpectedURI("test");
		requestExpectation.initialiseExpectationsForHandler(requestHandler);
		context.checking(mockHTTPServerBuilder.getExpectations());
		requestHandler.url("test");
		requestHandler.getResponseStatus();
		requestHandler.returnValue();
		context.assertIsSatisfied();


	}

	@Test
	public void testMatcher() {
		MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
		Mockery context = mockHTTPServerBuilder.getContext();
		RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
		RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
		requestExpectation.withExpectedURI("test");
		requestExpectation.withBodyMatching(containsString("BOB"));
		requestExpectation.initialiseExpectationsForHandler(requestHandler);
		context.checking(mockHTTPServerBuilder.getExpectations());
		requestHandler.url("test");
		requestHandler.returnValue();
		requestHandler.getResponseStatus();
		requestHandler.bodyMatching("HOHBOBBO");

		context.assertIsSatisfied();
	}

	@Test
	public void assertRequestWithNoExpectationsThrowsNoExeptions() throws Exception {
		MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
		Mockery context = mockHTTPServerBuilder.getContext();
		RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
		RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
		requestExpectation.initialiseExpectationsForHandler(requestHandler);
		context.checking(mockHTTPServerBuilder.getExpectations());
		context.assertIsSatisfied();

	}

	@Test
	public void testWithExpectedURIAndCount() throws Exception {
		MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
		Mockery context = mockHTTPServerBuilder.getContext();
		RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
		RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
		requestExpectation.withExpectedURI("test");
		requestExpectation.willBeInvoked(2);
		requestExpectation.initialiseExpectationsForHandler(requestHandler);
		context.checking(mockHTTPServerBuilder.getExpectations());
		requestHandler.url("test");
		requestHandler.getResponseStatus();
		requestHandler.returnValue();
		requestHandler.url("test");
		requestHandler.getResponseStatus();
		requestHandler.returnValue();
		context.assertIsSatisfied();
	}

	@Test
	public void testWithExpectedParam() throws Exception {
		MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
		Mockery context = mockHTTPServerBuilder.getContext();
		RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
		RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
		requestExpectation.withExpectedURI("test").withExpectedParam("param", List.of("value"));
		requestExpectation.initialiseExpectationsForHandler(requestHandler);
		context.checking(mockHTTPServerBuilder.getExpectations());
		requestHandler.url("test");
		requestHandler.param("param", List.of("value"));
		requestHandler.getResponseStatus();
		requestHandler.returnValue();
		context.assertIsSatisfied();
	}

	@Test
	public void testWithExpectedParamWithCount() throws Exception {
		MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
		Mockery context = mockHTTPServerBuilder.getContext();
		RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
		RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
		requestExpectation.withExpectedURI("test").withExpectedParam("param", List.of("value"));
		requestExpectation.willBeInvoked(2);
		requestExpectation.initialiseExpectationsForHandler(requestHandler);
		context.checking(mockHTTPServerBuilder.getExpectations());
		requestHandler.url("test");
		requestHandler.param("param", List.of("value"));
		requestHandler.getResponseStatus();
		requestHandler.returnValue();
		requestHandler.url("test");
		requestHandler.param("param", List.of("value"));
		requestHandler.getResponseStatus();
		requestHandler.returnValue();
		context.assertIsSatisfied();
	}

	@Test
	public void testWillReturn() throws Exception {
		MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
		Mockery context = mockHTTPServerBuilder.getContext();
		RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
		RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
		requestExpectation.withExpectedURI("test");
		requestExpectation.willReturn("testReturn");
		requestExpectation.initialiseExpectationsForHandler(requestHandler);
		context.checking(mockHTTPServerBuilder.getExpectations());
		requestHandler.url("test");
		requestHandler.getResponseStatus();
		assertThat(requestHandler.returnValue().toString(), is("testReturn"));

		context.assertIsSatisfied();
	}

	@Test
	public void testWillReturnWithErrorCode() throws Exception {
		MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
		Mockery context = mockHTTPServerBuilder.getContext();
		RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
		RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
		requestExpectation.withExpectedURI("test");
		requestExpectation.willReturn("", 400);
		requestExpectation.initialiseExpectationsForHandler(requestHandler);
		context.checking(mockHTTPServerBuilder.getExpectations());
		requestHandler.url("test");
		requestHandler.returnValue();
		assertThat(requestHandler.getResponseStatus(), is(400));
		context.assertIsSatisfied();

	}

	@Test
	public void testGetRequestExpectationBuilder() throws Exception {
		HTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
		RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);

		assertThat(requestExpectation.geHTTPServerBuilder(), is(mockHTTPServerBuilder));
	}

	@Test()
	public void assertThatWithExpectedURIAddsAnExpectationforUrlWillFailIfNotInvoked() throws Exception {
		assertThrows(ExpectationError.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
				Mockery context = mockHTTPServerBuilder.getContext();
				RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
				RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
				requestExpectation.withExpectedURI("test");
				requestExpectation.initialiseExpectationsForHandler(requestHandler);
				context.checking(mockHTTPServerBuilder.getExpectations());
				context.assertIsSatisfied();
			}
		});


	}

	@Test()
	public void testWithExpectedURIAndCountWillFailIfNotInvoked() throws Exception {
		assertThrows(ExpectationError.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
				Mockery context = mockHTTPServerBuilder.getContext();
				RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
				RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
				requestExpectation.withExpectedURI("test");
				requestExpectation.willBeInvoked(2);
				requestExpectation.initialiseExpectationsForHandler(requestHandler);
				context.checking(mockHTTPServerBuilder.getExpectations());
				requestHandler.url("test");
				requestHandler.getResponseStatus();
				requestHandler.returnValue();
				context.assertIsSatisfied();
			}
		});
	}

	@Test()
	public void testWithExpectedParamFailsIfNotInvoked() throws Exception {
		assertThrows(ExpectationError.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
				Mockery context = mockHTTPServerBuilder.getContext();
				RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
				RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
				requestExpectation.withExpectedURI("test").withExpectedParam("param", List.of("value"));
				requestExpectation.initialiseExpectationsForHandler(requestHandler);
				context.checking(mockHTTPServerBuilder.getExpectations());
				requestHandler.url("test");
				requestHandler.getResponseStatus();
				requestHandler.returnValue();
				context.assertIsSatisfied();
			}
		});
	}

	@Test()
	public void testWithExpectedParamWithCountIfNotInvoked() throws Exception {
		assertThrows(ExpectationError.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
				Mockery context = mockHTTPServerBuilder.getContext();
				RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
				RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
				requestExpectation.withExpectedURI("test").withExpectedParam("param", List.of("value"));
				requestExpectation.willBeInvoked(2);
				requestExpectation.initialiseExpectationsForHandler(requestHandler);
				context.checking(mockHTTPServerBuilder.getExpectations());
				requestHandler.url("test");
				requestHandler.param("param", List.of("value"));
				requestHandler.getResponseStatus();
				requestHandler.returnValue();
				requestHandler.url("test");
				requestHandler.getResponseStatus();
				requestHandler.returnValue();
				context.assertIsSatisfied();
			}
		});
	}

	@Test()
	public void testWillReturnIfNotInvoked() throws Exception {
		assertThrows(ExpectationError.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
				Mockery context = mockHTTPServerBuilder.getContext();
				RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
				RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
				requestExpectation.withExpectedURI("test");
				requestExpectation.willReturn("testReturn");
				requestExpectation.initialiseExpectationsForHandler(requestHandler);
				context.checking(mockHTTPServerBuilder.getExpectations());
				requestHandler.url("test");
				requestHandler.getResponseStatus();

				context.assertIsSatisfied();
			}
		});
	}

	@Test()
	public void testWillReturnWithErrorCodeIfNotInvoked() throws Exception {
		assertThrows(ExpectationError.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
				Mockery context = mockHTTPServerBuilder.getContext();
				RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
				RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
				requestExpectation.withExpectedURI("test");
				requestExpectation.willReturn("", 400);
				requestExpectation.initialiseExpectationsForHandler(requestHandler);
				context.checking(mockHTTPServerBuilder.getExpectations());
				requestHandler.url("test");
				requestHandler.returnValue();
				context.assertIsSatisfied();
			}
		});

	}

	@Test
	public void testUsingMatcher() throws Exception {
		MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
		Mockery context = mockHTTPServerBuilder.getContext();
		RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
		RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
		requestExpectation.withExpectedURI("/bob").withBodyMatching(containsString("Rowland")).initialiseExpectationsForHandler(requestHandler);
		context.checking(mockHTTPServerBuilder.getExpectations());
		requestHandler.url("/bob");
		requestHandler.returnValue();
		requestHandler.getResponseStatus();
		requestHandler.bodyMatching("YesRowland HahahahaRowlansadalksdjlakjd");
		context.assertIsSatisfied();

	}

	@Test()
	public void testUsingMatcherNotMatching() throws Exception {
		assertThrows(ExpectationError.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
				Mockery context = mockHTTPServerBuilder.getContext();
				RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
				RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
				requestExpectation.withExpectedURI("/bob").withBodyMatching(containsString("Samual")).initialiseExpectationsForHandler(requestHandler);
				context.checking(mockHTTPServerBuilder.getExpectations());
				requestHandler.url("/bob");
				requestHandler.returnValue();
				requestHandler.getResponseStatus();
				requestHandler.bodyMatching("YesRowland HahahahaRowlansadalksdjlakjd");
				context.assertIsSatisfied();
			}
		});

	}
}
