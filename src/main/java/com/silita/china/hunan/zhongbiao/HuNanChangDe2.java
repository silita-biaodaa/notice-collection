package com.silita.china.hunan.zhongbiao;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hujia on 2017/7/20.
 * 常德市 政府采购网  http://changd.ccgp-hunan.gov.cn/channel_1403.action
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath*:spring-test.xml"
		})

@JobHander(value = "HuNanChangDe2")
@Component
public class HuNanChangDe2 extends BaseSnatch{
	

	@Test
	public void run()throws Exception{
		getUrlList();
	}
	
	public void getUrlList()throws Exception{
		int page=1;
		String url="http://changd.ccgp-hunan.gov.cn/channel_1403.action";
		Map<String, String> data = null;
		Map<String, String> cookie = null;
		Document doc = null;
		super.queryBeforeSnatchState(url);
	    
		try {
			for (int i = 1; i <= page; i++) {
				XxlJobLogger.log("=========第" + i + "页");
				Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
				if (i == 1) {
					doc = conn.get();
					cookie = conn.response().cookies();
					if (conn.response().statusCode() == 200) {
						String text1 = doc.select("#total").text().trim();
						String[] text2 = text1.split(",");
						String text = text2[0] + text2[1];
						int page1 = Integer.parseInt(text);
						if (page1 % 20 == 0) {
							page = page1 / 20;
						} else {
							page = (page1 / 20) + 1;
						}
						XxlJobLogger.log("总页数" + page);
						//===入库代码====
						page = computeTotalPage(page, LAST_ALLPAGE);
					}
				} else {
					data = new HashMap<String, String>();
					data.put("grid.pageno", String.valueOf(i - 1));
					data.put("flag", "");
					data.put("grid.queryparam[0].name", "title");
					data.put("grid.queryparam[0].oper", "like");
					data.put("grid.queryparam[0].value", "");
					data.put("grid.queryparam[1].name", "purchasemodeno");
					data.put("grid.queryparam[1].oper", "=");
					data.put("grid.queryparam[1].value", "");
					data.put("grid.queryparam[4].name", "orgid");
					data.put("grid.queryparam[4].oper", "=");
					data.put("grid.queryparam[4].value", "");
					data.put("grid.queryparam[2].name", "purchasername");
					data.put("grid.queryparam[2].oper", "like");
					data.put("grid.queryparam[2].value", "");
					data.put("grid.queryparam[3].name", "agentname");
					data.put("grid.queryparam[3].oper", "like");
					data.put("grid.queryparam[3].value", "");
					conn.data(data);
					conn.cookies(cookie);
					doc = conn.post();
				}
				Elements tr = doc.select(".listcontainer").select("li");
				int lastRow = 0;
				for(int k = 0;k<tr.size();k++){
					String title = tr.get(k).select("a").attr("title").trim();
					if(!title.contains("采购")){
						lastRow = k;
					}
				}
				for (int j = 0; j < tr.size(); j++) {
					String href = tr.get(j).select("a").attr("href");
					if (href != null) {
						href = "http://changd.ccgp-hunan.gov.cn/" + href;
						String title = tr.get(j).select("a").attr("title").trim();
						if (title.contains("采购") == true) continue;
						String date = tr.get(j).select(".fr").text().trim();
						XxlJobLogger.log(title + "     " + date);
						//===入库代码====
						String catchType = "中标公告";
						Notice notice = new Notice();
						notice.setProvince("湖南省");
						notice.setProvinceCode("huns");
						notice.setCity("常德市");
						notice.setCityCode("cd");
						notice.setCatchType("2");
						notice.setTitle(title);
						notice.setOpendate(date);
						notice.setUrl(href);
						notice.setNoticeType(catchType);
						page = detailHandle(notice, i, page, j, lastRow);
					}
				}
				if(i==page){
					page = turnPageEstimate(page);
				}
				super.saveAllPageIncrement(url);
			}
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				throw e;
			}
			SnatchLogger.error(e);
		}finally {
			super.clearClassParam();
		}
	}

	public Notice detail(String href, Notice notice, String catchType) throws Exception {
		Document contdoc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
		String content = contdoc.select(".article").html();
		notice.setContent(content);
		return notice;
	 }

}
