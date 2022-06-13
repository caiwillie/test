package com.brandnewdata.mop.poc.common.service.result;

import lombok.Data;

import java.util.List;

/**
 * @author caiwillie
 */
@Data
public class PageResult<T> {

    private long total;

    private List<T> records;

    public PageResult() {

    }

    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
