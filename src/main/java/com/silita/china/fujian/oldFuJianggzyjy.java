package com.silita.china.fujian;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.gson.internal.LinkedTreeMap;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.codehaus.jackson.JsonNode;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.snatch.common.SnatchContent.*;

/**
 * 福建省公共资源交易电子公共服务平台    https://www.fjggfw.gov.cn
 * https://www.fjggfw.gov.cn/Website/FJBID_DATA/FJBID_DATA_LIST.aspx    历史数据
 * Created by maofeng on 2018/3/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "oldFuJianggzyjy")
public class oldFuJianggzyjy extends BaseSnatch {

    private static final String source = "fuj";
    private Map<String, String> cookies = new LinkedTreeMap<>();

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    /**
     * 获取页面生成的cookie
     * @return
     */
    public Map<String, String> obtainCookies() throws Exception {
        cookies = new HashMap<>();
        WebClient webClient = new WebClient();
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setTimeout(50000);
        webClient.getPage("https://www.fjggfw.gov.cn/Website/JYXXNew.aspx?category=GCJS");
        webClient.waitForBackgroundJavaScript(30000);
        Set<Cookie> cookie = webClient.getCookieManager().getCookies();
        if(cookie != null && cookie.size() > 0) {
            Iterator iter = cookie.iterator();
            while (iter.hasNext()) {
                Cookie tempCookie = (Cookie) iter.next();
                cookies.put(tempCookie.getName(), tempCookie.getValue());
            }
        }
        return cookies;
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "https://www.fjggfw.gov.cn/Website/FJBID_DATA/FJBID_DATA_LIST.aspx"
        };
        String interfaceUrl = "https://www.fjggfw.gov.cn/Website/FJBID_DATA/FjbidHandler.ashx";

        Connection conn = null;
        Document doc = null;

        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    conn = Jsoup.connect(interfaceUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    conn.cookies(cookies);
                    conn.data("flag", "GetList");
                    conn.data("pageNo", String.valueOf(pagelist));
                    conn.data("pageSize", "10");
                    conn.data("proArea", "-1");
                    conn.data("announcementType", "-1");
                    conn.data("ProType", "-1");
                    conn.data("xmlx", "-1");
                    conn.data("projectName", "");
                    conn.data("TopTime", "");
                    conn.data("EndTime", "");
                    conn.data("rrr", String.valueOf(Math.random()));
                    String docStr = conn.execute().body();
                    JsonNode jsonNode = SnatchUtils.getJsonNode(docStr);
                    if (page == 1) {
                        //获取总页数
                        int count = jsonNode.findPath("total").asInt();
                        page = count % 10 == 0 ? count / 10 : count / 10 + 1;
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    SnatchLogger.debug("第" + pagelist + "页");

                    JsonNode list = jsonNode.findPath("data");
                    for (int row = 0; row < list.size(); row++) {
                        String detailId = list.get(row).findPath("ZN_ID").asText();
                        String detailUrl = "https://www.fjggfw.gov.cn/Website/FJBID_DATA/CONTENT.aspx?ID=" + detailId;
                        Connection detailConn = Jsoup.connect(interfaceUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true).ignoreContentType(true);
                        detailConn.cookies(cookies);
                        detailConn.header("Content-Type", "application/json;charset=UTF-8");
                        detailConn.data("rrr", String.valueOf(Math.random()));
                        detailConn.data("flag", "GetModel");
                        detailConn.data("ID", detailId);
                        String text = detailConn.execute().body();
                        JSONArray details = null;
                        try {
                            details = new JSONArray(text);
                        } catch (Exception e) {
                            System.out.println("！！！这个页面有问题！！！");
                            e.printStackTrace();
                        }
                        if (details != null) {
                            //json字符串包含，招标、中标、其他公告
                            for (int j = 0; j < details.length(); j++) {
                                Notice notice = new Notice();
                                JSONObject detail = new JSONObject(details.get(j).toString());
                                String ggType = detail.getString("GGTYPE");
                                JsonNode data = SnatchUtils.getJsonNode(detail.get("DATA").toString());
                                String title = null;
                                if ("1".equals(ggType)) {
                                    title = data.findPath("ZN_TITLE").asText();
                                } else if ("2".equals(ggType) || "3".equals(ggType)) {
                                    title = data.findPath("ZR_TITLE").asText();
                                } else if ("4".equals(ggType)) {
                                    title = data.findPath("ZF_TITLE").asText();
                                }
                                String publishDate = data.findPath("CRTIME").asText().substring(0, 10);
                                String content = null;
                                if ("4".equals(ggType)) {
                                    content = data.findPath("ZF_QUESTION").asText();
                                } else {
                                    content = data.findPath("CONTENT").asText();
                                }

                                if (title.contains("采购")) {
                                    notice.setNoticeType("政府采购");
                                } else {
                                    notice.setNoticeType("工程建设");
                                }
                                notice.setCatchType(getCatchType(ggType, notice.getNoticeType()));
                                notice.setUrl(detailUrl + "&notice=" + j);
                                notice.setTitle(title);
                                notice.setOpendate(publishDate);
                                notice.setContent(content);
                                notice.setSnatchNumber(snatchNumber);
                                notice.setAreaRank(PROVINCE);
                                notice.setSource(source);
                                //详情信息入库，获取增量页数
                                if (notice.getNoticeType().contains("采购")) {
                                    page = govDetailHandle(notice, pagelist, page, row, details.length());
                                } else {
                                    page = detailHandle(notice, pagelist, page, row, details.length());
                                }
                            }
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
    public Notice detail(String href, Notice notice, String catchType) {
        return notice;
    }

    public String getCatchType(String ggType, String noticeType) {
        String catchType = "";
        if (noticeType.equals("工程建设")) {
            switch (ggType) {
                case "1":
                    catchType = "1";
                    break;
                case "2":
                    catchType = GENG_ZHENG_TYPE;
                    break;
                case "3":
                    catchType = DA_YI_TYPE;
                    break;
                case "4":
                    catchType = "2";
                    break;
                case "5":
                    catchType = "2";
                    break;
                case "6":
                    catchType = ZI_GE_YU_SHEN_TYPE;
                    break;
                case "7":
                    catchType = LIU_BIAO_TYPE;
                    break;
                default:
                    break;
            }
        } else {
            switch (ggType) {
                case "1":
                    catchType = "1";
                    break;
                case "2":
                    catchType = "2";
                    break;
                case "3":
                    catchType = HE_TONG_TYPE;
                    break;
                case "4":
                    catchType = GENG_ZHENG_TYPE;
                    break;
                default:
                    break;
            }
        }
        return catchType;
    }

}
