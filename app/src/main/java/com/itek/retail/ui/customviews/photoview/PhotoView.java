/*
 Copyright 2011, 2012 Chris Banes.
 <p>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p>
 http://www.apache.org/licenses/LICENSE-2.0
 <p>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.itek.retail.ui.customviews.photoview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * The Photo view.
 */
@SuppressWarnings("unused")
public class PhotoView extends AppCompatImageView{
  
  private PhotoViewAttacher attacher;
  private ScaleType pendingScaleType;
  
  /**
   * Instantiates a new Photo view.
   *
   * @param context the context
   */
  public PhotoView(Context context){
    this(context, null);
  }
  
  /**
   * Instantiates a new Photo view.
   *
   * @param context the context
   * @param attr    the attr
   */
  public PhotoView(Context context, AttributeSet attr){
    this(context, attr, 0);
  }
  
  /**
   * Instantiates a new Photo view.
   *
   * @param context  the context
   * @param attr     the attr
   * @param defStyle the def style
   */
  public PhotoView(Context context, AttributeSet attr, int defStyle){
    super(context, attr, defStyle);
    init();
  }
  
  /**
   * Init.
   */
  private void init(){
    attacher = new PhotoViewAttacher(this);
    //We always pose as a Matrix scale type, though we can change to another scale type
    //via the attacher
    super.setScaleType(ScaleType.MATRIX);
    //apply the previously applied scale type
    if(pendingScaleType != null){
      setScaleType(pendingScaleType);
      pendingScaleType = null;
    }
  }
  
  /**
   * Get attacher photo view attacher.
   *
   * @return the photo view attacher
   */
  public PhotoViewAttacher getAttacher(){
    return attacher;
  }
  
  @Override
  public ScaleType getScaleType(){
    return attacher.getScaleType();
  }
  
  @Override
  public void setScaleType(ScaleType scaleType){
    if(attacher == null){
      pendingScaleType = scaleType;
    }
    else{
      attacher.setScaleType(scaleType);
    }
  }
  
  @Override
  public Matrix getImageMatrix(){
    return attacher.getImageMatrix();
  }
  
  @Override
  public void setOnLongClickListener(OnLongClickListener l){
    attacher.setOnLongClickListener(l);
  }
  
  @Override
  public void setOnClickListener(OnClickListener l){
    attacher.setOnClickListener(l);
  }
  
  @Override
  public void setImageDrawable(Drawable drawable){
    super.setImageDrawable(drawable);
    // setImageBitmap calls through to this method
    if(attacher != null){
      attacher.update();
    }
  }
  
  @Override
  public void setImageResource(int resId){
    super.setImageResource(resId);
    if(attacher != null){
      attacher.update();
    }
  }
  
  @Override
  public void setImageURI(Uri uri){
    super.setImageURI(uri);
    if(attacher != null){
      attacher.update();
    }
  }
  
  @Override
  protected boolean setFrame(int l, int t, int r, int b){
    boolean changed = super.setFrame(l, t, r, b);
    if(changed){
      attacher.update();
    }
    return changed;
  }
  
  /**
   * Set rotation to.
   *
   * @param rotationDegree the rotation degree
   */
  public void setRotationTo(float rotationDegree){
    attacher.setRotationTo(rotationDegree);
  }
  
  /**
   * Set rotation by.
   *
   * @param rotationDegree the rotation degree
   */
  public void setRotationBy(float rotationDegree){
    attacher.setRotationBy(rotationDegree);
  }
  
  /**
   * Is zoomable boolean.
   *
   * @return the boolean
   */
  public boolean isZoomable(){
    return attacher.isZoomable();
  }
  
  /**
   * Set zoomable.
   *
   * @param zoomable the zoomable
   */
  public void setZoomable(boolean zoomable){
    attacher.setZoomable(zoomable);
  }
  
  /**
   * Get display rect rect f.
   *
   * @return the rect f
   */
  public RectF getDisplayRect(){
    return attacher.getDisplayRect();
  }
  
  /**
   * Get display matrix.
   *
   * @param matrix the matrix
   */
  public void getDisplayMatrix(Matrix matrix){
    attacher.getDisplayMatrix(matrix);
  }
  
  /**
   * Set display matrix boolean.
   *
   * @param finalRectangle the final rectangle
   * @return the boolean
   */
  @SuppressWarnings("UnusedReturnValue")
  public boolean setDisplayMatrix(Matrix finalRectangle){
    return attacher.setDisplayMatrix(finalRectangle);
  }
  
  /**
   * Get supp matrix.
   *
   * @param matrix the matrix
   */
  public void getSuppMatrix(Matrix matrix){
    attacher.getSuppMatrix(matrix);
  }
  
  /**
   * Set supp matrix boolean.
   *
   * @param matrix the matrix
   * @return the boolean
   */
  public boolean setSuppMatrix(Matrix matrix){
    return attacher.setDisplayMatrix(matrix);
  }
  
  /**
   * Get minimum scale float.
   *
   * @return the float
   */
  public float getMinimumScale(){
    return attacher.getMinimumScale();
  }
  
  /**
   * Set minimum scale.
   *
   * @param minimumScale the minimum scale
   */
  public void setMinimumScale(float minimumScale){
    attacher.setMinimumScale(minimumScale);
  }
  
  /**
   * Get medium scale float.
   *
   * @return the float
   */
  public float getMediumScale(){
    return attacher.getMediumScale();
  }
  
  /**
   * Set medium scale.
   *
   * @param mediumScale the medium scale
   */
  public void setMediumScale(float mediumScale){
    attacher.setMediumScale(mediumScale);
  }
  
  /**
   * Get maximum scale float.
   *
   * @return the float
   */
  public float getMaximumScale(){
    return attacher.getMaximumScale();
  }
  
  /**
   * Set maximum scale.
   *
   * @param maximumScale the maximum scale
   */
  public void setMaximumScale(float maximumScale){
    attacher.setMaximumScale(maximumScale);
  }
  
  /**
   * Get scale float.
   *
   * @return the float
   */
  public float getScale(){
    return attacher.getScale();
  }
  
  /**
   * Set scale.
   *
   * @param scale the scale
   */
  public void setScale(float scale){
    attacher.setScale(scale);
  }
  
  /**
   * Set allow parent intercept on edge.
   *
   * @param allow the allow
   */
  public void setAllowParentInterceptOnEdge(boolean allow){
    attacher.setAllowParentInterceptOnEdge(allow);
  }
  
  /**
   * Set scale levels.
   *
   * @param minimumScale the minimum scale
   * @param mediumScale  the medium scale
   * @param maximumScale the maximum scale
   */
  public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale){
    attacher.setScaleLevels(minimumScale, mediumScale, maximumScale);
  }
  
  /**
   * Set on matrix change listener.
   *
   * @param listener the listener
   */
  public void setOnMatrixChangeListener(OnMatrixChangedListener listener){
    attacher.setOnMatrixChangeListener(listener);
  }
  
  /**
   * Set on photo tap listener.
   *
   * @param listener the listener
   */
  public void setOnPhotoTapListener(OnPhotoTapListener listener){
    attacher.setOnPhotoTapListener(listener);
  }
  
  /**
   * Set on outside photo tap listener.
   *
   * @param listener the listener
   */
  public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener listener){
    attacher.setOnOutsidePhotoTapListener(listener);
  }
  
  /**
   * Set on view tap listener.
   *
   * @param listener the listener
   */
  public void setOnViewTapListener(OnViewTapListener listener){
    attacher.setOnViewTapListener(listener);
  }
  
  /**
   * Set on view drag listener.
   *
   * @param listener the listener
   */
  public void setOnViewDragListener(OnViewDragListener listener){
    attacher.setOnViewDragListener(listener);
  }
  
  /**
   * Set scale.
   *
   * @param scale   the scale
   * @param animate the animate
   */
  public void setScale(float scale, boolean animate){
    attacher.setScale(scale, animate);
  }
  
  /**
   * Set scale.
   *
   * @param scale   the scale
   * @param focalX  the focal x
   * @param focalY  the focal y
   * @param animate the animate
   */
  public void setScale(float scale, float focalX, float focalY, boolean animate){
    attacher.setScale(scale, focalX, focalY, animate);
  }
  
  /**
   * Set zoom transition duration.
   *
   * @param milliseconds the milliseconds
   */
  public void setZoomTransitionDuration(int milliseconds){
    attacher.setZoomTransitionDuration(milliseconds);
  }
  
  /**
   * Set on double tap listener.
   *
   * @param onDoubleTapListener the on double tap listener
   */
  public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener onDoubleTapListener){
    attacher.setOnDoubleTapListener(onDoubleTapListener);
  }
  
  /**
   * Set on scale change listener.
   *
   * @param onScaleChangedListener the on scale changed listener
   */
  public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangedListener){
    attacher.setOnScaleChangeListener(onScaleChangedListener);
  }
  
  /**
   * Set on single fling listener.
   *
   * @param onSingleFlingListener the on single fling listener
   */
  public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener){
    attacher.setOnSingleFlingListener(onSingleFlingListener);
  }
}
