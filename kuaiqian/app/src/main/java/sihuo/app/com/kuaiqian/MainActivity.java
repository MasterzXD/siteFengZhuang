package sihuo.app.com.kuaiqian;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
//import com.wllj.library.shapeloading.ShapeLoadingDialog;

import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import sihuo.app.com.kuaiqian.utils.WebViewJavaScriptFunction;
import sihuo.app.com.kuaiqian.utils.X5WebView;
import sihuo.app.com.kuaiqian.zxing.DecodeImage;

public class MainActivity extends Activity {
    final  int FILE_CHOOSER_RESULT_CODE = 40;
    ValueCallback<Uri[]> uploadMessage;
    X5WebView webview;
    ImageView back,home,floatHome,floatBack;
    ImageView refresh,shareBtn;
//    ShapeLoadingDialog shapeLoadingDialog ;
    String HOME;
    SwipeRefreshLayout refreshLayout;
    LinearLayout floatLayout;
    RelativeLayout rootView;
    RelativeLayout.LayoutParams floatParams;

    String imageUrl;
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
        HOME = getResources().getString(R.string.start_url);


        findViewById(R.id.title_layout).setVisibility(hasDaoHang?View.VISIBLE:View.GONE);
//        shapeLoadingDialog = new ShapeLoadingDialog(MainActivity.this);
//        shapeLoadingDialog.setLoadingText("loading...");

        webview = findViewById(R.id.webview);
        back = findViewById(R.id.back);
        home = findViewById(R.id.home);
        refresh = findViewById(R.id.refresh);
        rootView = findViewById(R.id.root_view);
        shareBtn = findViewById(R.id.share);

        if(shareBtn!=null){
            shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT,getResources().getString(R.string.app_name));
                    intent.putExtra(Intent.EXTRA_TEXT,webview.getUrl());
                    startActivity(Intent.createChooser(intent,getResources().getString(R.string.app_name)));
                }
            });
        }
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
                HOME = getResources().getString(R.string.home_url);
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
                                    HOME = getResources().getString(R.string.home_url);
                                    webview.loadUrl(HOME);
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

    /**
     * 判断是否为二维码
     * param url 图片地址
     * return
     */
    private boolean decodeImage(String sUrl){
        Result result = DecodeImage.handleQRCodeFormBitmap(getBitmap(sUrl));

        return result != null;
    }

    /**
     * 根据地址获取网络图片
     * @param sUrl 图片地址
     * @return
     * @throws IOException
     */
    public Bitmap getBitmap(String sUrl){
        try {
            URL url = new URL(sUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if(conn.getResponseCode() == 200){
                InputStream inputStream = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mmbitmap = bitmap;
                return bitmap;
            }else{
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"请检查网络！",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * bitmap 保存为jpg 图片
     * @param mBitmap 图片源
     * @param bitName  图片名
     */
    public void saveMyBitmap(Bitmap mBitmap,String bitName)  {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 30);
        }else{
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"存储卡不可用",Toast.LENGTH_SHORT).show();
                    }
                });

                return;
            }
            File path = new File( Environment.getExternalStorageDirectory()+"/DCIM/Camera");
            if(!path.exists()){
                path.mkdirs();
            }
            File file= new File( path,bitName + ".jpg");
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if(mBitmap==null){
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"没有获取到图片！",Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//根据请求是否通过的返回码进行判断，然后进一步运行程序
        if (grantResults.length > 0 && requestCode == 29 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveMyBitmap(mBitmap,""+System.currentTimeMillis());
        }else  if (grantResults.length > 0 && requestCode == 30 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveMyBitmap(mmbitmap,""+System.currentTimeMillis());
        }

    }
    Bitmap mmbitmap;
    byte[] mBitmap;

    /**
     * bitmap 保存为jpg 图片
     * @param mBitmap 图片源
     * @param bitName  图片名
     */
    public void saveMyBitmap(byte[] mBitmap,String bitName)  {
//        int i =ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        Toast.makeText(MainActivity.this,"checkSelfPermission:"+i,Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 29);
        }//REQUEST_EXTERNAL_STRONGE是自定义个的一个对应码，用来验证请求是否通过

        else {
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                Toast.makeText(MainActivity.this,"存储卡不可用",Toast.LENGTH_SHORT).show();
                return;
            }
            File path = new File( Environment.getExternalStorageDirectory()+"/DCIM/Camera");
            if(!path.exists()){
                path.mkdirs();
            }
            File file= new File( path,bitName + ".jpg");
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
                fOut.write(mBitmap,0,mBitmap.length);
                fOut.flush();
            } catch (FileNotFoundException e) {
                Toast.makeText(MainActivity.this,"FileNotFoundException",Toast.LENGTH_SHORT).show();
            } catch (IOException ex){
                Toast.makeText(MainActivity.this,"IOException:"+ ex.getMessage(),Toast.LENGTH_SHORT).show();
            }
            try {
                if(fOut!=null){
                    fOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
//                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), bitName, "description");
                //保存图片后发送广播通知更新数据库
//                Uri uri = Uri.fromFile(file);
                MediaScannerConnection.scanFile(this,
                        new String[] { file.getAbsolutePath() }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        });
//                MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

            }catch (Exception e){

            }
            Toast.makeText(MainActivity.this,"正在保存...",Toast.LENGTH_LONG).show();
//            Toast.makeText(MainActivity.this,"正在保存...",Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"成功保存图片到相册，请前往相册查看！",Toast.LENGTH_SHORT).show();
                }
            },5000);
        }


    }

    public class MyAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this,"正在保存...",Toast.LENGTH_LONG).show();
            MediaScannerConnection.scanFile(MainActivity.this,
                    new String[] { s }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {

                        }
                    });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"成功保存图片到相册，请前往相册查看！",Toast.LENGTH_SHORT).show();
                }
            },5000);
        }

        @Override
        protected String doInBackground(String... params) {
            String name=System.currentTimeMillis()+"";
            File file= new File( Environment.getExternalStorageDirectory()+"/DCIM/Camera/"+name + ".jpg");
            saveMyBitmap(getBitmap(params[0]),name);
            return file.getAbsolutePath();
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
        webview.setmCallBack(new X5WebView.LongClickCallBack() {
            @Override
            public void onLongClickCallBack(final String imgUrl) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(MainActivity.this).setTitle("").setNegativeButton("保存图片到相册", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(imgUrl.startsWith("data:image/png;base64,")){
                                    String tempimgUrl = imgUrl.replace("data:image/png;base64","");
                                    MainActivity.this.mBitmap = Base64.decode(tempimgUrl,Base64.DEFAULT);
                                    saveMyBitmap(MainActivity.this.mBitmap,""+System.currentTimeMillis());
//                            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
//                            Result result = DecodeImage.handleQRCodeFormBitmap(bitmap);
//                            if(result!=null){
//                                Log.e("----onLongClickCallBack", ""+result.getText());
//                                webview.loadUrl(result.getText());
//                            }
                                }else if(imgUrl.startsWith("http")){
                                    imageUrl=imgUrl;
                                    // 获取到图片地址后做相应的处理
                                    MyAsyncTask	mTask = new MyAsyncTask();
                                    mTask.execute(imgUrl);
                                }
                            }
                        }).show();
                    }
                });
            }
        });
        if(guestureNavigation||refreshable){
            webview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(webview.getWebScrollY()==0){
                        refreshLayout.setEnabled(true);
                    }else{
                        refreshLayout.setEnabled(false);
                    }
                    if(guestureNavigation){
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
                    }

                    return false;
                }
            });
        }

        webview.addJavascriptInterface(new WebViewJavaScriptFunction() {

            @Override
            public void onJsFunctionCalled(String tag) {

            }

            @JavascriptInterface
            public void toastMessage(String msg) {

            }
        }, "control");

        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
                jsResult.confirm();
                return super.onJsAlert(webView, s, s1, jsResult);
            }

            @Override
            public boolean onCreateWindow(WebView webView, boolean b, boolean b1, Message message) {
                Toast.makeText(MainActivity.this,"onCreateWindow11",Toast.LENGTH_LONG).show();
//                WebView.WebViewTransport transport = (WebView.WebViewTransport) message.obj;//以下的操作应该就是让新的webview去加载对应的url等操作。
//                transport.setWebView(webView);
//                message.sendToTarget();
                X5WebView x5WebView = new X5WebView(MainActivity.this);
                rootView.addView(x5WebView,new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                WebView.WebViewTransport transport = (WebView.WebViewTransport) message.obj;//以下的操作应该就是让新的webview去加载对应的url等操作。
                transport.setWebView(x5WebView);
                message.sendToTarget();
                return true;
//                return super.onCreateWindow(webView, b, b1, message);
            }

            @Override
            public void onCloseWindow(WebView webView) {
                super.onCloseWindow(webView);
            }

            @Override
            public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback) {

                Toast.makeText(MainActivity.this,"onShowCustomView IX5WebChromeClient",Toast.LENGTH_LONG).show();
                super.onShowCustomView(view, customViewCallback);
            }

            @Override
            public void onShowCustomView(View view, int i, IX5WebChromeClient.CustomViewCallback customViewCallback) {
                Toast.makeText(MainActivity.this,"onShowCustomView11",Toast.LENGTH_LONG).show();
                super.onShowCustomView(view, i, customViewCallback);
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
