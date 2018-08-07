package com.silita.china.hunan.zhongbiao;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
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
import java.text.SimpleDateFormat;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by maofeng on 2017/7/18.
 * 长沙市望城县  望城区政府门户网站   http://www.wangcheng.gov.cn/
 * 中标候选人公示
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanChangShaShiwcx")
public class HuNanChangShaShiwcx extends BaseSnatch {


    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        int page = 1;
        int pageTemp = 0;
        String url = "http://www.wangcheng.gov.cn/xxgk_343/qzfgzbmhbmgljg/qcxjsj/zbhxrgs/index.html";
        String snatchNumber = SnatchUtils.makeSnatchNumber();
        super.queryBeforeSnatchState(url);
        try {
            for (int i = 1; i <= page; i++) {
                if (pageTemp > 0 && page > pageTemp) {
                    break;
                }
                if (i > 1) {
                    url = "http://www.wangcheng.gov.cn/xxgk_343/qzfgzbmhbmgljg/qcxjsj/zbhxrgs/index_" + (i - 1) + ".html";
                }
                SnatchLogger.debug(i + "页");
                Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                Document doc = conn.get();
                if (page == 1) {
                    Element item = doc.select(".newslist").first().select("script").first();    //得到纪录页数区域的div（此中包含需要得到的总页数）
                    String type = item.html();                      //得到纪录页数区域的内容（此DIV中有需要的text内容）
                    String countPage = type.substring(type.indexOf("(\"") + 2, type.indexOf("\","));   //获取总页数
                    page = Integer.parseInt(countPage);
                    pageTemp = page;
                    SnatchLogger.debug("总" + page + "页");
                    page = computeTotalPage(page, LAST_ALLPAGE);
                }
                Element ul = doc.select(".newslist_list").first();    //得到存放标题以及时间的区域ul
                Elements lis = ul.select("ul").first().select("li");      //得到div中的所有li
                for (int j = 0; j < lis.size(); j++) {
                    String conturl = lis.get(j).select("a").attr("href");  //得到标题（a）的href值
                    if (!"".equals(conturl)) {
                        String date = lis.get(j).select("span").text().trim();   //得到日期
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String date1 = sdf.format(sdf.parse(date));
                        String catchType = "中标公告";
                        Notice notice = new Notice();
                        notice.setProvince("湖南省");
                        notice.setProvinceCode("huns");
                        notice.setCity("长沙市");
                        notice.setCityCode("cs");
                        notice.setCounty("望城区");
                        notice.setCountyCode("wcq");
                        notice.setAreaRank(COUNTY);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setUrl(conturl);
                        notice.setNoticeType(catchType);
                        notice.setOpendate(date1);
                        CURRENT_PAGE_LAST_OPENDATE = date1;
                        page = detailHandle(notice, i, page, j, lis.size());
                    }
                }
                if (i == page) {
                    page = turnPageEstimate(page);
                }
            }
            super.saveAllPageIncrement(url);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            }
        } finally {
            super.clearClassParam();
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = contdoc.select("h2").first().text().trim();
        if (title.contains("流标")) {
            notice.setCatchType(LIU_BIAO_TYPE);
        } else if (title.contains("废标")) {
            notice.setCatchType(FEI_BIAO_TYPE);
        } else if (title.contains("中标") && (title.contains("补充") || title.contains("更正") || title.contains("更改")
            ||title.contains("修改"))) {
            notice.setCatchType(ZHONG_BIAO_BU_CHONG_TYPE);
        } else {
            notice.setCatchType(ZHONG_BIAO_TYPE);
        }
        String content = contdoc.select(".TRS_PreAppend").html();              //得到中标详细内容区域
        if (!(content.length() > 0)) {
            content = String.valueOf(contdoc.select(".con").first().select("p"));
        }
        if (content.length() <= 100) {
            content = contdoc.select("#lbProclaimContent").html();
        }
        notice.setContent(content);
        notice.setTitle(title);
        return notice;
    }

}
