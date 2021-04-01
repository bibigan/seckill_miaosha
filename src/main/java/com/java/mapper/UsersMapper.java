package com.java.mapper;

import com.java.pojo.Users;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersMapper {
    @Select("select * from users ")
    List<Users> findAll();

    @Insert(" insert into users ( user_name,user_password,user_email ) values (#{user_name},#{user_password},#{user_email}) ")
    public int save(Users users);

    @Delete(" delete from users where id= #{id} ")
    public void delete(int id);

    @Select("select * from users where id= #{id} ")
    public Users get(int id);

    @Update("update users set user_name=#{user_name},user_password=#{user_password},user_email=#{user_email} where id=#{id} ")
    public int update(Users users);

    @Select("select * from users where user_name= #{user_name} ")
    public Users findByName(String user_name);

    @Select("select * from users where user_name= #{user_name} and user_password= #{user_password}")
    public Users findByNameAndPassword(@Param("user_name")String user_name, @Param("user_password")String user_password);
}
