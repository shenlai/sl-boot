package com.sl.boot.es;


import com.sl.boot.es.serviceclient.ESService;
import com.sl.boot.es.serviceclient.ESServiceImpl;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(EsConfigProperties.class)
//@ConditionalOnProperty(name = ConfigConstants.ES_ENABLED, matchIfMissing = true)
@Import({EsConnectionConfiguration.class})
public class EsAutoConfiguration {

    /**
     * 实例es service
     *
     * @return
     */
    @Bean("esServiceClient")
    @Primary
    @ConditionalOnMissingBean(name ="esServiceClient")
    public ESService esService(RestHighLevelClient restHighLevelClient) {
        ESServiceImpl esService = new ESServiceImpl();
        esService.setClient(restHighLevelClient);
        return esService;
    }



}
