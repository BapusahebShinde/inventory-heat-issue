package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.isUploadSlider;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itek.retail.R;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.ui.customviews.swipeButton.ProSwipeButtonVar;

public class BtnSwipeUploadView extends ConstraintLayout{
  
  Context context;
  TypedArray typedArray;
  ConstraintLayout clRoot;
  FloatingActionButton btnUpload;
  ProSwipeButtonVar btnSwipeUpload;
  OnClickListener clickListener;
  
  public BtnSwipeUploadView(@NonNull Context context){
    this(context, null);
  }
  
  public BtnSwipeUploadView(@NonNull Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  public BtnSwipeUploadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }
  
  public BtnSwipeUploadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes){
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs);
  }
  
  void init(Context context, @Nullable AttributeSet attrs){
    BtnSwipeUploadView.this.context = context;
    final View root = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_btn_swipe_upload, this, true);
    clRoot = root.findViewById(R.id.cl_btn_swipe_upload);
    btnUpload = root.findViewById(R.id.fabUpload);
    btnSwipeUpload = root.findViewById(R.id.btnSwipeUpload);
    //if (attrs != null)
      //typedArray = context.obtainStyledAttributes(attrs, R.styleable.BtnSwipeUploadView, 0, 0);
    if (typedArray != null) {
    
    }
    
    if(btnSwipeUpload!=null && context instanceof CommonActivity){
      final CommonActivity commonActivity = (CommonActivity) context;
      DisplayMetrics displayMetrics = new DisplayMetrics();
      commonActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
      btnSwipeUpload.getLayoutParams().width=(int) (displayMetrics.widthPixels/(commonActivity.isLandscape?20:10));
      btnSwipeUpload.getLayoutParams().height=(int) (displayMetrics.heightPixels/(commonActivity.isLandscape?4:5));
    }
    
    //ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) btnUpload.getLayoutParams();
    //layoutParams.height=100;
    //btnSwipeUpload.setLayoutParams(layoutParams);
    
    btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(btnSwipeUpload != null && btnSwipeUpload.getVisibility() == View.VISIBLE) return;
        onBtnUploadSwiped();
      }
    });
    
    btnSwipeUpload.isSuccessfulSwipe.observe((LifecycleOwner) context, new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          btnSwipeUpload.reset();
          onBtnUploadSwiped();
        }
      }
    });
    //updateConstraint();
    
  }
  
  @Override
  public void setOnClickListener(@Nullable OnClickListener clickListener){
    //super.setOnClickListener(l);
    this.clickListener = clickListener;
  }
  
  private void updateConstraint(){
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(clRoot);
    constraintSet.clear(btnUpload.getId(), isUploadSlider ? ConstraintSet.BOTTOM : ConstraintSet.TOP);
    constraintSet.connect(btnUpload.getId(), isUploadSlider ? ConstraintSet.TOP : ConstraintSet.BOTTOM, isUploadSlider ? btnSwipeUpload.getId() : ConstraintSet.PARENT_ID, isUploadSlider ? ConstraintSet.TOP : ConstraintSet.BOTTOM);
    constraintSet.applyTo(clRoot);
  }
  
  public void onBtnUploadSwiped(){
    if(clickListener != null) clickListener.onClick(this);
  }
}
