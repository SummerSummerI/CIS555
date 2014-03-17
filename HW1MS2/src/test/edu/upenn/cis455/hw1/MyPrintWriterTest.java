package test.edu.upenn.cis455.hw1;

import java.util.Hashtable;

import edu.upenn.cis455.webserver.MyHttpServletRequest;
import edu.upenn.cis455.webserver.MyResponseBuffer;
import junit.framework.TestCase;

public class MyPrintWriterTest extends TestCase {
    // need to change the HttpServletRequest here
    MyHttpServletRequest request;
    public void testA() {
        Hashtable<String, String> initialLineDict = new Hashtable<String, String>();
        Hashtable<String, String> headLinesDict = new Hashtable<String, String>();
        MyResponseBuffer buf = new MyResponseBuffer();
        request = new MyHttpServletRequest(initialLineDict, headLinesDict, buf);
        assertTrue(true);
    }
}
