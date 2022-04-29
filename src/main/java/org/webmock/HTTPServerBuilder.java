package org.webmock;

import org.jmock.Expectations;

import java.io.IOException;

public interface HTTPServerBuilder<S> {

    /**
     * This is used to Start the Http Server with the Configured Expectation on the port specified
     *
     * @param port eg 8080 for normal tomcat emulation
     * @return The Started MockHTTPServer instance;
     * @throws IOException if the Port is in use by other processes
     */
    S build(int port) throws IOException;

    void setUpExpectations();

    Expectations getExpectations();
}
