package com.silita.china.liaoning;

import com.snatch.model.Notice;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
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
import java.text.SimpleDateFormat;
import java.util.Map;

import static com.snatch.common.SnatchContent.*;

/**
 * 辽宁招投标监管网
 * http://www.lntb.gov.cn/Article_Class2.asp?ClassID=1 辽宁省招标投标监管网 >> 招标公告
 * http://www.lntb.gov.cn/Article_Class2.asp?ClassID=31 辽宁省招标投标监管网 >> 招标资格预审公告
 * http://www.lntb.gov.cn/Article_Class2.asp?ClassID=7 辽宁省招标投标监管网 >> 中标侯选人公示
 * http://www.lntb.gov.cn/Article_Class2.asp?ClassID=3 辽宁省招标投标监管网 >> 中标侯选人公示
 * Created by 91567 on 2018/3/5.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "LiaoNingZhaoTouBiaojgw")
public class LiaoNingZhaoTouBiaojgw extends BaseSnatch {

    private static final String source = "liaon";
    Map<String, String> cookies = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://www.lntb.gov.cn/Article_Class2.asp?ClassID=1",
                "http://www.lntb.gov.cn/Article_Class2.asp?ClassID=31",
                "http://www.lntb.gov.cn/Article_Class2.asp?ClassID=7",
                "http://www.lntb.gov.cn/Article_Class2.asp?ClassID=3"
        };
        String[] catchType = {ZHAO_BIAO_TYPE, ZI_GE_YU_SHEN_TYPE, ZHONG_BIAO_TYPE, ZHONG_BIAO_TYPE};

        Connection conn;
        Document doc;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i] + "&SpecialID=0&page=" + pagelist;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(2000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    if (i == 0 && pagelist == 1) {
                    } else {
                        conn.cookies(cookies);
                    }
                    doc = conn.get();
                    if (page == 1) {
                        if (i == 0) {
                            cookies = conn.response().cookies();
                        }
                        String textStr = doc.select("[name=page]").first().previousElementSibling().previousElementSibling().text();
//                        String textStr = doc.select("font[color=red]").first().parent().text();
                        String countPage = textStr.substring(textStr.indexOf("/") + 1);
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }

                    Element div = doc.select("td[width=815][valign=top]").select("table").get(1);
                    Elements trs = div.select("td[height=200][valign=top]");
                    for (int row = 0; row < trs.select("br").size(); row++) {
                        String conturl = trs.select("[src=images/article_common.gif]").get(row).nextElementSibling().absUrl("href");
                        if (!"".equals(conturl)) {
                            String title = trs.select("[src=images/article_common.gif]").get(row).nextElementSibling().text();
                            String date = sdf.format(sdf.parse(trs.select("[src=images/article_common.gif]").get(row).nextElementSibling().nextElementSibling().text().replaceAll("[\\u4E00-\\u9FA5]", "-")));
                            Notice notice = new Notice();
                            notice.setProvince("辽宁省");
                            notice.setProvinceCode("lns");
                            notice.setUrl(conturl);
                            notice.setCatchType(catchType[i]);
                            notice.setNoticeType("工程建设");
                            if (title.contains("采购")) {
                                notice.setNoticeType("政府采购");
                            }
                            notice.setAreaRank("0");
                            notice.setSource(source);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            //详情信息入库，获取增量页数
                            if ("政府采购".equals(notice.getNoticeType())) {
                                page = govDetailHandle(notice, pagelist, page, row, trs.select("a").size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, trs.select("a").size());
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
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "gbk", href);
        String title = contdoc.select(".title_maintxt").get(0).text();
        String content = contdoc.select("[style=word-break:break-all;Width:fixed]").html();
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
