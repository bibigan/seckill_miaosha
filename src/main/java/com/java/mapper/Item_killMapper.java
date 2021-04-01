package com.java.mapper;


import com.java.pojo.Item_kill;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Item_killMapper {
    @Select("select * from item_kill ")
    List<Item_kill> findAll();

    @Insert(" insert into item_kill ( item_id,item_kill_seckillStartTime,item_kill_seckillEndTime) values (#{item_id},#{item_kill_seckillStartTime},#{item_kill_seckillEndTime}) ")
    public int save(Item_kill pk);

    @Delete(" delete from item_kill where id= #{id} ")
    public void delete(int id);

    @Select("select * from item_kill where id= #{id} ")
    public Item_kill get(int id);

    @Update("update item_kill set item_kill_seckillStartTime=#{item_kill_seckillStartTime},item_kill_seckillEndTime=#{item_kill_seckillEndTime}  where id=#{id} ")
    public int update(Item_kill pk);
}
