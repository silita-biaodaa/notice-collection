package com.silita.china.hunan.zhongbiao;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yj on 2017/11/22.
 * <p>
 * 郴州市  郴州市公共资源交易中心   http://www.czggzy.net
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanChenZhouczggzynet")
public class HuNanChenZhouczggzynet extends BaseSnatch {


    /**
     * 最大重试次数
     */
    private int maxRetryTimes = 5;

    /**
     * 重试休息间隙 ms
     */
    private int retrySleepMillis = 3000;

    @Test
    @Rollback(false)
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        List<Map<String, String>> urlParams = new ArrayList<>();
        Map<String, String> urlParam1 = new HashMap<>();
        urlParam1.put("url", "http://www.czggzy.net/jyxx/004002/004002001/list.html");
        urlParams.add(urlParam1);
        Map<String, String> urlParam2 = new HashMap<>();
        urlParam2.put("url", "http://www.czggzy.net/jyxx/004002/004002002/list.html");
        urlParams.add(urlParam2);
        Map<String, String> urlParam3 = new HashMap<>();
        urlParam3.put("url", "http://www.czggzy.net/jyxx/004002/004002003/list.html");
        urlParams.add(urlParam3);
        Map<String, String> urlParam4 = new HashMap<>();
        urlParam4.put("url", "http://www.czggzy.net/jyxx/004002/004002004/list.html");
        urlParams.add(urlParam4);
        Map<String, String> urlParam5 = new HashMap<>();
        urlParam5.put("url", "http://www.czggzy.net/jyxx/004001/004001002/004001002001/list4.html");
        urlParams.add(urlParam5);
        Map<String, String> urlParam6 = new HashMap<>();
        urlParam6.put("url", "http://www.czggzy.net/jyxx/004001/004001002/004001002002/list4.html");
        urlParams.add(urlParam6);
        Map<String, String> urlParam7 = new HashMap<>();
        urlParam7.put("url", "http://www.czggzy.net/jyxx/004001/004001002/004001002003/list4.html");
        urlParams.add(urlParam7);
        Map<String, String> urlParam8 = new HashMap<>();
        urlParam8.put("url", "http://www.czggzy.net/jyxx/004001/004001001/004001001001/list4.html ");
        urlParams.add(urlParam8);
        Map<String, String> urlParam9 = new HashMap<>();
        urlParam9.put("url", "http://www.czggzy.net/jyxx/004001/004001001/004001001002/list4.html");
        urlParams.add(urlParam9);
        Map<String, String> urlParam10 = new HashMap<>();
        urlParam10.put("url", "http://www.czggzy.net/jyxx/004001/004001001/004001001003/list4.html");
        urlParams.add(urlParam10);
        Map<String, String> urlParam11 = new HashMap<>();
        urlParam11.put("url", "http://www.czggzy.net/jyxx/004001/004001001/004001001004/list4.html");
        urlParams.add(urlParam11);
        Map<String, String> urlParam12 = new HashMap<>();
        urlParam12.put("url", "http://www.czggzy.net/jyxx/004001/004001001/004001001005/list4.html");
        urlParams.add(urlParam12);


        for (Map<String, String> urlParam : urlParams) {
            int page = 1;
            int pageTemp = 0;
            String url = urlParam.get("url");
            super.queryBeforeSnatchState(url);
            try {
                String tempUrl = url;
                for (int i = 1; i <= page; i++) {
                    if(pageTemp>0 && page>pageTemp){
                        break;
                    }
                    if (i > 1){
                        if(url.contains("04001/004001002/00400100200")||url.contains("004001/004001001/00400100100")){
                            tempUrl = url.replace("list4.html", i + ".html");
                        }else{
                            tempUrl = url.replace("list.html", i + ".html");
                        }
                    }
                    SnatchLogger.debug(i + "页");
                    Connection conn = Jsoup.connect(tempUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    Document doc = conn.get();
                    if (page == 1) {
                        String type = doc.select("#index").text();
                        String count = type.substring(type.indexOf("/") + 1);
                        page = Integer.parseInt(count);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements lis = doc.select(".ewb-nbd-items").select("li");
                    for (int j = 0; j < lis.size(); j++) {
                        String conturl = lis.get(j).select("a").first().absUrl("href");
                        if (conturl.contains(".html")) {
                            String title = lis.get(j).select("a").text().trim();
                            String date = lis.get(j).select("span").text().trim();
                            Notice notice = new Notice();
                            notice = HuNanChenZhouczggzynet.getCatchType(notice,title);
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("郴州市");
                            notice.setCityCode("cz");
                            notice.setUrl(conturl);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            if(url.contains("04001/004001002/00400100200")||url.contains("004001/004001001/00400100100")||url.contains("jyxx/004002/004002002")){
                                page = detailHandle(notice, i, page, j, lis.size());
                            }else{
                                page = govDetailHandle(notice, i, page, j, lis.size());
                            }
                        }
                    }
                    if (i == page) {
                        page = turnPageEstimate(page);
                    }
                }
                super.saveAllPageIncrement(url);
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    throw e;
                }
                SnatchLogger.error(e);
            } finally {
                super.clearClassParam();
            }
        }
    }

    public Notice detail(String href, Notice notice, String catchType) {
        Document contdoc = doConnect(href);
        String title = null ;
        try {
        title = contdoc.select(".news-article-tt").first().html();
        contdoc.select(".news-article-tt").remove();
        contdoc.select(".news-article-info").remove();
        contdoc.select(".news-article-para").remove();
        String content = contdoc.select(".news-detail-wrap").first().html();
        notice.setTitle(title);
        notice.setContent(content);
        } catch (Exception e) {
        }
        return notice;
    }

    /**
     * 统一获取网页内容
     */
    private Document doConnect(String url) {
        int retryTimes = 0;
        do {
            try {
                Document doc = Jsoup.parse(new URL(url).openStream(),"gbk",url);
                return doc;
            } catch (IOException ioExc) {
                int sleepMillis = this.retrySleepMillis * (1 << retryTimes);
                if ((retryTimes + 1) < this.maxRetryTimes) {
                    try {
                        System.out.println("系统繁忙，" + sleepMillis + "ms 后重试(第" + (retryTimes + 1) + "次)");
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException interExc) {
                        throw new RuntimeException(interExc);
                    }
                }
            }
        } while (++retryTimes < this.maxRetryTimes);
        throw new RuntimeException();
    }

    public static Notice getCatchType(Notice notice,String title){
        if(title.contains("答疑")||title.contains("补充")||title.contains("澄清")||title.contains("延期")
                ||title.contains("修改")||title.contains("补遗")||title.contains("终止")||title.contains("质疑")
                ||title.contains("暂停")||title.contains("流标")||title.contains("废标")||title.contains("更正")
                ||title.contains("调整")){
            notice.setCatchType("0");
            notice.setNoticeType("其他公告");
        }else if (title.contains("代理")&&title.contains("结果")||title.contains("代理")&&title.contains("选定")
                ||title.contains("代理")&&title.contains("中标")){
            notice.setCatchType("5");
            notice.setNoticeType("代理中标公告");
        }else if (title.contains("代理")){
            notice.setCatchType("4");
            notice.setNoticeType("代理招标公告");
        }else if(title.contains("中标")||title.contains("成交")||title.contains("结果")
                ||title.contains("成果")||title.contains("中选")||title.contains("入围")){
            notice.setCatchType("2");
            notice.setNoticeType("中标公告");
        }else{
            notice.setCatchType("1");
            notice.setNoticeType("招标公告");
        }
        return notice;
    }

}