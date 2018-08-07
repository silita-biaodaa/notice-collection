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

/**
 * Created by hujia on 2017/7/19.
 * 郴州市 郴州市公共资源交易中心  http://www.czggzy.net/jyxx/004001/004001004/1.html
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanChenZhou2")
public class HuNanChenZhou2 extends BaseSnatch{


    @Test
    public void run()throws Exception  {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        int page = 1;
        String url = "http://www.czggzy.net/jyxx/004001/004001004/1.html";
        super.queryBeforeSnatchState(url);
        try {
        for(int i=1;i<=page;i++) {
            if (i > 1) {
                url = "http://www.czggzy.net/jyxx/004001/004001004/" + i + ".html";
            }
            XxlJobLogger.log(i + "页");
                Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                Document doc = conn.get();
                if (page == 1) {
                    String type = doc.select("#index").text();
                    String count = type.substring(type.indexOf("/") + 1);
                    page = Integer.parseInt(count);
                    XxlJobLogger.log("总" + page + "页");
                    page = computeTotalPage(page, LAST_ALLPAGE);
                }
                Elements trs = doc.select(".ewb-nbd-items").select("li");
                for (int j = 0; j < trs.size(); j++) {
                    String conturl = trs.get(j).select("a").attr("href");
                    if (!"".equals(conturl)) {
                        String catchType = "中标公告";
                            conturl = "http://www.czggzy.net" + conturl;
                            String title = trs.get(j).select("a").text().trim();
                            String date = trs.get(j).select("span").text().trim();
                            XxlJobLogger.log(date);
                            XxlJobLogger.log(conturl);
                            Notice notice = new Notice();
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("郴州市");
                            notice.setCityCode("cz");
                            notice.setCatchType("2");
                            notice.setTitle(title);
                            notice.setNoticeType(catchType);
                            notice.setUrl(conturl);
                            notice.setOpendate(date);
                            page = detailHandle(notice, i, page, j, trs.size());
                        }
                    }
            if(i==page){
                page = turnPageEstimate(page);
            }
        }
            url = "http://www.czggzy.net/jyxx/004001/004001004/1.html";
            super.saveAllPageIncrement(url);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            }
            SnatchLogger.error(e.getMessage(),e);
        }finally {
            super.clearClassParam();
        }
    }

    public Notice detail(String href,Notice notice,String catchType) throws Exception{
        String content="";
        if (href.endsWith("pdf")) {
                notice.setPdfURL(href);
                 content=href;
        }
        Document contdoc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        content=contdoc.select("#_Sheet1").html();
        if(content.length()==0){
            content=contdoc.select(".news-article").select("*:gt(1)").html();
        }
        if(content.length()==0){
            content=contdoc.select(".news-detail-wrap").select("*:gt(1)").html();
        }
        if(content.length()==0){
            String pdfUrl=contdoc.select(".news-detail-wrap").select("a").first().absUrl("href");
            notice.setPdfURL(pdfUrl);
            content=href;
        }
        notice.setContent(content);
       return notice;
    }


}
