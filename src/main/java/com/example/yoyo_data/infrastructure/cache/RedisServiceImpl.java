package com.example.yoyo_data.infrastructure.cache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * Redis操作实现
 * </p>
 *
 * @author miaoyj
 * @version 1.0.0-SNAPSHOT
 * @since 2020-07-20
 */
@Service
public class RedisServiceImpl implements RedisService {
    private static final Logger log = LoggerFactory.getLogger(RedisServiceImpl.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${spring.application.name}")
    private String appName;

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean hashHasHk(String hKey, String hashKey) {
        return redisTemplate.opsForHash().hasKey(hKey, hashKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T hashGet(String hKey, String hashKey, Class<T> valueCls) {
        Object result = redisTemplate.opsForHash().get(hKey, hashKey);
        if (result != null) {
            return Convert.convert(valueCls, result);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T hashGetJson(String hKey, String hashKey, Class<T> valueCls) {
        Object result = redisTemplate.opsForHash().get(hKey, hashKey);
        if (result != null) {
            return JSONUtil.toBean(result.toString(), valueCls);
        } else {
            return null;
        }
    }

    @Override
    public <T> List<T> hashGetList(String hKey, String hashKey, Class<T> valueCls) {
        Object result = redisTemplate.opsForHash().get(hKey, hashKey);
        if (result != null) {
            return JSONUtil.toList(result.toString(), valueCls);
        } else {
            return null;
        }
    }

    @Override
    public Map<Object, Object> hashGetMap(String hKey, String hashKey) {
        Object result = redisTemplate.opsForHash().get(hKey, hashKey);
        if (result != null) {
            return JSONUtil.toBean(result.toString(), Map.class);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Object, Object> hashGetAll(String key, Class valueCls) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (MapUtil.isNotEmpty(map)) {
            return Convert.toMap(Object.class, valueCls, map);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long hashIncrementLongOfHashMap(String hKey, String hashKey, Long delta) {
        return redisTemplate.opsForHash().increment(hKey, hashKey, delta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double hashIncrementDoubleOfHashMap(String hKey, String hashKey, Double delta) {
        return redisTemplate.opsForHash().increment(hKey, hashKey, delta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hashPushHashMap(String key, Object hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hashPushHashMap(String key, Map<String, Object> maps) {
        redisTemplate.opsForHash().putAll(key, maps);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Object> hashGetAllHashKey(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long hashGetHashMapSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> hashGetHashAllValues(String key, Class valueCls) {
        List<Object> result = redisTemplate.opsForHash().values(key);
        if (CollUtil.isNotEmpty(result)) {
            return Convert.toList(valueCls, result);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long hashDeleteHashKey(String key, Object... hashKeys) {
        if (hashKeys.length > 0) {
            return redisTemplate.opsForHash().delete(key, hashKeys);
        }
        return 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> listGet(String key, Class valueCls) {
        return this.listRangeList(key, 0L, -1L, valueCls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long listLeftPush(String key, Object... values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long listLeftPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long listRightPush(String key, Object... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long listRightPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T listRightPop(String key, Class<T> valueCls) {
        Object result = redisTemplate.opsForList().rightPop(key);
        if (ObjectUtil.isNotNull(result)) {
            return Convert.convert(valueCls, result);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T listLeftPop(String key, Class<T> valueCls) {
        Object result = redisTemplate.opsForList().leftPop(key);
        if (ObjectUtil.isNotNull(result)) {
            return Convert.convert(valueCls, result);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long listSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> listRangeList(String key, Long start, Long end, Class valueCls) {
        List<Object> result = redisTemplate.opsForList().range(key, start, end);
        if (CollUtil.isNotEmpty(result)) {
            result = Convert.toList(valueCls, result);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T listIndexFromList(String key, Long index, Class<T> valueCls) {
        Object result = redisTemplate.opsForList().index(key, index);
        if (ObjectUtil.isNotNull(result)) {
            return Convert.convert(valueCls, result);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void listSetValueToList(String key, Long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void listTrimByRange(String key, Long start, Long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long setAddSetObject(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long setGetSizeForSetMap(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Object> setGetMemberOfSetMap(String key, Class valueCls) {
        Set<Object> result = redisTemplate.opsForSet().members(key);
        if (CollUtil.isNotEmpty(result)) {
            result = CollUtil.newHashSet(Convert.toList(valueCls, result));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean setCheckIsMemberOfSet(String key, Object o) {
        return redisTemplate.opsForSet().isMember(key, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T setPop(String key, Class<T> valueCls) {
        Object result = redisTemplate.opsForSet().pop(key);
        if (ObjectUtil.isNotNull(result)) {
            return Convert.convert(valueCls, result);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long setRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stringAppendString(String key, String value) {
        stringRedisTemplate.opsForValue().append(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringGetString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long stringIncrementLongString(String key, Long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double stringIncrementDoubleString(String key, Double delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stringSetString(String key, String value) {
        stringSetString(key, value, 0L);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stringSetString(String key, String value, Long expireSeconds) {
        if (expireSeconds > 0) {
            stringRedisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringGetAndSet(String key, String value) {
        return stringRedisTemplate.opsForValue().getAndSet(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void objectSetObject(String key, Object o) {
        this.objectSetObject(key, o, 0L);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void objectSetObject(String key, Object o, Long expireSeconds) {
        if (expireSeconds > 0) {
            redisTemplate.opsForValue().set(key, o, expireSeconds, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, o);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T objectGetObject(String key, Class<T> valueCls) {
        Object result = redisTemplate.opsForValue().get(key);
        if (result != null) {
            return Convert.convert(valueCls, result);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean setExpire(String key, Long expireMilliSeconds) {
        return redisTemplate.expire(key, expireMilliSeconds, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> keys(String pattern) {
        Set<String> keyList = redisTemplate.keys(pattern + "*");
        String keyPrefix = new StringBuffer(appName).append(":").toString();
        return keyList.stream().map(s -> StrUtil.removePrefix(s, keyPrefix)).collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean delete(Set<String> keys) {
        Long successSize = redisTemplate.delete(keys);
        return ObjectUtil.isNotNull(successSize) && successSize == keys.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delByKeys(String prefix, Set<Long> ids) {
        Set<String> keys = new HashSet<>();
        for (Long id : ids) {
            CollUtil.addAll(keys, new StringBuffer(prefix).append(id).toString());
        }
        this.delete(keys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delByKeyPrefix(String prefix) {
        Set<String> keys = this.keys(prefix);
        return this.delete(keys);
    }

    /**
     * 转换发送
     * @param channel 通道
     * @param message 消息
     */
    @Override
    public void convertAndSend(String channel, Object message){
        redisTemplate.convertAndSend(channel,message);
    }

    @Override
    public Boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(key, date);
    }

    public Boolean setIfAbsent(String key, String value) {
        return this.redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    @Override
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return this.redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    @Override
    public String streamAdd(String streamKey, Map<String, Object> values) {
        String fullStreamKey = appName + ":" + streamKey;
        try {
            RecordId recordId = redisTemplate.opsForStream().add(fullStreamKey, values);
            return recordId != null ? recordId.getValue() : null;
        } catch (Exception e) {
            log.error("添加Stream消息失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> streamRead(String streamKey, int count) {
        String fullStreamKey = appName + ":" + streamKey;
        try {
            StreamOffset<String> offset = StreamOffset.create(fullStreamKey, ReadOffset.lastConsumed());
            StreamReadOptions options = StreamReadOptions.empty().count(count);
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(options, offset);
            List<Map<String, Object>> result = new ArrayList<>();
            if (records != null) {
                for (MapRecord<String, Object, Object> record : records) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("id", record.getId().getValue());
                    Map<String, Object> content = new HashMap<>();
                    Map<Object, Object> value = record.getValue();
                    for (Map.Entry<Object, Object> entry : value.entrySet()) {
                        content.put(entry.getKey().toString(), entry.getValue());
                    }
                    message.put("content", content);
                    result.add(message);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("读取Stream消息失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> streamReadGroup(String streamKey, String consumerGroup, String consumerName, int count) {
        String fullStreamKey = appName + ":" + streamKey;
        try {
            Consumer consumer = Consumer.from(consumerGroup, consumerName);
            StreamOffset<String> offset = StreamOffset.create(fullStreamKey, ReadOffset.lastConsumed());
            StreamReadOptions options = StreamReadOptions.empty().count(count);
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(consumer, options, offset);
            List<Map<String, Object>> result = new ArrayList<>();
            if (records != null) {
                for (MapRecord<String, Object, Object> record : records) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("id", record.getId().getValue());
                    Map<String, Object> content = new HashMap<>();
                    Map<Object, Object> value = record.getValue();
                    for (Map.Entry<Object, Object> entry : value.entrySet()) {
                        content.put(entry.getKey().toString(), entry.getValue());
                    }
                    message.put("content", content);
                    result.add(message);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("从消费组读取Stream消息失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Boolean streamCreateGroup(String streamKey, String consumerGroup) {
        String fullStreamKey = appName + ":" + streamKey;
        try {
            redisTemplate.opsForStream().createGroup(fullStreamKey, consumerGroup);
            return true;
        } catch (Exception e) {
            log.error("创建Stream消费组失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Long streamDelete(String streamKey, String messageId) {
        String fullStreamKey = appName + ":" + streamKey;
        try {
            return redisTemplate.opsForStream().delete(fullStreamKey, messageId);
        } catch (Exception e) {
            log.error("删除Stream消息失败: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long streamLen(String streamKey) {
        String fullStreamKey = appName + ":" + streamKey;
        try {
            return redisTemplate.opsForStream().size(fullStreamKey);
        } catch (Exception e) {
            log.error("获取Stream长度失败: {}", e.getMessage());
            return 0L;
        }
    }
}
