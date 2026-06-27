package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.itek.retail.R;

/**
 * The Sort header view.
 */
public class SortHeaderView extends LinearLayout{
  
  Context context;
  TypedArray typedArray;
  String sortColumn = "", text = "";
  boolean isShowOrderIcon = false;
  Boolean isDescOrder = null;
  LinearLayout llRoot;
  TextView txtMain, txtIcon;
  
  /**
   * Instantiates a new Sort header view.
   *
   * @param context the context
   */
  public SortHeaderView(Context context){
    this(context, null);
  }
  
  /**
   * Instantiates a new Sort header view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public SortHeaderView(Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Sort header view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public SortHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
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
    SortHeaderView.this.context = context;
    llRoot = (LinearLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_sort_header, this, true);
    txtMain = llRoot.findViewById(R.id.txt_main);
    txtIcon = llRoot.findViewById(R.id.txt_icon);
    if(attrs != null)
      typedArray = context.obtainStyledAttributes(attrs, R.styleable.SortHeaderView, 0, 0);
    if(typedArray != null){
      sortColumn = chkNull(typedArray.getString(R.styleable.SortHeaderView_sortColumn), "");
      text = chkNull(typedArray.getString(R.styleable.SortHeaderView_android_text), "");
      isShowOrderIcon = typedArray.getBoolean(R.styleable.SortHeaderView_isShowIcon, true);
      setText(text);
    }
    txtMain.setSelected(true);
    setLayout();
  }
  
  /**
   * Reset.
   */
  public void reset(){
    isDescOrder = null;
    setLayout();
  }
  
  /**
   * Update desc order.
   */
  public void updateDescOrder(){
    if(chkNotNullTrue(isDescOrder)) reset();
    else{
      isDescOrder = isDescOrder == null ? false : !isDescOrder ? true : false;
      setLayout();
    }
  }
  
  /**
   * Get sort column string.
   *
   * @return the string
   */
  public String getSortColumn(){ return chkNull(sortColumn, ""); }
  
  /**
   * Get sort order string.
   *
   * @return the string
   */
  public String getSortOrder(){ return isDescOrder != null ? isDescOrder ? "desc" : "asc" : ""; }
  
  /**
   * Set show order icon.
   *
   * @param isShowOrderIcon the is show order icon
   */
  public void setShowOrderIcon(boolean isShowOrderIcon){
    this.isShowOrderIcon = isShowOrderIcon;
    setLayout();
  }
  
  @Override
  protected void onFinishInflate(){
    super.onFinishInflate();
    setLayout();
  }
  
  /**
   * Set text.
   *
   * @param txtId the txt id
   */
  public void setText(@StringRes int txtId){
    txtMain.setText(txtId);
  }
  
  /**
   * Set text.
   *
   * @param txt the txt
   */
  public void setText(CharSequence txt){
    text = txt.toString();
    txtMain.setText(txt);
  }
  
  /**
   * Set text color.
   *
   * @param txtColor the txt color
   */
  public void setTextColor(int txtColor){
    txtMain.setTextColor(txtColor);
  }
  
  /**
   * Set layout.
   */
  void setLayout(){
    txtIcon.setVisibility(isShowOrderIcon ? VISIBLE : GONE);
    txtIcon.setText(isDescOrder != null ? isDescOrder ? R.string.icon_sort_desc : R.string.icon_sort_asc : R.string.icon_sort);
  }
}
