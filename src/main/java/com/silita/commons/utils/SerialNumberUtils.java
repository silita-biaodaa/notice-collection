package com.silita.commons.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by jianlan on 2017/4/6.
 */
public class SerialNumberUtils {
    @Autowired
    public static RedisTemplate<String, Integer> redisTemplate;

    /**
     * 获取前缀，当前时间戳
     *
     * @return
     */
    public static String getPrefix() {
//        String str = new DateTime(new Date()).toString("yyyyMMdd");
        String str = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return str;
    }

    /**
     * 获取后缀（redis队列中取）
     *
     * @param redisTemplate
     * @return
     */
    public static String getRedisSuffix(RedisTemplate<String, Integer> redisTemplate, String serialNumType) {
        int num = RedisSerialNum.getSerialNum(redisTemplate, serialNumType);
        return String.valueOf(num);
    }

    /**
     * 20170122-009
     *
     * @param redisTemplate
     * @return
     */
    public static String getRedisSerialNumber(RedisTemplate<String, Integer> redisTemplate, String serialNumType) {
        String pre = getPrefix();
        String suf = getRedisSuffix(redisTemplate, serialNumType);
        suf = getPreZeroNumber3(3, suf);
        return pre + "-" + suf;
    }

    /**
     * 数字补0(效率慢)
     *
     * @param num 需要补充到num位
     * @param str 待补充0的字符串(数字转化而来)
     * @return
     */
    public static String getPreZeroNumber(int num, String str) {
        int strLen = str.length();
        if (strLen < num) {
            while (strLen < num) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);//左补0
                // sb.append(str).append("0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /**
     * 数字补0（效率高）
     *
     * @param num 需要补充到num位
     * @param str 待补充0的字符串(数字转化而来)
     * @return
     */
    public static String getPreZeroNumber2(int num, String str) {
        int strLen = str.length();
        if (str.length() < num) {
            for (int i = strLen; i < num; i++) {
                str = "0" + str;
            }
        }
        return str;
    }

    /**
     * 数字补0(小数据效率最高,大数据最低)
     *
     * @param num 需要补充到num位
     * @param str 待补充0的字符串(数字转化而来)
     * @return
     */
    public static String getPreZeroNumber3(int num, String str) {
        int strLen = str.length();
        if (strLen < num) {
            for (int i = strLen; i < num; i++) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);
                str = sb.toString();
            }
        }
        return str;
    }

    /**
     * BLSW20170122-009
     *
     * @param pre
     * @param redisTemplate
     * @return
     */
    public static String getRedisSerialNumberAddPre(RedisTemplate<String, Integer> redisTemplate, String serialNumType, String pre) {
        return pre + getRedisSerialNumber(redisTemplate, serialNumType);
    }


    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        RedisTemplate<String, Integer> redisTemplate = (RedisTemplate<String, Integer>) applicationContext.getBean("redisTemplate");
        System.out.println(getPrefix());
        System.out.println(getRedisSerialNumberAddPre(redisTemplate, SerialType.SERIALNUM.getValue(), "TEST"));
    }
}
