package com.sl.experiment;

import com.sl.experiment.common.Versions;
import com.sl.experiment.properties.BucketProperties;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author rock
 * @Description TODO
 * @createTime 2021/12/02 09:52:00
 */

public class BucketSelecter {
    /**
     * 实验ID
     */
    private final String expId;
    /**
     * 总权重
     */
    private final int totalWeight;
    /**
     * 实验桶
     */
    private final List<BucketProperties> lstBucket;

    public BucketSelecter(String expId, List<BucketProperties> bucketProperties) {
        this.expId = expId;
        this.lstBucket = bucketProperties;
        this.totalWeight = bucketProperties.stream().mapToInt(BucketProperties::getWeight).sum();
    }

    /**
     * 实验分流
     *
     * @param uid
     * @return
     */
    public Versions doSelect(String uid) {
        if (StringUtils.isEmpty(uid)) {
            uid = "";
        }
        //int hash = fnvHash(expId + "_" + uid);
        int hash = murmur32Hash(expId + "_" + uid);
        int modulo = hash % totalWeight;
        int offset = 0;
        BucketProperties bucketProperties = null;
        for (BucketProperties item : lstBucket) {
            if (offset <= modulo && modulo < offset + item.getWeight()) {
                bucketProperties = item;
                break;
            }
            offset += item.getWeight();
        }
        if (Objects.isNull(bucketProperties)) {
            bucketProperties = lstBucket.get(0);
        }
        return bucketProperties.getVersion();
    }

    //hash算法： FnvHash &  murmur32Hash

    //参考hutool工具包
    public int murmur32Hash(String value) {
        byte[] data = value.getBytes();
        int hash = 0;
        int length = data.length;
        int nblocks = length >> 2;

        int idx;
        int k1;
        for (idx = 0; idx < nblocks; ++idx) {
            k1 = idx << 2;
            int k = data[k1] & 255 | (data[k1 + 1] & 255) << 8 | (data[k1 + 2] & 255) << 16 | (data[k1 + 3] & 255) << 24;
            k *= -862048943;
            k = Integer.rotateLeft(k, 15);
            k *= 461845907;
            hash ^= k;
            hash = Integer.rotateLeft(hash, 13) * 5 + -430675100;
        }

        idx = nblocks << 2;
        k1 = 0;
        switch (length - idx) {
            case 3:
                k1 ^= data[idx + 2] << 16;
            case 2:
                k1 ^= data[idx + 1] << 8;
            case 1:
                k1 ^= data[idx];
                k1 *= -862048943;
                k1 = Integer.rotateLeft(k1, 15);
                k1 *= 461845907;
                hash ^= k1;
            default:
                hash ^= length;
                hash ^= hash >>> 16;
                hash *= -2048144789;
                hash ^= hash >>> 13;
                hash *= -1028477387;
                hash ^= hash >>> 16;
        }

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }


    private int fnvHash(String value) {
        String digest = DigestUtils.md5DigestAsHex(value.getBytes());
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < digest.length(); i++) {
            hash = (hash ^ digest.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }
}
