package com.silita.china.jilin;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Map;

import static com.snatch.common.SnatchContent.*;

/**
 * 吉林公共资源交易中心
 * http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001001/1.html    交易信息 > 工程建设
 * http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001002/1.html    工程建设 > 变更公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001003/1.html    工程建设 > 预中标公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001004/1.html    工程建设 > 合同公示
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002001/1.html    政采集中 > 招标公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002002/1.html    政采集中 > 变更公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002004/1.html    政采集中 > 中标结果公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002005/1.html    政采集中 > 合同公示
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002007/1.html    政采集中 > 废标公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002006/1.html    政采集中 > 废标公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005001/1.html    政采非集中 > 招标公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005002/1.html    政采非集中 > 变更公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005004/1.html    政采非集中 > 中标结果公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005005/1.html    政采非集中 > 合同公示
 * http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005006/1.html    政采非集中 > 废标公告
 * Created by 91567 on 2018/3/5.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component()
@JobHander(value = "JiLinGongGongzyjyzx")
public class JiLinGongGongzyjyzx extends BaseSnatch {

    private static final String source = "jil";

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001001/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001002/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001003/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001004/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002001/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002002/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002004/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002005/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002007/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002006/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005001/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005002/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005004/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005005/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005006/1.html"
        };
        Map<String, String> cookies = null;
        String[] catchTypes = {
                ZHAO_BIAO_TYPE, GENG_ZHENG_TYPE, ZHONG_BIAO_TYPE, HE_TONG_TYPE,
                ZHAO_BIAO_TYPE, GENG_ZHENG_TYPE, ZHONG_BIAO_TYPE, HE_TONG_TYPE, FEI_BIAO_TYPE, FEI_BIAO_TYPE,
                ZHAO_BIAO_TYPE, GENG_ZHENG_TYPE, ZHONG_BIAO_TYPE, HE_TONG_TYPE, FEI_BIAO_TYPE
        };
        Connection conn = null;
        Document doc = null;
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
                    if (pagelist > 1) {
                        url = urls[i].substring(0, urls[i].lastIndexOf("/") + 1) + pagelist + ".html";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    if(cookies != null) {
                        conn.cookies(cookies);
                    }
                    doc = conn.get();
                    if (page == 1) {
                        if (i == 0) {
                            cookies = conn.response().cookies();
                        }
                        //获取总页数
                        if (!StringUtils.isEmpty(doc.select("#index").text())) {
                            String pageStr = doc.select("#index").first().text();
                            page = Integer.valueOf(pageStr.substring(pageStr.indexOf("/") + 1));
                        } else {
                            page = 0;
                        }
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".wb-data-item li");
                    for (int row = 0; row < trs.size(); row++) {
                        Notice notice = new Notice();
                        String detailUrl = trs.get(row).select("a").first().absUrl("href");
                        String title = trs.get(row).select("a").attr("title");
                        String publishDate = trs.get(row).select(".wb-data-date").text().replaceAll("\\[", "").replaceAll("\\]", "");
                        notice.setProvince("吉林省");
                        notice.setProvinceCode("jls");
                        notice.setCatchType(catchTypes[i]);
                        if (i <= 3) {
                            notice.setNoticeType("工程建设");
                        } else if (i >= 4 && i < 9) {
                            notice.setNoticeType("政府采购");
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
                        //详情信息入库，获取增量页数
                        if ("政府采购".equals(notice.getNoticeType())) {
                            page = govDetailHandle(notice, pagelist, page, row, trs.size());
                        } else {
                            page = detailHandle(notice, pagelist, page, row, trs.size());
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
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
//        String title = contdoc.select(".ewb-article-sources").first().previousElementSibling().text().trim();
        String content = contdoc.select(".MsoNormal").html();
        notice.setContent(content);
        return notice;
    }

}
