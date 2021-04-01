package com.java.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class OrdersInVuex {
    private Integer Item_id;
    private String title;
    private String img;
    private Float price;
    private String create_time;
    private String ocode;
    private Integer number;
    private Float total;

    public OrdersInVuex(){

    }
    public OrdersInVuex(Integer Item_id,String title, String img, Float price, String create_time, String ocode, Integer number) {
        this.Item_id=Item_id;
        this.title = title;
        this.img = img;
        this.price = price;
        this.create_time = create_time;
        this.ocode = ocode;
        this.number = number;
        this.total = 0.00f;
    }

    public Integer getItem_id() {
        return Item_id;
    }

    public void setItem_id(Integer item_id) {
        Item_id = item_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getOcode() {
        return ocode;
    }

    public void setOcode(String ocode) {
        this.ocode = ocode;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }
}
