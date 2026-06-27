package com.itek.retail.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.itek.retail.common.CommonActivity;

/**
 * The Max height linear layout.
 */
public class MaxHeightConstraintLayout extends ConstraintLayout{
  
  private Context context;
  //private int maxWidth;
  private int maxHeight;
  
  /**
   * Instantiates a new Max height linear layout.
   *
   * @param context the context
   */
  public MaxHeightConstraintLayout(Context context){
    super(context);
    this.context = context;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((CommonActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    //maxWidth = (((CommonActivity) context).isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / 2;
    //maxHeight = maxWidth;
    maxHeight = displayMetrics.heightPixels / 2;
  }
  
  /**
   * Instantiates a new Max height linear layout.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public MaxHeightConstraintLayout(Context context, AttributeSet attrs){
    super(context, attrs);
    this.context = context;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((CommonActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    //maxWidth = (((CommonActivity) context).isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / 2;
    //maxHeight = maxWidth;//displayMetrics.heightPixels / 3;
    maxHeight = displayMetrics.heightPixels / 2;
  }
  
  /**
   * Instantiates a new Max height linear layout.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public MaxHeightConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    this.context = context;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((CommonActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    //maxWidth = (((CommonActivity) context).isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / 2;
    //maxHeight = maxWidth;
    maxHeight = displayMetrics.heightPixels / 2;
  }
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
    //int maxWidthPx = maxWidth;
    int maxHeightPx = maxHeight;
    //widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidthPx, MeasureSpec.AT_MOST);
    heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST);
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
  
  /**
   * Set max height.
   *
   * @param maxHeight the max height
   */
  public void setMaxHeight(int maxHeight){
    this.maxHeight = maxHeight;
    invalidate();
  }
}
