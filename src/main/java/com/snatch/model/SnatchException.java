package com.snatch.model;

/**
 * Created by commons on 2017/3/7.
 */
public class SnatchException {

    private Integer id;                 //id
    private String exName;          	//异常名称
    private String exUrl;           	//异常网址
    private String exDesc;          	//异常描述
    private String exRank;          	//异常等级，1级：严重；2级：可处理；3级：可忽视。
    private String exTime;          	//发生异常时间
    private String noticeTitle;         //公告标题
    private String noticeOpendate;      //公告发布日期
    private String exClass;				//异常发生的类
    private String catchType;			//公告类型
    private Integer status;				//抓取状态
    
    private String provinceCode="";		//省code
    private String cityCode="";			//市code
    private String countyCode="";		//县code

	private String type=""; //采购，工程公告区分 0：采购 1：工程
	private String sityClassify;    //站点名称+分类url

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getCountyCode() {
		return countyCode;
	}

	public void setCountyCode(String countyCode) {
		this.countyCode = countyCode;
	}

	public String getExClass() {
		return exClass;
	}

	public void setExClass(String exClass) {
		this.exClass = exClass;
	}

	public String getCatchType() {
		return catchType;
	}

	public void setCatchType(String catchType) {
		this.catchType = catchType;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getNoticeTitle() {
		return noticeTitle;
	}

	public void setNoticeTitle(String noticeTitle) {
		this.noticeTitle = noticeTitle;
	}

	public String getNoticeOpendate() {
		return noticeOpendate;
	}

	public void setNoticeOpendate(String noticeOpendate) {
		this.noticeOpendate = noticeOpendate;
	}

	public String getExRank() {
		return exRank;
	}

	public void setExRank(String exRank) {
		this.exRank = exRank;
	}

	public String getExTime() {
        return exTime;
    }

    public void setExTime(String exTime) {
        this.exTime = exTime;
    }

    public String getExUrl() {
        return exUrl;
    }

    public void setExUrl(String exUrl) {
        this.exUrl = exUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExName() {
        return exName;
    }

    public void setExName(String exName) {
        this.exName = exName;
    }

    public String getExDesc() {
        return exDesc;
    }

    public void setExDesc(String exDesc) {
        this.exDesc = exDesc;
    }

	public String getSityClassify() {
		return sityClassify;
	}

	public void setSityClassify(String sityClassify) {
		this.sityClassify = sityClassify;
	}
}
