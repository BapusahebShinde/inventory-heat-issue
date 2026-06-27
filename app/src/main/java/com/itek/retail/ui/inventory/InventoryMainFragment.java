package com.itek.retail.ui.inventory;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.CustomTypefaceSpan;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.BrandWiseZoneInventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.ZoneDao;
import com.itek.retail.databinding.FragmentInventoryMainBinding;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Inventory main fragment.
 */
public class InventoryMainFragment extends CommonFragment{
  
  ZoneDao zoneDao;
  BrandWiseZoneInventoryDao brandWiseZoneInventoryDao;
  private List<String> listLocations = new ArrayList<>(0);
  private List<String> listBrands = new ArrayList<>(0);
  private List<String> listCategory = new ArrayList<>(0);
  private InventoryMainViewModel mViewModel;
  private MainViewModel mainViewModel;
  private FragmentInventoryMainBinding binding;
  final AdapterView.OnItemSelectedListener onItemSelected = new AdapterView.OnItemSelectedListener(){
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
      if(binding != null){
        final String location = binding.spinInventoryStockLocation.getSelectedItem();
        final Set<String> locations = binding.spinInventoryStockLocation.getSelectedVals();
        final String brand = binding.spinInventoryStockBrand.getSelectedItem();
        final Set<String> brands = binding.spinInventoryStockBrand.getSelectedVals();
        final String category = binding.spinInventoryStockCategory.getSelectedItem();
        final Set<String> categories = binding.spinInventoryStockCategory.getSelectedVals();
        final String counts = brandWiseZoneInventoryDao.getCounts(location, locations, brand, brands, category, categories);
        if(counts != null && counts.length() > 1 && counts.contains(",") && counts.matches("[0-9].*,[0-9].*")){
          binding.ddvInvCurrent.setText(counts.split(",")[0]);
          binding.ddvInvShortage.setText(counts.split(",")[1]);
        }
        else if(counts == null){
          binding.ddvInvCurrent.setText("0");
          binding.ddvInvShortage.setText("0");
        }
      }
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent){
      /*Empty Method (Default Overridden)*/
    }
  };
  
  /**
   * Instantiates a new Inventory main fragment.
   */
  public InventoryMainFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    mainViewModel = ((MainActivity) context).getRfidViewModel();
    zoneDao = AppDatabase.getZoneDao(context);
    brandWiseZoneInventoryDao = AppDatabase.getBrandWiseZoneInventoryDao(context);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(InventoryMainViewModel.class);
    binding = FragmentInventoryMainBinding.inflate(inflater, container, false);
    
    binding.swipeLayout.setColorSchemeColors(context.getColorPrimaryDarkFromTheme());
    binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
      @Override
      public void onRefresh(){
        binding.swipeLayout.setRefreshing(false);
        callAPI();
      }
    });
    
    listLocations = zoneDao.getAllZones();
    if(listLocations != null) listLocations.add(0, AppConstants.ALL);
    binding.spinInventoryStockLocation.setAdapter(listLocations);
    
    binding.header.imgConfigSync.setVisibility(View.VISIBLE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_sync);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        callAPI();
      }
    });
    
    if(savedInstanceState == null){
      binding.spinInventoryStockBrand.setEnabled(isNonEmpty(listBrands));
      listBrands.add(0, AppConstants.ALL);
      binding.spinInventoryStockBrand.setAdapter(listBrands);
      
      binding.spinInventoryStockCategory.setVisibility(isNonEmpty(listCategory) ? View.VISIBLE : View.GONE);
      binding.spinInventoryStockCategory.setEnabled(isNonEmpty(listCategory));
      listCategory.add(0, AppConstants.ALL);
      binding.spinInventoryStockCategory.setAdapter(listCategory);
      
      brandWiseZoneInventoryDao.deleteAll();
    }
    
    binding.ddvInvShortage.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        final View btnStockCorrect = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_STOCK_CORRECT);
        if(btnStockCorrect != null) btnStockCorrect.performClick();
      }
    });
    //commented by Bapu Shinde on 07-09-2023 to avoid long time wait to user to start inventory
    //if(savedInstanceState == null) callAPI();
    
    //code for setting label
    final String msgInvRefresh = getString(R.string.msg_refresh_dashboard);
    int index = msgInvRefresh.indexOf("'");
    int indexEnd = msgInvRefresh.lastIndexOf("'") - 1;
    SpannableString ss = new SpannableString(msgInvRefresh.replaceAll("\'", ""));
    ss.setSpan(new CustomTypefaceSpan("", ResourcesCompat.getFont(context, R.font.font_awesome)), index, indexEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    ss.setSpan(new StyleSpan(Typeface.BOLD), indexEnd, indexEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    ss.setSpan(new RelativeSizeSpan(2f), indexEnd, indexEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    //ss.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.red)),indexEnd,indexEnd,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    binding.lblRefresh.setText(ss);
    
    //binding.lblRefresh.setText(HtmlCompat.fromHtml(getString(R.string.msg_refresh_inv_dashboard), HtmlCompat.FROM_HTML_MODE_LEGACY));
    return binding.getRoot();
  }
  
  @Override
  public void onResume(){
    super.onResume();
    //commented by Bapu Shinde on 07-09-2023 to avoid long time wait to user to start inventory
    if(isTopInStack() && SharedPrefManager.getBoolean(ParamConstants.IS_DASHBOARD_AUTO_REFRESH, false))
      callAPI();
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    callWebService(URLConstants.GET_INVENTORY_DASHBOARD, new JSONObject(), getString(R.string.progress_msg_getting_data), false, true);
  }
  
  /**
   * Set active users.
   *
   * @param activeUsers the active users
   */
  private void setActiveUsers(final int activeUsers){
    binding.header.flActiveDevices.setVisibility(activeUsers >= -1 ? View.VISIBLE : View.GONE);
    binding.header.btnActiveDevices.setSelected(true);
    binding.header.btnActiveDevices.setText(activeUsers >= 0 ? "" + activeUsers : "");
  }
  
  public void updateInvCount(){
    listBrands.clear();
    listBrands.addAll(brandWiseZoneInventoryDao.getBrands());
    binding.spinInventoryStockBrand.setEnabled(isNonEmpty(listBrands));
    listBrands.add(0, AppConstants.ALL);
    binding.spinInventoryStockBrand.setAdapter(listBrands);
    
    listCategory.clear();
    listCategory.addAll(brandWiseZoneInventoryDao.getCategories());
    binding.spinInventoryStockCategory.setVisibility(isNonEmpty(listCategory) ? View.VISIBLE : View.GONE);
    binding.spinInventoryStockCategory.setEnabled(isNonEmpty(listCategory));
    listCategory.add(0, AppConstants.ALL);
    binding.spinInventoryStockCategory.setAdapter(listCategory);
    
    binding.spinInventoryStockLocation.setOnItemSelectedListener(onItemSelected);
    binding.spinInventoryStockBrand.setOnItemSelectedListener(onItemSelected);
    binding.spinInventoryStockCategory.setOnItemSelectedListener(onItemSelected);
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_INVENTORY_DASHBOARD:
          if(isSuccess && jsonResponse != null){
            final String lastInventoryDate = extractString(jsonResponse, ParamConstants.LAST_INVENTORY_TAKEN_TIME, "T").replaceFirst("T", " ").trim();
            SharedPrefManager.setString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, lastInventoryDate);
            binding.txtInventoryStockUpdateDate.setText(HtmlCompat.fromHtml(isNonEmpty(lastInventoryDate) && lastInventoryDate.trim().contains(" ") && lastInventoryDate.trim().split(" ").length > 1 && lastInventoryDate.length() >= 10 ? String.format(getString(R.string.txt_inventory_stock_date_time), lastInventoryDate.split(" ")[0], lastInventoryDate.split(" ")[1]) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
            binding.txtInventoryStockUpdateDate.setVisibility(chkNull(lastInventoryDate, "").length() >= 10 ? View.VISIBLE : View.INVISIBLE);
            JSONArray inventoryCounts = extractJSONArray(jsonResponse, ParamConstants.BRAND_INVENTORY_COUNTS, extractJSONArray(jsonResponse, ParamConstants.CATEGORY_INVENTORY_COUNTS, extractJSONArray(jsonResponse, ParamConstants.INVENTORY_COUNTS)));
            //JSONArray  brandwise= extractJSONArray(jsonResponse, ParamConstants.BRAND_INVENTORY_COUNTS);
            brandWiseZoneInventoryDao.deleteAll();
            if(inventoryCounts != null && context instanceof MainActivity)
              ((MainActivity) context).callInsertBrandwiseDBTask(this, url, jsonRequest, jsonResponse, args, inventoryCounts);
            else{
              hideProgressDialog();
              updateInvCount();
            }
          }
          setActiveUsers(extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, -2));
          break;
        case URLConstants.GET_STOCK_CORRECTION_DASHBOARD:
          if(isSuccess && jsonResponse != null){
            JSONArray shortageProductsArray = extractJSONArray(jsonResponse, ParamConstants.SHORTAGE_PRODUCTS);
            final ProductDao productDao = AppDatabase.getProductDao(context);
            productDao.deleteAllExcept();
            AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.STOCK_CORRECTION.getValue());
            if(shortageProductsArray != null && context instanceof MainActivity)
              ((MainActivity) context).callInsertProductDBTask(url, AppCommonMethods.SessionType.STOCK_CORRECTION.getValue(), jsonResponse, args, shortageProductsArray);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}

