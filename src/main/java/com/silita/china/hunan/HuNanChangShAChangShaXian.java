package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.snatch.common.SnatchContent.*;

/**
 * 长沙县招标采购网
 *   工程招投标（招标公告及文件  答疑及补充通知）
 *   政府采购（招标公告  更正及补充公告   其他公告）
 * @author zk
 * 
 * **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath*:spring-test.xml"
		})

@Component
@JobHander(value="HuNanChangShAChangShaXian")
public class HuNanChangShAChangShaXian extends BaseSnatch {

	@Test
	public void run()throws Exception {
		firstGetMaxId();
	}

	public void firstGetMaxId()throws Exception {
		int page = 1;
		int pageTemp = 0;
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		list.add("5");
		String urls[] ={
				"http://www.csx.gov.cn/zbcg/zbcg/gcztb/zbggjwj/index.html",
				"http://www.csx.gov.cn/zbcg/zbcg/gcztb/dyjbctz/",
				"http://www.csx.gov.cn/zbcg/zbcg/zfcg/zbgg/",
				"http://www.csx.gov.cn/zbcg/zbcg/zfcg/gzjbcgg/",
				"http://www.csx.gov.cn/zbcg/zbcg/zfcg/qtgg/"
		};
		String[] catchTypes = {"1",DA_YI_TYPE,"1",GENG_ZHENG_TYPE,"0"};
		for (int i = 0; i < list.size(); i++) {
			page = 1;
			//===入库代码====
			super.queryBeforeSnatchState(urls[i]);
			String snatchNumber = SnatchUtils.makeSnatchNumber();
			try { 
				for (int j = 1; j <= page; j++) {
					if (pageTemp > 0 && page > pageTemp) {
						break;
					}
					String url = "";
					if(list.get(i) == "1"){
						url = "http://www.csx.gov.cn/zbcg/zbcg/gcztb/zbggjwj/";
						if(j>1){
							url ="http://www.csx.gov.cn/zbcg/zbcg/gcztb/zbggjwj/index_"+ (j-1) +".html";
						}
					}else if(list.get(i) == "2"){
						url = "http://www.csx.gov.cn/zbcg/zbcg/gcztb/dyjbctz/";
						if(j>1){
							url ="http://www.csx.gov.cn/zbcg/zbcg/gcztb/dyjbctz/index_"+ (j-1) +".html";
						}
					}else if(list.get(i) == "3"){
						url = "http://www.csx.gov.cn/zbcg/zbcg/zfcg/zbgg/";
						if(j>1){
							url ="http://www.csx.gov.cn/zbcg/zbcg/zfcg/zbgg/index_"+ (j-1) +".html";
						}
					}else if(list.get(i) == "4"){
						url = "http://www.csx.gov.cn/zbcg/zbcg/zfcg/gzjbcgg/";
						if(j>1){
							url ="http://www.csx.gov.cn/zbcg/zbcg/zfcg/gzjbcgg/index_"+ (j-1) +".html";
						}
					}else if(list.get(i) == "5"){
						url = "http://www.csx.gov.cn/zbcg/zbcg/zfcg/qtgg/";
						if(j>1){
							url ="http://www.csx.gov.cn/zbcg/zbcg/zfcg/qtgg/index_"+ (j-1) +".html";
						}
					}
					Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
					Document doc= conn.get();
					SnatchLogger.debug("第"+j +"页");
					if(page ==1){
						Element item =doc.select("#autopage").first();
						String countPage = item.toString().substring(item.toString().indexOf("countPage =")+12,item.toString().indexOf("//共多少页"));
						page = Integer.parseInt(countPage);
						pageTemp = page;
						//===入库代码====
						page = computeTotalPage(page,LAST_ALLPAGE);
					}

					Elements html = doc.select(".inside_newsbox2");
					Elements trs = html.select("li"); // 得到正文的一条
					for (int l = 0; l < trs.size(); l++) {
						Element tr =  trs.get(l);
						String href = tr.select("a").first().absUrl("href");
						String date=tr.select("span").text();
						//===入库代码====
						Notice notice = new Notice();
	                    notice.setProvince("湖南省");
	                    notice.setProvinceCode("huns");
	                    notice.setCity("长沙市");
	                    notice.setCityCode("cs");
	                    notice.setCounty("长沙县");
	                    notice.setCountyCode("csx");
	                    notice.setAreaRank(COUNTY);
	                    notice.setSnatchNumber(snatchNumber);
						Document docCount = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
						String content=docCount.select(".inside_newsc").select(".cont").html();
						if (content.contains("<script>") && content.contains("document.write(") && content.length() < 1000) {
							String scriptText = docCount.select(".inside_newsc").select(".cont").select("script").first().toString();
							content = scriptText.substring(scriptText.indexOf("document.write('") + 16,scriptText.length());
							content = content.substring(0,content.indexOf("');"));
						}
						String title = docCount.select("#articleTitle").text();
						notice.setTitle(title);
						notice.setContent(content);

						notice = SnatchUtils.setCatchTypeByTitle(notice,title);
						if (SnatchUtils.isNull(notice.getCatchType())) {
							notice.setCatchType(catchTypes[i]);
						} else {
							SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchTypes[i]);
						}

	                    notice.setUrl(href);
	                    notice.setOpendate(date);
	                    CURRENT_PAGE_LAST_OPENDATE = date;
						if (i > 1) {
							notice.setNoticeType("政府采购");
						} else {
							notice.setNoticeType("工程建设");
						}
						if (notice.getNoticeType().contains("采购")) {
							page = govDetailHandle(notice,j,page,l,trs.size());
						} else {
							page =detailHandle(notice,j,page,l,trs.size());
						}
					}
					if(j == page){
						page = turnPageEstimate(page);
					}
				}
				//===入库代码====
				super.saveAllPageIncrement(urls[i]);
			} catch (Exception e) {
				if (e instanceof InterruptedException) {
					throw e;
				}
				SnatchLogger.error(e);
			}finally {
				super.clearClassParam();
			}
		}
	}

	public Notice detail(String href, Notice notice, String catchType) throws Exception{
		return notice;
	 }


}