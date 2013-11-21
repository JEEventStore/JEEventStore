
CREATE TABLE `event_store` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bucket_id` varchar(255) DEFAULT NULL,
  `stream_id` varchar(255) DEFAULT NULL,
  `stream_version` bigint(20) DEFAULT NULL,
  `change_set_id` varchar(255) DEFAULT NULL,
  `persisted_at` bigint(20) DEFAULT NULL,
  `body` longtext,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_event_store_optimistic_lock` (`bucket_id`,`stream_id`,`stream_version`),
  UNIQUE KEY `UNQ_event_store_change_set` (`change_set_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

