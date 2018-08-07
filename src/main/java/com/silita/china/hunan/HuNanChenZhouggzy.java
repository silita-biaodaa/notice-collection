package com.silita.china.hunan;

import com.silita.china.hunan.zhongbiao.HuNanChenZhouczggzynet;
import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/** http://www.czggzy.net/jyxx/004001/004001001/004001001001/list4.html (房建市政)
 * http://www.czggzy.net/jyxx/004001/004001001/004001001002/list4.html (交通)
 * http://www.czggzy.net/jyxx/004001/004001001/004001001003/list4.html (水利)
 * http://www.czggzy.net/jyxx/004001/004001001/004001001004/list4.html(其他)
 * 郴州公共资源交易中心（只抓取招标）公告内容为word文档
 * @author gmy
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring-test.xml" })


@Component
@JobHander(value="HuNanChenZhouggzy")
public class HuNanChenZhouggzy extends BaseSnatch {


    @Test
    public void run()throws Exception {
        firstGetMaxId();
    }

    private void firstGetMaxId()throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("房建市政");
        list.add("交通");
        list.add("其他");
        String[] urls = {"http://www.czggzy.net/jyxx/004001/004001001/004001001001/list4.html",
                "http://www.czggzy.net/jyxx/004001/004001001/004001001002/list4.html",
//            "http://www.czggzy.net/jyxx/004001/004001001/004001001003/list4.html",
                "http://www.czggzy.net/jyxx/004001/004001001/004001001004/list4.html"};
        Connection conn = null;
        Document doc = null;
        for (int i = 0; i < list.size(); i++) { //4个栏目
            int page = 1;
            int pageBackup = 0;
            String url = "";
            try{
                if(pageBackup> 0 &&page>pageBackup){
                    //===入库代码====
                    super.saveAllPageIncrement(urls[i]);
                    continue;
                }
                //===入库代码====
                super.queryBeforeSnatchState(urls[i]);//查询url前次抓取的情况（最大页数与公示时间）
                for(int pagelist = 1; pagelist <= page; pagelist++){ //分页
                    if(pageBackup>0 && page>pageBackup){
                        break;
                    }
                    if(list.get(i).equals("房建市政")){
                        url = "http://www.czggzy.net/jyxx/004001/004001001/004001001001/list4.html";
                        if(pagelist > 1) {
                            url = "http://www.czggzy.net/jyxx/004001/004001001/004001001001/" + pagelist + ".html";
                        }
                    }else if(list.get(i).equals("交通")){
                        url = "http://www.czggzy.net/jyxx/004001/004001001/004001001002/list4.html";
                        if(pagelist > 1) {
                            url = "http://www.czggzy.net/jyxx/004001/004001001/004001001002/" + pagelist + ".html";
                        }
                    }else if(list.get(i).equals("其他")){
                        url = "http://www.czggzy.net/jyxx/004001/004001001/004001001004/list4.html";
                        if(pagelist > 1) {
                            url = "http://www.czggzy.net/jyxx/004001/004001001/004001001004/" + pagelist + ".html";
                        }
                    }
                    SnatchLogger.debug("第"+pagelist+"页");
                    conn = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                    doc= null;
                    try {
                        doc = conn.get();
                    } catch (IOException e) {
                        SnatchLogger.error(e);
                    }
                    if(page == 1) {
                        String textStr = doc.select("#index").text();
                        String countPage=textStr.substring(textStr.indexOf("/")+1).trim();
                        page=Integer.parseInt(countPage);
                        pageBackup = page;
                        SnatchLogger.debug("总"+page+"页");
                        //===入库代码====
                        page = computeTotalPage(page,LAST_ALLPAGE);
                    }
                    Elements trs = doc.select("li.clearfix");
                    for(int row = 0;row < trs.size();row++){    //公告数
                        String filePath = trs.get(row).select("a").attr("href");
                        if("".equals(filePath.trim())||filePath==null){
                            continue;
                        }
                        String href = "http://www.czggzy.net" + filePath;
                        String date = trs.get(row).select("span").text().trim();
                        String title = trs.get(row).select("a").text();
                        //===入库代码====
                        Notice notice = new Notice();
                        notice.setProvince("湖南省");
                        notice.setProvinceCode("huns");
                        notice.setCity("郴州");
                        notice.setCityCode("cz");
                        notice = HuNanChenZhouczggzynet.getCatchType(notice,title);
                        notice.setUrl(href);
                        notice.setOpendate(date);
                        notice.setTitle(title);
                        //详情信息入库，获取增量页数
                        page =detailHandle(notice,pagelist,page,row,trs.size());
                    }
                    //===入库代码====
                    if(pagelist==page){
                        page = turnPageEstimate(page);
                    }
                }
                //===入库代码====
                super.saveAllPageIncrement(urls[i]);
            }finally {
                conn = null;
                doc = null;
                clearClassParam();
            }
        }
    }

    /**
     * 读PDF文件，使用了pdfbox开源项目
     * @throws IOException
     * @throws TikaException
     */
    public String readPDF(String pdfUrl) throws IOException {
        Metadata metadata = new Metadata();
        TikaInputStream stream = TikaInputStream.get(new URL(pdfUrl), metadata);
        // 获取解析后得到的PDF文档对象
        PDDocument document = PDDocument.load(stream);
        PDFTextStripper stripper = new PDFTextStripper();
        // 从PDF文档对象中剥离文本返回
        return stripper.getText(document);
    }

    /**
     * 读doc文件
     * @throws IOException
     * @throws TikaException
     */
    public String readWord(String wdUrl) throws Exception{
        //解析word文件
        Metadata metadata = new Metadata();
        TikaInputStream stream  = null;
        try {
            stream = TikaInputStream.get(new URL(wdUrl), metadata);
            HWPFDocument doc = new HWPFDocument(stream);
            Range rang = doc.getRange();
            return rang.text();
        } catch (Exception e) {
            XWPFDocument docx;
            String text = "";
            try {
                docx = new XWPFDocument(stream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
                text = extractor.getText();
            } catch (IOException e1) {
            }
            return text;
        }
    }

    public static String getMimeType(String fileUrl)
            throws IOException {
        String type = null;
        URL u = new URL(fileUrl);
        URLConnection uc = null;
        uc = u.openConnection();
        type = uc.getContentType();
        return type;
    }

    public Notice detail(String href, Notice notice, String catchType) throws Exception{
        String content="";
        if(href.contains(".html")) {    //文件在子頁面中
            Document docCount = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
            String subFilePath = docCount.select("[style=margin-left:20px;]").get(0).select("a").attr("href");
            href = "http://www.czggzy.net" + subFilePath;
        }
        String type = getMimeType(href);
        try{
            if("application/pdf".equals(type) || type.contains("pdf")){
                content = readPDF(href);
            } else if("application/msword".equals(type) || type.contains("doc") || type.contains("application/vnd")){
                content = readWord(href);
            }
        }catch (IOException ex){
            SnatchLogger.error(ex);
        }
        if(!"".equals(content)){
            content ="<p>"+content.replaceAll("\r\n", "</p><p>")+"</p>";
        }else {

        }
        notice.setContent(content);
        return notice;
    }


}
