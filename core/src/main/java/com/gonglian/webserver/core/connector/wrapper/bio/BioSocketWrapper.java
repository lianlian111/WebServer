package com.gonglian.webserver.core.connector.wrapper.bio;

import com.gonglian.webserver.core.connector.wrapper.SocketWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

@Getter
@Slf4j
public class BioSocketWrapper extends SocketWrapper {

    private Socket socket;

    public BioSocketWrapper(Socket socket){
        super();
        this.socket = socket;
    }


    public void close(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int read(ByteBuffer byteBuffer, boolean block) throws IOException {
        BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
        byte[] buffer = new byte[input.available()];
        int len = input.read(buffer);
        if(len < 0){
            throw new IOException();
        }
        byteBuffer.put(buffer);
        return len;
    }

    public void flush() throws IOException {
        OutputStream output = socket.getOutputStream();
        writeBuffer.flip();
        if(writeBuffer.hasRemaining()){
            byte[] bytes = writeBuffer.array();
            output.write(bytes);
            output.flush();
            writeBuffer.clear();
            log.info("响应数据输出成功");
        }
    }
}
