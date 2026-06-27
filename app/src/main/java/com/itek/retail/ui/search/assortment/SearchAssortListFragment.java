package com.itek.retail.ui.search.assortment;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.ProductListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.FragmentSearchAssortmentListBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.listsearch.SearchListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Replenishment list fragment.
 */
public class SearchAssortListFragment extends CommonFragment{
  
  public FragmentSearchAssortmentListBinding binding;
  ProductDao productDao;
  private List<String> listBrands = new ArrayList<>(0);
  private List<String> listCategories = new ArrayList<>(0);
  private List<ProductModel> listProducts = new ArrayList<>(0);
  private SearchListViewModel mViewModel;
  private String searchAssortmentListType = "";
  private String searchAssortmentListId = "0";
  private String searchAssortmentCode = "0";
  private String searchAssortmentPriority = "0";
  
  /**
   * Instantiates a new Replenishment list fragment.
   */
  public SearchAssortListFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(SearchListViewModel.class);
    binding = FragmentSearchAssortmentListBinding.inflate(inflater, container, false);
    searchAssortmentListId = extractString(getArguments(), AppConstants.SEARCH_LIST_ID, "0");
    searchAssortmentListType = extractString(getArguments(), AppConstants.SEARCH_LIST_TYPE, "");
    searchAssortmentCode = extractString(getArguments(), AppConstants.SEARCH_ASSORTMENT_CODE, "");
    searchAssortmentPriority = extractString(getArguments(), AppConstants.SEARCH_ASSORTMENT_PRIORITY, "0");
    
    listBrands.clear();
    listBrands.add(AppConstants.ALL);
    List<String> brands = productDao.getAssortBrandList(searchAssortmentCode);
    if(isNonEmpty(brands)) listBrands.addAll(brands);
    
    listCategories.clear();
    listCategories.add(AppConstants.ALL);
    List<String> categories = productDao.getAssortCategoryList(searchAssortmentCode);
    if(isNonEmpty(categories)) listCategories.addAll(categories);
    
    binding.spinBrand.setAdapter(listBrands);
    binding.spinCategory.setAdapter(listCategories);
    
    binding.listSearchProducts.setAdapter(new ProductListAdapter((MainActivity) context, SearchAssortListFragment.this, listProducts));
    binding.listSearchProducts.setLayoutManager(/*isLandscape?new GridLayoutManager(context,2) :*/new LinearLayoutManager(context));

    binding.edtSearchName.setHint(String.format(context.getString(R.string.hint_search_by__), SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name))+"/"+SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean))));
    binding.edtSearchName.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void afterTextChanged(Editable editable){ updateLists(); }
    });
    
    binding.edtSearchName.setOnEditorActionListener(new TextView.OnEditorActionListener(){
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent){
        if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
          hideKeyboard();
          return true;
        }
        return false;
      }
    });
    
    binding.spinBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
    });
    
    binding.spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        showLog("ReplenishList_Category", "OnItemSelected");
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
    });
    
    updateLists();
    
    return binding.getRoot();
  }
  
  /**
   * Get sel brand string.
   *
   * @return the string
   */
  public String getSelBrand(){ return chkNull(binding.spinBrand.getSelectedItem(), AppConstants.ALL); }
  
  /**
   * Get sel category string.
   *
   * @return the string
   */
  public String getSelCategory(){ return chkNull(binding.spinCategory.getSelectedItem(), AppConstants.ALL); }
  
  /**
   * Get search list id string.
   *
   * @return the string
   */
  public String getSearchAssortmentListId(){
    return searchAssortmentListId;
  }
  
  /**
   * Get search list type string.
   *
   * @return the string
   */
  public String getSearchAssortmentListType(){
    return searchAssortmentListType;
  }
  
  public String getSearchAssortmentCode(){
    return searchAssortmentCode;
  }
  
  public String getSearchAssortmentPriority(){
    return searchAssortmentPriority;
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()) updateLists();
  }
  
  @Override
  public void onBackPressed(){
    super.onBackPressed();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    final String selBrand = binding.spinBrand.getSelectedItem();
    final Set<String> selBrands = binding.spinBrand.getSelectedVals();
    final String selCategory = binding.spinCategory.getSelectedItem();
    final Set<String> selCategories = binding.spinCategory.getSelectedVals();
    final String searchName = chkNull(binding.edtSearchName.getText().toString(), "").trim();
    listProducts.clear();
    listProducts.addAll(productDao.getAssortProducts(searchAssortmentCode, selCategory, selCategories, selBrand, selBrands, searchName));
    ((RecyclerView.Adapter) binding.listSearchProducts.getAdapter()).notifyDataSetChanged();
    final Integer totalEanQty = productDao.getAssortTotalEANCount(searchAssortmentCode, selCategory, selCategories, selBrand, selBrands, searchName);
    binding.txtTotal.setText(String.format(getString(R.string.txt_movement_replenishment_total), chkZero(totalEanQty, "0")));
  }
}