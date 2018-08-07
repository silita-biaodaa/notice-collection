package com.silita.china.hunan.zhongbiao;

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

import static com.snatch.common.SnatchContent.CITY;
import static com.snatch.common.SnatchContent.ZHONG_BIAO_TYPE;

/**
 * Created by hujia on 2017/7/19.
 * 娄底市 娄底市公共资源交易中心
 * http://ldggzy.hnloudi.gov.cn/jyxx/gcjsjyxx/zbgs/  交易信息 >> 工程建设交易信息 >> 中标（废标）公示
 * http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/zbgs_11959/   交易信息 >> 政府采购交易信息 >> 中标（废标）公示
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanLouDi")
public class HuNanLouDi extends BaseSnatch {

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {

        String[] urls = {
                "http://ldggzy.hnloudi.gov.cn/jyxx/gcjsjyxx/zbgs/",
                "http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/zbgs_11959/"
        };
        Map cookies = null;

        Connection conn = null;
        Document doc = null;
        try {
            for (int i = 0; i < urls.length; i++) {
                int page = 1;
                int pageTemp = 0;
                String url = urls[i];
                String snatchNumber = SnatchUtils.makeSnatchNumber();
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i] + "index_" + (pagelist - 1) + ".html";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    conn.header("Upgrade-Insecure-Requests", "1");
                    if(i == 0 && pagelist == 1) {
                        conn.header("X-DevTools-Emulate-Network-Conditions-Client-Id", "59B419EB48287DFA862413F5748D3342");
                    } else {
                        conn.cookies(cookies);
                    }

                    doc = conn.get();
                    cookies = conn.response().cookies();
                    if (page == 1) {
                        //获取总页数
                        String type = doc.select(".page").html();
                        String count = type.substring(type.indexOf("(") + 1, type.indexOf(","));
                        page = Integer.parseInt(count);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".newsList").select("ul>li");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").attr("href").substring(1);
                        if (!"".equals(conturl)) {
                            conturl = trs.get(row).select("a").first().absUrl("href");
                            String title = trs.get(row).select("a").first().attr("title").replaceAll(" ", "");
                            String date = trs.get(row).select("span").text().trim();
                            Notice notice = new Notice();
                            if (i == 0) {
                                notice.setNoticeType("工程建设");
                            } else {
                                notice.setNoticeType("政府采购");
                            }
                            notice.setCatchType(ZHONG_BIAO_TYPE);
                            notice.setAreaRank(CITY);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("娄底市");
                            notice.setCityCode("ld");
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            if (notice.getNoticeType().contains("采购")) {
                                page = detailHandle(notice, pagelist, page, row, trs.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, trs.size());
                            }
                        }
                        Thread.sleep(1500);
                    }
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                super.saveAllPageIncrement(urls[i]);
            }
        } finally {
            super.clearClassParam();
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
//        String title = contdoc.select(".contentShow").select("h1").first().text().trim().replaceAll(" ", "");
        String content = contdoc.select("#fontzoom").html();
//        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }


}
