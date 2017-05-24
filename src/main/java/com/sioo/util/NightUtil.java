package com.sioo.util;

import java.util.Calendar;

/**
 * Created by morrigan on 2017/5/3.
 */
public class NightUtil {

    public static boolean day(){
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hour<7 || hour>22){
           return false;
        }
        return true;
    }
}
