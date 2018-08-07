package com.silita.china.hunan;

import com.snatch.common.BaseSnatch;
import com.snatch.common.util.Util;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.codehaus.jackson.JsonNode;
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
import java.util.HashMap;
import java.util.Map;

import static com.snatch.common.SnatchContent.CITY;
import static com.snatch.common.SnatchContent.GENG_ZHENG_TYPE;

/**
 * Created by wangying on 2017/5/23/0023.
 * 岳阳市   http://ggzy.yueyang.gov.cn/
 * http://ggzy.yueyang.gov.cn/004/004001/004001001/about-gcjs.html	工程建设--招标公告
 * http://ggzy.yueyang.gov.cn/004/004001/004001002/about-gcjs.html	工程建设--变更公告
 * http://ggzy.yueyang.gov.cn/004/004001/004001004/about-gcjs.html	工程建设--其他公告
 * http://ggzy.yueyang.gov.cn/004/004002/004002001/about-zfcg.html	政府采购--招标公告
 * http://ggzy.yueyang.gov.cn/004/004002/004002002/about-zfcg.html	政府采购--变更公告
 * http://ggzy.yueyang.gov.cn/004/004002/004002004/about-zfcg.html	政府采购--其它公告
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test.xml"})

@Component
@JobHander(value = "HuNanYueYangyueyang")
public class HuNanYueYangyueyang extends BaseSnatch {

    @Test
    @Override
    public void run() throws Exception {
        sntachTask();
    }

    public void sntachTask() throws Exception {
        String[] urls = {
                "http://ggzy.yueyang.gov.cn/004/004001/004001001/about-gcjs.html",
                "http://ggzy.yueyang.gov.cn/004/004001/004001002/about-gcjs.html",
                "http://ggzy.yueyang.gov.cn/004/004001/004001004/about-gcjs.html",
                "http://ggzy.yueyang.gov.cn/004/004002/004002001/about-zfcg.html",
                "http://ggzy.yueyang.gov.cn/004/004002/004002002/about-zfcg.html",
                "http://ggzy.yueyang.gov.cn/004/004002/004002004/about-zfcg.html"
        };
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String[] catchTypes = {"1",GENG_ZHENG_TYPE,"0","1",GENG_ZHENG_TYPE,"0"};
        Connection conn = null;
        Document doc = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < urls.length; i++) {
            int page = 1;
            int pageTemp = 0;
            String url = urls[i];
            String snatchNumber = SnatchUtils.makeSnatchNumber();
            try {
                super.queryBeforeSnatchState(urls[i]);
                for (int pagelist = 1; pagelist <= page; pagelist++) {
                    if (pageTemp > 0 && page > pageTemp) {
                        break;
                    }
                    if (pagelist > 1) {
                        url = urls[i].substring(0, urls[i].lastIndexOf("/") + 1) + pagelist + ".html";
                    }
                    SnatchLogger.debug("第" + pagelist + "页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc = conn.get();
                    if (page == 1) {
                        //获取总页数
                        Element item = doc.select(".fengye").first().select("li").last();    //得到纪录页数区域的div（此中包含需要得到的总页数）
                        String type = item.select("a").attr("href");    //得到纪录页数区域的内容（此DIV中有需要的text内容）
                        String countPage = type.substring(type.lastIndexOf("/") + 1, type.indexOf(".html"));   //获取总页数
                        page = Integer.parseInt(countPage);
                        pageTemp = page;
                        SnatchLogger.debug("总" + page + "页");
                        page = computeTotalPage(page, LAST_ALLPAGE);
                    }
                    Element ul = doc.select(".erjitongzhilist").select("ul").first();       //得到存放标题以及时间的区域ul
                    Elements lis = ul.select("li");                                          //得到ul中的所有li
                    for (int row = 0; row < lis.size(); row++) {
                        String conturl = lis.get(row).select("a").first().absUrl("href");  //得到标题（a）的href值
                        if (!"".equals(conturl)) {
                            String date = sdf.format(sdf.parse(lis.get(row).select("span").text().trim()));   //得到日期
                            String title = lis.get(row).select("a").attr("title");
                            Notice notice = new Notice();
                            notice.setProvince("湖南省");
                            notice.setProvinceCode("huns");
                            notice.setCity("岳阳市");
                            notice.setCityCode("yy");
                            notice.setSnatchNumber(snatchNumber);
                            notice.setAreaRank(CITY);
                            notice = SnatchUtils.setCatchTypeByTitle(notice,title);
                            if (SnatchUtils.isNull(notice.getCatchType())) {
                                notice.setCatchType(catchTypes[i]);
                            } else {
                                SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchTypes[i]);
                            }
                            if (i < 3) {
                                notice.setNoticeType("工程建设");
                            } else {
                                notice.setNoticeType("政府采购");
                            }

                            // 获取维度信息
                            if ((i==0 || i==3) && conturl.contains(".html")) {
                                String uuid = conturl.substring(conturl.lastIndexOf("/") + 1,conturl.lastIndexOf(".html"));
                                String postUrl = "";
                                String postParma = "";
                                if (i == 0) {
                                    postUrl = "http://ggzy.yueyang.gov.cn:90/TPFrame/jsgcztbmis2/pages/zhaobiaogonggao/yykbtjlistAction.action?cmd=page_Load&isCommondto=true&gonggaoguid=" + uuid;
                                    postParma = "[{\"id\":\"datagrid\",\"type\":\"datagrid\",\"action\":\"defaultModel\",\"idField\":\"rowguid\",\"pageIndex\":0,\"pageSize\":10,\"sortField\":\"\",\"sortOrder\":\"\",\"columns\":[{\"fieldName\":\"FaBaoNo\"},{\"fieldName\":\"FaBaoName\"},{\"fieldName\":\"BiaoDuanNo\"},{\"fieldName\":\"BiaoDuanName\"},{\"fieldName\":\"kaibiaodate\",\"format\":\"yyyy-MM-dd HH:mm\"},{\"fieldName\":\"KaiBiaoTiaoJian\"}],\"url\":\"yykbtjlistAction.action?cmd=defaultModel\",\"data\":[]},{\"id\":\"_common_hidden_viewdata\",\"type\":\"hidden\",\"value\":\"\"}]";
                                } else {
                                    postUrl = "http://ggzy.yueyang.gov.cn:90/TPFrame/zfcgztbmis/pages/zhaobiaogginfo/yyjyggkaibiaopanduanaction.action?cmd=page_Load&isCommondto=true&gonggaoguid=" + uuid;
                                    postParma = "[{\"id\":\"datagrid\",\"type\":\"datagrid\",\"action\":\"dataModel\",\"idField\":\"rowguid\",\"pageIndex\":0,\"pageSize\":10,\"sortField\":\"\",\"sortOrder\":\"\",\"columns\":[{\"fieldName\":\"projectno\"},{\"fieldName\":\"projectname\"},{\"fieldName\":\"biaoduanno\"},{\"fieldName\":\"biaoduanname\"},{\"fieldName\":\"kaibiaodate\",\"format\":\"yyyy-MM-dd HH:mm\"},{\"fieldName\":\"iskaibiaotiaojian\"}],\"url\":\"yyjyggkaibiaopanduanaction.action?cmd=dataModel\",\"data\":[]},{\"id\":\"_common_hidden_viewdata\",\"type\":\"hidden\",\"value\":\"\"}]";
                                }
                                Connection dmConn = Jsoup.connect(postUrl).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true).ignoreContentType(true);
                                dmConn.data("commonDto",postParma);
                                Map<String,String> headers = new HashMap<String,String>();
                                headers.put("Accept","application/json, text/javascript, */*; q=0.01");
                                headers.put("Accept-Encoding","gzip, deflate");
                                headers.put("Accept-Language","zh-CN,zh;q=0.9");
                                headers.put("Connection","keep-alive");
                                headers.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
                                headers.put("Host","ggzy.yueyang.gov.cn:90");
                                headers.put("Origin","http://ggzy.yueyang.gov.cn:90");
                                headers.put("Referer",postUrl);
                                headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36");
                                headers.put("X-Requested-With","XMLHttpRequest");
                                dmConn.data(headers);
                                dmConn.cookies(dmConn.response().cookies());
                                Document dmDoc = dmConn.post();
                                JsonNode json = Util.getContentByJson(dmDoc.text());
                                Dimension dm = new Dimension();
                                // 开标时间(投标截止时间)
                                String kbDate = json.findPath("kaibiaodate").asText();
                                if (SnatchUtils.isNotNull(kbDate)) {
                                    dm.setTbEndDate(SnatchUtils.isNull(kbDate)?null:kbDate.substring(0,kbDate.indexOf(" ")));
                                    dm.setTbEndTime(SnatchUtils.isNull(kbDate)?null:kbDate.substring(kbDate.indexOf(" ") + 1,kbDate.length()));
                                }
                                // 投标保证金截止时间
                                String tbbzjDate = json.findPath("bzjshoudate").asText();
                                if (SnatchUtils.isNotNull(tbbzjDate)) {
                                    tbbzjDate = sdf2.format(Long.valueOf(tbbzjDate));
                                    dm.setTbAssureEndDate(tbbzjDate.substring(0,tbbzjDate.indexOf(" ")));
                                    dm.setTbAssureEndTime(tbbzjDate.substring(tbbzjDate.indexOf(" ")+1,tbbzjDate.length()));
                                }
                                // 招标人
                                dm.setZbName(json.findPath("jianshedanwei").asText());
                                notice.setDimension(dm);
                            }

                            notice.setUrl(conturl);
                            notice.setOpendate(date);
                            CURRENT_PAGE_LAST_OPENDATE = date;
                            if ("政府采购".equals(notice.getNoticeType())) {
                                page = govDetailHandle(notice, pagelist, page, row, lis.size());
                            } else {
                                page = detailHandle(notice, pagelist, page, row, lis.size());
                            }
                        }
                    }
                    if (pagelist == page) {
                        page = turnPageEstimate(page);
                    }
                }
                super.saveAllPageIncrement(urls[i]);
            } finally {
                super.clearClassParam();
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document contdoc = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        contdoc.select("script").remove();
        String content = contdoc.select(".xiangxiyekuang").first().html();              //得到中标详细内容区域
        notice.setContent(content);
        String title = contdoc.select(".xiangxiyebiaoti").first().text().trim();
        notice.setTitle(title);
        return notice;
    }
}

