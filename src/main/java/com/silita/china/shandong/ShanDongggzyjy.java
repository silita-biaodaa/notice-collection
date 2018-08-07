package com.silita.china.shandong;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
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

import java.net.URL;

import static com.snatch.common.SnatchContent.*;

/**
 * 山东省公共资源交易中心  http://www.sdsggzyjyzx.gov.cn/
 * http://www.sdsggzyjyzx.gov.cn/jyxx/069001/069001001/about.html    招标公告--工程建设
 * http://www.sdsggzyjyzx.gov.cn/jyxx/069001/069001002/about.html    工程建设 > 中标公示
 * http://www.sdsggzyjyzx.gov.cn/jyxx/069002/069002002/about.html    政府采购 > 采购公告
 * http://www.sdsggzyjyzx.gov.cn/jyxx/069002/069002003/about.html    政府采购 > 更正公告
 * http://www.sdsggzyjyzx.gov.cn/jyxx/069002/069002004/about.html    政府采购 > 成交公示
 * Created by maofeng on 2018/3/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "ShanDongggzyjy")
public class ShanDongggzyjy extends BaseSnatch {

    private static final String source = "shand";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "http://www.sdsggzyjyzx.gov.cn/jyxx/069001/069001001/1.html",
                "http://www.sdsggzyjyzx.gov.cn/jyxx/069001/069001002/1.html",
                "http://www.sdsggzyjyzx.gov.cn/jyxx/069002/069002002/1.html",
                "http://www.sdsggzyjyzx.gov.cn/jyxx/069002/069002003/1.html",
                "http://www.sdsggzyjyzx.gov.cn/jyxx/069002/069002004/1.html"
        };
        String[] catchTypes = {ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHAO_BIAO_TYPE, GENG_ZHENG_TYPE, ZHONG_BIAO_TYPE};

        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].replace("1.html", pagelist + ".html");
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        if (doc.select("#index").first() == null) {
                            page = 1;
                        } else {
                            String pageCont = doc.select("#index").first().text();
                            page = Integer.valueOf(pageCont.substring(pageCont.lastIndexOf("/") + 1, pageCont.length()));
                        }
                        SnatchLogger.debug("总" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }

                    Elements trs = doc.select(".ewb-info-items").first().select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");
                        String title = trs.get(row).select("a").first().attr("title");

                        Notice notice = new Notice();
                        notice.setProvince("山东省");
                        notice.setProvinceCode("shds");

                        notice.setCatchType(catchTypes[i]);
                        if (i <= 1) {
                            notice.setNoticeType("工程建设");
                        } else {
                            notice.setNoticeType("政府采购");
                        }
                        notice.setUrl(conturl);
                        notice.setTitle(title);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setAreaRank(PROVINCE);
                        notice.setSource(source);
                        if (notice.getNoticeType().contains("采购")) {
                            page = govDetailHandle(notice, pagelist, page, row, trs.size());
                        } else {
                            page = detailHandle(notice, pagelist, page, row, trs.size());
                        }
                    }
                    //===入库代码====
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                    Thread.sleep(2000);
                }
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            } catch (Exception e) {
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document doc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String articleInfoStr = doc.select(".news-article-info").first().text();
        String publishDate = articleInfoStr.substring(articleInfoStr.indexOf("发稿时间") + 6, articleInfoStr.indexOf("作者")).replaceAll(" ", "");
        notice.setOpendate(publishDate);
        CURRENT_PAGE_LAST_OPENDATE = publishDate;
        Element contentEle = doc.select(".news-detail-wrap").first();
        String filePath = contentEle.select("#souceinfoid").first().previousElementSibling().absUrl("href");
        String content = SnatchUtils.readWord(filePath);
        notice.setContent(content);
        Thread.sleep(1000);
        return notice;
    }
}
