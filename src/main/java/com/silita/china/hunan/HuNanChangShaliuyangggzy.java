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

import static com.snatch.common.SnatchContent.CHENG_QING_TYPE;
import static com.snatch.common.SnatchContent.CITY;

/**
 * 长沙市浏阳市公共资源交易中心   http://www.liuyang.gov.cn/ggzyjy/2007270/index.html
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/fjsz/zbgg21/index.html 房建市政--招标公告
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/fjsz/zbgs/index.html   房建市政--中标公告
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/fjsz/cqdy/index.html   房建市政--澄清答疑
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/jtgc/zbgg67/index.html 交通工程--招标公告
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/jtgc/zbgs46/index.html 交通工程--中标公告
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/jtgc/dy6/index.html    交通工程--澄清答疑
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/slgc/zbgg72/index.html 水利工程--招标公告
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/slgc/zbgs30/index.html 水利工程--中标公告
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/slgc/dy23/index.html   水利工程--澄清答疑
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/zfcg26/zbgg32/index.html   政府采购--招标公告
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/zfcg26/zbgs85/index.html   政府采购--中标公告
 * http://www.liuyang.gov.cn/ggzyjy/jyxx/zfcg26/dy69/index.html     政府采购--澄清答疑
 * Created by maofeng on 2017/12/8.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})
@Component
@JobHander(value = "HuNanChangShaliuyangggzy")
public class HuNanChangShaliuyangggzy extends BaseSnatch {

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask () throws Exception {
        String[] urls = {
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008047&currentPage=1&moduleId=920821cd47184a34874107e527d34e61&staticRequest=yes",
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008053&currentPage=1&moduleId=a1d0e74b268a4738b1863a5077c83baa&staticRequest=yes",
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008056&currentPage=1&moduleId=6560ab95ba574b77baab27cbb33446e2&staticRequest=yes",
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008062&currentPage=1&moduleId=f5a7ca3f023242c9a13019f829a1c2f3&staticRequest=yes",
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008068&currentPage=1&moduleId=8171bbe12863448aad298c07f03e7420&staticRequest=yes",
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008071&currentPage=1&moduleId=1677e658c7594e6ea36a20b4319aa7e1&staticRequest=yes",
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008077&currentPage=1&moduleId=e2f6c5ca3a6d40ec8fc9864e54216100&staticRequest=yes",
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008083&currentPage=1&moduleId=7a1e05ce1f5e43559c70838f95754a31&staticRequest=yes",
                "http://www.liuyang.gov.cn/ggzyjy/jyxx/slgc/dy23/12f80c99-1.html",
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008092&currentPage=1&moduleId=25c6215ac754439d8d2b9665b4727dc1&staticRequest=yes",
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008098&currentPage=1&moduleId=08e080270d12404b896aa9cee095b20a&staticRequest=yes",
                "http://www.liuyang.gov.cn/eportal/ui?pageId=2008101&currentPage=1&moduleId=e50c3ebcfd0743f68358f1953e4bba0b&staticRequest=yes"
        };
        String[] catchTypes = {"1","2",CHENG_QING_TYPE,"1","2",CHENG_QING_TYPE,"1","2",CHENG_QING_TYPE,"1","2",CHENG_QING_TYPE};
        String[] noticeTypes = {"房建市政","房建市政","房建市政","交通工程","交通工程","交通工程",
                                "水利工程","水利工程","水利工程","政府采购","政府采购","政府采购"};
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
                    if(pagelist>1){
                        if (i == 8) {
                            url = urls[i].replace("-1.html","-" + pagelist + ".html");
                        } else {
                            url = urls[i].replace("currentPage=1","currentPage=" + pagelist);
                        }
                    }
                    SnatchLogger.debug("第"+pagelist+"页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc= conn.get();
                    if(page ==1){
                        //获取总页数
                        String pageCont = doc.select(".page_num").first().select("a[style=cursor:pointer]").last().attr("onclick");
                        try {
                            page = Integer.valueOf(pageCont.substring(pageCont.indexOf("currentPage=") + 12,pageCont.indexOf("&moduleId")));
                        } catch (IndexOutOfBoundsException e) {
                            page = Integer.valueOf(pageCont.substring(pageCont.lastIndexOf("-") + 1,pageCont.lastIndexOf(".html")));
                        }
                        pageTemp = page;
                        SnatchLogger.debug("总"+page+"页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".right_neirong").last().select("ul").first().select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");
                        if (SnatchUtils.isNotNull(conturl)) {
                            Notice notice = new Notice();
                            String title = trs.get(row).select("a").first().attr("title");
                            String date = trs.get(row).select("span").last().text().trim();

                            notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                            if (SnatchUtils.isNull(notice.getCatchType())) {
                                notice.setCatchType(catchTypes[i]);
                            }else {
                                SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchTypes[i]);
                            }

                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("长沙市");
                            notice.setCityCode("cs");
                            notice.setCounty("浏阳市");
                            notice.setCountyCode("liuys");
                            notice.setNoticeType(noticeTypes[i]);
                            notice.setUrl(conturl);
                            notice.setAreaRank(CITY);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            if (notice.getNoticeType().contains("采购")) {
                                page = govDetailHandle(notice,pagelist,page,row,trs.size());
                            } else {
                                page = detailHandle(notice,pagelist,page,row,trs.size());
                            }
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
        String content = detailDoc.select("div.zhengwen").first().html();
        notice.setContent(content);
        return notice;
    }
}
