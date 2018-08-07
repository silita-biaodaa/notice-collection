package com.silita.china.hebei;

import com.snatch.model.Notice;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
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

/**
 *
 * 全国公共资源交易平台（河北省）
 * http://www.hebpr.cn/hbjyzx/002/002009/002009002/002009002001/moreinfo.html 工程建设 > 招标/资审公告
 * http://www.hebpr.cn/hbjyzx/002/002009/002009002/002009002002/moreinfo.html 工程建设 > 澄清/变更公告
 * http://www.hebpr.cn/hbjyzx/002/002009/002009002/002009002003/moreinfo.html 工程建设 > 中标候选人公示
 * http://www.hebpr.cn/hbjyzx/002/002009/002009002/002009002005/moreinfo.html 工程建设 > 中标结果公示
 * http://www.hebpr.cn/hbjyzx/002/002009/002009001/002009001001/moreinfo.html 政府采购 > 采购/资审公告
 * http://www.hebpr.cn/hbjyzx/002/002009/002009001/002009001002/moreinfo.html 政府采购 > 更正公告
 * http://www.hebpr.cn/hbjyzx/002/002009/002009001/002009001006/moreinfo.html 政府采购 > 结果公告
 * http://www.hebpr.cn/hbjyzx/002/002009/002009001/002009001008/moreinfo.html 政府采购 > 采购合同公示
 * Created by 91567 on 2018/3/1.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "HeBeiGongGongZiYuanjyzx")
public class HeBeiGongGongZiYuanjyzx extends BaseSnatch {

    private static final String source = "hebei";

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://www.hebpr.cn/hbjyzx/002/002009/002009002/002009002001/moreinfo.html",
                "http://www.hebpr.cn/hbjyzx/002/002009/002009002/002009002002/moreinfo.html",
                "http://www.hebpr.cn/hbjyzx/002/002009/002009002/002009002003/moreinfo.html",
                "http://www.hebpr.cn/hbjyzx/002/002009/002009002/002009002005/moreinfo.html",
                "http://www.hebpr.cn/hbjyzx/002/002009/002009001/002009001001/moreinfo.html",
                "http://www.hebpr.cn/hbjyzx/002/002009/002009001/002009001002/moreinfo.html",
                "http://www.hebpr.cn/hbjyzx/002/002009/002009001/002009001006/moreinfo.html",
                "http://www.hebpr.cn/hbjyzx/002/002009/002009001/002009001008/moreinfo.html"
        };
        Connection conn ;
        Document doc ;
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
                        url = urls[i].substring(0, urls[i].lastIndexOf("/") + 1) + pagelist + ".html";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        String textStr = doc.select(".pagemargin").select(".huifont").text().trim();
                        String countPage = textStr.substring(textStr.indexOf("/") + 1, textStr.indexOf("转到")).trim();
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements div = doc.select("#categorypagingcontent");
                    Elements trs = div.select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").last().absUrl("href");
                        if (!"".equals(conturl)) {
                            String date = trs.get(row).select(".date").text();
//                            String date = sdf.format(sdf.parse(trs.get(row).select(".date").text()));
                            String title = trs.get(row).select(".frame-con-txt").text();
                            Notice notice = new Notice();
                            notice.setProvince("河北省");
                            notice.setProvinceCode("hbs");
                            notice.setUrl(conturl);
                            if (i <= 4 ) {   //工程建设
                                notice.setNoticeType("工程建设");
                                if(title.contains("采购")) {
                                    notice.setNoticeType("政府采购");
                                }
                            } else if(i == 7){    //合同公告
                                notice.setNoticeType("合同公告");
                            } else {
                                notice.setNoticeType("政府采购");
                            }
                            notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                            if(SnatchUtils.isNull(notice.getCatchType())) {
                                continue;
                            }
                            if(title.contains("测试")) {
                                break;
                            }
                            notice.setAreaRank("0");
                            notice.setSource(source);
                            if(SnatchUtils.isNotNull(date)) {
                                notice.setOpendate(date);
                            }
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
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = contdoc.select(".show-title").text();
        if(title.substring(0,4).equals("Z130") || title.substring(0,4).equals("G130")) {   //
            String startTitle = title.substring(0, 20);
            String endTitle = title.substring(20);
            title = startTitle.replaceAll("[^(\\u4e00-\\u9fa5)]", "") + endTitle;
        }
        String content = contdoc.select(".infoContent").html();
//        if(SnatchUtils.isNull(notice.getOpendate()) && title.contains("[变更公告]")) {
//            String tempContent = SnatchUtils.excludeStringByKey(content);
//            if(tempContent.contains("更正日期")) {
//                String dateStr = tempContent.substring(tempContent.indexOf("更正日期") + 5, tempContent.indexOf("更正日期") + 15);
//                String formarDate = dateStr.replace("年", "-").replace("月", "-").replace("日", "-");
//                notice.setOpendate(formarDate.replaceAll("[\\u4e00-\\u9fa5]", ""));
//            }
//        }
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
