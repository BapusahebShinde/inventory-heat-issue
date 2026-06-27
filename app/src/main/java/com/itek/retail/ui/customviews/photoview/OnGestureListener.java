/*
 Copyright 2011, 2012 Chris Banes.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.itek.retail.ui.customviews.photoview;

/**
 * The interface On gesture listener.
 */
interface OnGestureListener{
  
  /**
   * On drag.
   *
   * @param dx the dx
   * @param dy the dy
   */
  void onDrag(float dx, float dy);
  
  /**
   * On fling.
   *
   * @param startX    the start x
   * @param startY    the start y
   * @param velocityX the velocity x
   * @param velocityY the velocity y
   */
  void onFling(float startX, float startY, float velocityX, float velocityY);
  
  /**
   * On scale.
   *
   * @param scaleFactor the scale factor
   * @param focusX      the focus x
   * @param focusY      the focus y
   */
  void onScale(float scaleFactor, float focusX, float focusY);
  
  /**
   * On scale.
   *
   * @param scaleFactor the scale factor
   * @param focusX      the focus x
   * @param focusY      the focus y
   * @param dx          the dx
   * @param dy          the dy
   */
  void onScale(float scaleFactor, float focusX, float focusY, float dx, float dy);
}