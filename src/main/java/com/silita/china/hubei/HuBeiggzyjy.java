package com.silita.china.hubei;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.HttpsSSLUtil;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.snatch.common.SnatchContent.GENG_ZHENG_TYPE;
import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * 湖北省公共资源交易电子服务系统（全国公共资源交易平台（湖北省）） https://www.hbggzyfwpt.cn
 * https://www.hbggzyfwpt.cn/jyxx/jsgcXmxx?area=000 工程建设 > 项目信息
 * https://www.hbggzyfwpt.cn/jyxx/jsgcZbgg?area=000 工程建设 > 招标公告
 * https://www.hbggzyfwpt.cn/jyxx/jsgcKbjl?area=000 工程建设 > 开标记录
 * https://www.hbggzyfwpt.cn/jyxx/jsgcpbjggs?area=000   工程建设 > 评标结果公示
 * https://www.hbggzyfwpt.cn/jyxx/jsgcZbjggs?area=000   工程建设 > 中标公告
 * https://www.hbggzyfwpt.cn/jyxx/zfcg/xmxx?area=000    政府采购 > 项目信息
 * https://www.hbggzyfwpt.cn/jyxx/zfcg/cggg?area=000    政府采购 > 采购公告
 * https://www.hbggzyfwpt.cn/jyxx/zfcg/gzsx?area=000    政府采购 > 更正公告
 * https://www.hbggzyfwpt.cn/jyxx/zfcg/zbjggs?area=000  政府采购 > 中标结果
 * Created by maofeng on 2018/3/2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "HuBeiggzyjy")
public class HuBeiggzyjy extends BaseSnatch {

    private static final String source = "hubei";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "https://www.hbggzyfwpt.cn/jyxx/jsgcXmxx",
                "https://www.hbggzyfwpt.cn/jyxx/jsgcZbgg",
                "https://www.hbggzyfwpt.cn/jyxx/jsgcKbjl",
                "https://www.hbggzyfwpt.cn/jyxx/jsgcpbjggs",
                "https://www.hbggzyfwpt.cn/jyxx/jsgcZbjggs",
                "https://www.hbggzyfwpt.cn/jyxx/zfcg/xmxx",
                "https://www.hbggzyfwpt.cn/jyxx/zfcg/cggg",
                "https://www.hbggzyfwpt.cn/jyxx/zfcg/gzsx",
                "https://www.hbggzyfwpt.cn/jyxx/zfcg/zbjggs"
        };
        Connection conn = null;
        Document doc = null;
        String[] catchTypes = {"0", "1", "0", "0", "2", "0", "1", GENG_ZHENG_TYPE, "2"};
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
                    // 忽略网站的安全认证
                    HttpsSSLUtil.neglectSSL();
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    if (page == 1) {
                        doc = conn.get();
                        //获取总页数
                        String pageCont = doc.select(".mmggxlh").select("a").last().previousElementSibling().text().trim();
                        page = Integer.valueOf(pageCont);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    } else {
                        conn.data("area", "000");
                        conn.data("currentPage", String.valueOf(pagelist));
                        conn.data("industriesTypeCode", "0");
                        conn.data("projectCode", "");
                        conn.data("projectName", "");
                        conn.data("scrollValue", "785");
                        conn.data("secondArea", "");
                        doc = conn.post();
                    }

                    SnatchLogger.debug("第" + pagelist + "页");
                    Elements trs = doc.select("#data_tab").first().select("tbody").first().select("tr");
                    for (int row = 1; row < trs.size(); row++) {
                        Notice notice = new Notice();
                        String detailUrl = trs.get(row).select("a").first().absUrl("href");
                        String title = trs.get(row).select("a").first().attr("title").trim();
                        String publishDate = trs.get(row).select("td").last().text().trim();
                        notice.setProvince("湖北省");
                        notice.setProvinceCode("hubs");
                        notice.setCatchType(catchTypes[i]);
                        if (i > 4) {
                            notice.setNoticeType("政府采购");
                        } else {
                            notice.setNoticeType("工程建设");
                        }
                        notice.setUrl(detailUrl);
                        notice.setTitle(title);

                        if (!"0".equals(catchTypes[i])) {
                            notice = SnatchUtils.setCatchTypeByTitle(notice, title);
                            if (SnatchUtils.isNull(notice.getCatchType())) {
                                notice.setCatchType(catchTypes[i]);
                            } else {
                                SnatchUtils.judgeCatchType(notice, notice.getCatchType(), catchTypes[i]);
                            }
                        }
                        notice.setOpendate(publishDate);
                        notice.setSnatchNumber(snatchNumber);
                        notice.setAreaRank(PROVINCE);
                        notice.setSource(source);
                        if (notice.getNoticeType().contains("采购")) {
                            page = govDetailHandle(notice, pagelist, page, row, trs.size());
                        } else {
                            page = detailHandle(notice, pagelist, page, row, trs.size());
                        }

                    }
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
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
        HttpsSSLUtil.neglectSSL(); // 忽略安全证书
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = docCount.select(".detail_contect").first().html();
        notice.setContent(content);
        try {
            sdf.format(sdf.parse(notice.getOpendate()));
        } catch (ParseException e) {
            String date = docCount.select(".kdg").first().text();
            date = date.substring(date.indexOf("发布时间") + 4).trim();
            notice.setOpendate(date);
        }
        return notice;
    }
}
