import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import edu.upenn.cis455.xpathengine.XPathClient;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

@SuppressWarnings("serial")
/**
 * Parse Result Servlet
 * 
 * @author cis455
 *
 */
public class ParseResult extends HttpServlet {
    /**
     * the space would be replace with '+'
     * after decode, the '+' would be replaced with whitespace
     * Need to do input validation(not xpath validation)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String urlString = request.getParameter("url");
        String xpathString = request.getParameter("xpath");
        String urlDeocded = null;
        String xpathDecoded = null;
        try {
            urlDeocded = (new URI(urlString)).getPath();
            xpathDecoded = (new URI(xpathString)).getPath();
            xpathDecoded = xpathDecoded.replace('+', ' '); 
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println(xpathDecoded);
        
        String[] xpathStringArray = xpathDecoded.split(",");
        XPathEngineImpl xpathEngine = new XPathEngineImpl();
        xpathEngine.setXPaths(xpathStringArray);
        XPathClient xpathClient = new XPathClient();
        Document doc = xpathClient.xmlToDom(urlDeocded);
        xpathEngine.evaluate(doc);
        ArrayList<Entry<String, Boolean>> resultList = xpathEngine.getParseResultList();
        
        // HTML response
        System.out.println("PareResultServlet: " + xpathEngine.getParseResultList());
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<HTML><HEAD><TITLE>Simple Servlet</TITLE></HEAD><BODY>");
        out.println("<br>");
        out.println("<h1 ALIGN=center>The XPath result is:</h1>");
        out.println("<P ALIGN=center >" + resultList + "</P>");
        out.println("</BODY></HTML>");
    }
}
