package mianfeiwaptoapp.zhuanqian.com.mianfeiwaptoapp;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

/**
 * Created by 李宏阳 on 2017/11/9.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
// Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
    }
}
