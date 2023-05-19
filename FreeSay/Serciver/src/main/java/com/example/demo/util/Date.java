package com.example.demo.util;

import java.text.SimpleDateFormat;

public class Date {
    public static String getNowTime(){
        java.util.Date d = new java.util.Date();
        //System.out.println(d);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(d);
    }
}
