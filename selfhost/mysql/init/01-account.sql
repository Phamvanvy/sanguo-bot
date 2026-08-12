USE `account`;
-- MySQL dump 10.9
--
-- Host: localhost    Database: account
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
-- Table structure for table `tbl_account`
--

DROP TABLE IF EXISTS `tbl_account`;
CREATE TABLE `tbl_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `password` varchar(255) NOT NULL DEFAULT '',
  `guardpass` varchar(20) DEFAULT NULL,
  `abalance` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `gamecode` varchar(20) NOT NULL DEFAULT '',
  `lastlogintime` datetime DEFAULT NULL,
  `status` int(11) NOT NULL DEFAULT '0',
  `phone` varchar(20) DEFAULT NULL,
  `recommend` varchar(255) DEFAULT NULL,
  `comment` text,
  `serviceversion` varchar(200) NOT NULL DEFAULT '',
  `bbalance` int(11) NOT NULL DEFAULT '0',
  `lastpaytime` datetime DEFAULT NULL,
  `monthfee` int(11) NOT NULL DEFAULT '0',
  `lastmonthfee` int(11) NOT NULL DEFAULT '0',
  `modifypasswordtimes` int(11) NOT NULL DEFAULT '0',
  `model` varchar(200) DEFAULT NULL,
  `versionpatch` varchar(255) DEFAULT NULL,
  `cbalance` int(11) NOT NULL DEFAULT '0',
  `regtype` int(11) NOT NULL DEFAULT '0',
  `activephone` varchar(20) DEFAULT NULL,
  `regphone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `phone` (`phone`)
) ENGINE=MyISAM DEFAULT CHARSET=gbk DELAY_KEY_WRITE=1;

--
-- Table structure for table `tbl_accountcredit`
--

DROP TABLE IF EXISTS `tbl_accountcredit`;
CREATE TABLE `tbl_accountcredit` (
  `id` int(11) NOT NULL DEFAULT '0',
  `credit` int(11) NOT NULL DEFAULT '0',
  `logouttime` datetime DEFAULT NULL,
  `dayonline` int(11) NOT NULL DEFAULT '0',
  `daycredit` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=gbk DELAY_KEY_WRITE=1;

--
-- Table structure for table `tbl_fee`
--

DROP TABLE IF EXISTS `tbl_fee`;
CREATE TABLE `tbl_fee` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `charged` tinyint(4) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `finishtime` datetime DEFAULT NULL,
  `accountid` int(11) NOT NULL DEFAULT '0',
  `amount` int(11) DEFAULT NULL,
  `channel` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `channel` (`channel`),
  KEY `accountid` (`accountid`),
  KEY `finishtime` (`finishtime`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 DELAY_KEY_WRITE=1;

--
-- Table structure for table `tbl_gameaccount`
--

DROP TABLE IF EXISTS `tbl_gameaccount`;
CREATE TABLE `tbl_gameaccount` (
  `id` int(11) NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `issubscribe` tinyint(4) NOT NULL DEFAULT '0',
  `subscribetime` datetime DEFAULT NULL,
  `monthfee` int(11) NOT NULL DEFAULT '0',
  `lastfeetime` datetime DEFAULT NULL,
  `lastpaytime` datetime DEFAULT NULL,
  `monthpay` int(11) NOT NULL DEFAULT '0',
  `lastmonthpay` int(11) NOT NULL DEFAULT '0',
  `session` varchar(200) DEFAULT NULL,
  `serverid` varchar(200) DEFAULT NULL,
  `logintime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `accountid` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=gbk DELAY_KEY_WRITE=1;

--
-- Table structure for table `tbl_id`
--

DROP TABLE IF EXISTS `tbl_id`;
CREATE TABLE `tbl_id` (
  `usedid` int(11) NOT NULL DEFAULT '0',
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `tbl_imoneycard`
--

DROP TABLE IF EXISTS `tbl_imoneycard`;
CREATE TABLE `tbl_imoneycard` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cardno` varchar(20) NOT NULL,
  `password` varchar(20) NOT NULL,
  `gamecode` varchar(20) NOT NULL,
  `amount` int(11) NOT NULL,
  `createtime` datetime NOT NULL,
  `accountid` int(11) NOT NULL,
  `used` int(1) NOT NULL,
  `usetime` datetime DEFAULT NULL,
  `useaccount` int(11) NOT NULL DEFAULT '-1',
  `usegamecode` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `cardno` (`cardno`)
) ENGINE=MyISAM DEFAULT CHARSET=gbk DELAY_KEY_WRITE=1;

--
-- Table structure for table `tbl_logininfo`
--

DROP TABLE IF EXISTS `tbl_logininfo`;
CREATE TABLE `tbl_logininfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountid` int(11) NOT NULL DEFAULT '0',
  `logintime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `serviceid` varchar(200) NOT NULL DEFAULT '',
  `sessionid` varchar(200) NOT NULL DEFAULT '',
  `valid` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `accountid` (`accountid`)
) ENGINE=MyISAM DEFAULT CHARSET=gbk DELAY_KEY_WRITE=1;

--
-- Table structure for table `tbl_purchased`
--

DROP TABLE IF EXISTS `tbl_purchased`;
CREATE TABLE `tbl_purchased` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountid` int(11) NOT NULL DEFAULT '0',
  `code` int(11) NOT NULL DEFAULT '0',
  `status` int(11) NOT NULL DEFAULT '0',
  `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `feeid` int(11) NOT NULL DEFAULT '0',
  `phone` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `accountid` (`accountid`),
  KEY `phone` (`phone`)
) ENGINE=MyISAM DEFAULT CHARSET=gbk;

--
-- Table structure for table `tbl_recommendrequest`
--

DROP TABLE IF EXISTS `tbl_recommendrequest`;
CREATE TABLE `tbl_recommendrequest` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account` int(11) NOT NULL DEFAULT '0',
  `rectime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `gamecode` varchar(40) NOT NULL DEFAULT '',
  `targetphone` varchar(20) NOT NULL DEFAULT '',
  `validtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`),
  KEY `account` (`account`),
  KEY `targetphone` (`targetphone`)
) ENGINE=MyISAM DEFAULT CHARSET=gbk;

--
-- Table structure for table `tbl_recommendreward`
--

DROP TABLE IF EXISTS `tbl_recommendreward`;
CREATE TABLE `tbl_recommendreward` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rewardtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `guestid` int(11) NOT NULL DEFAULT '0',
  `guestphone` varchar(20) DEFAULT NULL,
  `guestgamecode` varchar(40) DEFAULT NULL,
  `roleid` int(11) NOT NULL DEFAULT '0',
  `guestlevel` int(11) NOT NULL DEFAULT '0',
  `guestrewardvalue` int(11) NOT NULL DEFAULT '0',
  `ownerid` int(11) NOT NULL DEFAULT '0',
  `ownerrewardvalue` int(11) NOT NULL DEFAULT '0',
  `rewardcode` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `guestid` (`guestid`),
  KEY `guestphone` (`guestphone`)
) ENGINE=MyISAM DEFAULT CHARSET=gbk;

--
-- Table structure for table `tbl_sequence`
--

DROP TABLE IF EXISTS `tbl_sequence`;
CREATE TABLE `tbl_sequence` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `usedid` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=gbk;

--
-- Table structure for table `tbl_sequence2`
--

DROP TABLE IF EXISTS `tbl_sequence2`;
CREATE TABLE `tbl_sequence2` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `usedid` int(11) NOT NULL DEFAULT '0',
  `maxid` int(11) NOT NULL DEFAULT '0',
  `step` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=gbk;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

LOCK TABLES `tbl_id` WRITE;
INSERT INTO `tbl_id` VALUES (1,1),(1,2),(1,3);
UNLOCK TABLES;
LOCK TABLES `tbl_sequence` WRITE;
INSERT INTO `tbl_sequence` VALUES (1,118139);
UNLOCK TABLES;
