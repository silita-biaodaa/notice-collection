package com.snatch.model;

/**
 * Created by dh on 2017/6/16.
 */
public class SnatchUrl {
    private String url;

    private String noticeType;

    private String notcieCatchType;

    private String snatchNumber;

    public String getSnatchNumber() {
        return snatchNumber;
    }

    public void setSnatchNumber(String snatchNumber) {
        this.snatchNumber = snatchNumber;
    }

    public String getNotcieCatchType(){
        return notcieCatchType;
    }

    public void setNotcieCatchType(String notcieCatchType){
        this.notcieCatchType = notcieCatchType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }
}
