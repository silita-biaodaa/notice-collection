package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by wangying on 2017/5/23/0023.
 * 益阳市  益阳公共资源交易监管网   http://jyzx.yiyang.gov.cn
 * 房屋市政     政府采购    国土资源    产权交易
 * 交通及水利    医用耗材及其他
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value="HuNanYiYangyiyang")
public class HuNanYiYangyiyang  extends BaseSnatch {


    @Test
    @Rollback(false)
    public void run()throws Exception  {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String urls [] =  {
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001001/003001001001/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001002/003001002001/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001003/003001003001/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001004/003001004001/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001001/003001001002/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001002/003001002002/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001003/003001003002/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001004/003001004002/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001001/003001001003/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001002/003001002003/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001003/003001003003/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003001/003001004/003001004003/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003002/003002001/003002001001/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003002/003002001/003002001002/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003002/003002001/003002001003/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003002/003002001/003002001004/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003002/003002001/003002001005/jyxx.html",
                "http://jyzx.yiyang.gov.cn/jyxx/003002/003002001/003002001006/jyxx.html"};
        String[] catchTypes = {"1","1","1","1",DA_YI_TYPE,DA_YI_TYPE,DA_YI_TYPE,DA_YI_TYPE,"2","2","2","2","1",GENG_ZHENG_TYPE,"2",ZHONG_ZHI_TYPE,HE_TONG_TYPE,"0"};
        for (int i= 0 ;i < urls.length ;i++) {
            int page =1;
            int pageTemp = 0;
            String noticeType = null;
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            if(i<=11){
                noticeType = "工程建设";
            }else{
                noticeType = "政府采购";
            }
            String url = urls[i];
            //===入库代码====
            super.queryBeforeSnatchState(url);//查询url前次抓取的情况（最大页数与公示时间）
            try {
                String tempUrl = url;
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if(pageTemp>0 && page>pageTemp){
                        break;
                    }
                    SnatchLogger.debug("第"+pagelist+"页");
                    if( pagelist > 1){
                        tempUrl = url.replace("jyxx.html",pagelist+".html");
                    }
                    Connection conn = Jsoup.connect(tempUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    Document doc= conn.get();
                    Elements trs = null ;
                    try {
                        trs = doc.select(".ewb-r-items").select("li");
                    }catch (Exception e){
                        continue;
                    }
                    if(trs.size()==15) {
                        if (page == 1) {
                            String pageCont = doc.select("#index").text().trim();
                            page = Integer.valueOf(pageCont.substring(pageCont.indexOf("/") + 1));
                            pageTemp = page;
                            SnatchLogger.debug("总" + page + "页");
                            page = computeTotalPage(page, LAST_ALLPAGE);
                        }
                    }
                    for(int row=0;row<trs.size();row++){
                        String conturl=trs.get(row).select("a").first().absUrl("href");
                        String title = trs.get(row).select("a").first().attr("title");
                        Notice notice = new Notice();
                        notice.setTitle(title);
                        String date=trs.get(row).select(".r").text().trim().replace("[","").replace("]","");
                        notice.setProvince("湖南省");
                        notice.setProvinceCode("huns");
                        notice.setCity("益阳市");
                        notice.setCityCode("yiy");
                        notice.setAreaRank(CITY);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setUrl(conturl);
                        notice.setOpendate(date);
                        notice.setNoticeType(noticeType);
                        notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                        if (SnatchUtils.isNull(notice.getCatchType())) {
                            notice.setCatchType(catchTypes[i]);
                        } else {
                            SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchTypes[i]);
                        }
                        if(noticeType.contains("采购")){
                            page = govDetailHandle(notice,pagelist,page,row,trs.size());
                        }else{
                            page = detailHandle(notice,pagelist,page,row,trs.size());
                        }
                    }
                    if(pagelist==page)
                        page = turnPageEstimate(page);
                }
            super.saveAllPageIncrement(url);
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    throw e;
                }
                SnatchLogger.error(e);
            }finally {
                super.clearClassParam();
            }
        }
    }

    public Notice detail(String href, Notice notice, String catchType) throws Exception{
        try {
            Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
            String title = contdoc.select(".news-article-tt").first().text().trim();
            contdoc.select("script").remove();
            contdoc.select(".news-article-tt").remove();
            contdoc.select(".news-article-info").remove();
            Element time2E = contdoc.select(".time2").first();
            Element time3E = contdoc.select(".time3").first();
            if (time2E != null && time3E != null) {
                String id = href.substring(href.lastIndexOf("/") + 1,href.lastIndexOf(".html"));
                Document dd =  Jsoup.connect("http://jyzx.yiyang.gov.cn/DataExchangeService/rest/kaipingbiaodata/GetKaibiaoInfo/"+id).ignoreContentType(true).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true).get();
                String time1 = Util.getContentByJson(dd.text()).findPath("bzjenddate").asText();
                String time2 = Util.getContentByJson(dd.text()).findPath("kaibiaodate").asText();
                Dimension dimension = new Dimension();
                boolean hasDimension = false;
                if (SnatchUtils.isNotNull(time1) && time1.length() > 15) {
                    String tbAssureEndDate = time1.substring(0,time1.indexOf(" "));
                    String tbAssureEndTime = time1.substring(time1.indexOf(" ") + 1,time1.length()-3);
                    dimension.setTbAssureEndDate(tbAssureEndDate);
                    dimension.setTbAssureEndTime(tbAssureEndTime);
                    hasDimension = true;
                }
                if (SnatchUtils.isNotNull(time2) && time2.length() > 15) {
                    String tbEndDate = time2.substring(0,time2.indexOf(" "));
                    String tbEndTime = time2.substring(time2.indexOf(" ") + 1,time2.length()-3);
                    dimension.setTbEndDate(tbEndDate);
                    dimension.setTbEndTime(tbEndTime);
                    hasDimension = true;
                }
                if (hasDimension) {
                    notice.setDimension(dimension);
                }
            }
            contdoc.select("#showtime").remove();
            String content = contdoc.select(".news-article").html();
            notice.setTitle(title);
            notice.setContent(content);
        }catch (Exception e){
        }
        return notice;
    }

}
