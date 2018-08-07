package com.silita.china.guangdong;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
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
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;

import static com.snatch.common.SnatchContent.*;

/**
 * 广东省招标投标监管网   http://www.gdzbtb.gov.cn
 * Created by maofeng on 2018/3/1.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "GuangDongztbjgw")
public class GuangDongztbjgw extends BaseSnatch {


    private static final String source = "guangd";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "http://www.gdzbtb.gov.cn/login?type=zgysgg",
                "http://www.gdzbtb.gov.cn/login?type=zgyswj",
                "http://www.gdzbtb.gov.cn/login?type=zgscbg",
                "http://www.gdzbtb.gov.cn/login?type=zbgg",
                "http://www.gdzbtb.gov.cn/login?type=zbwj",
                "http://www.gdzbtb.gov.cn/login?type=pbbg",
                "http://www.gdzbtb.gov.cn/login?type=zbhxrgs",
                "http://www.gdzbtb.gov.cn/login?type=zbjg"
        };
        String interfaceUrl = "http://www.gdzbtb.gov.cn/bid/list";
        String[] types = {"zgysgg", "zgyswj", "zgscbg", "zbgg", "zbwj", "pbbg", "zbhxrgs", "zbjg"};
        String[] catchTypes = {ZI_GE_YU_SHEN_TYPE, ZI_GE_YU_SHEN_TYPE, ZI_GE_YU_SHEN_TYPE, ZHAO_BIAO_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHONG_BIAO_TYPE};

        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            JSONObject jsonObject;
            Connection conn = null;
            Document doc = null;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }

                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(interfaceUrl + types[i].substring(0, 1).toUpperCase() + types[i].substring(1)).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36").timeout(1000 * 60).ignoreHttpErrors(true).ignoreContentType(true);
                    conn.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//                    conn.header("Cookie", "__jsluid=db6b2965f9b4a96b34df97fa2cea28b0; tabmode=1; tl.session.id=319dfd7021c94447994d4f26e3d4b031; JSESSIONID=266EC28DE7316D13AD9C28E4A4C12864");
                    conn.header("X-Requested-With", "XMLHttpRequest");
                    conn.data("draw", String.valueOf(pagelist));
                    conn.data("columns[0][data]", "id");
                    conn.data("columns[0][name]", "");
                    conn.data("columns[0][searchable]", "true");
                    conn.data("columns[0][orderable]", "false");
                    conn.data("columns[0][search][value]", "");
                    conn.data("columns[0][search][regex]", "false");
                    conn.data("start", String.valueOf((pagelist - 1) * 20));
                    conn.data("length", "20");
                    conn.data("search[value]", "");
                    conn.data("search[regex]", "false");
                    conn.data("page", String.valueOf(pagelist));
                    conn.data("type", types[i]);
                    conn.data("xmmc", "");
                    conn.data("rows", "20");
                    doc = conn.post();

                    if (page == 1) {
                        jsonObject = new JSONObject(doc.text());
                        String pageText = jsonObject.get("recordsTotal").toString();
                        int total = Integer.parseInt(pageText);
                        page = total % 20 == 0 ? total / 20 : total / 20 + 1;
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                        SnatchLogger.debug("总" + page + "页");
                    }

                    jsonObject = new JSONObject(doc.text());
                    String dateStr = jsonObject.get("data").toString();
                    JSONArray trs = new JSONArray(dateStr);
                    for (int row = 0; row < trs.length(); row++) {
                        String datatype = trs.getJSONObject(row).getString("datatype");
                        String detailUrl = "http://www.gdzbtb.gov.cn/bid/detail" + datatype.substring(0, 1).toUpperCase() + datatype.substring(1) + "?id=" + trs.getJSONObject(row).getString("id");
                        String openDate = trs.getJSONObject(row).getString("publishdate");
                        String title = trs.getJSONObject(row).getString("title");
                        String projArea = trs.getJSONObject(row).getString("szdq");
                        String type = trs.getJSONObject(row).getString("type");
                        Notice notice = new Notice();

                        // 地区维度
                        if (!projArea.contains("省")) {
                            Dimension dm = new Dimension();
                            dm.setProjDq(projArea);
                            notice.setDimension(dm);
                        }

                        notice.setProvince("广东省");
                        notice.setProvinceCode("gds");
                        notice.setCatchType(catchTypes[i]);
                        if (type.contains("采购")) {
                            notice.setNoticeType("政府采购");
                        } else {
                            notice.setNoticeType("工程建设");
                        }
                        notice.setUrl(detailUrl);
                        notice.setTitle(title);
                        notice.setOpendate(openDate);
                        CURRENT_PAGE_LAST_OPENDATE = openDate;
                        notice.setSnatchNumber(snatchNumber);
                        notice.setAreaRank(PROVINCE);
                        notice.setSource(source);
                        //详情信息入库，获取增量页数
                        if (notice.getNoticeType().contains("采购")) {
                            page = govDetailHandle(notice, pagelist, page, row, trs.length());
                        } else {
                            page = detailHandle(notice, pagelist, page, row, trs.length());
                        }
                    }
                    //===入库代码====
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        String detailUrl = "http://www.gdzbtb.gov.cn/platform/attach/getAttachList";
        String detailId = href.substring(href.lastIndexOf("=") + 1);
        Connection conn = Jsoup.connect(detailUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36").timeout(1000 * 60).ignoreHttpErrors(true).ignoreContentType(true);
        conn.data("parentId", detailId);
        conn.data("parentType", "GGNR");
        conn.data("rows", "999999");
        conn.data("page", "1");
        Document doc = conn.post();
        JsonNode detail = Util.getContentByJson(doc.text());

        //文件
        String recordsTotal = detail.findPath("recordsTotal").asText();
        if ("0".equals(recordsTotal)) {
            conn = Jsoup.connect(detailUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36").timeout(1000 * 60).ignoreHttpErrors(true).ignoreContentType(true);
            conn.data("parentId", detailId);
            conn.data("parentType", "ATTACH");
            conn.data("rows", "999999");
            conn.data("page", "1");
            doc = conn.post();
            detail = Util.getContentByJson(doc.text());
        }

        String content = null;
        if (!StringUtils.isEmpty(detail.findPath("pdfPath").asText())) {
            content = SnatchUtils.readPDFTwo("http://www.gdzbtb.gov.cn/platform/attach/viewPdf?id=" + detail.findPath("id").asText());
        } else if (!StringUtils.isEmpty(detail.findPath("filePath").asText())) {
            if (detail.findPath("filePath").asText().contains(".doc") || detail.findPath("filePath").asText().contains(".docx")) {
                content = SnatchUtils.readWordTwo("http://www.gdzbtb.gov.cn/platform/attach/download?id=" + detail.findPath("id").asText());
            } else if(detail.findPath("filePath").asText().contains(".pdf")) {
                content = SnatchUtils.readPDFTwo("http://www.gdzbtb.gov.cn/platform/attach/download?id=" + detail.findPath("id").asText());
            }
        } else {

        }
        notice.setContent(content);
        return notice;
    }
}
