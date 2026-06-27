package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.showLog;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.itek.retail.R;
import com.itek.retail.ui.customviews.speedviewlib.PointerSpeedometer;

/**
 * The Search view.
 */
public class SearchDirView extends ConstraintLayout{
  
  TypedArray typedArray;
  ConstraintLayout clRoot;
  PointerSpeedometer pointerSpeedometer;
  ImageView imgSearchDir;
  Context context;
  boolean isAllowDirectionalSearch = false;
  boolean isShowArrow = false;
  
  /**
   * Instantiates a new Search view.
   *
   * @param context the context
   */
  public SearchDirView(@NonNull Context context){
    this(context, null);
  }
  
  /**
   * Instantiates a new Search view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public SearchDirView(@NonNull Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Search view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public SearchDirView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }
  
  /**
   * Instantiates a new Search view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   * @param defStyleRes  the def style res
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public SearchDirView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs);
  }
  
  /**
   * Init.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  void init(Context context, @Nullable AttributeSet attrs){
    SearchDirView.this.context = context;
    final View root = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_search_dir, this, true);
    clRoot = root.findViewById(R.id.clSearchDirView);
    pointerSpeedometer = root.findViewById(R.id.psmSearch);
    imgSearchDir = root.findViewById(R.id.imgSearchDir);
    
    if(attrs != null)
      typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProductDetailsView, 0, 0);
    if(typedArray != null){
      //code here
    }
  }
  
  @Override
  protected void onFinishInflate(){
    super.onFinishInflate();
    setLayout();
  }
  
  /**
   * Reset to default.
   */
  public void resetToDefault(){
    pointerSpeedometer.resetToDefault();
  }
  
  protected void updateSearchDir(int direction, Double angle, Double angleX, Double angleY, String message, int percent){
    setPercentageValue(percent);
    //Rotate ImageView based on direction and angle
    if(isAllowDirectionalSearch && imgSearchDir != null){
      //showLog("DIRECTION.,angle", +direction + "," + angle + "\n" + message);
      float rotation = imgSearchDir.getRotation();
      if(percent >= 33 && !isShowArrow) isShowArrow = true;
      if(percent <= 0 && isShowArrow) isShowArrow = false;
      imgSearchDir.setVisibility(isShowArrow ? View.VISIBLE : View.GONE);
      
      float rotAngle = angle.floatValue();
      if(rotAngle > 180){
        rotAngle = (360 - angle.floatValue()) * -1;
      }
      //showLog("DIRECTION..", "PreAngle:" + rotation + "_CurAngle:" + rotAngle);
      
      imgSearchDir.animate().rotation(rotAngle).start();
      
      imgSearchDir.setImageResource(percent >= 30 ? R.drawable.top_green1 : R.drawable.top_red1);
      
    }
  }
  
  public void setPercentageValue(final int percent){
    pointerSpeedometer.setPercentageValue(percent);
  }
  
  /**
   * Set layout.
   */
  private void setLayout(){
    resetToDefault();
  }
  
}
