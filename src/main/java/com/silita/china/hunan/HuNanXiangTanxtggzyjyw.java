package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
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

import static com.snatch.common.SnatchContent.*;


/**
 * 湘潭公共资源交易中心
 * http://ggzy.xiangtan.gov.cn/zbgg/index.jhtml 工程建设 > 招标公告
 * http://ggzy.xiangtan.gov.cn/zsjggs/index.jhtml   工程建设 > 其它公告
 * http://ggzy.xiangtan.gov.cn/cggg/index.jhtml 政府采购 > 采购公告
 * http://ggzy.xiangtan.gov.cn/ygg/index.jhtml  政府采购 > 其他公告
 * http://ggzy.xiangtan.gov.cn/gzgg/index.jhtml 政府采购 > 更正公告
 *
 * @author gmy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})


@Component
@JobHander(value = "HuNanXiangTanxtggzyjyw")
public class HuNanXiangTanxtggzyjyw extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    private void firstGetMaxId() throws Exception {
        String[] urls = {
                "http://ggzy.xiangtan.gov.cn/zbgg/index.jhtml",
                "http://ggzy.xiangtan.gov.cn/zsjggs/index.jhtml",
                "http://ggzy.xiangtan.gov.cn/cggg/index.jhtml",
                "http://ggzy.xiangtan.gov.cn/ygg/index.jhtml",
                "http://ggzy.xiangtan.gov.cn/gzgg/index.jhtml"
        };
        Connection conn = null;
        Document doc = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
                        url = urls[i].substring(0, urls[i].lastIndexOf("/")) + "/index_" + pagelist + ".jhtml";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        String textStr = doc.select(".pagesite").select("div").text().trim();
                        String countPage = textStr.substring(textStr.indexOf("/") + 1, textStr.indexOf("页")).trim();
                        page = Integer.parseInt(countPage.trim());
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".text-list").select("li.tabletitle2");
                    for (int row = 0; row < trs.size(); row++) {
                        if (SnatchUtils.isNotNull(trs.get(row).text())) {
                            String href = trs.get(row).select("a").first().absUrl("href");
                            String title = trs.get(row).select("a").first().attr("title").trim().replaceAll(" ","");
                            String date = sdf.format(sdf.parse(trs.get(row).select("em").first().text().trim()));
                            Notice notice = new Notice();
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("湘潭市");
                            notice.setCityCode("xt");
                            notice.setAreaRank(CITY);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            switch (i) {
                                case 0:
                                    notice.setUrl(href + "?type=zb_zhaobiao");
                                    notice.setNoticeType("建设工程");
                                    break;
                                case 1:
                                    notice.setUrl(href + "?type=zb_qita&openDate=" + date);
                                    notice.setNoticeType("建设工程");
                                    break;
                                case 2:
                                    notice.setUrl(href + "?type=cg_zhaobiao");
                                    notice.setNoticeType("政府采购");
                                    break;
                                case 3:
                                    notice.setUrl(href + "?type=cg_qita&openDate=" + date);
                                    notice.setNoticeType("政府采购");
                                    break;
                                case 4:
                                    notice.setUrl(href + "?type=cg_gengzheng&openDate=" + date);
                                    notice.setNoticeType("政府采购");
                                    break;
                            }
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
                            if(i == 0 || i == 1) {  //工程建设
                                if(i == 0) {    //招标
                                    Elements eContent = docCount.select("#content");
                                    eContent.select("li.pro_child").remove();
                                    String content = eContent.first().html();
                                    notice.setContent(content);
                                    notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(ZHAO_BIAO_TYPE);
                                    } else {
                                        SnatchUtils.judgeCatchType(notice,notice.getCatchType(),ZHAO_BIAO_TYPE);
                                    }
                                } else {
                                    Elements eContent = docCount.select("#content");
                                    eContent.select("li.pro_child").remove();
                                    String content = eContent.get(1).html();
                                    notice.setContent(content);
                                    notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(OTHER_TYPE);
                                    } else {
                                        SnatchUtils.judgeCatchType(notice,notice.getCatchType(),OTHER_TYPE);
                                    }
                                }
                            } else{ //采购
                                if(i == 2) {    //招标
                                    Elements eContent = docCount.select("#content");
                                    eContent.select("li.pro_child").remove();
                                    String content = eContent.first().html();
                                    notice.setContent(content);
                                    notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(ZHAO_BIAO_TYPE);
                                    } else {
                                        SnatchUtils.judgeCatchType(notice,notice.getCatchType(),ZHAO_BIAO_TYPE);
                                    }
                                }
                                if(i == 3) {    //其他
                                    Elements eContent = docCount.select("#content");
                                    eContent.select("li.pro_child").remove();
                                    String content = eContent.get(1).html();
                                    notice.setContent(content);
                                    notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(OTHER_TYPE);
                                    } else {
                                        SnatchUtils.judgeCatchType(notice,notice.getCatchType(),OTHER_TYPE);
                                    }
                                }
                                if(i == 4) {    //更正
                                    Elements eContent = docCount.select("#content");
                                    eContent.select("li.pro_child").remove();
                                    String content = eContent.get(2).html();
                                    notice.setContent(content);
                                    notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(GENG_ZHENG_TYPE);
                                    } else {
                                        SnatchUtils.judgeCatchType(notice,notice.getCatchType(),GENG_ZHENG_TYPE);
                                    }
                                }
                            }

                            // 只有招标公告有维度信息抓取
                            if (i == 0 || i ==2) {
                                Dimension dimension = new Dimension();
                                String bmDate = docCount.select(".timecentert").select("tr").first().select("td").get(1).text();
                                if (bmDate.length() >= 10) {
                                    dimension.setBmEndDate(SnatchUtils.isNull(bmDate)?null:bmDate.substring(0,bmDate.indexOf(" ")));
                                }
                                if (bmDate.length() >= 16) {
                                    dimension.setBmEndTime(SnatchUtils.isNull(bmDate)?null:bmDate.substring(bmDate.indexOf(" ")+1,bmDate.length()-3));
                                }
                                String tbDate = docCount.select(".timecentert").select("tr").last().select("td").get(1).text();
                                if (tbDate.length() >= 10) {
                                    dimension.setTbEndDate(SnatchUtils.isNull(tbDate)?null:tbDate.substring(0,tbDate.indexOf(" ")));
                                }
                                if (tbDate.length() >= 16) {
                                    dimension.setTbEndTime(SnatchUtils.isNull(tbDate)?null:tbDate.substring(tbDate.indexOf(" ")+1,tbDate.length()-3));
                                }
                                String tbAssureDate = docCount.select(".timecentert").select("tr").last().select("td").last().text();
                                if (tbAssureDate.length() >= 10) {
                                    dimension.setTbAssureEndDate(SnatchUtils.isNull(tbAssureDate)?null:tbAssureDate.substring(0,tbAssureDate.indexOf(" ")));
                                }
                                if (tbAssureDate.length() >= 16) {
                                    dimension.setTbAssureEndTime(SnatchUtils.isNull(tbAssureDate)?null:tbAssureDate.substring(tbAssureDate.indexOf(" ")+1,tbAssureDate.length()-3));
                                }
                                notice.setDimension(dimension);
                            }

                            //详情信息入库，获取增量页数
                            if ("政府采购".equals(notice.getNoticeType())) {
                                page = govDetailHandle(notice, pagelist, page, row, trs.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, trs.size());
                            }
                        }
                        Thread.sleep(300);
                    }
                    //===入库代码====
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                urls = new String[]{
                        "http://ggzy.xiangtan.gov.cn/zbgg/index.jhtml",
                        "http://ggzy.xiangtan.gov.cn/zsjggs/index.jhtml",
                        "http://ggzy.xiangtan.gov.cn/cggg/index.jhtml",
                        "http://ggzy.xiangtan.gov.cn/ygg/index.jhtml",
                        "http://ggzy.xiangtan.gov.cn/gzgg/index.jhtml"
                };
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        return notice;
    }
}
