package com.pip.itimes.server.stage;

import java.util.*;
import java.io.*;
import com.pip.gtl.etf.ETFFile;
import com.pip.gtl.gtvm.GTVMConstants;

/**
 * 用户任务存盘信息。
 */
public class TaskSaveDataBean {
    /** 任务ID(Short)到任务存盘信息(byte[])的映射。*/
    public HashMap taskDataMap = new HashMap();

    /**
     * 转换为一个大的byte数组以便保存到数据库中。格式为：任务ID(2) + 存盘信息。每个任务存盘
     * 信息的格式为：状态数(1) + 状态 + 线索数(1) + EIP(线索数 * 2字节)。
     */
    public byte[] getData() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        Iterator itor = taskDataMap.entrySet().iterator();

        while (itor.hasNext()) {
            Map.Entry entry = (Map.Entry)itor.next();
            try {
//                dout.writeShort(((Short)entry.getKey()).intValue());
                dout.write((byte[])entry.getValue());
            } catch (IOException ex) {
            }
        }
        return bout.toByteArray();
    }

    public byte[] getTaskSaveData(short taskId){
        return (byte[])taskDataMap.get(new Short(taskId));
    }

    /** 读入写好的任务存盘信息 */
    public void updateData(byte[] savedata) {
        int start = 0;
        if (savedata == null) {
            return;
        }
        while (start < savedata.length) {
            int s = start;
            short taskId = getShort(savedata[start],savedata[start+1]);
            start += 2;
            int statLen = getShort(savedata[start],savedata[start+1]);
            start += 2;
            start += statLen;
            start += 2;
            int stringLen = getShort(savedata[start],savedata[start+1]);
            start += 2;
            start += stringLen;
            int threadLen = savedata[start];
            start ++;
            start += (threadLen*2);
            byte[] taskdata = new byte[start-s];
            System.arraycopy(savedata,s,taskdata,0,taskdata.length);
            taskDataMap.put(new Short(taskId), taskdata);
        }
    }

    /** 添加一个任务的存盘信息 */
    public void addTaskSave(short taskId, byte[] taskdata) {
        taskDataMap.put(new Short(taskId), taskdata);
    }

    /** 删除一个任务的存盘信息 */
    public void removeTaskSave(short taskId) {
        taskDataMap.remove(new Short(taskId));
    }

    /** 查找一个任务存盘对应的任务文件版本号。*/
    public short getTaskVersion(short taskId) {
        byte[] taskSave = (byte[])taskDataMap.get(new Short(taskId));
        if (taskSave == null) {
            return -1;
        } else {
            return getShort(taskSave[0], taskSave[1]);
        }
    }

    /** 得到一个Short值 */
    public static short getShort(byte h, byte l) {
        return (short)(((h << 8) | (l & 0xFF)) & 0xFFFF);
    }

    /** 把某个任务的存盘文件升级到新版本，保留状态数据，所有的EIP都设置为0。*/
    public void upgrade(short taskId, ETFFile newFile) {
        byte[] oldData = (byte[])taskDataMap.get(new Short(taskId));
        if (oldData == null) {
            return;
        }
        int oldStatLen = oldData[2] & 0xFF;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeShort(newFile.fileVersion);
            dos.writeByte(newFile.stateMemSize);
            if (oldStatLen < newFile.stateMemSize) {
                dos.write(oldData, 3, oldStatLen);
                dos.write(new byte[newFile.stateMemSize - oldStatLen]);
            } else if (oldStatLen == newFile.stateMemSize) {
                dos.write(oldData, 3, oldStatLen);
            } else {
                dos.write(oldData, 3, newFile.stateMemSize);
            }
            dos.writeByte(newFile.threadAttr.length);
            dos.write(new byte[newFile.threadAttr.length * 2]);
            taskDataMap.put(new Short(taskId), bos.toByteArray());
        } catch (IOException e) {
        }
    }

    /** 确保某个任务的存盘文件是合法的。此方法检查状态数据的大小，EIP的数量和EIP的合法性。*/
    public void normalize(short taskId, ETFFile newFile) {
        byte[] oldData = (byte[])taskDataMap.get(new Short(taskId));
        if (oldData == null) {
            return;
        }
        int start = 0;
        start += 2;
        int oldStatLen = getShort(oldData[start],oldData[start+1]);
        start += 2;
        start += oldStatLen;
        int oldStringCount = getShort(oldData[start], oldData[start + 1]);
        start += 2;
        int oldStringLen = getShort(oldData[start],oldData[start+1]);
        start += 2;
        start += oldStringLen;
        int oldEipLen = oldData[start];
        int oldEipStart = start + 1;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            // 拷贝状态内存
            dos.writeShort(taskId);
            dos.writeShort(newFile.stateMemSize);
            if (oldStatLen < newFile.stateMemSize) {
                dos.write(oldData, 4, oldStatLen);
                dos.write(new byte[newFile.stateMemSize - oldStatLen]);
            } else if (oldStatLen == newFile.stateMemSize) {
                dos.write(oldData, 4, oldStatLen);
            } else {
                dos.write(oldData, 4, newFile.stateMemSize);
            }

            dos.writeShort(newFile.stateStrCount);
            if (oldStringCount == newFile.stateStrCount) {
                dos.writeShort(oldStringLen);
                dos.write(oldData, 8 + oldStatLen, oldStringLen);
            } else if (oldStringCount < newFile.stateStrCount) {
                ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                DataOutputStream dos2 = new DataOutputStream(bos2);
                for (int j = 0; j < newFile.stateStrCount - oldStringCount; j++) {
                    dos2.writeUTF("");
                }
                dos2.flush();
                dos.writeShort(oldStringLen + bos2.size());
                dos.write(oldData, 8 + oldStatLen, oldStringLen);
                dos.write(bos2.toByteArray());
            } else if (oldStringCount > newFile.stateStrCount) {
                ByteArrayInputStream bis = new ByteArrayInputStream(oldData, 8 + oldStatLen, oldStringLen);
                ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                DataInputStream dis = new DataInputStream(bis);
                DataOutputStream dos2 = new DataOutputStream(bos2);
                for (int j = 0; j < newFile.stateStrCount; j++) {
                    dos2.writeUTF(dis.readUTF());
                }
                dos2.flush();
                dos.writeShort(bos2.size());
                dos.write(bos2.toByteArray());
            }

            // 拷贝，检查EIP
            dos.writeByte(newFile.threadAttr.length);
            for (int i = 0; i < newFile.threadAttr.length; i++) {
                if (i >= oldEipLen) {
                    dos.writeShort((short)0);
                    continue;
                }
                byte[] codeAttr = (byte[])newFile.threadCodeAttr[i];
                byte[] code = (byte[])newFile.threadCode[i];
                short teip = getShort(oldData[oldEipStart + i * 2],
                                      oldData[oldEipStart + i * 2 + 1]);
                if (teip >= codeAttr.length) {
                    teip = (short)codeAttr.length;
                } else if (codeAttr[teip] != 0) {
                    teip = 0;
                } else if (teip != 0 && code[teip - 1] != GTVMConstants.PSE) {
                    // 如果保存位置在线索中间，则前面必须是一条PSE指令
                    teip = 0;
                }
                dos.writeShort(teip);
            }
            taskDataMap.put(new Short(taskId), bos.toByteArray());
        } catch (IOException e) {
        }
    }
}
