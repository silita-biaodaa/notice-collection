package com.silita.china.hunan.zhongbiao;

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

import static com.snatch.common.SnatchContent.*;

/**
 * Created by maofeng on 2017/7/18.
 * 长沙市长沙县  长沙县招标采购局
 * http://www.csx.gov.cn/zbcg/zbcg/gcztb/zbgs   工程招投标--中标公告
 * http://www.csx.gov.cn/zbcg/zbcg/zfcg/zbgg2   政府采购--中标公告
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "HuNanChangShaShicsx")
public class HuNanChangShaShicsx extends BaseSnatch{


    @Test
    public void run()throws Exception
    {
        sntachTask();
    }

    public void sntachTask()throws Exception  {
        String[] urls = {
                "http://www.csx.gov.cn/zbcg/zbcg/gcztb/zbgs/index.html",
                "http://www.csx.gov.cn/zbcg/zbcg/zfcg/zbgg2/index.html"
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
                    if(pageTemp > 0 && page > pageTemp){
                        break;
                    }
                    if(pagelist>1){
                        url = urls[i].replace("index","index_" + (pagelist - 1));
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if(page ==1){
                        //获取总页数
                        Element item =doc.select("#autopage").select("center").first();    //得到纪录页数区域的div（此中包含需要得到的总页数）
                        String type = item.html();                      //得到纪录页数区域的内容（此DIV中有需要的text内容）
                        String countPage = type.substring(type.indexOf("countPage = ")+12,type.indexOf("//共多少页"));   //获取总页数
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }
                    Element div = doc.select(".inside_newsbox2").first();    //得到存放标题以及时间的区域div
                    Elements lis = div.select("ul").first().select("li");      //得到div中的所有li
                    for (int row = 0; row < lis.size(); row++) {
                        String conturl=lis.get(row).select("a").attr("href");  //得到标题（a）的href值
                        if(SnatchUtils.isNotNull(conturl)){
                            String date = lis.get(row).select("span").text().trim();
                            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
                            String date1 = sdf.format(sdf.parse(date));
                            Notice notice = new Notice();
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("长沙市");
                            notice.setCityCode("cs");
                            notice.setCounty("长沙县");
                            notice.setCountyCode("csx");
                            notice.setAreaRank(COUNTY);
                            notice.setSnatchNumber(snatchNumber);
                            switch (i) {
                                case 0:
                                    notice.setNoticeType("工程建设");
                                    break;
                                case 1:
                                    notice.setNoticeType("政府采购");
                                    break;
                            }
                            notice.setUrl(conturl);
                            notice.setOpendate(date1);
                            CURRENT_PAGE_LAST_OPENDATE = date1;
                            if (notice.getNoticeType().contains("采购")) {
                                page = govDetailHandle(notice, pagelist, page, row, lis.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, lis.size());
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
    public Notice detail(String href,Notice notice,String catchType) throws Exception{
        Document contdoc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
        String content=contdoc.select(".TRS_PreAppend").html();              //得到中标详细内容区域
        String title = contdoc.select("#articleTitle").first().text().trim();
        if (title.contains("流标")) {
            notice.setCatchType(LIU_BIAO_TYPE);
        } else if (title.contains("废标")) {
            notice.setCatchType(FEI_BIAO_TYPE);
        } else if (title.contains("中标") && (title.contains("补充") || title.contains("更正") || title.contains("更改")
                ||title.contains("修改"))) {
            notice.setCatchType(ZHONG_BIAO_BU_CHONG_TYPE);
        } else {
            notice.setCatchType(ZHONG_BIAO_TYPE);
        }
        if(!(content.length()>0)){
            content = String.valueOf(contdoc.select(".cont").get(3).select("p"));
        }
        if(content.length()<=250){
            content = contdoc.select(".TRS_Editor").first().html();
        }
        notice.setContent(content);
        notice.setTitle(title);
        return notice;
    }


}
