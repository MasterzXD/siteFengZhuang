package sihuo.app.com.kuaiqian.utils;

import android.content.Context;
import android.content.Intent;

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
}
