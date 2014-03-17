package test.edu.upenn.cis455.hw1;

import java.util.Hashtable;

import edu.upenn.cis455.webserver.MyHttpServletRequest;
import edu.upenn.cis455.webserver.MyResponseBuffer;
import junit.framework.TestCase;

public class ServerLogTest extends TestCase {
    // need to change the HttpServletRequest here
    MyHttpServletRequest request;
    public void testA() {
        Hashtable<String, String> initialLineDict = new Hashtable<String, String>();
        Hashtable<String, String> headLinesDict = new Hashtable<String, String>();
        MyResponseBuffer buf = new MyResponseBuffer();
        request = new MyHttpServletRequest(initialLineDict, headLinesDict, buf);
        assertTrue(true);
        ServerLog serverLog1 = ServerLog.getInstance("testServerLog.txt");
        ServerLog serverLog2 = ServerLog.getInstance("testServerLog2.txt");
        assertEquals("testServerLog.txt", serverLog2.serverLogName);
        assertEquals(serverLog1, serverLog2);
    }
}
