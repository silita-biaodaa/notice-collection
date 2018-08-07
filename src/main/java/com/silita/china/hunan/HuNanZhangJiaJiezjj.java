package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;


/**
 *   张家界市公共资源交易信息网
 * @author yj
 *  http://www.anxiang.gov.cn/c1712/index.html	工程交易 招标
 *  http://www.anxiang.gov.cn/c1713/index.html	工程交易 招标补充公告
 *  http://www.anxiang.gov.cn/c1714/index.html	工程交易 中标
 *  http://www.anxiang.gov.cn/c1709/index.html	政府采购 招标
 *	http://www.anxiang.gov.cn/c1710/index.html	政府采购 招标补充公告
 *	http://www.anxiang.gov.cn/c1711/index.html	政府采购 中标
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath*:spring-test.xml"
		})
@Component
@JobHander(value="HuNanZhangJiaJiezjj")
public class HuNanZhangJiaJiezjj extends BaseSnatch {


	@Test
	@Rollback(false)
	public void run() throws Exception{
		firstGetMaxId();
	}

	public void firstGetMaxId() throws Exception{
		String urls[] ={
				"http://www.zjj.gov.cn/c2231/index.html",	//重大建设项目-招投标信息
				"http://www.zjj.gov.cn/c635/index.html",	//政府采购-招标公告
				"http://www.zjj.gov.cn/c636/index.html"	//政府采购-中标公告
		};
		for (int i = 0; i < urls.length; i++) {
			int pageTemp = 0;
			int page =30;   //默认30页
			Connection conn = null;
			Document doc = null;
			String url = urls[i];
			try{
				super.queryBeforeSnatchState(urls[i]);
				for (int pagelist = 1; pagelist <= page; pagelist++) {
					if(i==0 && pagelist>1){
						url = "http://www.zjj.gov.cn/c2231/pages/"+pagelist+".html";
					}else if(i==1 && pagelist>1){
						url = "http://www.zjj.gov.cn/c635/pages/"+pagelist+".html";
					}else if(i==2 && pagelist>1){
						url = "http://www.zjj.gov.cn/c636/pages/"+pagelist+".html";
					}
					SnatchLogger.debug("第"+pagelist+"页");
					conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
					doc= conn.get();
					Elements els = doc.select("#pages").first().select("a");

					String pageCont = els.get(els.size() - 2).text().trim();
					String pageCont1 = els.get(els.size() - 1).absUrl("href");
					pageCont1 = pageCont1.substring(pageCont1.indexOf("pages/")+6,pageCont1.indexOf(".html"));
					page = Integer.valueOf(pageCont1) > Integer.valueOf(pageCont)?Integer.valueOf(pageCont1):Integer.valueOf(pageCont);
					//===入库代码====
					page = computeTotalPage(page, LAST_ALLPAGE);
					pageTemp = page;
					SnatchLogger.debug("总"+page+"页");

					Elements trs = doc.select(".list").first().select("li");
					for (int row = 0; row < trs.size(); row++) {
						String conturl = trs.get(row).select("a").first().absUrl("href");

						if(StringUtils.isNotBlank(conturl)){
							String date = trs.get(row).select("span").first().text().trim();
							date = date.replace("[","").replace("]","");

							Notice notice = new Notice();
							notice.setUrl(conturl);
							notice.setOpendate(date);
							notice.setProvince("湖南省");
							notice.setProvinceCode("huns");
							notice.setCity("张家界市");
							notice.setCityCode("zhangjjs");
							switch (i){
								case 0:
									notice.setCatchType("1");
									notice.setNoticeType("招标公告");
									break;
								case 1:
									notice.setCatchType("0");
									notice.setNoticeType("政府采购");
									break;
								case 2:
									notice.setCatchType("0");
									notice.setNoticeType("政府采购");
									break;
							}
							CURRENT_PAGE_LAST_OPENDATE = date;
							page = govDetailHandle(notice, pagelist, page, row, trs.size());
						}
					}
					//===入库代码====
					if(pagelist==page){
						page = turnPageEstimate(page);
					}
					page = pageTemp;
				}
				//===入库代码====
				super.saveAllPageIncrement(urls[i]);
			}finally {
				clearClassParam();
			}
		}
	}

	public Notice detail(String href, Notice notice, String catchType) throws Exception{
		Document docCount = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
		String title = docCount.select(".page").first().select("h2").first().text().trim();
		notice = SnatchUtils.setCatchTypeByTitle(notice, title);
		Element ele = docCount.select("#zoom").first();
		String content = ele.html();
		notice.setContent(content);
		notice.setTitle(title);
		return notice;
	 }
}