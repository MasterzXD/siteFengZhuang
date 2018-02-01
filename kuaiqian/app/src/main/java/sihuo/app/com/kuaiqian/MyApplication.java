package sihuo.app.com.kuaiqian;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.smtt.sdk.QbSdk;

import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import sihuo.app.com.kuaiqian.service.TBSService;

/**
 * Created by Administrator on 2017/11/14.
 */

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks{

    public static boolean resume;

    @Override
    public void onCreate() {
        super.onCreate();
        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(this);
        builder.statusBarDrawable = R.drawable.iconx;
        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失
        builder.notificationDefaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;  // 设置为铃声与震动都要
        JPushInterface.setPushNotificationBuilder(1, builder);
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this.getApplicationContext());

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        resume = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        resume = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

}
