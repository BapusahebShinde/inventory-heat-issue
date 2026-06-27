package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNonZeroId;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.textfield.TextInputLayout;
import com.itek.retail.R;

public class InputTextView extends TextInputLayout{
  
  Context context;
  TypedArray typedArray;
  TextView etInput;
  TextInputLayout tilRoot;
  String label, hint, validationRegex, inputRegex;
  int minLen, maxLen;
  boolean isHideLabel = false;
  View btnClick;
  View.OnClickListener buttonClick;
  View.OnClickListener clearErrorClick;
  View.OnClickListener textChangeEvent;
  //
  boolean isTextChange = true;
  String beforeText = "";
  int startIndex = 0;
  boolean isBackSpace = false;
  
  /**
   * Instantiates a new Input view.
   *
   * @param context the context
   */
  public InputTextView(Context context){ this(context, null); }
  
  /**
   * Instantiates a new Input view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public InputTextView(Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Input view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public InputTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
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
    InputTextView.this.context = context;
    tilRoot = (TextInputLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_text_input, this, true);
    etInput = tilRoot.findViewById(R.id.edt_input);
    if(attrs != null)
      typedArray = context.obtainStyledAttributes(attrs, R.styleable.InputView, 0, 0);
    if(typedArray != null){
      validationRegex = typedArray.getString(R.styleable.InputView_validationRegex);
      inputRegex = typedArray.getString(R.styleable.InputView_inputRegex);
      minLen = typedArray.getInt(R.styleable.InputView_minLength, 1);
      isHideLabel = typedArray.getBoolean(R.styleable.InputView_isHideLabel, false);
      etInput.setBackgroundResource(isHideLabel ? R.color.transparent : R.drawable.edit_text_border);
      final int padding = isHideLabel ? 0 : getResources().getDimensionPixelSize(R.dimen.dp_5);
      etInput.setPadding(padding, padding, padding, padding);
      setMaxLen(typedArray.getInt(R.styleable.InputView_android_maxLength, 50));
      setHint(chkNull(typedArray.getString(R.styleable.InputView_android_hint), ""));
      setLabel(chkNull(typedArray.getString(R.styleable.InputView_label), ""));
      final String digits = typedArray.getString(R.styleable.InputView_android_digits);
      final int inputType = typedArray.getInt(R.styleable.InputView_android_inputType, 0);
      if(inputType > 0) etInput.setRawInputType(inputType);
      if(isNonEmpty(digits)) etInput.setKeyListener(DigitsKeyListener.getInstance(digits));
      etInput.setText(chkNull(typedArray.getString(R.styleable.InputView_android_text), ""));
    }
    
  }
  
  @Override
  protected void onFinishInflate(){
    super.onFinishInflate();
    
  }
  
  /**
   * Set layout.
   */
  
  /**
   * Hide keyboard.
   */
  public void hideKeyboard(){
    if(context != null && etInput != null)
      ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etInput.getWindowToken(), 0);
  }
  
  /**
   * Set button.
   *
   * @param btnGo the btn go
   */
  public void setButton(final View btnGo){
    this.btnClick = btnGo;
  }
  
  /**
   * Set button click.
   *
   * @param buttonClick the button click
   */
  public void setButtonClick(View.OnClickListener buttonClick){
    this.buttonClick = buttonClick;
  }
  
  /**
   * Perform btn click.
   */
  public void performBtnClick(){
    if(buttonClick != null){ buttonClick.onClick(null); }
    else if(btnClick != null){ btnClick.performClick(); }
  }
  
  /**
   * Set clear error click.
   *
   * @param clearErrorClick the clear error click
   */
  public void setClearErrorClick(View.OnClickListener clearErrorClick){
    this.clearErrorClick = clearErrorClick;
  }
  
  /**
   * Set text change event.
   *
   * @param textChangeEvent the text change event
   */
  public void setTextChangeEvent(View.OnClickListener textChangeEvent){
    this.textChangeEvent = textChangeEvent;
  }
  
  /**
   * Clear error.
   */
  public void clearError(){
    if(clearErrorClick != null) clearErrorClick.onClick(null);
    this.setError(null);
    this.setErrorEnabled(false);
    this.setExpandedHintEnabled(false);
  }
  
  /**
   * Get err lbl string.
   *
   * @return the string
   */
  public String getErrLbl(){
    return chkNull(label, hint).replaceAll("Please ", "").replaceAll("Enter ", "").trim().toLowerCase();
  }
  
  /**
   * Validate boolean.
   *
   * @return the boolean
   */
  public boolean validate(){
    clearError();
    final String errLbl = chkNull(label, hint).replaceAll("Please ", "").replaceAll("Enter ", "").trim().toLowerCase();
    final String txt = chkNull(etInput.getText().toString(), "").trim();
    if(txt.length() == 0)
      updateError(String.format(context.getString(R.string.field_err_empty), errLbl));
    else if(txt.length() < minLen || (isNonEmpty(validationRegex) && !txt.matches(validationRegex)))
      updateError(String.format(context.getString(R.string.field_err_invalid), errLbl));
    return this.getError() == null;
  }
  
  /**
   * Update error.
   *
   * @param error the error
   */
  private void updateError(String error){
    if(isNonEmpty(error)){
      this.setErrorEnabled(true);
      this.setError(error);
    }
    else clearError();
  }
  
  /**
   * Get text string.
   *
   * @return the string
   */
  public String getText(){ return etInput.getText().toString(); }
  
  /**
   * Set text.
   *
   * @param textResId the text res id
   */
  public void setText(@StringRes int textResId){
    if(isNonZeroId(textResId)) etInput.setText(textResId);
  }
  
  /**
   * Set text.
   *
   * @param text the text
   */
  public void setText(CharSequence text){ etInput.setText(text); }
  
  /**
   * Get label string.
   *
   * @return the string
   */
  public String getLabel(){ return this.label; }
  
  /**
   * Set label.
   *
   * @param labelResId the label res id
   */
  public void setLabel(@StringRes int labelResId){
    if(isNonZeroId(labelResId)) setLabel(context.getString(labelResId));
  }
  
  /**
   * Set label.
   *
   * @param label the label
   */
  public void setLabel(CharSequence label){
    this.label = label.toString().trim();
    this.setPlaceholderText(label);
    
  }
  
  @Override
  public String getHint(){ return this.hint; }
  
  @Override
  public void setHint(@StringRes int hintResId){
    if(isNonZeroId(hintResId)) setHint(context.getString(hintResId));
  }
  
  /**
   * Set hint.
   *
   * @param hint the hint
   */
  public void setHint(String hint){
    this.hint = hint.trim();
    etInput.setHint(hint);
  }
  
  /**
   * Get validation regex string.
   *
   * @return the string
   */
  public String getValidationRegex(){ return this.validationRegex; }
  
  /**
   * Set validation regex.
   *
   * @param validationRegex the validation regex
   */
  public void setValidationRegex(String validationRegex){
    this.validationRegex = validationRegex.trim();
  }
  
  /**
   * Get input regex string.
   *
   * @return the string
   */
  public String getInputRegex(){ return this.inputRegex; }
  
  /**
   * Set input regex.
   *
   * @param inputRegex the input regex
   */
  public void setInputRegex(String inputRegex){
    this.inputRegex = inputRegex.trim();
  }
  
  /**
   * Get min len int.
   *
   * @return the int
   */
  public int getMinLen(){ return minLen; }
  
  /**
   * Set min len.
   *
   * @param minLen the min len
   */
  public void setMinLen(int minLen){ this.minLen = minLen; }
  
  /**
   * Get max len int.
   *
   * @return the int
   */
  public int getMaxLen(){ return maxLen; }
  
  /**
   * Set max len.
   *
   * @param maxLen the max len
   */
  public void setMaxLen(int maxLen){
    this.maxLen = maxLen;
    etInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
  }
  
  /**
   * Get max lines int.
   *
   * @return the int
   */
  public int getMaxLines(){ return /*etInput.isSingleLine()?1:*/etInput.getMaxLines(); }
  
  /**
   * Set max lines.
   *
   * @param maxLines the max lines
   */
  public void setMaxLines(int maxLines){
    etInput.setMaxLines(maxLines);
    etInput.setSingleLine(maxLines <= 1);
  }
  
  /**
   * Set digits.
   *
   * @param digitsRes the digits res
   */
  public void setDigits(@StringRes int digitsRes){
    if(isNonZeroId(digitsRes)) setDigits(context.getString(digitsRes));
  }
  
  /**
   * Set digits.
   *
   * @param digits the digits
   */
  public void setDigits(String digits){
    etInput.setKeyListener(digits.matches("^[0-9]+$") ? DigitsKeyListener.getInstance(digits) : TextKeyListener.getInstance());
  }
  
  /**
   * Set input type.
   *
   * @param inputType the input type
   */
  public void setInputType(int inputType){
    etInput.setRawInputType(inputType);
  }
}
