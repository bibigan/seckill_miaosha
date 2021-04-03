package com.java.service;

import com.java.pojo.Item;

import java.util.List;

public interface ItemService {
    void add(Item c);
    void delete(int id);
    void update(Item c);
    Item get(int id);
    List<Item> list();
    void delItemCache(int id);
    /**
     * 查询库存：通过缓存查询库存
     * 缓存命中：返回库存
     * 缓存未命中：查询数据库写入缓存并返回
     * @param id
     * @return
     */
    Integer getStockCount(int id);

    /**
     * 获取剩余库存：查数据库
     * @param id
     * @return
     */
    int getStockCountByDB(int id);

    /**
     * 获取剩余库存: 查缓存
     * @param id
     * @return
     */
    Integer getStockCountByCache(int id);

    /**
     * 将库存插入缓存
     * @param id
     * @return
     */
    void setStockCountCache(int id, int count);

    /**
     * 删除库存缓存
     * @param id
     */
    void delStockCountCache(int id);

    /**
     * 根据库存 ID 查询数据库库存信息
     * @param id
     * @return
     */
    Item getItemById(int id);

    /**
     * 根据库存 ID 查询数据库库存信息（悲观锁）
     * @param id
     * @return
     */
    Item getItemByIdForUpdate(int id);

    /**
     * 更新数据库库存信息
     * @param item
     * return
     */
    int updateItemById(Item item);

    /**
     * 更新数据库库存信息（乐观锁）
     * @param item
     * @return
     */
    int updateItemByOptimistic(Item item);

}
