package com.itek.retail.ui.customviews;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.dp2px;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.HeaderSpinnerAdapter;
import com.itek.retail.adapter.HeaderSpinnerSearchDialogAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.SharedPrefManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Header spinner.
 */
public class HeaderSpinner extends LinearLayout{
  
  Context context;
  TypedArray typedArray;
  String label = "";
  boolean isMultiSelect = false;
  int maxSelectionLimit = 0;
  HeaderSpinnerAdapter headerSpinnerAdapter;
  LinearLayout llRoot;
  LinearLayout llSpinHeader;
  TextView txtLabel;
  TextView txtSpinHeader;
  Spinner spinVals;
  Set<String> selObjects = new HashSet<>(0);
  List<?> listObjects = new ArrayList<>(0);
  AdapterView.OnItemSelectedListener onItemSelectedListener = null;
  boolean isShowDialog = true;
  private String selItem;
  private Object selObj;
  
  /**
   * Instantiates a new Header spinner.
   *
   * @param context the context
   */
  public HeaderSpinner(@NotNull Context context){
    this(context, null);
  }
  
  /**
   * Instantiates a new Header spinner.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public HeaderSpinner(@NotNull Context context, @Nullable AttributeSet attrs){
    this(context, attrs, 0);
  }
  
  /**
   * Instantiates a new Header spinner.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   */
  public HeaderSpinner(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }
  
  /**
   * Instantiates a new Header spinner.
   *
   * @param context      the context
   * @param attrs        the attrs
   * @param defStyleAttr the def style attr
   * @param defStyleRes  the def style res
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public HeaderSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
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
    HeaderSpinner.this.context = context;
    llRoot = (LinearLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_view_header_spinner, this, true);
    txtLabel = llRoot.findViewById(R.id.txt_label);
    spinVals = llRoot.findViewById(R.id.spin_vals);
    txtSpinHeader = llRoot.findViewById(R.id.txt_spin_header);
    llSpinHeader = llRoot.findViewById(R.id.ll_spin_header);
    spinVals.setBackgroundResource(R.color.transparent);
    if(attrs != null)
      typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeaderSpinner, 0, 0);
    if(typedArray != null){
      //llSpinHeader.setOrientation(LinearLayout.HORIZONTAL);//typedArray.getInt(R.styleable.HeaderSpinner_android_orientation,1));
      label = chkNull(typedArray.getString(R.styleable.HeaderSpinner_label), "");
      if(isNonEmpty(label)){
        if(isNonEmpty(SharedPrefManager.getString(ParamConstants.LABEL_BRANDS)) && !label.equalsIgnoreCase(SharedPrefManager.getString(ParamConstants.LABEL_BRANDS)) && label.matches("(?i)^.*" + context.getString(R.string.lbl_brand) + ".*$"))
          label = label.replaceAll(context.getString(R.string.lbl_brand), SharedPrefManager.getString(ParamConstants.LABEL_BRANDS));
        else if(isNonEmpty(SharedPrefManager.getString(ParamConstants.LABEL_CATEGORIES)) && !label.equalsIgnoreCase(SharedPrefManager.getString(ParamConstants.LABEL_CATEGORIES)) && label.matches("(?i)^.*" + context.getString(R.string.lbl_category) + ".*$"))
          label = label.replaceAll(context.getString(R.string.lbl_category), SharedPrefManager.getString(ParamConstants.LABEL_CATEGORIES));
        else if(isNonEmpty(SharedPrefManager.getString(ParamConstants.LABEL_ZONES)) && !label.equalsIgnoreCase(SharedPrefManager.getString(ParamConstants.LABEL_ZONES)) && label.matches("(?i)^.*(" + context.getString(R.string.lbl_loc) + "|" + context.getString(R.string.lbl_location) + ").*$"))
          label = label.replaceAll("(" + context.getString(R.string.lbl_loc).replaceAll("\\.", "\\\\.") + "|" + context.getString(R.string.lbl_location) + ")", SharedPrefManager.getString(ParamConstants.LABEL_ZONES));
        else if(isNonEmpty(SharedPrefManager.getString(ParamConstants.LABEL_OUTWARD_TOTE_TYPES)) && !label.equalsIgnoreCase(SharedPrefManager.getString(ParamConstants.LABEL_OUTWARD_TOTE_TYPES)) && label.matches("(?i)^.*(" + context.getString(R.string.lbl_out_tot_type) + "|" + context.getString(R.string.lbl_type) + ").*$"))
          label = label.replaceAll("(" + context.getString(R.string.lbl_type).replaceAll("\\.", "\\\\.") + "|" + context.getString(R.string.lbl_out_tot_type) + ")", SharedPrefManager.getString(ParamConstants.LABEL_OUTWARD_TOTE_TYPES));
      }
      setLabel(label);
      isMultiSelect = typedArray.getBoolean(R.styleable.HeaderSpinner_isMultiSelect, false);
      final boolean isSetOptionAll = typedArray.getBoolean(R.styleable.HeaderSpinner_isSetOptionAll, true);
      int headerLayout = typedArray.getResourceId(R.styleable.HeaderSpinner_android_entries, 0);
      if(headerLayout > 0){
        final String[] objs = context.getResources().getStringArray(headerLayout);
        if(objs != null && objs.length > 0){
          List<String> list = Arrays.asList(objs);
          if(isSetOptionAll) list.add(0, AppConstants.ALL);
          setAdapter(list);
        }
      }
    }
  }
  
  @Override
  public void setEnabled(boolean enabled){
    super.setEnabled(true);
    txtLabel.setEnabled(enabled);
    llSpinHeader.setEnabled(enabled);
    txtSpinHeader.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, enabled ? R.drawable.ic_dropdown_arrow : 0, 0);
    spinVals.setEnabled(enabled);
    if(headerSpinnerAdapter != null){
      headerSpinnerAdapter.setHideDropdownSymbol(!enabled);
      headerSpinnerAdapter.notifyDataSetChanged();
    }
  }
  
  /**
   * Get label string.
   *
   * @return the string
   */
  public String getLabel(){ return chkNull(label, ""); }
  
  /**
   * Set label.
   *
   * @param label the label
   */
  public void setLabel(String label){
    if(isNonEmpty(label)){
      this.label = chkNull(label, "");
      txtLabel.setText(HtmlCompat.fromHtml(label + (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? " : " : ""), HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
  }
  
  /**
   * Set adapter.
   *
   * @param objects the objects
   */
  public void setAdapter(final List<?> objects, final boolean isMultiSelect){
    this.isMultiSelect = isMultiSelect;
    setAdapter(objects);
  }
  
  /**
   * Set adapter.
   *
   * @param objects the objects
   */
  public void setAdapter(final List<?> objects, final boolean isMultiSelect, final String label){
    this.isMultiSelect = isMultiSelect;
    setAdapter(objects, label);
  }
  
  /**
   * Set adapter.
   *
   * @param objects the objects
   */
  public void setAdapter(final List<?> objects){ setAdapter(objects, 0); }
  
  /**
   * Set adapter.
   *
   * @param objects the objects
   */
  public void setAdapter(final List<?> objects, final String label){ setAdapter(objects, 0, label); }
  
  /**
   * Set adapter.
   *
   * @param objects           the objects
   * @param maxSelectionLimit the max selection limit
   */
  public void setAdapter(final List<?> objects, final int maxSelectionLimit){ setAdapter(objects, maxSelectionLimit, ""); }
  
  /**
   * Set adapter.
   *
   * @param objects           the objects
   * @param maxSelectionLimit the max selection limit
   */
  public void setAdapter(final List<?> objects, final int maxSelectionLimit, final String label){
    listObjects = objects;
    this.maxSelectionLimit = isNullOrEmpty(objects)?0:maxSelectionLimit;
    setLabel(label);
    if(!isShowDialog){
      spinVals.setVisibility(View.VISIBLE);
      llSpinHeader.setVisibility(View.GONE);
      headerSpinnerAdapter = new HeaderSpinnerAdapter((CommonActivity) context, HeaderSpinner.this, spinVals, objects, isMultiSelect ? selObjects : null);
      spinVals.setAdapter(headerSpinnerAdapter);
    }
    if(isShowDialog){
      spinVals.setVisibility(GONE);
      llSpinHeader.setVisibility(VISIBLE);
      HeaderSpinner.this.selObj = (this.maxSelectionLimit <= 0 || listObjects.get(0).toString().equalsIgnoreCase(AppConstants.ALL)) && isNonEmpty(listObjects) ? listObjects.get(0) : null;
      HeaderSpinner.this.selItem = selObj != null ? selObj.toString() : "";
      txtSpinHeader.setText(chkNull(selItem, String.format(context.getString(R.string.header_select), getLabel())));
      llSpinHeader.setOnClickListener(view -> {
        ((CommonActivity) context).dismissCustomAlertDialog();
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialog).create();
        final View rootView = ((CommonActivity) context).getLayoutInflater().inflate(R.layout.dialog_list_search, null);
        final EditText edtSearch = rootView.findViewById(R.id.edt_search);
        final RecyclerView recyclerView = rootView.findViewById(R.id.list_dropdown_dialog);
        recyclerView.setAdapter(new HeaderSpinnerSearchDialogAdapter((CommonActivity) context, HeaderSpinner.this, alertDialog, objects, selObjects, isMultiSelect, this.maxSelectionLimit));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        edtSearch.addTextChangedListener(new TextWatcher(){
          @Override
          public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
          
          @Override
          public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
          
          @Override
          public void afterTextChanged(Editable editable){
            ((HeaderSpinnerSearchDialogAdapter) recyclerView.getAdapter()).getFilter(editable.toString());
          }
        });
        edtSearch.setOnEditorActionListener((textView, actionId, keyEvent) -> {
          if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
            ((CommonActivity) context).hideKeyboard();
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(edtSearch != null)
              inputManager.hideSoftInputFromWindow(edtSearch.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            return true;
          }
          return false;
        });
        alertDialog.setView(rootView);
        alertDialog.setCanceledOnTouchOutside(true);
        if(isMultiSelect)
          alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.btn_ok), (DialogInterface.OnClickListener) null);
        alertDialog.setOnShowListener(dialogInterface -> {
          if(isMultiSelect){
            LayoutParams llParams = new LayoutParams(dp2px(85), dp2px(35));
            int margin = getResources().getDimensionPixelSize(R.dimen.dp_5);
            llParams.setMargins(margin, 0, margin, 0);
            llParams.gravity = Gravity.CENTER_HORIZONTAL;
            
            final Button pos = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            ((LinearLayout) pos.getParent()).setGravity(Gravity.CENTER);
            pos.setLayoutParams(llParams);
          }
          int[] location = new int[2];
          //HeaderSpinner.this.getLocationOnScreen(location);
          llSpinHeader.getLocationOnScreen(location);
          int x = location[0];
          int y = location[1];
          
          WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
          // set the new location [you will need to play with this]
          wlp.x = x;
          wlp.y = y;
          //wlp.width = HeaderSpinner.this.getWidth();
          wlp.width = llSpinHeader.getWidth();
          wlp.gravity = Gravity.TOP | Gravity.LEFT;
          // add to your window
          alertDialog.getWindow().setAttributes(wlp);
        });
        alertDialog.setOnDismissListener(dialogInterface -> {
          final HeaderSpinnerSearchDialogAdapter adapter = ((HeaderSpinnerSearchDialogAdapter) recyclerView.getAdapter());
          HeaderSpinner.this.selObj = chkNull(adapter.getSelObject(), selObj);
          HeaderSpinner.this.selItem = chkNull(adapter.getSelItem(), isMultiSelect ? "" : selItem);
          HeaderSpinner.this.selObjects = isNonEmpty(adapter.getSelItems()) ? adapter.getSelItems() : selObjects;
          if(onItemSelectedListener != null)
            onItemSelectedListener.onItemSelected(null, llSpinHeader, adapter.getItemIndex(selObj), 0);
          txtSpinHeader.setText(chkNull(selItem, String.format(context.getString(R.string.header_select), getLabel())));
          ((CommonActivity) context).hideKeyboard();
        });
        ((CommonActivity) context).customAlertDialog = alertDialog;
        ((CommonActivity) context).customAlertDialog.show();
      });
      txtLabel.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v){
          llSpinHeader.performClick();
        }
      });
    }
  }
  
  /**
   * Set on item selected listener.
   *
   * @param onItemSelectedListener the on item selected listener
   */
  public void setOnItemSelectedListener(final AdapterView.OnItemSelectedListener onItemSelectedListener){
    if(!isShowDialog) spinVals.setOnItemSelectedListener(onItemSelectedListener);
    else{
      HeaderSpinner.this.onItemSelectedListener = onItemSelectedListener;
      if(onItemSelectedListener != null)
        onItemSelectedListener.onItemSelected(null, llSpinHeader, 0, 0);
    }
  }
  
  /**
   * Set selection.
   *
   * @param index the index
   */
  public void setSelection(int index){
    if(index >= 0) setSelection(listObjects.get(index));
  }
  
  /**
   * Set selection.
   *
   * @param obj the obj
   */
  public void setSelection(Object obj){
    if(isShowDialog && obj != null && (!(obj instanceof String) || isNonEmpty(obj.toString()))){
      Set<String> selObjects = obj.toString().trim().length() > 0 ? new HashSet<>(Arrays.asList(obj.toString().split(","))) : null;
      HeaderSpinner.this.selObjects = selObjects == null ? new HashSet<>(0) : selObjects;
      HeaderSpinner.this.selObj = obj;
      HeaderSpinner.this.selItem = isMultiSelect ? isNullOrEmpty(selObjects) || selObjects.contains(AppConstants.ALL) || selObjects.size() >= listObjects.size() - 1 ? AppConstants.ALL : selObjects.toString().replaceFirst("\\[", "").replaceFirst("\\]", "") : obj != null ? obj.toString() : "";
      final int selIndex = selObj != null ? listObjects.indexOf(selObj) : -1;
      if(onItemSelectedListener != null && selObj != null && selIndex >= 0)
        onItemSelectedListener.onItemSelected(null, llSpinHeader, selIndex, 0);
      txtSpinHeader.setText(selItem);
    }
    if(!isShowDialog && spinVals != null && spinVals.getAdapter() != null){
      if(isMultiSelect && obj instanceof String){
        Set<String> selObjects = obj.toString().trim().length() > 0 ? new HashSet<>(Arrays.asList(obj.toString().split(","))) : null;
        selObjects = selObjects == null ? new HashSet<>(0) : selObjects;
        headerSpinnerAdapter.setSelObjects(selObjects);
        return;
      }
      int index = headerSpinnerAdapter.getItemIndex(obj);
      if(index >= 0) spinVals.setSelection(index);
    }
  }
  
  /**
   * Set selection.
   *
   * @param selObjects the sel objects
   */
  public void setSelection(Set<String> selObjects){
    if(isShowDialog && isNonEmpty(selObjects)){
      HeaderSpinner.this.selObjects = selObjects;
      HeaderSpinner.this.selItem = isMultiSelect ? isNullOrEmpty(selObjects) || selObjects.contains(AppConstants.ALL) /*|| selObjects.size() >= listObjects.size() - 1*/ ? AppConstants.ALL : selObjects.toString().replaceFirst("\\[", "").replaceFirst("\\]", "") : selObj != null ? selObj.toString() : "";
      txtSpinHeader.setText(selItem);
    }
    if(!isShowDialog && spinVals != null && spinVals.getAdapter() != null){
      headerSpinnerAdapter.setSelObjects(selObjects);
    }
  }
  
  /**
   * Get selected item string.
   *
   * @return the string
   */
  public String getSelectedItem(){
    //if(isMultiSelect && maxSelectionLimit > 0 && selItem.equalsIgnoreCase(AppConstants.ALL))
    //return getAllStr();
    return isShowDialog ? selItem : spinVals != null && spinVals.getAdapter() != null ? headerSpinnerAdapter.getSelItem() : "";
  }
  
  /**
   * Get selected object object.
   *
   * @return the object
   */
  public Object getSelectedObject(){
    return isShowDialog ? selObj : spinVals != null && spinVals.getAdapter() != null ? headerSpinnerAdapter.getSelObject() : null;
  }
  
  /**
   * Get all str string.
   *
   * @return the string
   */
  private String getAllStr(){
    String all = "";
    if(isMultiSelect && maxSelectionLimit > 0 && (isNullOrEmpty(selObjects) || selObjects.contains(AppConstants.ALL) || selItem.equalsIgnoreCase(AppConstants.ALL)))
      for(Object obj : listObjects)
        if(obj != null && !chkNull(obj.toString(), AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
          all += "," + obj.toString();
    return (all.startsWith(",") ? all.substring(1) : all).replaceAll("\\s*,\\s*", ",").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim();
  }
  
  /**
   * Get selected vals set.
   *
   * @return the set
   */
  public Set<String> getSelectedVals(){
    //if(isMultiSelect && maxSelectionLimit > 0 && (isNullOrEmpty(selObjects) || selObjects.contains(AppConstants.ALL) || selItem.equalsIgnoreCase(AppConstants.ALL)))
    //return new HashSet<String>(Arrays.asList(getAllStr().split(",")));
    return isShowDialog ? isNonEmpty(selObjects) && selObjects.contains(AppConstants.ALL) ? new HashSet<String>() : selObjects : spinVals != null && spinVals.getAdapter() != null && isMultiSelect ? headerSpinnerAdapter.getSelItems() : null;
  }
  
  /**
   * Validate boolean.
   *
   * @return the boolean
   */
  public boolean validate(){
    final String selItem = getSelectedItem().trim();
    final boolean isEmpty = isNullOrEmpty(selItem);
    if(isEmpty || selItem.matches("(?i)(^\\s*Select.*$)")){
      ((CommonActivity) context).showCustomErrDialog("Please " + (isEmpty ? "Select " + getLabel().trim() : selItem));
      return false;
    }
    return true;
  }
  
  /**
   * Get max selection limit int.
   *
   * @return the int
   */
  public int getMaxSelectionLimit(){ return isMultiSelect ? chkNull(maxSelectionLimit, 0) : 0; }
  
  /**
   * Set max selection limit.
   *
   * @param maxSelectionLimit the max selection limit
   */
  public void setMaxSelectionLimit(int maxSelectionLimit){ this.maxSelectionLimit = isMultiSelect ? maxSelectionLimit : 0; }
  
  /**
   * Get all items.
   *
   * @return the all items
   */
  public String getAllItems(){
    if(isMultiSelect && getSelectedItem().equalsIgnoreCase(AppConstants.ALL)){
      List<?> temp = new ArrayList<>(listObjects);
      if(temp.contains(AppConstants.ALL)) temp.remove(AppConstants.ALL);
      return temp.toString().replaceFirst("\\[", "").replaceFirst("\\]", "");
    }
    else return chkNull(getSelectedItem(), AppConstants.ALL);
  }
  
  /**
   * Get list objects size.
   *
   * @return the list object size
   */
  public int getListObjectSize(){
    if(isNonEmpty(listObjects)){
      List<?> temp = new ArrayList<>(listObjects);
      if(temp.contains(AppConstants.ALL)) temp.remove(AppConstants.ALL);
      return temp.size();
    }
    return -1;
  }
}
