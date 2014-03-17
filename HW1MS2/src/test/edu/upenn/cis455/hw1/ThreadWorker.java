package test.edu.upenn.cis455.hw1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;

/**
 * Worker Thread. Dequeue the socket, read in the request, and responses Could
 * handle servlet
 * 
 * @author cis455
 * 
 */
public class ThreadWorker extends Thread {
    private static BlockingQueue taskQueue = null;
    public static String htmlFolderDirecotry = "/";
    public ServerLog serverLog = ServerLog.getInstance("serverlog");
    private String currentUrl = null;
    private boolean runFlag = false;

    // put the raw request in
    private String initialLine = "";
    private ArrayList<String> rawRequest = new ArrayList<String>();
    private ArrayList<String> rawHeadLines = new ArrayList<String>();
    private ArrayList<String> rawContent = new ArrayList<String>();

    private Hashtable<String, String> rawInitLineDict = new Hashtable<String, String>();
    private Hashtable<String, String> rawRequestDict = new Hashtable<String, String>();

    private Hashtable<String, String> parsedInitialLineDict = new Hashtable<String, String>();
    private Hashtable<String, String> parsedHeaderLinesDict = new Hashtable<String, String>();
    private Hashtable<String, String> parsedRequestDict = new Hashtable<String, String>();

    // ?
    private Hashtable<String, String> responseDict = new Hashtable<String, String>();

    public ThreadWorker(BlockingQueue taskQueue) {
        ThreadWorker.setTaskQueue(taskQueue);
    }

    public void run() {
        runFlag = true;
        while (runFlag) {
            Socket clientSocket = null;
            try {
                init();
                clientSocket = (Socket) getTaskQueue().dequeue();
                clientSocket.setSoTimeout(10000); // set timeout
                InputStreamReader reader = new InputStreamReader(
                        clientSocket.getInputStream()); // input source
                BufferedReader bufferReader = new BufferedReader(reader);
                PrintStream outputHtml = new PrintStream(
                        clientSocket.getOutputStream(), true); // output
                // source

                // process the requests
                processRequest(bufferReader);
                execute(outputHtml);
            } catch (InterruptedException e) {
                serverLog.writeFile(e.toString());
                System.out.println("Thread Interrupted!");
            } catch (SocketException e) {
                serverLog.writeFile(e.toString());
            } catch (IOException e) {
                serverLog.writeFile(e.toString());
            } catch (NullPointerException e) {
                serverLog.writeFile(e.toString());
            } finally {
                // Close again?
            }
        }
    }

    private void init() {
        parsedRequestDict.clear();
        rawRequest.clear();
        rawHeadLines.clear();
        parsedHeaderLinesDict.clear();
        parsedInitialLineDict.clear();
        rawInitLineDict.clear();
        rawRequestDict.clear();
    }

    private void execute(PrintStream outputHtml) {
        String fileName = (String) parsedInitialLineDict.get("path");
        setCurrentUrl(fileName);
        directoryFileDisplayList(fileName);

        // respond to the request
        // Security Check
        if (!pathSecurityCheck(fileName)) {
            System.out.println("Insecure path: 400 Bad Request\n");
            generateErrorResponse(outputHtml, "400");
        } else {
            // Validation Check
            // HTTP/1.1
            int respondeCode = validationCheck(outputHtml);
            if (respondeCode != 200) {
                // send error response
                sendErrorMessage(outputHtml, respondeCode, "");
            } else {
                // if valid, send response, html page or servlet
                requestDispatcher(outputHtml);
            }
        }
    }

    private void requestDispatcher(PrintStream outputHtml) {
        String servletPattern = servletPattern();
        if (servletPattern == null) {
            doHtmlResponse(outputHtml);
        } else {
            MyResponseBuffer buf = new MyResponseBuffer();
            // need to change the HttpServletRequest here
            MyHttpServletRequest request = new MyHttpServletRequest(
                    parsedInitialLineDict, parsedHeaderLinesDict, buf);
            MyHttpServletResponse response = new MyHttpServletResponse(buf);
            String requestMethod = parsedInitialLineDict.get("GET");
            request.setMethod(requestMethod);

            if (requestMethod.equalsIgnoreCase("POST")) {
                String queryString = null;
                if (parsedHeaderLinesDict.get("Content-length") != null) {
                    System.out.println("Content-length!!!!: " + rawContent);
                    queryString = rawContent.get(0);
                } else {
                    String pathString = parsedInitialLineDict.get("path");
                    String[] pathStringSplit = pathString.split("\\?");
                    if (pathStringSplit.length == 2) {
                        queryString = pathStringSplit[1];
                    }

                }
                System.out.println("POST2324se:" + queryString);
                if (queryString != null) {
                    String[] queryArray = queryString.split("&");
                    for (int index = 0; index < queryArray.length; index++) {
                        String[] keyValue = queryArray[index].split("=");
                        if (keyValue.length == 2) {
                            request.setParameter(keyValue[0], keyValue[1]);
                        }
                    }
                }

            } // End of POST

            System.out.println("POST:fafade");
            String requestedServlet = servletPattern;
            System.out.println(requestedServlet);
            HttpServlet servlet = HttpServer.getServlets()
                    .get(requestedServlet);
            System.out.println("servlet" + servlet);
            try {
                System.out.println("fafvz");
                MyHttpSession mySession = (MyHttpSession) request.getSession();
                System.out.println("vzfpi32rf");
                Cookie sessionCookie = mySession.getCookie();
                servlet.service(request, response);
                System.out.println("--dva");
                System.out.println(requestedServlet);

                response.flushBuffer();
                System.out
                        .println("-------------------------------------------------------------");
                System.out.println(buf.getFlushedResponse());
                outputHtml.print(buf.getFlushedResponse());
                outputHtml.flush();
                outputHtml.close();
            } catch (ServletException e) {
                serverLog.writeFile(e.toString());
            } catch (IOException e) {
                serverLog.writeFile(e.toString());
            }

            // fRequest.getQueryString();
        }
        // FakeRequest request = new FakeRequest(fs);
        // FakeResponse response = new FakeResponse();
        // String[] strings = args[i+1].split("\\?|&|=");
        // HttpServlet servlet = HttpServer.getServlets().get("demo");
        // session
        // FakeSession fs = null;
        // FakeRequest request = new FakeRequest(fs);
        // FakeResponse response = new FakeResponse();

        // request.setParameter(strings[j], strings[j+1]);
        // request.setMethod(args[i]);

        // request.setMethod(args[i]);
        // servlet.service(request, response);

        // fs = (FakeSession) request.getSession(false);

    }

    /**
     * find servlets pattern match Return null if not matched
     * 
     * @return
     */
    private String servletPattern() {
        // need fix: /demo is ok. but /demo/ would result in errors
        // tested
        String pathString = parsedInitialLineDict.get("path");
        String[] urlSplit = pathString.split("/");

        if (urlSplit.length == 1 || urlSplit.length == 0) {
            return null;
        } else {
            String[] urlSplitByQuery = urlSplit[1].split("\\?");
            String urlPattern = "/" + urlSplitByQuery[0];
            HashMap<String, String> servletsUrl = HttpServer.getServletUrl();
            if (servletsUrl.get(urlPattern) == null) {
                System.out.println("Not servlet pattern");
                return null;
            } else {
                System.out.println("Servlet Pattern:"
                        + servletsUrl.get(urlPattern));
                return servletsUrl.get(urlPattern);
            }
        }
    }

    private int validationCheck(PrintStream outputHtml) {
        // different between get and head?
        if (rawInitLineDict.get("HTTP").equalsIgnoreCase("HTTP/1.0")) {
            if (rawRequestDict.get("path").contains("http://")) {
                System.out
                        .println("HTTP/1.0 Not Support Absolute Path: 404 Not Found\n");
                return 404;
            }
        } else if (rawInitLineDict.get("HTTP").equalsIgnoreCase("HTTP/1.1")) {
            if (rawInitLineDict.get("Host") == null) {
                System.out
                        .println("No Host!!!: header received: 400 Not Found\n");
                return 400;
            } else if (!rawInitLineDict.get("Host").equals(
                    ("localhost:" + Integer.toString(HttpServer.serverPort)))
                    && !rawInitLineDict.get("Host").equals(("localhost"))) {
                System.out
                        .println("Host incorrect: header received: 400 Not Found\n");
                // ?
                return 400;
            }
            if (rawRequestDict.get("path").contains("http://")) {
                System.out
                        .println("HTTP/1.1 ??? Not Support Absolute Path: 404 Not Found\n");
                // return 200;
            }
        } else {

        }
        return 200;
    }

    private void sendErrorMessage(PrintStream outputHtml, int errorCode,
            String errorMessage) {
        if (errorCode == 400) {
            System.out.println("No Host: header received: 400 Not Found\n");
            generateErrorResponse(outputHtml, "400");
        }
        if (errorCode == 404) {
            System.out
                    .println("HTTP/1.1 Not Support Absolute Path: 404 Not Found\n");
            generateErrorResponse(outputHtml, "404");
        }
        if (errorCode == 404) {
            System.out
                    .println("HTTP/1.0 Not Support Absolute Path: 404 Not Found\n");
            generateErrorResponse(outputHtml, "404");
        }
    }

    /**
     * Responde according to the requests Separate headlines from contents
     * 
     * @param outputHtml
     */
    private void doHtmlResponse(PrintStream outputHtml) {
        // picture could not be displayed
        sendHeader(outputHtml);
        sendContent(outputHtml);
    }

    /**
     * Send Initial Line and Headers
     * 
     * @param outputHtml
     */
    private void sendHeader(PrintStream outputHtml) {
        String fileName = (String) parsedInitialLineDict.get("path");
        File file = new File(htmlFolderDirecotry + fileName);
        RequestAnalyzer.prepareResponse(parsedRequestDict, rawInitLineDict,
                file);
        String responseHeadlines = prepareResonseLines();
        System.out.println(responseHeadlines);
        outputHtml.print(responseHeadlines);
        // HEAD Method
        if (rawInitLineDict.get("GET").equalsIgnoreCase("HEAD")) {
            outputHtml.flush();
            outputHtml.close();
        } else if (rawInitLineDict.get("GET").equalsIgnoreCase("GET")) {

        } else {
            System.out.println("Error");
        }
    }

    /**
     * Send Content
     * 
     * @param outputHtml
     */
    private void sendContent(PrintStream outputHtml) {
        String fileName = (String) parsedInitialLineDict.get("path");
        if (!rawInitLineDict.get("GET").equalsIgnoreCase("HEAD")) {
            File file = new File(htmlFolderDirecotry + fileName);
            RequestAnalyzer.prepareResponse(parsedRequestDict, rawInitLineDict,
                    file);
            if (fileName.equalsIgnoreCase("/control")) {
                generateControlPanelHtml(outputHtml);
            } else if (fileName.equalsIgnoreCase("/shutdown")) {
                generateShutdownHtml(outputHtml);
            } else if (rawPathProcessor(fileName)) {
                if (directoryFileDisplayList(fileName)) {
                    sendResponse(outputHtml, fileName);
                } else {
                    System.out
                            .println("File Format Not Supported: 404 Not Found\n");
                    generateErrorResponse(outputHtml, "400");
                }
            } else {
                System.out.println("File Not Exist: 404 Not Found\n");
                generateErrorResponse(outputHtml, "404");
            }
        } else {

        }
    }

    // ***** Read and Parse the Request *****
    // operate on the class fields
    private void processRequest(BufferedReader bufferReader) {
        // Read In Requests
        readInRequest(bufferReader);
        // Parse Initial Line
        parseInitLine(splitByList(initialLine));
        // Parse headers
        if (!rawHeadLines.isEmpty()) {
            parseHeadLines(rawHeadLines);
        }
    }

    /**
     * Get the request, line by line the first line is the intial line modify
     * the ArrayList
     * 
     * @param bufferReader
     */
    private void readInRequest(BufferedReader bufferReader) {
        // parsedRequestDict.clear();
        String data = null;
        boolean isPost = false;
        int contentLength = 0;
        String contentLengthString = null;
        char[] arr = null;
        while (true) {
            try {
                data = bufferReader.readLine();
                if (data.contains("Content-length")) {
                    contentLengthString = data;
                    String contentLengthStr = contentLengthString.split(":")[1]
                            .trim();
                    contentLength = Integer.parseInt(contentLengthStr);
                }
                if (data.toUpperCase().contains("POST")) {
                    isPost = true;
                }
                if (data.equals("") || data == null) {
                    if (isPost) {
                        System.out.println(contentLength);
                        arr = new char[contentLength];
                        bufferReader.read(arr);
                        System.out.println(String.copyValueOf(arr));
                        rawContent.add(String.copyValueOf(arr));
                        break;
                    } else {
                        break;
                    }
                }
                rawRequest.add(data);
            } catch (IOException e) {
                serverLog.writeFile(e.toString());
            }
        }
        if (!rawRequest.isEmpty())
            initialLine = new String(rawRequest.get(0));
        for (int index = 1; index < rawRequest.size(); index++) {
            rawHeadLines.add(rawRequest.get(index));
        }
    }

    // parse the initial line
    private void parseInitLine(ArrayList<String> initLine) {
        ArrayList<String> initLable = new ArrayList<String>();
        initLable.add("GET");
        initLable.add("path");
        initLable.add("HTTP");
        int index = 0;
        for (String element : initLine) {
            if (index == 1) {
                parsedInitialLineDict.put(initLable.get(index), element);
                rawInitLineDict.put(initLable.get(index), element);
                rawRequestDict.put(initLable.get(index), element);
            } else {
                // is toUpperCase() safe?
                parsedInitialLineDict.put(initLable.get(index),
                        element.toUpperCase());
                rawInitLineDict
                        .put(initLable.get(index), element.toUpperCase());
                rawRequestDict.put(initLable.get(index), element.toUpperCase());
            }
            index++;
        }
        if (parsedInitialLineDict.get("path").contains("http://")) {
            if (parsedInitialLineDict.get("HTTP").equalsIgnoreCase("HTTP/1.2")) {
                try {

                    URL urlPath = new URL(parsedInitialLineDict.get("path"));
                    parsedInitialLineDict.remove("path");
                    rawInitLineDict.remove("path");
                    parsedInitialLineDict.put("path", urlPath.getPath()
                            .toLowerCase());
                    rawInitLineDict
                            .put("path", urlPath.getPath().toLowerCase());
                    parsedHeaderLinesDict.put("Host", urlPath.getHost());
                    rawInitLineDict.put("Host", urlPath.getHost());
                    parsedHeaderLinesDict.put("Port",
                            Integer.toString(urlPath.getPort()));
                    rawInitLineDict.put("Port",
                            Integer.toString(urlPath.getPort()));
                    // urlPath.getPort();
                    // urlPath.getHost();
                    // urlPath.getPath();
                } catch (MalformedURLException e) {
                    serverLog.writeFile(e.toString());
                }
            } else {
                // error
            }
        }
    }

    private void parseHeadLines(ArrayList<String> headLines) {
        // System.out.println("Parse Header Lines");
        // System.out.println(headLines);
        for (String element : headLines) {
            parseHeadLine(element);
        }
    }

    // parse the head line into hashtable
    private void parseHeadLine(String headLine) {
        ArrayList<String> splitHeadLine = splitByColon(headLine);
        parsedHeaderLinesDict.put(splitHeadLine.get(0), splitHeadLine.get(1));
        rawInitLineDict.put(splitHeadLine.get(0), splitHeadLine.get(1));
        rawRequestDict.put(splitHeadLine.get(0), splitHeadLine.get(1));
    }

    // split by spaces
    private ArrayList<String> splitByList(String str) {
        System.out.println("NullPointerException Debug: " + str);
        String[] split = str.split("\\s+");
        for (int i = 0; i < split.length; i++) {
            System.out.println(split[i]);
        }

        return (new ArrayList<String>(Arrays.asList(split)));
    }

    // split by colon ":"
    private ArrayList<String> splitByColon(String str) {
        String[] split = str.split(":");
        ArrayList<String> splitList = new ArrayList<String>();
        if (split.length == 1) {
            split[0] = split[0].trim();
            splitList.add(split[0].trim());
            splitList.add("");
        } else if (split.length == 2) {
            for (String element : split) {
                splitList.add(element.trim());
            }
        } else if (split.length >= 2) {
            splitList.add(split[0]);
            String tempString = new String(split[1]);
            for (int index = 2; index < split.length; index++) {
                tempString = tempString + ":" + split[index];
            }
            splitList.add(tempString.trim());
        }
        return splitList;
    }

    private String http11HostHeader(String hostHeader) {
        String response = "";
        if (hostHeader.equals("")) {
            response = "HTTP/1.1 400 Bad Request\r\n"
                    + "Content-Type: text/html\r\n" + "Content-Length: 111\r\n"
                    + "\n" + "<html><body>\r\n"
                    + "<h2>No Host: header received</h2>\r\n"
                    + "HTTP 1.1 requests must include the Host: header.\r\n"
                    + "</body></html>\r\n";
        } else
            response = "";
        return response;
    }

    // ****** Security *****
    // security check
    private boolean pathSecurityCheck(String fileName) {
        // if "/directory" would split into "" and "directory"
        // at lease one element(represent the root). if empty, return false
        ArrayList<String> fileNameList = new ArrayList<String>(
                Arrays.asList(fileName.split("/")));
        Stack<String> fileNameStack = new Stack<String>();
        for (String fileElement : fileNameList) {
            if (fileElement.equals("..")) {
                if (!fileNameStack.empty()) {
                    fileNameStack.pop();
                    if (fileNameStack.empty()) {
                        return false;
                    }
                } else
                    throw new EmptyStackException();
            } else
                fileNameStack.push(fileElement);
        }
        return true;
    }

    // ***** Response *****
    // prepare response
    // send response(only valid response)
    private void sendResponse(PrintStream outputHtml, String fileName) {
        String responseHeadlines = prepareResonseLines();
        // output files
        try {
            if (rawPathProcessor(fileName)
                    && !responseHeadlines.contains("304")
                    && !responseHeadlines.contains("412")) {
                // html directory
                String fileLocation = htmlFolderDirecotry + fileName;
                if ((new File(fileLocation)).isDirectory()) {
                    generateDirectoryHtml(outputHtml, fileName);
                } else {
                    outputHtml.println("Content-length:"
                            + getHtmlFile(fileName).length);
                    outputHtml.println("\r\n");
                    outputHtml.write(getHtmlFile(fileName));
                    outputHtml.flush();
                    outputHtml.close();
                }
            } else {
                outputHtml.flush();
                outputHtml.close();
            }
        } catch (IOException e) {
            serverLog.writeFile(e.toString());
        }
    }

    private String prepareResonseLines() {
        String responseString = "";
        if (parsedRequestDict.get("expect") != null) {
            if (parsedRequestDict.get("expect")
                    .equalsIgnoreCase("100-continue")) {
                // responseDict.put("expect", "HTTP/1.1 100 Continue");
                responseString = responseString + "HTTP/1.1 100 Continue"
                        + "\r\n\n";
            }
        }
        // If Modified:
        if (parsedRequestDict.get("modify") != null) {
            if (parsedRequestDict.get("modify").equalsIgnoreCase(
                    "304 Not Modified")) {
                responseString = responseString + "HTTP/1.1 304 Not Modified"
                        + "\r\n";
                responseString = responseString + parsedRequestDict.get("Date")
                        + "\r\n";
                responseString = responseString + "Connection: close"
                        + "\r\n\n";
                return responseString;
            }
            if (parsedRequestDict.get("modify").equalsIgnoreCase(
                    "304 Not Modified")) {
                responseString = responseString
                        + "HTTP/1.1 412 Precondition Failed" + "\r\n";
                responseString = responseString + parsedRequestDict.get("Date")
                        + "\r\n";
                responseString = responseString + "Connection: close"
                        + "\r\n\n";
                return responseString;
            } else if (parsedRequestDict.get("modify").equalsIgnoreCase("true")) {

            } else
                System.out.println("Error");
        }
        // add date and content type
        if (parsedRequestDict.get("HTTP").equalsIgnoreCase("HTTP/1.0")) {
            // send 1.1 or 1.0?
            responseString = responseString + "HTTP/1.0 200 OK\r\n";
            if (parsedRequestDict.get("Date") != null) {
                responseString = responseString + "Date: "
                        + parsedRequestDict.get("Date") + "\r\n";
            }

        } else if (parsedRequestDict.get("HTTP").equalsIgnoreCase("HTTP/1.1")) {
            responseString = responseString + "HTTP/1.1 200 OK\r\n";
            if (parsedRequestDict.get("Date") != null) {
                responseString = responseString + "Date: "
                        + parsedRequestDict.get("Date") + "\r\n";
            }
        } else {

        }
        responseString = responseString + "Content-Type" + ": "
                + parsedRequestDict.get("Content-Type") + "\r\n";
        // responseString = responseString + "Content-Length" + ": "
        // + parsedRequestDict.get("Content-Type") + "\r\n";
        // add empty line
        responseString = responseString + "Connection: close" + "\r\n";

        return responseString;
    }

    // ***** Validation Check *****
    // check resource
    private boolean rawPathProcessor(String fileName) {
        // String fileLocation = htmlFolderDirecotry + fileName;
        File htmlFile = new File(htmlFolderDirecotry + fileName);
        // System.out.println("DEbug:" + "    " + htmlFolderDirecotry);
        // System.out.println("DEbug:" + "    " + htmlFile.getName());
        // System.out.println(htmlFolderDirecotry + "    " +
        // htmlFile.getName());
        if (htmlFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    // check if the file is supported
    // Updated to: probe mimeType
    private boolean directoryFileDisplayList(String fileName) {
        Path path = Paths.get(htmlFolderDirecotry + fileName);
        String probedMimeType = "";
        try {
            probedMimeType = Files.probeContentType(path);
        } catch (IOException e) {
            serverLog.writeFile(e.toString());
        }
        // System.out.println("ddd");
        // System.out.println(probedMimeType);
        if (probedMimeType != null) {
            if (!probedMimeType.contains("directory")) {
                parsedRequestDict.put("Content-Type", probedMimeType);
            }
            // folder
            else {
                parsedRequestDict.put("Content-Type", "text/html");
            }
        } else {
            parsedRequestDict.put("Content-Type", "text/html");
        }
        return true;
    }

    // ***** Generate Contents *****
    // Error Response
    private void generateErrorResponse(PrintStream outputHtml, String erroType) {
        if (erroType.equalsIgnoreCase("400")) {
            outputHtml.print("HTTP/1.1 400 BADREQUEST\r\n\n");
            outputHtml.flush();
            outputHtml.close();
        }
        if (erroType.equalsIgnoreCase("404")) {
            outputHtml.print("HTTP/1.1 404 NOTFOUND \r\n\n");
            outputHtml.flush();
            outputHtml.close();
        }
    }

    // Error Response
    private String generateErrorResponseString(String erroType) {
        String responseString = "";
        if (erroType.equalsIgnoreCase("400")) {
            responseString = responseString + ("HTTP/1.1 400 BADREQUEST\r\n\n");
        }
        if (erroType.equalsIgnoreCase("404")) {
            responseString = responseString + ("HTTP/1.1 404 NOTFOUND \r\n\n");
        }
        return responseString;
    }

    // Read Local HTML File
    // As byte[]
    private byte[] getHtmlFile(String fileName) {
        String fileLocation = htmlFolderDirecotry + fileName;
        File htmlFile = new File(fileLocation);
        FileInputStream fileStream = null;
        byte[] btyeFile = new byte[(int) htmlFile.length()];
        try {
            fileStream = new FileInputStream(htmlFile);
            fileStream.read(btyeFile);
            fileStream.close();
        } catch (IOException e) {
            serverLog.writeFile(e.toString());
        }
        return btyeFile;
    }

    // generate Directory HTML
    // error: recursive folder?
    // Updates: change to string buffer and return int, become slow?
    private void generateDirectoryHtml(PrintStream outputHtml, String fileName) {
        String currentDirectory = htmlFolderDirecotry + fileName;
        File fileFolder = new File(currentDirectory);
        StringBuilder outputBuffer = new StringBuilder();
        File[] fileList = fileFolder.listFiles();
        // String htmlPageString = "";
        outputBuffer.append("<html>");
        outputBuffer.append("  <body>");
        outputBuffer
                .append("    <h3>Hangfei Lin's Server (Pennkey: hangfei)</h3>");
        outputBuffer.append("    <h2>Directory</h2>");
        outputBuffer.append("    <hr>");
        for (File file : fileList) {
            if (!file.isHidden()) {
                String filePath = fileName + "/" + file.getName();
                String trimmedFilePath = filePath.trim().replaceAll("/+", "/");
                outputBuffer.append("<p>");
                outputBuffer.append("<a href=\"" + trimmedFilePath + "\"" + ">"
                        + file.getName() + "</a>");
                outputBuffer.append("</p>");
            }
        }
        outputBuffer.append("    <hr>");
        outputBuffer.append("  </body>");
        outputBuffer.append("<html>");
        if (parsedInitialLineDict.get("HTTP").equalsIgnoreCase("HTTP/1.0")) {
            outputHtml.println("Content-length: " + outputBuffer.length());
            outputHtml.println("\r\n");
            outputHtml.println(outputBuffer.toString());
            outputHtml.flush();
            outputHtml.close();
        } else {
            outputHtml.println("Transfer-Encoding: chunked");
            outputHtml.println("\r\n");
            outputHtml.println(chunkedEncoding(outputBuffer, 26));
            outputHtml.flush();
            outputHtml.close();
        }
    }

    /**
     * Shut donw the serer and generate a html page
     * 
     * @param outputHtml
     */
    private void generateShutdownHtml(PrintStream outputHtml) {
        StringBuilder outputBuffer = new StringBuilder();
        outputBuffer.append("<html>");
        outputBuffer.append("  <body");
        outputBuffer
                .append("    <h3>HangfeiLin's Server (Pennkey: hangfei)</h3>");
        outputBuffer.append("    <h2>Server Already shudown.</h2>");
        outputBuffer.append("    <hr>");
        outputBuffer.append("<a href=\"" + "shutdown" + "\"" + ">"
                + "Prove to me." + "</a>");
        outputBuffer.append("    <hr>");
        outputBuffer.append("  </body>");
        outputBuffer.append("</html>");
        if (parsedInitialLineDict.get("HTTP").equalsIgnoreCase("HTTP/1.0")) {
            outputHtml.println("Content-length: " + outputBuffer.length());
            outputHtml.println("\r\n");
            outputHtml.println(outputBuffer.toString());
            outputHtml.flush();
            outputHtml.close();
        } else {
            outputHtml.println("Transfer-Encoding: chunked");
            outputHtml.println("\r\n");
            outputHtml.println(chunkedEncoding(outputBuffer, 26));
            outputHtml.flush();
            outputHtml.close();
        }
        shutdownServer();
    }

    /**
     * Control Panel HTML
     */
    private void generateControlPanelHtml(PrintStream outputHtml) {
        Hashtable<Long, Thread.State> threadPoolStateTable = ControlPanel
                .controlPanelState();
        Hashtable<Long, String> threadPoolStatusTable = ControlPanel
                .controlPanelStatus();
        Set<Long> threadIdSet = ControlPanel.threadIdSet();
        String threadIdString = null;
        String threadStatus = null;
        StringBuilder outputBuffer = new StringBuilder();
        outputBuffer.append("<html>");
        outputBuffer.append("  <body");
        outputBuffer.append("    <h3>Hangfei's Server</h3>");
        outputBuffer.append("    <h2>Server Control Panel</h2>");
        outputBuffer.append("    <hr>");
        for (Long threadID : threadIdSet) {
            threadIdString = Long.toString(threadID);
            if (threadPoolStateTable.get(threadID) != Thread.State.WAITING) {
                threadStatus = threadPoolStatusTable.get(threadID);
            } else {
                threadStatus = threadPoolStateTable.get(threadID).toString();
            }
            outputBuffer.append("<h4>");
            outputBuffer.append(threadIdString);
            outputBuffer.append("<a href=\"" + threadStatus + "\"" + ">"
                    + threadStatus + "</a>");
            outputBuffer.append("</h4>");
        }
        outputBuffer.append("<a href=\"" + "shutdown" + "\"" + ">" + "Shutdown"
                + "</a>");
        outputBuffer.append("</br>");
        outputBuffer.append("<a href=\"" + "serverlog.txt" + "\"" + ">"
                + "Server Log" + "</a>");
        outputBuffer.append("    <hr>");
        outputBuffer.append("  </body>");
        outputBuffer.append("</html>");
        if (parsedInitialLineDict.get("HTTP").equalsIgnoreCase("HTTP/1.0")) {
            outputHtml.println("Content-length: " + outputBuffer.length());
            outputHtml.println("\r\n");
            outputHtml.println(outputBuffer.toString());
            outputHtml.flush();
            outputHtml.close();
        } else {
            outputHtml.println("Transfer-Encoding: chunked");
            outputHtml.println("\r\n");
            outputHtml.println(chunkedEncoding(outputBuffer, 26));
            outputHtml.flush();
            outputHtml.close();
        }
    }

    /**
     * Turn a stringbuilder into a chunked encoded stringbuilder ??? What about
     * read in a chunked request?
     * 
     * @param outputBuffer
     * @param portionSize
     * @return
     */
    private StringBuilder chunkedEncoding(StringBuilder outputBuffer,
            int portionSize) {
        StringBuilder chunkedOutput = new StringBuilder();
        while (outputBuffer.length() > 0) {
            if (outputBuffer.length() >= portionSize) {
                if (chunkedOutput.length() == 0) {
                    chunkedOutput.append(Integer.toHexString(portionSize) + ";"
                            + "ignore-stuff-here");
                    chunkedOutput.append("\r\n");
                } else {
                    chunkedOutput.append(Integer.toHexString(portionSize));
                    chunkedOutput.append("\r\n");
                }
                chunkedOutput.append(outputBuffer.substring(0, portionSize));
                chunkedOutput.append("\r\n");
                outputBuffer.delete(0, portionSize);
            } else {
                if (chunkedOutput.length() == 0) {
                    chunkedOutput.append(Integer.toHexString(outputBuffer
                            .length()) + ";" + "ignore-stuff-here");
                    chunkedOutput.append("\r\n");
                } else {
                    chunkedOutput.append(Integer.toHexString(outputBuffer
                            .length()));
                    chunkedOutput.append("\r\n");
                }
                chunkedOutput.append(outputBuffer.substring(0,
                        outputBuffer.length()));
                chunkedOutput.append("\r\n");
                outputBuffer.delete(0, outputBuffer.length());
            }
        } // End of while loop
        chunkedOutput.append(0);
        chunkedOutput.append("\r\n");
        chunkedOutput.append("\r\n"); // [blank line here]
        return chunkedOutput;
    }

    // ***** Thread Status *****
    public String getWorkingStatus() {
        return currentUrl;
    }

    // ***** Shutdown *****
    public void shutdownServer() {

        HttpServer.closeSocket();
    }

    // ****** Getter and Setter *****
    public static BlockingQueue getTaskQueue() {
        return taskQueue;
    }

    public void setRunFlag(boolean runFlag) {
        this.runFlag = runFlag;
    }

    public static void setTaskQueue(BlockingQueue taskQueue) {
        ThreadWorker.taskQueue = taskQueue;
    }

    private void setCurrentUrl(String fileName) {
        currentUrl = fileName;
    }

    public static void setRootDirectory(String rootDirecotry) {
        htmlFolderDirecotry = rootDirecotry;
    }
}
