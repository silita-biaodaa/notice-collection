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
import java.text.SimpleDateFormat;

/**
 * Created by maofeng on 2017/7/19.
 * 衡阳市  衡阳建设工程网   http://www.hnhyjs.com/showmore.asp?lb=1
 * 中标公示
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanHengYangShijsgcw")

public class HuNanHengYangShijsgcw extends BaseSnatch{


    @Test
    public void run()throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception{
        int page = 1;
        String url = "http://www.hnhyjs.com/showmore.asp?lb=1";
        super.queryBeforeSnatchState(url);
        try {
        for(int i=1;i<=page;i++){
            if(i>1){
                url = "http://www.hnhyjs.com/showmore.asp?lb=1&Page="+i;
            }
            XxlJobLogger.log(i+"页");
                Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                Document doc= conn.get();
                if(page == 1){
                    Element item = doc.select("font[color=red]").last();    //得到纪录页数区域的div（此中包含需要得到的总页数）
                    String countPage = item.text();   //获取总页数
                    page = Integer.parseInt(countPage);
                    XxlJobLogger.log("总"+page+"页");
                    page = computeTotalPage(page, LAST_ALLPAGE);
                }
                Element tbody = doc.select("tbody").get(4);    //得到存放标题以及时间的区域tbody
                Elements trs = tbody.select("tr");      //得到tbody中的所有tr 第一个tr没用
                for(int j=1;j<trs.size();j++){
                    String titleTemp = trs.get(j).select("a").text();
                    String conturl=trs.get(j).select("a").attr("href");  //得到标题（a）的href值
                    if(!"".equals(conturl) && (titleTemp.indexOf("中标")!=-1)){
                        String date = trs.get(j).select("td").first().text().replace("/","-");   //得到日期
                        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
                        String date1 = sdf.format(sdf.parse(date));
                        conturl = "http://www.hnhyjs.com/"+conturl;
                        String catchType = "中标公告";
                        Notice notice = new Notice();
                        notice.setProvince("湖南省");
                        notice.setProvinceCode("huns");
                        notice.setCity("衡阳市");
                        notice.setCityCode("hy");
                        notice.setCatchType("2");
                        notice.setUrl(conturl);
                        notice.setNoticeType(catchType);
                        notice.setOpendate(date1);
                        page = detailHandle(notice, i, page, j, trs.size());
                    }
                }
            if(i==page){
                page = turnPageEstimate(page);
            }
        }
        url = "http://www.hnhyjs.com/showmore.asp?lb=1";
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

    public Notice detail(String href,Notice notice,String catchType) throws Exception{
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = contdoc.select("span[class=title]").first().text().trim();
        String content=contdoc.select("tbody").get(2).select("tr").get(5).select("td").html();              //得到中标详细内容区域
        notice.setContent(content);
        notice.setTitle(title);
        XxlJobLogger.log(notice.getTitle()+"     "+notice.getOpendate());
        return notice;
    }


}
