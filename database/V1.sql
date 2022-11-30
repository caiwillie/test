CREATE TABLE `mop_de_model` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_by` datetime DEFAULT NULL,
`create_time` varchar(64) DEFAULT NULL,
`update_by` varchar(64) DEFAULT NULL,
`update_time` datetime DEFAULT NULL,
`name` varchar(64) DEFAULT NULL,
`model_key` varchar(64) DEFAULT NULL,
`editor_xml` longtext,
PRIMARY KEY (`id`),
UNIQUE KEY `mop_de_model_id_uindex` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_env` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`deploy_time` datetime DEFAULT NULL,
`name` varchar(64) NOT NULL,
`namespace` varchar(64) NOT NULL,
`status` varchar(64) NOT NULL,
`type` tinyint(2) NOT NULL COMMENT '1 sandbox; 2 custom',
`description` varchar(255) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `mop_env_service` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`name` varchar(64) DEFAULT NULL,
`env_id` bigint(20) DEFAULT NULL,
`cluster_ip` varchar(64) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_process_definition` (
`id` varchar(64) NOT NULL,
`create_time` datetime DEFAULT NULL,
`update_time` datetime DEFAULT NULL,
`name` varchar(64) DEFAULT NULL,
`xml` longtext,
`img_url` longtext,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_process_deploy` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`process_id` varchar(64) NOT NULL,
`process_name` varchar(64) NOT NULL,
`process_xml` longtext NOT NULL,
`version` int(11) NOT NULL,
`type` int(11) NOT NULL,
`zeebe_key` bigint(20) NOT NULL,
`zeebe_xml` longtext NOT NULL,
`trigger_type` varchar(256) NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_process_deploy_annotation` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`process_id` varchar(64) DEFAULT NULL,
`key` varchar(64) DEFAULT NULL,
`value` varchar(255) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_process_release_deploy` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`env_id` bigint(20) NOT NULL,
`process_id` varchar(64) NOT NULL,
`process_zeebe_key` bigint(20) NOT NULL,
`process_zeebe_version` int(11) NOT NULL,
`process_zeebe_xml` longtext NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_process_snapshot_deploy` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`env_id` bigint(20) NOT NULL,
`process_id` varchar(64) NOT NULL,
`process_zeebe_key` bigint(20) NOT NULL,
`process_zeebe_version` int(11) NOT NULL,
`process_xml` longtext NOT NULL,
`process_zeebe_xml` longtext NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `mop_reverse_proxy` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`name` varchar(16) NOT NULL,
`protocol` int(11) DEFAULT NULL,
`version` varchar(64) NOT NULL,
`description` varchar(128) DEFAULT NULL,
`domain` varchar(128) DEFAULT NULL,
`state` int(11) DEFAULT NULL COMMENT '状态：1 停止，2 运行',
`tag` varchar(64) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_reverse_proxy_endpoint` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`proxy_id` bigint(20) NOT NULL,
`location` varchar(128) NOT NULL,
`description` varchar(128) DEFAULT NULL,
`backend_type` int(11) DEFAULT NULL,
`backend_config` text,
`tag` varchar(64) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_scene` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_time` datetime DEFAULT NULL,
`update_time` datetime DEFAULT NULL,
`name` varchar(64) DEFAULT NULL,
`create_by` varchar(64) DEFAULT NULL,
`update_by` varchar(64) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_scene_load` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`zip_bytes` longblob,
`create_by` varchar(64) DEFAULT NULL,
`create_time` datetime NOT NULL,
`update_by` varchar(64) DEFAULT NULL,
`update_time` datetime DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_scene_process` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`business_scene_id` bigint(20) NOT NULL,
`process_id` varchar(128) NOT NULL,
PRIMARY KEY (`id`),
UNIQUE KEY `mop_business_scene_process_process_id_uindex` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_scene_version` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`version` varchar(64) NOT NULL,
`scene_id` bigint(20) NOT NULL,
`status` int(11) NOT NULL,
`env_list` varchar(255) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mop_version_process` (
`id` bigint(20) NOT NULL,
`create_time` datetime NOT NULL,
`update_time` datetime NOT NULL,
`version_id` bigint(20) NOT NULL,
`process_id` varchar(64) NOT NULL,
`process_name` varchar(64) NOT NULL,
`process_xml` longtext NOT NULL,
`process_img` longtext,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;