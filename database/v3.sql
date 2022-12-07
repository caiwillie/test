ALTER TABLE `mop`.`mop_process_release_deploy` MODIFY COLUMN `process_id` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;
ALTER TABLE `mop`.`mop_process_snapshot_deploy` MODIFY COLUMN `process_id` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;
ALTER TABLE `mop`.`mop_scene_release_deploy` MODIFY COLUMN `process_id` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL;
ALTER TABLE `mop`.`mop_version_process` MODIFY COLUMN `process_id` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;
ALTER TABLE `mop`.`mop_proxy_endpoint_scene` MODIFY COLUMN `process_id` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;