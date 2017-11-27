package sihuo.app.com.kuaiqian;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
//import com.wllj.library.shapeloading.ShapeLoadingDialog;

import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import sihuo.app.com.kuaiqian.utils.X5WebView;

public class MainActivity extends Activity {
    final  int FILE_CHOOSER_RESULT_CODE = 40;
    ValueCallback<Uri[]> uploadMessage;
    X5WebView webview;
    ImageView back,home,floatHome,floatBack;
    ImageView refresh;
//    ShapeLoadingDialog shapeLoadingDialog ;
    String HOME;
    SwipeRefreshLayout refreshLayout;
    LinearLayout floatLayout;
    RelativeLayout rootView;
    RelativeLayout.LayoutParams floatParams;

    int screenW,screenH;
    float density;
    boolean refreshable,hasDaoHang,showLoading,guestureNavigation,floatNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getBoolean(R.bool.full_screen)){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_main);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        density = dm.density;
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;
        initConfig();

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        HOME = getResources().getString(R.string.home_url);


        findViewById(R.id.title_layout).setVisibility(hasDaoHang?View.VISIBLE:View.GONE);
//        shapeLoadingDialog = new ShapeLoadingDialog(MainActivity.this);
//        shapeLoadingDialog.setLoadingText("loading...");

        webview = findViewById(R.id.webview);
        back = findViewById(R.id.back);
        home = findViewById(R.id.home);
        refresh = findViewById(R.id.refresh);
        rootView = findViewById(R.id.root_view);

        refreshLayout = findViewById(R.id.refesh_layout);
        refreshLayout.setEnabled(refreshable);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webview.reload();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(webview.canGoBack()){
                    webview.goBack();
                }
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webview.loadUrl(HOME);
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webview.reload();
            }
        });

        setupWebview();
        webview.loadUrl(HOME);
        setInitScale();

        initFloatView();
    }

    /**
     * 设置webview初始化缩放比例，因为某些机型内容不适配,比如三星曲屏
     */
    void setInitScale(){
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        Log.d("----MainActivity", "setupWebview:" + width);
        if(width > 650)
        {
            webview.setInitialScale(190);
        }else if(width > 520)
        {
            webview.setInitialScale(160);
        }else if(width > 450)
        {
            webview.setInitialScale(140);
        }else if(width > 300)
        {
            webview.setInitialScale(120);
        }else
        {
            webview.setInitialScale(100);
        }
    }

    int floatViewDownX,floatViewDownY,finalFloatViewDownX,finalFloatViewDownY;
    void initFloatView(){
        if(floatNavigation){
            floatLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.float_layout,null);
            final int floatViewW = (int)(40*density);
            final int floatViewH = (int)(100*density);
            floatParams = new RelativeLayout.LayoutParams(floatViewW,floatViewH);
            floatParams.leftMargin =  (int)(270*density);
            floatParams.topMargin = (int)(300*density);
            rootView.addView(floatLayout,floatParams);

            floatBack = floatLayout.findViewById(R.id.float_back);
            floatHome = floatLayout.findViewById(R.id.float_home);
            floatLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            floatViewDownX = (int)event.getRawX();
                            floatViewDownY = (int)event.getRawY();
                            finalFloatViewDownX = (int)event.getRawX();
                            finalFloatViewDownY = (int)event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:

                            int floatViewCurrentX = (int)event.getRawX();
                            int floatViewCurrentY = (int)event.getRawY();
                            if(Math.abs(floatViewCurrentX-finalFloatViewDownX)<10
                                    && Math.abs(floatViewCurrentY-finalFloatViewDownY)<10){
                                return true;
                            }
                            floatParams.leftMargin += floatViewCurrentX-floatViewDownX;
                            floatParams.topMargin += floatViewCurrentY-floatViewDownY;

                            if(floatParams.leftMargin<0){
                                floatParams.leftMargin=0;
                            }
                            if(floatParams.topMargin<0){
                                floatParams.topMargin=0;
                            }
                            if(floatParams.leftMargin+floatViewW>screenW){
                                floatParams.leftMargin = screenW-floatViewW;
                            }
                            if(floatParams.topMargin+floatViewH+22>screenH){
                                floatParams.topMargin = screenH-floatViewH-22;
                            }
                            floatViewDownX = floatViewCurrentX;
                            floatViewDownY = floatViewCurrentY;
                            rootView.updateViewLayout(floatLayout,floatParams);
                            break;
                        case MotionEvent.ACTION_UP:
                            if(Math.abs(event.getRawY()-finalFloatViewDownY)<10){
                                if(floatHome.getY()+floatHome.getHeight()>=event.getY()){
                                    webview.loadUrl("http://www.bff111.com/#/home");
                                }else{
                                    if(webview.canGoBack()){
                                        webview.goBack();
                                    }
                                }
                            }
                            floatLayout.setLayoutParams(floatParams);
                            break;
                    }
                    return true;
                }
            });
        }
    }


    int startX;
    final int scrollSize=200;
    void setupWebview(){
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        if(guestureNavigation){
            webview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(webview.getWebScrollY()==0){
                        refreshLayout.setEnabled(true);
                    }else{
                        refreshLayout.setEnabled(false);
                    }
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = (int) event.getX();
                            break;
                        case MotionEvent.ACTION_UP:

                            int endX = (int) event.getX();
                            if (endX > startX && webview.canGoBack() && endX - startX > scrollSize) {
                                webview.goBack();
                            } else if (endX < startX && webview.canGoForward() && startX - endX > scrollSize) {
                                webview.goForward();
                            }
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }

        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
                jsResult.confirm();
                return super.onJsAlert(webView, s, s1, jsResult);
            }

            @Override
            public void onProgressChanged(WebView webView, int progress) {
                super.onProgressChanged(webView, progress);
                if(getResources().getBoolean(R.bool.pull_refresh_enable)){
                    if(progress==100){
                        refreshLayout.setRefreshing(false);
                    }
                }
                if(getResources().getBoolean(R.bool.show_loadng)){
//                    if(progress!=100){
//                        shapeLoadingDialog.show();
//                    }else{
//                        webView.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                shapeLoadingDialog.dismiss();
//                            }
//                        },1000);
//
//                    }
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
                Log.e("----onShowFileChooser", ""+fileChooserParams);
                uploadMessage = valueCallback;
                openImageChooserActivity();
//                return super.onShowFileChooser(webView, valueCallback, fileChooserParams);
                return true;
            }
        });
        webview.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e("----should", ""+url);
//                Intent.parseUri(url,Intent.URI_INTENT_SCHEME);
                try {
                    if(url.startsWith("intent://")){
                        Intent intent = Intent.parseUri(url,Intent.URI_INTENT_SCHEME);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return true;
                    }else if(!url.startsWith("http")){
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                    view.loadUrl(url);
                    return true;
                }catch (Exception e){
//                    Log.e("----should--error", ""+e.getMessage());
                }
                return super.shouldOverrideUrlLoading(view,url);

            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest, Bundle bundle) {
                return super.shouldInterceptRequest(webView, webResourceRequest, bundle);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                Log.e("----Request", ""+url);
                return super.shouldInterceptRequest(view, url);

            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
//                Log.e("----onReceivedError", ""+error);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
//                Log.e("----onReceivedError", "failingUrl:"+failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                Log.e("----onPageFinished", ""+url);
            }
        });

    }


    void initConfig(){
        refreshable = getResources().getBoolean(R.bool.pull_refresh_enable);
        showLoading = getResources().getBoolean(R.bool.show_loadng);
        hasDaoHang = getResources().getBoolean(R.bool.save_daohang);
        guestureNavigation = getResources().getBoolean(R.bool.gesture_navigation);
        floatNavigation = getResources().getBoolean(R.bool.float_navigation);
    }

    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i,
                "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(new Uri[]{result});
                uploadMessage = null;
            }
        }
    }


    long mills = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(getResources().getBoolean(R.bool.can_goback) && !webview.getUrl().equals(HOME) && webview.canGoBack()){
                webview.goBack();
                return true;
            }
            if(System.currentTimeMillis()-mills>800){
                Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
                mills = System.currentTimeMillis();
            }else{
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
