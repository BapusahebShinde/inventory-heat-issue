package com.itek.retail.ui.actionmenu;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.getEanRegex;
import static com.itek.retail.common.AppCommonMethods.isDemoApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;
import static com.itek.retail.common.AppCommonMethods.successBeep;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.FIFOChartListAdapter;
import com.itek.retail.adapter.ProductColorsListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.InsertDBFIFOs;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.FIFODao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.DialogSimilarStylesBinding;
import com.itek.retail.databinding.DialogSizeChartBinding;
import com.itek.retail.databinding.FragmentActionMenuSearchBinding;
import com.itek.retail.model.FIFOModel;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;
import com.itek.retail.ui.search.fifo.SearchFIFOStartFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * The Action menu search fragment.
 */
public class ActionMenuSearchFragment extends CommonFragment{
  
  private final AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.SCAN;
  private ActionMenuSearchViewModel mViewModel;
  private FragmentActionMenuSearchBinding binding;
  private MainViewModel mainViewModel;
  private boolean isBarcodeScan = false;
  private boolean isShowingProdDtls = false;
  
  private AlertDialog styleChartAlert;
  private ProductDao productDao;
  private ProductModel model;
  
  /**
   * Instantiates a new Action menu search fragment.
   */
  public ActionMenuSearchFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
    mainViewModel = ((MainActivity) context).getRfidViewModel();
    mainViewModel.getReaderUHFInstance(sessionType);
    mainViewModel.getBarcodeReaderInstance(sessionType);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ActionMenuSearchViewModel.class);
    binding = FragmentActionMenuSearchBinding.inflate(inflater, container, false);
    binding.rbRfid.setSelected(true);
    binding.rbBarcode.setSelected(true);
    
    binding.rgSearchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        isBarcodeScan = radioGroup.getCheckedRadioButtonId() == binding.rbBarcode.getId();
        binding.searchProduct.setScanType(!isBarcodeScan);
        binding.btnGo.setVisibility(isBarcodeScan ? View.VISIBLE : View.GONE);
      }
    });
    
    isBarcodeScan = binding.rgSearchType.getCheckedRadioButtonId() == binding.rbBarcode.getId();
    
    binding.rgSearchType.setVisibility(SharedPrefManager.getDeviceTypeValue() > 0 ? View.VISIBLE : View.GONE);
    
    binding.searchProduct.setImgScanOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(isNullOrEmpty(binding.searchProduct.getText().toString()) && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) && !chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())){
          context.dismissCustomAlertDialog();
          if(isBarcodeScan) mainViewModel.softScan();
          else if(checkReaderConnected()) mainViewModel.performPick("");
        }
      }
    });
    
    binding.btnGo.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(binding.searchProduct.validate()){
          binding.searchProduct.setIsViewControlEnabled(false, true);
          callAPI();
        }
      }
    });
    
    binding.searchProduct.setGoBtn(binding.btnGo);
    binding.btnGo.setVisibility(isBarcodeScan ? View.VISIBLE : View.GONE);
    
    return binding.getRoot();
  }
  
  private void setHeader(){
    final MenuModel menuSearchDetails = AppDatabase.getMenuDao(context).getMenuByCode(AppConstants.MENU_CODE_SER_PROD);
    final String title = "Product Details";
    final String logoURL = menuSearchDetails != null ? menuSearchDetails.getScreenImageUrl() : "";
    final int logoId = menuSearchDetails != null ? menuSearchDetails.getScreenIconId(context) : R.drawable.ic_ser_prod;
    final LinearLayout llHeader = binding.header.titleLayout;
    if(llHeader!=null){
      if(isNonEmpty(title)) ((TextView) llHeader.findViewById(R.id.txt_title)).setText(title);
      if(isNonEmpty(logoURL) || chkNull(logoId, 0) > 0)
        context.loadImage((ImageView) llHeader.findViewById(R.id.img_title_logo), logoURL, logoId);
    }
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
          updateViews(isPickOn || chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()));
        }
      }
    });
    
    mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
    mainViewModel.getPickData().observe(getViewLifecycleOwner(), new Observer<Inventory>(){
      @Override
      public void onChanged(Inventory inventory){
        if(!isTopInStack()) return;
        if(inventory != null /*&& chkNull(inventory.ean, "").matches(getEanRegex(true))*/){
          binding.searchProduct.setTag(inventory);
          if(SharedPrefManager.getIsEANMapped()){
            try{
              JSONObject jsonRequest = new JSONObject();
              jsonRequest.put(ParamConstants.MAPPED_EAN, inventory.ean);
              jsonRequest.put(ParamConstants.EPC, inventory.epc);
              callWebService(URLConstants.GET_UNMAPPED_EAN, jsonRequest, getString(R.string.progress_msg_check_map_data));
            }
            catch(Exception e){ e.printStackTrace(); }
          }
          else{
            binding.searchProduct.setText(inventory.ean);
            successBeep();
            binding.btnGo.performClick();
          }
        }
        else if(inventory != null && isNonEmpty(inventory.ean)){
          //show Error
        }
        if(inventory != null) mainViewModel.getPickData().postValue(null);
      }
    });
    
    setObservers();
    setTriggerDataObserver();
    mainViewModel.getIsDeviceConfigured().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isReaderConfigured){
        if(isTopInStack() && chkNotNullTrue(isReaderConfigured)){
          setObservers();
        }
      }
    });
    setHeader();
  }
  
  private void updateViews(final boolean isProcessOn){
    ((MainActivity) context).lockDrawer(isProcessOn);
    binding.rgSearchType.setEnabled(!isProcessOn);
    binding.rbRfid.setEnabled(!isProcessOn);
    binding.rbBarcode.setEnabled(!isProcessOn);
    binding.searchProduct.setIsProcessOn(isProcessOn);
    binding.btnGo.setEnabled(!isProcessOn);
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
  void setObservers(){
    mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getIsBarcodeOn().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isBarcodeOn){
        if(!isTopInStack()) return;
        showLog("isBarcodeOn", "" + AppCommonMethods.chkVal(isBarcodeOn));
        if(isBarcodeOn == null) return;
        else if(isTopInStack()){
          insertAuditTrailsLog("Barcode_" + (isBarcodeOn ? "ON" : "OFF"));
          updateViews(isBarcodeOn || chkNotNullTrue(mainViewModel.getIsPickOn().getValue()));
        }
      }
    });
    
    mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
    mainViewModel.getBarcodeData().observe(getViewLifecycleOwner(), new Observer<String>(){
      @Override
      public void onChanged(String barcode){
        if(!isTopInStack()) return;
        showLog(ActionMenuSearchFragment.this.getClass().getSimpleName() + "_barcodeData", "" + chkNull(barcode, ""));
        if(isNonEmpty(barcode)){
          binding.searchProduct.setText(barcode);
          binding.searchProduct.setKeyPadActive(false);
          binding.btnGo.performClick();
        }
        if(isNonEmpty(barcode)){ mainViewModel.getBarcodeData().postValue(""); }
      }
    });
  }
  
  /**
   * Set trigger data observer.
   */
  private void setTriggerDataObserver(){
    mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
    mainViewModel.isTriggerPressed().observe(getViewLifecycleOwner(), triggerPressed -> {
      if(!isTopInStack()) return;
      if(triggerPressed != null && getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && allowBtnClick){
        if(triggerPressed && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) && !chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())){
          if(!isShowingProdDtls) binding.searchProduct.performScan();
        }
      }
    });
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      mainViewModel.getBarcodeReaderInstance(sessionType);
      setObservers();
      new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
        @Override
        public void run(){
          mainViewModel.onResume(sessionType);
        }
      }, 300);
    }
  }
  
  @Override
  public void onPause(){
    super.onPause();
    mainViewModel.onPause();
  }
  
  @Override
  public void onDestroyView(){
    mainViewModel.getIsDeviceConfigured().removeObservers(getViewLifecycleOwner());
    mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
    mainViewModel.getIsPickOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
    mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
    super.onDestroyView();
  }
  
  @Override
  public void onDetach(){
    ((MainActivity) context).lockDrawer(false);
    super.onDetach();
  }
  
  @Override
  public void onDestroy(){
    //mainViewModel.onDestroy();
    super.onDestroy();
  }
  
  /**
   * Call api.
   */
  void callAPI(){
    if(!isTopInStack()) return;
    Bundle args = new Bundle();
    final MenuModel menuSearchDetails = AppDatabase.getMenuDao(context).getMenuByCode(AppConstants.MENU_CODE_SER_PROD);
    args.putString(AppConstants.TITLE, "Product Details");
    args.putString(AppConstants.TITLE_LOGO_URL, menuSearchDetails != null ? menuSearchDetails.getScreenImageUrl() : "");
    args.putInt(AppConstants.TITLE_LOGO_RES_ID, menuSearchDetails != null ? menuSearchDetails.getScreenIconId(context) : R.drawable.ic_ser_prod);
    try{
      JSONObject jsonRequest = new JSONObject();
      final Inventory tagInfo = !isBarcodeScan && binding.searchProduct.getTag() != null && binding.searchProduct.getTag() instanceof Inventory ? (Inventory) binding.searchProduct.getTag() : null;
      jsonRequest.put(ParamConstants.EAN, binding.searchProduct.getText().toString());
      jsonRequest.put(ParamConstants.EPC, tagInfo != null ? chkNull(tagInfo.epc, "") : "");
      jsonRequest.put(ParamConstants.TID, tagInfo != null ? chkNull(tagInfo.tid, "") : "");
      final String productInfoUrl = getProductInfoUrl();
      if(productInfoUrl.equalsIgnoreCase(URLConstants.GET_PRODUCT_INFO_BY_SKU)){
        JSONArray js = new JSONArray();
        js.put(jsonRequest.get(ParamConstants.EAN));
        jsonRequest.put(ParamConstants.ITEMS, js);
      }
      callWebService(productInfoUrl, jsonRequest, args, getString(R.string.progress_msg_getting_data), true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void onBackPressed(){
    if(mainViewModel != null && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue())))
      context.showShortToast(String.format(getString(R.string.err_op_back_press), getTypeCharCode(), sessionType.name()));
    else if(isShowingProdDtls) toggleUI();
    else super.onBackPressed();
  }
  
  private void clearText(){
    binding.searchProduct.setIsViewControlEnabled(true);
    binding.searchProduct.setText("");
  }
  
  private void toggleUI(){
    isShowingProdDtls=!isShowingProdDtls;
    if(!isShowingProdDtls && model!=null) model=null;
    binding.clProdDtls.setVisibility(isShowingProdDtls?View.VISIBLE:View.GONE);
    binding.clSelOption.setVisibility(!isShowingProdDtls? View.VISIBLE: View.GONE);
  }
  
  public void callStyleChartAPI(final boolean isShowFIFOChart){
    if(styleChartAlert == null || !styleChartAlert.isShowing()){
      if(isShowFIFOChart){
        context.showCustomAlertSelectionDialog("Choose Option", "", getString(R.string.tab_size_chart), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialog, int which){
            callStyleChartAPI(false);
          }
        }, getString(R.string.tab_fifo_products), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialog, int which){
            try{
              JSONObject jsonRequest = new JSONObject();
              jsonRequest.put(ParamConstants.EAN, model.getEan());
              callWebService(URLConstants.GET_FIFO_SEARCH_LIST, jsonRequest, getString(R.string.progress_msg_getting_data));
              
            }
            catch(Exception e){ e.printStackTrace(); }
            //showFIFOChartAlert();
          }
        });
      }
      else{
        try{
          JSONObject jsonRequest = new JSONObject();
          jsonRequest.put(ParamConstants.EAN, model.getEan());
          callWebService(URLConstants.GET_SIZE_CHART, jsonRequest, getString(R.string.progress_msg_getting_data));
          
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    }
  }

  /**
   * Show fifo style chart alert.
   */
  public void showFIFOChartAlert(){
    showFIFOChartAlert(AppDatabase.getFIFODao(context).getDatewiseList(model.ean));
  }

  public void showFIFOChartAlert(final List<FIFOModel> listFIFO){
    styleChartAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    setAlertDialogCustomTitle(styleChartAlert, R.string.tab_fifo_products);
    final DialogSimilarStylesBinding binding = DialogSimilarStylesBinding.inflate(LayoutInflater.from(context));
    binding.listDialogSimilarStyles.setAdapter(new FIFOChartListAdapter((MainActivity) context, ActionMenuSearchFragment.this, styleChartAlert, listFIFO, model));
    binding.listDialogSimilarStyles.setLayoutManager(new LinearLayoutManager(context));
    binding.listDialogSimilarStyles.setVisibility(View.VISIBLE);
    styleChartAlert.setView(binding.getRoot());
    styleChartAlert.setCancelable(false);
    styleChartAlert.show();
  }
  
  /**
   * Show style chart alert.
   */
  public void showStyleChartAlert(){
    styleChartAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    setAlertDialogCustomTitle(styleChartAlert, R.string.tab_size_chart);
    final DialogSizeChartBinding dialogBinding = DialogSizeChartBinding.inflate(LayoutInflater.from(context));
    final RecyclerView lvProdColors = dialogBinding.listDialogSizeChartColors;
    final RecyclerView lvProdSizes = dialogBinding.listDialogSizeChart;
    final List<String> listColors = productDao.getColorList();
    if(isNonEmpty(listColors) && listColors.contains(model.getColor()) && listColors.indexOf(model.getColor()) >= 0){
      listColors.remove(model.getColor());
      listColors.add(0, model.getColor());
    }
    lvProdColors.setAdapter(new ProductColorsListAdapter((MainActivity) context, ActionMenuSearchFragment.this, styleChartAlert, lvProdSizes, listColors, model));
    lvProdColors.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
    styleChartAlert.setView(dialogBinding.getRoot());
    styleChartAlert.setCancelable(false);
    styleChartAlert.show();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_SIZE_CHART:
          productDao.deleteAll(AppCommonMethods.SessionType.SEARCH.getValue());
          if(isDemoApp && isStaticDebug(context)) isSuccess = false;
          if(isSuccess && jsonResponse != null){
            final JSONArray jsonArraySizeChartProducts = extractJSONArray(jsonResponse, ParamConstants.SIZE_CHART_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(jsonArraySizeChartProducts != null && context instanceof MainActivity)
              ((MainActivity) context).callInsertProductDBTask(this, url, AppCommonMethods.SessionType.SEARCH.getValue(), jsonRequest, jsonResponse, args, jsonArraySizeChartProducts);
            /*else if(isNonEmpty(jsonArraySizeChartProducts)){
              for(int i = 0; i < jsonArraySizeChartProducts.length(); i++){
                final JSONObject product = jsonArraySizeChartProducts.getJSONObject(i);
                final ProductModel productModel = getGSON().fromJson(product.toString(), ProductModel.class);
                if(productModel != null){
                  productModel.setSessionType(AppCommonMethods.SessionType.SEARCH.getValue());
                  productModel.setItemImgUrl(extractString(product, ParamConstants.IMG_URL, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim());
                  final JSONArray jsonZones = extractJSONArray(product, ParamConstants.ZONES);
                  if(jsonZones != null && jsonZones.length() > 0){
                    for(int j = 0; j < jsonZones.length(); j++){
                      JSONObject zone = jsonZones.getJSONObject(j);
                      final String zoneName = extractString(zone, ParamConstants.ZONE_NAME, "");
                      final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                      final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, 0);
                      if(isNonEmpty(zoneName) && chkNull(eanQty, 0) > 0){
                        productModel.setZone(zoneName);
                        productModel.setZoneId(zoneId);
                        productModel.setEanQty(eanQty);
                        productDao.insert(productModel);
                      }
                    }
                  }
                  else productDao.insert(productModel);
                }
              }
              hideProgressDialog();
            }*/
            else hideProgressDialog();
          }
          //else context.showCustomErrDialog(R.string.err_no_data);
          if(chkNull(productDao.getAllTotal(), 0) > 0) showStyleChartAlert();
          break;
        case URLConstants.GET_PRODUCT_INFO:
        case URLConstants.GET_PRODUCT_INFO_BY_SKU:
          ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
          if(productModel != null){
            if(isStaticDebug())
              productModel.setEan(extractString(jsonRequest, ParamConstants.EAN, ""));
            productModel.setSessionType(AppCommonMethods.SessionType.SEARCH.getValue());
            model=productModel;
            getArguments().putSerializable(productModel.getClass().getSimpleName(), productModel);
            binding.pdvProdSearch.setProductModel(productModel, chkNull(productModel.getZone(), AppConstants.DEFAULT_NO_VALUE).equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
            binding.pdvProdSearch.setFragmentButton(this, R.string.tab_check_availability);
            toggleUI();
            successBeep();
            clearText();
            /*if(args == null) args = getArguments();
            if(args == null) args = new Bundle();
            args.putSerializable(productModel.getClass().getSimpleName(), productModel);
            popBackStack();
            successBeep();
            context.loadFragment(new ProductDetailsFragment(), args);*/
          }
          break;
        case URLConstants.GET_UNMAPPED_EAN:
          if(isSuccess){
            final String mappedEan = extractString(jsonResponse, ParamConstants.MAPPED_EAN, extractString(jsonRequest, ParamConstants.EAN));
            final String ean = extractString(jsonResponse, ParamConstants.EAN, "");
            binding.searchProduct.setText(ean);
            successBeep();
            binding.btnGo.performClick();
          }
          break;
        case URLConstants.GET_FIFO_SEARCH_LIST:
          if(isSuccess){
            try{
              final String ean = extractString(jsonRequest, ParamConstants.EAN, "");
              final FIFODao fifoDao = AppDatabase.getFIFODao(context);
              JSONArray jsonFIFOProducts = extractJSONArray(jsonResponse, ParamConstants.PRODUCTS);
              if(jsonFIFOProducts != null){
                new InsertDBFIFOs((MainActivity) context, this, AppCommonMethods.SessionType.SEARCH_FIFO.getValue(), url, jsonRequest, jsonResponse, args).execute(jsonFIFOProducts);
              }
              /*else if(isNonEmpty(jsonFIFOProducts)){
                ArrayList<FIFOModel> list = new ArrayList<FIFOModel>(0);
                //AppDatabase.getRIFDSessionDao(context).deleteAll(AppCommonMethods.SessionType.SEARCH_FIFO.getValue());
                fifoDao.deleteAll();
                for(int i = 0; i < jsonFIFOProducts.length(); i++){
                  showLog("FIFOProducts[" + i + "]", jsonFIFOProducts.getJSONObject(i).toString());
                  final FIFOModel fifoModel = getGSON().fromJson(jsonFIFOProducts.getJSONObject(i).toString(), FIFOModel.class);
                  if(fifoModel != null && isNonEmpty(fifoModel.ean) && isNonEmpty(fifoModel.epc) && isNonEmpty(fifoModel.fifoDate) && Integer.parseInt(chkZero(fifoModel.zoneId, "0")) > 0){
                    fifoModel.setFifoDate(fifoModel.getFifoDate());
                    list.add(fifoModel);
                  }
                }
                if(list.size() > 0){
                  fifoDao.insertAll(list);
                  showFIFOChartAlert(fifoDao.getDatewiseList(ean));
                }
              }*/
            }
            catch(Exception e){ e.printStackTrace(); }
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}