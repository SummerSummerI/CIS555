

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 * 
 * @author cis455
 *
 */
@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
    /**
     * doGet
     * tested: [/gesmes:Envelope/gesmes:subject=true]
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<HTML><HEAD><TITLE>Simple Servlet</TITLE></HEAD><BODY>");
        
        out.println("<br>");
        
        out.println("<h1 ALIGN=center>XPath Evaluation Engine</h1>");
        out.println("<h3 ALIGN=center>Hangfei Lin (Pennkey: hangfei)</h3>");
        out.println("<P ALIGN=center>Hello. Please input your url and xpath.</P>");
        out.println("<form name=\"input\" action=\"parseresult\" method=\"post\">");
        out.println("URL:   <input type=\"text\" name=\"url\">");
        out.println("<br>");
        out.println("XPath: <input type=\"text\" name=\"xpath\">");
        out.println("<input type=\"submit\" value=\"Submit\">");
        out.println("</form>");
        out.println("</BODY></HTML>");      
    }
}









