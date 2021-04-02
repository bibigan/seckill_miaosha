package com.java.service.Impl;

import com.java.controller.UsersController;
import com.java.mapper.ItemMapper;
import com.java.pojo.Item;
import com.java.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

    @Override
    public void add(Item c) {
        itemMapper.save(c);
    }

    @Override
    public void delete(int id) {
        itemMapper.delete(id);
    }

    @Override
    public void update(Item c) {
        itemMapper.update(c);
    }

    @Override
    public Item get(int id) {
        return itemMapper.get(id);
    }

    @Override
    public List<Item> list() {
        return itemMapper.findAll();
    }


    @Override
    public Integer getStockCount(int item_id) {
        Integer stockLeft;
        stockLeft = getStockCountByCache(item_id);
        LOGGER.info("缓存中取得库存数：[{}]", stockLeft);
        if (stockLeft == null) {
            stockLeft = getStockCountByDB(item_id);
            LOGGER.info("缓存未命中，查询数据库，并写入缓存");
            setStockCountCache(item_id, stockLeft);
        }
        return stockLeft;
    }

    @Override
    public int getStockCountByDB(int id) {//返回的是剩余库存
        Item item = itemMapper.selectByPrimaryKey(id);
        return item.getitem_stock() - item.getItem_sale();
    }

    @Override
    public Integer getStockCountByCache(int id) {
        String hashKey = "items-one-" + id + "_stock";
        String countStr = stringRedisTemplate.opsForValue().get(hashKey);
        if (countStr != null) {//缓存存的是剩下库存
            return Integer.parseInt(countStr);
        }
        return null;
    }

    @Override
    public void setStockCountCache(int id, int count) {
        String hashKey = "items-one-" + id + "_stock";
        String countStr = String.valueOf(count);
        LOGGER.info("从数据库写入商品库存缓存: [{}] [{}]", hashKey, countStr);
        stringRedisTemplate.opsForValue().set(hashKey, countStr, 3600, TimeUnit.SECONDS);
    }

    @Override
    public void delStockCountCache(int id) {

    }

    @Override
    public Item getItemById(int id) {
        return itemMapper.selectByPrimaryKey(id);
    }

    @Override
    public Item getItemByIdForUpdate(int id) {
        return itemMapper.selectByPrimaryKeyForUpdate(id);
    }

    @Override
    public int updateItemById(Item item) {
        return itemMapper.updateByPrimaryKeySelective(item);
    }

    @Override
    public int updateItemByOptimistic(Item item) {
        return itemMapper.updateByOptimistic(item);
    }

}
