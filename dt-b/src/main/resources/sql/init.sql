CREATE TABLE `t_storage`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `product_id` varchar(255) DEFAULT NULL,
    `total`      int          DEFAULT NULL COMMENT '总库存',
    `used`       int          DEFAULT NULL COMMENT '已使用库存',
    `residue`    int          DEFAULT NULL COMMENT '可用库存',
    `frozen`     int          DEFAULT '0' COMMENT '冻结库存',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `dt_demo_b`.`t_storage` (`id`, `product_id`, `total`, `used`, `residue`, `frozen`) VALUES (1, '1', 100, 0, 100, 0);