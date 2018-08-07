package com.silita.china.hunan.zhongbiao;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.apache.commons.lang3.StringUtils;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yj on 2017/11/22.
 * <p>
 * 永州市  永州市公共资源交易网   http://www.yzcity.gov.cn
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanYongZhouyzcitygov")
public class HuNanYongZhouyzcitygov extends BaseSnatch {


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
        urlParam1.put("url", "http://www.yzcity.gov.cn/cnyz/zfcg/list2.shtml");
        urlParam1.put("catchType", "1");
        urlParam1.put("noticeType", "招标公告");
        urlParams.add(urlParam1);
        Map<String, String> urlParam2 = new HashMap<>();
        urlParam2.put("url", "http://www.yzcity.gov.cn/zwgkyd/2016zbtb/list2.shtml");
        urlParam2.put("catchType", "1");
        urlParam2.put("noticeType", "招标公告");
        urlParams.add(urlParam2);

        for (Map<String, String> urlParam : urlParams) {
            int page = 1;
            int pageTemp = 0;
            String url = urlParam.get("url");
            super.queryBeforeSnatchState(url);
            try {
                String tempUrl = url;
                for (int i = 1; i == 1 || i <= page; i++) {
                    if(pageTemp>0 && page>pageTemp){
                        break;
                    }
                    if (i > 1) {
                        tempUrl = url.replace("list2.shtml", "list2_" + i + ".shtml");
                    }
                    SnatchLogger.debug(i + "页");
                    Document doc = doConnect(tempUrl);
                    if (page == 1) {
                        String docHtml = doc.html();
                        String pageStr = docHtml.substring(docHtml.indexOf("createPageHTML('page_div',")+26,docHtml.indexOf(",'list2','shtml'")).trim();
                        String[] pages = pageStr.split(",");
                        String count = pages[0];
                        page = Integer.parseInt(count);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements lis = doc.select(".list_div");
                    for (int j = 0; j < lis.size(); j++) {
                        String conturl = lis.get(j).select("a").first().absUrl("href");
                        if (StringUtils.isNotBlank(conturl)) {
                            String dateStr = lis.get(j).select("td").get(0).text().trim();
                            String date = dateStr.substring(dateStr.indexOf("发布时间： ")+6);
                            Notice notice = new Notice();
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("永州市");
                            notice.setCityCode("yongzs");
                            notice.setCatchType(urlParam.get("catchType"));
                            notice.setUrl(conturl);
                            notice.setNoticeType(urlParam.get("noticeType"));
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            if(url.contains("zfcg")){
                                page = govDetailHandle(notice, i, page, j, lis.size());
                            }else{
                                page = detailHandle(notice, i, page, j, lis.size());
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

    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contdoc = doConnect(href);
        Element titleEle = contdoc.select(".title_cen").first();
        String title = null;
        if(!titleEle.select("ucaptitle").isEmpty()) {
            title = titleEle.select("ucaptitle").first().text();
        }else {
            title = titleEle.text();
        }
        String content = contdoc.select("#zoom").first().html();
        notice = HuNanYongZhouyzcitygov.getCatchType(notice,title);
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }

    /**
     * 统一获取网页内容
     */
    private Document doConnect(String url) {
        int retryTimes = 0;
        do {
            try {
                Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                Document doc = conn.get();
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