package com.wllj.library;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.wllj.library.googleloading.GoogleProgressBar;

//import com.wllj.library.googleloading.ChromeFloatingCirclesDrawable;
//import com.wllj.library.googleloading.FoldingCirclesDrawable;
//import com.wllj.library.googleloading.GoogleMusicDicesDrawable;
//import com.wllj.library.googleloading.NexusRotationCrossDrawable;


public class LoadingTestActivity extends Activity {
//    private ProgressBar foldingCircles_progress;
//    private ProgressBar googleMusicDices_progress;
//    private ProgressBar nexusRotationCross_progress;
//    private ProgressBar chromeFloatingCircles_progress;


    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		GoogleProgressBar googleProgressBar = new GoogleProgressBar(this);
		googleProgressBar.setStyle(GoogleProgressBar.ProgressType.FOLDING_CIRCLES,0);
//        this.setContentView(googleProgressBar);
        this.setContentView(R.layout.main);
//		foldingCircles_progress = (ProgressBar)this.findViewById(R.id.foldingCircles_progress);
//		foldingCircles_progress.setIndeterminateDrawable(new FoldingCirclesDrawable.Builder(this).colors(getProgressDrawableColors()).build());
//
//		googleMusicDices_progress = (ProgressBar)this.findViewById(R.id.googleMusicDices_progress);
//		googleMusicDices_progress.setIndeterminateDrawable(new GoogleMusicDicesDrawable.Builder().build());
//
//		nexusRotationCross_progress = (ProgressBar)this.findViewById(R.id.nexusRotationCross_progress);
//		nexusRotationCross_progress.setIndeterminateDrawable(new NexusRotationCrossDrawable.Builder(this).colors(getProgressDrawableColors()).build());
//
//		chromeFloatingCircles_progress = (ProgressBar)this.findViewById(R.id.chromeFloatingCircles_progress);
//		chromeFloatingCircles_progress.setIndeterminateDrawable(new ChromeFloatingCirclesDrawable.Builder(this).colors(getProgressDrawableColors()).build());
//
		
	}

	private int[] getProgressDrawableColors() {
        int[] colors = new int[4];
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        colors[0] = prefs.getInt(getString(R.string.firstcolor_pref_key),getResources().getColor(R.color.google_loading_red));
        colors[1] = prefs.getInt(getString(R.string.secondcolor_pref_key),getResources().getColor(R.color.google_loading_blue));
        colors[2] = prefs.getInt(getString(R.string.thirdcolor_pref_key),getResources().getColor(R.color.google_loading_yellow));
        colors[3] = prefs.getInt(getString(R.string.fourthcolor_pref_key), getResources().getColor(R.color.google_loading_green));
        return colors;
    }
}
