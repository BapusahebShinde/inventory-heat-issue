package com.itek.retail.ui.customviews.swipeButton;

import static com.itek.retail.ui.customviews.swipeButton.Constants.BTN_INIT_RADIUS;
import static com.itek.retail.ui.customviews.swipeButton.Constants.BTN_MORPHED_RADIUS;
import static com.itek.retail.ui.customviews.swipeButton.Constants.DEFAULT_SWIPE_DISTANCE;
import static com.itek.retail.ui.customviews.swipeButton.Constants.DEFAULT_TEXT_SIZE;
import static com.itek.retail.ui.customviews.swipeButton.Constants.MORPH_ANIM_DURATION;
import static com.itek.retail.ui.customviews.swipeButton.UiUtils.animateFadeHide;
import static com.itek.retail.ui.customviews.swipeButton.UiUtils.animateFadeShow;
import static com.itek.retail.ui.customviews.swipeButton.UiUtils.dpToPx;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;

/**
 * The Pro swipe button.
 */
public class ProSwipeButton extends RelativeLayout{
  
  public MutableLiveData<Boolean> isSuccessfulSwipe = new MutableLiveData<>(false);
  private Context context;
  private View view;
  private GradientDrawable gradientDrawable;
  private RelativeLayout contentContainer;
  private TextView contentTv;
  private ImageView arrow1;
  private LinearLayout arrowHintContainer;
  private ProgressBar progressBar;
  
  //// TODO: 26/10/17 Add touch blocking
  /*
      User configurable settings
   */
  private CharSequence btnText = "BUTTON";
  @ColorInt
  private int textColorInt;
  @ColorInt
  private int bgColorInt;
  @ColorInt
  private int arrowColorInt;
  private float btnRadius = BTN_INIT_RADIUS;
  @Dimension
  private float textSize = DEFAULT_TEXT_SIZE;
  @Nullable
  private OnSwipeListener swipeListener = null;
  private float swipeDistance = DEFAULT_SWIPE_DISTANCE;
  
  /**
   * Instantiates a new Pro swipe button.
   *
   * @param context the context
   */
  public ProSwipeButton(Context context){
    super(context);
    this.context = context;
    init();
  }
  
  /**
   * Instantiates a new Pro swipe button.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public ProSwipeButton(Context context, AttributeSet attrs){
    super(context, attrs);
    this.context = context;
    setAttrs(context, attrs);
    init();
  }
  
  /**
   * Instantiates a new Pro swipe button.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public ProSwipeButton(Context context, AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    this.context = context;
    setAttrs(context, attrs);
    init();
  }
  
  /**
   * Set attrs.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  private void setAttrs(Context context, AttributeSet attrs){
    TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ProSwipeButton, 0, 0);
    try{
      btnText = AppCommonMethods.chkNull(a.getString(R.styleable.ProSwipeButton_btn_text), btnText.toString());
      textColorInt = a.getColor(R.styleable.ProSwipeButton_text_color, ContextCompat.getColor(context, android.R.color.white));
      TypedValue tv = new TypedValue();
      bgColorInt = a.getColor(R.styleable.ProSwipeButton_bg_color, (context.getTheme().resolveAttribute(R.attr.colorPrimaryDark,tv,true)? tv.data:ContextCompat.getColor(context, R.color.proswipebtn_red)));
      arrowColorInt = a.getColor(R.styleable.ProSwipeButton_arrow_color, ContextCompat.getColor(context, R.color.proswipebtn_translucent_white));
      btnRadius = a.getFloat(R.styleable.ProSwipeButton_btn_radius, BTN_INIT_RADIUS);
      textSize = a.getDimensionPixelSize(R.styleable.ProSwipeButton_text_size, (int) DEFAULT_TEXT_SIZE);
    }
    finally{
      a.recycle();
    }
  }
  
  /**
   * Init.
   */
  public void init(){
    LayoutInflater inflater = LayoutInflater.from(context);
    view = inflater.inflate(R.layout.view_proswipebtn, this, true);
  }
  
  @Override
  protected void onFinishInflate(){
    super.onFinishInflate();
    
    contentContainer = view.findViewById(R.id.relativeLayout_swipeBtn_contentContainer);
    arrowHintContainer = view.findViewById(R.id.linearLayout_swipeBtn_hintContainer);
    contentTv = view.findViewById(R.id.tv_btnText);
    arrow1 = view.findViewById(R.id.iv_arrow1);
    
    tintArrowHint();
    contentTv.setText(btnText);
    contentTv.setTextColor(textColorInt);
    contentTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    gradientDrawable = new GradientDrawable();
    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    gradientDrawable.setCornerRadius(btnRadius);
    setBackgroundColor(bgColorInt);
    updateBackground();
    setupTouchListener();
  }
  
  /**
   * Setup touch listener.
   */
  private void setupTouchListener(){
    isSuccessfulSwipe.postValue(false);
    setOnTouchListener(new OnTouchListener(){
      @Override
      public boolean onTouch(View v, MotionEvent event){
        switch(event.getAction()){
          case MotionEvent.ACTION_DOWN:
            return true;
          case MotionEvent.ACTION_MOVE:
            // Movement logic here
            if(event.getX() > arrowHintContainer.getWidth() / 2 && event.getX() + arrowHintContainer.getWidth() / 2 < getWidth() && (event.getX() < arrowHintContainer.getX() + arrowHintContainer.getWidth() || arrowHintContainer.getX() != 0)){
              // snaps the hint to user touch, only if the touch is within hint width or if it has already been displaced
              arrowHintContainer.setX(event.getX() - arrowHintContainer.getWidth() / 2);
            }
            
            if(arrowHintContainer.getX() + arrowHintContainer.getWidth() > getWidth() && arrowHintContainer.getX() + arrowHintContainer.getWidth() / 2 < getWidth()){
              // allows the hint to go up to a max of btn container width
              arrowHintContainer.setX(getWidth() - arrowHintContainer.getWidth());
            }
            
            if(event.getX() < arrowHintContainer.getWidth() / 2 && arrowHintContainer.getX() > 0){
              // allows the hint to go up to a min of btn container starting
              arrowHintContainer.setX(0);
            }
            
            return true;
          case MotionEvent.ACTION_UP:
            //Release logic here
            if(arrowHintContainer.getX() + arrowHintContainer.getWidth() > getWidth() / 1.5 * swipeDistance){
              // swipe completed, fly the hint away!
              
              isSuccessfulSwipe.postValue(true);
            }
            else{
              isSuccessfulSwipe.postValue(false);
              // swipe not completed, pull back the hint
              animateHintBack();
            }
            return true;
        }
        
        return false;
      }
    });
  }
  
  /**
   * Perform successful swipe.
   */
  public void performSuccessfulSwipe(){
    if(swipeListener != null) swipeListener.onSwipeConfirm();
    morphToCircle();
  }
  
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh){
    super.onSizeChanged(w, h, oldw, oldh);
    startFwdAnim();//moving arrow anim
  }
  
  /**
   * Animate hint back.
   */
  private void animateHintBack(){
    final ValueAnimator positionAnimator = ValueAnimator.ofFloat(arrowHintContainer.getX(), 0);
    positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
      @Override
      public void onAnimationUpdate(ValueAnimator animation){
        float x = (Float) positionAnimator.getAnimatedValue();
        arrowHintContainer.setX(x);
      }
    });
    positionAnimator.setDuration(200);
    positionAnimator.start();
  }
  
  /**
   * Start fwd anim.
   */
  private void startFwdAnim(){
    if(isEnabled()){
      TranslateAnimation animation = new TranslateAnimation(0, getMeasuredWidth(), 0, 0);
      animation.setInterpolator(new AccelerateDecelerateInterpolator());
      animation.setDuration(500);
      animation.setAnimationListener(new Animation.AnimationListener(){
        @Override
        public void onAnimationStart(Animation animation){
          /*Empty Method (Default Overridden)*/
        }
        
        @Override
        public void onAnimationEnd(Animation animation){
          startHintInitAnim();
        }
        
        @Override
        public void onAnimationRepeat(Animation animation){
          /*Empty Method (Default Overridden)*/
        }
      });
      arrowHintContainer.startAnimation(animation);
    }
  }
  
  /**
   * Start hint init anim.
   */
  private void startHintInitAnim(){
    TranslateAnimation anim = new TranslateAnimation(-arrowHintContainer.getWidth(), 0, 0, 0);
    anim.setDuration(500);
    arrowHintContainer.startAnimation(anim);
  }
  
  /**
   * Perform on swipe.
   */
  public void performOnSwipe(){
    performSuccessfulSwipe();
  }
  
  /**
   * Morph to circle.
   */
  public void morphToCircle(){
    animateFadeHide(context, arrowHintContainer);
    setOnTouchListener(null);
    ObjectAnimator cornerAnimation = ObjectAnimator.ofFloat(gradientDrawable, "cornerRadius", BTN_INIT_RADIUS, BTN_MORPHED_RADIUS);
    
    animateFadeHide(context, contentTv);
    ValueAnimator widthAnimation;
    widthAnimation = ValueAnimator.ofInt(getWidth(), dpToPx(50));
    widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator){
        int val = (Integer) valueAnimator.getAnimatedValue();
        ViewGroup.LayoutParams layoutParams = contentContainer.getLayoutParams();
        layoutParams.width = val;
        contentContainer.setLayoutParams(layoutParams);
      }
    });
    ValueAnimator heightAnimation = ValueAnimator.ofInt(getHeight(), dpToPx(50));
    heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator){
        int val = (Integer) valueAnimator.getAnimatedValue();
        ViewGroup.LayoutParams layoutParams = contentContainer.getLayoutParams();
        layoutParams.height = val;
        contentContainer.setLayoutParams(layoutParams);
      }
    });
    
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.setDuration(MORPH_ANIM_DURATION);
    animatorSet.playTogether(cornerAnimation, widthAnimation, heightAnimation);
    animatorSet.start();
    
    showProgressBar();
  }
  
  /**
   * Morph to rect.
   */
  private void morphToRect(){
    setupTouchListener();
    ObjectAnimator cornerAnimation = ObjectAnimator.ofFloat(gradientDrawable, "cornerRadius", BTN_MORPHED_RADIUS, BTN_INIT_RADIUS);
    
    ValueAnimator widthAnimation;
    widthAnimation = ValueAnimator.ofInt(dpToPx(50), getWidth());
    widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator){
        int val = (Integer) valueAnimator.getAnimatedValue();
        ViewGroup.LayoutParams layoutParams = contentContainer.getLayoutParams();
        layoutParams.width = val;
        contentContainer.setLayoutParams(layoutParams);
      }
    });
    ValueAnimator heightAnimation = ValueAnimator.ofInt(dpToPx(50), getWidth());
    heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator){
        int val = (Integer) valueAnimator.getAnimatedValue();
        ViewGroup.LayoutParams layoutParams = contentContainer.getLayoutParams();
        layoutParams.height = val;
        contentContainer.setLayoutParams(layoutParams);
      }
    });
    
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.setDuration(MORPH_ANIM_DURATION);
    animatorSet.playTogether(cornerAnimation, widthAnimation, heightAnimation);
    animatorSet.start();
  }
  
  /**
   * Update background.
   */
  public void updateBackground(){
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
      contentContainer.setBackground(gradientDrawable);
    }
    else{
      // noinspection deprecation
      contentContainer.setBackgroundDrawable(gradientDrawable);
    }
  }
  
  @Override
  public void setEnabled(boolean enabled){
    super.setEnabled(enabled);
    if(!enabled){
      gradientDrawable.setColor(ContextCompat.getColor(context, R.color.proswipebtn_disabled_grey));
      updateBackground();
      this.setAlpha(0.5f);
    }
    else{
      setBackgroundColor(getBackgroundColor());
      this.setAlpha(1f);
    }
  }
  
  /**
   * Show progress bar.
   */
  private void showProgressBar(){
    progressBar = new ProgressBar(context);
    progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context, android.R.color.white), PorterDuff.Mode.SRC_IN);
    animateFadeHide(context, contentTv);
    contentContainer.addView(progressBar);
  }
  
  /**
   * Show result icon.
   *
   * @param isSuccess   the is success
   * @param shouldReset the should reset
   */
  public void showResultIcon(boolean isSuccess, boolean shouldReset){
    animateFadeHide(context, progressBar);
    isSuccessfulSwipe.postValue(false);
    final AppCompatImageView failureIcon = new AppCompatImageView(context);
    LayoutParams icLayoutParams = new LayoutParams(dpToPx(50), dpToPx(50));
    failureIcon.setLayoutParams(icLayoutParams);
    failureIcon.setVisibility(GONE);
    int icon;
    if(isSuccess) icon = R.drawable.ic_success;
    else icon = R.drawable.ic_error;
    failureIcon.setImageResource(icon);
    contentContainer.addView(failureIcon);
    animateFadeShow(context, failureIcon);
    
    if(shouldReset){
      // expand the btn again
      new Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
          animateFadeHide(context, failureIcon);
          morphToRect();
          arrowHintContainer.setX(0);
          animateFadeShow(context, arrowHintContainer);
          animateFadeShow(context, contentTv);
        }
      }, 1000);
    }
  }
  
  /**
   * Show result icon.
   *
   * @param isSuccess the is success
   */
  public void showResultIcon(boolean isSuccess){
    showResultIcon(isSuccess, !isSuccess);
  }
  
  /**
   * Tint arrow hint.
   */
  private void tintArrowHint(){
    arrow1.setColorFilter(arrowColorInt, PorterDuff.Mode.MULTIPLY);
    
  }
  
  /**
   * Get text char sequence.
   *
   * @return the char sequence
   */
  public CharSequence getText(){
    return this.btnText;
  }
  
  /**
   * Set text.
   *
   * @param text the text
   */
  public void setText(CharSequence text){
    this.btnText = text;
    contentTv.setText(text);
  }
  
  /**
   * Get text color int.
   *
   * @return the int
   */
  @ColorInt
  public int getTextColor(){
    return this.textColorInt;
  }
  
  /**
   * Set text color.
   *
   * @param textColor the text color
   */
  public void setTextColor(@ColorInt int textColor){
    this.textColorInt = textColor;
    contentTv.setTextColor(textColor);
  }
  
  /**
   * Get background color int.
   *
   * @return the int
   */
  @ColorInt
  public int getBackgroundColor(){
    return this.bgColorInt;
  }
  
  public void setBackgroundColor(@ColorInt int bgColor){
    this.bgColorInt = bgColor;
    gradientDrawable.setColor(bgColor);
    updateBackground();
  }
  
  /**
   * Get corner radius float.
   *
   * @return the float
   */
  public float getCornerRadius(){
    return this.btnRadius;
  }
  
  /**
   * Set corner radius.
   *
   * @param cornerRadius the corner radius
   */
  public void setCornerRadius(float cornerRadius){
    this.btnRadius = cornerRadius;
  }
  
  /**
   * Get arrow color res int.
   *
   * @return the int
   */
  public int getArrowColorRes(){
    return this.arrowColorInt;
  }
  
  /**
   * Set arrow color.
   *
   * @param arrowColor the arrow color
   */
  public void setArrowColor(int arrowColor){
    this.arrowColorInt = arrowColor;
    tintArrowHint();
  }
  
  /**
   * Get text size float.
   *
   * @return the float
   */
  @Dimension
  public float getTextSize(){
    return this.textSize;
  }
  
  /**
   * Set text size.
   *
   * @param textSize the text size
   */
  public void setTextSize(@Dimension float textSize){
    this.textSize = textSize;
    contentTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
  }
  
  /**
   * Get swipe distance float.
   *
   * @return the float
   */
  @Dimension
  public float getSwipeDistance(){
    return this.swipeDistance;
  }
  
  /**
   * Set swipe distance.
   *
   * @param swipeDistance the swipe distance
   */
  public void setSwipeDistance(@Dimension float swipeDistance){
    if(swipeDistance > 1.0f){
      swipeDistance = 1.0f;
    }
    if(swipeDistance < 0.0f){
      swipeDistance = 0.0f;
    }
    this.swipeDistance = swipeDistance;
  }
  
  /**
   * Set on swipe listener.
   *
   * @param customSwipeListener the custom swipe listener
   */
  public void setOnSwipeListener(@Nullable OnSwipeListener customSwipeListener){
    this.swipeListener = customSwipeListener;
  }
  
  /**
   * The interface On swipe listener.
   */
  public interface OnSwipeListener{
    
    /**
     * On swipe confirm.
     */
    void onSwipeConfirm();
  }
  
}
