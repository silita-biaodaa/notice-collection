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
import java.util.Map;

import static com.snatch.common.SnatchContent.*;

/**
 * http://app.huaihua.gov.cn/hhggzyjyzx/27595/27596/27597/index.jsp?pager.offset=0&pager.desc=false 工程建设 > 招标公告
 * http://app.huaihua.gov.cn/hhggzyjyzx/27595/27596/27598/index.jsp?pager.offset=0&pager.desc=false 工程建设 > 中标公示
 * http://ggzy.huaihua.gov.cn/27595/27596/27599/index.htm 工程建设 > 更正公告
 * http://ggzy.huaihua.gov.cn/27595/27596/27600/index.htm 工程建设 > 流标公告
 * http://ggzy.huaihua.gov.cn/27595/27601/27602/index.htm 政府采购 > 采购公告
 * http://app.huaihua.gov.cn/hhggzyjyzx/27595/27601/27603/index.jsp?pager.offset=0&pager.desc=false 政府采购 > 中标公告
 * http://app.huaihua.gov.cn/hhggzyjyzx/27595/27601/27604/index.jsp?pager.offset=0&pager.desc=false 政府采购 > 更正公告
 * http://ggzy.huaihua.gov.cn/27595/27601/27605/index.htm 政府采购 > 流标公告
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanHuaiHuaShiggzyjyzx")
public class HuNanHuaiHuaShiggzyjyzx extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String[] urls = {
                "http://app.huaihua.gov.cn/hhggzyjyzx/27595/27596/27597/index.jsp?pager.offset=0&pager.desc=false",
                "http://app.huaihua.gov.cn/hhggzyjyzx/27595/27596/27598/index.jsp?pager.offset=0&pager.desc=false",
                "http://app.huaihua.gov.cn/hhggzyjyzx/27595/27596/27599/index.jsp?pager.offset=0&pager.desc=false",
                "http://ggzy.huaihua.gov.cn/27595/27596/27600/index.htm",
                "http://ggzy.huaihua.gov.cn/27595/27601/27602/index.htm",
                "http://app.huaihua.gov.cn/hhggzyjyzx/27595/27601/27603/index.jsp?pager.offset=0&pager.desc=false",
                "http://app.huaihua.gov.cn/hhggzyjyzx/27595/27601/27604/index.jsp?pager.offset=0&pager.desc=false",
                "http://ggzy.huaihua.gov.cn/27595/27601/27605/index.htm"
        };
        String[] catchType = {ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, GENG_ZHENG_TYPE, LIU_BIAO_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, GENG_ZHENG_TYPE, LIU_BIAO_TYPE};
        Map<String, String> cookies = null;

        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        if (i == 0 || i == 1 || i == 2 || i == 5 || i == 6) {
                            url = urls[i].substring(0, urls[i].indexOf("=") + 1) + (pagelist - 1) * 12 + "&pager.desc=false";
                        } else {
                            url = urls[i].substring(0, urls[i].lastIndexOf("/")) + "/index_" + (pagelist - 1) + ".htm";
                        }
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    if(cookies != null) {
                        conn.cookies(cookies);
                    }
                    doc = conn.get();
                    if (page == 1) {
                        if (i == 0) {
                            cookies = conn.response().cookies();
                        }
                        String pageCont = doc.select(".pager").select("li").last().select("a").first().attr("href");
                        if (i == 0 || i == 1 || i == 2 || i == 5 || i == 6) {
                            page = Integer.parseInt(pageCont.substring(pageCont.indexOf("=") + 1, pageCont.indexOf("&"))) / 12;
                        } else {
                            page = Integer.parseInt(pageCont.substring(pageCont.indexOf("_") + 1, pageCont.indexOf("."))) + 1;
                        }
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }

                    Elements trs = doc.select(".lbcc-nr").select("ul").select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        Notice notice = new Notice();
                        String detailUrl = trs.get(row).select("a").first().absUrl("href");
//                        String title = trs.get(row).select("a").first().attr("title");
                        String publishDate = trs.get(row).select("span").last().text().trim();
                        if (i < 5) {
                            notice.setNoticeType("工程建设");
                        } else {
                            notice.setNoticeType("政府采购");
                        }
                        notice.setCatchType(catchType[i]);
                        notice.setProvince("湖南省");
                        notice.setProvinceCode("huns");
                        notice.setCity("怀化市");
                        notice.setCityCode("hh");
                        notice.setAreaRank(CITY);
                        notice.setUrl(detailUrl);
                        notice.setOpendate(publishDate);
                        CURRENT_PAGE_LAST_OPENDATE = publishDate;
                        if (notice.getNoticeType().contains("采购")) {
                            page = govDetailHandle(notice, pagelist, page, row, trs.size());
                        } else {
                            page = detailHandle(notice, pagelist, page, row, trs.size());
                        }
                    }
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                super.saveAllPageIncrement(urls[i]);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "gb2312", href);
        String title = docCount.select(".xlnk").select("h2").first().text().replaceAll(" ", "");
        notice.setTitle(title);
        docCount.select("#div_div").remove();
        String content = docCount.select(".xl-xqnr").html();
        if (content.length() < 10) {
            content = docCount.select(".div-article2").html();
        }
        notice.setContent(content);
        return notice;
    }
}
