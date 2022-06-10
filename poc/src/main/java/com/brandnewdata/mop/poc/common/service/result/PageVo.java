package com.brandnewdata.mop.poc.common.service.result;

import lombok.Data;

import java.util.List;

/**
 * @author caiwillie
 */
@Data
public class PageVo<T> {

    private int total;

    private List<T> records;

    public PageVo() {

    }

    public PageVo(int total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
