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

import static com.snatch.common.SnatchContent.*;


/**
 * Created by hujia on 2017/7/21.
 * 衡阳市 衡阳市公共资源交易中心
 * http://ggzy.hengyang.gov.cn/jyxx/jsgc/zbgg_64796/index.html  建设工程交易\招标公告
 * http://ggzy.hengyang.gov.cn/jyxx/jsgc/zbhxrgs_64798/index.html   建设工程交易\中标候选人
 * http://ggzy.hengyang.gov.cn/jyxx/jsgc/qtgg_64799/index.html  建设工程交易\其他公告
 * http://ggzy.hengyang.gov.cn/jyxx/zfcg/zbgg_64800/index.html  政府采购交易\招标公告
 * http://ggzy.hengyang.gov.cn/jyxx/zfcg/jggs/index.html    政府采购交易\结果公示
 * http://ggzy.hengyang.gov.cn/jyxx/zfcg/qtgg_64802/index.html  政府采购交易\其他公告
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})


@Component
@JobHander(value = "HuNanHengYanghyggzyjyw")
public class HuNanHengYanghyggzyjyw extends BaseSnatch {

    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    private void firstGetMaxId() throws Exception {
        String[] urls = {
                "http://ggzy.hengyang.gov.cn/jyxx/jsgc/zbgg_64796/index.html",
                "http://ggzy.hengyang.gov.cn/jyxx/jsgc/zbhxrgs_64798/index.html",
                "http://ggzy.hengyang.gov.cn/jyxx/jsgc/qtgg_64799/index.html",
                "http://ggzy.hengyang.gov.cn/jyxx/zfcg/zbgg_64800/index.html",
                "http://ggzy.hengyang.gov.cn/jyxx/zfcg/jggs/index.html",
                "http://ggzy.hengyang.gov.cn/jyxx/zfcg/qtgg_64802/index.html"
        };
        String[] catchTypes = {ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, OTHER_TYPE, ZHAO_BIAO_TYPE, ZI_SHENG_JIE_GUO_TYPE, OTHER_TYPE};
        Connection conn = null;
        Document doc = null;

        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 0; pagelist < page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].substring(0, urls[i].lastIndexOf("/")) + "/index_" + pagelist + ".html";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        String pageStr = doc.select(".clsPage").select("[type=text/javascript]").html();
                        String pageCount = pageStr.substring(pageStr.indexOf("//createPageHTML(") + 17, pageStr.indexOf(", 0,"));
                        page = Integer.parseInt(pageCount);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".contentText li");
                    for (int row = 0; row < trs.size(); row++) {
                        Notice notice = new Notice();
                        String detailUrl = trs.get(row).select("a").first().absUrl("href");
//                        String title = trs.get(row).select("a").text();
                        String publishDate = trs.get(row).select(".titleDate").text();
                        notice.setProvince("湖南省");
                        notice.setProvinceCode("huns");
                        notice.setCity("衡阳市");
                        notice.setCityCode("hy");
                        notice.setAreaRank(CITY);
                        notice.setCatchType(catchTypes[i]);
                        if (i < 3) {
                            notice.setNoticeType("工程建设");
                        } else {
                            notice.setNoticeType("政府采购");
                        }
                        notice.setUrl(detailUrl);
                        notice.setOpendate(publishDate);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setAreaRank(PROVINCE);
                        CURRENT_PAGE_LAST_OPENDATE = publishDate;
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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                super.clearClassParam();
            }
        }
    }

    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document doc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = doc.select(".finalTitle h3").html();
        String content = doc.select(".article").html();
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }

}
