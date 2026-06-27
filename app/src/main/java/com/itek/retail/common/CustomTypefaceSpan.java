package com.itek.retail.common;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

/**
 * The Custom typeface span.
 */
public class CustomTypefaceSpan extends TypefaceSpan{
  
  private final Typeface newType;
  
  /**
   * Instantiates a new Custom typeface span.
   *
   * @param family the family
   * @param type   the type
   */
  public CustomTypefaceSpan(String family, Typeface type){
    super(family);
    newType = type;
  }
  
  /**
   * Instantiates a new Custom typeface span.
   *
   * @param type the type
   */
  public CustomTypefaceSpan(Typeface type){
    super("");
    newType = type;
  }
  
  /**
   * Apply custom type face.
   *
   * @param paint the paint
   * @param tf    the tf
   */
  @SuppressLint("WrongConstant")
  private static void applyCustomTypeFace(Paint paint, Typeface tf){
    int oldStyle;
    Typeface old = paint.getTypeface();
    if(old == null){
      oldStyle = 0;
    }
    else{
      oldStyle = old.getStyle();
    }
    
    int fake = oldStyle & ~tf.getStyle();
    if((fake & Typeface.BOLD) != 0){
      paint.setFakeBoldText(true);
    }
    
    if((fake & Typeface.ITALIC) != 0){
      paint.setTextSkewX(-0.25f);
    }
    
    paint.setTypeface(tf);
  }
  
  @Override
  public void updateDrawState(TextPaint ds){
    applyCustomTypeFace(ds, newType);
  }
  
  @Override
  public void updateMeasureState(TextPaint paint){
    applyCustomTypeFace(paint, newType);
  }
}