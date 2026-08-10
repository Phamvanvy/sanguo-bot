package com.pip.itimes.utils;

import java.io.*;

import org.apache.commons.configuration.*;
import org.apache.commons.io.*;
import com.pip.itimes.server.stage.*;
import com.pip.itimes.utils.decoder.*;
import com.pip.itimes.utils.visitor.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Main {


    public static void main(String[] args) throws Exception{
        //读取配置文件
        Configuration configuration = new PropertiesConfiguration(
                "config.properties");
        String stageDirName = configuration.getString("datadir");
        String equDirName = FilenameUtils.concat(stageDirName,
                                                 "Items/equ.xml");
        String itemDirName = FilenameUtils.concat(stageDirName,
                                                  "Items/item.xml");
        String abilityDirName = FilenameUtils.concat(stageDirName,
                                                  "Skill/index.xml");
        //载入物品信息
        ItemLoader loader = new ItemLoader(new File(equDirName),new File(itemDirName));
        //载入技能信息
        AbilitiesLoader aLoader = new AbilitiesLoader(new File(abilityDirName));
        String in = System.getProperty("user.dir")+"\\pet.log";
        PetFileOutputVisitor visitor = new PetFileOutputVisitor(new File(in+".a"));
        LogParser parser = new LogParser(new FileReader(in),21);
        ILineDecoder[] decoders = new ILineDecoder[]{new BattleResultDecoder()};
        parser.setILineDecoders(decoders);
        parser.setVisitor(visitor);
        parser.parse();
        visitor.close();
    }
}
