package com.brandnewdata.mop.poc.common.dto;

import lombok.Data;

import java.util.List;

/**
 * @author caiwillie
 */
@Data
public class Page<T> {

    private long total;

    private List<T> records;

    public Page() {

    }


    public Page(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
