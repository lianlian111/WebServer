package com.gonglian.webserver.core.connector.http;

/**
 * 容器对 HttpProcessor 的回调动作
 */
public enum ActionCode {

    /** 请求提交响应头数据到缓冲区 */
    COMMIT,

    /** 请求读取并解析请求参数 */
    PARSE_PARAMS,

    /** 请求写入响应体数据 */
    WRITE_BODY,

    /** 请求读取请求体数据 */
    READ_BODY,

    /** 请求将响应发送到客户端 */
    FLUSH,

    /** 响应处理完毕 */
    CLOSE
}
