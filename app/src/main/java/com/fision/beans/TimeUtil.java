package com.fision.beans;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    public static final String getNowTimeStr(){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date());
    }
}
