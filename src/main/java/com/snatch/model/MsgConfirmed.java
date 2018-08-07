package com.snatch.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/6.
 */
public class MsgConfirmed implements Serializable {
    public final long deliveryId;

    public MsgConfirmed(long deliveryId) {
        this.deliveryId = deliveryId;
    }
}