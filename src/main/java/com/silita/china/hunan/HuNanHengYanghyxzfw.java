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

/**
 * 衡阳市衡阳县人民政府网  http://www.hyx.gov.cn/
 * http://www.hyx.gov.cn/list.ashx?categoryid=541   招标采购
 * Created by maofeng on 2017/11/30.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})
@Component
@JobHander(value = "HuNanHengYanghyxzfw")
public class HuNanHengYanghyxzfw extends BaseSnatch {

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    Connection conn = null;
    Document doc = null;
    public void snatchTask() throws Exception {
        int page = 1;
        int pageTemp = 0;
        String staticUrl = "http://www.hyx.gov.cn/list.ashx?page=1&categoryId=541";
        String dyUrl = staticUrl;
        super.queryBeforeSnatchState(staticUrl);
        for (int pagelist = 1; pagelist <= page; pagelist++) {
            if (pageTemp > 0 && page > pageTemp) {
                break;
            }
            if (pagelist > 1) {
                dyUrl = staticUrl.replace("page=1", "page=" + pagelist);
            }
            SnatchLogger.debug("第" + pagelist + "页");
            conn = Jsoup.connect(dyUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
            doc = conn.get();
            if (page == 1) {
                //获取总页数
                String pageCont = doc.select("#page_bar").first().select("strong").last().text().trim();
                page = Integer.valueOf(pageCont);
                pageTemp = page;
                SnatchLogger.debug("总" + page + "页");
                page = computeTotalPage(page, LAST_ALLPAGE);
            }
            doc.select("li.line").remove();
            Elements trs = doc.select(".comlist01").first().select("li");
            for (int row = 0; row < trs.size(); row++) {
                String conturl = trs.get(row).select("a").first().absUrl("href");
                if (SnatchUtils.isNotNull(conturl)) {
                    Notice notice = new Notice();
                    String title = trs.get(row).select("a").first().attr("title");
                    String date = trs.get(row).select("span").first().text().trim().replace("(", "").replace(")", "");
                    notice.setProvince("湖南省");
                    notice.setProvinceCode("huns");
                    notice.setCity("衡阳市");
                    notice.setCityCode("hy");
                    notice.setCounty("衡阳县");
                    notice.setCountyCode("hyx");
                    notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                    if (SnatchUtils.isNull(notice.getCatchType())) {
                        continue;
                    }
                    switch (notice.getCatchType()) {
                        case "1":
                            notice.setNoticeType("招标公告");
                            if (title.contains("采购")) {
                                notice.setNoticeType("政府采购");
                            }
                            break;
                        case "2":
                            notice.setNoticeType("中标公告");
                            break;
                        case "0":
                            notice.setNoticeType("其他公告");
                            break;
                    }
                    notice.setUrl(conturl);
                    notice.setOpendate(date);
                    CURRENT_PAGE_LAST_OPENDATE = date;
                    if (notice.getNoticeType().contains("采购")) {
                        page = govDetailHandle(notice, pagelist, page, row, trs.size());
                    } else {
                        page = detailHandle(notice, pagelist, page, row, trs.size());
                    }
                }
            }
            if (pagelist == page) {
                page = turnPageEstimate(page);
            }
        }
        super.saveAllPageIncrement(staticUrl);
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document detailDoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = detailDoc.select("h2").first().text().trim();
        String content = detailDoc.select("#div_content").first().html();
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
