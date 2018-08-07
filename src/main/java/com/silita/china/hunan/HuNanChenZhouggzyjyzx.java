package com.silita.china.hunan;

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

import java.net.URL;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by hujia on 2017/12/11.
 * 郴州市  郴州市公共资源交易中心  http://www.czggzy.czs.gov.cn/
 * 0-0 工程建设2550
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18382/18383/index.jsp?pager.offset=0&pager.desc=false  // 建设工程 > 招标公告 > 房屋市政
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18382/18384/index.jsp?pager.offset=0&pager.desc=false // 建设工程 > 招标公告 > 交通
 * http://czggzy.czs.gov.cn/18360/18370/18371/18382/18385/index.htm  // 建设工程 > 招标公告 > 水利
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18382/18386/index.jsp?pager.offset=0&pager.desc=false  // 建设工程 > 招标公告 > 其他
 * http://czggzy.czs.gov.cn/18360/18370/18371/18382/18387/index.htm  // 建设工程 > 招标公告 > 代理公告
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18388/18389/index.jsp?pager.offset=0&pager.desc=false  // 建设工程 > 招标信息 > 招标文件补遗漏
 * http://czggzy.czs.gov.cn/18360/18370/18371/18388/18390/index.htm  // 建设工程 > 招标信息 > 资审结果公示
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18392/18393/index.jsp?pager.offset=0&pager.desc=false  // 建设工程 > 中标公示 > 中标公告
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18394/18395/index.jsp?pager.offset=0&pager.desc=false // 建设工程 > 代理中标公示 > 代理中标公示
 * 9-17 政府采购
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18396/18397/index.jsp?pager.offset=0&pager.desc=false  // 政府采购 > 采购公告 > 招标
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18396/18398/index.jsp?pager.offset=0&pager.desc=false  // 政府采购 > 采购公告 > 竞争性谈判
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18396/18399/index.jsp?pager.offset=0&pager.desc=false  // 政府采购 > 采购公告 > 询价
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18396/18400/index.jsp?pager.offset=0&pager.desc=false  // 政府采购 > 采购公告 > 单一来源
 * http://czggzy.czs.gov.cn/18360/18370/18372/18396/18401/index.htm  // 政府采购 > 采购公告 > 资格预审
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18406/18407/index.jsp?pager.offset=0&pager.desc=false  // 政府采购 > 结果公示 > 中标公告
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18406/18408/index.jsp?pager.offset=0&pager.desc=false  // 政府采购 > 结果公示 > 结果公示
 * http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18409/18410/index.jsp?pager.offset=0&pager.desc=false  // 政府采购 > 更正答疑 > 更正公告
 * http://czggzy.czs.gov.cn/18360/18370/18372/18411/18412/index.htm 政府采购 > 其他公告 > 其他公告 1页
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanChenZhouggzyjyzx")
public class HuNanChenZhouggzyjyzx extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    private void firstGetMaxId() throws Exception {
        String[] urls = {
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18382/18383/index.jsp?pager.offset=0&pager.desc=false",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18382/18384/index.jsp?pager.offset=0&pager.desc=false",
                "http://czggzy.czs.gov.cn/18360/18370/18371/18382/18385/index.htm",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18382/18386/index.jsp?pager.offset=0&pager.desc=false",
                "http://czggzy.czs.gov.cn/18360/18370/18371/18382/18387/index.htm",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18388/18389/index.jsp?pager.offset=0&pager.desc=false",
                "http://czggzy.czs.gov.cn/18360/18370/18371/18388/18390/index.htm",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18392/18393/index.jsp?pager.offset=0&pager.desc=false",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18371/18394/18395/index.jsp?pager.offset=0&pager.desc=false",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18396/18397/index.jsp?pager.offset=0&pager.desc=false",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18396/18398/index.jsp?pager.offset=0&pager.desc=false",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18396/18399/index.jsp?pager.offset=0&pager.desc=false",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18396/18400/index.jsp?pager.offset=0&pager.desc=false",
                "http://czggzy.czs.gov.cn/18360/18370/18372/18396/18401/index.htm",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18406/18407/index.jsp?pager.offset=0&pager.desc=false",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18406/18408/index.jsp?pager.offset=0&pager.desc=false",
                "http://www.app.czs.gov.cn/czggzy/18360/18370/18372/18409/18410/index.jsp?pager.offset=0&pager.desc=false",
                "http://czggzy.czs.gov.cn/18360/18370/18372/18411/18412/index.htm"
        };

        try {
            for (int i = 0; i < urls.length; i++) {
                int page = 1;
                int pageTemp = 0;
                Connection conn = null;
                Document doc = null;
                String url = urls[i];
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        if (i == 2 || i == 4 || i == 6 || i == 13 || i == 17) {
                            url = urls[i].substring(0, urls[i].lastIndexOf("/")) + "/index_" + (pagelist - 1) + ".htm";
                        } else {
                            url = urls[i].substring(0, urls[i].indexOf("=") + 1) + (pagelist - 1) * 15 + "&pager.desc=false";
                        }
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        String pageCont = doc.select(".pager").select("li").last().select("a").first().attr("href");
                        if (i == 2 || i == 4 || i == 6 || i == 13) {
                            page = Integer.parseInt(pageCont.substring(pageCont.indexOf("_") + 1, pageCont.indexOf("."))) + 1;
                        } else if (i == 17) {
                            page = 1;
                        } else {
                            page = Integer.parseInt(pageCont.substring(pageCont.indexOf("=") + 1, pageCont.indexOf("&"))) / 15;
                        }
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".left_list").select(".list-ul").select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");
                        if (SnatchUtils.isNotNull(conturl)) {
                            Notice notice = new Notice();
                            String title = trs.get(row).select("a").first().attr("title");
                            String date = trs.get(row).select("span").last().text().trim();
                            //===入库代码====
                            if (i < 9) {
                                notice.setNoticeType("工程建设");
                            } else {
                                notice.setNoticeType("政府采购");
                            }
                            if (i >= 0 && i < 5) {
                                notice.setCatchType(ZHAO_BIAO_TYPE);
                            } else if (i == 5) {
                                notice.setCatchType(BU_CHONG_TYPE);
                            } else if (i == 6) {
                                notice.setCatchType(ZI_GE_YU_SHEN_TYPE);
                            } else if (i == 7 || i == 8) {
                                notice.setCatchType(ZHONG_BIAO_TYPE);
                            } else if (i > 8 && i < 15) {
                                notice.setCatchType(ZHAO_BIAO_TYPE);
                            } else if (i == 15 || i == 16) {
                                notice.setCatchType(ZHONG_BIAO_TYPE);
                            } else {
                                notice.setCatchType(GENG_ZHENG_TYPE);
                            }
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("郴州市");
                            notice.setCityCode("cz");
                            notice.setAreaRank(CITY);
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            notice.setSityClassify("郴州市公共资源交易中心" + "$$$" + urls[i]);
                            //详情信息入库，获取增量页数
                            if (notice.getNoticeType().contains("采购")) {
                                page = govDetailHandle(notice, i, page, row, trs.size());
                            } else {
                                page = detailHandle(notice, i, page, row, trs.size());
                            }
                        }
                    }
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                super.saveAllPageIncrement(urls[i]);
            }
        } finally {
            super.clearClassParam();
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        if (href.contains("doc") || href.contains("docx") || href.contains("pdf")) {
            notice.setContent(href);
        } else {
            Document docCount = Jsoup.parse(new URL(href).openStream(), "gbk", href);
            String title = docCount.select(".title").text().trim();
            String content = docCount.select(".main_text").html();
            notice.setTitle(title);
            notice.setContent(content);
        }
        return notice;
    }
}
