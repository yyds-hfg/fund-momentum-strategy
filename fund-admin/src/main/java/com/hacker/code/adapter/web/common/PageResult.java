package com.hacker.code.adapter.web.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long pageSize;
    private long current;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, long pageSize, long current) {
        this.records = records;
        this.total = total;
        this.pageSize = pageSize;
        this.current = current;
    }
}
