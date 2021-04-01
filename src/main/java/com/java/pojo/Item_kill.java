package com.java.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Item_kill {
    private Integer id;
    private Integer item_id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date item_kill_seckillStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date item_kill_seckillEndTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public void setItem_id(Integer item_id) {
        this.item_id = item_id;
    }

    public Date getItem_kill_seckillStartTime() {
        return item_kill_seckillStartTime;
    }

    public void setItem_kill_seckillStartTime(Date item_kill_seckillStartTime) {
        this.item_kill_seckillStartTime = item_kill_seckillStartTime;
    }

    public Date getItem_kill_seckillEndTime() {
        return item_kill_seckillEndTime;
    }

    public void setItem_kill_seckillEndTime(Date item_kill_seckillEndTime) {
        this.item_kill_seckillEndTime = item_kill_seckillEndTime;
    }
}
