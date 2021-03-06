package com.sl.boot.es.serviceclient;

import com.alibaba.fastjson.JSON;
import com.sl.boot.es.model.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Setter
public class ESServiceImpl<T> implements ESService<T> {

    private RestHighLevelClient client;

    @Override
    public boolean add(T t) throws Exception {
        return this.save(t, DocWriteRequest.OpType.INDEX);
    }

    @Override
    public CUDResponse add(List<T> list) throws Exception {
        return this.save(list, DocWriteRequest.OpType.INDEX);
    }


    @Override
    public boolean addNoRepeat(T t) throws Exception {
        return this.save(t, DocWriteRequest.OpType.CREATE);
    }


    @Override
    public CUDResponse addNoRepeat(List<T> list) throws Exception {
        return this.save(list, DocWriteRequest.OpType.CREATE);
    }

    @Override
    public boolean save(T t, DocWriteRequest.OpType opType) throws Exception {
        if (t == null) {
            throw new Exception("es parameter is null!");
        }
        Map map = JSON.parseObject(JSON.toJSONString(t), Map.class);
        MetaData metaData = Tools.getMetaData(t.getClass());
        String id = Tools.getESId(t);
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();
        IndexRequest request = new IndexRequest(indexName, indexType, id);

        // ????????????id??????opType=CREATE?????????????????????????????????????????????INDEX???????????????????????????
        if (StringUtils.isEmpty(id)) {
            opType = DocWriteRequest.OpType.INDEX;
        }
        request.opType(opType);
        request.source(map);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        return dealResponse(response);
    }

    @Override
    public CUDResponse save(List<T> list, DocWriteRequest.OpType opType) throws Exception {
        if (list == null || list.isEmpty()) {
            throw new Exception("es parameter is null!");
        }

        DocWriteRequest.OpType useOpType;

        MetaData metaData = Tools.getMetaData(list.get(0).getClass());
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();
        BulkRequest bulkRequest = new BulkRequest();

        for (T t : list) {
            useOpType = opType;
            String id = Tools.getESId(t);
            Map map = JSON.parseObject(JSON.toJSONString(t), Map.class);

            // ????????????id??????opType=CREATE?????????????????????????????????????????????INDEX???????????????????????????
            if (StringUtils.isEmpty(id)) {
                useOpType = DocWriteRequest.OpType.INDEX;
            }

            IndexRequest request = new IndexRequest(indexName, indexType, id)
                    .source(map)
                    .opType(useOpType);

            bulkRequest.add(request);
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        return dealBulkResponse(bulkResponse);
    }


    @Override
    public boolean delete(String id, Class<T> clazz) throws Exception {
        if (id.isEmpty()) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();
        DeleteRequest request = new DeleteRequest(indexName, indexType, id);

        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);

        return dealResponse(response);
    }


    @Override
    public CUDResponse delete(List<String> ids, Class<T> clazz) throws Exception {
        if (ids == null && ids.isEmpty()) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();
        BulkRequest bulkRequest = new BulkRequest();

        for (String id : ids) {
            DeleteRequest request = new DeleteRequest(indexName, indexType, id);
            bulkRequest.add(request);
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        return dealBulkResponse(bulkResponse);
    }


    @Override
    public CUDResponse deleteByQuery(QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();

        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        request.setQuery(queryBuilder);
        request.setConflicts("proceed");//????????????????????????

        BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
        return dealBulkByScrollResponse(response);
    }

    @Override
    public void deleteByQueryBigData(QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();

        // ???????????????????????????????????????????????????slice?????????????????????
        GetIndexRequest indexRequest = new GetIndexRequest().indices(indexName);
        GetIndexResponse indexResponse = client.indices().get(indexRequest, RequestOptions.DEFAULT);
        int numberOfShard = Integer.parseInt(indexResponse.getSetting(indexName, "index.number_of_shards"));

        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        request.setQuery(queryBuilder);
        request.setSlices(numberOfShard);
//        request.setBatchSize(5000);//?????????1000???cpu??????????????????????????????
        request.setConflicts("proceed");//????????????????????????

        ActionListener listener = new ActionListener<BulkByScrollResponse>() {

            public void onResponse(BulkByScrollResponse response) {
                log.info("deleteByQueryBigData success!, result:" + response.toString());
            }


            public void onFailure(Exception e) {
                log.error("deleteByQueryBigData failed!, queryBuilder:" + queryBuilder.toString(), e);
            }
        };
        client.deleteByQueryAsync(request, RequestOptions.DEFAULT, listener);
    }


    @Override
    public boolean update(T t) throws Exception {
        if (t == null) {
            throw new Exception("es parameter is null!");
        }


        MetaData metaData = Tools.getMetaData(t.getClass());
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();
        String id = Tools.getESId(t);

        if (id == null) {
            throw new Exception("ESId can not be null!");
        }

        Map map = JSON.parseObject(JSON.toJSONString(t), Map.class);
        UpdateRequest request = new UpdateRequest(indexName, indexType, id).doc(map);
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);

        return dealResponse(response);
    }


    @Override
    public CUDResponse update(List<T> list) throws Exception {
        if (list == null && list.isEmpty()) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(list.get(0).getClass());
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();

        BulkRequest bulkRequest = new BulkRequest();

        for (T t : list) {
            String id = Tools.getESId(t);
            if (StringUtils.isEmpty(id)) {
                throw new Exception("ESId can not be null!");
            }
            Map map = JSON.parseObject(JSON.toJSONString(t), Map.class);
            UpdateRequest request = new UpdateRequest(indexName, indexType, id).doc(map);
            bulkRequest.add(request);
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        return dealBulkResponse(bulkResponse);
    }


    @Override
    public boolean updateCover(T t) throws Exception {
        return add(t);
    }


    @Override
    public CUDResponse updateCover(List<T> list) throws Exception {
        return add(list);
    }


    @Override
    public CUDResponse updateByQuery(QueryBuilder queryBuilder, Script script, Class<T> clazz) throws Exception {
        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        UpdateByQueryRequest request = new UpdateByQueryRequest(indexName);
        request.setQuery(queryBuilder);
        request.setScript(script);
        request.setConflicts("proceed");//????????????????????????

        BulkByScrollResponse response = client.updateByQuery(request, RequestOptions.DEFAULT);

        return dealBulkByScrollResponse(response);
    }


    @Override
    public void updateByQueryBigData(QueryBuilder queryBuilder, Script script, Class<T> clazz) throws Exception {
        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();

        // ???????????????????????????????????????????????????slice?????????????????????
        GetIndexRequest indexRequest = new GetIndexRequest().indices(indexName);
        GetIndexResponse indexResponse = client.indices().get(indexRequest, RequestOptions.DEFAULT);
        int numberOfShard = Integer.parseInt(indexResponse.getSetting(indexName, "index.number_of_shards"));

        UpdateByQueryRequest request = new UpdateByQueryRequest(indexName);
        request.setQuery(queryBuilder);
        request.setScript(script);
        request.setSlices(numberOfShard);
        request.setConflicts("proceed");//????????????????????????

        ActionListener listener = new ActionListener<BulkByScrollResponse>() {

            public void onResponse(BulkByScrollResponse response) {
                log.info("updateByQueryBigData success!, result:" + queryBuilder.toString());
            }


            public void onFailure(Exception e) {
                log.error("updateByQueryBigData failed!, queryBuilder:" + request.toString(), e);
            }
        };

        client.updateByQueryAsync(request, RequestOptions.DEFAULT, listener);
    }


    @Override
    public SearchResponse search(SearchRequest request) throws Exception {
        return client.search(request, RequestOptions.DEFAULT);
    }


    @Override
    public T search(String id, Class<T> clazz) throws Exception {
        if (id == null && id.isEmpty()) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
//        String indexType = metaData.getIndexType();

//        GetRequest request = new GetRequest(indexName, indexType, id);
        GetRequest request = new GetRequest(indexName, id);

        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        if (response.isExists()) {
            String resultString = response.getSourceAsString();
            return JSON.parseObject(resultString, clazz);
        }

        return null;
    }


    @Override
    public List<T> search(List<String> ids, Class<T> clazz) throws Exception {
        List<T> resultList = new ArrayList<>();

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        //String indexType = metaData.getIndexType();

        MultiGetRequest request = new MultiGetRequest();

        for (String id : ids) {
            if (!StringUtils.isEmpty(id)) {
                request.add(new MultiGetRequest.Item(indexName, id));
            }
        }

        MultiGetResponse responses = client.mget(request, RequestOptions.DEFAULT);

        for (MultiGetItemResponse itemResponse : responses) {
            if (itemResponse.getResponse().isExists()) {
                String resultString = itemResponse.getResponse().getSourceAsString();
                resultList.add(JSON.parseObject(resultString, clazz));
            }
        }
        return resultList;
    }


    @Override
    public List<T> search(T t) throws Exception {
        return search(t, null);
    }


    @Override
    public List<T> search(T t, ESPage page) throws Exception {
        if (t == null) {
            throw new Exception("es parameter is null!");
        }


        List<T> resultList = new ArrayList<>();
        MetaData metaData = Tools.getMetaData(t.getClass());
        String indexName = metaData.getIndexName();
//        String indexType = metaData.getIndexType();

        SearchRequest request = new SearchRequest(indexName);
//        request.types(indexType);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(t), Map.class);
        for (String key : map.keySet()) {
            boolQueryBuilder.must(new TermQueryBuilder(key, map.get(key)));
        }
        // ??????????????????
        if (page != null) {
            page.countFromAndSize();
            sourceBuilder.size(page.getSize());
            sourceBuilder.from(page.getFrom());
            if (page.getSize() + page.getFrom() > ESConstant.esCoreSizeParam.PAGINATION_SIZE) {
                throw new Exception("?????????????????????????????????????????????");
            }
        } else {
            sourceBuilder.size(ESConstant.esCoreSizeParam.PAGINATION_SIZE);//??????????????????10000?????????????????????searchScroll()???????????????????????????
        }

        request.source(sourceBuilder.query(boolQueryBuilder));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        for (SearchHit hit : response.getHits()) {
            String resultString = hit.getSourceAsString();
            resultList.add((T) JSON.parseObject(resultString, t.getClass()));
        }

        // ??????????????????
        if (page != null) {
            int totalNum = (int) response.getHits().getTotalHits().value;
            int totalPage = totalNum / page.getSize() + 1;
            page.setTotalNumber(totalNum);
            page.setTotalPage(totalPage);
        }

        return resultList;
    }


    @Override
    public List<T> search(SearchSourceBuilder sourceBuilder, Class<T> clazz, ESPage page) throws Exception {
        if (sourceBuilder == null) {
            throw new Exception("es parameter is null!");
        }

        List<T> resultList = new ArrayList<>();
        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
//        String indexType = metaData.getIndexType();

        SearchRequest request = new SearchRequest(indexName);
//        request.types(indexType);

        // ??????????????????
        if (page != null) {
            page.countFromAndSize();
            sourceBuilder.size(page.getSize());
            sourceBuilder.from(page.getFrom());
            if ((page.getSize() + page.getFrom()) > ESConstant.esCoreSizeParam.PAGINATION_SIZE) {
                throw new Exception("?????????????????????????????????????????????");
            }
        } else {
            sourceBuilder.size(ESConstant.esCoreSizeParam.PAGINATION_SIZE);//??????????????????10000?????????????????????searchScroll()???????????????????????????
        }

        // ????????????
        if (page != null && !CollectionUtils.isEmpty(page.getSortList())) {
            for (Sort sort : page.getSortList()) {
                sourceBuilder.sort(sort.getProperty(), sort.getDirection());
            }
        }

        request.source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        try {
            for (SearchHit hit : response.getHits()) {
                String resultString = hit.getSourceAsString();
                resultList.add(JSON.parseObject(resultString, clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        // ??????????????????
        if (page != null) {
            int totalNum = (int) response.getHits().getTotalHits().value;
            int totalPage = totalNum / page.getSize() + 1;
            page.setTotalNumber(totalNum);
            page.setTotalPage(totalPage);
        }

        return resultList;
    }

    @Override
    public List<T> search(SearchSourceBuilder sourceBuilder, Class<T> clazz) throws Exception {
        if (sourceBuilder == null) {
            throw new Exception("es parameter is null!");
        }

        List<T> resultList = new ArrayList<>();
        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
//        String indexType = metaData.getIndexType();

        SearchRequest request = new SearchRequest(indexName);
//        request.types(indexType);

        request.source(sourceBuilder);
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            for (SearchHit hit : response.getHits()) {
                String resultString = hit.getSourceAsString();
                resultList.add(JSON.parseObject(resultString, clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }


        return resultList;
    }


    @Override
    public SearchResponse searchAggregation(SearchSourceBuilder sourceBuilder, Class<T> clazz) throws Exception {
        return searchAggregation(sourceBuilder, clazz, null);
    }


    @Override
    public SearchResponse searchAggregation(SearchSourceBuilder sourceBuilder, Class<T> clazz, ESPage page) throws Exception {
        if (sourceBuilder == null) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
//        String indexType = metaData.getIndexType();

        SearchRequest request = new SearchRequest(indexName);
//        request.types(indexType);

        // ??????????????????
        if (page != null) {
            page.countFromAndSize();
            sourceBuilder.size(page.getSize());
            sourceBuilder.from(page.getFrom());
            if (page.getSize() + page.getFrom() > ESConstant.esCoreSizeParam.PAGINATION_SIZE) {
                throw new Exception("?????????????????????????????????????????????");
            }
        } else {
            sourceBuilder.size(0);//???????????????????????????????????????
        }

        request.source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // ??????????????????
        if (page != null) {
            int totalNum = (int) response.getHits().getTotalHits().value;
            int totalPage = totalNum / page.getSize() + 1;
            page.setTotalNumber(totalNum);
            page.setTotalPage(totalPage);
        }

        return response;
    }


    @Override
    public SearchResponse searchByScroll(SearchSourceBuilder sourceBuilder, Class<T> clazz, String scrollId) throws Exception {
        if (sourceBuilder == null && StringUtils.isEmpty(scrollId)) {
            throw new Exception("The es parameter must have an not empty one!");
        }

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();

        SearchResponse response;
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1));
        // ??????????????????sourceBuilder?????????????????????10???
        if (StringUtils.isEmpty(scrollId)) {
            SearchRequest request = new SearchRequest(indexName);
            request.scroll(scroll);
            request.source(sourceBuilder);

            response = client.search(request, RequestOptions.DEFAULT);
        } else {
            // ???????????????scrollId???
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            response = client.scroll(scrollRequest, RequestOptions.DEFAULT);
        }

        scrollId = response.getScrollId();
        // ???????????????????????????????????????????????????
        if (scrollId != null && response.getHits().getHits().length == 0) {
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

            if (clearScrollResponse.isSucceeded()) {
                log.info("es scroll clear successed!");
            } else {
                log.error("es scroll clear failed!");
            }
        }

        return response;
    }

    /**
     * ??????????????????
     *
     * @param response
     * @return
     */
    private boolean dealResponse(DocWriteResponse response) {
        boolean result = true;
        if (response.getResult() == DocWriteResponse.Result.CREATED) {
            log.info("DOCUMENT CREATE SUCCESS!");
        } else if (response.getResult() == DocWriteResponse.Result.UPDATED) {
            log.info("DOCUMENT UPDATE SUCCESS!");
        } else if (response.getResult() == DocWriteResponse.Result.DELETED) {
            log.info("DOCUMENT DELETE SUCCESS!");
        } else if (response.getResult() == DocWriteResponse.Result.NOOP) {
            log.info("DOCUMENT NO CHANGE!");
        } else {
            result = false;
            log.info("DOCUMENT OPERATION FAILED! ---" + response.getId());
        }

        return result;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    private CUDResponse dealBulkResponse(BulkResponse bulkResponse) {
        List<String> succeedIdList = new ArrayList<>();
        List<String> failedIdList = new ArrayList<>();

        if (bulkResponse != null) {
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                String id = bulkItemResponse.getId();
                if (bulkItemResponse.isFailed()) {
                    failedIdList.add(id);
                } else {
                    succeedIdList.add(id);
                }
            }
        }
        if (!failedIdList.isEmpty()) {
            log.error("BULK DOCUMENT OPERATION FAILED! ---" + failedIdList);
        }

        return new CUDResponse(succeedIdList.size(), failedIdList.size(), succeedIdList, failedIdList);
    }

    private CUDResponse dealBulkByScrollResponse(BulkByScrollResponse bulkByScrollResponse) {
        long successed = 0;
        long failed = 0;
        List<String> failedIdList = new ArrayList<>();

        if (bulkByScrollResponse != null) {
            for (BulkItemResponse.Failure failure : bulkByScrollResponse.getBulkFailures()) {
                failedIdList.add(failure.getId());
            }
            successed = bulkByScrollResponse.getTotal() - bulkByScrollResponse.getVersionConflicts();//?????????-???????????????
            failed = failedIdList.size();
        }

        return new CUDResponse(successed, failed, null, failedIdList);
    }

    @Override
    public void setClient(RestHighLevelClient client) {
        this.client = client;
    }

}
