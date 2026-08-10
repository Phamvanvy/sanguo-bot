package com.pip.itimes.server.camp;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class CampLoader{
    public CampLoader(File campFile) throws Exception{
        XMLConfiguration config = new XMLConfiguration(campFile);

        loadCamp(config);
        loadAuction(config);
        loadVote(config);
        loadSkills(config);
    }

    private void loadCamp(XMLConfiguration config){
        CampConfig.qualificationLevel = config.getInt("QualificationLevel");
        CampConfig.voteLevel = config.getInt("VoteLevel");
        CampConfig.amerceLimit = config.getInt("AmerceLimit");
        CampConfig.amercePlayerLimit = config.getInt("AmercePlayerLimit");

        SubnodeConfiguration taxConfig = config.configurationAt("Tax");
        CampConfig.taxDefault = taxConfig.getInt("Default");
        CampConfig.taxMin = taxConfig.getInt("Min");
        CampConfig.taxMax = taxConfig.getInt("Max");
        CampConfig.taxNoCamp = taxConfig.getInt("NoCamp");
    }

    private void loadAuction(XMLConfiguration config){
        SubnodeConfiguration auctionConfig = config.configurationAt("Auction");

        SubnodeConfiguration auctionStartConfig = auctionConfig.configurationAt("Start");
        CampConfig.campAuctionConfig.setStartWeek(auctionStartConfig.getInt("Week"));
        CampConfig.campAuctionConfig.setStartHour(auctionStartConfig.getInt("Hour"));
        CampConfig.campAuctionConfig.setStartMinute(auctionStartConfig.getInt("Minute"));

        SubnodeConfiguration auctionEndConfig = auctionConfig.configurationAt("End");
        CampConfig.campAuctionConfig.setEndWeek(auctionEndConfig.getInt("Week"));
        CampConfig.campAuctionConfig.setEndHour(auctionEndConfig.getInt("Hour"));
        CampConfig.campAuctionConfig.setEndMinute(auctionEndConfig.getInt("Minute"));

        CampConfig.campAuctionConfig.setNoticeMessage(auctionConfig.getString("NoticeMessage"));
        CampConfig.campAuctionConfig.setAdMessage(auctionConfig.getString("AdMessage"));
        CampConfig.campAuctionConfig.setFailMessage(auctionConfig.getString("FailMessage"));
        CampConfig.campAuctionConfig.setSuccessMessage(auctionConfig.getString("SuccessMessage"));
    }

    private void loadVote(XMLConfiguration config){
        SubnodeConfiguration voteConfig = config.configurationAt("Vote");

        SubnodeConfiguration voteStartConfig = voteConfig.configurationAt("Start");
        CampConfig.campVoteConfig.setStartWeek(voteStartConfig.getInt("Week"));
        CampConfig.campVoteConfig.setStartHour(voteStartConfig.getInt("Hour"));
        CampConfig.campVoteConfig.setStartMinute(voteStartConfig.getInt("Minute"));

        SubnodeConfiguration voteEndConfig = voteConfig.configurationAt("End");
        CampConfig.campVoteConfig.setEndWeek(voteEndConfig.getInt("Week"));
        CampConfig.campVoteConfig.setEndHour(voteEndConfig.getInt("Hour"));
        CampConfig.campVoteConfig.setEndMinute(voteEndConfig.getInt("Minute"));

        CampConfig.campVoteConfig.setAdMessage(voteConfig.getString("AdMessage"));
        CampConfig.campVoteConfig.setElectionMessage(voteConfig.getString("ElectionMessage"));
    }

    private void loadSkills(XMLConfiguration config){
        CampConfig.campSkills.clear();

        SubnodeConfiguration skillConfig = config.configurationAt("Skills");
        List<SubnodeConfiguration> skillList = skillConfig.configurationsAt("Skill");

        for(SubnodeConfiguration skillNode : skillList){
            CampSkill skill = new CampSkill();

            skill.setEffect(skillNode.getInt("Effect"));
            skill.setName(skillNode.getString("Name"));
            skill.setUpLimit(skillNode.getInt("UpLimit"));
            skill.setNoLevelDesc(skillNode.getString("NoLevelDesc"));
            skill.setLevelDesc(skillNode.getString("LevelDesc"));
            skill.initLevels();

            SubnodeConfiguration levelConfig = skillNode.configurationAt("Levels");
            List<SubnodeConfiguration> levelList = levelConfig.configurationsAt("Level");

            for(SubnodeConfiguration levelNode : levelList){
                CampSkillLevel level = new CampSkillLevel();

                level.setLevel(levelNode.getInt("Level"));
                level.setParm1(levelNode.getInt("Parm1"));
                level.setParm2(levelNode.getInt("Parm2"));
                level.setUpgrade(levelNode.getInt("Upgrade"));
                level.setMaint(levelNode.getInt("Maint"));
                level.setDestroy(levelNode.getInt("Destroy"));

                skill.addLevel(level);
            }
            
            CampConfig.campSkills.put(skill.getEffect(), skill);
        }
    }
}
