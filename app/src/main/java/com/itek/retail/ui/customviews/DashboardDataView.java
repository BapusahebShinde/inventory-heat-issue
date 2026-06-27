package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.itek.retail.R;
import com.itek.retail.common.AppConstants;

/**
 * The Dashboard data view.
 */
public class DashboardDataView extends LinearLayout{
  
  Context context;
  TypedArray typedArray;
  boolean isUseSplitLabels = false, isShowGapsInSpiltLabels = false, isUseSplitViews = false, isShowGapsInSpiltValues = false, isShowPercent = false;
  Boolean isUpwardArrow = false;
  LinearLayout llRoot;
  String label, percent, data, header;
  TextView txtHeader, txtLabel, txtLabelDivider, txtLabel2, txtData, txtOutOf, txtUnit;
  LinearLayout llDivider;
  int percentColor = 0;
  
  /**
   * Instantiates a new Dashboard data view.
   *
   * @param context the context
   */
  public DashboardDataView(Context context){ this(context, null); }
  
  /**
   * Instantiates a new Dashboard data view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public DashboardDataView(Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Dashboard data view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public DashboardDataView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }
  
  /**
   * Instantiates a new Dashboard data view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   * @param defStyleRes  the def style res
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public DashboardDataView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
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
    DashboardDataView.this.context = context;
    
    llRoot = (LinearLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_view_dashboard_data, this, true);
    txtHeader = llRoot.findViewById(R.id.txt_header);
    txtLabel = llRoot.findViewById(R.id.txt_label);
    txtLabelDivider = llRoot.findViewById(R.id.txt_label_divider);
    txtLabel2 = llRoot.findViewById(R.id.txt_label2);
    txtData = llRoot.findViewById(R.id.txt_data);
    llDivider = llRoot.findViewById(R.id.ll_divider_symbol);
    txtOutOf = llRoot.findViewById(R.id.txt_data_outof);
    txtUnit = llRoot.findViewById(R.id.txt_percent);
    
    txtData.setSelected(true);
    txtOutOf.setSelected(true);
    if(attrs != null)
      typedArray = context.obtainStyledAttributes(attrs, R.styleable.DashboardDataView, 0, 0);
    if(typedArray != null){
      label = chkNull(typedArray.getString(R.styleable.DashboardDataView_label), "");
      header = chkNull(typedArray.getString(R.styleable.DashboardDataView_header), "");
      float headerSize = typedArray.getDimension(R.styleable.DashboardDataView_labelSize, 0.0f);
      int headerColor = typedArray.getResourceId(R.styleable.DashboardDataView_labelColor, 0);
      //showLog("headerColor", "" + headerColor);
      if(headerSize > 0) txtHeader.setTextSize(headerSize);
      if(headerColor > 0) txtHeader.setTextColor(ContextCompat.getColor(context, headerColor));
      setHeader(header);
      float lblSize = typedArray.getDimension(R.styleable.DashboardDataView_labelSize, 0.0f);
      int lblColor = typedArray.getResourceId(R.styleable.DashboardDataView_labelColor, 0);
      //showLog("lblColor", "" + lblColor);
      if(lblSize > 0){
        txtLabel.setTextSize(lblSize);
        txtLabelDivider.setTextSize(lblSize);
        txtLabel2.setTextSize(lblSize);
      }
      if(lblColor > 0){
        txtLabel.setTextColor(ContextCompat.getColor(context, lblColor));
        txtLabelDivider.setTextColor(ContextCompat.getColor(context, lblColor));
        txtLabel2.setTextColor(ContextCompat.getColor(context, lblColor));
      }
      data = chkNull(typedArray.getString(R.styleable.DashboardDataView_android_text), context.getString(R.string.default_no_value));
      float txtSize = typedArray.getDimension(R.styleable.DashboardDataView_android_textSize, 0.0f);
      int txtColor = typedArray.getColor(R.styleable.DashboardDataView_android_textColor, 0);
      if(txtSize > 0){
        txtData.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
        txtOutOf.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
      }
      if(txtColor > 0){
        txtData.setTextColor(txtColor);
        txtOutOf.setTextColor(txtColor);
      }
      percent = chkNull(typedArray.getString(R.styleable.DashboardDataView_percent), "");
      percentColor = typedArray.getResourceId(R.styleable.DashboardDataView_percentColor, 0);
      isUseSplitLabels = typedArray.getBoolean(R.styleable.DashboardDataView_isUseSplitLabels, false);
      isShowGapsInSpiltLabels = typedArray.getBoolean(R.styleable.DashboardDataView_isShowGapsInSpiltLabels, false);
      isUseSplitViews = typedArray.getBoolean(R.styleable.DashboardDataView_isUseSplitViews, true);
      isShowGapsInSpiltValues = typedArray.getBoolean(R.styleable.DashboardDataView_isShowGapsInSpiltValues, true);
      isUpwardArrow = typedArray.getBoolean(R.styleable.DashboardDataView_isUpwardArrow, true);
      isShowPercent = typedArray.getBoolean(R.styleable.DashboardDataView_isShowPercent, false);
      txtUnit.setVisibility(isShowPercent && isNonEmpty(percent) ? VISIBLE : GONE);
      setLabelTextPercent(label, data, percent, isUpwardArrow);
    }
    llRoot.setOrientation(VERTICAL);
  }
  
  @Override
  protected void onFinishInflate(){
    super.onFinishInflate();
    setLayout();
  }
  
  /**
   * Set layout.
   */
  void setLayout(){
    /*Empty Method*/
  }
  
  /**
   * Get text string.
   *
   * @return the string
   */
  public String getText(){
    return txtData.getText().toString();
  }
  
  /**
   * Set text.
   *
   * @param text the text
   */
  public void setText(CharSequence text){
    String data = chkNull(text.toString(), "");
    llDivider.setVisibility(GONE);
    txtOutOf.setVisibility(GONE);
    txtData.setGravity(Gravity.CENTER);
    txtOutOf.setGravity(Gravity.CENTER);
    final boolean hasHtmlcode = data.matches("^.*<.*>.*.*$");
    final boolean hasColor = data.matches("^.*<font .*color=.*>.*</font>.*$");
    if(data.contains(",")){
      txtData.setText(HtmlCompat.fromHtml("" + data.substring(0, data.indexOf(",")) + "", HtmlCompat.FROM_HTML_MODE_LEGACY));
      txtOutOf.setText(HtmlCompat.fromHtml("" + data.substring(data.indexOf(",") + 1) + "", HtmlCompat.FROM_HTML_MODE_LEGACY));
      txtOutOf.setVisibility(VISIBLE);
      llDivider.setVisibility(INVISIBLE);
    }
    else if(data.contains("/") && isUseSplitViews){
      txtData.setText(HtmlCompat.fromHtml("" + data.substring(0, data.indexOf("/")) + "", HtmlCompat.FROM_HTML_MODE_LEGACY));
      txtOutOf.setText(HtmlCompat.fromHtml("" + data.substring(data.indexOf("/") + 1) + "", HtmlCompat.FROM_HTML_MODE_LEGACY));
      if(!isShowGapsInSpiltValues){
        txtData.setGravity(Gravity.RIGHT);
        txtOutOf.setGravity(Gravity.LEFT);
      }
      txtOutOf.setVisibility(VISIBLE);
      llDivider.setVisibility(VISIBLE);
    }
    else txtData.setText(HtmlCompat.fromHtml("" + data + "", HtmlCompat.FROM_HTML_MODE_LEGACY));
  }
  
  /**
   * Is upward arrow boolean.
   *
   * @return the boolean
   */
  public boolean isUpwardArrow(){
    return isUpwardArrow;
  }
  
  /**
   * Set is upward arrow.
   *
   * @param upwardArrow the upward arrow
   */
  public void setIsUpwardArrow(Boolean upwardArrow){ setIsUpwardArrow(upwardArrow, ""); }
  
  /**
   * Set is upward arrow.
   *
   * @param upwardArrow the upward arrow
   * @param type        the type
   */
  public void setIsUpwardArrow(Boolean upwardArrow, String type){
    if(upwardArrow != null){
      this.isUpwardArrow = upwardArrow;
      txtUnit.setText(HtmlCompat.fromHtml((percent + (percent.contains("%") ? "" : "% ") + (percent.matches("(?i)(\u2191|\u2193)") ? "" : isUpwardArrow ? "\u2191" : "\u2193")).trim(), HtmlCompat.FROM_HTML_MODE_LEGACY));
      txtUnit.setTextColor(ContextCompat.getColor(context, isNonEmpty(type) ? type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_PENDING) ? isUpwardArrow ? R.color.txtRed : R.color.txtGreen : isUpwardArrow ? R.color.txtGreen : R.color.txtRed : percentColor > 0 ? percentColor : isUpwardArrow ? R.color.txtGreen : R.color.txtRed));
      txtUnit.setVisibility(isShowPercent && isNonEmpty(percent) ? VISIBLE : GONE);
    }
  }
  
  /**
   * Get header string.
   *
   * @return the string
   */
  public String getHeader(){ return header; }
  
  /**
   * Set header.
   *
   * @param header the header
   */
  public void setHeader(String header){
    this.header = chkNull(header, "").trim();
    txtHeader.setText(this.header);
    txtHeader.setVisibility(txtHeader.getText().length() > 0 ? View.VISIBLE : GONE);
  }
  
  /**
   * Get label string.
   *
   * @return the string
   */
  public String getLabel(){ return label; }
  
  /**
   * Set label.
   *
   * @param label the label
   */
  public void setLabel(String label){ setLabel(label, ""); }
  
  /**
   * Set label.
   *
   * @param label the label
   * @param type  the type
   */
  public void setLabel(String label, String type){
    this.label = label;
    txtLabelDivider.setVisibility(GONE);
    txtLabel2.setVisibility(GONE);
    txtLabel.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
    txtLabel.setBackgroundResource(R.color.transparent);
    txtLabel.setGravity(Gravity.CENTER);
    txtLabel2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
    txtLabel2.setBackgroundResource(R.color.transparent);
    txtLabel2.setGravity(Gravity.CENTER);
    final boolean hasHtmlcode = label.matches("^.*<.*>.*.*$");
    final boolean hasColor = label.matches("^.*<font .*color=.*>.*</font>.*$");
    if(!hasColor && label.contains("(") && !label.contains("(s)"))
      txtLabel.setText(HtmlCompat.fromHtml(label.substring(0, label.indexOf("(")) + "<font color=\"#949494\">" + label.substring(label.indexOf("(")) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY));
    else if(label.contains(",") /*&& data.contains(",")*/){
      txtLabelDivider.setVisibility(GONE);
      txtLabel2.setVisibility(VISIBLE);
      txtLabel.setText(HtmlCompat.fromHtml("" + label.split(",")[0].trim() + "", HtmlCompat.FROM_HTML_MODE_LEGACY));
      txtLabel2.setText(HtmlCompat.fromHtml("" + label.split(",")[1].trim() + "", HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
    else if(label.contains("/") && isUseSplitLabels){
      txtLabelDivider.setVisibility(VISIBLE);
      txtLabel2.setVisibility(VISIBLE);
      txtLabel.setText(HtmlCompat.fromHtml("" + label.split("/")[0].trim() + "", HtmlCompat.FROM_HTML_MODE_LEGACY));
      txtLabel2.setText(HtmlCompat.fromHtml("" + label.split("/")[1].trim() + "", HtmlCompat.FROM_HTML_MODE_LEGACY));
      if(!isShowGapsInSpiltLabels){
        txtLabel.setGravity(Gravity.RIGHT);
        txtLabel.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        txtLabel2.setGravity(Gravity.LEFT);
        txtLabel2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      }
      if(!hasColor && isNonEmpty(type) && chkNull(type, "").matches("(?i)(pending|completed)")){
        txtLabel.setTextColor(getResources().getColor(chkNull(type, "").matches("(?i) completed") ? R.color.txtGreen : R.color.txtRed));
        txtLabel2.setTextColor(getResources().getColor(chkNull(type, "").matches("(?i) completed") ? R.color.txtGreen : R.color.txtRed));
      }
    }
    else{
      txtLabel.setText(hasHtmlcode ? HtmlCompat.fromHtml(label, HtmlCompat.FROM_HTML_MODE_LEGACY) : label);
      if(!hasColor && isNonEmpty(type) && chkNull(type, "").matches("(?i)(pending|completed)"))
        txtLabel.setTextColor(getResources().getColor(chkNull(type, "").matches("(?i) completed") ? R.color.txtGreen : R.color.txtRed));
    }
  }
  
  /**
   * Set label under line.
   *
   * @param viewId the view id
   */
  public void setLabelUnderLine(final int viewId){
    if(label.contains("/") && isUseSplitLabels){
      txtLabel.setBackgroundResource(viewId == txtLabel.getId() ? R.drawable.lbl_red_border_bottom : R.color.transparent);
      txtLabel2.setBackgroundResource(viewId == txtLabel2.getId() ? R.drawable.lbl_red_border_bottom : R.color.transparent);
    }
    else
      txtLabel.setBackgroundResource(viewId == txtLabel.getId() ? R.drawable.lbl_red_border_bottom : R.color.transparent);
  }
  
  /**
   * Get txt label text view.
   *
   * @return the text view
   */
  public TextView getTxtLabel(){ return txtLabel; }
  
  /**
   * Get txt label 2 text view.
   *
   * @return the text view
   */
  public TextView getTxtLabel2(){ return txtLabel2; }
  
  /**
   * Get percent string.
   *
   * @return the string
   */
  public String getPercent(){ return percent; }
  
  /**
   * Set percent.
   *
   * @param percent the percent
   */
  public void setPercent(String percent){
    this.percent = chkNull(percent, "");
    txtUnit.setText(this.percent + "% " + (isUpwardArrow ? "\u2191" : "\u2193"));
    txtUnit.setTextColor(ContextCompat.getColor(context, isUpwardArrow ? R.color.txtGreen : R.color.txtRed));
    txtUnit.setVisibility(isShowPercent && isNonEmpty(this.percent) ? VISIBLE : GONE);
  }
  
  /**
   * Set percent.
   *
   * @param percent       the percent
   * @param isUpwardArrow the is upward arrow
   */
  public void setPercent(String percent, boolean isUpwardArrow){
    this.percent = percent;
    setIsUpwardArrow(isUpwardArrow);
  }
  
  /**
   * Set label text percent.
   *
   * @param label         the label
   * @param data          the data
   * @param percent       the percent
   * @param isUpwardArrow the is upward arrow
   */
  public void setLabelTextPercent(String label, String data, String percent, boolean isUpwardArrow){
    setLabel(label);
    setText(data);
    this.percent = chkNull(percent, "");
    setIsUpwardArrow(isUpwardArrow);
  }
  
  /**
   * Set label text percent.
   *
   * @param type          the type
   * @param label         the label
   * @param data          the data
   * @param percentLabel  the percent label
   * @param percent       the percent
   * @param isUpwardArrow the is upward arrow
   */
  public void setLabelTextPercent(String type, String label, String data, String percentLabel, String percent, Boolean isUpwardArrow){
    setLabel(label);
    setText(data);
    this.percent = percent;
    if(isNonEmpty(percentLabel))
      txtUnit.setText(HtmlCompat.fromHtml(percentLabel, HtmlCompat.FROM_HTML_MODE_LEGACY));
    else{
      if(isUpwardArrow == null && isNonEmpty(percent) && percent.contains("%") && percent.matches("^.*%.*(\u2191|\u2193).*$"))
        isUpwardArrow = chkNull(percentLabel, chkNull(percent, "")).contains("\u2191");
      setIsUpwardArrow(isUpwardArrow, type);
    }
  }
}
