package sihuo.app.com.kuaiqian.utils;

import android.content.Context;
import android.content.res.Resources;

import sihuo.app.com.kuaiqian.R;

/**
 * Created by Administrator on 2018/1/1.
 */

public class ADFilterTool {
    public static boolean hasAd(Context context, String url) {
        Resources res = context.getResources();
        String[] adUrls = res.getStringArray(R.array.adBlockUrl);
        for (String adUrl : adUrls) {
            if (url.contains(adUrl)) {
                return true;
            }
        }
        return false;
    }
}
