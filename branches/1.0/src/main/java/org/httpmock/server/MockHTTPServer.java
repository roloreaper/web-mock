package org.httpmock.server;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;

public class MockHTTPServer extends NanoHTTPD {
    private static HashMap<Integer, MockHTTPServer> mockServers = new HashMap<Integer,MockHTTPServer>();
    RequestExpectationBuilder requestExpectationBuilder ;

  public static MockHTTPServer getServerOnPort(int port) {
    return mockServers.get(port);
  }

  public static class RequestExpectationBuilder {
        private RequestHandler requestHandler;
        private Expectations requestExpectations =new Expectations();
        private Mockery context;

        public RequestExpectationBuilder() {
            this.context = new Mockery();
            this.requestHandler = context.mock(RequestHandler.class);
        }

        public RequestExpectationBuilder withExpectedURI(String uri) {
          return withExpectedURI(uri,1);
        }

        public RequestExpectationBuilder withExpectedURI(String uri, int count) {
            requestExpectations.exactly(count).of(requestHandler).url(requestExpectations.with(uri));
            return this;
        }

        public RequestExpectationBuilder withExpectedParam(String key, String value) {
            requestExpectations.oneOf(requestHandler).param(requestExpectations.with(key), requestExpectations.with(value));
            return this;
        }

        public RequestExpectationBuilder willReturn(Object obj) {
           return  willReturn(obj,200);
        }

        public RequestExpectationBuilder willReturn(Object obj,int code) {
            requestExpectations.oneOf(requestHandler).returnValue();
            requestExpectations.will(requestExpectations.returnValue(obj));
            requestExpectations.oneOf(requestHandler).getResponseStatus();
            switch (code) {
             case 301 :
                    requestExpectations.will(requestExpectations.returnValue(HTTP_REDIRECT));
                 break;
             case 400 : requestExpectations.will(requestExpectations.returnValue(HTTP_BADREQUEST));
                 break;
                case 403:
                    requestExpectations.will(requestExpectations.returnValue(HTTP_FORBIDDEN));
                    break;
                case 404:
                    requestExpectations.will(requestExpectations.returnValue(HTTP_NOTFOUND));
                    break;
             case 500 : requestExpectations.will(requestExpectations.returnValue(HTTP_INTERNALERROR));
                    break;
                case 501:
                    requestExpectations.will(requestExpectations.returnValue(HTTP_NOTIMPLEMENTED));
                    break;
             default : requestExpectations.will(requestExpectations.returnValue(HTTP_OK));
                 break;
            }

            return this;
        }

        public MockHTTPServer build(int port) throws IOException {
               return MockHTTPServer.startServer(port, this);
        }

        public Expectations getRequestExpectations() {
            return requestExpectations;
        }

        public void setChecking() {
            context.checking(requestExpectations);
        }

        public RequestHandler getRequestHandler() {
            return requestHandler;
        }

        public void assertAllExpectationsAreMet() {
            context.assertIsSatisfied();
        }


    }

  private static MockHTTPServer startServer(int port, RequestExpectationBuilder requestExpectationBuilder) throws IOException {

    if (mockServers.get(port)==null) {
        mockServers.put(port, new MockHTTPServer(requestExpectationBuilder, port));
    }
    else
    {
      mockServers.get(port).stop();
      mockServers.put(port, new MockHTTPServer(requestExpectationBuilder, port));
    }

    return mockServers.get(port);
  }

  MockHTTPServer(RequestExpectationBuilder requestExpectationBuilder, int port) throws IOException {
        super(port);
        this.requestExpectationBuilder= requestExpectationBuilder;
        requestExpectationBuilder.setChecking();
    }


    protected Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        try {
        RequestHandler requestHandler = requestExpectationBuilder.getRequestHandler();
        requestHandler.url(uri);
        for (Object param : parms.keySet()) {
            requestHandler.param(param.toString(),parms.getProperty(param.toString()));
        }
        return new NanoHTTPD.Response(requestHandler.getResponseStatus(), MIME_HTML, requestHandler.returnValue().toString());
        }
        catch (java.lang.Throwable e) {
            e.printStackTrace();
            stop();
        }

        return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "");

    }

   public void assertAllExpectationsAreMet() {
       stop();
     for (Integer integer : mockServers.keySet()) {
       if ((mockServers.get(integer)).equals(this)) {
         mockServers.remove(integer);
       }
     }
       requestExpectationBuilder.assertAllExpectationsAreMet();
   }


}
