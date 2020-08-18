package com.gonglian.webserver.core.connector.handler.aio;

import com.gonglian.webserver.core.context.WebApplication;
import com.gonglian.webserver.core.connector.handler.Adapter;

import com.gonglian.webserver.core.connector.http.HttpProcessor;
import com.gonglian.webserver.core.connector.wrapper.aio.AioSocketWrapper;

public class AioHandler implements Runnable {

    private AioSocketWrapper aioSocketWrapper;
    private HttpProcessor httpProcessor;
    private Adapter adapter;

    public AioHandler(AioSocketWrapper aioSocketWrapper){
        this.aioSocketWrapper = aioSocketWrapper;
        httpProcessor = new HttpProcessor();
        adapter = new Adapter();
        adapter.setContext(WebApplication.getContext());
        httpProcessor.setAdapter(adapter);
    }

    @Override
    public void run() {
        httpProcessor.process(aioSocketWrapper);
        aioSocketWrapper.close();
    }
}
