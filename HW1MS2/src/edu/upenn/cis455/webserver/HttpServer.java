package edu.upenn.cis455.webserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**w
 * Multi-thread Http Server Support servlet Invoke from command line, taking
 * three arguments Port Number, Root Directory, Config File Path Testing: ab -n
 * 10000 -c 1000 -t 20 http://example.com/
 * 
 * @version 2.0 Servlet
 * @version 1.0 Multi-thread Http Server
 * @author cis455
 * 
 */
class HttpServer extends HttpServlet {
    public static String serverInfo = "Hangfei's Server";
    public static HashMap<String, HttpServlet> servlets = null;
    public static HashMap<String, String> servletsUrl = null;
    
    protected static int serverPort = 8080;
    protected static ServerSocket serverSocket = null;
    protected Thread runningThread = null;
    protected static SessionManager sessionManager;
    protected static ServerLog serverLog;

    private static final long serialVersionUID = 1L;
    private static boolean isStopped = false;
    private static String rootDirectory = "/home/cis455";
    private static ThreadPool threadPool1 = null;
    private static String WebdotxmlPath = null;

    public static void main(String args[]) {
        if (args.length < 3 || args.length % 2 == 0) {
            usage();
            System.exit(-1);
        }
        serverPort = Integer.valueOf(args[0]);
        rootDirectory = args[1];
        WebdotxmlPath = args[2];
        setUp(); // Set Up
        printInfo(); // Set up thread pool
        loadServlet(); // Load Servlet
        run(); // run the server : loop
    }

    /**
     * Print the welcome info.
     */
    private static void printInfo() {
        System.out.println("************************************************");
        System.out.println("    Hangfei Lin's Server. PennKey: hangfei");
        System.out.println("    Port: " + serverPort);
        System.out.println("    Root Directory: " + rootDirectory);
        System.out.println("************************************************");
    }

    /**
     * If wrong input, print the usage info.
     */
    private static void usage() {
        System.err.println("usage: java TestHarness <path to web.xml> "
                + "[<GET|POST> <servlet?params> ...]");
    }

    /**
     * Load Servlet
     */
    private static void loadServlet() {
        Handler h = null;
        Handler2 h2 = null;
        try {
            h = parseWebdotxml(WebdotxmlPath);
            h2 = parseWebdotxml2(WebdotxmlPath);
        } catch (Exception e) {
            serverLog.writeFile(e.toString());
        }
        MyApplicationContext context = createContext(h);
        try {

            servlets = createServlets(h, context);
            setServletUrl(h2);
        } catch (Exception e) {
            serverLog.writeFile(e.toString());
        }
    }

    /**
     * Set up the server: thread pool, server log, session manager, open server
     * socket
     */
    private static void setUp() {
        serverLog = ServerLog.getInstance(rootDirectory + "serverlog2.txt");
        threadPool1 = new ThreadPool(10, 100000); // 10 threads, 100000 blocking
                                                  // queue size
        sessionManager = SessionManager.getInstance();
        ThreadWorker.setRootDirectory(rootDirectory);
        openServerSocket();
    }

    /**
     * Open Server Socket
     */
    private static void openServerSocket() {
        try {
            // adding backlog -
            // requested maximum length of the queue of incoming connections.
            serverSocket = new ServerSocket(serverPort, 20000);
        } catch (IOException e) {
            serverLog.writeFile(e.toString());
            throw new RuntimeException("Cannot open port" + serverPort, e);
        }
    }

    /**
     * Run the server
     */
    private static void run() {
        while (!isStopped) {
            Socket x = null;
            try {
                x = serverSocket.accept();
            } catch (SocketException e) {
                if (isStopped()) {
                    serverLog.writeFile(e.toString());
                    System.out.println("Socket closed!");
                    return;
                }
                throw new RuntimeException("Error accepting client connection",
                        e);
            } catch (IOException e) {
                serverLog.writeFile(e.toString());
            }
            threadPool1.handleSocket(x);
        }
        System.out.println("Server Stopped.");
    }

    /**
     * Determines if the server is stopped.
     * 
     * @return
     */
    private synchronized static boolean isStopped() {
        return isStopped;
    }

    /**
     * Close the server socket. Stopped to receive incoming sockets.
     */
    public static void closeSocket() {
        isStopped = true;
        try {
            closeServlet();
            serverSocket.close();
            // why putting in the run(), doesn't stop?
            ThreadPool.closeThreads();
            System.out.println("Closing Server.");
            serverLog.closeLogFile();
        } catch (IOException e) {
            serverLog.writeFile(e.toString());
        }
    }

    /**
     * Get Server Information
     * 
     * @return
     */
    public static String getServerInfo() {
        return serverInfo;
    }

    /**
     * Get Session Manager
     * 
     * @return
     */
    public static SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Close the servlet
     */
    private static void closeServlet() {
        for (String servlet : HttpServer.getServlets().keySet()) {
            HttpServer.getServlets().get(servlet).destroy();
        }
    }

    // ***** Servlet Map *****
    private static Handler parseWebdotxml(String webdotxml) throws Exception {
        Handler h = new Handler();
        File file = new File(webdotxml);
        if (file.exists() == false) {
            System.err.println("error: cannot find " + file.getPath());
            System.exit(-1);
        }
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(file, h);
        
        return h;
    }
    
    private static Handler2 parseWebdotxml2(String webdotxml) throws Exception {
        Handler2 h = new Handler2();
        File file = new File(webdotxml);
        if (file.exists() == false) {
            System.err.println("error: cannot find " + file.getPath());
            System.exit(-1);
        }
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(file, h);

        return h;
    }

    private static MyApplicationContext createContext(Handler h) {
        MyApplicationContext fc = new MyApplicationContext();
        for (String param : h.m_contextParams.keySet()) {
            fc.setInitParam(param, h.m_contextParams.get(param));
        }
        return fc;
    }

    private static HashMap<String, HttpServlet> createServlets(Handler h,
            MyApplicationContext fc) throws Exception {
        HashMap<String, HttpServlet> servlets = new HashMap<String, HttpServlet>();
        for (String servletName : h.m_servlets.keySet()) {
            MyServletConfig config = new MyServletConfig(servletName, fc);
            String className = h.m_servlets.get(servletName);
            Class servletClass = Class.forName(className);
            HttpServlet servlet = (HttpServlet) servletClass.newInstance();
            HashMap<String, String> servletParams = h.m_servletParams
                    .get(servletName);
            if (servletParams != null) {
                for (String param : servletParams.keySet()) {
                    config.setInitParam(param, servletParams.get(param));
                }
            }
            servlet.init(config);
            servlets.put(servletName, servlet);
        }
        return servlets;
    }
    
    public static HashMap<String, String> getServletUrl() {
        return servletsUrl;
    }

    private static void setServletUrl(Handler2 h) {
        servletsUrl = h.m_urlPattern;
    }
    /**
     * Get servelts
     * 
     * @return
     */
    public static HashMap<String, HttpServlet> getServlets() {
        return servlets;
    }

    /**
     * Handler for XML Parse
     * 
     * @author cis455
     * 
     */
    static class Handler extends DefaultHandler {
        private int m_state = 0;
        public String m_serverName;
        public String m_servletName;
        private String m_paramName;
        public String m_servletName400;
        HashMap<String, String> m_servlets = new HashMap<String, String>();
        HashMap<String, String> m_urlPattern = new HashMap<String, String>();
        HashMap<String, String> m_contextParams = new HashMap<String, String>();
        HashMap<String, HashMap<String, String>> m_servletParams = new HashMap<String, HashMap<String, String>>();
        
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) {
            /**
             * if(qName.compareTo("dispaly-name") == 0) { // m_serverName =
             * m_state = -1; }
             **/
            if (qName.compareTo("servlet") == 0) {
                m_state = 1;
            } else if (qName.compareTo("servlet-mapping") == 0) {
                m_state = 2;
            } else if (qName.compareTo("context-param") == 0) {
                m_state = 3;
            } else if (qName.compareTo("init-param") == 0) {
                m_state = 4;
            } else if (qName.compareTo("servlet-name") == 0) {
                m_state = (m_state == 1) ? 300 : 400;
            } else if (qName.compareTo("servlet-class") == 0) {
                m_state = 301;
            } else if (qName.compareTo("url-pattern") == 0) {
                m_state = 401;
            } else if (qName.compareTo("param-name") == 0) {
                m_state = (m_state == 3) ? 10 : 20;
            } else if (qName.compareTo("param-value") == 0) {
                m_state = (m_state == 10) ? 11 : 21;
            }
        }

        public void characters(char[] ch, int start, int length) {
            String value = new String(ch, start, length);
            /**
             * if(m_state == 1) { m_serverName = value; m_state = 0; }
             **/
            if (m_state == 300) {
                m_servletName = value;
                m_state = 0;
            } else if (m_state == 301) {
                m_servlets.put(m_servletName, value);
                m_state = 0;
            } else if (m_state == 400) {
                m_servletName400 = value;
                m_state = 0;
            } else if(m_state == 401) {
                m_urlPattern.put(value, m_servletName400);
                m_state = 0;
            } else if (m_state == 1) {
                m_servletName = value;
                m_state = 0;
            } else if (m_state == 2) {
                m_servlets.put(m_servletName, value);
                m_state = 0;
            } else if (m_state == 10 || m_state == 20) {
                m_paramName = value;
            } else if (m_state == 11) {
                if (m_paramName == null) {
                    System.err.println("Context parameter value '" + value
                            + "' without name");
                    System.exit(-1);
                }
                m_contextParams.put(m_paramName, value);
                m_paramName = null;
                m_state = 0;
            } else if (m_state == 21) {
                if (m_paramName == null) {
                    System.err.println("Servlet parameter value '" + value
                            + "' without name");
                    System.exit(-1);
                }
                HashMap<String, String> p = m_servletParams.get(m_servletName);
                if (p == null) {
                    p = new HashMap<String, String>();
                    m_servletParams.put(m_servletName, p);
                }
                p.put(m_paramName, value);
                m_paramName = null;
                m_state = 0;
            }
        } 
    } // End of DefaultHandler Class
    
    /**
     * Handler for XML Parse
     * 
     * @author cis455
     * 
     */
    static class Handler2 extends DefaultHandler {
        private int m_state = 0;
        public String m_serverName;
        public String m_servletName;
        private String m_paramName;
        public String m_servletName400;
        HashMap<String, String> m_servlets = new HashMap<String, String>();
        HashMap<String, String> m_urlPattern = new HashMap<String, String>();
        HashMap<String, String> m_contextParams = new HashMap<String, String>();
        HashMap<String, HashMap<String, String>> m_servletParams = new HashMap<String, HashMap<String, String>>();
        
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) {
            /**
             * if(qName.compareTo("dispaly-name") == 0) { // m_serverName =
             * m_state = -1; }
             **/
            if (qName.compareTo("servlet") == 0) {
                m_state = 1;
            } else if (qName.compareTo("servlet-mapping") == 0) {
                m_state = 2;
            } else if (qName.compareTo("context-param") == 0) {
                m_state = 3;
            } else if (qName.compareTo("init-param") == 0) {
                m_state = 4;
            } else if (qName.compareTo("servlet-name") == 0) {
                m_state = (m_state == 1) ? 300 : 400;
            } else if (qName.compareTo("servlet-class") == 0) {
                m_state = 301;
            } else if (qName.compareTo("url-pattern") == 0) {
                m_state = 401;
            } else if (qName.compareTo("param-name") == 0) {
                m_state = (m_state == 3) ? 10 : 20;
            } else if (qName.compareTo("param-value") == 0) {
                m_state = (m_state == 10) ? 11 : 21;
            }
        }

        public void characters(char[] ch, int start, int length) {
            String value = new String(ch, start, length);
            /**
             * if(m_state == 1) { m_serverName = value; m_state = 0; }
             **/
            if (m_state == 300) {
                m_servletName = value;
                m_state = 0;
            } else if (m_state == 301) {
                m_servlets.put(m_servletName, value);
                m_state = 0;
            } else if (m_state == 400) {
                m_servletName400 = value;
                m_state = 0;
            } else if(m_state == 401) {
                m_urlPattern.put(value, m_servletName400);
                m_state = 0;
            } else if (m_state == 1) {
                m_servletName = value;
                m_state = 0;
            } else if (m_state == 2) {
                m_servlets.put(m_servletName, value);
                m_state = 0;
            } else if (m_state == 10 || m_state == 20) {
                m_paramName = value;
            } else if (m_state == 11) {
                if (m_paramName == null) {
                    System.err.println("Context parameter value '" + value
                            + "' without name");
                    System.exit(-1);
                }
                m_contextParams.put(m_paramName, value);
                m_paramName = null;
                m_state = 0;
            } else if (m_state == 21) {
                if (m_paramName == null) {
                    System.err.println("Servlet parameter value '" + value
                            + "' without name");
                    System.exit(-1);
                }
                HashMap<String, String> p = m_servletParams.get(m_servletName);
                if (p == null) {
                    p = new HashMap<String, String>();
                    m_servletParams.put(m_servletName, p);
                }
                p.put(m_paramName, value);
                m_paramName = null;
                m_state = 0;
            }
        } 
    } // End of DefaultHandler Class

} // End of Main Class
