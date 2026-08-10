package com.pip.itimes.server.world.camp;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CampJob{
    private int id;
    
    private int startWeek;
    private int startHour;
    private int startMinute;
    private int endWeek;
    private int endHour;
    private int endMinute;
    private int repeatType;
    public long nextProcessTime;
    public long endTime;
    
    //添加long时间
    private long timeStart;
    private long timeEnd;
    
    private int state;
    
    private JobProcessor processor;

    /**
     * STATE：等待开始
     */
    public static final int STATE_WAIT_START = 0;
    /**
     * STATE：已经开始
     */
    public static final int STATE_STARTED = 1;

    /**
     * 重复方式：不重复
     */
    public static final int REPEAT_TYPE_NONE = -1;
    /**
     * 重复方式：每小时
     */
    public static final int REPEAT_TYPE_HOUR = 1;
    
    /**
     * ID：选举宣传
     */
    public static final int ID_AUCTION_NOTICE = 1;
    /**
     * ID：竞拍广告
     */
    public static final int ID_AUCTION_AD = 2;
    /**
     * ID：竞拍
     */
    public static final int ID_AUCTION = 3;
    
    /**
     * ID：投票广告
     */
    public static final int ID_VOTE_AD = 11;
    
    /**
     * ID：投票
     */
    public static final int ID_VOTE = 12;

    public CampJob(int id, JobProcessor processor, int repeatType){
        this.id = id;
        this.processor = processor;
        this.repeatType = repeatType;
        state = STATE_WAIT_START;
    }
    
    private static SimpleDateFormat format = new SimpleDateFormat ("yyyy-MM-dd HH:mm");
    
    /**
     * 初始化时间 返回真是表示是下次的时间
     * @param startWeek
     * @param startHour
     * @param startMinute
     * @param endWeek
     * @param endHour
     * @param endMinute
     * @return
     */
    public boolean init(int startWeek, int startHour, int startMinute, int endWeek, int endHour, int endMinute){
        this.startWeek = startWeek;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endWeek = endWeek;
        this.endHour = endHour;
        this.endMinute = endMinute;
        
        nextProcessTime = initTime(this.startWeek, this.startHour, this.startMinute, 0);
        endTime = initTime(this.endWeek, this.endHour, this.endMinute, 0);
        
        if(endTime < nextProcessTime){
            nextProcessTime = initTime(this.startWeek, this.startHour, this.startMinute, -7);
        }
        
        long now = System.currentTimeMillis();
        //时间已经过去时 需要设置为下次的时间
        if(now > endTime){
        	nextProcessTime = initTime(this.startWeek, this.startHour, this.startMinute, 7);
            endTime = initTime(this.endWeek, this.endHour, this.endMinute, 7);
            if(nextProcessTime > endTime){
            	nextProcessTime = initTime(this.startWeek, this.startHour, this.startMinute, 0);
            }
            setNextHour();
//            Log.info("endTime:" + format.format(new Date(endTime)) + " nextProcessTime:" + format.format(new Date(nextProcessTime)));
            return true;
        }
        setNextHour();
//        Log.info("endTime:" + format.format(new Date(endTime)) + " nextProcessTime:" + format.format(new Date(nextProcessTime)));
        return false;
    }
    
    private void setNextHour(){
    	if(repeatType == REPEAT_TYPE_HOUR){
    		//每小时的轮放需要重置下时间
            long now = System.currentTimeMillis();
            if(now < endTime && now > nextProcessTime){
            	nextProcessTime = initNextHour();
            	state = STATE_WAIT_START;
            }
    	}
    }
    
    public boolean process(long time){
        boolean result = false;
        
        switch(state){
            case STATE_WAIT_START:
//            	Log.info("endTime:" + format.format(new Date(endTime)) + " nextProcessTime:" + format.format(new Date(nextProcessTime)));
                if(endTime > nextProcessTime && time >= endTime){
                    processor.processEnd(id, time);
                    result = true;
                }else if(time >= nextProcessTime){
                    processor.processStart(id, time);
                    
                    switch(repeatType){
                        case REPEAT_TYPE_NONE:
                            state = STATE_STARTED;
                            break;
                        case REPEAT_TYPE_HOUR:
                            nextProcessTime = initNextHour();
                            state = STATE_WAIT_START;
                            break;
                    }
                }
                break;
            case STATE_STARTED:
                if(time >= endTime){
                    processor.processEnd(id, time);
                    result = true;
                }
                
                break;
        }
        
        return result;
    }
    
    public int getId(){
        return id;
    }
    
    public int getState(){
        return state;
    }

    /**
     * 
     * @param week
     * @param hour
     * @param minute
     * @param day 0时为指定的时间 -7为获得上一星期的时间 +7为获得下一星期的时间
     * @return
     */
    private long initTime(int week, int hour, int minute, int day){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, week);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + day);
        return cal.getTimeInMillis();
    }
    
    /**
     * 初始化下一小时的时间
     * @return
     */
    private long initNextHour(){
    	Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
//    private long initTime(int week, int hour, int minute, boolean preWeek){
//    	Calendar cal = Calendar.getInstance();
//    	
//    	if(cal.get(Calendar.DAY_OF_WEEK) != week){
//    		if(preWeek){
//    			cal.set(Calendar.DAY_OF_MONTH, getPreWeekDay(cal, week));
//    		}else{
//    			cal.set(Calendar.DAY_OF_MONTH, getNextWeekDay(cal, week));
//    		}
//    	}
//    	
//    	cal.set(Calendar.HOUR_OF_DAY, hour);
//    	cal.set(Calendar.MINUTE, minute);
//    	cal.set(Calendar.SECOND, 0);
//    	cal.set(Calendar.MILLISECOND, 0);
//    	
//    	return cal.getTimeInMillis();
//    }
    
    private static int getNextWeekDay(Calendar cal, int dayOfWeek){
        int dw = cal.get(Calendar.DAY_OF_WEEK);
        int dm = cal.get(Calendar.DAY_OF_MONTH);
        
        int tmp = dayOfWeek - dw;
        
        if(tmp <= 0){
            tmp += 7;
        }
        
        return dm + tmp;
    }
    
    private static int getPreWeekDay(Calendar cal, int dayOfWeek){
        int dw = cal.get(Calendar.DAY_OF_WEEK);
        int dm = cal.get(Calendar.DAY_OF_MONTH);
        
        int tmp = dw - dayOfWeek;
        
//        if(tmp <=){
//            tmp -= 7;
//        }
        
        return dm + tmp;
    }
}
