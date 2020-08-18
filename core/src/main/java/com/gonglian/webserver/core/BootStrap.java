package com.gonglian.webserver.core;

import com.gonglian.webserver.core.connector.endpoint.EndPoint;
import com.gonglian.webserver.core.util.PropertyUtil;

import java.util.Scanner;

public class BootStrap {

    /**
     * 服务器启动入口
     * 用户程序与服务器的接口
     */
    public static void run(){
        String port = PropertyUtil.getProperty("server.port");
        if(port == null){
            throw new IllegalArgumentException("server.port 不存在");
        }
        String connector = PropertyUtil.getProperty("server.connector");
        if(connector == null || (!connector.equals("bio") &&!connector.equals("nio") && !connector.equals("aio"))){
            throw new IllegalArgumentException("server.connector不存在或不符合规范");
        }
        EndPoint server = EndPoint.getInstance(connector);
        server.start(Integer.parseInt(port));
        Scanner scanner = new Scanner(System.in);
        String input;
        while(scanner.hasNext()){
            input = scanner.next();
            if(input.equalsIgnoreCase("EXIT")){
                server.close();
                System.exit(0);
            }
        }
    }
}
