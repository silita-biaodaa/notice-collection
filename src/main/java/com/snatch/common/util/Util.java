package com.snatch.common.util;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.snatch.common.utils.SnatchLogger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Util {
	//增量抓取方式一：根据日期实现增量抓取
	public static boolean isToday(String date){
		SimpleDateFormat sdf=null;
		if(date.indexOf("-")!=-1){
			 sdf=new SimpleDateFormat("yyyy-MM-dd");
		}
		else if(date.indexOf("/")!=-1){
			sdf=new SimpleDateFormat("yyyy/MM/dd");
		}
		else if(date.indexOf("年")!=-1){
			date=date.replace("年", "-");
			date=date.replace("月", "-");
			date=date.replace("日", "").trim();
			sdf=new SimpleDateFormat("yyyy-MM-dd");
		}
		sdf.format(new Date());
		Boolean flag=true;
		Date current=null;
		Date noticeDate=null;
		
		try {
			//当前时间
			current = sdf.parse(sdf.format(new Date()));
			noticeDate=sdf.parse(date);
			//如果是当天之前的公告就停止抓取
			if(noticeDate.getTime()<current.getTime()){
				flag=false;
			}
//			System.out.println(flag);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}
		
		return flag;
	}

	//增量抓取方式二：比较网址中的最大id
	public static boolean isMaxId(String currentId,String lastMaxId){
		boolean flag=true;
		if(lastMaxId.equals(currentId)){
			flag=false;
		}
		
		return flag;
	}

	//增量抓取方式二：比较网址中的总页数
	public static boolean isCurrentAllPage(int currentAllPage,int lastAllPage){
		boolean flag=true;
		
		if(currentAllPage<=lastAllPage){
			flag=false;
		}
		return flag;
	}
	//使用htmlunit获取页面
	public static Document getPageByHtmlUnit(String url)throws Exception{
		Document doc=null;
		WebClient wc = new WebClient(BrowserVersion.CHROME);
		wc.getOptions().setUseInsecureSSL(true);
		// 启用JS解释器，默认为true
		wc.getOptions().setJavaScriptEnabled(true);
		wc.getOptions().setCssEnabled(false); // 禁用css支持
		// js运行错误时，是否抛出异常
		wc.getOptions().setThrowExceptionOnScriptError(false);
		// 设置连接超时时间 ，这里是10S。如果为0，则无限期等待

		wc.getOptions().setTimeout(100000);
		wc.getOptions().setDoNotTrackEnabled(false);
		try {
			HtmlPage page = wc.getPage(url);
			doc=Jsoup.parse(page.asXml());
		} catch (Exception e) {
			throw e;
		}finally {
			wc.close();
		}
		return doc; 
	}
	//使用htmlunit获取页面
		public static Document getPageByHtmlUnit(String url, List<NameValuePair> pairs)throws Exception{
			Document doc=null;
			WebClient wc = new WebClient(BrowserVersion.CHROME);
			WebRequest webRequest=null;
			try {
				webRequest = new WebRequest(new URL(url));
				wc.getOptions().setUseInsecureSSL(true);
				// 启用JS解释器，默认为true
				wc.getOptions().setJavaScriptEnabled(true);
				wc.getOptions().setCssEnabled(false); // 禁用css支持
				// js运行错误时，是否抛出异常
				wc.getOptions().setThrowExceptionOnScriptError(false);
				// 设置连接超时时间 ，这里是10S。如果为0，则无限期等待

				wc.getOptions().setTimeout(100000);
				wc.getOptions().setDoNotTrackEnabled(false);

				// 设置访问方式
				webRequest.setHttpMethod(HttpMethod.POST);
				//设置参数
				webRequest.setRequestParameters(pairs);

				HtmlPage page = wc.getPage(webRequest);
				doc=Jsoup.parse(page.asXml());
			} catch (Exception e) {
				throw e;
			}finally {
				wc.close();
			}
			return doc; 
		}	
		//使用htmlunit获取页面
	public static HtmlPage getPageByHtmlUnitForHtmlPage(String url)throws Exception {
		HtmlPage page=null;
		WebClient wc = new WebClient(BrowserVersion.CHROME);
		
		WebRequest webRequest = null;
		try {
			webRequest = new WebRequest(new URL(url));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
		wc.getOptions().setUseInsecureSSL(true);
		// 启用JS解释器，默认为true
		wc.getOptions().setJavaScriptEnabled(true);
		wc.getOptions().setCssEnabled(false); // 禁用css支持
		// js运行错误时，是否抛出异常
		wc.getOptions().setThrowExceptionOnScriptError(false);
		// 设置连接超时时间 ，这里是10S。如果为0，则无限期等待

		wc.getOptions().setTimeout(100000);
		wc.getOptions().setDoNotTrackEnabled(false);

		// 设置访问方式
		webRequest.setHttpMethod(HttpMethod.POST);
		try {
			page = wc.getPage(webRequest);
		} catch (Exception e) {
			throw e;
		}finally {
			wc.close();
		}
		return page;
	} 
	// 解析PDF文件
	 public static String readPDF(String pdfUrl) throws IOException {
	        Metadata metadata = new Metadata();
	        TikaInputStream stream = TikaInputStream.get(new URL(pdfUrl), metadata);
	        // 获取解析后得到的PDF文档对象
	        PDDocument document = PDDocument.load(stream);
	        PDFTextStripper stripper = new PDFTextStripper();
	        String content=stripper.getText(document);
	        stream.close();
	        document.close();
	        // 从PDF文档对象中剥离文本返回
	        return content;
	    }
	 // 解析doc文件
	 public static String readDOC(String docUrl) throws IOException {
		 BodyContentHandler handler = new BodyContentHandler();
	      Metadata metadata = new Metadata();
	      FileInputStream inputstream = new FileInputStream(new File(docUrl));
	      ParseContext pcontext = new ParseContext();
	      
	      OOXMLParser  msofficeparser = new OOXMLParser (); 
	      try {
			msofficeparser.parse(inputstream, handler, metadata,pcontext);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TikaException e) {
			e.printStackTrace();
		}
	      System.out.println("Contents of the document:" + handler.toString());
	      System.out.println("Metadata of the document:");
	      String[] metadataNames = metadata.names();
	      
	      for(String name : metadataNames) {
	         System.out.println(name + ": " + metadata.get(name));
	      }
		return handler.toString();
	    }
	 
	 //执行js方法进行
	 public static Document executeJs(String url, String js) throws Exception{
		 Document doc=null;
		 HtmlPage page=null;
			WebClient wc = new WebClient(BrowserVersion.CHROME);
			wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
			wc.getOptions().setUseInsecureSSL(true);
			// 启用JS解释器，默认为true
			wc.getOptions().setJavaScriptEnabled(true);
			wc.getOptions().setCssEnabled(false); // 禁用css支持
			// js运行错误时，是否抛出异常
			wc.getOptions().setThrowExceptionOnScriptError(false);
			// 设置连接超时时间 ，这里是10S。如果为0，则无限期等待

			wc.getOptions().setTimeout(100000);
			wc.getOptions().setDoNotTrackEnabled(false);
			try {
				page = wc.getPage(url);
				doc=Jsoup.parse(page.asXml());
			} catch (Exception e) {
				throw e;
			} finally {
				wc.close();
			}
			
			ScriptResult pageData=page.executeJavaScript(js);
	        doc=Jsoup.parse(pageData.getJavaScriptResult().toString());
	         
	         return doc;
	 }

	 public static void main(String[] args){

     }
	 
	public static JsonNode getContentByJson(String json) throws Exception{
		 ObjectMapper mapper = new ObjectMapper();
		 mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
//		 //解决转义字符问题
		 mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;
		 mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		 mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		 mapper.configure(Feature.ALLOW_COMMENTS,true);

		JsonNode node = null;
		try {
			node = mapper.readValue(json, JsonNode.class);
		} catch (Exception e) {
			SnatchLogger.error(e);
            throw e;
		}
			
		return node;
	 }
	
	//使用HtmlUnit简单访问
	public static HtmlPage getPageByHtml(String url)throws Exception{
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
	    WebRequest webRequest = null;
	    HtmlPage rootPage = null;
	    try {
			webRequest = new WebRequest(new URL(url));
			rootPage=webClient.getPage(webRequest);
		} catch (Exception e) {
			throw e;
		}finally {
			webClient.close();
		}
		
		return rootPage ;
	}
	//使用HtmlUnit简单访问
	public static HtmlPage getPageByHtmlForPost(String url)throws Exception{
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
	    WebRequest webRequest = null;
	    HtmlPage rootPage = null;
	    try {
			webRequest = new WebRequest(new URL(url));
			webRequest.setHttpMethod(HttpMethod.POST);
			rootPage=webClient.getPage(webRequest);
		} catch (Exception e) {
			throw e;
		}finally {
			webClient.close();
		}
		
		return rootPage ;
	}
}
