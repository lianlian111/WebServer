package com.gonglian.webserver.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class IOUtil {

    public static byte[] getBytesFromFile(String fileName) throws IOException {
        InputStream in = IOUtil.class.getResourceAsStream(fileName);
        if(in == null){
            log.info("Not Found File:{}", fileName);
            throw new FileNotFoundException();
        }
        log.info("正在读取文件：{}", fileName);
        return getBytesFromStream(in);
    }

    private static byte[] getBytesFromStream(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = in.read(buffer)) > 0){
            baos.write(buffer, 0, len);
        }
        baos.close();
        in.close();
        return baos.toByteArray();
    }
}
