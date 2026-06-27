package com.itek.retail.ui.outward;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.ProductListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.database.ZoneDao;
import com.itek.retail.databinding.DialogHuScanBinding;
import com.itek.retail.databinding.FragmentOutwardPicklistDetalisBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Outward list fragment.
 */
public class OutwardPickListDetailsFragment extends CommonFragment{
  
  private final AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.SCAN;
  ZoneDao zoneDao;
  ProductDao productDao;
  TripStatusDao tripStatusDao;
  MainViewModel mainViewModel = null;
  private OutwardPickListDetailsViewModel mViewModel;
  private FragmentOutwardPicklistDetalisBinding binding;
  private List<String> listBrands = new ArrayList<>(0);
  private List<String> listCategories = new ArrayList<>(0);
  private List<ProductModel> listOutwardProducts = new ArrayList<>(0);
  private String selBrand = "";
  private String selCategory = "";
  private TripStatus tripStatus;
  private String tripNum = "";
  private String huNum = "";
  private String tripType = "";
  private String actionType = "";
  private DialogHuScanBinding dialogHuScanBinding;
  private AlertDialog huScanDialog = null;
  
  /**
   * Instantiates a new Outward list fragment.
   */
  public OutwardPickListDetailsFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    mainViewModel = ((MainActivity) context).getRfidViewModel();
    zoneDao = AppDatabase.getZoneDao(context);
    tripStatusDao = AppDatabase.getTripStatusDao(context);
    productDao = AppDatabase.getProductDao(context);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    binding = FragmentOutwardPicklistDetalisBinding.inflate(inflater, container, false);
    Object obj = extractSerializable(getArguments(), TripStatus.class);
    tripStatus = obj != null && obj instanceof TripStatus ? (TripStatus) obj : null;
    tripNum = tripStatus != null ? tripStatus.getTripNumber() : extractString(getArguments(), AppConstants.TRIP_NUMBER);
    tripType = tripStatus != null ? tripStatus.getTripType() : extractString(getArguments(), AppConstants.TRIP_TYPE);
    actionType = tripStatus != null ? tripStatus.getType() : extractString(getArguments(), AppConstants.ACTION_TYPE);
    
    binding.txtPickingListNo.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_list_pick), tripNum), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    listBrands.clear();
    listBrands.add(AppConstants.ALL);
    List<String> brands = productDao.getBrandList();
    if(isNonEmpty(brands)) listBrands.addAll(brands);
    
    listCategories.clear();
    listCategories.add(AppConstants.ALL);
    List<String> categories = productDao.getCategoryList();
    if(isNonEmpty(categories)) listCategories.addAll(categories);
    
    binding.spinOutwardBrand.setAdapter(listBrands);
    binding.spinOutwardCategory.setAdapter(listCategories);
    
    binding.listOutwardProducts.setAdapter(new ProductListAdapter((MainActivity) context, OutwardPickListDetailsFragment.this, listOutwardProducts));
    binding.listOutwardProducts.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    
    binding.spinOutwardBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
    });
    
    binding.spinOutwardCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        showLog("Outward_Category", "OnItemSelected");
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
    });
    
    ((LinearLayout) binding.txtHUNumberScan.getParent()).setVisibility(isNonEmpty(huNum) ? View.VISIBLE : View.GONE);
    
    updateLists();
    
    binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
          return;
        context.showCustomConfirmDialog(getString(R.string.msg_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            uploadTrip(null);
          }
        });
      }
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          binding.btnSwipeUpload.reset();
          context.showCustomConfirmDialog(getString(R.string.msg_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              uploadTrip(null);
            }
          });
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);
    
    return binding.getRoot();
  }
  
  public void uploadTrip(final Bundle args){
    new Handler().post(new Runnable(){
      @Override
      public void run(){
        try{
          showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          JSONObject requestParams = new JSONObject();
          requestParams.put(ParamConstants.SESSION_TYPE, AppCommonMethods.SessionType.OUTWARD_PICK.name());
          requestParams.put(ParamConstants.TYPE, AppConstants.OUTWARD);
          requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD);
          JSONArray tripJsonArray = new JSONArray();
          JSONObject jobj = new JSONObject();
          jobj.put(ParamConstants.K_TRIP_NUMBER, tripNum);
          jobj.put(ParamConstants.K_TRIP_TYPE, tripType);
          List<Inventory> list = AppDatabase.getInventoryDao(context).getAllInventoryData(AppCommonMethods.SessionType.OUTWARD_PICK.getValue());
          JSONArray jhuArray = new JSONArray();
          JSONObject huobj = isNonEmpty(huNum) ? new JSONObject() : null;
          if(huobj != null){
            huobj.put(ParamConstants.K_TRIP_HU_NUMBER, huNum);
            huobj.put(ParamConstants.K_TRIP_HU_STATUS, "C");
          }
          
          JSONArray jArrayItems = new JSONArray();
          if(isNonEmpty(list)){
            for(Inventory inventory : list){
              if(inventory != null && !inventory.isUploaded){
                JSONObject item = inventory.toJson(context);
                if(item != null) jArrayItems.put(item);
              }
            }
          }
          if(huobj != null){
            huobj.put(ParamConstants.ITEMS, jArrayItems);
            jhuArray.put(huobj);
          }
          jobj.put(ParamConstants.K_TRIP_HU_DETAILS, jhuArray);
          tripJsonArray.put(jobj);
          
          requestParams.put(ParamConstants.K_TRIPS_DATA, tripJsonArray);
          
          callWebService(URLConstants.UPLOAD_OUTWARD_PICK, requestParams, args, getString(R.string.progress_msg_uploading_data), false);
          
        }
        catch(JSONException e){
          e.printStackTrace();
          hideProgressDialog();
        }
      }
    });
  }
  
  public void apiCall(String action){
    
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    if(isInternetConnected(context, false, isUpload)){
      try{
        allowBtnClick = false;
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        requestParams.put(ParamConstants.TYPE, AppConstants.OUTWARD_PICK);
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, action.replaceFirst("(?i)" + AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst("(?i)" + AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst("(?i)" + AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        //TODO add hu details
        allowBtnClick = true;
        callWebService(URLConstants.UPLOAD_OUTWARD_PICK, requestParams, isUpload, getString(R.string.progress_msg_uploading_data), !isUpload);
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
        allowBtnClick = true;
      }
    }
  }
  
  /**
   * Get sel brand string.
   *
   * @return the string
   */
  public String getSelBrand(){
    return chkNull(binding.spinOutwardBrand.getSelectedItem(), AppConstants.ALL);
  }
  
  /**
   * Get sel category string.
   *
   * @return the string
   */
  public String getSelCategory(){
    return chkNull(binding.spinOutwardCategory.getSelectedItem(), AppConstants.ALL);
  }
  
  /**
   * Get trip num string.
   *
   * @return the string
   */
  public String getTripNum(){
    return chkNull(tripNum, "");
  }
  
  /**
   * Get trip type string.
   *
   * @return the string
   */
  public String getTripType(){
    return chkNull(tripNum, "");
  }
  
  /**
   * Get action type string.
   *
   * @return the string
   */
  public String getActionType(){
    return chkNull(actionType, "");
  }
  
  /**
   * Get hu num string.
   *
   * @return the string
   */
  public String getHuNum(){
    return chkNull(huNum, "");
  }
  
  public void scanHU(final ProductModel product){
    if(product != null && (huScanDialog == null || !huScanDialog.isShowing())){
      mainViewModel.getReaderUHFInstance(sessionType);
      mainViewModel.getBarcodeReaderInstance(sessionType);
      
      final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialog).create();
      setAlertDialogCustomTitle(alertDialog, R.string.btn_create_hu);
      
      DialogHuScanBinding binding = DialogHuScanBinding.inflate(LayoutInflater.from(context), null, false);
      alertDialog.setView(binding.getRoot());
      alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.btn_go), new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          mainViewModel.getIsBarcodeOn().postValue(false);
          if(binding.edtSearchHu.validate()){
            binding.edtSearchHu.setIsViewControlEnabled(false, true);
            huNum = binding.edtSearchHu.getText().toString().trim();
            OutwardPickListDetailsFragment.this.binding.txtHUNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_hu), huNum), HtmlCompat.FROM_HTML_MODE_LEGACY));
            ((LinearLayout) OutwardPickListDetailsFragment.this.binding.txtHUNumberScan.getParent()).setVisibility(isNonEmpty(huNum) ? View.VISIBLE : View.GONE);
            Bundle args = chkNull(getArguments(), new Bundle());
            args.putSerializable(product.getClass().getSimpleName(), product);
            args.putString(AppConstants.TRIP_NUMBER, getTripNum());
            args.putString(AppConstants.TRIP_TYPE, getTripType());
            args.putString(AppConstants.ACTION_TYPE, getActionType());
            args.putString(AppConstants.HU_NUMBER, getHuNum());
            args.putString(AppConstants.BRAND, getSelBrand());
            args.putString(AppConstants.CATEGORY, getSelCategory());
            if(SharedPrefManager.getIsEANMapped()){
              try{
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put(ParamConstants.EAN, product.getEan());
                jsonRequest.put(ParamConstants.EPC, "");
                callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, args, context.getString(R.string.progress_msg_check_map_data));
              }
              catch(Exception e){ e.printStackTrace(); }
            }
            else
              ((MainActivity) context).checkReaderConnection(new OutwardPickStartFragment(), args);
          }
        }
      });
      
      binding.edtSearchHu.setImgScanOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          if(isNullOrEmpty(binding.edtSearchHu.getText().toString()) && mainViewModel != null && !chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())){
            context.dismissCustomAlertDialog();
            showLog("Fragment softScan", "softScan");
            mainViewModel.softScan(binding.edtSearchHu.getLabel());
          }
        }
      });
      
      binding.edtSearchHu.setGoBtn(alertDialog.getButton(AlertDialog.BUTTON_POSITIVE));
      
      alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){
        @Override
        public void onShow(DialogInterface dialogInterface){
          huScanDialog = alertDialog;
          setTriggerDataObserver();
          dialogHuScanBinding = binding;
          setBarcodeObservers();
        }
      });
      alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
        @Override
        public void onDismiss(DialogInterface dialogInterface){
          dialogHuScanBinding = null;
          huScanDialog = null;
          if(mainViewModel != null) mainViewModel.onPause();
          setBarcodeObservers();
          setTriggerDataObserver();
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
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      if(dialogHuScanBinding != null && huScanDialog != null && huScanDialog.isShowing() && mainViewModel != null){
        mainViewModel.getBarcodeReaderInstance(sessionType);
        setBarcodeObservers();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
          @Override
          public void run(){
            mainViewModel.onResume(sessionType);
          }
        }, 300);
        
      }
      updateLists();
    }
    
  }
  
  @Override
  public void onPause(){
    super.onPause();
    if(isTopInStack()){
      if(dialogHuScanBinding != null && huScanDialog != null && huScanDialog.isShowing() && mainViewModel != null){
        mainViewModel.onPause();
      }
    }
  }
  
  @Override
  public void onDestroyView(){
    productDao.deleteAllExcept();
    if(mainViewModel != null){
      mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
      mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
      mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
    }
    super.onDestroyView();
    
  }
  
  @Override
  public void onBackPressed(){
    if(productDao.getTotalFoundCount() > 0){
      context.showCustomAlertDialog("", getString(R.string.msg_outward_pick_back), R.string.btn_no, null, R.string.btn_yes, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialogInterface, int i){
          AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.OUTWARD_PICK.getValue());
          AppDatabase.getRIFDSessionDao(context).deleteAll(AppCommonMethods.SessionType.OUTWARD_PICK.getValue());
          context.popBackStack();
        }
      });
    }
    else{
      productDao.deleteAllExcept();
      AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.OUTWARD_PICK.getValue());
      AppDatabase.getRIFDSessionDao(context).deleteAll(AppCommonMethods.SessionType.OUTWARD_PICK.getValue());
      super.onBackPressed();
    }
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    final String selBrand = binding.spinOutwardBrand.getSelectedItem();
    final Set<String> selBrands = binding.spinOutwardBrand.getSelectedVals();
    final String selCategory = binding.spinOutwardCategory.getSelectedItem();
    final Set<String> selCategories = binding.spinOutwardCategory.getSelectedVals();
    listOutwardProducts.clear();
    listOutwardProducts.addAll(productDao.getReplenishmentProducts(selCategory, selCategories, selBrand, selBrands));
    ((RecyclerView.Adapter) binding.listOutwardProducts.getAdapter()).notifyDataSetChanged();
    final boolean isPicked = false && productDao.getTotalFoundCount() > 0;
    binding.btnUpload.setVisibility(isPicked ? View.VISIBLE : View.GONE);
    binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && isPicked ? View.VISIBLE : View.GONE);
    
    binding.btnCompleteHU.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(isNonEmpty(huNum) && v.getVisibility() == View.VISIBLE){
          context.showCustomConfirmDialog(String.format(getString(R.string.msg_complete_hu_alert), huNum), R.string.hu_upload, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              uploadTrip(null);
            }
          });
        }
      }
    });
  }
  
  /**
   * Set observers.
   */
  void setBarcodeObservers(){
    if(mainViewModel != null){
      mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
      mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
      if(huScanDialog != null && huScanDialog.isShowing() && huScanDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null){
        
        mainViewModel.getIsBarcodeOn().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
          @Override
          public void onChanged(Boolean isBarcodeOn){
            if(!isTopInStack()) return;
            
            showLog("isBarcodeOn", "" + chkNotNullTrue(isBarcodeOn));
            insertAuditTrailsLog("Barcode_" + (chkNotNullTrue(isBarcodeOn) ? "ON" : "OFF"));
            ((MainActivity) context).lockDrawer(chkNotNullTrue(isBarcodeOn));
            if(dialogHuScanBinding != null && dialogHuScanBinding.edtSearchHu != null)
              dialogHuScanBinding.edtSearchHu.setIsProcessOn(chkNotNullTrue(isBarcodeOn));//.setIsViewControlEnabled(!chkNotNullTrue(isBarcodeOn));
          }
        });
        
        mainViewModel.getBarcodeData().observe(getViewLifecycleOwner(), new Observer<String>(){
          @Override
          public void onChanged(String barcode){
            if(!isTopInStack()) return;
            showLog(OutwardPickListDetailsFragment.this.getClass().getSimpleName() + "_barcodeData", "" + chkNull(barcode, ""));
            if(isNonEmpty(barcode) && dialogHuScanBinding != null && dialogHuScanBinding.edtSearchHu != null && huScanDialog != null && huScanDialog.isShowing() && huScanDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null){
              context.showCustomConfirmDialog(String.format(getString(R.string.msg_otw_confirm_barcode_action_set_hu), barcode), R.string.btn_yes, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  if(dialogHuScanBinding != null && dialogHuScanBinding.edtSearchHu != null)
                    dialogHuScanBinding.edtSearchHu.setText(barcode);
                  if(huScanDialog != null && huScanDialog.isShowing() && huScanDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null)
                    huScanDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                }
              });
            }
            if(isNonEmpty(barcode)){
              mainViewModel.getBarcodeData().postValue("");
            }
          }
          
        });
      }
      
    }
  }
  
  /**
   * Set trigger data observer.
   */
  private void setTriggerDataObserver(){
    if(mainViewModel != null){
      mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
      if(huScanDialog != null && huScanDialog.isShowing() && huScanDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null){
        mainViewModel.isTriggerPressed().observe(getViewLifecycleOwner(), triggerPressed -> {
          if(!isTopInStack()) return;
          if(triggerPressed != null && getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && allowBtnClick && isTopInStack()){
            AppCommonMethods.showLog("isTriggerPressed", "" + triggerPressed);
            if(triggerPressed && !chkNotNullTrue(mainViewModel.getIsProcessOn().getValue()) && !chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) && dialogHuScanBinding != null && dialogHuScanBinding.edtSearchHu != null){
              dialogHuScanBinding.edtSearchHu.performScan();
            }
          }
        });
      }
    }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.UPLOAD_OUTWARD_PICK:
          if(isSuccess){
            final Object obj = extractSerializable(args, ProductModel.class);
            final ProductModel productModel = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), productModel == null);
            if(productModel != null)
              AppDatabase.getInventoryDao(context).updateUploaded(AppCommonMethods.SessionType.OUTWARD_PICK.getValue());
            scanHU(productModel);
          }
          break;
        case URLConstants.GET_OUTWARD_PICK_LIST_DETAILS:
          if(isSuccess && jsonResponse != null){
            JSONArray outwardProductsArray = extractJSONArray(jsonResponse, ParamConstants.OUTWARD_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(isNonEmpty(outwardProductsArray)){
              productDao.deleteAllExcept();
              AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.OUTWARD.getValue());
              int insertCount = 0;
              for(int i = 0; i < outwardProductsArray.length(); i++){
                JSONObject outwardProduct = outwardProductsArray.getJSONObject(i);
                ProductModel productModel = outwardProduct != null ? getGSON().fromJson(outwardProduct.toString(), ProductModel.class) : null;
                if(productModel != null){
                  if(isDebugApp && isNullOrEmpty(productModel.getZone()))
                    productModel.setZone("BOH");
                  productModel.setSessionType(AppCommonMethods.SessionType.OUTWARD.getValue());
                  productModel.setItemImgUrl(extractString(outwardProduct, ParamConstants.IMG_URL, "").replaceAll(AppConstants.IMAGE_URL_REPLACE_REGEX, "").trim());
                  final JSONArray jsonZones = extractJSONArray(outwardProduct, ParamConstants.ZONES);
                  productModel.setTotalQty(productModel.getEanQty());
                  if(jsonZones != null && jsonZones.length() > 0){
                    for(int j = 0; j < jsonZones.length(); j++){
                      JSONObject zone = jsonZones.getJSONObject(j);
                      final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                      final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, extractInt(zone, ParamConstants.QTY, 0));
                      final String zoneName = extractString(zone, ParamConstants.ZONE_NAME, extractString(zone, ParamConstants.ZONE, Integer.parseInt(chkZero(zoneId, "0")) > 0 && chkNull(eanQty, 0) > 0 ? AppDatabase.getZoneDao(context).getZoneNameById(zoneId) : ""));
                      if(isNonEmpty(zoneName) && chkNull(eanQty, 0) > 0){
                        productModel.setZone(zoneName);
                        productModel.setZoneId(zoneId);
                        productModel.setEanQty(eanQty);
                        productDao.insert(productModel);
                        insertCount++;
                      }
                    }
                  }
                  else{
                    productDao.insert(productModel);
                    insertCount++;
                  }
                }
              }
              if(insertCount > 0){
                if(listBrands.size() <= 1){
                  listBrands.clear();
                  listBrands.add(0, AppConstants.ALL);
                  final List<String> brands = productDao.getBrandList();
                  if(isNonEmpty(brands)) listBrands.addAll(brands);
                  binding.spinOutwardBrand.setAdapter(listBrands);
                }
                if(listCategories.size() <= 1){
                  listCategories.clear();
                  listCategories.add(0, AppConstants.ALL);
                  final List<String> categories = productDao.getCategoryList();
                  if(isNonEmpty(categories)) listCategories.addAll(categories);
                  binding.spinOutwardCategory.setAdapter(listCategories);
                }
                selCategory = extractString(jsonRequest, ParamConstants.CATEGORY);
                selBrand = extractString(jsonRequest, ParamConstants.BRAND);
              }
              hideProgressDialog();
            }
            else hideProgressDialog();
          }
          else{
            if(isNonEmpty(selBrand) && listBrands.indexOf(selBrand.split(",")[0].trim()) >= 0)
              binding.spinOutwardBrand.setSelection(selBrand);
            if(isNonEmpty(selCategory) && listCategories.indexOf(selCategory.split(",")[0].trim()) >= 0)
              binding.spinOutwardCategory.setSelection(selCategory);
          }
          updateLists();
          break;
        case URLConstants.GET_MAPPED_EAN:
          if(isSuccess){
            final String ean = extractString(jsonResponse, ParamConstants.EAN, extractString(jsonRequest, ParamConstants.EAN));
            final String mappedEan = extractString(jsonResponse, ParamConstants.MAPPED_EAN, "");
            if(args == null) args = getArguments();
            if(args == null) args = new Bundle();
            //use this Mapped EAN for Search/Encode
            args.putString(ParamConstants.MAPPED_EAN, mappedEan);
            if(context != null && !context.isFinishing() && context instanceof MainActivity)
              ((MainActivity) context).checkReaderConnection(new OutwardPickStartFragment(), args);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
}
