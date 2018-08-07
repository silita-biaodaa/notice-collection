package com.silita.dao;

import com.snatch.model.SnatchException;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SnatchExceptionMapper {

    /**
     *
     * @param exception
     */
    void insertSnatchException(SnatchException exception);

    /**
     *
     * @param exception
     */
    void updateSnatchException(SnatchException exception);

    /**
     *
     * @param url
     */
    Integer getSnatchExceptionTotalByUrl(@Param("url")String url);

    /**
     *
     * @param url
     */
    void updateSnatchExceptionStatus(@Param("url")String url);

    /**
     *
     * @param siteClassify
     * @return
     */
    Integer getSnatchExceptionTotal(@Param("siteClassify")String siteClassify);

    /**
     *
     * @param exClass
     * @return
     */
    List<SnatchException> listSnatchExceptionByExClass(@Param("exClass")String exClass);
}
