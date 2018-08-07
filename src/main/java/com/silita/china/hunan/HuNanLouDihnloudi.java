package com.silita.china.hunan;

import com.alibaba.fastjson.JSONArray;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.snatch.model.TbSnatchStatistics;
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
import java.text.SimpleDateFormat;
import java.util.*;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by wangying on 2017/5/23/0023.
 * 娄底市公共资源交易中心  http://ldggzy.hnloudi.gov.cn
 * http://ldggzy.hnloudi.gov.cn/jyxx/gcjsjyxx/zbgg/ 工程建设--招标公告
 * http://ldggzy.hnloudi.gov.cn/jyxx/gcjsjyxx/zbgs/ 工程建设--中标（废标）公示
 * http://ldggzy.hnloudi.gov.cn/jyxx/gcjsjyxx/gzgg/ 工程建设--补充（变更）公告
 * http://ldggzy.hnloudi.gov.cn/jyxx/gcjsjyxx/dygg/ 工程建设--答疑公告
 * http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/zbgg_11958/   政府采购--招标公告
 * http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/gzgg_11960/   政府采购--补充（变更）公告
 * http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/zbgs_11959/    政府采购--中标（废标）公示
 * http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/htgg/ 政府采购--合同公告
 * http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/dygg_11961/   政府采购--答疑公告
 * http://ldggzy.hnloudi.gov.cn/jyxx/zjjg/zbgg_11992/   中介机构--公告（公示）
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanLouDihnloudi")
public class HuNanLouDihnloudi extends BaseSnatch {

    private int min = 1;
    private int max = 5;
    private Random random = new Random();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://ldggzy.hnloudi.gov.cn/jyxx/gcjsjyxx/zbgg/",
                "http://ldggzy.hnloudi.gov.cn/jyxx/gcjsjyxx/zbgs/",
                "http://ldggzy.hnloudi.gov.cn/jyxx/gcjsjyxx/gzgg/",
                "http://ldggzy.hnloudi.gov.cn/jyxx/gcjsjyxx/dygg/",
                "http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/zbgg_11958/",
                "http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/gzgg_11960/",
                "http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/zbgs_11959/",
                "http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/htgg/",
                "http://ldggzy.hnloudi.gov.cn/jyxx/zfcgjyxx/dygg_11961/",
                "http://ldggzy.hnloudi.gov.cn/jyxx/zjjg/zbgg_11992/"
        };
        String[] catchTypes = {ZHAO_BIAO_TYPE, ZHONG_BIAO_BU_CHONG_TYPE, GENG_ZHENG_TYPE, DA_YI_TYPE, ZHAO_BIAO_TYPE, GENG_ZHENG_TYPE, ZHONG_BIAO_BU_CHONG_TYPE, HE_TONG_TYPE, DA_YI_TYPE, ZHAO_BIAO_TYPE};
        String source = "hunan";
        String siteName = "娄底市公共资源交易中心";

        Map cookies = null;
        Connection conn = null;
        Document doc = null;

        List<String> pageNumList = new ArrayList<>();
        List<String> openDateList = new ArrayList<>();
        String startTime = sdf.format(new Date());
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);//查询url前次抓取的情况（最大页数与公示时间）
                try {
                    for (int pagelist = 1; pagelist <= page; pagelist++) {
                        if (pagelist > 1) {
                            url = urls[i] + "index_" + (pagelist - 1) + ".html";
                        }
                        SnatchLogger.debug("第" + pagelist + "页");
                        conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true).followRedirects(true);
                        conn.header("Upgrade-Insecure-Requests", "1");
                        if (i == 0 && pagelist == 1) {
//                            conn.header("X-DevTools-Emulate-Network-Conditions-Client-Id", "59B419EB48287DFA862413F5748D3342");
                            conn.header("X-DevTools-Emulate-Network-Conditions-Client-Id", SnatchUtils.getStringRandom(32).toUpperCase());
                        } else {
                            conn.cookies(cookies);
                        }

                        doc = conn.get();
                        cookies = conn.response().cookies();
                        if (page == 1) {
                            Element item = doc.select(".page").first();
                            Element ele = item.getElementsByTag("script").first();
                            String pageStr = ele.childNode(0).toString();
                            String countPage = pageStr.substring(pageStr.indexOf("createPageHTML") + 15, pageStr.indexOf(","));
                            page = Integer.parseInt(countPage);
                            SnatchLogger.debug("总" + page + "页");
                            pageNumList.add("[url:" + url + "]collection_total:" + page);
                            page = computeTotalPage(page, LAST_ALLPAGE);
                        }

                        Elements html = doc.select(".newsList");
                        Elements trs = html.select("li"); // 得到正文的一条
                        for (int row = 0; row < trs.size(); row++) {
                            Element tr = trs.get(row);
                            String conturl = tr.select("a").first().absUrl("href");
                            if (!"".equals(conturl)) {
                                String title = trs.get(row).select("a").first().attr("title").trim();
                                String date = tr.select("span").text().trim();
                                Notice notice = new Notice();
                                notice.setProvince("湖南省");
                                notice.setProvinceCode("huns");
                                notice.setCity("娄底市");
                                notice.setCityCode("ld");
                                notice.setAreaRank(CITY);
                                notice.setSnatchNumber(snatchNumber);
                                notice.setUrl(conturl);

                                notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                                if (SnatchUtils.isNull(notice.getCatchType())) {
                                    notice.setCatchType(catchTypes[i]);
                                }

                                if (i < 3) {
                                    notice.setNoticeType("工程建设");
                                } else if (i < 7) {
                                    notice.setNoticeType("政府采购");
                                } else {
                                    notice.setNoticeType("中介代理");
                                }

                                notice.setTitle(title);
                                notice.setOpendate(date);
                                CURRENT_PAGE_LAST_OPENDATE = date;
                                notice.setSityClassify(source + ":" + siteName + ":" + urls[i]);
                                //详情信息入库，获取增量页数
                                if (notice.getNoticeType().contains("采购")) {
                                    page = govDetailHandle(notice, pagelist, page, row, trs.size());
                                } else {
                                    page = detailHandle(notice, pagelist, page, row, trs.size());
                                }
                                //最近公式时间
                                if (pagelist == 1 && row == 0) {
                                    openDateList.add(i, "[url:" + url + "]startOpen_date:" + date);
                                }
                                //最远公式时间
                                if (pagelist == page && row == trs.size() - 1) {
                                    openDateList.set(i, openDateList.get(i) + "endOpen_date:" + date);
                                }
                                Thread.sleep(1000 * (random.nextInt(max) % (max - min + 1)));
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
                }
            } finally {
                clearClassParam();
            }
        }
        /**
         * 统计业务
         */
        String endTime = sdf.format(new Date());
        TbSnatchStatistics tbSnatchStatistics = new TbSnatchStatistics();
        tbSnatchStatistics.setSource(source);
        tbSnatchStatistics.setSiteDomainName("http://ldggzy.hnloudi.gov.cn/");
        tbSnatchStatistics.setSiteName(siteName);
        tbSnatchStatistics.setClassPageNum(JSONArray.toJSON(pageNumList).toString());
        tbSnatchStatistics.setClassDateDifference(JSONArray.toJSON(openDateList).toString());
//        tbSnatchStatistics.setExceptionTotal(service.getExceptionCount(siteName));
        tbSnatchStatistics.setUrlTotal(urls.length);
        tbSnatchStatistics.setExecuteDate("[startDate:" + startTime + "][endDate:" + endTime + "]");
        for (int i = 0; i < pageNumList.size(); i++) {
            String pageStr = pageNumList.get(i);
            Integer page = Integer.parseInt(pageStr.substring(pageStr.lastIndexOf(":") + 1));
            String url = null;
            if (page == 1) {
                url = urls[i];
            } else {
                url = urls[i] + "index_" + (page - 1) + ".html";
            }
            conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
            doc = conn.get();
            Integer lastPageSize = doc.select(".newsList").select("li").size();
            Integer classPageCount = (page - 1) * 15 + lastPageSize;
            System.out.println(classPageCount + "共条******" + page);
        }
        snatchStatistics(tbSnatchStatistics);
//        service.insertNoticeStatistics(tbSnatchStatistics);
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Thread.sleep(800);
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = contdoc.select(".contentShow").select("h1").first().text().trim().replaceAll(" ", "");
        String content = contdoc.select("#fontzoom").html();
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }

}

