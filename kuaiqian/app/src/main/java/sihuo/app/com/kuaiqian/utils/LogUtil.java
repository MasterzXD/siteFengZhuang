package sihuo.app.com.kuaiqian.utils;

import android.util.Log;

/**
 * Created by 李宏阳 on 2018/1/3.
 */

public class LogUtil {
    public static final boolean DEBUG = false;
    public static void e(String tag,String content){
        if(DEBUG){
            Log.e(tag,content);
        }
    }
}
