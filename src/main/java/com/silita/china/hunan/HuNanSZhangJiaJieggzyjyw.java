package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
import org.codehaus.jackson.JsonNode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * 湖南省张家界公共资源交易网 create by SuDan 2017/03/21
 * http://www.zjjsggzy.gov.cn
 * 房建市政  -- 招标公告、补充公告
 * 交通运输  -- 招标公告、补充公告
 * 水利工程  -- 招标公告、补充公告
 *
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath*:spring-test.xml"
})

@Component
@JobHander(value="HuNanSZhangJiaJieggzyjyw")
public class HuNanSZhangJiaJieggzyjyw extends BaseSnatch {


	@Test
	@Override
	public void run()throws Exception {
		Outer();
	}
	@Deprecated//和HuNanZhangJiaJie 重复
	public void Outer()throws Exception{
		Connection conn;
		String[] type={
				"招标公告","补充通知", "控制价", "答疑"};
		String[] typeName={"房建市政","交通运输","水利"};
		String[] urls={
				"http://www.zjjsggzy.gov.cn/TenderProject/GetTpList?", //招标公告
				"http://www.zjjsggzy.gov.cn/TenderProject/GetSupList?",//补充通知
				"http://www.zjjsggzy.gov.cn/TenderProject/GetTenderControl?",//控制价
				"http://www.zjjsggzy.gov.cn/TenderProject/GetTenderQuestion?"//答疑
		};
		String[] type1={
				"TenderFlow/GetTpInfo",
				"supplenotice/GetInfosByTpId",
				"TenderProject/GetConPriceInfo",
				"TenderQuestion/GetInfosByTpId"};
		SimpleDateFormat sdf =  new SimpleDateFormat( "yyyy-MM-dd" );
		for(int k=0;k<type.length;k++){
			XxlJobLogger.log("-----------------------"+type[k]+"-----------------------------------");
			for(int s=0;s<typeName.length;s++){
				String link=urls[k]+"records=15&IsShowOld=true&category="+typeName[s]+"&page=1";
				XxlJobLogger.log(link);
				String url = link;
				super.queryBeforeSnatchState(url);
				try{
					conn = Jsoup.connect(link).ignoreContentType(true).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
					Document doc= null;
					doc = conn.get();
					if(conn.response().statusCode()==200){
						//访问网站成功，开始分页抓取
						int allPage= 0;
						allPage = getAllPage(doc);
						XxlJobLogger.log("共"+allPage+"页");
						//===入库代码====
						allPage = computeTotalPage(allPage,LAST_ALLPAGE);
						//上次抓取的公告的最大id
						for(int i=1;i<=allPage;i++){
							XxlJobLogger.log(i+"=======================================");
							String u=link.replace("page=1", "page="+i);
							conn = Jsoup.connect(u).ignoreContentType(true).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
							Document document= null;
							document = conn.get();
							//获取当前页面的公告目录
							JsonNode lis= Util.getContentByJson(document.text());
							lis=lis.findPath("json");
							for(int j=0;j<lis.size();j++){
								String href="";
								String id="";
								//公告标题
								String title=lis.get(j).findPath("Title").asText();
								//公告日期
								String date = lis.get(j).findPath("time").asText();
								if(k==0){
									id=lis.get(j).findPath("id").asText();
								}else{
									id=lis.get(j).findPath("TpId").asText();
								}
								href="http://www.zjjsggzy.gov.cn/"+type1[k]+"?tpId="+id;
								XxlJobLogger.log(href);
								//===入库代码====
								Notice notice = new Notice();
								notice.setProvince("湖南省");
								notice.setProvinceCode("huns");
								notice.setCity("张家界市");
								notice.setCityCode("zjj");
								if(k==0){
									notice.setCatchType("1");
									notice.setNoticeType("招标公告");
								}else{
									notice.setCatchType("0");
								}
								notice.setTitle(title);
								notice.setSyncUrl(href);
								notice.setNoticeType(typeName[s]);
								notice.setUrl("http://www.zjjsggzy.gov.cn/新流程/招投标信息/jyxx_1.html?type="+type[k]+"&tpid="+id);
								notice.setOpendate(sdf.format(sdf.parse(date)));
								CURRENT_PAGE_LAST_OPENDATE = notice.getOpendate();
								allPage = detailHandle(notice,i,allPage,j,lis.size());
							}
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
		}
	}

	//获取网址的总页数
	public int getAllPage(Document doc) throws Exception{
		int pageSize=15;
		int allPage=0;
		String total=Util.getContentByJson(doc.text()).findPath("msg").asText();
		if(SnatchUtils.isNull(total)){
			return 1;
		}
		int totalMsg=Integer.parseInt(total);
		if(totalMsg!=0&&totalMsg%pageSize>0){
			allPage=totalMsg/pageSize+1;
		}
		else if(totalMsg!=0&&totalMsg%pageSize==0){
			allPage=totalMsg/pageSize;
		}
		return allPage;
	}

	@Override
	public Notice detail(String href, Notice notice, String catchType) throws Exception{
		String content="";
		Document doc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
		String json=doc.body().toString().replace("<body>", "").replace("</body>", "");
		json=json.replace("\" ", "'");
		json=json.replace("\">", "'>");
		json=json.replace("=\"", "='");
		json=json.replace("\\&quot;", "");
		json=json.replace("&quot;", "");
		json=json.replace("\n", "");
		JsonNode text=Util.getContentByJson(json);
		//公告内容
		if("1".equals(notice.getCatchType())){
			content=text.findPath("json").findPath("招标内容").asText();
		}else{
			content=text.findPath("json").findPath("修改文本").asText();
			String title = text.findPath("json").findPath("标题").asText();
			if(SnatchUtils.isNull(title)){
				title = text.findPath("json").findPath("招标标题").asText();
			}
			if(SnatchUtils.isNotNull(title)){
				notice.setTitle(title);
			}
		}
		if(SnatchUtils.isNull(content)){
			content=text.findPath("json").findPath("内容").asText();
		}
		notice.setContent(content);
		return notice;
	}
}
