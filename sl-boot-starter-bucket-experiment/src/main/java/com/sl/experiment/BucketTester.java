package com.sl.experiment;

import com.sl.experiment.common.BucketTestException;
import com.sl.experiment.common.Versions;
import com.sl.experiment.properties.BucketProperties;
import com.sl.experiment.properties.WhiteListProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author rock
 * @Description TODO
 * @createTime 2021/12/02 09:52:00
 */
public class BucketTester {
    private final static ConcurrentHashMap<String, BucketSelecter> selecterMap = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, List<WhiteListProperties>> whiteMap = new ConcurrentHashMap<>();

    /**
     * 初始化A/B测试实验配置
     *
     * @param expId     实验号
     * @param buckets   实验分组配置
     * @param whiteList 实验白名单配置
     */
    public void init(String expId, List<BucketProperties> buckets, List<WhiteListProperties> whiteList) {
        if (StringUtils.isEmpty(expId)) {
            throw new BucketTestException("参数[expId]不可为空");
        }
        if (CollectionUtils.isEmpty(buckets)) {
            throw new BucketTestException("参数[bucketProperties]不可为空");
        }
        if (selecterMap.containsKey(expId)) {
            throw new BucketTestException("实验ID不可重复");
        }
        Map<Versions, Long> bucketCollect = buckets.stream().collect(Collectors.groupingBy(BucketProperties::getVersion, Collectors.counting()));
        if (bucketCollect.values().stream().anyMatch(item -> item >= 2)) {
            throw new BucketTestException("同一实验下实验分组版本号不能重复");
        }
        selecterMap.putIfAbsent(expId, new BucketSelecter(expId, buckets));
        if (!CollectionUtils.isEmpty(whiteList)) {
            whiteMap.putIfAbsent(expId, whiteList);
        }
    }

    /**
     * 自动配置A/B测试实验
     *
     * @param expId     实验号
     * @param buckets   实验分组配置
     * @param whiteList 实验白名单配置
     */
    public void initAutoConfig(String expId, List<BucketProperties> buckets, List<WhiteListProperties> whiteList){
        if (StringUtils.isEmpty(expId)) {
            throw new BucketTestException("参数[expId]不可为空");
        }
        if (CollectionUtils.isEmpty(buckets)) {
            throw new BucketTestException("参数[bucketProperties]不可为空");
        }
        Map<Versions, Long> bucketCollect = buckets.stream().collect(Collectors.groupingBy(BucketProperties::getVersion, Collectors.counting()));
        if (bucketCollect.values().stream().anyMatch(item -> item >= 2)) {
            throw new BucketTestException("同一实验下实验分组版本号不能重复");
        }
        selecterMap.put(expId, new BucketSelecter(expId, buckets));
        if (!CollectionUtils.isEmpty(whiteList)) {
            whiteMap.put(expId, whiteList);
        }
    }



    /**
     * 执行A/B测试
     *
     * @param expId 实验ID
     * @param uid 分流数据
     * @return
     */
    public Versions doTest(String expId, String uid) {
        if (StringUtils.isEmpty(expId)) {
            throw new BucketTestException("参数[expId]不可为空");
        }
        if (StringUtils.isEmpty(uid)) {
            throw new BucketTestException("参数[uid]不可为空");
        }
        if (!selecterMap.containsKey(expId)) {
            return Versions.A;
        }
        if (whiteMap.containsKey(expId)) {
            List<WhiteListProperties> lstWhite = whiteMap.getOrDefault(expId, new ArrayList<>());
            Optional<WhiteListProperties> first = lstWhite.stream().filter(item -> uid.equals(item.getUid())).findFirst();
            if (first.isPresent()) {
                return first.get().getVersion();
            }
        }
        return selecterMap.get(expId).doSelect(uid);
    }
}
