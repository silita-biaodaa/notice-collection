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
import java.text.SimpleDateFormat;

import static com.snatch.common.SnatchContent.CITY;
import static com.snatch.common.SnatchContent.ZHONG_BIAO_TYPE;

/**
 * Created by maofeng on 2017/7/21.
 * 湘潭市  湘潭市公共资源交易中心
 * http://ggzy.xiangtan.gov.cn/zbhxrgs/index.jhtml    //工程建设 > 中标候选人公示
 * http://ggzy.xiangtan.gov.cn/jggg/index.jhtml   //政府采购 > 结果公告
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanXiangTanShigcjshx")
public class HuNanXiangTanShigcjshx extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://ggzy.xiangtan.gov.cn/zbhxrgs/index.jhtml",
                "http://ggzy.xiangtan.gov.cn/jggg/index.jhtml"
        };

        Connection conn = null;
        Document doc = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String dyUrl = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        dyUrl = urls[i].substring(0, urls[i].lastIndexOf("/")) + "/index_" + pagelist + ".jhtml";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(dyUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        String textStr = doc.select(".pagesite").select("div").text().trim();
                        String countPage = textStr.substring(textStr.indexOf("/") + 1, textStr.indexOf("页")).trim();
                        page = Integer.parseInt(countPage.trim());
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".text-list").select("li.tabletitle2");
                    for (int row = 0; row < trs.size(); row++) {
                        String href = trs.get(row).select("a").first().absUrl("href");
                        if (!"".equals(href)) {
                            String date = sdf.format(sdf.parse(trs.get(row).select("em").first().text().trim()));
                            String title = trs.get(row).select("a").first().attr("title").trim().replaceAll(" ","");
                            Notice notice = new Notice();
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("湘潭市");
                            notice.setCityCode("xt");
                            notice.setOpendate(date);
                            notice.setTitle(title);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setAreaRank(CITY);
                            notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                            if (SnatchUtils.isNull(notice.getCatchType())) {
                                notice.setCatchType(ZHONG_BIAO_TYPE);
                            } else {
                                SnatchUtils.judgeCatchType(notice,notice.getCatchType(),ZHONG_BIAO_TYPE);
                            }
                            switch (i) {
                                case 0:
                                    notice.setUrl(href + "?type=zb_zhongbiao");
                                    notice.setNoticeType("建设工程");
                                    break;
                                case 1:
                                    notice.setUrl(href + "?type=cg_zhongbiao");
                                    notice.setNoticeType("政府采购");
                                    break;
                            }
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
                            Elements eContent = docCount.select("#content");
                            String content = eContent.last().html();
                            eContent.select("li.pro_child").remove();
//                            notice.setTitle(title);
                            notice.setContent(content);
                            //详情信息入库，获取增量页数
                            if ("政府采购".equals(notice.getNoticeType())) {
                                page = govDetailHandle(notice, i, page, row, trs.size());
                            } else {
                                page = detailHandle(notice, i, page, row, trs.size());
                            }
                        }
                    }
                    //===入库代码====
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                urls = new String[]{
                        "http://ggzy.xiangtan.gov.cn/zbhxrgs/index.jhtml",
                        "http://ggzy.xiangtan.gov.cn/jggg/index.jhtml"
                };
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        /*Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        docCount.select("script").remove();
        String content = docCount.select("#content").html();
        notice.setContent(content);*/
        return notice;
    }
}
