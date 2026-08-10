package com.pip.itimes.server.camp;

public class CampAuctionConfig{
    private int startWeek;
    private int startHour;
    private int startMinute;
    private int endWeek;
    private int endHour;
    private int endMinute;

    private String noticeMessage;
    private String adMessage;
    private String successMessage;
    private String failMessage;

    public int getStartWeek(){
        return startWeek;
    }

    public void setStartWeek(int startWeek){
        this.startWeek = startWeek;
    }

    public int getStartHour(){
        return startHour;
    }

    public void setStartHour(int startHour){
        this.startHour = startHour;
    }

    public int getStartMinute(){
        return startMinute;
    }

    public void setStartMinute(int startMinute){
        this.startMinute = startMinute;
    }

    public int getEndWeek(){
        return endWeek;
    }

    public void setEndWeek(int endWeek){
        this.endWeek = endWeek;
    }

    public int getEndHour(){
        return endHour;
    }

    public void setEndHour(int endHour){
        this.endHour = endHour;
    }

    public int getEndMinute(){
        return endMinute;
    }

    public void setEndMinute(int endMinute){
        this.endMinute = endMinute;
    }

    public String getNoticeMessage(){
        return noticeMessage;
    }

    public void setNoticeMessage(String noticeMessage){
        this.noticeMessage = noticeMessage;
    }

    public String getAdMessage(){
        return adMessage;
    }

    public void setAdMessage(String adMessage){
        this.adMessage = adMessage;
    }

    public String getSuccessMessage(){
        return successMessage;
    }

    public void setSuccessMessage(String successMessage){
        this.successMessage = successMessage;
    }

    public String getFailMessage(){
        return failMessage;
    }

    public void setFailMessage(String failMessage){
        this.failMessage = failMessage;
    }
}
