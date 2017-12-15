package sihuo.app.com.kuaiqian;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

public class Loading extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getResources().getBoolean(R.bool.need_guide)&& getSharedPreferences("config",MODE_PRIVATE).getBoolean("aaa",true)){
                    SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("aaa",false);
                    editor.commit();
                    startActivity(new Intent(Loading.this,YinDaoActivity.class));
                }else{
                    startActivity(new Intent(Loading.this,MainActivity.class));
                }

                finish();
            }
        },getResources().getInteger(R.integer.loading_delay));
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
