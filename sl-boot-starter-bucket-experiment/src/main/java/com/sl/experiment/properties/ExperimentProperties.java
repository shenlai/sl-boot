package com.sl.experiment.properties;

import lombok.Data;

import java.util.List;

/**
 * @author rock
 * @Description TODO
 * @createTime 2021/12/02 10:22:00
 */
@Data
public class ExperimentProperties {

    /**
     * 实验桶
     */
    private List<BucketProperties> buckets;

    /**
     * 白名单
     */
    private List<WhiteListProperties> whites;
}
