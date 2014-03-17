package test.edu.upenn.cis455.hw1;

import javax.servlet.*;
import java.util.*;

/**
 * @author Nick Taylor
 * A servlet configuration object used by a servlet container used 
 * to pass information to a servlet during initialization.
 */
class MyServletConfig implements ServletConfig {
    private String name;
    private MyApplicationContext context;
    private HashMap<String,String> initParams;
    
    public MyServletConfig(String name, MyApplicationContext context) {
        this.name = name;
        this.context = context;
        initParams = new HashMap<String,String>();
    }

    public String getInitParameter(String name) {
        return initParams.get(name);
    }
    
    public Enumeration getInitParameterNames() {
        Set<String> keys = initParams.keySet();
        Vector<String> atts = new Vector<String>(keys);
        return atts.elements();
    }
    
    public ServletContext getServletContext() {
        return context;
    }
    
    public String getServletName() {
        return name;
    }

    void setInitParam(String name, String value) {
        initParams.put(name, value);
    }
}
