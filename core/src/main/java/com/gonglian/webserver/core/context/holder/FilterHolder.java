package com.gonglian.webserver.core.context.holder;

import com.gonglian.webserver.core.filter.Filter;
import lombok.Data;

@Data
public class FilterHolder {
    private Filter filter;
    private String filterClass;

    public FilterHolder(String filterClass){
        this.filterClass = filterClass;
    }
}
