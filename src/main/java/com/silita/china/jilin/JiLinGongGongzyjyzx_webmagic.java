package com.silita.china.jilin;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 吉林公共资源交易中心
 * http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001001/1.html    交易信息 > 工程建设
 * http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001002/1.html    工程建设 > 变更公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001003/1.html    工程建设 > 预中标公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001004/1.html    工程建设 > 合同公示
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002001/1.html    政采集中 > 招标公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002002/1.html    政采集中 > 变更公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002004/1.html    政采集中 > 中标结果公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002005/1.html    政采集中 > 合同公示
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002007/1.html    政采集中 > 废标公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002006/1.html    政采集中 > 废标公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005001/1.html    政采非集中 > 招标公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005002/1.html    政采非集中 > 变更公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005004/1.html    政采非集中 > 中标结果公告
 * http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005005/1.html    政采非集中 > 合同公示
 * http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005006/1.html    政采非集中 > 废标公告
 * Created by 91567 on 2018/3/5.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component("JiLinGongGongzyjyzx_webmagic")
@JobHander(value = "JiLinGongGongzyjyzx_webmagic")
public class JiLinGongGongzyjyzx_webmagic extends BaseSnatch implements PageProcessor {

    private static final String source = "jil";
    final int thread_num = 1;
    final CountDownLatch latch = new CountDownLatch(thread_num);

    @Test
    public void run() throws Exception {
        String[] urls = {
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001001/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001002/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001003/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001004/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002001/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002002/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002004/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002005/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002007/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002006/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005001/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005002/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005004/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005005/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005006/1.html"
        };
        Spider.create(new JiLinGongGongzyjyzx_webmagic()).addUrl(urls).thread(thread_num).start();
        latch.await();
    }

    @Override
    public void process(Page page) {
        Html html = page.getHtml();
        Document doc = html.getDocument();
        String url = page.getUrl().get();
        long priority = page.getRequest().getPriority();

        int pageCount = 1;
        int pageTemp = 0;
        try {
            if (priority == 0) {
                //获取全部详情列表url
                List<String> trs = html.xpath("//ul[@class='wb-data-item']/li//a/@href").all();
                page.addTargetRequests(trs, 1);
                if(pageCount == 1) {
                    super.queryBeforeSnatchState(url);
                }

                //获取总页数
                String pageStr = html.xpath("//span[@id='index']/allText()").regex("\\d+/(\\d+)").get();
                pageCount = Integer.valueOf(pageStr);
                SnatchLogger.debug("总" + page + "页");
                pageCount = computeTotalPage(pageCount, LAST_ALLPAGE);

                for (int pagelist = 2; pagelist <= pageCount; pagelist++) {
                    if (pageTemp > 0 && pagelist > pageTemp) {
                        break;
                    }
                    url = url.substring(0, url.lastIndexOf("/") + 1) + pagelist + ".html";
                    page.addTargetRequest(url);

//                    List<String> trs = html.xpath("//ul[@class='wb-data-item']/li//a/@href").all();
//                    for (int row = 1; row <= trs.size(); row++) {
//                        Notice notice = new Notice();
//                        String title = html.xpath("//*[@id='jt']/ul/li[" + row +"]/div/a/@title").get();
//                        System.out.println(title);
//                        String detailUrl = page.getUrl().get();
//                        String publishDate = "";
//                        notice.setProvince("吉林省");
//                        notice.setProvinceCode("jls");
//                        notice.setCatchType(ZHAO_BIAO_TYPE);
//                        notice.setAreaRank(PROVINCE);
//                        notice.setSource(source);
//
////                        if ("政府采购".equals(notice.getNoticeType())) {
////                            pageCount = govDetailHandle(notice, pagelist, pageCount, row, trs.size());
////                        } else {
////                            pageCount = detailHandle(notice, pagelist, pageCount, row, trs.size());
////                        }
//                    }
                    //===入库代码====
//                    super.saveAllPageIncrement(url);
                }
            } else if (priority == 1) {
                String title = html.xpath("//div[@class='ewb-article']/h3/allText()").get();
                System.out.println(title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            super.clearClassParam();
        }
        latch.countDown();
    }

    @Override
    public Site getSite() {
        return Site.me().setRetryTimes(10).setCycleRetryTimes(10).setSleepTime(100).setRetrySleepTime(500).setTimeOut(180000)
                .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Core/1.53.4549.400 QQBrowser/9.7.12900.400")
                .addHeader("Connection", "close")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8");
    }

    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        return notice;
    }
}
