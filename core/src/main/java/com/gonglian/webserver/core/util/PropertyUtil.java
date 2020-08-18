package com.gonglian.webserver.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertyUtil {

    private static Properties props;

    private synchronized static void loadProps(){
        log.info("开始加载properties的内容...");
        props = new Properties();
        InputStream in = null;
        try {
            in = PropertyUtil.class.getClassLoader().getResourceAsStream("server.properties");
            props.load(in);
        } catch (IOException e) {
            log.info("出现IOException");
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error("rpc.properties文件流关闭出现异常");
            }
        }
        log.info("加载properties文件内容完成.....");
        log.info("properties文件内容:{}", props);
    }

    public static String getProperty(String key){
        if(props == null){
            loadProps();
        }
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue){
        if(props == null){
            loadProps();
        }
        return props.getProperty(key, defaultValue);
    }
}
