package com.silita.china.beijing;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.HttpsConnection;
import com.snatch.common.utils.HttpsSSLUtil;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
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
import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * 全国公共资源交易平台（北京市）https请求
 * https://www.bjggzyfw.gov.cn/cmsbj/jyxxggjtbyqs/index.html 工程建设   招标公告
 * https://www.bjggzyfw.gov.cn/cmsbj/jyxxzbhxrgs/index.html 工程建设   中标候选人公式
 * https://www.bjggzyfw.gov.cn/cmsbj/jyxxzbgg/index.html  工程建设   中标结果
 * https://www.bjggzyfw.gov.cn/cmsbj/jyxxcggg/index.html 政府采购 采购公告
 * https://www.bjggzyfw.gov.cn/cmsbj/jyxxgzsx/index.html 政府采购 更正事宜
 * https://www.bjggzyfw.gov.cn/cmsbj/jyxxzbjggg/index.html 政府采购 成交结果公告
 * Created by 91567 on 2018/3/1.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "BeiJingGongGongZiYuanjyzx")
public class BeiJingGongGongZiYuanjyzx extends BaseSnatch {

    private static final String source = "beij";
    Random random = new Random();

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "https://www.bjggzyfw.gov.cn/cmsbj/jyxxggjtbyqs/index.html",
                "https://www.bjggzyfw.gov.cn/cmsbj/jyxxzbhxrgs/index.html",
                "https://www.bjggzyfw.gov.cn/cmsbj/jyxxzbgg/index.html",
                "https://www.bjggzyfw.gov.cn/cmsbj/jyxxcggg/index.html",
                "https://www.bjggzyfw.gov.cn/cmsbj/jyxxgzsx/index.html",
                "https://www.bjggzyfw.gov.cn/cmsbj/jyxxzbjggg/index.html"
        };
        Connection conn ;
        Document doc ;
        Random random;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);//查询url前次抓取的情况（最大页数与公示时间）
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].substring(0, urls[i].lastIndexOf(".")) + "_" + pagelist + ".html";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    doc = Jsoup.parse(HttpsConnection.connect(url, "utf-8"));
                    if (page == 1) {
                        String textStr = doc.select(".pages-list").select("li").first().text().trim();
                        String countPage = textStr.substring(textStr.indexOf("/") + 1, textStr.indexOf("页"));
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements div = doc.select(".article-list2");
                    Elements trs = div.select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = "https://www.bjggzyfw.gov.cn" + trs.get(row).select("a").attr("href");
                        if (!"".equals(conturl)) {
                            String date = sdf.format(sdf.parse(trs.get(row).select(".list-times").text()));
                            String title = trs.get(row).select("a").attr("title");
                            Notice notice = new Notice();
                            notice.setProvince("北京");
                            notice.setProvinceCode("bj");
                            notice.setUrl(conturl);
                            if (i <= 3) {   //工程建设
                                notice.setNoticeType("工程建设");
                            } else {    //政府采购
                                notice.setNoticeType("政府采购");
                            }
                            notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                            if(SnatchUtils.isNull(notice.getCatchType())) {
                                continue;
                            }
                            notice.setAreaRank("0");
                            notice.setSource(source);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            //详情信息入库，获取增量页数
                            if ("政府采购".equals(notice.getNoticeType())) {
                                page = govDetailHandle(notice, pagelist, page, row, trs.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, trs.size());
                            }
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
                clearClassParam();
            }

        }

    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Dimension dim ;
        Document contdoc = null;
        int ran = (int) (Math.random()*2);
        //2中方式随机
        if(ran == 1) {
            contdoc = Jsoup.parse(HttpsConnection.connect(href, "utf-8"));
        } else {
            HttpsSSLUtil.neglectSSL();
            contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        }
        String title = contdoc.select(".div-content").select(".div-title").text();
        String content = contdoc.select(".div-content").select(".div-article2").html();
        if(contdoc.select(".div-content").select(".div-article2").select("table").first() != null) {
            if(contdoc.select(".div-content").select(".div-article2").select("table").first().select("tr").first().text().trim().equals("中标公告")) {
                dim = new Dimension();
                if(contdoc.select(".newsCon").select("table").select("tr").get(2).select("td") != null) {
                    String oneName = contdoc.select(".newsCon").select("table").select("tr").get(2).select("td").last().text().replace(",", "");  //第一中标候选人
                    dim.setOneName(oneName);
                }
                if(contdoc.select(".newsCon").select("table").select("tr").get(3).select("td") != null) {
                    String oneProjDuty = contdoc.select(".newsCon").select("table").select("tr").get(3).select("td").last().text().replace(",", "");  //项目负责人
                    dim.setOneProjDuty(oneProjDuty);
                }
                if(contdoc.select(".newsCon").select("table").select("tr").last().select("td") != null) {
                    String oneOffer = contdoc.select(".newsCon").select("table").select("tr").last().select("td").last().text().replaceAll("[\\u4e00-\\u9fa5]", ""); //报价
                    dim.setOneOffer(oneOffer.replace("(", "").replace(")", ""));
                }
                notice.setDimension(dim);
            }
        }
        notice.setTitle(title);
        notice.setContent(content);
        Thread.sleep(300*(random.nextInt(5)%(5-1+1)));//随机暂停
        return notice;
    }
}
