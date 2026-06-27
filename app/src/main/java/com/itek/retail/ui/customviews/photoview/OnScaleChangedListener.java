package com.itek.retail.ui.customviews.photoview;

/**
 * The interface On scale changed listener.
 */
public interface OnScaleChangedListener{
  
  /**
   * On scale change.
   *
   * @param scaleFactor the scale factor
   * @param focusX      the focus x
   * @param focusY      the focus y
   */
  void onScaleChange(float scaleFactor, float focusX, float focusY);
}
