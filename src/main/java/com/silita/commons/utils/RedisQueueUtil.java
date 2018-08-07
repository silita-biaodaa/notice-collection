package com.silita.commons.utils;

import com.silita.commons.redisJMS.RedisQueue;
import com.snatch.common.jdbc.JdbcBase;
import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import com.snatch.model.SnatchException;

/**
 * Created by commons on 2017/3/9.
 * redis工具类
 */
public class RedisQueueUtil extends JdbcBase{

    public static final String SPLIT_STRING = "@@####@@";
    public static final String DEFAULT_STRING = "___";

    /**
     * 将SnatchException对象为null和“”的属性赋___初值
     */
    public static  SnatchException initAttrOfException(SnatchException se){
        if (se.getExName() == null || "".equals(se.getExName())){
            se.setExName(DEFAULT_STRING);
        }
        if (se.getExDesc() == null || "".equals(se.getExDesc())){
            se.setExDesc(DEFAULT_STRING);
        }
        if (se.getExUrl() == null || "".equals(se.getExUrl())){
            se.setExUrl(DEFAULT_STRING);
        }
        if (se.getExRank() == null || "".equals(se.getExRank())){
            se.setExRank(DEFAULT_STRING);
        }
        if (se.getExTime() == null || "".equals(se.getExTime())){
            se.setExTime(DEFAULT_STRING);
        }
        return se;
    }

    /**
     * 将Notice对象为null和“”的属性赋初值（占位符）
     * @param no 公告对象
     * @return 赋初值后的公告对象
     */
    public static  Notice initAttrOfNotice(Notice no){
        if (no.getProvince() == null || "".equals(no.getProvince())){
            no.setProvince(DEFAULT_STRING);
        }
        if (no.getCity() == null || "".equals(no.getCity())){
            no.setCity(DEFAULT_STRING);
        }
        if (no.getCounty() == null || "".equals(no.getCounty())){
            no.setCounty(DEFAULT_STRING);
        }
        if (no.getAreaCode() == null || "".equals(no.getAreaCode())){
            no.setAreaCode(DEFAULT_STRING);
        }
        if (no.getTitle() == null || "".equals(no.getTitle())){
            no.setTitle(DEFAULT_STRING);
        }
        if (no.getOpendate() == null || "".equals(no.getOpendate())){
            no.setOpendate(DEFAULT_STRING);
        }
        if (no.getContent() == null || "".equals(no.getContent())){
            no.setContent(DEFAULT_STRING);
        }
        if (no.getUrl() == null || "".equals(no.getUrl())){
            no.setUrl(DEFAULT_STRING);
        }
        if (no.getPdfURL() == null || "".equals(no.getPdfURL())){
            no.setPdfURL(DEFAULT_STRING);
        }
        if (no.getPhotoUrl() == null || "".equals(no.getPhotoUrl())){
            no.setPhotoUrl(DEFAULT_STRING);
        }
        if (no.getCatchType() == null || "".equals(no.getCatchType())){
            no.setCatchType(DEFAULT_STRING);
        }
        if (no.getCatchTime() == null || "".equals(no.getCatchTime())){
            no.setCatchTime(DEFAULT_STRING);
        }
        if (no.getNoticeType() == null || "".equals(no.getNoticeType())){
            no.setNoticeType(DEFAULT_STRING);
        }
        if (no.getProvinceCode() == null || "".equals(no.getProvinceCode())){
            no.setProvinceCode(DEFAULT_STRING);
        }
        if (no.getCityCode() == null || "".equals(no.getCityCode())){
            no.setCityCode(DEFAULT_STRING);
        }
        if (no.getCountyCode() == null || "".equals(no.getCountyCode())){
            no.setCountyCode(DEFAULT_STRING);
        }
        if (no.getType() == null || "".equals(no.getType())){
            no.setType(DEFAULT_STRING);
        }
        if (no.getSnatchNumber() == null || "".equals(no.getSnatchNumber())){
            no.setSnatchNumber(DEFAULT_STRING);
        }
        if (no.getAreaRank() == null || "".equals(no.getAreaRank())){
            no.setAreaRank(DEFAULT_STRING);
        }
        Dimension dm = no.getDimension();
        if (dm == null) {
            dm = new Dimension();
            dm.setCert(DEFAULT_STRING);
            dm.setProjDq(DEFAULT_STRING);
            dm.setProjXs(DEFAULT_STRING);
            dm.setProjSum(DEFAULT_STRING);
            dm.setPbMode(DEFAULT_STRING);
            dm.setTbAssureSum(DEFAULT_STRING);
            dm.setBmEndDate(DEFAULT_STRING);
            dm.setBmEndTime(DEFAULT_STRING);
            dm.setBmSite(DEFAULT_STRING);
            dm.setKbStaffAsk(DEFAULT_STRING);
            dm.setTbEndDate(DEFAULT_STRING);
            dm.setTbEndTime(DEFAULT_STRING);
            dm.setKbSite(DEFAULT_STRING);
            dm.setOneName(DEFAULT_STRING);
            dm.setTwoName(DEFAULT_STRING);
            dm.setThreeName(DEFAULT_STRING);
            dm.setOneOffer(DEFAULT_STRING);
            dm.setProjectTimeLimit(DEFAULT_STRING);
            dm.setOneProjDuty(DEFAULT_STRING);
            dm.setFile_url(DEFAULT_STRING);
            dm.setRelation_url(DEFAULT_STRING);
            dm.setZbName(DEFAULT_STRING);
            no.setDimension(dm);
        } else {
            if (dm.getCert() == null || "".equals(dm.getCert())) {
                dm.setCert(DEFAULT_STRING);
            }
            if (dm.getProjDq() == null || "".equals(dm.getProjDq())) {
                dm.setProjDq(DEFAULT_STRING);
            }
            if (dm.getProjXs() == null || "".equals(dm.getProjXs())) {
                dm.setProjXs(DEFAULT_STRING);
            }
            if (dm.getProjSum() == null || "".equals(dm.getProjSum())) {
                dm.setProjSum(DEFAULT_STRING);
            }
            if (dm.getPbMode() == null || "".equals(dm.getPbMode())) {
                dm.setPbMode(DEFAULT_STRING);
            }
            if (dm.getTbAssureSum() == null || "".equals(dm.getTbAssureSum())) {
                dm.setTbAssureSum(DEFAULT_STRING);
            }
            if (dm.getBmEndDate() == null || "".equals(dm.getBmEndDate())) {
                dm.setBmEndDate(DEFAULT_STRING);
            }
            if (dm.getBmEndTime() == null || "".equals(dm.getBmEndTime())) {
                dm.setBmEndTime(DEFAULT_STRING);
            }
            if (dm.getBmSite() == null || "".equals(dm.getBmSite())) {
                dm.setBmSite(DEFAULT_STRING);
            }
            if (dm.getKbStaffAsk() == null || "".equals(dm.getKbStaffAsk())) {
                dm.setKbStaffAsk(DEFAULT_STRING);
            }
            if (dm.getTbEndDate() == null || "".equals(dm.getTbEndDate())) {
                dm.setTbEndDate(DEFAULT_STRING);
            }
            if (dm.getTbEndTime() == null || "".equals(dm.getTbEndTime())) {
                dm.setTbEndTime(DEFAULT_STRING);
            }
            if (dm.getKbSite() == null || "".equals(dm.getKbSite())) {
                dm.setKbSite(DEFAULT_STRING);
            }
            if (dm.getOneName() == null || "".equals(dm.getOneName())) {
                dm.setOneName(DEFAULT_STRING);
            }
            if (dm.getTwoName() == null || "".equals(dm.getTwoName())) {
                dm.setTwoName(DEFAULT_STRING);
            }
            if (dm.getThreeName() == null || "".equals(dm.getThreeName())) {
                dm.setThreeName(DEFAULT_STRING);
            }
            if (dm.getOneOffer() == null || "".equals(dm.getOneOffer())) {
                dm.setOneOffer(DEFAULT_STRING);
            }
            if (dm.getProjectTimeLimit() == null || "".equals(dm.getProjectTimeLimit())) {
                dm.setProjectTimeLimit(DEFAULT_STRING);
            }
            if (dm.getOneProjDuty() == null || "".equals(dm.getOneProjDuty())) {
                dm.setOneProjDuty(DEFAULT_STRING);
            }
            if (dm.getFile_url() == null || "".equals(dm.getFile_url())) {
                dm.setFile_url(DEFAULT_STRING);
            }
            if (dm.getRelation_url() == null || "".equals(dm.getRelation_url())) {
                dm.setRelation_url(DEFAULT_STRING);
            }
            if (dm.getZbName() == null || "".equals(dm.getZbName())) {
                dm.setZbName(DEFAULT_STRING);
            }
            no.setDimension(dm);
        }
        return  no;
    }

    /**
     * 将notice对象拼接成字符串
     * @param no 公告对象 拼接数据源
     * @return 拼接之后的字符串
     */
    public static String getNoticeString(Notice no){
        no = initAttrOfNotice(no);
        Dimension dm = no.getDimension();
        String noticeStr = no.getAreaCode()                         //公告地区code
                + SPLIT_STRING + no.getProvince()                   //省
                + SPLIT_STRING + no.getTitle()                  	//公告标题
                + SPLIT_STRING + no.getOpendate()               	//公告公示时间
                + SPLIT_STRING + no.getContent()                	//公告内容
                + SPLIT_STRING + no.getUrl()                    	//公告url
                + SPLIT_STRING + no.getPdfURL()                 	//公告pdf的url
                + SPLIT_STRING + no.getPhotoUrl()               	//公告照片的url
                + SPLIT_STRING + no.getCatchType()              	//公告类型
                + SPLIT_STRING + no.getCatchTime()             		//公告抓取时间
                + SPLIT_STRING + no.getId()              			//id
                + SPLIT_STRING + no.getRedisId()              		//redis生成的唯一id
                + SPLIT_STRING + no.getCity()              			//市
                + SPLIT_STRING + no.getCounty()              		//县
                + SPLIT_STRING + no.getNoticeType()              	//公告类型
                + SPLIT_STRING + no.getProvinceCode()              	//省code码
                + SPLIT_STRING + no.getCityCode()              		//市code码
                + SPLIT_STRING + no.getCountyCode()              	//县code码
                + SPLIT_STRING + no.getSnatchNumber()               //抓取批次
                + SPLIT_STRING + no.getType()                       //公告类型(普通/采购)
                + SPLIT_STRING + no.getAreaRank()                   // 地区等级
                + SPLIT_STRING + dm.getCert()                       // 资质
                + SPLIT_STRING + dm.getProjDq()                     // 地区
                + SPLIT_STRING + dm.getProjXs()                     // 县市
                + SPLIT_STRING + dm.getProjSum()                    // 项目金额
                + SPLIT_STRING + dm.getPbMode()                     // 评标办法
                + SPLIT_STRING + dm.getTbAssureSum()                // 投标保证金
                + SPLIT_STRING + dm.getBmEndDate()                  // 报名结束日期
                + SPLIT_STRING + dm.getBmEndTime()                  // 报名结束时点
                + SPLIT_STRING + dm.getBmSite()                     // 报名地点
                + SPLIT_STRING + dm.getKbStaffAsk()                 // 开标人员
                + SPLIT_STRING + dm.getTbEndDate()                  // 投标结束日期
                + SPLIT_STRING + dm.getTbEndTime()                  // 投标结束时点
                + SPLIT_STRING + dm.getKbSite()                     // 开标地点
                + SPLIT_STRING + dm.getOneName()                    // 第一中标候选人
                + SPLIT_STRING + dm.getTwoName()                    // 第二中标候选人
                + SPLIT_STRING + dm.getThreeName()                  // 第三中标候选人
                + SPLIT_STRING + dm.getFile_url()                   // 附件url
                + SPLIT_STRING + dm.getRelation_url()               // 相关公告url
                + SPLIT_STRING + dm.getOneOffer()                   // 报价
                + SPLIT_STRING + dm.getProjectTimeLimit()           // 项目工期
                + SPLIT_STRING + dm.getOneProjDuty()                // 项目负责人
                + SPLIT_STRING + dm.getZbName();                    // 招标人
//        StringBuffer buffer = new StringBuffer("");
//	        buffer.append(no.getAreaCode());
//	        buffer.append(SPLIT_STRING + no.getProvince());
//	        buffer.append(SPLIT_STRING + no.getTitle());
//	        buffer.append(SPLIT_STRING + no.getOpendate());
//	        buffer.append(SPLIT_STRING + no.getContent());
//	        buffer.append(SPLIT_STRING + no.getUrl());
//	        buffer.append(SPLIT_STRING + no.getPdfURL());
//	        buffer.append(SPLIT_STRING + no.getPhotoUrl());
//	        buffer.append(SPLIT_STRING + no.getCatchType());
//	        buffer.append(SPLIT_STRING + no.getCatchTime());
//	        buffer.append(SPLIT_STRING + no.getId());
//	        buffer.append(SPLIT_STRING + no.getCity());
//	        buffer.append(SPLIT_STRING + no.getCounty());
//	        buffer.append(SPLIT_STRING + no.getNoticeType());
//	        buffer.append(SPLIT_STRING + no.getProvinceCode());
//	        buffer.append(SPLIT_STRING + no.getCityCode());
//	        buffer.append(SPLIT_STRING + no.getCountyCode());
//        return buffer.toString();
        return noticeStr;
    }

    /**
     *push公告数据到redis队列
     */
    public static void pushToRedisQueue(Notice no,RedisQueue<Notice> redisQueue) throws InterruptedException{
//        String noticestr = getNoticeString(no);         //获取拼接的公告数据
//        System.out.println(noticestr);RedisQueue
        redisQueue.pushFromHead(no);             //push到队列
    }

//    /**
//     * 保存公告数据到arangoDB
//     */
//    public static void insertToArangoOfNotice(Notice no) {
//        InputStream in = Notice.class.getResourceAsStream("arangodb.properties");
//        ArangoDB arangoDB = new ArangoDB.Builder().loadProperties(in).build();
//
//        Collection<Notice> documents = new ArrayList<Notice>();
//        documents.add(no);
//        arangoDB.db("mishu_snatch").collection("notice").insertDocuments(documents);
//    }
//
//    /**
//     * 保存抓取数据异常到arangoDB
//     */
//    public static void saveSnatchExceptionOfArango(RedisTemplate redisTemplate, SnatchException se){
//        InputStream in = Notice.class.getResourceAsStream("arangodb.properties");
//        ArangoDB arangoDB = new ArangoDB.Builder().loadProperties(in).build();
//
//        Collection<SnatchException> documents = new ArrayList<SnatchException>();
//        documents.add(se);
//        arangoDB.db("mishu_snatch").collection("snatch_exception").insertDocuments(documents);
//    }
}
