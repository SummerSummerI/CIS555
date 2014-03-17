package edu.upenn.cis455.webserver;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
/**
 * Program to do
 * 
 * @version 
 * @author cis455
 *
 */
public class RequestAnalyzer {
    
    
    //
    public boolean validationCheck(Hashtable<String, String> request) {
        if( !request.containsKey("Host")) {
            return false;
        }
        return true;
    }
    
    //
    public static void prepareResponse(Hashtable<String, String> parsedRequestDict, 
            Hashtable<String, String> request, File file) {
        Hashtable<String, String> responseDict = new Hashtable<String, String>();
        if(request.get("HTTP") != null) {
            parsedRequestDict.put("HTTP", request.get("HTTP"));
            if(request.get("path") != null) {
                // path
                parsedRequestDict.put("path", request.get("path"));
            }
            if(request.get("GET") != null) {
                // GET
                parsedRequestDict.put("GET", request.get("GET"));
            }
        }
        if(request.get("expect") != null) {
            // To-do: append 100 wait
            parsedRequestDict.put("expect", request.get("expect"));
        }
        // add date to each response except 100
        SimpleDateFormat df1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        df1.setTimeZone(TimeZone.getTimeZone("GMT"));
        parsedRequestDict.put("Date", df1.format(new Date()));
        // handle modification
        
        //System.out.println("file.lastModified: " + file.lastModified());
        if(modifySinceCheck(request, file)) {
            // send contents
            parsedRequestDict.put("modify", "true");
        }
        else {
            // send only headers, no contents
            if(request.get("If-Modified-Since") != null) {
                parsedRequestDict.put("modify", "304 Not Modified");
            }    
            else if(request.get("If-Unmodified-Since") != null) {
                parsedRequestDict.put("modify", "412 Precondition Failed");
            } 
            else System.out.println("ERROR");
        }
    }
    
    // true if we need to send back new file
    private static boolean modifySinceCheck(Hashtable<String, String> request, File file) {
        boolean modification = true;
        String modifyType = null;
        String dateString = null;
        if(request.get("If-Modified-Since") != null) {
            modifyType = "If-Modified-Since";
            dateString = request.get("If-Modified-Since");
            modification = modificationDateCheck(modifyType, uniformDate(dateString), file); 
        }    
        else if(request.get("If-Unmodified-Since") != null) {
            modifyType = "If-Unmodified-Since";
            dateString = request.get("If-Unmodified-Since");
            modification = modificationDateCheck(modifyType, uniformDate(dateString), file); 
        } 
        else modification = true;
        return modification;
    }
    
    // true if we need to send back new file
    private static boolean modificationDateCheck(String modifyType, Date date, File file) {
        // Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT-2"));
        
        SimpleDateFormat df1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        df1.setTimeZone(TimeZone.getTimeZone("GMT"));
        //parsedRequestDict.put("Date", df1.format(new Date()));
        
        
        
        
        
        if(modifyType.equalsIgnoreCase("If-Modified-Since")) {
    
            if( date.getTime() < file.lastModified()) {
                System.out.println("TRUE");
                return true;
            }
            else {
                System.out.println("False");
                return false;
            }
        }
        else if(modifyType.equalsIgnoreCase("If-Unmodified-Since")) {
    
            if( date.getTime() > file.lastModified()) {
                return true;
            }
            else {
                return false;            
            }
        }
        //else throw new Exception();
        return true;
    }
    
    private static Date uniformDate(String dateString) {
        String upperCaseDate = dateString.toUpperCase();
        SimpleDateFormat df1 = null;
        if(upperCaseDate.contains("GMT")) {
            if(upperCaseDate.charAt(3) == ',') {
                df1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            }
            else if(upperCaseDate.charAt(6) == ',' || upperCaseDate.charAt(7) == ',') {
                df1 = new SimpleDateFormat("E, dd-MMM-yy HH:mm:ss z");
            }
        }
        else if(!upperCaseDate.contains(",") && !upperCaseDate.contains("-")){
            df1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        }
        //else throw new ParseException();
        Date date = null;
        try {
            date = df1.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }
    
    // split by spaces
    private ArrayList<String> splitBySpaceList(String str) {
        String[] split = str.split("\\s+");
        return (new ArrayList<String>(Arrays.asList(split)));
    }
    
    // split by spaces
    private ArrayList<String> splitByCommaList(String str) {
        String[] split = str.split(",");
        return (new ArrayList<String>(Arrays.asList(split)));
    }    
    
    
}