package com.sl.boot.es.model;

import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.search.sort.SortOrder;

@Setter
@Getter
public class Sort {
    private SortOrder direction;
    private String property;

    public Sort() {
    }

    public Sort(SortOrder direction, String property) {
        this.direction = direction;
        this.property = property;
    }

}
