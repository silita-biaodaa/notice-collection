package com.silita.china.sichuan;

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
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

import java.net.URL;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by hujia on 2018/03/06
 * 四川省公共资源交易网
 * 招标信息
 * http://www.scggzy.gov.cn/Info/Index/project.html     // 招标公告
 * 政府采购
 * http://www.scggzy.gov.cn/Info/Index/purchase.html     // 采购公告
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "SiChuanggzyjyw")
public class SiChuanggzyjyw extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String urls[] = {
                "http://www.scggzy.gov.cn/Info/GetInfoListNew?keywords=&times=4&timesStart=&timesEnd=&province=&area=&businessType=project&informationType=&industryType=&parm=1530004328450&page=1",
                "http://www.scggzy.gov.cn/Info/GetInfoListNew?keywords=&times=4&timesStart=&timesEnd=&province=&area=&businessType=purchase&informationType=&industryType=&parm=1530004328450&page=1"
        };

        Connection conn = null;
        Document doc = null;

        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].substring(0, urls[i].lastIndexOf("=") + 1) + pagelist;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    String docStr = conn.execute().body();

                    JSONObject object = new JSONObject(docStr);
                    if (pagelist == 1) {
                        page = object.getInt("pageCount");
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                        SnatchLogger.debug("总" + page + "页");
                    }

                    JSONArray datas = new JSONArray(object.get("data").toString());
                    Dimension dim;
                    JSONObject data;
                    for (int row = 0; row < datas.length(); row++) {
                        data = new JSONObject(datas.get(row).toString());
                        String href = "http://www.scggzy.gov.cn" + data.getString("Link");
                        String title = data.getString("Title");
                        title = title.replaceAll("【", "").replaceAll("】", "");
                        String publishDate = data.getString("CreateDateStr");

                        dim = new Dimension();
                        dim.setProjDq(data.getString("username"));
                        dim.setProjType(data.getString("businessType"));
                        String catchType = data.getString("TableName");
                        if (catchType.equals("签约履行")) {
                            continue;
                        }
                        //===入库代码====
                        Notice notice = new Notice();
                        notice.setDimension(dim);
                        notice.setProvince("四川省");
                        notice.setProvinceCode("scs");
                        notice.setAreaRank(PROVINCE);
                        notice.setSnatchNumber(snatchNumber);
                        if (i == 1 || title.contains("采购")) {
                            notice.setNoticeType("政府采购");
                        } else {
                            notice.setNoticeType("工程建设");
                        }
                        String catchTypeNumber = queryCatchTypeNumber(catchType, notice.getNoticeType());
                        notice.setCatchType(catchTypeNumber);
                        notice.setUrl(href);
                        notice.setSource("sichuan");
                        notice.setOpendate(publishDate);
                        CURRENT_PAGE_LAST_OPENDATE = publishDate;
                        //详情信息入库，获取增量页数
                        if (notice.getNoticeType().contains("采购")) {
                            page = govDetailHandle(notice, pagelist, page, row, datas.length());
                        } else {
                            page = detailHandle(notice, pagelist, page, row, datas.length());
                        }
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
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = docCount.select(".titFontname").text();
        Element content = null;
        if ("政府采购".equals(notice.getNoticeType())) {
            if (notice.getCatchType().equals(ZHAO_BIAO_TYPE)) {
                content = docCount.select("#divOne0").select("input").first();
            } else if (notice.getCatchType().equals(GENG_ZHENG_TYPE)) {
                content = docCount.select("#divTwo0").select("input").first();
            } else if (notice.getCatchType().equals(ZHONG_BIAO_TYPE)) {
                content = docCount.select("#divThree0").select("input").first();
            } else if (notice.getCatchType().equals(HE_TONG_TYPE)) {
                content = docCount.select(".detailedIntroduc").last();
                notice.setContent(content.text());
            }
            if(content != null && StringUtils.isEmpty(content.html()) && !notice.getCatchType().equals(HE_TONG_TYPE)) {
                notice.setContent(content.attr("value"));
            }
        } else {
            if (notice.getCatchType().equals(ZHAO_BIAO_TYPE)) {
                content = docCount.select("#divOne0").select("input").first();
            } else if (notice.getCatchType().equals(GENG_ZHENG_TYPE)) {
                content = docCount.select("#divTwo0").select("input").first();
            } else if (notice.getCatchType().equals(ZHONG_ZHI_TYPE)) {
                content = docCount.select("#divTwo0").select("input").first();
            } else if (notice.getCatchType().equals(ZHONG_BIAO_TYPE)) {
                content = docCount.select("#divSeven0").select("input").first();
            } else if (notice.getCatchType().equals(CHENG_QING_TYPE)) {
                content = docCount.select("#divFive0").select("input").first();
            } else if (notice.getCatchType().equals(HE_TONG_TYPE)) {
                content = docCount.select("#divSix0").select("input").first();
            } else {
                content = docCount.select("#divThree0").select("input").first();
            }
            if(content != null && StringUtils.isEmpty(content.html())) {
                notice.setContent(content.attr("value"));
            }
        }
        notice.setTitle(title);
        return notice;
    }

    public String queryCatchTypeNumber(String catchType, String noticeType) {
        String catchTypeNumber = "";
        if ("工程建设".equals(noticeType)) {
            if (catchType.equals("招标公告")) {
                catchTypeNumber = ZHAO_BIAO_TYPE;
            } else if (catchType.equals("评标公示") || catchType.equals("中标候选公示") || catchType.equals("中标公告")) {
                catchTypeNumber = ZHONG_BIAO_TYPE;
            } else if (catchType.equals("签约履行")) {
                catchTypeNumber = HE_TONG_TYPE;
            } else if (catchType.equals("公告变更")) {
                catchTypeNumber = GENG_ZHENG_TYPE;
            } else if (catchType.equals("补遗/澄清")) {
                catchTypeNumber = CHENG_QING_TYPE;
            } else if (catchType.equals("流标或终止公告")) {
                catchTypeNumber = ZHONG_ZHI_TYPE;
            } else if (catchType.equals("开标记录")) {
                catchTypeNumber = OTHER_TYPE;
            }
        } else {
            if (catchType.equals("采购公告")) {
                catchTypeNumber = ZHAO_BIAO_TYPE;
            } else if (catchType.equals("更正公告")) {
                catchTypeNumber = GENG_ZHENG_TYPE;
            } else if (catchType.equals("中标公告")) {
                catchTypeNumber = ZHONG_BIAO_TYPE;
            } else if (catchType.equals("签约履行")) {
                catchTypeNumber = HE_TONG_TYPE;
            }
        }
        return catchTypeNumber;
    }


}
