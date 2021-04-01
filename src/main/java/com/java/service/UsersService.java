package com.java.service;
import com.java.pojo.Users;
import java.util.List;

public interface UsersService {
    void add(Users c);
    void delete(int id);
    void update(Users c);
    Users get(int id);
    List<Users> list();
    Boolean isExist(String name);
    Users getByNameAndPassword(String name, String password);
    Users getByName(String name);

}
