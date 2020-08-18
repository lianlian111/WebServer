package com.gonglian.webserver.core.connector.http;

/**
 *  连接处理过程中 socket 可能的状态
 */
public enum SocketState {

    /** 长连接 */
    OPEN,
    /** 继续读取 */
    LONG,
    /** 发送 */
    WRITE,
    /** 断开连接 */
    CLOSED
}
