package com.java.mapper;


import com.java.pojo.Orders;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersMapper {
    @Select("select * from orders ")
    List<Orders> findAll();

    @Insert(" insert into orders ( orders_ocode,orders_number,item_id,item_kill_id,user_id,orders_status,orders_create_time ) values (#{orders_ocode},#{orders_number},#{item_id},#{item_kill_id},#{user_id},#{orders_status},#{orders_create_time}) ")
    public int save(Orders p);

    @Delete(" delete from orders where id= #{id} ")
    public void delete(int id);

    @Select("select * from orders where id= #{id} ")
    public Orders get(int id);

    @Update("update orders set orders_status=#{orders_status} where id= #{id} ")
    public int update(Orders p);

    @Select("select * from orders where user_id= #{uid} ")
    public List<Orders> getByUid(int uid);

    int deleteByPrimaryKey(Integer id);

    int insert(Orders record);

    int insertSelective(Orders record);

    Orders selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Orders record);

    int updateByPrimaryKey(Orders record);
}
