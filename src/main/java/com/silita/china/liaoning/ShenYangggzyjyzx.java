package com.silita.china.liaoning;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
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

import java.net.URL;
import java.text.SimpleDateFormat;import static com.snatch.common.SnatchContent.*;


/**
 * http://www.syjy.gov.cn/NoticeTabQx/Tab_Jsgc_tab3_Qx?page=1  中标结果
 * http://www.syjy.gov.cn/NoticeTabQx/Tab_Jsgc_tab2_Qx?page=1  结果公告
 * Created by 91567 on 2018/3/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "ShenYangggzyjyzx")
public class ShenYangggzyjyzx extends BaseSnatch {

    private static final String source = "liaon";
    Dimension dimension;

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
//                "http://www.syjy.gov.cn/NoticeTabQx/Tab_Jsgc_tab3_Qx?page=1",
                "http://www.syjy.gov.cn/NoticeTabQx/Tab_Jsgc_tab2_Qx?page=1"
        };
        Connection conn;
        Document doc;
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
                        url = urls[i].substring(0, urls[i].lastIndexOf("=")+1) + pagelist;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(2000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        String textStr = doc.select("#page_string").text().trim();
//                        String textStr = doc.select("font[color=red]").first().parent().text();
                        String countPage = textStr.substring(textStr.indexOf("[1/")+3, textStr.indexOf("]"));
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Element div = doc.select(".tra_tab").first();
                    Elements trs = div.select(".list_mb_lista");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");
                        String title = trs.get(row).select("a").text();
                        if (!"".equals(conturl) && title.contains("沈北新区")) {
                            String date = sdf.format(sdf.parse(trs.get(row).select(".list_mb_list_ba").last().text()));
                            Notice notice = new Notice();
                            notice.setProvince("辽宁省");
                            notice.setProvinceCode("lns");
                            notice.setUrl(conturl);
                            notice.setTitle(title);
//                            if(i == 0) {
//                                notice.setNoticeType("中标公告");
//                            } else {
//                                notice.setNoticeType("结果公式");
//                            }
                            notice.setNoticeType("结果公式");
                            notice.setCatchType(ZHONG_BIAO_TYPE);
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
                    Thread.sleep(800);
                }
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn = null;
                doc = null;
                clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
//        String title = contdoc.select(".more_title").get(0).text();
        String content = contdoc.select(".more_article").html();
        if(notice.getNoticeType().equals("结果公式")) {
            dimension = new Dimension();
            dimension.setZbName(contdoc.select("#_Sheet1").select("tr").get(3).select("[class=B]").first().text());
            dimension.setProjType(contdoc.select("#_Sheet1").select("tr").get(4).select("[class=B]").first().text());
            dimension.setOneName( contdoc.select("#_Sheet1").select("tr").get(8).select("[class=B]").first().text());
            dimension.setProjectTimeLimit( contdoc.select("#_Sheet1").select("tr").last().select("[class=B]").last().text());
            dimension.setProjSum(contdoc.select("#_Sheet1").select("tr").get(9).select("[class=B]").first().text().replace("元", ""));
            dimension.setCert(contdoc.select("#_Sheet1").select("tr").get(6).select("[class=B]").first().text());

            contdoc.select("#_Sheet1").select("tr").get(3).select("[class=B]").first().text();  //招标人
            contdoc.select("#_Sheet1").select("tr").get(4).select("[class=B]").first().text();  //类型
            contdoc.select("#_Sheet1").select("tr").get(8).select("[class=B]").first().text();  //中标人
            contdoc.select("#_Sheet1").select("tr").last().select("[class=B]").last().text();  //工期
            contdoc.select("#_Sheet1").select("tr").get(9).select("[class=B]").first().text().replace("元", "");  //金额
            contdoc.select("#_Sheet1").select("tr").get(6).select("[class=B]").first().text();  //施工内容
            notice.setDimension(dimension);
        }
        notice.setContent(content);
        return notice;
    }
}
