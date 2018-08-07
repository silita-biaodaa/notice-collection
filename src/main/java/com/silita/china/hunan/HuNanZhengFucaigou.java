package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
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

import static com.snatch.common.SnatchContent.*;

/**
 * 湖南省政府采购网 http://www.ccgp-hunan.gov.cn
 * 采购公告
 * http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=prcmNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18
 * 中标公告
 * http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=dealNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18
 * 废标公告
 * http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=invalidNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18
 * 合同公告
 * http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=contractNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18
 * 更正公告
 * http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=modfiyNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18
 * 终止公告
 * http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=endNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18
 * 其他公告
 * http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=otherNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18
 * Created by maofeng on 2017/12/8.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})
@Component
@JobHander(value="HuNanZhengFucaigou")
public class HuNanZhengFucaigou extends BaseSnatch {

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask () throws Exception {
        String[] urls = {
                "http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=prcmNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18",
                "http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=dealNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18",
                "http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=invalidNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18",
                "http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=contractNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18",
                "http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=modfiyNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18",
                "http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=endNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18",
                "http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do?nType=otherNotices&pType=&prcmPrjName=&prcmItemCode=&prcmOrgName=&startDate=&endDate=&prcmPlanNo=&page=1&pageSize=18"
        };
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            Connection conn = null;
            Document doc = null;
            String url = urls[i];
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if(pageTemp>0 && page>pageTemp){
                        break;
                    }
                    if(pagelist>1){
                        url = urls[i].replace("page=1","page=" + pagelist);
                    }
                    SnatchLogger.debug("第"+pagelist+"页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true).ignoreContentType(true);
                    doc= conn.get();
                    if(page ==1){
                        //获取总页数
                        JsonNode lis= Util.getContentByJson(doc.text());
                        int count = Integer.valueOf(lis.findPath("total").asText());
                        if (count % 18 == 0) {
                            page = count / 18;
                        } else {
                            page = (count / 18) + 1;
                        }
                        pageTemp = page;
                        SnatchLogger.debug("总"+page+"页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }
                    JsonNode trs = Util.getContentByJson(doc.text()).findPath("rows");
                    for (int row = 0; row < trs.size(); row++) {
                        String contId = trs.get(row).findPath("NOTICE_ID").asText().trim();
                        if (SnatchUtils.isNotNull(contId)) {
                            Notice notice = new Notice();
                            String title = trs.get(row).findPath("NOTICE_TITLE").asText().trim();
                            String date = trs.get(row).findPath("NEWWORK_DATE").asText().trim();
                            String conturl = "http://www.ccgp-hunan.gov.cn/mvc/viewNoticeContent.do?noticeId=" + contId + "&area_id=";

                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");

                            switch (i) {
                                case 0:
                                    notice.setCatchType(ZHAO_BIAO_TYPE);
                                    break;
                                case 1:
                                    notice.setCatchType(ZHONG_BIAO_TYPE);
                                    break;
                                case 2:
                                    notice.setCatchType(FEI_BIAO_TYPE);
                                    break;
                                case 3:
                                    notice.setCatchType(ZHAO_BIAO_TYPE);
                                    break;
                                case 4:
                                    notice.setCatchType(GENG_ZHENG_TYPE);
                                    break;
                                case 5:
                                    notice.setCatchType(OTHER_TYPE);
                                    break;
                                case 6:
                                    notice.setCatchType(OTHER_TYPE);
                                    break;
                            }


                            notice.setNoticeType("政府采购");
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            page = govDetailHandle(notice,pagelist,page,row,trs.size());
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
        String content = detailDoc.select("body").first().html();
        notice.setContent(content);
        return notice;
    }
}
