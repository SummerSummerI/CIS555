package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Http Servlet Response containing response buffer, initial line and head lines
 * 
 * @author tjgreen, Hangfei
 * 
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class MyHttpServletResponse implements HttpServletResponse {
    private MyResponseBuffer buf;
    // shared variables: MyHttpServletResonse, MyHttpBuffer
    protected HashMap<String, ArrayList<String>> initialLineDict;
    protected HashMap<String, ArrayList<String>> headLinesDict;
    protected ArrayList<Cookie> cookieList;

    /**
     * Set a default value for initial response lines
     */
    public MyHttpServletResponse(MyResponseBuffer buf) {
        this.buf = buf;
        initialLineDict = buf.initialLineDict;
        headLinesDict = buf.headLinesDict;
        cookieList = buf.cookieList;

        // Set default initial line
        ArrayList<String> httpVersion = new ArrayList<String>();
        httpVersion.add("HTTP/1.1");
        initialLineDict.put("httpVersion", httpVersion);
        ArrayList<String> responseStatusCode = new ArrayList<String>();
        responseStatusCode.add("200");
        initialLineDict.put("responseStatusCode", responseStatusCode);
        ArrayList<String> responseStatusDescription = new ArrayList<String>();
        responseStatusDescription.add("OK");
        initialLineDict.put("responseStatusDescription",
                responseStatusDescription);
    }

    /**
     * Add Cookie
     * @param arg0
     * @return void
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie
     * )
     */
    public void addCookie(Cookie arg0) {
        // Who should add cookie? Bufffer?
        for (Cookie c : cookieList) {
            if (arg0.getName().equals(c.getName())
                    && arg0.getValue().equals(c.getValue())) {
                System.err.println("Cookie arealy exist!");
                return;
            }
        }
        cookieList.add(arg0);
    }

    /**
     * Contains Header
     * @param arg0
     * @return boolean
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
     */
    public boolean containsHeader(String arg0) {
        if (headLinesDict.get(arg0) != null)
            return true;
        else
            return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
     */
    public String encodeURL(String arg0) {
        return arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String
     * )
     */
    public String encodeRedirectURL(String arg0) {
        return arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
     */
    public String encodeUrl(String arg0) {
        return arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String
     * )
     */
    public String encodeRedirectUrl(String arg0) {
        return arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#sendError(int,
     * java.lang.String)
     */
    public void sendError(int arg0, String arg1) throws IOException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#sendError(int)
     */
    public void sendError(int arg0) throws IOException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
     */
    public void sendRedirect(String arg0) throws IOException {
        System.out.println("[DEBUG] redirect to " + arg0 + " requested");
        System.out.println("[DEBUG] stack trace: ");
        Exception e = new Exception();
        StackTraceElement[] frames = e.getStackTrace();
        for (int i = 0; i < frames.length; i++) {
            System.out.print("[DEBUG]   ");
            System.out.println(frames[i].toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String,
     * long)
     */
    public void setDateHeader(String arg0, long arg1) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String,
     * long)
     */
    public void addDateHeader(String arg0, long arg1) {
    }

    /**
     * Sets a response header with the given name and value. If the header had
     * already been set, the new value overwrites the previous one. The
     * containsHeader method can be used to test for the presence of a header
     * before setting its value.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String,
     * java.lang.String)
     */
    public void setHeader(String arg0, String arg1) {
        // tested
        if (headLinesDict.get(arg0) == null) {
            ArrayList<String> headerArrayList = (new ArrayList<String>());
            headerArrayList.clear();
            headerArrayList.add(arg1);
            headLinesDict.put(arg0, headerArrayList);
        } else {
            ArrayList<String> headerArrayList = headLinesDict.get(arg0);
            headerArrayList.clear();
            headerArrayList.add(arg1);
        }

    }

    /**
     * Adds a response header with the given name and value. This method allows
     * response headers to have multiple values.
     * 
     * @param arg0
     * @param arg1
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String,
     * java.lang.String)
     */
    public void addHeader(String arg0, String arg1) {
        // tested
        if (headLinesDict.get(arg0) == null) {
            ArrayList<String> headerArrayList = (new ArrayList<String>());
            headerArrayList.add(arg1);
            headLinesDict.put(arg0, headerArrayList);
        } else {
            ArrayList<String> headerArrayList = headLinesDict.get(arg0);
            headerArrayList.add(arg1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String,
     * int)
     */
    public void setIntHeader(String arg0, int arg1) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String,
     * int)
     */
    public void addIntHeader(String arg0, int arg1) {

    }

    /**
     * Sets the status code for this response. This method is used to set the
     * return status code when there is no error (for example, for the status
     * codes SC_OK or SC_MOVED_TEMPORARILY). If there is an error, and the
     * caller wishes to invoke an error-page defined in the web application, the
     * sendError method should be used instead. Possible status code: 200 - OK
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#setStatus(int)
     */
    public void setStatus(int arg0) {
        if (arg0 == 200) {
            initialLineDict.clear();
            ArrayList<String> httpVersion = new ArrayList<String>();
            httpVersion.add("HTTP/1.1");
            initialLineDict.put("httpVersion", httpVersion);
            ArrayList<String> responseStatusCode = new ArrayList<String>();
            responseStatusCode.add("200");
            initialLineDict.put("responseStatusCode", responseStatusCode);
            ArrayList<String> responseStatusDescription = new ArrayList<String>();
            responseStatusDescription.add("OK");
            initialLineDict.put("responseStatusDescription",
                    responseStatusDescription);
        } else {

        }
    }

    /**
     * Deprecated
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#setStatus(int,
     * java.lang.String)
     */
    public void setStatus(int arg0, String arg1) {
        // Deprecated
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getContentType()
     */
    public String getContentType() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    /**
     * Retrun the printWriter
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getWriter()
     */
    public PrintWriter getWriter() throws IOException {
        return (PrintWriter) (new MyPrintWriter(buf, this));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String arg0) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#setContentLength(int)
     */
    public void setContentLength(int arg0) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    public void setContentType(String arg0) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#setBufferSize(int)
     */
    public void setBufferSize(int arg0) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getBufferSize()
     */
    public int getBufferSize() {
        return 0;
    }

    /**
     * Flush the buffer
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#flushBuffer()
     */
    public void flushBuffer() throws IOException {
        // Cookie Lines would be set in MyRespsneBuffer.flush
        buf.confirmInitialLine(initialLineDict);
        buf.confirmHeaderLines(headLinesDict);
        buf.flushBuffer();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#resetBuffer()
     */
    public void resetBuffer() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#isCommitted()
     */
    public boolean isCommitted() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#reset()
     */
    public void reset() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
     */
    public void setLocale(Locale arg0) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getLocale()
     */
    public Locale getLocale() {
        return null;
    }

    public MyResponseBuffer getMyResponseBuffer() {
        return buf;
    }

}
