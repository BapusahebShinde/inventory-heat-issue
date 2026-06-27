package com.itek.retail.ui.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.itek.retail.R;

/**
 * The Custom gauge.
 */
public class CustomGauge extends View{
  
  private static final int DEFAULT_LONG_POINTER_SIZE = 1;
  
  private Paint mPaint;
  private float mStrokeWidth;
  private int mStrokeColor;
  private RectF mRect;
  private String mStrokeCap;
  private int mStartAngle;
  private int mSweepAngle;
  private int mStartValue;
  private int mEndValue;
  private int mValue;
  private double mPointAngle;
  private int mPoint;
  private int mPointSize;
  private int mPointStartColor;
  private int mPointEndColor;
  private int mDividerColor;
  private int mDividerSize;
  private int mDividerStepAngle;
  private int mDividersCount;
  private boolean mDividerDrawFirst;
  private boolean mDividerDrawLast;
  
  /**
   * Instantiates a new Custom gauge.
   *
   * @param context the context
   */
  public CustomGauge(Context context){
    super(context);
    init();
  }
  
  /**
   * Instantiates a new Custom gauge.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public CustomGauge(Context context, AttributeSet attrs){
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomGauge, 0, 0);
    
    // stroke style
    setStrokeWidth(a.getDimension(R.styleable.CustomGauge_gaugeStrokeWidth, 10));
    setStrokeColor(a.getColor(R.styleable.CustomGauge_gaugeStrokeColor, ContextCompat.getColor(context, android.R.color.darker_gray)));
    setStrokeCap(a.getString(R.styleable.CustomGauge_gaugeStrokeCap));
    
    // angle start and sweep (opposite direction 0, 270, 180, 90)
    setStartAngle(a.getInt(R.styleable.CustomGauge_gaugeStartAngle, 0));
    setSweepAngle(a.getInt(R.styleable.CustomGauge_gaugeSweepAngle, 360));
    
    // scale (from mStartValue to mEndValue)
    setStartValue(a.getInt(R.styleable.CustomGauge_gaugeStartValue, 0));
    setEndValue(a.getInt(R.styleable.CustomGauge_gaugeEndValue, 1000));
    
    // pointer size and color
    setPointSize(a.getInt(R.styleable.CustomGauge_gaugePointSize, 0));
    setPointStartColor(a.getColor(R.styleable.CustomGauge_gaugePointStartColor, ContextCompat.getColor(context, android.R.color.white)));
    setPointEndColor(a.getColor(R.styleable.CustomGauge_gaugePointEndColor, ContextCompat.getColor(context, android.R.color.white)));
    
    // divider options
    int dividerSize = a.getInt(R.styleable.CustomGauge_gaugeDividerSize, 0);
    setDividerColor(a.getColor(R.styleable.CustomGauge_gaugeDividerColor, ContextCompat.getColor(context, android.R.color.white)));
    int dividerStep = a.getInt(R.styleable.CustomGauge_gaugeDividerStep, 0);
    setDividerDrawFirst(a.getBoolean(R.styleable.CustomGauge_gaugeDividerDrawFirst, true));
    setDividerDrawLast(a.getBoolean(R.styleable.CustomGauge_gaugeDividerDrawLast, true));
    
    // calculating one point sweep
    mPointAngle = ((double) Math.abs(mSweepAngle) / (mEndValue - mStartValue));
    
    // calculating divider step
    if(dividerSize > 0){
      mDividerSize = mSweepAngle / (Math.abs(mEndValue - mStartValue) / dividerSize);
      mDividersCount = 100 / dividerStep;
      mDividerStepAngle = mSweepAngle / mDividersCount;
    }
    a.recycle();
    init();
  }
  
  /**
   * Init.
   */
  private void init(){
    //main Paint
    mPaint = new Paint();
    mPaint.setColor(mStrokeColor);
    mPaint.setStrokeWidth(mStrokeWidth);
    mPaint.setAntiAlias(true);
    if(!TextUtils.isEmpty(mStrokeCap)){
      if(mStrokeCap.equals("BUTT")) mPaint.setStrokeCap(Paint.Cap.BUTT);
      else if(mStrokeCap.equals("ROUND")) mPaint.setStrokeCap(Paint.Cap.ROUND);
    }
    else mPaint.setStrokeCap(Paint.Cap.BUTT);
    mPaint.setStyle(Paint.Style.STROKE);
    mRect = new RectF();
    
    mValue = mStartValue;
    mPoint = mStartAngle;
  }
  
  @Override
  protected void onDraw(Canvas canvas){
    super.onDraw(canvas);
    float padding = getStrokeWidth();
    float size = getWidth() < getHeight() ? getWidth() : getHeight();
    float width = size - (2 * padding);
    float height = size - (2 * padding);
    float radius = (width < height ? width / 2 : height / 2);
    
    float rectLeft = (getWidth() - (2 * padding)) / 2 - radius + padding;
    float rectTop = (getHeight() - (2 * padding)) / 2 - radius + padding;
    float rectRight = (getWidth() - (2 * padding)) / 2 - radius + padding + width;
    float rectBottom = (getHeight() - (2 * padding)) / 2 - radius + padding + height;
    
    mRect.set(rectLeft, rectTop, rectRight, rectBottom);
    
    mPaint.setColor(mStrokeColor);
    mPaint.setShader(null);
    canvas.drawArc(mRect, mStartAngle, mSweepAngle, false, mPaint);
    mPaint.setColor(mPointStartColor);
    mPaint.setShader(new LinearGradient(getWidth(), getHeight(), 0, 0, mPointEndColor, mPointStartColor, Shader.TileMode.CLAMP));
    if(mPointSize > 0){//if size of pointer is defined
      if(mPoint > mStartAngle + mPointSize / 2){
        canvas.drawArc(mRect, mPoint - mPointSize / 2, mPointSize, false, mPaint);
      }
      else{ //to avoid excedding start/zero point
        canvas.drawArc(mRect, mPoint, mPointSize, false, mPaint);
      }
    }
    else{ //draw from start point to value point (long pointer)
      if(mValue == mStartValue) //use non-zero default value for start point (to avoid lack of pointer for start/zero value)
        canvas.drawArc(mRect, mStartAngle, DEFAULT_LONG_POINTER_SIZE, false, mPaint);
      else canvas.drawArc(mRect, mStartAngle, mPoint - mStartAngle, false, mPaint);
    }
    
    if(mDividerSize > 0){
      mPaint.setColor(mDividerColor);
      mPaint.setShader(null);
      int i = mDividerDrawFirst ? 0 : 1;
      int max = mDividerDrawLast ? mDividersCount + 1 : mDividersCount;
      for(; i < max; i++){
        canvas.drawArc(mRect, mStartAngle + i * mDividerStepAngle, mDividerSize, false, mPaint);
      }
    }
    
  }
  
  /**
   * Get value int.
   *
   * @return the int
   */
  public int getValue(){
    return mValue;
  }
  
  /**
   * Set value.
   *
   * @param value the value
   */
  public void setValue(int value){
    mValue = value;
    mPoint = (int) (mStartAngle + (mValue - mStartValue) * mPointAngle);
    invalidate();
  }
  
  /**
   * Get stroke width float.
   *
   * @return the float
   */
  @SuppressWarnings("unused")
  public float getStrokeWidth(){
    return mStrokeWidth;
  }
  
  /**
   * Set stroke width.
   *
   * @param strokeWidth the stroke width
   */
  public void setStrokeWidth(float strokeWidth){
    mStrokeWidth = strokeWidth;
  }
  
  /**
   * Get stroke color int.
   *
   * @return the int
   */
  @SuppressWarnings("unused")
  public int getStrokeColor(){
    return mStrokeColor;
  }
  
  /**
   * Set stroke color.
   *
   * @param strokeColor the stroke color
   */
  public void setStrokeColor(int strokeColor){
    mStrokeColor = strokeColor;
  }
  
  /**
   * Get stroke cap string.
   *
   * @return the string
   */
  @SuppressWarnings("unused")
  public String getStrokeCap(){
    return mStrokeCap;
  }
  
  /**
   * Set stroke cap.
   *
   * @param strokeCap the stroke cap
   */
  public void setStrokeCap(String strokeCap){
    mStrokeCap = strokeCap;
    if(mPaint != null){
      if(mStrokeCap.equals("BUTT")){
        mPaint.setStrokeCap(Paint.Cap.BUTT);
      }
      else if(mStrokeCap.equals("ROUND")){
        mPaint.setStrokeCap(Paint.Cap.ROUND);
      }
    }
  }
  
  /**
   * Get start angle int.
   *
   * @return the int
   */
  @SuppressWarnings("unused")
  public int getStartAngle(){
    return mStartAngle;
  }
  
  /**
   * Set start angle.
   *
   * @param startAngle the start angle
   */
  public void setStartAngle(int startAngle){
    mStartAngle = startAngle;
  }
  
  /**
   * Get sweep angle int.
   *
   * @return the int
   */
  @SuppressWarnings("unused")
  public int getSweepAngle(){
    return mSweepAngle;
  }
  
  /**
   * Set sweep angle.
   *
   * @param sweepAngle the sweep angle
   */
  public void setSweepAngle(int sweepAngle){
    mSweepAngle = sweepAngle;
  }
  
  /**
   * Get start value int.
   *
   * @return the int
   */
  @SuppressWarnings("unused")
  public int getStartValue(){
    return mStartValue;
  }
  
  /**
   * Set start value.
   *
   * @param startValue the start value
   */
  public void setStartValue(int startValue){
    mStartValue = startValue;
  }
  
  /**
   * Get end value int.
   *
   * @return the int
   */
  @SuppressWarnings("unused")
  public int getEndValue(){
    return mEndValue;
  }
  
  /**
   * Set end value.
   *
   * @param endValue the end value
   */
  public void setEndValue(int endValue){
    mEndValue = endValue;
    mPointAngle = ((double) Math.abs(mSweepAngle) / (mEndValue - mStartValue));
    invalidate();
  }
  
  /**
   * Get point size int.
   *
   * @return the int
   */
  @SuppressWarnings("unused")
  public int getPointSize(){
    return mPointSize;
  }
  
  /**
   * Set point size.
   *
   * @param pointSize the point size
   */
  public void setPointSize(int pointSize){
    mPointSize = pointSize;
  }
  
  /**
   * Get point start color int.
   *
   * @return the int
   */
  @SuppressWarnings("unused")
  public int getPointStartColor(){
    return mPointStartColor;
  }
  
  /**
   * Set point start color.
   *
   * @param pointStartColor the point start color
   */
  public void setPointStartColor(int pointStartColor){
    mPointStartColor = pointStartColor;
  }
  
  /**
   * Get point end color int.
   *
   * @return the int
   */
  @SuppressWarnings("unused")
  public int getPointEndColor(){
    return mPointEndColor;
  }
  
  /**
   * Set point end color.
   *
   * @param pointEndColor the point end color
   */
  public void setPointEndColor(int pointEndColor){
    mPointEndColor = pointEndColor;
  }
  
  /**
   * Set point color.
   *
   * @param pointColor the point end color
   */
  public void setPointColor(int pointColor){
    mPointStartColor = pointColor;
    mPointEndColor = pointColor;
  }
  
  /**
   * Get divider color int.
   *
   * @return the int
   */
  @SuppressWarnings("unused")
  public int getDividerColor(){
    return mDividerColor;
  }
  
  /**
   * Set divider color.
   *
   * @param dividerColor the divider color
   */
  public void setDividerColor(int dividerColor){
    mDividerColor = dividerColor;
  }
  
  /**
   * Is divider draw first boolean.
   *
   * @return the boolean
   */
  @SuppressWarnings("unused")
  public boolean isDividerDrawFirst(){
    return mDividerDrawFirst;
  }
  
  /**
   * Set divider draw first.
   *
   * @param dividerDrawFirst the divider draw first
   */
  public void setDividerDrawFirst(boolean dividerDrawFirst){
    mDividerDrawFirst = dividerDrawFirst;
  }
  
  /**
   * Is divider draw last boolean.
   *
   * @return the boolean
   */
  @SuppressWarnings("unused")
  public boolean isDividerDrawLast(){
    return mDividerDrawLast;
  }
  
  /**
   * Set divider draw last.
   *
   * @param dividerDrawLast the divider draw last
   */
  public void setDividerDrawLast(boolean dividerDrawLast){
    mDividerDrawLast = dividerDrawLast;
  }
  
  /**
   * Set divider step.
   *
   * @param dividerStep the divider step
   */
  public void setDividerStep(int dividerStep){
    if(dividerStep > 0){
      mDividersCount = 100 / dividerStep;
      mDividerStepAngle = mSweepAngle / mDividersCount;
    }
  }
  
  /**
   * Set divider size.
   *
   * @param dividerSize the divider size
   */
  public void setDividerSize(int dividerSize){
    if(dividerSize > 0){
      mDividerSize = mSweepAngle / (Math.abs(mEndValue - mStartValue) / dividerSize);
    }
  }
}
