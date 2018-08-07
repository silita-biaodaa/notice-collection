package com.silita.china.hunan;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;


/**  http://www.hnsggzy.com/web/shaoyang/listdsview.htm?code=shaoyang_jygkszgcjs
 *  邵阳公共资源交易中心（只抓取招标）
 *
 * @author gmy
 *
 *
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring-test.xml" })


@Component
@JobHander(value="HuNanShaoYangsyggzy")
public class HuNanShaoYangsyggzy  extends BaseSnatch {


    @Test
    public void run()throws Exception{
        firstGetMaxId();
    }

    private void firstGetMaxId()throws Exception {
        int page=1;
        HtmlPage hPage = null;
        Document doc;
        String url="http://www.hnsggzy.com/web/shaoyang/listdsview.htm?code=shaoyang_jygkszgcjs";
        WebClient webClient=new WebClient(BrowserVersion.CHROME); // 实例化Web客户端
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setTimeout(55000);
        try {
            hPage=webClient.getPage(url); // 解析第一页
            webClient.waitForBackgroundJavaScript(6000);
        } catch (IOException e) {
            SnatchLogger.error(e);
            XxlJobLogger.log("访问网页出错了");
        }finally {
            webClient.close();
        }

        //===入库代码====
        super.queryBeforeSnatchState(url);//查询url前次抓取的情况（最大页数与公示时间）

        try {
            for(int j=1;j<=page;j++){
                if (j>0 && j%10==0){
                    XxlJobLogger.log("10的整数页无法抓取。");
                    continue;
                }
                if(j == 1) {
                    doc = Jsoup.parse(hPage.asXml());
                    String textStr = doc.select("#pager").select(".pages").select(".thpoint").text();
                    String countPage=textStr.substring(textStr.indexOf(":")+1, textStr.lastIndexOf(" 当前")-2).trim();
                    page=Integer.parseInt(countPage);
                    XxlJobLogger.log("总"+page+"页");
                    //===入库代码====
                    page = computeTotalPage(page,LAST_ALLPAGE);
                } else {
                    HtmlInput pageNum = hPage.getHtmlElementById("gotoval");    //得到页码输入框
                    HtmlElement goBtn = (HtmlElement)hPage.getHtmlElementById("gotoval").getParentNode().getNextSibling();  //得到按钮
                    pageNum.setValueAttribute(String.valueOf(j)); //翻页
                    HtmlPage retPage = goBtn.click();
                    doc = Jsoup.parse(retPage.asXml());
                }
                XxlJobLogger.log("第"+j+"页");
                Element table = doc.select("#newList").get(0);
                Elements trs = table.select("li");
                for(int i=0;i<(trs.size());i++){
                    String href = "http://www.hnsggzy.com/web/shaoyang/" + trs.get(i).select("a").attr("href");
                    XxlJobLogger.log("次数："+i+"\turl:"+href);
                    SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
                    String date = sdf.format(sdf.parse(trs.get(i).select("span").text()));
                    //===入库代码====
                    Notice notice = new Notice();
                    notice.setProvince("湖南省");
                    notice.setProvinceCode("huns");
                    notice.setCity("邵阳");
                    notice.setCityCode("sy");
                    notice.setCatchType("1");
                    notice.setUrl(href);
                    notice.setOpendate(date);
                    XxlJobLogger.log(href);
                    page = detailHandle(notice,j,page,i,trs.size());
                }
                //===入库代码====
                if(j==page ){
                    page = turnPageEstimate(page);
                }
            }
            //===入库代码====
            super.saveAllPageIncrement(url);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            }
            SnatchLogger.error(e);
        }
    }

    public Notice detail(String href, Notice notice, String catchType) throws Exception{
        Document docCount = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        String content=docCount.select(".context").html();
        String title=docCount.select("h2").text();
        notice.setTitle(title);
        notice.setContent(content);
        XxlJobLogger.log(title);
        return notice;
    }


}
