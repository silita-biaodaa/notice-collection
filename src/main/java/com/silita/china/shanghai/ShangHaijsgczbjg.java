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

import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * 上海市住房和城乡建设管理委员会官网    建设工程中标结果公告
 * https://www.ciac.sh.cn/XmZtbbaWeb/gsqk/ZbjgGkList.aspx    建设工程中标结果公告
 * Created by Administrator on 2018/3/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "ShangHaijsgczbjg")
public class ShangHaijsgczbjg extends BaseSnatch{


    private static final String source = "shangh";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String snatchNumber = SnatchUtils.makeSnatchNumber();
        int page = 1;
        int pageTemp = 0;
        Document doc = null;
        String staticUrl = "https://www.ciac.sh.cn/XmZtbbaWeb/gsqk/ZbjgGkList.aspx";
        WebClient webClient = null;
        HtmlPage hPage = null;
        super.queryBeforeSnatchState(staticUrl);
        try {
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
                    hPage = webClient.getPage(staticUrl); // 解析第一页
                    webClient.waitForBackgroundJavaScript(10000);
                    doc = Jsoup.parse(hPage.asXml());
                    pageTemp = page = 1; // 固定页数
                    SnatchLogger.debug("总"+page+"页");
                    page = computeTotalPage(page,LAST_ALLPAGE);
                }
                SnatchLogger.debug("第"+pagelist+"页");

                String eleId = "gvZbjgGkList_lbXmmc_";

                Elements trs = doc.select("#gvZbjgGkList").first().select("tr");
                for (int row = 1; row < trs.size() - 2; row++) {
                    String title = doc.select("#" + eleId + (row-1)).first().text().trim();
                    if (SnatchUtils.isNotNull(title)) {
                        Notice notice = new Notice();
                        HtmlElement detailBtn = hPage.getHtmlElementById(eleId + (row -1));   // 公告详情列

                        notice.setProvince("上海市");
                        notice.setProvinceCode("shs");

                        notice.setCatchType("2");
                        notice.setNoticeType("建设工程");

                        notice.setSource(source);
                        notice.setAreaRank(PROVINCE);
                        notice.setSnatchNumber(snatchNumber);


                        try {
                            // 详情页
                            HtmlPage detailPage = detailBtn.click();
                            Document detailDoc = Jsoup.parse(detailPage.asXml());
                            title = detailDoc.select("#Label6").first().text().trim();
                            String content = detailDoc.select(".table01").first().html();
                            String date = detailDoc.select("#lblzbrq").first().text().trim().replace("年","-").replace("月","-").replace("日","");
                            String conturl = detailDoc.select("#form1").first().attr("action");
                            conturl = conturl.replaceAll("./","");
                            conturl = "https://www.ciac.sh.cn/XmZtbbaWeb/gsqk/" + conturl;

                            Dimension dm = new Dimension();
                            String oneOffer = detailDoc.select("#lblzbj").first().text().trim();
                            if (SnatchUtils.isNotNull(oneOffer)) {
                                BigDecimal b = new BigDecimal(oneOffer);
                                oneOffer = String.valueOf(b.multiply(new BigDecimal(10000)));
                            }
                            String oneName = detailDoc.select("#Label9").first().text().trim();
                            String zbName = detailDoc.select("#Label2").first().text().trim();
                            String dlName = detailDoc.select("#Label8").first().text().trim();

                            dm.setOneOffer(SnatchUtils.isNull(oneOffer)?"":oneOffer);
                            dm.setOneName(SnatchUtils.isNull(oneName)?"":oneName);
                            dm.setZbName(SnatchUtils.isNull(zbName)?"":zbName);
                            dm.setDlName(SnatchUtils.isNull(dlName)?"":dlName);

                            notice.setDimension(dm);
                            notice.setTitle(title);
                            notice.setContent(content);
                            notice.setOpendate(date);
                            notice.setUrl(conturl);
                        } catch (Exception e) {
                            continue;
                        } finally {
                            hPage = webClient.getPage(staticUrl); // 解析第一页
                            webClient.waitForBackgroundJavaScript(10000);
                            doc = Jsoup.parse(hPage.asXml());
                        }

                        page = detailHandle(notice,pagelist,page,row,trs.size()-2);
                    }
                }
                if(pagelist==page){
                    page = turnPageEstimate(page);
                }
            }
            super.saveAllPageIncrement(staticUrl);
        } catch (IOException e) {
            SnatchLogger.error(e);
            SnatchLogger.warn("访问网页出错了");
        } finally {
            webClient.close();
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        return notice;
    }
}
