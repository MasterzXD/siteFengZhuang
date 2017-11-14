package sihuo.app.com.kuaiqian;

import android.app.Application;

import com.tencent.smtt.sdk.QbSdk;

/**
 * Created by Administrator on 2017/11/14.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        QbSdk.initX5Environment(this,null);
    }
}
