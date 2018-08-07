package com.silita.china.shanxi;

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
 * Created by hujia on 2018/03/02
 * 陕西省采购与招标网
 * http://www.sxggzyjy.cn/jydt/001001/001001001/001001001001/1.html  招标/资审公告
 * http://www.sxggzyjy.cn/jydt/001001/001001001/001001001002/1.html  澄清/变更公告
 * http://www.sxggzyjy.cn/jydt/001001/001001001/001001001004/1.html  流标/终止公告
 * http://www.sxggzyjy.cn/jydt/001001/001001001/001001001005/1.html  中标候选人公示
 * http://www.sxggzyjy.cn/jydt/001001/001001001/001001001003/1.html  中标/成交公示
 * http://www.sxggzyjy.cn/jydt/001001/001001004/001001004001/1.html  采购公告
 * http://www.sxggzyjy.cn/jydt/001001/001001004/001001004002/1.html  澄清/变更公告
 * http://www.sxggzyjy.cn/jydt/001001/001001004/001001004003/1.html  中标/成交公示
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "ShanXizgyzbw")
public class ShanXizgyzbw extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {

        String urls[] = {
                "http://www.sxggzyjy.cn/jydt/001001/001001001/001001001001/1.html",
                "http://www.sxggzyjy.cn/jydt/001001/001001001/001001001002/1.html",
                "http://www.sxggzyjy.cn/jydt/001001/001001001/001001001004/1.html",
                "http://www.sxggzyjy.cn/jydt/001001/001001001/001001001005/1.html",
                "http://www.sxggzyjy.cn/jydt/001001/001001001/001001001003/1.html",
                "http://www.sxggzyjy.cn/jydt/001001/001001004/001001004001/1.html",
                "http://www.sxggzyjy.cn/jydt/001001/001001004/001001004002/1.html",
                "http://www.sxggzyjy.cn/jydt/001001/001001004/001001004003/1.html",
        };

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
                        url = urls[i].substring(0, urls[i].lastIndexOf("/") + 1) + pagelist + ".html";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (pagelist == 1) {
                        String pageCountStr = doc.select("#index").text();
                        page = Integer.parseInt(pageCountStr.substring(pageCountStr.indexOf("/")+1, pageCountStr.length()));
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                        SnatchLogger.debug("总" + page + "页");
                    }

                    Elements trs = doc.select("#categorypagingcontent .ewb-list").select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String href = trs.get(row).select("a").attr("abs:href");
                        String title = trs.get(row).select("a").text();
                        String date = trs.get(row).select("span").text();
                        //===入库代码====
                        Notice notice = new Notice();
                        notice.setProvince("陕西省");
                        notice.setProvinceCode("sxs");
                        notice.setAreaRank(PROVINCE);
                        notice.setSnatchNumber(snatchNumber);
                        if(i <= 4) {
                            notice.setNoticeType("建设工程");
                        } else {
                            notice.setNoticeType("政府采购");
                        }
                        switch (i) {
                            case 0 :
                                notice.setCatchType(ZHAO_BIAO_TYPE);
                                break;
                            case 1 :
                                notice.setCatchType(GENG_ZHENG_TYPE);
                                break;
                            case 2 :
                                notice.setCatchType(ZHONG_ZHI_TYPE);
                                break;
                            case 3 :
                                notice.setCatchType(ZHONG_BIAO_TYPE);
                                break;
                            case 4 :
                                notice.setCatchType(ZHONG_BIAO_TYPE);
                                break;
                            case 5 :
                                notice.setCatchType(ZHAO_BIAO_TYPE);
                                break;
                            case 6 :
                                notice.setCatchType(GENG_ZHENG_TYPE);
                                break;
                            case 7 :
                                notice.setCatchType(ZHONG_BIAO_TYPE);
                                break;
                        }
                        notice.setUrl(href);
                        notice.setSource("shanxi");
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
                if (e instanceof InterruptedException) {
                    throw e;
                }
                e.printStackTrace();
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = docCount.select("#mainContent").html();
        notice.setContent(content);
        return notice;
    }
}
