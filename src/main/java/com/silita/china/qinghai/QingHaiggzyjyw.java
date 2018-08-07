package com.silita.china.qinghai;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.apache.commons.lang3.StringUtils;
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

import java.net.URL;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by hujia on 2018/03/05
 * 青海省公共资源交易信息网
 * 工程建设
 * http://www.qhggzyjy.gov.cn/ggzy/jyxx/001001/001001001/secondPage.html       // 招标公告
 * http://www.qhggzyjy.gov.cn/ggzy/jyxx/001001/001001002/secondPage.html        // 资格预审公告
 * http://www.qhggzyjy.gov.cn/ggzy/jyxx/001001/001001003/secondPage.html       // 澄清及变更
 * http://www.qhggzyjy.gov.cn/ggzy/jyxx/001001/001001005/secondPage.html        // 中标候选人公示
 * http://www.qhggzyjy.gov.cn/ggzy/jyxx/001001/001001006/secondPage.html        // 中标结果公示
 * 政府采购
 * http://www.qhggzyjy.gov.cn/ggzy/jyxx/001002/001002001/secondPage.html        // 采购公告
 * http://www.qhggzyjy.gov.cn/ggzy/jyxx/001002/001002002/secondPage.html        // 澄清及变更
 * http://www.qhggzyjy.gov.cn/ggzy/jyxx/001002/001002004/secondPage.html        // 中标公示
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "QingHaiggzyjyw")
public class QingHaiggzyjyw extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String urls[] = {
                "http://www.qhggzyjy.gov.cn/ggzy/jyxx/001001/001001001/secondPage.html",
                "http://www.qhggzyjy.gov.cn/ggzy/jyxx/001001/001001002/secondPage.html",
                "http://www.qhggzyjy.gov.cn/ggzy/jyxx/001001/001001003/secondPage.html",
                "http://www.qhggzyjy.gov.cn/ggzy/jyxx/001001/001001005/secondPage.html",
                "http://www.qhggzyjy.gov.cn/ggzy/jyxx/001001/001001006/secondPage.html",
                "http://www.qhggzyjy.gov.cn/ggzy/jyxx/001002/001002001/secondPage.html",
                "http://www.qhggzyjy.gov.cn/ggzy/jyxx/001002/001002002/secondPage.html",
                "http://www.qhggzyjy.gov.cn/ggzy/jyxx/001002/001002004/secondPage.html"
        };
        String oldInterfaceUrl = "http://www.qhggzyjy.gov.cn/wzds/CustomSearchInfoShow.action?cmd=Custom_Search_InfoShow";
        String JSESSIONID = SnatchUtils.getStringRandom(32).toUpperCase();

        String[] categoryCode = {"001001001", "001001002", "001001003", "001001005", "001001006", "001002001", "001002002", "001002004"};
        String[] catchTypes = {ZHAO_BIAO_TYPE, ZI_GE_YU_SHEN_TYPE, CHENG_QING_TYPE, ZHONG_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHAO_BIAO_TYPE, CHENG_QING_TYPE, ZHONG_BIAO_TYPE};
        Connection conn = null;
        Document doc = null;

        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {

                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(oldInterfaceUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    conn.cookie("JSESSIONID", JSESSIONID);
                    conn.header("X-Requested-With", "XMLHttpRequest");
                    conn.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                    conn.data("cnum", "001;002;003;004;005;006;007;008;009;010");
                    conn.data("front", "/ggzy");
                    conn.data("area", "0");
                    conn.data("categoryNum", categoryCode[i]);
                    conn.data("pageIndex", String.valueOf(pagelist));
                    conn.data("pageSize", "10");
                    conn.data("xiaquCode", "");
                    conn.data("titleInfo", "");
                    String docStr = conn.execute().body();

                    JSONObject jsonStr = new JSONObject(docStr);
                    String custom = jsonStr.get("custom").toString();
                    JSONObject data = new JSONObject(custom);
                    if (pagelist == 1) {
                        String totalStr = data.get("totalNumBer").toString();
                        int total = Integer.parseInt(totalStr);
                        page = total % 10 == 0 ? total / 10 : total / 10 + 1;
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                        SnatchLogger.debug("总" + page + "页");
                    }

                    JSONArray trs = new JSONArray(data.get("records").toString());
                    for (int row = 0; row < trs.length(); row++) {
                        JSONObject tr = new JSONObject(trs.get(row).toString());
                        String href = "http://www.qhggzyjy.gov.cn/" + tr.get("linkurl").toString();
                        String title = tr.get("title").toString();
                        String date = tr.get("date").toString();
                        String cityStr = tr.get("xiaquname").toString();
                        //===入库代码====
                        Notice notice = new Notice();
                        notice.setProvince("青海省");
                        if (cityStr.contains(".")) {
                            notice.setCity(cityStr.substring(0, cityStr.indexOf(".")));
                            notice.setCounty(cityStr.substring(cityStr.indexOf("."), cityStr.length()));
                        } else {
                            notice.setCity(cityStr);
                        }
                        notice.setProvinceCode("qhs");
                        notice.setAreaRank(PROVINCE);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setCatchType(catchTypes[i]);
                        if (i <= 4) {
                            notice.setNoticeType("建设工程");
                        } else {
                            notice.setNoticeType("政府采购");
                        }
                        notice.setUrl(href);
                        notice.setSource("qingh");
                        notice.setOpendate(date);
                        CURRENT_PAGE_LAST_OPENDATE = date;
                        notice.setTitle(title);
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
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    throw e;
                }
            } finally {
                super.clearClassParam();
            }
        }
    }


    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "UTF-8", href);
        String content = null;
        if (docCount.select(".ewb-info-content").hasText()) {
            content = docCount.select(".ewb-info-content").html();
        } else if (StringUtils.isEmpty(content) && docCount.select(".xiangxiyekuang").hasText()) {
            content = docCount.select(".xiangxiyekuang").html();
        }
        if (StringUtils.isEmpty(content)) {
            System.out.println("!!!!!");
        }
        notice.setContent(content);
        return notice;
    }
}
