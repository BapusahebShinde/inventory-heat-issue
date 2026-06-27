package com.itek.retail.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;

import com.itek.retail.common.CommonActivity;

/**
 * The Max height linear layout.
 */
public class MaxHeightLinearLayout extends LinearLayout{
  
  private Context context;
  private int maxHeight;
  
  /**
   * Instantiates a new Max height linear layout.
   *
   * @param context the context
   */
  public MaxHeightLinearLayout(Context context){
    super(context);
    this.context = context;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((CommonActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    maxHeight = displayMetrics.heightPixels / 2;
  }
  
  /**
   * Instantiates a new Max height linear layout.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public MaxHeightLinearLayout(Context context, AttributeSet attrs){
    super(context, attrs);
    this.context = context;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((CommonActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    maxHeight = displayMetrics.heightPixels / 2;
  }
  
  /**
   * Instantiates a new Max height linear layout.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public MaxHeightLinearLayout(Context context, AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    this.context = context;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((CommonActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    maxHeight = displayMetrics.heightPixels / 2;
  }
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
    int maxHeightPx = maxHeight;
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
