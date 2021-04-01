package com.java.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Users {
    private Integer id;
    private String user_name;
    private String user_password;
    private String user_email;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
//    private Date create_time;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
//    private Date update_time;

    public Integer getId() {
        return id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_password() {
        return user_password;
    }

    public String getUser_email() {
        return user_email;
    }

//    public Date getCreate_time() {
//        return create_time;
//    }
//
//    public Date getUpdate_time() {
//        return update_time;
//    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }



    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

//    public void setCreate_time(Date create_time) {
//        this.create_time = create_time;
//    }
//
//    public void setUpdate_time(Date update_time) {
//        this.update_time = update_time;
//    }
//
//    public Users(Date create_time, Date update_time) {
//        this.create_time = create_time;
//        this.update_time = update_time;
//    }
    public Users(){

    }

    public Users(String user_name, String user_password, String user_email) {
        this.user_name = user_name;
        this.user_password = user_password;
        this.user_email = user_email;
    }

    public Users(String user_name, String user_password) {
        this.user_name = user_name;
        this.user_password = user_password;
    }

    @Override
    public String toString() {
        return "Users{" +
                "user_name='" + user_name + '\'' +
                ", user_email='" + user_email + '\'' +
                '}';
    }
}
