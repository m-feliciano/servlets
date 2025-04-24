package com.dev.servlet.core.cache;

import com.dev.servlet.core.mapper.Mapper;
import com.dev.servlet.core.util.CacheUtils;
import com.dev.servlet.domain.model.Entity;
import com.dev.servlet.domain.repository.ICrudRepository;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import com.dev.servlet.infrastructure.persistence.IPageable;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
public class CachedServiceDecorator<T extends Entity<ID>, ID> implements ICrudRepository<T, ID> {
    private ICrudRepository<T, ID> decorated;
    private String cacheKeyPrefix;
    private String cacheToken;
    public CachedServiceDecorator(ICrudRepository<T, ID> decorated, String cacheKeyPrefix, String cacheToken) {
        this.decorated = decorated;
        this.cacheKeyPrefix = cacheKeyPrefix;
        this.cacheToken = cacheToken;
    }
    @Override
    public T findById(ID id) {
        if (id == null) return null;
        String cacheKey = cacheKeyPrefix + ":findById:" + id;
        T cached = CacheUtils.getObject(cacheKey, cacheToken);
        if (cached != null) {
            log.debug("Cache hit for key={}", cacheKey);
            return cached;
        }
        log.debug("Cache miss for key={}, fetching from database", cacheKey);
        T result = decorated.findById(id);
        if (result != null) {
            CacheUtils.setObject(cacheKey, cacheToken, result);
        }
        return result;
    }
    @Override
    public T find(T object) {
        if (object != null && object.getId() != null) {
            return findById(object.getId());
        }
        return decorated.find(object);
    }
    @Override
    public Collection<T> findAll(T object) {
        String cacheKey = cacheKeyPrefix + ":findAll:" +
                          Optional.ofNullable(object)
                                  .map(obj -> obj.getId() != null ? obj.getId().toString() : "null")
                                  .orElse("null");
        Collection<T> cached = CacheUtils.get(cacheKey, cacheToken);
        if (cached != null && !cached.isEmpty()) {
            log.debug("Cache hit for key={}", cacheKey);
            return cached;
        }
        log.debug("Cache miss for key={}, fetching from database", cacheKey);
        Collection<T> result = decorated.findAll(object);
        if (result != null && !result.isEmpty()) {
            CacheUtils.set(cacheKey, cacheToken, result);
        }
        return result;
    }
    @Override
    public T save(T object) {
        T result = decorated.save(object);
        invalidateCache();
        return result;
    }
    @Override
    public T update(T object) {
        T result = decorated.update(object);
        invalidateCache();
        return result;
    }
    @Override
    public boolean delete(T object) {
        boolean deleted = decorated.delete(object);
        invalidateCache();
        return deleted;
    }
    @Override
    public IPageable<T> getAllPageable(IPageRequest<T> pageRequest) {
        String cacheKey = buildPageRequestCacheKey(pageRequest);
        IPageable<T> cached = CacheUtils.getObject(cacheKey, cacheToken);
        if (cached != null) {
            log.debug("Cache hit for pageable request: {}", cacheKey);
            return cached;
        }
        log.debug("Cache miss for pageable request: {}", cacheKey);
        IPageable<T> result = decorated.getAllPageable(pageRequest);
        if (result != null && result.getTotalElements() > 0) {
            CacheUtils.setObject(cacheKey, cacheToken, result);
        }
        return result;
    }
    @Override
    public <U> IPageable<U> getAllPageable(IPageRequest<T> pageRequest, Mapper<T, U> mapper) {
        String cacheKey = buildPageRequestCacheKey(pageRequest);
        IPageable<U> cached = CacheUtils.getObject(cacheKey, cacheToken);
        if (cached != null) {
            log.debug("Cache hit for mapped pageable request: {}", cacheKey);
            return cached;
        }
        log.debug("Cache miss for mapped pageable request: {}", cacheKey);
        IPageable<U> result = decorated.getAllPageable(pageRequest, mapper);
        if (result != null && result.getTotalElements() > 0) {
            CacheUtils.setObject(cacheKey, cacheToken, result);
        }
        return result;
    }

    public void invalidateCache() {
        CacheUtils.clearCacheKeyPrefix(cacheKeyPrefix, cacheToken);
    }

    private String buildPageRequestCacheKey(IPageRequest<T> pageRequest) {
        StringBuilder key = new StringBuilder(cacheKeyPrefix).append(":pageable");
        key.append(":page=").append(pageRequest.getInitialPage());
        key.append(":size=").append(pageRequest.getPageSize());
        if (pageRequest.getSort() != null) {
            key.append(":sort=").append(pageRequest.getSort().getField());
            key.append(":dir=").append(pageRequest.getSort().getDirection());
        }
        if (pageRequest.getFilter() != null) {
            T filter = pageRequest.getFilter();
            if (filter.getId() != null) {
                key.append(":filterId=").append(filter.getId());
            } else {
                String filterKey = String.valueOf(filter).replaceAll("[^a-zA-Z0-9=,]", "");
                key.append(":filter=").append(filterKey.hashCode());
            }
        }
        return key.toString();
    }
}
