package com.silita.china.hunan.zhongbiao;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.HttpRequestUtils;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by hujia on 2017/7/24.
 * 张家界市 张家界公共资源交易网 http://www.zjjsggzy.gov.cn/newsList.html?index=4&type=%E4%BA%A4%E6%98%93%E4%BF%A1%E6%81%AF&xtype=%E5%B7%A5%E7%A8%8B%E5%BB%BA%E8%AE%BE
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanZhangJiaJie")
public class HuNanZhangJiaJie extends BaseSnatch {

    /**
     * 最大重试次数
     */
    private int maxRetryTimes = 5;

    /**
     * 重试休息间隙 ms
     */
    private int retrySleepMillis = 1000;


    @Test
    public void run() throws Exception {
        Outer();
    }

    public void Outer() throws Exception {
        List<Map<String, String>> urlParams = new ArrayList<>();
        //工程建设 招标公告
        Map<String, String> urlParam1 = new HashMap<>();
        urlParam1.put("url", "http://www.zjjsggzy.gov.cn/TenderProject/GetTpList?page=1&records=15&name=&category=%E6%88%BF%E5%BB%BA%E5%B8%82%E6%94%BF%2C%E6%B0%B4%E5%88%A9%2C%E4%BA%A4%E9%80%9A%E8%BF%90%E8%BE%93%2C%E5%9C%9F%E5%9C%B0%E5%BC%80%E5%8F%91%E6%95%B4%E7%90%86%2C%E5%85%B6%E4%BB%96&method=&publishbegintime=&publishendtime=&IsShowOld=true");
        urlParam1.put("catchType", "1");
        urlParam1.put("noticeType", "工程建设");
        urlParam1.put("dataUrl", "http://www.zjjsggzy.gov.cn/TenderFlow/GetTpInfo?tpId=");
        urlParam1.put("baseUrl", "http://www.zjjsggzy.gov.cn/新流程/招投标信息/jyxx_1.html?iq=x&type=招标公告&tpid=");
        urlParam1.put("snatchNumber", SnatchUtils.makeSnatchNumber());
        urlParams.add(urlParam1);
        //工程建设 控制价
        Map<String, String> urlParam2 = new HashMap<>();
        urlParam2.put("url", "http://www.zjjsggzy.gov.cn/TenderProject/GetTenderControl?page=1&records=15&name=&category=%E6%88%BF%E5%BB%BA%E5%B8%82%E6%94%BF%2C%E6%B0%B4%E5%88%A9%2C%E4%BA%A4%E9%80%9A%E8%BF%90%E8%BE%93%2C%E5%9C%9F%E5%9C%B0%E5%BC%80%E5%8F%91%E6%95%B4%E7%90%86%2C%E5%85%B6%E4%BB%96&method=&publishbegintime=&publishendtime=&IsShowOld=true");
        urlParam2.put("catchType", KONG_ZHI_JIA_TYPE);
        urlParam2.put("noticeType", "工程建设");
        urlParam2.put("dataUrl", "http://www.zjjsggzy.gov.cn/TenderProject/GetConPriceInfo?tpId=");
        urlParam2.put("baseUrl", "http://www.zjjsggzy.gov.cn/新流程/招投标信息/jyxx_1.html?iq=x&type=控制价&tpid=");
        urlParam2.put("snatchNumber", SnatchUtils.makeSnatchNumber());
        urlParams.add(urlParam2);
        //工程建设 补充通知
        Map<String, String> urlParam3 = new HashMap<>();
        urlParam3.put("url", "http://www.zjjsggzy.gov.cn/TenderProject/GetSupList?page=1&records=15&name=&category=%E6%88%BF%E5%BB%BA%E5%B8%82%E6%94%BF%2C%E6%B0%B4%E5%88%A9%2C%E4%BA%A4%E9%80%9A%E8%BF%90%E8%BE%93%2C%E5%9C%9F%E5%9C%B0%E5%BC%80%E5%8F%91%E6%95%B4%E7%90%86%2C%E5%85%B6%E4%BB%96&method=&publishbegintime=&publishendtime=&IsShowOld=true");
        urlParam3.put("catchType", BU_CHONG_TYPE);
        urlParam3.put("noticeType", "工程建设");
        urlParam3.put("dataUrl", "http://www.zjjsggzy.gov.cn/supplenotice/GetInfosByTpId?tpId=");
        urlParam3.put("baseUrl", "http://www.zjjsggzy.gov.cn/新流程/招投标信息/jyxx_1.html?iq=x&type=补充通知&tpid=");
        urlParam3.put("snatchNumber", SnatchUtils.makeSnatchNumber());
        urlParams.add(urlParam3);
        //工程建设 答疑
        Map<String, String> urlParam4 = new HashMap<>();
        urlParam4.put("url", "http://www.zjjsggzy.gov.cn/TenderProject/GetTenderQuestion?page=1&records=15&name=&category=%E6%88%BF%E5%BB%BA%E5%B8%82%E6%94%BF%2C%E6%B0%B4%E5%88%A9%2C%E4%BA%A4%E9%80%9A%E8%BF%90%E8%BE%93%2C%E5%9C%9F%E5%9C%B0%E5%BC%80%E5%8F%91%E6%95%B4%E7%90%86%2C%E5%85%B6%E4%BB%96&method=&publishbegintime=&publishendtime=&IsShowOld=true");
        urlParam4.put("catchType", DA_YI_TYPE);
        urlParam4.put("noticeType", "工程建设");
        urlParam4.put("dataUrl", "http://www.zjjsggzy.gov.cn/TenderQuestion/GetInfosByTpId?tpId=");
        urlParam4.put("baseUrl", "http://www.zjjsggzy.gov.cn/新流程/招投标信息/jyxx_1.html?iq=x&type=答疑&tpid=");
        urlParam4.put("snatchNumber", SnatchUtils.makeSnatchNumber());
        urlParams.add(urlParam4);
        //工程建设 中标候选人公示
        Map<String, String> urlParam5 = new HashMap<>();
        urlParam5.put("url", "http://www.zjjsggzy.gov.cn/TenderProject/GetBidderList?page=1&records=15&name=&category=%E6%88%BF%E5%BB%BA%E5%B8%82%E6%94%BF%2C%E6%B0%B4%E5%88%A9%2C%E4%BA%A4%E9%80%9A%E8%BF%90%E8%BE%93%2C%E5%9C%9F%E5%9C%B0%E5%BC%80%E5%8F%91%E6%95%B4%E7%90%86%2C%E5%85%B6%E4%BB%96&method=&publishbegintime=&publishendtime=&IsShowOld=true");
        urlParam5.put("catchType", "2");
        urlParam5.put("noticeType", "工程建设");
        urlParam5.put("dataUrl", "http://www.zjjsggzy.gov.cn/BidderPublic/GetInfosByTpId?tpId=");
        urlParam5.put("baseUrl", "http://www.zjjsggzy.gov.cn/新流程/招投标信息/jyxx_1.html?iq=x&type=中标公示&tpid=");
        urlParam5.put("snatchNumber", SnatchUtils.makeSnatchNumber());
        urlParams.add(urlParam5);
        //政府采购 采购公告
        Map<String, String> urlParam6 = new HashMap<>();
        urlParam6.put("url", "http://www.zjjsggzy.gov.cn/TenderProject/GetTpList?page=1&records=15&name=&category=%E6%94%BF%E5%BA%9C%E9%87%87%E8%B4%AD&method=%E5%85%AC%E5%BC%80%E6%8B%9B%E6%A0%87&publishbegintime=&publishendtime=&IsShowOld=true");
        urlParam6.put("catchType", "1");
        urlParam6.put("noticeType", "政府采购");
        urlParam6.put("dataUrl", "http://www.zjjsggzy.gov.cn/TenderFlow/GetTpInfo?tpId=");
        urlParam6.put("baseUrl", "http://www.zjjsggzy.gov.cn/新流程/招投标信息/jyxx_1.html?iq=x&type=招标公告&tpid=");
        urlParam6.put("snatchNumber", SnatchUtils.makeSnatchNumber());
        urlParams.add(urlParam6);
        //政府采购 补充通知
        Map<String, String> urlParam7 = new HashMap<>();
        urlParam7.put("url", "http://www.zjjsggzy.gov.cn/TenderProject/GetSupList?page=1&records=15&name=&category=%E6%94%BF%E5%BA%9C%E9%87%87%E8%B4%AD&method=%E5%85%AC%E5%BC%80%E6%8B%9B%E6%A0%87&publishbegintime=&publishendtime=&IsShowOld=true");
        urlParam7.put("catchType", BU_CHONG_TYPE);
        urlParam7.put("noticeType", "政府采购");
        urlParam7.put("dataUrl", "http://www.zjjsggzy.gov.cn/supplenotice/GetInfosByTpId?tpId=");
        urlParam7.put("baseUrl", "http://www.zjjsggzy.gov.cn/新流程/招投标信息/jyxx_1.html?iq=x&type=补充通知&tpid=");
        urlParam7.put("snatchNumber", SnatchUtils.makeSnatchNumber());
        urlParams.add(urlParam7);
        //政府采购 答疑
        Map<String, String> urlParam8 = new HashMap<>();
        urlParam8.put("url", "http://www.zjjsggzy.gov.cn/TenderProject/GetTenderQuestion?page=1&records=15&name=&category=%E6%94%BF%E5%BA%9C%E9%87%87%E8%B4%AD&method=%E5%85%AC%E5%BC%80%E6%8B%9B%E6%A0%87&publishbegintime=&publishendtime=&IsShowOld=true");
        urlParam8.put("catchType", DA_YI_TYPE);
        urlParam8.put("noticeType", "政府采购");
        urlParam8.put("dataUrl", "http://www.zjjsggzy.gov.cn/TenderQuestion/GetInfosByTpId?tpId=");
        urlParam8.put("baseUrl", "http://www.zjjsggzy.gov.cn/新流程/招投标信息/jyxx_1.html?iq=x&type=答疑&tpid=");
        urlParam8.put("snatchNumber", SnatchUtils.makeSnatchNumber());
        urlParams.add(urlParam8);
        //政府采购 结果公示
        Map<String, String> urlParam9 = new HashMap<>();
        urlParam9.put("url", "http://www.zjjsggzy.gov.cn/TenderProject/GetBidderList?page=1&records=15&name=&category=%E6%94%BF%E5%BA%9C%E9%87%87%E8%B4%AD&method=%E5%85%AC%E5%BC%80%E6%8B%9B%E6%A0%87&publishbegintime=&publishendtime=&IsShowOld=true");
        urlParam9.put("catchType", "2");
        urlParam9.put("noticeType", "政府采购");
        urlParam9.put("dataUrl", "http://www.zjjsggzy.gov.cn/BidderPublic/GetInfosByTpId?tpId=");
        urlParam9.put("baseUrl", "http://www.zjjsggzy.gov.cn/新流程/招投标信息/jyxx_1.html?iq=x&type=中标公示&tpid=");
        urlParam9.put("snatchNumber", SnatchUtils.makeSnatchNumber());
        urlParams.add(urlParam9);
        for (int page = 0; page < urlParams.size(); page++) {
            Map<String, String> urlParam = urlParams.get(page);
            String url = urlParam.get("url");
            super.queryBeforeSnatchState(url);
            int pageTemp = 0;
            //工程建设打开原文基础链接
            String baseUrl = urlParam.get("baseUrl");
            try {
                Connection conn = Jsoup.connect(url).ignoreContentType(true).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                Document doc = null;
                doc = conn.get();
                if (conn.response().statusCode() == 200) {
                    //访问网站成功，开始分页抓取
                    int allPage = getAllPage(doc);
                    pageTemp = allPage;
                    //===入库代码====
                    allPage = computeTotalPage(allPage, LAST_ALLPAGE);
                    //上次抓取的公告的最大id
                    for (int i = 1; i <= allPage; i++) {
                        String u = url.replace("page=1", "page=" + i);
                        SnatchLogger.debug("第" + i + "页");
                        conn = Jsoup.connect(u).ignoreContentType(true).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                        Document document = null;
                        document = conn.get();
                        //获取当前页面的公告目录
                        JsonNode lis = getJsonNode(document.text());
                        lis = lis.findPath("json");
                        for (int j = 0; j < lis.size(); j++) {
                            String tpId = lis.get(j).findPath("TpId").asText();
                            if (StringUtils.isBlank(tpId)) {
                                tpId = lis.get(j).findPath("id").asText();
                            }
                            //公告标题
                            String title = lis.get(j).findPath("Title").asText().trim();
                            //公告日期
                            String date = null;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            date = sdf.format(sdf.parse(lis.get(j).findPath("time").asText()));
                            //===入库代码====
                            Notice notice = new Notice();
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("张家界市");
                            notice.setCityCode("zjj");
                            String catchType = urlParam.get("catchType");
                            String noticeType = urlParam.get("noticeType");
                            notice.setNoticeType(noticeType);
                            notice.setUrl(baseUrl + tpId);
                            notice.setSyncUrl(urlParam.get("dataUrl") + tpId);
                            notice.setOpendate(date);
                            notice.setSnatchNumber(urlParam.get("snatchNumber"));
                            notice.setAreaRank(CITY);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            notice.setTitle(title);
                            notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                            if (SnatchUtils.isNull(notice.getCatchType())) {
                                notice.setCatchType(catchType);
                            } else {
                                SnatchUtils.judgeCatchType(notice, notice.getCatchType(), catchType);
                            }

                            if (noticeType.contains("采购")) {
                                allPage = govDetailHandle(notice, i, allPage, j, lis.size());
                            } else {
                                allPage = detailHandle(notice, i, allPage, j, lis.size());
                            }
                        }
                        if (i == allPage) {
                            allPage = turnPageEstimate(allPage);
                        }
                    }
                    super.saveAllPageIncrement(url);
                }
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

    //获取网址的总页数
    public int getAllPage(Document doc) throws Exception {
        int pageSize = 15;
        int allPage = 10;
        String total = Util.getContentByJson(doc.text()).findPath("msg").asText();
        if (StringUtils.isNotBlank(total)) {
            int totalMsg = Integer.parseInt(total);
            if (totalMsg != 0 && totalMsg % pageSize > 0) {
                allPage = totalMsg / pageSize + 1;
            } else if (totalMsg != 0 && totalMsg % pageSize == 0) {
                allPage = totalMsg / pageSize;
            }
            SnatchLogger.debug("总" + allPage + "页");
        }
        return allPage;
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        String content = "";
        String json = HttpRequestUtils.get(href);
        JsonNode text = getJsonNode(json);
        //公告内容
        JsonNode jsonNode = text.findPath("json");
        JsonNode contentJsonNode = jsonNode.findPath("内容");
        if (contentJsonNode != null && StringUtils.isNotBlank(contentJsonNode.asText())) {
            content = contentJsonNode.asText().trim();
        } else {
            contentJsonNode = jsonNode.findPath("招标内容");
            if (contentJsonNode != null && StringUtils.isNotBlank(contentJsonNode.asText())) {
                content = contentJsonNode.asText().trim();
            } else {
                contentJsonNode = jsonNode.findPath("修改文本");
                if (contentJsonNode != null && StringUtils.isNotBlank(contentJsonNode.asText())) {
                    content = contentJsonNode.asText().trim();
                } else {
                    SnatchLogger.debug(contentJsonNode.toString());
                    throw new Exception();
                }
            }
        }
        notice.setContent(content);
        getDimension(notice, jsonNode);
        return notice;
    }

    /**
     * 统一获取网页内容
     */
    private JsonNode getJsonNode(String context) {
        int retryTimes = 0;
        do {
            try {
                JsonNode jsonNode = Util.getContentByJson(context);
                if (jsonNode == null) {
                    throw new Exception();
                }
                return jsonNode;
            } catch (Exception ioExc) {
                int sleepMillis = this.retrySleepMillis * (1 << retryTimes);
                if ((retryTimes + 1) < this.maxRetryTimes) {
                    try {
                        SnatchLogger.debug("解析异常，" + sleepMillis + "ms 后重试(第" + (retryTimes + 1) + "次)");
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException interExc) {
                        throw new RuntimeException(interExc);
                    }
                }
            }
        } while (++retryTimes < this.maxRetryTimes);
        throw new RuntimeException();
    }


    // 抓取维度信息
    public void getDimension(Notice no, JsonNode json) throws Exception {
        if (ZHAO_BIAO_TYPE.equals(no.getCatchType()) || ZHONG_BIAO_TYPE.equals(no.getCatchType())) {
            Dimension dm = new Dimension();
            if (ZHAO_BIAO_TYPE.equals(no.getCatchType())) {
                dm.setBmStartDate(no.getOpendate());
            }

            // 招标人
            String zbName = json.findPath("招标人").asText();
            dm.setZbName(SnatchUtils.isNull(zbName) ? null : zbName);

            // 项目金额
            String projSum = json.findPath("项目预算").asText();
            if (SnatchUtils.isNotNull(projSum)) {
                try {
                    Double.valueOf(projSum);
                    projSum = new BigDecimal(json.findPath("项目预算").asDouble() * 10000).toString();
                    if (projSum.contains(".") && projSum.length() - projSum.indexOf(".") > 5) {
                        // 小数位过长保留4位小数
                        DecimalFormat df = new DecimalFormat("#.0000");
                        projSum = df.format(Double.valueOf(projSum));
                    }
                    dm.setProjSum(SnatchUtils.isNull(projSum) || "0".equals(projSum) ? null : projSum);
                } catch (NumberFormatException e) {
                    dm.setProjSum(null);
                }
            } else if (SnatchUtils.isNotNull(json.findPath("预算金额").asText())) {
                projSum = json.findPath("预算金额").asText();
                try {
                    Double.valueOf(projSum);
                    projSum = new BigDecimal(json.findPath("预算金额").asDouble() * 10000).toString();
                    if (projSum.contains(".") && projSum.length() - projSum.indexOf(".") > 5) {
                        // 小数位过长保留4位小数
                        DecimalFormat df = new DecimalFormat("#.0000");
                        projSum = df.format(Double.valueOf(projSum));
                    }
                    dm.setProjSum(SnatchUtils.isNull(projSum) || "0".equals(projSum) ? null : projSum);
                } catch (NumberFormatException e) {
                    dm.setProjSum(null);
                }
            }

            // 投标保证金
            String tbAssureSum = json.findPath("投标保证金").asText();
            if (SnatchUtils.isNotNull(tbAssureSum)) {
                try {
                    Double.valueOf(tbAssureSum);
                    dm.setTbAssureSum(SnatchUtils.isNull(tbAssureSum) || "0".equals(tbAssureSum) ? null : tbAssureSum);
                } catch (NumberFormatException e) {
                    dm.setTbAssureSum(null);
                }
            }


            if (SnatchUtils.isNotNull(json.findPath("标书下载截止时间").asText())) {
                // 报名截止时间
                if (json.findPath("标书下载截止时间").asText().length() >= 10) {
                    String bmEndDate = json.findPath("标书下载截止时间").asText().substring(0, 10).trim();
                    dm.setBmEndDate(SnatchUtils.isNull(bmEndDate) ? null : bmEndDate);
                }
                if (json.findPath("标书下载截止时间").asText().length() >= 16) {
                    String bmEndTime = json.findPath("标书下载截止时间").asText().substring(10, 16).trim();
                    dm.setBmEndTime(SnatchUtils.isNull(bmEndTime) ? null : bmEndTime);
                }
            }

            if (SnatchUtils.isNotNull(json.findPath("保证金缴纳截止时间").asText())) {
                // 投标保证金截止时间
                if (json.findPath("保证金缴纳截止时间").asText().length() >= 10) {
                    String tbAssureEndDate = json.findPath("保证金缴纳截止时间").asText().substring(0, 10).trim();
                    dm.setTbAssureEndDate(SnatchUtils.isNull(tbAssureEndDate) ? null : tbAssureEndDate);
                }
                if (json.findPath("保证金缴纳截止时间").asText().length() >= 16) {
                    String tbAssureEndTime = json.findPath("保证金缴纳截止时间").asText().substring(10, 16).trim();
                    dm.setTbAssureEndTime(SnatchUtils.isNull(tbAssureEndTime) ? null : tbAssureEndTime);
                }
            }

            // 项目类型
            String projType = json.findPath("Category").asText();
            dm.setProjType(SnatchUtils.isNull(projType) ? null : projType);

            // 中介代理机构
            String dlName = json.findPath("中介代理机构").asText();
            dm.setDlName(SnatchUtils.isNull(dlName) ? null : dlName);

            if (SnatchUtils.isNotNull(json.findPath("开标时间").asText())) {
                // 投标截止时间
                if (json.findPath("开标时间").asText().length() >= 10) {
                    String tbEndDate = json.findPath("开标时间").asText().substring(0, 10).trim();
                    dm.setTbEndDate(SnatchUtils.isNull(tbEndDate) ? null : tbEndDate);
                }
                if (json.findPath("开标时间").asText().length() >= 16) {
                    String tbEndTime = json.findPath("开标时间").asText().substring(10, 16).trim();
                    dm.setTbEndTime(SnatchUtils.isNull(tbEndTime) ? null : tbEndTime);
                }
            }

            if (SnatchUtils.isNotNull(json.findPath("项目所属区域").asText())) {
                // 项目地区
                String projXs = json.findPath("项目所属区域").asText();
                dm.setProjDq("张家界");
                if (SnatchUtils.isNotNull(projXs) && !"张家界".equals(projXs) && !"张家界市".equals(projXs)) {
                    dm.setProjXs(projXs);
                }
            }

            // 标书费
            String biaoshuSum = json.findPath("标书费").asText();
            if (SnatchUtils.isNotNull(biaoshuSum) && !"0".equals(biaoshuSum)) {
                dm.setFileCost(biaoshuSum);
            }

            if (ZHONG_BIAO_TYPE.equals(no.getCatchType())) {
                // 中标候选人
                String oneName = json.findPath("候选人").findPath("候选人").asText();
                dm.setOneName(SnatchUtils.isNull(oneName) ? null : oneName);
                // 报价
                if (SnatchUtils.isNotNull(json.findPath("候选人").findPath("候选人报价").asText())) {
                    String oneOffer = new BigDecimal(json.findPath("候选人").findPath("候选人报价").asDouble() * 10000).toString();
                    if (oneOffer.contains(".") && oneOffer.length() - oneOffer.indexOf(".") > 5) {
                        // 小数位过长保留4位小数
                        DecimalFormat df = new DecimalFormat("#.0000");
                        oneOffer = df.format(Double.valueOf(oneOffer));
                    }
                    dm.setOneOffer(SnatchUtils.isNull(oneOffer) ? null : oneOffer);
                } else if (SnatchUtils.isNotNull(json.findPath("候选人").findPath("候选人预算金额").asText())) {
                    String oneOffer = new BigDecimal(json.findPath("候选人").findPath("候选人预算金额").asDouble() * 10000).toString();
                    if (oneOffer.contains(".") && oneOffer.length() - oneOffer.indexOf(".") > 5) {
                        // 小数位过长保留4位小数
                        DecimalFormat df = new DecimalFormat("#.0000");
                        oneOffer = df.format(Double.valueOf(oneOffer));
                    }
                    dm.setOneOffer(SnatchUtils.isNull(oneOffer) ? null : oneOffer);
                }
                // 项目负责人
                String oneProjDuty = json.findPath("候选人").findPath("候选人项目经理").asText();
                dm.setOneProjDuty(SnatchUtils.isNull(oneProjDuty) ? null : oneProjDuty);
                // 项目工期
                String projectTimeLimit = json.findPath("候选人").findPath("候选人工期").asText();
                dm.setProjectTimeLimit(SnatchUtils.isNull(projectTimeLimit) ? null : projectTimeLimit);
            }
            no.setDimension(dm);
        }
    }


}
