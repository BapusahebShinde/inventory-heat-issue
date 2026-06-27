package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.ui.customviews.customseekbar.BubbleSeekBar;
import com.itek.retail.ui.home.MainViewModel;

/**
 * The Power view.
 */
public class PowerView extends LinearLayout{
  
  Context context;
  TypedArray typedArray;
  LinearLayout llRoot;
  
  TextView txtMinPower;
  TextView txtMaxPower;
  BubbleSeekBar seekbarPower;
  
  float min;
  float max;
  //float pow;
  
  /**
   * Instantiates a new Power view.
   *
   * @param context the context
   */
  public PowerView(Context context){
    super(context);
  }
  
  /**
   * Instantiates a new Power view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public PowerView(Context context, @Nullable AttributeSet attrs){
    super(context, attrs);
    init(context, attrs);
  }
  
  /**
   * Instantiates a new Power view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public PowerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }
  
  /**
   * Instantiates a new Power view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   * @param defStyleRes  the def style res
   */
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public PowerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
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
    PowerView.this.context = context;
    llRoot = (LinearLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_view_power, this, true);
    txtMinPower = llRoot.findViewById(R.id.lbl_inv_config_power_min);
    txtMaxPower = llRoot.findViewById(R.id.lbl_inv_config_power_max);
    seekbarPower = llRoot.findViewById(R.id.seek_inventory_start_power);
    typedArray = context.obtainStyledAttributes(attrs, R.styleable.PowerView, 0, 0);
    if(typedArray != null){
      min = typedArray.getFloat(R.styleable.PowerView_pv_min, 5.0f);
      max = typedArray.getFloat(R.styleable.PowerView_pv_max, 30.0f);
      seekbarPower.setMin(min);
      seekbarPower.setMax(max);
      setMinMaxTxt();
    }
    
    llRoot.setOrientation(VERTICAL);
  }
  
  @Override
  public void setEnabled(boolean isEnabled){ this.seekbarPower.setEnabled(isEnabled); }
  
  @Override
  protected void onFinishInflate(){
    super.onFinishInflate();
  }
  
  /**
   * Get progress int.
   *
   * @return the int
   */
  public int getProgress(){
    return this.seekbarPower.getProgress();
  }
  
  /**
   * Set progress.
   *
   * @param power the power
   */
  public void setProgress(int power){
    this.seekbarPower.setProgress(power);
  }
  
  /**
   * Set progress.
   *
   * @param power the power
   */
  public void setProgress(float power){
    this.seekbarPower.setProgress(power);
  }
  
  /**
   * Get max float.
   *
   * @return the float
   */
  public float getMax(){ return this.seekbarPower.getMax(); }
  
  /**
   * Set min max txt.
   */
  public void setMinMaxTxt(){
    int mintxt = (int) seekbarPower.getMin();
    int maxtxt = (int) seekbarPower.getMax();
    this.txtMinPower.setText(String.format("%02d", mintxt));
    this.txtMaxPower.setText(String.format("%02d", maxtxt));
  }
  
  /**
   * Set on progress changed listener.
   *
   * @param onProgressChangedListener the on progress changed listener
   */
  public void setOnProgressChangedListener(BubbleSeekBar.OnProgressChangedListener onProgressChangedListener){
    this.seekbarPower.setOnProgressChangedListener(onProgressChangedListener);
  }
  
  public void setupProgress(final MainViewModel mainViewModel){
    if(mainViewModel != null){
      this.seekbarPower.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener(){
        @Override
        public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser){
          /*Empty Method (Default Overridden)*/
        }
        
        @Override
        public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, String progressStr, float progressFloat){
          showLog("BubbleSeekBar", "ActionUp:" + progressStr);
          final int progress = AppCommonMethods.parseInt(progressStr);
          showLog("readerPower(Set)",""+progress);
          if(chkNull(mainViewModel.getReaderPower().getValue(), progress) != progress)
            mainViewModel.setReaderPower(progress);
        }
        
        @Override
        public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser){
          /*Empty Method (Default Overridden)*/
        }
      });
      updateReaderPower(mainViewModel);
    }
  }
  
  public void updateReaderPower(final MainViewModel mainViewModel){ updateReaderPower(mainViewModel, null); }
  
  public void updateReaderPower(final MainViewModel mainViewModel, final Integer power){
    if(mainViewModel != null){
      final int readerPower = chkNull(power, chkNull(mainViewModel.getReaderPower().getValue(), getProgress()));
      showLog("readerPower",""+readerPower);
      if(getProgress() != readerPower){
        if(readerPower > getMax()) mainViewModel.setReaderPower((int) getMax());
        else setProgress(readerPower);
      }
    }
  }
}