package com.silita.china.hunan.zhongbiao;

import com.silita.service.INoticeService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.snatch.common.SnatchContent.PROVINCE;
import static com.snatch.common.SnatchContent.ZHONG_BIAO_TYPE;

/**
 * 湖南省招标监管网 http://218.76.24.174/
 * http://www.bidding.hunan.gov.cn/jyxxzbhx/index.jhtml
 * http://www.bidding.hunan.gov.cn/jyxxzbjg/index.jhtml
 * 中标候选人公示、中标结果
 * Created by dh on 2017/6/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@JobHander(value = "HuNanZhaobiaoMonitor")
@Component
public class HuNanZhaobiaoMonitor extends BaseSnatch{

    @Autowired
    private INoticeService service;

    @Test
    @Override
    public void run()throws Exception{
        snatchTask();
    }
    
    private void snatchTask () throws Exception{
        String[] urls = {
                "http://www.bidding.hunan.gov.cn/jyxxzbhx/index.jhtml",
                "http://www.bidding.hunan.gov.cn/jyxxzbjg/index.jhtml"
        };
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
                        url = urls[i].replace("index" ,"index_" + pagelist);
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if(page == 1){
                        //获取总页数
                        String pageCont = doc.select(".pages-list").first().select("a").first().text();
                        page = Integer.valueOf(pageCont.substring(pageCont.indexOf("/")+1,pageCont.lastIndexOf("页")));
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".article-list3-t");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");
                        if (SnatchUtils.isNotNull(conturl)) {
                            Notice notice = new Notice();
                            String date = trs.get(row).select(".list-times").first().text().trim();
                            String areaTitle = trs.get(row).select("label").first().text().trim().replace("【","").replace("】","");
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            if (!"省级".equals(areaTitle)) {
                                // 查询地区代码
                                List<Map<String,Object>> cityCodeList = service.querysCityCode("湖南");
                                for (Map<String,Object> map:cityCodeList) {
                                    String cityName = String.valueOf(map.get("name"));
                                    if (cityName.indexOf(areaTitle) != -1) {
                                        notice.setCity(cityName);
                                        notice.setCityCode(String.valueOf(map.get("name_abbr")));
                                        break;
                                    }
                                }
                            }
                            notice.setAreaRank(PROVINCE);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setCatchType(ZHONG_BIAO_TYPE);
                            notice.setNoticeType("中标公告");
                            notice.setUrl(conturl);
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
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String title = docCount.select(".div-title").first().text().trim();
        String content = docCount.select(".div-article2").first().html();
        notice = SnatchUtils.setCatchTypeByTitle(notice,title);
        if (SnatchUtils.isNull(notice.getCatchType())) {
            notice.setCatchType(ZHONG_BIAO_TYPE);
        }else {
            SnatchUtils.judgeCatchType(notice,notice.getCatchType(),ZHONG_BIAO_TYPE);
        }
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
