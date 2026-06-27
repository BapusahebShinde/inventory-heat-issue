package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Search view.
 */
public class SearchView extends ConstraintLayout{
  
  private final int gauge_start = 200;
  private final float const_degree = -225;
  //internal flag
  private final boolean isMultiColor = true;
  Context context;
  TypedArray typedArray;
  ConstraintLayout clRoot;
  CustomGauge arc;
  GaugeView needle;
  TextView txtPercent;
  CheckBox chkSingleTag;
  private boolean isEnableCheck = false;
  private int oldPercentage = 0;
  private float degree = const_degree;
  private int counter = 0;
  private CountDownTimer searchCTD = null;
  private Timer searchT = null;
  //internal Flags
  private boolean isCDT = false;
  private boolean isFixedAvgs = false;
  private boolean isPercentVals = false;
  
  /**
   * Instantiates a new Search view.
   *
   * @param context the context
   */
  public SearchView(@NonNull Context context){
    this(context, null);
  }
  
  /**
   * Instantiates a new Search view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public SearchView(@NonNull Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Search view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public SearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr){
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
  public SearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
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
    SearchView.this.context = context;
    final View root = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_search_needle, this, true);
    clRoot = root.findViewById(R.id.clSearchView);
    arc = root.findViewById(R.id.customGuage);
    needle = root.findViewById(R.id.needleView);
    txtPercent = root.findViewById(R.id.txtPercentage);
    chkSingleTag = root.findViewById(R.id.chkSingleTag);
    //temp code
    chkSingleTag.setVisibility(isDebugApp ? View.VISIBLE : GONE);
    
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
    if(searchCTD != null){
      searchCTD.cancel();
      searchCTD = null;
    }
    if(searchT != null){
      searchT.cancel();
      searchT = null;
    }
    oldPercentage = 0;
    arc.setValue(gauge_start);
    if(isMultiColor) arc.setPointColor(ContextCompat.getColor(context, R.color.red));
    txtPercent.setText(" 0 %");
    degree = const_degree;
    needle.setRotateDegree(degree);
    if(isMultiColor) needle.setNeedleColor(ContextCompat.getColor(context, R.color.red));
  }
  
  public void setPercentageValue(final int percentageValue){
    setPercentageValue(percentageValue, "");
  }
  
  public void setPercentageValue(final int percentageValue, final String rssi){
    showLog("sv_on_setPercentVal", "setPercentageValue");
    showLog("sv_percentageOld", "" + oldPercentage);
    showLog("sv_percentageValue", "" + percentageValue);
    int diff = Math.abs(oldPercentage - percentageValue);
    int unit = 2;
    if(diff > 20 / unit){
      //based on fixed avgs
      final int avg = (oldPercentage + percentageValue) / 2;
      final int avgM1 = diff > 40 / unit ? (oldPercentage + avg) / 2 : 0;
      final int avg1 = diff > 40 / unit ? (avg + percentageValue) / 2 : 0;
      final int avgM0_5 = diff > 80 / unit ? (avgM1 + avg) / 2 : 0;
      final int avgM1_5 = diff > 80 / unit ? (oldPercentage + avgM1) / 2 : 0;
      final int avg0_5 = diff > 80 / unit ? (avg1 + avg) / 2 : 0;
      final int avg1_5 = diff > 80 / unit ? (percentageValue + avg1) / 2 : 0;
      showLog("sv_diff", "" + diff);
      
      List<Integer> avgs = new ArrayList<>(0);
      if(avgM1_5 > 0) avgs.add(avgM1_5);
      if(avgM1 > 0) avgs.add(avgM1);
      if(avgM0_5 > 0) avgs.add(avgM0_5);
      if(avg > 0) avgs.add(avg);
      if(avg0_5 > 0) avgs.add(avg0_5);
      if(avg1 > 0) avgs.add(avg1);
      if(avg1_5 > 0) avgs.add(avg1_5);
      showLog("sv_counter_avgs", "" + avgs.toString() + " (" + avgs.size() + ")");
      counter = 0;
      if(searchCTD != null && isCDT){
        searchCTD.cancel();
        searchCTD = null;
        setPercentageValue(percentageValue, rssi, false);
      }
      else if(searchT != null){
        searchT.cancel();
        searchT = null;
        setPercentageValue(percentageValue, rssi, false);
      }
      
      if(searchCTD == null && isCDT){
        searchCTD = new CountDownTimer(480, 480 / avgs.size()){
          @Override
          public void onTick(long l){
            showLog("sv_c_tick", "" + l);
            showLog("sv_csize", "" + avgs.size());
            showLog("sv_counter", "" + counter);
            if(counter < avgs.size()){
              showLog("sv_val" + counter, "" + avgs.get(counter));
              setPercentageValue(avgs.get(counter++), rssi, true);
            }
          }
          
          @Override
          public void onFinish(){
            showLog("sv_c_finish", "finish");
            showLog("sv_counter1", "" + counter);
            counter = 0;
            searchCTD = null;
            setPercentageValue(percentageValue, rssi, false);
          }
        };
        searchCTD.start();
      }
      
      //code with fixed timer
      else if(searchT == null){
        searchT = new Timer();
        searchT.schedule(new TimerTask(){
          @Override
          public void run(){
            if(counter < avgs.size()){
              showLog("sv_counter" + counter, "" + avgs.get(counter));
              setPercentageValue(avgs.get(counter++), rssi, true);
            }
            else{
              showLog("sv_on_finish", "onFinish");
              setPercentageValue(percentageValue, rssi, false);
              if(searchT != null) searchT.cancel();
              searchT = null;
            }
          }
        }, 0, (1000 / 2) / (avgs.size() + 1));
      }
    }
    else{
      //counter = 0;
      setPercentageValue(percentageValue, rssi, false);
    }
  }
  
  /**
   * Set percentage value.
   *
   * @param percentageValue the old percentage
   */
  public void setPercentageValue(final int percentageValue, final String rssi, boolean isAnimated){
    if(context != null && context instanceof Activity){
      ((Activity) context).runOnUiThread(() -> {
        int result = percentageValue;
        AppCommonMethods.searchBeep((Activity) context, result);
        if(!isAnimated) showLog("sv_result", "" + result);
        final int colorId = !isMultiColor ? R.color.red : result >= AppConstants.SEARCH_PERCENT_VALUE_90 ? R.color.green : result >= AppConstants.SEARCH_PERCENT_VALUE_66 ? R.color.light_green : result >= AppConstants.SEARCH_PERCENT_VALUE_33 ? R.color.orange : R.color.red;
        if(isMultiColor) arc.setPointColor(ContextCompat.getColor(context, colorId));
        arc.setValue(gauge_start + (result * 6));
        txtPercent.setText(HtmlCompat.fromHtml((result >= 0 && result < 10 ? " " : "") + result + " %" + (isNonEmpty(rssi) ? "<br/><small>(" + rssi + ")</small>" : ""), HtmlCompat.FROM_HTML_MODE_LEGACY));
        degree = const_degree + (result * 2.72f);
        showLog("DEGREE", "" + degree);
        showLog("RES", "" + result);
        needle.setRotateDegree(degree);
        if(isMultiColor) needle.setNeedleColor(ContextCompat.getColor(context, colorId));
        degree = const_degree;
        if(!isAnimated) oldPercentage = result;
      });
    }
  }
  
  public boolean isSingleTagSearch(){ return chkSingleTag.isChecked(); }
  
  public void setEnableCheck(final boolean isEnableCheck){ chkSingleTag.setEnabled(isEnableCheck); }
  
  /**
   * Set layout.
   */
  private void setLayout(){
    resetToDefault();
  }
  
}
