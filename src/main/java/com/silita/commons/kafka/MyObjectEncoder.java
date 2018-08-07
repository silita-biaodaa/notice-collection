package com.silita.commons.kafka;

import com.silita.commons.utils.BeanUtils;
import kafka.utils.VerifiableProperties;


public class MyObjectEncoder implements kafka.serializer.Encoder<Object>{
    public MyObjectEncoder(VerifiableProperties verifiableProperties){
    }

    @Override
    public byte[] toBytes(Object map) {
        return BeanUtils.ObjectToBytes(map);
    }
}
