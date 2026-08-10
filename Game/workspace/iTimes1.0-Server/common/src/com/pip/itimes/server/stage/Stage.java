package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffery
 * @version 1.0
 */
public class Stage {

    private byte retCode;
    private short version;
    private short id;
    private String name;
    private byte musicId;
    private byte defaultMapId;
    private byte defaultX;
    private byte defaultY;
    private List inPkgFiles = new ArrayList();
    private List scenes = new ArrayList();
    private List monsters = new ArrayList();
    private classFile classFile;

    public Stage() {
    }


    public void setRetCode(byte retCode){
        this.retCode = retCode;
    }

    public byte getRetCode() {
        return retCode;
    }

    public void setVersion(short version){
        this.version = version;
    }

    public short getVersion() {
        return version;
    }

    public void setId(short id){
        this.id = id;
    }

    public short getId() {
        return id;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public void setMusicId(byte musicId){
        this.musicId = musicId;
    }

    public byte getMusicId() {
        return musicId;
    }

    public void setDefaultMapId(byte defaultMapId){
        this.defaultMapId = defaultMapId;
    }

    public byte getDefaultMapId() {
        return defaultMapId;
    }

    public void setDefaultX(byte defaultX){
        this.defaultX = defaultX;
    }

    public byte getDefaultX() {
        return defaultX;
    }

    public void setDefaultY(byte defaultY){
        this.defaultY = defaultY;
    }

    public byte getDefaultY() {
        return defaultY;
    }

    public void addInPkgFile(InPkgFile file){
        inPkgFiles.add(file);
    }

    public InPkgFile[] getInPkgFiles() {
        InPkgFile[] files = new InPkgFile[inPkgFiles.size()];
        inPkgFiles.toArray(files);
        return files;
    }

    public void addScene(Scene scene){
        scenes.add(scene);
    }

    public Scene[] getScenes() {
        Scene[] ret = new Scene[scenes.size()];
        scenes.toArray(ret);
        return ret;
    }

    public Scene getScene(int index){
        for(int i=0;i<scenes.size();i++){
            Scene scene = (Scene)scenes.get(i);
            if(scene.getId()==index)
                return scene;
        }
        return null;
    }

    public void setClassFile(classFile classFile){
        this.classFile = classFile;
    }

    public classFile getClassFile(){
        return classFile;
    }

    public void addMonster(Monster monster){
        monster.setStageId(id);
        monster.setIndex(monsters.size());
        monsters.add(monster);
    }

    public Monster[] getMonsters(){
        Monster[] ret = new Monster[monsters.size()];
        monsters.toArray(ret);
        return ret;
    }

    public Monster getMonster(int index){
        return (Monster)monsters.get(index);
    }

}
