package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.itek.retail.R;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.ui.customviews.HeaderSpinner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Header spinner adapter is a Spinner/Dropdown Adapter
 * used in Header Spinner View
 * for showing the Dropdown List in the Spinner
 * list will be shown as multiple checkable options if value 'isMultiSelect' is true
 */
public class HeaderSpinnerAdapter extends ArrayAdapter{
  
  private CommonActivity context;
  private HeaderSpinner head;
  private List<?> listObjects = new ArrayList<>(0);
  private List<Object> listFilteredObjects = new ArrayList<>(0);
  private Set<String> selObjects = new HashSet<>(0);
  private boolean isMultiSelect = false;
  private String selItem;
  private Object selObj;
  private Spinner spin;
  private boolean isHideDropdownSymbol = false;
  private Class objClass;
  private boolean isShowSearch = false;
  
  /**
   * Instantiates a new Header spinner adapter.
   *
   * @param context    the context
   * @param head       the head
   * @param spinner    the spinner
   * @param objects    the objects
   * @param selObjects the sel objects
   */
  public HeaderSpinnerAdapter(@NonNull CommonActivity context, HeaderSpinner head, Spinner spinner, List<?> objects, Set<String> selObjects){
    super(context, R.layout.spin_header, R.id.txt_spin_header, objects);
    this.context = context;
    this.head = head;
    this.spin = spinner;
    this.listObjects = objects;
    listFilteredObjects.addAll(listObjects);
    if(isNonEmpty(objects)) objClass = objects.getClass();
    this.selObjects = selObjects != null ? selObjects : new HashSet<>(0);
    this.isMultiSelect = selObjects != null;
  }
  
  /**
   * Set sel objects.
   *
   * @param selObjects the sel objects
   */
  public void setSelObjects(Set<String> selObjects){
    this.selObjects = selObjects;
  }
  
  @Override
  public int getCount(){ return listFilteredObjects.size() + (isShowSearch ? 1 : 0); }
  
  @Nullable
  @Override
  public Object getItem(int position){ return position >= 0 && position < getCount() ? listFilteredObjects.get(position) : null; }
  
  @Override
  public long getItemId(int position){ return position; }
  
  @Override
  public View getDropDownView(final int position, View convertView, ViewGroup parent){
    convertView = context.getLayoutInflater().inflate(position == 0 && isShowSearch ? R.layout.view_edit_search : isMultiSelect ? R.layout.spin_dropdown_multiselect : R.layout.spin_dropdown, null);
    if(position >= 0 && position < getCount()){
      if(position == 0 && isShowSearch){
        final EditText edtSearch = convertView.findViewById(R.id.edt_search);
        edtSearch.addTextChangedListener(new TextWatcher(){
          @Override
          public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){/** unused **/}
          
          @Override
          public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){/** unused **/}
          
          @Override
          public void afterTextChanged(Editable editable){
            HeaderSpinnerAdapter.this.getFilter(editable.toString());
          }
        });
      }
      else{
        final String item = getItem(position - (isShowSearch ? 1 : 0)).toString();
        if(isMultiSelect){
          final CheckBox chkItem = convertView.findViewById(R.id.chk_spin_dropdown);
          final boolean isChecked = selObjects.contains(AppConstants.ALL) || selObjects.contains(item);
          chkItem.setText(item);
          chkItem.setChecked(isChecked);
          if(chkItem.isChecked()) showLog(chkItem.getText().toString(), "" + chkItem.isChecked());
          chkItem.setOnClickListener(v -> {
            if(item.equalsIgnoreCase(AppConstants.ALL)){
              if(selObjects.contains(AppConstants.ALL)) selObjects.clear();
              else if(!selObjects.contains(AppConstants.ALL)){
                for(Object o : listObjects){
                  selObjects.add(o.toString());
                }
              }
            }
            else if(selObjects.contains(item)){
              selObjects.remove(item);
              if(selObjects.contains(AppConstants.ALL)) selObjects.remove(AppConstants.ALL);
            }
            else if(!selObjects.contains(item)){
              selObjects.add(item);
              if(selObjects.size() >= listObjects.size() - 1 && !selObjects.contains(AppConstants.ALL))
                selObjects.add(AppConstants.ALL);
            }
            if(spin.getSelectedItemPosition() == position) spin.setSelection(-1);
            spin.setSelection(position);
            HeaderSpinnerAdapter.this.notifyDataSetChanged();
          });
        }
        else{
          final TextView txtItem = convertView.findViewById(R.id.txt_spin_dropdown);
          txtItem.setText(item);
        }
      }
    }
    return convertView;
  }
  
  @Override
  public View getView(final int position, View convertView, ViewGroup parent){
    convertView = LayoutInflater.from(context).inflate(R.layout.spin_header, null);
    if(position >= 0 && position < getCount()){
      final TextView txtValue = convertView.findViewById(R.id.txt_spin_header);
      txtValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, !isHideDropdownSymbol ? R.drawable.ic_dropdown_arrow : 0, 0);
      selObj = getItem(position);
      final String item = selObj != null ? selObj.toString() : "";
      if(isMultiSelect)
        txtValue.setText(selObjects.isEmpty() || selObjects.contains(AppConstants.ALL) || selObjects.size() >= listObjects.size() - 1 ? AppConstants.ALL : selObjects.toString().replaceFirst("\\[", "").replaceFirst("\\]", ""));
      else txtValue.setText(item);
      selItem = isMultiSelect ? txtValue.getText().toString() : item;
    }
    return convertView;
  }
  
  /**
   * Get sel object object.
   *
   * @return the object
   */
  public Object getSelObject(){ return selItem; }
  
  /**
   * Get sel item string.
   *
   * @return the string
   */
  public String getSelItem(){ return chkNull(selItem, ""); }
  
  /**
   * Get sel items set.
   *
   * @return the set
   */
  public Set<String> getSelItems(){ return selObjects.isEmpty() || selObjects.contains(AppConstants.ALL) || selObjects.size() >= listObjects.size() - 1 ? new HashSet<String>(0) : selObjects; }
  
  /**
   * Get list objects array list.
   *
   * @return the array list
   */
  public List<?> getListObjects(){ return listObjects; }
  
  /**
   * Get item index int.
   *
   * @param obj the obj
   * @return the int
   */
  public int getItemIndex(Object obj){ return listObjects.indexOf(obj); }
  
  /**
   * Is hide dropdown symbol boolean.
   *
   * @return the boolean
   */
  public boolean isHideDropdownSymbol(){ return isHideDropdownSymbol; }
  
  /**
   * Set hide dropdown symbol.
   *
   * @param hideDropdownSymbol the hide dropdown symbol
   */
  public void setHideDropdownSymbol(boolean hideDropdownSymbol){ isHideDropdownSymbol = hideDropdownSymbol; }
  
  /**
   * Gets filter.
   *
   * @param filterStr the char sequence
   * @return the filter
   */
  public List<?> getFilter(String filterStr){
    listFilteredObjects.clear();
    if(isNullOrEmpty(filterStr)){
      listFilteredObjects.addAll(listObjects);
    }
    else{
      for(Object data : listObjects){
        if(data.toString().toLowerCase().contains(filterStr.toLowerCase()))
          listFilteredObjects.add(data);
      }
      if(isNonEmpty(listFilteredObjects) && listFilteredObjects.contains(AppConstants.ALL))
        listFilteredObjects.remove(AppConstants.ALL);
      listFilteredObjects.add(0, AppConstants.ALL);
    }
    HeaderSpinnerAdapter.this.notifyDataSetChanged();
    return listFilteredObjects;
  }
}
