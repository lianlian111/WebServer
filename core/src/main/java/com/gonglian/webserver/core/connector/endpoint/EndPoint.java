package com.gonglian.webserver.core.connector.endpoint;

import org.springframework.util.StringUtils;

public abstract class EndPoint {
    /**
     * 启动服务器
     * @param port
     */
    public abstract  void start(int port);

    /**
     * 关闭服务器
     */
    public abstract void close();

    /**
     * 根据传入的bio、nio、aio获取相应的EndPoint实例
     * @param connector
     * @return
     */
    public static EndPoint getInstance(String connector){
        StringBuilder sb = new StringBuilder();
        sb.append("com.gonglian.webserver.core.network.endpoint")
                .append(".")
                .append(connector)
                .append(".")
                .append(StringUtils.capitalize(connector))
                .append("Endpoint");
        try {
            return (EndPoint) Class.forName(sb.toString()).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException(connector);
    }
}
