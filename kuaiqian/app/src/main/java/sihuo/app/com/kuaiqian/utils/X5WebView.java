package sihuo.app.com.kuaiqian.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;


import sihuo.app.com.kuaiqian.R;

public class X5WebView extends WebView implements View.OnLongClickListener{

	private LongClickCallBack mCallBack;
	private ScrollChange scrollChange;

	public X5WebView(Context arg0) {
		this(arg0,null);
		setBackgroundColor(Color.WHITE);

	}

	@SuppressLint("SetJavaScriptEnabled")
	public X5WebView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		initWebViewSettings();
		setClickable(true);
		setOnLongClickListener(this);
	}

	private void initWebViewSettings() {
		setOverScrollMode(OVER_SCROLL_ALWAYS);
		WebSettings webSetting = this.getSettings();
		webSetting.setJavaScriptEnabled(true);
		webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
		webSetting.setAllowFileAccess(true);
		webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		webSetting.setSupportZoom(true);
//		webSetting.setBuiltInZoomControls(true);
		webSetting.setUseWideViewPort(true);
		webSetting.setDefaultZoom(WebSettings.ZoomDensity.FAR);
		webSetting.setSupportMultipleWindows(false);
		webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
		webSetting.setLoadWithOverviewMode(true);
		webSetting.setAppCacheEnabled(getResources().getBoolean(R.bool.need_cache));
		// webSetting.setDatabaseEnabled(true);
		webSetting.setDomStorageEnabled(true);
		webSetting.setGeolocationEnabled(true);
		webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
		webSetting.setPluginState(WebSettings.PluginState.ON);
//		webSetting.setBlockNetworkImage(true);
		webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
		webSetting.setCacheMode(getResources().getBoolean(R.bool.need_cache)?WebSettings.LOAD_DEFAULT:WebSettings.LOAD_NO_CACHE);
		CookieSyncManager.createInstance(getContext());
		CookieSyncManager.getInstance().sync();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
		// this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
		// settings 的设计
	}

	@Override
	public boolean onLongClick(View view) {
		// 长按事件监听（注意：需要实现LongClickCallBack接口并传入对象）

		final HitTestResult htr = getHitTestResult();//获取所点击的内容
		Log.d("----X5WebView", "onLongClick:" + htr.getType() +"____"+htr.getExtra());
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

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		Log.d("----X5WebView", "onScrollChanged:" + t);
		if(scrollChange!=null){
			scrollChange.onScrollChanged(l,t,oldl,oldt);
		}
	}

	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		Log.d("----X5WebView", "onOverScrolled:" + scrollY);
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		Log.d("----X5WebView", "onTouchEvent:" + event);
//
//		return super.onTouchEvent(event);
//	}
//
//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
////		requestDisallowInterceptTouchEvent(false);
//		Log.d("----X5WebView", "onInterceptTouchEvent:" +getScrollY() +"_"+getWebScrollY() );
////		if(scrollChange!=null){
////			scrollChange.onScrollChanged(0,1,1,1);
////		}
//		return super.onInterceptTouchEvent(ev);
//	}

	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		Log.d("----X5WebView", "overScrollBy:" + scrollY);
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
	}



	/**
	 * 长按事件回调接口，传递图片地址
	 * @author LinZhang
	 */
	public interface LongClickCallBack{
		/**用于传递图片地址*/
		void onLongClickCallBack(String imgUrl);
	}

	public interface ScrollChange{
		void onScrollChanged(int l, int t, int oldl, int oldt);
	}

	public void setScrollChange(ScrollChange scrollChange) {
		this.scrollChange = scrollChange;
	}
}
