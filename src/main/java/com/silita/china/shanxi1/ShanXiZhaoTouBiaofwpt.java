package com.silita.china.shanxi1;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;import static com.snatch.common.SnatchContent.*;

/**
 * 山西省招标投标公共服务平台（详情页面需要登录）
 * http://www.sxbid.com.cn/f/list-6796f0c147374f85a50199b38ecb0af6.html?pageNo=1&pageSize=15 招标信息 > 招标公告
 * http://www.sxbid.com.cn/f/list-54f5e594f4314654aadf09f7c9ae28bf.html?pageNo=1&pageSize=15 招标信息 > 中标候选人公示
 * http://www.sxbid.com.cn/f/list-d4bfee46e6ed452d82588dc17207a34b.html?pageNo=1&pageSize=15 招标信息 > 中标结果公告
 * http://www.sxbid.com.cn/f/list-097ac52e57a94598b7a78ea4e678a040.html?pageNo=1&pageSize=15 招标信息 > 改变招标方式
 * Created by 91567 on 2018/3/2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "ShanXiZhaoTouBiaofwpt")
public class ShanXiZhaoTouBiaofwpt extends BaseSnatch {

    private static final String source = "sanx";

    private Map cookies = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    int userValidDays = 365 - SnatchUtils.comparePastDate("2018-06-19", sdf.format(System.currentTimeMillis()));

    @Override
    @Test
    public void run() throws Exception {
        login();
        sntachTask();
    }

    /**
     * 模拟登录，获取账号cookie信息
     * @throws IOException
     */
    public void login() throws IOException {
        if (cookies == null) {
            String JSESSIONID = SnatchUtils.getStringRandom(32).toUpperCase();
            String session = SnatchUtils.getStringRandom(32).toLowerCase();
            String loginUrl = "http://www.sxbid.com.cn/a/login";
            Connection con = Jsoup.connect(loginUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true).followRedirects(true);
            con.header("Upgrade-Insecure-Requests", "1");
            con.cookie("JSESSIONID", JSESSIONID);
            con.cookie("psp.session.id", session);

            con.data("iswebsite", "");
            con.data("form_random_token", SnatchUtils.getNumberRandom(19));
            con.data("username", "20141125");
//            con.data("password", "yb20141125");
            con.data("password", "Q8MlJh8hUWaI1Ltl/FT4bA==");
            con.post();

            cookies = con.response().cookies();
            cookies.put("JSESSIONID", JSESSIONID);
            cookies.put("__51cke__", "");
            cookies.put("__tins__3082775", "%7B%22sid%22%3A%201528081777226%2C%20%22vd%22%3A%208%2C%20%22expires%22%3A%201528084769897%7D");
            cookies.put("__51laig__", "30");
            cookies.put("psp.user.login", "1");
            cookies.put("psp.user.id", "8a39041dd2f4449093c6393d82c290e0");
        }
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://www.sxbid.com.cn/f/list-6796f0c147374f85a50199b38ecb0af6.html",
                "http://www.sxbid.com.cn/f/list-54f5e594f4314654aadf09f7c9ae28bf.html",
                "http://www.sxbid.com.cn/f/list-d4bfee46e6ed452d82588dc17207a34b.html",
                "http://www.sxbid.com.cn/f/list-097ac52e57a94598b7a78ea4e678a040.html"
        };
        String[] catchTypes = {ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHONG_BIAO_TYPE, GENG_ZHENG_TYPE};

        Connection conn;
        Document doc;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            //查询url前次抓取的情况（最大页数与公示时间）
            super.queryBeforeSnatchState(urls[i]);
            try {
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    conn = Jsoup.connect(url).userAgent("Mozilla").cookies(cookies).timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    conn.header("X-DevTools-Emulate-Network-Conditions-Client-Id", "4A121ED13202C763616FC3E50C39C229");
                    conn.header("Upgrade-Insecure-Requests", "1");
                    conn.header("Content-Type", "application/x-www-form-urlencoded");

                    conn.data("pageNo", String.valueOf(pagelist));
                    conn.data("pageSize", "15");
                    conn.data("accordToLaw", "");
                    conn.data("resourceType", "1");
                    conn.data("title", "");
                    conn.data("publishTimeRange", "");
                    conn.data("form_random_token", SnatchUtils.getNumberRandom(19));

                    cookies.put("pageNo", String.valueOf(pagelist));
                    cookies.put("pageSize", "15");
                    doc = conn.post();
                    SnatchLogger.debug("第" + pagelist + "页");

                    if (page == 1) {
                        String textStr = doc.select(".list_pages").first().select("span").last().text().trim();
                        String countPage = textStr.substring(textStr.indexOf("共") + 1, textStr.indexOf("页")).trim();
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements div = doc.select(".download_table").select("tbody");
                    Elements trs = div.select("tr");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = "";
                        if (i == 3) {
                            String tempUrl = trs.get(row).select("td").get(1).attr("onclick");
                            conturl = "http://www.sxbid.com.cn" + tempUrl.substring(tempUrl.indexOf("('") + 2, tempUrl.indexOf("',"));
                        } else {
                            conturl = trs.get(row).select("a").last().absUrl("href");
                        }
                        if (!"".equals(conturl)) {
                            String title = trs.get(row).select("td").attr("title");
                            String date = sdf.format(sdf.parse(trs.get(row).select(".text-align").last().text()));
                            Notice notice = new Notice();
                            notice.setProvince("山西省");
                            notice.setProvinceCode("sxis");
                            if (i == 0) {
                                notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                                if (SnatchUtils.isNull(notice.getCatchType())) {
                                    notice.setCatchType(catchTypes[i]);
                                }
                            } else {
                                notice.setCatchType(catchTypes[i]);
                            }
                            notice.setNoticeType("工程建设");
                            if (title.contains("采购")) {
                                notice.setNoticeType("政府采购");
                            }
                            notice.setAreaRank("0");
                            notice.setSource(source);
                            notice.setOpendate(date);
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            //详情信息入库，获取增量页数
                            if ("政府采购".equals(notice.getNoticeType())) {
                                page = govDetailHandle(notice, pagelist, page, row, trs.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, trs.size());
                            }
                        }
                    }
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                    //===入库代码====
                    super.saveAllPageIncrement(url);
                    Thread.sleep(2000);
                }
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            } finally {
                super.clearClassParam();
            }
        }
        //所有分页抓取完毕模拟退出
        String logoutUrl = "http://www.sxbid.com.cn/a/logout?site=" + urls[3];
        conn = Jsoup.connect(logoutUrl).userAgent("Mozilla").cookies(cookies).timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
        conn.execute();
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Connection conn = Jsoup.connect(href).cookies(cookies).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
        conn.header("Upgrade-Insecure-Requests", "1");
        conn.header("X-DevTools-Emulate-Network-Conditions-Client-Id", "4A121ED13202C763616FC3E50C39C229");

        Document contdoc = conn.get();
        String title = notice.getTitle();
        String bidding_tit = contdoc.select(".bidding_tit").html();
        String content = contdoc.select(".body_content").html();
        Dimension dimension = new Dimension();
        //维度表格
        if (SnatchUtils.isNotNull(contdoc.select(".bidding_tit").html())) {
            Element dimensionTable = contdoc.select(".bidding_tit").first();
            dimension.setProjDq(dimensionTable.select(".bidding_conter").get(0).text());
            dimension.setProjType(dimensionTable.select(".bidding_conter").get(1).text());
            String bmEndDate = dimensionTable.select(".bidding_conter").get(3).text();
            if (bmEndDate.contains("至")) {
                dimension.setBmEndDate(bmEndDate.substring(bmEndDate.indexOf("至") + 1));
            }
        }
        //相关公告
        if (contdoc.select(".bidding_more") != null) {
            Elements relations = contdoc.select(".bidding_more").select("li");
            if (relations.size() > 0) {
                String relationUrl = "";
                for (int i = 0; i < relations.size(); i++) {
                    if (relations.get(i).select("a").text().equals(title)) {
                        continue;
                    }
                    relationUrl = "," + relations.get(i).select("a").first().absUrl("href");
                }
                if (SnatchUtils.isNotNull(relationUrl)) {
                    dimension.setRelation_url(relationUrl.substring(1));
                }
            }
        }
        notice.setDimension(dimension);
        if (title.contains("关于")) {
            title = title.substring(title.indexOf("关于") + 2);
            notice.setTitle(title);
        }
        notice.setContent(bidding_tit + content);
        Thread.sleep(1500);
        return notice;
    }
}
