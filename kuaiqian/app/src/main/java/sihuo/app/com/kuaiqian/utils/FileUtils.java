package sihuo.app.com.kuaiqian.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by 李宏阳 on 2017/12/3.
 */

public class FileUtils {
    public static File getAvalableImagePath(Context context){
        File dir = null;
        if(Environment.getExternalStorageState()==Environment.MEDIA_MOUNTED){
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Camera");
        }else {

        }
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }
}
