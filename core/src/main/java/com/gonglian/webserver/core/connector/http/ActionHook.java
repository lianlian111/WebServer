package com.gonglian.webserver.core.connector.http;

/**
 * 容器对HttpProcessor请求操作的回调机制
 *
 */
public interface ActionHook {

    /**
     * 请求 Processor 处理一个动作
     *
     * @param actionCode 动作类型
     * @param param 动作发生时关联的参数
     */
    public void action(ActionCode actionCode, Object... param);

}
