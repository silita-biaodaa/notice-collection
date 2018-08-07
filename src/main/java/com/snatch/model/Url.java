package com.snatch.model;

/**
 * Created by liuqi on 2017/4/28 0028.
 */
public class Url {
    private String url;
    private String title;
    private String openDate;
    public Url(String noticeUrl, String title, String openDate) {
        this.url=noticeUrl;
        this.title=title;
        this.openDate=openDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String noticeUrl) {
        this.url = noticeUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }
}
