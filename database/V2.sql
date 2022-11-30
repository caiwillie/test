-- 删除字段 mop_scene_version.env_list
ALTER TABLE `mop_scene_version`DROP COLUMN `env_list`;

-- 新增表 mop_scene_release_deploy
CREATE TABLE `mop_scene_release_deploy` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
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

