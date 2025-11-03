package com.layor.tinyflow.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// PageResponseDTO.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDTO<T> {
    private List<T> content;           // 数据列表
    private long totalElements;        // 总条数
    private int totalPages;            // 总页数
    private int size;                  // 每页大小
    private int number;                // 当前页码（0起始）
    private boolean first;
    private boolean last;
    private boolean empty;
}