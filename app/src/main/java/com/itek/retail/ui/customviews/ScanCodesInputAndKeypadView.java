package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.SharedPrefManager;

/**
 * The Scan codes input and keypad view.
 */
public class ScanCodesInputAndKeypadView extends LinearLayout{
  
  public ImageView imgScan, imgKeyPad;
  public InputView edtCode;
  Context context;
  TypedArray typedArray;
  boolean isKeyPadActive = false, isShowingError = false, isShowRfidLogo = false, isShowClearLogo = false;
  LinearLayout llRoot;
  String label, hint;
  Boolean isHUField;
  Boolean isSimpleScanField;
  
  Boolean isViewControlEnabled = true, isAllowClearOnViewControlDisabled = false;
  boolean isShowKeyboard = true, isRFIDScan = false;
  View.OnClickListener textChangeEvent;
  private boolean isProcessOn = false;
  
  /**
   * Instantiates a new Scan codes input and keypad view.
   *
   * @param context the context
   */
  public ScanCodesInputAndKeypadView(Context context){ this(context, null); }
  
  /**
   * Instantiates a new Scan codes input and keypad view.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public ScanCodesInputAndKeypadView(Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Scan codes input and keypad view.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public ScanCodesInputAndKeypadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
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
  public ScanCodesInputAndKeypadView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
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
    ScanCodesInputAndKeypadView.this.context = context;
    llRoot = (LinearLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_scan_code_input_and_keypad_view, this, true);
    edtCode = llRoot.findViewById(R.id.iv_code);
    imgScan = llRoot.findViewById(R.id.scan_img);
    imgKeyPad = llRoot.findViewById(R.id.verify_encoding_keyboard_img);
    if(attrs != null)
      typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanKeypadView, 0, 0);
    if(typedArray != null){
      hint = chkNull(typedArray.getString(R.styleable.ScanKeypadView_android_hint),"");
      label = chkNull(typedArray.getString(R.styleable.ScanKeypadView_label),"");
      isHUField = typedArray.getBoolean(R.styleable.ScanKeypadView_isHUField, false);
      isSimpleScanField = typedArray.getBoolean(R.styleable.ScanKeypadView_isSimpleScanField, false);
      isRFIDScan = !isSimpleScanField && !isHUField && typedArray.getBoolean(R.styleable.ScanKeypadView_isRFIDScan, false);
      isShowKeyboard = !isRFIDScan && typedArray.getBoolean(R.styleable.ScanKeypadView_isShowKeyboard, !isRFIDScan);
      isShowRfidLogo = typedArray.getBoolean(R.styleable.ScanKeypadView_isShowRfidLogo, SharedPrefManager.getDeviceTypeValue() > 0);
      isShowClearLogo = typedArray.getBoolean(R.styleable.ScanKeypadView_isShowClearLogo, true);
      
      edtCode.setValidationRegex(chkNull(typedArray.getString(R.styleable.ScanKeypadView_validationRegex),""));
      edtCode.setInputRegex(chkNull(typedArray.getString(R.styleable.ScanKeypadView_inputRegex),""));
      edtCode.setMinLen(typedArray.getInt(R.styleable.ScanKeypadView_minLength, 1));
      edtCode.setMaxLen(typedArray.getInt(R.styleable.ScanKeypadView_android_maxLength, 50));
      edtCode.setUnit(chkNull(typedArray.getString(R.styleable.ScanKeypadView_unit), ""));
      edtCode.setDigits(chkNull(typedArray.getString(R.styleable.ScanKeypadView_android_digits),context.getString(R.string.onlyPasswordDigits)));
      edtCode.setInputType(typedArray.getInt(R.styleable.ScanKeypadView_android_inputType, InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
      
    }
    llRoot.setOrientation(VERTICAL);
    edtCode.setText("");
    imgScan.setVisibility((isShowClearLogo && edtCode.getText().length() > 0) || (edtCode.getText().length() <= 0 && isShowRfidLogo) ? VISIBLE : GONE);
    imgKeyPad.setImageResource(isKeyPadActive ? R.drawable.ic_keyboard_disabled : R.drawable.ic_keyboard);
    
    setScanType(isRFIDScan, isShowKeyboard, label, hint);
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
   * @param isRFIDScan the is rfid scan
   */
  public void setScanType(boolean isRFIDScan){ setScanType(isRFIDScan, !isRFIDScan, "", ""); }
  
  /**
   * Set scan type.
   *
   * @param isRFIDScan     the is rfid scan
   * @param isShowKeyboard the is show keyboard
   * @param label          the label
   * @param hint           the hint
   */
  public void setScanType(boolean isRFIDScan, boolean isShowKeyboard, String label, String hint){
    this.isRFIDScan = isRFIDScan;
    final boolean isHUField = this.isHUField || chkNull(label, "").matches("(?i).*(hu|box|carton).*") || chkNull(hint, "").matches("(?i).*(hu|box|carton).*");
    final boolean isTIDField = !isHUField && chkNull(label, "").matches("(?i).*(tid|rfid qr).*") || chkNull(hint, "").matches("(?i).*(tid).*");
    this.isShowKeyboard = isShowKeyboard;
    edtCode.clearError();
    edtCode.setText("");
    edtCode.setLabel(chkNull(label, context.getString(isRFIDScan ? R.string.lbl_rfid : R.string.lbl_barcode)));
    edtCode.setHint(chkNull(hint, context.getString(isRFIDScan ? R.string.hint_rfid : R.string.hint_barcode)));
    if(!isSimpleScanField){
      edtCode.setMinLen(1);
      edtCode.setMaxLen(isHUField ? 20 : isTIDField ? 24 : !isRFIDScan && SharedPrefManager.getIsAllowNonStdEans() && SharedPrefManager.getIsAllowAlphaNumericNonStdEans() ? SharedPrefManager.getBoolean(ParamConstants.IS_USE_REFERENCE_BARCODE, AppCommonMethods.isUseReferenceBarcode) ? 50 : 17 : SharedPrefManager.getBoolean(ParamConstants.IS_USE_REFERENCE_BARCODE, AppCommonMethods.isUseReferenceBarcode) ? 50 : 20);
      //edtCode.setMaxLen(20);
      if(!isRFIDScan && !isTIDField && !isHUField && !SharedPrefManager.getIsAllowAlphaNumericNonStdEans())
        edtCode.setInputType(InputType.TYPE_CLASS_NUMBER);
      else edtCode.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
      edtCode.setDigits(isTIDField ? R.string.onlyHexDigits : SharedPrefManager.getIsAllowAlphaNumericNonStdEans() || isHUField ? R.string.onlyBarcodeDigits : isRFIDScan ? R.string.onlyHexDigits : R.string.onlyDigits);
      edtCode.setValidationRegex(AppCommonMethods.getEanRegex(isHUField, isRFIDScan));
    }
    imgScan.setImageResource(edtCode.getText().length() > 0 ? R.drawable.ic_cancel : isRFIDScan ? R.drawable.ic_rfid : isTIDField ? R.drawable.ic_qr_code : R.drawable.ic_scan);
    imgKeyPad.setVisibility(!isRFIDScan && isShowKeyboard && isViewControlEnabled ? VISIBLE : GONE);
    edtCode.setEnabled(!isRFIDScan && !isTIDField);
  }
  
  public void setHUField(Boolean HUField){
    isHUField = HUField;
  }
  
  /**
   * Image clear click.
   */
  public void imageClearClick(){
    imgScan.setOnClickListener(v -> {
      if(isShowClearLogo && isViewControlEnabled && edtCode.getText().length() > 0){
        edtCode.setText("");
      }
    });
  }
  
  /**
   * Set img scan on click listener.
   *
   * @param onClickListener the on click listener
   */
  public void setImgScanOnClickListener(View.OnClickListener onClickListener){
    imgScan.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View view){
        if(isShowClearLogo && (isAllowClearOnViewControlDisabled || isViewControlEnabled) && edtCode.getText().length() > 0){
          onClickListener.onClick(view);
          edtCode.setText("");
          if(ScanCodesInputAndKeypadView.this.getTag() != null)
            ScanCodesInputAndKeypadView.this.setTag(null);
          imgKeyPad.setVisibility(!isRFIDScan && isShowKeyboard && isViewControlEnabled ? VISIBLE : GONE);
          setIsViewControlEnabled(true);
        }
        else if(isViewControlEnabled && SharedPrefManager.getDeviceTypeValue() > 0){
          edtCode.hideKeyboard();
          setKeyPadActive(false);
          edtCode.setText("");
          imgKeyPad.setVisibility(GONE);
          onClickListener.onClick(view);
        }
      }
    });
  }
  
  /**
   * Perform scan.
   */
  public void performScan(){ imgScan.performClick(); }
  
  /**
   * Image key pad click.
   */
  private void imageKeyPadClick(){
    imgKeyPad.setOnClickListener(v -> {
      if(isViewControlEnabled){
        setKeyPadActive(!isKeyPadActive);
        edtCode.setText("");
        if(isKeyPadActive) edtCode.requestFocus();
      }
    });
  }
  
  /**
   * Set key pad active.
   *
   * @param isKeyPadActive the is key pad active
   */
  public void setKeyPadActive(boolean isKeyPadActive){
    this.isKeyPadActive = !isRFIDScan && isKeyPadActive;
    imgKeyPad.setImageResource(isKeyPadActive ? R.drawable.ic_keyboard_disabled : R.drawable.ic_keyboard);
    edtCode.setEnabled(isKeyPadActive);
  }
  
  /**
   * Set layout.
   */
  void setLayout(){
    imageClearClick();
    imageKeyPadClick();
    final boolean isHUField = this.isHUField || chkNull(label, "").matches("(?i).*(hu).*") || chkNull(hint, "").matches("(?i).*(hu).*");
    final boolean isTIDField = chkNull(label, "").matches("(?i).*(tid|rfid qr).*") || chkNull(hint, "").matches("(?i).*(tid).*");
    edtCode.setTextChangeEvent(new OnClickListener(){
      @Override
      public void onClick(View view){
        imgScan.setImageResource(edtCode.getText().length() > 0 ? R.drawable.ic_cancel : isRFIDScan ? R.drawable.ic_rfid : isTIDField ? R.drawable.ic_qr_code : R.drawable.ic_scan);
        imgScan.setVisibility((edtCode.getText().length() > 0 && isShowClearLogo) || (edtCode.getText().length() <= 0 && isShowRfidLogo) ? VISIBLE : GONE);
        if(textChangeEvent != null) textChangeEvent.onClick(null);
      }
    });
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
   * Set go btn.
   *
   * @param btnGo the btn go
   */
  public void setGoBtn(final View btnGo){
    edtCode.setButton(btnGo);
  }
  
  public void setIsShowClearLogo(final boolean isShowClearLogo){
    this.isShowClearLogo = isShowClearLogo;
    imgScan.setVisibility((isShowClearLogo && edtCode.getText().length() > 0) || (edtCode.getText().length() <= 0 && isShowRfidLogo) ? VISIBLE : GONE);
  }
  
  public void setIsShowRfidLogo(final boolean isShowRfidLogo){
    this.isShowRfidLogo = isShowRfidLogo;
    this.isShowKeyboard = isShowRfidLogo;
    imgScan.setVisibility((isShowClearLogo && edtCode.getText().length() > 0) || (edtCode.getText().length() <= 0 && isShowRfidLogo) ? VISIBLE : GONE);
    imgKeyPad.setVisibility(!isRFIDScan && isShowKeyboard && isViewControlEnabled ? VISIBLE : GONE);
  }
  
  /**
   * Set button click.
   *
   * @param buttonClick the button click
   */
  public void setButtonClick(View.OnClickListener buttonClick){
    edtCode.setButtonClick(buttonClick);
  }
  
  /**
   * Get text string.
   *
   * @return the string
   */
  public String getText(){
    return edtCode.getText().toString();
  }
  
  /**
   * Set text.
   *
   * @param text the text
   */
  public void setText(CharSequence text){
    edtCode.setText(isSimpleScanField||isHUField?text.toString().trim():AppCommonMethods.getLeftZeroReplacedString(context, text.toString()));
    if(!isRFIDScan && isShowKeyboard) setIsViewControlEnabled(isNullOrEmpty(text.toString()), true);
  }
  
  /**
   * Get hint string.
   *
   * @return the string
   */
  public String getHint(){
    return edtCode.getHint();
  }
  
  public void setHint(final String hint){
    edtCode.setHint(chkNull(hint, chkNull(this.hint, context.getString(isRFIDScan ? R.string.hint_rfid : R.string.hint_barcode))));
  }
  
  /**
   * Get label string.
   *
   * @return the string
   */
  public String getLabel(){
    return edtCode.getLabel().split("\\(")[0].trim();
  }
  
  public void setLabel(final String label){
    edtCode.setLabel(chkNull(label, chkNull(this.label, context.getString(isRFIDScan ? R.string.lbl_rfid : R.string.lbl_barcode))));
  }
  
  /**
   * Is view control enabled boolean.
   *
   * @return the boolean
   */
  public boolean isViewControlEnabled(){
    return isViewControlEnabled;
  }
  
  public void setIsProcessOn(Boolean isProcessOn){
    this.isProcessOn = isProcessOn;
    setIsViewControlEnabled(null, null);
  }
  
  /**
   * Set is view control enabled.
   *
   * @param viewControlEnabled the view control enabled
   */
  public void setIsViewControlEnabled(Boolean viewControlEnabled){ setIsViewControlEnabled(viewControlEnabled, false); }
  
  /**
   * Set is view control enabled.
   *
   * @param viewControlEnabled              the view control enabled
   * @param allowClearOnViewControlDisabled the allow clear on view control disabled
   */
  public void setIsViewControlEnabled(Boolean viewControlEnabled, Boolean allowClearOnViewControlDisabled){
    if(viewControlEnabled != null) isViewControlEnabled = viewControlEnabled;
    if(allowClearOnViewControlDisabled != null)
      isAllowClearOnViewControlDisabled = allowClearOnViewControlDisabled;
    //isViewControlEnabled = viewControlEnabled;
    //isAllowClearOnViewControlDisabled = allowClearOnViewControlDisabled;
    isKeyPadActive = !isRFIDScan && isViewControlEnabled && !isProcessOn;
    imgKeyPad.setImageResource(isKeyPadActive ? R.drawable.ic_keyboard_disabled : R.drawable.ic_keyboard);
    imgKeyPad.setVisibility(!isRFIDScan && isShowKeyboard && isViewControlEnabled && !isProcessOn ? VISIBLE : GONE);
    edtCode.setEnabled(isKeyPadActive);
  }
  
  /**
   * Validate boolean.
   *
   * @return the boolean
   */
  public boolean validate(){ return edtCode.validate(); }
  
  /**
   * Get err lbl string.
   *
   * @return the string
   */
  public String getErrLbl(){ return edtCode.getErrLbl(); }

  public void setValidationRegex(final String validationRegex){
    edtCode.setValidationRegex(validationRegex);
  }
}