package com.gonglian.webserver.core.connector.handler.bio;

import com.gonglian.webserver.core.context.WebApplication;
import com.gonglian.webserver.core.connector.handler.Adapter;
import com.gonglian.webserver.core.connector.http.HttpProcessor;
import com.gonglian.webserver.core.connector.wrapper.bio.BioSocketWrapper;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class BioHandler implements Runnable {

    private BioSocketWrapper bioSocketWrapper;
    private HttpProcessor httpProcessor;
    private Adapter adapter;

    public BioHandler(BioSocketWrapper bioSocketWrapper){
        this.bioSocketWrapper = bioSocketWrapper;
        httpProcessor = new HttpProcessor();
        adapter = new Adapter();
        adapter.setContext(WebApplication.getContext());
        httpProcessor.setAdapter(adapter);
    }

    @Override
    public void run() {
        httpProcessor.process(bioSocketWrapper);
        bioSocketWrapper.close();
    }
}
