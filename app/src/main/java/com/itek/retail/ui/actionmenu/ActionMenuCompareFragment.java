package com.itek.retail.ui.actionmenu;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.getEanRegex;
import static com.itek.retail.common.AppCommonMethods.isDCApp;
import static com.itek.retail.common.AppCommonMethods.isDemoApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.ProductListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.DialogSimilarStylesBinding;
import com.itek.retail.databinding.FragmentActionMenuCompareBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.encoding.EncodingVerifyViewModel;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Encoding verify fragment.
 */
public class ActionMenuCompareFragment extends CommonFragment{
  
  private final AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.SCAN;
  private EncodingVerifyViewModel mViewModel;
  private FragmentActionMenuCompareBinding binding;
  private MainViewModel mainViewModel;
  private ProductDao productDao;
  private AlertDialog similarStylesAlert;
  //private ProductModel selModel = null;
  
  /**
   * Instantiates a new Encoding verify fragment.
   */
  public ActionMenuCompareFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    mainViewModel = ((MainActivity) context).getRfidViewModel();
    mainViewModel.getReaderUHFInstance(sessionType);
    productDao = AppDatabase.getProductDao(context);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(EncodingVerifyViewModel.class);
    binding = FragmentActionMenuCompareBinding.inflate(inflater, container, false);
    
    binding.scanEncodingVerifyBarcode.setImgScanOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) return;
        context.dismissCustomAlertDialog();
        if(similarStylesAlert != null && similarStylesAlert.isShowing()) return;
        if(binding.pdvEncodingVerifyBarcode.isShowingDetailAlert() || binding.pdvEncodingVerifyRfid.isShowingDetailAlert()) return;
        if(isNonEmpty(binding.scanEncodingVerifyBarcode.getText().toString())){
          binding.scanEncodingVerifyBarcode.setText("");
          binding.scanEncodingVerifyBarcode.setTag(null);
          binding.pdvEncodingVerifyBarcode.setProductModel(null);
          binding.pdvEncodingVerifyBarcode.setVisibility(View.GONE);
          binding.scanEncodingVerifyRfid.setText("");
          binding.scanEncodingVerifyRfid.setTag(null);
          binding.pdvEncodingVerifyRfid.setProductModel(null);
          binding.pdvEncodingVerifyRfid.setVisibility(View.INVISIBLE);
          //binding.btnSizeChart.setVisibility(binding.pdvEncodingVerifyRfid.getVisibility() == View.VISIBLE && binding.pdvEncodingVerifyRfid.getProductModel() != null ? View.VISIBLE : View.INVISIBLE);
          binding.scanEncodingVerifyRfid.setVisibility(View.GONE);
          binding.pdvEncodingVerifyBarcode.setVisibility(View.GONE);
          //binding.btnSizeChart0.setVisibility(binding.pdvEncodingVerifyBarcode.getVisibility() == View.VISIBLE && binding.pdvEncodingVerifyBarcode.getProductModel() != null ? View.VISIBLE : View.GONE);
        }
        else if(checkReaderConnected() && isNullOrEmpty(binding.scanEncodingVerifyBarcode.getText().toString()) && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          mainViewModel.performPick("");
        }
      }
    });
    
    binding.scanEncodingVerifyRfid.setImgScanOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) return;
        context.dismissCustomAlertDialog();
        if(similarStylesAlert != null && similarStylesAlert.isShowing()) return;
        if(binding.pdvEncodingVerifyBarcode.isShowingDetailAlert() || binding.pdvEncodingVerifyRfid.isShowingDetailAlert()) return;
        if(isNonEmpty(binding.scanEncodingVerifyRfid.getText().toString())){
          binding.scanEncodingVerifyRfid.setText("");
          binding.scanEncodingVerifyRfid.setTag(null);
          binding.pdvEncodingVerifyRfid.setVisibility(View.INVISIBLE);
          //binding.btnSizeChart.setVisibility(binding.pdvEncodingVerifyRfid.getVisibility() == View.VISIBLE && binding.pdvEncodingVerifyRfid.getProductModel() != null ? View.VISIBLE : View.INVISIBLE);
        }
        else if(checkReaderConnected() && isNullOrEmpty(binding.scanEncodingVerifyRfid.getText().toString()) && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          mainViewModel.performPick("");
        }
      }
    });
    
    if(!isLandscape && !context.isTablet && binding != null && binding.header != null && binding.header.imgConfigSync.getParent() instanceof ConstraintLayout && ((ConstraintLayout) binding.header.imgConfigSync.getParent()).getLayoutParams() instanceof LinearLayout.LayoutParams && binding.header.imgBack.getParent() instanceof ConstraintLayout && ((ConstraintLayout) binding.header.imgBack.getParent()).getLayoutParams() instanceof LinearLayout.LayoutParams){
      ((LinearLayout.LayoutParams) ((ConstraintLayout) binding.header.imgConfigSync.getParent()).getLayoutParams()).weight = 2.25f;
      ((LinearLayout.LayoutParams) ((ConstraintLayout) binding.header.imgBack.getParent()).getLayoutParams()).weight = 2.25f;
    }
    return binding.getRoot();
  }
  
  /**
   * Show style chart alert.
   */
  public void showSimilarStyles(final List<ProductModel> listSimilarStyles){
    similarStylesAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    setAlertDialogCustomTitle(similarStylesAlert, R.string.tab_similar_style);
    final DialogSimilarStylesBinding binding = DialogSimilarStylesBinding.inflate(LayoutInflater.from(context));
    binding.listDialogSimilarStyles.setAdapter(new ProductListAdapter((MainActivity) context, ActionMenuCompareFragment.this, similarStylesAlert, listSimilarStyles));
    binding.listDialogSimilarStyles.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
    similarStylesAlert.setView(binding.getRoot());
    similarStylesAlert.setCancelable(false);
    similarStylesAlert.show();
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    
    mainViewModel.getIsPickOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getIsPickOn().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isPickOn){
        if(!isTopInStack()) return;
        showLog("isPickOn", "" + chkNotNullTrue(isPickOn));
        if(isPickOn == null) return;
        else{
          final boolean isProcessOn = isPickOn || chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue());
          ((MainActivity) context).lockDrawer(isProcessOn);
          
        }
      }
    });
    
    mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
    mainViewModel.getPickData().observe(getViewLifecycleOwner(), new Observer<Inventory>(){
      @Override
      public void onChanged(Inventory inventory){
        if(!isTopInStack()) return;
        if(inventory != null && isNonEmpty(inventory.ean) /*&& chkNull(inventory.ean, "").matches(getEanRegex(true))*/){
          if(binding.scanEncodingVerifyRfid.getVisibility() == View.VISIBLE){
            binding.scanEncodingVerifyRfid.setText(chkNull(inventory.ean, ""));
            binding.scanEncodingVerifyRfid.setTag(inventory);
            //binding.scanEncodingVerifyRfid.setIsViewControlEnabled(false, true);
            final boolean isEanMatching = isNonEmpty(inventory.ean) && inventory.ean.equalsIgnoreCase(chkNull(binding.scanEncodingVerifyBarcode.getText().toString(), ""));
            if(isEanMatching){
              context.showCustomErrDialog(R.string.err_same_product);
              binding.scanEncodingVerifyRfid.setText("");
              //binding.scanEncodingVerifyRfid.setIsViewControlEnabled(true, true);
            }
            else
              callAPI(chkNull(inventory.ean, ""), chkNull(inventory.epc, ""), chkNull(inventory.tid, ""));
          }
          else{
            binding.scanEncodingVerifyBarcode.setText(chkNull(inventory.ean, ""));
            binding.scanEncodingVerifyBarcode.setTag(inventory);
            //binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(false, true);
            callAPI(chkNull(inventory.ean, ""), chkNull(inventory.epc, ""), chkNull(inventory.tid, ""));
          }
        }
        if(inventory != null) mainViewModel.getPickData().postValue(null);
      }
    });
    
    setTriggerDataObserver();
  }
  
  @Override
  public AppCommonMethods.SessionType getSessionType(){ return sessionType; }
  
  /**
   * Check reader connected boolean.
   *
   * @return the boolean
   */
  protected boolean checkReaderConnected(){
    if(mainViewModel.isReaderConnected()) return true;
    else{
      context.showCustomAlertDialog("", String.format(getString(R.string.err_reader_connection), getTypeCharCode()), getString(R.string.btn_ok), (dialogInterface, i) -> {
        if(((MainActivity) context).isReaderConnected()) mainViewModel.performPick("");
        else mainViewModel.checkAndConnectReader();
      });
      return false;
    }
  }
  
  /**
   * Set observers.
   */
  protected void setObservers(){
    mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getIsBarcodeOn().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isBarcodeOn){
        if(!isTopInStack()) return;
        showLog("isBarcodeOn", "" + AppCommonMethods.chkVal(isBarcodeOn));
        final boolean isProcessOn = chkNotNullTrue(isBarcodeOn) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue());
        ((MainActivity) context).lockDrawer(isProcessOn);
        binding.scanEncodingVerifyBarcode.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(isNonEmpty(binding.scanEncodingVerifyBarcode.getText().toString()) || !isProcessOn, !isProcessOn);
      }
    });
    
    mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
    mainViewModel.getBarcodeData().observe(getViewLifecycleOwner(), new Observer<String>(){
      @Override
      public void onChanged(String barcode){
        if(!isTopInStack()) return;
        showLog(ActionMenuCompareFragment.this.getClass().getSimpleName() + "_barcodeData", "" + chkNull(barcode, ""));
        if(isNonEmpty(barcode)){
          if(chkNull(barcode, "").matches(getEanRegex())){//SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_BOTH) || SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_STD) == SGTIN96.IsValidGtin(ApplicationCommonMethods.getZeroFilledString(barcode, 14))){
            binding.scanEncodingVerifyBarcode.setText(barcode);
            callAPI(chkNull(binding.scanEncodingVerifyBarcode.getText().toString(), ""));
          }
          else{
            context.showCustomErrDialog(String.format(getString(R.string.err_invalid_barcode), getTypeCharCode(sessionType), barcode));
            binding.scanEncodingVerifyBarcode.setText("");
          }
        }
        if(isNonEmpty(barcode)){ mainViewModel.getBarcodeData().postValue(""); }
      }
    });
  }
  
  /**
   * Call api.
   *
   * @param ean the ean
   */
  void callAPI(final String ean){ callAPI(ean, "", ""); }
  
  /**
   * Call api.
   *
   * @param ean the ean
   * @param epc the epc
   */
  void callAPI(final String ean, final String epc, final String tid){
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.EAN, chkNull(ean, ""));
      jsonRequest.put(ParamConstants.EPC, chkNull(epc, ""));
      jsonRequest.put(ParamConstants.TID, chkNull(tid, ""));
      final String productInfoUrl = getProductInfoUrl();
      if(productInfoUrl.equalsIgnoreCase(URLConstants.GET_PRODUCT_INFO_BY_SKU)){
        JSONArray js = new JSONArray();
        js.put(jsonRequest.get(ParamConstants.EAN));
        jsonRequest.put(ParamConstants.ITEMS, js);
      }
      callWebService(productInfoUrl, jsonRequest, getString(R.string.progress_msg_getting_data), true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Set trigger data observer.
   */
  private void setTriggerDataObserver(){
    mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
    mainViewModel.isTriggerPressed().observe(getViewLifecycleOwner(), triggerPressed -> {
      if(!isTopInStack()) return;
      if(triggerPressed != null && getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && allowBtnClick){
        if(triggerPressed && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          context.dismissCustomAlertDialog();
          if(checkReaderConnected() && isNonEmpty(binding.scanEncodingVerifyBarcode.getText().toString()) && binding.pdvEncodingVerifyBarcode.getProductModel() != null && isNullOrEmpty(binding.scanEncodingVerifyRfid.getText().toString()) && binding.pdvEncodingVerifyRfid.getProductModel() == null){
            mainViewModel.performPick("");
          }
          else if(isNullOrEmpty(binding.scanEncodingVerifyBarcode.getText().toString()) && binding.pdvEncodingVerifyBarcode.getProductModel() == null){
            mainViewModel.performPick("");
          }
        }
      }
    });
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      //mainViewModel.getBarcodeReaderInstance(sessionType);
      new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
        @Override
        public void run(){
          mainViewModel.onResume(sessionType);
          //setTriggerDataObserver();
        }
      }, 300);
    }
  }
  
  public void callSimilarStyles(ProductModel productModel){
    if(!chkNotNullTrue(mainViewModel.getIsProcessOn().getValue()) && (similarStylesAlert == null || !similarStylesAlert.isShowing())){
      try{
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put(ParamConstants.EAN, productModel.getEan());
        if(productModel.getEan().matches("(8907796297374|8907796297381)"))
          callWebService(URLConstants.GET_SIMILAR_STYLES, jsonRequest, context.getString(R.string.progress_msg_getting_data), false, true);
        else{
          try{
            handleResponse(URLConstants.GET_SIMILAR_STYLES, jsonRequest, new JSONObject("{\"Message\":\"No Data Found\"}"), 411, false, null);
          }
          catch(Exception e){ e.printStackTrace(); }
        }
      }
      catch(Exception e){ e.printStackTrace(); }
    }
  }
  
  @Override
  public void onPause(){
    super.onPause();
    mainViewModel.onPause();
  }
  
  @Override
  public void onDestroyView(){
    //mainViewModel.getIsDeviceConfigured().removeObservers(getViewLifecycleOwner());
    //mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
    //mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
    mainViewModel.getIsPickOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
    mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
    super.onDestroyView();
  }
  
  @Override
  public void onDestroy(){
    //mainViewModel.onDestroy();
    super.onDestroy();
  }
  
  @Override
  public void onDetach(){
    ((MainActivity) context).lockDrawer(false);
    super.onDetach();
  }
  
  public void onBackPressed(){
    if(mainViewModel != null && chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
      context.showCustomAlertDialog("", String.format(getString(R.string.err_op_back_press), getTypeCharCode(), sessionType.name()), false, true, getString(R.string.btn_ok), null);
    }
    else super.onBackPressed();
  }
  
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_PRODUCT_INFO:
        case URLConstants.GET_PRODUCT_INFO_BY_SKU:
          ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
          if(isStaticDebug())
            productModel.setEan(extractString(jsonRequest, ParamConstants.EAN, ""));
          productModel.setSessionType(AppCommonMethods.SessionType.SCAN.getValue());
          final ProductModel prodBarcode = binding.pdvEncodingVerifyBarcode.getProductModel();
          if(isNonEmpty(binding.scanEncodingVerifyRfid.getText().toString()) && prodBarcode != null && prodBarcode.getEan().equalsIgnoreCase(productModel.getEan())){
            context.showCustomErrDialog(R.string.err_same_product);
            binding.scanEncodingVerifyRfid.setText("");
            //binding.scanEncodingVerifyRfid.setIsViewControlEnabled(true, true);
          }
          else if(isNonEmpty(binding.scanEncodingVerifyRfid.getText().toString()) && prodBarcode != null){
            binding.pdvEncodingVerifyRfid.setProductModel(productModel, chkNull(productModel.getZone(), AppConstants.DEFAULT_NO_VALUE).equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
            if(productModel != null && isNonEmpty(productModel.getEan()) && !productModel.getEan().equalsIgnoreCase(binding.scanEncodingVerifyRfid.getText().toString().trim()))
              binding.scanEncodingVerifyRfid.setText(productModel.getEan());
            //else
            //binding.scanEncodingVerifyRfid.setIsViewControlEnabled(false, true);
            binding.pdvEncodingVerifyRfid.setFragmentButton(ActionMenuCompareFragment.this, R.string.tab_similar_style);
            //binding.pdvEncodingVerifyRfid.setProductModel(productModel, binding.vidProduct);
            binding.pdvEncodingVerifyRfid.setVisibility(View.VISIBLE);
            //binding.btnSizeChart.setVisibility(binding.pdvEncodingVerifyRfid.getVisibility() == View.VISIBLE && binding.pdvEncodingVerifyRfid.getProductModel() != null ? View.VISIBLE : View.INVISIBLE);
            //binding.scanEncodingVerifyRfid.setIsViewControlEnabled(false, true);
          }
          else{
            binding.pdvEncodingVerifyBarcode.setProductModel(productModel, chkNull(productModel.getZone(), AppConstants.DEFAULT_NO_VALUE).equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
            if(productModel != null && isNonEmpty(productModel.getEan()) && !productModel.getEan().equalsIgnoreCase(binding.scanEncodingVerifyBarcode.getText().toString().trim()))
              binding.scanEncodingVerifyBarcode.setText(productModel.getEan());
            //else
            //binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(false, true);
            binding.pdvEncodingVerifyBarcode.setFragmentButton(ActionMenuCompareFragment.this, R.string.tab_similar_style);
            //binding.pdvEncodingVerifyBarcode.setProductModel(productModel, binding.vidProduct);
            binding.pdvEncodingVerifyBarcode.setVisibility(View.VISIBLE);
            //binding.btnSizeChart0.setVisibility(binding.pdvEncodingVerifyBarcode.getVisibility() == View.VISIBLE && binding.pdvEncodingVerifyBarcode.getProductModel() != null ? View.VISIBLE : View.GONE);
            //binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(false, true);
            //binding.btnVerifyEncoding.setVisibility(View.GONE);
            binding.scanEncodingVerifyRfid.setVisibility(View.VISIBLE);
            binding.pdvEncodingVerifyRfid.setVisibility(View.INVISIBLE);
            binding.scanEncodingVerifyRfid.setIsViewControlEnabled(true, true);
            //binding.btnSizeChart.setVisibility(binding.pdvEncodingVerifyRfid.getVisibility() == View.VISIBLE && binding.pdvEncodingVerifyRfid.getProductModel() != null ? View.VISIBLE : View.INVISIBLE);
          }
          break;
        case URLConstants.GET_SIMILAR_STYLES:
          if(isSuccess && jsonResponse != null){
            final String ean1 = extractString(jsonRequest, ParamConstants.EAN, "");
            final JSONArray jsonArrayProducts = extractJSONArray(jsonResponse, ParamConstants.PRODUCTS);
            if(isNonEmpty(jsonArrayProducts)){
              List<ProductModel> listSimilarProducts = new ArrayList<>(0);
              for(int i = 0; i < jsonArrayProducts.length(); i++){
                final JSONObject product = jsonArrayProducts.getJSONObject(i);
                final ProductModel prodModel = getGSON().fromJson(product.toString(), ProductModel.class);
                if(prodModel != null && isNonEmpty(prodModel.getEan()) && (!isDemoApp || ean1.matches("890779629737[0-9]") == prodModel.getEan().matches("890779629737[0-9]"))){
                  prodModel.setSessionType(AppCommonMethods.SessionType.SEARCH.getValue());
                  prodModel.setItemImgUrl(extractString(product, ParamConstants.IMG_URL, "").replaceAll(AppConstants.IMAGE_URL_REPLACE_REGEX, "").trim());
                  final JSONArray jsonZones = extractJSONArray(product, ParamConstants.ZONES);
                  if(jsonZones != null && jsonZones.length() > 0){
                    for(int j = 0; j < jsonZones.length(); j++){
                      JSONObject zone = jsonZones.getJSONObject(j);
                      final String zoneName = extractString(zone, ParamConstants.ZONE_NAME, "");
                      final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                      final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, 0);
                      if(isNonEmpty(zoneName) && chkNull(eanQty, 0) > 0){
                        prodModel.setZone(zoneName);
                        prodModel.setZoneId(zoneId);
                        prodModel.setEanQty(eanQty);
                        listSimilarProducts.add(prodModel);
                      }
                    }
                  }
                  else listSimilarProducts.add(prodModel);
                }
              }
              hideProgressDialog();
              if(isNonEmpty(listSimilarProducts)) showSimilarStyles(listSimilarProducts);
            }
            else hideProgressDialog();
          }
          else{
            final String errMsg = extractString(jsonResponse, ParamConstants.MESSAGE, context.getString(R.string.err_server_no_connect)).trim();
            context.showCustomErrDialog(errMsg);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}