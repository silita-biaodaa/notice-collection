package com.silita.china.anhui;

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
import org.springframework.util.StringUtils;

import static com.snatch.common.SnatchContent.*;


/**
 * http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=714&scid=713&srcode=&sttype=&stime=36500&stitle=&pageSize=15&pageNum=1     建设工程 招标事项核准
 * http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=714&scid=596&srcode=&sttype=%E5%85%B6%E4%BB%96&stime=36500&stitle=&pageNum=1&pageSize=15  建设工程 资格预审公告
 * http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=714&scid=597&srcode=&sttype=%E5%85%B6%E4%BB%96&stime=36500&stitle=&pageNum=1&pageSize=15  建设工程 招标信息
 * http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=714&scid=599&srcode=&sttype=%E5%85%B6%E4%BB%96&stime=36500&stitle=&pageNum=1&pageSize=15  建设工程 变更公告
 * http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=714&scid=600&srcode=&sttype=%E5%85%B6%E4%BB%96&stime=36500&stitle=&pageNum=1&pageSize=15  建设工程 中标候选人及中标结果公示
 * http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=739&scid=740&srcode=&sttype=%E5%85%B6%E4%BB%96&stime=36500&stitle=&pageNum=1&pageSize=15  政府采购 交易信息
 * http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=739&scid=741&srcode=&sttype=%E5%85%B6%E4%BB%96&stime=36500&stitle=&pageNum=1&pageSize=15  政府采购 中标（成交）公示
 *
 * http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=569&scid=604&srcode=&sttype=%E5%85%B6%E4%BB%96&stime=36500&stitle=&pageNum=2&pageSize=15   自愿采招项目 采购公告
 * http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=569&scid=605&srcode=&sttype=%E5%85%B6%E4%BB%96&stime=36500&stitle=&pageNum=2&pageSize=15   自愿采招项目 自主采招项目变更公告
 * http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=569&scid=606&srcode=&sttype=&stime=36500&stitle=&pageNum=1&pageSize=15 自愿采招项目 中标候选人及中标结果公示
 * Created by maofeng on 2018/3/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "AnHuiztbjgw")
public class AnHuiztbjgw extends BaseSnatch {

    private static final String source = "anh";


    @Test
    @Override
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String urls[] = {
                "http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=714&scid=713&srcode=&sttype=&stime=73000&stitle=&pageSize=15&pageNum=1",
                "http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=714&scid=596&srcode=&sttype=&stime=73000&stitle=&pageSize=15&pageNum=1",
                "http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=714&scid=597&srcode=&sttype=&stime=73000&stitle=&pageSize=15&pageNum=1",
                "http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=714&scid=599&srcode=&sttype=&stime=73000&stitle=&pageSize=15&pageNum=1",
                "http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=714&scid=600&srcode=&sttype=&stime=73000&stitle=&pageSize=15&pageNum=1",
                "http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=739&scid=740&srcode=&sttype=&stime=73000&stitle=&pageSize=15&pageNum=1",
                "http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=739&scid=741&srcode=&sttype=&stime=73000&stitle=&pageSize=15&pageNum=1",
                "http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=569&scid=604&srcode=&sttype=&stime=73000&stitle=&pageSize=15&pageNum=1",
                "http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=569&scid=605&srcode=&sttype=&stime=73000&stitle=&pageSize=15&pageNum=1",
                "http://www.ahtba.org.cn/Notice/AnhuiNoticeSearch?spid=569&scid=606&srcode=&sttype=&stime=73000&stitle=&pageSize=15&pageNum=1"
        };
        String[] categoryType = {ZHAO_BIAO_TYPE, ZI_GE_YU_SHEN_TYPE, ZHAO_BIAO_TYPE, GENG_ZHENG_TYPE, ZHONG_BIAO_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, ZHAO_BIAO_TYPE, GENG_ZHENG_TYPE, ZHONG_BIAO_TYPE};

        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].substring(0, urls[i].lastIndexOf("=") + 1) + pagelist;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        if(!StringUtils.isEmpty(doc.select(".pagination").html())) {
                            String textStr = doc.select(".pagination").select("a").last().attr("onclick");
                            String pageStr = textStr.substring(textStr.indexOf("(") + 1, textStr.indexOf(","));
                            page = Integer.parseInt(pageStr);
                        } else {
                            page = 1;
                        }
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }

                    Elements trs = doc.select(".iweifa_right_nr").last().select("p");
                    for (int row = 1; row < trs.size(); row++) {
                        String detailUrl = trs.get(row).select("a").first().absUrl("href");
                        String title = trs.get(row).select("a").first().attr("title");
                        String openDate = trs.get(row).select("span").first().text();

                        Notice notice = new Notice();
                        notice.setProvince("安徽省");
                        notice.setProvinceCode("ahs");
                        notice.setAreaRank(PROVINCE);
                        notice.setSource(source);
                        notice.setTitle(title);

                        if(i == 0) {
                            notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                        } else {
                            notice.setCatchType(categoryType[i]);
                        }

                        if (i <= 4) {
                            notice.setNoticeType("建设工程");
                            if(notice.getTitle().contains("采购")) {
                                notice.setNoticeType("政府采购");
                            }
                        } else {
                            notice.setNoticeType("政府采购");
                        }

                        notice.setUrl(detailUrl);
                        notice.setOpendate(openDate);
                        notice.setSnatchNumber(snatchNumber);
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
        String contentId = href.substring(href.lastIndexOf("=") + 1);
        String detailUrl = "http://www.ahtba.org.cn/Notice/NoticeContent";
        Connection conn = Jsoup.connect(detailUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
        conn.cookie("ASP.NET_SessionId", "2lq2wsyinuqkvhlv2ya33fm3");
        conn.header("Content-Type", "application/x-www-form-urlencoded");
        conn.data("id", contentId);
        Document doc = conn.post();
        String content = doc.select(".zbdl_nr").html();
        notice.setContent(content);
        return notice;
    }
}
