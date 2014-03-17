package test.edu.upenn.cis455.hw1;

import java.util.ArrayList;
import java.util.HashMap;
import edu.upenn.cis455.webserver.MyHttpServletRequest;
import edu.upenn.cis455.webserver.MyResponseBuffer;
import junit.framework.TestCase;

public class MyResponseBufferTest extends TestCase {
    // need to change the HttpServletRequest here
    MyHttpServletRequest request;
    public void testA() {
        HashMap<String, ArrayList<String>> initialLineDict = new HashMap<String, ArrayList<String>>();
        MyResponseBuffer buf = new MyResponseBuffer();
        assertTrue(true);
        assertEquals(buf.getFlushedHeaderLines(), "");
        assertEquals(buf.getFlushedResponse(), "");
        assertEquals(buf.getFlushedInitialLines(), "");
        assertEquals(buf.getFlushedMessageBody(), "");
        buf.flushBuffer();
        assertEquals(buf.getFlushedHeaderLines(), "");
        assertEquals(buf.getFlushedResponse(), "");
        assertEquals(buf.getFlushedInitialLines(), "");
        assertEquals(buf.getFlushedMessageBody(), "");
        ArrayList<String> t1 = new ArrayList<String>();
        t1.add("http/1.0");
        initialLineDict.put("httpVersion", t1);
        ArrayList<String> t2 = new ArrayList<String>();
        t2.add("200");
        initialLineDict.put("responseStatusCode", t2);
        ArrayList<String> t3 = new ArrayList<String>();
        t3.add("OK");
        initialLineDict.put("responseStatusDescription", t3);
        buf.flushBuffer();
        assertEquals(buf.getFlushedHeaderLines(), "");
        assertEquals(buf.getFlushedResponse(), "");
        assertEquals(buf.getFlushedInitialLines(), "");
        assertEquals(buf.getFlushedMessageBody(), "");
        buf.confirmInitialLine(initialLineDict);
        // before flush
        assertEquals(buf.getFlushedHeaderLines(), "");
        assertEquals(buf.getFlushedResponse(), "");
        assertEquals(buf.getFlushedInitialLines(), "");
        assertEquals(buf.getFlushedMessageBody(), "");
        // after flush
        buf.flushBuffer();
        assertEquals(buf.getFlushedHeaderLines(), "");
        assertEquals(buf.getFlushedResponse(), "http/1.0 200 OK\r\n");
        assertEquals(buf.getFlushedInitialLines(), "http/1.0 200 OK\r\n");
        assertEquals(buf.getFlushedMessageBody(), "");  
        // try flush again
        buf.flushBuffer();
        assertEquals(buf.getFlushedHeaderLines(), "");
        assertEquals(buf.getFlushedResponse(), "http/1.0 200 OK\r\n");
        assertEquals(buf.getFlushedInitialLines(), "http/1.0 200 OK\r\n");
        assertEquals(buf.getFlushedMessageBody(), "");  
    }
}
