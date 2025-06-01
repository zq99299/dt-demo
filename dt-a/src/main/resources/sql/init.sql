CREATE TABLE `t_order`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `user_id`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户ID',
    `product_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '产品 ID',
    `count`      int                                                           DEFAULT NULL COMMENT '购买数量',
    `money`      decimal(11, 0)                                                DEFAULT NULL COMMENT '订单金额',
    `status`     int                                                           DEFAULT NULL COMMENT '订单状态：1 待确认、2已确认、已取消',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';