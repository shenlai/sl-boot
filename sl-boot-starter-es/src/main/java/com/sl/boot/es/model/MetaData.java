package com.sl.boot.es.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetaData {

    private String indexName;

    private String indexType;

    public MetaData(String indexName, String indexType) {
        this.indexName = indexName;
        this.indexType = indexType;
    }
}
