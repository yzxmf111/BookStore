package com.bookstore.common;

import java.io.Serializable;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/15 21:37
 */

//这是由泛型实现的高可复用的服务端响应类
public class ServerResponse<T>  implements Serializable {

    private int status;
    private String message;
    private  T data;

    private ServerResponse(int status){
        this.status=status;
    }

    private ServerResponse(int status, String message){
        this.status=status;
        this.message=message;
    }
    private ServerResponse(int status, T data){
        this.status=status;
        this.data=data;
    }

    private ServerResponse(int status, String message, T data){
        this.status=status;
        this.message=message;
        this.data=data;
    }

    //可以作为if判断,并且很实用
    public  boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }
    //上边的这四个私有的构造方法,会出现一定的问题,如果传入的是一个int类型,一个String,但是我们想让string放入data里边
    //这就需要以下的方法
    public  static <T> ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public  static <T> ServerResponse<T> createBySuccessMessage(String message){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),message);
    }

    public  static <T> ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }

    public  static <T> ServerResponse<T> createBySuccess(String message, T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),message,data);
    }

    public  static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    public  static <T> ServerResponse<T> createByErrorMessage(String errorMessage){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }
    //对错误进行了扩展
    public  static <T> ServerResponse<T> createByErrorCodeMessage(int errorcode , String errorMessage){
        return new ServerResponse<T>(errorcode,errorMessage);
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }


}
