package org.httpmock.server;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HTTPStatusCodeTest {
  @Test
  public void testThatInstanceOfReturnsSameInstanceForCode() {
    HTTPStatusCode httpStatusCode = HTTPStatusCode.instanceOf(200);
    assertThat(httpStatusCode,is(HTTPStatusCode.HTTP_OK));
  }

  @Test(expected = EnumConstantNotPresentException.class)
  public void testThatInstanceOfForCodeNotExistingThrowsException() {
    HTTPStatusCode httpStatusCode = HTTPStatusCode.instanceOf(9000);
  }
}
