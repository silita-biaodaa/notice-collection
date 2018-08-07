package com.snatch.common.jdbc;

import com.snatch.model.Mo;

import java.util.List;
import java.util.Map;


public class Page<T> extends Mo {
    private List<T> rows;
    private Map<String,Object> statistics;

	public List<T> getRows() {
		return rows;
	}

	public void setRows(List<T> rows) {
		this.rows = rows;
	}

	public Map<String, Object> getStatistics() {
		return statistics;
	}

	public void setStatistics(Map<String, Object> statistics) {
		this.statistics = statistics;
	}

    
}
