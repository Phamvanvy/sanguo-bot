-- Table "tbl_activity" DDL
CREATE TABLE `tbl_activity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '活动名称-不可重复',
  `begintime` datetime DEFAULT NULL COMMENT '开始时间',
  `endtime` datetime DEFAULT NULL COMMENT '结束时间',
  `valid` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否有效',
  `enable` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否开启',
  `pool` text NOT NULL COMMENT '参数池',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_admin" DDL

CREATE TABLE `tbl_admin` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `password` varchar(255) NOT NULL DEFAULT '',
  `auth` mediumtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_arenateam" DDL

CREATE TABLE `tbl_arenateam` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) NOT NULL DEFAULT '0',
  `arenaname` varchar(255) NOT NULL DEFAULT '',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `owner` int(11) NOT NULL DEFAULT '0',
  `slogan` text,
  `arenalevel` int(11) NOT NULL DEFAULT '0',
  `lastrepairtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `memebercount` int(11) NOT NULL DEFAULT '0',
  `valid` tinyint(4) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `index_arenateam_arenalevel` (`arenalevel`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_auction" DDL

CREATE TABLE `tbl_auction` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL DEFAULT '0',
  `shopid` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `startprice` int(11) NOT NULL DEFAULT '0',
  `currentprice` int(11) NOT NULL DEFAULT '0',
  `endprice` int(11) NOT NULL DEFAULT '0',
  `item` blob NOT NULL,
  `name` varchar(255) NOT NULL DEFAULT '',
  `type` int(11) NOT NULL DEFAULT '0',
  `lastplayerid` int(11) NOT NULL DEFAULT '0',
  `playername` varchar(20) NOT NULL DEFAULT '',
  `quality` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `areaid` int(11) NOT NULL DEFAULT '0',
  `state` int(11) NOT NULL DEFAULT '0',
  `validtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_battlefield" DDL
CREATE TABLE `tbl_battlefield` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `campbattleid` varchar(255) NOT NULL DEFAULT '',
  `playerid` int(11) NOT NULL DEFAULT '0',
  `israndom` int(11) NOT NULL DEFAULT '0',
  `issummon` int(11) NOT NULL DEFAULT '0',
  `camptype` tinyint(4) NOT NULL DEFAULT '0',
  `campbattlefieldtype` varchar(255) NOT NULL DEFAULT '',
  `killpoint` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `pool` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_bbs" DDL

CREATE TABLE `tbl_bbs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bbsid` int(11) NOT NULL DEFAULT '0',
  `playerid` int(11) NOT NULL DEFAULT '0',
  `playername` varchar(255) NOT NULL DEFAULT '',
  `title` varchar(255) NOT NULL DEFAULT '',
  `content` mediumtext,
  `posttime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `priority` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_bbs_bbsid` (`bbsid`),
  KEY `index_bbs_priority` (`priority`),
  KEY `index_bbs_posttime` (`posttime`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


-- Table "tbl_billing" DDL
CREATE TABLE `tbl_billing` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `msisdn` varchar(40) DEFAULT NULL,
  `balance` int(11) DEFAULT NULL,
  `feeplan` int(11) DEFAULT NULL,
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `lastbillingtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `credit` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- Table "tbl_bindrequest" DDL
CREATE TABLE `tbl_bindrequest` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL DEFAULT '',
  `accountid` int(11) NOT NULL DEFAULT '0',
  `randomstring` varchar(255) NOT NULL DEFAULT '',
  `content` varchar(255) NOT NULL DEFAULT '',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `used` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `randomstring` (`randomstring`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- Table "tbl_blog" DDL

CREATE TABLE `tbl_blog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL DEFAULT '0',
  `playername` varchar(100) NOT NULL DEFAULT '',
  `title` varchar(255) NOT NULL DEFAULT '',
  `content` mediumtext NOT NULL,
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `readedtimes` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_buy" DDL

CREATE TABLE `tbl_buy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shopid` int(11) NOT NULL DEFAULT '0',
  `itemid` int(11) NOT NULL DEFAULT '0',
  `total` int(11) NOT NULL DEFAULT '0',
  `current` int(11) NOT NULL DEFAULT '0',
  `price` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `name` varchar(255) NOT NULL DEFAULT '',
  `type` int(11) NOT NULL DEFAULT '0',
  `areaid` int(11) NOT NULL DEFAULT '0',
  `state` int(11) NOT NULL DEFAULT '0',
  `quality` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_camp" DDL
CREATE TABLE `tbl_camp` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `camp` tinyint(4) NOT NULL COMMENT '阵营',
  `kingid` int(11) NOT NULL COMMENT '领袖角色id',
  `createtime` datetime NOT NULL COMMENT '创建时间',
  `lasttime` datetime NOT NULL COMMENT '最后操作时间',
  `money` bigint(20) NOT NULL COMMENT '阵营资金',
  `taxrate` int(11) NOT NULL COMMENT '当前税率',
  `skills` blob NOT NULL COMMENT '阵营科技',
  `slogan` text NOT NULL COMMENT '阵营公告',
  `pool` text NOT NULL COMMENT '参数池',
  `valid` tinyint(4) NOT NULL COMMENT '是否有效',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='阵营表';

-- Table "tbl_campcandidate" DDL
CREATE TABLE `tbl_campcandidate` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `playerid` int(11) NOT NULL COMMENT '角色id',
  `createtime` datetime NOT NULL COMMENT '创建时间',
  `lasttime` datetime NOT NULL COMMENT '最后操作时间',
  `camp` tinyint(4) NOT NULL COMMENT '阵营',
  `preking` tinyint(4) NOT NULL COMMENT '是否前国王(0: 不是, 1: 是)',
  `totalvote` int(11) NOT NULL COMMENT '总计票数',
  `normalvote` int(11) NOT NULL COMMENT '真心支持票数',
  `itemvote` int(11) NOT NULL COMMENT '鲜花票数',
  `ishopitemvote` int(11) NOT NULL COMMENT '蓝色妖姬票数',
  `magicvote` int(11) NOT NULL COMMENT '魔力分享票数',
  `magicremain` int(11) NOT NULL COMMENT '魔力分享剩余票数',
  `eggvote` int(11) NOT NULL COMMENT '臭鸡蛋票数',
  `slogan` text NOT NULL COMMENT '竞选宣言',
  `valid` tinyint(4) NOT NULL COMMENT '是否有效',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='候选人表';

-- Table "tbl_campqualification" DDL
CREATE TABLE `tbl_campqualification` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `playerid` int(11) NOT NULL COMMENT '角色id',
  `createtime` datetime NOT NULL COMMENT '创建时间',
  `lasttime` datetime NOT NULL COMMENT '最后操作时间',
  `camp` tinyint(4) NOT NULL COMMENT '阵营',
  `total` int(11) NOT NULL COMMENT '总计投入荣誉',
  `added` int(11) NOT NULL COMMENT '追加的荣誉',
  `addcount` int(11) NOT NULL COMMENT '追加次数',
  `remain` int(11) NOT NULL COMMENT '角色剩余荣誉',
  `level` tinyint(4) NOT NULL COMMENT '角色级别',
  `valid` tinyint(4) NOT NULL COMMENT '是否有效',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='竞选资格表';

-- Table "tbl_camptech" DDL

CREATE TABLE `tbl_camptech` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `kingid` int(11) NOT NULL DEFAULT '0',
  `kingname` varchar(255) NOT NULL DEFAULT '',
  `camp` tinyint(4) NOT NULL DEFAULT '1',
  `credit` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `moeny` bigint(20) NOT NULL DEFAULT '0',
  `percent` int(11) NOT NULL DEFAULT '0',
  `campmoeny` int(11) NOT NULL DEFAULT '0',
  `integral` int(11) NOT NULL DEFAULT '0',
  `technology` blob,
  `valid` tinyint(4) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `index_kingid` (`kingid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_campvote" DDL
CREATE TABLE `tbl_campvote` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `playerid` int(11) NOT NULL COMMENT '角色id',
  `camp` tinyint(4) NOT NULL COMMENT '阵营',
  `createtime` datetime NOT NULL COMMENT '创建时间',
  `lasttime` datetime NOT NULL COMMENT '最后操作时间',
  `totalvote` int(11) NOT NULL COMMENT '总计票数',
  `normalvote` int(11) NOT NULL COMMENT '真心支持票数',
  `itemvote` int(11) NOT NULL COMMENT '鲜花票数',
  `ishopitemvote` int(11) NOT NULL COMMENT '蓝色妖姬票数',
  `magicvote` int(11) NOT NULL COMMENT '魔力分享票数',
  `eggvote` int(11) NOT NULL COMMENT '臭鸡蛋票数',
  `valid` tinyint(4) NOT NULL COMMENT '是否有效',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='竞选投票表';

-- Table "tbl_charge" DDL
CREATE TABLE `tbl_charge` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountid` int(11) DEFAULT NULL,
  `playerid` int(11) DEFAULT NULL,
  `playerlevel` int(11) DEFAULT NULL,
  `money` int(11) DEFAULT NULL,
  `chargetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_chargehistory" DDL
CREATE TABLE `tbl_chargehistory` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account` int(11) NOT NULL DEFAULT '0',
  `msisdn` varchar(40) NOT NULL DEFAULT '',
  `serviceno` varchar(40) NOT NULL DEFAULT '',
  `smscontent` varchar(80) NOT NULL DEFAULT '',
  `fee` float NOT NULL DEFAULT '0',
  `addpoints` int(11) NOT NULL DEFAULT '0',
  `chargetime` bigint(20) NOT NULL DEFAULT '0',
  `chargestate` tinyint(4) NOT NULL DEFAULT '0',
  `finishtime` bigint(20) NOT NULL DEFAULT '0',
  `phoneNumber` varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- Table "tbl_chargeplan" DDL
CREATE TABLE `tbl_chargeplan` (
  `id` int(11) NOT NULL DEFAULT '0',
  `serviceno` varchar(40) NOT NULL DEFAULT '',
  `smscontent` varchar(80) NOT NULL DEFAULT '',
  `fee` float NOT NULL DEFAULT '0',
  `addpoints` int(11) NOT NULL DEFAULT '0',
  `message` varchar(255) NOT NULL DEFAULT '',
  `monthmax` float NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- Table "tbl_farm" DDL
CREATE TABLE `tbl_farm` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL DEFAULT '0',
  `playername` varchar(255) NOT NULL DEFAULT '',
  `landcount` tinyint(4) NOT NULL DEFAULT '0',
  `landinfo` blob,
  `otherpool` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `index_playerid` (`playerid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_freeuser" DDL
CREATE TABLE `tbl_freeuser` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountid` int(11) NOT NULL DEFAULT '0',
  `playerid` int(11) NOT NULL DEFAULT '0',
  `freeTime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- Table "tbl_friend" DDL

CREATE TABLE `tbl_friend` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL DEFAULT '0',
  `playername` varchar(255) DEFAULT '',
  `friendplayerid` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `imoney` int(11) NOT NULL DEFAULT '0',
  `valid` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_gift" DDL

CREATE TABLE `tbl_gift` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupid` int(11) NOT NULL DEFAULT '0',
  `playerid` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `modifytime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `rcount` int(11) NOT NULL DEFAULT '0',
  `count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `gfit_index` (`groupid`,`playerid`),
  KEY `group_index` (`groupid`),
  KEY `player_index` (`playerid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;

-- Table "tbl_hopegrass" DDL

CREATE TABLE `tbl_hopegrass` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL DEFAULT '0',
  `mapid` int(11) NOT NULL DEFAULT '0',
  `x` int(11) NOT NULL DEFAULT '0',
  `y` int(11) NOT NULL DEFAULT '0',
  `itemgroupid` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `validtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `obsoletetime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `grasstype` int(11) NOT NULL DEFAULT '0',
  `ratio` int(11) NOT NULL DEFAULT '0',
  `grouprnd` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `playerid` (`playerid`),
  KEY `mapid` (`mapid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_house" DDL

CREATE TABLE `tbl_house` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL DEFAULT '0',
  `playername` varchar(100) NOT NULL DEFAULT '',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `level` int(11) NOT NULL DEFAULT '0',
  `style` int(11) NOT NULL DEFAULT '0',
  `rule` int(11) NOT NULL DEFAULT '0',
  `areaid` int(11) NOT NULL DEFAULT '0',
  `gridsize` int(11) NOT NULL DEFAULT '0',
  `items` blob,
  `parts` blob,
  `lasttime` datetime DEFAULT NULL,
  `title` mediumtext NOT NULL,
  `waiterid` int(11) NOT NULL DEFAULT '0',
  `visitedtimes` int(11) NOT NULL DEFAULT '0',
  `usedimoney` int(11) NOT NULL DEFAULT '0',
  `leavemessagetimes` int(11) NOT NULL DEFAULT '0',
  `canusewaitertime` datetime DEFAULT NULL,
  `autobuywaiter` int(11) NOT NULL DEFAULT '0',
  `addgridsize` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `playerid` (`playerid`),
  KEY `house_visitedtimes` (`visitedtimes`),
  KEY `house_usedimoney` (`usedimoney`),
  KEY `house_leavemessagetimes` (`leavemessagetimes`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_ibuy" DDL

CREATE TABLE `tbl_ibuy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountid` int(11) NOT NULL DEFAULT '0',
  `playerid` int(11) NOT NULL DEFAULT '0',
  `itemid` int(11) NOT NULL DEFAULT '0',
  `itemname` varchar(255) NOT NULL DEFAULT '',
  `type` tinyint(4) NOT NULL DEFAULT '0',
  `imoney` int(11) NOT NULL DEFAULT '0',
  `buytime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `giftflag` tinyint(4) NOT NULL DEFAULT '0',
  `otherplayerid` int(11) NOT NULL DEFAULT '-1',
  `count` int(11) NOT NULL DEFAULT '1',
  `otherplayername` varchar(255) DEFAULT NULL,
  `level` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `ibuy_playerid` (`playerid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;

-- Table "tbl_id" DDL

CREATE TABLE `tbl_id` (
  `usedid` int(11) NOT NULL DEFAULT '0',
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_imoneycard" DDL
CREATE TABLE `tbl_imoneycard` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `createaccountid` int(11) NOT NULL DEFAULT '-1',
  `createplayerid` int(11) NOT NULL DEFAULT '-1',
  `createtime` datetime DEFAULT NULL,
  `useaccountid` int(11) NOT NULL DEFAULT '-1',
  `useplayerid` int(11) NOT NULL DEFAULT '-1',
  `usetime` datetime DEFAULT NULL,
  `cardno` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `amount` int(11) NOT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `create_imoney_card` (`createaccountid`,`createplayerid`),
  KEY `use_imoney_card` (`useaccountid`,`useplayerid`),
  KEY `imoney_card_no` (`cardno`),
  KEY `imoney_card_status` (`status`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_leavemessage" DDL

CREATE TABLE `tbl_leavemessage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sourceid` int(11) NOT NULL DEFAULT '0',
  `sourcename` varchar(100) NOT NULL DEFAULT '',
  `title` varchar(255) NOT NULL DEFAULT '',
  `content` mediumtext NOT NULL,
  `ownerid` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_mail" DDL

CREATE TABLE `tbl_mail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sourceid` int(11) NOT NULL DEFAULT '0',
  `sourcename` varchar(255) NOT NULL DEFAULT '',
  `destid` int(11) NOT NULL DEFAULT '0',
  `destname` varchar(255) NOT NULL DEFAULT '',
  `title` varchar(255) NOT NULL DEFAULT '',
  `content` mediumtext,
  `attachment` blob,
  `price` int(11) NOT NULL DEFAULT '0',
  `posttime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `readed` tinyint(4) NOT NULL DEFAULT '0',
  `validtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`),
  KEY `index_mail_user` (`destid`),
  KEY `index_mail_posttime` (`posttime`),
  KEY `index_validtime` (`validtime`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;

-- Table "tbl_master" DDL

CREATE TABLE `tbl_master` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `masterid` int(11) NOT NULL DEFAULT '0',
  `mastername` varchar(255) NOT NULL DEFAULT '',
  `prenticeid` int(11) NOT NULL DEFAULT '0',
  `prenticename` varchar(255) NOT NULL DEFAULT '',
  `beginlevel` int(11) NOT NULL DEFAULT '0',
  `state` tinyint(4) NOT NULL DEFAULT '0',
  `intimacy` int(11) NOT NULL DEFAULT '0',
  `fame` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_mate" DDL

CREATE TABLE `tbl_mate` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `husbandid` int(11) NOT NULL DEFAULT '0',
  `husbandname` varchar(255) NOT NULL DEFAULT '',
  `wifeid` int(11) NOT NULL DEFAULT '0',
  `wifename` varchar(255) NOT NULL DEFAULT '',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_mercenary" DDL
CREATE TABLE `tbl_mercenary` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `masterid` int(11) NOT NULL DEFAULT '0',
  `accountid` int(11) NOT NULL DEFAULT '0',
  `buyplayerid` int(11) NOT NULL DEFAULT '0',
  `profession` tinyint(4) NOT NULL DEFAULT '0',
  `playername` varchar(255) NOT NULL DEFAULT '',
  `level` int(11) NOT NULL DEFAULT '0',
  `sex` tinyint(4) NOT NULL DEFAULT '0',
  `camp` tinyint(4) NOT NULL DEFAULT '0',
  `price` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `buytime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `leavetime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `usetime` int(11) NOT NULL DEFAULT '0',
  `battletime` int(11) NOT NULL DEFAULT '0',
  `face` int(11) NOT NULL DEFAULT '0',
  `viany` int(11) NOT NULL DEFAULT '0',
  `strength` int(11) NOT NULL DEFAULT '0',
  `agility` int(11) NOT NULL DEFAULT '0',
  `vitality` int(11) NOT NULL DEFAULT '0',
  `intelligence` int(11) NOT NULL DEFAULT '0',
  `abilities` blob,
  `usedequipments` blob,
  `state` tinyint(4) NOT NULL DEFAULT '0',
  `valid` tinyint(4) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_oem" DDL

CREATE TABLE `tbl_oem` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shopid` int(11) NOT NULL DEFAULT '0',
  `itemid` int(11) NOT NULL DEFAULT '0',
  `total` int(11) NOT NULL DEFAULT '0',
  `current` int(11) NOT NULL DEFAULT '0',
  `pay` int(11) NOT NULL DEFAULT '0',
  `workpoint` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `name` varchar(255) NOT NULL DEFAULT '',
  `type` int(11) NOT NULL DEFAULT '0',
  `areaid` int(11) NOT NULL DEFAULT '0',
  `state` int(11) NOT NULL DEFAULT '0',
  `quality` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_petmanager" DDL

CREATE TABLE `tbl_petmanager` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `petid` int(11) NOT NULL DEFAULT '0',
  `playerid` int(11) NOT NULL DEFAULT '0',
  `pet` blob,
  `stone` int(11) NOT NULL DEFAULT '0',
  `eattime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `information` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_question" DDL

CREATE TABLE `tbl_question` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `questionid` int(11) DEFAULT '0',
  `succeed` int(11) DEFAULT '0',
  `fail` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- Table "tbl_sequence" DDL
CREATE TABLE `tbl_sequence` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `usedid` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_sequence2" DDL
CREATE TABLE `tbl_sequence2` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `usedid` int(11) NOT NULL DEFAULT '0',
  `maxid` int(11) NOT NULL DEFAULT '0',
  `step` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_shop" DDL

CREATE TABLE `tbl_shop` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `money` int(11) NOT NULL DEFAULT '0',
  `playerid` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `items` blob NOT NULL,
  `areaid` int(11) NOT NULL DEFAULT '0',
  `gridsize` int(11) NOT NULL DEFAULT '0',
  `state` int(11) NOT NULL DEFAULT '0',
  `buyplayerid` int(11) NOT NULL DEFAULT '0',
  `price` int(11) NOT NULL DEFAULT '0',
  `selltime` datetime DEFAULT NULL,
  `leveluptime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_smsfee" DDL
CREATE TABLE `tbl_smsfee` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `charged` int(4) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `accountid` int(11) NOT NULL DEFAULT '0',
  `phone` varchar(20) NOT NULL DEFAULT '',
  `amount` int(11) NOT NULL DEFAULT '0',
  `consumecode` varchar(20) NOT NULL DEFAULT '',
  `smscode` varchar(20) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `smsfee_charged` (`charged`),
  KEY `smsfee_createtime` (`createtime`),
  KEY `smsfee_accountid` (`accountid`),
  KEY `smsfee_phone` (`phone`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- Table "tbl_task" DDL

CREATE TABLE `tbl_task` (
  `id` int(11) NOT NULL DEFAULT '0',
  `current` blob,
  `finished` blob,
  `savedata` blob,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;

-- Table "tbl_tong" DDL

CREATE TABLE `tbl_tong` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tongname` varchar(255) NOT NULL DEFAULT '',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `owner` int(11) NOT NULL DEFAULT '0',
  `slogan` mediumtext,
  `level` int(11) NOT NULL DEFAULT '0',
  `money` int(11) NOT NULL DEFAULT '0',
  `resource` int(11) NOT NULL DEFAULT '0',
  `health` int(11) NOT NULL DEFAULT '0',
  `lastrepairtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `memebercount` int(11) NOT NULL DEFAULT '0',
  `credit` int(11) NOT NULL DEFAULT '0',
  `toplisthot` int(11) NOT NULL DEFAULT '0',
  `toplistonline` int(11) NOT NULL DEFAULT '0',
  `leastcredit` int(11) NOT NULL DEFAULT '0',
  `valid` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `credit` (`credit`),
  KEY `index_tong_toplisthot` (`toplisthot`),
  KEY `index_tong_toplistonline` (`toplistonline`),
  KEY `index_tongname` (`tongname`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_tongisland" DDL

CREATE TABLE `tbl_tongisland` (
  `id` int(11) NOT NULL DEFAULT '0',
  `tongid` int(11) NOT NULL DEFAULT '0',
  `begintime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `endtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- Table "tbl_treasure" DDL

CREATE TABLE `tbl_treasure` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL DEFAULT '0',
  `x` int(11) NOT NULL DEFAULT '0',
  `y` int(11) NOT NULL DEFAULT '0',
  `mapid` int(11) NOT NULL DEFAULT '0',
  `itemgroupid` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `keyitemid` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`),
  KEY `playerid` (`playerid`),
  KEY `mapid` (`mapid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


-- Table "tbl_userdata" DDL

CREATE TABLE `tbl_userdata` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountid` int(11) NOT NULL DEFAULT '0',
  `playername` varchar(255) NOT NULL DEFAULT '',
  `level` int(11) NOT NULL DEFAULT '0',
  `mapid` int(11) NOT NULL DEFAULT '0',
  `x` int(11) NOT NULL DEFAULT '0',
  `y` int(11) NOT NULL DEFAULT '0',
  `sex` tinyint(4) NOT NULL DEFAULT '0',
  `exp` int(11) NOT NULL DEFAULT '0',
  `returntimes` int(11) NOT NULL DEFAULT '0',
  `data` blob,
  `moeny` int(11) NOT NULL DEFAULT '0',
  `taskdata` blob,
  `tongid` int(11) NOT NULL DEFAULT '-1',
  `tongname` varchar(255) DEFAULT NULL,
  `tongduty` int(11) DEFAULT '-1',
  `tongtitle` varchar(255) DEFAULT NULL,
  `houselevel` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `lastlogintime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `credit` int(11) NOT NULL DEFAULT '0',
  `face` int(11) NOT NULL DEFAULT '0',
  `strength` int(11) NOT NULL DEFAULT '0',
  `agility` int(11) NOT NULL DEFAULT '0',
  `vitality` int(11) NOT NULL DEFAULT '0',
  `intelligence` int(11) NOT NULL DEFAULT '0',
  `luck` int(11) NOT NULL DEFAULT '0',
  `hp` int(11) DEFAULT NULL,
  `mp` int(11) NOT NULL DEFAULT '0',
  `leavepoints` int(11) NOT NULL DEFAULT '0',
  `abilities` blob,
  `techskills` blob,
  `basicitems` blob,
  `pets` blob,
  `options` blob,
  `metaitems` blob,
  `equipments` blob,
  `usedequipments` blob,
  `usedpet` blob,
  `taskitems` blob,
  `recipes` blob,
  `chatoptions` blob,
  `gridsize` int(11) NOT NULL DEFAULT '0',
  `friends` blob,
  `abilitypoints` int(11) DEFAULT NULL,
  `point` int(11) DEFAULT NULL,
  `addedgridsize` int(11) NOT NULL DEFAULT '0',
  `petid` int(11) NOT NULL DEFAULT '-1',
  `petsize` int(11) NOT NULL DEFAULT '0',
  `abilitytimes` int(11) NOT NULL DEFAULT '1',
  `valid` tinyint(4) NOT NULL DEFAULT '1',
  `messagecount` int(11) NOT NULL DEFAULT '0',
  `lastmessagetime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `title` varchar(64) DEFAULT NULL,
  `modifynametimes` int(11) NOT NULL DEFAULT '0',
  `blacklist` blob,
  `bufs` blob,
  `jumpmapid` int(11) NOT NULL DEFAULT '0',
  `jumpx` int(11) NOT NULL DEFAULT '0',
  `jumpy` int(11) NOT NULL DEFAULT '0',
  `bathhousetime` datetime DEFAULT NULL,
  `questiontime` datetime DEFAULT NULL,
  `questionstate` int(11) NOT NULL DEFAULT '0',
  `lastkills` int(11) NOT NULL DEFAULT '0',
  `lastsneaks` int(11) NOT NULL DEFAULT '0',
  `kills` int(11) NOT NULL DEFAULT '0',
  `sneaks` int(11) NOT NULL DEFAULT '0',
  `vipbathhousetime` datetime DEFAULT NULL,
  `enemys` blob,
  `boxcount` int(11) NOT NULL DEFAULT '0',
  `contribution` int(11) NOT NULL DEFAULT '0',
  `consumepoint` int(11) NOT NULL DEFAULT '0',
  `islanditemtime` datetime DEFAULT NULL,
  `ibuylasttime` datetime DEFAULT NULL,
  `tongintime` datetime NOT NULL DEFAULT '1900-01-01 00:00:00',
  `arenav1id` int(11) NOT NULL DEFAULT '-1',
  `arenav2id` int(11) NOT NULL DEFAULT '-1',
  `arenav3id` int(11) NOT NULL DEFAULT '-1',
  `arenalevel` int(11) NOT NULL DEFAULT '0',
  `arenapoint` int(11) NOT NULL DEFAULT '0',
  `arenalevel2` int(11) NOT NULL DEFAULT '0',
  `arenalevel3` int(11) NOT NULL DEFAULT '0',
  `lastlogouttime` datetime DEFAULT NULL,
  `useskill` blob,
  `key9options` blob,
  `camp` tinyint(4) NOT NULL DEFAULT '0',
  `campwin` int(11) NOT NULL DEFAULT '0',
  `camplost` int(11) NOT NULL DEFAULT '0',
  `campcredit` int(11) NOT NULL DEFAULT '0',
  `endvotetime` datetime NOT NULL DEFAULT '2000-01-01 00:00:00',
  `roleface` blob,
  `prescription` blob,
  `playerpool` text NOT NULL,
  `otherpool` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `index_user_name` (`playername`),
  KEY `index_user_accountid` (`accountid`),
  KEY `credit` (`credit`),
  KEY `index_user_lastkills` (`lastkills`),
  KEY `index_user_lastsneaks` (`lastsneaks`),
  KEY `index_userdata_tongid` (`tongid`),
  KEY `index_userdata_arenalevel` (`arenalevel`),
  KEY `arenav1id_index` (`arenav1id`),
  KEY `arenav2id_index` (`arenav2id`),
  KEY `arenav3id_index` (`arenav3id`),
  KEY `index_userdata_camp` (`camp`),
  KEY `userdata_createtime_index` (`createtime`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;

-- Table "tbl_vote" DDL

CREATE TABLE `tbl_vote` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `votersid` int(11) NOT NULL DEFAULT '0',
  `playeridvoters` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `votepoint` int(11) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '0',
  `valid` tinyint(4) NOT NULL DEFAULT '1',
  `isimoneyitem` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `type_index` (`type`),
  KEY `votersid_index` (`votersid`),
  KEY `playeridvoters_index` (`playeridvoters`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_votecamp" DDL

CREATE TABLE `tbl_votecamp` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL DEFAULT '0',
  `playername` varchar(255) NOT NULL DEFAULT '',
  `camp` tinyint(4) NOT NULL DEFAULT '1',
  `credit` int(11) NOT NULL DEFAULT '0',
  `creditoffer` int(11) NOT NULL DEFAULT '0',
  `leve` int(11) NOT NULL DEFAULT '0',
  `moeny` int(11) NOT NULL DEFAULT '0',
  `fristtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `endtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `title` varchar(64) NOT NULL DEFAULT '',
  `ticket` int(11) NOT NULL DEFAULT '0',
  `itemcount` int(11) NOT NULL DEFAULT '0',
  `kingflag` tinyint(4) NOT NULL DEFAULT '0',
  `valid` tinyint(4) NOT NULL DEFAULT '1',
  `itemcounttotal` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `index_playerid` (`playerid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Table "tbl_votecontent" DDL

CREATE TABLE `tbl_votecontent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `votersid` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `type` int(11) NOT NULL DEFAULT '0',
  `valid` tinyint(4) NOT NULL DEFAULT '1',
  `content` text,
  PRIMARY KEY (`id`),
  KEY `group_index` (`votersid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO `tbl_admin` VALUES ('1', 'admin', 'jefflin', 'mute|kick|forbidaccount|add|delete|releaseaccount|reload|addip|shutdown|accountinfo|modifyaccount|move|forbid|modify');
INSERT INTO `tbl_admin` VALUES ('2', 'cwu', '.adm', '0x7fffffff');
INSERT INTO `tbl_admin` VALUES ('3', 'kj', 'kjgao1955', 'maxplayer|mute|kick|forbidaccount|add|delete|releaseaccount|reload|addip|shutdown|accountinfo|modifyaccount|move|forbid|modify');
INSERT INTO `tbl_admin` VALUES ('4', 'garyzhang', '69623721', 'mute|kick|forbidaccount|add|delete|releaseaccount|reload|addip|shutdown|accountinfo|modifyaccount|move|forbid|modify');
INSERT INTO `tbl_admin` VALUES ('5', 'lhu', 'snake', 'maxplayer|mute|kick|forbidaccount|add|delete|releaseaccount|reload|addip|shutdown|accountinfo|modifyaccount|move|forbid|modify');
INSERT INTO `tbl_admin` VALUES ('6', '001', 'fcg001', 'delete|modify|add|move|forbid|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|regulateExp|traceAdmin|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('7', '002', 'q1w2e3r4t5', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('8', '003', '134679heng', 'delete|modify|add|move|forbid|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|regulateExp|traceAdmin|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('9', '004', 'pip134679', 'modify|move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|regulateExp|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('10', '005', 'pipgonggao', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('11', '006', 'pjn426719', 'delete|modify|move|forbid|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|regulateExp|traceAdmin|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('12', '007', '007700', 'delete|modify|move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|regulateExp|traceAdmin|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('13', '008', '973391', 'delete|modify|move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|regulateExp|traceAdmin|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('14', '010', '870314', 'delete|modify|move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|regulateExp|traceAdmin|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('15', '009', '008026ls', 'delete|modify|move|forbid|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|regulateExp|traceAdmin|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('16', '011', '24647747', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('17', '012', 'zc8857asd', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('18', 'agent', 'agent345', 'mute|kick|forbidaccount|add|delete|releaseaccount|reload|addip|shutdown|accountinfo|modifyaccount|move|forbid|modify');
INSERT INTO `tbl_admin` VALUES ('19', '013', '611501', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('20', 'jlin', 'jlin', 'mute|kick|forbidaccount|add|delete|releaseaccount|reload|addip|shutdown|accountinfo|modifyaccount|move|forbid|modify');
INSERT INTO `tbl_admin` VALUES ('21', '014', 'lvfengye', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('22', '101', 'fengyits13', 'add|move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('23', '015', '250250', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('24', '016', 'dr10845788', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('25', '017', '517667', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('26', '018', '19890711', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('27', '019', '198885zwk', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('28', '020', '64411816', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('29', '021', '123123', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('30', '022', '23456', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('31', '023', '81920792', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('32', '024', '0577061w', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('33', '025', 'pip', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('34', '026', '097976', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('35', '027', '880824', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('36', '028', '794613', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('37', '029', 'tz', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('38', '030', 'zhelove', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('39', '103', 'lina1031', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('40', '104', 'nyning', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('41', '031', 'pip', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('42', '032', 'pip', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('43', '033', '880107', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('44', '034', '211314', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('45', '035', 'wangxugame44', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('47', '051', 'cjgr1014', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('48', '052', 'nan123', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('49', '053', '387800', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('50', '054', 'xy5632', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('51', '055', '555555', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('52', '056', '441527', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('54', '038', 'pip', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('55', '109', '850126', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('56', '037', '891110', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('57', '036', 'a21200', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('58', '057', 'pip', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('59', '058', 'pip', 'move|kick|mute|releaseaccount|accountinfo|show|who|chat|brocast|queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('88', '039', 'abc123', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('89', '040', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('60', '061', '61676438', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('61', '062', 'cyn5201314', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('62', '063', 'jP1116', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('63', '064', 'acheng', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('64', '065', '1989wu', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('65', '066', 'pip', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('66', '067', 'nokia6120c', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('67', '068', 'pip', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('68', '069', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('69', '070', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('70', '102', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('71', '105', '1', 'add:move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('72', '106', '795221', 'add:move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('73', '107', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('74', '108', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('75', '110', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('76', '111', 'mima123', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('77', '112', '901685', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('78', '113', 'xlw123', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('79', '114', 'pip12', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('80', '115', 'a21200', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('81', '116', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('82', '117', '001232', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('83', '118', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('84', '119', 'douqusiba', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('85', '120', '3325772', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('86', '059', 'pip', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('87', '060', 'pip', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('90', '041', 'pip', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:traceAdmin:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('91', '042', '711720', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:traceAdmin:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('92', '043', 'ylsds', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:traceAdmin:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('93', '044', '111qqq', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('94', '045', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('95', '046', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('96', '047', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('97', '048', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('98', '049', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');
INSERT INTO `tbl_admin` VALUES ('99', '050', '1', 'move:kick:mute:releaseaccount:accountinfo:show:who:chat:brocast:queryHelpByDate');

INSERT INTO tbl_id
   (`usedid`, `id`)
VALUES
   (10, 1);

INSERT INTO tbl_id
   (`usedid`, `id`)
VALUES
   (10, 2);

INSERT INTO tbl_id
   (`usedid`, `id`)
VALUES
   (3, 3);

