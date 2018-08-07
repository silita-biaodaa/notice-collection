package com.silita.china.heilongjang;

import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
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
import java.text.SimpleDateFormat;import static com.snatch.common.SnatchContent.*;

/**
 * 全国公共资源交易平台( 黑龙江省 )
 * http://hljggzyjyw.gov.cn/trade/tradezfcg?cid=16&type=1   交易信息 > 工程建设信息 > 交易公告
 * http://hljggzyjyw.gov.cn/trade/tradezfcg?cid=16&type=5   交易信息 > 工程建设信息 > 流标/废标公示
 * http://hljggzyjyw.gov.cn/trade/tradezfcg?cid=16&type=7   交易信息 > 工程建设信息 > 项目澄清
 * http://hljggzyjyw.gov.cn/trade/tradezfcg?cid=16&type=4   交易信息 > 工程建设信息 > 成交公示
 * http://hljggzyjyw.gov.cn/trade/tradezfcg?cid=16&type=3   交易信息 > 工程建设信息 > 交易结果
 * Created by 91567 on 2018/3/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "HeiLongJiangggzygov")
public class HeiLongJiangggzygov extends BaseSnatch {

    private static final String source = "heilj";

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://hljggzyjyw.gov.cn/trade/tradezfcg?cid=16&type=1",
                "http://hljggzyjyw.gov.cn/trade/tradezfcg?cid=16&type=5",
                "http://hljggzyjyw.gov.cn/trade/tradezfcg?cid=16&type=7",
                "http://hljggzyjyw.gov.cn/trade/tradezfcg?cid=16&type=4",
                "http://hljggzyjyw.gov.cn/trade/tradezfcg?cid=16&type=3"
        };
        Connection conn ;
        Document doc ;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            Dimension dim = null ;
            String url = urls[i];
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);//查询url前次抓取的情况（最大页数与公示时间）
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i] + "&pageNo=" + pagelist;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        String textStr = doc.select(".page").select("span").last().text().trim();
                        String countPage = textStr.substring(textStr.indexOf("/") + 1, textStr.indexOf("页"));
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements div = doc.select(".right_box");
                    Elements trs = div.select("ul").first().select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");
                        if (!"".equals(conturl)) {
                            String title = trs.get(row).select("a").text();
                            String date = sdf.format(sdf.parse(trs.get(row).select("span").last().text()));
                            if(title.substring(0, title.indexOf("]")).contains("市")) {
                                dim = new Dimension();
                                dim.setProjDq(title.substring(1, title.indexOf("公")));
                            }
                            Notice notice = new Notice();
                            notice.setProvince("黑龙江省");
                            notice.setProvinceCode("hlj");
                            notice.setUrl(conturl);
                            notice.setDimension(dim);
                            notice.setTitle(title.substring(title.indexOf("]")+1));
                            notice.setNoticeType("工程建设");
                            switch (i) {
                                case 0:
                                    notice.setCatchType(ZHAO_BIAO_TYPE);
                                    break;
                                case 1:
                                    if(title.contains("流标")) {
                                        notice.setCatchType(LIU_BIAO_TYPE);
                                    } else {
                                        notice.setCatchType(FEI_BIAO_TYPE);
                                    }
                                    break;
                                case 2:
                                    notice.setCatchType(CHENG_QING_TYPE);
                                    break;
                                case 3:
                                    notice.setCatchType(ZHONG_BIAO_TYPE);
                                    break;
                                case 4:
                                    notice.setCatchType(ZHONG_BIAO_TYPE);
                                    break;
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
                conn = null;
                doc = null;
                clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "UTF-8", href);
        String title = notice.getTitle();
        if(contdoc.select(".nav-line").first().previousElementSibling() != null) {
            title = contdoc.select(".nav-line").first().previousElementSibling().text();
        }
        String content = contdoc.select("#contentdiv").html();
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
