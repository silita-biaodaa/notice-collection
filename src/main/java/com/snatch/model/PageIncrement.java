package com.snatch.model;

import java.util.Date;

/**
 * Created by dh on 2017/8/23.
 */
public class PageIncrement {
    private int currentAllPage;

    private int lastAllPage;

    private String url;

    private Date catchDate;

    private String snatchOpendate;

    private String siteClassify;

    private String source;

    public int getCurrentAllPage() {
        return currentAllPage;
    }

    public void setCurrentAllPage(int currentAllPage) {
        this.currentAllPage = currentAllPage;
    }

    public int getLastAllPage() {
        return lastAllPage;
    }

    public void setLastAllPage(int lastAllPage) {
        this.lastAllPage = lastAllPage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCatchDate() {
        return catchDate;
    }

    public void setCatchDate(Date catchDate) {
        this.catchDate = catchDate;
    }

    public String getSnatchOpendate() {
        return snatchOpendate;
    }

    public void setSnatchOpendate(String snatchOpendate) {
        this.snatchOpendate = snatchOpendate;
    }

    public String getSiteClassify() {
        return siteClassify;
    }

    public void setSiteClassify(String siteClassify) {
        this.siteClassify = siteClassify;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
