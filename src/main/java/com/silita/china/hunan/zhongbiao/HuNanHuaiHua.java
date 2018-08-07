package com.silita.china.hunan.zhongbiao;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
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


/**
 * Created by hujia on 2017/7/21.
 * 怀化市 怀化市公共资源交易网  http://ggzy.huaihua.gov.cn/hhweb/zbgs/022001/
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath*:spring-test.xml"
		})

@Component
@JobHander(value = "HuNanHuaiHua")
public class HuNanHuaiHua extends BaseSnatch{


	@Test
	public void run()throws Exception{
		firstGetMaxId();
	}



	public void firstGetMaxId()throws Exception{
		String a1 = null;
		String a2 = null;
		String a3 = null;
		String a4 = null;
		int page=1;
		String url = "http://ggzy.huaihua.gov.cn/hhweb/zbgs/022001/022001003/MoreInfo.aspx?CategoryNum=022001003";
			//===入库代码====
		super.queryBeforeSnatchState(url);
			try {
				Document doc = null;
				for (int j = 1; j <= page; j++) {
					Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000*60).ignoreHttpErrors(true);
					XxlJobLogger.log(j+"页");
					if(page==1){
						doc=conn.get();
						Element item=doc.select("#MoreInfoList1_Pager").first();
						String  countPage=item.select("b").get(1).text();
						page=Integer.parseInt(countPage);
						//===入库代码====
						page = computeTotalPage(page,LAST_ALLPAGE);
					}else{
						conn.data("__VIEWSTATE", a1);
						conn.data("__VIEWSTATEGENERATOR",a2);
						conn.data("__EVENTTARGET", a3);
						conn.data("__EVENTARGUMENT",a4);

						String data = conn.request().data().toString();
						byte[] postDataBytes = data.getBytes("UTF-8");
						conn.header("Content-Length",String.valueOf(postDataBytes.length));
						doc=conn.post();
					}
					conn.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					conn.header("Accept-Encoding", "gzip, deflate");
					conn.header("Accept-Language", "zh-CN,zh;q=0.8");
					conn.header("Cache-Control", "max-age=0");
					conn.header("Connection","keep-alive");
					conn.header("Content-Length", "11666");
					conn.header("Content-Type", "application/x-www-form-urlencoded");
					conn.header("Cookie", "ASP.NET_SessionId=ounvsu45aqumvh55lymzm5iy");
					conn.header("Host","ggzy.huaihua.gov.cn");
					conn.header("Origin", "http://ggzy.huaihua.gov.cn");
					conn.header("Referer", "http://ggzy.huaihua.gov.cn/hhweb/jygg/004001/004001001/MoreInfo.aspx?CategoryNum=004001001");
					conn.header("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.124 Safari/537.36");


					a1 = doc.select("#__VIEWSTATE").attr("value");
					a2 = doc.select("#__VIEWSTATEGENERATOR").attr("value");
					a3 = "MoreInfoList1$Pager";
					a4 = j+1+"";

					Elements data = doc.select("#MoreInfoList1_DataGrid1");
					Elements trs = data.select("tr"); 
					for(int k=0;k<trs.size();k++){
						String href=trs.get(k).select("a").first().absUrl("href");
						Document docCount = Jsoup.parse(new URL(href).openStream(), "gbk", href);
						String title = docCount.select("#tdTitle").select("b").text().trim();
						String date=trs.get(k).select("td").get(2).text().trim();
						//===入库代码====
						String catchType="";
						Notice notice = new Notice();
	                    notice.setProvince("湖南省");
	                    notice.setProvinceCode("huns");
	                    notice.setCity("怀化市");
	                    notice.setCityCode("hh");
	                    notice.setCatchType("2");
						notice.setNoticeType("中标公告");
	                    notice.setUrl(href);
	                    notice.setOpendate(date);
						CURRENT_PAGE_LAST_OPENDATE = date;
	                    notice.setTitle(title);
						page = detailHandle(notice,j,page,k,trs.size());
						XxlJobLogger.log(title+"----"+date);
					}
					if(j==page){
						page = turnPageEstimate(page);
					}
				}
				//===入库代码====
				url = "http://ggzy.huaihua.gov.cn/hhweb/zbgs/022001/022001003/MoreInfo.aspx?CategoryNum=022001003";
				super.saveAllPageIncrement(url);
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
		Document docCount = Jsoup.parse(new URL(href).openStream(),"gbk",href);
		String content=docCount.select("#TDContent").html();
		notice.setContent(content);
		return notice;
	 }
}
