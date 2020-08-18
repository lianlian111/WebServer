package com.gonglian.webserver.core.response.output;

import com.gonglian.webserver.core.constnt.CharsetProperties;
import com.gonglian.webserver.core.connector.http.ActionCode;
import com.gonglian.webserver.core.connector.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;


@Slf4j
public class OutputBuffer extends Writer {

    private HttpResponse httpResponse;

    private ByteBuffer bodyBytes;
    private CharBuffer bodyChar;

    //不响应数据，只发送响应头，用于 redirect，默认响应数据
    private boolean suspended = false;
    private boolean isNew = true;

    public OutputBuffer(){
        bodyBytes = ByteBuffer.allocate(1024*8);
        clear(bodyBytes);
        bodyChar = CharBuffer.allocate(1024*8);
        clear(bodyChar);
    }

    public void setHttpResponse(HttpResponse httpResponse){
        this.httpResponse = httpResponse;
    }

    @Override
    public void write(int b) throws IOException {
        writeByte((byte) b);
    }

    public void writeByte(byte c) throws IOException {
        if (isFull(bodyBytes)) {
            flushByteBuffer();
        }
        transfer(c, bodyBytes);
        isNew = false;
    }
    public void writeChar(char c) throws IOException {
        if (isFull(bodyChar)) {
            flushCharBuffer();
        }
        transfer(c, bodyChar);
        isNew = false;
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        if (len <= bodyChar.capacity() - bodyChar.limit()) { // remainning
            transfer(cbuf, off, len, bodyChar);
            return;
        }

        // 发送已有数据
        flushCharBuffer();

        // 发送剩余的字符序列
        realWriteChars(CharBuffer.wrap(cbuf, off, len));
        isNew = false;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (bodyBytes.remaining() == 0) { // 首次写入时等于 0
            appendByteArray(b, off, len);
        } else {
            int n = transfer(b, off, len, bodyBytes);
            len = len - n;
            off = off + n;

            flushByteBuffer();
            if (isFull(bodyBytes)) {
                flushByteBuffer();
                appendByteArray(b, off, len);
            }
        }
        isNew = false;
    }

    private void appendByteArray(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }

        // 发送剩余数据
        int limit = bodyBytes.capacity();
        while (len >= limit) {
            httpResponse.doWrite(ByteBuffer.wrap(b, off, len));
            len = len - limit;
            off = off + limit;
        }
        // 还有剩余数据，写入 bodyBytes 中
        if (len > 0) {
            transfer(b, off, len, bodyBytes);
        }
    }

    /**
     * 将 CharBuffer 转为 ByteBuffer 写入数据
     *
     * @param from 待转的 CharBuffer
     * @throws IOException
     */
    private void realWriteChars(CharBuffer from) throws IOException {

        if (from.hasRemaining()) {
            ByteBuffer bb = CharsetProperties.UTF_8_CHARSET.encode(from);
            if (bb.remaining() <= bodyBytes.remaining()) {
                transfer(bb, bodyBytes);
            } else {
                flushByteBuffer();
                httpResponse.doWrite(bb.slice());
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (suspended) return;

        if (bodyChar.remaining() > 0) {
            flushCharBuffer();
        }
        if (bodyBytes.remaining() > 0) {
            flushByteBuffer();
        }

        httpResponse.action(ActionCode.FLUSH , null);
    }

    @Override
    public void close() throws IOException {
        if (suspended) return;

        if (bodyChar.remaining() > 0) {
            flushCharBuffer();
        }

        if (!httpResponse.isCommitted()) {
            httpResponse.setContentLength(bodyBytes.remaining());
        }
        flush();
        //httpResponse.action(ActionCode.CLOSE, null);
    }

    public void recycle() {
        clear(bodyBytes);
        clear(bodyChar);
        suspended = false;
        isNew = true;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    // Tomcat 为了方便使用 ByteBuffer 定义的方法
    private void flushByteBuffer() throws IOException {
        if (bodyBytes.remaining() > 0) {
            httpResponse.doWrite(bodyBytes.slice());
            clear(bodyBytes); // 切成可写模式
        }
    }

    private void flushCharBuffer() throws IOException {
        if (bodyChar.remaining() > 0) {
            realWriteChars(bodyChar.slice());
            clear(bodyChar); // 切成可写模式
        }
    }

    private void clear(Buffer buffer) {
        buffer.rewind().limit(0);
    }

    private boolean isFull(Buffer buffer) {
        return buffer.limit() == buffer.capacity();
    }

    private void toReadMode(Buffer buffer) {
        buffer.limit(buffer.position())
                .reset();
    }

    private void toWriteMode(Buffer buffer) {
        buffer.mark()
                .position(buffer.limit())
                .limit(buffer.capacity());
    }

    private int transfer(byte[] buf, int off, int len, ByteBuffer to) {
        toWriteMode(to);
        int min = Math.min(len, to.remaining());
        if (min > 0) {
            to.put(buf, off, min);
        }
        toReadMode(to);
        return min; // 返回写入数据长度
    }

    private int transfer(char[] buf, int off, int len, CharBuffer to) {
        toWriteMode(to);
        int min = Math.min(len, to.remaining());
        if (min > 0) {
            to.put(buf, off, min);
        }
        toReadMode(to);
        return min;// 返回写入数据长度
    }
    private void transfer(byte b, ByteBuffer to) {
        toWriteMode(to);
        to.put(b);
        toReadMode(to);
    }
    private void transfer(char b, CharBuffer to) {
        toWriteMode(to);
        to.put(b);
        toReadMode(to);
    }
    private void transfer(ByteBuffer from, ByteBuffer to) {
        toWriteMode(to);
        int min = Math.min(from.remaining(), to.remaining());
        if (min > 0) {
            int fromLimit = from.limit();
            from.limit(from.position() + min);
            to.put(from);
            from.limit(fromLimit);
        }
        toReadMode(to);
    }
}
