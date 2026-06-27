package com.itek.retail.ui.search.assortment;

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
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.ProductColorsListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.DialogSizeChartBinding;
import com.itek.retail.databinding.FragmentSearchListStartBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.listsearch.SearchListStartViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * The Product search details fragment.
 */
public class SearchAssortStartFragment extends RFIDSessionFragment{
  
  private SearchListStartViewModel mViewModel;
  private FragmentSearchListStartBinding binding;
  private ProductModel model;
  private ProductDao productDao;
  private AlertDialog styleChartAlert;
  private String searchListId = "0";
  private String searchListType = "";
  private boolean showMarkFoundBtn = false;
  
  /**
   * Instantiates a new Product search details fragment.
   */
  public SearchAssortStartFragment(){
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
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(SearchListStartViewModel.class);
    binding = FragmentSearchListStartBinding.inflate(inflater, container, false);
    final String mappedEan = !SharedPrefManager.getIsEANMapped() ? "" : extractString(getArguments(), ParamConstants.MAPPED_EAN, "");
    Serializable obj = extractSerializable(getArguments(), ProductModel.class);
    model = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    searchListId = extractString(getArguments(), AppConstants.SEARCH_LIST_ID, "0");
    searchListType = extractString(getArguments(), AppConstants.SEARCH_LIST_TYPE, "");
    if(model == null){ popBackStack(); }
    
    binding.pdvProdSearch.setProductModel(model);
    
    binding.btnSizeChart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(!chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()) && (styleChartAlert == null || !styleChartAlert.isShowing())){
          try{
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put(ParamConstants.EAN, model.getEan());
            callWebService(URLConstants.GET_SIZE_CHART, jsonRequest, getString(R.string.progress_msg_getting_data), false, true);
          }
          catch(Exception e){ e.printStackTrace(); }
        }
      }
    });
    
    binding.txtFound.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_found), String.format(String.format(context.getString(R.string.txt_append_qty), context.getColorCode(model.getFoundQty() > 0 && model.getEanQty() == model.getFoundQty() ? R.color.green : model.getFoundQty() > 0 ? R.color.orange : R.color.err_red), model.getFoundQtyStr(), "" + model.getEanQty()))), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(styleChartAlert != null && styleChartAlert.isShowing()) return;
        if(binding.pdvProdSearch!=null && binding.pdvProdSearch.isShowingDetailAlert()) return;
        if(savedInstanceState == null && searchLog == null){
          JSONObject jsonExtras = null;
          try{
            jsonExtras = new JSONObject();
            jsonExtras.put(ParamConstants.ZONE, model.getZone());
            jsonExtras.put(ParamConstants.ZONE_ID, model.getZoneId(context));
            jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
            jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
            jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
            jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
            jsonExtras.put(ParamConstants.TYPE, searchListType);
            jsonExtras.put(ParamConstants.SEARCH_LIST_TYPE, searchListType);
            jsonExtras.put(ParamConstants.SEARCH_LIST_ID, searchListId);
          }
          catch(Exception e){ e.printStackTrace(); }
          searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), model.getEanQty(), getSessionType().name(), searchListType, searchListId, jsonExtras);
        }
        toggleSearch(chkNull(mappedEan, model.getSearchEan()), searchLog, binding.clProdSearch.isSingleTagSearch());
      }
    });
    showMarkFoundBtn = model.getFoundQty() > 0 && model.getFoundQty() < model.getEanQty();
    binding.btnMarkFound.setVisibility(showMarkFoundBtn ? View.VISIBLE : View.GONE);
    binding.btnMarkFound.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view != null && view.getVisibility() == View.VISIBLE && model.getFoundQty() < model.getEanQty()){
          context.showCustomAlertDialog("", String.format(getString(R.string.msg_confirm_mark_found), model.getEan()), null, getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              model.setFoundQty(model.getFoundQty() + 1);
              model.setFound(model.getFoundQty() > 0);
              productDao.update(model);
              binding.txtFound.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_found), String.format(String.format(context.getString(R.string.txt_append_qty), context.getColorCode(model.getFoundQty() > 0 && model.getEanQty() == model.getFoundQty() ? R.color.green : model.getFoundQty() > 0 ? R.color.orange : R.color.err_red), model.getFoundQtyStr(), "" + model.getEanQty()))), HtmlCompat.FROM_HTML_MODE_LEGACY));
              if(model.getFoundQty() >= model.getEanQty()){
                showMarkFoundBtn = false;
                binding.btnMarkFound.setVisibility(View.GONE);
              }
            }
          }, getString(R.string.btn_no));
        }
      }
    });
    
    setDefaultSearchViews();
    setSessionAction(AppConstants.SESSION_ACTION_START);
    return binding.getRoot();
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
    lvProdColors.setAdapter(new ProductColorsListAdapter((MainActivity) context, SearchAssortStartFragment.this, styleChartAlert, lvProdSizes, listColors, model));
    lvProdColors.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
    styleChartAlert.setView(binding.getRoot());
    styleChartAlert.setCancelable(false);
    styleChartAlert.show();
  }
  
  /**
   * Set default search views.
   */
  @Override
  protected void setDefaultSearchViews(){
    super.setDefaultSearchViews();
    binding.clProdSearch.resetToDefault();
  }
  
  @Override
  protected void updateSearchUI(final int result){
    super.updateSearchUI(result);
    if(!showMarkFoundBtn && result >= AppCommonMethods.markFoundPercentAssortmentSearch && model.getFoundQty() < model.getEanQty())
      showMarkFoundBtn = true;
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    if(isSearchOn == null) return;
    else{
      //if(binding.clProdSearch!=null && binding.clProdSearch instanceof SearchView)
      //((SearchView)binding.clProdSearch).setEnableCheck(!isSearchOn);
      binding.pdvProdSearch.setEnabled(!isSearchOn);
      binding.btnSizeChart.setEnabled(!isSearchOn);
      if(!isSearchOn) stopTimer();
      else startTimer(binding.clProdSearch, binding.imgSearchDir);
      binding.llBtnStart.toggle(isSearchOn);
      binding.btnMarkFound.setVisibility(!isSearchOn && showMarkFoundBtn ? View.VISIBLE : View.GONE);
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
      sessionObject.sessionType = AppCommonMethods.SessionType.SEARCH_LIST.getValue();
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
           /* else if(isNonEmpty(jsonArraySizeChartProducts)){
              for(int i = 0; i < jsonArraySizeChartProducts.length(); i++){
                final JSONObject productJson = jsonArraySizeChartProducts.getJSONObject(i);
                final ProductModel productModel = getGSON().fromJson(productJson.toString(), ProductModel.class);
                if(productModel != null){
                  productModel.setSessionType(AppCommonMethods.SessionType.SEARCH.getValue());
                  productModel.setItemImgUrl(extractString(productJson, ParamConstants.IMG_URL, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim());
                  final JSONArray jsonZones = extractJSONArray(productJson, ParamConstants.ZONES);
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
          if(chkNull(productDao.getAllTotal(), 0) > 0) showStyleChartAlert();
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}