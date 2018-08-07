package com.snatch.common.util;

import org.apache.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URL;

public class CheskUrl {
	   private static Logger logger = Logger.getLogger(CheskUrl.class);
       private static URL urlStr;
       private static HttpURLConnection connection;
       private static int state = -1;

      
       public static boolean isConnect(String url) {
           int counts = 0;

           if (url == null || url.length() <= 0) {
               return false;
           }
           while (counts < 5) {
               try {
                   urlStr = new URL(url);
                   connection = (HttpURLConnection) urlStr.openConnection();
                   state = connection.getResponseCode();
                   if (state == 200) {
                       return true;
                   }
                   break;
               } catch (Exception ex) {
                   counts++; logger.info("loop :" + counts);
                   continue;
               }
           }
           return false;
       }

}
