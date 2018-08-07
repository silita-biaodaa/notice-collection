package com.silita.china.xinjiang;

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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Map;import static com.snatch.common.SnatchContent.*;

/**
 * 新疆维吾尔自治区建设工程招标投标网
 * http://ztb.xjjs.gov.cn/xjweb/jyxx/004001/004001001/MoreInfo.aspx?CategoryNum=004001001   交易信息 >> 招标公告 >> 施工
 * http://ztb.xjjs.gov.cn/xjweb/jyxx/004001/004001002/MoreInfo.aspx?CategoryNum=004001002   交易信息 >> 招标公告 >> 服务
 * http://ztb.xjjs.gov.cn/xjweb/jyxx/004001/004001003/MoreInfo.aspx?CategoryNum=004001003   交易信息 >> 招标公告 >> 货物
 * http://ztb.xjjs.gov.cn/xjweb/jyxx/004002/004002001/MoreInfo.aspx?CategoryNum=004002001   交易信息 >> 中标公示 >> 施工
 * http://ztb.xjjs.gov.cn/xjweb/jyxx/004002/004002002/MoreInfo.aspx?CategoryNum=004002002   交易信息 >> 中标公示 >> 服务
 * http://ztb.xjjs.gov.cn/xjweb/jyxx/004002/004002003/MoreInfo.aspx?CategoryNum=004002003   交易信息 >> 中标公示 >> 货物
 * Created by 91567 on 2018/3/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "XinJiangJianSheggzbw")
public class XinJiangJianSheggzbw extends BaseSnatch {

    private static final String source = "xinjiang";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://ztb.xjjs.gov.cn/xjweb/jyxx/004001/004001001/MoreInfo.aspx?CategoryNum=004001001",
                "http://ztb.xjjs.gov.cn/xjweb/jyxx/004001/004001002/MoreInfo.aspx?CategoryNum=004001002",
                "http://ztb.xjjs.gov.cn/xjweb/jyxx/004001/004001003/MoreInfo.aspx?CategoryNum=004001003",
                "http://ztb.xjjs.gov.cn/xjweb/jyxx/004002/004002001/MoreInfo.aspx?CategoryNum=004002001",
                "http://ztb.xjjs.gov.cn/xjweb/jyxx/004002/004002002/MoreInfo.aspx?CategoryNum=004002002",
                "http://ztb.xjjs.gov.cn/xjweb/jyxx/004002/004002003/MoreInfo.aspx?CategoryNum=004002003"
        };
        Map<String, String> cookies = null;

        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            Dimension dim = null;
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);//查询url前次抓取的情况（最大页数与公示时间）
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                       url = urls[i] + "&Paging=" + pagelist;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    if(cookies != null) {
                        conn.cookies(cookies);
                    }
                    doc = conn.get();
                    cookies = conn.response().cookies();
                    if (pagelist == 1) {
                        String textStr = doc.select("#Paging").first().select(".huifont").first().text().trim();
                        String pageStr = textStr.substring(textStr.indexOf("/") + 1, textStr.length());
                        page = Integer.parseInt(pageStr);
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                        SnatchLogger.debug("总" + page + "页");
                    }

                    Elements div = doc.select("#MoreInfoList1_tdcontent").select("tbody");
                    Elements trs = div.select("tr");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("td").get(1).select("a").first().absUrl("href");
                        String title = trs.get(row).select("td").get(1).select("a").first().attr("title");
                        if (SnatchUtils.isNotNull(conturl)) {
                            if (trs.get(row).select("td").get(1).select("font").first() != null) {
                                String projArea = trs.get(row).select("td").get(1).select("font").first().text().replace("[", "").replace("]", "");
                                if (projArea.contains("县")) {
                                    dim = new Dimension();
                                    dim.setProjXs(projArea);
                                } else if (projArea.contains("市")) {
                                    dim = new Dimension();
                                    dim.setProjDq(projArea);
                                }
                            }
                            String cacheType = "";
                            if (trs.get(row).select("td").get(1).select("red") != null) {
                                cacheType = trs.get(row).select("td").get(1).select("red").text().replace("[", "").replace("]", "");
                            }
                            String date = sdf.format(sdf.parse(trs.get(row).select("td").last().text().replace("[", "").replace("]", "")));
                            Notice notice = new Notice();
                            notice.setProvince("新疆维吾尔自治区");
                            notice.setProvinceCode("xj");
                            notice.setDimension(dim);
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setSnatchNumber(snatchNumber);
                            if (i <= 3) {
                                notice.setCatchType(ZHAO_BIAO_TYPE);
                            } else {
                                notice.setCatchType(ZHONG_BIAO_TYPE);
                            }
                            if (SnatchUtils.isNotNull(cacheType)) {
                                if (cacheType.contains("重发") || cacheType.contains("重新")) {
                                    notice.setCatchType(GENG_ZHENG_TYPE);
                                }
                            }
                            notice.setAreaRank("0");
                            notice.setSource(source);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            //详情信息入库，获取增量页数
                            if ("政府采购".equals(notice.getNoticeType())) {
                                page = govDetailHandle(notice, pagelist, page, row, trs.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, trs.size());
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
            } finally {
                clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = contdoc.select("#lblTitle").text().trim();
        contdoc.select("#divDS").remove();
        contdoc.select("#divDS_Line").remove();
        contdoc.select("#TDContent").select("table").attr("border", "1");
        String content = contdoc.select("#TDContent").html();
        notice.setContent(content);
        return notice;
    }
}
