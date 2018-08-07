import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.utils.UrlUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/7/25.
 */
public class JiLingGGZYGov implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    @Override
    public void process(Page page) {
        // 拿到Html对象
        Html html = page.getHtml();
        // 拿到当前抓取的url
        String url = page.getUrl().get();
        // 获得页面优先级，默认0
        long priority = page.getRequest().getPriority();
        if(page.getUrl().regex("http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001001/[0-9]{1,5}.html").match()) {

            String pageStr = html.xpath("//*[@id=\"index\"]/text()").get();
            int pageCount = Integer.parseInt(pageStr.substring(pageStr.indexOf("/") + 1));
            for (int i = 2; i <= 5; i++) {
                page.addTargetRequest("http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001001/" + i + ".html");
            }

            List<String> detailUrls = html.xpath("//*[@id=\"jt\"]/ul/li/div/a/@href").all();
            for (String detailsUrl : detailUrls) {
                detailsUrl =  UrlUtils.canonicalizeUrl(detailsUrl, url);
                Request request = new Request(detailsUrl);
                // 设置优先级
//                request.setPriority(1);
                page.addTargetRequest(request);
            }
        } else {
            String title = html.xpath("//*[@class='ewb-article']/h3/text()").get();
            System.out.println(title);
        }
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    public static void main(String[] args) {
        String url = "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001001/1.html";
        Spider.create(new JiLingGGZYGov()).addUrl(url).thread(1).run();
    }
}
