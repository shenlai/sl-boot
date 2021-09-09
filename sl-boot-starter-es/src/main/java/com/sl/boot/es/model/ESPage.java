package com.sl.boot.es.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ESPage {

    /**
     * 当前表中总条目数量
     */
    private long totalNumber;

    /**
     * 当前页的位置
     */
    private int currentPage = 1;

    /**
     * 总页数
     */
    private int totalPage;

    /**
     * 页面大小
     */
    private int size = 10;

    /**
     * 检索的起始位置
     */
    private int from = 0;

    List<Sort> sortList;


    public ESPage() {
        super();
    }

    public ESPage(int currentPage, int size) {
        super();
        this.currentPage = currentPage;
        this.size = size;
    }

    public ESPage(int size, int from, List<Sort> sortList) {
        this.size = size;
        this.from = from;
        this.sortList = sortList;
    }


    public void count() {
        int totalPageTemp = (int) this.totalNumber / this.size;
        int plus = (this.totalNumber % this.size) == 0 ? 0 : 1;
        totalPageTemp = totalPageTemp + plus;
        if (totalPageTemp <= 0) {
            totalPageTemp = 1;
        }
        this.totalPage = totalPageTemp;//总页数

        if (this.totalPage < this.currentPage) {
            this.currentPage = this.totalPage;
        }
        if (this.currentPage < 1) {
            this.currentPage = 1;
        }
        //起始位置等于之前所有页面输乘以页面大小
        this.from = (this.currentPage - 1) * this.size;
    }

    public void countFromAndSize() {
        //起始位置等于之前所有页面输乘以页面大小
        this.from = (this.currentPage - 1) * this.size;
    }
}
