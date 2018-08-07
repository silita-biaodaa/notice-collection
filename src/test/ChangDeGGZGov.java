import com.snatch.common.NewBaseSnatch;
import com.snatch.model.Notice;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.JobHander;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.utils.HttpConstant;
import us.codecraft.webmagic.utils.UrlUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.snatch.common.SnatchContent.*;

/**
 * Created by Administrator on 2018/7/25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component("changDeGGZGov")
@JobHander(value = "ChangDeGGZGov")
public class ChangDeGGZGov extends NewBaseSnatch {

    @Test
    public void test() {
        Spider.create(new ChangDeGGZGov()).addUrl(URLS[0]).thread(1).run();
    }

    private static final String[] URLS = {
            "http://ggzy.changde.gov.cn/cdweb/showinfo/MoreInfoJSGC.aspx?CategoryNum=004001001",
            "http://ggzy.changde.gov.cn/cdweb/jyxx/004001/004001002/MoreInfo.aspx?CategoryNum=004001002",
            "http://ggzy.changde.gov.cn/cdweb/jyxx/004001/004001003/MoreInfo.aspx?CategoryNum=004001003",
            "http://ggzy.changde.gov.cn/cdweb/showinfo/MoreInfoJSGC.aspx?CategoryNum=004002001",
            "http://ggzy.changde.gov.cn/cdweb/jyxx/004002/004002002/MoreInfo.aspx?CategoryNum=004002002",
            "http://ggzy.changde.gov.cn/cdweb/jyxx/004002/004002003/MoreInfo.aspx?CategoryNum=004002003"
    };
    private static final String[] CATCH_TYPES = {ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, GENG_ZHENG_TYPE, ZHAO_BIAO_TYPE, ZHONG_BIAO_TYPE, GENG_ZHENG_TYPE};
    private static final String[] NOTICE_TYPES = {"工程建设", "工程建设", "工程建设", "政府采购", "政府采购", "政府采购"};

    String __VIEWSTATE;
    int pageTemp = 1;
    int pageCount = 1;

    @Override
    public void process(Page page) {
        Html html = page.getHtml();
        String currentUrl = page.getUrl().get();
        long priority = page.getRequest().getPriority();
        __VIEWSTATE = html.$("#__VIEWSTATE", "value").get();
        if (priority == 0) {
            System.out.println("开始抓取" + pageTemp + "页列表链接");
            String pageStr = html.xpath("//*[@id='MoreinfoList1_Pager']/table/tbody/tr/td[1]/font[2]/b/text()").get();
            pageCount = Integer.parseInt(pageStr);
            if (pageTemp <= pageCount) {
                Map params = new LinkedHashMap<String, String>();
                params.put("__VIEWSTATE", __VIEWSTATE);
                params.put("__EVENTTARGET", "MoreinfoList1$Pager");
                params.put("__EVENTARGUMENT", String.valueOf(++pageTemp));
                Request request = new Request("http://ggzy.changde.gov.cn/cdweb/showinfo/MoreInfoJSGC.aspx?CategoryNum=004001001");
                request.setMethod(HttpConstant.Method.POST);
                request.setPriority(0);
                request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
                page.addTargetRequest(request);
            }
            //详情urls
            List<String> detailUrls = html.xpath("//*[@id='MoreinfoList1_DataGrid1']/tbody/tr/td[2]/a/@href").all();
            for (int row = 0; row <= detailUrls.size() - 1; row++) {
                String detailsUrl = UrlUtils.canonicalizeUrl(detailUrls.get(row), currentUrl);
                Map extras = new LinkedHashMap<String, Object>();
                extras.put("catchType", accordUrlJudgmentType(currentUrl).get("catchType"));
                extras.put("noticeType", accordUrlJudgmentType(currentUrl).get("noticeType"));
                Request req = new Request(detailsUrl);
                req.setPriority(1);
                req.setExtras(extras);
                page.addTargetRequest(req);
            }
        } else {
            Document detailDoc = html.getDocument();
            String title = detailDoc.select("#InfoDetail_lblTitle").text();
            String dateStr = detailDoc.select("#InfoDetail_lblDate").text();
            String publishDate = dateStr.substring(dateStr.indexOf("更新时间") + 5, dateStr.indexOf("阅读次数")).trim();
            String content = detailDoc.select("#InfoDetail_tblInfo").select("tr").get(2).html();

            Notice notice = new Notice();
            notice.setUrl(currentUrl);
            notice.setProvince("湖南省");
            notice.setProvinceCode("huns");
            notice.setCity("常德市");
            notice.setCityCode("cd");
            notice.setAreaRank(CITY);
            notice.setCatchType((String) page.getRequest().getExtra("catchType"));
            notice.setNoticeType((String) page.getRequest().getExtra("noticeType"));
            notice.setTitle(title);
            notice.setContent(content);
            notice.setOpendate(publishDate);
            System.out.println(notice.getTitle() + "抓取完毕");
        }
    }

    private Map<String, String> accordUrlJudgmentType(String url) {
        Map resultParams = new HashMap<String, String>();
        for (int i = 0; i < URLS.length; i++) {
            if (URLS[i].equals(url)) {
                resultParams.put("catchType", CATCH_TYPES[i]);
                resultParams.put("noticeType", NOTICE_TYPES[i]);
                break;
            }
        }
        return resultParams;
    }

    @Override
    public ReturnT<String> execute(String... params) {
        Spider.create(new ChangDeGGZGov()).addUrl(URLS[0]).thread(1).run();
        return ReturnT.SUCCESS;
    }
}
