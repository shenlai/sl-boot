package com.sl.experiment.properties;

import com.sl.experiment.common.Versions;
import lombok.Data;

/**
 * @author rock
 * @Description TODO
 * @createTime 2021/12/02 10:30:00
 */
@Data
public class WhiteListProperties {
    /**
     * 用户唯一标识
     */
    private String uid;
    /**
     * 版本
     */
    private Versions version;
}
