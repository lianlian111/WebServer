package com.gonglian.webserver.core.template;

import com.gonglian.webserver.core.enumeration.ModelScope;
import com.gonglian.webserver.core.exception.TemplateResolverException;
import com.gonglian.webserver.core.request.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简化版的模板引擎，基于正则表达式的替换
 * 比如${a.b.c} 就可以解析为a.getB().getC()，并将值填充至占位符
 */
@Slf4j
public class TemplateResolver {

    //由于Pattern的构造函数是私有的,不可以直接创建,所以通过静态方法compile(String regex)方法来创建,
    // 将给定的正则表达式编译并赋予给Pattern类。
    public static final Pattern regex = Pattern.compile("\\$\\{(.*?)}"); //${a.b.c}

    public static String resolver(String content, Request request) throws TemplateResolverException {

        // 对指定输入的字符串创建一个Matcher对象。Matcher对象一般通过这个方法生成
        Matcher matcher = regex.matcher(content);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            log.info("{}", matcher.group(1));
            // placeHolder 格式为scope.x.y.z
            // scope值为requestScope,sessionScope,applicationScope
            String placeHolder = matcher.group(1);
            if(placeHolder.indexOf(".") == -1){
                throw new TemplateResolverException();
            }
            ModelScope scope = ModelScope
                    .valueOf(placeHolder.substring(0,placeHolder.indexOf("."))
                    .replace("Scope", "").toUpperCase());
            // key 格式为x.y.z
            if(scope == null){
                throw new TemplateResolverException();
            }
            String key = placeHolder.substring(placeHolder.indexOf(".")+1);
            Object value = null;
            // 按照.分隔为数组,格式为[x,y,z]
            String[] segments = key.split(".");
            log.info("key: {}, segments:{}",key, Arrays.toString(segments) );
            switch (scope){
                case REQUEST:
                    value = request.getAttribute(segments[0]);
                    break;
                case SESSION:
                    value = request.getSession().getAttribute(segments[0]);
                    break;
                case APPLICATION:
                    value = request.getContext().getAttribute(segments[0]);
                    break;
                default:
                    break;
            }
            // 此时value为x，如果没有y、z，那么会直接返回；如果有，就会递归地进行属性读取（基于反射）
            if(segments.length>1){
                try {
                    value = parse(value, segments, 1);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    throw new TemplateResolverException();
                }
            }
            log.info("value:{}", value);
            // 如果解析得到的值为null，则将占位符去掉；否则将占位符替换为值
            if(value == null){
                //将输入字符序列首次与正在表达式匹配的部分进行更改为replacement
                // 并且把结果添加到一个sb结果集中，对于匹配之前的字符序列，它们被转移到sb字符集中。
                matcher.appendReplacement(sb, "");
            }else{
                //把group(1)得到的数据，替换为value
                matcher.appendReplacement(sb, value.toString());
            }
        }
        // 将源文件后续部分添加至尾部
        matcher.appendTail(sb);
        String result = sb.toString();
        return result.length() == 0 ? content : result;
    }

    /**
     * 基于反射实现多级查询，比如user.dept.name
     *
     * @param segments
     * @return
     */
    private static Object parse(Object value, String[] segments, int index) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(index == segments.length){
            return value;
        }
        Method method = value.getClass().getMethod("get"+ StringUtils.capitalize(segments[index]),new Class[0]);
        return parse(method.invoke(value, new Object[0]), segments, index+1);
    }
}
