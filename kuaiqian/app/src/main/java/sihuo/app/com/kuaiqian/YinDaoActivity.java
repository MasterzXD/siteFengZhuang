package sihuo.app.com.kuaiqian;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class YinDaoActivity extends Activity implements View.OnTouchListener {
    int screenW,screenH;
    LinearLayout rootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yin_dao);
        rootView = findViewById(R.id.root_layout);
        DisplayMetrics dm = getResources().getDisplayMetrics();
    //    int density = dm.density;
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;
//        final int arrat[] = {R.drawable.a,R.drawable.b,R.drawable.c};
        final int arrat[] = getResources().getIntArray(R.array.guide_list);
        for (int i = 0; i < arrat.length; i++) {
            ImageView image = new ImageView(this);
            image.setLayoutParams(new ViewGroup.LayoutParams(screenW,screenH));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setImageResource(arrat[i]);
            image.setTag(i);
            image.setOnTouchListener(this);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = (Integer) v.getTag();
                    if(index<arrat.length-1){
                        rootView.scrollBy(screenW,0);
                    }else if(index==arrat.length-1){
//                        startActivity(new Intent(YinDaoActivity.this,MainActivity.class));
                        finish();
                    }
                }
            });
            rootView.addView(image);
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
