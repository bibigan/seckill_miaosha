package com.java.service;



import com.java.pojo.Item_kill;

import java.util.List;

public interface Item_killService {
    void add(Item_kill c);
    void delete(int id);
    void update(Item_kill c);
    Item_kill get(int id);
    List<Item_kill> list();
}
