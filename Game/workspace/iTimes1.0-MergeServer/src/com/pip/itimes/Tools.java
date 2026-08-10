package com.pip.itimes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.pip.itimes.bean.Tbl_House;
import com.pip.itimes.server.stage.EquipmentHelper;
import com.pip.itimes.server.stage.IEquipment;
//import com.pip.itimes.server.stage.Technology;

public class Tools{
	private static final Logger log = Logger.getLogger(Tbl_House.class);
	
    private static final char[] HEX_CHAR = {
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static final SimpleDateFormat DATE_FORMATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String toSqlString(byte[] data){
        if(data == null){
            return "null";
        }

        if(data.length == 0){
            return "\'\'";
        }

        StringBuffer sb = new StringBuffer();

        sb.append("0x");

        for(int i = 0; i < data.length; i++){
            int c1 = data[i] >> 4 & 0xF;
            int c2 = data[i] & 0xF;

            sb.append(HEX_CHAR[c1]);
            sb.append(HEX_CHAR[c2]);
        }

        return sb.toString();
    }

    public static String toSqlString(Date date){
        if(date == null){
            return "null";
        }

        StringBuffer sb = new StringBuffer();

        sb.append('\'');
        sb.append(DATE_FORMATE.format(date));
        sb.append('\'');

        return sb.toString();
    }

    public static String toSqlString(boolean data){
        StringBuffer sb = new StringBuffer();

        sb.append('\'');
        sb.append(data? 1: 0);
        sb.append('\'');

        return sb.toString();
    }

    public static String toSqlString(int data){
        StringBuffer sb = new StringBuffer();

        sb.append('\'');
        sb.append(data);
        sb.append('\'');

        return sb.toString();
    }

    public static String toSqlString(long data){
        StringBuffer sb = new StringBuffer();

        sb.append('\'');
        sb.append(data);
        sb.append('\'');

        return sb.toString();
    }

    public static String toSqlString(float data){
        StringBuffer sb = new StringBuffer();

        sb.append('\'');
        sb.append(data);
        sb.append('\'');

        return sb.toString();
    }

    public static String toSqlString(String data){
        if(data == null){
            return null;
        }

        StringBuffer sb = new StringBuffer();

        sb.append('\'');
        sb.append(reverseConv(data));
        sb.append('\'');

        return sb.toString();
    }

    /**
     * 把Java字符串转换为表达式中的格式。
     */
    private static String reverseConv(String msg){
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < msg.length(); i++){
            switch(msg.charAt(i)){
                case '\n':
                    buf.append("\\n");
                    break;
                case '\r':
                    buf.append("\\r");
                    break;
                case '\t':
                    buf.append("\\t");
                    break;
                case '"':
                    buf.append("\\\"");
                    break;
                case '\'':
                    buf.append("\\\'");
                    break;
                case '\\':
                    buf.append("\\\\");
                    break;
                default:
                    buf.append(msg.charAt(i));
                    break;
            }
        }
        return buf.toString();
    }
    
    
    public static byte[] procItems(byte[] itemData, MergeData mergeData){
    	try{
	        if (itemData != null && itemData.length > 0) {
	            ByteArrayInputStream bis = new ByteArrayInputStream(itemData);
	            DataInputStream dis = new DataInputStream(bis);
	            ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            DataOutputStream dos = new DataOutputStream(bos);
	            
	            byte version = dis.readByte();
	            dos.writeByte((byte)7);
	            
	            short size = dis.readShort();
	            dos.writeShort(size);
	            
	            for (int i = 0; i < size; i++) {
	                int id = dis.readInt();
	                dos.writeInt(id);
	                
	                byte count = dis.readByte();
	                dos.writeByte(count);
	            }
	            
	            size = dis.readShort();
	            dos.writeShort(size);
	            
	            for (int i = 0; i < size; i++) {
	                int id = dis.readInt();
	                dos.writeInt(id);
	                
	                byte count = dis.readByte();
	                dos.writeByte(count);
	            }
	            
	            size = dis.readShort();
	            dos.writeShort(size);
	            
	            for (int i = 0; i < size; i++) {
	                IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
	                int instanceId = equ.getId();
	                instanceId = mergeData.procEquipmentId(instanceId);
	                equ.setId(instanceId);
	                dos.write(equ.toDbBytes());
	            }
	            
	            itemData = bos.toByteArray();
	        }
    	}catch(Exception e){
    		log.error(e, e);
    	}
    	
    	return itemData;
    }
    
    public static byte[] procEquipments(byte[] equData, MergeData mergeData){
    	try{
            ByteArrayInputStream bis = new ByteArrayInputStream(equData);
            DataInputStream dis = new DataInputStream(bis);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            
            byte version = dis.readByte();
            dos.write((byte)7);
            
            short size = dis.readShort();
            dos.writeShort(size);
            
            for (int i = 0; i < size; i++) {
                IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
                int instanceId = equ.getId();
                instanceId = mergeData.procEquipmentId(instanceId);
                equ.setId(instanceId);
                dos.write(equ.toDbBytes());
            }
            
            equData = bos.toByteArray();
		}catch(Exception e){
			log.error(e, e);
		}
		
		return equData;
    }
    
//	public static Technology[] getTechnologys(byte[] techData) throws Exception{
//		ByteArrayInputStream bis = new ByteArrayInputStream(techData);
//		DataInputStream dis = new DataInputStream(bis);
//		byte version = dis.readByte(); // 读出版本号
//		
//		if (version == 1) {
//			short size = dis.readShort();
//			Technology[] result = new Technology[size];
//			
//			for (int i = 0; i < size; i++) {
//				result[i] = Technology.fromDbBytes(dis);
//			}
//			
//			return result;
//		}
//		
//		return null;
//	}
//	
//	public static byte[] saveTechnologys(Technology[] techs) throws Exception {
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream(bos);
//		try {
//			dos.write((byte) 1); // 版本号
//			dos.writeShort(techs.length);
//			
//			if (techs.length > 1) {
//				for (int i = 0; i < techs.length; i++) {
//					dos.write(techs[i].toDbBytes());
//				}
//			}
//			
//			return bos.toByteArray();
//		} catch (Exception e) {
//			log.error(e, e);
//		}
//		
//		return null;
//	}
}
