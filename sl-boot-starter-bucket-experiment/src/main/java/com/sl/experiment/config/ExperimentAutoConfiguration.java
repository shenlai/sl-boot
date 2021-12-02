package com.sl.experiment.config;

import com.sl.experiment.BucketTester;
import com.sl.experiment.properties.ExperimentConfigProperties;
import com.sl.experiment.properties.ExperimentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.Map;


/**
 * # 实验分组ID
 * config.experiment.data.20210120.buckets[0].id=202101200000
 * config.experiment.data.20210120.buckets[0].version=A
 * config.experiment.data.20210120.buckets[0].weight=50
 * config.experiment.data.20210120.buckets[1].id=202101200001
 * config.experiment.data.20210120.buckets[1].version=B
 * config.experiment.data.20210120.buckets[1].weight=50
 */

/**
 * @author rock
 * @Description TODO
 * @createTime 2021/12/02 09:55:00
 */
@Slf4j
@Configuration
@AutoConfigureOrder(-1)
@EnableConfigurationProperties({ExperimentConfigProperties.class})
public class ExperimentAutoConfiguration {

    @Autowired
    private ExperimentConfigProperties experimentConfigProperties;

    @Bean
    public BucketTester bucketTester() {
        log.info("a/b test register start");
        Assert.notNull(this.experimentConfigProperties, "a/b test config empty");
        BucketTester bucketTester = new BucketTester();
        Map<String, ExperimentProperties> mapData = experimentConfigProperties.getData();
        for (Map.Entry<String, ExperimentProperties> entry : this.experimentConfigProperties.getData().entrySet()) {
            ExperimentProperties experiment = entry.getValue();
            bucketTester.init(entry.getKey(), experiment.getBuckets(), experiment.getWhites());
        }
        return bucketTester;
    }
}
