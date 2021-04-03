package com.java.service.Impl;

import com.java.mapper.Item_killMapper;
import com.java.pojo.Item_kill;
import com.java.service.Item_killService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames="itemsKill")
public class Item_killServiceImpl implements Item_killService {
    @Autowired
    Item_killMapper item_killMapper;
    @Override
    @CacheEvict(allEntries=true)
    public void add(Item_kill c) {
        item_killMapper.save(c);
    }

    @Override
    @CacheEvict(key="'itemsKill-one-'+ #p0")
    public void delete(int id) {
        item_killMapper.delete(id);
    }

    @Override
    @CacheEvict(allEntries=true)
    public void update(Item_kill c) {
        item_killMapper.update(c);
    }

    @Override
    @Cacheable(key="'itemsKill-one-'+ #p0")
    public Item_kill get(int id) {
        return item_killMapper.get(id);
    }

    @Override
    @Cacheable(key="'itemsKill-all'")
    public List<Item_kill> list() {
        return item_killMapper.findAll();
    }
}
