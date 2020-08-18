package com.gonglian.webserver.core.request.dispatcher.impl;

import com.gonglian.webserver.core.constnt.CharsetProperties;
import com.gonglian.webserver.core.exception.ResourceNotFoundException;
import com.gonglian.webserver.core.exception.TemplateResolverException;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.request.dispatcher.RequestDispatcher;
import com.gonglian.webserver.core.resource.ResourceHandler;
import com.gonglian.webserver.core.response.Response;
import com.gonglian.webserver.core.template.TemplateResolver;
import com.gonglian.webserver.core.util.IOUtil;
import com.gonglian.webserver.core.util.MimeTypeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;



import java.io.IOException;
import java.nio.ByteBuffer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ApplicationRequestDispatcher implements RequestDispatcher {

    private String url;

    @Override
    public void forward(Request request, Response response) throws ResourceNotFoundException, IOException, TemplateResolverException {
        //
        if(ResourceHandler.class.getResource(url) == null){
            throw new ResourceNotFoundException();
        }
        log.info("forward至 {} 页面", url);
        String body = TemplateResolver.resolver(new String(IOUtil.getBytesFromFile(url), CharsetProperties.UTF_8_CHARSET), request);
        response.setContentType(MimeTypeUtil.getTypes(url));
        response.getHttpResponse().doWrite(ByteBuffer.wrap(body.getBytes(CharsetProperties.UTF_8_CHARSET)));
    }
}
