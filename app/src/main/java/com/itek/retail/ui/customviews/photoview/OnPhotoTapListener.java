package com.itek.retail.ui.customviews.photoview;

import android.widget.ImageView;

/**
 * The interface On photo tap listener.
 */
public interface OnPhotoTapListener{
  
  /**
   * On photo tap.
   *
   * @param view the view
   * @param x    the x
   * @param y    the y
   */
  void onPhotoTap(ImageView view, float x, float y);
}
