package com.itek.retail.ui.customviews;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itek.retail.R;
import com.itek.retail.databinding.ViewBtnStartStopBinding;

/**
 * The Input view.
 */
public class BtnStartStopView extends LinearLayout{
  
  Context context;
  ViewBtnStartStopBinding binding;
  boolean isOperationOn = false;
  boolean isEnabled = true;
  boolean isLandscape = false;
  
  /**
   * Instantiates a new Input view.
   *
   * @param context the context
   */
  public BtnStartStopView(Context context){ this(context, null); }
  
  /**
   * Instantiates a new Input view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public BtnStartStopView(Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Input view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public BtnStartStopView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }
  
  /**
   * Init.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  void init(Context context, @Nullable AttributeSet attrs){
    BtnStartStopView.this.context = context;
    binding = ViewBtnStartStopBinding.inflate(LayoutInflater.from(context), this, true);
    isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((AppCompatActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    final int wid = (int) ((isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) * 0.15);//0.085); //Increase Btn Size
    final LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(wid, wid);
    llParams.gravity = Gravity.CENTER;
    if(binding != null) binding.imgStartStop.setLayoutParams(llParams);
    TypedArray typedArray = attrs != null?context.obtainStyledAttributes(attrs, R.styleable.BtnStartStopView, 0, 0):null;
    if(typedArray!=null){
      boolean isHideBottomPadding = typedArray.getBoolean(R.styleable.BtnStartStopView_isHideBottomPadding,false);
      if(isHideBottomPadding)
        binding.getRoot().setPadding(binding.getRoot().getPaddingLeft(),binding.getRoot().getPaddingTop(),binding.getRoot().getPaddingRight(),0);
    }
  }
  
  @Override
  protected void onFinishInflate(){
    super.onFinishInflate();
  }
  
  @Override
  public void setOnClickListener(final OnClickListener btnClick){
    if(binding != null) binding.getRoot().setOnClickListener(btnClick);
  }
  
  @Override
  public boolean performClick(){
    return binding != null && binding.getRoot().performClick();
  }
  
  @Override
  public void setEnabled(final boolean isEnabled){
    if(binding != null){
      this.isEnabled = isEnabled;
      binding.getRoot().setEnabled(isEnabled);
      binding.lblStartStop.setEnabled(isEnabled);
      binding.imgStartStop.setImageResource(!isEnabled ? R.drawable.ic_start_disable : isOperationOn ? R.drawable.ic_stop : R.drawable.ic_start);
      binding.imgStartStop.setEnabled(isEnabled);
    }
  }
  
  public void setStart(){ toggle(false); }
  
  public void setStop(){ toggle(true); }
  
  public void toggle(final boolean isOperationOn){
    if(binding != null){
      this.isOperationOn = isOperationOn;
      binding.lblStartStop.setText(isEnabled && isOperationOn ? R.string.lbl_stop : R.string.lbl_start);
      binding.imgStartStop.setImageResource(!isEnabled ? R.drawable.ic_start_disable : isOperationOn ? R.drawable.ic_stop : R.drawable.ic_start);
    }
  }
}
