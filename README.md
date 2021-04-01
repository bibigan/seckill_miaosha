# seckill_miaosha
默认值:
<br>
user_id=1;
item_id=6;
number=1;
<br>
<br>
查看数据库结果：
<br>
use seckill1;

select * from item where id=6;

select count(id) from orders where item_id=6;

update item set item_stock=10000;

update item set item_sale=0;

delete from orders where item_id=6;

select * from item where id=6;

select count(id) from orders where item_id=6;
<br>
<br>
备注：
<br>
1. 去掉了网关和跨域访问
2. Get方法无参下单
3. 无缓存
4. 数据库为seckill1
5. orders去掉了orders_status字段，将创建时间由数据库自动生成
6. item增加sale和version字段
<br>
7. 访问接口情况：
<br>
 http://39.103.137.8:8088/profiler.html
 <br>
 http://39.103.166.28:8088/profiler.html
 <br>
 http://121.89.206.190:8088/profiler.html


