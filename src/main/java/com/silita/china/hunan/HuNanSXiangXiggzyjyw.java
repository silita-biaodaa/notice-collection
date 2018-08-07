package com.silita.china.hunan;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static com.snatch.common.SnatchContent.*;

/**
 * 湖南省湘西公共资源交易网 create by SuDan 2017/03/21
 * http://ggzyjy.xxz.gov.cn./EpointWebBuilderXiang/moreinfojyxxlistAction.action?cmd=getInfolist
 * 005001001招标公告  005001002补充通知  005001003答疑  005001004中标候选人公示
 * 005002001采购公告  005002002补充通知  005002003答疑  005002004结果公告  005002005合同公告
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanSXiangXiggzyjyw")
public class HuNanSXiangXiggzyjyw extends BaseSnatch {

    String JSESSIONID = SnatchUtils.getStringRandom(32).toUpperCase();

    @Test
    @Override
    public void run() throws Exception {

        String[] urls = {
                "http://ggzyjy.xxz.gov.cn./jyxx/005001/005001001/aboutjyxxsearch.html",
                "http://ggzyjy.xxz.gov.cn./jyxx/005001/005001002/aboutjyxxsearch.html",
                "http://ggzyjy.xxz.gov.cn./jyxx/005001/005001003/aboutjyxxsearch.html",
                "http://ggzyjy.xxz.gov.cn./jyxx/005001/005001004/aboutjyxxsearch.html",
                "http://ggzyjy.xxz.gov.cn./jyxx/005002/005002001/aboutjyxxsearch.html",
                "http://ggzyjy.xxz.gov.cn./jyxx/005002/005002002/aboutjyxxsearch.html",
                "http://ggzyjy.xxz.gov.cn./jyxx/005002/005002003/aboutjyxxsearch.html",
                "http://ggzyjy.xxz.gov.cn./jyxx/005002/005002004/aboutjyxxsearch.html",
                "http://ggzyjy.xxz.gov.cn./jyxx/005002/005002005/aboutjyxxsearch.html",
        };
        String interfaceUrl = "http://ggzyjy.xxz.gov.cn./EpointWebBuilderXiang/moreinfojyxxlistAction.action?cmd=getInfolist";
        String[] typeInnerNum = {"005001001", "005001002", "005001003", "005001004", "005002001", "005002002", "005002003", "005002004", "005002005"};
        String[] catchType = {ZHAO_BIAO_TYPE, BU_CHONG_TYPE, DA_YI_TYPE, ZHONG_BIAO_TYPE, ZHAO_BIAO_TYPE, BU_CHONG_TYPE, DA_YI_TYPE, ZHONG_BIAO_TYPE, HE_TONG_TYPE};

        Connection conn = null;
        Document doc = null;

        //遍历站点
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            JSONObject jsonObject;
            String url = urls[i];
            //===入库代码====
            super.queryBeforeSnatchState(urls[i]);
            try {
                //遍历每个分类
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    XxlJobLogger.log(pagelist + "页");
                    conn = Jsoup.connect(interfaceUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    conn.cookie("JSESSIONID", JSESSIONID);
                    conn.header("X-Requested-With", "XMLHttpRequest");

                    conn.data("CatgoryNum", typeInnerNum[i]);
                    conn.data("Title", "");
                    conn.data("Time", "");
                    conn.data("pageSize", "15");
                    conn.data("pageIndex", String.valueOf(pagelist - 1));
                    String docStr = conn.execute().body();
                    SnatchLogger.debug("第" + pagelist + "页");

                    JSONObject custom = new JSONObject(new JSONObject(docStr).getString("custom"));
                    if (page == 1) {
                        int total = custom.getInt("total");
                        if (total == 0) {
                            break;
                        }
                        page = total % 15 == 0 ? total / 15 : total / 15 + 1;
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                        SnatchLogger.debug("总" + page + "页");
                    }

                    JSONArray trs = new JSONArray(custom.get("data").toString());
                    //遍历每页数据
                    for (int row = 0; row < trs.length(); row++) {
                        JSONObject data = new JSONObject(trs.get(row).toString());
                        String detailUrl = "http://ggzyjy.xxz.gov.cn" + data.getString("href");
                        String title = data.getString("title");
                        String publishDate = data.getString("date");

                        Notice notice = new Notice();
                        notice.setUrl(detailUrl);
                        notice.setTitle(title);
                        notice.setOpendate(publishDate);
                        if (i <= 3) {
                            notice.setNoticeType("工程建设");
                        } else {
                            notice.setNoticeType("政府采购");
                        }
                        notice.setCatchType(catchType[i]);
                        notice.setAreaRank(CITY);
                        notice.setProvince("湖南省");
                        notice.setProvinceCode("huns");
                        notice.setCity("湘西土家族苗族自治州");
                        notice.setCityCode("xiangxzzz");
                        CURRENT_PAGE_LAST_OPENDATE = publishDate;
                        //详情信息入库，获取增量页数
                        if (notice.getNoticeType().contains("采购")) {
                            page = govDetailHandle(notice, pagelist, page, row, trs.length());
                        } else {
                            page = detailHandle(notice, pagelist, page, row, trs.length());
                        }
                    }
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                //===入库代码====
                super.saveAllPageIncrement(url);
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setTimeout(35000);
        try {
            HtmlPage hPage = webClient.getPage(href);
            webClient.waitForBackgroundJavaScript(6000);
            String content = Jsoup.parse(hPage.asXml()).select(".ewb-article-info").html();
            if(HE_TONG_TYPE.equals(notice.getCatchType())) {
                String enclosure = Jsoup.parse(hPage.asXml()).select(".ewb-article-add").html();
                content += enclosure;
            }
            notice.setContent(content);
        } catch (IOException e) {
            System.out.println("访问网页出错了");
        } finally {
            webClient.close();
        }
        //抓取招标维度
        if (ZHAO_BIAO_TYPE.equals(notice.getCatchType()) && !"政府采购".equals(notice.getNoticeType())) {
            Dimension dimension = new Dimension();
            String infoid = href.substring(href.lastIndexOf("/") + 1, href.lastIndexOf("."));
            String detailContentInterfaceUrl = "http://ggzyjy.xxz.gov.cn./EpointWebService/rest/xxzWebDataSync/GetZBGGTimeInfo";

            Connection conn =Jsoup.connect(detailContentInterfaceUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
            conn.cookie("JSESSIONID", JSESSIONID);
            conn.header("X-Requested-With", "XMLHttpRequest");

            conn.data("token", "EpointWebInfo@123");
            conn.data("infoid", infoid);
            String detailStr = conn.post().body().html();
            JSONObject jsonObject = new JSONObject(detailStr);
            if (jsonObject.has("ZBGGTimeInfo")) {
                JSONObject customStr = jsonObject.getJSONObject("ZBGGTimeInfo");
                String bmEndDate = customStr.getString("baomingto");
                if (SnatchUtils.isNotNull(bmEndDate)) {
                    dimension.setBmStartDate(bmEndDate.substring(0, 10).replaceAll("/", "-"));
                    if (bmEndDate.length() > 12) {
                        dimension.setBmEndTime(bmEndDate.substring(10).trim());
                    }
                }
                String bzjDate = customStr.getString("bzjenddate");
                if (SnatchUtils.isNotNull(bmEndDate)) {
                    dimension.setAssureEndDate(bzjDate.substring(0, 10).replaceAll("/", "-"));
                }
                String kbDate = customStr.getString("kaibiaodate");
                if (SnatchUtils.isNotNull(kbDate)) {
                    dimension.setTbEndDate(kbDate.substring(0, 10).replaceAll("/", "-"));
                }
            }
            notice.setDimension(dimension);
        }
        return notice;
    }
}
