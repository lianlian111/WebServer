package com.gonglian.webserver.core.connector.wrapper.aio;

import com.gonglian.webserver.core.connector.endpoint.aio.AioEndpoint;
import com.gonglian.webserver.core.connector.wrapper.SocketWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Slf4j
@Data
public class AioSocketWrapper extends SocketWrapper {

    private AsynchronousSocketChannel socketChannel;
    private AioEndpoint aioEndpoint;

    public AioSocketWrapper(AsynchronousSocketChannel socketChannel, AioEndpoint aioEndpoint){
        super();
        this.socketChannel = socketChannel;
        this.aioEndpoint = aioEndpoint;
    }

    @Override
    public void close() {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public int read(ByteBuffer byteBuffer, boolean block) throws IOException{

        socketChannel.read(byteBuffer,this , new CompletionHandler<Integer, AioSocketWrapper>() {
            @Override
            public void completed(Integer result, AioSocketWrapper socketWrapper) {
                log.info("read succeed {}", result);
                if(result > 0) {
                    aioEndpoint.execute(socketWrapper);
                }else{
                    socketWrapper.close();
                }
            }

            @Override
            public void failed(Throwable exc, AioSocketWrapper attachment) {
                log.error("read failed");
                exc.printStackTrace();
                close();

            }
        });
        return byteBuffer.position();
    }

    public void flush(){
        writeBuffer.flip();
        if(writeBuffer.hasRemaining()){
            writeBuffer.clear();
            socketChannel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    log.info("write succeed {}, {}", socketChannel,result);
                    attachment.clear();
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    log.info("write failed {}", socketChannel);
                    exc.printStackTrace();
                    close();
                }
            });
        }
    }
}
