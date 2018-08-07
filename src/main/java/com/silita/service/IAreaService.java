package com.silita.service;

import com.snatch.model.Area;

import java.util.List;

public interface IAreaService {
    /**
     *
     * @param name
     * @return
     */
    List<Area> findBlurArea(String name);

    /**
     *
     * @param name
     * @return
     */
    List<Area> queryProvArea(String name);
}
