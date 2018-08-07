package com.silita.china.shanghai;

import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
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

import java.io.IOException;
import java.math.BigDecimal;import static com.snatch.common.SnatchContent.*;

/**
 * 上海市住房和城乡建设管理委员会官网    http://www.shjjw.gov.cn
 * https://www.ciac.sh.cn/NetInterBidweb/GKTB/SgfbZbxx.aspx 建设工程招标公告
 * http://www.ciac.sh.cn/XmZtbbaWeb/Gsqk/GsFbList.aspx      建设工程中标候选人公示
 * https://www.ciac.sh.cn/NetInterBidweb/GKTB/SgfbZbxx.aspx?xmlb=sgfb   施工专业分包招标
 * http://www.ciac.sh.cn/XmZtbbaFbWeb/Gsqk/GsFbList.aspx    施工专业分包工程中标候选人公示
 * Created by maofeng on 2018/3/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "ShangHaizfcj")
public class ShangHaizfcj extends BaseSnatch {


    private static final String source = "shangh";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask() throws Exception {
        String[] urls = {
                "https://www.ciac.sh.cn/NetInterBidweb/GKTB/SgfbZbxx.aspx", // 建设工程招标公告
                "http://www.ciac.sh.cn/XmZtbbaWeb/Gsqk/GsFbList.aspx",      // 建设工程中标候选人公示
                "https://www.ciac.sh.cn/NetInterBidweb/GKTB/SgfbZbxx.aspx?xmlb=sgfb",   // 施工专业分包招标
                "http://www.ciac.sh.cn/XmZtbbaFbWeb/Gsqk/GsFbList.aspx"     // 施工专业分包工程中标候选人公示
        };
        String[] catchTypes = {"1","2","1","2"};

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
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);

                    if(pagelist == 1){
                        //获取总页数
                        doc = conn.get();
                        // 固定页数
                        switch (i) {
                            case 0 :
                                page = 3;
                                break;
                            case 1 :
                                page = 3;
                                break;
                            case 2 :
                                page = 1;
                                break;
                            case 3 :
                                page = 1;
                                break;
                            default:
                                break;
                        }
                        pageTemp = page;
                        SnatchLogger.debug("总"+page+"页");
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    } else {
                        if (i == 0) {
                            conn.data("__EVENTTARGET","");
                            conn.data("__EVENTARGUMENT","");
                            conn.data("__VIEWSTATE",doc.select("#__VIEWSTATE").first().val());
                            conn.data("__VIEWSTATEGENERATOR",doc.select("#__VIEWSTATEGENERATOR").first().val());
                            conn.data("__EVENTVALIDATION",doc.select("#__EVENTVALIDATION").first().val());
                            conn.data("dr_gglb","0");
                            conn.data("txt_beginTime","");
                            conn.data("txt_endTime","");
                            conn.data("nextPages","下一页");
                            conn.data("DropDownList_page",pagelist - 1 + "");
                            conn.data("hdInputNum",pagelist - 1 + "");
                            conn.data("hdPageCount","3");
                            conn.data("hdState","");
                        } else {
                            conn.data("__EVENTTARGET","gvList");
                            conn.data("__EVENTARGUMENT","Page$" + pagelist);
                            conn.data("__VIEWSTATE",doc.select("#__VIEWSTATE").first().val());
                            conn.data("__VIEWSTATEGENERATOR",doc.select("#__VIEWSTATEGENERATOR").first().val());
                            conn.data("__EVENTVALIDATION",doc.select("#__EVENTVALIDATION").first().val());
                            conn.data("ddlZblx","");
                            conn.data("txtgsrq","");
                            conn.data("txtTogsrq","");
                            conn.data("txttbr","");
                            conn.data("txtzbhxr","");
                            conn.data("txtxmmc","");
                        }
                        doc = conn.post();
                    }


                    SnatchLogger.debug("第"+pagelist+"页");
                    Elements trs = null;
                    // 每页公告数目
                    if (i % 2 == 0) {
                        trs = doc.select("tr[style=border-bottom: black solid; border-bottom-width: 1px; font-size: 12px;]");
                    } else {
                        trs = doc.select("td.tdhh2");
                    }

                    for (int row = 0; row < trs.size(); row++) {
                        String conturl = "";
                        try {
                            conturl = trs.get(row).select("a").first().attr("onclick").trim();
                        }catch (NullPointerException e) {
                            break;
                        }

                        String ggId = "";
                        if (i % 2 == 0) {
                            ggId = conturl.substring(conturl.indexOf("openWindow('") + 12 ,conturl.indexOf("','"));
                            conturl = "https://www.ciac.sh.cn/NetInterBidweb/GKTB/DefaultV2011.aspx?gkzbXh=" + ggId;
                        } else if (i == 3) {
                            ggId = conturl.substring(conturl.indexOf("ShowGs(") + 7,conturl.indexOf(");"));
                            conturl = "http://www.ciac.sh.cn/XmZtbbaFbWeb/Gsqk/GsFb.aspx?zbid=" + ggId;
                        } else {
                            ggId = conturl.substring(conturl.indexOf("ShowGs(") + 7,conturl.indexOf(","));
                            conturl = "http://www.ciac.sh.cn/XmZtbbaWeb/Gsqk/GsFb2015.aspx?zbdjid=&zbid=" + ggId;
                        }

                        if (SnatchUtils.isNotNull(conturl)) {
                            Notice notice = new Notice();
                            String title = trs.get(row).select("a").first().text().trim();
                            String date = "";
                            String projType = "";

                            if (i % 2 == 0) {
                                date = trs.get(row).select("a").first().parent().nextElementSibling().text().trim();
                                projType = trs.get(row).select("a").first().parent().nextElementSibling().nextElementSibling().text().trim();
                            } else {
                                date = trs.get(row).nextElementSibling().text().trim();
                                projType = trs.get(row).nextElementSibling().nextElementSibling().text().trim();
                            }

                            date = date.replaceAll("/","-");
                            date = date.replace("年","-").replace("月","-").replace("日","");
                            notice.setProvince("上海市");
                            notice.setProvinceCode("shs");
                            notice.setCatchType(catchTypes[i]);
                            notice.setNoticeType("工程建设");
                            notice.setUrl(conturl);
                            notice.setTitle(title);
                            notice.setOpendate(date);
                            notice.setSnatchNumber(snatchNumber);
                            notice.setAreaRank(PROVINCE);
                            notice.setSource(source);
                            Dimension dm = new Dimension();
                            dm.setProjType(projType);
                            notice.setDimension(dm);

                            page = detailHandle(notice, pagelist, page, row, trs.size());
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
        String type = SnatchUtils.getMimeType(href);
        String content = "";
        try{
            if("application/pdf".equals(type)){
                content = SnatchUtils.readPDF(href);
                if(!"".equals(content)){
                    content ="<p>"+content.replaceAll("\r\n", "</p><p>")+"</p>";
                }
            }  else {
                Document docCount = Jsoup.connect(href).userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko").timeout(1000 * 60).ignoreHttpErrors(true).get();
                if ("1".equals(notice.getCatchType())) {
                    Elements elms = docCount.select("table.table_css").first().select("tr");
                    if (elms.size() > 2) {
                        elms.get(elms.size()-1).remove();
                        elms.get(elms.size()-2).remove();
                        content = docCount.select("table.table_css").first().html();
                        Document doc = Jsoup.parse(content);
                        // 维度获取
                        Dimension dm = notice.getDimension();
                        String zbName = doc.select("#ctl00_ContentPlaceHolder1_zbxxV2011_lblzbr").first().text().trim(); // 招标人
                        String zbContactMan = doc.select("#ctl00_ContentPlaceHolder1_zbxxV2011_lbllxr").first().text().trim(); // 招标联系人
                        String zbContactWay = doc.select("#ctl00_ContentPlaceHolder1_zbxxV2011_lbllxdh").first().text().trim(); // 招标联系方式
                        String projSum = "";
                        String projectTimeLimit = "";
                        try {
                            projSum = doc.select("#ctl00_ContentPlaceHolder1_zbxxV2011_InfoGctz1_lblZtz").first().text().trim(); // 项目金额
                            if (SnatchUtils.isNotNull(projSum)) {
                                BigDecimal b = new BigDecimal(projSum);
                                projSum = String.valueOf(b.multiply(new BigDecimal(10000)));
                            }
                            projectTimeLimit = doc.select("#ctl00_ContentPlaceHolder1_zbxxV2011_InfoGctz1_lblGq").first().text().trim(); // 项目工期
                        } catch (NullPointerException e) {
                        }


                        // 资质要求

                        String dlName = doc.select("#ctl00_ContentPlaceHolder1_zbxxV2011_lblzbdrdw").first().text().trim(); // 代理人
                        String tbEndDate = doc.select("#ctl00_ContentPlaceHolder1_zbxxV2011_lblTbjzsj").first().text().trim(); // 投标截止时间
                        String tbEndTime = "";
                        if (SnatchUtils.isNotNull(tbEndDate)) {
                            String[] tbTime = tbEndDate.split(" ");
                            tbEndDate = tbTime[0];
                            tbEndTime = tbTime.length>1?tbTime[1]:"";
                            tbEndTime = tbEndTime.length() > 5?tbEndTime.substring(0,5):"";
                        }
                        String tbAssureSum = doc.select("#ctl00_ContentPlaceHolder1_zbxxV2011_lblTbbzj").first().text().trim(); // 投标保证金
                        if (SnatchUtils.isNotNull(tbAssureSum) && !"0".equals(tbAssureSum)) {
                            BigDecimal b = new BigDecimal(tbAssureSum);
                            tbAssureSum = String.valueOf(b.multiply(new BigDecimal(10000)));
                        }
                        String fileCost = doc.select("#ctl00_ContentPlaceHolder1_zbxxV2011_lblGbf").first().text().trim(); // 标书费

                        dm.setZbName(SnatchUtils.isNull(zbName)?"":zbName);
                        dm.setZbContactMan(SnatchUtils.isNull(zbContactMan)?"":zbContactMan);
                        dm.setZbContactWay(SnatchUtils.isNull(zbContactWay)?"":zbContactWay);
                        dm.setProjSum(SnatchUtils.isNull(projSum)?"":projSum);
                        dm.setProjectTimeLimit(SnatchUtils.isNull(projectTimeLimit)?"":projectTimeLimit);
                        dm.setDlName(SnatchUtils.isNull(dlName)?"":dlName);
                        dm.setTbEndDate(SnatchUtils.isNull(tbEndDate)?"":tbEndDate);
                        dm.setTbEndTime(SnatchUtils.isNull(tbEndTime)?"":tbEndTime);
                        dm.setTbAssureSum(SnatchUtils.isNull(tbAssureSum) || "0".equals(tbAssureSum)?"":tbAssureSum);
                        dm.setFileCost(SnatchUtils.isNull(fileCost)?"":fileCost);

                        notice.setDimension(dm);
                    } else {
                        docCount.select("#ctl00_ContentPlaceHolder1_UpanlTime").first().remove();
                        content = docCount.select("table").first().select("tr").get(1).html();
                    }
                } else if ("2".equals(notice.getCatchType())) {
                    if (docCount.text().contains("访问的页面不存在")) {
                        return notice;
                    }
                    content = docCount.select("table.table01").first().html();
                    Document doc = Jsoup.parse(content);
                    // 维度获取
                    Elements elmts = doc.select("table.table01").first().select("tr");
                    Dimension dm = notice.getDimension();
                    for (int i = 1; i < elmts.size(); i++) {
                        Elements e2 = elmts.get(i).select("td");
                        if (e2.size() == 6) {
                            switch (i) {
                                case 1 :
                                    String oneName = e2.get(0).text().trim();
                                    String oneProjDuty = e2.get(2).text().trim();
                                    String projectTimeLimit = e2.get(3).text().trim();
                                    String oneOffer = e2.get(5).text().trim();
                                    if (SnatchUtils.isNotNull(oneOffer)) {
                                        BigDecimal b = new BigDecimal(oneOffer);
                                        oneOffer = String.valueOf(b.multiply(new BigDecimal(10000)));
                                    }
                                    dm.setOneName(SnatchUtils.isNull(oneName)?"":oneName);
                                    dm.setOneProjDuty(SnatchUtils.isNull(oneProjDuty)?"":oneProjDuty);
                                    dm.setProjectTimeLimit(SnatchUtils.isNull(projectTimeLimit)?"":projectTimeLimit);
                                    dm.setOneOffer(SnatchUtils.isNull(oneOffer)?"":oneOffer);
                                    break;
                                case 2 :
                                    String twoName = e2.get(0).text().trim();
                                    String twoOffer = e2.get(5).text().trim();
                                    if (SnatchUtils.isNotNull(twoOffer)) {
                                        BigDecimal b = new BigDecimal(twoOffer);
                                        twoOffer = String.valueOf(b.multiply(new BigDecimal(10000)));
                                    }
                                    dm.setTwoOffer(SnatchUtils.isNull(twoName)?"":twoName);
                                    dm.setTwoOffer(SnatchUtils.isNull(twoOffer)?"":twoOffer);
                                    break;
                                case 3:
                                    String threeName = e2.get(0).text().trim();
                                    String threeOffer = e2.get(5).text().trim();
                                    if (SnatchUtils.isNotNull(threeOffer)) {
                                        BigDecimal b = new BigDecimal(threeOffer);
                                        threeOffer = String.valueOf(b.multiply(new BigDecimal(10000)));
                                    }
                                    dm.setThreeName(SnatchUtils.isNull(threeName)?"":threeName);
                                    dm.setThreeOffer(SnatchUtils.isNull(threeOffer)?"":threeOffer);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    notice.setDimension(dm);
                }
            }
        } catch (IOException e) {
            SnatchLogger.error(e);
        }
        notice.setContent(content);
        return notice;
    }
}
