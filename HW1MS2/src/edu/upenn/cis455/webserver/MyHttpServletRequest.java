package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Todd J. Green
 */
public class MyHttpServletRequest implements HttpServletRequest {
    private MyResponseBuffer buf;
    private Properties m_params = new Properties();
    private Properties m_props = new Properties();
    private MyHttpSession m_session = null;
    private String m_method;
    private String characterEncoding = "ISO-8859-1";

    // shared variables: MyHttpServletResonse, MyHttpBuffer
    private Hashtable<String, String> initialLineDict = null;
    private Hashtable<String, String> headLinesDict = null;
    protected ArrayList<Cookie> cookieList;

    public MyHttpServletRequest(Hashtable<String, String> initialLineDict,
            Hashtable<String, String> headLinesDict, MyResponseBuffer buf) {
        this.buf = buf;
        this.initialLineDict = initialLineDict;
        this.headLinesDict = headLinesDict;
        this.cookieList = buf.cookieList;
        initCookies();
    }

    MyHttpServletRequest(MyHttpSession session) {
        m_session = session;
    }

    /**
     * Returns the name of the authentication scheme used to protect the
     * servlet. All servlet containers support basic, form and client
     * certificate authentication, and may additionally support digest
     * authentication. If the servlet is not authenticated null is returned.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    public String getAuthType() {
        return BASIC_AUTH;
    }

    /**
     * Initialization the cookies according to the headers
     * 
     */
    private void initCookies() {
        String cookiesString = headLinesDict.get("Cookie");
        if (cookiesString != null) {
            ArrayList<String> cookiesStringList = new ArrayList<String>(
                    Arrays.asList(cookiesString.split(";")));

            for (String cookie : cookiesStringList) {
                String trimmedString = cookie.trim(); // trim the string
                ArrayList<String> cookieString = new ArrayList<String>(
                        Arrays.asList(trimmedString.split("=")));
                cookieList.add(new Cookie(cookieString.get(0), cookieString
                        .get(1)));
            }
        }
    }

    /**
     * Returns an array containing all of the Cookie objects the client sent
     * with this request. This method returns null if no cookies were sent.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    public Cookie[] getCookies() {
        return cookieList.toArray(new Cookie[cookieList.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
     */
    public long getDateHeader(String arg0) {
        // returned as the number of milliseconds since January 1, 1970 GMT
        // tested
        // Case sensitive
        // does not support Unmodified yet
        if (headLinesDict.get(arg0) == null) {
            return -1;
        } else {
            Date date = null;
            String dateString = headLinesDict.get(arg0);
            SimpleDateFormat df1 = new SimpleDateFormat(
                    "EEE, dd MMM yyyy HH:mm:ss z");
            df1.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                date = df1.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long millisecond = date.getTime();
            System.out.println(millisecond);
            return millisecond;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    public String getHeader(String arg0) {
        // case sensitive
        return headLinesDict.get(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    public Enumeration<String> getHeaders(String arg0) {
        LinkedList<String> arr = new LinkedList<String>();
        arr.add(headLinesDict.get(arg0));
        Enumeration<String> e = Collections.enumeration(arr);
        return e;
    }

    /**
     * Returns an enumeration of all the header names this request contains. If
     * the request has no headers, this method returns an empty enumeration.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    public Enumeration<String> getHeaderNames() {
        Enumeration<String> e = Collections.enumeration(headLinesDict.keySet());
        return e;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    public int getIntHeader(String arg0) {
        if (headLinesDict.get(arg0) == null) {
            return -1;
        } else {
            try {
                return Integer.valueOf(headLinesDict.get(arg0));
            } catch (NumberFormatException e) {
                // ?
                return -2;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getMethod()
     */
    /**
     * Same as the value of the CGI variable REQUEST_METHOD.
     */
    public String getMethod() {
        return m_method;
    }

    /**
     * Returns any extra path information associated with the URL the client
     * sent when it made this request. The extra path information follows the
     * servlet path but precedes the query string and will start with a "/"
     * character.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */

    public String getPathInfo() {
        String path = initialLineDict.get("path");
        System.out.println("getPathInfo: " + path);
        if (path.contains("?")) {
            String pathWithoutQuery = path.substring(0, path.lastIndexOf("?"));
            ArrayList<String> pathSplit = new ArrayList<String>(
                    Arrays.asList(pathWithoutQuery.split("/")));
            // assume the servlet name is the between first two slashes
            pathSplit.remove(0);
            pathSplit.remove(1);
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : pathSplit) {
                stringBuilder.append("/");
                stringBuilder.append(s);
            }
            return stringBuilder.toString();
        } else {
            return "";
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    public String getPathTranslated() {
        // not required
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        // just return empty string if in root
        // extra credit here
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        // robutness?
        String path = initialLineDict.get("path");
        String queryString = path.substring(path.lastIndexOf("?") + 1,
                path.length());
        return queryString;
    }

    /**
     * Returns the login of the user making this request, if the user has been
     * authenticated, or null if the user has not been authenticated. Whether
     * the user name is sent with each subsequent request depends on the browser
     * and type of authentication. Same as the value of the CGI variable
     * REMOTE_USER.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String arg0) {
        // not required
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        // not required
        return null;
    }

    /**
     * Returns the session ID specified by the client. This may not be the same
     * as the ID of the current valid session for this request. If the client
     * did not specify a session ID, this method returns null.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        if (m_session != null) {
            return m_session.getId();
        } else
            return null;
    }

    /**
     * Returns the part of this request's URL from the protocol name up to the
     * query string in the first line of the HTTP request. The web container
     * does not decode this String. For example:
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        String requestedURL = initialLineDict.get("path");
        if (requestedURL != null) {
            return requestedURL.split("?")[1];
        } else
            return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return new StringBuffer(initialLineDict.get("path"));
    }

    /**
     * Reconstructs the URL the client used to make the request. The returned
     * URL contains a protocol, server name, port number, and server path, but
     * it does not include query string parameters.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    public String getServletPath() {
        return null;
    }

    /**
     * Returns the current HttpSession associated with this request or, if there
     * is no current session and create is true, returns a new session.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     */
    public HttpSession getSession(boolean arg0) {
        boolean sessionInStock = false;
        if (arg0) {
            if (!hasSession()) {
                for (Cookie c : cookieList) {
                    // if found session cookie, get the session from the
                    // SessionManager
                    if (c.getName().equalsIgnoreCase("JSESSIONID")) {
                        // If has session cookie, but session not in pool,
                        // create a new session.
                        // Don't add session cookie to response. User already
                        // hold one.
                        if (SessionManager.getSession(c.getValue()) != null) {
                            sessionInStock = true;
                            m_session = SessionManager.getSession(c.getValue());
                            return m_session;
                        }
                    }
                }
                if (!sessionInStock) {
                    // create new session and new session cookie if no session
                    // cookie found
                    // and create a session cookie and add to response
                    m_session = new MyHttpSession();
                    Cookie sessionCookie = new Cookie("JSESSIONID",
                            Long.toString(m_session.getSessoinId()));
                    buf.addCookie(sessionCookie);
                    SessionManager.addSession(m_session);
                }
            }
        } else {
            if (!hasSession()) {
                m_session = null;
            }
        }
        return m_session;
    }

    /**
     * Returns the current session associated with this request, or if the
     * request does not have a session, creates one.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    public HttpSession getSession() {
        return getSession(true);
    }

    /**
     * Checks whether the requested session ID is still valid.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    /**
     * Checks whether the requested session ID came in as a cookie.
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /**
     * Checks whether the requested session ID came in as part of the request
     * URL.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl() {
        // Deprecated. As of Version 2.1 of the Java Servlet API, use
        // isRequestedSessionIdFromURL() instead.
        return false;
    }

    /**
     * Returns the value of the named attribute as an Object, or null if no
     * attribute of the given name exists.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) {
        return m_props.get(arg0);
    }

    /**
     * Returns an Enumeration containing the names of the attributes available
     * to this request. This method returns an empty Enumeration if the request
     * has no attributes available to it.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return m_props.keys();
    }

    /**
     * Returns the name of the character encoding used in the body of this
     * request. This method returns null if the request does not specify a
     * character encoding
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        //
        return characterEncoding;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String arg0)
            throws UnsupportedEncodingException {
        //
        characterEncoding = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    public int getContentLength() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getContentType()
     */
    public String getContentType() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getInputStream()
     */
    public ServletInputStream getInputStream() throws IOException {
        // not required
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(String arg0) {
        return m_params.getProperty(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return m_params.keys();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String arg0) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    public String getProtocol() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getScheme()
     */
    public String getScheme() {
        return "http";
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getServerName()
     */
    public String getServerName() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    public int getServerPort() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String,
     * java.lang.Object)
     */
    public void setAttribute(String arg0, Object arg1) {
        m_props.put(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String arg0) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocale()
     */
    public Locale getLocale() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocales()
     */
    public Enumeration getLocales() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#isSecure()
     */
    public boolean isSecure() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String arg0) {
        // not required
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    public String getRealPath(String arg0) {
        return null;
    }

    /**
     * Returns the Internet Protocol (IP) source port of the client or last
     * proxy that sent the request.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemotePort()
     */
    public int getRemotePort() {
        return 0;
    }

    /**
     * Returns the host name of the Internet Protocol (IP) interface on which
     * the request was received.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocalName()
     */
    public String getLocalName() {
        return null;
    }

    /**
     * Returns the Internet Protocol (IP) address of the interface on which the
     * request was received.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocalAddr()
     */
    public String getLocalAddr() {
        return null;
    }

    /**
     * Returns the Internet Protocol (IP) port number of the interface on which
     * the request was received.
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocalPort()
     */
    public int getLocalPort() {
        return 0;
    }

    void setMethod(String method) {
        m_method = method;
    }

    void setParameter(String key, String value) {
        m_params.setProperty(key, value);
    }

    void clearParameters() {
        m_params.clear();
    }

    boolean hasSession() {
        return ((m_session != null) && m_session.isValid());
    }

}
