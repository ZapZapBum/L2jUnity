CREATE TABLE IF NOT EXISTS `clan_variables` (
  `clanId` int(10) UNSIGNED NOT NULL,
  `var` varchar(255) NOT NULL,
  `val` text NOT NULL,
  KEY `clanId` (`clanId`)
);