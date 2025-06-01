CREATE TABLE `t_storage`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `product_id` varchar(255) DEFAULT NULL,
    `total`      int          DEFAULT NULL,
    `used`       int          DEFAULT NULL,
    `residue`    int          DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 插入一条库存数据
INSERT INTO `dt_demo_b`.`t_storage` (`id`, `product_id`, `total`, `used`, `residue`) VALUES (1, '1', 100, 5, 100);