use seckill;

CREATE TABLE users (
  id int(11) unsigned NOT NULL auto_increment,
  user_name varchar(255) character set utf8mb4 not null COMMENT '用户名',
  user_password varchar(255) character set utf8mb4 not null COMMENT '密码',
  user_email varchar(100) character set utf8mb4 not null COMMENT '邮箱',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户表';

CREATE TABLE item (
  id int(11) unsigned NOT NULL auto_increment,
  item_title varchar(255) NOT NULL DEFAULT '' COMMENT '商品名',
  item_img varchar(255) NOT NULL DEFAULT '' COMMENT '图片文件名',
  item_stock int(11) NOT NULL COMMENT '库存',
  item_sale int(11) NOT NULL COMMENT '已售',
  item_price float NOT NULL COMMENT '单价',
  version int(11) NOT NULL COMMENT '乐观锁，版本号',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品表';

CREATE TABLE item_kill (
  id int(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  item_id int(11) NOT NULL COMMENT '商品id',
  item_kill_seckillStartTime datetime NOT NULL COMMENT '秒杀开始时间',
  item_kill_seckillEndTime datetime NOT NULL COMMENT '秒杀结束时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='待秒杀商品表';

CREATE TABLE orders (
  id int(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  orders_ocode varchar(50) NOT NULL COMMENT '订单编号',
  orders_number int(11) NOT NULL COMMENT '数量',
  item_id int(11) NOT NULL COMMENT '商品id',
  item_kill_id int(11) NOT NULL COMMENT '秒杀id',
  user_id int(11) NOT NULL COMMENT '用户id',
  orders_create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秒杀成功订单表';

INSERT INTO `item` (`id`, `item_title`, `item_img`, `item_stock`, `item_sale`, `item_price`,`version`) VALUES ('1', '瑞典钓鱼', 'autumnclouds.jpg', '20','0', '3424.00','0');
INSERT INTO `item` (`id`, `item_title`, `item_img`, `item_stock`, `item_sale`, `item_price`,`version`) VALUES ('2', '春季巴黎', 'christmas.jpg', '20','0', '2124.00','0');
INSERT INTO `item` (`id`, `item_title`, `item_img`, `item_stock`, `item_sale`, `item_price`,`version`) VALUES ('3', '烟火晚宴', 'july4.jpg', '20','0', '899.00','0');
INSERT INTO `item` (`id`, `item_title`, `item_img`, `item_stock`, `item_sale`, `item_price`,`version`) VALUES ('4', '户外野炊', 'firepots.jpg', '20','0', '654.00','0');
INSERT INTO `item` (`id`, `item_title`, `item_img`, `item_stock`, `item_sale`, `item_price`,`version`) VALUES ('5', '水底冒险', 'jellyfish.jpg', '20','0', '664.00','0');
INSERT INTO `item` (`id`, `item_title`, `item_img`, `item_stock`, `item_sale`, `item_price`,`version`) VALUES ('6', '山壁小屋', 'mountaincabin.jpg', '20','0', '678.00','0');

INSERT INTO `item_kill` (`id`, `item_id`, `item_kill_seckillStartTime`, `item_kill_seckillEndTime`) VALUES ('1', '1', '2021-02-01 01:13:13', '2021-04-11 01:13:13');
INSERT INTO `item_kill` (`id`, `item_id`, `item_kill_seckillStartTime`, `item_kill_seckillEndTime`) VALUES ('2', '2', '2021-02-27 01:13:13', '2021-04-01 01:13:13');
INSERT INTO `item_kill` (`id`, `item_id`, `item_kill_seckillStartTime`, `item_kill_seckillEndTime`) VALUES ('3', '3', '2021-02-22 01:13:13', '2021-04-07 01:13:13');
INSERT INTO `item_kill` (`id`, `item_id`, `item_kill_seckillStartTime`, `item_kill_seckillEndTime`) VALUES ('4', '4', '2021-02-05 01:13:13', '2021-04-17 01:13:13');
INSERT INTO `item_kill` (`id`, `item_id`, `item_kill_seckillStartTime`, `item_kill_seckillEndTime`) VALUES ('5', '5', '2021-02-02 01:13:13', '2021-04-09 01:13:13');
INSERT INTO `item_kill` (`id`, `item_id`, `item_kill_seckillStartTime`, `item_kill_seckillEndTime`) VALUES ('6', '6', '2021-02-01 01:13:13', '2021-04-02 01:13:13');

INSERT INTO `users` (`id`, `user_name`, `user_password`, `user_email`) VALUES ('1', '1', '11111', '1049593374@qq.com');

