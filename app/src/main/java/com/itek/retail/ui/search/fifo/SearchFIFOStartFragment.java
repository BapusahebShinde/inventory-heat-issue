package com.itek.retail.ui.search.fifo;

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
import static com.itek.retail.common.AppCommonMethods.isSinglePick;
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
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.FIFOChartListAdapter;
import com.itek.retail.adapter.OmniPickedListAdapter;
import com.itek.retail.adapter.ProductColorsListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.FIFODao;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.databinding.DialogOmniEpcSearchBinding;
import com.itek.retail.databinding.DialogSimilarStylesBinding;
import com.itek.retail.databinding.DialogSizeChartBinding;
import com.itek.retail.databinding.FragmentFifoSearchStartBinding;
import com.itek.retail.model.FIFOModel;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Product search details fragment.
 */
public class SearchFIFOStartFragment extends RFIDSessionFragment{
  
  private SearchFIFOStartViewModel mViewModel;
  private FragmentFifoSearchStartBinding binding;
  private ProductModel model;
  private List<FIFOModel> listFIFO = new ArrayList(0);
  private String ean = "", fifoDate = "", zone = "", zoneId = "";
  private Integer age = -1;
  private ProductDao productDao;
  private FIFODao fifoDao;
  private InventoryDao inventoryDao;
  private AlertDialog styleChartAlert;
  private DialogOmniEpcSearchBinding dialogOmniEpcSearchBinding;
  private AlertDialog searchEPCDialog = null;
  private boolean isActionPick = false;
  private boolean isActionDecode = false;
  private boolean isAllowDecode = false;
  private boolean isAllowDecodeOnPick = false;
  private List<Inventory> listOmniPicked = new ArrayList<>(0);
  
  /**
   * Instantiates a new Product search details fragment.
   */
  public SearchFIFOStartFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    //inventoryDao = AppDatabase.getInventoryDao(context);
    productDao = AppDatabase.getProductDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    fifoDao = AppDatabase.getFIFODao(context);
    if(isAllowDirectionalSearch) mainViewModel.getSensorAndStart();
    
  }
  
  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(SearchFIFOStartViewModel.class);
    binding = FragmentFifoSearchStartBinding.inflate(inflater, container, false);
    final String mappedEan = !SharedPrefManager.getIsEANMapped() ? "" : extractString(getArguments(), ParamConstants.MAPPED_EAN, "");
    final Object obj = extractSerializable(getArguments(), ProductModel.class);
    model = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    if(model == null){ popBackStack(); }
    ean = getArguments().getString(ParamConstants.EAN, model.getEan());
    fifoDate = getArguments().getString(ParamConstants.FIFO_DATE, model.getFifoDate());
    zone = getArguments().getString(ParamConstants.ZONE_NAME, model.getZone());
    zoneId = getArguments().getString(ParamConstants.ZONE_ID, model.getZoneId());
    age = getArguments().getInt(ParamConstants.AGE, model.getStockAge());
    JSONObject jsonExtras = null;
    try{
      jsonExtras = new JSONObject();
      jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
      jsonExtras.put(ParamConstants.EPC, chkNull(mappedEan, model.getEpc()));
      jsonExtras.put(ParamConstants.FIFO_DATE, model.getFifoDate());
      jsonExtras.put(ParamConstants.AGE, model.getStockAge());
      jsonExtras.put(ParamConstants.QTY, model.getTotalQtyStr());
      jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
      jsonExtras.put(ParamConstants.TYPE, getSessionType().name());
      jsonExtras.put(ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
      jsonExtras.put(ParamConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecodeOnPick);
    }
    catch(Exception e){ e.printStackTrace(); }
    searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), model.getTotalQty(), getSessionType().name(), fifoDate, jsonExtras);
    
    binding.listOmniDecode.setAdapter(new OmniPickedListAdapter((MainActivity) context, this, listOmniPicked));
    binding.listOmniDecode.setLayoutManager(new LinearLayoutManager(context));
    
    isAllowDecode = extractBoolean(getArguments(), ParamConstants.IS_ALLOW_DECODE, false);
    isAllowDecodeOnPick = isAllowDecode && extractBoolean(getArguments(), ParamConstants.IS_ALLOW_DECODE_ON_PICK, AppCommonMethods.isAllowDecodeOnPick);
    //binding.txtOmniChannelScoreCount.setText("" + 0);
    binding.ctwInventoryStart.setScore(0);
   // binding.txtOmniChannelStartTotal.setText("" + model.getTotalQty());
    binding.ctwInventoryStart.setTotal(model.getTotalQty());
    
    binding.pdvProdSearch.setProductModel(model, chkNull(model.getZone(), AppConstants.DEFAULT_NO_VALUE).equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
    binding.pdvProdSearch.setFragmentButton(this, R.string.tab_check_availability);
    
    isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
    isActionDecode = isAllowDecode && binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbDecode.getId();
    
   // binding.clOmniChannelPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.clProdSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
    binding.llCircle.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
    binding.llBtnStart.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
    binding.listOmniDecode.setVisibility(isActionDecode ? View.VISIBLE : View.GONE);
    
    binding.rbPick.setSelected(true);
    binding.rbSearch.setSelected(true);
    binding.rbDecode.setSelected(true);
    
    binding.divPick.setVisibility(!isAllowDecode || getSize() <= 0 ? View.VISIBLE : View.GONE);
    binding.rbDecode.setVisibility(isAllowDecode && getSize() > 0 ? View.VISIBLE : View.GONE);
    
    //binding.rgPickSearchType.setVisibility(isAllowDecode ? View.VISIBLE : View.GONE);
    
    binding.rgPickSearchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        if(isProcessOn()) mainViewModel.stopInventory();
        isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
        isActionDecode = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbDecode.getId();
        //binding.clOmniChannelPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.clProdSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
        binding.llPickedDecoded.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) && chkNotNullFalse(mainViewModel.getIsDecodeOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.txtPicked.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) && chkNotNullFalse(mainViewModel.getIsDecodeOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.llCircle.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
        binding.llBtnStart.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
        binding.listOmniDecode.setVisibility(isActionDecode ? View.VISIBLE : View.GONE);
      }
    });
    
    binding.btnSizeChart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view.getVisibility() == View.VISIBLE){
          showLog("listFIFO", "" + listFIFO.size());
          callStyleChartAPI(isNonEmpty(listFIFO));
        }
      }
    });
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(styleChartAlert != null && styleChartAlert.isShowing()) return;
        if(binding.pdvProdSearch!=null && binding.pdvProdSearch.isShowingDetailAlert()) return;
        if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if(binding.llBtnStart.getVisibility() != View.VISIBLE) return;
        if(!isActionPick && chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) return;
        if(isActionPick && chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) return;
        if(!isActionPick && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject();
              jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
              jsonExtras.put(ParamConstants.EPC, model.getEpc());
              jsonExtras.put(ParamConstants.FIFO_DATE, model.getFifoDate());
              jsonExtras.put(ParamConstants.QTY, model.getTotalQtyStr());
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
              jsonExtras.put(ParamConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecodeOnPick);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), model.getTotalQty(), getSessionType().name(), fifoDate, jsonExtras);
          }
          if(checkReaderConnected()){
            if(searchLog != null && isNullOrEmpty(searchLog.getSearchDurationTime()))
              searchStartTime = System.currentTimeMillis();
            //mainViewModel.performBarcodeBasedSearch(chkNull(mappedEan, model.getSearchEan()), binding.clProdSearch.isSingleTagSearch());
            mainViewModel.performEPCBasedSearch(model.getEpc());
          }
        }
        else if(isActionPick && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject();
              jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
              jsonExtras.put(ParamConstants.EPC, model.getEpc());
              jsonExtras.put(ParamConstants.FIFO_DATE, model.getFifoDate());
              jsonExtras.put(ParamConstants.QTY, model.getTotalQtyStr());
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
              jsonExtras.put(ParamConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecodeOnPick);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), model.getTotalQty(), getSessionType().name(), fifoDate, jsonExtras);
          }
          int size = getSize();
          if(size >= chkNull(model.totalQty, 0) || fifoDao.isQtyFound(model.ean, model.fifoDate)){
            context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_qty_matched), getTypeCharCode()));
            return;
          }
          if(checkReaderConnected()){
            if(size > 0 && !isSinglePick){
              context.showCustomAlertDialog(getString(R.string.title_confirm_action), String.format(getString(R.string.msg_clear_and_start), "" + size), getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  inventoryDao.deleteInventory(sessionObject.sessionId);
                  mainViewModel.performPick(chkNull(mappedEan, model.getEan()));
                }
              }, getString(R.string.btn_no), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  mainViewModel.performPick(chkNull(mappedEan, model.getEan()));
                }
              });
            }
            else{
              mainViewModel.performPick(chkNull(mappedEan, model.getEan()), isAllowDecodeOnPick);
            }
          }
        }
        else if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          mainViewModel.stopInventory();
        }
      }
    });
    
    setDefaultSearchViews();
    setSessionAction(AppConstants.SESSION_ACTION_START);
    
    ConstraintSet set = new ConstraintSet();
    set.clone(binding.getRoot());
    set.constrainPercentHeight(R.id.pdv_prod_search, isAllowDecode ? 0.3f : 0.45f);
    set.applyTo(binding.getRoot());
    
    return binding.getRoot();
  }
  
  public void checkDecodeQtyForCallingStyleChart(boolean isShowFIFOChart){
    final int decodeQty = isAllowDecode ? inventoryDao.getEANDecodeQty(sessionObject.sessionId, model.getEan()) : 0;
    if(decodeQty > 0){
      context.showCustomConfirmDialog(String.format(getString(R.string.confirm_decoded_tags_discard), binding.txtDecoded.getText().toString(), model.getFifoDate()), R.string.btn_continue, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          callStyleChartAPI(isShowFIFOChart);
        }
      });
    }
    else callStyleChartAPI(isShowFIFOChart);
  }
  
  public void callStyleChartAPI(boolean isShowFIFOChart){
    if(!chkNotNullTrue(mainViewModel.getIsProcessOn().getValue()) && (styleChartAlert == null || !styleChartAlert.isShowing())){
      if(isShowFIFOChart){
        listFIFO = fifoDao.getDatewiseList(ean);
        if(isNonEmpty(listFIFO)){
          context.showCustomAlertSelectionDialog("Choose Option", "", getString(R.string.tab_size_chart), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
              callStyleChartAPI(false);
            }
          }, getString(R.string.tab_fifo_products), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
              showFIFOChartAlert(listFIFO);
            }
          });
        }
        else callStyleChartAPI(false);
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
  
  public int getDecodedQty(){
    return inventoryDao.getEANDecodeQty(sessionObject.sessionId, model.getEan());
  }
  
  public String getBackPressMsg(){
    int decodeQty = inventoryDao.getEANDecodeQty(sessionObject.sessionId, model.getEan());
    //return decodeQty>0?String.format(getString(R.string.msg_fifo_search_back_decode),""+decodeQty,model.getTotalQtyStr(),model.getFifoDate()):
    return decodeQty > 0 ? String.format(getString(R.string.msg_fifo_search_back_decode), binding.txtDecoded.getText().toString(), model.getFifoDate()) : getString(R.string.msg_fifo_search_back);
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
    
    mainViewModel.setFifoDate(model.getFifoDate());
    inventoryDao.deleteInventory(sessionObject.sessionId);
    
    final String mappedEan = !SharedPrefManager.getIsEANMapped() ? "" : extractString(args, ParamConstants.MAPPED_EAN, "");
    ean = args.getString(ParamConstants.EAN, model.getEan());
    fifoDate = args.getString(ParamConstants.FIFO_DATE, model.getFifoDate());
    zone = args.getString(ParamConstants.ZONE_NAME, model.getZone());
    zoneId = args.getString(ParamConstants.ZONE_ID, model.getZoneId());
//    age = args.getInt(ParamConstants.AGE, model.getStockAge());
    //listFIFO = fifoDao.getDatewiseList(ean);
    
    binding.pdvProdSearch.setProductModel(model);
    
    //binding.txtOmniChannelScoreCount.setText("" + 0);
    binding.ctwInventoryStart.setScore(0);
   // binding.txtOmniChannelStartTotal.setText("" + model.getTotalQty());
    binding.ctwInventoryStart.setTotal(model.getTotalQty());
    
    JSONObject jsonExtras = null;
    try{
      jsonExtras = new JSONObject();
      jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
      jsonExtras.put(ParamConstants.EPC, model.getEpc());
      jsonExtras.put(ParamConstants.FIFO_DATE, model.getFifoDate());
      jsonExtras.put(ParamConstants.QTY, model.getTotalQtyStr());
      jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
      jsonExtras.put(ParamConstants.TYPE, getSessionType().name());
      jsonExtras.put(ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
      jsonExtras.put(ParamConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecodeOnPick);
    }
    catch(Exception e){ e.printStackTrace(); }
    searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), model.getTotalQty(), getSessionType().name(), fifoDate, jsonExtras);
    setDefaultSearchViews();
    setSessionAction(AppConstants.SESSION_ACTION_START);
    binding.rbSearch.setChecked(true);
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
    lvProdColors.setAdapter(new ProductColorsListAdapter((MainActivity) context, SearchFIFOStartFragment.this, styleChartAlert, lvProdSizes, listColors, model));
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
    styleChartAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    setAlertDialogCustomTitle(styleChartAlert, R.string.tab_fifo_products);
    final DialogSimilarStylesBinding binding = DialogSimilarStylesBinding.inflate(LayoutInflater.from(context));
    binding.listDialogSimilarStyles.setAdapter(new FIFOChartListAdapter((MainActivity) context, SearchFIFOStartFragment.this, styleChartAlert, listFIFO, model));
    binding.listDialogSimilarStyles.setLayoutManager(new LinearLayoutManager(context));
    binding.listDialogSimilarStyles.setVisibility(View.VISIBLE);
    styleChartAlert.setView(binding.getRoot());
    styleChartAlert.setCancelable(false);
    styleChartAlert.show();
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    if(isSearchOn == null) return;
    else{
      updateViews();
      if(!isSearchOn) stopTimer();
      else
        startTimer(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null ? dialogOmniEpcSearchBinding.clOmniChannelEpcSearch : binding.clProdSearch, dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.imgSearchDir != null ? dialogOmniEpcSearchBinding.imgSearchDir : binding.imgSearchDir);
      if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
        dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.setEnableCheck(!isSearchOn);
      if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null && dialogOmniEpcSearchBinding.btnDecode != null){
        dialogOmniEpcSearchBinding.llBtnStart.toggle(isSearchOn);
        final Object tag = dialogOmniEpcSearchBinding.btnDecode.getTag();
        final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
        dialogOmniEpcSearchBinding.btnDecode.setVisibility(inventory != null && !inventory.isDecoded() && !isProcessOn() ? View.VISIBLE : View.GONE);
      }
      else{
        binding.llBtnStart.toggle(isSearchOn);
      }
    }
  }
  
  @Override
  protected void isPickOnChanged(Boolean isPickOn){
    super.isPickOnChanged(isPickOn);
    if(isPickOn == null) return;
    else{
      updateViews();
      if(!isPickOn){ stopTimer(); }
      binding.llBtnStart.toggle(isPickOn);
    }
  }
  
  @Override
  protected void onPickDataChanged(Inventory pickData){
    super.onPickDataChanged(pickData);
    if(isAllowDecodeOnPick && pickData != null){
      showLog("EAN", pickData.ean);
      showLog("EPC", pickData.epc);
      showLog("TID", pickData.tid);
      showLog("PC", pickData.pcdata);
      try{
        pickData.fifoDate = model.getFifoDate();
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
        if(sessionObject != null && chkNotNullTrue(mainViewModel.getIsDecodeDone().getValue())){//Case Tag Write Success
          AppCommonMethods.showLog("isDecodeDone", "" + true);
          if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.btnDecode != null){
            final Object tag = dialogOmniEpcSearchBinding.btnDecode.getTag();
            final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
            inventory.isUploaded = false;
            if(isNullOrEmpty(inventory.newEpc)) inventory.newEpc = "0" + inventory.epc.substring(1);
            dialogOmniEpcSearchBinding.btnDecode.setTag(inventory);
            dialogOmniEpcSearchBinding.btnDecode.setVisibility(inventory != null && !inventory.isDecoded() && !isProcessOn() ? View.VISIBLE : View.GONE);
            apiCall(AppConstants.SESSION_ACTION_UPLOAD);
          }
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
  
  @Override
  public void updateLists(){
    if(isAllowDecode && binding != null && binding.listOmniDecode != null && binding.listOmniDecode.getAdapter() != null && binding.listOmniDecode.getAdapter() instanceof RecyclerView.Adapter){
      listOmniPicked.clear();
      //listOmniPicked.addAll(fifoDao.getList(model.getEan(), model.getFifoDate()));
      listOmniPicked.addAll(inventoryDao.getEANInventory(sessionObject.sessionId, model.getEan()));
      ((RecyclerView.Adapter) binding.listOmniDecode.getAdapter()).notifyDataSetChanged();
    }
  }
  
  private void updateViews(){
    final boolean isProcessOn = chkNotNullTrue(mainViewModel.getIsDecodeOn().getValue()) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) || chkNotNullTrue(mainViewModel.getIsSearchOn().getValue());
    if(dialogOmniEpcSearchBinding != null){
      dialogOmniEpcSearchBinding.btnDecode.setEnabled(!isProcessOn);
      dialogOmniEpcSearchBinding.llBtnStart.setEnabled(!chkNotNullTrue(mainViewModel.getIsDecodeOn().getValue()));
    }
    binding.pdvProdSearch.setEnabled(!isProcessOn);
    binding.rgPickSearchType.setEnabled(!isProcessOn);
    binding.rgPickSearchType.setVisibility(!isProcessOn?View.VISIBLE:View.INVISIBLE);
    binding.rbPick.setEnabled(!isProcessOn);
    binding.rbSearch.setEnabled(!isProcessOn);
    binding.rbDecode.setEnabled(isAllowDecode && !isProcessOn);
    binding.llPickedDecoded.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
    binding.txtPicked.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
    binding.txtDecoded.setVisibility(isAllowDecode && getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
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
      //ConstraintLayout.LayoutParams llParams = new ConstraintLayout.LayoutParams(wid, wid);
      binding.clOmniEPCSearch.setLayoutParams(llParams);
      binding.btnDecode.setTag(inventory);
      binding.btnDecode.setVisibility(inventory != null && !inventory.isDecoded() && !isProcessOn() ? View.VISIBLE : View.GONE);
      binding.btnDecode.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          if(!isProcessOn() && inventory != null && !inventory.isDecoded()) startDecode(inventory);
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
  
  @Override
  protected void onTriggerPressed(){
    showLog(this.getClass().getSimpleName(), "onTriggerPressed");
    if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null)
      dialogOmniEpcSearchBinding.llBtnStart.performClick();
    else binding.llBtnStart.performClick();
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    size = chkNull(size, 0) > 0 ? inventoryDao.getEANQty(sessionObject.sessionId, model.getEan()) : size;
    //final int preSize = Integer.parseInt(chkZero(binding.txtOmniChannelScoreCount.getText().toString().trim(), "0"));
    //final int preSize = Integer.parseInt(chkZero(binding.txtPicked.getText().toString().split("/")[0].trim(), "0"));
    //final long total = Long.parseLong(chkNull(binding.txtOmniChannelStartTotal.getText().toString().replace("-", ""), "0"));
    //showLog("presize_size",preSize+"_"+size);
    /*final boolean isJustPicked = size > 0 && preSize==chkNull(size, 0)-1 && preSize<total;
    if(isJustPicked && isAllowDecodeOnPick){
      binding.txtOmniChannelScoreCount.setText("" + chkNull(size, 0));
      binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty_total), "" + chkNull(size, 0), "" + model.getTotalQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
      final Inventory pickData = inventoryDao.getLastInserted(sessionObject.sessionId);
      if(pickData != null){
        showLog("EAN", pickData.ean);
        showLog("EPC", pickData.epc);
        showLog("TID", pickData.tid);
        showLog("PC", pickData.pcdata);
        try{
          pickData.fifoDate = model.getFifoDate();
          mainViewModel.performDecoding(pickData);
        }catch(Exception e){e.printStackTrace();}
      }
    }*/
    //else{
   // binding.txtOmniChannelScoreCount.setText("" + chkNull(size, 0));
    binding.ctwInventoryStart.setScore(chkNull(size,0));
   // binding.txtOmniChannelStartTotal.setText("" + model.getTotalQty());
    binding.ctwInventoryStart.setTotal(model.getTotalQty());
    //}
   // long total = Long.parseLong(chkNull(binding.txtOmniChannelStartTotal.getText().toString().replace("-", ""), "0"));
    long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
    final boolean isInvCount = chkNull(size, 0) > 0;
    int foundQty = chkNull(size, 0);
    final int decodeQty = inventoryDao.getEANDecodeQty(sessionObject.sessionId, model.getEan());
    final int totalQty = model.getTotalQty();
    //binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty_total), "" + chkNull(size, 0), "" + model.getTotalQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    //binding.txtDecoded.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_decoded_qty_total), String.valueOf(chkNull(decodeQty, 0)), model.getTotalQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_found), String.format(context.getString(R.string.txt_append_qty), context.getColorCode(foundQty > 0 && totalQty == foundQty ? R.color.green : foundQty > 0 ? R.color.orange : R.color.err_red), foundQty + "", "" + totalQty)), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtDecoded.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_decoded), String.format(context.getString(R.string.txt_append_qty), context.getColorCode(decodeQty > 0 && totalQty == decodeQty ? R.color.green : decodeQty > 0 ? R.color.orange : R.color.err_red), decodeQty + "", "" + totalQty)), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.divPick.setVisibility(!isAllowDecode || getSize() <= 0 ? View.VISIBLE : View.GONE);
    binding.rbDecode.setVisibility(isAllowDecode && getSize() > 0 ? View.VISIBLE : View.GONE);
    //binding.progressOmniChannelStart.setVisibility(isInvCount && total > 0 ? View.VISIBLE : View.GONE);
    if(size > model.getTotalQty()){
      context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_extra_tag), getTypeCharCode(), (chkNull(size, 0) + "/" + model.getTotalQtyStr())));
      mainViewModel.stopInventory();
      inventoryDao.deleteInventory(sessionObject.sessionId);
    }
    
    double per = total > 0 ? (chkNull(size, 0) * 100) / total : 0;
    int percentage = (int) per;
    //binding.progressOmniChannelStart.setProgress(percentage);
    updateViews();
    updateLists();
  }
  
  public void apiCall(String action){
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    if(isInternetConnected(context, false, isUpload)){
      try{
        if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.EAN, model.getEan());
        requestParams.put(ParamConstants.EPC, model.getEpc());
        requestParams.put(ParamConstants.FIFO_DATE, model.getFifoDate());
        requestParams.put(ParamConstants.QTY, model.getTotalQty());
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        if(sessionObject != null)
          requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
        if(isUpload){
          showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          new Handler().post(new Runnable(){
            @Override
            public void run(){
              try{
                JSONArray js = new JSONArray();
                if(model != null){
                  for(Inventory inventory : inventoryDao.getAllInventoryData(sessionObject.sessionId)){
                    if(inventory != null && isUpload && !inventory.isUploaded && inventory.isDecoded()){
                      JSONObject dataobject = inventory.toJson(context);
                      if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                        js.put(dataobject);
                    }
                  }
                }
                requestParams.put(ParamConstants.ITEMS, js);
                if(js != null && js.length() > 0)
                  callWebService(URLConstants.UPLOAD_DECODING, requestParams, /*args,*/false, getString(R.string.progress_msg_uploading_data), !isUpload);
              }
              catch(Exception e){
                e.printStackTrace();
                hideProgressDialog();
              }
            }
          });
        }
        else
          callWebService(isUpload ? URLConstants.UPLOAD_DECODING : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload);
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
      }
    }
  }
  
  /**
   * Set default search views.
   */
  @Override
  protected void setDefaultSearchViews(){
    super.setDefaultSearchViews();
    if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
      dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.resetToDefault();
    else binding.clProdSearch.resetToDefault();
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
    if(fifoDao != null) fifoDao.deleteAll();
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
      sessionObject.zone = zone;
      sessionObject.zoneId = zoneId;
      sessionObject.eans = ean;
      sessionObject.category = fifoDate;
      sessionObject.sessionType = AppCommonMethods.SessionType.SEARCH_FIFO.getValue();
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
        case URLConstants.UPLOAD_DECODING:
          final JSONArray js = AppCommonMethods.isUpdateUploadStatusBasedOnTID ? extractJSONArray(jsonRequest, ParamConstants.ITEMS) : null;
          final Set<String> tids = new HashSet<String>(0);
          if(isNonEmpty(js)){
            for(int i = 0; i < js.length(); i++){
              final String tid = extractString(js.getJSONObject(i), ParamConstants.TID, "").trim();
              if(isNonEmpty(tid))
                tids.add(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid);
            }
          }
          UploadInventoryDao uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
          if(isSuccess){
            String dialogMsg = extractString(jsonResponse, ParamConstants.MESSAGE, getString(R.string.success_decoding));
            context.showCustomSuccessDialog(dialogMsg);
            if(isNonEmpty(tids)){
              inventoryDao.updateUploaded(sessionObject.sessionId, tids);
              uploadInventoryDao.updateUploaded(sessionObject.sessionId, tids);
            }
            else{
              inventoryDao.updateUploaded(sessionObject.sessionId);
              uploadInventoryDao.updateUploaded(sessionObject.sessionId);
            }
            uploadInventoryDao.deleteUploaded();
          }
          else{
            if(isNonEmpty(tids)){
              inventoryDao.updateUploadRetryCount(sessionObject.sessionId, tids);
              uploadInventoryDao.updateUploadRetryCount(sessionObject.sessionId, tids);
            }
            else{
              inventoryDao.updateUploadRetryCount(sessionObject.sessionId);
              uploadInventoryDao.updateUploadRetryCount(sessionObject.sessionId);
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