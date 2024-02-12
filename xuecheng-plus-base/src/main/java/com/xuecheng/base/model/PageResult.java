package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private List<T> items;

    private long counts;
    private long page;
    private long pageSize;

}
