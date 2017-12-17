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

public class PageLoadingDialog extends Dialog
{
    String title;
    PageLoadingDialog loadingDialog;
    public PageLoadingDialog(@NonNull Context context) {
        this(context,R.style.LoadingDialog);
    }

    public PageLoadingDialog(@NonNull Context context,String text) {
        this(context,R.style.LoadingDialog);
        title = text;
    }

    public PageLoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    void init(){
        setCancelable(true);
        setContentView(R.layout.page_loading);
        loadingDialog = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
