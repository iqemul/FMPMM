package edu.hit.fmpmm.util.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class CacheClient {
    private final StringRedisTemplate redis;

    @Autowired
    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.redis = stringRedisTemplate;
    }

    /**
     * 【普通存入缓存】将一个对象以Hash方式存储缓存，并设置过期时间
     *
     * @param key      存入缓存的key
     * @param value    存入缓存的对象
     * @param time     过期时间
     * @param timeUnit 过期时间的单位
     * @param <T>      对象的类型
     */
    public <T> void setHash(String key, T value, Long time, TimeUnit timeUnit) {
        ReflectUtil<T> reflectUtil = new ReflectUtil<>();
        Map<String, Object> objectMap = reflectUtil.object2Map(value);
        redis.opsForHash().putAll(key, objectMap);
        redis.expire(key, time, timeUnit);
    }

    /**
     * 【普通获取缓存】通过一个key查询缓存是否存在（Hash类型）
     *
     * @param key   缓存key
     * @param clazz 想要获取的对象的类型
     * @param <T>   对象类型
     * @return 对象
     */
    public <T> T getHash(String key, Class<T> clazz)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Map<Object, Object> objectMap = redis.opsForHash().entries(key);
        if (!objectMap.isEmpty()) {
            ReflectUtil<T> reflectUtil = new ReflectUtil<>();
            return reflectUtil.map2Object(objectMap, clazz);
        }
        return null;
    }

    public void delHash(String key) {
        redis.delete(key);
    }
}
