package com.snatch.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 91567 on 2017/11/21.
 */
public class zzUtil {

    public static boolean exisCert(String content){
        String[] regs = {
                "(颁发的).*?(资质)",
                "(核发的).*?(资质)",
                "(具备).*?(资质)",
                "(持有).*?(资质)",
                "(具有).*?(资质)"
        };
        String zzCont = "";
        // 去除html标签
        content = content.replaceAll("\\s*",""); // 去除空格
        String regEx_html="<.+?>"; // HTML标签的正则表达式
        Pattern pattern = Pattern.compile(regEx_html);
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceAll("");
        content = content.replaceAll("&nbsp;","");
        for (int i = 0; i < regs.length; i++) {
            Pattern pa = Pattern.compile(regs[i]);
            Matcher ma = pa.matcher(content);
            while(ma.find()){
                zzCont = ma.group();
                int firstIndex = regs[i].substring(regs[i].indexOf("(")+1,regs[i].indexOf(")")).length();
                zzCont = zzCont.substring(firstIndex,zzCont.length()-2);
                if(SnatchUtils.isNotNull(zzCont) && zzCont.contains("级")){
                    return true;
                }
            }
        }
        return false;
    }

}
