package com.silita.china.guizhou;


import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.codehaus.jackson.JsonNode;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

import java.net.URL;

import static com.snatch.common.SnatchContent.PROVINCE;
import static com.snatch.common.SnatchContent.ZHAO_BIAO_TYPE;

/**
 * http://www.gzsztb.gov.cn/api/trade/search?pubDate=all&region=5200&industry=all&prjType=A&noticeType=all&noticeClassify=all&args=&pageIndex=1    //招标公告 工程建设
 * http://www.gzsztb.gov.cn/api/trade/search?pubDate=all&region=5200&industry=all&prjType=D&noticeType=all&noticeClassify=all&args=&pageIndex=1   //中标公示  工程建设
 * http://www.gzsztb.gov.cn/api/trade/search?pubDate=all&region=5200&industry=all&prjType=A&noticeType=all&noticeClassify=all&args=&pageIndex=1   //招标公告 政府采购
 * http://www.gzsztb.gov.cn/api/trade/search?pubDate=all&region=5200&industry=all&prjType=D&noticeType=all&noticeClassify=all&args=&pageIndex=1   //中标公示  政府采购
 * http://www.gzsztb.gov.cn/api/trade/search?pubDate=all&region=5200&industry=all&prjType=Z&noticeType=all&noticeClassify=all&args=&pageIndex=1   //其他
 * Created by HuJia on 2018/2/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "GuiZhouggzyjyw")
public class GuiZhouggzyjyw extends BaseSnatch {


    @Test
    @Override
    public void run() throws Exception {

        String[] urls = {
                "http://www.gzsztb.gov.cn/api/trade/search?pubDate=all&region=5200&industry=all&prjType=A&noticeType=all&noticeClassify=all&args=&pageIndex=1",
                "http://www.gzsztb.gov.cn/api/trade/search?pubDate=all&region=5200&industry=all&prjType=D&noticeType=all&noticeClassify=all&args=&pageIndex=1",
                "http://www.gzsztb.gov.cn/api/trade/search?pubDate=all&region=5200&industry=all&prjType=A&noticeType=all&noticeClassify=all&args=&pageIndex=1",
                "http://www.gzsztb.gov.cn/api/trade/search?pubDate=all&region=5200&industry=all&prjType=D&noticeType=all&noticeClassify=all&args=&pageIndex=1",
                "http://www.gzsztb.gov.cn/api/trade/search?pubDate=all&region=5200&industry=all&prjType=Z&noticeType=all&noticeClassify=all&args=&pageIndex=1"
        };


        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            JSONObject jsonObject;
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].substring(0, urls[i].lastIndexOf("=") + 1) + pagelist;
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true).ignoreHttpErrors(true);
                    doc = conn.get();

                    if (pagelist == 1) {
                        jsonObject = new JSONObject(doc.text());
                        String pageCount = jsonObject.get("totalPage").toString();
                        page = Integer.parseInt(pageCount);
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page, LAST_ALLPAGE);
                        SnatchLogger.debug("总" + page + "页");
                    }

                    jsonObject = new JSONObject(doc.text());
                    String dateStr = jsonObject.get("data").toString();
                    JsonNode trs = Util.getContentByJson(dateStr);
                    for (int row = 0; row < trs.size(); row++) {
                        String href = "http://www.gzsztb.gov.cn/trade/bulletin/?id=" + trs.get(row).findPath("Id").toString();
                        String title = trs.get(row).findPath("Title").asText();
                        String date = trs.get(row).findPath("PubDate").asText();
                        String RegionName = trs.get(row).findPath("RegionName").asText();
                        String BTypeName = trs.get(row).findPath("BTypeName").asText();

                        Notice notice = new Notice();
                        notice.setProvince("贵州省");
                        notice.setProvinceCode("guizs");
                        notice.setAreaRank(PROVINCE);
                        notice.setSnatchNumber(snatchNumber);
                        if(i == 0 || i == 2) {
                            notice.setNoticeType("建设工程");
                        } else {
                            if(i == 4) {
                                if(title.contains("采购")) {
                                    notice.setNoticeType("政府采购");
                                } else {
                                    notice.setNoticeType("建设工程");
                                }
                            } else {
                                notice.setNoticeType("政府采购");
                            }
                        }
                        notice = SnatchUtils.setCatchTypeByTitle(notice, BTypeName);
                        if(StringUtils.isEmpty(notice.getCatchType())) {
                            notice.setCatchType(ZHAO_BIAO_TYPE);
                        }
                        notice.setSource("guiz");
                        notice.setTitle(title);
                        notice.setUrl(href);
                        notice.setOpendate(date);
                        CURRENT_PAGE_LAST_OPENDATE = date;
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
                e.printStackTrace();
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        String interfacedetailUrl = "http://www.gzsztb.gov.cn/api/trade/" + href.substring(href.indexOf("=")+1, href.length());
        Document detailDoc = Jsoup.parse(new URL(interfacedetailUrl).openStream(), "UTF-8", href);
        JSONObject jsonObject = new JSONObject(detailDoc.text());
        String centent = jsonObject.getString("Content");
        Document temp = Jsoup.parse(centent);
        String imgSrc = temp.select("img").attr("src");
        //包含图片
        if(!StringUtils.isEmpty(imgSrc)) {
            if(imgSrc.contains("image")) {
                imgSrc = "http://www.gzsztb.gov.cn/" + imgSrc;
                temp.select("img").attr("src", imgSrc);
                notice.setPhotoUrl(imgSrc);
            }
        }
        notice.setContent(temp.html());
        Thread.sleep(1000);
        return notice;
    }
}
