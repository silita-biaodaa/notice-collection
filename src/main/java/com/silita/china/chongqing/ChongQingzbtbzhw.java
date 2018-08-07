package com.silita.china.chongqing;

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
import java.util.Map;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by hujia on 2018/02/28
 * 重庆市招标投标综合网
 * 招标信息
 * http://www.cqzb.gov.cn/class-5-1.aspx      // 招标公告
 * http://www.cqzb.gov.cn/class-5-45.aspx     // 中标公示
 * 比选信息
 * http://www.cqzb.gov.cn/class-6-2.aspx      // 比选公告
 * http://www.cqzb.gov.cn/class-6-46.aspx     // 比选公示
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "ChongQingztbzhw")
public class ChongQingzbtbzhw extends BaseSnatch {

    private static final String source = "chongq";
    Map<String, String> cookies = null;

    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String urls[] = {
                "http://www.cqzb.gov.cn/class-5-1.aspx",
                "http://www.cqzb.gov.cn/class-5-45.aspx",
                "http://www.cqzb.gov.cn/class-6-2.aspx",
                "http://www.cqzb.gov.cn/class-6-46.aspx"
        };

        String[] catchTypes = {ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE};
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
                        url = urls[i].replace(".aspx", "(" + pagelist + ").aspx");
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    if (i == 0 && pagelist == 1) {
                    } else {
                        conn.cookies(cookies);
                    }
                    doc = conn.get();
                    if (pagelist == 1) {
                        if (i == 0) {
                            cookies = conn.response().cookies();
                        }
                        String pageText = doc.select(".p_bar").select("td[style='white-space:nowrap;']").text();
                        page = Integer.parseInt(pageText.substring(pageText.indexOf("/") + 1, pageText.indexOf("页")));
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                        SnatchLogger.debug("总" + page + "页");
                    }
                    Elements trs = doc.select(".ztb_list_right").select("ul").first().select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String href = trs.get(row).select("a").first().absUrl("href");
                        String title = trs.get(row).select("a").first().text();
                        String date = trs.get(row).select("span").text().replace("(", "").replace(")", "");
                        //===入库代码====
                        Notice notice = new Notice();
                        notice.setProvince("重庆市");
                        notice.setProvinceCode("cqs");
                        notice.setAreaRank(PROVINCE);
                        notice.setSnatchNumber(snatchNumber);
                        notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                        if (SnatchUtils.isNull(notice.getCatchType())) {
                            notice.setCatchType(catchTypes[i]);
                        } else {
                            SnatchUtils.judgeCatchType(notice, notice.getCatchType(), catchTypes[i]);
                        }
                        if (title.contains("采购")) {
                            notice.setNoticeType("政府采购");
                        } else {
                            notice.setNoticeType("建设工程");
                        }
                        notice.setUrl(href);
                        notice.setSource(source);
                        notice.setOpendate(date);
                        CURRENT_PAGE_LAST_OPENDATE = date;
                        notice.setTitle(title);
                        //详情信息入库，获取增量页数
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
                }
                //===入库代码====
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
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = docCount.select("#ztb_zbxx1").html();
        if (content.length() < 1) {
            content = docCount.select("#ztb_zbxx2").html();
        }
        notice.setContent(content);
        return notice;
    }

}
