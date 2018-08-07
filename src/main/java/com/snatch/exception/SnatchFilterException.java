package com.snatch.exception;

/**
 * Created by dh on 2017/12/14.
 */
public class SnatchFilterException extends Exception{
    public SnatchFilterException(String message) {
        super("==抓取过滤异常=="+message);
    }
}
