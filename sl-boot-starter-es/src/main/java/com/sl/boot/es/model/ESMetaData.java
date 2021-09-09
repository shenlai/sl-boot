package com.sl.boot.es.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ESMetaData {

    /**
     * 索引名
     *
     * @return
     */
    String indexName();

    /**
     * 索引类型
     *
     * @return
     */
    String indexType();

}
