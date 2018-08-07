package com.snatch.common.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 大写中文转数字or数字转大写中文
 * @author Administrator
 *
 */
public class CNNumberFormat {
	static String CHN_NUMBER[] = {"零", "壹", "贰", "叁", "肆", "伍","陆", "柒", "捌", "玖"};
    static String CHN_UNIT[] = {"", "拾", "佰", "仟"};          //权位  
    static String CHN_UNIT_SECTION[] = {"", "万", "亿", "万亿"}; //节权位  

    private static long result = 0;
    // HashMap
    private static Map<String, Long> unitMap = new java.util.HashMap<String, Long>();
    private static Map<String, Long> numMap = new java.util.HashMap<String, Long>();
    // 字符串分离
    private static String stryi = new String();
    private static String stryiwan = new String();
    private static String stryione = new String();
    private static String strwan = new String();
    private static String strone = new String();

    /** 
     * 测试数据的数据类型 
     */  
    public static class Test_Data{  
        int number;  
        String chnNum;  
        public Test_Data(int number,String chnNum){  
            this.chnNum=chnNum;  
            this.number=number;  
        }  
    }  
  
    /** 
     * 阿拉伯数字转换为中文数字的核心算法实现。 
     * @param num为需要转换为中文数字的阿拉伯数字，是无符号的整形数 
     * @return 
     */  
    public static String NumberToChn(int num) {  
        StringBuffer returnStr = new StringBuffer();  
        Boolean needZero = false;  
        int pos=0;           //节权位的位置  
        if(num==0){  
            //如果num为0，进行特殊处理。  
            returnStr.insert(0,CHN_NUMBER[0]);  
        }  
        while (num > 0) {  
            int section = num % 10000;  
            if (needZero) {  
                returnStr.insert(0, CHN_NUMBER[0]);  
            }  
            String sectionToChn = SectionNumToChn(section);  
            //判断是否需要节权位  
            sectionToChn += (section != 0) ? CHN_UNIT_SECTION[pos] : CHN_UNIT_SECTION[0];  
            returnStr.insert(0, sectionToChn);  
            needZero = ((section < 1000 && section > 0) ? true : false); //判断section中的千位上是不是为零，若为零应该添加一个零。  
            pos++;  
            num = num / 10000;  
        }  
        return returnStr.toString();  
    }  
  
    /** 
     * 将四位的section转换为中文数字 
     * @param section 
     * @return 
     */  
    public static String SectionNumToChn(int section) {  
        StringBuffer returnStr = new StringBuffer();  
        int unitPos = 0;       //节权位的位置编号，0-3依次为个十百千;  
  
        Boolean zero = true;  
        while (section > 0) {  
  
            int v = (section % 10);  
            if (v == 0) {  
                if ((section == 0) || !zero) {  
                    zero = true; /*需要补0，zero的作用是确保对连续的多个0，只补一个中文零*/  
                    //chnStr.insert(0, chnNumChar[v]);  
                    returnStr.insert(0, CHN_NUMBER[v]);  
                }  
            } else {  
                zero = false; //至少有一个数字不是0  
                StringBuffer tempStr = new StringBuffer(CHN_NUMBER[v]);//数字v所对应的中文数字  
                tempStr.append(CHN_UNIT[unitPos]);  //数字v所对应的中文权位  
                returnStr.insert(0, tempStr);  
            }  
            unitPos++; //移位  
            section = section / 10;  
        }  
        return returnStr.toString();  
    }  
  
    /** 
     * 中文转换成阿拉伯数字，中文字符串除了包括0-9的中文汉字，还包括十，百，千，万等权位。 
     * 此处是完成对这些权位的类型定义。 
     * name是指这些权位的汉字字符串。 
     * value是指权位多对应的数值的大小。诸如：十对应的值的大小为10，百对应为100等 
     * secUnit若为true，代表该权位为节权位，即万，亿，万亿等 
     */  
    public static class Chn_Name_value{  
        String name;  
        int value;  
        Boolean secUnit;  
        public Chn_Name_value(String name,int value,Boolean secUnit){  
            this.name=name;  
            this.value=value;  
            this.secUnit=secUnit;  
        }  
    }  
  
    static Chn_Name_value chnNameValue[]={  
            new Chn_Name_value("拾",10,false),  
            new Chn_Name_value("佰",100,false),  
            new Chn_Name_value("仟",1000,false),  
            new Chn_Name_value("万",10000,true),  
            new Chn_Name_value("亿",100000000,true)  
    };  
  
    /** 
     * 返回中文数字汉字所对应的阿拉伯数字，若str不为中文数字，则返回-1 
     * @param str 
     * @return 
     */  
    public static int ChnNumToValue(String str){  
        for(int i=0;i<CHN_NUMBER.length;i++){  
            if(str.equals(CHN_NUMBER[i])){  
                return i;  
            }  
        }  
        return -1;  
    }  
  
    /** 
     * 返回中文汉字权位在chnNameValue数组中所对应的索引号，若不为中文汉字权位，则返回-1 
     * @param str 
     * @return 
     */  
    public static int ChnUnitToValue(String str){  
        for(int i=0;i<chnNameValue.length;i++){  
            if(str.equals(chnNameValue[i].name)){  
                return i;  
            }  
        }  
        return -1;  
    }  
  
    /** 
     * 返回中文数字字符串所对应的int类型的阿拉伯数字 
     * @param str 
     * @return 
     */  
    public static int ChnStringToNumber(String str){  
    	String strNumber = "";
        int returnNumber=0;  
        double section=0;  
        int pos=0;  
        int number=0;  
        while (pos<str.length()){  
        	if(isWholeNumber(str.substring(pos,pos+1))){
        		strNumber+=str.substring(pos,pos+1);
        		 pos++;  
        	}else{
        		if(!"".equals(strNumber)){
        			section+=Double.parseDouble(strNumber);
        		}
        		int num=ChnNumToValue(str.substring(pos,pos+1));  
        		//若num>=0，代表该位置（pos），所对应的是数字不是权位。若小于0，则表示为权位  
        		if(num>=0){  
        			number=num;  
        			pos++;  
        			//pos是最好一位，直接将number加入到section中。  
        			if(pos>=str.length()){  
        				section+=number;  
        				returnNumber+=section;  
        				break;  
        			}  
        		}else{  
        			int chnNameValueIndex=ChnUnitToValue(str.substring(pos,pos+1));  
        			//chnNameValue[chnNameValueIndex].secUnit==true，表示该位置所对应的权位是节权位，
        			if(chnNameValue[chnNameValueIndex].secUnit){  
        				section=(section+number)*chnNameValue[chnNameValueIndex].value;  
        				returnNumber+=section;  
        				section=0;  
        			}else{  
        				section+=number*chnNameValue[chnNameValueIndex].value;  
        			}  
        			pos++;  
        			number=0;  
        			if(pos>=str.length()){  
        				returnNumber+=section;
        				
        				break;  
        			}  
        		}  
        	}  
        }
        return returnNumber;  
    }


    public static void ChangeChnString(String chnStr) {
        unitMap.put("拾", 10L);
        unitMap.put("佰", 100L);
        unitMap.put("仟", 1000L);
        unitMap.put("万", 10000L);
        unitMap.put("亿", 100000000L);
        // num
        numMap.put("零", 0L);
        numMap.put("壹", 1L);
        numMap.put("贰", 2L);
        numMap.put("叁", 3L);
        numMap.put("肆", 4L);
        numMap.put("伍", 5L);
        numMap.put("陆", 6L);
        numMap.put("柒", 7L);
        numMap.put("捌", 8L);
        numMap.put("玖", 9L);
        // 去零
        for (int i = 0; i < chnStr.length(); i++) {
            if ('零' == (chnStr.charAt(i))) {
                chnStr = chnStr.substring(0, i) + chnStr.substring(i + 1);
            }
        }
        // 分切成三部分
        int index = 0;
        boolean yiflag = true;
        boolean yiwanflag = true; //亿字节中存在万
        boolean wanflag = true;
        for (int i = 0; i < chnStr.length(); i++) {
            if ('亿' == (chnStr.charAt(i))) {
                // 存在亿前面也有小节的情况
                stryi = chnStr.substring(0, i);
                if (chnStr.indexOf('亿' + "") > chnStr.indexOf('万' + "")) {
                    stryi = chnStr.substring(0, i);
                    for (int j = 0; j < stryi.length(); j++) {
                        if ('万' == (stryi.charAt(j))) {
                            yiwanflag = false;
                            stryiwan = stryi.substring(0, j);
                            stryione = stryi.substring(j + 1);
                        }
                    }
                }
                if(yiwanflag){//亿字节中没有万，直接赋值
                    stryione = stryi;
                }
                index = i + 1;
                yiflag = false;// 分节完毕
                strone = chnStr.substring(i + 1);

            }
            if ('万' == (chnStr.charAt(i)) && chnStr.indexOf('亿' + "") < chnStr.indexOf('万' + "")) {
                strwan = chnStr.substring(index, i);
                strone = chnStr.substring(i + 1);
                wanflag = false;// 分节完毕
            }
        }
        if (yiflag && wanflag) {// 没有处理
            strone = chnStr;
        }
    }
    // 字符串转换为数字
    public static long chnStrToNum(String str) {
        long strreuslt = 0;
        long value1 = 0;
        long value2 = 0;
        long value3 = 0;
        if (str.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < str.length(); i++) {
            char bit = str.charAt(i);
            // 数字
            if (numMap.containsKey(bit + "")) {
                value1 = numMap.get(bit + "");
                if (i == str.length() - 1) {
                    strreuslt += value1;
                }

            }
            // 单位
            else if (unitMap.containsKey(bit + "")) {
                value2 = unitMap.get(bit + "");
                if (value1 == 0 && value2 == 10L) {
                    value3 = 1 * value2;
                } else {
                    value3 = value1 * value2;
                    // 清零避免重复读取
                    value1 = 0;
                }
                strreuslt += value3;
            }
        }
        return strreuslt;
    }
    // 组合数字
    public static long ChnStringToNumber2(String chnStr) {
        if(chnStr.contains("万")) {
            chnStr = chnStr.substring(0,chnStr.indexOf("万")+1);
        }
        ChangeChnString(chnStr);
        long stryiwanresult = chnStrToNum(stryiwan);
        long stryioneresult = chnStrToNum(stryione);
        long strwanresult = chnStrToNum(strwan);
        long stroneresult = chnStrToNum(strone);
        result = (stryiwanresult + stryioneresult) * 100000000 + strwanresult * 10000 + stroneresult;
        // 重置
        stryi = "";
        strwan = "";
        strone = "";
        return result;
    }

    
    private static boolean isMatch(String regex, String orginal){  
        if (orginal == null || orginal.trim().equals("")) {  
            return false;  
        }  
        Pattern pattern = Pattern.compile(regex);  
        Matcher isNum = pattern.matcher(orginal);  
        return isNum.matches();  
    }  
  
    public static boolean isPositiveInteger(String orginal) {  
        return isMatch("^\\+{0,1}[1-9]\\d*", orginal);  
    }  
  
    public static boolean isNegativeInteger(String orginal) {  
        return isMatch("^-[1-9]\\d*", orginal);  
    }  
  
    public static boolean isWholeNumber(String orginal) {  
        return isMatch("[+-]{0,1}0", orginal) || isPositiveInteger(orginal) || isNegativeInteger(orginal) || isDot(orginal);  
    }  
      
    public static boolean isPositiveDecimal(String orginal){  
        return isMatch("\\+{0,1}[0]\\.[1-9]*|\\+{0,1}[1-9]\\d*\\.\\d*", orginal);  
    }  
      
    public static boolean isNegativeDecimal(String orginal){  
        return isMatch("^-[0]\\.[1-9]*|^-[1-9]\\d*\\.\\d*", orginal);  
    }  
      
    public static boolean isDecimal(String orginal){  
        return isMatch("[-+]{0,1}\\d+\\.\\d*|[-+]{0,1}\\d*\\.\\d+", orginal);  
    }  
      
    public static boolean isRealNumber(String orginal){  
        return isWholeNumber(orginal) || isDecimal(orginal);  
    }  
  
    public static boolean isDot(String orginal){
    	return isMatch("[.]", orginal);  
    }
  
}