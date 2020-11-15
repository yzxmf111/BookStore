package com.bookstore.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by Tian
 * PropertiesUtil工具类的作用在于提高代码的灵活性,降低耦合性.在我们改变一些配置的时候不需要修改源代码,只需要修改配置文件
 */
public class PropertiesUtil {

    //让日志输出,而不是sout输出,降低我们应用的损耗
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    //
    private static Properties props;

    static {
        String fileName = "bookstore.properties";
        props = new Properties();
        try {
            //PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName).返回用于读取指定资源的输入流。
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            logger.error("配置文件读取异常",e);
        }
    }
    //方法的重载形式
    public static String getProperty(String key){
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }

    public static String getProperty(String key, String defaultValue){

        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            value = defaultValue;
        }
        return value.trim();
    }



}
