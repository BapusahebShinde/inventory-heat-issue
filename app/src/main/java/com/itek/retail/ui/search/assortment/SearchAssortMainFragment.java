package com.itek.retail.ui.search.assortment;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.AssortmentCodeListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.FragmentSearchAssortmentMainBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.home.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * The Replenishment list fragment.
 */
public class SearchAssortMainFragment extends CommonFragment{
  
  public FragmentSearchAssortmentMainBinding binding;
  ProductDao productDao;
  private List<ProductModel> listProducts = new ArrayList<>(0);
  private SearchAssortMainViewModel mViewModel;
  private String sortByValues = "";
  private String searchAssortmentListType = "";
  private String searchAssortmentListId = "0";
  
  /**
   * Instantiates a new Replenishment list fragment.
   */
  public SearchAssortMainFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(SearchAssortMainViewModel.class);
    binding = FragmentSearchAssortmentMainBinding.inflate(inflater, container, false);
    searchAssortmentListId = extractString(getArguments(), AppConstants.SEARCH_LIST_ID, "0");
    searchAssortmentListType = extractString(getArguments(), AppConstants.SEARCH_LIST_TYPE, "");
    
    binding.listAssortmentCodes.setAdapter(new AssortmentCodeListAdapter((MainActivity) context, SearchAssortMainFragment.this, listProducts));
    binding.listAssortmentCodes.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));

    binding.edtAssortmentCode.setHint(String.format(context.getString(R.string.hint_search_by__), SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name))+"/"+SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean))));
    binding.edtAssortmentCode.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void afterTextChanged(Editable editable){ updateLists(); }
    });
    
    setHeader();
    
    binding.edtAssortmentCode.setOnEditorActionListener(new TextView.OnEditorActionListener(){
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent){
        if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
          hideKeyboard();
          return true;
        }
        return false;
      }
    });
    
    updateLists();
    
    return binding.getRoot();
  }
  
  /**
   * Set header.
   */
  public void setHeader(){
    final LinearLayout llHeader = binding.llListHeader.llAssortCode;
    final int childCount = llHeader.getChildCount();
    if(childCount > 0) for(int i = 0; i < childCount; i++){
      final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
      if(sortView != null){
        sortView.setOnClickListener(new View.OnClickListener(){
          @Override
          public void onClick(View view){
            if(llHeader.getVisibility() != View.VISIBLE) return;
            final SortHeaderView sortView = view != null && view instanceof SortHeaderView ? (SortHeaderView) view : null;
            if(sortView != null){
              resetHeader(sortView.getId());
              
              setSortBy(sortView.getSortColumn(), sortView.getSortOrder());
            }
          }
        });
      }
    }
  }
  
  /**
   * Reset header.
   *
   * @param viewId the view id
   */
  public void resetHeader(@IdRes final int viewId){
    final LinearLayout llHeader = binding.llListHeader.llAssortCode;
    final int childCount = llHeader.getChildCount();
    if(childCount > 0) for(int i = 0; i < childCount; i++){
      final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
      if(sortView != null){
        if(viewId != 0 && sortView.getId() == viewId) sortView.updateDescOrder();
        else sortView.reset();
      }
    }
  }
  
  /**
   * Set sort by.
   *
   * @param column  the column
   * @param orderBy the order by
   */
  private void setSortBy(String column, String orderBy){
    sortByValues = isNonEmpty(column) && isNonEmpty(orderBy)?column + " " + orderBy:"";
    updateLists();
  }
  
  public void callDetailsAPI(final ProductModel productModel){
    //selProd = productModel;
    try{
      Bundle args = getArguments();
      args.putSerializable(productModel.getClass().getSimpleName(), productModel);
      args.putString(AppConstants.SEARCH_ASSORTMENT_CODE, productModel.getOrderNo());
      args.putInt(AppConstants.SEARCH_ASSORTMENT_PRIORITY, productModel.getPriority());
      context.loadFragment(new SearchAssortListFragment(), args);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()) updateLists();
  }
  
  @Override
  public void onBackPressed(){
    context.showCustomAlertDialog("", R.string.msg_assort_search_back, R.string.btn_no, null, R.string.btn_yes, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        productDao.deleteAllExcept();
        AppDatabase.getInventoryDao(context).deleteAllExcept();
        popBackStack();
      }
    });
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    final String searchName = chkNull(binding.edtAssortmentCode.getText().toString(), "").trim();
    listProducts.clear();
    listProducts.addAll(productDao.searchAssortHeader(searchName, sortByValues));
    if(binding != null && binding.listAssortmentCodes != null && binding.listAssortmentCodes.getAdapter() != null && binding.listAssortmentCodes.getAdapter() instanceof RecyclerView.Adapter)
      ((RecyclerView.Adapter) binding.listAssortmentCodes.getAdapter()).notifyDataSetChanged();
  }
  
}