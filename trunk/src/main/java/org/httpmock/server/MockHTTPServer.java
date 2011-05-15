package org.httpmock.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.jmock.Mockery;

public class MockHTTPServer extends NanoHTTPD {
  private static HashMap<Integer, MockHTTPServer> mockServers = new HashMap<Integer,MockHTTPServer>();
  private RequestHandler requestHandler;
  private Mockery context;
  private Throwable thrown;

  public static MockHTTPServer getServerOnPort(int port) {
    return mockServers.get(port);
  }


  public static MockHTTPServer startServer(int port, RequestHandler requestHandler, Mockery context) throws IOException {

    if (mockServers.get(port)==null) {
        mockServers.put(port, new MockHTTPServer(port, requestHandler,context));
    }
    else
    {
      mockServers.get(port).stop();
      mockServers.put(port, new MockHTTPServer(port,requestHandler,context));
    }

    return mockServers.get(port);
  }

  MockHTTPServer(int port, RequestHandler requestHandler, Mockery context) throws IOException {
      super(port);
      this.requestHandler = requestHandler;
      this.context=context;
  }


    protected Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        try {
        requestHandler.url(uri);
        for (Object param : parms.keySet()) {
            requestHandler.param(param.toString(),parms.getProperty(param.toString()));
        }
        return new NanoHTTPD.Response(requestHandler.getResponseStatus(), MIME_HTML, requestHandler.returnValue().toString());
        }
        catch (java.lang.Throwable e) {
            this.thrown = e;
            stop();
        }

        return new NanoHTTPD.Response(HTTPStatusCode.HTTP_OK.toString(), MIME_HTML, "");

    }

   public void assertThatAllExpectationsAreMet() {
     stop();
     for (Integer integer : mockServers.keySet()) {
       if ((mockServers.get(integer)).equals(this)) {
         mockServers.remove(integer);
       }
     }
     if (thrown!=null) {
       thrown.printStackTrace();
       throw new AssertionError(thrown);
     }
     context.assertIsSatisfied();
   }


}
