package com.snatch.model;

import java.io.Serializable;

public class EsNotice implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String uuid;
	private String url;
	private String title;
	private String openDate;
	private String content;
	private String province;
	private String city;
	private String county;
	private Integer type;
	private Integer rank;
	private Integer redisId;
	private Integer websitePlanId;
	private String tableName;
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	public Integer getRedisId() {
		return redisId;
	}
	public void setRedisId(Integer redisId) {
		this.redisId = redisId;
	}
	public Integer getWebsitePlanId() {
		return websitePlanId;
	}
	public void setWebsitePlanId(Integer websitePlanId) {
		this.websitePlanId = websitePlanId;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
}
