package com.silita.china.hunan.zhongbiao;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
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

import static com.snatch.common.SnatchContent.*;

/**
 * Created by maofeng on 2017/7/20.
 * 长沙市  长沙市公共资源交易监管网  https://csggzy.gov.cn
 * https://ggzy.changsha.gov.cn/jrfwpt/ThemeStandard/Standard/TradeCenter/tradeList.do?Deal_Type=Deal_Type1 房建市政
 * https://ggzy.changsha.gov.cn/jrfwpt/ThemeStandard/Standard/TradeCenter/tradeList.do?Deal_Type=Deal_Type2 交通工程
 * https://ggzy.changsha.gov.cn/jrfwpt/ThemeStandard/Standard/TradeCenter/tradeList.do?Deal_Type=Deal_Type3 水利工程
 * https://ggzy.changsha.gov.cn/jrfwpt/ThemeStandard/Standard/TradeCenter/tradeList.do?Deal_Type=Deal_Type4 政府采购
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "HuNanChangShaShiggzyjy")
public class HuNanChangShaShiggzyjy extends BaseSnatch {

    private static final String source = "huns";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    @Override
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "https://ggzy.changsha.gov.cn/spweb/CS/TradeCenter/tradeList.do?Deal_Type=Deal_Type1",
                "https://ggzy.changsha.gov.cn/spweb/CS/TradeCenter/tradeList.do?Deal_Type=Deal_Type2",
                "https://ggzy.changsha.gov.cn/spweb/CS/TradeCenter/tradeList.do?Deal_Type=Deal_Type3",
                "https://ggzy.changsha.gov.cn/spweb/CS/TradeCenter/tradeList.do?Deal_Type=Deal_Type4"
        };

        String[] noticeType = {"房建市政", "交通工程", "水利工程", "政府采购"};
        String[] noticType = {"1", "2", "91", "92", "PUBLICITY", "WEB_JY_NOTICE"};

        Document doc = null;

        HtmlPage hPage = null;
        WebClient webClient = null;

        for (int i = 0; i < urls.length; i++) {
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                webClient = new WebClient();
                webClient.getOptions().setUseInsecureSSL(true);
                webClient.getOptions().setJavaScriptEnabled(true);
                webClient.getOptions().setCssEnabled(false);
                webClient.getOptions().setRedirectEnabled(true);
                webClient.setAjaxController(new NicelyResynchronizingAjaxController());
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                webClient.getOptions().setTimeout(100000);
                hPage = webClient.getPage(url);
                webClient.waitForBackgroundJavaScript(50000);

                HtmlElement itemEle;
                DomNodeList<HtmlElement> typeTab = hPage.getElementById("trade-list-item").getElementsByTagName("li");

                for (int tab = 0; tab <= 5; tab++) {
                    int page = 1;
                    int pageTemp = 0;
                    url = urls[i] + "&noticType=" + noticType[tab];
                    //===入库代码====
                    super.queryBeforeSnatchState(url);//查询url前次抓取的情况（最大页数与公示时间）

                    itemEle = typeTab.get(tab);
                    if ("display: list-item;".equals(typeTab.get(tab).getAttribute("style"))) {
                        hPage = itemEle.click();
                        webClient.waitForBackgroundJavaScript(50000);
                    }

                    for (int pagelist = 1; pagelist <= page; pagelist++) {
                        if (pageTemp > 0 && page > pageTemp) {
                            break;
                        }
                        SnatchLogger.debug("第" + pagelist + "页");
                        if (pagelist == 1) {
                            doc = Jsoup.parse(hPage.asXml());
                            String pageStr = hPage.getElementById("kkpager").querySelector(".totalPageNum").asText();
                            page = Integer.parseInt(pageStr);
                            pageTemp = page;
                            SnatchLogger.debug("共" + page + "页");
                            //===入库代码====
                            page = computeTotalPage(page, LAST_ALLPAGE);
                        } else {
                            HtmlElement nextPage = (HtmlElement) hPage.getElementById("kkpager").getElementsByTagName("span").get(0).getLastElementChild().getPreviousElementSibling();
                            hPage = nextPage.click();
                            webClient.waitForBackgroundJavaScript(50000);
                            doc = Jsoup.parse(hPage.asXml());
                        }

                        Elements trs = doc.select("#index-list").select("tr");
                        for (int row = 1; row < trs.size(); row++) {
                            if (!"暂无数据".equals(trs.get(row).text())) {
                                String detailUrl = "https://ggzy.changsha.gov.cn" + trs.get(row).select("td").first().select("a").first().attr("href");
                                String title = trs.get(row).select("td").select("a").first().attr("title");
                                String openDate = sdf.format(sdf.parse(trs.get(row).select("td").last().text()));
                                //===入库代码====
                                Notice notice = new Notice();
                                notice.setProvince("湖南省");
                                notice.setProvinceCode("huns");
                                notice.setCity("长沙市");
                                notice.setCityCode("cs");
                                notice.setTitle(title);
                                notice.setUrl(detailUrl);

                                notice.setNoticeType(noticeType[i]);
                                switch (tab) {
                                    case 0:
                                        notice.setCatchType(ZHAO_BIAO_TYPE);
                                        break;
                                    case 1:
                                        notice.setCatchType(ZI_GE_YU_SHEN_TYPE);
                                        break;
                                    case 2:
                                        notice.setCatchType(ZI_GE_YU_SHEN_TYPE);
                                        break;
                                    case 3:
                                        notice.setCatchType(KONG_ZHI_JIA_TYPE);
                                        break;
                                    case 4:
                                        notice.setCatchType(ZHONG_BIAO_TYPE);
                                        break;
                                    case 5:
                                        notice.setCatchType(OTHER_TYPE);
                                        break;
                                }
                                notice.setAreaRank(CITY);
                                notice.setOpendate(openDate);
                                CURRENT_PAGE_LAST_OPENDATE = openDate;
                                //详情信息入库，获取增量页数
                                if ("政府采购".equals(notice.getNoticeType())) {
                                    page = govDetailHandle(notice, pagelist, page, row, trs.size());
                                } else {
                                    page = detailHandle(notice, pagelist, page, row, trs.size());
                                }
                            } else {
                                System.out.println("该栏目下暂时没有数据！");
                            }
                        }
                        //===入库代码====
                        if (pagelist == page) {
                            page = turnPageEstimate(page);
                        }
                    }
                    System.out.println("####" + url + "抓取完毕####");
                    super.saveAllPageIncrement(url);
                }
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contentDoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = contentDoc.select("#content-box-id").html();
        notice.setContent(content);
        return notice;
    }
}
