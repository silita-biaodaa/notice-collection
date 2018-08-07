package com.silita.dao;

import com.snatch.model.Dimension;
import com.snatch.model.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface NoticeMapper {

    /**
     * 根据表名称判断表是否存在
     * @param tableName
     * @return
     */
    public Integer checkTableIsExist(@Param("tableName")String tableName);

    /**
     * 根据省份名称创建表
     * @param tableName
     * @return
     */
    public void createNoticeTable(@Param("tableName")String tableName);

    /**
     * 插入抓取信息表
     * @param notice
     * @return
     */
    public void insertNotice(@Param("notice")Notice notice, @Param("tableName")String tableName, @Param("dimension")Dimension dimension);

    /**
     * 根据url&openDate获取公告个数
     * @param params
     * @return
     */
    public Integer getNoticeTotalByUrlAndOpenDate(Map params);

    /**
     * 根据抓取时间统计各分类抓取公告个数
     * @return
     */
    public Map<String, Number> getNoticeTotalsByUrlAndSnatchDate(Map params);
}
