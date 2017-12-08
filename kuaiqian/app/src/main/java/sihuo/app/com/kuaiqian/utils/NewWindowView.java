package sihuo.app.com.kuaiqian.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import sihuo.app.com.kuaiqian.R;

/**
 * Created by 李宏阳 on 2017/12/8.
 */

public class NewWindowView extends FrameLayout {

    public X5WebView x5WebView;
    TextView close;
    NewWindowView self;
    public NewWindowView(@NonNull Context context) {
        this(context,null);
    }

    public NewWindowView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public NewWindowView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        self = this;
        x5WebView = findViewById(R.id.webview);
        close = findViewById(R.id.close_window);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getParent()!=null){
                    ((ViewGroup)getParent()).removeView(self);
                }
            }
        });
    }

}
