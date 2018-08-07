package com.silita.china.shanghai;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;

import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * 上海市住房和城乡建设管理委员会官网 -- 拆除工程         http://www.shjjw.gov.cn
 * http://www.ciac.sh.cn/CcgcInterWeb/gkzblist.aspx 拆除工程招标公告
 * http://www.ciac.sh.cn/CcgcInterWeb/zbjglist.aspx 拆除工程中标结果
 * Created by Administrator on 2018/3/12.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "ShangHaizfcjcc")
public class ShangHaizfcjcc extends BaseSnatch{


    private static final String source = "shangh";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception{
        String[] urls = {
                "http://www.ciac.sh.cn/CcgcInterWeb/gkzblist.aspx", // 拆除工程招标公告
                "http://www.ciac.sh.cn/CcgcInterWeb/zbjglist.aspx"  // 拆除工程中标结果
        };
        String[] catchTypes = {"1","2"};
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            Connection conn = null;
            Document doc = null;
            String url = urls[i];
            HtmlPage hPage = null;
            WebClient webClient = null;
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if(pageTemp>0 && page>pageTemp){
                        break;
                    }
                    if(pagelist == 1){
                        //获取总页数
                        webClient = new WebClient(BrowserVersion.CHROME); // 实例化Web客户端
                        webClient.getOptions().setUseInsecureSSL(true);
                        webClient.getOptions().setJavaScriptEnabled(true);
                        webClient.getOptions().setCssEnabled(false);
                        webClient.getOptions().setTimeout(60000);
                        try {
                            hPage=webClient.getPage(url); // 解析第一页
                            webClient.waitForBackgroundJavaScript(10000);
                        } catch (IOException e) {
                            SnatchLogger.error(e);
                            SnatchLogger.warn("访问网页出错了");
                        }
                        doc = Jsoup.parse(hPage.asXml());
                        String pageCont = doc.select("#GridViewPagedNvg1_lblPageCount").first().text().trim();
                        page = Integer.valueOf(pageCont.substring(pageCont.indexOf("共") + 1 ,pageCont.indexOf("页")));
                        pageTemp = page;
                        SnatchLogger.debug("总"+page+"页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    } else {
                        HtmlElement goBtn = hPage.getHtmlElementById("GridViewPagedNvg1_cmdNext");
                        hPage = goBtn.click();
                        Thread.sleep(10000);
                        doc = Jsoup.parse(hPage.asXml());
                    }
                    SnatchLogger.debug("第"+pagelist+"页");

                    Elements e = doc.select("tr[class=tr1]");

                    Elements trs = doc.select("tr[class=tr1,tr2]");
                    trs.addAll(e);
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().attr("href");
                        if (SnatchUtils.isNotNull(conturl)) {
                            Notice notice = new Notice();
                            conturl = "http://www.ciac.sh.cn/CcgcInterWeb/" + conturl;
                            String title = trs.get(row).select("a").first().text().trim();
                            String date = trs.get(row).select("td").last().text().trim();
                            date = sdf2.format(sdf.parse(date));

                            notice.setProvince("上海市");
                            notice.setProvinceCode("shs");

                            notice.setCatchType(catchTypes[i]);
                            notice.setNoticeType("拆除工程");
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setAreaRank(PROVINCE);
                            notice.setSource(source);

                            if (i == 0) {
                                Dimension dm = new Dimension();
                                String bmEndDate = trs.get(row).select("td").get(1).text().trim();
                                bmEndDate = sdf2.format(sdf.parse(bmEndDate));
                                dm.setBmEndDate(bmEndDate);
                                notice.setDimension(dm);
                            }


                            page = detailHandle(notice, pagelist, page, row, trs.size());
                        }
                    }
                    if(pagelist==page){
                        page = turnPageEstimate(page);
                    }
                }
                super.saveAllPageIncrement(urls[i]);
            }finally {
                webClient.close();
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        docCount.select("input[type=submit]").first().remove();
        String content = docCount.select("#UpdatePanel1").first().html();
        notice.setContent(content);

        // 维度获取
        if ("1".equals(notice.getCatchType())) {
            Dimension dm = notice.getDimension();
            String zbName = docCount.select("#lblzbr").first().text().trim();   // 招标人
            String zbContactMan = docCount.select("#lbljsdwlxr").first().text().trim(); // 招标联系人
            String dlName = docCount.select("#lblzbdldw").first().text().trim();    // 招标代理人
            String zbContactWay = docCount.select("#lbljsdwdh").first().text().trim(); // 招标联系方式
            String cert = docCount.select("#lblzztj").first().text().trim();    // 资质
            String bmSite = docCount.select("#lblbmdz").first().text().trim();  // 报名地点
            String fileCost = docCount.select("#lblgbf").first().text().trim(); // 标书费
            dm.setZbName(SnatchUtils.isNull(zbName)?"":zbName);
            dm.setZbContactMan(SnatchUtils.isNull(zbContactMan)?"":zbContactMan);
            dm.setDlName(SnatchUtils.isNull(dlName)?"":dlName);
            dm.setZbContactWay(SnatchUtils.isNull(zbContactWay)?"":zbContactWay);
            dm.setCert(SnatchUtils.isNull(cert) || cert.length() > 100?"":cert);
            dm.setBmSite(SnatchUtils.isNull(bmSite)?"":bmSite);
            dm.setFileCost(SnatchUtils.isNull(fileCost)?"":fileCost);
            notice.setDimension(dm);
        } else {
            Dimension dm = new Dimension();
            String oneName = docCount.select("#lblzbdw").first().text().trim();
            String oneOffer = docCount.select("#lblzbj").first().text().trim();
            if (SnatchUtils.isNotNull(oneOffer)) {
                BigDecimal b = new BigDecimal(oneOffer);
                oneOffer = String.valueOf(b.multiply(new BigDecimal(10000)));
            }
            dm.setOneName(SnatchUtils.isNull(oneName)?"":oneName);
            dm.setOneOffer(SnatchUtils.isNull(oneOffer)?"":oneOffer);
            notice.setDimension(dm);
        }

        notice = SnatchUtils.setCatchTypeByTitle(notice,notice.getTitle());
        if (SnatchUtils.isNull(notice.getCatchType())) {
            notice.setCatchType(catchType);
        } else {
            SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchType);
        }

        return notice;
    }
}
