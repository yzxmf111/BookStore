package com.bookstore.common;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/15 22:26
 */
//这是由泛型实现的高可复用的服务端响应类
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public enum ResponseCode {

    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10, "NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2, "ILLEGAL_ARGUMENT");

    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
