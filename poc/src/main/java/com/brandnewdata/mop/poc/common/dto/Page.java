package com.brandnewdata.mop.poc.common.dto;

import cn.hutool.core.collection.ListUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author caiwillie
 */

@Getter
@Setter
public class Page<T> {

    private long total;

    private List<T> records;

    private Map<String, Object> extraMap = new HashMap<>();

    public Page() {

    }

    public Page(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public static <T> Page<T> empty() {
        return new Page<>(0, ListUtil.empty());
    }
}
