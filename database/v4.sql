ALTER TABLE `mop`.`mop_process_snapshot_deploy` ADD COLUMN `process_digest` varchar(255) NULL;

CREATE TABLE `mop_lock` (
    `id` bigint(20) NOT NULL,
    `create_time` datetime NOT NULL,
    `update_time` datetime NOT NULL,
    `resource_digest` varchar(64) NOT NULL,
    `resource_content` varchar(1024) NOT NULL,
    `lock_status` tinyint(1) NOT NULL,
    `lock_version` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;