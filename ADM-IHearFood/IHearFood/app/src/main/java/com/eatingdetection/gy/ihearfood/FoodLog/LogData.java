package com.eatingdetection.gy.ihearfood.FoodLog;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kinse on 3/18/2016.
 */
public class LogData {
    public String startTime = null;
    public String endTime = null;
    public String foodInfo = null;
    public String isConfirmed = null;

    public LogData(){}
    public LogData(String start){
        startTime = start;
    }

    public LogData(String start, String end, String food, String confirm){
        startTime = start;
        endTime = end;
        foodInfo = food;
        isConfirmed = confirm;
    }

    public LogData(String[] splitResult){
        startTime = splitResult[0];
        endTime = splitResult[1];
        foodInfo = splitResult[2];
        isConfirmed = splitResult[3];
    }
    public void setStartTime(String start){
        startTime = start;
    }

    public void setEndTime(String end){
        endTime = end;
    }

    public void setFoodInfo(String food){
        foodInfo = food;
    }

    public void setIsConfirmed(String confirm){
        isConfirmed = confirm;
    }

    public String getStartTime(){ return startTime;    }

    public String getEndTime(){ return endTime; }

    public String getFoodInfo(){ return foodInfo; }

    public String getIsConfirmed(){ return isConfirmed;}



    public Boolean iscompleted(){
        if(startTime == null)
            return false;
        else if(endTime == null)
            return false;
        else if(foodInfo == null)
            return false;
        else if(isConfirmed == null)
            return false;
        else
            return true;
    }

    public void empty(){
        startTime = null;
        endTime = null;
        foodInfo = null;
        isConfirmed = null;
    }
}
