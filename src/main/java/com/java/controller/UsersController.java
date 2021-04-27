package com.java.controller;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xjs.ezprofiler.annotation.Profiler;
import com.google.common.util.concurrent.RateLimiter;
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
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import org.springframework.web.util.HtmlUtils;
import org.springframework.amqp.core.AmqpTemplate;

@Profiler
@RestController
public class UsersController {
    @Autowired
    UsersService usersService;
    @Autowired
    ItemService itemService;
    @Autowired
    Item_killService item_killService;
    @Autowired
    OrdersService ordersService;
    @Autowired
    private AmqpTemplate rabbitTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

    RateLimiter rateLimiter = RateLimiter.create(10);

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
            int stock=item.getitem_stock()-item.getItem_sale();
            ItemInVuex itemInVuex=new ItemInVuex(item_kill.getId(),item.getItem_title(),item.getItem_img(),item.getItem_price(),stock,item_kill.getItem_kill_seckillStartTime(),item_kill.getItem_kill_seckillEndTime());
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

    @PostMapping("buy")
    public String createOrder0(@RequestBody Map<String,Object> para) throws JsonProcessingException {
        LOGGER.info("访问/buy");
        int user_id=getUserId();
        Integer orders_number=(Integer)para.get("orders_number");
        Integer item_kill_id=(Integer)para.get("item_kill_id");
        int item_id =item_killService.get(item_kill_id).getItem_id();

        // 非阻塞式获取令牌——初始化了令牌桶类，每秒放行10个请求
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            LOGGER.warn("mq：你被限流了，真不幸，直接返回失败");
            return "你被限流了，请稍后再试";
        }
        LOGGER.info("mq：未被限流");
        // 单一用户访问控制
        int count = usersService.addUserCount(user_id);
        LOGGER.info("mq：用户截至该次的访问次数为: [{}]", count);
        boolean isBanned = usersService.getUserIsBanned(user_id);
        if (isBanned) {
            LOGGER.warn("mq：购买失败，超过频率限制");
            return "操作过于频繁，请稍后再试";
        }

        try{
            // 没有下单过，检查缓存中商品是否还有库存
            // 注意这里的有库存和已经下单都是缓存中的结论，存在不可靠性，在消息队列中会查表再次验证
            LOGGER.info("server：没有抢购过，检查缓存中商品是否还有库存");
            Integer leftStock = ordersService.checkStockWithRedisNoThrow(item_id,orders_number);
            if (leftStock<0) {
                return "秒杀请求失败，库存不足.....";
            }
            LOGGER.info("server：库存足够，预计剩余[{}]",leftStock);
            //初始化订单变量
            Orders orders = ordersService.initOrder(item_id,orders_number,user_id);
            //异步生成订单
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("orders", orders);
            jsonObject.put("item_id", item_id);
            sendToordersQueue(jsonObject.toJSONString());
            return "订单生成";
        } catch (Exception e) {
            LOGGER.error("server：下单接口：异步处理订单异常：", e);
            return "下单失败";
        }
    }
    public int getUserId(){
        String userName=TokenUtil.getUserName();
        return usersService.getByName(userName).getId();
    }
//================压测代码============================================================================
    /**
     * 下单接口：导致超卖的错误示范
     * @return
     */
    @GetMapping("/createWrongOrder")
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
    @GetMapping("/createOptimisticOrder")
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
    @GetMapping("/createPessimisticOrder")
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


    @GetMapping("/getStockByCache")
    public String getStockByCache() {
        Integer count;
        int sid =6;
        try {
            count = itemService.getStockCountByCache(sid);//从缓存获得剩余库存
            if (count == null) {//缓存不命中
                count = itemService.getStockCountByDB(sid);//从数据库获得剩余库存
                LOGGER.info("Redis缓存未命中，查询数据库，并写入缓存");
                itemService.setStockCountCache(sid, count);//保存到缓存
            }
        } catch (Exception e) {
            LOGGER.error("Redis查询库存失败：[{}]", e.getMessage());
            return "Cache查询库存失败";
        }
//        LOGGER.info("商品Id: [{}] 剩余库存为: [{}]", sid, count);
        return "Cache查询库存成功,剩余库存为:"+count;
    }
    @GetMapping("/getStockByDB")
    public String getStockByDB() {
        int count;
        int sid =6;
        try {
            count = itemService.getStockCountByDB(sid);//从数据库获得剩余库存
        } catch (Exception e) {
            LOGGER.error("DB查询库存失败：[{}]", e.getMessage());
            return "DB查询库存失败";
        }
//        LOGGER.info("商品Id: [{}] 剩余库存为: [{}]", sid, count);
        return "DB查询库存成功,剩余库存为:"+count;
    }

    @GetMapping(value = "/createUserOrderWithRedis")
    public String createUserOrderWithRedis(@RequestParam(value = "user_id") Integer user_id,
                                           @RequestParam(value = "orders_number") Integer orders_number,
                                           @RequestParam(value = "item_kill_id") Integer item_kill_id){
//        int item_id =item_killService.get(item_kill_id).getItem_id();
        int item_id = item_kill_id;
        int id;
        try {
            id = ordersService.createOrderWithRedis(item_id,orders_number,user_id);
            LOGGER.info("购买成功，剩余库存为: [{}]", id);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", id);
    }
    @GetMapping(value = "/createUserOrderWithMQ")
    public String createUserOrderWithMQ(@RequestParam(value = "user_id") Integer user_id,
                                      @RequestParam(value = "orders_number") Integer orders_number,
                                      @RequestParam(value = "item_kill_id") Integer item_kill_id){
        int item_id = item_kill_id;
        try{
            // 没有下单过，检查缓存中商品是否还有库存
            // 注意这里的有库存和已经下单都是缓存中的结论，存在不可靠性，在消息队列中会查表再次验证
            LOGGER.info("server：没有抢购过，检查缓存中商品是否还有库存");
            Integer leftStock = ordersService.checkStockWithRedisNoThrow(item_id,orders_number);
            if (leftStock<0) {
                return "秒杀请求失败，库存不足.....";
            }
            LOGGER.info("server：库存足够，预计剩余[{}]",leftStock);
            //初始化订单变量
            Orders orders = ordersService.initOrder(item_id,orders_number,user_id);
            //异步生成订单
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("orders", orders);
            jsonObject.put("item_id", item_id);
            sendToordersQueue(jsonObject.toJSONString());
            return "订单生成";
        } catch (Exception e) {
            LOGGER.error("server：下单接口：异步处理订单异常：", e);
            return "秒杀请求失败，服务器正忙.....";
        }
    }
    /**
     * 向消息队列ordersQueue发送消息
     * @param message
     */
    private void sendToordersQueue(String message) {
        LOGGER.info("server：这就去通知消息队列开始下单：[{}]", message);
        this.rabbitTemplate.convertAndSend("ordersQueue", message);
    }
}
