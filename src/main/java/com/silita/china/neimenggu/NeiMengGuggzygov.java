package com.silita.china.neimenggu;

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

import java.net.URL;
import java.text.SimpleDateFormat;import static com.snatch.common.SnatchContent.*;

/**
 * 内蒙古自治区公共资源交易网
 * currentPage当前页码，area地区
 * http://www.nmgggzyjy.gov.cn/jyxx/jsgcZbgg?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0 工程建设>招标公告与资格预审公告
 * http://www.nmgggzyjy.gov.cn/jyxx/jsgcGzsx?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0 工程建设>招标文件/招标文件澄清与修改
 * http://www.nmgggzyjy.gov.cn/jyxx/jsgcZbhxrgs?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0 工程建设>中标候选人公示
 * http://www.nmgggzyjy.gov.cn/jyxx/jsgcZbjggs?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0 工程建设>交易结果公示
 * http://www.nmgggzyjy.gov.cn/jyxx/zfcg/cggg?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0 政府采购>采购/资格预审公告
 * http://www.nmgggzyjy.gov.cn/jyxx/zfcg/gzsx?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0 政府采购>更正公告
 * http://www.nmgggzyjy.gov.cn/jyxx/zfcg/zbjggs?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0 政府采购>中标公告
 * Created by 91567 on 2018/3/2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "NeiMengGuggzygov")
public class NeiMengGuggzygov extends BaseSnatch {

    private static final String source = "neimg";

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] areas = {"自治区001", "呼和浩特市002", "包头市003", "呼伦贝尔市004", "兴安盟005", "通辽市006", "赤峰市007", "锡林郭勒盟008",
                "乌兰察布市009", "鄂尔多斯市010", "巴彦淖尔市011", "乌海市012", "阿拉善盟013", "满洲里市014", "二连浩特市015"};  //地区&地区代码
        String[] urls = {
                "http://www.nmgggzyjy.gov.cn/jyxx/jsgcZbgg?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0",
                "http://www.nmgggzyjy.gov.cn/jyxx/jsgcGzsx?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0",
                "http://www.nmgggzyjy.gov.cn/jyxx/jsgcZbhxrgs?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0",
                "http://www.nmgggzyjy.gov.cn/jyxx/jsgcZbjggs?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0",
                "http://www.nmgggzyjy.gov.cn/jyxx/jsgcZbjggs?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0",
                "http://www.nmgggzyjy.gov.cn/jyxx/zfcg/gzsx?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0",
                "http://www.nmgggzyjy.gov.cn/jyxx/zfcg/zbjggs?currentPage=1&area=001&industriesTypeCode=000&scrollValue=0"
        };
        Connection conn;
        Document doc;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < urls.length; i++) { //7个url
            try {

                for (int area = 0; area < areas.length; area++) {  //14个地区
                    if (area > 0) { //替换地区代码
                        System.out.println(areas[area] + "栏目开始抓取");
                        for (int j = 0; j < areas.length; j++) {
                            String areaNum = areas[area].replaceAll("[^0-9]", "");
                            urls[i] = urls[i].replace("area=" + areas[area-1].replaceAll("[^0-9]", ""), "area=" + areaNum);
                        }
                    }
                    int page = 1;
                    int pageTemp = 0;
                    String url = urls[i];
                    //===入库代码====
                    super.queryBeforeSnatchState(urls[i]);//查询url前次抓取的情况（最大页数与公示时间）
                    for (int pagelist = 0; pagelist <= page; pagelist++) {  //页数
                        if (pageTemp > 0 && page > pageTemp) {
                            break;
                        }
                        if (pagelist > 1) {
                            url = urls[i].replace("currentPage=1", "currentPage=" + pagelist);
                        }
                        SnatchLogger.debug("第" + pagelist + "页");
                        conn = Jsoup.connect(url).userAgent("Mozilla").timeout(2000 * 60).ignoreHttpErrors(true);
                        doc = conn.get();
                        if (page == 1) {
                            String textStr = doc.select(".mmggxlh").select("a").last().previousElementSibling().text().trim();
                            page = Integer.parseInt(textStr);
                            pageTemp = page;
                            SnatchLogger.debug("共" + page + "页");
                            //===入库代码====
                            page = computeTotalPage(page, LAST_ALLPAGE);
                        }
                        Elements div = doc.select(".right").last().select("table");
                        Elements trs = div.select("tr");
                        if(trs.size() > 1) {    //有内容
                            for (int row = 1; row < trs.size(); row++) {
                                String conturl = trs.get(row).select("a").last().absUrl("href");
                                if (SnatchUtils.isNotNull(conturl)) {
                                    String title = trs.get(row).select("a").text();
                                    String date = sdf.format(sdf.parse(trs.get(row).select("td").last().text()));
                                    Notice notice = new Notice();
                                    notice.setProvince("内蒙古自治区");
                                    notice.setProvinceCode("nmgzzq");
                                    notice.setUrl(conturl);
                                    switch (i) {
                                        case 0:
                                            notice.setCatchType(ZHAO_BIAO_TYPE);
                                            notice.setNoticeType("工程建设");
                                            break;
                                        case 1:
                                            notice.setCatchType(GENG_ZHENG_TYPE);
                                            notice.setNoticeType("工程建设");
                                            break;
                                        case 2:
                                            notice.setCatchType(ZHONG_BIAO_TYPE);
                                            notice.setNoticeType("工程建设");
                                            break;
                                        case 3:
                                            notice.setCatchType(ZHONG_BIAO_TYPE);
                                            notice.setNoticeType("工程建设");
                                            break;
                                        case 4:
                                            notice.setCatchType(ZHAO_BIAO_TYPE);
                                            notice.setNoticeType("政府采购");
                                            break;
                                        case 5:
                                            notice.setCatchType(GENG_ZHENG_TYPE);
                                            notice.setNoticeType("政府采购");
                                            break;
                                        case 6:
                                            notice.setCatchType(ZHONG_BIAO_TYPE);
                                            notice.setNoticeType("政府采购");
                                            break;
                                    }
                                    if(title.contains("采购")) {
                                        notice.setNoticeType("政府采购");
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
                        }
                        //===入库代码====
                        if (pagelist == page) {
                            page = turnPageEstimate(page);
                        }
                    }
                    //===入库代码====
                    super.saveAllPageIncrement(urls[i]);
                }
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
        String title = contdoc.select(".content").select(".title").text();
        String content = contdoc.select(".detail_contect").html();
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
