package com.java.util;

import com.java.pojo.ItemInVuex;

import java.util.Comparator;

public class ComparatorItems implements Comparator<ItemInVuex> {

    @Override
    public int compare(ItemInVuex o1, ItemInVuex o2) {//把end-start高的放前面
        long num= (o2.getSeckillEndTime().getTime()-o2.getSeckillStartTime().getTime())-(o1.getSeckillEndTime().getTime()-o1.getSeckillStartTime().getTime());
        if(num<0.00)
            return -1;
        return 1;
    }
}
