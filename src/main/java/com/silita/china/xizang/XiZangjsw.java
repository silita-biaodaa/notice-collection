package com.silita.china.xizang;

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

import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * http://www.xzcs.gov.cn   西藏自治区建设网
 * http://www.xzcs.gov.cn/zbggzbgg/index.jhtml  招标公告
 * http://www.xzcs.gov.cn/zhbgg/index.jhtml     中标公告
 * Created by maofeng on 2018/3/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "XiZangjsw")
public class XiZangjsw extends BaseSnatch {


    private static final String source = "xizang";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "http://www.xzcs.gov.cn/zbggzbgg/index.jhtml",  // 招标公告
                "http://www.xzcs.gov.cn/zhbgg/index.jhtml"      // 中标公告
        };
        for (int i = 0; i < urls.length; i++) {
            int page =1;
            int pageTemp = 0;
            Connection conn = null;
            Document doc = null;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if(pageTemp>0 && page>pageTemp){
                        break;
                    }
                    if(pagelist>1){
                        url = urls[i].replace("index","index_" + pagelist);
                    }
                    SnatchLogger.debug("第"+pagelist+"页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc= conn.get();
                    if(page ==1){
                        //获取总页数
                        String pageCont = doc.select(".pagination").first().text().trim();
                        page = Integer.valueOf(pageCont.substring(pageCont.indexOf("/")+1,pageCont.indexOf("页")));
                        pageTemp = page;
                        SnatchLogger.debug("总"+page+"页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }

                    Elements trs = doc.select("div.c1-bline");

                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");
                        if (SnatchUtils.isNotNull(conturl)) {
                            Notice notice = new Notice();
                            String title = trs.get(row).select("a").first().attr("title").trim();
                            String date = trs.get(row).select(".f-right").first().text().trim();

                            notice.setProvince("西藏自治区");
                            notice.setProvinceCode("xzzzq");

                            notice.setCatchType(i==0?"1":"2");
                            notice.setNoticeType(i==0?"招标公告":"中标公告");
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setAreaRank(PROVINCE);
                            notice.setSource(source);

                            page = detailHandle(notice, pagelist, page, row, trs.size());
                        }
                    }
                    if(pagelist==page){
                        page = turnPageEstimate(page);
                    }
                }
                super.saveAllPageIncrement(urls[i]);
            }finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = docCount.select("div.x-main-news-content").first().html();
        notice.setContent(content);
        notice = SnatchUtils.setCatchTypeByTitle(notice,notice.getTitle());
        if (SnatchUtils.isNull(notice.getCatchType())) {
            notice.setCatchType(catchType);
        } else {
            SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchType);
        }
        return notice;
    }
}
