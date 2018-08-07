package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.snatch.common.SnatchContent.*;
/**
 * Created by maofeng on 2017/9/22.
 * 湖南省公共资源交易服务平台
 * http://www.hnsggzy.com/gczb/index.jhtml      工程招标
 * http://www.hnsggzy.com/jygkqt/index.jhtml    其他交易
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value="HuNanShenggzyfwpt")
public class HuNanShenggzyfwpt extends BaseSnatch {


    @Test
    @Override
    public void run()throws Exception{
        firstGetMaxId();
    }

    private void firstGetMaxId() throws Exception{
        String[] urls = {
                "http://www.hnsggzy.com/gczb/index.jhtml",      //工程招标
                "http://www.hnsggzy.com/jygkqt/index.jhtml"     //其他交易
        };

        for (int i = 0; i < urls.length; i++) {
            int pageTemp = 0;
            int page = 1;
            Connection conn = null;
            Document doc = null;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try{
                super.queryBeforeSnatchState(urls[i]);//查询url前次抓取的情况（最大页数与公示时间）
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if(pageTemp>0 && page>pageTemp){
                        break;
                    }
                    if(pagelist>1){
                        url = urls[i].replace("index","index_" + pagelist);
                    }
                    SnatchLogger.debug("==== 第"+pagelist+"页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000*60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if(page == 1){
                        String pageCont = doc.select(".pages-list").select("li").first().text().trim();
                        page = Integer.valueOf(pageCont.substring(pageCont.indexOf("/")+1,pageCont.lastIndexOf("页")));
                        SnatchLogger.debug("共"+page+"页");
                        pageTemp = page;
                        //===入库代码====
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }
                    Elements trs = doc.select(".article-list2").first().select("li");
                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = trs.get(row).select("a").first().absUrl("href");   //公告url
                        if(SnatchUtils.isNotNull(conturl)){
                            Notice notice = new Notice();
                            Element div = trs.get(row).select(".article-list3-t2").first(); // 副标题信息区域

                            String type = div.select(".list-t2").get(1).text().trim();
                            type = type.substring(type.indexOf("信息类型：")+5);

                            notice = SnatchUtils.setCatchTypeByTitle(notice,type);
                            if (SnatchUtils.isNull(notice.getCatchType())) {
                                notice.setCatchType(OTHER_TYPE);
                            }

                            if(i==0){
                                String noticeType = div.select(".list-t2").last().text().trim();
                                noticeType = noticeType.substring(noticeType.indexOf("招标类型：")+5);
                                notice.setNoticeType(noticeType);
                            }else if(i==1){
                                if (SnatchUtils.isNull(notice.getCatchType())) {
                                    notice.setCatchType(ZHAO_BIAO_TYPE);
                                }
                                notice.setNoticeType("其他交易");
                            }
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCatchType(PROVINCE);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setUrl(conturl);
                            //详情信息入库，获取增量页数
                            page = detailHandle(notice,pagelist,page,row,trs.size());
                        }
                    }
                    //===入库代码====
                    if(pagelist==page){
                        page = turnPageEstimate(page);
                    }
                }
                //===入库代码====
                saveAllPageIncrement(urls[i]);
            }finally {
                clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception{
        Document docCont = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        String title = docCont.select(".content-title").first().text().trim();  //公告标题
        String content = docCont.select(".gycq-table").first().html();      //公告内容
        if (href.contains("changde")){
            content = docCont.select(".gycq-table").first().text();
        }
        String dateCont = docCont.select(".div-title2").first().select("span[style=padding-right:50px]").first().text();
        String dateReg = "\\d{4}(\\-|\\/|\\.)\\d{1,2}\\1\\d{1,2}";
        Pattern pattern = Pattern.compile(dateReg);
        Matcher matcher = pattern.matcher(dateCont);
        String date = "";
        while (matcher.find()){
            date = matcher.group();
            break;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        date = sdf.format(sdf.parse(date));
        notice.setOpendate(date);
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }






}