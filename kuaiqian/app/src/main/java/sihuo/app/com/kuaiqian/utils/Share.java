package sihuo.app.com.kuaiqian.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import sihuo.app.com.kuaiqian.R;

/**
 * Created by Administrator on 2018/1/1.
 */

public class Share {
    /**
     *
     */
    public static void shareWebLink(Context context,String link){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,context.getResources().getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT,link);
        context.startActivity(Intent.createChooser(intent,context.getResources().getString(R.string.app_name)));
    }

    /**
     *
     */
    public static void shareWebLinkWithIcon(Context context,String link){
        InputStream inputStream= null;
        try {
            File file = null;
            file = new File(Environment.getExternalStorageDirectory(),context.getPackageName()+"share.png");
            if(!file.exists()){
                inputStream = context.getAssets().open("share_icon.png");
                FileOutputStream out = new FileOutputStream(file);
                byte[] temp = new byte[1024];
                int len = -1;
                while ((len=inputStream.read(temp))!=-1){
                    out.write(temp,0,len);
                }
                out.flush();
                inputStream.close();
                out.close();
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra("sms_body", link);
            intent.putExtra("Kdescription", link);
            intent.putExtra(Intent.EXTRA_TEXT,link);
            intent.putExtra(Intent.EXTRA_SUBJECT,link);
//            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//            intent.setType("image/*");
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_HTML_TEXT,link);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(Intent.createChooser(intent,context.getResources().getString(R.string.app_name)));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // 将InputStream转换成byte[]
    public static  byte[] inputStream2Bytes(InputStream is) {
        String str = "";
        byte[] readByte = new byte[1024];
        int readCount = -1;
        try {
            while ((readCount = is.read(readByte, 0, 1024)) != -1) {
                str += new String(readByte).trim();
            }
            return str.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
