package com.silita.china.hunan.zhongbiao;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.snatch.common.SnatchContent.COUNTY;
import static com.snatch.common.SnatchContent.ZHONG_BIAO_TYPE;

/**
 * Created by maofeng on 2017/7/21
 * 长沙市  宁乡县公共资源交易中心   http://www.nxggzy.cn/
 * 房屋市政--中标候选人公示
 * 交通工程--中标候选人公示
 * 水利工程-中标候选人公示
 * 小额交易--中标候选人公示
 * 政府采购--中标候选人公示
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring-test.xml" })

@Component
@JobHander("HuNanChangShaShinxx")
public class HuNanChangShaShinxx extends BaseSnatch{


    public static int page=1;//总页数

    @Test
    public void run()throws Exception{
        //获取需要抓取的类别列表
        List<SnatchUrl> urlList = buildSnatchList();
        for(SnatchUrl snatchUrl: urlList) {
            getUrlList(snatchUrl);
        }
    }

    private List<SnatchUrl> buildSnatchList()throws Exception{
        String urls[] ={
                "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004001003",   //房建市政--中标候选人公示
                "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004002003",   //交通工程--中标候选人公示
                "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004003003",   //水利工程--中标候选人公示
                "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004007002",   //小额交易--中标候选人公示
                "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004005002"    //政府采购--中标候选人公示
        };
        String[] types = new String[]{
                "房建市政",
                "交通工程",
                "水利工程",
                "小额交易",
                "政府采购"};
        List<SnatchUrl> urlList =new ArrayList<SnatchUrl>(urls.length);
        for(int i=0;i<urls.length;i++){
            SnatchUrl snatchUrl1 = new SnatchUrl();
            snatchUrl1.setUrl(urls[i]);
            snatchUrl1.setNoticeType(types[i]);
            snatchUrl1.setSnatchNumber(SnatchUtils.makeSnatchNumber());
            urlList.add(snatchUrl1);
        }
        return urlList;
    }


    /**
     * 保存当前总页数，计算此次应该抓取的总页数
     */
    private void computeSnatchPageNum()throws Exception{
        page = computeTotalPage(page,LAST_ALLPAGE);
    }

    private void getUrlList(SnatchUrl snatchUrl)throws Exception{
        String url = snatchUrl.getUrl();
        String noticeType = snatchUrl.getNoticeType();
        String snatchNumber = snatchUrl.getSnatchNumber();
        //===入库代码====
        super.queryBeforeSnatchState(url);
        try {
            snatchListHandle(url,noticeType,snatchNumber);
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

    /**
     * 处理每一个列表请求
     * @param url
     * @param noticeType
     * @throws Exception
     */
    private void snatchListHandle(String url,String noticeType,String snatchNumber)throws Exception{
        Map<String, String> data = null;
        Map<String, String> cookie = null;
        Document doc = null;
        for (int i = 1; i <= page; i++) {
            SnatchLogger.debug("=========第"+i+"页");
            Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
            if(i==1){
                doc = conn.get();
                cookie = conn.response().cookies();
                if(conn.response().statusCode()==200){
                    //取总页数
                    String text = doc.select("#MoreinfoListsearch1_Pager>div").get(0).text();
                    page = Integer.parseInt(text.substring(text.indexOf("总页数：")+4, text.indexOf("当前页")).trim());
                    //===入库代码====
                    SnatchLogger.debug("总页数："+page);
                    computeSnatchPageNum();
                }else{
                    throw new Exception("访问异常,Status Code:"+conn.response().statusCode()+","+url);
                }
            }else{
                data = new HashMap<String, String>();
                data.put("__CSRFTOKEN",doc.select("#__CSRFTOKEN").attr("value"));
                data.put("__VIEWSTATE",doc.select("#__VIEWSTATE").attr("value"));
                data.put("__VIEWSTATEGENERATOR", doc.select("#__VIEWSTATEGENERATOR").attr("value"));
                data.put("__EVENTTARGET", "MoreinfoListsearch1$Pager");
                data.put("__EVENTARGUMENT", String.valueOf(i));
                data.put("__EVENTVALIDATION", doc.select("#__EVENTVALIDATION").attr("value"));
                data.put("MoreinfoListsearch1$txtTitle", "  ");
                data.put("MoreinfoListsearch1$Pager_input", String.valueOf(i-1));
                conn.data(data);
                conn.cookies(cookie);
                doc = conn.post();
            }

            Elements tr = doc.select("#MoreinfoListsearch1_DataGrid1").select("tr");
            for (int j = 0; j < tr.size(); j++) {
                Element el = tr.get(j).select("a").first();
                String href = el.absUrl("href");
                String title = el.attr("title");
                String openDate  = tr.get(j).select("td").last().text().trim();
                //===入库代码====
                Notice notice = new Notice();
                notice.setProvince("湖南省");
                notice.setProvinceCode("huns");
                notice.setCity("长沙市");
                notice.setCityCode("cs");
                notice.setCounty("宁乡县");
                notice.setCountyCode("nxx");
                notice.setNoticeType(noticeType);

                notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                if (SnatchUtils.isNull(notice.getCatchType())) {
                    notice.setCatchType(ZHONG_BIAO_TYPE);
                } else {
                    SnatchUtils.judgeCatchType(notice,notice.getCatchType(),ZHONG_BIAO_TYPE);
                }


                notice.setSnatchNumber(snatchNumber);
                notice.setAreaRank(COUNTY);
                notice.setUrl(href);
                notice.setTitle(title);
                notice.setOpendate(openDate);
                CURRENT_PAGE_LAST_OPENDATE = openDate;
                if (notice.getNoticeType().contains("采购")) {
                    page = govDetailHandle(notice,i,page,j,tr.size());
                } else {
                    page = detailHandle(notice,i,page,j,tr.size());
                }
            }
        }
    }

    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = docCount.select("#mainContent").html();
        if (docCount.select("#filedown").first() != null) {
            content += docCount.select("#filedown").first().html();
        }
        notice.setContent(content);
        return notice;
    }
}
