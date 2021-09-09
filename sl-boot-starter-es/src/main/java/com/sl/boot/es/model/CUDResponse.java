package com.sl.boot.es.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CUDResponse {

    /**
     * 成功数
     */
    private long successed;

    /**
     * 失败数
     */
    private long failed;

    /**
     * 成功ID列表
     */
    private List<String> successedIdList;

    /**
     * 失败ID列表
     */
    private List<String> failedIdList;

    public CUDResponse() {
    }

    public CUDResponse(long successed, long failed, List<String> successedIdList, List<String> failedIdList) {
        this.successed = successed;
        this.failed = failed;
        this.successedIdList = successedIdList;
        this.failedIdList = failedIdList;
    }
}
