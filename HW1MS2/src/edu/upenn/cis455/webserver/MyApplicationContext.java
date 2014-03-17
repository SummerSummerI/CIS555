package edu.upenn.cis455.webserver;

import javax.servlet.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * a single Context per "application" Defines a set of methods that a servlet
 * uses to communicate with its servlet container, for example, to get the MIME
 * type of a file, dispatch requests, or write to a log file.
 * 
 * @author Nick Taylor
 */
class MyApplicationContext implements ServletContext {
    private HashMap<String, Object> attributes;
    private HashMap<String, String> initParams;

    public MyApplicationContext() {
        attributes = new HashMap<String, Object>();
        initParams = new HashMap<String, String>();
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    // added generic type
    public Enumeration<String> getAttributeNames() {
        Set<String> keys = attributes.keySet();
        Vector<String> atts = new Vector<String>(keys);
        return atts.elements();
    }

    // ??
    // Returns a ServletContext object that corresponds to a specified URL on
    // the server.
    public ServletContext getContext(String name) {
        return null;
    }

    public String getInitParameter(String name) {
        return initParams.get(name);
    }

    public Enumeration<String> getInitParameterNames() {
        Set<String> keys = initParams.keySet();
        Vector<String> atts = new Vector<String>(keys);
        return atts.elements();
    }

    public int getMajorVersion() {
        return 2;
    }

    // use the FIles.probeContentType
    // ref:
    // http://stackoverflow.com/questions/51438/getting-a-files-mime-type-in-java
    // not tested yet
    public String getMimeType(String file) {
        // not required
        /**
         * Path path = Paths.get(file);; try { return
         * Files.probeContentType(path); } catch (IOException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); return null; }
         **/
        return null;

    }

    public int getMinorVersion() {
        return 4;
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        // not required
        return null;
    }

    public String getRealPath(String path) {
        // ?
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String name) {
        // not required
        return null;
    }

    public java.net.URL getResource(String path) {
        // not required
        return null;
    }

    public java.io.InputStream getResourceAsStream(String path) {
        // not required
        return null;
    }

    public java.util.Set getResourcePaths(String path) {
        // not required
        return null;
    }

    // Returns the name and version of the servlet container
    // on which the servlet is running.
    public String getServerInfo() {
        // ?
        return HttpServer.getServerInfo();
    }

    // Deprecated. As of Java Servlet API 2.1, with no direct replacement.
    public Servlet getServlet(String name) {
        return null;
    }

    //Returns the name of this web application corresponding to 
    // this ServletContext as specified in the deployment descriptor 
    // for this web application by the display-name element.
    public String getServletContextName() {
        // not working out
        return "Test Harness";
    }

    // Deprecated. As of Java Servlet API 2.1, with no replacement.
    public Enumeration<String> getServletNames() {
        return null;
    }

    // Deprecated. As of Java Servlet API 2.0, with no replacement.
    public Enumeration<String> getServlets() {
        return null;
    }

    // not required
    public void log(Exception exception, String msg) {
        log(msg, (Throwable) exception);
    }

    // not required
    public void log(String msg) {
        System.err.println(msg);
    }

    // not required
    public void log(String message, Throwable throwable) {
        System.err.println(message);
        throwable.printStackTrace(System.err);
    }

    // done
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    // done
    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    // done
    void setInitParam(String name, String value) {
        initParams.put(name, value);
    }
}
