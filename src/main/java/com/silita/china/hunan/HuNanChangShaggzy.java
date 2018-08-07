package com.silita.china.hunan;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.HuNanAreaUtil;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.snatch.model.SnatchUrl;
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

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.snatch.common.SnatchContent.*;


/**
 * 长沙市公共资源交易监管网
 * https://csggzy.gov.cn/Front/Index
 *	房建市政 ---- 招标公告	补充公告	延期公告	招标控制价公示
 *	交通工程 ---- 招标公告	补充公告	延期公告	招标控制价公示
 *	水利工程 ---- 招标公告	补充公告	延期公告	招标控制价公示
 *	政府采购 ---- 招标公告	其他公告
 *	医药采购 ---- 招标公告	其他公告	废标公告
 * @author WangYing
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})
@Component
@JobHander(value="HuNanChangShaggzy")
public class HuNanChangShaggzy extends BaseSnatch {

    @Test
    @Override
    public void run()throws Exception {
        //获取需要抓取的类别列表
        List<SnatchUrl> urlList = buildSnatchList();
        for(SnatchUrl snatchUrl: urlList) {
            getUrlList(snatchUrl);
        }
    }

    private List<SnatchUrl> buildSnatchList() throws Exception{
        String urls[] = new String[]{
                //0-3房建市政
                "https://csggzy.gov.cn/NoticeFile.aspx/Index/1?type=101&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm=%E6%88%BF%E5%BB%BA%E5%B8%82%E6%94%BF&Sm2=%E8%BD%AC%E8%AE%A9%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=undefined&Sm=%E6%88%BF%E5%BB%BA%E5%B8%82%E6%94%BF&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm2=%E8%A1%A5%E5%85%85%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=undefined&Sm=%E6%88%BF%E5%BB%BA%E5%B8%82%E6%94%BF&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm2=%E5%BB%B6%E6%9C%9F%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=undefined&Sm=%E6%88%BF%E5%BB%BA%E5%B8%82%E6%94%BF&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm2=%E6%8B%9B%E6%A0%87%E6%8E%A7%E5%88%B6%E4%BB%B7%E5%85%AC%E7%A4%BA",
                //4-7交通工程
                "https://csggzy.gov.cn/NoticeFile.aspx/Index/1?type=201&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm=%E4%BA%A4%E9%80%9A%E5%B7%A5%E7%A8%8B&Sm2=%E6%8B%9B%E6%A0%87%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=201&Sm=%E4%BA%A4%E9%80%9A%E5%B7%A5%E7%A8%8B&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm2=%E8%A1%A5%E5%85%85%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=201&Sm=%E4%BA%A4%E9%80%9A%E5%B7%A5%E7%A8%8B&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm2=%E5%BB%B6%E6%9C%9F%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=201&Sm=%E4%BA%A4%E9%80%9A%E5%B7%A5%E7%A8%8B&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm2=%E6%8B%9B%E6%A0%87%E6%8E%A7%E5%88%B6%E4%BB%B7%E5%85%AC%E7%A4%BA",
                //8-11水利工程
                "https://csggzy.gov.cn/NoticeFile.aspx/Index/1?type=301&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm=%E6%B0%B4%E5%88%A9%E5%B7%A5%E7%A8%8B&Sm2=%E6%8B%9B%E6%A0%87%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=301&Sm=%E6%B0%B4%E5%88%A9%E5%B7%A5%E7%A8%8B&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm2=%E8%A1%A5%E5%85%85%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=301&Sm=%E6%B0%B4%E5%88%A9%E5%B7%A5%E7%A8%8B&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm2=%E5%BB%B6%E6%9C%9F%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=301&Sm=%E6%B0%B4%E5%88%A9%E5%B7%A5%E7%A8%8B&Ptype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE&Sm2=%E6%8B%9B%E6%A0%87%E6%8E%A7%E5%88%B6%E4%BB%B7%E5%85%AC%E7%A4%BA",
                //12-13政府采购
                "https://csggzy.gov.cn/NoticeFile.aspx/Index/1?type=102&Ptype=%E6%94%BF%E5%BA%9C%E9%87%87%E8%B4%AD&Sm=%E6%94%BF%E5%BA%9C%E9%87%87%E8%B4%AD&Sm2=%E6%8B%9B%E6%A0%87%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=102&Sm=%E6%94%BF%E5%BA%9C%E9%87%87%E8%B4%AD&Ptype=%E6%94%BF%E5%BA%9C%E9%87%87%E8%B4%AD&Sm2=%E5%85%B6%E4%BB%96%E5%85%AC%E5%91%8A",
                //14-16医药采购
                "https://csggzy.gov.cn/NoticeFile.aspx/Index/1?type=104&Ptype=%E5%8C%BB%E8%8D%AF%E9%87%87%E8%B4%AD&Sm=%E5%8C%BB%E8%8D%AF%E9%87%87%E8%B4%AD&Sm2=%E6%8B%9B%E6%A0%87%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=104&Sm=%E5%8C%BB%E8%8D%AF%E9%87%87%E8%B4%AD&Ptype=%E5%8C%BB%E8%8D%AF%E9%87%87%E8%B4%AD&Sm2=%E5%85%B6%E4%BB%96%E5%85%AC%E5%91%8A",
                "https://csggzy.gov.cn/NoticeFile/Index?type=104&Sm=%E5%8C%BB%E8%8D%AF%E9%87%87%E8%B4%AD&Ptype=%E5%8C%BB%E8%8D%AF%E9%87%87%E8%B4%AD&Sm2=%E5%BA%9F%E6%A0%87%E5%85%AC%E5%91%8A"
        };
        List<SnatchUrl> urlList =new ArrayList<SnatchUrl>(urls.length);
        for(int i=0;i<urls.length;i++){
            String snatchNumber = new SimpleDateFormat("yyyyMMddhhmmss").format(System.currentTimeMillis());
            SnatchUrl snatchUrl1 = new SnatchUrl();
            snatchUrl1.setSnatchNumber(snatchNumber);
            snatchUrl1.setUrl(urls[i]);
            if (i==0 || i==4 || i==8 || i==12 || i==14) {
                snatchUrl1.setNotcieCatchType(ZHAO_BIAO_TYPE);
            } else if (i == 1 || i == 5 || i == 9) {
                snatchUrl1.setNotcieCatchType(BU_CHONG_TYPE);
            } else if (i == 2 || i == 6 || i ==10) {
                snatchUrl1.setNotcieCatchType(YAN_QI_TYPE);
            } else if (i == 3 || i == 7 || i == 11) {
                snatchUrl1.setNotcieCatchType(KONG_ZHI_JIA_TYPE);
            } else if (i == 13 || i == 15) {
                snatchUrl1.setNotcieCatchType(OTHER_TYPE);
            } else {
                snatchUrl1.setNotcieCatchType(FEI_BIAO_TYPE);
            }

            if(i<4){
                snatchUrl1.setNoticeType("房建市政");
            }else if(i<8){
                snatchUrl1.setNoticeType("交通工程");
            }else if(i<12){
                snatchUrl1.setNoticeType("水利工程");
            }else if(i<14){
                snatchUrl1.setNoticeType("政府采购");
            }else {
                snatchUrl1.setNoticeType("医药采购");
            }
            urlList.add(snatchUrl1);
        }
        return urlList;
    }

    private void snatchListHandle(String url,String noticeType,String noticeCatchType,String snatchNumber)throws Exception{
        Connection conn = null;
        Document doc = null;
        String dyUrl = url;
        int page = 1;
        int pageBackup = 0;
        for (int i = 1; i <= page; i++) {
            if(pageBackup>0 && page>pageBackup){
                break;
            }
            if(i>1){
                String replaceText = url.substring(url.indexOf("Index"),url.indexOf("?"));
                dyUrl = url.replace(replaceText,("Index/"+i));
            }
            SnatchLogger.debug("=========第" + i + "页");
            conn = Jsoup.connect(dyUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
            doc = conn.get();
            if(page==1){
                String cont = doc.select("#pagecont").text();
                cont = cont.substring(cont.indexOf("/")+1, cont.length());
                page = Integer.parseInt(cont);
                pageBackup = page;
                //===入库代码====
                SnatchLogger.debug("总" + page + "页");
                page = computeTotalPage(page,LAST_ALLPAGE);
            }
            Elements trs = doc.select("a[style=text-align: left;]");
            if(trs.size()<=0){
                SnatchLogger.debug("error: tr.size()-->"+trs.size());
                isUpdateIncrement=false;
            }
            for (int j = 0; j < trs.size(); j++) {
                Element ele = trs.get(j);
                String conturl = ele.absUrl("href");
                if(SnatchUtils.isNotNull(conturl)){
                    String title = ele.attr("title").trim();
                    String date = ele.parent().nextElementSibling().text().trim();
                    //===入库代码====
                    Notice notice = new Notice();
                    notice.setProvince("湖南省");
                    notice.setProvinceCode("huns");
                    notice.setCity("长沙市");
                    notice.setCityCode("cs");
                    notice.setTitle(title);
                    notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                    if (SnatchUtils.isNull(notice.getCatchType())) {
                        notice.setCatchType(noticeCatchType);
                    } else {
                        SnatchUtils.judgeCatchType(notice,notice.getCatchType(),noticeCatchType);
                    }
                    notice.setNoticeType(noticeType);
                    String a = conturl.substring(0,conturl.indexOf("?")+1);
                    String b = conturl.substring(conturl.indexOf("&PTType")+1);
                    notice.setUrl(a+b);
                    notice.setOpendate(date);
                    notice.setAreaRank(CITY);
                    notice.setSnatchNumber(snatchNumber);
                    //详情信息入库，获取增量页数
                    if (notice.getNoticeType().contains("采购")) {
                        page = govDetailHandle(notice,i,page,j,trs.size());
                    } else {
                        page =detailHandle(notice,i,page,j,trs.size());
                    }
                }
            }
            //===入库代码====
            if(i==page){
                page = turnPageEstimate(page);
            }

        }
    }

    /**
     * 公告详情内容获取
     * @param href
     * @param notice
     * @param catchType
     * @return
     * @throws Exception
     */
    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        String content = null;
        //需要跳转到新页面
        if("查看公告".equals(docCount.select(".detail_con").first().nextElementSibling().text())) {
            String hrefStr = docCount.select(".detail").select("[target=_blank]").first().attr("onclick");
            String tempUrl = hrefStr.substring(hrefStr.indexOf("','") + 3, hrefStr.indexOf("')"));
            //抓新页面必须模拟流量器请求
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setUseInsecureSSL(true);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setTimeout(35000);
            try {
                HtmlPage hPage = webClient.getPage(tempUrl);
                webClient.waitForBackgroundJavaScript(6000);
                notice.setUrl(tempUrl);
                content = Jsoup.parse(hPage.asXml()).select("#content-box-id").html();
            } catch (IOException e) {
                SnatchLogger.error(e);
                SnatchLogger.warn("访问网页出错了");
            }finally {
                webClient.close();
            }
        } else {
            content = docCount.select(".detail_con").html();
        }


        notice.setContent(content);

        // 抓取维度信息
        Element noticeDimension = docCount.select(".notice_table").first();
        String zbName = noticeDimension.select("tr").first().select("td").last().text().trim();
        String projType = noticeDimension.select("tr").get(1).select("td").get(1).text().trim();
        String projXs = noticeDimension.select("tr").get(1).select("td").last().text().trim();
        Elements relations = docCount.select(".CategoryTree").first().select("li");
        String relation_url = "";
        for (int i = 0; i < relations.size(); i++) {
            if (relations.get(i).hasAttr("id")) {
                break;
            }
            String relationUrl = relations.get(i).select("a").first().absUrl("href");
            String relationDate = relations.get(i).select("a").first().select("div").last().text().trim();
            if (!href.equals(relationUrl) && SnatchUtils.isNotNull(relationDate)) {
                if (SnatchUtils.isNotNull(relation_url)) {
                    relation_url += "," + relationUrl;
                } else {
                    relation_url = relationUrl;
                }
            }
        }
        Dimension dimension = new Dimension();
        dimension.setProjType(projType);
        dimension.setZbName(zbName);
        dimension.setRelation_url(relation_url);
        dimension.setProjDq("长沙");
        if (!"本市级" .equals(projXs) && !"长沙市".equals(projXs) && !"市本级".equals(projXs)) {
            dimension.setProjXs(SnatchUtils.isNull(projXs)?null:HuNanAreaUtil.hunanArea(projXs));
        } else {
            dimension.setProjXs("长沙市");
        }
        notice.setDimension(dimension);
        return notice;
    }

    private void getUrlList(SnatchUrl snatchUrl)throws Exception{
        String url = snatchUrl.getUrl();
        String noticeType = snatchUrl.getNoticeType();
        String noticeCatchType = snatchUrl.getNotcieCatchType();
        //===入库代码====
        LAST_ALLPAGE = 0;
        super.queryBeforeSnatchState(url);//查询url前次抓取的情况（最大页数与公示时间）
        try {
            //处理列表请求
            snatchListHandle(url,noticeType,noticeCatchType,snatchUrl.getSnatchNumber());
            //===入库代码====
            super.saveAllPageIncrement(url);
        } catch (IOException e) {
            SnatchLogger.error(e);
        }
    }
}