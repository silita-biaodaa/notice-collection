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

import static com.snatch.common.SnatchContent.*;


/**
 * Created by hujia on 2017/7/22.
 * 常德市 常德市公共资源交易网
 * http://ggzy.changde.gov.cn/cdweb/jyxx/004001/004001002/MoreInfo.aspx?CategoryNum=004001002	工程建设--中标公示
 * http://ggzy.changde.gov.cn/cdweb/jyxx/004002/004002002/MoreInfo.aspx?CategoryNum=004002002	政府采购--中标公示
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@JobHander(value = "HuNanChangDe1")
@Component
public class HuNanChangDe1 extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String[] urls = {
                "http://ggzy.changde.gov.cn/cdweb/jyxx/004001/004001002/MoreInfo.aspx?CategoryNum=004001002",
                "http://ggzy.changde.gov.cn/cdweb/jyxx/004002/004002002/MoreInfo.aspx?CategoryNum=004002002"
        };
        Document doc = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
                    Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    SnatchLogger.debug("第" + pagelist + "页");
                    String va = doc.select("#__VIEWSTATE").attr("value");
                    if(pagelist > 1) {
                        conn.data("__VIEWSTATE", va);
                        conn.data("__EVENTTARGET", "MoreInfoList1$Pager");
                        conn.data("__EVENTARGUMENT", String.valueOf(pagelist));//下一页
                        conn.cookies(conn.response().cookies());
                        doc = conn.post();
                    }
                    if (pagelist == 1) {
                        String item = doc.select("#MoreInfoList1_Pager").text();
                        String countPage = item.toString().substring(item.toString().indexOf("总页数：") + 4, item.toString().indexOf(" 当前页"));
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements html = doc.select("#MoreInfoList1_DataGrid1");
                    Elements trs = html.select("tr"); // 得到正文的一条
                    for (int k = 0; k < trs.size(); k++) {
                        Element tr = trs.get(k);
                        String href = tr.select("a").first().absUrl("href");
                        String date = sdf.format(sdf.parse(tr.select("td").last().text().trim()));
                        //===入库代码====
                        Notice notice = new Notice();
                        notice.setProvince("湖南省");
                        notice.setProvinceCode("huns");
                        notice.setCity("常德市");
                        notice.setCityCode("cd");
                        notice.setAreaRank(CITY);
                        notice.setSnatchNumber(snatchNumber);
                        switch (i) {
                            case 0:
                                notice.setNoticeType("工程建设");
                                break;
                            case 1:
                                notice.setNoticeType("政府采购");
                                break;
                        }
                        notice.setUrl(href);
                        notice.setOpendate(date);
                        CURRENT_PAGE_LAST_OPENDATE = date;
                        if ("政府采购".equals(notice.getNoticeType())) {
                            page = govDetailHandle(notice, pagelist, page, k, trs.size());
                        } else {
                            page = detailHandle(notice, pagelist, page, k, trs.size());
                        }
                        Thread.sleep(2000);
                    }
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            } finally {
                clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = docCount.select("#InfoDetail_lblTitle").text();
        String content = docCount.select("#InfoDetail_tblInfo").select("tr").get(2).html();
        if (title.contains("流标")) {
            notice.setCatchType(LIU_BIAO_TYPE);
        } else if (title.contains("废标")) {
            notice.setCatchType(FEI_BIAO_TYPE);
        } else if (title.contains("中标") && (title.contains("补充") || title.contains("更正") || title.contains("更改")
                ||title.contains("修改"))) {
            notice.setCatchType(ZHONG_BIAO_BU_CHONG_TYPE);
        } else {
            notice.setCatchType(ZHONG_BIAO_TYPE);
        }
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }

}
