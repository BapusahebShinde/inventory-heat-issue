package com.itek.retail.ui.customviews.photoview;

import android.view.MotionEvent;

/**
 * The interface On single fling listener.
 */
public interface OnSingleFlingListener{
  
  /**
   * On fling boolean.
   *
   * @param e1        the e 1
   * @param e2        the e 2
   * @param velocityX the velocity x
   * @param velocityY the velocity y
   * @return the boolean
   */
  boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
}
