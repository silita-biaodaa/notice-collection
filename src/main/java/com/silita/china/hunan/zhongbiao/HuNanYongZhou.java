package com.silita.china.hunan.zhongbiao;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.ChineseCompressUtil;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.snatch.model.SnatchUrl;
import com.xxl.job.core.handler.annotation.JobHander;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.snatch.common.SnatchContent.CITY;
import static com.snatch.common.SnatchContent.GENG_ZHENG_TYPE;

/**
 * Created by hujia on 2017/7/19.
 * 永州市  永州市公共资源交易中心  http://ggzy.yzcity.gov.cn/yzweb/
 * 各分类中的区县信息公告没有抓取，最新公告是15年末的，且只有几条
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanYongZhou")
public class HuNanYongZhou extends BaseSnatch{


    /**
     * 最大重试次数
     */
    private int maxRetryTimes = 5;

    /**
     * 重试休息间隙 ms
     */
    private int retrySleepMillis = 1000;

    /**
     * 内容链接为文件下载链接 内容替换
     */
    private String fileContentBase = "<a href=\"#url\">文件下载</a><script> window.location='#url';</script>";

    @Test
    @Rollback(false)
    public void run()throws Exception{
        //获取需要抓取的类别列表
        List<SnatchUrl> urlList = buildSnatchList();
        for(SnatchUrl snatchUrl: urlList) {
            getUrlList(snatchUrl);
        }
    }

    private List<SnatchUrl> buildSnatchList() throws Exception{
        String urls[] = new String[]{
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004001/004001004/004001004001/",      //工程交易 >> 中标公示 >> 施工类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004001/004001004/004001004002/",      //工程交易 >> 中标公示 >> 监理类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004001/004001004/004001004003/",      //工程交易 >> 中标公示 >> 勘察设计类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004001/004001001/004001001001/",      //工程交易 >> 招标公告 >> 施工类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004001/004001001/004001001002/",      //工程交易 >> 招标公告 >> 监理类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004001/004001001/004001001003/",      //工程交易 >> 招标公告 >> 勘察设计类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004001/004001002/004001002001/",      //工程交易 >> 变更答疑 >> 施工类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004001/004001002/004001002002/",      //工程交易 >> 变更答疑 >> 监理类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004001/004001002/004001002003/",      //工程交易 >> 变更答疑 >> 勘察设计类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004002/004002001/004002001001/",      //政府采购 >> 采购公告 >> 工程类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004002/004002001/004002001002/",      //政府采购 >> 采购公告 >> 货物类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004002/004002001/004002001003/",      //政府采购 >> 采购公告 >> 服务类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004002/004002002/004002002001/",      //政府采购 >> 变更答疑 >> 工程类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004002/004002002/004002002002/",      //政府采购 >> 变更答疑 >> 货物类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004002/004002002/004002002003/",      //政府采购 >> 变更答疑 >> 服务类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004002/004002004/004002004001/",      //政府采购 >> 中标公示 >> 工程类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004002/004002004/004002004002/",      //政府采购 >> 中标公示 >> 货物类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004002/004002004/004002004003/",      //政府采购 >> 中标公示 >> 服务类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004005/004005001/004005001001/",      //中介遴选 >> 招标公告 >> 工程类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004005/004005001/004005001002/",      //中介遴选 >> 招标公告 >> 采购类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004005/004005001/004005001003/",      //中介遴选 >> 招标公告 >> 服务类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004005/004005003/004005003001/",      //中介遴选 >> 变更答疑 >> 工程类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004005/004005003/004005003002/",      //中介遴选 >> 变更答疑 >> 采购类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004005/004005002/004005002001/",      //中介遴选 >> 中标公示 >> 工程类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004005/004005002/004005002002/",      //中介遴选 >> 中标公示 >> 采购类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004005/004005002/004005002003/",      //中介遴选 >> 中标公示 >> 服务类
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004007/004007001/",                      //简易交易 >> 交易公告
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004007/004007002/",                       //简单交易>变更答疑
                "http://ggzy.yzcity.gov.cn/yzweb/jyxx/004007/004007003/"                        //简单交易>成交公示
        };
        String[] types = new String[]{"工程交易","工程交易","工程交易","工程交易","工程交易","工程交易","工程交易","工程交易","工程交易",
                                        "政府采购","政府采购","政府采购","政府采购","政府采购","政府采购","政府采购","政府采购","政府采购",
                                        "中介遴选","中介遴选","中介遴选","中介遴选","中介遴选","中介遴选","中介遴选","中介遴选",
                                        "简易交易","简易交易","简易交易"};
        String[] catchTypes = new String[]{"2","2","2","1","1","1",GENG_ZHENG_TYPE,GENG_ZHENG_TYPE,GENG_ZHENG_TYPE,
                                            "1","1","1",GENG_ZHENG_TYPE,GENG_ZHENG_TYPE,GENG_ZHENG_TYPE,"2","2","2",
                                            "1","1","1",GENG_ZHENG_TYPE,GENG_ZHENG_TYPE,"2","2","2",
                                            "1",GENG_ZHENG_TYPE,"2"};
        List<SnatchUrl> urlList =new ArrayList<SnatchUrl>(urls.length);
        for(int i=0;i<urls.length;i++){
            SnatchUrl snatchUrl1 = new SnatchUrl();
            snatchUrl1.setUrl(urls[i]);
            snatchUrl1.setNoticeType(types[i]);
            snatchUrl1.setNotcieCatchType(catchTypes[i]);
            snatchUrl1.setSnatchNumber(SnatchUtils.makeSnatchNumber());
            urlList.add(snatchUrl1);
        }
        return urlList;
    }

    private void snatchListHandle(String url,String noticeType,String noticeCatchType,String snatchNumber)throws Exception{
        Connection conn = null;
        Document doc = null;
        String dyUrl = url;
        int page = 1;
        int pageBackup = 0;
        for (int i = 1; i <= page; i++) {
            if(pageBackup>0 && page>pageBackup){
                CURRENT_PAGE = pageBackup;
                break;
            }
            if(i>1){
                dyUrl = url+"?Paging="+i;
            }
            SnatchLogger.info("=========第"+i+"页");
            doc = doConnect(dyUrl);
            if(page==1){
                Element pagination = doc.select("#Paging").select("font").get(1);
                String html=pagination.select("b").text();
                page = Integer.parseInt(html.trim());
                SnatchLogger.info("总共" + page + "页");
                page = computeTotalPage(page, LAST_ALLPAGE);
            }
            Elements trs=doc.select(".moreinfo").select("tr[height=21]");
            if(trs.size()<=0){
                SnatchLogger.info(doc.html());
                SnatchLogger.info("页面无数据 tr.size()-->"+trs.size());
                isUpdateIncrement=false;
            }
            for (int j = 0; j < trs.size(); j++) {
                String conturl = trs.get(j).select("a").attr("href");
                String title = trs.get(j).select("a").attr("title");
                if(SnatchUtils.isNotNull(conturl)){
                    conturl = "http://ggzy.yzcity.gov.cn" + conturl;
                    String date = trs.get(j).select("td").get(3).text();
                    //===入库代码====
                    Notice notice = new Notice();
                    notice.setProvince("湖南省");
                    notice.setProvinceCode("huns");
                    notice.setCity("永州市");
                    notice.setCityCode("yz");
                    notice.setAreaRank(CITY);
                    notice.setSnatchNumber(snatchNumber);
                    notice.setNoticeType(noticeType);

                    notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                    if (SnatchUtils.isNull(notice.getCatchType())) {
                        notice.setCatchType(noticeCatchType);
                    } else {
                        SnatchUtils.judgeCatchType(notice,notice.getCatchType(),noticeCatchType);
                    }

                    notice.setUrl(conturl);
                    notice.setOpendate(date);
                    notice.setTitle(title);
                    notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                    //详情信息入库，获取增量页数
                    if(noticeType.contains("采购"))
                        page = govDetailHandle(notice,i,page,j,trs.size());
                    else
                        page =detailHandle(notice,i,page,j,trs.size());
                }
            }
//            Thread.sleep(1500);
            //===入库代码====
            if(i==page){
                page = turnPageEstimate(page);
            }
        }
    }

    /**
     * 公告详情内容获取
     * @param href
     * @param notice
     * @param catchType
     * @return
     * @throws Exception
     */
    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contdoc = Jsoup.parse(new URL(href).openStream(),"gbk",href);
        if(StringUtils.isBlank(contdoc.text())){
            notice.setContent(fileContentBase.replaceAll("#url",href));
            return notice;
        }
        Elements tables = contdoc.select("table");
        Element contentEle = contdoc.select("#tblInfo").first();
        contentEle.select("tr").first().remove();
        String content=contentEle.html();
        if (content.contains("<img")) {
            content = content.replaceAll("src=\"","src=\"http://ggzy.yzcity.gov.cn");
        }else if (tables.size() == 0){
            ChineseCompressUtil util = new ChineseCompressUtil();
            content = util.getPlainText(content);
        }
        content = content.replace("width: 935px;","width: 100%;");
        notice.setContent(content);
        return notice;
    }

    private void getUrlList(SnatchUrl snatchUrl)throws Exception{
        String url = snatchUrl.getUrl();
        String noticeType = snatchUrl.getNoticeType();
        String noticeCatchType = snatchUrl.getNotcieCatchType();
        String snatchNumber = snatchUrl.getSnatchNumber();
        //===入库代码====
        super.queryBeforeSnatchState(url); // 查询url前次抓取的情况（最大页数与公示时间）
        try {
            //处理列表请求
            snatchListHandle(url,noticeType,noticeCatchType,snatchNumber);
            //===入库代码====
            super.saveAllPageIncrement(url);
        } catch (IOException e) {
            SnatchLogger.error(e);
        }
    }

    /**
     * 统一获取网页内容
     */
    private Document doConnect(String url) {
        int retryTimes = 0;
        do {
            try {
                Connection conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60);
                        //.ignoreHttpErrors(true);
                Document doc = conn.get();
                if(doc.select("a").first() == null)
                    throw new IOException();
                return doc;
            } catch (Exception Exc) {
                int sleepMillis = this.retrySleepMillis * (1 << retryTimes);
                if ((retryTimes + 1) <= this.maxRetryTimes) {
                    try {
                        SnatchLogger.info("系统繁忙，" + sleepMillis + "ms 后重试(第" + (retryTimes + 1) + "次)");
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException interExc) {
                        throw new RuntimeException(interExc);
                    }
                }
            }
        } while (++retryTimes <= this.maxRetryTimes);
        SnatchLogger.error("网页内容获取异常，url:"+url,new RuntimeException());
        throw new RuntimeException();
    }
}
