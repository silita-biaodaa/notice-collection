package com.silita.china.hainan;

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

import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * Created by hujia on 2018/02/28
 * 海南省公共资源交易网
 * 建设工程
 http://zw.hainan.gov.cn/ggzy/ggzy/jgzbgg/index.jhtml    // 工程建设--招标
 http://zw.hainan.gov.cn/ggzy/ggzy/jgzbgs/index.jhtml    // 工程建设--中标
 * 政府采购
 http://zw.hainan.gov.cn/ggzy/ggzy/cggg/index.jhtml      // 政府采购--招标
 http://zw.hainan.gov.cn/ggzy/ggzy/cgzbgg/index.jhtml    // 政府采购--中标
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HaiNanggzyjyw")
public class HaiNanggzyjyw extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        firstGetMaxId();
    }

    public void firstGetMaxId() throws Exception {
        String urls[] = {
                "http://zw.hainan.gov.cn/ggzy/ggzy/jgzbgg/index.jhtml",
                "http://zw.hainan.gov.cn/ggzy/ggzy/jgzbgs/index.jhtml",
                "http://zw.hainan.gov.cn/ggzy/ggzy/cggg/index.jhtml",
                "http://zw.hainan.gov.cn/ggzy/ggzy/cgzbgg/index.jhtml"
        };

        String[] catchTypes = {"1","2","1","2"};
        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            boolean isEmptyList = false;
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].replace("index.jhtml","index_"+pagelist+".jhtml");
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (pagelist == 1) {
                            String pageText= doc.select(".pagesite").text();
                        int pageCount= Integer.parseInt(pageText.substring(pageText.indexOf("共")+1,pageText.indexOf("条")));
                            if(pageCount%10==0){
                                page = pageCount/10;
                            }else{
                                page = pageCount/10+1;
                            }
                            pageTemp = page;
                            //===入库代码====
                            page = computeTotalPage(page, LAST_ALLPAGE);
                            SnatchLogger.debug("总" + page + "页");
                    }
                    Elements trs = doc.select(".newtable").select("tbody").select("tr");
                    for (int row = 0; row < 10; row++) {
                        Dimension dimension = new Dimension();
                        String href = trs.get(row).select("a").first().absUrl("href");
                        String title = trs.get(row).select("a").first().attr("title");
                        String city = trs.get(row).select("td").get(1).text();
                        String date = trs.get(row).select("td").get(3).text();
                        //===入库代码====
                        Notice notice = new Notice();
                        if(!city.contains("省")){
                            dimension.setProjDq(city+"市");
                        }
                        notice.setProvince("海南省");
                        notice.setProvinceCode("hns");
                        notice.setAreaRank(PROVINCE);
                        notice.setSnatchNumber(snatchNumber);
                        notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                        if (SnatchUtils.isNull(notice.getCatchType())) {
                            notice.setCatchType(catchTypes[i]);
                        } else {
                            SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchTypes[i]);
                        }
                        if (i > 1) {
                            notice.setNoticeType("政府采购");
                        } else {
                            notice.setNoticeType("建设工程");
                        }
                        notice.setUrl(href);
                        notice.setSource("hain");
                        notice.setOpendate(date);
                        if(dimension.getProjDq()!=null){
                            notice.setDimension(dimension);
                        }
                        CURRENT_PAGE_LAST_OPENDATE = date;
                        notice.setTitle(title);
                        //详情信息入库，获取增量页数
                        if (notice.getNoticeType().contains("采购")) {
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
                if (e instanceof InterruptedException) {
                    throw e;
                }
            } finally {
                super.clearClassParam();
            }
        }
    }


    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = docCount.select(".newsCon").html();
        notice.setContent(content);
        return notice;
    }
}
