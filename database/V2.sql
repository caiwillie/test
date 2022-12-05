-- 删除字段 mop_scene_version.env_list
ALTER TABLE `mop_scene_version`DROP COLUMN `env_list`;

-- 新增表 mop_scene_release_deploy
CREATE TABLE `mop_scene_release_deploy` (
`id` bigint(20) NOT NULL,
`create_time` datetime DEFAULT NULL,
`update_time` datetime DEFAULT NULL,
`scene_id` bigint(20) DEFAULT NULL,
`scene_name` varchar(64) DEFAULT NULL,
`version_id` bigint(20) DEFAULT NULL,
`version_name` varchar(64) DEFAULT NULL,
`process_id` varchar(64) DEFAULT NULL,
`process_name` varchar(64) DEFAULT NULL,
`env_id` bigint(20) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 创建 mop_proxy_endpoint_scene
CREATE TABLE `mop_proxy_endpoint_scene` (
`id` bigint(20) NOT NULL,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`endpoint_id` bigint(20) NOT NULL,
`env_id` bigint(20) NOT NULL,
`env_name` varchar(64) NOT NULL,
`scene_id` bigint(20) NOT NULL,
`scene_name` varchar(64) NOT NULL,
`version_id` bigint(20) NOT NULL,
`version_name` varchar(64) NOT NULL,
`process_id` varchar(64) NOT NULL,
`process_name` varchar(64) NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 创建表 `mop_proxy_endpoint_call`
CREATE TABLE `mop_proxy_endpoint_call` (
`id` bigint(20) NOT NULL,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`endpoint_id` bigint(20) NOT NULL,
`ip` varchar(255) DEFAULT NULL,
`mac` varchar(255) DEFAULT NULL,
`user_agent` varchar(255) DEFAULT NULL,
`http_method` varchar(255) DEFAULT NULL,
`http_status` varchar(255) DEFAULT NULL,
`http_query` varchar(1024) DEFAULT NULL,
`http_body` text,
`time_consuming` int(11) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 新增 mop_env_service 表中的端口字段
ALTER TABLE `mop`.`mop_env_service` ADD COLUMN `ports` varchar(64) NULL AFTER `cluster_ip`;

-- 修改 mop_proxy_endpoint_call 的字段
ALTER TABLE `mop`.`mop_proxy_endpoint_call`
CHANGE COLUMN `ip` `ip_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
CHANGE COLUMN `mac` `mac_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
CHANGE COLUMN `http_query` `request_query` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
CHANGE COLUMN `http_body` `request_body` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
ADD COLUMN `response_body` text NULL,
ADD COLUMN `error_message` text NULL;