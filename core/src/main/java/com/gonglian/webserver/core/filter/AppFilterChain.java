package com.gonglian.webserver.core.filter;

import com.gonglian.webserver.core.context.Context;
import com.gonglian.webserver.core.exception.FilterNotFoundException;
import com.gonglian.webserver.core.exception.base.ServletException;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppFilterChain implements FilterChain {

    private int pos = 0;
    private int n=0;
    private List<Filter> filters = new ArrayList<>();

    /**
     * 递归执行，自定义filter中如果同意放行，那么会调用filterChain(也就是requestHandler)的doiFilter方法，
     * 此时会执行下一个filter的doFilter方法；
     * 如果不放行，那么会在sendRedirect之后将响应数据写回客户端，结束；
     * 如果所有Filter都执行完毕，那么会调用service方法，执行servlet逻辑
     * @param request
     * @param response
     */
    @Override
    public void doFilter(Request request, Response response) throws ServletException, IOException {
        while(pos < n){
            Filter filter = filters.get(pos++);
            filter.doFilter(request, response, this);
        }
    }

    public static AppFilterChain createFilterChain(Request request) throws FilterNotFoundException {
        Context context = request.getContext();
        AppFilterChain chain = new AppFilterChain();
        List<Filter> filters = context.mapFilter(request.getRequestURL().toString());
        if(filters != null && filters.size() > 0){
            chain.setFilters(filters);
        }
        return chain;
    }

    public void setFilters(List<Filter> filters){
        n = filters.size();
        for(Filter filter : filters){
            this.filters.add(filter);
        }
    }

    public void releaseFilters(){
        pos = 0;
        n = 0;
        filters.clear();
    }
}
