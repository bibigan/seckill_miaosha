package com.java.service.Impl;

import com.java.mapper.ItemMapper;
import com.java.pojo.Item;
import com.java.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    ItemMapper itemMapper;
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
    public Integer getStockCount(int id) {
        return null;
    }

    @Override
    public int getStockCountByDB(int id) {
        Item item = itemMapper.selectByPrimaryKey(id);
        return item.getitem_stock() - item.getItem_sale();
    }

    @Override
    public Integer getStockCountByCache(int id) {
        return null;
    }

    @Override
    public void setStockCountCache(int id, int count) {

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
