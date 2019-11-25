package com.example.roylurui.memo;

import org.litepal.crud.DataSupport;

/**
 * Created by sf roylurui on 2019/11/20.
 */
//存储的备忘信息JavaBean
public class Memo extends DataSupport {

    private int num;
    //标签颜色序号
    private int tag;

    private String textDate;
    private String textTime;
    //提醒时间（事件提醒的时间）
    private String alarm;
    private String mainText;
    private int id;

    //getter
    public int getNum(){
        return num;
    }
    public int getTag(){
        return tag;
    }
    public String getTextDate(){
        return textDate;
    }
    public String getTextTime(){
        return textTime;
    }
    public String getAlarm(){
        return alarm;
    }
    public String getMainText(){
        return mainText;
    }
    public int getId() { return id; }

    //setter
    public void setNum(int num) {
        this.num=num;
    }
    public void setTag(int tag){
        this.tag=tag;
    }
    public void setTextDate(String textDate){
        this.textDate=textDate;
    }
    public void setTextTime(String textTime){
        this.textTime=textTime;
    }
    public void setAlarm(String alarm){
        this.alarm=alarm;
    }
    public void setMainText(String mainText){
        this.mainText=mainText;
    }
    public void setId(int id){ this.id=id; }
}
