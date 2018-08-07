package com.silita.china.guangxi;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
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
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Map;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by hujia on 2018/03/06
 * 广西壮族自治区招标投标综合网
 * http://www.gxzbtb.cn/gxzbw//showinfo/jyxx.aspx?QuYu=450001&categoryNum=001010001      // 铁路工程
 * http://www.gxzbtb.cn/gxzbw//showinfo/jyxx.aspx?QuYu=450001&categoryNum=001001001      // 房建市政
 * http://www.gxzbtb.cn/gxzbw//showinfo/jyxx.aspx?QuYu=450001&categoryNum=001004001      // 政府采购
 * http://www.gxzbtb.cn/gxzbw//showinfo/jyxx.aspx?QuYu=450001&categoryNum=001011001      // 水利工程
 * http://www.gxzbtb.cn/gxzbw//showinfo/jyxx.aspx?QuYu=450001&categoryNum=001012001      // 交通工程
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "GuangXiZhuangZuZiZhiQuggzyjyw")
public class GuangXiZhuangZuZiZhiQuggzyjyw extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String baseInterfaceUrl = "http://www.gxzbtb.cn/gxzbw//showinfo/MoreInfo.aspx?QuYu=450001&categoryNum=";
        String[] categoryNum = {
                "001010001", "001010002", "001010003", "001010004", "001010005",    //铁路工程
                "001001001", "001001002", "001001004", "001001005", //房建市政
                "001004001", "001004002", "001004004", "001004005", "001004006",    //政府采购
                "001011001", "001011002", "001011003", "001011004", "001011005",    //水利工程
                "001012001", "001012002", "001012003", "001012004", "001012005" //交通工程
        };
        String[] category = {
                ZHAO_BIAO_TYPE, CHENG_QING_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHONG_BIAO_TYPE,
                ZHAO_BIAO_TYPE, CHENG_QING_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE,
                ZHAO_BIAO_TYPE, CHENG_QING_TYPE, ZHONG_BIAO_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE,
                ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, CHENG_QING_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE,
                ZHAO_BIAO_TYPE, CHENG_QING_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHONG_BIAO_TYPE
        };

        Map<String, String> cookies = null;
        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < categoryNum.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = baseInterfaceUrl + categoryNum[i];
            String __CSRFTOKEN = null;
            String __VIEWSTATE = null;
            String __VIEWSTATEGENERATOR = null;
            String __EVENTVALIDATION = null;
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(url);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    conn.data("Upgrade-Insecure-Requests", "1");
                    if (i == 0 && pagelist == 1) {
                    } else {
                        conn.cookies(cookies);
                    }
                    if (pagelist == 1) {
                        doc = conn.get();
                        if (i == 0) {
                            cookies = conn.response().cookies();
                        }
                        String pageCont = doc.select("[color=red]").first().previousElementSibling().text();
                        page = Integer.valueOf(pageCont);
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    } else {
                        conn.data("__CSRFTOKEN", __CSRFTOKEN);
                        conn.data("__VIEWSTATE", __VIEWSTATE);
                        conn.data("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
                        conn.data("__EVENTTARGET", "MoreInfoList1$Pager");
                        conn.data("__EVENTARGUMENT", String.valueOf(pagelist));
                        conn.data("__VIEWSTATEENCRYPTED", "");
                        conn.data("__EVENTVALIDATION", __EVENTVALIDATION);
                        conn.data("MoreInfoList1$txtTitle", "");
                        doc = conn.post();
                    }
                    __CSRFTOKEN = doc.select("#__CSRFTOKEN").attr("value");
                    __VIEWSTATE = doc.select("#__VIEWSTATE").attr("value");
                    __VIEWSTATEGENERATOR = doc.select("#__VIEWSTATEGENERATOR").attr("value");
                    __EVENTVALIDATION = doc.select("#__EVENTVALIDATION").attr("value");
                    SnatchLogger.debug("第" + pagelist + "页");

                    Elements trs = doc.select("#MoreInfoList1_DataGrid1").first().select("tr");
                    for (int row = 1; row < trs.size(); row++) {
                        String href = trs.get(row).select("a").first().absUrl("href");
                        String title = trs.get(row).select("a").first().attr("title").trim();
                        String date = trs.get(row).select("td").last().text();
                        //===入库代码====
                        Notice notice = new Notice();
                        notice.setProvince("广西省");
                        notice.setProvinceCode("gxs");
                        notice.setAreaRank(PROVINCE);
                        notice.setSnatchNumber(snatchNumber);
                        notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                        if (StringUtils.isEmpty(notice.getCatchType())) {
                            notice.setCatchType(category[i]);
                        }
                        if (i >= 0 && i <= 4) {
                            notice.setNoticeType("铁路工程");
                        } else if (i >= 5 && i <= 8) {
                            notice.setNoticeType("房建市政");
                        } else if (i >= 9 && i <= 13) {
                            notice.setNoticeType("政府采购");
                        } else if (i >= 14 && i <= 18) {
                            notice.setNoticeType("水利工程");
                        } else {
                            notice.setNoticeType("交通工程");
                        }
                        notice.setUrl(href);
                        notice.setSource("guangx");
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
                super.saveAllPageIncrement(url);
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
        String content = docCount.select("#TDContent").html();
        content = content.length() < 20 ? docCount.select("#unitArticle_unitContent_trContent").html() : content;
        content = content.length() < 20 ? docCount.select("#Main_unitDisplay_unitContent_trContent").html() : content;
        notice.setContent(content);
        Elements relevant = docCount.select("#content").select("a");
        //相关公告
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < relevant.size(); i++) {
            Dimension dimension = new Dimension();
            if (i == relevant.size()) {
                sb.append(relevant.get(i).absUrl("href"));
            } else {
                sb.append(relevant.get(i).absUrl("href")).append(",");
            }
            dimension.setRelation_url(sb.toString());
            notice.setDimension(dimension);
        }
        return notice;
    }

}
