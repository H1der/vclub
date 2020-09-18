package com.hider.vclub.entity;


import lombok.Data;

// 封装分页项关的信息
@Data
public class Page {

//     当前页码
    private int current = 1;

    // 显示上限
    private int limit = 10;

    // 数据总数
    private int rows;


    // 查询路径
    private String path;

    // 当前页的起始行
    public int getOffset() {
        //current * limit - limit
        return (current - 1) * limit;

    }
//    // 获取总页数
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }
    // 获取起始页码
    public int getFrom() {
        int from = current - 2;
//        return Math.max(from, 1);
        return Math.max(from, 1);

    }
//    // 获取结束页码
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
//        return Math.min(to, total);
        return Math.min(to, total);

    }



}
