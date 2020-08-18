package com.gonglian.webserver.core.request.dispatcher;

import com.gonglian.webserver.core.exception.ResourceNotFoundException;
import com.gonglian.webserver.core.exception.TemplateResolverException;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;

import java.io.IOException;

public interface RequestDispatcher {
    void forward(Request request, Response response) throws ResourceNotFoundException, IOException, TemplateResolverException;
}
