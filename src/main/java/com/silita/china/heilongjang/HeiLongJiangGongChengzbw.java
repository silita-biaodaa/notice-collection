package com.silita.china.heilongjang;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.codehaus.jackson.JsonNode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.text.SimpleDateFormat;import static com.snatch.common.SnatchContent.*;

/**
 * //http://ztb.hljjs.gov.cn/ajaxtools.ashx?dopost=product_list&pagesize=5&CategoryID=2&sort=&keyword=&pageno=3&rnd=0.45509129495417144
 * 黑龙江工程招标网
 * http://ztb.hljjs.gov.cn/list_bidyw.aspx?CategoryID=2&Sort=18101 招标公告 >> 勘查设计
 * http://ztb.hljjs.gov.cn/list_bidyw.aspx?CategoryID=2&Sort=18102 招标公告 >> 施工
 * http://ztb.hljjs.gov.cn/list_bidyw.aspx?CategoryID=2&Sort=18103 招标公告 >> 监理
 * http://ztb.hljjs.gov.cn/list_bidyw.aspx?CategoryID=6&Sort=18101 中标公示 >> 勘查设计
 * http://ztb.hljjs.gov.cn/list_bidyw.aspx?CategoryID=6&Sort=18102 中标公示 >> 施工
 * http://ztb.hljjs.gov.cn/list_bidyw.aspx?CategoryID=6&Sort=18103 中标公示 >> 监理
 * Created by 91567 on 2018/3/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "HeiLongJiangGongChengzbw")
public class HeiLongJiangGongChengzbw extends BaseSnatch {

    private static final String source = "heilj";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Dimension dim ;

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://ztb.hljjs.gov.cn/ajaxtools.ashx?dopost=product_list&pagesize=5&CategoryID=2&sort=18101&keyword=&pageno=1&rnd=0.45509129495417144",
                "http://ztb.hljjs.gov.cn/ajaxtools.ashx?dopost=product_list&pagesize=5&CategoryID=2&sort=18102&keyword=&pageno=1&rnd=0.45509129495417144",
                "http://ztb.hljjs.gov.cn/ajaxtools.ashx?dopost=product_list&pagesize=5&CategoryID=2&sort=18103&keyword=&pageno=1&rnd=0.45509129495417144",
                "http://ztb.hljjs.gov.cn/ajaxtools.ashx?dopost=product_list&pagesize=5&CategoryID=6&sort=18101&keyword=&pageno=1&rnd=0.45509129495417144",
                "http://ztb.hljjs.gov.cn/ajaxtools.ashx?dopost=product_list&pagesize=5&CategoryID=6&sort=18102&keyword=&pageno=1&rnd=0.45509129495417144",
                "http://ztb.hljjs.gov.cn/ajaxtools.ashx?dopost=product_list&pagesize=5&CategoryID=6&sort=18103&keyword=&pageno=1&rnd=0.45509129495417144"
        };
        Connection conn ;
        Document doc ;
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
                        url = urls[i].replace("&pageno=1", "&pageno=" + pagelist);
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        JsonNode textStr = Util.getContentByJson(doc.text());
                        String countPage = textStr.findPath("pagecount").asText();
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("共" + page + "页");
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }

                    JsonNode trs = Util.getContentByJson(doc.text());
                    trs = trs.findPath("listdata");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).findPath("SUrl").asText();
                        if (!"".equals(conturl)) {
                            String date = sdf.format(sdf.parse(trs.get(row).findPath("FTime").asText().trim()));
                            String title = trs.get(row).findPath("Name").asText().trim();
                            Notice notice = new Notice();
                            notice.setProvince("黑龙江省");
                            notice.setProvinceCode("hlj");
                            notice.setUrl("http://ztb.hljjs.gov.cn/" + conturl);
                            notice.setNoticeType("工程建设");
                            notice.setTitle(title);
                            if(i <= 3) {
                                notice.setCatchType(ZHAO_BIAO_TYPE);
                            } else {
                                notice.setCatchType(ZHONG_BIAO_TYPE);
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
        Document contdoc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        String title = contdoc.select(".bidtit").text();
        contdoc.select("a").select("[href=http://office.hljjs.gov.cn:3000/Default.aspx?gid=67D531B7-3F10-46D8-A166-6555CD9F49C0]").remove();
        String content = contdoc.select(".bidtable").first().html();
        if(ZHAO_BIAO_TYPE.equals(notice.getCatchType())) {
            dim = new Dimension();
            String bmStartDate = contdoc.select(".bidtable").select("tbody").select("#lblFbidStartDate").text();
            if(bmStartDate.length() < 11) {
                if(SnatchUtils.isNotNull(bmStartDate)) {
                    dim.setBmStartDate(sdf.format(sdf.parse(bmStartDate)));
                }
            } else {
                dim.setBmStartDate(sdf.format(sdf.parse(bmStartDate.substring(0, bmStartDate.indexOf(" ")).replace("/", "-"))));
            }

            String bmEndDate = contdoc.select(".bidtable").select("tbody").select("#lblFBidendDate").text();
            if(bmEndDate.length() < 11) {
                if(SnatchUtils.isNotNull(bmEndDate)) {
                    dim.setBmEndDate(sdf.format(sdf.parse(bmEndDate)));
                }
            } else {
                dim.setBmEndDate(sdf.format(sdf.parse(bmEndDate.substring(0, bmEndDate.indexOf(" ")).replace("/", "-"))));
            }
            if(bmEndDate.length() > 11) {
                dim.setBmEndTime(bmEndDate.substring(bmEndDate.indexOf(" ")));
            }

            String bmSite = contdoc.select(".bidtable").select("tbody").select("#blFBidAddress").text();
            dim.setBmSite(bmSite);
            String zbContactMan = contdoc.select(".bidtable").select("tbody").select("#lblFDeputizeDeptLinkMan").text();
            dim.setZbContactMan(zbContactMan);
            String zbContactWay = contdoc.select(".bidtable").select("tbody").select("#lblFDeputizeDeptTel").text();
            dim.setZbContactWay(zbContactWay);
            dim.setZbContactWay(zbContactWay);
            dim.setDlName(contdoc.select(".bidtable").select("tbody").select("#lblFDeputizeDept").text());
//            dim.setProjXs(contdoc.select(".bidtable").select("tbody").select("#lblFProjectAddress").text());
            String projDq = notice.getTitle().substring(notice.getTitle().indexOf("【")+1, notice.getTitle().indexOf("】"));
            dim.setProjDq(projDq);
//            dim.setProjType(contdoc.select(".bidtable").select("tbody").select("#lblFBidContext").text());
            notice.setDimension(dim);
        }
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
