package sihuo.app.com.kuaiqian;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Administrator on 2017/12/15.
 */

public class LoadingDialog extends Dialog
{
    LoadingDialog loadingDialog;
    public LoadingDialog(@NonNull Context context) {
        this(context,R.style.LoadingDialog);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    void init(){

        setCancelable(false);
        ImageView imageView = new ImageView(getContext( ));
//        Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
//        int width = display.getWidth();
//        int height = display.getHeight();
//设置dialog的宽高为屏幕的宽高
        ViewGroup.LayoutParams layoutParams = new  ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout bg = new FrameLayout(getContext());
        bg.addView(imageView,layoutParams);
        setContentView(bg, layoutParams);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.drawable.loading);
        loadingDialog = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show() {
        super.show();

    }
    public void showWithCallBack(final HideCallBack hideCallBack){
        this.show();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getContext().getResources().getBoolean(R.bool.need_guide)){
                    getContext().startActivity(new Intent(getContext(),YinDaoActivity.class));
                }
                loadingDialog.dismiss();
                if(hideCallBack!=null){
                    hideCallBack.onHide();
                }
            }
        },getContext().getResources().getInteger(R.integer.loading_delay));
    }


    public interface HideCallBack{
        void onHide();
    }
}
