package sihuo.app.com.kuaiqian;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.wllj.library.shapeloading.ShapeLoadingDialog;

import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
    final  int FILE_CHOOSER_RESULT_CODE = 40;
    ValueCallback<Uri[]> uploadMessage;
    WebView webview; 
    ImageView back;
    ImageView home;
    ImageView refresh;
    final String HOME = "http://wap.uu8r.com";
    ShapeLoadingDialog shapeLoadingDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shapeLoadingDialog  = new ShapeLoadingDialog(MainActivity.this);
        shapeLoadingDialog.setLoadingText("loading...");

        setContentView(R.layout.activity_main);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        webview = findViewById(R.id.webview);
        back = findViewById(R.id.back);
        home = findViewById(R.id.home);
        refresh = findViewById(R.id.refresh);

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

        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
                jsResult.confirm();
                return super.onJsAlert(webView, s, s1, jsResult);
            }

            @Override
            public void onProgressChanged(WebView webView, int progress) {
                super.onProgressChanged(webView, progress);
                if(progress!=100){
                    shapeLoadingDialog.show();
                }else{
                    webView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            shapeLoadingDialog.dismiss();
                        }
                    },1000);

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
//                Log.e("----should", ""+url);
                try {
                    if(!url.startsWith("http")){
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

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setAllowContentAccess(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.loadUrl(HOME);
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
            if(!webview.getUrl().equals(HOME) && webview.canGoBack()){
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
