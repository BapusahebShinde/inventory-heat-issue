package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.databinding.ListSpinDropdownBinding;
import com.itek.retail.ui.customviews.HeaderSpinner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Header spinner search dialog adapter.
 * used in HeaderSpinner Search Dialog
 * for Performing Searchable/Filterable List
 * list will be shown as multiple checkable options if value 'isMultiSelect' is true
 */
public class HeaderSpinnerSearchDialogAdapter extends RecyclerView.Adapter<HeaderSpinnerSearchDialogAdapter.ViewHolder>{
  
  private CommonActivity context;
  private HeaderSpinner headerSpinner;
  private AlertDialog alertDialog;
  private List<?> listObjects = new ArrayList<>(0);
  private List<Object> listFilteredObjects = new ArrayList<>(0);
  private Set<String> selObjects = new HashSet<>(0);
  private boolean isMultiSelect = false;
  private int maxSelectionLimit = 0;
  private String selItem;
  private Object selObj;
  private Class objClass;
  private String headerItem;
  
  /**
   * Instantiates a new Header spinner 1 adapter.
   *
   * @param context           the context
   * @param headerSpinner     the header spinner
   * @param alertDialog       the alert dialog
   * @param objects           the objects
   * @param selObjects        the sel objects
   * @param isMultiSelect     the is multi select
   * @param maxSelectionLimit the max Selection Limit
   */
  public HeaderSpinnerSearchDialogAdapter(@NonNull CommonActivity context, HeaderSpinner headerSpinner, AlertDialog alertDialog, List<?> objects, Set<String> selObjects, boolean isMultiSelect, int maxSelectionLimit){
    this.context = context;
    this.headerSpinner = headerSpinner;
    this.alertDialog = alertDialog;
    this.listObjects = objects;
    this.headerItem = isNonEmpty(objects) ? objects.get(0).toString() : "";
    this.isMultiSelect = isMultiSelect;
    this.maxSelectionLimit = isMultiSelect ? maxSelectionLimit : 0;
    if(!(headerItem.equalsIgnoreCase(AppConstants.ALL) || headerItem.matches("(?i)^Select.*")))
      this.headerItem = "";
    listFilteredObjects.addAll(listObjects);
    if(isNonEmpty(objects)) objClass = objects.getClass();
    this.selObjects = selObjects != null ? selObjects : new HashSet<>(0);
  }
  
  /**
   * Set sel objects.
   *
   * @param selObjects the sel objects
   */
  public void setSelObjects(Set<String> selObjects){
    this.selObjects = selObjects;
  }
  
  /**
   * Get sel object object.
   *
   * @return the object
   */
  public Object getSelObject(){ return selObj; }
  
  /**
   * Get sel item string.
   *
   * @return the string
   */
  public String getSelItem(){ return isMultiSelect ? selObjects.isEmpty() || selObjects.contains(AppConstants.ALL) || selObjects.size() >= (listObjects.size() > 1 ? listObjects.size() : listObjects.size() - 1) ? isNonEmpty(headerItem) && headerItem.equalsIgnoreCase(AppConstants.ALL) ? AppConstants.ALL : "" : selObjects.toString().replaceFirst("\\[", "").replaceFirst("\\]", "") : chkNull(selItem, ""); }
  
  /**
   * Get sel items set.
   *
   * @return the set
   */
  public Set<String> getSelItems(){ return selObjects.isEmpty() || selObjects.contains(AppConstants.ALL) || selObjects.size() >= (listObjects.size() > 1 ? listObjects.size() : (listObjects.size() > 1 ? listObjects.size() : listObjects.size() - 1)) ? new HashSet<String>(0) : selObjects; }
  
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
      if(isNonEmpty(listFilteredObjects) && isNonEmpty(headerItem) && listFilteredObjects.contains(headerItem))
        listFilteredObjects.remove(headerItem);
      if(isNonEmpty(headerItem)) listFilteredObjects.add(0, headerItem);
    }
    HeaderSpinnerSearchDialogAdapter.this.notifyDataSetChanged();
    return listFilteredObjects;
  }
  
  @Override
  @NonNull
  public HeaderSpinnerSearchDialogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new HeaderSpinnerSearchDialogAdapter.ViewHolder(ListSpinDropdownBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), isMultiSelect);
  }
  
  @Override
  public int getItemCount(){ return listFilteredObjects.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull HeaderSpinnerSearchDialogAdapter.ViewHolder holder, final int position){
    final Object obj = listFilteredObjects.get(position);
    final String item = obj != null ? chkNull(obj.toString().trim(), context.getString(R.string.default_no_value)).trim() : context.getString(R.string.default_no_value);
    holder.setText(item);
    if(isMultiSelect){
      holder.chkItem.setChecked(selObjects.contains(AppConstants.ALL) || selObjects.contains(item.trim()));
    }
    holder.itemView.setOnClickListener(view -> {
      context.hideKeyboard();
      if(isMultiSelect){
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
          if(maxSelectionLimit > 0 && selObjects.size() >= maxSelectionLimit){
            context.showShortToast(String.format(context.getString(R.string.max_select_limit_reached), maxSelectionLimit + " " + headerSpinner.getLabel()));
            return;
          }
          selObjects.add(item);
          if(selObjects.size() >= (listObjects.size() > 1 ? listObjects.size() : listObjects.size() - 1) && listObjects.contains(AppConstants.ALL) && !selObjects.contains(AppConstants.ALL))
            selObjects.add(AppConstants.ALL);
        }
        selObj = obj;
        selItem = selObjects.isEmpty() || selObjects.contains(AppConstants.ALL) || selObjects.size() >= (listObjects.size() > 1 ? listObjects.size() : listObjects.size() - 1) ? AppConstants.ALL : selObjects.toString().replaceFirst("\\[", "").replaceFirst("\\]", "");
        HeaderSpinnerSearchDialogAdapter.this.notifyDataSetChanged();
      }
      else{
        selObj = obj;
        selItem = item;
        alertDialog.dismiss();
        //dismiss Dialog
      }
    });
  }
  
  /**
   * The View holder.
   */
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    CheckBox chkItem;
    TextView txtItem;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding       the binding
     * @param isMultiSelect the is multi select
     */
    ViewHolder(ListSpinDropdownBinding binding, boolean isMultiSelect){
      super(binding.getRoot());
      chkItem = binding.chkSpinDropdown;
      txtItem = binding.txtSpinDropdown;
      chkItem.setVisibility(isMultiSelect ? View.VISIBLE : View.GONE);
      txtItem.setVisibility(!isMultiSelect ? View.VISIBLE : View.GONE);
    }
    
    /**
     * Set text.
     *
     * @param sequence the sequence
     */
    public void setText(CharSequence sequence){
      chkItem.setText(sequence);
      txtItem.setText(sequence);
    }
  }
}
