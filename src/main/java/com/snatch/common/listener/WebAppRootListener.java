package com.snatch.common.listener;

import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Enumeration;

/**
 * 将所有web.xml配置的键值对放入system property
 * @author Administrator
 *
 */
public class WebAppRootListener  implements ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {
		setWebAppRootSystemProperty(event.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent event) {
		WebUtils.removeWebAppRootSystemProperty(event.getServletContext());
	}
	/**
	 * 将所有web.xml配置的参数放入System.getProperty()
	 * @param servletContext
	 * @throws IllegalStateException
	 */
	private  void setWebAppRootSystemProperty(ServletContext servletContext) throws IllegalStateException {
		Assert.notNull(servletContext, "ServletContext must not be null");
		String root = servletContext.getRealPath("/");
		if (root == null) {
			throw new IllegalStateException(
			    "Cannot set web app root system property when WAR file is not expanded");
		}
		Enumeration paramsName = servletContext.getInitParameterNames();
		while(paramsName.hasMoreElements()){
			String key=(String)paramsName.nextElement();
			String value = servletContext.getInitParameter(key);
			System.setProperty(key, value);
			servletContext.log("Set web app system property: '" + key + "' = [" + root + "]");
			
		}
		
	}
}
