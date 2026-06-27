package com.itek.retail.ui.search.productsearch;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isDemoApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
import com.itek.retail.common.InsertDBFIFOs;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.FIFODao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.DialogSimilarStylesBinding;
import com.itek.retail.databinding.DialogSizeChartBinding;
import com.itek.retail.databinding.FragmentProductSearchDetailsBinding;
import com.itek.retail.model.FIFOModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * The Product search details fragment.
 */
public class ProductSearchDetailsFragment extends RFIDSessionFragment{
  
  /**
   * The current orientation provider that delivers device orientation.
   */
  private ProductSearchDetailsViewModel mViewModel;
  private FragmentProductSearchDetailsBinding binding;
  private ProductModel model;
  private ProductDao productDao;
  private AlertDialog styleChartAlert;
  
  /**
   * Instantiates a new Product search details fragment.
   */
  public ProductSearchDetailsFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
    if(isAllowDirectionalSearch) mainViewModel.getSensorAndStart();
  }
  
  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ProductSearchDetailsViewModel.class);
    binding = FragmentProductSearchDetailsBinding.inflate(inflater, container, false);
    final String mappedEan = !SharedPrefManager.getIsEANMapped() ? "" : extractString(getArguments(), ParamConstants.MAPPED_EAN, "");
    final Object obj = extractSerializable(getArguments(), ProductModel.class);
    model = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    //mappedEan = SharedPrefManager.getBoolean(ParamConstants.IS_USE_REFERENCE_EAN,AppCommonMethods.isUseReferenceEan) && model!=null && isNonEmpty(model.getRefEan())?model.getRefEan():"";
    if(model == null){ popBackStack(); }
    searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), 1, getSessionType().name());
    binding.pdvProdSearch.setProductModel(model, chkNull(model.getZone(), AppConstants.DEFAULT_NO_VALUE).equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
    binding.pdvProdSearch.setFragmentButton(this, R.string.tab_check_availability);
    
    //if(binding != null && binding.imgSearchDir != null)
    //binding.imgSearchDir.setColorFilter(binding.imgSearchDir.getContext().getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
    
    binding.btnSizeChart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view.getVisibility() == View.VISIBLE)
          callStyleChartAPI(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_CHART));
      }
    });
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(styleChartAlert != null && styleChartAlert.isShowing()) return;
        if(binding.pdvProdSearch!=null && binding.pdvProdSearch.isShowingDetailAlert()) return;
        toggleSearch(chkNull(mappedEan, model.getSearchEan()), searchLog, binding.clProdSearch.isSingleTagSearch());
      }
    });
    
    setDefaultSearchViews();
    setSessionAction(AppConstants.SESSION_ACTION_START);
    return binding.getRoot();
  }
  
  public void callStyleChartAPI(final boolean isShowFIFOChart){
    if(!chkNotNullTrue(mainViewModel.getIsProcessOn().getValue()) && (styleChartAlert == null || !styleChartAlert.isShowing())){
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
          callWebService(URLConstants.GET_SIZE_CHART, jsonRequest, getString(R.string.progress_msg_getting_data), false, true);
          
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    }
  }
  
  /**
   * Set product.
   *
   * @param args the args
   */
  public void setProduct(Bundle args){
    final Object obj = extractSerializable(args, ProductModel.class);
    model = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    if(model == null){ popBackStack(); }
    productDao.deleteAll(AppCommonMethods.SessionType.SEARCH.getValue());
    binding.pdvProdSearch.setProductModel(model);
  }
  
  /**
   * Get model product model.
   *
   * @return the product model
   */
  public ProductModel getModel(){ return model; }
  
  /**
   * Show style chart alert.
   */
  public void showStyleChartAlert(){
    styleChartAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    setAlertDialogCustomTitle(styleChartAlert, R.string.tab_size_chart);
    final DialogSizeChartBinding binding = DialogSizeChartBinding.inflate(LayoutInflater.from(context));
    
    final RecyclerView lvProdColors = binding.listDialogSizeChartColors;
    final RecyclerView lvProdSizes = binding.listDialogSizeChart;
    List<String> listColors = productDao.getColorList();
    if(isNonEmpty(listColors) && listColors.contains(model.getColor()) && listColors.indexOf(model.getColor()) > 0){
      listColors.remove(model.getColor());
      listColors.add(0, model.getColor());
    }
    lvProdColors.setAdapter(new ProductColorsListAdapter((MainActivity) context, ProductSearchDetailsFragment.this, styleChartAlert, lvProdSizes, listColors, model));
    lvProdColors.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
    styleChartAlert.setView(binding.getRoot());
    styleChartAlert.setCancelable(false);
    styleChartAlert.show();
  }
  
  /**
   * Show fifo style chart alert.
   */
  public void showFIFOChartAlert(){
    showFIFOChartAlert(AppDatabase.getFIFODao(context).getDatewiseList(model.ean));
  }
  
  public void showFIFOChartAlert(final List<FIFOModel> listFIFO){
    if(isNonEmpty(listFIFO)){
      styleChartAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
      setAlertDialogCustomTitle(styleChartAlert, R.string.tab_fifo_products);
      final DialogSimilarStylesBinding binding = DialogSimilarStylesBinding.inflate(LayoutInflater.from(context));
      binding.listDialogSimilarStyles.setAdapter(new FIFOChartListAdapter((MainActivity) context, ProductSearchDetailsFragment.this, styleChartAlert, listFIFO, model));
      binding.listDialogSimilarStyles.setLayoutManager(new LinearLayoutManager(context));
      binding.listDialogSimilarStyles.setVisibility(View.VISIBLE);
      styleChartAlert.setView(binding.getRoot());
      styleChartAlert.setCancelable(false);
      styleChartAlert.show();
    }
  }
  
  /**
   * Set default search views.
   */
  protected void setDefaultSearchViews(){
    super.setDefaultSearchViews();
    //if(binding.clProdSearch instanceof SearchView)
    //((SearchView)binding.clProdSearch).resetToDefault();
    //if(binding.clProdSearch instanceof PointerSpeedometer)
    //((PointerSpeedometer) binding.clProdSearch).resetToDefault();
    //updateSearchDir(0, 0.0,0.0,0.0, "", 0);
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    if(isSearchOn == null) return;
    else{
      binding.pdvProdSearch.setEnabled(!isSearchOn);
      binding.btnSizeChart.setEnabled(!isSearchOn);
      if(!isSearchOn) stopTimer();
      else{
        startTimer(binding.clProdSearch, binding.imgSearchDir);
      }
      binding.llBtnStart.toggle(isSearchOn);
    }
  }
  
  @Override
  protected void onTriggerPressed(){
    showLog(this.getClass().getSimpleName(), "onTriggerPressed");
    binding.llBtnStart.performClick();
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      new Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
          if(sessionObject != null && !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))
            mainViewModel.startSession(sessionObject, false);
          
        }
      }, 300);
    }
    
  }
  
  @Override
  public void onDestroy(){
    if(productDao != null) productDao.deleteAll(AppCommonMethods.SessionType.SEARCH.getValue());
    super.onDestroy();
  }
  
  @Override
  public void onBackPressed(){
    super.onBackPressed();
  }
  
  public void updateSearch(final JSONArray jsonArray){
    context.runOnUiThread(new Runnable(){
      @Override
      public void run(){
        if(binding != null && binding.pdvProdSearch != null){
          final ProductModel productModel = binding.pdvProdSearch.getProductModel();
          productModel.setDisplayData(jsonArray.toString());
          binding.pdvProdSearch.setProductModel(productModel);
        }
      }
    });
  }
  
  /**
   * Set session action.
   *
   * @param action the action
   */
  public void setSessionAction(String action){
    if(sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START))
      mainViewModel.startSession(sessionObject, false);
    else if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      
      sessionObject.sessionType = AppCommonMethods.SessionType.SEARCH.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.SEARCH.getValue();
      Calendar cc = Calendar.getInstance();
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, 24);
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc);
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, false);
      apiCall(action);
    }
    else if(sessionObject != null && !action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      //TODO
    }
  }
  
  /**
   * Stop session.
   */
  public void stopSession(){
    mainViewModel.stopSession(sessionObject, false);
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.SET_SESSION:
          break;
        case URLConstants.GET_SIZE_CHART:
          productDao.deleteAll(AppCommonMethods.SessionType.SEARCH.getValue());
          if(isDemoApp && isStaticDebug(context)) isSuccess = false;
          if(isSuccess && jsonResponse != null){
            final JSONArray jsonArraySizeChartProducts = extractJSONArray(jsonResponse, ParamConstants.SIZE_CHART_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(jsonArraySizeChartProducts != null && context instanceof MainActivity)
              ((MainActivity) context).callInsertProductDBTask(this, url, AppCommonMethods.SessionType.SEARCH.getValue(), jsonRequest, jsonResponse, args, jsonArraySizeChartProducts);
            /*else if(isNonEmpty(jsonArraySizeChartProducts)){
              showLog("jsonArraySizeChartProducts", "" + jsonArraySizeChartProducts.length());
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