package com.silita.dao;

import com.snatch.model.Area;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AreaMapper {
    /**
     *
     * @param name
     * @return
     */
    List<Area> findBlurArea(@Param("name")String name);

    /**
     *
     * @param name
     * @return
     */
    List<Area> queryProvArea(@Param("name")String name);

    /**
     *
     * @param province
     * @return
     */
    List<Map<String,Object>> querysCityCode (@Param("province")String province);
}
