package test.edu.upenn.cis455.hw1;

import java.util.Hashtable;

import junit.framework.TestCase;

public class MyHttpServletRequestTest extends TestCase {
    // need to change the HttpServletRequest here

    public void testA() {
        MyHttpServletRequest request1;
        MyHttpServletRequest request2;
        MyHttpServletRequest request3;
        MyHttpServletRequest request4;
        Hashtable<String, String> initialLineDict = new Hashtable<String, String>();
        Hashtable<String, String> headLinesDict = new Hashtable<String, String>();
        headLinesDict.put("GET", "GET");
        MyResponseBuffer buf = new MyResponseBuffer();
        request1 = new MyHttpServletRequest(initialLineDict, headLinesDict, buf);
        request2 = new MyHttpServletRequest(initialLineDict, headLinesDict, buf);
        request3 = new MyHttpServletRequest(initialLineDict, headLinesDict, buf);
        request4 = new MyHttpServletRequest(initialLineDict, headLinesDict, buf);
        request1.setMethod("GET");
        request2.setMethod("POST");
        request3.setMethod("HEAD");
        request4.setMethod("SOMETHINGNOTSUPPORT");
        assertEquals(request1.getScheme(), "http");
        assertEquals(request1.getCharacterEncoding(), "ISO-8859-1");
        assertNull(request1.getServerName());
        assertEquals(request1.getMethod(), "GET");
        assertEquals(request2.getMethod(), "POST");
        assertEquals(request3.getMethod(), "HEAD");
        assertEquals(request4.getMethod(), "SOMETHINGNOTSUPPORT");
        assertNull(request1.getServerName());
        assertNull(request1.getServerName());
        assertNull(request1.getPathTranslated());
        assertEquals(request1.getContextPath(), "");
        assertEquals(request1.isUserInRole(""), false);
        assertNull(request1.getUserPrincipal());
        assertNull(request1.getPathTranslated());
    }
}
