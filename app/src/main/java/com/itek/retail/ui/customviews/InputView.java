package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNonZeroId;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.text.HtmlCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.itek.retail.R;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * The Input view.
 */
public class InputView extends TextInputLayout{
  
  Context context;
  TypedArray typedArray;
  AutoCompleteTextView etInput;
  //EditText etInput;
  
  TextInputLayout tilRoot;
  String label, hint, validationRegex, inputRegex, unit;
  int minLen, maxLen;
  boolean isHideLabel = false;
  boolean isAutoCompleteView = false;
  boolean isMandatory = false;
  View btnClick;
  View.OnClickListener buttonClick;
  View.OnClickListener clearErrorClick;
  View.OnClickListener textChangeEvent;
  List<String> adapterList = new ArrayList<>(0);
  boolean isAdapterSelectionMandatory = false;
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
  public InputView(Context context){ this(context, null); }
  
  /**
   * Instantiates a new Input view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public InputView(Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Input view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public InputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
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
    InputView.this.context = context;
    tilRoot = (TextInputLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_input, this, true);
    etInput = tilRoot.findViewById(R.id.act_input);
    if(attrs != null)
      typedArray = context.obtainStyledAttributes(attrs, R.styleable.InputView, 0, 0);
    if(typedArray != null){
      validationRegex = chkNull(typedArray.getString(R.styleable.InputView_validationRegex),"");
      inputRegex = chkNull(typedArray.getString(R.styleable.InputView_inputRegex),"");
      minLen = typedArray.getInt(R.styleable.InputView_minLength, 1);
      isHideLabel = typedArray.getBoolean(R.styleable.InputView_isHideLabel, false);
      isMandatory = typedArray.getBoolean(R.styleable.InputView_isMandatory, false);
      unit = chkNull(typedArray.getString(R.styleable.InputView_unit), "");
      etInput.setBackgroundResource(isHideLabel ? R.color.transparent : R.drawable.edit_text_border);
      final int padding = isHideLabel ? 0 : getResources().getDimensionPixelSize(R.dimen.dp_7);
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
    setLayout();
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
    etInput.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after){
        beforeText = s.toString();
        startIndex = start;
        isBackSpace = start > after || count < 1;
      }
      
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count){
        /*
         *
         * todo use in future*/
      }
      
      @Override
      public void afterTextChanged(Editable s){
        clearError();
        if(isTextChange){
          if(isNullOrEmpty(inputRegex) || s.length() == 0 || s.toString().matches(inputRegex)){
            if(textChangeEvent != null) textChangeEvent.onClick(null);
          }
          else{
            isTextChange = false;
            etInput.setText(beforeText);
            etInput.setSelection(startIndex);
          }
        }
        else isTextChange = true;
      }
    });
    
    etInput.setOnEditorActionListener((v, actionId, event) -> {
      if(event != null && (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH)){
        hideKeyboard();
        performBtnClick();
        return true;
      }
      return false;
    });
  }
  
  public void setAdapter(List<String> list){
    setAdapter(list,false);
  }
  public void setAdapter(List<String> list,boolean isAdapterSelectionMandatory){
    if(isNullOrEmpty(list)) return;
    adapterList.clear();
    adapterList.addAll(list);
    this.isAdapterSelectionMandatory=isAdapterSelectionMandatory;
    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
      int minLen = list.stream().mapToInt(String::length).min().orElse(-1);
      if(minLen>0 && minLen<this.minLen) setMinLen(minLen);
      int maxLen = list.stream().mapToInt(String::length).max().orElse(-1);
      if(maxLen>0 && maxLen>this.maxLen) setMaxLen(maxLen);
    }
    setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_spinner_dropdown_item,adapterList));
  }
  
  /**
   * Set Adapter.
   */
  public void setAdapter(ArrayAdapter<?> arrayAdapter){
    final boolean isEmptyAdapter = arrayAdapter==null || arrayAdapter.isEmpty();
    etInput.setAdapter(isEmptyAdapter?null:arrayAdapter);
    etInput.setThreshold(1);
    etInput.setOnClickListener(isEmptyAdapter?null: new OnClickListener(){
      @Override
      public void onClick(View v){
        if(etInput.getText().length()<=0) etInput.showDropDown();
      }
    });
    etInput.setOnItemSelectedListener(isEmptyAdapter?null: new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        etInput.setText(etInput.getAdapter().getItem(position).toString().trim());
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> parent){
      
      }
    });
  }
  
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
    InputView.this.setError(null);
    InputView.this.setErrorEnabled(false);
    InputView.this.setExpandedHintEnabled(false);
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
    else if(isAdapterSelectionMandatory && etInput.getAdapter()!=null && isNonEmpty(adapterList) && !adapterList.contains(txt)) //Show Error if Not Matching From given List
      updateError(String.format(context.getString(R.string.field_err_invalid), errLbl));
    return this.getError() == null;
  }
  
  /**
   * Update error.
   *
   * @param error the error
   */
  public void updateError(String error){
    if(isNonEmpty(error)){
      InputView.this.setErrorEnabled(true);
      InputView.this.setError(error);
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
    InputView.this.label = label.toString().trim();
    InputView.this.setPlaceholderText(HtmlCompat.fromHtml((label+(isNonEmpty(unit)?"\t("+unit+")":"").trim()+(isMandatory?"\t"+"<font color=\"red\">"+context.getString(R.string.icon_asterisk)+"</font>":"")).trim(),HtmlCompat.FROM_HTML_MODE_COMPACT));
    InputView.this.setPlaceholderText(HtmlCompat.fromHtml((label+(isNonEmpty(unit)?"\t("+unit+")":"").trim()+(isMandatory?"\t"+context.getString(R.string.lbl_mandatory_asterisk):"")).trim(),HtmlCompat.FROM_HTML_MODE_COMPACT));
    //InputView.this.setPlaceholderText(label+(isNonEmpty(unit)?"\t("+unit+")":"").trim()+(isMandatory?context.getString(R.string.icon_asterisk):""));
  }
  
  public String getUnit(){
    return chkNull(unit,"");
  }
  
  public void setUnit(@StringRes int unitResId){
    if(isNonZeroId(unitResId)) setUnit(context.getString(unitResId));
  }
  
  public void setUnit(CharSequence unit){
    this.unit = unit.toString().trim();
    if(isNonEmpty(label)) setLabel(label);
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
    InputView.this.hint = hint.trim();
    etInput.setHint(hint);
  }
  
  /**
   * Get validation regex string.
   *
   * @return the string
   */
  public String getValidationRegex(){ return chkNull(this.validationRegex,""); }
  
  /**
   * Set validation regex.
   *
   * @param validationRegex the validation regex
   */
  public void setValidationRegex(String validationRegex){
    InputView.this.validationRegex = chkNull(validationRegex,"").trim();
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
    InputView.this.inputRegex = chkNull(inputRegex,"").trim();
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
  public int getMaxLines(){
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
      return /*etInput.isSingleLine()?1:*/etInput.getMaxLines();
    else return 1;
    
  }
  
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
    if(isNonEmpty(digits))
      etInput.setKeyListener(digits.matches("^[0-9\\.-]+$") ? DigitsKeyListener.getInstance(digits) : TextKeyListener.getInstance());
  }
  
  /**
   * Set input type.
   *
   * @param inputType the input type
   */
  public void setInputType(int inputType){
    if(inputType > 0) etInput.setRawInputType(inputType);
  }
  
  public void setMandatory(boolean mandatory){
    isMandatory = mandatory;
    if(isNonEmpty(label)) setLabel(label);
  }
}
