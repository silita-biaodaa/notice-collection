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

import static com.snatch.common.SnatchContent.*;

/**
 * 湖南建设工程招投标网   http://www.hnztb.org
 * 招标信息--代理机构比选信息
 * http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0003&ItemCode=000005004&name=%E4%BB%A3%E7%90%86%E6%9C%BA%E6%9E%84%E6%AF%94%E9%80%89%E4%BF%A1%E6%81%AF
 * 招标信息--建设工程招标信息
 * http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0003&ItemCode=000005001&name=%E5%BB%BA%E8%AE%BE%E5%B7%A5%E7%A8%8B%E6%8B%9B%E6%A0%87%E4%BF%A1%E6%81%AF
 * 招标信息--招标答疑
 * http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0006&ItemCode=000007001&name=%u62db%u6807%u7b54%u7591
 * 中标信息--代理机构比选公示
 * http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0004&ItemCode=000009003&name=%u4ee3%u7406%u673a%u6784%u6bd4%u9009%u516c%u793a
 * 中标信息-建设工程中标公示
 * http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0004&ItemCode=000009002&name=%E5%BB%BA%E7%AD%91%E5%B7%A5%E7%A8%8B%E4%B8%AD%E6%A0%87%E5%85%AC%E7%A4%BA
 * 中标信息-代理机构比选结果
 * http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0004&ItemCode=000009004&name=%E4%BB%A3%E7%90%86%E6%9C%BA%E6%9E%84%E6%AF%94%E9%80%89%E7%BB%93%E6%9E%9C
 * 中标信息-建设工程中标结果
 * http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0004&ItemCode=000009001&name=%E5%BB%BA%E7%AD%91%E5%B7%A5%E7%A8%8B%E4%B8%AD%E6%A0%87%E7%BB%93%E6%9E%9C
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})
@Component
@JobHander(value="HuNanJianShegczb")
public class HuNanJianShegczb extends BaseSnatch{

    @Override
    @Test
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception{
        String[] urls = {
                "http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0003&ItemCode=000005004&name=%u4ee3%u7406%u673a%u6784%u6bd4%u9009%u4fe1%u606f",
                "http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0003&ItemCode=000005001&name=%u5efa%u8bbe%u5de5%u7a0b%u62db%u6807%u4fe1%u606f",
                "http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0006&ItemCode=000007001&name=%u62db%u6807%u7b54%u7591",
                "http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0004&ItemCode=000009003&name=%u4ee3%u7406%u673a%u6784%u6bd4%u9009%u516c%u793a",
                "http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0004&ItemCode=000009002&name=%u5efa%u7b51%u5de5%u7a0b%u4e2d%u6807%u516c%u793a",
                "http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0004&ItemCode=000009004&name=%u4ee3%u7406%u673a%u6784%u6bd4%u9009%u7ed3%u679c",
                "http://www.hnztb.org/Index.aspx?action=ucBiddingList&modelCode=0004&ItemCode=000009001&name=%E5%BB%BA%E7%AD%91%E5%B7%A5%E7%A8%8B%E4%B8%AD%E6%A0%87%E7%BB%93%E6%9E%9C"
        };
        String a1 = "";
        String a2 = "";
        String a3 = "";
        String a4 = "";
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
                    if(pageTemp>0 && page>pageTemp){
                        break;
                    }
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc= conn.get();
                    SnatchLogger.debug("第"+pagelist+"页");
                    if(pagelist > 1){
                        conn.data("__EVENTTARGET","ID_ucBiddingList$ucPager1$btnNext");
                        conn.data("__EVENTARGUMENT",a1);
                        conn.data("__VIEWSTATE",a2);
                        conn.data("__VIEWSTATEGENERATOR",a3);
                        conn.data("ID_ucBiddingList$txtTitle","");
                        conn.data("ID_ucBiddingList$ucPager1$listPage",a4);
                        conn.cookies(conn.response().cookies());
                        doc = conn.post();
                    }
                    if(page ==1){
                        //获取总页数
                        String pageCont = doc.select("#ID_ucBiddingList_ucPager1_lbPage").first().text().trim();
                        page = Integer.valueOf(pageCont.substring(pageCont.lastIndexOf("/") + 1,pageCont.length()));
                        pageTemp = page;
                        SnatchLogger.debug("总"+page+"页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }
                    a1 = doc.select("#__EVENTARGUMENT").val();
                    a2 = doc.select("#__VIEWSTATE").val();
                    a3 = doc.select("#__VIEWSTATEGENERATOR").val();
                    a4 = "" + pagelist;
                    Elements trs = doc.select("tr.trStyle");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");
                        if (SnatchUtils.isNotNull(conturl)) {
                            Notice notice = new Notice();
                            String title = trs.get(row).select("a").first().attr("title").trim();
                            String date = trs.get(row).select("span").first().text().trim().replace("(","").replace(")","");
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCatchType(PROVINCE);
                            notice.setSnatchNumber(snatchNumber);
                            notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                            switch (i) {
                                case 0 :
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(DL_ZHAO_BIAO_TYPE);
                                    }
                                    notice.setNoticeType("招标代理");
                                    break;
                                case 1 :
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(ZHAO_BIAO_TYPE);
                                    }
                                    notice.setNoticeType("工程建设");
                                    break;
                                case 2 :
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(OTHER_TYPE);
                                    }
                                    notice.setNoticeType("答疑公告");
                                    break;
                                case 3 :
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(DL_ZHONG_BIAO_TYPE);
                                    }
                                    notice.setNoticeType("招标代理");
                                    break;
                                case 4 :
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(ZHONG_BIAO_TYPE);
                                    }
                                    notice.setNoticeType("工程建设");
                                    break;
                                case 5 :
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(DL_ZHONG_BIAO_TYPE);
                                    }
                                    notice.setNoticeType("招标代理");
                                    break;
                                case 6 :
                                    if (SnatchUtils.isNull(notice.getCatchType())) {
                                        notice.setCatchType(ZHONG_BIAO_TYPE);
                                    }
                                    notice.setNoticeType("工程建设");
                                    break;
                            }

                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            page = detailHandle(notice,pagelist,page,row,trs.size());
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
        Document detailDoc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        String content = detailDoc.select("#ID_ucNewsView_Tr1").first().nextElementSibling().nextElementSibling().html();
        notice.setContent(content);
        return notice;
    }
}