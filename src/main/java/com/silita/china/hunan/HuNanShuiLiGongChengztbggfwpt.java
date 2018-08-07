package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.codehaus.jackson.JsonNode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.snatch.common.SnatchContent.BU_CHONG_TYPE;
import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * Created by hujia on 2017/8/7.
 * 湖南省水利工程招投标公共服务平台  http://218.76.24.60:8081/Homes/ZTB/Index.aspx
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})
@Component
@JobHander(value="HuNanShuiLiGongChengztbggfwpt")
public class HuNanShuiLiGongChengztbggfwpt extends BaseSnatch {

    @Override
    @Test
    public void run() throws Exception{
        sntachTask();
    }

    public void sntachTask() throws Exception{
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        String urls[] = {
                "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page=1&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBTBXXZBGG%22,%22type%22:%22string%22}]}",
                "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page=1&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBTBXXBCGG%22,%22type%22:%22string%22}]}",
                "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page=1&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBHXR%22,%22type%22:%22string%22},{%22op%22:%22equal%22,%22field%22:%22Extension3%22,%22value%22:%221%22,%22type%22:%22string%22}]}",
                "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page=1&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBJGGG%22,%22type%22:%22string%22}]}"
        };
        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < list.size(); i++) {
            int h = 0;
            int page =1;
            String url = " ";
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try{
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);//查询url前次抓取的情况（最大页数与公示时间）
                try {
                    for (int pagelist = 1; pagelist <= page; pagelist++) {
                        if (list.get(i) == "1") {
                            url = "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page=1&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBTBXXZBGG%22,%22type%22:%22string%22}]}";//招标公告
                            if (pagelist > 1) {
                                url = "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page="+pagelist+"&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBTBXXZBGG%22,%22type%22:%22string%22}]}";
                            }
                            h=1;
                        } else if (list.get(i) == "2") {
                            url = "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page=1&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBTBXXBCGG%22,%22type%22:%22string%22}]}";//补充公告
                            if (pagelist > 1) {
                                url = "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page="+pagelist+"&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBTBXXBCGG%22,%22type%22:%22string%22}]}";
                            }
                            h=0;
                        } else if (list.get(i) == "3") {
                            url = "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page=1&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBHXR%22,%22type%22:%22string%22},{%22op%22:%22equal%22,%22field%22:%22Extension3%22,%22value%22:%221%22,%22type%22:%22string%22}]}";//中标候选人公示
                            if (pagelist > 1) {
                                url = "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page="+pagelist+"&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBHXR%22,%22type%22:%22string%22},{%22op%22:%22equal%22,%22field%22:%22Extension3%22,%22value%22:%221%22,%22type%22:%22string%22}]}";
                            }
                            h=2;
                        } else if (list.get(i) == "4") {
                            url = "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page=1&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBJGGG%22,%22type%22:%22string%22}]}";//中标结果公示
                            if (pagelist > 1) {
                                url = "http://218.76.24.60:8081/api/SysQuery/get?Code=CMS_GetContentByCode&page="+pagelist+"&pagesize=45&sortname=EFFECTDATE%20desc,%20EXTENSION2%20desc&sortorder=&Where={%22op%22:%22and%22,%22rules%22:[{%22op%22:%22equal%22,%22field%22:%22CATEGORYFULLCODE%22,%22value%22:%22ZTB_ZBJGGG%22,%22type%22:%22string%22}]}";
                            }
                            h=2;
                        }
                        SnatchLogger.debug("第" + pagelist + "页");
                        conn = Jsoup.connect(url).ignoreContentType(true).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                        doc = conn.get();
                        if (page == 1) {
                            JsonNode lis=Util.getContentByJson(doc.text());
                            String number=lis.findPath("Total").asText();
                            int pageNumber2  = Integer.parseInt(number);
                            if(pageNumber2%45==0){
                                page = pageNumber2/45;
                            }else {
                                page = (pageNumber2/45)+1;
                            }
                            SnatchLogger.debug("总共" + page + "页");
                            page = computeTotalPage(page,LAST_ALLPAGE);
                        }
                        JsonNode lis=Util.getContentByJson(doc.text());
                        lis=lis.findPath("Rows");
                        for(int row=0;row<lis.size();row++){
                            String conturl = lis.get(row).findPath("CONTENTID").asText().trim();
                            if (!"".equals(conturl)) {
                                conturl = "http://218.76.24.60:8081/Homes/ZTB/pages/" + conturl+".htm";
                                String title = lis.get(row).findPath("CONTENTTITLE").asText().trim();
                                String date = lis.get(row).findPath("EFFECTDATE").asText().trim().substring(0,10);
                                String catchType = null;
                                Notice notice = new Notice();
                                notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                                if(h==1){
                                    catchType = "招标公告";
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType("1");
                                    }
                                    notice.setNoticeType(catchType);
                                }else if(h==2){
                                    catchType = "中标公告";
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType("2");
                                    }
                                    notice.setNoticeType(catchType);
                                }else if(h==0){
                                    catchType = "补充公告";
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(BU_CHONG_TYPE);
                                    }
                                    notice.setNoticeType(catchType);
                                }
                                notice.setProvince("湖南省");
                                notice.setProvinceCode("huns");
                                notice.setAreaRank(PROVINCE);
                                notice.setSnatchNumber(snatchNumber);
                                notice.setUrl(conturl);
                                notice.setTitle(title);
                                notice.setOpendate(date);
                                //详情信息入库，获取增量页数
                                page =detailHandle(notice,pagelist,page,row,lis.size());
                            }
                        }
                        //===入库代码====
                        if(pagelist==page){
                            page = turnPageEstimate(page);
                        }
                    }
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        throw e;
                    }
                    SnatchLogger.error(e);
                }
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            }finally {
                conn = null;
                doc = null;
                clearClassParam();
            }
        }

    }


    public Notice detail(String href, Notice notice, String catchType) throws Exception{
        Document contdoc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        String content=contdoc.select("#zoom2").html();
        notice.setContent(content);
        Thread.sleep(500);
        return notice;
    }

}
