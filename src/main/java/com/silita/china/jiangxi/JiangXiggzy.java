package com.silita.china.jiangxi;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
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
 * 江西公共资源交易网    http://www.jxsggzy.cn
 * http://www.jxsggzy.cn/web/jyxx/002001/002001001/jyxx.html    房建及市政工程--招标公告
 * http://www.jxsggzy.cn/web/jyxx/002001/002001002/jyxx.html    房建及市政工程--答疑澄清
 * http://www.jxsggzy.cn/web/jyxx/002001/002001004/jyxx.html    房建及市政工程--中标公告
 * http://www.jxsggzy.cn/web/jyxx/002002/002002002/jyxx.html    交通工程--招标公告
 * http://www.jxsggzy.cn/web/jyxx/002002/002002003/jyxx.html    交通工程--补遗书
 * http://www.jxsggzy.cn/web/jyxx/002002/002002005/jyxx.html    交通工程--中标公告
 * http://www.jxsggzy.cn/web/jyxx/002003/002003001/jyxx.html    水利工程--资格预审公告/招标公告
 * http://www.jxsggzy.cn/web/jyxx/002003/002003002/jyxx.html    水利工程--澄清补遗
 * http://www.jxsggzy.cn/web/jyxx/002003/002003004/jyxx.html    水利工程--中标候选人公示
 * http://www.jxsggzy.cn/web/jyxx/002005/002005001/jyxx.html    重点工程--招标公告
 * http://www.jxsggzy.cn/web/jyxx/002005/002005002/jyxx.html    重点工程--答疑澄清
 * http://www.jxsggzy.cn/web/jyxx/002005/002005004/jyxx.html    重点工程--结果公示
 * http://www.jxsggzy.cn/web/jyxx/002006/002006001/jyxx.html    政府采购--采购公告
 * http://www.jxsggzy.cn/web/jyxx/002006/002006002/jyxx.html    政府采购--变更公告
 * http://www.jxsggzy.cn/web/jyxx/002006/002006004/jyxx.html    政府采购--结果公示
 * http://www.jxsggzy.cn/web/jyxx/002006/002006005/jyxx.html    政府采购--单一来源公示
 * Created by maofeng on 2018/3/1.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "JiangXiggzy")
public class JiangXiggzy extends BaseSnatch {


    private static final String source = "jiangx";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }


    public void snatchTask () throws Exception{
        String[] urls = {
                "http://www.jxsggzy.cn/web/jyxx/002001/002001001/jyxx.html",     //房建及市政工程--招标公告
                "http://www.jxsggzy.cn/web/jyxx/002001/002001002/jyxx.html",    //房建及市政工程--答疑澄清
                "http://www.jxsggzy.cn/web/jyxx/002001/002001004/jyxx.html",    //房建及市政工程--中标公告
                "http://www.jxsggzy.cn/web/jyxx/002002/002002002/jyxx.html",    //交通工程--招标公告
                "http://www.jxsggzy.cn/web/jyxx/002002/002002003/jyxx.html",    //交通工程--补遗书
                "http://www.jxsggzy.cn/web/jyxx/002002/002002005/jyxx.html",    //交通工程--中标公告
                "http://www.jxsggzy.cn/web/jyxx/002003/002003001/jyxx.html",    //水利工程--资格预审公告/招标公告
                "http://www.jxsggzy.cn/web/jyxx/002003/002003002/jyxx.html",    //水利工程--澄清补遗
                "http://www.jxsggzy.cn/web/jyxx/002003/002003004/jyxx.html",    //水利工程--中标候选人公示
                "http://www.jxsggzy.cn/web/jyxx/002005/002005001/jyxx.html",    //重点工程--招标公告
                "http://www.jxsggzy.cn/web/jyxx/002005/002005002/jyxx.html",    //重点工程--答疑澄清
                "http://www.jxsggzy.cn/web/jyxx/002005/002005004/jyxx.html",    //重点工程--结果公示
                "http://www.jxsggzy.cn/web/jyxx/002006/002006001/jyxx.html",    //政府采购--采购公告
                "http://www.jxsggzy.cn/web/jyxx/002006/002006002/jyxx.html",    //政府采购--变更公告
                "http://www.jxsggzy.cn/web/jyxx/002006/002006004/jyxx.html",    //政府采购--结果公示
                "http://www.jxsggzy.cn/web/jyxx/002006/002006005/jyxx.html",    //政府采购--单一来源公示
        };
        String[] catchTypes = {"1",DA_YI_TYPE,"2","1",BU_CHONG_TYPE,"2","1",CHENG_QING_TYPE,"2","1",DA_YI_TYPE,"2","1",GENG_ZHENG_TYPE,"2","0"};
        String[] noticeTypes = {"房建市政","房建市政","房建市政","交通工程","交通工程","交通工程","水利工程","水利工程","水利工程",
                "重点工程","重点工程","重点工程","政府采购","政府采购","政府采购","政府采购"};
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
                    // 翻页url
                    if(pagelist > 1){
                        url = urls[i].replace("jyxx.html",pagelist + ".html");
                    }
                    SnatchLogger.debug("第"+pagelist+"页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc= conn.get();
                    if(page ==1){
                        //获取总页数
                        String pageCont = doc.select("#index").first().text();
                        page = Integer.valueOf(pageCont.substring(pageCont.indexOf("/") +1));
                        SnatchLogger.debug("总"+page+"页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".ewb-list-node");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");
                        if (SnatchUtils.isNotNull(conturl)) {
                            Notice notice = new Notice();
                            String title = trs.get(row).select("a").first().text().trim();
                            String date = trs.get(row).select(".ewb-list-date").first().text().trim();
                            notice.setProvince("江西省");
                            notice.setProvinceCode("jiangxs");
                            notice.setCatchType(catchTypes[i]);
                            notice.setNoticeType(noticeTypes[i]);
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setAreaRank(PROVINCE);
                            notice.setSource(source);

                            // 地区维度
                            if (title.charAt(0) == '[' && title.contains("]")) {
                                String projDq = title.substring(title.indexOf("[")+1,title.indexOf("]"));
                                if (!projDq.contains("省")) {
                                    Dimension dm = new Dimension();
                                    dm.setProjDq(projDq);
                                    notice.setDimension(dm);
                                }
                            }

                            if (notice.getNoticeType().contains("采购")) {
                                if (title.contains("关于")) {
                                    notice.setTitle(title.substring(title.indexOf("关于")+2));
                                }
                                page = govDetailHandle(notice, pagelist, page, row, trs.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, trs.size());
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
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = docCount.select(".con").first().html();
        notice = SnatchUtils.setCatchTypeByTitle(notice,notice.getTitle());
        if (SnatchUtils.isNull(notice.getCatchType())) {
            notice.setCatchType(catchType);
        } else {
            SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchType);
        }
        notice.setContent(content);
        return notice;
    }
}
