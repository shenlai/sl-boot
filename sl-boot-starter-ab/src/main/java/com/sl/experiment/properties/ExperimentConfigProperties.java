package com.sl.experiment.properties;

import com.sl.experiment.common.ExperimentConfigConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rock
 * @Description TODO
 * @createTime 2021/12/02 10:21:00
 * <p>
 * * # 实验分组ID
 * * config.experiment.data.20210120.buckets[0].id=202101200000
 * * config.experiment.data.20210120.buckets[0].version=A
 * * config.experiment.data.20210120.buckets[0].weight=50
 * * config.experiment.data.20210120.buckets[1].id=202101200001
 * * config.experiment.data.20210120.buckets[1].version=B
 * * config.experiment.data.20210120.buckets[1].weight=50
 */
@Data
@ConfigurationProperties(prefix = ExperimentConfigConstants.PREFIX)
public class ExperimentConfigProperties {

    private Map<String, ExperimentProperties> data = new HashMap<>();

}
