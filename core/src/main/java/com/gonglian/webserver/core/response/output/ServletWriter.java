package com.gonglian.webserver.core.response.output;

import java.io.IOException;
import java.io.PrintWriter;

public class ServletWriter extends PrintWriter {

    private OutputBuffer outputBuffer;
    private boolean error = false;

    public ServletWriter(OutputBuffer outputBuffer) {
        super(outputBuffer);
        this.outputBuffer = outputBuffer;
    }

    @Override
    protected Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    void clear() {
        outputBuffer = null;
    }


    void recycle() {
        error = false;
    }

    @Override
    public void flush() {

        if (error) {
            return;
        }

        try {
            outputBuffer.flush();
        } catch (IOException e) {
            error = true;
        }

    }

    @Override
    public void close() {

        // We don't close the PrintWriter - super() is not called,
        // so the stream can be reused. We close ob.
        try {
            outputBuffer.close();
        } catch (IOException ex ) {
            // Ignore
        }
        error = false;

    }


    @Override
    public boolean checkError() {
        flush();
        return error;
    }


    @Override
    public void write(int c) {

        if (error) {
            return;
        }

        try {
            outputBuffer.write(c);
        } catch (IOException e) {
            error = true;
        }

    }


    @Override
    public void write(char buf[], int off, int len) {

        if (error) {
            return;
        }

        try {
            outputBuffer.write(buf, off, len);
        } catch (IOException e) {
            error = true;
        }

    }


    @Override
    public void write(char buf[]) {
        write(buf, 0, buf.length);
    }


    @Override
    public void write(String s, int off, int len) {

        if (error) {
            return;
        }

        try {
            outputBuffer.write(s, off, len);
        } catch (IOException e) {
            error = true;
        }

    }

    @Override
    public void write(String s) {
        write(s, 0, s.length());
    }

}
