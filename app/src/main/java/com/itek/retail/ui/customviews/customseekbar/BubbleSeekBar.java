package com.itek.retail.ui.customviews.customseekbar;

import static com.itek.retail.ui.customviews.customseekbar.BubbleSeekBar.TextPosition.BELOW_SECTION_MARK;
import static com.itek.retail.ui.customviews.customseekbar.BubbleSeekBar.TextPosition.BOTTOM_SIDES;
import static com.itek.retail.ui.customviews.customseekbar.BubbleSeekBar.TextPosition.SIDES;
import static com.itek.retail.ui.customviews.customseekbar.BubbleUtils.dp2px;
import static com.itek.retail.ui.customviews.customseekbar.BubbleUtils.sp2px;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;

/**
 * The Bubble seek bar.
 */
public class BubbleSeekBar extends View{
  
  static final int NONE = -1;
  private static final String STRING_BOUND = "0123456789";
  public float mMin; // min
  public float mMax; // max
  float dx;
  private float mProgress; // real time value
  private boolean isFloatType; // support for float type output
  private int mTrackSize; // height of right-track(on the right of thumb)
  private int mSecondTrackSize; // height of left-track(on the left of thumb)
  private int mThumbRadius; // radius of thumb
  private int mThumbRadiusOnDragging; // radius of thumb when be dragging
  private int mTrackColor; // color of right-track
  private int mSecondTrackColor; // color of left-track
  private int mThumbColor; // color of thumb
  private int mSectionCount; // shares of whole progress(max - min)
  private boolean isShowSectionMark; // show demarcation points or not
  private boolean isAutoAdjustSectionMark; // auto scroll to the nearest section_mark or not
  private boolean isShowSectionText; // show section-text or not
  private int mSectionTextSize; // text size of section-text
  private int mSectionTextColor; // text color of section-text
  @TextPosition
  private int mSectionTextPosition = NONE; // text position of section-text relative to track
  private int mSectionTextInterval; // the interval of two section-text
  private boolean isShowThumbText; // show real time progress-text under thumb or not
  private int mThumbTextSize; // text size of progress-text
  private int mThumbTextColor; // text color of progress-text
  private boolean isShowProgressInFloat; // show bubble-progress in float or not
  private boolean isTouchToSeek; // touch anywhere on track to quickly seek
  private boolean isSeekStepSection; // seek one step by one section, the progress is discrete
  private boolean isSeekBySection; // seek by section, the progress may not be linear
  private long mAnimDuration; // duration of animation
  private boolean isAlwaysShowBubble; // bubble shows all time
  private long mAlwaysShowBubbleDelay; // the delay duration before bubble shows all the time
  private boolean isHideBubble; // hide bubble
  private boolean isRtl; // right to left
  private int mBubbleColor;// color of bubble
  private int mBubbleTextSize; // text size of bubble-progress
  private int mBubbleTextColor; // text color of bubble-progress
  private float mDelta; // max - min
  private float mSectionValue; // (mDelta / mSectionCount)
  private float mThumbCenterX; // X coordinate of thumb's center
  private float mTrackLength; // pixel length of whole track
  private float mSectionOffset; // pixel length of one section
  private boolean isThumbOnDragging; // is thumb on dragging or not
  private int mTextSpace; // space between text and track
  private boolean triggerBubbleShowing;
  private SparseArray<String> mSectionTextArray = new SparseArray<>();
  private float mPreThumbCenterX;
  private boolean triggerSeekBySection;
  private OnProgressChangedListener mProgressListener; // progress changing listener
  private float mLeft; // space between left of track and left of the view
  private float mRight; // space between right of track and left of the view
  private Paint mPaint;
  private Rect mRectText;
  private WindowManager mWindowManager;
  private BubbleView mBubbleView;
  private int mBubbleRadius;
  private float mBubbleCenterRawSolidX;
  private float mBubbleCenterRawSolidY;
  private float mBubbleCenterRawX;
  private WindowManager.LayoutParams mLayoutParams;
  private int[] mPoint = new int[2];
  private boolean isTouchToSeekAnimEnd = true;
  private boolean isShowProgressInPercent = true;
  private float mPreSecValue; // previous SectionValue
  private BubbleConfigBuilder mConfigBuilder; // config attributes
  
  /**
   * Instantiates a new Bubble seek bar.
   *
   * @param context the context
   */
  public BubbleSeekBar(Context context){
    this(context, null);
  }
  
  /**
   * Instantiates a new Bubble seek bar.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public BubbleSeekBar(Context context, AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Bubble seek bar.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public BubbleSeekBar(Context context, AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BubbleSeekBar, defStyleAttr, 0);
    mMin = a.getFloat(R.styleable.BubbleSeekBar_bsb_min, 0.0f);
    mMax = a.getFloat(R.styleable.BubbleSeekBar_bsb_max, 30.0f);
    mProgress = a.getFloat(R.styleable.BubbleSeekBar_bsb_progress, mMin);
    isFloatType = a.getBoolean(R.styleable.BubbleSeekBar_bsb_is_float_type, false);
    mTrackSize = a.getDimensionPixelSize(R.styleable.BubbleSeekBar_bsb_track_size, dp2px(2));
    mSecondTrackSize = a.getDimensionPixelSize(R.styleable.BubbleSeekBar_bsb_second_track_size, mTrackSize + dp2px(2));
    mThumbRadius = a.getDimensionPixelSize(R.styleable.BubbleSeekBar_bsb_thumb_radius, mSecondTrackSize + dp2px(2));
    mThumbRadiusOnDragging = a.getDimensionPixelSize(R.styleable.BubbleSeekBar_bsb_thumb_radius_on_dragging, mSecondTrackSize * 2);
    mSectionCount = a.getInteger(R.styleable.BubbleSeekBar_bsb_section_count, 5);
    mTrackColor = ContextCompat.getColor(context, R.color.colorPrimaryDarkDef);
    TypedValue tv = new TypedValue();
    final int colorPrimary = context instanceof CommonActivity ? ((CommonActivity) context).getColorPrimaryFromTheme() : context.getTheme().resolveAttribute(R.attr.colorPrimary, tv, true) ? tv.data : ContextCompat.getColor(context, R.color.colorPrimaryDef);
    mTrackColor = a.getColor(R.styleable.BubbleSeekBar_bsb_track_color, colorPrimary);//ContextCompat.getColor(context, R.color.colorPrimary));
    TypedValue tv1 = new TypedValue();
    final int colorAccent = context instanceof CommonActivity ? ((CommonActivity) context).getColorAccentFromTheme() : context.getTheme().resolveAttribute(R.attr.colorAccent, tv1, true) ? tv1.data : ContextCompat.getColor(context, R.color.colorAccentDef);
    //mSecondTrackColor = a.getColor(R.styleable.BubbleSeekBar_bsb_second_track_color, colorAccent);
    mSecondTrackColor = colorAccent;
    mThumbColor = a.getColor(R.styleable.BubbleSeekBar_bsb_thumb_color, mSecondTrackColor);
    isShowSectionText = a.getBoolean(R.styleable.BubbleSeekBar_bsb_show_section_text, false);
    mSectionTextSize = a.getDimensionPixelSize(R.styleable.BubbleSeekBar_bsb_section_text_size, sp2px(14));
    mSectionTextColor = a.getColor(R.styleable.BubbleSeekBar_bsb_section_text_color, mTrackColor);
    isSeekStepSection = a.getBoolean(R.styleable.BubbleSeekBar_bsb_seek_step_section, false);
    isSeekBySection = a.getBoolean(R.styleable.BubbleSeekBar_bsb_seek_by_section, false);
    int pos = a.getInteger(R.styleable.BubbleSeekBar_bsb_section_text_position, NONE);
    if(pos == 0){
      mSectionTextPosition = SIDES;
    }
    else if(pos == 1){
      mSectionTextPosition = BOTTOM_SIDES;
    }
    else if(pos == 2){
      mSectionTextPosition = BELOW_SECTION_MARK;
    }
    else{
      mSectionTextPosition = NONE;
    }
    mSectionTextInterval = a.getInteger(R.styleable.BubbleSeekBar_bsb_section_text_interval, 1);
    isShowThumbText = a.getBoolean(R.styleable.BubbleSeekBar_bsb_show_thumb_text, false);
    mThumbTextSize = a.getDimensionPixelSize(R.styleable.BubbleSeekBar_bsb_thumb_text_size, sp2px(14));
    mThumbTextColor = a.getColor(R.styleable.BubbleSeekBar_bsb_thumb_text_color, mSecondTrackColor);
    mBubbleColor = a.getColor(R.styleable.BubbleSeekBar_bsb_bubble_color, mSecondTrackColor);
    mBubbleTextSize = a.getDimensionPixelSize(R.styleable.BubbleSeekBar_bsb_bubble_text_size, sp2px(14));
    mBubbleTextColor = a.getColor(R.styleable.BubbleSeekBar_bsb_bubble_text_color, Color.WHITE);
    isShowSectionMark = a.getBoolean(R.styleable.BubbleSeekBar_bsb_show_section_mark, false);
    isAutoAdjustSectionMark = a.getBoolean(R.styleable.BubbleSeekBar_bsb_auto_adjust_section_mark, false);
    isShowProgressInFloat = a.getBoolean(R.styleable.BubbleSeekBar_bsb_show_progress_in_float, false);
    isShowProgressInPercent = a.getBoolean(R.styleable.BubbleSeekBar_bsb_show_progress_in_percent, false);
    int duration = a.getInteger(R.styleable.BubbleSeekBar_bsb_anim_duration, -1);
    mAnimDuration = duration < 0 ? 200 : duration;
    isTouchToSeek = a.getBoolean(R.styleable.BubbleSeekBar_bsb_touch_to_seek, false);
    isAlwaysShowBubble = a.getBoolean(R.styleable.BubbleSeekBar_bsb_always_show_bubble, false);
    duration = a.getInteger(R.styleable.BubbleSeekBar_bsb_always_show_bubble_delay, 0);
    mAlwaysShowBubbleDelay = duration < 0 ? 0 : duration;
    isHideBubble = a.getBoolean(R.styleable.BubbleSeekBar_bsb_hide_bubble, false);
    isRtl = a.getBoolean(R.styleable.BubbleSeekBar_bsb_rtl, false);
    setEnabled(a.getBoolean(R.styleable.BubbleSeekBar_android_enabled, isEnabled()));
    a.recycle();
    
    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setTextAlign(Paint.Align.CENTER);
    
    mRectText = new Rect();
    mTextSpace = dp2px(2);
    
    initConfigByPriority();
    
    if(isHideBubble) return;
    
    mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    
    // init BubbleView
    mBubbleView = new BubbleView(context);
    mBubbleView.setProgressText(isShowProgressInFloat ? String.valueOf(getProgressFloat()) : String.format("%02d", getProgress()) + (isShowProgressInPercent ? "%" : ""));
    
    mLayoutParams = new WindowManager.LayoutParams();
    mLayoutParams.gravity = Gravity.START | Gravity.TOP;
    mLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
    mLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
    mLayoutParams.format = PixelFormat.TRANSLUCENT;
    mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
    // MIUI禁止了开发者使用TYPE_TOAST，Android 7.1.1 对TYPE_TOAST的使用更严格
    if(BubbleUtils.isMIUI() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1){
      mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
    }
    else{
      mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
    }
    
    calculateRadiusOfBubble();
  }
  
  /**
   * Init config by priority.
   */
  private void initConfigByPriority(){
    if(mMin == mMax){
      mMin = 0.0f;
      mMax = 100.0f;
    }
    if(mMin > mMax){
      float tmp = mMax;
      mMax = mMin;
      mMin = tmp;
    }
    if(mProgress < mMin){
      mProgress = mMin;
    }
    if(mProgress > mMax){
      mProgress = mMax;
    }
    if(mSecondTrackSize < mTrackSize){
      mSecondTrackSize = mTrackSize + dp2px(2);
    }
    if(mThumbRadius <= mSecondTrackSize){
      mThumbRadius = mSecondTrackSize + dp2px(2);
    }
    if(mThumbRadiusOnDragging <= mSecondTrackSize){
      mThumbRadiusOnDragging = mSecondTrackSize * 2;
    }
    if(mSectionCount <= 0){
      mSectionCount = 5;
    }
    mDelta = mMax - mMin;
    mSectionValue = mDelta / mSectionCount;
    
    if(mSectionValue < 1){
      isFloatType = true;
    }
    if(isFloatType){
      isShowProgressInFloat = true;
    }
    if(mSectionTextPosition != NONE){
      isShowSectionText = true;
    }
    if(isShowSectionText){
      if(mSectionTextPosition == NONE){
        mSectionTextPosition = SIDES;
      }
      if(mSectionTextPosition == BELOW_SECTION_MARK){
        isShowSectionMark = true;
      }
    }
    if(mSectionTextInterval < 1){
      mSectionTextInterval = 1;
    }
    
    initSectionTextArray();
    
    if(isSeekStepSection){
      isSeekBySection = false;
      isAutoAdjustSectionMark = false;
    }
    if(isAutoAdjustSectionMark && !isShowSectionMark){
      isAutoAdjustSectionMark = false;
    }
    if(isSeekBySection){
      mPreSecValue = mMin;
      if(mProgress != mMin){
        mPreSecValue = mSectionValue;
      }
      isShowSectionMark = true;
      isAutoAdjustSectionMark = true;
    }
    if(isHideBubble){
      isAlwaysShowBubble = false;
    }
    if(isAlwaysShowBubble){
      setProgress(mProgress);
    }
    
    mThumbTextSize = isFloatType || isSeekBySection || (isShowSectionText && mSectionTextPosition == BELOW_SECTION_MARK) ? mSectionTextSize : mThumbTextSize;
  }
  
  /**
   * Calculate radius of bubble.
   */
  private void calculateRadiusOfBubble(){
    mPaint.setTextSize(mBubbleTextSize);
    
    // 计算滑到两端气泡里文字需要显示的宽度，比较取最大值为气泡的半径
    String text;
    if(isShowProgressInFloat){
      text = float2String(isRtl ? mMax : mMin);
    }
    else{
      if(isRtl){
        text = isFloatType ? float2String(mMax) : String.valueOf((int) mMax);
      }
      else{
        text = isFloatType ? float2String(mMin) : String.valueOf((int) mMin);
      }
    }
    mPaint.getTextBounds(text, 0, text.length(), mRectText);
    int w1 = (mRectText.width() + mTextSpace * 2) >> 1;
    
    if(isShowProgressInFloat){
      text = float2String(isRtl ? mMin : mMax);
    }
    else{
      if(isRtl){
        text = isFloatType ? float2String(mMin) : String.valueOf((int) mMin);
      }
      else{
        text = isFloatType ? float2String(mMax) : String.valueOf((int) mMax);
      }
    }
    mPaint.getTextBounds(text, 0, text.length(), mRectText);
    int w2 = (mRectText.width() + mTextSpace * 2) >> 1;
    
    mBubbleRadius = dp2px(14); // default 14dp
    int max = Math.max(mBubbleRadius, Math.max(w1, w2));
    mBubbleRadius = max + mTextSpace;
  }
  
  /**
   * Init section text array.
   */
  private void initSectionTextArray(){
    boolean ifBelowSection = mSectionTextPosition == BELOW_SECTION_MARK;
    boolean ifInterval = mSectionTextInterval > 1 && mSectionCount % 2 == 0;
    float sectionValue;
    for(int i = 0; i <= mSectionCount; i++){
      sectionValue = isRtl ? mMax - mSectionValue * i : mMin + mSectionValue * i;
      
      if(ifBelowSection){
        if(ifInterval){
          if(i % mSectionTextInterval == 0){
            sectionValue = isRtl ? mMax - mSectionValue * i : mMin + mSectionValue * i;
          }
          else{
            continue;
          }
        }
      }
      else{
        if(i != 0 && i != mSectionCount){
          continue;
        }
      }
      
      mSectionTextArray.put(i, isFloatType ? float2String(sectionValue) : (int) sectionValue + "");
    }
  }
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    
    int height = mThumbRadiusOnDragging * 2; // 默认高度为拖动时thumb圆的直径
    if(isShowThumbText){
      mPaint.setTextSize(mThumbTextSize);
      mPaint.getTextBounds("j", 0, 1, mRectText); // j is the highest of all letters and numbers
      height += mRectText.height(); // 如果显示实时进度，则原来基础上加上进度文字高度和间隔
    }
    if(isShowSectionText && mSectionTextPosition >= BOTTOM_SIDES){ // 如果Section值在track之下显示，比较取较大值
      mPaint.setTextSize(mSectionTextSize);
      mPaint.getTextBounds("j", 0, 1, mRectText);
      height = Math.max(height, mThumbRadiusOnDragging * 2 + mRectText.height());
    }
    height += mTextSpace * 2;
    setMeasuredDimension(resolveSize(dp2px(180), widthMeasureSpec), height);
    
    mLeft = getPaddingLeft() + mThumbRadiusOnDragging;
    mRight = getMeasuredWidth() - getPaddingRight() - mThumbRadiusOnDragging;
    
    if(isShowSectionText){
      mPaint.setTextSize(mSectionTextSize);
      
      if(mSectionTextPosition == SIDES){
        String text = mSectionTextArray.get(0);
        mPaint.getTextBounds(text, 0, text.length(), mRectText);
        mLeft += (mRectText.width() + mTextSpace);
        
        text = mSectionTextArray.get(mSectionCount);
        mPaint.getTextBounds(text, 0, text.length(), mRectText);
        mRight -= (mRectText.width() + mTextSpace);
      }
      else if(mSectionTextPosition >= BOTTOM_SIDES){
        String text = mSectionTextArray.get(0);
        mPaint.getTextBounds(text, 0, text.length(), mRectText);
        float max = Math.max(mThumbRadiusOnDragging, mRectText.width() / 2f);
        mLeft = getPaddingLeft() + max + mTextSpace;
        
        text = mSectionTextArray.get(mSectionCount);
        mPaint.getTextBounds(text, 0, text.length(), mRectText);
        max = Math.max(mThumbRadiusOnDragging, mRectText.width() / 2f);
        mRight = getMeasuredWidth() - getPaddingRight() - max - mTextSpace;
      }
    }
    else if(isShowThumbText && mSectionTextPosition == NONE){
      mPaint.setTextSize(mThumbTextSize);
      
      String text = mSectionTextArray.get(0);
      mPaint.getTextBounds(text, 0, text.length(), mRectText);
      float max = Math.max(mThumbRadiusOnDragging, mRectText.width() / 2f);
      mLeft = getPaddingLeft() + max + mTextSpace;
      
      text = mSectionTextArray.get(mSectionCount);
      mPaint.getTextBounds(text, 0, text.length(), mRectText);
      max = Math.max(mThumbRadiusOnDragging, mRectText.width() / 2f);
      mRight = getMeasuredWidth() - getPaddingRight() - max - mTextSpace;
    }
    
    mTrackLength = mRight - mLeft;
    mSectionOffset = mTrackLength / mSectionCount;
    
    if(!isHideBubble){
      mBubbleView.measure(widthMeasureSpec, heightMeasureSpec);
    }
  }
  
  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom){
    super.onLayout(changed, left, top, right, bottom);
    
    if(!isHideBubble){
      locatePositionInWindow();
    }
  }
  
  /**
   * Locate position in window.
   */
  private void locatePositionInWindow(){
    getLocationInWindow(mPoint);
    
    ViewParent parent = getParent();
    if(parent instanceof View && ((View) parent).getMeasuredWidth() > 0){
      mPoint[0] %= ((View) parent).getMeasuredWidth();
    }
    
    if(isRtl){
      mBubbleCenterRawSolidX = mPoint[0] + mRight - mBubbleView.getMeasuredWidth() / 2f;
    }
    else{
      mBubbleCenterRawSolidX = mPoint[0] + mLeft - mBubbleView.getMeasuredWidth() / 2f;
    }
    mBubbleCenterRawX = calculateCenterRawXofBubbleView();
    mBubbleCenterRawSolidY = mPoint[1] - mBubbleView.getMeasuredHeight();
    mBubbleCenterRawSolidY -= dp2px(24);
    if(BubbleUtils.isMIUI()){
      mBubbleCenterRawSolidY -= dp2px(4);
    }
    
    Context context = getContext();
    if(context instanceof Activity){
      Window window = ((Activity) context).getWindow();
      if(window != null){
        int flags = window.getAttributes().flags;
        if((flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0){
          Resources res = Resources.getSystem();
          int id = res.getIdentifier("status_bar_height", "dimen", "android");
          mBubbleCenterRawSolidY += res.getDimensionPixelSize(id);
        }
      }
    }
  }
  
  @Override
  protected void onDraw(Canvas canvas){
    super.onDraw(canvas);
    
    float xLeft = getPaddingLeft();
    float xRight = getMeasuredWidth() - getPaddingRight();
    float yTop = getPaddingTop() + mThumbRadiusOnDragging;
    
    // draw sectionText SIDES or BOTTOM_SIDES
    if(isShowSectionText){
      mPaint.setColor(mSectionTextColor);
      mPaint.setTextSize(mSectionTextSize);
      mPaint.getTextBounds(STRING_BOUND, 0, STRING_BOUND.length(), mRectText); // compute solid height
      
      if(mSectionTextPosition == SIDES){
        float y_ = yTop + mRectText.height() / 2f;
        
        String text = mSectionTextArray.get(0);
        mPaint.getTextBounds(text, 0, text.length(), mRectText);
        canvas.drawText(text, xLeft + mRectText.width() / 2f, y_, mPaint);
        xLeft += mRectText.width() + mTextSpace;
        
        text = mSectionTextArray.get(mSectionCount);
        mPaint.getTextBounds(text, 0, text.length(), mRectText);
        canvas.drawText(text, xRight - (mRectText.width() + 0.5f) / 2f, y_, mPaint);
        xRight -= (mRectText.width() + mTextSpace);
        
      }
      else if(mSectionTextPosition >= BOTTOM_SIDES){
        float y_ = yTop + mThumbRadiusOnDragging + mTextSpace;
        
        String text = mSectionTextArray.get(0);
        mPaint.getTextBounds(text, 0, text.length(), mRectText);
        y_ += mRectText.height();
        xLeft = mLeft;
        if(mSectionTextPosition == BOTTOM_SIDES){
          canvas.drawText(text, xLeft, y_, mPaint);
        }
        
        text = mSectionTextArray.get(mSectionCount);
        mPaint.getTextBounds(text, 0, text.length(), mRectText);
        xRight = mRight;
        if(mSectionTextPosition == BOTTOM_SIDES){
          canvas.drawText(text, xRight, y_, mPaint);
        }
      }
    }
    else if(isShowThumbText && mSectionTextPosition == NONE){
      xLeft = mLeft;
      xRight = mRight;
    }
    
    if((!isShowSectionText && !isShowThumbText) || mSectionTextPosition == SIDES){
      xLeft += mThumbRadiusOnDragging;
      xRight -= mThumbRadiusOnDragging;
    }
    
    boolean isShowTextBelowSectionMark = isShowSectionText && mSectionTextPosition == BELOW_SECTION_MARK;
    
    // draw sectionMark & sectionText BELOW_SECTION_MARK
    if(isShowTextBelowSectionMark || isShowSectionMark){
      mPaint.setTextSize(mSectionTextSize);
      mPaint.getTextBounds(STRING_BOUND, 0, STRING_BOUND.length(), mRectText); // compute solid height
      
      float x_;
      float y_ = yTop + mRectText.height() + mThumbRadiusOnDragging + mTextSpace;
      float r = (mThumbRadiusOnDragging - dp2px(2)) / 2f;
      float junction; // where secondTrack meets firstTrack
      if(isRtl){
        junction = mRight - mTrackLength / mDelta * Math.abs(mProgress - mMin);
      }
      else{
        junction = mLeft + mTrackLength / mDelta * Math.abs(mProgress - mMin);
      }
      
      for(int i = 0; i <= mSectionCount; i++){
        x_ = xLeft + i * mSectionOffset;
        if(isRtl){
          mPaint.setColor(x_ <= junction ? mTrackColor : mSecondTrackColor);
        }
        else{
          mPaint.setColor(x_ <= junction ? mSecondTrackColor : mTrackColor);
        }
        // sectionMark
        canvas.drawCircle(x_, yTop, r, mPaint);
        
        // sectionText belows section
        if(isShowTextBelowSectionMark){
          mPaint.setColor(mSectionTextColor);
          if(mSectionTextArray.get(i, null) != null){
            canvas.drawText(mSectionTextArray.get(i), x_, y_, mPaint);
          }
        }
      }
    }
    
    if(!isThumbOnDragging || isAlwaysShowBubble){
      if(isRtl){
        mThumbCenterX = xRight - mTrackLength / mDelta * (mProgress - mMin);
      }
      else{
        mThumbCenterX = xLeft + mTrackLength / mDelta * (mProgress - mMin);
      }
    }
    
    // draw thumbText
    if(isShowThumbText && !isThumbOnDragging && isTouchToSeekAnimEnd){
      mPaint.setColor(mThumbTextColor);
      mPaint.setTextSize(mThumbTextSize);
      mPaint.getTextBounds("0123456789", 0, "0123456789".length(), mRectText); // compute solid height
      float y_ = yTop + mRectText.height() + mThumbRadiusOnDragging + mTextSpace;
      
      if(isFloatType || (isShowProgressInFloat && mSectionTextPosition == BOTTOM_SIDES && mProgress != mMin && mProgress != mMax)){
        canvas.drawText(String.format("%02.0f", getProgressFloat()), mThumbCenterX, y_, mPaint);
      }
      else{
        canvas.drawText(String.format("%02d", getProgress()) + (isShowProgressInPercent ? "%" : ""), mThumbCenterX, y_, mPaint);
      }
    }
    
    // draw track
    mPaint.setColor(mSecondTrackColor);
    mPaint.setStrokeWidth(mSecondTrackSize);
    if(isRtl){
      canvas.drawLine(xRight, yTop, mThumbCenterX, yTop, mPaint);
    }
    else{
      canvas.drawLine(xLeft, yTop, mThumbCenterX, yTop, mPaint);
    }
    
    // draw second track
    mPaint.setColor(mTrackColor);
    mPaint.setStrokeWidth(mTrackSize);
    if(isRtl){
      canvas.drawLine(mThumbCenterX, yTop, xLeft, yTop, mPaint);
    }
    else{
      canvas.drawLine(mThumbCenterX, yTop, xRight, yTop, mPaint);
    }
    
    // draw thumb
    mPaint.setColor(mThumbColor);
    canvas.drawCircle(mThumbCenterX, yTop, isThumbOnDragging ? mThumbRadiusOnDragging : mThumbRadius, mPaint);
  }
  
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh){
    super.onSizeChanged(w, h, oldw, oldh);
    
    post(() -> requestLayout());
  }
  
  @Override
  protected void onVisibilityChanged(@NonNull View changedView, int visibility){
    if(isHideBubble || !isAlwaysShowBubble) return;
    
    if(visibility != VISIBLE){
      hideBubble();
    }
    else{
      if(triggerBubbleShowing){
        showBubble();
      }
    }
    super.onVisibilityChanged(changedView, visibility);
  }
  
  @Override
  protected void onDetachedFromWindow(){
    hideBubble();
    super.onDetachedFromWindow();
  }
  
  @Override
  public boolean performClick(){
    return super.performClick();
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event){
    switch(event.getActionMasked()){
      case MotionEvent.ACTION_DOWN:
        performClick();
        getParent().requestDisallowInterceptTouchEvent(true);
        
        isThumbOnDragging = isThumbTouched(event);
        if(isThumbOnDragging){
          if(isSeekBySection && !triggerSeekBySection){
            triggerSeekBySection = true;
          }
          if(isAlwaysShowBubble && !triggerBubbleShowing){
            triggerBubbleShowing = true;
          }
          if(!isHideBubble){
            showBubble();
          }
          
          invalidate();
        }
        else if(isTouchToSeek && isTrackTouched(event)){
          isThumbOnDragging = true;
          if(isSeekBySection && !triggerSeekBySection){
            triggerSeekBySection = true;
          }
          if(isAlwaysShowBubble){
            hideBubble();
            triggerBubbleShowing = true;
          }
          
          if(isSeekStepSection){
            mThumbCenterX = mPreThumbCenterX = calThumbCxWhenSeekStepSection(event.getX());
          }
          else{
            mThumbCenterX = event.getX();
            if(mThumbCenterX < mLeft){
              mThumbCenterX = mLeft;
            }
            if(mThumbCenterX > mRight){
              mThumbCenterX = mRight;
            }
          }
          
          mProgress = calculateProgress();
          
          if(!isHideBubble){
            mBubbleCenterRawX = calculateCenterRawXofBubbleView();
            showBubble();
          }
          
          invalidate();
        }
        
        dx = mThumbCenterX - event.getX();
        
        break;
      case MotionEvent.ACTION_MOVE:
        if(isThumbOnDragging){
          boolean flag = true;
          
          if(isSeekStepSection){
            float x = calThumbCxWhenSeekStepSection(event.getX());
            if(x != mPreThumbCenterX){
              mThumbCenterX = mPreThumbCenterX = x;
            }
            else{
              flag = false;
            }
          }
          else{
            mThumbCenterX = event.getX() + dx;
            if(mThumbCenterX < mLeft){
              mThumbCenterX = mLeft;
            }
            if(mThumbCenterX > mRight){
              mThumbCenterX = mRight;
            }
          }
          
          if(flag){
            mProgress = calculateProgress();
            
            if(!isHideBubble && mBubbleView.getParent() != null){
              mBubbleCenterRawX = calculateCenterRawXofBubbleView();
              mLayoutParams.x = (int) (mBubbleCenterRawX + 0.5f);
              mWindowManager.updateViewLayout(mBubbleView, mLayoutParams);
              mBubbleView.setProgressText(isShowProgressInFloat ? String.valueOf(getProgressFloat()) : String.format("%02d", getProgress()) + (isShowProgressInPercent ? "%" : ""));
            }
            else{
              processProgress();
            }
            
            invalidate();
            
            if(mProgressListener != null){
              mProgressListener.onProgressChanged(this, getProgress(), getProgressFloat(), true);
            }
          }
        }
        
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        getParent().requestDisallowInterceptTouchEvent(false);
        
        if(isAutoAdjustSectionMark){
          if(isTouchToSeek){
            postDelayed(() -> {
              isTouchToSeekAnimEnd = false;
              autoAdjustSection();
            }, mAnimDuration);
          }
          else{
            autoAdjustSection();
          }
        }
        else if(isThumbOnDragging || isTouchToSeek){
          if(isHideBubble){
            animate().setDuration(mAnimDuration).setStartDelay(!isThumbOnDragging && isTouchToSeek ? 300 : 0).setListener(new AnimatorListenerAdapter(){
              @Override
              public void onAnimationEnd(Animator animation){
                isThumbOnDragging = false;
                invalidate();
              }
              
              @Override
              public void onAnimationCancel(Animator animation){
                isThumbOnDragging = false;
                invalidate();
              }
            }).start();
          }
          else{
            postDelayed(() -> mBubbleView.animate().alpha(isAlwaysShowBubble ? 1f : 0f).setDuration(mAnimDuration).setListener(new AnimatorListenerAdapter(){
              @Override
              public void onAnimationEnd(Animator animation){
                if(!isAlwaysShowBubble){
                  hideBubble();
                }
                
                isThumbOnDragging = false;
                invalidate();
              }
              
              @Override
              public void onAnimationCancel(Animator animation){
                if(!isAlwaysShowBubble){
                  hideBubble();
                }
                
                isThumbOnDragging = false;
                invalidate();
              }
            }).start(), mAnimDuration);
          }
        }
        
        if(mProgressListener != null){
          mProgressListener.onProgressChanged(this, getProgress(), getProgressFloat(), true);
          mProgressListener.getProgressOnActionUp(this, String.format("%02d", getProgress()) + (isShowProgressInPercent ? "%" : ""), getProgressFloat());
        }
        
        break;
      default:
        break;
    }
    
    return isThumbOnDragging || isTouchToSeek || super.onTouchEvent(event);
  }
  
  /**
   * Is thumb touched boolean.
   *
   * @param event the event
   * @return the boolean
   */
  private boolean isThumbTouched(MotionEvent event){
    if(!isEnabled()) return false;
    
    float distance = mTrackLength / mDelta * (mProgress - mMin);
    float x = isRtl ? mRight - distance : mLeft + distance;
    float y = getMeasuredHeight() / 2f;
    return (event.getX() - x) * (event.getX() - x) + (event.getY() - y) * (event.getY() - y) <= (mLeft + dp2px(8)) * (mLeft + dp2px(8));
  }
  
  /**
   * Is track touched boolean.
   *
   * @param event the event
   * @return the boolean
   */
  private boolean isTrackTouched(MotionEvent event){
    return isEnabled() && event.getX() >= getPaddingLeft() && event.getX() <= getMeasuredWidth() - getPaddingRight() && event.getY() >= getPaddingTop() && event.getY() <= getMeasuredHeight() - getPaddingBottom();
  }
  
  /**
   * Cal thumb cx when seek step section float.
   *
   * @param touchedX the touched x
   * @return the float
   */
  private float calThumbCxWhenSeekStepSection(float touchedX){
    if(touchedX <= mLeft) return mLeft;
    if(touchedX >= mRight) return mRight;
    
    int i;
    float x = 0;
    for(i = 0; i <= mSectionCount; i++){
      x = i * mSectionOffset + mLeft;
      if(x <= touchedX && touchedX - x <= mSectionOffset){
        break;
      }
    }
    
    if(touchedX - x <= mSectionOffset / 2f){
      return x;
    }
    else{
      return (i + 1) * mSectionOffset + mLeft;
    }
  }
  
  /**
   * Auto adjust section.
   */
  private void autoAdjustSection(){
    int i;
    float x = 0;
    for(i = 0; i <= mSectionCount; i++){
      x = i * mSectionOffset + mLeft;
      if(x <= mThumbCenterX && mThumbCenterX - x <= mSectionOffset){
        break;
      }
    }
    
    BigDecimal bigDecimal = BigDecimal.valueOf(mThumbCenterX);
    float x_ = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
    boolean onSection = x_ == x; // 就在section处，不作valueAnim，优化性能
    
    AnimatorSet animatorSet = new AnimatorSet();
    
    ValueAnimator valueAnim = null;
    if(!onSection){
      if(mThumbCenterX - x <= mSectionOffset / 2f){
        valueAnim = ValueAnimator.ofFloat(mThumbCenterX, x);
      }
      else{
        valueAnim = ValueAnimator.ofFloat(mThumbCenterX, (i + 1) * mSectionOffset + mLeft);
      }
      valueAnim.setInterpolator(new LinearInterpolator());
      valueAnim.addUpdateListener(animation -> {
        mThumbCenterX = (float) animation.getAnimatedValue();
        mProgress = calculateProgress();
        
        if(!isHideBubble && mBubbleView.getParent() != null){
          mBubbleCenterRawX = calculateCenterRawXofBubbleView();
          mLayoutParams.x = (int) (mBubbleCenterRawX + 0.5f);
          mWindowManager.updateViewLayout(mBubbleView, mLayoutParams);
          mBubbleView.setProgressText(isShowProgressInFloat ? String.valueOf(getProgressFloat()) : String.format("%02d", getProgress()) + (isShowProgressInPercent ? "%" : ""));
        }
        else{
          processProgress();
        }
        
        invalidate();
        
        if(mProgressListener != null){
          mProgressListener.onProgressChanged(BubbleSeekBar.this, getProgress(), getProgressFloat(), true);
        }
      });
    }
    
    if(isHideBubble){
      if(!onSection){
        animatorSet.setDuration(mAnimDuration).playTogether(valueAnim);
      }
    }
    else{
      ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mBubbleView, View.ALPHA, isAlwaysShowBubble ? 1 : 0);
      if(onSection){
        animatorSet.setDuration(mAnimDuration).play(alphaAnim);
      }
      else{
        animatorSet.setDuration(mAnimDuration).playTogether(valueAnim, alphaAnim);
      }
    }
    animatorSet.addListener(new AnimatorListenerAdapter(){
      @Override
      public void onAnimationEnd(Animator animation){
        if(!isHideBubble && !isAlwaysShowBubble){
          hideBubble();
        }
        
        mProgress = calculateProgress();
        isThumbOnDragging = false;
        isTouchToSeekAnimEnd = true;
        invalidate();
        
        if(mProgressListener != null){
          mProgressListener.getProgressOnFinally(BubbleSeekBar.this, getProgress(), getProgressFloat(), true);
        }
      }
      
      @Override
      public void onAnimationCancel(Animator animation){
        if(!isHideBubble && !isAlwaysShowBubble){
          hideBubble();
        }
        
        mProgress = calculateProgress();
        isThumbOnDragging = false;
        isTouchToSeekAnimEnd = true;
        invalidate();
      }
    });
    animatorSet.start();
  }
  
  /**
   * Show bubble.
   */
  private void showBubble(){
    if(mBubbleView == null || mBubbleView.getParent() != null){
      return;
    }
    
    mLayoutParams.x = (int) (mBubbleCenterRawX + 0.5f);
    mLayoutParams.y = (int) (mBubbleCenterRawSolidY + 0.5f);
    
    mBubbleView.setAlpha(0);
    mBubbleView.setVisibility(VISIBLE);
    mBubbleView.animate().alpha(1f).setDuration(isTouchToSeek ? 0 : mAnimDuration).setListener(new AnimatorListenerAdapter(){
      @Override
      public void onAnimationStart(Animator animation){
        mWindowManager.addView(mBubbleView, mLayoutParams);
      }
    }).start();
    mBubbleView.setProgressText(isShowProgressInFloat ? String.valueOf(getProgressFloat()) : String.format("%02d", getProgress()) + (isShowProgressInPercent ? "%" : ""));
  }
  
  /**
   * Hide bubble.
   */
  private void hideBubble(){
    if(mBubbleView == null) return;
    
    mBubbleView.setVisibility(GONE); // 防闪烁
    if(mBubbleView.getParent() != null){
      mWindowManager.removeViewImmediate(mBubbleView);
    }
  }
  
  /**
   * Float 2 string string.
   *
   * @param value the value
   * @return the string
   */
  private String float2String(float value){
    return String.valueOf(formatFloat(value));
  }
  
  /**
   * Format float float.
   *
   * @param value the value
   * @return the float
   */
  private float formatFloat(float value){
    BigDecimal bigDecimal = BigDecimal.valueOf(value);
    return bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
  }
  
  /**
   * Calculate center raw xof bubble view float.
   *
   * @return the float
   */
  private float calculateCenterRawXofBubbleView(){
    if(isRtl){
      return mBubbleCenterRawSolidX - mTrackLength * (mProgress - mMin) / mDelta;
    }
    else{
      return mBubbleCenterRawSolidX + mTrackLength * (mProgress - mMin) / mDelta;
    }
  }
  
  /**
   * Calculate progress float.
   *
   * @return the float
   */
  private float calculateProgress(){
    if(isRtl){
      return (mRight - mThumbCenterX) * mDelta / mTrackLength + mMin;
    }
    else{
      return (mThumbCenterX - mLeft) * mDelta / mTrackLength + mMin;
    }
  }
  
  /**
   * Correct offset when container on scrolling.
   */
  public void correctOffsetWhenContainerOnScrolling(){
    if(isHideBubble) return;
    
    locatePositionInWindow();
    
    if(mBubbleView.getParent() != null){
      if(isAlwaysShowBubble){
        mLayoutParams.y = (int) (mBubbleCenterRawSolidY + 0.5f);
        mWindowManager.updateViewLayout(mBubbleView, mLayoutParams);
      }
      else{
        postInvalidate();
      }
    }
  }
  
  /////// Api begins /////////////////////////////////////////////////////////////////////////////
  
  /**
   * Get min float.
   *
   * @return the float
   */
  public float getMin(){
    return mMin;
  }
  
  /**
   * Set min.
   *
   * @param min the min
   */
  public void setMin(float min){
    mMin = min;
    initConfigByPriority();
  }
  
  /**
   * Get max float.
   *
   * @return the float
   */
  public float getMax(){
    return mMax;
  }
  
  /**
   * Set max.
   *
   * @param max the max
   */
  public void setMax(float max){
    mMax = max;
    initConfigByPriority();
  }
  
  /**
   * Get progress int.
   *
   * @return the int
   */
  public int getProgress(){
    return Math.round(processProgress());
  }
  
  /**
   * Set progress.
   *
   * @param progress the progress
   */
  public void setProgress(float progress){
    mProgress = progress;
    
    if(mProgressListener != null){
      mProgressListener.onProgressChanged(this, getProgress(), getProgressFloat(), false);
      mProgressListener.getProgressOnFinally(this, getProgress(), getProgressFloat(), false);
    }
    if(!isHideBubble){
      mBubbleCenterRawX = calculateCenterRawXofBubbleView();
    }
    if(isAlwaysShowBubble){
      hideBubble();
      
      postDelayed(() -> {
        showBubble();
        triggerBubbleShowing = true;
      }, mAlwaysShowBubbleDelay);
    }
    if(isSeekBySection){
      triggerSeekBySection = false;
    }
    
    postInvalidate();
  }
  
  /**
   * Get progress float float.
   *
   * @return the float
   */
  public float getProgressFloat(){
    return formatFloat(processProgress());
  }
  
  /**
   * Process progress float.
   *
   * @return the float
   */
  private float processProgress(){
    final float progress = mProgress;
    
    if(isSeekBySection && triggerSeekBySection){
      float half = mSectionValue / 2;
      
      if(isTouchToSeek){
        if(progress == mMin || progress == mMax){
          return progress;
        }
        
        float secValue;
        for(int i = 0; i <= mSectionCount; i++){
          secValue = Float.parseFloat(i * mSectionValue + "%");
          if(secValue < progress && secValue + mSectionValue >= progress){
            if(secValue + half > progress){
              return secValue;
            }
            else{
              return secValue + mSectionValue;
            }
          }
        }
      }
      
      if(progress >= mPreSecValue){ // increasing
        if(progress >= mPreSecValue + half){
          mPreSecValue += mSectionValue;
          return mPreSecValue;
        }
        else{
          return mPreSecValue;
        }
      }
      else{ // reducing
        if(progress >= mPreSecValue - half){
          return mPreSecValue;
        }
        else{
          mPreSecValue -= mSectionValue;
          return mPreSecValue;
        }
      }
    }
    
    return progress;
  }
  
  /**
   * Get on progress changed listener on progress changed listener.
   *
   * @return the on progress changed listener
   */
  public OnProgressChangedListener getOnProgressChangedListener(){
    return mProgressListener;
  }
  
  /**
   * Set on progress changed listener.
   *
   * @param onProgressChangedListener the on progress changed listener
   */
  public void setOnProgressChangedListener(OnProgressChangedListener onProgressChangedListener){
    mProgressListener = onProgressChangedListener;
  }
  
  /**
   * Set track color.
   *
   * @param trackColor the track color
   */
  public void setTrackColor(@ColorInt int trackColor){
    if(mTrackColor != trackColor){
      mTrackColor = trackColor;
      invalidate();
    }
  }
  
  /**
   * Set second track color.
   *
   * @param secondTrackColor the second track color
   */
  public void setSecondTrackColor(@ColorInt int secondTrackColor){
    if(mSecondTrackColor != secondTrackColor){
      mSecondTrackColor = secondTrackColor;
      invalidate();
    }
  }
  
  /**
   * Set thumb color.
   *
   * @param thumbColor the thumb color
   */
  public void setThumbColor(@ColorInt int thumbColor){
    if(mThumbColor != thumbColor){
      mThumbColor = thumbColor;
      invalidate();
    }
  }
  
  /**
   * Set bubble color.
   *
   * @param bubbleColor the bubble color
   */
  public void setBubbleColor(@ColorInt int bubbleColor){
    if(mBubbleColor != bubbleColor){
      mBubbleColor = bubbleColor;
      if(mBubbleView != null){
        mBubbleView.invalidate();
      }
    }
  }
  
  /**
   * Set custom section text array.
   *
   * @param customSectionTextArray the custom section text array
   */
  public void setCustomSectionTextArray(@NonNull CustomSectionTextArray customSectionTextArray){
    mSectionTextArray = customSectionTextArray.onCustomize(mSectionCount, mSectionTextArray);
    for(int i = 0; i <= mSectionCount; i++){
      if(mSectionTextArray.get(i) == null){
        mSectionTextArray.put(i, "");
      }
    }
    
    isShowThumbText = false;
    requestLayout();
    invalidate();
  }
  
  /**
   * Config.
   *
   * @param builder the builder
   */
  void config(BubbleConfigBuilder builder){
    mMin = builder.min;
    mMax = builder.max;
    mProgress = builder.progress;
    isFloatType = builder.floatType;
    mTrackSize = builder.trackSize;
    mSecondTrackSize = builder.secondTrackSize;
    mThumbRadius = builder.thumbRadius;
    mThumbRadiusOnDragging = builder.thumbRadiusOnDragging;
    mTrackColor = builder.trackColor;
    mSecondTrackColor = builder.secondTrackColor;
    mThumbColor = builder.thumbColor;
    mSectionCount = builder.sectionCount;
    isShowSectionMark = builder.showSectionMark;
    isAutoAdjustSectionMark = builder.autoAdjustSectionMark;
    isShowSectionText = builder.showSectionText;
    mSectionTextSize = builder.sectionTextSize;
    mSectionTextColor = builder.sectionTextColor;
    mSectionTextPosition = builder.sectionTextPosition;
    mSectionTextInterval = builder.sectionTextInterval;
    isShowThumbText = builder.showThumbText;
    mThumbTextSize = builder.thumbTextSize;
    mThumbTextColor = builder.thumbTextColor;
    isShowProgressInFloat = builder.showProgressInFloat;
    mAnimDuration = builder.animDuration;
    isTouchToSeek = builder.touchToSeek;
    isSeekStepSection = builder.seekStepSection;
    isSeekBySection = builder.seekBySection;
    mBubbleColor = builder.bubbleColor;
    mBubbleTextSize = builder.bubbleTextSize;
    mBubbleTextColor = builder.bubbleTextColor;
    isAlwaysShowBubble = builder.alwaysShowBubble;
    mAlwaysShowBubbleDelay = builder.alwaysShowBubbleDelay;
    isHideBubble = builder.hideBubble;
    isRtl = builder.rtl;
    
    initConfigByPriority();
    calculateRadiusOfBubble();
    
    if(mProgressListener != null){
      mProgressListener.onProgressChanged(this, getProgress(), getProgressFloat(), false);
      mProgressListener.getProgressOnFinally(this, getProgress(), getProgressFloat(), false);
    }
    
    mConfigBuilder = null;
    
    requestLayout();
  }
  /////// Api ends ///////////////////////////////////////////////////////////////////////////////
  
  /**
   * Get config builder bubble config builder.
   *
   * @return the bubble config builder
   */
  public BubbleConfigBuilder getConfigBuilder(){
    if(mConfigBuilder == null){
      mConfigBuilder = new BubbleConfigBuilder(this);
    }
    
    mConfigBuilder.min = mMin;
    mConfigBuilder.max = mMax;
    mConfigBuilder.progress = mProgress;
    mConfigBuilder.floatType = isFloatType;
    mConfigBuilder.trackSize = mTrackSize;
    mConfigBuilder.secondTrackSize = mSecondTrackSize;
    mConfigBuilder.thumbRadius = mThumbRadius;
    mConfigBuilder.thumbRadiusOnDragging = mThumbRadiusOnDragging;
    mConfigBuilder.trackColor = mTrackColor;
    mConfigBuilder.secondTrackColor = mSecondTrackColor;
    mConfigBuilder.thumbColor = mThumbColor;
    mConfigBuilder.sectionCount = mSectionCount;
    mConfigBuilder.showSectionMark = isShowSectionMark;
    mConfigBuilder.autoAdjustSectionMark = isAutoAdjustSectionMark;
    mConfigBuilder.showSectionText = isShowSectionText;
    mConfigBuilder.sectionTextSize = mSectionTextSize;
    mConfigBuilder.sectionTextColor = mSectionTextColor;
    mConfigBuilder.sectionTextPosition = mSectionTextPosition;
    mConfigBuilder.sectionTextInterval = mSectionTextInterval;
    mConfigBuilder.showThumbText = isShowThumbText;
    mConfigBuilder.thumbTextSize = mThumbTextSize;
    mConfigBuilder.thumbTextColor = mThumbTextColor;
    mConfigBuilder.showProgressInFloat = isShowProgressInFloat;
    mConfigBuilder.animDuration = mAnimDuration;
    mConfigBuilder.touchToSeek = isTouchToSeek;
    mConfigBuilder.seekStepSection = isSeekStepSection;
    mConfigBuilder.seekBySection = isSeekBySection;
    mConfigBuilder.bubbleColor = mBubbleColor;
    mConfigBuilder.bubbleTextSize = mBubbleTextSize;
    mConfigBuilder.bubbleTextColor = mBubbleTextColor;
    mConfigBuilder.alwaysShowBubble = isAlwaysShowBubble;
    mConfigBuilder.alwaysShowBubbleDelay = mAlwaysShowBubbleDelay;
    mConfigBuilder.hideBubble = isHideBubble;
    mConfigBuilder.rtl = isRtl;
    
    return mConfigBuilder;
  }
  
  @Override
  protected Parcelable onSaveInstanceState(){
    Bundle bundle = new Bundle();
    bundle.putParcelable(AppConstants.SAVE_INSTANCE, super.onSaveInstanceState());
    bundle.putFloat(AppConstants.PROGRESS, mProgress);
    
    return bundle;
  }
  
  @Override
  protected void onRestoreInstanceState(Parcelable state){
    if(state instanceof Bundle){
      Bundle bundle = (Bundle) state;
      mProgress = bundle.getFloat(AppConstants.PROGRESS);
      super.onRestoreInstanceState(bundle.getParcelable(AppConstants.SAVE_INSTANCE));
      
      if(mBubbleView != null){
        mBubbleView.setProgressText(isShowProgressInFloat ? String.valueOf(getProgressFloat()) : String.format("%02d", getProgress()) + (isShowProgressInPercent ? "%" : ""));
      }
      setProgress(mProgress);
      
      return;
    }
    
    super.onRestoreInstanceState(state);
  }
  
  /**
   * The interface Text position.
   */
  @IntDef({NONE, SIDES, BOTTOM_SIDES, BELOW_SECTION_MARK})
  @Retention(RetentionPolicy.SOURCE)
  public @interface TextPosition{
    
    int SIDES = 0, BOTTOM_SIDES = 1, BELOW_SECTION_MARK = 2;
  }
  
  /**
   * The interface On progress changed listener.
   */
  public interface OnProgressChangedListener{
    
    /**
     * On progress changed.
     *
     * @param bubbleSeekBar the bubble seek bar
     * @param progress      the progress
     * @param progressFloat the progress float
     * @param fromUser      the from user
     */
    void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser);
    
    /**
     * Gets progress on action up.
     *
     * @param bubbleSeekBar the bubble seek bar
     * @param progress      the progress
     * @param progressFloat the progress float
     */
    void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, String progress, float progressFloat);
    
    /**
     * Gets progress on finally.
     *
     * @param bubbleSeekBar the bubble seek bar
     * @param progress      the progress
     * @param progressFloat the progress float
     * @param fromUser      the from user
     */
    void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser);
  }
  
  /**
   * Listener adapter
   * <br/>
   * usage like {@link AnimatorListenerAdapter}
   */
  
  /**
   * The interface Custom section text array.
   */
  public interface CustomSectionTextArray{
    
    /**
     * On customize sparse array.
     *
     * @param sectionCount the section count
     * @param array        the array
     * @return the sparse array
     */
    @NonNull
    SparseArray<String> onCustomize(int sectionCount, @NonNull SparseArray<String> array);
  }
  
  /**
   * The Bubble view.
   */
  private class BubbleView extends View{
    
    private Paint mBubblePaint;
    private Path mBubblePath;
    private RectF mBubbleRectF;
    private Rect mRect;
    private String mProgressText = "";
    
    /**
     * Instantiates a new Bubble view.
     *
     * @param context the context
     */
    BubbleView(Context context){
      this(context, null);
    }
    
    /**
     * Instantiates a new Bubble view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    BubbleView(Context context, AttributeSet attrs){
      this(context, attrs, 0);
    }
    
    /**
     * Instantiates a new Bubble view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    BubbleView(Context context, AttributeSet attrs, int defStyleAttr){
      super(context, attrs, defStyleAttr);
      
      mBubblePaint = new Paint();
      mBubblePaint.setAntiAlias(true);
      mBubblePaint.setTextAlign(Paint.Align.CENTER);
      
      mBubblePath = new Path();
      mBubbleRectF = new RectF();
      mRect = new Rect();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      
      setMeasuredDimension(3 * mBubbleRadius, 3 * mBubbleRadius);
      
      mBubbleRectF.set(getMeasuredWidth() / 2f - mBubbleRadius, 0, getMeasuredWidth() / 2f + mBubbleRadius, 2 * mBubbleRadius);
    }
    
    @Override
    protected void onDraw(Canvas canvas){
      super.onDraw(canvas);
      
      mBubblePath.reset();
      float x0 = getMeasuredWidth() / 2f;
      float y0 = getMeasuredHeight() - mBubbleRadius / 3f;
      mBubblePath.moveTo(x0, y0);
      float x1 = (float) (getMeasuredWidth() / 2f - Math.sqrt(3) / 2f * mBubbleRadius);
      float y1 = 3 / 2f * mBubbleRadius;
      mBubblePath.quadTo(x1 - dp2px(2), y1 - dp2px(2), x1, y1);
      mBubblePath.arcTo(mBubbleRectF, 150, 240);
      
      float x2 = (float) (getMeasuredWidth() / 2f + Math.sqrt(3) / 2f * mBubbleRadius);
      mBubblePath.quadTo(x2 + dp2px(2), y1 - dp2px(2), x0, y0);
      mBubblePath.close();
      
      mBubblePaint.setColor(mBubbleColor);
      canvas.drawPath(mBubblePath, mBubblePaint);
      
      mBubblePaint.setTextSize(mBubbleTextSize);
      mBubblePaint.setColor(mBubbleTextColor);
      mBubblePaint.getTextBounds(mProgressText, 0, mProgressText.length(), mRect);
      Paint.FontMetrics fm = mBubblePaint.getFontMetrics();
      float baseline = mBubbleRadius + (fm.descent - fm.ascent) / 2f - fm.descent;
      canvas.drawText(mProgressText, getMeasuredWidth() / 2f, baseline, mBubblePaint);
    }
    
    /**
     * Set progress text.
     *
     * @param progressText the progress text
     */
    void setProgressText(String progressText){
      if(progressText != null && !mProgressText.equals(progressText)){
        mProgressText = progressText;
        invalidate();
      }
    }
  }
}
