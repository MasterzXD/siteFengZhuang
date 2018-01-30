package sihuo.app.com.kuaiqian.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;

public class TBSService extends Service {
    public TBSService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initTBS();
        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * 初始化TBS浏览服务X5内核
     */
    private void initTBS() {
        QbSdk.preInit(getApplicationContext());
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.e("----onViewInitFinished", ""+arg0);
//                Toast.makeText(MyApplication.this,"onViewInitFinished:"+arg0,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
                Log.e("----onCoreInitFinished", "initTBS");
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(),  cb);
    }
}
