package com.gonglian.webserver.core.context;

import com.gonglian.webserver.core.context.holder.FilterHolder;
import com.gonglian.webserver.core.context.holder.ServletHolder;
import com.gonglian.webserver.core.cookie.Cookie;
import com.gonglian.webserver.core.exception.FilterNotFoundException;
import com.gonglian.webserver.core.exception.ServletNotFoundException;
import com.gonglian.webserver.core.filter.Filter;
import com.gonglian.webserver.core.listener.HttpSessionListener;
import com.gonglian.webserver.core.listener.ServletContextListener;
import com.gonglian.webserver.core.listener.ServletRequestListener;
import com.gonglian.webserver.core.listener.event.HttpSessionEvent;
import com.gonglian.webserver.core.listener.event.ServletContextEvent;
import com.gonglian.webserver.core.listener.event.ServletRequestEvent;
import com.gonglian.webserver.core.request.Request;
import com.gonglian.webserver.core.response.Response;
import com.gonglian.webserver.core.servlet.Servlet;
import com.gonglian.webserver.core.session.HttpSession;
import com.gonglian.webserver.core.session.IdleSessionCleaner;
import com.gonglian.webserver.core.util.UUIDUtil;
import com.gonglian.webserver.core.util.XMLUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.util.AntPathMatcher;

import javax.swing.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.gonglian.webserver.core.constnt.ContextConstant.DEFAULT_SERVLET_ALIAS;
import static com.gonglian.webserver.core.constnt.ContextConstant.DEFAULT_SESSION_EXPIRE_TIME;

/**
 * ServletContext 在应用启动时被初始化
 */

@Data
@Slf4j
public class Context {

    //别名->类名
    //一个Servlet类只能有一个Servlet别名，一个Servlet别名只能对应一个Servlet类
     private Map<String, ServletHolder> servlets;


     //一个Servlet可以对应多个URL，一个URL只能对应一个Servlet
     //URL Pattern -> Servlet别名
    private Map<String, String> servletMapping;


    //别名->类名
    private Map<String, FilterHolder> filters;


    //URL Pattern -> 别名列表，注意同一个URLPattern可以对应多个Filter，但只能对应一个Servlet
    private Map<String, List<String>> filterMapping;

    //监听器们
    private List<ServletContextListener> servletContextListeners;
    private List<ServletRequestListener> servletRequestListeners;
    private List<HttpSessionListener> httpSessionListeners;

    //域
    private Map<String, Object> attributes;

    //整个应用对应的session
    private Map<String, HttpSession> sessions;

    //路径匹配 由spring提供
    private AntPathMatcher matcher;

    private IdleSessionCleaner idleSessionCleaner;



    public Context() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        init();
    }

    /**
     * 由URL得到对应的一个Servlet实例
     *
     * @param url
     * @return
     * @throws ServletNotFoundException
     */
    public Servlet mapServlet(String url) throws ServletNotFoundException {
        // 1、精确匹配
        String servletAlias = this.servletMapping.get(url);
        if(servletAlias != null){
            return initAndGetServlet(servletAlias);
        }
        //路径查找(模式匹配)
        List<String> matchingPattern = new ArrayList<>();
        //pattern为指定的url模式
        Set<String> patterns = this.servletMapping.keySet();
        for(String pattern : patterns){
            if(matcher.match(pattern, url)){
                matchingPattern.add(pattern);
            }
        }
        if(!matchingPattern.isEmpty()){
            Comparator<String> patternComparator = matcher.getPatternComparator(url);
            Collections.sort(matchingPattern, patternComparator);
            String bestMath = matchingPattern.get(0);
            return initAndGetServlet(bestMath);
        }
        return initAndGetServlet(DEFAULT_SERVLET_ALIAS);
    }

    /**
     * 初始化并获取Servlet实例，如果已经初始化过则直接返回
     *
     * @param servletAlias
     * @return
     * @throws ServletNotFoundException
     */
    private Servlet initAndGetServlet(String servletAlias) throws ServletNotFoundException {
        ServletHolder servletHolder = servlets.get(servletAlias);
        if(servletHolder == null){
            throw new ServletNotFoundException();
        }
        if(servletHolder.getServlet() == null){
            try {
                Servlet servlet = (Servlet) Class.forName(servletHolder.getServletClass()).newInstance();
                servlet.init();
                servletHolder.setServlet(servlet);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return servletHolder.getServlet();
    }

    /**
     * 由URL得到一系列匹配的Filter实例
     *
     * @param url
     * @return
     */
    public List<Filter> mapFilter(String url) throws FilterNotFoundException {
        List<String> matchingPattern = new ArrayList<>();
        Set<String> urlPatterns = this.filterMapping.keySet();
        for(String urlPattern : urlPatterns){
            if(matcher.match(urlPattern, url)){
                matchingPattern.add(urlPattern);
            }
        }
        Set<String> filterAlias = matchingPattern.stream()
                .flatMap(pattern -> this.filterMapping.get(pattern).stream())
                .collect(Collectors.toSet());
        List<Filter> filters = new ArrayList<>();
        for(String filterAlia : filterAlias){
            filters.add(initAndGetFilter(filterAlia));
        }
        return filters;
    }

    /**
     * 初始化并返回Filter实例，如果已经初始化过则直接返回
     * @param filterAlia
     * @return
     * @throws FilterNotFoundException
     */
    private Filter initAndGetFilter(String filterAlia) throws FilterNotFoundException {
        FilterHolder filterHolder = this.filters.get(filterAlia);
        if(filterHolder == null){
            throw new FilterNotFoundException();
        }
        if(filterHolder.getFilter() == null){
            try {
                Filter filter = (Filter) Class.forName(filterHolder.getFilterClass()).newInstance();
                filter.init();
                filterHolder.setFilter(filter);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return filterHolder.getFilter();
    }

    /**
     * 初始化
     */
    public void init() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.servlets = new HashMap<>();
        this.servletMapping = new HashMap<>();
        this.filters = new HashMap<>();
        this.filterMapping = new HashMap<>();
        this.attributes = new ConcurrentHashMap<>();
        this.sessions = new ConcurrentHashMap<>();
        this.matcher = new AntPathMatcher();
        this.idleSessionCleaner = new IdleSessionCleaner();
        this.idleSessionCleaner.start();
        this.servletContextListeners = new ArrayList<>();
        this.servletRequestListeners = new ArrayList<>();
        this.httpSessionListeners = new ArrayList<>();
        parseConfig();
        ServletContextEvent event = new ServletContextEvent(this);
        for(ServletContextListener listener : servletContextListeners){
            listener.contextInitialized(event);
        }
    }

    /**
     * 应用关闭前被调用
     */
    public void destroy(){
        servlets.values().forEach(servletHolder -> {
            if(servletHolder.getServlet() != null){
                servletHolder.getServlet().destroy();
            }
        });
        filters.values().forEach(filterHolder -> {
            if(filterHolder.getFilter() != null){
                filterHolder.getFilter().destroy();
            }
        });
        ServletContextEvent servletContextEvent = new ServletContextEvent(this);
        for(ServletContextListener listener : servletContextListeners){
            listener.contextDestroyed(servletContextEvent);
        }
    }

    /**
     * web.xml文件解析，比如servlet，filter， listener
     *
     *
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void parseConfig() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Document document = XMLUtil.getDocument(Context.class.getResourceAsStream("/web.xml"));
        Element root = document.getRootElement();
        //解析servlet
        List<Element> servlets = root.elements("servlet");
        for(Element servlet : servlets){
            String key = servlet.element("servlet-name").getText();
            String value = servlet.element("servlet-class").getText();
            this.servlets.put(key, new ServletHolder(value));
        }
        List<Element> servletMapping = root.elements("servlet-mapping");
        for(Element mapping : servletMapping){
            List<Element> urlPatterns = mapping.elements("url-pattern");
            String value = mapping.element("servlet-name").getText();
            for(Element pattern : urlPatterns){
                this.servletMapping.put(pattern.getText(), value);
            }
        }

        //解析filter
        List<Element> filters = root.elements("filter");
        for(Element filter : filters){
            String key = filter.element("filter-name").getText();
            String value = filter.element("filter-class").getText();
            this.filters.put(key, new FilterHolder(value));
        }

        List<Element> filterMapping = root.elements("filter-mapping");
        for(Element mapping : filterMapping){
            List<Element> urlPatterns = mapping.elements("url-pattern");
            String value = mapping.element("filter-name").getText();
            for(Element pattern : urlPatterns){
                List<String> values = this.filterMapping.get(pattern.getText());
                if(values == null){
                    values = new ArrayList<>();
                    this.filterMapping.put(pattern.getText(), values);
                }
                values.add(value);
            }
        }

        //解析listener
        Element listeners = root.element("listener");
        List<Element> listenerEles = listeners.elements("listener-class");
        for(Element listener : listenerEles){
            EventListener eventListener = (EventListener) Class.forName(listener.getText()).newInstance();
            if(eventListener instanceof ServletContextListener){
                servletContextListeners.add((ServletContextListener) eventListener);
            }else if(eventListener instanceof ServletRequestListener){
                servletRequestListeners.add((ServletRequestListener) eventListener);
            }else if(eventListener instanceof HttpSessionListener){
                httpSessionListeners.add((HttpSessionListener) eventListener);
            }
        }
    }

    /**
     * 获取session
     * @param JSESSIONID
     * @return
     */
    public HttpSession getSession(String JSESSIONID){
        HttpSession httpSession = sessions.get(JSESSIONID);
        if(httpSession==null || !httpSession.isValid()){
            return null;
        }
        httpSession.setLastAccessed();
        return httpSession;

    }

    /**
     * 创建session
     * @param response
     * @return
     */
    public HttpSession createSession(Response response){
        HttpSession httpSession = new HttpSession(UUIDUtil.uuid());
        sessions.put(httpSession.getId(), httpSession);
        response.addCookie(new Cookie("JSESSIONID", httpSession.getId()));
        HttpSessionEvent httpSessionEvent = new HttpSessionEvent(httpSession);
        for(HttpSessionListener httpSessionListener : httpSessionListeners){
            httpSessionListener.sessionCreated(httpSessionEvent);
        }
        return httpSession;
    }

    /**
     * 销毁session
     * @param httpSession
     */
    public void invalidateSession(HttpSession httpSession){
        sessions.remove(httpSession.getId());
        afterSessionDestroyed(httpSession);
    }

    /**
     * 清除空闲的session
     * 由于ConcurrentHashMap是线程安全的，所以remove不需要进行加锁
     */
    public void cleanIdleSessions(){
        for(Iterator<Map.Entry<String, HttpSession>> iterator = sessions.entrySet().iterator(); iterator.hasNext();){
            Map.Entry<String, HttpSession> entry = iterator.next();
            if(!entry.getValue().isValid()){
                afterSessionDestroyed(entry.getValue());
                iterator.remove();
            }
        }
    }

    private void afterSessionDestroyed(HttpSession httpSession) {
        HttpSessionEvent httpSessionEvent = new HttpSessionEvent(httpSession);
        for(HttpSessionListener httpSessionListener : httpSessionListeners){
            httpSessionListener.sessionDestroyed(httpSessionEvent);
        }
    }

    public void afterRequestCreated(Request request){
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(request);
        for(ServletRequestListener servletRequestListener : servletRequestListeners){
            servletRequestListener.requestInitialized(servletRequestEvent);
        }
    }

    public void afterRequestDestroyed(Request request){
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(request);
        for(ServletRequestListener servletRequestListener : servletRequestListeners){
            servletRequestListener.requestDestroyed(servletRequestEvent);
        }
    }

    public Object getAttribute(String key){
        return this.attributes.get(key);
    }

    public void setAttribute(String key, Object value){
        this.attributes.put(key, value);
    }
}
