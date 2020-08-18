package com.gonglian.webserver.core.connector.acceptor.bio;


import com.gonglian.webserver.core.connector.endpoint.bio.BioEndpoint;
import com.gonglian.webserver.core.connector.wrapper.bio.BioSocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;


@Slf4j
public class BioAcceptor implements Runnable {

    private BioEndpoint bioEndpoint;

    public BioAcceptor(BioEndpoint bioEndpoint){
        this.bioEndpoint = bioEndpoint;
    }

    @Override
    public void run() {
        while(bioEndpoint.isRunning()){
            try {
                Socket client = bioEndpoint.accept();
                log.info("Acceptor接收到客户端连接：{}", client);
                BioSocketWrapper bioSocketWrapper = new BioSocketWrapper(client);
                int n = bioSocketWrapper.read(bioSocketWrapper.getReadBuffer(), false);
                if(n>0){
                    bioEndpoint.execute(bioSocketWrapper);
                }else{
                    bioSocketWrapper.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
