package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.DATE_FORMAT;
import static com.itek.retail.common.AppCommonMethods.SERVER_DATE_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * The Scan codes input and keypad view.
 */
public class DateInputView extends LinearLayout{
  
  public ImageView imgDateLogo;
  public InputTextView txtCode;
  Context context;
  TypedArray typedArray;
  boolean isShowingError = false;
  Boolean isViewControlEnabled = true;
  LinearLayout llRoot;
  String label, hint, serverDate = "";
  Date date = null;
  final OnClickListener clickListener = new OnClickListener(){
    @Override
    public void onClick(View v){
      if(DateInputView.this.getVisibility() == VISIBLE && isViewControlEnabled && context != null && context instanceof CommonActivity){
        CommonActivity activity = (CommonActivity) context;
        final Calendar c = Calendar.getInstance();
        if(date != null){
          try{
            c.setTime(date);
          }
          catch(Exception e){
            e.printStackTrace();
          }
        }
        final DatePicker dpd = new DatePicker(activity);
        dpd.setMaxDate(Calendar.getInstance().getTimeInMillis());
        dpd.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        activity.showCustomAlertDialog(chkNull(label, chkNull(hint, "")), "", dpd, activity.getString(R.string.btn_set), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialog, int which){
            c.set(Calendar.YEAR, dpd.getYear());
            c.set(Calendar.MONTH, dpd.getMonth());
            c.set(Calendar.DAY_OF_MONTH, dpd.getDayOfMonth());
            setDate(c.getTime());
            //setText(c.getTime());
          }
        }, activity.getString(R.string.btn_cancel));
      }
    }
  };
  
  /**
   * Instantiates a new Scan codes input and keypad view.
   *
   * @param context the context
   */
  public DateInputView(Context context){ this(context, null); }
  
  /**
   * Instantiates a new Scan codes input and keypad view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public DateInputView(Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Scan codes input and keypad view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public DateInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }
  
  /**
   * Instantiates a new Scan codes input and keypad view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   * @param defStyleRes  the def style res
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public DateInputView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
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
    DateInputView.this.context = context;
    llRoot = (LinearLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_date_input_view, this, true);
    txtCode = llRoot.findViewById(R.id.itv_date);
    imgDateLogo = llRoot.findViewById(R.id.img_date);
    boolean isTodayDefaultDate = false;
    if(attrs != null)
      typedArray = context.obtainStyledAttributes(attrs, R.styleable.DateInputView, 0, 0);
    if(typedArray != null){
      hint = typedArray.getString(R.styleable.DateInputView_android_hint);
      label = typedArray.getString(R.styleable.DateInputView_label);
      isTodayDefaultDate = typedArray.getBoolean(R.styleable.DateInputView_isTodayDefaultDate, true);
    }
    llRoot.setOrientation(VERTICAL);
    setLabelHint(label, hint);
    if(isTodayDefaultDate) setDate(new Date());
    setLayout();
  }
  
  @Override
  protected void onFinishInflate(){
    super.onFinishInflate();
    setLayout();
  }
  
  /**
   * Set scan type.
   *
   * @param label the label
   * @param hint  the hint
   */
  public void setLabelHint(String label, String hint){
    txtCode.clearError();
    txtCode.setText("");
    txtCode.setLabel(chkNull(label, ""));
    txtCode.setHint(chkNull(hint, ""));
  }
  
  /**
   * Set layout.
   */
  void setLayout(){
    txtCode.setOnClickListener(clickListener);
    imgDateLogo.setOnClickListener(clickListener);
  }
  
  /**
   * Get text string.
   *
   * @return the string
   */
  public String getText(){
    return txtCode.getText().toString();
  }
  
  /**
   * Set text.
   *
   * @param text the text
   */
  public void setText(CharSequence text){
    txtCode.setText(AppCommonMethods.getLeftZeroReplacedString(context,text.toString()));
  }
  
  /**
   * Set text.
   *
   * @param cc the calendar
   */
  public void setText(final Calendar cc){
    setText(cc.getTime());
  }
  
  /**
   * Set text.
   *
   * @param date the date
   */
  public void setText(final Date date){
    setDate(date);
  }
  
  /**
   * Get date.
   *
   * @return the date
   */
  public Date getDate(){
    return date;
  }
  
  /**
   * Set text.
   *
   * @param date the date
   */
  public void setDate(final Date date){
    this.date = date;
    if(date != null){
      try{
        txtCode.setText(new SimpleDateFormat(DATE_FORMAT).format(date));
        serverDate = new SimpleDateFormat(SERVER_DATE_FORMAT).format(date);
      }
      catch(Exception e){ e.printStackTrace(); }
    }
  }
  
  /**
   * Get hint string.
   *
   * @return the string
   */
  public String getHint(){
    return txtCode.getHint();
  }
  
  /**
   * Get label string.
   *
   * @return the string
   */
  public String getLabel(){
    return txtCode.getLabel();
  }
  
  /**
   * Get server date string.
   *
   * @return the string
   */
  public String getServerDate(){
    return serverDate;
  }
  
  /**
   * Set is enabled.
   *
   * @param isEnabled the is enabled
   */
  public void setEnabled(boolean isEnabled){
    isViewControlEnabled = isEnabled;
    txtCode.setEnabled(isViewControlEnabled);
    imgDateLogo.setEnabled(isViewControlEnabled);
  }
  
  /**
   * Validate boolean.
   *
   * @return the boolean
   */
  public boolean validate(){ return txtCode.validate(); }
  
  /**
   * Get err lbl string.
   *
   * @return the string
   */
  public String getErrLbl(){ return txtCode.getErrLbl(); }
}