package com.silita.china.tianjin;

import com.snatch.model.Notice;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
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
import java.util.Map;import static com.snatch.common.SnatchContent.*;

/**
 * 天津公共资源交易中心
 * http://ggzy.xzsp.tj.gov.cn/queryContent-jyxx.jspx?title=&inDates=&ext=&ext1=&origin=&channelId=81&beginTime=&endTime= 工程建设 > 招标公告
 * http://ggzy.xzsp.tj.gov.cn/queryContent-jyxx.jspx?title=&inDates=&ext=&ext1=&origin=&channelId=83&beginTime=&endTime= 工程建设 > 中标结果公示
 * http://ggzy.xzsp.tj.gov.cn/queryContent-jyxx.jspx?title=&inDates=&ext=&ext1=&origin=&channelId=87&beginTime=&endTime= 政府采购 > 采购公告
 * http://ggzy.xzsp.tj.gov.cn/queryContent-jyxx.jspx?title=&inDates=&ext=&ext1=&origin=&channelId=90&beginTime=&endTime= 政府采购 > 更正公告
 * http://ggzy.xzsp.tj.gov.cn/queryContent-jyxx.jspx?title=&inDates=&ext=&ext1=&origin=&channelId=88&beginTime=&endTime= 政府采购 > 采购结果公告
 * Created by 91567 on 2018/3/1.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "TianjJinGongGongZiYuanjyzx")
public class TianjJinGongGongZiYuanjyzx extends BaseSnatch {

    private static final String source = "tianj";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Map<String, String> cookies = null;

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://ggzy.xzsp.tj.gov.cn/queryContent-jyxx.jspx?title=&inDates=&ext=&ext1=&origin=&channelId=81&beginTime=&endTime=",
                "http://ggzy.xzsp.tj.gov.cn/queryContent-jyxx.jspx?title=&inDates=&ext=&ext1=&origin=&channelId=83&beginTime=&endTime=",
                "http://ggzy.xzsp.tj.gov.cn/queryContent-jyxx.jspx?title=&inDates=&ext=&ext1=&origin=&channelId=87&beginTime=&endTime=",
                "http://ggzy.xzsp.tj.gov.cn/queryContent-jyxx.jspx?title=&inDates=&ext=&ext1=&origin=&channelId=90&beginTime=&endTime=",
                "http://ggzy.xzsp.tj.gov.cn/queryContent-jyxx.jspx?title=&inDates=&ext=&ext1=&origin=&channelId=88&beginTime=&endTime="
        };
        String[] catchType = {ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHAO_BIAO_TYPE, GENG_ZHENG_TYPE, ZHONG_BIAO_TYPE};

        Connection conn ;
        Document doc ;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);//查询url前次抓取的情况（最大页数与公示时间）
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].replace("queryContent-jyxx", "queryContent_" + pagelist + "-jyxx");
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    if(i == 0 && pagelist == 1) {
                    } else {
                        conn.cookies(cookies);
                    }
                    doc = conn.get();
                    if (page == 1) {
                        if(i == 0) {
                            cookies = conn.response().cookies();
                        }
                        String textStr = doc.select(".pages-list").select("li").first().text().trim();
                        String countPage = textStr.substring(textStr.indexOf("/") + 1, textStr.indexOf("页"));
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements div = doc.select(".article-list2");
                    Elements trs = div.select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").attr("href");
                        if (!"".equals(conturl)) {
                            String title = trs.get(row).select("a").text();
                            String date = sdf.format(sdf.parse(trs.get(row).select(".list-times").text()));
                            Notice notice = new Notice();
                            notice.setProvince("天津");
                            notice.setProvinceCode("tj");
                            notice.setUrl(conturl);
                            if(i <= 1) {
                                notice.setNoticeType("工程建设");
                            }else {
                                notice.setNoticeType("政府采购");
                            }
                            notice.setCatchType(catchType[i]);
                            if(title.contains("(延")) {
                                notice.setCatchType(YAN_QI_TYPE);
                            }
                            notice.setAreaRank("0");
                            notice.setSource(source);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            //详情信息入库，获取增量页数
                            if ("政府采购".equals(notice.getNoticeType())) {
                                page = govDetailHandle(notice, pagelist, page, row, trs.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, trs.size());
                            }
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
                clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = contdoc.select(".content-title").text();
        if(title.indexOf("(") == 0 ||  title.indexOf("（") == 0) {
            title = title.substring(4);
        }
        contdoc.select("#content").select(".share_box").remove();
        contdoc.select("#content").select(".jiathis_style").remove();
        contdoc.select("#content").select(".prev").remove();
        contdoc.select("#content").select(".next").remove();
        contdoc.select("#content").select(".dzan").remove();
        String content = contdoc.select("#content").last().html();
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
