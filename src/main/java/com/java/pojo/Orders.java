package com.java.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Orders {
    private Integer id;
    private String orders_ocode;
    private Integer orders_number;
    private Integer item_id;
    private Integer item_kill_id;
    private Integer user_id;
    //    private Byte orders_status;//秒杀结果: -1无效  0成功(未付款)  1已付款  2已取消
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date orders_create_time;

    private String item_title;
    private String item_img;
    private Float price;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrders_ocode() {
        return orders_ocode;
    }

    public void setOrders_ocode(String orders_ocode) {
        this.orders_ocode = orders_ocode;
    }

    public Integer getOrders_number() {
        return orders_number;
    }

    public void setOrders_number(Integer orders_number) {
        this.orders_number = orders_number;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public void setItem_id(Integer item_id) {
        this.item_id = item_id;
    }

    public Integer getItem_kill_id() {
        return item_kill_id;
    }

    public void setItem_kill_id(Integer item_kill_id) {
        this.item_kill_id = item_kill_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

//    public Byte getOrders_status() {
//        return orders_status;
//    }
//
//    public void setOrders_status(Byte orders_status) {
//        this.orders_status = orders_status;
//    }

    public Date getOrders_create_time() {
        return orders_create_time;
    }

    public void setOrders_create_time(Date orders_create_time) {
        this.orders_create_time = orders_create_time;
    }

    public String getItem_title() {
        return item_title;
    }

    public void setItem_title(String item_title) {
        this.item_title = item_title;
    }

    public String getItem_img() {
        return item_img;
    }

    public void setItem_img(String item_img) {
        this.item_img = item_img;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Orders(String orders_ocode, Integer orders_number, Integer item_id, Integer item_kill_id, Integer user_id) {
        this.orders_ocode = orders_ocode;
        this.orders_number = orders_number;
        this.item_id = item_id;
        this.item_kill_id = item_kill_id;
        this.user_id = user_id;
    }

    public Orders() {
    }

    @Override
    public String toString() {
        return "Orders{" +
                "orders_ocode='" + orders_ocode + '\'' +
                ", orders_number=" + orders_number +
                ", item_id=" + item_id +
                ", item_kill_id=" + item_kill_id +
                ", user_id=" + user_id +
                ", orders_create_time=" + orders_create_time +
                '}';
    }
}
