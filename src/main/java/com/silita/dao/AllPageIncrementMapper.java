package com.silita.dao;

import java.util.List;
import java.util.Map;

public interface AllPageIncrementMapper {
    /**
     *
     */
    List<Map<String, Object>> listAllPageIncrement(String url);

    /**
     *
     * @param params
     */
    void insertAllPageIncrement(Map params);
}
