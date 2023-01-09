ALTER TABLE `mop`.`mop_process_snapshot_deploy` ADD COLUMN `process_digest` varchar(255) NULL;

CREATE TABLE `mop_lock` (
    `id` bigint(20) NOT NULL,
    `create_time` datetime NOT NULL,
    `update_time` datetime NOT NULL,
    `resource_digest` varchar(64) NOT NULL,
    `resource_content` varchar(1024) NOT NULL,
    `lock_status` varchar(64) NOT NULL,
    `lock_version` bigint(20) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `resource_digest_uni` (`resource_digest`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `mop_process_deploy_task` (
    `id` bigint(20) NOT NULL,
    `create_time` datetime NOT NULL,
    `update_time` datetime NOT NULL,
    `process_id` varchar(256) NOT NULL,
    `process_name` varchar(64) NOT NULL,
    `process_xml` longtext NOT NULL,
    `process_zeebe_xml` longtext NOT NULL,
    `env_id` bigint(20) NOT NULL,
    `deploy_status` int(11) NOT NULL,
    `error_message` text,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `mop`.`mop_reverse_proxy` ADD COLUMN `delete_flag` bigint(20) NULL;

ALTER TABLE `mop`.`mop_reverse_proxy_endpoint` ADD COLUMN `delete_flag` bigint(20) NULL;

ALTER TABLE `mop`.`mop_version_process` ADD COLUMN `delete_flag` bigint(20) NULL;

ALTER TABLE `mop`.`mop_scene_version` ADD COLUMN `delete_flag` bigint(20) NULL;

ALTER TABLE `mop`.`mop_scene` ADD COLUMN `delete_flag` bigint(20) NULL;

ALTER TABLE `mop`.`mop_scene_release_deploy` ADD COLUMN `delete_flag` bigint(20) NULL;

ALTER TABLE `mop`.`mop_scene_version` ADD COLUMN `deploy_progress_percentage` double NULL;

ALTER TABLE `mop`.`mop_scene_version` ADD COLUMN `exception_message` text NULL;

ALTER TABLE `mop`.`mop_scene_version` ADD COLUMN `deploy_status` int(11) NULL;