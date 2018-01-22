package sihuo.app.com.kuaiqian;

import android.app.Application;
import android.content.Intent;

import com.tencent.smtt.sdk.QbSdk;

import sihuo.app.com.kuaiqian.service.TBSService;

/**
 * Created by Administrator on 2017/11/14.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, TBSService.class));
    }

}
