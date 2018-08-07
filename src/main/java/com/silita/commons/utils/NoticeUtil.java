package com.silita.commons.utils;

import java.util.HashMap;
import java.util.Map;

public class NoticeUtil {

    /**
     * 根据url判断
     * @param urls 全部待抓取urls
     * @param noticeTypes 公告类别
     * @param catchTypes 公告类型
     * @param url 当前url
     * @return
     */
    public static Map<String, String> accordUrlJudgmentType(String[] urls, String[] noticeTypes, String[] catchTypes, String url) {

        if(urls.length != noticeTypes.length || urls.length != catchTypes.length || noticeTypes.length != catchTypes.length) {
            System.out.println("请检查参数是否是否正确！！！");
            return null;
        } else {
            Map resultParams = new HashMap<String, String>();
            for (int i = 0; i < urls.length; i++) {
                if (urls[i].equals(url)) {
                    resultParams.put("catchType", catchTypes[i]);
                    resultParams.put("noticeType", noticeTypes[i]);
                    break;
                }
            }
            return resultParams;
        }
    }
}
