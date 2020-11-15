package com.bookstore.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class TokenCache {


    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";

    //LRU算法-----guava里边的本地缓存,设置本地缓存的初始化容量是1000,并设置缓存的最大容量,如果唱过了这个容量,guawa会使用LRU算法(最少使用算法),来移除缓存项
    //expireAfterAccess(12, TimeUnit.HOURS) 缓存的有效期是12小时
    private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //匿名内部类?复习下?lamada表达式简单回顾下
                //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
                @Override
                public String load(String s) throws Exception {
                    //这里不要直接返回null,防止空指针异常
                    return "null";
                }
            });

    public static void setKey(String key, String value){
        localCache.put(key,value);
    }

    public static String getKey(String key){
        String value = null;
        try {
            value = localCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error",e);
        }
        return null;
    }
}
