package com.silita.china.fujian;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.codehaus.jackson.JsonNode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.snatch.common.SnatchContent.*;

/**
 * 福建省公共资源交易电子公共服务平台    https://www.fjggfw.gov.cn
 * https://www.fjggfw.gov.cn/Website/JYXXNew.aspx   工程建设/政府采购
 * <p>
 * Created by maofeng on 2018/3/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "FuJianggzyjy")
public class FuJianggzyjy extends BaseSnatch {


    private static final String source = "fuj";
    private Map<String, String> cookies = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    @Test
    public void run() throws Exception {
        obtainCookies();
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
                "https://www.fjggfw.gov.cn/Website/JYXXNew.aspx?category=GCJS",
                "https://www.fjggfw.gov.cn/Website/JYXXNew.aspx?category=ZFCG"
        };
        String postUrl = "https://www.fjggfw.gov.cn/Website/AjaxHandler/BuilderHandler.ashx";

        Connection conn = null;
        Document doc = null;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        calendar.add(Calendar.DAY_OF_MONTH, +2);
        String topTime = sdf.format(calendar.getTime()) + " 00:00:00";
        String endTime = sdf.format(System.currentTimeMillis()) + " 23:59:59";
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
                    conn = Jsoup.connect(postUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    conn.cookies(cookies);
                    conn.data("OPtype", "GetListNew");
                    conn.data("pageNo", String.valueOf(pagelist));
                    conn.data("pageSize", "10");
                    conn.data("proArea", "-1");
                    conn.data("category", i == 0 ? "GCJS" : "ZFCG");
                    conn.data("announcementType", "-1");
                    conn.data("ProType", "-1");
                    conn.data("xmlx", "-1");
                    conn.data("projectName", "");
                    conn.data("TopTime", topTime);
                    conn.data("EndTime", endTime);
                    conn.data("rrr", Math.random() + "");
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

                    JsonNode lis = jsonNode.findPath("data");
                    for (int row = 0; row < lis.size(); row++) {
                        String idCode = lis.get(row).findPath(i == 0 ? "M_ID" : "PROCODE").asText().trim();
                        idCode = idCode.contains(".") ? idCode.substring(0, idCode.indexOf(".")) : idCode;
                        String ggType = lis.get(row).findPath("GGTYPE").asText().trim();
                        String date = lis.get(row).findPath("TM").asText();
                        if (SnatchUtils.isNotNull(idCode)) {
                            String projType = lis.get(row).findPath("PROTYPE_TEXT").asText().trim();
                            String title = lis.get(row).findPath("NAME").asText().trim();
                            // 政府采购公告只抓工程类
                            if ((i == 1 && !"工程类".equals(projType)) || "招标公告与资格预审公告".equals(title)) {
                                SnatchLogger.debug("-----非工程类政府采购公告-----");
                                continue;
                            }
                            Notice notice = new Notice();
                            String conturl = "";
                            String syncUrl = "";
                            switch (i) {
                                case 0:
                                    conturl = "https://www.fjggfw.gov.cn/Website/JYXX_GCJS.aspx?ID=" + idCode + "&GGTYPE=" + ggType;
                                    syncUrl = "https://www.fjggfw.gov.cn/Website/AjaxHandler/BuilderHandler.ashx?OPtype=GetGGInfoPC&ID=" + idCode + "&GGTYPE=" + ggType + "&url=AjaxHandler%2FBuilderHandler.ashx";
                                    notice.setSyncUrl(syncUrl);
                                    break;
                                case 1:
                                    conturl = "https://www.fjggfw.gov.cn/Website/JYXX_Content/ZFCG.aspx?PROCODE=" + idCode + "&GGTYPE=" + ggType;
                                    break;
                                default:
                                    break;
                            }

                            date = date.substring(0, 10);
                            notice.setProvince("福建省");
                            notice.setProvinceCode("fjs");

                            notice.setCatchType(getCatchType(ggType, i));
                            notice.setNoticeType(i == 0 ? "工程建设" : "政府采购");
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setAreaRank(PROVINCE);
                            notice.setSource(source);

                            Dimension dm = new Dimension();
                            dm.setProjType(projType); // 项目类型

                            String projDq = lis.get(row).findPath("AREANAME").asText().trim();
                            if (SnatchUtils.isNotNull(projDq) && !projDq.contains("省")) {
                                dm.setProjDq(projDq); // 项目地区
                            }
                            notice.setDimension(dm);

                            page = detailHandle(notice, pagelist, page, row, lis.size());
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
        try {
            if ("工程建设".equals(notice.getNoticeType())) {
                Connection connection = Jsoup.connect(href).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true).ignoreContentType(true);
                connection.cookies(cookies);
                String body = connection.execute().body().toString();
                JsonNode jsonNode = SnatchUtils.getJsonNode(body);
                JsonNode dataNode = jsonNode.findPath("data");
                if (dataNode.size() > 0) {
                    String content = dataNode.get(0).asText();
                    Document dd = Jsoup.parse(content);
                    content = dd.select("body").first().html();
                    content = content.replace("Report", "");
                    notice.setContent(content);
                }
                if ("1".equals(notice.getCatchType())) {
                    // 招标公告获取维度
                    Dimension dm = notice.getDimension();
                    String bmEndDate = jsonNode.findPath("baseInfo").findPath("T1").asText().trim();    // 报名结束时间
                    dm.setBmEndDate("null".equals(bmEndDate) ? "" : bmEndDate.substring(0, 10));
                    dm.setBmEndTime(SnatchUtils.isNull(dm.getBmEndDate()) ? "" : bmEndDate.split(" ")[1]);

                    String assureEndDate = jsonNode.findPath("baseInfo").findPath("T3").asText().trim();//保证金截止时间
                    dm.setAssureEndDate("null".equals(assureEndDate) ? "" : assureEndDate.substring(0, 10));
                    dm.setAssureEndTime(SnatchUtils.isNull(dm.getAssureEndDate()) ? "" : assureEndDate.split(" ")[1]);

                    String tbEndDate = jsonNode.findPath("baseInfo").findPath("T4").asText().trim();    // 投标截止时间
                    dm.setTbEndDate("null".equals(tbEndDate) ? "" : tbEndDate.substring(0, 10));
                    dm.setTbEndTime(SnatchUtils.isNull(dm.getTbEndDate()) ? "" : tbEndDate.split(" ")[1]);
                    notice.setDimension(dm);
                }

                // 关联公告
                JsonNode jn = jsonNode.findPath("node");
                if (jn.size() > 1) {
                    Dimension dm = notice.getDimension();
                    String templateUrl = notice.getUrl().substring(0, notice.getUrl().lastIndexOf("&GGTYPE"));
                    String relation_url = "";
                    for (int i = 0; i < jn.size(); i++) {
                        String ggType = jn.get(i).findPath("GGTYPE").asText().trim();
                        String relation = templateUrl + "&GGTYPE=" + ggType;
                        if (!relation.equals(notice.getUrl())) {
                            if (SnatchUtils.isNull(relation_url)) {
                                relation_url = relation;
                            } else {
                                if (relation_url.contains(relation)) {
                                    continue;
                                }
                                relation_url += "," + relation;
                            }
                        }
                    }
                    dm.setRelation_url(relation_url);
                    notice.setDimension(dm);
                }
            } else {
                Connection conn = Jsoup.connect("https://www.fjggfw.gov.cn/Website/AjaxHandler/BuilderHandler.ashx").userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                conn.cookies(cookies);
                String proCode = href.substring(href.indexOf("?PROCODE=") + 9, href.indexOf("&GGTYPE="));
                conn.data("OPtype", "GetJYXXContentZFCG");
                conn.data("PROCODE", proCode);
                conn.data("rrr", Math.random() + "");
                Document doc = conn.post();
                JsonNode jsonNode = SnatchUtils.getJsonNode(doc.text());
                String content = jsonNode.findPath("CONTENT").asText();
                content = content.replace("Report", "");
                if (SnatchUtils.isNull(content)) {
                    String pbrchaserName = jsonNode.findPath("PURCHASER_NAME").asText();
                    String supplierName = jsonNode.findPath("SUPPLIER_NAME").asText();
                    String amount = jsonNode.findPath("CONTRACT_AMOUNT").asText();
                    String unitText = jsonNode.findPath("PRICE_UNIT_TEXT").asText();
                    String codeText = jsonNode.findPath("CURRENCY_CODE_TEXT").asText();
                    String contractTerm = jsonNode.findPath("CONTRACT_TERM").asText();

                    content += "<div class=\"detail_content\"><table class=\"detail_Table\" cellspacing=\"1\" cellpadding=\"1\"><tbody>";
                    content += "<tr><th>采购人名称</th><td>  " + pbrchaserName + "  </td></tr>";
                    content += "<tr><th>中标（成交）供应商名称</th><td>" + supplierName + "</td></tr>";
                    content += "<tr><th>合同金额</th><td>" + amount + unitText + codeText + "</td></tr>";
                    content += "<tr><th>合同期限</th><td> " + contractTerm + " </td></tr>";
                    content += "</tbody></table></div>";
                    content += "</div></div>";

                }
                notice.setContent(content);
            }
        } catch (Exception e) {
        }
        return notice;
    }

    public String getCatchType(String ggType, int i) {
        String catchType = "";
        if (i == 0) {
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
