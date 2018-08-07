package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
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

/**
 * Created by wangying on 2017/5/23/0023.
 * 浏阳市招标投标监管网   http://ztb.liuyang.gov.cn
 * 房政市政---招标公告      房政市政---答疑公告
 * 交通工程---招标公告      交通工程---答疑公告
 * 水利工程---招标公告      水利工程---答疑公告
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value="HuNanChangShaLiuYanglygov")
public class HuNanChangShaLiuYanglygov extends BaseSnatch {


    @Test
    public void run()throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception{
        int page = 1;
        String url = "";
         String[] urls = {"http://ztb.liuyang.gov.cn/zbj/jyxx/fjsz/zbgg21/920821cd-",
                "http://ztb.liuyang.gov.cn/zbj/jyxx/fjsz/cqdy/6560ab95-",
                "http://ztb.liuyang.gov.cn/zbj/jyxx/jtgc/zbgg67/f5a7ca3f-",
                "http://ztb.liuyang.gov.cn/zbj/jyxx/jtgc/dy6/1677e658-",
                "http://ztb.liuyang.gov.cn/zbj/jyxx/slgc/zbgg72/e2f6c5ca-",
                "http://ztb.liuyang.gov.cn/zbj/jyxx/slgc/dy23/12f80c99-"
        };
         String[] url1 = {"http://ztb.liuyang.gov.cn/eportal/ui?pageId=2008047&currentPage=",
                "http://ztb.liuyang.gov.cn/eportal/ui?pageId=2008056&currentPage=",
                "http://ztb.liuyang.gov.cn/eportal/ui?pageId=2008062&currentPage=",
                "http://ztb.liuyang.gov.cn/eportal/ui?pageId=2008071&currentPage="
        };
         String[] url2 ={"&moduleId=920821cd47184a34874107e527d34e61&staticRequest=yes",
                "&moduleId=6560ab95ba574b77baab27cbb33446e2&staticRequest=yes",
                "&moduleId=f5a7ca3f023242c9a13019f829a1c2f3&staticRequest=yes",
                "&moduleId=1677e658c7594e6ea36a20b4319aa7e1&staticRequest=yes"
        };
        try {
            for (int i=0;i<urls.length;i++){
                switch (i){
                    case 0:
                        SnatchLogger.debug("房政市政-----招标公告");
                        break;
                    case 1:
                        SnatchLogger.debug("房政市政-----答疑");
                        break;
                    case 2:
                        SnatchLogger.debug("交通工程-----招标公告");
                        break;
                    case 3:
                        SnatchLogger.debug("交通工程-----答疑");
                        break;
                    case 4:
                        SnatchLogger.debug("水利工程-----招标公告");
                        break;
                    case 5:
                        SnatchLogger.debug("水利工程-----答疑");
                        break;
                }
                url = urls[i]+"1.html";
                super.queryBeforeSnatchState(url);
                Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                Document doc = conn.get();
                String p = doc.select(".page_num").select("a").get(5).text();
                page = Integer.parseInt(p.substring(p.indexOf("/") + 1, p.length()));
                SnatchLogger.debug("总"+page+"页");
                page = computeTotalPage(page,LAST_ALLPAGE);
                //分页
                for (int j = 1; j <= page; j++) {
                    SnatchLogger.debug(j+"页");
                    String newUrl = "";
                    if (j < 4) {
                        newUrl = urls[i] + j + ".html";
                    } else {
                        if(i<4){
                            newUrl = url1[i] + j + url2[i];
                        }
                    }
                    conn = Jsoup.connect(newUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    Elements trs = doc.select(".right_neirong").select("li");
                    for (int k = 0; k < trs.size(); k++) {
                        String href = trs.get(k).select("a").attr("href");
                        String date = trs.get(k).select("span").text();
                        String title = trs.get(k).select("a").attr("title");
                        String conturl = "http://ztb.liuyang.gov.cn" + href;
                        String catchType = "招标公告";
                        Notice notice = new Notice();
                        notice.setProvince("湖南省");
                        notice.setProvinceCode("huns");
                        notice.setCity("长沙市");
                        notice.setCityCode("cs");
                        notice.setUrl(conturl);
                        notice.setCounty("浏阳市");
                        notice.setCountyCode("liuys");
                        switch (i){
                            case 0:
                                notice.setCatchType("1");
                                notice.setNoticeType("房政市政");
                                break;
                            case 1:
                                notice.setCatchType("0");
                                notice.setNoticeType("房政市政");
                                break;
                            case 2:
                                notice.setCatchType("1");
                                notice.setNoticeType("交通工程");
                                break;
                            case 3:
                                notice.setCatchType("0");
                                notice.setNoticeType("交通工程");
                                break;
                            case 4:
                                notice.setCatchType("1");
                                notice.setNoticeType("水利工程");
                                break;
                            case 5:
                                notice.setCatchType("0");
                                notice.setNoticeType("水利工程");
                                break;
                        }
                        notice.setTitle(title);
                        notice.setOpendate(date);
                        page =detailHandle(notice,j,page,k,trs.size());
                    }
                    if(i==page){
                        page = turnPageEstimate(page);
                    }
                }
                super.saveAllPageIncrement(url);
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            }
            SnatchLogger.error(e);
        }finally {
            super.clearClassParam();
        }
    }

    public Notice detail(String href, Notice notice, String catchType) throws Exception{
        Document contdoc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        String content = contdoc.select(".zhengwen").html();
        notice.setContent(content);
        Thread.sleep(1000*2);
        return notice;
    }
}

