package edu.upenn.cis455.webserver;

import java.io.PrintWriter;

/**
 * PrintWriter to buffer
 * Check response.isCommitted before print
 * @author cis455
 *
 */

public class MyPrintWriter extends PrintWriter {
    public MyResponseBuffer buf;
    public MyHttpServletResponse res;
    
    public MyPrintWriter(MyResponseBuffer buf,  MyHttpServletResponse res) {
        super(System.out, true);
        this.buf = buf;
        this.res = res;
    }
    
    public void print(String str) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(str);
        }
    }
    
    public void print(boolean b) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(b);
        }
    }
    
    public void print(char c) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(c);
        }
    }
    
    public void print(int i) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(i);
        }
    }
    
    public void print(long l) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(l);
        }
    }
    
    public void print(float f) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(f);
        }
    }
    
    public void print(double d) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(d);
        }
    }
    
    public void print(char[] s) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(s);
        }
    }
    
    public void println(String str) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(str);
            buf.append("\n");
        }
    }
    
    public void println(boolean b) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(b);
            buf.append("\n");
        }
    }
    
    public void println(char c) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(c);
            buf.append("\n");
        }
    }
    
    public void println(int i) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(i);
            buf.append("\n");
        }
    }
    
    public void println(long l) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(l);
            buf.append("\n");
        }
    }
    
    public void println(float f) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(f);
            buf.append("\n");
        }
    }
    
    public void println(double d) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(d);
            buf.append("\n");
        }
    }
    
    public void println(char[] s) {
        if(res.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            buf.append(s);
            buf.append("\n");
        }
    }    
}