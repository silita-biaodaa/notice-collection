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

import static com.snatch.common.SnatchContent.ZHAO_BIAO_TYPE;

/**
 * 株洲市公共交易中心
 * http://www.zzzyjy.cn/016/016001/016001001/1.html 建设工程 > 房屋建筑 > 招标公告
 * http://www.zzzyjy.cn/016/016002/016002001/1.html 建设工程 > 市政工程 > 招标公告
 * http://www.zzzyjy.cn/016/016003/016003001/1.html 建设工程 > 交通 > 招标公告
 * http://www.zzzyjy.cn/016/016004/016004001/1.html 建设工程 > 水利水电 > 招标公告
 * http://www.zzzyjy.cn/016/016005/016005001/1.html 建设工程 > 其他 > 招标公告
 * Created by dh on 2017/6/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "HuNanZhuZhouPrtc")
public class HuNanZhuZhouPrtc extends BaseSnatch {

    int page = 1;

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = new String[]{
                "http://www.zzzyjy.cn/016/016001/016001001/1.html",
                "http://www.zzzyjy.cn/016/016002/016002001/1.html",
                "http://www.zzzyjy.cn/016/016003/016003001/1.html",
                "http://www.zzzyjy.cn/016/016004/016004001/1.html",
                "http://www.zzzyjy.cn/016/016005/016005001/1.html"
        };
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            Connection conn = null;
            Document doc = null;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].substring(0, urls[i].lastIndexOf("/") + 1) + pagelist + ".html";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        //获取总页数
                        String pageCont = doc.select("#index").first().text().trim();
                        page = Integer.valueOf(pageCont.substring(pageCont.indexOf("/") + 1));
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".ewb-info-list").first().select("[class=ewb-list-node clearfix]");
                    for (int row = 0; row < trs.size(); row++) {
                        if (SnatchUtils.isNotNull(trs.get(row).text())) {
                            Notice notice = new Notice();
                            String contUrl = trs.get(row).select("a").first().absUrl("href");
                            String date = trs.get(row).select(".ewb-list-date").text().trim();
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("株洲市");
                            notice.setCityCode("zz");
                            notice.setCatchType(ZHAO_BIAO_TYPE);
                            notice.setNoticeType("建设工程");
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            notice.setUrl(contUrl);
                            page = detailHandle(notice, pagelist, page, row, trs.size());
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
        Document docCount = Jsoup.parse(new URL(href).openStream(), "UTF-8", href);
        String title = docCount.select(".ewb-article-hd").first().text().trim();
        String content = docCount.select("[class=ewb-article-content con]").html();
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
