package com.snatch.model;

import java.io.Serializable;

/**
 * 维度信息
 * Created by maofeng on 2017/12/26.
 */
public class Dimension implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer noticeId;
    private String catchType;   // 公告类别
    private String cert;        // 资质
    private String projDq;      // 项目地区（市级）
    private String projXs;      // 项目县市 （县级）
    private String projType;    // 项目类型
    private String projSum;     // 项目金额
    private String pbMode;      // 评标办法
    private String bmStartDate; // 报名开始时间
    private String bmEndDate;   // 报名结束时间
    private String bmEndTime;   // 报名结束时间点
    private String bmSite;      // 报名地点
    private String tbAssureSum; // 投标保证金
    private String lyAssureSum; // 履约保证金
    private String slProveSum;  // 其他保证金
    private String tbAssureEndDate; // 投标保证金截止时间
    private String tbAssureEndTime; // 投标保证金截止时点
    private String assureEndDate;   // 保证金截止时间
    private String assureEndTime;   // 保证金截止时点
    private String tbEndDate;       // 投标截止时间
    private String tbEndTime;       // 投标截止时点
    private String kbSite;          // 开标地点
    private String registrationForm;// 报名方式
    private String block;           // 标段信息
    private String zbName;          // 招标人
    private String zbContactMan;    // 招标联系人
    private String zbContactWay;    // 招标联系方式
    private String dlName;          // 代理人
    private String dlContactMan;    // 代理联系人
    private String dlContactWay;    // 代理联系方式
    private String oneName;         // 第一中标候选人
    private String twoName;         // 第二中标候选人
    private String threeName;       // 第三中标候选人
    private String kbStaffAsk;      // 开标人员
    private String oneOffer;        // 第一中标报价
    private String twoOffer;        // 第二中标报价
    private String threeOffer;      // 第三中标报价
    private String projectTimeLimit;// 项目工期
    private String oneProjDuty;     // 项目负责人
    private String fileCost;        // 标书费
    private String file_url;        // 附件url 多个url逗号隔开
    private String relation_url;    // 相关公告url 多个url逗号隔开
    private String relation_title;  // 相关公告url 对应的标题  多个url逗号隔开

    public String getRelation_title() {
        return relation_title;
    }
    public void setRelation_title(String relation_title) {
        this.relation_title = relation_title;
    }
    public void setTwoOffer(String twoOffer) {
        this.twoOffer = twoOffer;
    }
    public void setThreeOffer(String threeOffer) {
        this.threeOffer = threeOffer;
    }
    public String getTwoOffer() {
        return twoOffer;
    }
    public String getThreeOffer() {
        return threeOffer;
    }
    public void setFileCost(String fileCost) {
        this.fileCost = fileCost;
    }
    public String getFileCost() {
        return fileCost;
    }
    public void setOneOffer(String oneOffer) {
        this.oneOffer = oneOffer;
    }
    public void setProjectTimeLimit(String projectTimeLimit) {
        this.projectTimeLimit = projectTimeLimit;
    }
    public void setOneProjDuty(String oneProjDuty) {
        this.oneProjDuty = oneProjDuty;
    }
    public String getOneOffer() {
        return oneOffer;
    }
    public String getProjectTimeLimit() {
        return projectTimeLimit;
    }
    public String getOneProjDuty() {
        return oneProjDuty;
    }
    public String getKbStaffAsk() {
        return kbStaffAsk;
    }
    public void setKbStaffAsk(String kbStaffAsk) {
        this.kbStaffAsk = kbStaffAsk;
    }
    public String getCert() {
        return cert;
    }
    public void setCert(String cert) {
        this.cert = cert;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public void setNoticeId(Integer noticeId) {
        this.noticeId = noticeId;
    }
    public void setCatchType(String catchType) {
        this.catchType = catchType;
    }
    public void setProjDq(String projDq) {
        this.projDq = projDq;
    }
    public void setProjXs(String projXs) {
        this.projXs = projXs;
    }
    public void setProjType(String projType) {
        this.projType = projType;
    }
    public void setProjSum(String projSum) {
        this.projSum = projSum;
    }
    public void setPbMode(String pbMode) {
        this.pbMode = pbMode;
    }
    public void setBmStartDate(String bmStartDate) {
        this.bmStartDate = bmStartDate;
    }
    public void setBmEndDate(String bmEndDate) {
        this.bmEndDate = bmEndDate;
    }
    public void setBmEndTime(String bmEndTime) {
        this.bmEndTime = bmEndTime;
    }
    public void setBmSite(String bmSite) {
        this.bmSite = bmSite;
    }
    public void setTbAssureSum(String tbAssureSum) {
        this.tbAssureSum = tbAssureSum;
    }
    public void setLyAssureSum(String lyAssureSum) {
        this.lyAssureSum = lyAssureSum;
    }
    public void setSlProveSum(String slProveSum) {
        this.slProveSum = slProveSum;
    }
    public void setTbAssureEndDate(String tbAssureEndDate) {
        this.tbAssureEndDate = tbAssureEndDate;
    }
    public void setTbAssureEndTime(String tbAssureEndTime) {
        this.tbAssureEndTime = tbAssureEndTime;
    }
    public void setAssureEndDate(String assureEndDate) {
        this.assureEndDate = assureEndDate;
    }
    public void setAssureEndTime(String assureEndTime) {
        this.assureEndTime = assureEndTime;
    }
    public void setTbEndDate(String tbEndDate) {
        this.tbEndDate = tbEndDate;
    }
    public void setTbEndTime(String tbEndTime) {
        this.tbEndTime = tbEndTime;
    }
    public void setKbSite(String kbSite) {
        this.kbSite = kbSite;
    }
    public void setRegistrationForm(String registrationForm) {
        this.registrationForm = registrationForm;
    }
    public void setBlock(String block) {
        this.block = block;
    }
    public void setZbName(String zbName) {
        this.zbName = zbName;
    }
    public void setZbContactMan(String zbContactMan) {
        this.zbContactMan = zbContactMan;
    }
    public void setZbContactWay(String zbContactWay) {
        this.zbContactWay = zbContactWay;
    }
    public void setDlName(String dlName) {
        this.dlName = dlName;
    }
    public void setDlContactMan(String dlContactMan) {
        this.dlContactMan = dlContactMan;
    }
    public void setDlContactWay(String dlContactWay) {
        this.dlContactWay = dlContactWay;
    }
    public void setOneName(String oneName) {
        this.oneName = oneName;
    }
    public void setTwoName(String twoName) {
        this.twoName = twoName;
    }
    public void setThreeName(String threeName) {
        this.threeName = threeName;
    }
    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }
    public void setRelation_url(String relation_url) {
        this.relation_url = relation_url;
    }
    public Integer getId() {
        return id;
    }
    public Integer getNoticeId() {
        return noticeId;
    }
    public String getCatchType() {
        return catchType;
    }
    public String getProjDq() {
        return projDq;
    }
    public String getProjXs() {
        return projXs;
    }
    public String getProjType() {
        return projType;
    }
    public String getProjSum() {
        return projSum;
    }
    public String getPbMode() {
        return pbMode;
    }
    public String getBmStartDate() {
        return bmStartDate;
    }
    public String getBmEndDate() {
        return bmEndDate;
    }
    public String getBmEndTime() {
        return bmEndTime;
    }
    public String getBmSite() {
        return bmSite;
    }
    public String getTbAssureSum() {
        return tbAssureSum;
    }
    public String getLyAssureSum() {
        return lyAssureSum;
    }
    public String getSlProveSum() {
        return slProveSum;
    }
    public String getTbAssureEndDate() {
        return tbAssureEndDate;
    }
    public String getTbAssureEndTime() {
        return tbAssureEndTime;
    }
    public String getAssureEndDate() {
        return assureEndDate;
    }
    public String getAssureEndTime() {
        return assureEndTime;
    }
    public String getTbEndDate() {
        return tbEndDate;
    }
    public String getTbEndTime() {
        return tbEndTime;
    }
    public String getKbSite() {
        return kbSite;
    }
    public String getRegistrationForm() {
        return registrationForm;
    }
    public String getBlock() {
        return block;
    }
    public String getZbName() {
        return zbName;
    }
    public String getZbContactMan() {
        return zbContactMan;
    }
    public String getZbContactWay() {
        return zbContactWay;
    }
    public String getDlName() {
        return dlName;
    }
    public String getDlContactMan() {
        return dlContactMan;
    }
    public String getDlContactWay() {
        return dlContactWay;
    }
    public String getOneName() {
        return oneName;
    }
    public String getTwoName() {
        return twoName;
    }
    public String getThreeName() {
        return threeName;
    }
    public String getFile_url() {
        return file_url;
    }
    public String getRelation_url() {
        return relation_url;
    }
}
