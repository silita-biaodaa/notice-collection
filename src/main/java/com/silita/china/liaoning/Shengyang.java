package com.silita.china.liaoning;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
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

import java.text.SimpleDateFormat;import static com.snatch.common.SnatchContent.*;

/**
 * 沈北新区测试
 * http://www.syjy.gov.cn/NoticeTabQx/Tab_List
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "Shengyang")
public class Shengyang extends BaseSnatch {

    private static final String source = "liaon";

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://www.syjy.gov.cn/NoticeTabQx/Tab_Jsgc_tab1_Qx"
        };
        Connection conn;
        Document doc;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            try {
                //===入库代码====
                super.queryBeforeSnatchState(url);//查询url前次抓取的情况（最大页数与公示时间）
                for(int pagelist=1;pagelist<=page;pagelist++){
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i]+"?page="+pagelist;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(2000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        String textStr = doc.select("#page_string").text();
                        textStr = textStr.substring(textStr.indexOf("["),textStr.indexOf("]"));
                        textStr =textStr.substring(textStr.indexOf("/")+1);
//                        String textStr = doc.select("font[color=red]").first().parent().text();
                        String countPage = textStr;
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".list_mb_lista");

                    for (int row = 0; row < trs.size(); row++) {
                        Element tr = trs.get(row);
                        String title =tr.select(".list_mb_list_aa").text();
                        if(title.indexOf("沈北新区")!=-1) {
                            String date = tr.select(".list_mb_list_ba").text();
                            System.out.println(title);
                            String conturl =tr.select(".list_mb_list_aa").select("a").attr("abs:href");
                            Notice notice = new Notice();
                            notice.setProvince("辽宁省");
                            notice.setProvinceCode("lns");
                            notice.setUrl(conturl);
                            notice.setCatchType(ZHAO_BIAO_TYPE);
                            notice.setNoticeType("工程建设");
                            notice.setSource(source);
                            notice.setOpendate(date);
                            notice.setTitle(title);
                            //详情信息入库，获取增量页数
                            page =detailHandle(notice,pagelist,page,row,trs.size());
                        }else{
                            continue;
                        }
                    }
                    //===入库代码====
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn = null;
                doc = null;
                clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
//        Document contdoc = Jsoup.parse(new URL(href).openStream(), "gb2312", href);
//        String title = contdoc.select(".title_maintxt").get(0).text();
//        String content = contdoc.select("[style=word-break:break-all;Width:fixed]").html();
//        notice.setTitle(title);
        notice.setContent("test");
        return notice;
    }
}
