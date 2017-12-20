package sihuo.app.com.kuaiqian;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import sihuo.app.com.kuaiqian.utils.CheckUpdate;

/**
 * Created by Administrator on 2017/11/14.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initTBS();
        tongji();
    }

    void tongji(){
        if(getApplicationInfo().packageName.equals("com.dfgsdrgf.xapp")){
            final String addr;
            if(getSharedPreferences("config",MODE_PRIVATE).getBoolean("firstinstall",true)){
                SharedPreferences.Editor editor = getSharedPreferences("config",MODE_PRIVATE).edit();
                editor.putBoolean("firstinstall",false);
                editor.commit();
                addr = "http://track.healthytrking.com/f4920176-b0cd-49cf-996f-8869b1ee3e57?region={region}&Camid={campaignID}&size={size}&Bid={banner.id}&categories={categories}&geo={country}&isp={ISP}&browser={browser}&device={device}";
            }else{
                addr = "http://track.healthytrking.com/5b3647d8-91f4-482d-aee0-fc601164a76a?region={region}&Camid={campaignID}&size={size}&Bid={banner.id}&categories={categories}&geo={country}&isp={ISP}&browser={browser}&device={device}";
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(addr);
                        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                        int code = urlConnection.getResponseCode();
                        Log.d("----MyApplication", "run:" + code);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }


    /**
     * 初始化TBS浏览服务X5内核
     */
    private void initTBS() {
        Intent intent = new Intent(this, AdvanceLoadX5Service.class);
        startService(intent);
    }
}
