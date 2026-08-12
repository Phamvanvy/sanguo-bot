USE `SanGo`;
-- MySQL dump 10.9
--
-- Host: localhost    Database: sanguo_01
-- ------------------------------------------------------
-- Server version	5.1.32-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `schedule` blob NOT NULL,
  `implclass` varchar(255) NOT NULL,
  `configdata` varchar(255) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  `visible` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `alphagift`
--

DROP TABLE IF EXISTS `alphagift`;
CREATE TABLE `alphagift` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `exchanged` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `password` (`password`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `association`
--

DROP TABLE IF EXISTS `association`;
CREATE TABLE `association` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '""',
  `createplayerid` int(11) NOT NULL DEFAULT '0',
  `members` blob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `auction`
--

DROP TABLE IF EXISTS `auction`;
CREATE TABLE `auction` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `startprice` int(11) NOT NULL DEFAULT '0',
  `currentprice` int(11) NOT NULL DEFAULT '0',
  `endprice` int(11) NOT NULL DEFAULT '0',
  `item` blob NOT NULL,
  `count` int(11) NOT NULL DEFAULT '0',
  `name` varchar(255) NOT NULL DEFAULT '',
  `type` int(11) NOT NULL DEFAULT '0',
  `lastplayerid` int(11) NOT NULL DEFAULT '0',
  `playername` varchar(20) NOT NULL DEFAULT '',
  `quality` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `validtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `faction` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


--
-- Table structure for table `bbs`
--

DROP TABLE IF EXISTS `bbs`;
CREATE TABLE `bbs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `priv` int(11) NOT NULL,
  `message` text,
  `createtime` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `beauty`
--

DROP TABLE IF EXISTS `beauty`;
CREATE TABLE `beauty` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL DEFAULT '0',
  `name` varchar(20) NOT NULL,
  `sex` int(11) NOT NULL DEFAULT '0',
  `votes` int(11) NOT NULL DEFAULT '0',
  `signupdate` datetime NOT NULL,
  `slogan` varchar(100) NOT NULL,
  `faction` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `bossscore`
--

DROP TABLE IF EXISTS `bossscore`;
CREATE TABLE `bossscore` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bossid` int(11) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  `members` blob,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `bosstimescore`
--

DROP TABLE IF EXISTS `bosstimescore`;
CREATE TABLE `bosstimescore` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bossid` int(11) DEFAULT NULL,
  `members` blob,
  `time` int(11) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `candidate`
--

DROP TABLE IF EXISTS `candidate`;
CREATE TABLE `candidate` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL,
  `faction` int(11) NOT NULL,
  `funds` int(11) NOT NULL,
  `credit` int(11) NOT NULL,
  `createtime` datetime NOT NULL,
  `votes` int(11) NOT NULL,
  `previousking` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


--
-- Table structure for table `charge`
--

DROP TABLE IF EXISTS `charge`;
CREATE TABLE `charge` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountid` int(11) DEFAULT NULL,
  `money` int(11) DEFAULT NULL,
  `chargetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `clientbbs`
--

DROP TABLE IF EXISTS `clientbbs`;
CREATE TABLE `clientbbs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `minlevel` int(11) NOT NULL,
  `maxlevel` int(11) NOT NULL,
  `explaination` varchar(255) DEFAULT NULL,
  `active` text,
  `publishtime` datetime NOT NULL,
  `obsoletetime` datetime DEFAULT NULL,
  `enable` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `fame`
--

DROP TABLE IF EXISTS `fame`;
CREATE TABLE `fame` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `hp` int(11) NOT NULL DEFAULT '0',
  `faction` int(11) NOT NULL,
  `sex` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `clazz` int(11) NOT NULL,
  `x` int(11) DEFAULT NULL,
  `y` int(11) DEFAULT NULL,
  `guildname` varchar(255) NOT NULL,
  `matename` varchar(255) DEFAULT NULL,
  `rank` int(11) DEFAULT NULL,
  `credit` int(11) DEFAULT NULL,
  `weekcredit` int(11) DEFAULT NULL,
  `activepower` int(11) DEFAULT NULL,
  `equipments` blob,
  `chatoptions` blob,
  `titles` blob,
  `horseinstanceid` int(11) NOT NULL DEFAULT '0',
  `horsebag` blob,
  `bag` blob,
  `pool` text,
  `mp` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `forbid`
--

DROP TABLE IF EXISTS `forbid`;
CREATE TABLE `forbid` (
  `id` int(11) NOT NULL,
  `targetid` int(11) NOT NULL,
  `endtime` datetime NOT NULL,
  `createtime` datetime NOT NULL,
  KEY `id` (`id`),
  KEY `targetid` (`targetid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `friend`
--

DROP TABLE IF EXISTS `friend`;
CREATE TABLE `friend` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `PlayerID` int(11) NOT NULL,
  `PlayerName` varchar(20) NOT NULL DEFAULT '',
  `FriendID` int(11) NOT NULL,
  `FriendName` varchar(20) NOT NULL DEFAULT '',
  `FriendLevel` int(11) NOT NULL DEFAULT '0',
  `IsBlack` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `ID` (`ID`),
  KEY `PlayerID` (`PlayerID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `gift`
--

DROP TABLE IF EXISTS `gift`;
CREATE TABLE `gift` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL,
  `groupid` int(11) NOT NULL,
  `lasttime` datetime NOT NULL,
  `alltimes` int(11) NOT NULL,
  `repeattimes` int(11) NOT NULL,
  `endtime` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `playerid` (`playerid`),
  KEY `groupid` (`groupid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `gmrequest`
--

DROP TABLE IF EXISTS `gmrequest`;
CREATE TABLE `gmrequest` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL,
  `playername` varchar(255) DEFAULT NULL,
  `cause` text NOT NULL,
  `state` int(11) NOT NULL,
  `solvent` text NOT NULL,
  `createtime` datetime NOT NULL,
  `type` int(11) NOT NULL,
  `mapid` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `ibuy`
--

DROP TABLE IF EXISTS `ibuy`;
CREATE TABLE `ibuy` (
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
  `level` int(11) DEFAULT NULL,
  `faction` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `ibuy_playerid` (`playerid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;

--
-- Table structure for table `mail`
--

DROP TABLE IF EXISTS `mail`;
CREATE TABLE `mail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sourceid` int(11) NOT NULL,
  `sourcename` varchar(255) NOT NULL,
  `destid` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `attachment` blob,
  `price` int(11) NOT NULL,
  `posttime` datetime NOT NULL,
  `status` int(11) NOT NULL,
  `validtime` datetime NOT NULL,
  `expirationtime` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `destid` (`destid`),
  KEY `posttime` (`posttime`),
  KEY `expirationtime` (`expirationtime`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;

--
-- Table structure for table `nation`
--

DROP TABLE IF EXISTS `nation`;
CREATE TABLE `nation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `faction` int(11) NOT NULL,
  `money` bigint(20) NOT NULL,
  `power` int(11) NOT NULL,
  `slogan` text,
  `pool` text,
  `guardtime` datetime DEFAULT NULL,
  `skills` blob,
  `tax` decimal(4,2) NOT NULL DEFAULT '0.05',
  PRIMARY KEY (`id`),
  UNIQUE KEY `faction` (`faction`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `nationrel`
--

DROP TABLE IF EXISTS `nationrel`;
CREATE TABLE `nationrel` (
  `id` int(11) NOT NULL,
  `source` int(11) NOT NULL,
  `dest` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `createtime` datetime DEFAULT NULL,
  `endtime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `officer`
--

DROP TABLE IF EXISTS `officer`;
CREATE TABLE `officer` (
  `id` int(11) DEFAULT NULL,
  `level` int(11) NOT NULL,
  `faction` int(11) NOT NULL,
  `pool` text,
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
CREATE TABLE `player` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `maxhp` int(11) NOT NULL,
  `maxmp` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `agility` int(11) NOT NULL,
  `strength` int(11) NOT NULL,
  `intellect` int(11) NOT NULL,
  `stamina` int(11) NOT NULL,
  `sex` int(11) NOT NULL,
  `money` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `mapid` int(11) NOT NULL,
  `faction` int(11) NOT NULL,
  `hp` int(11) NOT NULL,
  `mp` int(11) NOT NULL,
  `bag` blob,
  `vm` blob,
  `skills` blob,
  `skillpoint` int(11) NOT NULL,
  `accountid` int(11) NOT NULL,
  `clazz` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `propertypoint` int(11) NOT NULL,
  `equipments` blob,
  `chatoptions` blob,
  `actionbar` blob,
  `credit` int(11) NOT NULL,
  `exist` smallint(6) NOT NULL,
  `buffs` blob,
  `createtime` datetime NOT NULL,
  `honor` int(11) NOT NULL,
  `config` blob,
  `pool` text,
  `lastlogin` datetime DEFAULT NULL,
  `horses` blob,
  `weekcredit` int(11) NOT NULL,
  `rank` int(11) NOT NULL,
  `titles` blob,
  `formulalist` blob,
  `activepower` int(11) NOT NULL,
  `depot` blob,
  `lastlogout` datetime DEFAULT NULL,
  `cooldowns` blob,
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `accountid` (`accountid`),
  KEY `weekcredit` (`weekcredit`),
  KEY `player_createtime_index` (`createtime`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;

--
-- Table structure for table `property`
--

DROP TABLE IF EXISTS `property`;
CREATE TABLE `property` (
  `id` int(11) NOT NULL,
  `pool` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `punish`
--

DROP TABLE IF EXISTS `punish`;
CREATE TABLE `punish` (
  `id` int(11) NOT NULL,
  `targetid` int(11) NOT NULL,
  `money` int(11) NOT NULL,
  `createtime` datetime NOT NULL,
  KEY `id` (`id`),
  KEY `targetid` (`targetid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `pvpinfo`
--

DROP TABLE IF EXISTS `pvpinfo`;
CREATE TABLE `pvpinfo` (
  `id` int(11) NOT NULL,
  `totaldiecount` int(11) NOT NULL,
  `totalkillcount` int(11) NOT NULL,
  `yesterdaydiecount` int(11) NOT NULL,
  `yesterkillcount` int(11) NOT NULL,
  `todaydiecount` int(11) NOT NULL,
  `todaykillcount` int(11) NOT NULL,
  `faction` int(11) NOT NULL,
  `pool` text,
  UNIQUE KEY `id` (`id`),
  KEY `yesterkillcount` (`yesterkillcount`),
  KEY `faction` (`faction`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `relation`
--

DROP TABLE IF EXISTS `relation`;
CREATE TABLE `relation` (
  `id` int(11) NOT NULL,
  `friends` text,
  `blacklist` text,
  `enemies` text,
  `templist` text,
  `mateid` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
CREATE TABLE `sequence` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  `usedid` int(11) NOT NULL,
  `maxid` int(11) NOT NULL,
  `step` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `tong`
--

DROP TABLE IF EXISTS `tong`;
CREATE TABLE `tong` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `createtime` datetime NOT NULL,
  `owner` int(11) NOT NULL,
  `slogan` varchar(255) NOT NULL,
  `level` int(11) NOT NULL,
  `honor` int(11) NOT NULL,
  `money` int(11) NOT NULL,
  `imoney` int(11) NOT NULL,
  `pool` text,
  `skills` blob,
  `tax` decimal(4,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `tongbattleapply`
--

DROP TABLE IF EXISTS `tongbattleapply`;
CREATE TABLE `tongbattleapply` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mapid` int(11) DEFAULT NULL,
  `tongid` int(11) DEFAULT NULL,
  `money` int(11) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `tongname` varchar(200) DEFAULT NULL,
  `faction` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `tongmember`
--

DROP TABLE IF EXISTS `tongmember`;
CREATE TABLE `tongmember` (
  `id` int(11) NOT NULL,
  `tongid` int(11) NOT NULL,
  `duty` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `honor` int(11) NOT NULL,
  `money` int(11) NOT NULL,
  `forbid` tinyint(4) NOT NULL,
  `battletag` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `tongid` (`tongid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `voteplayer`
--

DROP TABLE IF EXISTS `voteplayer`;
CREATE TABLE `voteplayer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerid` int(11) NOT NULL,
  `name` varchar(20) NOT NULL,
  `votes` int(11) NOT NULL,
  `faction` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


--
-- Table structure for table `accountdepot`
--

CREATE TABLE `accountdepot` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountid` int(11) NOT NULL,
  `depot` blob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

