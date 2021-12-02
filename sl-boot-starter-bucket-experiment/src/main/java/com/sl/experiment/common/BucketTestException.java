package com.sl.experiment.common;

/**
 * @author rock
 * @Description TODO
 * @createTime 2021/12/02 09:52:00
 */
public class BucketTestException extends RuntimeException {

    /**
     * 错误描述
     */
    private String message;

    public BucketTestException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
