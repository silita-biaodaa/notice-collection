package com.silita.china.country;

import com.silita.service.IAreaService;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.snatch.common.BaseSnatch;
import com.snatch.common.SnatchContent;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.model.Area;
import com.xxl.job.core.handler.annotation.JobHander;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全国招投标公共服务平台
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:spring-test.xml"
})
@Component
@JobHander(value="ChinaTBPSP")
public class ChinaTBPSP extends BaseSnatch {


    @Autowired
    private IAreaService areaService;
    private final String pageEncoding = "utf-8";

    /**
     * 入库路由字段
     */
    private final String source = "country";

    @Override
    @Test
    public void run() throws Exception {
        String[] urls ={"http://bulletin.cebpubservice.com/xxfbcms/category/qualifyBulletinList.html",
                        "http://bulletin.cebpubservice.com/xxfbcms/index.html",
                        "http://bulletin.cebpubservice.com/xxfbcms/category/candidateBulletinList.html",
                        "http://bulletin.cebpubservice.com/xxfbcms/category/resultBulletinList.html",
                        "http://bulletin.cebpubservice.com/xxfbcms/category/changeBulletinList.html"};
        String[] categoryIds={"92","88","91","90","89"};
        String[] tabNames = {"资格预审公告","招标公告","中标候选人公示","中标结果公示","更正公告公示"};
        for(int i=0;i<urls.length;i++) {
            sntachTask(urls[i], categoryIds[i], tabNames[i]);
        }
    }

    /**
     * 获取分类页面的总页数
     * @param doc
     * @return
     */
    private int fetchTotalCount( Document doc){
        int totalPage;
        try {
            String count = doc.select(".pagination").select("label").first().text().trim();
            totalPage = Integer.parseInt(count);
            SnatchLogger.debug("总" + totalPage + "页");
        }catch (Exception e){
            SnatchLogger.warn(doc.select(".pagination").outerHtml());
            throw e;
        }
        return totalPage;
    }

    /**
     * 识别CatchType
     * @param categoryId
     * @return
     */
    private String regCatchType(String categoryId){
        switch(categoryId){
            case "92": return SnatchContent.PRE_QUALIFICATION;
            case "88": return SnatchContent.ZHAO_BIAO_TYPE;
            case "91": return SnatchContent.ZHONG_BIAO_TYPE;
            case "90": return SnatchContent.ZHONG_BIAO_TYPE;
            case "89": return SnatchContent.GENG_ZHENG_TYPE;
            default: return null;
        }
    }

    /**
     * 识别更正公告中的关联公告
     * @param el
     * @param dimension
     */
    private void ObtainRelation(Element el,Dimension dimension){
        Elements aEl = el.select("a");
        if(aEl!=null && aEl.hasText()){
            String href = aEl.attr("href");
            String title = aEl.attr("title");
            if(href !=null) {
                dimension.setRelation_title(title);
                dimension.setRelation_url(href);
            }
        }
    }

    /**
     * 识别地区信息
     * @param regionEl
     * @return
     */
    private String regionTextObtain(Element regionEl){
        String result = null;
        Elements targetEls=null;
        targetEls =  regionEl.select("span");
        if(targetEls==null) {
            targetEls = regionEl.select("td");
        }

        String regionTxt = null;
        if(targetEls==null){
            regionTxt = regionEl.text();
        }else{
            regionTxt = targetEls.text();
        }
        if(regionTxt !=null && !regionTxt.trim().equals("")){
            result= regionTxt.trim();
            String[] exlStr = {"【","】"};
            result=SnatchUtils.excludeString(result,exlStr).trim();
        }
        return result;
    }

    /**
     * 获取开标时间维度
     * @param el
     * @param dimension
     */
    private void obtainOpenTime(Element el,Dimension dimension){
        String openTime = el.attr("id").trim();
        if(openTime!=null && openTime.length()>15) {
            String tbEndDate = openTime.substring(0,10);
            String tbEndTime = openTime.substring(11);
            dimension.setTbEndDate(tbEndDate);
            dimension.setTbEndTime(tbEndTime.trim());
        }
    }

    /**
     * 执行单个分类的抓取动作
     * @param staticUrl
     * @throws Exception
     */
    public void sntachTask(String staticUrl,String categoryId,String tabName) throws Exception{
        int page = 1;
        super.queryBeforeSnatchState(staticUrl);//查询url前次抓取的情况（最大页数与公示时间）
        String dyUrl = staticUrl;
        try {
            String currentCatchType =  regCatchType(categoryId);

            Map argMap = new HashMap<>();
            for(int pagelist=1;pagelist<=page;pagelist++){
                argMap.clear();
                argMap.put("page",String.valueOf(pagelist));
                argMap.put("categoryId",categoryId);
                if(pagelist==1){
                    argMap.put("dates","300");
                    argMap.put("tabName",tabName);
                }else{
                    argMap.put("searchDate","1993-03-06");
                    argMap.put("dates","300");
                    argMap.put("word","");
                    argMap.put("industryName","");
                    argMap.put("area","");
                    argMap.put("status","");
                    argMap.put("publishMedia","");
                    argMap.put("sourceInfo","");
                }
                SnatchLogger.debug("=================第"+pagelist+"页===================");
                Connection conn = Jsoup.connect(dyUrl).data(argMap).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true);
                Document doc = conn.get();
                if (page == 1) {
                    page = computeTotalPage(fetchTotalCount(doc),LAST_ALLPAGE);
                }

                //抓取列表数据
                Elements trs = doc.select(".table_text").select("tr");
                for (int row = 1; row < trs.size(); row++) {
                    SnatchLogger.debug("row:"+row);
                    Elements tdEls =trs.get(row).select("td");
                    String detailUrl = tdEls.get(0).select("a").first().absUrl("href");

                    if (!"".equals(detailUrl)) {
                        Area area =null;
                        String region=null;
                        String channelSrc=null;
                        Dimension dimension = new Dimension();
                        String title = tdEls.get(0).select("a").first().text().trim();
                        String date =  null;
                        if(!categoryId.equals("89")){//非【更正公告公示】类
                            String projType= tdEls.get(1).text().trim();
                            region = regionTextObtain(tdEls.get(2));
                            channelSrc= tdEls.get(3).text().trim();
                            date =  tdEls.get(4).text().trim();
                            dimension.setProjType(projType);
                            if(categoryId.equals("88") || categoryId.equals("92")){//招标公告
                                obtainOpenTime(tdEls.get(5),dimension);
                            }
                        }else{//【更正公告公示】类
                            region=  regionTextObtain(tdEls.get(1));
                            channelSrc= tdEls.get(2).text().trim();
                            date =  tdEls.get(3).text().trim();
                            ObtainRelation(tdEls.get(4),dimension);
                        }

                        //获取地区信息
                        if(region !=null && !region.equals("")){
                            SnatchLogger.debug("region:"+region);
                            List<Area> areaList = areaService.queryProvArea(region);
                            if(areaList!=null && areaList.size()>=1) {
                                area = areaList.get(0);
                            }else{
                                areaList = areaService.queryProvArea(channelSrc.substring(0,2));
                                if(areaList!=null && areaList.size()>=1) {
                                    area = areaList.get(0);
                                }
                            }
                            if(area==null || area.equals("")){
                                SnatchLogger.warn("地区信息匹配失败！[area:"+area+"][region:"+region+"][channelSrc:"+channelSrc+"][page:"+page+"][url:"+staticUrl+"]");
                            }
                        }else{
                            SnatchLogger.warn("地区字符获取失败！[region:"+region+"][page:"+page+"][url:"+staticUrl+"]");
                        }

                        Notice notice = new Notice();

                        //地区编码信息设置
                        if(area!=null) {
                            notice.setProvince(area.getName());
                            notice.setProvinceCode(area.getName_abbr());
                        }
                        notice.setUrl(detailUrl);
                        notice.setTitle(title);
                        notice.setOpendate(date);
                        notice.setCatchType(currentCatchType);
                        notice.setSource(source);
                        notice.setAreaRank(SnatchContent.PROVINCE);
                        notice.setDimension(dimension);
                        //详情信息入库，获取增量页数
                        page =detailHandle(notice,pagelist,page,row,trs.size());
                    }
                }
                //===入库代码====
                if(pagelist==page){
                    page = turnPageEstimate(page);
                }
            }
            //===入库代码====
            super.saveAllPageIncrement(staticUrl);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            }
            SnatchLogger.error(e);
        }
    }

    @Override
    /**
     * 获取详情页内容
     */
    public Notice detail(String href, Notice notice, String catchType) throws Exception {
        Thread.sleep(500);
        Document docCount = probeHttpDetail(href,pageEncoding,".mian_list_03");
        Element mian03 =docCount.select(".mian_list_03").first();
        if(mian03==null){
            throw new Exception("html内容抓取失败。");
        }
        String scriptTxt = mian03.getElementsByTag("script").get(0).data().toString();
        String swfFile =scriptTxt.substring(scriptTxt.indexOf("escape")+8,scriptTxt.indexOf("),")-1);
        notice.setContent(swfFile);

        Elements mian02List =docCount.select(".mian_list_02");
        String title = mian02List.select("h3").text();
        notice.setTitle(title);
        return notice;
    }


}
