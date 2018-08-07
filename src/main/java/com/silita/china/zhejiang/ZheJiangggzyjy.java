package com.silita.china.zhejiang;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.snatch.common.SnatchContent.PROVINCE;
import static com.snatch.common.SnatchContent.ZI_GE_YU_SHEN_TYPE;

/**
 * 浙江省公共资源交易服务平台    http://www.zjpubservice.com
 * http://www.zjpubservice.com/002/infogov.html 招标公告、资格预审公告、开标结果公示、中标候选人公示、中标结果公告
 * Created by maofeng on 2018/3/5.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "ZheJiangggzyjy")
public class ZheJiangggzyjy extends BaseSnatch {

    private static final String source = "zhej";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "http://www.zjpubservice.com/fulltextsearch/rest/getfulltextdata?format=json&sort=0&rmk1=002001001&pn=0&rn=20&idx_cgy=web", //工程项目--招标公告
                "http://www.zjpubservice.com/fulltextsearch/rest/getfulltextdata?format=json&sort=0&rmk1=002001002&pn=0&rn=20&idx_cgy=web", //工程项目--资格预审公告
                "http://www.zjpubservice.com/fulltextsearch/rest/getfulltextdata?format=json&sort=0&rmk1=002001003&pn=0&rn=20&idx_cgy=web", //工程项目--开标结果公示
                "http://www.zjpubservice.com/fulltextsearch/rest/getfulltextdata?format=json&sort=0&rmk1=002001004&pn=0&rn=20&idx_cgy=web", //工程项目--中标候选人公示
                "http://www.zjpubservice.com/fulltextsearch/rest/getfulltextdata?format=json&sort=0&rmk1=002001005&pn=0&rn=20&idx_cgy=web", //工程项目--中标结果公告
                "http://www.zjpubservice.com/fulltextsearch/rest/getfulltextdata?format=json&sort=0&rmk1=002002001&pn=0&rn=20&idx_cgy=web", //政府采购--采购公告
                "http://www.zjpubservice.com/fulltextsearch/rest/getfulltextdata?format=json&sort=0&rmk1=002002002&pn=0&rn=20&idx_cgy=web", //政府采购--中标成交公告
                "http://www.zjpubservice.com/fulltextsearch/rest/getfulltextdata?format=json&sort=0&rmk1=002005001&pn=0&rn=20&idx_cgy=web", //其他交易--交易公告
                "http://www.zjpubservice.com/fulltextsearch/rest/getfulltextdata?format=json&sort=0&rmk1=002005002&pn=0&rn=20&idx_cgy=web"  //其他交易--交易结果
        };
        String[] catchTypes = {"1", ZI_GE_YU_SHEN_TYPE, "0", "2", "2", "1", "2", "1", "2"};
        String[] noticeTypes = {"工程项目", "工程项目", "工程项目", "工程项目", "工程项目", "政府采购", "政府采购", "其他交易", "其他交易"};
        String[] hrefHeads = {
                "http://zjpubservice.zjzwfw.gov.cn/002/002001/002001001/",
                "http://zjpubservice.zjzwfw.gov.cn/002/002001/002001002/",
                "http://zjpubservice.zjzwfw.gov.cn/002/002001/002001003/",
                "http://zjpubservice.zjzwfw.gov.cn/002/002001/002001004/",
                "http://zjpubservice.zjzwfw.gov.cn/002/002001/002001005/",
                "http://zjpubservice.zjzwfw.gov.cn/002/002002/002002001/",
                "http://zjpubservice.zjzwfw.gov.cn/002/002002/002002002/",
                "http://zjpubservice.zjzwfw.gov.cn/002/002005/002005001/",
                "http://zjpubservice.zjzwfw.gov.cn/002/002005/002005002/"
        };
        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].replace("&pn=0", "&pn=" + (pagelist - 1));
                    }
                    SnatchLogger.debug("第" + pagelist + "页");

                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    String docStr = conn.execute().body();
                    JSONObject jsonStr = new JSONObject(docStr);
                    JSONObject result = jsonStr.getJSONObject("result");
                    if (page == 1) {
                        //获取总页数
                        String totalcount = result.getString("totalcount");
                        int count = Integer.parseInt(totalcount);
                        if (count % 20 == 0) {
                            page = count / 20;
                        } else {
                            page = (count / 20) + 1;
                        }
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }

                    JSONArray trs = new JSONArray(result.get("records").toString());
                    for (int row = 0; row < trs.length(); row++) {
                        JSONObject tr = new JSONObject(trs.get(row).toString());
                        Notice notice = new Notice();
                        String uuid = tr.getString("guid");
                        String title = tr.getString("title");
                        String date = tr.getString("date");
                        date = date.substring(0, 10);
                        notice.setProvince("浙江省");
                        notice.setProvinceCode("zjs");
                        notice.setCatchType(catchTypes[i]);
                        notice.setNoticeType(noticeTypes[i]);
                        notice.setTitle(title);
                        notice.setOpendate(date);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setAreaRank(PROVINCE);
                        notice.setSource(source);
                        String href = hrefHeads[i] + (date.replaceAll("-", "")) + "/" + uuid + ".html";
                        notice.setUrl(href);

                        // 维度信息
                        Dimension dm = new Dimension();
                        String projDq = tr.getString("remark5");
                        if (SnatchUtils.isNotNull(projDq) && !projDq.contains("省")) {
                            dm.setProjDq(projDq);
                            notice.setDimension(dm);
                        }

                        String dimensions = tr.getString("content");
                        String tempStr = "";

                        if (dimensions.contains("招标文件获取截止时间:")) {
                            tempStr = dimensions.substring(dimensions.indexOf("招标文件获取截止时间:") + 11).trim();
                            dm.setBmEndDate(tempStr.substring(0, tempStr.indexOf(" ")));
                            notice.setDimension(dm);
                        }
                        if (dimensions.contains("中标人:")) {
                            tempStr = dimensions.substring(dimensions.indexOf("中标人:") + 4).trim();
                            dm.setOneName(tempStr.substring(0, tempStr.indexOf(" ")));
                            notice.setDimension(dm);
                        }

                        if (notice.getNoticeType().contains("采购")) {
                            page = govDetailHandle(notice, pagelist, page, row, trs.length());
                        } else {
                            page = detailHandle(notice, pagelist, page, row, trs.length());
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
        Connection co1 = Jsoup.connect(href).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true).ignoreContentType(true);
        Document docCount = co1.get();
        String content = docCount.select(".article_con").first().select("table").first().html();
        content = content.replace("/images/OldUrl.png", "http://zjpubservice.zjzwfw.gov.cn//images/OldUrl.png");
        notice.setContent(content);
        notice = SnatchUtils.setCatchTypeByTitle(notice, notice.getTitle());
        if (SnatchUtils.isNull(notice.getCatchType())) {
            notice.setCatchType(catchType);
        } else {
            SnatchUtils.judgeCatchType(notice, notice.getCatchType(), catchType);
        }
        return notice;
    }

}
