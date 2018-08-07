package com.silita.china.hunan;

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

import static com.snatch.common.SnatchContent.CITY;
import static com.snatch.common.SnatchContent.DA_YI_TYPE;
/**
 * 长沙公共资源交易中心 -- 答疑
 * https://csggzy.gov.cn/ProjectDY.aspx/Index/1?type=21&sel_dystat=4
 * Created by maofeng on 2018/1/3.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})
@Component
@JobHander(value="HuNanChangShaggzydy")
public class HuNanChangShaggzydy extends BaseSnatch{


    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask () throws Exception {
        int page = 1;
        int pageTemp = 0;
        Connection conn = null;
        Document doc = null;
        String staticUrl = "https://csggzy.gov.cn/ProjectDY.aspx/Index/1?type=21&sel_dystat=4";
        String dyUrl = staticUrl;
        String snatchNumber = SnatchUtils.makeSnatchNumber();
        super.queryBeforeSnatchState(staticUrl);
        for (int pagelist = 1; pagelist <= page; pagelist++) {
            if(pageTemp>0 && page>pageTemp){
                break;
            }
            if(pagelist > 1){
                dyUrl = staticUrl.replace("Index/1","Index/" + pagelist);
            }
            SnatchLogger.debug("第"+pagelist+"页");
            conn = Jsoup.connect(dyUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
            doc = conn.get();
            if(page ==1){
                //获取总页数
                String pageCont = doc.select("#pagecont").first().text().trim();
                page = Integer.valueOf(pageCont.substring(pageCont.lastIndexOf("/") + 1,pageCont.length()));
                SnatchLogger.debug("总"+page+"页");
                page = computeTotalPage(page,LAST_ALLPAGE);
            }
            Elements trs = doc.select("table[width=\"996\"]").first().select("tr");
            for (int row = 1; row < trs.size(); row++) {
                String conturl = trs.get(row).select("td").last().select("a").last().absUrl("href");
                if (SnatchUtils.isNotNull(conturl)) {
                    Notice notice = new Notice();
                    String noticeType = trs.get(row).select("td").get(3).text().trim();
                    String date = trs.get(row).select("td").get(5).text().trim();
                    date = date.substring(0,date.indexOf(" "));
                    notice.setProvince("湖南省");
                    notice.setProvinceCode("huns");
                    notice.setCity("长沙市");
                    notice.setCityCode("cs");
                    notice.setAreaRank(CITY);
                    notice.setSnatchNumber(snatchNumber);
                    notice.setCatchType(DA_YI_TYPE);
                    notice.setNoticeType(noticeType);
                    notice.setUrl(conturl);
                    notice.setOpendate(date);
                    CURRENT_PAGE_LAST_OPENDATE = date;
                    page = detailHandle(notice,pagelist,page,row,trs.size());
                }
            }
            if(pagelist==page){
                page = turnPageEstimate(page);
            }
        }
        super.saveAllPageIncrement(staticUrl);
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document detailDoc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        Elements e = detailDoc.select(".mm738_1").first().select("div");
        String title = detailDoc.select(".mm738_1").first().select("div").get(3).text().trim();
        title = title.substring(title.indexOf("项目名称：") + 5);
        title += "答疑";
        String content = detailDoc.select(".mm738_1").first().select("tbody").first().select("tr").get(1).html();
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
