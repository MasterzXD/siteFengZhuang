package sihuo.app.com.kuaiqian.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import sihuo.app.com.kuaiqian.R;

public class X5WebView extends WebView {

	private LongClickCallBack mCallBack;

	public X5WebView(Context arg0) {
		this(arg0,null);
		setBackgroundColor(85621);

	}

	@SuppressLint("SetJavaScriptEnabled")
	public X5WebView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		initWebViewSettings();
		this.getView().setClickable(true);
		setOnLongClickListener(this);
		setClient();
	}

	private void initWebViewSettings() {
		WebSettings webSetting = this.getSettings();
		webSetting.setJavaScriptEnabled(true);
		webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
		webSetting.setAllowFileAccess(true);
		webSetting.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSetting.setSupportZoom(true);
		webSetting.setBuiltInZoomControls(true);
		webSetting.setUseWideViewPort(true);
		webSetting.setSupportMultipleWindows(true);
		webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
		// webSetting.setLoadWithOverviewMode(true);
		webSetting.setAppCacheEnabled(getResources().getBoolean(R.bool.need_cache));
		// webSetting.setDatabaseEnabled(true);
		webSetting.setDomStorageEnabled(true);
		webSetting.setGeolocationEnabled(true);
		webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
		webSetting.setPluginsEnabled(true);
		// webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
		webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
		// webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
		webSetting.setCacheMode(getResources().getBoolean(R.bool.need_cache)?WebSettings.LOAD_DEFAULT:WebSettings.LOAD_NO_CACHE);
		CookieSyncManager.createInstance(getContext());
		CookieSyncManager.getInstance().sync();
		// this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
		// settings 的设计
	}

	void setClient(){
		this.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				try {
					if(url.startsWith("intent://")){
						Intent intent = Intent.parseUri(url,Intent.URI_INTENT_SCHEME);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getContext().startActivity(intent);
						return true;
					}else if(!url.startsWith("http")){
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setData(Uri.parse(url));
						getContext().startActivity(intent);
						return true;
					}
					view.loadUrl(url);
					return true;
				}catch (Exception e){
//                    Log.e("----should--error", ""+e.getMessage());
				}
				return super.shouldOverrideUrlLoading(view,url);

			}


		});

		this.setWebChromeClient(new WebChromeClient(){
			@Override
			public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
				jsResult.confirm();
				return super.onJsAlert(webView, s, s1, jsResult);
			}

			@Override
			public boolean onCreateWindow(WebView webView, boolean b, boolean b1, Message message) {
				NewWindowView newview = (NewWindowView) LayoutInflater.from(getContext()).inflate(R.layout.new_window,null);
				rootView.addView(newview,new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				WebView.WebViewTransport transport = (WebView.WebViewTransport) message.obj;//以下的操作应该就是让新的webview去加载对应的url等操作。
				transport.setWebView(newview.x5WebView);
				message.sendToTarget();
				return true;
			}

			@Override
			public void onCloseWindow(WebView webView) {
				super.onCloseWindow(webView);
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
				//            Log.e("----onShowFileChooser", ""+fileChooserParams);
				uploadMessage = valueCallback;
				openImageChooserActivity();
//                return super.onShowFileChooser(webView, valueCallback, fileChooserParams);
				return true;
			}
		});
	}



	@Override
	public boolean onLongClick(View view) {
		// 长按事件监听（注意：需要实现LongClickCallBack接口并传入对象）
		final HitTestResult htr = getHitTestResult();//获取所点击的内容
		if (htr.getType() == WebView.HitTestResult.IMAGE_TYPE) {//判断被点击的类型为图片
			if(mCallBack!=null){
				mCallBack.onLongClickCallBack(htr.getExtra());
				return true;
			}

		}
		return false;
	}

	public void setmCallBack(LongClickCallBack mCallBack){
//		this.mCallBack = mCallBack;
	}

	/**
	 * 长按事件回调接口，传递图片地址
	 * @author LinZhang
	 */
	public interface LongClickCallBack{
		/**用于传递图片地址*/
		void onLongClickCallBack(String imgUrl);
	}
}
