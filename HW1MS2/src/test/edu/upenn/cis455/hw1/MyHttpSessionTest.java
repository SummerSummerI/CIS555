package test.edu.upenn.cis455.hw1;

import java.util.Hashtable;

import edu.upenn.cis455.webserver.MyHttpServletRequest;
import edu.upenn.cis455.webserver.MyResponseBuffer;
import junit.framework.TestCase;

public class MyHttpSessionTest extends TestCase {
    // need to change the HttpServletRequest here
    MyHttpServletRequest request;
    MyHttpSession testSession1 = new MyHttpSession();
    MyHttpSession testSession2 = new MyHttpSession();
    MyHttpSession testSession3 = new MyHttpSession();
    MyHttpSession testSession4 = new MyHttpSession();
    MyHttpSession testSession5 = new MyHttpSession();
    
    public void testA() {
        Hashtable<String, String> initialLineDict = new Hashtable<String, String>();
        Hashtable<String, String> headLinesDict = new Hashtable<String, String>();
        MyResponseBuffer buf = new MyResponseBuffer();

        request = new MyHttpServletRequest(initialLineDict, headLinesDict, buf);
        assertTrue(true);
        
        assertEquals(testSession1.getSessoinId(), 1);
        assertEquals(testSession2.getSessoinId(), 2);
        assertEquals(testSession3.getSessoinId(), 3);
        assertEquals(testSession4.getSessoinId(), 4);
        assertEquals(testSession5.getSessoinId(), 5);

        assertEquals(testSession1.getCreationTime(), 0);
        assertEquals(testSession2.getCreationTime(), 0);
        assertEquals(testSession3.getCreationTime(), 0);
        assertEquals(testSession4.getCreationTime(), 0);
        assertEquals(testSession5.getCreationTime(), 0);
        
        
    }
    
    public void testB() {
        Hashtable<String, String> initialLineDict = new Hashtable<String, String>();
        Hashtable<String, String> headLinesDict = new Hashtable<String, String>();
        MyResponseBuffer buf = new MyResponseBuffer();

        request = new MyHttpServletRequest(initialLineDict, headLinesDict, buf);
        assertTrue(true);

        
        assertEquals(testSession1.getCreationTime(), 0);
        assertEquals(testSession2.getCreationTime(), 0);
        assertEquals(testSession3.getCreationTime(), 0);
        assertEquals(testSession4.getCreationTime(), 0);
        assertEquals(testSession5.getCreationTime(), 0);
        
        assertEquals(testSession1.isValid(), true);
        assertEquals(testSession2.isValid(), true);
        assertEquals(testSession3.isValid(), true);
        assertEquals(testSession4.isValid(), true);
        assertEquals(testSession5.isValid(), true);
        
        testSession1.invalidate();
        testSession2.invalidate();
        testSession3.invalidate();
        testSession4.invalidate();
        testSession5.invalidate();
        
        assertEquals(testSession1.isValid(), false);
        assertEquals(testSession2.isValid(), false);
        assertEquals(testSession3.isValid(), false);
        assertEquals(testSession4.isValid(), false);
        assertEquals(testSession5.isValid(), false);
    }
}
