package com.silita.china.hunan.zhongbiao;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.HttpRequestUtils;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
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
import java.text.SimpleDateFormat;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by maofeng on 2017/7/20.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanShaoYangShijszbw")
public class HuNanShaoYangShijszbw extends BaseSnatch {

    /**
     * 最大重试次数
     */
    private int maxRetryTimes = 5;

    /**
     * 重试休息间隙 ms
     */
    private int retrySleepMillis = 1000;


    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://ggzy.shaoyang.gov.cn/TenderProject/GetTpList?page=1&records=15&name=&publishbegintime=&publishendtime=&IsShowOld=true&category=房建市政,水利,交通运输,土地开发整理,其他",
                "http://ggzy.shaoyang.gov.cn/TenderProject/GetSupList?page=1&records=15&name=&publishbegintime=&publishendtime=&IsShowOld=true&category=房建市政,水利,交通运输,土地开发整理,其他",
                "http://ggzy.shaoyang.gov.cn/TenderProject/GetTenderQuestion?page=1&records=15&name=&publishbegintime=&publishendtime=&IsShowOld=true&category=房建市政,水利,交通运输,土地开发整理,其他",
                "http://ggzy.shaoyang.gov.cn/TenderProject/GetBidderList?page=1&records=15&name=&publishbegintime=&publishendtime=&IsShowOld=true&category=房建市政,水利,交通运输,土地开发整理,其他",
                "http://ggzy.shaoyang.gov.cn/TenderProject/GetTpList?page=1&records=15&name=&publishbegintime=&publishendtime=&IsShowOld=true&category=政府采购",
                "http://ggzy.shaoyang.gov.cn/TenderProject/GetSupList?page=1&records=15&name=&publishbegintime=&publishendtime=&IsShowOld=true&category=政府采购",
                "http://ggzy.shaoyang.gov.cn/TenderProject/GetTenderQuestion?page=1&records=15&name=&publishbegintime=&publishendtime=&IsShowOld=true&category=政府采购",
                "http://ggzy.shaoyang.gov.cn/TenderProject/GetBidderList?page=1&records=15&name=&publishbegintime=&publishendtime=&IsShowOld=true&category=政府采购",
                "http://ggzy.shaoyang.gov.cn/ContractPublicity/GetConPublicityList?page=1&records=15&name=&publishbegintime=&publishendtime=&IsShowOld=true&category=政府采购"
        };
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            Connection conn = null;
            Document doc = null;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && (page > pageTemp || pagelist > page)) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].replace("page=1", "page=" + pagelist);
                    }
                    XxlJobLogger.log("第" + pagelist + "页");
                    conn = Jsoup.connect(url).ignoreContentType(true).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        //获取总页数
                        page = getAllPage(doc);
                        pageTemp = page;
                        XxlJobLogger.log("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    //获取当前页面的公告目录
                    JsonNode lis = Util.getContentByJson(doc.text());
                    lis = lis.findPath("json");
                    for (int row = 0; row < lis.size(); row++) {
                        String id;
                        if (i == 0 || i == 4) {
                            id = lis.get(row).findPath("id").asText();
                        } else {
                            id = lis.get(row).findPath("TpId").asText();
                        }
                        //公告标题
                        String title = lis.get(row).findPath("Title").asText();
                        //公告日期
                        String date = lis.get(row).findPath("time").asText();
                        date = sdf.format(sdf.parse(date));
                        //===入库代码====
                        Notice notice = new Notice();
                        if (i < 4) {
                            notice.setNoticeType("工程建设");
                        } else {
                            notice.setNoticeType("政府采购");
                        }
                        String type = "";
                        String subHref = "";
                        switch (i) {
                            case 0:
                                notice.setCatchType(ZHAO_BIAO_TYPE);
                                type = "招标公告";
                                subHref = "http://ggzy.shaoyang.gov.cn/TenderFlow/GetTpInfo?tpId=";
                                break;
                            case 1:
                                notice.setCatchType(BU_CHONG_TYPE);
                                type = "补充通知";
                                subHref = "http://ggzy.shaoyang.gov.cn/supplenotice/GetInfosByTpId?tpId=";
                                break;
                            case 2:
                                notice.setCatchType(DA_YI_TYPE);
                                type = "答疑";
                                subHref = "http://ggzy.shaoyang.gov.cn/TenderQuestion/GetInfosByTpId?tpId=";
                                break;
                            case 3:
                                notice.setCatchType(ZHONG_BIAO_TYPE);
                                type = "中标候选人公示";
                                subHref = "http://ggzy.shaoyang.gov.cn/BidderPublic/GetInfosByTpId?tpId=";
                                break;
                            case 4:
                                notice.setCatchType(ZHAO_BIAO_TYPE);
                                type = "采购公告";
                                subHref = "http://ggzy.shaoyang.gov.cn/TenderFlow/GetTpInfo?tpId=";
                                break;
                            case 5:
                                notice.setCatchType(BU_CHONG_TYPE);
                                type = "补充通知";
                                subHref = "http://ggzy.shaoyang.gov.cn/supplenotice/GetInfosByTpId?tpId=";
                                break;
                            case 6:
                                notice.setCatchType(DA_YI_TYPE);
                                type = "答疑";
                                subHref = "http://ggzy.shaoyang.gov.cn/TenderQuestion/GetInfosByTpId?tpId=";
                                break;
                            case 7:
                                notice.setCatchType(ZHONG_BIAO_TYPE);
                                type = "中标公示";
                                subHref = "http://ggzy.shaoyang.gov.cn/BidderPublic/GetInfosByTpId?tpId=";
                                break;
                            case 8:
                                notice.setCatchType(ZHONG_BIAO_TYPE);
                                type = "合同公示";
                                subHref = "http://ggzy.shaoyang.gov.cn/ContractPublicity/GetInfosByTpId?tpId=";
                                break;
                        }
                        notice.setProvince("湖南省");
                        notice.setProvinceCode("huns");
                        notice.setCity("邵阳市");
                        notice.setCityCode("sy");
                        notice.setAreaRank(CITY);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setTitle(title);
                        notice.setSyncUrl(subHref + id);
                        if(i < 4) {
                            notice.setUrl("http://ggzy.shaoyang.gov.cn/新流程/招投标信息/jyxx_x.aspx?iq=x&type=" + type + "&tpid=" + id);
                        } else {
                            notice.setUrl("http://ggzy.shaoyang.gov.cn/新流程/招投标信息/jyxx_x.aspx?iq=gc&type=" + type + "&tpid=" + id);
                        }
                        notice.setOpendate(date);
                        CURRENT_PAGE_LAST_OPENDATE = notice.getOpendate();
                        if (notice.getNoticeType().contains("采购")) {
                            page = govDetailHandle(notice, pagelist, page, row, lis.size());
                        } else {
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

    //获取网址的总页数
    public int getAllPage(Document doc) throws Exception {
        int pageSize = 15;
        int allPage = 0;
        String total = Util.getContentByJson(doc.text()).findPath("msg").asText();
        if (SnatchUtils.isNull(total)) {
            return 1;
        }
        int totalMsg = Integer.parseInt(total);
        if (totalMsg != 0 && totalMsg % pageSize > 0) {
            allPage = totalMsg / pageSize + 1;
        } else if (totalMsg != 0 && totalMsg % pageSize == 0) {
            allPage = totalMsg / pageSize;
        }
        return allPage;
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        JsonNode text = getJsonNode(HttpRequestUtils.get(href));
        String content = text.findPath("json").findPath("招标内容").asText();
        if(content.equals("")) {
            content = text.findPath("json").findPath("修改文本").asText();
        }
        if(content.equals("")) {
            content = text.findPath("json").findPath("内容").asText();
        }
        notice.setContent(content);

        // 获取中标公告的维度信息
        if (ZHONG_BIAO_TYPE.equals(notice.getCatchType())) {
            Dimension dimension = null;
            JsonNode jsonN = text.findParent("json");
            if (SnatchUtils.isNotNull(jsonN.findPath("候选人").findPath("候选人").asText().trim())) {
                dimension = new Dimension();
                dimension.setOneName(jsonN.findPath("候选人").get(0).findPath("候选人").asText().trim());
                if (SnatchUtils.isNotNull(jsonN.findPath("候选人").get(0).findPath("候选人报价").asText())) {
                    String strOneOffer = jsonN.findPath("候选人").get(0).findPath("候选人报价").asText();
                    if (strOneOffer.length() < 10) {
                        double oneOffer = new BigDecimal(strOneOffer.replaceAll("[\\u4e00-\\u9fa5]", "").trim()).doubleValue();
                        double base = 10000;
                        dimension.setOneOffer(String.valueOf(SnatchUtils.mul(oneOffer, base)));
                    }
                } else if (SnatchUtils.isNotNull(jsonN.findPath("候选人").get(0).findPath("中标金额").asText())) {
                    String strOneOffer = jsonN.findPath("候选人").get(0).findPath("中标金额").asText();
                    if (strOneOffer.length() < 10) {
                        double oneOffer = new BigDecimal(strOneOffer.replaceAll("[\\u4e00-\\u9fa5]", "").trim()).doubleValue();
                        double base = 10000;
                        dimension.setOneOffer(String.valueOf(SnatchUtils.mul(oneOffer, base)));
                    }
                }
                String projectTimeLimit = jsonN.findPath("候选人").get(0).findPath("候选人工期").asText().replaceAll("[^0-9]", "").trim();
                dimension.setProjectTimeLimit(SnatchUtils.isNull(projectTimeLimit) ? null : projectTimeLimit);
                String oneProjDuty = jsonN.findPath("候选人").get(0).findPath("候选人项目经理").asText().trim();
                dimension.setOneProjDuty(SnatchUtils.isNull(oneProjDuty) ? null : oneProjDuty);
                notice.setDimension(dimension);
            } else if (jsonN.findPath("Category").asText().contains("采购")) {
                dimension = new Dimension();
                String oneName = jsonN.findPath("候选人").findPath("投标人").asText().trim();
                dimension.setOneName(SnatchUtils.isNull(oneName) ? null : oneName);
                String oneOffer = jsonN.findPath("候选人").findPath("中标金额").asText().trim();
                dimension.setOneOffer(SnatchUtils.isNull(oneOffer) ? null : oneOffer);
                notice.setDimension(dimension);
            }
        } else if (ZHAO_BIAO_TYPE.equals(notice.getCatchType())) {
            Dimension dimension = null;
            JsonNode jsonN = text.findPath("json");
            if (SnatchUtils.isNotNull(jsonN.findPath("投标保证金").asText().trim())) {
                dimension = new Dimension();
                dimension.setTbAssureSum(jsonN.findPath("投标保证金").asText());
                dimension.setZbName(jsonN.findPath("招标人").asText());
                if (SnatchUtils.isNotNull(jsonN.findPath("投标截止时间").asText())) {
                    String bmEndDate = jsonN.findPath("开标时间").asText().substring(0, 10);
                    String bmEndTime = jsonN.findPath("开标时间").asText().substring(10);
                    dimension.setBmEndDate(bmEndDate);
                    dimension.setBmEndTime(bmEndTime);
                }
            }
            notice.setDimension(dimension);
        }
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


}
