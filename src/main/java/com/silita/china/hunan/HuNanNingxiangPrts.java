package com.silita.china.hunan;

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
import static com.snatch.common.SnatchContent.KONG_ZHI_JIA_TYPE;

/**
 * 宁乡县公共资源交易监管网
 * http://www.nxggzy.cn/TPFront_NX/
 * 房建市政、交通工程、水利工程、其他工程、小额交易、政府采购
 * Created by dh on 2017/6/20.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value="HuNanNingxiangPrts")
public class HuNanNingxiangPrts extends BaseSnatch {


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
        String[] urls = new String[]{
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004002001",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004002002",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004002004",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004003001",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004003002",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004003004",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004001001",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004001002",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004001004",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004004001",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004004002",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004007001",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004007003",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004005001",
        "http://61.186.94.156/TPFront_NX/showinfo/searchmore.aspx?CategoryNum=004005003"
        };
        String[] types = new String[]{"交通工程","交通工程","交通工程","水利工程","水利工程","水利工程","房建市政","房建市政","房建市政",
                                      "其他工程","其他工程","小额交易","小额交易","政府采购","政府采购"};
        String[] catchTypes = new String[]{"1",KONG_ZHI_JIA_TYPE,"0","1",KONG_ZHI_JIA_TYPE,"0","1",KONG_ZHI_JIA_TYPE,"0","1","0","1","0","1","0"};
        List<SnatchUrl> urlList =new ArrayList<SnatchUrl>(urls.length);
        for(int i=0;i<urls.length;i++){
            SnatchUrl snatchUrl1 = new SnatchUrl();
            snatchUrl1.setUrl(urls[i]);
            snatchUrl1.setNoticeType(types[i]);
            snatchUrl1.setNotcieCatchType(catchTypes[i]);
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
        String noticeCatchType = snatchUrl.getNotcieCatchType();
        String snatchNumber = snatchUrl.getSnatchNumber();
        //===入库代码====
        super.queryBeforeSnatchState(url);
        try {
            snatchListHandle(url,noticeType,noticeCatchType,snatchNumber);
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
    private void snatchListHandle(String url,String noticeType,String catchType,String snatchNumber)throws Exception{
        Map<String, String> data = null;
        Map<String, String> cookie = null;
        int pageTemp = 0;
        Document doc = null;
        for (int i = 1; i <= page; i++) {
            if (pageTemp > 0 && page > pageTemp) {
                break;
            }
            SnatchLogger.debug("=========第"+i+"页");
            Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
            if(i==1){
                doc = conn.get();
                cookie = conn.response().cookies();
                if(conn.response().statusCode()==200){
                    //取总页数
                    String text = doc.select("#MoreinfoListsearch1_Pager>div").get(0).text();
                    page = Integer.parseInt(text.substring(text.indexOf("总页数：")+4, text.indexOf("当前页")).trim());
                    pageTemp = page;
                    SnatchLogger.debug("总" + page + "页");
                    //===入库代码====
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

                notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                if (SnatchUtils.isNull(notice.getCatchType())) {
                    notice.setCatchType(catchType);
                } else {
                    SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchType);
                }

                notice.setAreaRank(COUNTY);
                notice.setSnatchNumber(snatchNumber);
                notice.setNoticeType(noticeType);
                notice.setUrl(href);
                notice.setTitle(title);
                notice.setOpendate(openDate);
                if (notice.getNoticeType().contains("采购")) {
                    page = govDetailHandle(notice,i,page,j,tr.size());
                } else {
                    page =detailHandle(notice,i,page,j,tr.size());
                }
            }
        }
    }

    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = docCount.select("#mainContent").html();
        notice.setContent(content);
        return notice;
    }
}
