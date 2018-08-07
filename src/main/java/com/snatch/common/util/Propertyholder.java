package com.snatch.common.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将properties中的参数存储以便访问
 * @author Administrator
 *
 */
public class Propertyholder  extends
        PropertyPlaceholderConfigurer {
	 private static Map<String, Object> ctxPropertiesMap;  
	  
	    @Override  
	    protected void processProperties(  
	            ConfigurableListableBeanFactory beanFactoryToProcess,
	            Properties props) throws BeansException {
	        super.processProperties(beanFactoryToProcess, props);  
	        ctxPropertiesMap = new HashMap<String, Object>(); 
			Pattern pattern = Pattern.compile("\\$\\{([\\w,\\.]*)\\}");
	        for (Object key : props.keySet()) {  
	            String keyStr = key.toString();  
	            String value = props.getProperty(keyStr);
	            Matcher matcher=pattern.matcher(value);
	        	while (matcher.find()){
	        		String code01=matcher.group(0);
	        		String code02=matcher.group(1);
	        		value=value.replace(code01, System.getProperty(code02));
	        	}
	            
	            ctxPropertiesMap.put(keyStr, value);  
	        }  
	    }  
	  
	    public static String getContextProperty(String name) {  
	        return (String)ctxPropertiesMap.get(name);  
	    }
	    
}
