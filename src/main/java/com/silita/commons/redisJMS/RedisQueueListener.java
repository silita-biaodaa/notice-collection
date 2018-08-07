package com.silita.commons.redisJMS;

/**
 * Created by liuqi on 2017/1/28 0028.
 */
public interface RedisQueueListener<T> {
    void onMessage(T value);
}