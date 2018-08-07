package com.silita.china.shandong;

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

import java.util.Map;
import java.util.Random;

import static com.snatch.common.SnatchContent.*;

/**
 * 山东省公共资源交易中心  http://www.sdsggzyjyzx.gov.cn/
 * http://www.sdggzyjy.gov.cn/queryContent-jyxxgg.jspx?channelId=78 工程建设 招标公告
 * http://www.sdggzyjy.gov.cn/queryContent-jyxxgs.jspx?channelId=78 工程建设 公Z示信息
 * http://www.sdggzyjy.gov.cn/queryContent-jyxxgg.jspx?channelId=79 政府采购 招标公告
 * http://www.sdggzyjy.gov.cn/queryContent-jyxxgs.jspx?channelId=79 政府采购 公示信息
 * Created by gmy on 2018/6/27.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "ShanDSongggzyjyw")
public class ShanDSongggzyjyw extends BaseSnatch {

    private static final String source = "shand";

    /**
     * 代理服务器信息
     */
    public static final String PROXY_HOST = "http-dyn.abuyun.com";
    public static final Integer PROXY_PORT = 9020;
    public static final String PROXY_USER = "H42I0796HK140EUD";
    public static final String PROXY_PASSWD = "169AE1671CB4912F";

    private int min = 1;
    private int max = 5;
    private Random random = new Random();
    Map<String, String> cookies = null;

    @Test
    @Override
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "http://www.sdggzyjy.gov.cn/queryContent_1-jyxxgg.jspx?channelId=78",
                "http://www.sdggzyjy.gov.cn/queryContent_1-jyxxgs.jspx?channelId=78",
                "http://www.sdggzyjy.gov.cn/queryContent_1-jyxxgg.jspx?channelId=79",
                "http://www.sdggzyjy.gov.cn/queryContent_1-jyxxgs.jspx?channelId=79"
        };
        String[] catchTypes = {ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE};

        Connection conn;
        Document doc;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===查询url前次抓取的情况（最大页数与公示时间）===
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].replace("queryContent_1", "queryContent_" + pagelist);
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    if (i == 0 && pagelist == 1) {
                    } else {
                        conn.cookies(cookies);
                    }
//                    Authenticator.setDefault(new Authenticator() {
//                        public PasswordAuthentication getPasswordAuthentication() {
//                            return new PasswordAuthentication(PROXY_USER, PROXY_PASSWD.toCharArray());
//                        }
//                    });
//                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT));
//                    conn = conn.proxy(proxy);
                    doc = conn.get();
                    if (page == 1) {
                        if (i == 0) {
                            cookies = conn.response().cookies();
                        }
                        String pageCont = doc.select(".pages-list").first().select("li").first().text();
                        page = Integer.valueOf(pageCont.substring(pageCont.lastIndexOf("/") + 1, pageCont.indexOf("页")));
                        SnatchLogger.debug("总" + page + "页");
                        //===入计算本次应该抓取的总页数====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }

                    Elements trs = doc.select(".article-list-a").first().select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String detailUrl = trs.get(row).select("div").first().select("a").first().absUrl("href");
                        String title = trs.get(row).select("div").first().select("a").text();
                        if (title.contains("】")) {
                            title = title.substring(title.indexOf("】") + 1, title.length());
                        }
                        String publishDate = trs.get(row).select("div").first().select(".list-times").text().substring(0, 10).trim();

                        Notice notice = new Notice();
                        notice.setProvince("山东省");
                        notice.setProvinceCode("shds");
                        notice.setCatchType(catchTypes[i]);
                        if (i <= 1) {
                            notice.setNoticeType("工程建设");
                        } else {
                            notice.setNoticeType("政府采购");
                        }
                        notice.setUrl(detailUrl);
                        notice.setTitle(title);
                        notice.setOpendate(publishDate);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setAreaRank(PROVINCE);
                        notice.setSource(source);
                        CURRENT_PAGE_LAST_OPENDATE = publishDate;
                        if (notice.getNoticeType().contains("采购")) {
                            page = govDetailHandle(notice, pagelist, page, row, trs.size());
                        } else {
                            page = detailHandle(notice, pagelist, page, row, trs.size());
                        }
                        //随机暂停几秒
                        Thread.sleep(1000 * (random.nextInt(max) % (max - min + 1)));
                    }
                    //===入库代码====
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Connection conn = Jsoup.connect(href).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
        if (cookies != null) {
            conn.cookies(cookies);
        }
//        Authenticator.setDefault(new Authenticator() {
//            public PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(PROXY_USER, PROXY_PASSWD.toCharArray());
//            }
//        });
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT));
//        conn = conn.proxy(proxy);
        Document doc = conn.get();
        String content = doc.select(".div-article2").html();
        notice.setContent(content);
        return notice;
    }
}
