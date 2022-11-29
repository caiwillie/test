-- 新增非空约束 mop_scene_version.status
ALTER TABLE `mop`.`mop_scene_version` MODIFY COLUMN `status` int(11) NOT NULL AFTER `scene_id`;

-- 新增字段 mop_scene_version.env_list
ALTER TABLE `mop`.`mop_scene_version` ADD COLUMN `env_list` varchar(255) NULL AFTER `status`;