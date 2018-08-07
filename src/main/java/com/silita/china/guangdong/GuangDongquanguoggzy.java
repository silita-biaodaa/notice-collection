package com.silita.china.guangdong;

import com.snatch.common.BaseSnatch;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.xxl.job.core.handler.annotation.JobHander;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;

import static com.snatch.common.SnatchContent.GENG_ZHENG_TYPE;
import static com.snatch.common.SnatchContent.PROVINCE;

/**
 * 全国公共资源交易平台（广东省）http://www.gdggzy.org.cn/
 * 政府采购
 * 工程建设
 * Created by Administrator on 2018/3/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})

@Component
@JobHander(value = "GuangDongquanguoggzy")
public class GuangDongquanguoggzy extends BaseSnatch {


    private static final String source = "guangd";

    @Override
    @Test
    public void run() throws Exception {
        snatchTask();
    }

    public void snatchTask () throws Exception {
        String[] postUrls = {
                "http://www.gdggzy.org.cn/prip-portal-web/main/viewList.do?types=", // 政府采购
                "http://www.gdggzy.org.cn/prip-portal-web/main/viewList.do?types="  // 工程建设
        };
        String[] oneTypes = {"30011","30012","30013"};  // 招标、中标、更正
        String[] oneCatchTypes = {"1","2",GENG_ZHENG_TYPE};
        String[] twoTypes = {"200122","200123","200125","200130"};  // 招标、更正、中标、中标信息
        String[] twoCatchTypes = {"1",GENG_ZHENG_TYPE,"2","2"};
        String[] citys = {"gd","gz","sz","zh","st","sg","fs","jm","zj","mm","zq","hz","mz","sw","hy","yj","qy","dg","zs","cz","jy","yf","sd"}; // 广东城市代码

        for (int i = 0; i < postUrls.length; i++) {
            String[] types = i==0?oneTypes:twoTypes;
            String[] catchTypes = i==0?oneCatchTypes:twoCatchTypes;
            for (int j = 0; j < types.length; j++) {
                for (int k = 0; k < citys.length; k++) {
                    int page = 1;
                    int pageTemp = 0;
                    Connection conn = null;
                    Document doc = null;
                    String url = postUrls[i] + types[j] + "&citys=" + citys[k];
                    String snatchNumber = SnatchUtils.makeSnatchNumber();
                    try {
                        super.queryBeforeSnatchState(url);
                        for (int pagelist = 1; pagelist <= page; pagelist++) {
                            if(pageTemp>0 && page>pageTemp){
                                break;
                            }

                            conn = Jsoup.connect(postUrls[i] + Math.random()).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                            conn.data("city",citys[k]);
                            conn.data("typeId",types[j]);
                            conn.data("pageSize","20");
                            conn.data("currPage",pagelist+"");
                            conn.data("noHeadAndFoot","");
                            conn.data("title","");
                            conn.data("startTime","");
                            conn.data("endTime","");
                            doc = conn.post();
                            if (pagelist == 1) {
                                // 获取总页数
                                int count = Integer.valueOf(doc.select("#TestView_pageableDiv").first().attr("totalSize"));
                                if (count % 20 == 0) {
                                    page = count / 20;
                                } else {
                                    page = (count / 20) + 1;
                                }
                                pageTemp = page;
                                SnatchLogger.debug("=======>>>>>>>>>>>>"+ (i==0?"政府采购":"工程建设") + "  ---- city:" + citys[k] + "  ---- catchType:" + catchTypes[j] + "<<<<<<<<<<<<=======");
                                SnatchLogger.debug("总"+page+"页");
                                page = computeTotalPage(page,LAST_ALLPAGE);
                            }

                            SnatchLogger.debug("第"+pagelist+"页");

                            Elements trs = doc.select(".list_box").first().select("ul");
                            for (int row = 0; row < trs.size(); row++) {
                                String conturl = trs.get(row).select("a").first().absUrl("href");
                                if (SnatchUtils.isNotNull(conturl)) {
                                    Notice notice = new Notice();
                                    String urlId = conturl.substring(conturl.indexOf("portalNewsId=") + 13);
                                    String title = trs.get(row).select("a").first().attr("title");
                                    String date = trs.get(row).select("li").last().text().trim();
                                    notice.setProvince("广东省");
                                    notice.setProvinceCode("gds");
                                    notice = setCity(notice,k);
                                    notice.setCatchType(catchTypes[j]);
                                    notice.setNoticeType(i==0?"政府采购":"工程建设");
                                    notice.setUrl(conturl);
                                    notice.setTitle(title);
                                    notice.setOpendate(date);
                                    notice.setSnatchNumber(snatchNumber);
                                    notice.setAreaRank(PROVINCE);
                                    notice.setSource(source);
                                    notice.setSyncUrl("http://www.gdggzy.org.cn/prip-portal-web/PortalNews/portalNewsConten.do?id=" + urlId);

                                    if (notice.getNoticeType().contains("采购")) {
                                        page = govDetailHandle(notice, pagelist, page, row, trs.size());
                                    } else {
                                        page = detailHandle(notice, pagelist, page, row, trs.size());
                                    }
                                }
                            }
                            if(pagelist==page){
                                page = turnPageEstimate(page);
                            }
                        }
                        super.saveAllPageIncrement(url);
                    }finally {
                        super.clearClassParam();
                    }
                }
            }
        }
    }

    @Override
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Document docCount = Jsoup.parse(new URL(href).openStream(), "utf-8", href);
        String content = docCount.select("#portalNewsContent").first().html();
        notice.setContent(content);
        notice = SnatchUtils.setCatchTypeByTitle(notice,notice.getTitle());
        if (SnatchUtils.isNull(notice.getCatchType())) {
            notice.setCatchType(catchType);
        } else {
            SnatchUtils.judgeCatchType(notice,notice.getCatchType(),catchType);
        }
        return notice;
    }

    private Notice setCity (Notice notice,int i) {
        if (i > 0) {
            Dimension dm = new Dimension();
            String city = "";
            String cityCode = "";
            switch (i) {
                case 1 :
                    city = "广州市";
                    cityCode = "gzs";
                    break;
                case 2:
                    city = "深圳市";
                    cityCode = "shzs";
                    break;
                case 3:
                    city = "珠海市";
                    cityCode = "zhs";
                    break;
                case 4:
                    city = "汕头市";
                    cityCode = "sts";
                    break;
                case 5:
                    city = "韶关市";
                    cityCode = "sgs";
                    break;
                case 6:
                    city = "佛山市";
                    cityCode = "foss";
                    break;
                case 7:
                    city = "江门市";
                    cityCode = "jms";
                    break;
                case 8:
                    city = "湛江市";
                    cityCode = "zhjs";
                    break;
                case 9:
                    city = "茂名市";
                    cityCode = "mms";
                    break;
                case 10:
                    city = "肇庆市";
                    cityCode = "zqs";
                    break;
                case 11:
                    city = "惠州市";
                    cityCode = "huzs";
                    break;
                case 12:
                    city = "梅州市";
                    cityCode = "mzs";
                    break;
                case 13:
                    city = "汕尾市";
                    cityCode = "sws";
                    break;
                case 14:
                    city = "河源市";
                    cityCode = "hys";
                    break;
                case 15:
                    city = "阳江市";
                    cityCode = "yjs";
                    break;
                case 16:
                    city = "清远市";
                    cityCode = "qingys";
                    break;
                case 17:
                    city = "东莞市";
                    cityCode = "dgs";
                    break;
                case 18:
                    city = "中山市";
                    cityCode = "zhss";
                    break;
                case 19:
                    city = "潮州市";
                    cityCode = "chzs";
                    break;
                case 20:
                    city = "揭阳市";
                    cityCode = "jys";
                    break;
                case 21:
                    city = "云浮市";
                    cityCode = "yfs";
                    break;
//                case 22:
//                    city = "顺德区";
//                    cityCode = "";
//                    break;
                default:
                    break;
            }
            dm.setProjDq(city);
            notice.setDimension(dm);
            notice.setCity(city);
            notice.setCityCode(cityCode);
        }
        return notice;
    }


}
