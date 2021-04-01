package com.java.controller;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xjs.ezprofiler.annotation.Profiler;
import com.java.pojo.*;
import com.java.service.ItemService;
import com.java.service.Item_killService;
import com.java.service.OrdersService;
import com.java.service.UsersService;
import com.java.util.ComparatorItems;
import com.java.util.Result;
import com.java.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import org.springframework.web.util.HtmlUtils;
import org.apache.commons.lang.math.RandomUtils;
import javax.servlet.http.HttpSession;

@RestController
@Profiler
public class UsersController {
    @Autowired
    UsersService usersService;
    @Autowired
    ItemService itemService;
    @Autowired
    Item_killService item_killService;
    @Autowired
    OrdersService ordersService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

    @GetMapping("/users")
    public String listUsers() throws Exception {
        String jsonString=JSON.toJSONString(usersService.list());
        return jsonString;
    }
    /*
    1. 通过参数Users获取浏览器提交的账号密码
    2. 通过HtmlUtils.htmlEscape(name);把账号里的特殊符号进行转义
    3. 判断用户名是否存在
    3.1 如果已经存在，就返回Result.fail,并带上错误信息
    3.2 如果不存在，则加入到数据库中，并返回 Result.success()*/
    @PostMapping("/register")
    public String add(@RequestBody Users user) throws Exception {
        System.out.println("访问/register");
        String name =  user.getUser_name();
        name = HtmlUtils.htmlEscape(name);
        boolean exist = usersService.isExist(name);
        if(exist){
            String message ="用户名已经被使用,不能使用";
            return Result.fail(message);
        }
        usersService.add(user);
        return Result.success();
    }

    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestHeader Map<String,Object> he,@RequestBody Map<String,Object> para) throws JsonProcessingException {
        System.out.println("访问login");
        String username=(String)para.get("user_name");
        username = HtmlUtils.htmlEscape(username);
        String password=(String)para.get("user_password");

        Users res=usersService.getByNameAndPassword(username,password);

        ObjectMapper objectMapper=new ObjectMapper();
        if(null!=res){
            System.out.println("登录成功！");
            String token= TokenUtil.sign(res);
            HashMap<String,Object> hs=new HashMap<>();
            hs.put("token","token"+token);
            return objectMapper.writeValueAsString(hs);
        }
        return "error";
    }

    @GetMapping("/items")
    public Object listItems() throws Exception {
        /*先得到ik的list，遍历list，为每个ik找到对应的i，加入到新list*/
        System.out.println("访问/items");
        List<Item_kill> item_kills=item_killService.list();
        List<ItemInVuex> items=new ArrayList<>();
        for (Item_kill item_kill:item_kills){
            int item_id=item_kill.getItem_id();
            Item item=itemService.get(item_id);
            ItemInVuex itemInVuex=new ItemInVuex(item_kill.getId(),item.getItem_title(),item.getItem_img(),item.getItem_price(),item.getitem_stock(),item_kill.getItem_kill_seckillStartTime(),item_kill.getItem_kill_seckillEndTime());
            items.add(itemInVuex);
        }
        Collections.sort(items, new ComparatorItems());
        System.out.println(JSON.toJSONString(items));
        return items;
    }

    @GetMapping("/order")
    public Object listOrders() throws Exception {
//        Users users=(Users) session.getAttribute("user");
        //先根据uid找到其所有的orders,遍历os,依次设置ocode，number，create_time
        //期间通过item_id找到对应的item，设置title,img,price
        System.out.println("访问/order");
        int user_id=getUserId();
        List<OrdersInVuex> orders=new ArrayList<>();
        List<Orders> ordersList=ordersService.listByUid(user_id);
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy/MM/dd HH:mm:ss" );
        for(Orders o:ordersList){
            Item item=itemService.get(o.getItem_id());
            OrdersInVuex ordersInVuex=new OrdersInVuex(o.getItem_id(),item.getItem_title(),item.getItem_img(),item.getItem_price(),sdf.format(o.getOrders_create_time()),o.getOrders_ocode(),o.getOrders_number());
            orders.add(ordersInVuex);
        }
        System.out.println(JSON.toJSONString(orders));
        return orders;
    }

    public int getUserId(){
        String userName=TokenUtil.getUserName();
        return usersService.getByName(userName).getId();
    }

    /**
     * 下单接口：导致超卖的错误示范
     * @return
     */
    @RequestMapping("/createWrongOrder")
    @ResponseBody
    public String createWrongOrder() {
        int item_id=6;
        Integer number=1;
        Integer user_id=1;

        int id = 0;
        try {
            id = ordersService.createWrongOrder(item_id,number,user_id);
            LOGGER.info("创建订单id: [{}]", id);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
        return String.valueOf(id);
    }

    /**
     * 下单接口：乐观锁更新库存 + 令牌桶限流
     * @return
     */
    @RequestMapping("/createOptimisticOrder")
    @ResponseBody
    public String createOptimisticOrder() {
        int item_id=6;
        Integer number=1;
        Integer user_id=1;
        int id;
        try {
            id = ordersService.createOptimisticOrder(item_id,number,user_id);
            LOGGER.info("购买成功，剩余库存为: [{}]", id);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", id);
    }

    /**
     * 下单接口：悲观锁更新库存 事务for update更新库存
     * @return
     */
    @RequestMapping("/createPessimisticOrder")
    public String createPessimisticOrder() {
        int item_id=6;
        Integer number=1;
        Integer user_id=1;
        int id;
        try {
            id = ordersService.createPessimisticOrder(item_id,number,user_id);
            LOGGER.info("购买成功，剩余库存为: [{}]", id);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", id);
    }


}
