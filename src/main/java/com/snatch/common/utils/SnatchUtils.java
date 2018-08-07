package com.snatch.common.utils;

import com.silita.service.INoticeService;
import com.snatch.common.SnatchContent;
import com.snatch.common.util.Util;
import com.snatch.model.Notice;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.codehaus.jackson.JsonNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SnatchUtils {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public static String excludeScriptTag(String content) {
        int sSstart = content.indexOf("<script>");
        int eEnd = content.indexOf("</script>");
        if (sSstart != -1 && eEnd != -1) {
            content = content.substring(0, sSstart) + content.substring(eEnd + 9);
        }
        if (content.indexOf("<script>") != -1) {
            content = excludeScriptTag(content);
        }
        return content;
    }

    public static String extractStr(String content, String[] startArray, String[] endArray) {
        String startStr = null;
        String endStr = null;
        for (int i = 0; i < startArray.length; i++) {
            startStr = startArray[i];
            endStr = endArray[i];
            int start = content.indexOf(startStr);
            int end = content.indexOf(endStr);
            if (start != -1 && end != -1) {
                return content.substring(start + 1, end);
            }
        }
        return null;
    }

    /**
     * 把字符中的每一条exclude字符去掉，返回新的字符
     *
     * @param str
     * @param excludes
     * @return
     */
    public static String excludeString(String str, String[] excludes) {
        for (String ex : excludes) {
            str = str.replaceAll(ex, "");
        }
        return str;
    }

    public static boolean isNotNull(String str) {
        return str != null && !str.trim().equals("");
    }

    public static boolean isNull(String str) {
        return !isNotNull(str);
    }

    public static List<String> StringSplit(String str, int num) {
        int length = str.length();
        List<String> listStr = new ArrayList<String>();
        int lineNum = length % num == 0 ? length / num : length / num + 1;
        String subStr = "";
        for (int i = 1; i <= lineNum; i++) {
            if (i < lineNum) {
                subStr = str.substring((i - 1) * num, i * num);
            } else {
                subStr = str.substring((i - 1) * num, length);
            }
            listStr.add(subStr);
        }
        return listStr;
    }

    /**
     * 从目标内容中祛除以开头、结尾关键字包含的片段
     *
     * @param content  目标内容
     * @param startStr 开头关键字
     * @param endStr   结尾关键字
     * @return
     */
    public static String excludeStringByKey(String content, String startStr, String endStr) {
        int sSstart = content.indexOf(startStr);
        int eEnd = content.indexOf(endStr);
        if (sSstart != -1 && eEnd != -1) {
            content = content.substring(0, sSstart) + content.substring(eEnd + endStr.length());
        }
        if (content.indexOf(startStr) != -1) {
            content = excludeStringByKey(content, startStr, endStr);
        }
        return content;
    }

    /**
     * 去除所有的字符串中html标签
     *
     * @param content
     * @return
     */
    public static String excludeStringByKey(String content) {
        final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(content);
        content = m_html.replaceAll(""); // 过滤html标签
        return content;
    }

    /**
     * 公告内容table宽度过宽处理
     */
    public static String replaceTable(String html) {
        try {
            Document doc = Jsoup.parse(html);
            Elements tbs = doc.select("table");
            String width = "";
            boolean flag = false;
            for (Element tb : tbs) {
                width = tb.attr("width");
                if (SnatchUtils.isNull(width)) {
                    width = tb.attr("style");
                    if (SnatchUtils.isNotNull(width) && width.toLowerCase().contains("width")) {
                        // 宽度值在style属性中
                        width = width.toLowerCase();
                        String[] styleAttrs = width.split(";");
                        String widthCont = "";
                        for (int i = 0; i < styleAttrs.length; i++) {
                            if (styleAttrs[i].contains("width")) {
                                widthCont = styleAttrs[i];
                                break;
                            }
                        }
                        int widthAttr = 0;
                        String regx = "[1-9]\\d*";
                        Pattern pa = Pattern.compile(regx);
                        Matcher ma = pa.matcher(widthCont);
                        while (ma.find()) {
                            widthAttr = Integer.valueOf(ma.group());
                            break;
                        }
                        if (widthAttr > 696) {    // 宽度属性值大于投标秘书设定宽度
                            width = width.replace(widthCont, "width:100%");
                            tb.attr("style", width);
                            flag = true;
                        }
                    }
                } else {
                    // 宽度值在width属性中
                    width = width.replace("px", "").replace("pt", "");
                    double widthAttr = Double.valueOf(width);
                    if (widthAttr > 696) { //宽度超过投标秘书设定宽度
                        tb.attr("width", "100%");
                        flag = true;
                    }
                }
            }
            if (flag) {    //去除Jsoup.parse() 自动添加的html标签
                StringBuilder sb = new StringBuilder(doc.html());
                sb = sb.replace(sb.indexOf("<html>"), sb.indexOf("<body>") + 6, "");
                sb = sb.replace(sb.indexOf("</body>"), sb.indexOf("</html>") + 7, "");
                html = sb.toString();
            }
            return html;
        } catch (Throwable e) {
            return html;
        }
    }

    /**
     * 采购公告详情是否包含资质
     *
     * @param content
     * @return
     */
    public static boolean isExisCert(String content) {
        String[] regs = {
                "(颁发的).*?(资质)",
                "(核发的).*?(资质)",
                "(具备).*?(资质)",
                "(持有).*?(资质)",
                "(具有).*?(资质)"
        };
        String zzCont = "";
        // 去除html标签
        content = deleteHtmlTag(content);
        for (int i = 0; i < regs.length; i++) {
            Pattern pa = Pattern.compile(regs[i]);
            Matcher ma = pa.matcher(content);
            while (ma.find()) {
                zzCont = ma.group();
                if (SnatchUtils.isNotNull(zzCont) && zzCont.contains("级")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 清理html标签
     *
     * @param content
     * @return
     */
    public static String deleteHtmlTag(String content) {
        content = content.replaceAll("\\s*", ""); // 去除空格
        String regEx_html = "<.+?>"; // HTML标签的正则表达式
        Pattern pattern = Pattern.compile(regEx_html);
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceAll("");
        content = content.replaceAll("&nbsp;", "");
        return content;
    }

    /**
     * 招标绿色通道判断，满足条件的不做过滤，直接抓取
     *
     * @param content
     * @return
     */
    public static boolean isZhaobiaoGreenChannel(String content) {
        String[] regs = {
                "(延期)",
                "(变更)",
                "(补充)",
                "(答疑)",
                "(澄清)",
                "(修改)",
                "(补遗)",
                "(质疑)",
                "(暂停)",
                "(更正)",
                "(终止)"
        };
        String zzCont = "";
        // 去除html标签
        content = deleteHtmlTag(content);
        for (int i = 0; i < regs.length; i++) {
            Pattern pa = Pattern.compile(regs[i]);
            Matcher ma = pa.matcher(content);
            while (ma.find()) {
                zzCont = ma.group();
                if (SnatchUtils.isNotNull(zzCont)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean matchStringArray(String[] regs, String content) {
        String zzCont = "";
        // 去除html标签
        content = deleteHtmlTag(content);
        for (int i = 0; i < regs.length; i++) {
            Pattern pa = Pattern.compile(regs[i]);
            Matcher ma = pa.matcher(content);
            while (ma.find()) {
                zzCont = ma.group();
                if (SnatchUtils.isNotNull(zzCont)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 通过标题设置catchType
     *
     * @param notice
     * @param title  公告标题
     * @return notice
     */
    public static Notice setCatchTypeByTitle(Notice notice, String title) {
        if (title.contains("中标") || title.contains("结果") || title.contains("成交") || title.contains("成果")
                || title.contains("中选")) {
            if (title.contains("代理") || title.contains("比选")) {
                notice.setCatchType(SnatchContent.DL_ZHONG_BIAO_TYPE);
            } else {
                notice.setCatchType(SnatchContent.ZHONG_BIAO_TYPE);
            }
        } else if (title.contains("合同公告") || title.contains("合同公示")) {
            notice.setCatchType(SnatchContent.HE_TONG_TYPE);
        } else if (title.contains("资审结果")) {
            notice.setCatchType(SnatchContent.ZI_SHENG_JIE_GUO_TYPE);
        } else if (title.contains("资格预审")) {
            notice.setCatchType(SnatchContent.ZI_GE_YU_SHEN_TYPE);
        } else if (title.contains("入围")) {
            notice.setCatchType(SnatchContent.RU_WEI_TYPE);
        } else if (title.contains("中标补充") || title.contains("中标更改")) {
            notice.setCatchType(SnatchContent.ZHONG_BIAO_BU_CHONG_TYPE);
        } else if (title.contains("暂停")) {
            notice.setCatchType(SnatchContent.ZAN_TING_TYPE);
        } else if (title.contains("补充") || title.contains("补遗")) {
            notice.setCatchType(SnatchContent.BU_CHONG_TYPE);
        } else if (title.contains("答疑") || title.contains("补疑") || title.contains("质疑")) {
            notice.setCatchType(SnatchContent.DA_YI_TYPE);
        } else if (title.contains("流标")) {
            notice.setCatchType(SnatchContent.LIU_BIAO_TYPE);
        } else if (title.contains("澄清")) {
            notice.setCatchType(SnatchContent.CHENG_QING_TYPE);
        } else if (title.contains("延期") || title.contains("推迟") || title.contains("延长")) {
            notice.setCatchType(SnatchContent.YAN_QI_TYPE);
        } else if (title.contains("变更") || title.contains("更正") || title.contains("调整")) {
            notice.setCatchType(SnatchContent.GENG_ZHENG_TYPE);
        } else if (title.contains("废标")) {
            notice.setCatchType(SnatchContent.FEI_BIAO_TYPE);
        } else if (title.contains("终止")) {
            notice.setCatchType(SnatchContent.ZHONG_ZHI_TYPE);
        } else if (title.contains("修改") && !title.contains("维修改造")) {
            notice.setCatchType(SnatchContent.XIU_GAI_TYPE);
        } else if (title.contains("控制价")) {
            notice.setCatchType(SnatchContent.KONG_ZHI_JIA_TYPE);
        } else if (title.contains("招标") || title.contains("谈判") || title.contains("磋商") || title.contains("询价")
                || title.contains("竞价") || title.contains("单一来源") || title.contains("预审") || title.contains("招标文件")) {
            if (title.contains("代理") || title.contains("比选")) {
                notice.setCatchType(SnatchContent.DL_ZHAO_BIAO_TYPE);
            } else {
                notice.setCatchType(SnatchContent.ZHAO_BIAO_TYPE);
            }
        }
        return notice;
    }

    /**
     * 清理部分HTML标签格式
     *
     * @param noticeContent
     */
    public static String clearHtmlTag(String noticeContent) {
        String Content = excludeStringByKey(noticeContent, "<script", "</script>");
        Content = excludeStringByKey(Content);
        Content = Content.replaceAll("&nbsp;", "");
        return Content;
    }

    /**
     * 抓取普通公告(当网站分类不明确，多种类别公告混合在一个列表中)：
     * 标题中没有招标、中标关键字时，需要根据详情判断是否有资质信息。
     * 有资质则设置catchtype为招标公告；无资质又不是延期、补充类公告时判断为中标类公告。
     */
    public static Notice setCatchTypeByAptitude(Notice notice) {
        if (StringUtils.isBlank(notice.getCatchType())) {//无catchType
            String noticeContent = clearHtmlTag(notice.getContent());
            //判断详情中是否带相关资质关键字
            if (isExisCert(noticeContent))
                notice.setCatchType(SnatchContent.ZHAO_BIAO_TYPE);
            else if (isZhaobiaoGreenChannel(noticeContent))
                notice.setCatchType(SnatchContent.OTHER_TYPE);
            else
                notice.setCatchType(SnatchContent.ZHONG_BIAO_TYPE);
        }
        return notice;
    }

    /**
     * 相对路径转换绝对路径
     *
     * @param content 公告内容
     * @param url     公告url
     * @return sring
     */
    public static String relative2AbsolutePath(String content, String url) {
        if (isExisRelativePath(content)) {
            try {
                content = relative2Absolute(content, url, "a", "href");
                content = relative2Absolute(content, url, "img", "src");
            } catch (Exception e) {
                SnatchLogger.error(e.getMessage(), e);
            }
            if (content.contains("<html>")) {
                // 去除Jsoup加载后自动生成的html、body标签
                StringBuilder sb = new StringBuilder(content);
                sb = sb.replace(sb.indexOf("<html>"), sb.indexOf("<body>") + 6, "");
                sb = sb.replace(sb.indexOf("</body>"), sb.indexOf("</html>") + 7, "");
                content = sb.toString();
            }
        }
        return content;
    }

    public static String relative2Absolute(String content, String url, String tag, String property) throws Exception {
        String newContent = "";
        boolean flag = false;
        if (content != null && content.trim() != "") {
            URI base = new URI(url);// 基本网页URI
            Document doc = Jsoup.parse(content);
            for (Element ele : doc.getElementsByTag(tag)) {
                String elePropValue = ele.attr(property);
                if (elePropValue.contains("javascript") || elePropValue.contains("#"))
                    continue;
                if (!elePropValue.matches("^(https?|ftp|tel|mailto|qq):(\\\\|//).*$")) {
                    try {
                        flag = true;
                        URI abs = base.resolve(elePropValue);// 解析相对URL，得到绝对URI
                        ele.attr(property, abs.toURL().toString());
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
            newContent = doc.html();
        }
        if (flag) {
            return newContent;
        } else {
            return content;
        }
    }


    /**
     * 判断内容中是否存在相对路径
     *
     * @param content
     * @return
     */
    public static boolean isExisRelativePath(String content) {
        String regex = "(href=\"|src=\"|href =\"|src =\"|href = \"|src = \").*?(\")";
        String regex_url = "((http://)|(https://))?([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}(/)";   //绝对路径正则
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String matchResult = matcher.group();
            pattern = Pattern.compile(regex_url);
            matcher = pattern.matcher(matchResult);
            if (!matcher.find()) {
                return true;
            }
        }
        return false;
    }

    private static synchronized Set getAptitude(INoticeService service) {
//    private static synchronized Set getAptitude(CatchService service) {
        if (SnatchContent.APTITUDE_SET == null) {
            SnatchLogger.debug("初始化资质库。。。");
            SnatchContent.APTITUDE_SET = new HashSet<String>();
            List<Map<String, Object>> zzList = service.listAllZh();
//            List<Map<String, Object>> zzList = service.queryzh();
            for (int i = 0; i < zzList.size(); i++) {
                String name = zzList.get(i).get("name").toString();
                SnatchContent.APTITUDE_SET.add(name);
            }
        }
        SnatchLogger.debug("APTITUDE_SET size:" + SnatchContent.APTITUDE_SET.size());
        return SnatchContent.APTITUDE_SET;
    }

    public static boolean existsAptitude(Notice notice, INoticeService service) {
//    public static boolean existsAptitude(Notice notice, CatchService service) throws Exception {
        boolean isExistCert = false;
        String noticeContent = clearHtmlTag(notice.getContent());
        //判断详情中是否带相关资质关键字
//            SnatchLogger.debug(noticeContent);
        isExistCert = isExisCert(noticeContent);
        SnatchLogger.debug("1.isExistCert-->" + isExistCert);
        if (isExistCert) {
            Long start = System.nanoTime();
            //继续判断详情中是否包含资质别名库
            Set<String> zzSet = null;
            if (SnatchContent.APTITUDE_SET == null) {
                zzSet = getAptitude(service);
            } else {
                zzSet = SnatchContent.APTITUDE_SET;
            }
            for (String zzName : zzSet) {
                int idx = noticeContent.indexOf(zzName);
                if (idx != -1) {
                    isExistCert = true;
                    SnatchLogger.debug("2.##isExistCert-->" + isExistCert + "耗时：" + (System.nanoTime() - start) + "ns");
                    break;
                } else {
                    isExistCert = false;
                }
            }
            SnatchLogger.debug("2.##isExistCert-->" + isExistCert + "耗时：" + (System.nanoTime() - start) + "ns");
        }
        noticeContent = null;
        return isExistCert;
    }

    /**
     * 公告详情判断并设置catchType
     */
    public static Notice parseCatchType(Notice notice, INoticeService service) {
//    public static Notice parseCatchType(Notice notice, CatchService service) throws Exception {
        if (existsAptitude(notice, service)) {
            // 详情资质判断，存在即为招标公告
            notice.setCatchType(SnatchContent.ZHAO_BIAO_TYPE);
        } else if (isZhaobiaoGreenChannel(notice.getTitle())) {
            // 标题判断是否带绿色通道的关键字 ， 是则为补充公告
            notice.setCatchType(SnatchContent.OTHER_TYPE);
        } else {
            // 公告详情判断是否为中标公告
            String[] zhongBiaoKey = {"第一候选人", "第一中标候选人", "第一名", "第1名", "第一标段中标候选人",
                    "第二名", "第二候选人", "第二中标候选人", "第2名", "第二标段中标候选人",
                    "第三名", "第三候选人", "第三中标候选人", "第3名", "第三标段中标候选人"};
            String content = deleteHtmlTag(notice.getContent());
            for (int i = 0; i < zhongBiaoKey.length; i++) {
                if (content.contains(zhongBiaoKey[i])) {
                    notice.setCatchType(SnatchContent.ZHONG_BIAO_TYPE);
                    return notice;
                }
            }
        }
        return notice;
    }

    /**
     * 生成批次号
     *
     * @return
     */
    public static String makeSnatchNumber() {
        return sdf.format(System.currentTimeMillis());
    }

    /**
     * 读doc文件
     *
     * @throws IOException
     */
    public static String readWord(String wdUrl) {
        //解析word文件
        Metadata metadata = new Metadata();
        TikaInputStream stream = null;
        try {
            stream = TikaInputStream.get(new URL(wdUrl), metadata);
            HWPFDocument doc = new HWPFDocument(stream);
            Range rang = doc.getRange();
            return rang.text();
        } catch (Exception e) {
            XWPFDocument docx;
            String text = "";
            try {
                stream = TikaInputStream.get(new URL(wdUrl), metadata);
                docx = new XWPFDocument(stream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
                text = extractor.getText();
                if (text == null) {
                    POIXMLTextExtractor extractors = new XWPFWordExtractor(docx);
                    text = extractors.getText();
                }
            } catch (IOException e1) {
            }
            return text;
        }
    }

    /**
     * 读doc文件
     *
     * @param wdUrl
     * @return
     * @throws Exception
     */
    public static String readWordTwo(String wdUrl) throws Exception {
        // 统一资源
        URL url = new URL(wdUrl);
        // 连接类的父类，抽象类
        URLConnection urlConnection = url.openConnection();
        // http的连接类
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        // 设定请求的方法，默认是GET
        httpURLConnection.setRequestMethod("POST");
        // 设置字符编码
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
        // 打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
        httpURLConnection.connect();
        // 文件大小
        BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());
        TikaInputStream stream = null;
        try {
            stream = TikaInputStream.get(bin);
            HWPFDocument doc = new HWPFDocument(stream);
            Range rang = doc.getRange();
            return rang.text();
        } catch (Exception e) {
            XWPFDocument docx;
            String text = "";
            try {
                stream = TikaInputStream.get(bin);
                docx = new XWPFDocument(stream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
                text = extractor.getText();
                if (text == null) {
                    POIXMLTextExtractor extractors = new XWPFWordExtractor(docx);
                    text = extractors.getText();
                }
            } catch (IOException e1) {
            }
            return text;
        }
    }

    /**
     * 读PDF文件，使用了pdfbox开源项目
     *
     * @throws IOException
     */
    public static String readPDF(String pdfUrl) throws IOException {
        Metadata metadata = new Metadata();
        TikaInputStream stream = TikaInputStream.get(new URL(pdfUrl), metadata);
        // 获取解析后得到的PDF文档对象
        PDDocument document = PDDocument.load(stream);
        PDFTextStripper stripper = new PDFTextStripper();
        // 从PDF文档对象中剥离文本返回
        return stripper.getText(document);
    }

    /**
     * 读PDF文件
     *
     * @param pdfUrl
     * @return
     * @throws IOException
     */
    public static String readPDFTwo(String pdfUrl) throws IOException {
        // 统一资源
        URL url = new URL(pdfUrl);
        // 连接类的父类，抽象类
        URLConnection urlConnection = url.openConnection();
        // http的连接类
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        // 设定请求的方法，默认是GET
        httpURLConnection.setRequestMethod("POST");
        // 设置字符编码
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
        // 打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
        httpURLConnection.connect();
        // 文件大小
        BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());

        TikaInputStream stream = TikaInputStream.get(bin);
        // 获取解析后得到的PDF文档对象
        PDDocument document = PDDocument.load(stream);
        PDFTextStripper stripper = new PDFTextStripper();
        // 从PDF文档对象中剥离文本返回
        return stripper.getText(document);
    }

    /**
     * JSON
     *
     * @param context
     * @return
     */
    public static JsonNode getJsonNode(String context) {
        int retryTimes = 0;
        do {
            try {
                JsonNode jsonNode = Util.getContentByJson(context);
                if (jsonNode == null) {
                    throw new Exception();
                }
                return jsonNode;
            } catch (Exception ioExc) {
                int sleepMillis = 1000 * (1 << retryTimes);
                if ((retryTimes + 1) < 5) {
                    try {
                        System.out.println("解析异常，" + sleepMillis + "ms 后重试(第" + (retryTimes + 1) + "次)");
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException interExc) {
                        throw new RuntimeException(interExc);
                    }
                }
            }
        } while (++retryTimes < 5);
        throw new RuntimeException();
    }

    /**
     * 标题判断catchType的后续处理工作
     *
     * @param notice
     * @param noticeCatchType  标题判断出的catchType
     * @param defaultCatchType 默认的catchType
     */
    public static void judgeCatchType(Notice notice, String noticeCatchType, String defaultCatchType) {
        int noticeType = Integer.valueOf(noticeCatchType);
        int defaultType = Integer.valueOf(defaultCatchType);
        /*默认catchType为中标的，标题判断出的catchType为招标，强制设置为为默认catchType*/
        if (defaultType == 2 || defaultType == 5 || defaultType > 50) {
            if (noticeType == 1 || noticeType == 4) {
                notice.setCatchType(defaultCatchType);
            }
        }

        /*默认catchType是补充大分类的，标题判断出的catchType为招标，强制设置为默认catchType*/
        if (defaultType > 10 && defaultType < 50) {
            if (noticeType == 1 || noticeType == 4) {
                notice.setCatchType(defaultCatchType);
            }
        }

    }

    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
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

    public static int comparePastDate(String oldDate, String nowDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date old;
        int day = 0;
        try {
            old = sdf.parse(oldDate);
            calendar.setTime(old);
            Long oTime = calendar.getTimeInMillis();

            Date now = sdf.parse(nowDate);
            calendar.setTime(now);
            Long nTime = calendar.getTimeInMillis();

            day = (int) ((nTime - oTime) / (3600F * 1000 * 24));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return day;
    }

    public static String getNumberRandom(int length) {
        StringBuilder val = new StringBuilder();
        Random random = new Random();
        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {
            val.append(String.valueOf(random.nextInt(10)));
        }
        return val.toString();
    }

    public static String getStringRandom(int length) {
        StringBuilder val = new StringBuilder();
        Random random = new Random();
        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
//                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val.append((char) (random.nextInt(26) + 97));
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                val.append(String.valueOf(random.nextInt(10)));
            }
        }
        return val.toString();
    }

}
