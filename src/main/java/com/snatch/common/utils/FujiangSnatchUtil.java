package com.snatch.common.utils;

import org.json.JSONObject;

import java.util.Random;

/**
 * created by gmy
 */
public class FujiangSnatchUtil {
    /**
     * 生成随机数字和字母,
     *
     * @param length 生成字符串长度
     * @return 生成的随机字符串
     */
    public static String getStringRandom(int length) {
        StringBuilder val = new StringBuilder();
        Random random = new Random();
        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
//                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val.append((char) (random.nextInt(26) + 97));
//                val += ;
            } else if ("num".equalsIgnoreCase(charOrNum)) {
//                val += String.valueOf(random.nextInt(10));
                val.append(String.valueOf(random.nextInt(10)));
            }
        }
        return val.toString();
    }


    /**
     * 解析错误json字符串（如福建的）
     *
     * @param text 错误json字符串
     * @return 正确json字符串
     */
    public static String parseErrorJsonStr(String text) {
        try {
            com.alibaba.fastjson.JSONObject.parse(text);
            return text;
        } catch (Exception e) {
            StringBuffer sb = new StringBuffer();
            String subHead = text.substring(0, text.indexOf("[") + 1);
            String subTail = text.substring(text.indexOf("]"), text.length());
            text = text.substring(text.indexOf("[") + 1, text.indexOf("]"));
            sb.append(subHead);

            String[] jsonArr = text.split("},");
            //
            for (int i = 0; i < jsonArr.length; i++) {
                String jsonStr = jsonArr[i];
                if (!jsonStr.contains("}")) {
                    jsonStr = jsonStr + "}";
                }
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonStr);
                } catch (Exception ex) {
                    //解析失敗（不是标准的json字符串）
                    String[] cols = jsonStr.split("\\,");
                    StringBuffer subSb = new StringBuffer();
                    for (int j = 0; j < cols.length; j++) {
                        String key = cols[j].substring(0, cols[j].indexOf(":"));
                        String value = cols[j].substring(cols[j].indexOf(":") + 1, cols[j].length());
                        //is String
                        if (value.indexOf("\"") == 0 && value.lastIndexOf("\"") == value.length() - 1) {
                            value = value.substring(1, value.length() - 1);
                            if (value.contains("“") && value.contains("\"")) {
                                value = value.replaceAll("\\\"", "”");
                            } else if (value.contains("”") && value.contains("\"")) {
                                value = value.replaceAll("\\\"", "“");
                            }
                            value = "\"" + value + "\"";
                        }
                        if (j != cols.length) {
                            subSb.append(key + ":" + value + ",");
                        } else {
                            subSb.append(key + ":" + value);
                        }
                    }
                    jsonObject = new JSONObject(subSb.toString());
                }
//                System.out.println(jsonObject);
                if (i != jsonArr.length - 1) {
                    sb.append(jsonObject.toString() + ",");
                } else {
                    sb.append(jsonObject.toString());
                }
            }
            sb.append(subTail);
            return sb.toString();
        }
    }
}
