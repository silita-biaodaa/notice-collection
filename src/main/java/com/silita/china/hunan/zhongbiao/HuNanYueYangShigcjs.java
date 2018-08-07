package com.silita.china.hunan.zhongbiao;

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
import java.text.SimpleDateFormat;

import static com.snatch.common.SnatchContent.CITY;
import static com.snatch.common.SnatchContent.DL_ZHONG_BIAO_TYPE;

/**
 * Created by maofeng on 2017/7/20.
 * 岳阳市  岳阳市公共资源交易中心网   http://ggzy.yueyang.gov.cn/
 * http://ggzy.yueyang.gov.cn/004/004001/004001003/about-gcjs.html  工程建设--中标公告
 * http://ggzy.yueyang.gov.cn/004/004002/004002003/about-zfcg.html  政府采购--中标公告
 * http://ggzy.yueyang.gov.cn/004/004001/004001005/about-gcjs.html  工程建设 > 中介代理机构比选
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanYueYangShigcjs")
public class HuNanYueYangShigcjs extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://ggzy.yueyang.gov.cn/004/004001/004001003/about-gcjs.html",
                "http://ggzy.yueyang.gov.cn/004/004002/004002003/about-zfcg.html",
                "http://ggzy.yueyang.gov.cn/004/004001/004001005/about-gcjs.html"
        };
        String[] catchTypes = {"2","2",DL_ZHONG_BIAO_TYPE};
        Document doc = null;
        Connection conn = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].substring(0, urls[i].lastIndexOf("/") + 1) + pagelist + ".html";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        Element item = doc.select(".fengye").first().select("li").last();
                        String type = item.select("a").attr("href");
                        String countPage = type.substring(type.lastIndexOf("/") + 1, type.indexOf(".html"));
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Element ul = doc.select(".erjitongzhilist").select("ul").first();
                    Elements lis = ul.select("li");
                    for (int row = 0; row < lis.size(); row++) {
                        String conturl = lis.get(row).select("a").first().absUrl("href");
                        if (!"".equals(conturl)) {
                            String date = sdf.format(sdf.parse(lis.get(row).select("span").text()));
                            Notice notice = new Notice();
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("岳阳市");
                            notice.setCityCode("yy");

                            Document contdoc = Jsoup.parse(new URL(conturl).openStream(), "utf-8", conturl);
                            contdoc.select("script").remove();
                            String content = contdoc.select(".xiangxiyekuang").first().html();             //得到中标详细内容区域
                            notice.setContent(content);
                            String title = contdoc.select(".xiangxiyebiaoti").first().text().trim();
                            notice.setTitle(title);

                            notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                            if (SnatchUtils.isNull(notice.getCatchType())) {
                                notice.setCatchType(catchTypes[i]);
                            } else {
                                SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchTypes[i]);
                            }
                            notice.setAreaRank(CITY);
                            notice.setSnatchNumber(snatchNumber);

                            if (i == 1) {
                                notice.setNoticeType("政府采购");
                            } else {
                                notice.setNoticeType("工程建设");
                            }
                            notice.setUrl(conturl);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            if ("政府采购".equals(notice.getNoticeType())) {
                                page = govDetailHandle(notice, pagelist, page, row, lis.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, lis.size());
                            }
                        }
                    }
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                urls = new String[]{
                        "http://ggzy.yueyang.gov.cn/004/004001/004001003/about-gcjs.html",
                        "http://ggzy.yueyang.gov.cn/004/004002/004002003/about-zfcg.html",
                        "http://ggzy.yueyang.gov.cn/004/004001/004001005/about-gcjs.html"
                };
                super.saveAllPageIncrement(urls[i]);
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        return notice;
    }
}
