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
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Map;

import static com.snatch.common.SnatchContent.*;


/**
 * http://ggzy.changde.gov.cn/cdweb/showinfo/MoreInfoJSGC.aspx?CategoryNum=004001001    工程建设--招标公告
 * http://ggzy.changde.gov.cn/cdweb/jyxx/004001/004001002/MoreInfo.aspx?CategoryNum=004001002    工程建设--中标公告
 * http://ggzy.changde.gov.cn/cdweb/jyxx/004001/004001003/MoreInfo.aspx?CategoryNum=004001003    工程建设--变更公告
 * http://ggzy.changde.gov.cn/cdweb/showinfo/MoreInfoJSGC.aspx?CategoryNum=004002001    政府采购--招标公告
 * http://ggzy.changde.gov.cn/cdweb/jyxx/004002/004002002/MoreInfo.aspx?CategoryNum=004002002    政府采购--中标公告
 * http://ggzy.changde.gov.cn/cdweb/jyxx/004002/004002003/MoreInfo.aspx?CategoryNum=004002003    政府采购--变更公告
 * 常德市公共资源交易网
 *
 * @author zk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "HuNanChangDeShiggzyjyw")
public class HuNanChangDeShiggzyjyw extends BaseSnatch {

    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String[] urls = {
                "http://ggzy.changde.gov.cn/cdweb/showinfo/MoreInfoJSGC.aspx?CategoryNum=004001001",
                "http://ggzy.changde.gov.cn/cdweb/jyxx/004001/004001002/MoreInfo.aspx?CategoryNum=004001002",
                "http://ggzy.changde.gov.cn/cdweb/jyxx/004001/004001003/MoreInfo.aspx?CategoryNum=004001003",
                "http://ggzy.changde.gov.cn/cdweb/showinfo/MoreInfoJSGC.aspx?CategoryNum=004002001",
                "http://ggzy.changde.gov.cn/cdweb/jyxx/004002/004002002/MoreInfo.aspx?CategoryNum=004002002",
                "http://ggzy.changde.gov.cn/cdweb/jyxx/004002/004002003/MoreInfo.aspx?CategoryNum=004002003"
        };
        String[] catchTypes = {ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, GENG_ZHENG_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, GENG_ZHENG_TYPE};
        Map<String, String> cookies = null;

        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String __VIEWSTATE = null;
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true).followRedirects(true);
                    if (i == 0 && pagelist == 1) {
                    } else {
                        conn.cookies(cookies);
                    }
                    if (pagelist == 1) {
                        doc = conn.get();
                        if (i == 0) {
                            cookies = conn.response().cookies();
                        }
                        String pageStr = doc.select("font").select("[color=blue]").first().text();
                        Integer countPage = Integer.parseInt(pageStr);
                        page = countPage % 20 == 0 ? countPage / 20 : countPage / 20 + 1;
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    } else {
                        conn.data("__VIEWSTATE", __VIEWSTATE);
                        conn.data("__EVENTTARGET", "MoreinfoList1$Pager");
                        conn.data("__EVENTARGUMENT", String.valueOf(pagelist));
                        doc = conn.post();
                    }
                    __VIEWSTATE = doc.select("#__VIEWSTATE").attr("value");
                    SnatchLogger.debug("第" + pagelist + "页");

                    Elements trs = doc.select("#MoreinfoList1_DataGrid1").select("tr");
                    if(trs.size() == 0) {
                        trs = doc.select("#MoreInfoList1_tdcontent").select("#MoreInfoList1_DataGrid1").select("tr");
                    }
                    for (int k = 0; k < trs.size(); k++) {
                        String hrefEle = trs.get(k).select("a").text();
                        if(!StringUtils.isEmpty(hrefEle)) {
                            Notice notice = new Notice();
                            String title = trs.get(k).select("a").attr("title").trim();
                            String href = trs.get(k).select("a").first().absUrl("href");
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("常德市");
                            notice.setCityCode("cd");
                            notice.setAreaRank(CITY);
                            notice.setSnatchNumber(snatchNumber);
                            if (i <= 3) {
                                notice.setNoticeType("工程建设");
                            } else {
                                notice.setNoticeType("政府采购");
                            }
                            notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                            if (SnatchUtils.isNull(notice.getCatchType())) {
                                notice.setCatchType(catchTypes[i]);
                            } else {
                                SnatchUtils.judgeCatchType(notice, notice.getCatchType(), catchTypes[i]);
                            }
                            notice.setTitle(title);
                            notice.setUrl(href);
                            //详情信息入库，获取增量页数
                            if ("政府采购".equals(notice.getNoticeType())) {
                                page = govDetailHandle(notice, pagelist, page, k, trs.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, k, trs.size());
                            }
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
        Thread.sleep(1000);
        Document docCount = Jsoup.parse(new URL(href).openStream(), "UTF-8", href);
//        String title = docCount.select("#InfoDetail_lblTitle").text();
        String openDate = docCount.select("#InfoDetail_lblDate").text();
        String date = openDate.substring(openDate.indexOf("更新时间") + 5, openDate.indexOf("阅读次数")).replaceAll(" ", "");
        notice.setOpendate(date);
        CURRENT_PAGE_LAST_OPENDATE = date;
        String content = docCount.select("#InfoDetail_tblInfo").select("tr").get(2).html();
        notice.setContent(content);
        return notice;
    }

}
