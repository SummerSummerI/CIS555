package edu.upenn.cis455.webserver;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.Set;
import java.util.Stack;
import java.util.Hashtable;

/**
 * Worker Thread
 * Deque the socket, read in the request, and responde
 * @author cis455
 *
 */
public class ThreadWorker extends Thread{
    private static BlockingQueue taskQueue = null;
    public static String htmlFolderDirecotry = "/";
    private ArrayList<String> rawRequest = new ArrayList<String>();
    private ArrayList<String> rawHeadLines = new ArrayList<String>();
    private String initialLine = null;
    private String currentUrl = null;
    private boolean runFlag = false;
    
    private Hashtable<String, String> initialLineParsed = new Hashtable<String, String>();
    private Hashtable<String, String> headLinesDict = new Hashtable<String, String>();
    // initialLineParsed + headLinesDict
    private Hashtable<String, String> requestDict = new Hashtable<String, String>();
    private Hashtable<String, String> rawRequestDict = new Hashtable<String, String>();
    
    private Hashtable<String, String> parsedRequestDict = new Hashtable<String, String>();    
    private Hashtable<String, String> responseDict = new Hashtable<String, String>();    
    public ThreadWorker(BlockingQueue taskQueue) {
        ThreadWorker.setTaskQueue(taskQueue);
    }
    public void run() {
        runFlag = true;
        while(runFlag) {
            Socket clientSocket = null;  
            try {
                parsedRequestDict.clear();
                rawRequest.clear();
                rawHeadLines.clear();
                headLinesDict.clear();
                initialLineParsed.clear();
                requestDict.clear();
                rawRequestDict.clear();
                clientSocket = (Socket) getTaskQueue().dequeue();
                // set timeout
                clientSocket.setSoTimeout(10000);
                // input source
                // problem here! could not close here!s
                InputStreamReader reader= new InputStreamReader(clientSocket.getInputStream());
                BufferedReader bufferReader = new BufferedReader(reader);
                // output source
                PrintStream outputHtml = new PrintStream(clientSocket.getOutputStream(), true);  
                
                // process the requests
                processRequest(bufferReader);
                
                String fileName = (String)initialLineParsed.get("path");
                setCurrentUrl(fileName);
                directoryFileDisplayList(fileName);

                // respond to the request
                // Security Check
                if( !pathSecurityCheck(fileName)) {
                    System.out.println("Insecure path: 400 Bad Request\n");
                    generateErrorResponse(outputHtml, "400");
                }
                else {
                    // Validation Check 
                    // HTTP/1.1
                    int respondeCode = validationCheck(outputHtml);
                    if(respondeCode != 200) {
                        // send error response
                        sendErrorMessage(outputHtml, respondeCode, "");
                    }
                    else {
                        // if valid, send response
                        doResponde(outputHtml) ;
                    }
                }
            } 
            catch (InterruptedException e) {
               // e.printStackTrace();
                System.out.println("Thread Interrupted!");
            } 
            catch (SocketException e) {         
                e.printStackTrace();
            }  
            catch (IOException e) {
                e.printStackTrace();
            }  
            catch (NullPointerException e) {
                e.printStackTrace();
            }
            finally {
                // Close again?
            }
        } 
    }
    
    
    private int validationCheck(PrintStream outputHtml) {
        // different between get and head?
        if(requestDict.get("HTTP").equalsIgnoreCase("HTTP/1.0")) {
            if(rawRequestDict.get("path").contains("http://")) {
                System.out.println("HTTP/1.0 Not Support Absolute Path: 404 Not Found\n");
                return 404;
            }
        }
        else if(requestDict.get("HTTP").equalsIgnoreCase("HTTP/1.1")) {
            if( requestDict.get("Host") == null ) {
                System.out.println("No Host!!!: header received: 400 Not Found\n");
                return 400;
            }
            else if(!requestDict.get("Host").equals(("localhost:" + Integer.toString(HttpServer.port)))
                    && !requestDict.get("Host").equals(("localhost"))) {
                System.out.println("Host incorrect: header received: 400 Not Found\n");
                // ?
                return 400;
            }
            if(rawRequestDict.get("path").contains("http://")) {
                System.out.println("HTTP/1.1 ??? Not Support Absolute Path: 404 Not Found\n");
                //return 200;
            }
        }
        else {
            
        }
        return 200;
    }
    
    private void sendErrorMessage(PrintStream outputHtml, int errorCode, String errorMessage) {
        if(errorCode == 400) {
            System.out.println("No Host: header received: 400 Not Found\n");
            generateErrorResponse(outputHtml, "400");
        }
        if(errorCode == 404) {
            System.out.println("HTTP/1.1 Not Support Absolute Path: 404 Not Found\n");
            generateErrorResponse(outputHtml, "404");
        }
        if(errorCode == 404) {
            System.out.println("HTTP/1.0 Not Support Absolute Path: 404 Not Found\n");
            generateErrorResponse(outputHtml, "404");
        }
    }
    
    
    // Responde according to the requests
    // Separate headlines from contents
    private void doResponde(PrintStream outputHtml) {
        sendHeader(outputHtml);
        sendContent(outputHtml);
    }
    
    private void sendHeader(PrintStream outputHtml) {
        String fileName =  (String)initialLineParsed.get("path");     
        File file = new File(htmlFolderDirecotry +fileName);
        RequestAnalyzer.prepareResponse(parsedRequestDict, requestDict, file);
        String responseHeadlines = prepareResonseLines();
        outputHtml.print(responseHeadlines);
        // HEAD Method
        if(requestDict.get("GET").equalsIgnoreCase("HEAD")) {
            outputHtml.flush();
            outputHtml.close();
        }
        else if(requestDict.get("GET").equalsIgnoreCase("GET")) {

        }
        else {
            System.out.println("Error");
        }
    }
    
    private void sendContent(PrintStream outputHtml) {
        String fileName = (String)initialLineParsed.get("path");
        //directoryFileDisplayList(fileName);
        if(!requestDict.get("GET").equalsIgnoreCase("HEAD")) {
            File file = new File(htmlFolderDirecotry + fileName);
            RequestAnalyzer.prepareResponse(parsedRequestDict, requestDict, file);
            if(fileName.equalsIgnoreCase("/control")) {
                generateControlPanelHtml(outputHtml);
            }
            else if(fileName.equalsIgnoreCase("/shutdown")) {
                generateShutdownHtml(outputHtml);
            }
            else if(rawPathProcessor(fileName)) {
                if(directoryFileDisplayList(fileName)) {
                    sendResponse(outputHtml, fileName);
                }
                else {
                    System.out.println("File Format Not Supported: 404 Not Found\n");
                    generateErrorResponse(outputHtml, "400");
                }
            }
            else {
                System.out.println("File Not Exist: 404 Not Found\n");
                generateErrorResponse(outputHtml, "404");
            }
        }
        else {
            
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
        if(!rawHeadLines.isEmpty()) {
            parseHeadLines(rawHeadLines);        
        }
    }
    
    // read the request
    private void readInRequest(BufferedReader bufferReader) {
        // clear the old data

        //parsedRequestDict.clear();
        String data = null;
        while(true) {
            try {
                data = bufferReader.readLine();
                if(data == null || data.equals("")) break;
                rawRequest.add(data);
            } catch (IOException e) {
                // socket timeout would trigger this exception
                //e.printStackTrace();
            }
        }
        if(!rawRequest.isEmpty()) initialLine = new String(rawRequest.get(0));        
        for(int index = 1; index < rawRequest.size(); index++) {
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
        for(String element : initLine) {
            if(index == 1) {
                initialLineParsed.put(initLable.get(index), element);
                requestDict.put(initLable.get(index), element);
                rawRequestDict.put(initLable.get(index), element);
            }
            else {
                // is toUpperCase() safe?
                initialLineParsed.put(initLable.get(index), element.toUpperCase());
                requestDict.put(initLable.get(index), element.toUpperCase());
                rawRequestDict.put(initLable.get(index), element.toUpperCase());
            }
            index++;
        }
        if(initialLineParsed.get("path").contains("http://")) {
            if(initialLineParsed.get("HTTP").equalsIgnoreCase("HTTP/1.2")) {
                try {
                    
                    URL urlPath = new URL(initialLineParsed.get("path"));
                    initialLineParsed.remove("path");
                    requestDict.remove("path");
                    initialLineParsed.put("path", urlPath.getPath().toLowerCase());
                    requestDict.put("path", urlPath.getPath().toLowerCase());
                    headLinesDict.put("Host", urlPath.getHost());
                    requestDict.put("Host", urlPath.getHost());
                    headLinesDict.put("Port", Integer.toString(urlPath.getPort()));
                    requestDict.put("Port", Integer.toString(urlPath.getPort()));
                    //urlPath.getPort();
                    //urlPath.getHost();
                    //urlPath.getPath();
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }             
            }
            else {
                // error
            }    
        }
    }
    
    
    
    private void parseHeadLines(ArrayList<String> headLines) {
        for(String element: headLines) {
            parseHeadLine(element);
        }
    }

    // parse the head line into hashtable
    private void parseHeadLine(String headLine) {
        ArrayList<String> splitHeadLine = splitByColon(headLine);
        headLinesDict.put(splitHeadLine.get(0), splitHeadLine.get(1));
        requestDict.put(splitHeadLine.get(0), splitHeadLine.get(1));
    }
    
    // split by spaces
    private ArrayList<String> splitByList(String str) {
        String[] split = str.split("\\s+");
        return (new ArrayList<String>(Arrays.asList(split)));
    }
    
    //split by colon ":"
    private ArrayList<String> splitByColon(String str) {
        String[] split = str.split(":");
        ArrayList<String> splitList= new ArrayList<String>();
        if(split.length == 1) {
            split[0] = split[0].trim();
            splitList.add(split[0].trim());
            splitList.add("");
        }
        else if(split.length == 2){
            for(String element: split) {
                splitList.add(element.trim());
            }            
        }
        else if(split.length >= 2){
            splitList.add(split[0]);
            String tempString = new String(split[1]);
            for(int index = 2; index < split.length; index++) {
                tempString = tempString + ":" + split[index];
            } 
            splitList.add(tempString.trim());
        }
        return splitList;        
    }
   
    
    private String http11HostHeader(String hostHeader) {
        String response = "";
        if(hostHeader.equals("")) {
            response = 
                "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: 111\r\n" + 
                "\n" +
                "<html><body>\r\n" +
                "<h2>No Host: header received</h2>\r\n" +
                "HTTP 1.1 requests must include the Host: header.\r\n" +
                "</body></html>\r\n";
        }
        // to do
        else response = "";
        return response;
    }

    // ****** Security *****
    // security check
    private boolean pathSecurityCheck(String fileName) {
        // if "/directory" would split into "" and "directory"
        // at lease one element(represent the root). if empty, return false
        ArrayList<String> fileNameList = new ArrayList<String>(Arrays.asList(fileName.split("/")));
        Stack<String> fileNameStack = new Stack<String>();
        for(String fileElement: fileNameList) {
            if(fileElement.equals("..")) {
                if(!fileNameStack.empty()) {
                    fileNameStack.pop();
                    if(fileNameStack.empty()) {
                        return false;
                    }
                }
                else throw new EmptyStackException();
            }
            else fileNameStack.push(fileElement);
        }
        return true;
    }
    


    
    // ***** Response *****
    // prepare response
    // send response(only valid response)
    private void sendResponse(PrintStream outputHtml, String fileName) {
        String responseHeadlines = prepareResonseLines();

        // output contents
        try {
            // shutdown functionality
            if(fileName.equalsIgnoreCase("/shutdown")) {
                generateShutdownHtml(outputHtml);
            }
            // control panel functionality
            else if(fileName.equalsIgnoreCase("/control")) generateControlPanelHtml(outputHtml);
            // redundent 
            else if(rawPathProcessor(fileName) 
                    && !responseHeadlines.contains("304") && !responseHeadlines.contains("412")) {
                // html directory
                String fileLocation = htmlFolderDirecotry + fileName;
                if((new File(fileLocation)).isDirectory()) {
                    generateDirectoryHtml(outputHtml, fileName);
                }
                else{
                    outputHtml.write(getHtmlFile(fileName));
                    outputHtml.flush();
                    outputHtml.close();
                }
            }
            else {
                outputHtml.flush();
                outputHtml.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String prepareResonseLines() {
        String responseString = "";
        if(parsedRequestDict.get("expect") != null) {
            if(parsedRequestDict.get("expect").equalsIgnoreCase("100-continue")) {
                //responseDict.put("expect", "HTTP/1.1 100 Continue");
                responseString = responseString +
                        "HTTP/1.1 100 Continue" + "\r\n\n";
            }
        }
        // If Modified: 
        if(parsedRequestDict.get("modify") != null) {
            if(parsedRequestDict.get("modify").equalsIgnoreCase("304 Not Modified")) {
                responseString = responseString +
                        "HTTP/1.1 304 Not Modified" + "\r\n";
                responseString = responseString + parsedRequestDict.get("Date") + "\r\n";
                responseString = responseString + "Connection: close" + "\r\n\n";
                return responseString;
            }
            if(parsedRequestDict.get("modify").equalsIgnoreCase("304 Not Modified")) {
                responseString = responseString +
                        "HTTP/1.1 412 Precondition Failed" + "\r\n";
                responseString = responseString + parsedRequestDict.get("Date") + "\r\n";
                responseString = responseString + "Connection: close" + "\r\n\n";
                return responseString;
            }
            else if(parsedRequestDict.get("modify").equalsIgnoreCase("true")) {
                
            }
            else System.out.println("Error");
        }
        // 
        if(parsedRequestDict.get("HTTP").equalsIgnoreCase("HTTP/1.0")) {
            // send 1.1 or 1.0?
            responseString = responseString + "HTTP/1.0 200 OK\r\n";
            if(parsedRequestDict.get("Date") != null) {
                responseString = responseString + "Date: " +
                        parsedRequestDict.get("Date") + "\r\n";
            }
            responseString = responseString + "Content-Type" + ": "
                     + parsedRequestDict.get("Content-Type") + "\r\n";            
        }
        else if(parsedRequestDict.get("HTTP").equalsIgnoreCase("HTTP/1.1")) {
            responseString = responseString + "HTTP/1.1 200 OK\r\n";
            if(parsedRequestDict.get("Date") != null) {
                responseString = responseString + "Date: " +
                        parsedRequestDict.get("Date") + "\r\n";
            }
            responseString = responseString + "Content-Type" + ": " +
                    parsedRequestDict.get("Content-Type") + "\r\n";           
        }
        else {
            
        }
        // add empty line
        responseString = responseString + "Connection: close" + "\r\n";
        responseString = responseString + "\n";
        return responseString;
    }
    
    // ***** Validation Check *****
    // check resource
    private boolean rawPathProcessor(String fileName) {
        //String fileLocation = htmlFolderDirecotry + fileName;
        File htmlFile = new File(htmlFolderDirecotry + fileName);
        if(htmlFile.exists()) {
            return true;
        }
        else {
            return false;
        }
    }
    
    // check if the file is supported
    private boolean directoryFileDisplayList(String fileName) {
        boolean checkFlag = false;
        ArrayList<String> fileSufixList = new ArrayList<String>();
        // ???
        fileSufixList.add(".jpg");
        fileSufixList.add(".gif");
        fileSufixList.add(".png");
        fileSufixList.add(".html");
        fileSufixList.add(".txt");
        if(fileName.contains(".")) {
            //if(fileName.charAt(fileName.length() - 1) == '~') return false;
            //if(fileName.charAt(fileName.length() - 1) == '#') return false;
            //if(!(Character.isLetter(fileName.charAt(fileName.length() - 1))) 
            //        && fileName.charAt(fileName.length() - 1) != '/') return false;
            for(String element: fileSufixList) {
                if(fileName.contains((element))) {
                    if(element.equalsIgnoreCase(".gif")) {
                        parsedRequestDict.put("Content-Type", "image/gif");
                    }
                    if(element.equalsIgnoreCase(".png")) {
                        parsedRequestDict.put("Content-Type", "image/png");
                    }
                    if(element.equalsIgnoreCase(".jpg")) {
                        parsedRequestDict.put("Content-Type", "image/jpeg");

                    }
                    if(element.equalsIgnoreCase(".txt")) {
                        parsedRequestDict.put("Content-Type", "text/plain");
                    }
                    if(element.equalsIgnoreCase(".html")) {
                        parsedRequestDict.put("Content-Type", "text/html");
                    }
                    checkFlag = true;                
                }
            }
        }
        // folder
        else {
            checkFlag = true;
            parsedRequestDict.put("Content-Type", "text/html");
        }
        
        return true;
    }

    // ***** Generate Contents *****
    // Error Response
    private void generateErrorResponse(PrintStream outputHtml, String erroType) {
        if(erroType.equalsIgnoreCase("400")) {
            outputHtml.print("HTTP/1.1 400 BADREQUEST\r\n\n");
            outputHtml.flush();
            outputHtml.close(); 
        }
        if(erroType.equalsIgnoreCase("404")) {
            outputHtml.print("HTTP/1.1 404 NOTFOUND \r\n\n");
            outputHtml.flush();
            outputHtml.close(); 
        }
    }
    
    // Error Response
    private String generateErrorResponseString(String erroType) {
        String responseString = "";
        if(erroType.equalsIgnoreCase("400")) {
            responseString = responseString + ("HTTP/1.1 400 BADREQUEST\r\n\n");
        }
        if(erroType.equalsIgnoreCase("404")) {
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
            e.printStackTrace();
        }  
        return btyeFile;
    } 
    
    // generate Directory HTML
    // error: recursive folder?
    private void generateDirectoryHtml(PrintStream outputHtml, String fileName) {
        String currentDirectory = htmlFolderDirecotry + fileName;
        File fileFolder = new File(currentDirectory);
        File[] fileList = fileFolder.listFiles();
        String htmlPageString = "";
        htmlPageString = htmlPageString + "<html>";
        htmlPageString = htmlPageString + "  <body>";
        htmlPageString = htmlPageString + "    <h3>Hangfei Lin's Server (Pennkey: hangfei)</h3>";
        htmlPageString = htmlPageString + "    <h2>Directory</h2>";
        htmlPageString = htmlPageString + "    <hr>";
        
        for(File file: fileList) {
                String filePath =  fileName + "/" + file.getName();
                String trimmedFilePath = filePath.trim().replaceAll("/+", "/");
                htmlPageString = htmlPageString + "<p>";
                htmlPageString = htmlPageString + "<a href=\"" + trimmedFilePath + "\"" + ">" + file.getName() + "</a>";
                htmlPageString = htmlPageString + "</p>";
        }
        htmlPageString = htmlPageString + "    <hr>";
        htmlPageString = htmlPageString + "  </body>";
        htmlPageString = htmlPageString + "<html>";
        outputHtml.println(htmlPageString);
        outputHtml.flush();
        outputHtml.close();
    }
    
    // Shutdown HTML
    private void generateShutdownHtml(PrintStream outputHtml) {
        outputHtml.println("<html>");
        outputHtml.println("  <body");
        outputHtml.println("    <h3>HangfeiLin's Server (Pennkey: hangfei)</h3>");
        outputHtml.println("    <h2>Server Control Panel</h2>");
        outputHtml.println("    <hr>");
        outputHtml.println("<a href=\"" + "shutdown" + "\"" + ">" + "Shutdown" + "</a>");
        outputHtml.println("    <hr>");
        outputHtml.println("  </body>");
        outputHtml.println("</html>");
        outputHtml.flush();
        outputHtml.close();      
        shutdownServer();
    }
    
    // Control Panel HTML
    private void generateControlPanelHtml(PrintStream outputHtml) {
        Hashtable <Long, Thread.State> threadPoolStateTable = ControlPanel.controlPanelState();
        Hashtable <Long, String> threadPoolStatusTable = ControlPanel.controlPanelStatus();
        Set<Long> threadIdSet = ControlPanel.threadIdSet();
        String threadIdString = null;
        String threadStatus = null;
        outputHtml.println("<html>");
        outputHtml.println("  <body");
        outputHtml.println("    <h3>Hangfei's Server</h3>");
        outputHtml.println("    <h2>Server Control Panel</h2>");
        outputHtml.println("    <hr>");
        for(Long threadID: threadIdSet) {
            threadIdString = Long.toString(threadID);
            if(threadPoolStateTable.get(threadID) != Thread.State.WAITING) {
                threadStatus = threadPoolStatusTable.get(threadID);
            }
            else {
                threadStatus = threadPoolStateTable.get(threadID).toString();
            }
            outputHtml.println("<h4>");
            outputHtml.println(threadIdString);
            outputHtml.println("<a href=\"" + threadStatus + "\"" + ">" + threadStatus + "</a>");
            outputHtml.println("</h4>");
        }
        outputHtml.println("<a href=\"" + "shutdown" + "\"" + ">" + "Shutdown" + "</a>");
        outputHtml.println("    <hr>");
        outputHtml.println("  </body>");
        outputHtml.println("</html>");
        outputHtml.flush();
        outputHtml.close();
    }
    
    // ***** Thread Status *****
    public String getWorkingStatus() {
        return currentUrl;
    }
    
    // ***** Shutdown *****
    public void shutdownServer() { 
        ThreadPool.closeThreads();
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
        // TODO Auto-generated method stub
        
    }
    
    public static void setRootDirectory(String rootDirecotry) {
        htmlFolderDirecotry = rootDirecotry;
    }
}
