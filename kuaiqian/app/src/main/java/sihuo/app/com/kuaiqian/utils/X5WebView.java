package sihuo.app.com.kuaiqian.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;

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
