package test.edu.upenn.cis455.hw1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

/**
 * Response Buffer
 * Buffer the outputs of HTML contents
 * @author cis455
 * 
 */
public class MyResponseBuffer {

    public StringBuilder responseInitialLine = new StringBuilder();
    public StringBuilder responseHeaderLines = new StringBuilder();
    private StringBuilder cookieHeaderLines = new StringBuilder();
    public StringBuilder messageBody = new StringBuilder();
    private String flushedMessageBody = "";
    private String flushedInitialLines = "";
    private String flushedHeaderLines = "";
    
    // shared variables: MyHttpServletResonse, MyHttpBuffer
    protected HashMap<String,ArrayList<String>> initialLineDict = new HashMap<String,ArrayList<String>>();
    protected HashMap<String,ArrayList<String>> headLinesDict = new HashMap<String,ArrayList<String>>();
    protected ArrayList<Cookie> cookieList = new ArrayList<Cookie>();

    public void append(String str) {
        messageBody.append(str);
    }

    public void append(boolean b) {
        messageBody.append("" + b);
    }

    public void append(char c) {
        messageBody.append("" + c);
    }

    public void append(int i) {
        messageBody.append("" + i);
    }

    public void append(long l) {
        messageBody.append("" + l);
    }

    public void append(float f) {
        messageBody.append("" + f);
    }

    public void append(double d) {
        messageBody.append("" + d);
    }

    public void append(char[] s) {
        messageBody.append(new String(s));
    }

    public void flushBuffer() {
        flushedInitialLines = responseInitialLine.toString();
        flushedHeaderLines = responseHeaderLines.toString();
        flushedMessageBody  =  messageBody.toString();
    }
    
    public void flushBuffer(int bufferSize) {
        flushedInitialLines = responseInitialLine.toString();
        flushedHeaderLines = responseHeaderLines.toString();
        if (bufferSize > 0) {
            flushedMessageBody  =  messageBody.toString().substring(0, bufferSize);
        } else {
            flushedMessageBody  =  messageBody.toString();;
        }
    }
    
    /**
     * Confirm the initial line
     * after confirmation, could not modified
     * @param initialLineDict
     */
    public void confirmInitialLine(
            HashMap<String, ArrayList<String>> initialLineDict) {
        responseInitialLine.append(convertListToString(initialLineDict.get("httpVersion")));
        responseInitialLine.append(" ");
        responseInitialLine.append(convertListToString(initialLineDict.get("responseStatusCode")));
        responseInitialLine.append(" ");
        responseInitialLine.append(convertListToString(initialLineDict.get("responseStatusDescription")));
        responseInitialLine.append(" ");
        responseInitialLine.append("\r\n");
    }
    
    /**
     * Convert list to string, with one comma separated
     * If single element, just remove the bracket
     * @param list
     * @return
     */
    public String convertListToString(ArrayList<String> list) {
        // not fully tested yet
        ArrayList<String> newList = new ArrayList<String>(list);
        if(list.isEmpty()) {
            return "";
        }
        else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(newList.remove(0));
            for(String s: newList) {
                stringBuilder.append(",");
                stringBuilder.append(s);
            }
            return stringBuilder.toString();
        }        
    }

    /**
     * Confirm the header lines
     * after confirmation, could not modified
     * with one empty line at the end
     * @param initialLineDict
     */
    public void confirmHeaderLines(
            HashMap<String, ArrayList<String>> headLinesDict) {
        for(String key:headLinesDict.keySet()) {
            if(key.equals("Set-Cookie")) {
                for(String cookie: headLinesDict.get(key)) {
                    responseHeaderLines.append(key);
                    responseHeaderLines.append(":");
                    responseHeaderLines.append(cookie);
                    responseHeaderLines.append("\r\n");
                }
            }
            else {
                responseHeaderLines.append(key);
                responseHeaderLines.append(":");
                responseHeaderLines.append(convertListToString(headLinesDict.get(key)));
                responseHeaderLines.append("\r\n");
            }
        }
        responseHeaderLines.append("\n");
    }

    /**
     * Return flushed response
     * @return
     */
    public String getFlushedResponse() {
        // 
        return flushedInitialLines + setCookieHeaderLines() + flushedHeaderLines  + flushedMessageBody;
    }

    /**
     * Return flushed initial lines
     * @return
     */
    public String getFlushedInitialLines() {
        return flushedInitialLines;
    }

    /**
     * Return flushed header lines
     * @return
     */
    public String getFlushedHeaderLines() {
        return flushedHeaderLines;
    }
    
    /**
     * Return flushed message body
     * @return
     */
    public String getFlushedMessageBody() {
        return flushedMessageBody;
    }
    
    /**
     * @param arg0
     * @return void
     */   
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
     */
    public void addCookie(Cookie arg0) {
        cookieList.add(arg0);
    }
    
    /**
     * Adds a response header with the given name and value. 
     * This method allows response headers to have multiple values.
     * @param arg0
     * @param arg1
     */
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
     */
    public void addHeader(String arg0, String arg1) {
        // tested
        if(headLinesDict.get(arg0) == null) {
            ArrayList<String> headerArrayList = (new ArrayList<String>());
            headerArrayList.add(arg1);
            headLinesDict.put(arg0, headerArrayList);
        }
        else {
            ArrayList<String> headerArrayList = headLinesDict.get(arg0);
            headerArrayList.add(arg1);
        }
    }
    
    /**
     * When confirm, set the cookie header lines.
     * Set-Cookie: name=value; expiratoin-date
     */
    private String setCookieHeaderLines() {
        for (Cookie c : cookieList) {
            int timeDuration = c.getMaxAge();
            if (timeDuration == -1) {
                cookieHeaderLines.append("Set-Cookie" + ":" + c.getName() + "=" + c.getValue());
                cookieHeaderLines.append("\r\n");
            } else {
                Date currentDate = new Date();
                long millisecond = currentDate.getTime();
                long expireMilliSecond = millisecond + ((long) timeDuration)
                        * 1000; // since Unix EPOCH
                Date expireDate = new Date(expireMilliSecond);
                SimpleDateFormat df1 = new SimpleDateFormat(
                        "EEE, dd MMM yyyy HH:mm:ss z");
                df1.setTimeZone(TimeZone.getTimeZone("GMT"));
                String expireDateString = df1.format(expireDate);
                System.out.println("Current Time: " + df1.format(currentDate));
                System.out.println("Set-Cookie" + ":" + c.getName() + "="
                        + c.getValue() + ";" + "expires=" + expireDateString);
                cookieHeaderLines.append("Set-Cookie" + ":" + c.getName() + "=" + c.getValue() + ";"
                        + "expires=" + expireDateString);
                cookieHeaderLines.append("\r\n");
            }
        }
        return cookieHeaderLines.toString();
    }

}
