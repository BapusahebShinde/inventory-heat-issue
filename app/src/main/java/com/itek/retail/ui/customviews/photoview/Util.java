package com.itek.retail.ui.customviews.photoview;

import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * The Util.
 */
class Util{
  
  /**
   * Check zoom levels.
   *
   * @param minZoom the min zoom
   * @param midZoom the mid zoom
   * @param maxZoom the max zoom
   */
  static void checkZoomLevels(float minZoom, float midZoom, float maxZoom){
    if(minZoom >= midZoom){
      throw new IllegalArgumentException("Minimum zoom has to be less than Medium zoom. Call setMinimumZoom() with a more appropriate value");
    }
    else if(midZoom >= maxZoom){
      throw new IllegalArgumentException("Medium zoom has to be less than Maximum zoom. Call setMaximumZoom() with a more appropriate value");
    }
  }
  
  /**
   * Has drawable boolean.
   *
   * @param imageView the image view
   * @return the boolean
   */
  static boolean hasDrawable(ImageView imageView){
    return imageView.getDrawable() != null;
  }
  
  /**
   * Is supported scale type boolean.
   *
   * @param scaleType the scale type
   * @return the boolean
   */
  static boolean isSupportedScaleType(final ImageView.ScaleType scaleType){
    if(scaleType == null){
      return false;
    }
    switch(scaleType){
      case MATRIX:
        throw new IllegalStateException("Matrix scale type is not supported");
    }
    return true;
  }
  
  /**
   * Get pointer index int.
   *
   * @param action the action
   * @return the int
   */
  static int getPointerIndex(int action){
    return (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
  }
}
