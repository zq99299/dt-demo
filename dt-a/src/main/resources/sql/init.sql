CREATE TABLE `t_order`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `user_id`    varchar(255)   DEFAULT NULL,
    `product_id` varchar(255)   DEFAULT NULL,
    `count`      int            DEFAULT NULL,
    `money`      decimal(11, 0) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;