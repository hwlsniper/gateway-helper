package io.choerodon.gateway.helper.cache.multi;

import io.choerodon.gateway.helper.cache.l1.L1CacheManager;
import io.choerodon.gateway.helper.cache.l2.L2CacheManager;
import io.choerodon.gateway.helper.cache.setting.MultiCacheProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MultiCacheManager implements CacheManager {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

    private final L1CacheManager l1CacheManager;

    private final L2CacheManager l2CacheManager;

    private final MultiCacheProperties properties;

    public MultiCacheManager(L1CacheManager l1CacheManager,
                             L2CacheManager l2CacheManager,
                             MultiCacheProperties properties) {
        this.l1CacheManager = l1CacheManager;
        this.l2CacheManager = l2CacheManager;
        this.properties = properties;
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = this.cacheMap.get(name);
        if (cache == null) {
            synchronized (this.cacheMap) {
                cache = this.cacheMap.get(name);
                if (cache == null) {
                    cache = createMultiCacheByProperties(name);
                    if (cache != null) {
                        cacheMap.put(name, cache);
                    }
                }
            }
        }
        return cache;
    }

    private MultiCache createMultiCacheByProperties(String name) {
        MultiCacheProperties.Cache config = properties.getCaches().get(name);
        if (config == null) {
            return null;
        }
        if (config.isL1Enabled() && config.isL2Enabled() && l1CacheManager != null && l2CacheManager != null) {
            return new MultiAllCache(name, true,
                    l1CacheManager.getL1Cache(name, config.getL1Spec()), l2CacheManager.getL2Cache(name, config.getL2Spec()));
        }
        if (config.isL1Enabled() && !config.isL2Enabled() && l1CacheManager != null) {
            return new MultiL1Cache(name, true,
                    l1CacheManager.getL1Cache(name, config.getL1Spec()));
        }
        if (!config.isL1Enabled() && config.isL2Enabled() && l2CacheManager != null) {
            return new MultiL2Cache(name, true,
                    l2CacheManager.getL2Cache(name, config.getL2Spec()));
        }
        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(this.cacheMap.keySet());
    }

}
