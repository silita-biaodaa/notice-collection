package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.text.SimpleDateFormat;

import static com.snatch.common.SnatchContent.*;


/**
 * 长沙市公共资源交易中心 县市（长沙县、浏阳市、宁乡县、望城区）
 * https://csggzy.gov.cn/qxsxm.aspx/Index/1?areaCode=430121 长沙县
 * https://csggzy.gov.cn/qxsxm.aspx/Index/1?areaCode=430181 浏阳市
 * https://csggzy.gov.cn/qxsxm.aspx/Index/1?areaCode=430124 宁乡县
 * https://csggzy.gov.cn/qxsxm.aspx/Index/1?areaCode=430112 望城区
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanChangShaxsggzy")
public class HuNanChangShaxsggzy extends BaseSnatch {

    @Test
    @Override
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String[] urls = {
                "https://csggzy.gov.cn/qxsxm.aspx/Index/1?areaCode=430121",
                "https://csggzy.gov.cn/qxsxm.aspx/Index/1?areaCode=430181",
                "https://csggzy.gov.cn/qxsxm.aspx/Index/1?areaCode=430124",
                "https://csggzy.gov.cn/qxsxm.aspx/Index/1?areaCode=430112"
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
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].replace("Index/1", "Index/" + pagelist);
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        //获取总页数
                        Element item = doc.select("#pagecont").first();    //得到纪录页数区域的div（此中包含需要得到的总页数）
                        String type = item.text();                           //得到纪录页数区域的内容（此DIV中只有的text内容）
                        String countPage = type.substring(type.indexOf("/") + 1, type.length());   //获取总页数
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Element table = doc.select("table").last();    //得到存放标题以及时间的区域tbody
                    Elements trs = table.select("tr");               //得到tbody中的所有tr
                    for (int row = 0; row < trs.size() - 1; row++) {
                        if (row != 0 && row % 2 != 0) {
                            String conturl = trs.get(row).select("td").get(1).select("a").first().absUrl("href");  //得到标题（a）的href值
                            String title = (trs.get(row).select("td").get(1).select("a").attr("title").trim()); //得到标题
                            if (SnatchUtils.isNotNull(conturl)) {
                                Notice notice = new Notice();
                                String type = trs.get(row).select("td").get(2).text().trim();
                                String date = trs.get(row).select("td").get(3).text().trim();
                                String date1 = sdf.format(sdf.parse(date));
                                notice.setProvince("湖南省");
                                notice.setProvinceCode("huns");
                                notice.setCity("长沙市");
                                notice.setCityCode("cs");
                                String county = "";
                                String countyCode = "";
                                switch (i) {
                                    case 0:
                                        county = "长沙县";
                                        countyCode = "csx";
                                        break;
                                    case 1:
                                        county = "浏阳市";
                                        countyCode = "liuys";
                                        break;
                                    case 2:
                                        county = "宁乡县";
                                        countyCode = "nxx";
                                        break;
                                    case 3:
                                        county = "望城区";
                                        countyCode = "wcq";
                                        break;
                                }
                                notice.setCounty(county);
                                notice.setCountyCode(countyCode);
                                notice.setAreaRank(COUNTY);
                                notice.setSnatchNumber(snatchNumber);
                                if (type.contains("招标")) {
                                    notice.setCatchType("1");
                                    notice.setNoticeType("招标公告");
                                } else if (type.contains("中标") || type.contains("结果")) {
                                    notice.setCatchType("2");
                                    notice.setNoticeType("中标公告");
                                } else if (type.contains("补充")) {
                                    notice.setCatchType(BU_CHONG_TYPE);
                                    notice.setNoticeType("补充公告");
                                } else if (type.contains("延期")){
                                    notice.setCatchType(YAN_QI_TYPE);
                                    notice.setNoticeType("延期公告");
                                }else {
                                    if (SnatchUtils.isNull(getCatchType(title))) {
                                        notice.setCatchType("0");
                                    } else {
                                        notice.setCatchType(getCatchType(title));
                                    }
                                    notice.setNoticeType("其他公告");
                                }
                                notice.setUrl(conturl);
                                notice.setTitle(title);
                                notice.setOpendate(date1);
                                CURRENT_PAGE_LAST_OPENDATE = date1;
                                page = detailHandle(notice, pagelist, page, row, trs.size() - 1);
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
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = contdoc.select("#cont").html();              //得到中标详细内容区域
        notice.setContent(content);
        return notice;
    }

    public String getCatchType (String title) {
        if (title.contains("补充")) {
            return BU_CHONG_TYPE;
        }
        if (title.contains("答疑")) {
            return DA_YI_TYPE;
        }
        if (title.contains("流标")) {
            return LIU_BIAO_TYPE;
        }
        if (title.contains("澄清")) {
            return CHENG_QING_TYPE;
        }
        if (title.contains("延期")) {
            return YAN_QI_TYPE;
        }
        if (title.contains("更正") || title.contains("变更")) {
            return GENG_ZHENG_TYPE;
        }
        if (title.contains("废标")) {
            return FEI_BIAO_TYPE;
        }
        if (title.contains("终止")) {
            return ZHONG_ZHI_TYPE;
        }
        if (title.contains("修改")) {
            return XIU_GAI_TYPE;
        }
        if (title.contains("控制价")) {
            return KONG_ZHI_JIA_TYPE;
        }
        if (title.contains("合同公示") || title.contains("合同公告")) {
            return HE_TONG_TYPE;
        }
        if (title.contains("资审结果")) {
            return ZI_SHENG_JIE_GUO_TYPE;
        }
        if (title.contains("资格预审")) {
            return ZI_GE_YU_SHEN_TYPE;
        }
        if (title.contains("入围公示") || title.contains("入围公告")) {
            return RU_WEI_TYPE;
        }
        return null;
    }

}