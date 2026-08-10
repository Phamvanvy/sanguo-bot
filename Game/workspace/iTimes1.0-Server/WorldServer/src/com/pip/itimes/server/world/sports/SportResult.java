package com.pip.itimes.server.world.sports;

public class SportResult {
    public int id;  //如果是个人为个人id，如果是公会为公会id，如果是爬山为个人id
    public String name; //如果是个人为个人名字，如果为公会为公会名字，如果是爬山为个人名字
    public SportRecord[] records; //如果是个人那么是个人的比赛成绩，如果是公会那么就是前三名的比赛成绩，如果是爬山那么就是个人比赛成绩
}
