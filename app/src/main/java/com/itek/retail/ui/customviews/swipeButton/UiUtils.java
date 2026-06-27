package com.itek.retail.ui.customviews.swipeButton;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.itek.retail.R;

/**
 * The Ui utils.
 */
public class UiUtils{
  
  /**
   * Animate fade hide.
   *
   * @param context the context
   * @param view    the view
   */
  public static void animateFadeHide(Context context, View view){
    if(view != null && view.getVisibility() == View.VISIBLE){
      Animation animFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
      
      view.startAnimation(animFadeOut);
      view.setVisibility(View.GONE);
    }
  }
  
  /**
   * Animate fade show.
   *
   * @param context the context
   * @param view    the view
   */
  public static void animateFadeShow(Context context, View view){
    if(view.getVisibility() != View.VISIBLE){
      Animation animFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
      
      view.startAnimation(animFadeIn);
      view.setVisibility(View.VISIBLE);
    }
  }
  
  /**
   * Dp to px int.
   *
   * @param dp the dp
   * @return the int
   */
  public static int dpToPx(int dp){
    return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
  }
  
}
