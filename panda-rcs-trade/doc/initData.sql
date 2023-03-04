CREATE TABLE `rcs_tournament_play_margin_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sport_id` int(11) DEFAULT NULL,
  `play_id` int(11) DEFAULT NULL,
  `play_name` varchar(255) COLLATE utf8mb4_0900_as_cs DEFAULT NULL,
  `market_type` int(50) DEFAULT NULL COMMENT '0:马来盘1：欧洲盘',
  `margin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=512 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;



insert into rcs_tournament_play_margin_template
(sport_id,play_id,play_name,market_type,margin,live_market_type,live_margin)

SELECT
	mc.sport_id,
	c.id,
	li.text,
	case when c.id in (4,19,33,113,121,128,130,143,232,2,10,11,18,26,34,87,88,97,98,114,115,116,122,123,124,127,134,233) then 0 else 1 end,
	case when c.id in (4,19,33,113,121,128,130,143,232,2,10,11,18,26,34,87,88,97,98,114,115,116,122,123,124,127,134,233) then 0.1 else 1.1 end,
	case when c.id in (4,19,33,113,121,128,130,143,232,2,10,11,18,26,34,87,88,97,98,114,115,116,122,123,124,127,134,233) then 0 else 1 end,
	case when c.id in (4,19,33,113,121,128,130,143,232,2,10,11,18,26,34,87,88,97,98,114,115,116,122,123,124,127,134,233) then 0.1 else 1.1 end
FROM
	standard_sport_market_category AS c
	LEFT JOIN standard_sport_market_category_ref AS mc ON mc.category_id = c.id
	AND mc.`status` = 1
	AND c.`status` = 1
	and mc.sport_id=1
	LEFT JOIN language_internation AS li ON li.name_code = c.name_code
	AND li.language_type = 'zs' and not c.id is null ;