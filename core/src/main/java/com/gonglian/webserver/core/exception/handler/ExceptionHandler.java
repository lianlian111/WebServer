package com.gonglian.webserver.core.exception.handler;

import com.gonglian.webserver.core.exception.RequestInvalidException;
import com.gonglian.webserver.core.exception.base.ServletException;
import com.gonglian.webserver.core.connector.http.HttpResponse;
import com.gonglian.webserver.core.util.IOUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.gonglian.webserver.core.constnt.ContextConstant.ERROR_PAGE;

/**
 * 异常处理器
 * 会根据对应的 Http Status设置response的状态以及相应的错误页面
 */
@Slf4j
public class ExceptionHandler {

    public void handle(ServletException e, HttpResponse httpResponse){
        try {
            if(e instanceof RequestInvalidException){
                log.info("请求无法读取，丢弃");
            }else{
                log.info("抛出异常:{}", e.getClass().getName());
                e.printStackTrace();
                httpResponse.addHeader("Connection", "close");
                httpResponse.setStatus(e.getHttpStatus().getCode());
                byte[] errorPage = IOUtil.getBytesFromFile(
                        String.format(ERROR_PAGE, e.getHttpStatus().getCode()));
                httpResponse.doWrite(ByteBuffer.wrap(errorPage));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
