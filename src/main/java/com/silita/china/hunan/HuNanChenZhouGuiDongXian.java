package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
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
 * Created by hujia on 2017/10/06.
 * 郴州市  桂东县门户网站
 * http://www.gdx.gov.cn/page/item650/index.html      政府采购-招标公告
 * http://www.gdx.gov.cn/page/item651/index.html      政府采购-中标公告
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanChenZhouGuiDongXian")
public class HuNanChenZhouGuiDongXian extends BaseSnatch{


    @Test
    public void run()throws Exception  {
        sntachTask();
    }

    public void sntachTask() throws Exception {

        String[] urls = {
                "http://www.gdx.gov.cn/module/jslib/jquery/jpage/dataproxy.jsp?startrecord=1&endrecord=20&perpage=20&appid=1&webid=4&path=%2F&columnid=650&sourceContentType=1&unitid=1019&webname=%E6%A1%82%E4%B8%9C%E5%8E%BF%E4%BA%BA%E6%B0%91%E6%94%BF%E5%BA%9C&permissiontype=0",
                "http://www.gdx.gov.cn/module/jslib/jquery/jpage/dataproxy.jsp?startrecord=1&endrecord=40&perpage=20&appid=1&webid=4&path=%2F&columnid=651&sourceContentType=1&unitid=1019&webname=%E6%A1%82%E4%B8%9C%E5%8E%BF%E4%BA%BA%E6%B0%91%E6%94%BF%E5%BA%9C&permissiontype=0"};
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            Connection conn = null;
            Document doc = null;
            String url = urls[i];
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if(pageTemp>0 && page>pageTemp){
                        break;
                    }
                    if(pagelist>1){
                        url = urls[i].replace("startrecord=1","startrecord="+((pagelist-1)*20+1));
                    }
                    SnatchLogger.debug("第"+pagelist+"页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc= conn.get();
                    if(page ==1){
                        //获取总页数
                        String type = doc.html();
                        String count = type.substring(type.indexOf("P")+6,type.indexOf("S")-5).trim();
                        page = Integer.parseInt(count);
                        pageTemp = page;
                        SnatchLogger.debug("总"+page+"页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }
                    Elements trs = doc.select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").attr("href");
                        if (!"".equals(conturl)) {
                            conturl = trs.get(row).select("a").first().absUrl("href");
                            String title = trs.get(row).select("a").attr("title").trim();
                            String date = trs.get(row).select("span").text().trim();
                            Notice notice = new Notice();
                            notice = HuNanChenZhouGuiDongXian.getCatchType(notice,title);
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("郴州市");
                            notice.setCityCode("cz");
                            notice.setCounty("桂东县");
                            notice.setCountyCode("gdx");
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            page = govDetailHandle(notice,pagelist,page,row,trs.size());
                        }
                    }
                    if(pagelist==page){
                        page = turnPageEstimate(page);
                    }
                }
                super.saveAllPageIncrement(urls[i]);
            }finally {
                super.clearClassParam();
            }
        }
    }

    public Notice detail(String href,Notice notice,String catchType) throws Exception{
        Document contdoc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        String title = contdoc.select(".hd").select("h1").text();
        String content=contdoc.select(".bd").html();
        notice.setTitle(title);
        notice.setContent(content);
        return notice ;
    }

    public static Notice getCatchType(Notice notice,String title){
        if(title.contains("答疑")||title.contains("补充")||title.contains("澄清")||title.contains("延期")
                ||title.contains("修改")||title.contains("补遗")||title.contains("终止")||title.contains("质疑")
                ||title.contains("暂停")||title.contains("流标")||title.contains("废标")||title.contains("更正")
                ||title.contains("调整")){
            notice.setCatchType("0");
            notice.setNoticeType("其他公告");
        }else if (title.contains("代理")&&title.contains("结果")||title.contains("代理")&&title.contains("选定")
                ||title.contains("代理")&&title.contains("中标")){
            notice.setCatchType("5");
            notice.setNoticeType("代理中标公告");
        }else if (title.contains("代理")){
            notice.setCatchType("4");
            notice.setNoticeType("代理招标公告");
        }else if(title.contains("中标")||title.contains("成交")||title.contains("结果")
                ||title.contains("成果")||title.contains("中选")||title.contains("入围")){
            notice.setCatchType("2");
            notice.setNoticeType("中标公告");
        }else{
            notice.setCatchType("1");
            notice.setNoticeType("招标公告");
        }
        return notice;
    }

}
