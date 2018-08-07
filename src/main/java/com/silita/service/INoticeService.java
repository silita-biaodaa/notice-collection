package com.silita.service;

import com.snatch.model.Notice;
import com.snatch.model.PageIncrement;
import com.snatch.model.SnatchException;
import com.snatch.model.TbSnatchStatistics;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

public interface INoticeService {

    /**
     * 根据url获取最后的抓取信息
     * @param url
     * @return
     */
    PageIncrement getLastPageIncrement(String url);

    /**
     * 插入抓取url增量表
     * @param allPage 本次抓取公告总页数
     * @param LAST_ALLPAGE 上次抓取公告总页数
     * @param url url
     */
    void insertIncrementByAllPage(int allPage, int LAST_ALLPAGE, String openDate, String url);

    /**
     * 根据url、open判断公告是否存在
     * @return
     */
    Integer getNoticeCountByOpenDateAndUrl(Notice notice, String tableName);

    /**
     * 数据添加到redis、mysql、kafka
     * @param notice
     */
    void insertNoticeDate(Notice notice);

    /**
     * 表不存在则创建表
     * @param tableName
     */
    void createNoticeByTableName(String tableName);

    /**
     * 添加异常信息
     * @param snatchException
     */
    void insertSnatchException(SnatchException snatchException);

    /**
     * 更新异常公告状态
     * @param url
     */
    void updateSnatchExceptionStatus(String url);

    /**
     * 获取全部资质别名
     * @return
     */
    @Cacheable(value="allZhCache")
    List<Map<String, Object>> listAllZh();

    /**
     *
     * @param siteClassify
     * @return
     */
    Integer getSnatchExceptionTotal(String siteClassify);

    /**
     * 插入异常统计表
     * @param snatchStatistics
     */
    void insertSnatchStatistics(TbSnatchStatistics snatchStatistics);

    /**
     * 根据抓取时间统计各分类抓取个数
     * @param params
     * @return
     */
    Map<String, Number> getNoticeTotalBySnatchDate(Map params);

    /**
     * 根据异常类获取异常信息
     * @return
     */
    List<SnatchException> listSnatchExceptionByExClass(String exClass);

    /**
     *
     * @param province
     * @return
     */
    @Cacheable(value = "cityCode")
    List<Map<String,Object>> querysCityCode (String province);
}
