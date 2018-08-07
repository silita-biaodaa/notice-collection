package com.silita.service.impl;

import com.silita.dao.AreaMapper;
import com.silita.service.IAreaService;
import com.snatch.model.Area;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("areaService")
public class AreaServiceImpl implements IAreaService {

    @Autowired
    private AreaMapper areaMapper;

    @Override
    @Cacheable(value="blurArea",key = "'findBlurArea'+#name")
    public List<Area> findBlurArea(String name) {
        return areaMapper.findBlurArea(name);
    }

    @Override
    @Cacheable(value="blurArea",key = "'queryProvArea'+#name")
    public List<Area> queryProvArea(String name) {
        return areaMapper.queryProvArea(name);
    }
}
