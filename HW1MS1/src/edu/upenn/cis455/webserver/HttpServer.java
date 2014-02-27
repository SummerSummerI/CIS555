package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


/**
 * Multi-thread Http Server
 * ab -n 10000 -c 1000 -t 20 http://example.com/
 * @version
 * @author cis455
 * 
 */
class HttpServer {
    public static ServerSocket socket1 = null;
    private static boolean runFlag = true;
    public static int port = 8082;
    private static String rootDirectory = "/";
    public static void main(String args[]) {
        if(args.length == 1) {
            System.out.println("Error");
            port = Integer.valueOf(args[0]);
        }
        else if(args.length == 2) {
            rootDirectory = args[1];
            port = Integer.valueOf(args[0]);
            ThreadWorker.setRootDirectory(rootDirectory);
        }
        System.out.println("Hangfei Lin's Server. PennKey: hangfei");
        System.out.println("Port: " + port + "\nRoot Directory: " + rootDirectory + "\n");
        ThreadPool threadPool1 = new ThreadPool(10, 100000);

        try {
            // adding buffer
            socket1 = new ServerSocket(port, 20000);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (runFlag) {
            Socket x = null;
            try {
                x = socket1.accept();

                threadPool1.handleSocket(x);
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                System.out.println("Socket closed!");
                //break;
            } catch (IOException e) {
                e.printStackTrace();
                // ?
                //break;
            }
            finally {
                //System.out.println("yes");
                if(!runFlag) {
                    break;
                }
            }

        } 
        System.out.println("Server Closed.");
    }

    public static void closeSocket() {
        try {
            runFlag = false;
            socket1.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
