package com.wllj.library.googleloading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.wllj.library.R;


public class GoogleProgressBar extends ProgressBar {

    public enum ProgressType {
        FOLDING_CIRCLES,
        GOOGLE_MUSIC_DICES,
        NEXUS_ROTATION_CROSS,
        CHROME_FLOATING_CIRCLES
    }

    public GoogleProgressBar(Context context) {
        this(context, null);
    }

    public GoogleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs,android.R.attr.progressBarStyle);
    }

    public GoogleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Due to some new ADT features, initialing with values from resource file may meet preview problems.
        // If View.isInEditMode() returns true, skip drawing.
        if (isInEditMode())
            return;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GoogleProgressBar, defStyle, 0);
        final int typeIndex = a.getInteger(R.styleable.GoogleProgressBar_type, context.getResources().getInteger(R.integer.default_type));
        final int colorsId = a.getResourceId(R.styleable.GoogleProgressBar_colors, R.array.google_colors);
        a.recycle();

        Drawable drawable = buildDrawable(context,typeIndex,colorsId);
        if(drawable!=null)
        setIndeterminateDrawable(drawable);
    }

    /**
     * 设置样式
     * @param progressType  @see{ProgressType}
     * @param colorsId  设置变换的颜色 default is 0. Forexamble <resources><integer-array name="google_colors">
     */
    public void setStyle(ProgressType progressType,int colorsId){
        if(colorsId==0)
            colorsId = R.array.google_colors;
        Drawable drawable = buildDrawable(getContext(),progressType,colorsId);
        if(drawable!=null)
            setIndeterminateDrawable(drawable);
    }


    private Drawable buildDrawable(Context context, ProgressType progressType,int colorsId) {
        Drawable drawable = null;
        switch (progressType        ){
            case FOLDING_CIRCLES:
                drawable = new FoldingCirclesDrawable.Builder(context)
                        .colors(getResources().getIntArray(colorsId))
                        .build();
                break;
            case GOOGLE_MUSIC_DICES:
                drawable = new GoogleMusicDicesDrawable.Builder()
                        .build();
                break;
            case NEXUS_ROTATION_CROSS:
                drawable = new NexusRotationCrossDrawable.Builder(context)
                        .colors(getResources().getIntArray(colorsId))
                        .build();
                break;
            case CHROME_FLOATING_CIRCLES:
                drawable = new ChromeFloatingCirclesDrawable.Builder(context)
                        .colors(getResources().getIntArray(colorsId))
                        .build();
                break;
        }

        return drawable;
    }

    private Drawable buildDrawable(Context context, int typeIndex,int colorsId) {
        ProgressType type = ProgressType.values()[typeIndex];
        return buildDrawable(context,type,colorsId);
    }

}
