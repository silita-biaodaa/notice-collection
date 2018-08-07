package com.silita.china.gansu;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * Created by hujia on 2018/03/05
 * 甘肃省公共资源交易信息网
 * 工程建设
 http://www.gsggfw.cn/w/bid/73/tradeinfo.html
 * 政府采购
 http://www.gsggfw.cn/w/bid/76/tradeinfo.html
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "GanSuggzyjyw")
public class GanSuggzyjyw extends BaseSnatch {

    Map<String, String> cook = new HashMap<String, String>();

    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {

        String area [] = {"{\"areaCode\":\"620000\"","{\"areaCode\":\"620100\"","{\"areaCode\":\"620200\"","{\"areaCode\":\"620300\"",
                "{\"areaCode\":\"620400\"","{\"areaCode\":\"620500\"","{\"areaCode\":\"620600\""
                ,"{\"areaCode\":\"620700\"","{\"areaCode\":\"620800\"","{\"areaCode\":\"620900\"",
                "{\"areaCode\":\"621000\"","{\"areaCode\":\"621100\"","{\"areaCode\":\"621200\"",
                "{\"areaCode\":\"622900\"","{\"areaCode\":\"623000\"","{\"areaCode\":\"0\""};
        String type [] = {",\"workNotice\":{\"noticeNature\":\"1\",\"bulletinType\":\"1\"},\"assortmentindex\":\"0\"}",
                  ",\"workNotice\":{\"noticeNature\":\"1\",\"bulletinType\":\"2\"},\"assortmentindex\":\"0\"}",
                  ",\"workNotice\":{\"noticeNature\":\"2\",\"bulletinType\":\"\"},\"assortmentindex\":\"1\"}",
                  ",\"workNotice\":{\"noticeNature\":\"2\",\"bulletinType\":\"\"},\"assortmentindex\":\"1\"}",
                  ",\"workNotice\":{\"noticeNature\":\"1\",\"bulletinType\":\"3\"},\"assortmentindex\":\"1\"}",
                  ",\"workNotice\":{\"noticeNature\":\"2\",\"bulletinType\":\"3\"},\"assortmentindex\":\"1\"}",
                  ",\"workNotice\":{\"noticeNature\":\"1\",\"bulletinType\":\"1\"}}",
                  ",\"workNotice\":{\"noticeNature\":\"1\",\"bulletinType\":\"1\"}}",
                  ",\"workNotice\":{\"noticeNature\":\"1\",\"bulletinType\":\"1\"}}"};

        String [] urls = {"http://www.gsggfw.cn/w/bid/tenderAnnQuaInqueryAnn/pageList",
                "http://www.gsggfw.cn/w/bid/qualiInqueryResult/pageList",
                "http://www.gsggfw.cn/w/bid/winResultAnno/pageList",
                "http://www.gsggfw.cn/w/bid/purchaseQualiInqueryAnn/pageList",
                "http://www.gsggfw.cn/w/bid/correctionItem/pageList",
                "http://www.gsggfw.cn/w/bid/bidDealAnnounce/pageList"};

        List<String> filterparam = new ArrayList<String>();
        List<String> flags = new ArrayList<String>();
        List<String> catchTypes = new ArrayList<String>();
        for(int i=0;i<area.length;i++){
            for (int j=0;j<type.length;j++){
                filterparam.add(area[i]+type[j]);
                if(j==0){
                    flags.add(urls[0]);
                    catchTypes.add("1");
                }else if(j==1){
                    flags.add(urls[0]);
                    catchTypes.add("1");
                }else if(j==2){
                    flags.add(urls[0]);
                    catchTypes.add("16");
                }else if(j==3){
                    flags.add(urls[1]);
                    catchTypes.add("2");
                }else if(j==4){
                    flags.add(urls[2]);
                    catchTypes.add("2");
                }else if(j==5){
                    flags.add(urls[2]);
                    catchTypes.add("2");
                }else if(j==6){
                    flags.add(urls[3]);
                    catchTypes.add("1");
                }else if(j==7){
                    flags.add(urls[4]);
                    catchTypes.add("16");
                }else if(j==8){
                    flags.add(urls[5]);
                    catchTypes.add("2");
                }
            }
        }
        Connection conn = null;
        Document doc = null;

        for (int i = 0; i < flags.size(); i++) {

            int page = 1;
            int pageTemp = 0;
            String url = flags.get(i);
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(flags.get(i)+filterparam.get(i));
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    if (pagelist == 1) {
                        conn.data("filterparam",filterparam.get(i));
                        doc=conn.post();
                        String pageText= doc.select(".controls").text();
                        int pageNumber = Integer.parseInt(pageText.substring(pageText.indexOf("共")+1,pageText.length()-1).trim());
                        if(pageNumber%20==0){
                            page = pageNumber/20;
                        }else{
                            page = pageNumber/20+1;
                        }
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                        SnatchLogger.debug("总" + page + "页");
                    }else{
                        url = flags.get(i)+"?pageNo="+pagelist+"&pageSize=20";
                        conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                        conn.data("filterparam",filterparam.get(i));
                        conn.cookies(cook);

                        String data = conn.request().data().toString();
                        byte[] postDataBytes = data.getBytes("UTF-8");
                        conn.header("Content-Length",String.valueOf(postDataBytes.length));
                        doc=conn.post();
                    }

                    conn.header("Accept", "*/*");
                    conn.header("Accept-Encoding", "gzip, deflate");
                    conn.header("Accept-Language", "zh-CN,zh;q=0.9");
                    conn.header("Cache-Control", "max-age=0");
                    conn.header("Connection","keep-alive");
                    conn.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    conn.header("Host","www.gsggfw.cn");
                    conn.header("Origin", "http://www.gsggfw.cn");
                    if (url.contains("purchase")||url.contains("correctionItem")||url.contains("bidDealAnnounce")) {
                        conn.header("Referer", "http://www.gsggfw.cn/w/bid/76/tradeinfo.html");
                    }else{
                        conn.header("Referer", "http://www.gsggfw.cn/w/bid/73/tradeinfo.html");
                    }
                    conn.header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.167 Safari/537.36");
                    conn.header("X-Requested-With","XMLHttpRequest");
                    cook = conn.response().cookies();

                    Elements trs = doc.select(".trad-sear-con").select("li");
                    if(trs.size()>0) {
                    for (int row = 0; row < trs.size(); row++) {

//                        Dimension dim = new Dimension();
                        String href1 = trs.get(row).select("a").attr("onclick").substring(15);
                        String href = "http://www.gsggfw.cn"+href1.substring(0,href1.length()-1);
                        String title = trs.get(row).select("a").attr("title").trim();
                        title = title.contains("E6")?title.substring(23):title;
                        String date = trs.get(row).select("span").text();
                        //===入库代码====
                        Notice notice = new Notice();
                        notice.setProvince("甘肃省");
                        notice.setProvinceCode("gss");
                        notice.setAreaRank(PROVINCE);
//                        dim.setProjProv("甘肃省");
                        notice.setSnatchNumber(snatchNumber);
                        notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                        if (SnatchUtils.isNull(notice.getCatchType())) {
                            notice.setCatchType(catchTypes.get(i));
                        } else {
                            SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchTypes.get(i));
                        }
                        if (url.contains("purchase")||url.contains("correctionItem")||url.contains("bidDealAnnounce")) {
                            notice.setNoticeType("政府采购");
                        }else{
                             notice.setNoticeType("建设工程");
                        }
                        notice.setUrl(href);
                        notice.setOpendate(date);
                        notice.setSource("gans");
//                        notice.setDimension(dim);
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
                    }else{
                        break;
                    }
                }
                //===入库代码====
                super.saveAllPageIncrement(flags.get(i)+filterparam.get(i));
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
        String content = docCount.select(".arti-des-con").html();
        notice.setContent(content);
        return notice;
    }


}
