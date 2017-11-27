package sihuo.app.com.kuaiqian;

import android.app.Application;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;

/**
 * Created by Administrator on 2017/11/14.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean success) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。  
                Log.d("----MyApplication", "onViewInitFinished:" + success);
            }

            @Override
            public void onCoreInitFinished() {}
        };
        QbSdk.initX5Environment(this,cb);
    }
}
