package com.gonglian.webserver.core.request;

import com.gonglian.webserver.core.context.Context;
import com.gonglian.webserver.core.cookie.Cookie;
import com.gonglian.webserver.core.enumeration.RequestMethod;
import com.gonglian.webserver.core.connector.http.HttpRequest;
import com.gonglian.webserver.core.request.dispatcher.RequestDispatcher;
import com.gonglian.webserver.core.request.dispatcher.impl.ApplicationRequestDispatcher;
import com.gonglian.webserver.core.response.Response;
import com.gonglian.webserver.core.session.HttpSession;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

@Slf4j
public class Request {

    private Response response;

    private HttpRequest httpRequest;

    private HttpSession httpSession;

    private Context context;

    public void setResponse(Response response){
        this.response = response;
    }

    public void setHttpRequest(HttpRequest httpRequest){
        this.httpRequest = httpRequest;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public Context getContext(){
        return context;
    }

    public void recycle(){
        if(httpSession != null){
            context.invalidateSession(httpSession);
        }
        httpSession = null;
        context = null;
    }


    public Cookie[] getCookies() {
        return httpRequest.getCookies();
    }

    public HttpSession getSession(boolean create) {
        if(httpSession != null && httpSession.isValid()){
            httpSession.setLastAccessed();
            return httpSession;
        }
        Cookie[] cookies = httpRequest.getCookies();
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("JSESSIONID")){
                HttpSession currentHttpSession = context.getSession(cookie.getValue());
                if(currentHttpSession != null){
                    httpSession = currentHttpSession;
                    return httpSession;
                }
            }
        }
        if(!create){
            return null;
        }
        httpSession = context.createSession(response);
        return httpSession;
    }

    public ApplicationRequestDispatcher getApplicationRequestDispatcher(String url){
        return new ApplicationRequestDispatcher(url);
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public Map<String, String> getParameters(){
        return httpRequest.getParams();
    }


    public String getHeader(String s) {
        return httpRequest.getHeader(s);
    }

    public RequestMethod getRequestMethod(){
        return httpRequest.getMethod();
    }


    public String getMethod() {
        return httpRequest.getMethod().toString();
    }


    public StringBuffer getRequestURL() {
        return new StringBuffer(httpRequest.getUrl());
    }

    public void setRequestURL(String url){
        httpRequest.setUrl(url);
    }


    public Object getAttribute(String s) {
        return httpRequest.getAttribute(s);
    }


    public int getContentLength() {
        String contentLength = httpRequest.getHeader("Content-Length");
        return Integer.valueOf(contentLength);
    }


    public String getContentType() {
        return httpRequest.getHeader("Content-Type");
    }


    public String getRequestURI() {
        return null;
    }


    public String getAuthType() {
        return null;
    }


    public long getDateHeader(String s) {
        return 0;
    }


    public Enumeration<String> getHeaders(String s) {
        return null;
    }


    public Enumeration<String> getHeaderNames() {
        return null;
    }


    public int getIntHeader(String s) {
        return 0;
    }


    public String getPathInfo() {
        return null;
    }


    public String getPathTranslated() {
        return null;
    }


    public String getContextPath() {
        return null;
    }


    public String getQueryString() {
        return null;
    }


    public String getRemoteUser() {
        return null;
    }


    public boolean isUserInRole(String s) {
        return false;
    }


    public Principal getUserPrincipal() {
        return null;
    }


    public String getRequestedSessionId() {
        return null;
    }


    public String getServletPath() {
        return httpRequest.getUrl();
    }


    public String changeSessionId() {
        return null;
    }


    public boolean isRequestedSessionIdValid() {
        return false;
    }


    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }


    public boolean isRequestedSessionIdFromURL() {
        return false;
    }


    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }


    public Enumeration<String> getAttributeNames() {
        return null;
    }


    public String getCharacterEncoding() {
        return null;
    }


    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }


    public long getContentLengthLong() {
        return 0;
    }


    public String getParameter(String s) {
        return null;
    }


    public Enumeration<String> getParameterNames() {
        return null;
    }


    public String[] getParameterValues(String s) {
        return new String[0];
    }


    public Map<String, String[]> getParameterMap() {
        return null;
    }


    public String getProtocol() {
        return null;
    }


    public String getScheme() {
        return null;
    }


    public String getServerName() {
        return null;
    }


    public int getServerPort() {
        return 0;
    }


    public BufferedReader getReader() throws IOException {
        return null;
    }


    public String getRemoteAddr() {
        return null;
    }


    public String getRemoteHost() {
        return null;
    }


    public void setAttribute(String s, Object o) {

    }


    public void removeAttribute(String s) {

    }


    public Locale getLocale() {
        return null;
    }


    public Enumeration<Locale> getLocales() {
        return null;
    }


    public boolean isSecure() {
        return false;
    }


    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }


    public String getRealPath(String s) {
        return null;
    }


    public int getRemotePort() {
        return 0;
    }


    public String getLocalName() {
        return null;
    }


    public String getLocalAddr() {
        return null;
    }


    public int getLocalPort() {
        return 0;
    }
}
