package com.sl.boot.es.serviceclient;


import com.sl.boot.es.model.CUDResponse;
import com.sl.boot.es.model.ESPage;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;

/**
 * @author zouqi
 * @version Id: ESService, v 0.1 2020/7/17 16:13 zouqi Exp $
 */
public interface ESService<T> {
    boolean add(T t) throws Exception;

    CUDResponse add(List<T> list) throws Exception;

    boolean addNoRepeat(T t) throws Exception;

    CUDResponse addNoRepeat(List<T> list) throws Exception;

    boolean save(T t, DocWriteRequest.OpType opType) throws Exception;

    CUDResponse save(List<T> list, DocWriteRequest.OpType opType) throws Exception;

    boolean delete(String id, Class<T> clazz) throws Exception;

    CUDResponse delete(List<String> ids, Class<T> clazz) throws Exception;

    CUDResponse deleteByQuery(QueryBuilder queryBuilder, Class<T> clazz) throws Exception;

    void deleteByQueryBigData(QueryBuilder queryBuilder, Class<T> clazz) throws Exception;

    boolean update(T t) throws Exception;

    CUDResponse update(List<T> list) throws Exception;

    boolean updateCover(T t) throws Exception;

    CUDResponse updateCover(List<T> list) throws Exception;

    CUDResponse updateByQuery(QueryBuilder queryBuilder, Script script, Class<T> clazz) throws Exception;

    void updateByQueryBigData(QueryBuilder queryBuilder, Script script, Class<T> clazz) throws Exception;

    SearchResponse search(SearchRequest request) throws Exception;

    T search(String id, Class<T> clazz) throws Exception;

    List<T> search(List<String> ids, Class<T> clazz) throws Exception;

    List<T> search(T t) throws Exception;

    List<T> search(T t, ESPage page) throws Exception;

    List<T> search(SearchSourceBuilder sourceBuilder, Class<T> clazz, ESPage page) throws Exception;

    List<T> search(SearchSourceBuilder sourceBuilder, Class<T> clazz) throws Exception;

    SearchResponse searchAggregation(SearchSourceBuilder sourceBuilder, Class<T> clazz) throws Exception;

    SearchResponse searchAggregation(SearchSourceBuilder sourceBuilder, Class<T> clazz, ESPage page) throws Exception;

    SearchResponse searchByScroll(SearchSourceBuilder sourceBuilder, Class<T> clazz, String scrollId) throws Exception;

    void setClient(RestHighLevelClient client);
}
