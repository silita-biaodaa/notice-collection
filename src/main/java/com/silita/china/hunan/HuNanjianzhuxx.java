package com.silita.china.hunan;


import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
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
import java.text.SimpleDateFormat;

import static com.snatch.common.SnatchContent.ZHAO_BIAO_TYPE;
import static com.snatch.common.SnatchContent.ZHONG_BIAO_TYPE;

/**
 * 湖南建筑信息网  http://www.hunanjz.com
 * http://www.hunanjz.com/Html/GcxxList.aspx    工程信息--招标信息
 * http://www.hunanjz.com/Html/GcxxList.aspx?lb=2   工程信息--中标信息
 * Created by maofeng on 2017/10/18.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring-test.xml" })


@Component
@JobHander(value="HuNanjianzhuxx")
public class HuNanjianzhuxx extends BaseSnatch{

    @Test
    @Override
    public void run () throws Exception{
        snatchTask();
    }
    private void snatchTask () throws Exception {
        String[] urls = {
                "http://www.hunanjz.com/Html/GcxxList.aspx",
                "http://www.hunanjz.com/Html/GcxxList.aspx?lb=2"
        };
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            Connection conn = null;
            Document doc = null;
            String url = urls[i];
            String viewstate = "";
            String viewstategenerator = "";
            String eventvalidation = "";
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if(pageTemp>0 && page>pageTemp){
                        break;
                    }
                    if(pagelist>1){
                        conn.data("__EVENTTARGET","ctl00$ContentPlaceHolder1$Lb_Next");
                        conn.data("__EVENTARGUMENT","");
                        conn.data("__LASTFOCUS","");
                        conn.data("__VIEWSTATE",viewstate);
                        conn.data("__VIEWSTATEGENERATOR",viewstategenerator);
                        conn.data("__EVENTVALIDATION",eventvalidation);
                        conn.data("ctl00$ContentPlaceHolder1$ddlsz","");
                        conn.data("ctl00$ContentPlaceHolder1$gcmc","");
                        conn.data("ctl00$ContentPlaceHolder1$ddl_page","" + (pagelist-1));
                        doc = conn.post();
                    } else {
                        conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                        doc = conn.get();
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    if(page ==1){
                        //获取总页数
                        String pageCont = doc.select("select").last().select("option").last().text().trim();
                        page = Integer.valueOf(pageCont.substring(pageCont.indexOf("第")+1,pageCont.lastIndexOf("页")));
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }
                    viewstate = doc.select("#__VIEWSTATE").first().val();
                    viewstategenerator = doc.select("#__VIEWSTATEGENERATOR").first().val();
                    eventvalidation = doc.select("#__EVENTVALIDATION").first().val();
                    Elements trs = doc.select("#ctl00_ContentPlaceHolder1_td_gcxxlist").select("tr");
                    for (int row = 1; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");
                        if (SnatchUtils.isNotNull(conturl)) {
                            Notice notice = new Notice();
                            String title = trs.get(row).select("a").first().text().trim();
                            String date = trs.get(row).select("td").last().text().trim();
                            date = sdf.format(sdf.parse(date));
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            switch (i) {
                                case 0 :
                                    notice.setCatchType(ZHAO_BIAO_TYPE);
                                    break;
                                case 1 :
                                    notice.setCatchType(ZHONG_BIAO_TYPE);
                                    break;
                            }
                            notice.setNoticeType("工程建设");
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            page = detailHandle(notice,pagelist,page,row,trs.size());
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
    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception  {
        Document docCount = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        String content = docCount.select("textarea").first().parent().html();
        notice.setContent(content);
        return notice;
    }
}