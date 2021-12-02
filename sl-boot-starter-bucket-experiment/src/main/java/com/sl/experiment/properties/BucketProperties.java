package com.sl.experiment.properties;

import com.sl.experiment.common.Versions;
import lombok.Data;

/**
 * @author rock
 * @Description TODO
 * @createTime 2021/12/02 10:20:00
 */
@Data
public class BucketProperties {

    /**
     * 唯一ID
     */
    private String id;
    /**
     * 版本
     */
    private Versions version;
    /**
     * 描述
     */
    private String desc;
    /**
     * 权重
     */
    private Integer weight;

}
