package com.snatch.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by 91567 on 2018/5/9.
 */
public class TbSnatchStatistics implements Serializable {
    private static final long serialVersionUID = 1111L;

    private Integer pkid;
    private String source;
    private String siteName;
    private Integer noticeTotal;
    private Integer zhaobiaoTotal;
    private Integer zhongbiaoTotal;
    private Integer otherTotal;
    private Integer exceptionTotal;
    private Integer urlTotal;
    private String classPageNum;
    private String classDateDifference;
    private String executeDate;
    private String siteDomainName;
    private Date created;
    private Date updated;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    public Integer getPkid() {
        return pkid;
    }
    public void setPkid(Integer pkid) {
        this.pkid = pkid;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getSiteName() {
        return siteName;
    }
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
    public Integer getNoticeTotal() {
        return noticeTotal;
    }
    public void setNoticeTotal(Integer noticeTotal) {
        this.noticeTotal = noticeTotal;
    }
    public Integer getZhaobiaoTotal() {
        return zhaobiaoTotal;
    }
    public void setZhaobiaoTotal(Integer zhaobiaoTotal) {
        this.zhaobiaoTotal = zhaobiaoTotal;
    }
    public Integer getZhongbiaoTotal() {
        return zhongbiaoTotal;
    }
    public void setZhongbiaoTotal(Integer zhongbiaoTotal) {
        this.zhongbiaoTotal = zhongbiaoTotal;
    }
    public Integer getOtherTotal() {
        return otherTotal;
    }
    public void setOtherTotal(Integer otherTotal) {
        this.otherTotal = otherTotal;
    }
    public Integer getExceptionTotal() {
        return exceptionTotal;
    }
    public void setExceptionTotal(Integer exceptionTotal) {
        this.exceptionTotal = exceptionTotal;
    }
    public Integer getUrlTotal() {
        return urlTotal;
    }
    public void setUrlTotal(Integer urlTotal) {
        this.urlTotal = urlTotal;
    }
    public String getClassPageNum() {
        return classPageNum;
    }
    public void setClassPageNum(String classPageNum) {
        this.classPageNum = classPageNum;
    }
    public String getClassDateDifference() {
        return classDateDifference;
    }
    public void setClassDateDifference(String classDateDifference) {
        this.classDateDifference = classDateDifference;
    }
    public String getExecuteDate() {
        return executeDate;
    }
    public void setExecuteDate(String executeDate) {
        this.executeDate = executeDate;
    }
    public String getSiteDomainName() {
        return siteDomainName;
    }
    public void setSiteDomainName(String siteDomainName) {
        this.siteDomainName = siteDomainName;
    }
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }
    public Date getUpdated() {
        return updated;
    }
    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
