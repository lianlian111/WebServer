package com.gonglian.webserver.core.resource;

import com.gonglian.webserver.core.constnt.CharsetProperties;
import com.gonglian.webserver.core.exception.ResourceNotFoundException;
import com.gonglian.webserver.core.exception.base.ServletException;
import com.gonglian.webserver.core.exception.handler.ExceptionHandler;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;
import com.gonglian.webserver.core.template.TemplateResolver;
import com.gonglian.webserver.core.util.IOUtil;
import com.gonglian.webserver.core.util.MimeTypeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 用于处理静态资源
 */
@Slf4j
public class ResourceHandler {

    private ExceptionHandler exceptionHandler;

    public ResourceHandler(ExceptionHandler exceptionHandler){
        this.exceptionHandler = exceptionHandler;
    }

    public void handle(Request request, Response response){
        String url = request.getRequestURL().toString();
        try{
            if(ResourceHandler.class.getResource(url) == null){
                log.info("找不到资源:{}", url);
                throw new ResourceNotFoundException();
            }
            byte[] body = IOUtil.getBytesFromFile(url);
            if(url.endsWith(".html")){
                body = TemplateResolver
                        .resolver(new String(body, CharsetProperties.UTF_8_CHARSET),request)
                        .getBytes(CharsetProperties.UTF_8_CHARSET);
            }
            response.setContentType(MimeTypeUtil.getTypes(url));
            response.getHttpResponse().doWrite(ByteBuffer.wrap(body));
        }catch (ServletException e){
            exceptionHandler.handle(e, response.getHttpResponse());
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
