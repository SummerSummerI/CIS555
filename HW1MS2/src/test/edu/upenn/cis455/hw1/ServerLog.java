/**
 * 
 */
package test.edu.upenn.cis455.hw1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Server log Constructor: fileName
 * 
 * @author cis455
 * 
 */
public class ServerLog {
    String serverLogName;
    File serverLogFile;
    BufferedWriter writer;
    private static ServerLog _instance;

    private ServerLog(String fileName) {
        serverLogName = fileName;
        serverLogFile = new File(fileName);
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(serverLogFile), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static ServerLog getInstance(String fileName) {
        if (_instance == null) {
            _instance = new ServerLog(fileName);
        }
        return _instance;
    }

    public void writeFile(String log) {
        try {
            writer.write(log);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void flushWriter() {
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the log file
     */
    public void closeLogFile() {
        try {
            writer.close();
        } catch (IOException e) {

        }
    }

    /**
     * Write exception or error log to local file on the disk
     * 
     * @param string
     */
    public void getWriterString(String string) {
        writer.toString();
    }
}
