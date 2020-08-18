package com.gonglian.webserver.core.response.output;

import java.io.IOException;


public class ServletOutputStream {

    private OutputBuffer outputBuffer;

    public ServletOutputStream(OutputBuffer outputBuffer){
        this.outputBuffer = outputBuffer;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    void clear() {
        outputBuffer = null;
    }

    public boolean isReady() {
        return false;
    }


    public void write(int b) throws IOException {
        outputBuffer.write(b);
    }


    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }



    public void write(byte[] b, int off, int len) throws IOException {
        outputBuffer.write(b, off, len);
    }


    public void flush() throws IOException {
        outputBuffer.flush();
    }
}
