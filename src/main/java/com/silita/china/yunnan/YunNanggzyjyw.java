package com.silita.china.yunnan;

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

import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * Created by hujia on 2018/03/01
 * 云南省公共资源交易信息网
 * 工程建设
 https://www.ynggzyxx.gov.cn/jyxx/jsgcZbgg        // 招标公告
 https://www.ynggzyxx.gov.cn/jyxx/jsgcGzsx        // 更正事项
 https://www.ynggzyxx.gov.cn/jyxx/jsgcpbjggs      // 评标结果公示
 https://www.ynggzyxx.gov.cn/jyxx/jsgcZbjggs      // 中标公告
 https://www.ynggzyxx.gov.cn/jyxx/jsgcZbyc        // 招标异常
 * 政府采购
 https://www.ynggzyxx.gov.cn/jyxx/zfcg/cggg       // 采购公告
 https://www.ynggzyxx.gov.cn/jyxx/zfcg/gzsx       // 更正事项
 https://www.ynggzyxx.gov.cn/jyxx/zfcg/kbjl       // 开标记录
 https://www.ynggzyxx.gov.cn/jyxx/zfcg/zbjggs     // 中标结果
 https://www.ynggzyxx.gov.cn/jyxx/zfcg/zfcgYcgg   // 异常公告
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "YunNanggzyjyw")
public class YunNanggzyjyw extends BaseSnatch {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {

        String urls[] = {
                "https://www.ynggzyxx.gov.cn/jyxx/jsgcZbgg",
                "https://www.ynggzyxx.gov.cn/jyxx/jsgcGzsx",
                "https://www.ynggzyxx.gov.cn/jyxx/jsgcpbjggs",
                "https://www.ynggzyxx.gov.cn/jyxx/jsgcZbjggs",
                "https://www.ynggzyxx.gov.cn/jyxx/jsgcZbyc",
                "https://www.ynggzyxx.gov.cn/jyxx/zfcg/cggg",
                "https://www.ynggzyxx.gov.cn/jyxx/zfcg/gzsx",
                "https://www.ynggzyxx.gov.cn/jyxx/zfcg/kbjl",
                "https://www.ynggzyxx.gov.cn/jyxx/zfcg/zbjggs",
                "https://www.ynggzyxx.gov.cn/jyxx/zfcg/zfcgYcgg"
        };

        String[] catchTypes = {"1","16","2","2","13","1","16","2","2","13"};
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

                    String six1 = String.valueOf(pagelist);
                    String six2 = "000";
                    String six3 = "0";
                    String six4 = "";
                    String six5 = "";
                    String six6 = "";
                    String six7 = "";
                    switch(i) {
                        case 0:six4 = "771";
                            break;
                        case 1:six4 = "881";
                            break;
                        case 2:six4 = "700";
                            break;
                        case 3:six4 = "484";
                            break;
                        case 4:six4 = "851";
                               six3 = "";
                            break;
                        case 5:six4 = "652";
                            six3 = "";
                            break;
                        case 6:six4 = "771";
                            six3 = "";
                            break;
                        case 7:six4 = "508";
                            six3 = "";
                            break;
                        case 8:six4 = "652";
                            six3 = "";
                            break;
                        case 9:six4 = "771";
                                six2 = "";
                                six3 = "";
                            break;
                        default:six3 = "616";
                            break;
                    }

                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);

                    if (pagelist == 1) {
                        doc = conn.get();
                        String pageText= doc.select(".mmggxlh").text();
                        page = i!=7?Integer.parseInt(pageText.substring(pageText.indexOf("·")+4,pageText.indexOf("下")).trim()):Integer.parseInt(pageText.substring(pageText.indexOf("下")-2,pageText.indexOf("下")).trim());
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                        SnatchLogger.debug("总" + page + "页");
                    }else{
                       doc =  postUrl(doc,conn,six1,six2,six3,six4,six5,six6,six7,i);
                    }

                    Elements trs = doc.select("#data_tab").select("tr");
                    for (int row = 1; row < trs.size(); row++) {

                        Dimension dim = new Dimension();
                        String href = trs.get(row).select("a").first().absUrl("href");
                        String title = null;
                        String date = null;

                        if(i==2||i==6){
                            String titleText = trs.get(row).select("a").text();
                            if(titleText.contains("]")){
                                title = titleText.substring(titleText.indexOf("]")+1).trim();
                            }else{
                                title = titleText.trim();
                            }
                            date = trs.get(row).select("td").get(3).text().substring(0,9);
                            date = dateFormatWay(date);
                        }else {
                            if(i==3||i==8){
                                date = trs.get(row).select("td").get(2).text();
                            }else if(i==4||i==9){
                                date = trs.get(row).select("td").get(4).text();
                            }else{
                                date = trs.get(row).select("td").get(3).text();
                            }
                            if(i==0||i==1||i==5) {
                                dim.setTbAssureEndDate(trs.get(row).select("td").get(4).text());
                            }
                            title = trs.get(row).select("a").first().attr("title").trim();
                        }
                        String title1 = trs.get(row).select("a").text().trim();
                        if(title1.contains("]")){
                            String city = title1.substring(1,title1.indexOf("]"));
                            if(city.contains("县")||city.contains("区")||city.contains("州")){
                                dim.setProjXs(city);
                            }
                        }
                        //===入库代码====
                        Notice notice = new Notice();
                        notice.setProvince("云南省");
                        notice.setProvinceCode("yns");
                        notice.setAreaRank(PROVINCE);
                        notice.setSnatchNumber(snatchNumber);
                        notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                        if (SnatchUtils.isNull(notice.getCatchType())) {
                            notice.setCatchType(catchTypes[i]);
                        } else {
                            SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchTypes[i]);
                        }
                        if (i>4) {
                            notice.setNoticeType("政府采购");
                        }else{
                             notice.setNoticeType("建设工程");
                        }
                        notice.setUrl(href);
                        notice.setSource("yunn");
//                        notice.setOpendate(date);
                        notice.setDimension(dim);
                        CURRENT_PAGE_LAST_OPENDATE = date;
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

    public Document postUrl(Document doc,Connection conn,String six1,String six2,String six3,String six4,String six5,String six6,String six7,int i)throws Exception{
        conn.data("currentPage",six1);
        conn.data("area",six2);
        conn.data("industriesTypeCode",six3);
        conn.data("scrollValue",six4);
        if(i!=3&&i!=8){
            conn.data("tenderProjectCode",six5);
        }else if(i==4&&i==7){
            conn.data("bidSectionCode",six5);
        }else if(i==5||i==6||i==9){
            conn.data("purchaseProjectCode",six5);
        }
        if(i==2){
            conn.data("tenderProjectName",six6);
        }else if(i==4){
            conn.data("exceptionName",six6);
        }else if(i==5&&i==9){
            conn.data("bulletinTitle",six6);
        }else if(i==6){
            conn.data("terminationBulletinTitle",six6);
        }else if(i==7){
            conn.data("bidSectionName",six6);
        }else if(i==8){
            conn.data("winBidBulletinTitle",six6);
        }else{
            conn.data("bulletinName",six6);
        }
        conn.data("secondArea",six7);
        doc=conn.post();
        return doc;
    }

    public String dateFormatWay(String date)throws  Exception{
        String d1 = date.substring(0,4);
        String d2 = date.substring(4,6);
        String d3 = date.substring(6,8);
        date = d1+"-"+d2+"-"+d3;
        return date;
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String openDate = docCount.select(".kdg").html().replaceAll("[\\u4e00-\\u9fa5]", "");
        openDate = sdf.format(sdf.parse(openDate));
        String content = docCount.select(".page_contect").html();
        notice.setContent(content);
        notice.setOpendate(openDate);
        return notice;
    }


    @Test
    public void test()throws  Exception{
        String href = "https://www.ynggzyxx.gov.cn/jyxx/zfcg/kbjl";
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String pageText= docCount.select(".mmggxlh").text();
        int page= Integer.parseInt(pageText.substring(pageText.indexOf("下")-2,pageText.indexOf("下")).trim());

        System.out.println(page);

    }

}
