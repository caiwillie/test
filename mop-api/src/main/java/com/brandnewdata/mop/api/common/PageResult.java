package com.brandnewdata.mop.api.common;

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
public class PageResult<T> {

    private long total;

    private List<T> records;

    private Map<String, Object> extraMap = new HashMap<>();

    public PageResult() {

    }

    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(0, ListUtil.empty());
    }
}
