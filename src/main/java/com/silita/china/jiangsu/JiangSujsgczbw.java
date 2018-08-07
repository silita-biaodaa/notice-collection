package com.silita.china.jiangsu;

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

import java.math.BigDecimal;
import java.net.URL;

import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * 江苏省建设工程招标网   http://www.jszb.com.cn
 * http://www.jszb.com.cn/jszb/YW_info/ZhaoBiaoGG/MoreInfo_ZBGG.aspx?categoryNum=012    招标公告
 * http://www.jszb.com.cn/jszb/YW_info/ZuiGaoXJ/MoreInfo_ZGXJ.aspx?categoryNum=012      最高限价公示
 * http://www.jszb.com.cn/jszb/YW_info/KaiBiaoJL/MoreInfo_KBJL.aspx?categoryNum=012     开标情况公示
 * http://www.jszb.com.cn/jszb/YW_info/ZiGeYS/MoreInfo_ZGYS.aspx?categoryNum=012        未入围公示
 * http://www.jszb.com.cn/jszb/YW_info/PBJieGuoGS/MoreInfo_PBJieGuoGS.aspx?categoryNum=012  评标结果公示
 * http://www.jszb.com.cn/jszb/YW_info/ZhongBiaoGS/MoreInfo_ZBGS.aspx?categoryNum=012   中标结果公告
 * Created by Administrator on 2018/3/5.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "JiangSujsgczbw")
public class JiangSujsgczbw extends BaseSnatch {


    private static final String source = "jiangs";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "http://www.jszb.com.cn/jszb/YW_info/ZhaoBiaoGG/MoreInfo_ZBGG.aspx?categoryNum=012",    //招标公告
                "http://www.jszb.com.cn/jszb/YW_info/ZuiGaoXJ/MoreInfo_ZGXJ.aspx?categoryNum=012",  //最高限价公示
                "http://www.jszb.com.cn/jszb/YW_info/KaiBiaoJL/MoreInfo_KBJL.aspx?categoryNum=012", //开标情况公示
                "http://www.jszb.com.cn/jszb/YW_info/ZiGeYS/MoreInfo_ZGYS.aspx?categoryNum=012",    //未入围公示
                "http://www.jszb.com.cn/jszb/YW_info/PBJieGuoGS/MoreInfo_PBJieGuoGS.aspx?categoryNum=012",//评标结果公示
                "http://www.jszb.com.cn/jszb/YW_info/ZhongBiaoGS/MoreInfo_ZBGS.aspx?categoryNum=012"    //中标结果公告
        };
        String[] catchTypes = {"1", "0", "0", "0", "0", "2"};
        String[] noticeTypes = {"招标公告", "最高限价公示", "开标情况公示", "未入围公示", "评标结果公示", "中标公告"};

        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            Connection conn = null;
            Document doc = null;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            String __VIEWSTATE = null;
            String __VIEWSTATEGENERATOR = null;
            String __EVENTVALIDATION = null;
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    if (page == 1) {
                        //获取总页数
                        doc = conn.get();
                        String pageCont = doc.select("#MoreInfoList1_Pager").first().select("font[color=red]").get(1).text();
                        page = Integer.valueOf(pageCont.substring(pageCont.indexOf("/") + 1));
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    } else {
                        conn.data("__EVENTTARGET", "MoreInfoList1$Pager");
                        conn.data("__EVENTARGUMENT", String.valueOf(pagelist));
                        conn.data("__LASTFOCUS", "");
                        conn.data("__VIEWSTATE", __VIEWSTATE);
                        conn.data("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
                        conn.data("__VIEWSTATEENCRYPTED", "");
                        conn.data("__EVENTVALIDATION", __EVENTVALIDATION);
                        conn.data("MoreInfoList1$txtProjectName", "");
                        conn.data("MoreInfoList1$txtBiaoDuanName", "");
                        conn.data("MoreInfoList1$txtBiaoDuanNo", "");
                        conn.data("MoreInfoList1$txtJSDW", "");
                        conn.data("MoreInfoList1$StartDate", "");
                        conn.data("MoreInfoList1$EndDate", "");
                        conn.data("MoreInfoList1$jpdDi", "-1");
                        conn.data("MoreInfoList1$jpdXian", "-1");
                        doc = conn.post();
                    }
                    __VIEWSTATE = doc.select("#__VIEWSTATE").attr("value");
                    __VIEWSTATEGENERATOR = doc.select("#__VIEWSTATEGENERATOR").attr("value");
                    __EVENTVALIDATION = doc.select("#__EVENTVALIDATION").attr("value");
                    SnatchLogger.debug("第" + pagelist + "页");

                    Elements trs = doc.select("#MoreInfoList1_tdcontent").first().select(".moreinfoline");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().attr("onclick");
                        if (SnatchUtils.isNotNull(conturl)) {
                            Notice notice = new Notice();
                            conturl = conturl.substring(conturl.indexOf("..") + 2, conturl.indexOf("\",\""));
                            conturl = "http://www.jszb.com.cn/jszb/YW_info" + conturl;
                            String title = trs.get(row).select("a").first().attr("title");
                            String date = trs.get(row).select("td").last().text().trim();
                            String noticeType = trs.get(row).select("td").last().previousElementSibling().text().trim();
                            notice.setProvince("江苏省");
                            notice.setProvinceCode("jiangss");
                            notice.setCatchType(catchTypes[i]);
                            notice.setNoticeType(SnatchUtils.isNull(noticeType) ? noticeTypes[i] : noticeType);
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setAreaRank(PROVINCE);
                            notice.setSource(source);

                            Dimension dm = null;
                            // 地区维度
                            if (title.charAt(0) == '[' && title.contains("]")) {
                                String projDq = title.substring(title.indexOf("[") + 1, title.indexOf("]"));
                                if (!projDq.contains("省")) {
                                    dm = new Dimension();
                                    dm.setProjDq(projDq);
                                }
                            }
                            notice.setDimension(dm);

                            // 公告详情抓取
                            Document docCount = Jsoup.parse(new URL(conturl).openStream(), "gbk", conturl);
                            title = docCount.select("font[face=宋体-方正超大字符集]").first().text().trim();
                            String content = docCount.select("#Table1").first().select("tbody").first().html();
                            notice.setTitle(title);
                            notice.setContent(content);
                            notice = SnatchUtils.setCatchTypeByTitle(notice, notice.getTitle());
                            if (SnatchUtils.isNull(notice.getCatchType())) {
                                notice.setCatchType(catchTypes[i]);
                            } else {
                                SnatchUtils.judgeCatchType(notice, notice.getCatchType(), catchTypes[i]);
                            }

                            if (i == 0) {
                                dm = notice.getDimension();
                                if (dm == null) {
                                    dm = new Dimension();
                                }
                                // 招标公告 项目类型维度
                                String projType = docCount.select("tr#Tr14").first().nextElementSibling().nextElementSibling().select("td").last().text().trim();
                                dm.setProjType(projType);
                            } else if (i == 5) {
                                dm = notice.getDimension();
                                if (dm == null) {
                                    dm = new Dimension();
                                }
                                // 中标公告 第一候选人、项目负责人、报价、工期维度
                                String oneName = docCount.select("span#Label6").first().text().trim();
                                String oneProjDuty = docCount.select("span#Label7").first().text().trim();
                                String bj = docCount.select("span#Label9").first().text().trim();
                                String oneOffer = bj.contains("%") ? "" : String.valueOf(new BigDecimal(bj).multiply(new BigDecimal(10000)));
                                String projectTimeLimit = docCount.select("span#Label10").first().text().trim();
                                dm.setOneName(oneName);
                                dm.setOneProjDuty(oneProjDuty);
                                dm.setOneOffer(oneOffer);
                                dm.setProjectTimeLimit("/".equals(projectTimeLimit) ? "" : projectTimeLimit);
                            }

                            notice.setDimension(dm);

                            if (notice.getNoticeType().contains("采购")) {
                                page = govDetailHandle(notice, pagelist, page, row, trs.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, trs.size());
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
        return notice;
    }
}
