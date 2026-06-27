package com.itek.retail.ui.search.listsearch;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isDemoApp;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;
import static com.itek.retail.common.AppCommonMethods.isUse24LengthTIDForUpload;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.OmniPickedListAdapter;
import com.itek.retail.adapter.ProductColorsListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.databinding.DialogOmniEpcSearchBinding;
import com.itek.retail.databinding.DialogSizeChartBinding;
import com.itek.retail.databinding.FragmentSearchListStartBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Product search details fragment.
 */
public class SearchListStartFragment extends RFIDSessionFragment{
  
  private SearchListStartViewModel mViewModel;
  private FragmentSearchListStartBinding binding;
  private ProductModel model;
  private ProductDao productDao;
  private InventoryDao inventoryDao;
  private UploadInventoryDao uploadInventoryDao;
  private AlertDialog styleChartAlert;
  private String searchListId = "0";
  private String searchListType = "";
  private boolean showMarkFoundBtn = false;
  
  private DialogOmniEpcSearchBinding dialogOmniEpcSearchBinding;
  private AlertDialog searchEPCDialog = null;
  private boolean isActionPick = false;
  private boolean isActionDecode = false;
  private String uploadType = "";
  private boolean isAllowDecode = false;
  private boolean isAllowDecodeOnPick = false;
  private List<Inventory> listOmniPicked = new ArrayList<>(0);
  
  /**
   * Instantiates a new Product search details fragment.
   */
  public SearchListStartFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
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
    uploadType = extractString(getArguments(), ParamConstants.OMNI_UPLOAD_TYPE, AppConstants.OMNI_UPLOAD_TYPE_COMPLETE);
    isAllowDecode = extractBoolean(getArguments(), AppConstants.IS_ALLOW_DECODE, false);
    isAllowDecodeOnPick = isAllowDecode && extractBoolean(getArguments(), AppConstants.IS_ALLOW_DECODE_ON_PICK, false);
    if(model == null){ popBackStack(); }
    
    binding.listOmniDecode.setAdapter(new OmniPickedListAdapter((MainActivity) context, this, listOmniPicked));
    binding.listOmniDecode.setLayoutManager(new LinearLayoutManager(context));
    
    binding.pdvProdSearch.setProductModel(model);
    
    binding.rgPickSearchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        if(isProcessOn()) mainViewModel.stopInventory();
        isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
        isActionDecode = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbDecode.getId();
        //binding.clOmniChannelPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.clProdSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
        //binding.llPickedDecoded.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) && chkNotNullFalse(mainViewModel.getIsDecodeOn().getValue()) ? View.VISIBLE : View.GONE);
        //binding.txtPicked.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) && chkNotNullFalse(mainViewModel.getIsDecodeOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.llCircle.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
        binding.llBtnStart.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
        binding.listOmniDecode.setVisibility(isActionDecode ? View.VISIBLE : View.GONE);
        int foundQty = isAllowDecode ? getSize() : model.getFoundQty();
        showMarkFoundBtn = (isAllowDecode || showMarkFoundBtn || foundQty > 0) && !isActionPick && !isActionDecode && !productDao.isQtyFound(model.ean);//foundQty < model.getEanQty();
        binding.btnMarkFound.setVisibility(showMarkFoundBtn && !isProcessOn() ? View.VISIBLE : View.GONE);
      }
    });
    
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
    
    //binding.txtFound.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_found), String.format(String.format(context.getString(R.string.txt_append_qty), context.getColorCode(model.getFoundQty() > 0 && model.getEanQty() == model.getFoundQty() ? R.color.green : model.getFoundQty() > 0 ? R.color.orange : R.color.err_red), model.getFoundQtyStr(), "" + model.getEanQty()))), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(binding.llBtnStart.getVisibility() != View.VISIBLE) return;
        if(styleChartAlert != null && styleChartAlert.isShowing()) return;
        if(binding.pdvProdSearch!=null && binding.pdvProdSearch.isShowingDetailAlert()) return;
        if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if(savedInstanceState == null && searchLog == null){
          JSONObject jsonExtras = null;
          try{
            jsonExtras = new JSONObject();
            jsonExtras.put(ParamConstants.ZONE, model.getZone());
            jsonExtras.put(ParamConstants.ZONE_ID, model.getZoneId(context));
            jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
            //TBD jsonExtras.put(ParamConstants.REFERENCE_EAN, model.getRefEan());
            jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
            jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
            jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
            jsonExtras.put(ParamConstants.SEARCH_LIST_ID, searchListId);
            jsonExtras.put(ParamConstants.SEARCH_LIST_TYPE, searchListType);
            jsonExtras.put(ParamConstants.TYPE, searchListType);
          }
          catch(Exception e){ e.printStackTrace(); }
          searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), model.getEanQty(), getSessionType().name(), searchListType, searchListId, jsonExtras);
        }
        toggleSearch(chkNull(mappedEan, model.getSearchEan()), searchLog, binding.clProdSearch.isSingleTagSearch());
      }
    });
    //showMarkFoundBtn = (isAllowDecode || model.getFoundQty() > 0) && model.getFoundQty() < model.getEanQty();
    binding.btnMarkFound.setText(isAllowDecodeOnPick ? R.string.pick_decode : isAllowDecode ? R.string.pick : R.string.btn_mark_found);
    
    binding.btnMarkFound.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view != null && view.getVisibility() == View.VISIBLE && !productDao.isQtyFound(model.getEan())){//model.getFoundQty() < model.getEanQty()){
          if(isAllowDecode){
            if(/*getSize() >= chkNull(model.eanQty, 0) ||*/ productDao.isQtyFound(model.ean)){
              context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_qty_matched), getTypeCharCode()));
              return;
            }
            mainViewModel.performPick(model.getEan(), isAllowDecodeOnPick);
          }
          else{
            context.showCustomAlertDialog("", String.format(getString(R.string.msg_confirm_mark_found), model.getEan()), null, getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                model.setFoundQty(model.getFoundQty() + 1);
                model.setFound(model.getFoundQty() > 0);
                productDao.update(model);
                updateCount();
                //binding.txtFound.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_found), String.format(String.format(context.getString(R.string.txt_append_qty), context.getColorCode(model.getFoundQty() > 0 && model.getEanQty() == model.getFoundQty() ? R.color.green : model.getFoundQty() > 0 ? R.color.orange : R.color.err_red), model.getFoundQtyStr(), "" + model.getEanQty()))), HtmlCompat.FROM_HTML_MODE_LEGACY));
                if(model.getFoundQty() >= model.getEanQty()){
                  showMarkFoundBtn = false;
                  binding.btnMarkFound.setVisibility(View.GONE);
                }
              }
            }, getString(R.string.btn_no));
          }
        }
      }
    });
    
    setDefaultSearchViews();
    updateCount();
    setSessionAction(AppConstants.SESSION_ACTION_START);
    return binding.getRoot();
  }
  
  private void updateCount(){
    int foundQty = isAllowDecode ? getSize() : model.getFoundQty();
    int decodeQty = isAllowDecode && sessionObject != null ? inventoryDao.getEANDecodeQty(sessionObject.sessionId, model.getEan(), model.getZone()) : 0;
    binding.txtFound.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_found), String.format(context.getString(R.string.txt_append_qty), context.getColorCode(foundQty > 0 && model.getEanQty() == foundQty ? R.color.green : foundQty > 0 ? R.color.orange : R.color.err_red), foundQty + "", "" + model.getEanQty())), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_found), String.format(context.getString(R.string.txt_append_qty), context.getColorCode(foundQty > 0 && model.getEanQty() == foundQty ? R.color.green : foundQty > 0 ? R.color.orange : R.color.err_red), foundQty + "", "" + model.getEanQty())), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtDecoded.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_decoded), String.format(context.getString(R.string.txt_append_qty), context.getColorCode(decodeQty > 0 && model.getEanQty() == decodeQty ? R.color.green : decodeQty > 0 ? R.color.orange : R.color.err_red), decodeQty + "", "" + model.getEanQty())), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtFound.setVisibility(!isAllowDecode && !isProcessOn() ? View.VISIBLE : View.GONE);
    binding.llPickedDecoded.setVisibility(isAllowDecode && foundQty > 0 && !isProcessOn() ? View.VISIBLE : View.GONE);
    binding.rgPickSearchType.setVisibility(isAllowDecode && foundQty > 0 ? View.VISIBLE : View.GONE);
    showMarkFoundBtn = (isAllowDecode || showMarkFoundBtn || foundQty > 0) && !isActionPick && !isActionDecode && !productDao.isQtyFound(model.ean);// && foundQty < model.getEanQty();
    binding.btnMarkFound.setVisibility(showMarkFoundBtn && !isProcessOn() ? View.VISIBLE : View.GONE);
    if(foundQty == 0 && binding.rgPickSearchType.getCheckedRadioButtonId() != R.id.rbSearch){
      binding.rbSearch.setChecked(true);
    }
  }
  
  @Override
  public void updateLists(){
    if(isAllowDecode && binding != null && binding.listOmniDecode != null && binding.listOmniDecode.getAdapter() != null && binding.listOmniDecode.getAdapter() instanceof RecyclerView.Adapter){
      listOmniPicked.clear();
      //listOmniPicked.addAll(fifoDao.getList(model.getEan(), model.getFifoDate()));
      listOmniPicked.addAll(inventoryDao.getEANInventory(sessionObject.sessionId, model.getEan(), model.getZone()));
      ((RecyclerView.Adapter) binding.listOmniDecode.getAdapter()).notifyDataSetChanged();
    }
  }
  
  public void apiCall(String action){
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    if(isInternetConnected(context, false, isUpload)){
      try{
        //if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.ZONE, model.getZone());
        requestParams.put(ParamConstants.ZONE_ID, model.getZoneId(context));
        requestParams.put(ParamConstants.SEARCH_LIST_ID, searchListId);
        requestParams.put(ParamConstants.SEARCH_LIST_TYPE, searchListType);
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        requestParams.put(ParamConstants.EAN, model.getEan());
        requestParams.put(ParamConstants.SKU_ID, model.getEan());
        requestParams.put(ParamConstants.QTY, model.getTotalQty());
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        if(sessionObject != null)
          requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
        if(isUpload){
          AppCommonMethods.showLog("requestParams", "" + (requestParams!=null));
          //showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          /*new Handler().post(new Runnable(){
            @Override
            public void run(){*/
              try{
                JSONArray js = new JSONArray();
                if(model != null){
                  for(Inventory inventory : inventoryDao.getAllInventoryData(sessionObject.sessionId)){
                    AppCommonMethods.showLog("inventory", "" + (inventory != null && isUpload && !inventory.isUploaded && inventory.isDecoded()));
                    if(inventory != null && isUpload && !inventory.isUploaded && inventory.isDecoded()){
                      JSONObject dataobject = inventory.toJson(context);
                      AppCommonMethods.showLog("dataobject", "" + (dataobject != null && chkNull(dataobject.toString(), "").length() > 2));
                      if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                        js.put(dataobject);
                    }
                  }
                }
                requestParams.put(ParamConstants.ITEMS, js);
                AppCommonMethods.showLog("requestParams", "" + (requestParams!=null));
                AppCommonMethods.showLog("js", "" + (js != null && js.length() > 0));
                if(js != null && js.length() > 0)
                  callWebService(URLConstants.UPLOAD_ENCODE, requestParams, /*args,*/false, getString(R.string.progress_msg_uploading_data), !isUpload);
              }
              catch(Exception e){
                e.printStackTrace();
                hideProgressDialog();
              }
            /*}
          });*/
        }
        else
          callWebService(isUpload ? URLConstants.UPLOAD_ENCODE : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload);
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
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
    lvProdColors.setAdapter(new ProductColorsListAdapter((MainActivity) context, SearchListStartFragment.this, styleChartAlert, lvProdSizes, listColors, model));
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
    if(!showMarkFoundBtn && result >= AppCommonMethods.markFoundPercentLBS && model.getFoundQty() < model.getEanQty())
      showMarkFoundBtn = true;
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    if(isSearchOn == null) return;
    else{
      //if(binding.clProdSearch!=null && binding.clProdSearch instanceof SearchView)
      //((SearchView)binding.clProdSearch).setEnableCheck(!isSearchOn);
      //binding.pdvProdSearch.setEnabled(!isSearchOn);
      //binding.btnSizeChart.setEnabled(!isSearchOn);
      if(!isSearchOn) stopTimer();
        //else startTimer(binding.clProdSearch, binding.imgSearchDir);
      else
        startTimer(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null ? dialogOmniEpcSearchBinding.clOmniChannelEpcSearch : binding.clProdSearch, dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.imgSearchDir != null ? dialogOmniEpcSearchBinding.imgSearchDir : binding.imgSearchDir);
      if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
        dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.setEnableCheck(!isSearchOn);
      if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null && dialogOmniEpcSearchBinding.btnDecode != null){
        dialogOmniEpcSearchBinding.llBtnStart.toggle(isSearchOn);
        final Object tag = dialogOmniEpcSearchBinding.btnDecode.getTag();
        final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
        dialogOmniEpcSearchBinding.btnDecode.setVisibility(inventory != null && !inventory.isDecoded() && !isSearchOn ? View.VISIBLE : View.GONE);
      }
      else{
        binding.llBtnStart.toggle(isSearchOn);
        updateViews();
        //binding.btnMarkFound.setVisibility(!isSearchOn && showMarkFoundBtn ? View.VISIBLE : View.GONE);
      }
      //binding.llBtnStart.toggle(isSearchOn);
      //binding.btnMarkFound.setVisibility(!isSearchOn && showMarkFoundBtn ? View.VISIBLE : View.GONE);
    }
  }
  
  @Override
  protected void isPickOnChanged(Boolean isPickOn){
    super.isPickOnChanged(isPickOn);
    if(isPickOn == null) return;
    else{
      updateViews();
      if(!isPickOn){
        stopTimer();
      }
      binding.llBtnStart.toggle(isPickOn);
    }
  }
  
  @Override
  protected void onPickDataChanged(Inventory pickData){
    super.onPickDataChanged(pickData);
    if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
    if(pickData != null && isAllowDecodeOnPick){
      showLog("EAN", pickData.ean);
      showLog("EPC", pickData.epc);
      showLog("TID", pickData.tid);
      showLog("PC", pickData.pcdata);
      try{
        startDecode(pickData);
      }
      catch(Exception e){ e.printStackTrace(); }
    }
  }
  
  @Override
  protected void isDecodeOnChanged(Boolean isDecodeOn){
    super.isDecodeOnChanged(isDecodeOn);
    if(isDecodeOn == null) return;
    else{
      if(chkNotNullFalse(isDecodeOn)){
        AppCommonMethods.showLog("isDecodeOn", "" + false);
        if(sessionObject != null && chkNotNullTrue(mainViewModel.getIsDecodeDone().getValue())){//Case Tag Write Success
          AppCommonMethods.showLog("isDecodeDone", "" + true);
          if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.btnDecode != null){
            final Object tag = dialogOmniEpcSearchBinding.btnDecode.getTag();
            final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
            AppCommonMethods.showLog("inventory", "" + (inventory!=null));
            inventory.isUploaded = false;
            if(isNullOrEmpty(inventory.newEpc)) inventory.newEpc = "0" + inventory.epc.substring(1);
            AppCommonMethods.showLog("dialogOmniEpcSearchBinding", "" + (dialogOmniEpcSearchBinding!=null));
            AppCommonMethods.showLog("searchEPCDialog", "" + (searchEPCDialog!=null));
            dialogOmniEpcSearchBinding.btnDecode.setTag(inventory);
            dialogOmniEpcSearchBinding.btnDecode.setVisibility(inventory != null && !inventory.isDecoded() && !isProcessOn() ? View.VISIBLE : View.GONE);
          }
          apiCall(AppConstants.SESSION_ACTION_UPLOAD);
          mainViewModel.getIsDecodeDone().postValue(null);
          updateLists();
        }
        else if(sessionObject != null && chkNotNullFalse(mainViewModel.getIsDecodeDone().getValue())){//Case Tag Write Fail
          mainViewModel.getIsDecodeDone().postValue(null);
        }
      }
      updateViews();
    }
  }
  
  private void updateViews(){
    final boolean isProcessOn = chkNotNullTrue(mainViewModel.getIsDecodeOn().getValue()) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) || chkNotNullTrue(mainViewModel.getIsSearchOn().getValue());
    if(dialogOmniEpcSearchBinding != null){
      dialogOmniEpcSearchBinding.btnDecode.setEnabled(!isProcessOn);
      dialogOmniEpcSearchBinding.llBtnStart.setEnabled(!chkNotNullTrue(mainViewModel.getIsDecodeOn().getValue()));
    }
    binding.pdvProdSearch.setEnabled(!isProcessOn);
    binding.btnSizeChart.setEnabled(!isProcessOn);
    binding.rgPickSearchType.setEnabled(!isProcessOn);
    binding.rgPickSearchType.setVisibility(!isProcessOn?View.VISIBLE:View.GONE);
    binding.rbPick.setEnabled(!isProcessOn);
    binding.rbSearch.setEnabled(!isProcessOn);
    binding.rbDecode.setEnabled(isAllowDecode && !isProcessOn);
    
    updateCount();
    //binding.btnMarkFound.setVisibility(!isProcessOn && showMarkFoundBtn ? View.VISIBLE : View.GONE);
    //binding.txtFound.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_found), String.format(String.format(context.getString(R.string.txt_append_qty), context.getColorCode(model.getFoundQty() > 0 && model.getEanQty() == model.getFoundQty() ? R.color.green : model.getFoundQty() > 0 ? R.color.orange : R.color.err_red), model.getFoundQtyStr(), "" + model.getEanQty()))), HtmlCompat.FROM_HTML_MODE_LEGACY));
    //binding.llPickedDecoded.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
    //binding.txtPicked.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
    //binding.txtDecoded.setVisibility(isAllowDecode && getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    size = chkNull(size, 0) > 0 ? inventoryDao.getEANQty(sessionObject.sessionId, model.getEan(), model.getZone()) : size;
    updateCount();
    updateLists();
  }
  
  @Override
  protected void onTriggerPressed(){
    showLog(this.getClass().getSimpleName(), "onTriggerPressed");
    if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null)
      dialogOmniEpcSearchBinding.llBtnStart.performClick();
    else binding.llBtnStart.performClick();
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
  
  public void startDecode(Inventory inventory){
    if(isAllowDecode && !isProcessOn() && inventory != null && !inventory.isDecoded() && isNonEmpty(inventory.epc) && inventory.epc.length() > 1 && !inventory.epc.startsWith("0") && (isNullOrEmpty(inventory.newEpc) || !inventory.newEpc.startsWith("0"))){
      mainViewModel.performDecoding(inventory);
    }
  }
  
  public void removeNonDecodedTag(Inventory inventory){
    if(isAllowDecode && !isProcessOn() && inventory != null && !inventory.isUploaded && !inventory.isDecoded()){
      context.showCustomAlertDialog("", String.format(getString(R.string.msg_list_delete_tag), inventory.ean), R.string.btn_delete_all, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          //UnPick/Remove Tag
          if(isAllowDecode && !isProcessOn() && inventory != null && !inventory.isUploaded && !inventory.isDecoded()){
            inventoryDao.deleteInventoryData(inventory);
            productDao.updateDeletedEPC(inventory.epc, model.ean, model.zone);
          }
        }
      }, R.string.btn_cancel, null);
      
    }
  }
  
  @Override
  public void startEPCSearch(Inventory inventory){
    if(isAllowDecode && !isProcessOn() && inventory != null){
      final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
      setAlertDialogCustomTitle(alertDialog, R.string.search);
      DisplayMetrics displayMetrics = new DisplayMetrics();
      context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
      final int wid = (context.isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / 2;
      DialogOmniEpcSearchBinding binding = DialogOmniEpcSearchBinding.inflate(LayoutInflater.from(context), null, false);
      LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(wid, wid);
      binding.clOmniEPCSearch.setLayoutParams(llParams);
      binding.btnDecode.setTag(inventory);
      binding.btnDecode.setVisibility(inventory != null && !inventory.isDecoded() && !isProcessOn() ? View.VISIBLE : View.GONE);
      binding.btnDecode.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          if(!isProcessOn() && inventory != null && !inventory.isDecoded()){
            startDecode(inventory);
          }
        }
      });
      binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          if(binding.llBtnStart.getVisibility() != View.VISIBLE) return;
          if(styleChartAlert != null && styleChartAlert.isShowing()) return;
          if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
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
          searchEPCDialog = alertDialog;
        }
      });
      alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
        @Override
        public void onDismiss(DialogInterface dialogInterface){
          if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
            mainViewModel.stopInventory();
          dialogOmniEpcSearchBinding = null;
          searchEPCDialog = null;
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
    else if(isAllowDecode && isProcessOn() && inventory != null)
      showShortToast(R.string.not_allowed);
    //showShortToast(String.format(getString(R.string.err_op_not_allowed),getTypeCharCode(),getSessionType().name()));
  }
  
  /**
   * Set session action.
   *
   * @param action the action
   */
  public void setSessionAction(String action){
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.eans = model.getEan();
      sessionObject.zone = model.getZone();
      sessionObject.zoneId = model.getZoneId();
      sessionObject.brands = model.getBrand();
      sessionObject.category = model.getCategory();
      sessionObject.total = model.getEanQtyStr();
      sessionObject.sessionType = AppCommonMethods.SessionType.SEARCH_LIST.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.SEARCH.getValue();
      try{
        JSONObject jsonExtras = new JSONObject();
        jsonExtras.put(ParamConstants.SEARCH_LIST_ID, searchListId);
        jsonExtras.put(ParamConstants.SEARCH_LIST_TYPE, searchListType);
        jsonExtras.put(ParamConstants.EAN, model.getEan());
        jsonExtras.put(ParamConstants.QTY, model.getTotalQty());
        jsonExtras.put(ParamConstants.ZONE, model.getZone());
        jsonExtras.put(ParamConstants.ZONE_ID, model.getZoneId(context));
        sessionObject.extras = jsonExtras.toString();
      }
      catch(Exception e){ e.printStackTrace(); }
      Calendar cc = Calendar.getInstance();
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, 24);
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc);
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, false);
      //apiCall(action);
    }
    else if(sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      sessionObject.eans = model.getEan();
      sessionObject.zone = model.getZone();
      sessionObject.zoneId = model.getZoneId();
      sessionObject.brands = model.getBrand();
      sessionObject.category = model.getCategory();
      sessionObject.total = model.getEanQtyStr();
      sessionObject.sessionType = AppCommonMethods.SessionType.SEARCH_LIST.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.SEARCH.getValue();
      try{
        JSONObject jsonExtras = new JSONObject();
        jsonExtras.put(ParamConstants.SEARCH_LIST_ID, searchListId);
        jsonExtras.put(ParamConstants.SEARCH_LIST_TYPE, searchListType);
        jsonExtras.put(ParamConstants.EAN, model.getEan());
        jsonExtras.put(ParamConstants.QTY, model.getTotalQty());
        jsonExtras.put(ParamConstants.ZONE, model.getZone());
        jsonExtras.put(ParamConstants.ZONE_ID, model.getZoneId(context));
        sessionObject.extras = jsonExtras.toString();
      }
      catch(Exception e){ e.printStackTrace(); }
      mainViewModel.startSession(sessionObject, false);
    }
    else if(sessionObject != null && !action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      mainViewModel.startSession(sessionObject, false);
    }
  }
  
  @Override
  public int getSize(){
    int size = super.getSize();
    return sessionObject != null && model != null && inventoryDao != null ? inventoryDao.getEANQty(sessionObject.sessionId, model.getEan(), model.getZone()) : size;
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
        case URLConstants.UPLOAD_ENCODE:
          if(isSuccess){
            final int sessionType = sessionObject != null ? sessionObject.sessionType : 0;
            inventoryDao.updateUploaded(sessionObject.sessionId);
            if(sessionType > 0){
              /*final boolean isAllDecoded = isAllDecoded();
              final int decodeQty = inventoryDao.getEANDecodeQty(sessionObject.sessionId, model.getEan(), model.getZone());
              final boolean isEanZoneDecoded = isAutoBackOnEanZoneDecoded && decodeQty >= model.getEanQty();
              showLog("isAllDecoded", "" + isAllDecoded);*/
              /*if(searchEPCDialog != null && searchEPCDialog.isShowing())
                searchEPCDialog.dismiss();*/
              context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_decode), getTypeCharCode())).replaceFirst("(?i)Upload", "Decode"), false/*isAllDecoded || isEanZoneDecoded*/ ? new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  //if(isAllDecoded) context.doublePopBackStack();
                  //else if(isEanZoneDecoded) context.popBackStack();
                }
              } : null);
              //update Uploaded Records
              new Handler().post(new Runnable(){
                @Override
                public void run(){
                  //if(isAllDecoded) inventoryDao.deleteInventory(sessionType);
                  JSONArray js = extractJSONArray(jsonRequest, ParamConstants.ITEMS);
                  Set<String> tids = new HashSet<String>(0);
                  if(isNonEmpty(js)){
                    for(int i = 0; i < js.length(); i++){
                      try{
                        final String tid = extractString(js.getJSONObject(i), ParamConstants.TID, "").trim();
                        if(isNonEmpty(tid))
                          tids.add(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid);
                      }
                      catch(Exception e){ e.printStackTrace(); }
                    }
                  }
                  if(isNonEmpty(tids)){
                    /*if(!isAllDecoded)*/
                    inventoryDao.updateUploaded(sessionType, tids);
                    uploadInventoryDao.updateUploaded(sessionType, tids);
                  }
                  uploadInventoryDao.deleteUploaded();
                  updateLists();
                }
              });
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