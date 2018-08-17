package com.silita.china.henan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;

import static com.snatch.common.SnatchContent.GENG_ZHENG_TYPE;
import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * Create by IntelliJ Idea 2018.1
 * Company: silita
 * Author: gemingyi
 * Date: 2018/8/15 11:24
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HeNanggzyjyggfwpt")
public class HeNanggzyjyggfwpt extends BaseSnatch {

    private static final String source = "henan";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getSelect?response=application/json&pageIndex=1&pageSize=22&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002001001&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",  //工程建设--招标公告
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getSelect?response=application/json&pageIndex=1&pageSize=22&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002001002&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",  //工程建设--变更公告
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getSelect?response=application/json&pageIndex=1&pageSize=22&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002001003&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",  //工程建设--评审结果公示（中标）
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getSelect?response=application/json&pageIndex=1&pageSize=22&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002001004&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",  //工程建设--中标结果公示
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getSelect?response=application/json&pageIndex=1&pageSize=22&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002002001&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",  //政府采购--采购公告
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getSelect?response=application/json&pageIndex=1&pageSize=22&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002002002&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",  //政府采购--变更公告
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getSelect?response=application/json&pageIndex=1&pageSize=22&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002002003&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38"   //政府采购--结果公告
        };
        String[] pageCountUrls = {
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getCount?response=application/json&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002001001&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",   //工程建设--招标公告
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getCount?response=application/json&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002001002&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",   //工程建设--变更公告
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getCount?response=application/json&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002001003&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",   //工程建设--评审结果公示（中标）
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getCount?response=application/json&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002001004&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",   //工程建设--中标结果公示
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getCount?response=application/json&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002002001&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",   //政府采购--采购公告
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getCount?response=application/json&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002002002&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38",   //政府采购--变更公告
                "http://www.hnsggzyfwpt.gov.cn/services/hl/getCount?response=application/json&day=&sheng=x1&qu=&xian=&title=&timestart=&timeend=&categorynum=002002003&siteguid=9f5b36de-4e8f-4fd6-b3a1-a8e08b38ea38"    //政府采购--结果公告
        };
        String[] catchTypes = {"1", GENG_ZHENG_TYPE, "2", "2", "1", GENG_ZHENG_TYPE, "2"};
        String[] noticeTypes = {"工程建设", "工程建设", "工程建设", "工程建设", "政府采购", "政府采购", "政府采购",};

        Connection conn;
        Document doc;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].replace("pageIndex=1", "pageIndex=" + pagelist);
                    }
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    SnatchLogger.debug("第" + pagelist + "页");
                    if (page == 1) {
                        //获取总页数
                        page = getPage(pageCountUrls[i]);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }

                    String dataStr = conn.execute().body();
                    JSONObject jsonObject = new JSONObject(new JSONObject(dataStr).getString("return"));
                    JSONArray datas = new JSONArray(jsonObject.get("Table").toString());
                    for (int row = 0; row < datas.length(); row++) {
                        JSONObject data = new JSONObject(datas.get(row).toString());
                        String conturl = "http://www.hnsggzyfwpt.gov.cn" + data.getString("href");
                        String title = data.getString("title");
                        String date = data.getString("infodate");
                        Notice notice = new Notice();
                        notice.setProvince("河南省");
                        notice.setProvinceCode("hens");
                        notice.setCatchType(catchTypes[i]);
                        notice.setNoticeType(noticeTypes[i]);
                        notice.setUrl(conturl);
                        notice.setTitle(title);
                        notice.setOpendate(date);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setAreaRank(PROVINCE);
                        notice.setSource(source);
                        CURRENT_PAGE_LAST_OPENDATE = date;

                        // 地区维度
                        String infoc = data.get("infoc").toString();
                        if (SnatchUtils.isNotNull(infoc) && !infoc.contains("省")) {
                            Dimension dm = new Dimension();
                            dm.setProjDq(infoc);
                            notice.setDimension(dm);
                        }

                        if (notice.getNoticeType().contains("采购")) {
                            page = govDetailHandle(notice, pagelist, page, row, datas.length());
                        } else {
                            page = detailHandle(notice, pagelist, page, row, datas.length());
                        }
                    }
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                super.saveAllPageIncrement(urls[i]);
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = docCount.select(".ewb-left-bd").first().html();
        notice.setContent(content);
        return notice;
    }

    /**
     * 获取总页数
     * @param url
     * @return
     */
    private int getPage(String url) throws IOException {
        Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
        String pageStr = conn.execute().body();
        JSONObject result = new JSONObject(pageStr);
        int count = result.getInt("return");
        return count % 22 == 0 ? count / 22 : (count / 22) + 1;
    }
}
