package com.sl.boot.es;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Slf4j
@Configuration
@EnableConfigurationProperties(EsConfigProperties.class)
public class EsConnectionConfiguration {

    @Autowired
    private EsConfigProperties esConfigProperties;

    @Bean("restHighLevelClient")
    @ConditionalOnMissingBean(name = "restHighLevelClient")
    public RestHighLevelClient restHighLevelClient() {
        Assert.notNull(esConfigProperties, "ES EsConfigProperties empty.");
        return buildEsClient();
    }



    /**
     * 构建es client
     *
     * @return
     */
    private RestHighLevelClient buildEsClient() {

        log.info("ES register start");
        Assert.notNull(esConfigProperties.getPort(), "port empty");
        Assert.notNull(esConfigProperties.getAddr(), "addr empty");
        Assert.notNull(esConfigProperties.getUsername(), "userName empty");
        Assert.notNull(esConfigProperties.getPassword(), "password empty");
        // 阿里云ES集群验证basic auth。
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        //访问用户名和密码为您创建阿里云Elasticsearch实例时设置的用户名和密码，也是Kibana控制台的登录用户名和密码。
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esConfigProperties.getUsername(), esConfigProperties.getPassword()));

        // RestHighLevelClient实例通过REST low-level client builder进行构造。
        RestClientBuilder builder = RestClient.builder(new HttpHost(esConfigProperties.getAddr(), esConfigProperties.getPort())).setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            httpClientBuilder.setMaxConnTotal(esConfigProperties.getMaxConnTotal());
            httpClientBuilder.setMaxConnPerRoute(esConfigProperties.getMaxConnPerRoute());
            return httpClientBuilder;
        }).setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(esConfigProperties.getConnectTimeout());
            requestConfigBuilder.setSocketTimeout(esConfigProperties.getSocketTimeout());
            requestConfigBuilder.setConnectionRequestTimeout(esConfigProperties.getConnectionRequestTimeout());
            return requestConfigBuilder;
        });
        log.info("ES register success");
        return new RestHighLevelClient(builder);
    }


}
