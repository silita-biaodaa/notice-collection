package com.silita.china.ningxia;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
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

import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * Created by hujia on 2018/03/05
 * 宁夏回族自治区公共资源交易网
 * 招标信息
 http://www.nxggzyjy.org/ningxiaweb/002/002001/002001001/listPage.html      // 招标公告
 http://www.nxggzyjy.org/ningxiaweb/002/002001/002001002/listPage.html      // 变更公示
 http://www.nxggzyjy.org/ningxiaweb/002/002001/002001003/listPage.html      // 中标公告
 * 政府采购
 http://www.nxggzyjy.org/ningxiaweb/002/002002/002002001/listPage.html      // 采购公告
 http://www.nxggzyjy.org/ningxiaweb/002/002002/002002002/listPage.html      // 变更公示
 http://www.nxggzyjy.org/ningxiaweb/002/002002/002002003/listPage.html      // 中标公告
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "NingXiaHuiZuZiZhiQuggzyjyw")
public class NingXiaHuiZuZiZhiQuggzyjyw extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String urls[] = {
                "http://www.nxggzyjy.org/ningxiaweb/002/002001/002001001/listPage.html",
                "http://www.nxggzyjy.org/ningxiaweb/002/002001/002001002/listPage.html",
                "http://www.nxggzyjy.org/ningxiaweb/002/002001/002001003/listPage.html",
                "http://www.nxggzyjy.org/ningxiaweb/002/002002/002002001/listPage.html",
                "http://www.nxggzyjy.org/ningxiaweb/002/002002/002002002/listPage.html",
                "http://www.nxggzyjy.org/ningxiaweb/002/002002/002002003/listPage.html"
        };

        String[] catchTypes = {"1","16","2","1","16","2"};
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
                        url = urls[i].replace("listPage",String.valueOf(pagelist));
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (pagelist == 1) {
                            String pageText= doc.select("#index").text();
                            page= Integer.parseInt(pageText.substring(pageText.indexOf("/")+1));
                            pageTemp = page;
                            //===入库代码====
                            page = computeTotalPage(page, LAST_ALLPAGE);
                            SnatchLogger.debug("总" + page + "页");
                    }
                    Elements trs = doc.select("#showList").select("ul").first().select("li").select(".ewb-info-item");
                    for (int row = 0; row < trs.size(); row++) {

                        Dimension dim = new Dimension();
                        String href = trs.get(row).select("a").first().absUrl("href");
                        String title = "";
                        String titleContent = trs.get(row).select("a").first().text();
                        String city = titleContent.substring(1,titleContent.indexOf("]"));
                        String title1 = titleContent.substring(titleContent.indexOf("]")+1);
                        if(title1.substring(0,8).contains("[")){
                            title1 = title1.substring(title1.indexOf("]")+1);
                        }
                        if(title1.contains("[")) {
                            title = title1.substring(0, title1.indexOf("["));
                        }else{
                            title = title1;
                        }
                        if(city.contains("区")){
                            city = "宁夏回族自治区";
                        }
                        dim.setProjDq(city);
                        String date = trs.get(row).select(".ewb-date").text();

                        //===入库代码====
                        Notice notice = new Notice();
                        notice.setDimension(dim);
                        notice.setProvince("宁夏回族自治区");
                        notice.setProvinceCode("nxhzzzq");
                        notice.setAreaRank(PROVINCE);
                        notice.setSnatchNumber(snatchNumber);
                        notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                        if (SnatchUtils.isNull(notice.getCatchType())) {
                            notice.setCatchType(catchTypes[i]);
                        } else {
                            SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchTypes[i]);
                        }
                        if (i>2||title.contains("采购")) {
                            notice.setNoticeType("政府采购");
                        }else{
                             notice.setNoticeType("建设工程");
                        }
                        notice.setUrl(href);
                        notice.setSource("ningx");
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
            } finally {
                super.clearClassParam();
            }
        }
    }


    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {

        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        Elements trs = docCount.select(".ewb-main-con");
        String relationUrl = "";
        String c1 = "";
        try {
            c1 = trs.get(0).html() + trs.get(1).html();
            String c2 = trs.select("div[style='display: none;']").html();
            c1.replace(c2,"");
        }catch (Exception e){

        }
        String date1 = docCount.select(".ewb-main-bar").text();
        String date = date1.substring(date1.indexOf("间")+2,date1.indexOf("间")+12);

        CURRENT_PAGE_LAST_OPENDATE = date;
        notice.setOpendate(date);
        notice.setContent(c1);

        String relationDetailUrl =  href.substring(href.indexOf("0")+30);
        String relationDetail = "http://www.nxggzyjy.org/ningxia/services/BulletinWebServer/getRelateInDetail?infoid="+relationDetailUrl.substring(0,relationDetailUrl.indexOf("."));
        Document doc =  Jsoup.connect(relationDetail).ignoreContentType(true).get();
        JsonNode data = Util.getContentByJson(doc.text());
        JSONArray list = JSONObject.parseArray(String.valueOf(data));

        for (int k = 0; k < list.size(); k++) {
            JSONObject type = list.getJSONObject(k);
            String url = type.getString("infoid");
            if(!url.equals(relationDetailUrl.substring(0,relationDetailUrl.indexOf(".")))){
                String  relationUrl1 = "http://www.nxggzyjy.org/ningxiaweb/002/002001/"+type.getString("categorynum")+"/"+type.getString("infodate").replace("-","")+"/"+type.getString("infoid")+".html";
                relationUrl = relationUrl+relationUrl1+",";
            }
        }
        if(!StringUtils.isBlank(relationUrl)){
            Dimension dim = notice.getDimension();
            dim.setRelation_url(relationUrl);
            notice.setDimension(dim);
        }
        return notice;
    }

    @Test
    public void test()throws Exception{
        String href = "http://www.nxggzyjy.org/ningxiaweb/002/002002/002002001/20180305/b0fbc0df-d32a-4781-981e-f69f9744e1c8.html";
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        Elements trs = docCount.select(".ewb-main-con");
        String c1 = trs.get(0).html()+trs.get(1).html();
        String c2 = trs.select("div[style='display: none;']").html();
        c1.replace(c2,"");

        System.out.println("1");
    }



}
