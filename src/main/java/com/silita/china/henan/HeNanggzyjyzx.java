package com.silita.china.henan;

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

import static com.snatch.common.SnatchContent.*;

/**
 * Create by IntelliJ Idea 2018.1
 * Company: silita
 * Author: gemingyi
 * Date: 2018/8/15 10:06
 *
 * 河南省公共资源交易中心
 * http://www.hnggzy.com/hnsggzy/jyxx/002001/002001001/?Paging=1    工程建设/招标公告
 * http://www.hnggzy.com/hnsggzy/jyxx/002001/002001002/?Paging=1    工程建设/变更公告
 * http://www.hnggzy.com/hnsggzy/jyxx/002001/002001003/?Paging=1    工程建设/评标结果公示
 * http://www.hnggzy.com/hnsggzy/jyxx/002001/002001004/?Paging=1    工程建设/文件预公示
 * http://www.hnggzy.com/hnsggzy/jyxx/002001/002001005/?Paging=1    工程建设/异常公告
 * http://www.hnggzy.com/hnsggzy/jyxx/002002/002002001/?Paging=1    政府采购/采购公告
 * http://www.hnggzy.com/hnsggzy/jyxx/002002/002002002/?Paging=1    政府采购/变更公告
 * http://www.hnggzy.com/hnsggzy/jyxx/002002/002002003/?Paging=1    政府采购/结果公示
 * http://www.hnggzy.com/hnsggzy/jyxx/002002/002002004/?Paging=1    政府采购/其他公告
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HeNanggzyjyzx")
public class HeNanggzyjyzx extends BaseSnatch {

    private static final String source = "henan";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "http://www.hnggzy.com/hnsggzy/jyxx/002001/002001001/?Paging=1",
                "http://www.hnggzy.com/hnsggzy/jyxx/002001/002001002/?Paging=1",
                "http://www.hnggzy.com/hnsggzy/jyxx/002001/002001003/?Paging=1",
                "http://www.hnggzy.com/hnsggzy/jyxx/002001/002001004/?Paging=1",
                "http://www.hnggzy.com/hnsggzy/jyxx/002001/002001005/?Paging=1",
                "http://www.hnggzy.com/hnsggzy/jyxx/002002/002002001/?Paging=1",
                "http://www.hnggzy.com/hnsggzy/jyxx/002002/002002002/?Paging=1",
                "http://www.hnggzy.com/hnsggzy/jyxx/002002/002002003/?Paging=1",
                "http://www.hnggzy.com/hnsggzy/jyxx/002002/002002004/?Paging=1",
        };
        String[] catchTypes = {ZHAO_BIAO_TYPE, GENG_ZHENG_TYPE, ZHONG_BIAO_TYPE, PRE_QUALIFICATION, FEI_BIAO_TYPE, ZHAO_BIAO_TYPE, GENG_ZHENG_TYPE, ZHONG_BIAO_TYPE, OTHER_TYPE};

        Connection conn ;
        Document doc ;
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
                        url = urls[i].substring(0, urls[i].lastIndexOf("=") + 1) + pagelist;
                    }
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    SnatchLogger.debug("第" + pagelist + "页");
                    doc = conn.get();
                    if (page == 1) {
                        //获取总页数
                        String pageStr = doc.select("td .huifont").first().text();
                        page = Integer.valueOf(pageStr.substring(pageStr.indexOf("/") + 1));
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".bd-content1 table").first().select("tr");
                    for (int row = 0; row < trs.size(); row++) {
                        Notice notice = new Notice();
                        String detailUrl = trs.get(row).select("a").first().absUrl("href");
                        String title = trs.get(row).select("a").attr("title");
                        String publishDate = trs.get(row).select("[align=right]").text().replaceAll("\\[", "").replaceAll("\\]", "");
                        notice.setProvince("河南省");
                        notice.setProvinceCode("hens");
                        notice.setCatchType(catchTypes[i]);
                        if (i <= 4) {
                            notice.setNoticeType("工程建设");
                        } else {
                            notice.setNoticeType("政府采购");
                        }
                        notice.setUrl(detailUrl);
                        notice.setTitle(title);
                        notice.setOpendate(publishDate);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setAreaRank(PROVINCE);
                        notice.setSource(source);
                        CURRENT_PAGE_LAST_OPENDATE = publishDate;
                        //详情信息入库，获取增量页数
                        if ("政府采购".equals(notice.getNoticeType())) {
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
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document doc = Jsoup.parse(new URL(href).openStream(), "gb2312", href);
        String content = doc.select("td [style=padding:26px 40px 10px;]").first().html();
        notice.setContent(content);
        return notice;
    }
}
