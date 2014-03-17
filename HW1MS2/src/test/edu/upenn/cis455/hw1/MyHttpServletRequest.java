package test.edu.upenn.cis455.hw1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    public String getAuthType() {
        // ??
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
    public Enumeration getHeaders(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    public Enumeration getHeaderNames() {
        // TODO Auto-generated method stub

        return null;
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
        // TODO Auto-generated method stub
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser() {
        // ??
        // TODO Auto-generated method stub
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    public String getServletPath() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    public HttpSession getSession() {
        return getSession(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) {
        // TODO Auto-generated method stub
        return m_props.get(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        // TODO Auto-generated method stub
        return m_props.keys();
    }

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
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getContentType()
     */
    public String getContentType() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    public String getProtocol() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    public int getServerPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocale()
     */
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocales()
     */
    public Enumeration getLocales() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#isSecure()
     */
    public boolean isSecure() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemotePort()
     */
    public int getRemotePort() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocalName()
     */
    public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocalAddr()
     */
    public String getLocalAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocalPort()
     */
    public int getLocalPort() {
        // TODO Auto-generated method stub
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
