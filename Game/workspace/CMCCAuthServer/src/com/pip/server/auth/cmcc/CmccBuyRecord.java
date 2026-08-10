package com.pip.server.auth.cmcc;

import java.util.ArrayList;
import java.util.List;

/**
 * 卓望平台用户购买历史快速查找表。实际数据在tbl_fee表中有，这里的数据只是用于快速统计用户
 * 24小时内的购买金额，以限制每日购买量。
 */
public class CmccBuyRecord {
    private static class Record {
        public long time;
        public int amount;
    }
    
    /*
     * 用户ID
     */
    private String userId;
    /*
     * 购买记录
     */
    private List<Record> records;
    /*
     * 是否修改
     */
    private boolean modified;

    public CmccBuyRecord(String userId, String data) {
        this.userId = userId;
        records = new ArrayList<Record>();
        if (data != null) {
            String[] lines = data.split("\n");
            for (String line : lines) {
                String[] sec = line.split(",");
                if (sec.length == 2) {
                    Record r = new Record();
                    r.time = Long.parseLong(sec[0]);
                    r.amount = Integer.parseInt(sec[1]);
                    records.add(r);
                }
            }
        }
    }

    public String getUserId() {
        return userId;
    }
    
    /**
     * 添加购买记录。
     */
    public void addRecord(int amount) {
        Record r = new Record();
        r.time = System.currentTimeMillis();
        r.amount = amount;
        records.add(r);
    }
    
    /**
     * 计算在指定时间内的购买金额。
     * @param period 毫秒
     * @return
     */
    public int getAmount(long period) {
        long valve = System.currentTimeMillis() - period;
        int total = 0;
        for (int i = records.size() - 1; i >= 0; i--) {
            if (records.get(i).time < valve) {
                break;
            } else {
                total += records.get(i).amount;
            }
        }
        return total;
    }
    
    /**
     * 清除指定时间之前的购买金额。
     * @param period 毫秒
     */
    public void clearOldData(long period) {
        long valve = System.currentTimeMillis() - period;
        for (int i = records.size() - 1; i >= 0; i--) {
            if (records.get(i).time < valve) {
                records.remove(i);
                modified = true;
            }
        }
    }
    
    /**
     * 在清除过程中是否修改过。
     * @return
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * 取得保存到bdb里的数据。
     * @return
     */
    public String getSaveData() {
        StringBuilder sb = new StringBuilder();
        for (Record r : records) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(r.time);
            sb.append(",");
            sb.append(r.amount);
        }
        return sb.toString();
    }    
}
