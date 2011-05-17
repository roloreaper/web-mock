package org.httpmock;

import org.httpmock.server.HTTPStatusCode;
import org.httpmock.server.RequestHandler;
import org.jmock.Mockery;
import org.jmock.api.ExpectationError;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
    requestExpectation.withExpectedURI("test").withExpectedParam("param","value");
    requestExpectation.initialiseExpectationsForHandler(requestHandler);
    context.checking(mockHTTPServerBuilder.getExpectations());
    requestHandler.url("test");
    requestHandler.param("param","value");
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
    requestExpectation.withExpectedURI("test").withExpectedParam("param", "value");
    requestExpectation.willBeInvoked(2);
    requestExpectation.initialiseExpectationsForHandler(requestHandler);
    context.checking(mockHTTPServerBuilder.getExpectations());
    requestHandler.url("test");
    requestHandler.param("param", "value");
    requestHandler.getResponseStatus();
    requestHandler.returnValue();
    requestHandler.url("test");
    requestHandler.param("param", "value");
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
    assertThat(requestHandler.returnValue().toString(),is("testReturn"));

    context.assertIsSatisfied();
  }

  @Test
  public void testWillReturnWithErrorCode() throws Exception {
    MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
    Mockery context = mockHTTPServerBuilder.getContext();
    RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
    RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
    requestExpectation.withExpectedURI("test");
    requestExpectation.willReturn("",HTTPStatusCode.HTTP_BADREQUEST.getCode());
    requestExpectation.initialiseExpectationsForHandler(requestHandler);
    context.checking(mockHTTPServerBuilder.getExpectations());
    requestHandler.url("test");
    requestHandler.returnValue();
    assertThat(requestHandler.getResponseStatus(),is(HTTPStatusCode.HTTP_BADREQUEST.toString()));
    context.assertIsSatisfied();

  }

  @Test
  public void testGetRequestExpectationBuilder() throws Exception {
    MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
    RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);

    assertThat(requestExpectation.getMockHTTPServerBuilder(),is(mockHTTPServerBuilder));
  }

  @Test(expected = ExpectationError.class)
  public void assertThatWithExpectedURIAddsAnExpectationforUrlWillFailIfNotInvoked() throws Exception {
    MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
    Mockery context = mockHTTPServerBuilder.getContext();
    RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
    RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
    requestExpectation.withExpectedURI("test");
    requestExpectation.initialiseExpectationsForHandler(requestHandler);
    context.checking(mockHTTPServerBuilder.getExpectations());
    context.assertIsSatisfied();

  }

  @Test(expected = ExpectationError.class)
  public void testWithExpectedURIAndCountWillFailIfNotInvoked() throws Exception {
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

  @Test(expected = ExpectationError.class)
  public void testWithExpectedParamFailsIfNotInvoked() throws Exception {
    MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
    Mockery context = mockHTTPServerBuilder.getContext();
    RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
    RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
    requestExpectation.withExpectedURI("test").withExpectedParam("param", "value");
    requestExpectation.initialiseExpectationsForHandler(requestHandler);
    context.checking(mockHTTPServerBuilder.getExpectations());
    requestHandler.url("test");
    requestHandler.getResponseStatus();
    requestHandler.returnValue();
    context.assertIsSatisfied();
  }

  @Test(expected = ExpectationError.class)
  public void testWithExpectedParamWithCountIfNotInvoked() throws Exception {
    MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
    Mockery context = mockHTTPServerBuilder.getContext();
    RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
    RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
    requestExpectation.withExpectedURI("test").withExpectedParam("param", "value");
    requestExpectation.willBeInvoked(2);
    requestExpectation.initialiseExpectationsForHandler(requestHandler);
    context.checking(mockHTTPServerBuilder.getExpectations());
    requestHandler.url("test");
    requestHandler.param("param", "value");
    requestHandler.getResponseStatus();
    requestHandler.returnValue();
    requestHandler.url("test");
    requestHandler.getResponseStatus();
    requestHandler.returnValue();
    context.assertIsSatisfied();
  }

  @Test(expected = ExpectationError.class)
  public void testWillReturnIfNotInvoked() throws Exception {
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

  @Test(expected = ExpectationError.class)
  public void testWillReturnWithErrorCodeIfNotInvoked() throws Exception {
    MockHTTPServerBuilder mockHTTPServerBuilder = new MockHTTPServerBuilder();
    Mockery context = mockHTTPServerBuilder.getContext();
    RequestHandler requestHandler = mockHTTPServerBuilder.getRequestHandler();
    RequestExpectation requestExpectation = new RequestExpectation(mockHTTPServerBuilder);
    requestExpectation.withExpectedURI("test");
    requestExpectation.willReturn("", HTTPStatusCode.HTTP_BADREQUEST.getCode());
    requestExpectation.initialiseExpectationsForHandler(requestHandler);
    context.checking(mockHTTPServerBuilder.getExpectations());
    requestHandler.url("test");
    requestHandler.returnValue();
    context.assertIsSatisfied();

  }
}
