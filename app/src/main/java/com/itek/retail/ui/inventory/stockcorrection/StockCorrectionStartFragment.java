package com.itek.retail.ui.inventory.stockcorrection;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT_PATTERN;
import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.OmniPickedListAdapter;
import com.itek.retail.adapter.ProductListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.DialogOmniEpcSearchBinding;
import com.itek.retail.databinding.DialogSimilarStylesBinding;
import com.itek.retail.databinding.FragmentStockCorrectionStartBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.customviews.DashboardDataView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The Stock correction start fragment.
 */
public class StockCorrectionStartFragment extends RFIDSessionFragment{
  
  //temp flags
  boolean isStartSessionImmediately = true;
  boolean isCallUploadForAll = true;
  
  List<ProductModel> listProductsFoundShortage = new ArrayList<>(0);
  private StockCorrectionStartViewModel mViewModel;
  private FragmentStockCorrectionStartBinding binding;
  private ProductDao productDao;
  private InventoryDao inventoryDao;
  private boolean isShowFoundList = false;
  private int shortage = 0, found = 0;
  private DialogOmniEpcSearchBinding dialogOmniEpcSearchBinding;
  private AlertDialog styleChartAlert;
  private DialogSimilarStylesBinding dialogSimilarStylesBinding;
  private boolean showMarkFoundBtn = false;
  
  /**
   * Instantiates a new Stock correction start fragment.
   */
  public StockCorrectionStartFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    if(isAllowDirectionalSearch) mainViewModel.getSensorAndStart();
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(StockCorrectionStartViewModel.class);
    binding = FragmentStockCorrectionStartBinding.inflate(inflater, container, false);
    
    if(sessionObject != null)
      setSessionAction(AppConstants.SESSION_ACTION_RESUME, null, null, null, false);
    else if(sessionObject == null && isStartSessionImmediately)
      setSessionAction(AppConstants.SESSION_ACTION_START, null, null, null, false);
    
    List<String> listCategories = productDao.getCategoryList();
    if(listCategories != null) listCategories.add(0, AppConstants.ALL);
    binding.spinInventoryCorrectionDetailsCategory.setAdapter(listCategories);
    
    List<String> listLocations = productDao.getLocationList();
    if(listLocations != null) listLocations.add(0, AppConstants.ALL);
    binding.spinInventoryCorrectionDetailsLocation.setAdapter(listLocations);
    
    List<String> listBrands = productDao.getBrandList();
    if(listBrands != null) listBrands.add(0, AppConstants.ALL);
    binding.spinInventoryCorrectionDetailsBrand.setAdapter(listBrands);
    
    final MultiQtyModel multiQtyModel = productDao.getTotalCounts();
    if(multiQtyModel != null)
      setFoundShortageText(binding.ddvStockCorrectShortageAll, multiQtyModel.getFound(), multiQtyModel.getTotal());
    
    found = extractInt(getArguments(), AppConstants.FOUND, 0);
    shortage = extractInt(getArguments(), AppConstants.SHORTAGE, 0);
    setFoundShortageText(binding.ddvStockCorrectShortageFound, found, shortage);
    
    binding.ddvStockCorrectShortageFound.getTxtLabel().setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        binding.ddvStockCorrectShortageFound.setLabelUnderLine(v.getId());
        //filter list
        isShowFoundList = true;
        updateLists();
      }
    });
    
    binding.ddvStockCorrectShortageFound.getTxtLabel2().setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        binding.ddvStockCorrectShortageFound.setLabelUnderLine(v.getId());
        //filter list
        isShowFoundList = false;
        updateLists();
      }
    });
    
    binding.spinInventoryCorrectionDetailsCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id){/*Do Nothing*/}
      
      @Override
      public void onNothingSelected(AdapterView<?> parent){
        /*Empty Method (Default Overridden)*/
      }
    });
    
    final String category = extractString(getArguments(), AppConstants.CATEGORY, AppConstants.ALL);
    
    if(isNonEmpty(category))
      binding.spinInventoryCorrectionDetailsCategory.setSelection(category);
    
    binding.spinInventoryCorrectionDetailsBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id){/*Do Nothing*/}
      
      @Override
      public void onNothingSelected(AdapterView<?> parent){
        /*Empty Method (Default Overridden)*/
      }
    });
    
    final String brand = extractString(getArguments(), AppConstants.BRANDS, AppConstants.ALL);
    
    if(isNonEmpty(brand))
      binding.spinInventoryCorrectionDetailsBrand.setSelection(brand);
    
    binding.spinInventoryCorrectionDetailsLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id){/*Do Nothing*/}
      
      @Override
      public void onNothingSelected(AdapterView<?> parent){/*Empty Method (Default Overridden)*/}
    });
    
    final String zone = extractString(getArguments(), AppConstants.ZONE, AppConstants.ALL);
    
    if(isNonEmpty(zone)) binding.spinInventoryCorrectionDetailsLocation.setSelection(zone);
    binding.listInventoryFoundShortage.setAdapter(new ProductListAdapter((MainActivity) context, StockCorrectionStartFragment.this, listProductsFoundShortage));
    binding.listInventoryFoundShortage.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view!=null && view.getVisibility()==View.VISIBLE){
          if(styleChartAlert != null && styleChartAlert.isShowing()) return;
          context.dismissCustomAlertDialog();
          if(chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())){
            final MultiQtyModel multiQtyModel = productDao.getTotalCounts();
            final boolean isTotalMatched = multiQtyModel != null && chkNull(multiQtyModel.found, 0) > 0 && chkNull(multiQtyModel.total, 0) > 0 && chkNull(multiQtyModel.found, 0) == chkNull(multiQtyModel.total, 0);
            if(!isTotalMatched || chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
              toggleInventory();
            else if(isTotalMatched){
              //show error?
            }
          }
          else if(!isStartSessionImmediately){
            setSessionAction(AppConstants.SESSION_ACTION_START, null, null, null, true);
          }
        }
      }
    });
    
    binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        callUploadClick();
      }
    });
    
    /*binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view.getVisibility() != View.VISIBLE) return;
        if(binding.btnUpload != null && binding.btnUpload.getVisibility() == View.VISIBLE) return;
        callUploadClick();
      }
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        if(chkNotNullTrue(isSuccessfulSwipe)) callUploadClick();
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);*/
    
    return binding.getRoot();
  }
  
  private void callUploadClick(){
    final String txtCounts = binding.ddvStockCorrectShortageFound.getText().trim();
    final int found = AppCommonMethods.parseInt(chkNull(txtCounts.split("/")[0], "0"));
    final int total = AppCommonMethods.parseInt(chkNull(txtCounts.split("/")[1], "0"));
    if(found > 0 && total > 0){
      //binding.btnSwipeUpload.reset();
      context.showCustomConfirmDialog(R.string.title_stock_correction_upload_details, R.string.btn_upload, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialogInterface, int i){
          apiCall(AppConstants.SESSION_ACTION_UPLOAD);
        }
      });
    }
  }
  
  private void setFoundShortageText(DashboardDataView ddv, int found, int shortage){
    final int len = String.valueOf(AppCommonMethods.greater(found, shortage)).length();
    if(len > 3){
      final int loopLimit = len / 2;
      final String format = "<small>%s</small>";
      String appendFormat = format;
      for(int i = 0; i < loopLimit; i++)
        appendFormat = appendFormat.replaceFirst(">%s</", ">" + format + "</");
      ddv.setText(String.format(appendFormat, found) + "/" + String.format(appendFormat, shortage));
    }
    else ddv.setText(found + "/" + shortage);
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    if(binding != null && binding.spinInventoryCorrectionDetailsCategory != null && binding.spinInventoryCorrectionDetailsLocation != null && binding.spinInventoryCorrectionDetailsBrand != null && binding.listInventoryFoundShortage != null){
      final String selZone = binding.spinInventoryCorrectionDetailsLocation.getSelectedItem().trim();
      final String selCategory = binding.spinInventoryCorrectionDetailsCategory.getSelectedItem().trim();
      final String selBrand = binding.spinInventoryCorrectionDetailsBrand.getSelectedItem().trim();
      
      binding.ddvStockCorrectShortageAll.setVisibility(!(selZone.equalsIgnoreCase(AppConstants.ALL) && selBrand.equalsIgnoreCase(AppConstants.ALL) && selCategory.equalsIgnoreCase(AppConstants.ALL)) ? View.VISIBLE : View.INVISIBLE);
      
      MultiQtyModel multiQtyModel = productDao.getTotalCounts();
      if(multiQtyModel != null){
        final boolean isTotalMatched = multiQtyModel != null && chkNull(multiQtyModel.found, 0) > 0 && chkNull(multiQtyModel.total, 0) > 0 && chkNull(multiQtyModel.found, 0) == chkNull(multiQtyModel.total, 0);
        if(multiQtyModel != null)
          setFoundShortageText(binding.ddvStockCorrectShortageAll, multiQtyModel.getFound(), multiQtyModel.getTotal());
        binding.llBtnStart.setVisibility(chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && (chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) || !isTotalMatched) ? View.VISIBLE : View.INVISIBLE);
      }
      found = productDao.getFoundCount(selZone, selCategory, selBrand);
      setFoundShortageText(binding.ddvStockCorrectShortageFound, found, shortage);
      binding.btnUpload.setVisibility(chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && found > 0 ? View.VISIBLE : View.GONE);
      //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && found > 0 ? View.VISIBLE : View.GONE);
      
      listProductsFoundShortage.clear();
      listProductsFoundShortage.addAll(isShowFoundList ? productDao.getShortageFoundProducts(selZone, selCategory, selBrand) : productDao.getShortageProducts(selZone, selCategory, selBrand));
      ((RecyclerView.Adapter) binding.listInventoryFoundShortage.getAdapter()).notifyDataSetChanged();
    }
  }
  
  @Override
  protected void isSessionOnChanged(final Boolean isInventorySessionOn){
    super.isSessionOnChanged(isInventorySessionOn);
    if(isInventorySessionOn == null) return;
    boolean isInvSessionOn = chkNotNullTrue(isInventorySessionOn);
    
    binding.spinInventoryCorrectionDetailsLocation.setEnabled(!isInvSessionOn);
    binding.spinInventoryCorrectionDetailsBrand.setEnabled(!isInvSessionOn);
    binding.spinInventoryCorrectionDetailsCategory.setEnabled(!isInvSessionOn);
    binding.llBtnStart.setVisibility(isInvSessionOn ? View.VISIBLE : View.INVISIBLE);
    
    final int found = AppCommonMethods.parseInt(chkNull(binding.ddvStockCorrectShortageFound.getText().split("/")[0], "0"));
    final boolean isInventoryOn = chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue());
    binding.btnUpload.setVisibility(isInventorySessionOn && !isInventoryOn && found > 0 ? View.VISIBLE : View.GONE);
    //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && isInventorySessionOn && !isInventoryOn && found > 0 ? View.VISIBLE : View.GONE);
  }
  
  @Override
  protected void isInventoryOnChanged(Boolean isInventoryOn){
    super.isInventoryOnChanged(isInventoryOn);
    final boolean isInventorySessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
    if(isInventoryOn == null) return;
    else{
      binding.llBtnStart.toggle(isInventoryOn);
      final int found = AppCommonMethods.parseInt(chkNull(binding.ddvStockCorrectShortageFound.getText().split("/")[0], "0"));
      final int shortage = AppCommonMethods.parseInt(chkNull(binding.ddvStockCorrectShortageFound.getText().split("/")[1], "0"));
      final MultiQtyModel multiQtyModel = productDao.getTotalCounts();
      final boolean isTotalMatched = multiQtyModel != null && chkNull(multiQtyModel.found, 0) > 0 && chkNull(multiQtyModel.total, 0) > 0 && chkNull(multiQtyModel.found, 0) == chkNull(multiQtyModel.total, 0);
      binding.llBtnStart.setVisibility(isInventoryOn || !isTotalMatched ? View.VISIBLE : View.INVISIBLE);
      binding.btnUpload.setVisibility(isInventorySessionOn && !isInventoryOn && found > 0 ? View.VISIBLE : View.GONE);
      //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && isInventorySessionOn && !isInventoryOn && found > 0 ? View.VISIBLE : View.GONE);
      updateViews();
    }
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    final MultiQtyModel multiQtyModel = productDao.getTotalCounts();
    if(multiQtyModel != null){
      final boolean isTotalMatched = multiQtyModel != null && chkNull(multiQtyModel.found, 0) > 0 && chkNull(multiQtyModel.total, 0) > 0 && chkNull(multiQtyModel.found, 0) == chkNull(multiQtyModel.total, 0);
      if(isTotalMatched && chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
        mainViewModel.stopInventory();
    }
    updateViews();
    updateLists();
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    if(isSearchOn == null) return;
    else{
      updateViews();
      updateLists();
      if(!isSearchOn) stopTimer();
      else
        startTimer(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null ? dialogOmniEpcSearchBinding.clOmniChannelEpcSearch : null, dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.imgSearchDir != null ? dialogOmniEpcSearchBinding.imgSearchDir : null);
      if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
        dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.setEnableCheck(false);
      if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null && dialogOmniEpcSearchBinding.btnDecode != null){
        dialogOmniEpcSearchBinding.llBtnStart.toggle(isSearchOn);
        final Object tag = dialogOmniEpcSearchBinding.btnDecode.getTag();
        final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
        dialogOmniEpcSearchBinding.btnDecode.setVisibility(showMarkFoundBtn && inventory != null && !inventory.isFound && !isSearchOn ? View.VISIBLE : View.GONE);
      }
    }
  }
  
  @Override
  protected void onTriggerPressed(){
    if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null)
      dialogOmniEpcSearchBinding.llBtnStart.performClick();
    else binding.llBtnStart.performClick();
  }
  
  @Override
  protected void setDefaultSearchViews(){
    super.setDefaultSearchViews();
    if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
      dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.resetToDefault();
  }
  
  @Override
  protected void updateSearchUI(int result){
    super.updateSearchUI(result);
    final Object tag = dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.btnDecode != null ? dialogOmniEpcSearchBinding.btnDecode.getTag() : null;
    final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
    if(!showMarkFoundBtn && result >= AppCommonMethods.markFoundPercentAlienSearch && inventory != null && !inventory.isFound)
      showMarkFoundBtn = true;
  }
  
  private void updateViews(){
    if(dialogOmniEpcSearchBinding != null){
      dialogOmniEpcSearchBinding.llBtnStart.setEnabled(!chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()));
    }
  }
  
  @Override
  public void apiCall(String action){
    
    final Boolean isInventorySessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
    if(isInventorySessionOn == null) return;
    else if(!isInventorySessionOn){
      setSessionAction(action, null, null, null, true);
    }
    else if(isInventorySessionOn && sessionObject != null){
      final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
      final String selZone = isCallUploadForAll ? AppConstants.ALL : chkNull(binding.spinInventoryCorrectionDetailsLocation.getSelectedItem(), AppConstants.ALL).trim();
      final String selCategory = isCallUploadForAll ? AppConstants.ALL : chkNull(binding.spinInventoryCorrectionDetailsCategory.getSelectedItem(), AppConstants.ALL).trim();
      final String selBrand = isCallUploadForAll ? AppConstants.ALL : chkNull(binding.spinInventoryCorrectionDetailsBrand.getSelectedItem(), AppConstants.ALL).trim();
      
      if(isInternetConnected(context, false, isUpload)){
        try{
          if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          allowBtnClick = false;
          JSONObject requestParams = new JSONObject();
          
          requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
          requestParams.put(ParamConstants.TYPE, AppConstants.STOCK_CORRECTION);
          if(selZone.equalsIgnoreCase(AppConstants.ALL)){
            requestParams.put(ParamConstants.ZONE_ID, 0);
            requestParams.put(ParamConstants.ZONE, selZone);
          }
          else{
            final List<Zone> listZones = AppDatabase.getZoneDao(context).getZoneByName(selZone);
            if(isNonEmpty(listZones) && listZones.size() == 1){
              Zone zone = listZones.get(0);
              requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
              requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : AppConstants.ALL);
              requestParams.put(ParamConstants.ZONE_TYPE, zone != null ? zone.zoneType : null);
              requestParams.put(ParamConstants.IS_DEFAULT_ZONE, zone != null ? zone.isDefault : false);
            }
            else{
              JSONArray zones = new JSONArray();
              for(Zone zone : listZones){
                if(zone != null){
                  JSONObject jsonZone = zone.toJson();
                  if(jsonZone != null){
                    zones.put(jsonZone);
                  }
                }
              }
              if(zones != null && zones.length() > 0)
                requestParams.put(ParamConstants.ZONES, zones);
            }
          }
          requestParams.put(ParamConstants.CATEGORY, !chkNull(selZone, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL) ? sessionObject != null ? sessionObject.category : selCategory : AppConstants.ALL);
          requestParams.put(ParamConstants.BRAND, !chkNull(selZone, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL) ? sessionObject != null ? sessionObject.brands : selBrand : AppConstants.ALL);
          requestParams.put(ParamConstants.ACTION, action);
          requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
          
          if(isUpload){
            new Handler().post(new Runnable(){
              @Override
              public void run(){
                try{
                  JSONArray js = new JSONArray();
                  List<ProductModel> listFoundProducts = isCallUploadForAll ? productDao.getAllFoundProducts() : productDao.getShortageFoundProducts(selZone, selCategory, selBrand);
                  if(isNonEmpty(listFoundProducts))
                    for(ProductModel productModel : listFoundProducts){
                      if(productModel != null && productModel.getEpc() != null){
                        Inventory inventory = inventoryDao.getInventoryByEPC(productModel.epc);
                        if(inventory != null){
                          JSONObject dataobject = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_STOCK_CORRECTION_UPLOAD) ? inventory.toOnlyEpcJson() : inventory.toJson(context);
                          
                          if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                            js.put(dataobject);
                        }
                      }
                    }
                  requestParams.put(ParamConstants.ITEMS, js);
                  allowBtnClick = true;
                  callWebService(URLConstants.UPLOAD_STOCK_CORRECTION, requestParams, isUpload, getString(R.string.progress_msg_uploading_data), !isUpload);
                }
                catch(Exception e){
                  e.printStackTrace();
                  hideProgressDialog();
                  allowBtnClick = true;
                }
              }
            });
            
            //add Other Parameters for bulk uploading
          }
          else{
            allowBtnClick = true;
            callWebService(isUpload ? URLConstants.UPLOAD_STOCK_CORRECTION : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload);
          }
        }
        catch(JSONException e){
          if(!isUpload) setSessionAction(action, sessionObject.sessionId, null, null, false);
          else hideProgressDialog();
          allowBtnClick = true;
        }
      }
      else if(!isUpload) setSessionAction(action, sessionObject.sessionId, null, null, false);
    }
  }
  
  public void showEpcList(final String ean){
    if(!SharedPrefManager.getBoolean(ParamConstants.IS_EPC_SEARCH_IN_STOCK_CORRECTION)) return;
    if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())) return;
    final List<Inventory> listPicked = inventoryDao.getEANInventory(sessionObject.sessionId, ean);
    if(isNullOrEmpty(listPicked)) return;
    styleChartAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    setAlertDialogCustomTitle(styleChartAlert, getString(R.string.title_stock_correction_search_tag));
    DialogSimilarStylesBinding binding = DialogSimilarStylesBinding.inflate(LayoutInflater.from(context));
    binding.listDialogSimilarStyles.setAdapter(new OmniPickedListAdapter((MainActivity) context, StockCorrectionStartFragment.this, listPicked));
    binding.listDialogSimilarStyles.setLayoutManager(new LinearLayoutManager(context));
    styleChartAlert.setView(binding.getRoot());
    styleChartAlert.setCancelable(false);
    styleChartAlert.setOnShowListener(new DialogInterface.OnShowListener(){
      @Override
      public void onShow(DialogInterface dialog){
        dialogSimilarStylesBinding = binding;
      }
    });
    styleChartAlert.setOnDismissListener(new DialogInterface.OnDismissListener(){
      @Override
      public void onDismiss(DialogInterface dialog){
        dialogSimilarStylesBinding = null;
      }
    });
    styleChartAlert.show();
  }
  
  @Override
  public void startEPCSearch(Inventory inventory){
    if(!isProcessOn() && inventory != null){
      final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
      setAlertDialogCustomTitle(alertDialog, R.string.search);
      DisplayMetrics displayMetrics = new DisplayMetrics();
      context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
      final int wid = (context.isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / 2;
      DialogOmniEpcSearchBinding binding = DialogOmniEpcSearchBinding.inflate(LayoutInflater.from(context), null, false);
      LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(wid, wid);
      binding.clOmniEPCSearch.setLayoutParams(llParams);
      binding.btnDecode.setText(R.string.btn_mark_found);
      binding.btnDecode.setTag(inventory);
      showMarkFoundBtn = false;
      binding.btnDecode.setVisibility(!isProcessOn() && showMarkFoundBtn && !inventory.isFound ? View.VISIBLE : View.GONE);
      binding.btnDecode.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          //Mark as found
          if(view != null && view.getVisibility() == View.VISIBLE && !inventory.isFound){
            try{
              inventory.isFound = true;
              inventoryDao.updateInventoryData(inventory);
              binding.btnDecode.setTag(inventory);
              showMarkFoundBtn = false;
              binding.btnDecode.setVisibility(showMarkFoundBtn && !isProcessOn() && !inventory.isFound ? View.VISIBLE : View.GONE);
              if(dialogSimilarStylesBinding != null && dialogSimilarStylesBinding.listDialogSimilarStyles != null && dialogSimilarStylesBinding.listDialogSimilarStyles.getAdapter() != null && dialogSimilarStylesBinding.listDialogSimilarStyles.getAdapter() instanceof RecyclerView.Adapter)
                ((RecyclerView.Adapter) dialogSimilarStylesBinding.listDialogSimilarStyles.getAdapter()).notifyDataSetChanged();
            }
            catch(Exception e){ e.printStackTrace(); }
          }
        }
      });
      binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          context.dismissCustomAlertDialog();
          if(!isProcessOn() && checkReaderConnected()){
            mainViewModel.performEPCBasedSearch(chkNull(inventory.newEpc, inventory.epc));
            searchStartTime = System.currentTimeMillis();
          }
          else if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
            mainViewModel.stopInventory();
        }
      });
      alertDialog.setView(binding.getRoot());
      alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){
        @Override
        public void onShow(DialogInterface dialogInterface){
          dialogOmniEpcSearchBinding = binding;
          if(searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject();
              jsonExtras.put(ParamConstants.BRANDS, sessionObject.brands);
              jsonExtras.put(ParamConstants.CATEGORY, sessionObject.category);
              jsonExtras.put(ParamConstants.ZONE, sessionObject.zone);
              jsonExtras.put(ParamConstants.ZONE, sessionObject.zone);
              jsonExtras.put(ParamConstants.ZONE_ID, sessionObject.zoneId);
              jsonExtras.put(ParamConstants.EAN, inventory.epc);
              jsonExtras.put(ParamConstants.EAN_QTY, 1);
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, false);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, AppConstants.STOCK_CORRECTION);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", inventory.epc, 1, getSessionType().name(), "", jsonExtras);
          }
        }
      });
      alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
        @Override
        public void onDismiss(DialogInterface dialogInterface){
          if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
            mainViewModel.stopInventory();
          if(searchLog != null) searchLog = null;
          dialogOmniEpcSearchBinding = null;
        }
      });
      alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
          context.handleTriggerKeyEvent(keyCode, event);
          return false;
        }
      });
      alertDialog.show();
    }
    else if(isProcessOn() && inventory != null) showShortToast(R.string.not_allowed);
  }
  
  /**
   * Set session action.
   *
   * @param action           the action
   * @param sessionId        the session id
   * @param sessionTime      the session time
   * @param validTill        the valid till
   * @param isStartInventory the is start inventory
   */
  void setSessionAction(String action, String sessionId, String sessionTime, Integer validTill, boolean isStartInventory){
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.zone = isCallUploadForAll ? AppConstants.ALL : extractString(getArguments(), AppConstants.ZONE, AppConstants.ALL);
      sessionObject.zoneId = isCallUploadForAll ? "0" : extractString(getArguments(), AppConstants.ZONE_ID, "0");
      sessionObject.category = isCallUploadForAll ? AppConstants.ALL : extractString(getArguments(), AppConstants.CATEGORY, AppConstants.ALL);
      sessionObject.brands = isCallUploadForAll ? AppConstants.ALL : extractString(getArguments(), AppConstants.BRANDS, AppConstants.ALL);
      
      sessionObject.sessionType = AppCommonMethods.SessionType.STOCK_CORRECTION.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.INVENTORY.getValue();
      sessionObject.userId = SharedPrefManager.getUserID();
      
      Calendar cc = Calendar.getInstance();
      if(chkNull(sessionTime, "").length() > 0 && sessionTime.matches(DATE_TIME_FORMAT_PATTERN)){
        try{
          cc.setTime(new SimpleDateFormat(DATE_TIME_FORMAT).parse(sessionTime));
        }
        catch(Exception e){ e.printStackTrace(); }
      }
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, chkNull(validTill, 48));
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = chkNull(sessionId, mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc));
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, isStartInventory);
    }
    else if(sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_RESUME))
      mainViewModel.startSession(sessionObject, isStartInventory);
    else if(sessionObject != null && !action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")")){
      mainViewModel.stopSession(sessionObject, action.matches("(?i)(" + AppConstants.SESSION_ACTION_UPLOAD + "|" + AppConstants.SESSION_ACTION_DISCARD + ")"));
      context.popBackStack();
    }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.UPLOAD_STOCK_CORRECTION:
          if(isSuccess && jsonResponse != null){
            final int found = AppCommonMethods.parseInt(chkNull(binding.ddvStockCorrectShortageFound.getText().split("/")[0], "0"));
            final int total = AppCommonMethods.parseInt(chkNull(binding.ddvStockCorrectShortageFound.getText().split("/")[1], "0"));
            final MultiQtyModel multiQtyModel = productDao.getTotalCounts();
            final int allFound = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
            final int allTotal = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), isCallUploadForAll ? new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                if(allFound == allTotal){
                  context.doublePopBackStack();
                }
                else if(found == total){
                  mainViewModel.stopSession(sessionObject, false);
                  context.popBackStack();
                }
              }
            } : null);
            //delete Uploaded Records
            final String selZone = chkNull(binding.spinInventoryCorrectionDetailsLocation.getSelectedItem(), AppConstants.ALL).trim();
            final String selCategory = chkNull(binding.spinInventoryCorrectionDetailsCategory.getSelectedItem(), AppConstants.ALL).trim();
            final String selBrand = chkNull(binding.spinInventoryCorrectionDetailsBrand.getSelectedItem(), AppConstants.ALL).trim();
            List<ProductModel> listFoundProducts = isCallUploadForAll ? productDao.getAllFoundProducts() : productDao.getShortageFoundProducts(selZone, selCategory, selBrand);
            if(isNonEmpty(listFoundProducts)){
              for(ProductModel productModel : listFoundProducts){
                if(productModel != null && productModel.getEpc() != null){
                  Inventory inventory = inventoryDao.getInventoryByEPC(productModel.epc);
                  if(inventory != null){
                    inventoryDao.deleteInventoryData(inventory);
                    productDao.delete(productModel);
                  }
                }
              }
              shortage = productDao.getTotalCount(selZone, selCategory, selBrand);
              if(allFound == allTotal){
                AppDatabase.getDbInstance(context).RFIDSessionDao().deleteAll(sessionObject.sessionId);
                mainViewModel.stopSession(sessionObject, true);
              }
            }
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}