package com.pip.sanguo.data;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.Sheet;

public class WorldMapDataForExcel {
    /**世界地图ID*/
    private final static byte WORLD_ID     =        0;
    /**地图ID*/
    private final static byte MAP_ID       =        1;
    /**阵营ID*/
    private final static byte CAMP_ID      =        2;
    /**地图名称*/
    private final static byte MAP_NAME     =        3;
    /**阵营名称*/
    private final static byte CAMP_NAME    =        4;
    /**适合等级*/
    private final static byte LEVEL        =        5;
    /**地图坐标*/
    private final static byte XX           =        6;
    /**地图坐标*/
    private final static byte YY           =        7;
    /**通行方向UP*/
    private final static byte UP           =        8;
    /**通行方向DOWN*/
    private final static byte DOWN         =        9;
    /**通行方向LEFT*/
    private final static byte LEFT         =        10;
    /**通行方向RIGHT*/
    private final static byte RIGHT        =        11;
    /**共享地图标识*/
    private final static byte SHARE_ID     =        12;
    /**mapID键步长*/
    private final static byte MAPID_STEP   = 2;
    /**worldID键步长*/
    private final static byte WORLDID_STEP = 12;
    
    public static void save(String filePath){
        try{
            Workbook workbook = Workbook.getWorkbook(new File(filePath));
            System.out.println("导入工作簿");
            System.out.println("工作表数量：" + workbook.getNumberOfSheets());
            Sheet sheet = workbook.getSheet(0);
            System.out.println("工作表名：" + sheet.getName());
            int rows = sheet.getRows();
            int cols = sheet.getColumns();
            System.out.println("工作表行数：" + rows);
            System.out.println("工作表列数：" + cols);
            
            String[][] cells = new String[rows - 1][cols];
            for(int i=1; i<rows; i++){
                for(int j=0; j<cols; j++){
                    cells[i - 1][j] = sheet.getCell(j, i).getContents();
                    System.out.print((sheet.getCell(j, i)).getContents() + " ");
                }
                System.out.println();
            }
            System.out.println("导入工作表数据");
            
            Vector<String> keyMapID = new Vector<String>();
            Vector<String> keyMapID_curShare = new Vector<String>();
            for(int i=0; i<cells.length; i++){
                if(cells[i][SHARE_ID].equals("false")){
                    keyMapID.addElement(cells[i][MAP_ID]);
                    keyMapID.addElement(cells[i][WORLD_ID]);
                }
                else{
                    keyMapID_curShare.addElement(cells[i][MAP_ID]);
                    keyMapID_curShare.addElement(cells[i][WORLD_ID]);
                }
            }
            Vector<String> keyMapID_share = new Vector<String>();
            for(int i=0; i<keyMapID_curShare.size(); i+=MAPID_STEP){
                if(keyMapID_share.size()==0){
                    keyMapID_share.addElement(keyMapID_curShare.elementAt(i));
                    keyMapID_share.addElement(setShareID(keyMapID_curShare.elementAt(i + 1)));
                }
                else{
                    boolean _canAdd = true;
                    for(int j=0; j<keyMapID_share.size(); j+=MAPID_STEP){
                        if(keyMapID_curShare.elementAt(i).equals(keyMapID_share.elementAt(j))){
                            _canAdd = false;
                            break;
                        }
                    }
                    if(_canAdd){
                        keyMapID_share.addElement(keyMapID_curShare.elementAt(i));
                        keyMapID_share.addElement(setShareID(keyMapID_curShare.elementAt(i + 1)));
                    }
                }
            }
            System.out.println("需要共享数量(2倍)：" + keyMapID_share.size());
            for(int i=0; i<keyMapID_share.size(); i+=MAPID_STEP){
                keyMapID.addElement(keyMapID_share.elementAt(i));
                keyMapID.addElement(keyMapID_share.elementAt(i + 1));
            }
            System.out.println("mapID键数据量(2倍)：" + keyMapID.size());
            System.out.println("mapID键数据保存完毕");
            
            Vector<String> keyWorldID = new Vector<String>();
            for(int i=0; i<cells.length; i++){
                keyWorldID.addElement(cells[i][WORLD_ID]);
                keyWorldID.addElement(cells[i][MAP_ID]);
                keyWorldID.addElement(cells[i][CAMP_ID]);
                keyWorldID.addElement(cells[i][MAP_NAME]);
                keyWorldID.addElement(cells[i][CAMP_NAME]);
                keyWorldID.addElement(cells[i][LEVEL]);
                keyWorldID.addElement(cells[i][XX]);
                keyWorldID.addElement(cells[i][YY]);
                keyWorldID.addElement(cells[i][UP]);
                keyWorldID.addElement(cells[i][DOWN]);
                keyWorldID.addElement(cells[i][LEFT]);
                keyWorldID.addElement(cells[i][RIGHT]);
            }
            System.out.println("worldID键数据量(12倍)：" + keyWorldID.size());
            System.out.println("worldID键数据保存完毕");

            FileOutputStream fos = new FileOutputStream(new File(new File(filePath).getParent(), "worldMapInfo.data"));
            DataOutputStream dos = new DataOutputStream(fos);
            
            System.out.println("记录mapID键数据");
            dos.writeInt(keyMapID.size());
            dos.writeByte(MAPID_STEP);
            for(int i=0; i<keyMapID.size(); i+=MAPID_STEP){
                dos.writeShort(Short.parseShort(keyMapID.elementAt(i)));//mapID
                dos.writeUTF(keyMapID.elementAt(i + 1));//worldID
            }
            
            System.out.println("记录worldID键数据");
            dos.writeInt(keyWorldID.size());
            dos.writeByte(WORLDID_STEP);
            for(int i=0; i<keyWorldID.size(); i+=WORLDID_STEP){
                dos.writeUTF(keyWorldID.elementAt(i + WORLD_ID));
                dos.writeShort(Short.parseShort(keyWorldID.elementAt(i + MAP_ID)));
                dos.writeByte(Byte.parseByte(keyWorldID.elementAt(i + CAMP_ID)));
                dos.writeUTF(keyWorldID.elementAt(i + MAP_NAME));
                dos.writeUTF(keyWorldID.elementAt(i + CAMP_NAME));
                dos.writeUTF(keyWorldID.elementAt(i + LEVEL));
                dos.writeInt(Integer.parseInt(keyWorldID.elementAt(i + XX)));
                dos.writeInt(Integer.parseInt(keyWorldID.elementAt(i + YY)));
                dos.writeUTF(keyWorldID.elementAt(i + UP));
                dos.writeUTF(keyWorldID.elementAt(i + DOWN));
                dos.writeUTF(keyWorldID.elementAt(i + LEFT));
                dos.writeUTF(keyWorldID.elementAt(i + RIGHT));
            }
            
            dos.close();
            fos.close();
            workbook.close();
        }
        catch (BiffException e) {
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
//    /**将*,*,*格式的worldID转成short*/
//    private static Short setWorldID(String worldID){
//        String[] _sID = worldID.split(",");
//        return (short) (Short.parseShort(_sID[0]) << 11 | Short.parseShort(_sID[1]) << 4 | Short.parseShort(_sID[2]));
//    }
    /**将需要共享的worldID变成4,*,*的格式*/
    private static String setShareID(String worldID){
        String[] _curID = worldID.split(",");
        _curID[0] = "4";
        StringBuffer _sbID = new StringBuffer();
        for(int i=0; i<_curID.length; i++){
            _sbID.append(_curID[i] + ",");
        }
        _sbID.deleteCharAt(_sbID.length() - 1);
        System.out.println("转换前数据：" + worldID + "转换后数据：" + _sbID.toString());
        return _sbID.toString();
    }
}
