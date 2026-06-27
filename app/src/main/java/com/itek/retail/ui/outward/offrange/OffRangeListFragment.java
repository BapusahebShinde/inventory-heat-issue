package com.itek.retail.ui.outward.offrange;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT_PATTERN;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isAllowDiscardOperationForPickedData;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isSaveCompletedCartonAfterUpload;
import static com.itek.retail.common.AppCommonMethods.isSinglePick;
import static com.itek.retail.common.AppCommonMethods.saveLimitForCompletedCartons;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.ProductListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.OutwardBatchDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.databinding.FragmentOffRangeListDetalisBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.OutwardTypes;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.UploadInventory;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * The Outward list fragment.
 */
public class OffRangeListFragment extends RFIDSessionFragment{
  
  //int activeUsers = 0, sessionValidTill = 48;
  ProductDao productDao;
  private OffRangeViewModel mViewModel;
  private FragmentOffRangeListDetalisBinding binding;
  private List<String> listBrands = new ArrayList<>(0);
  private List<String> listCategories = new ArrayList<>(0);
  private Long totalListCount = 0l;
  private List<ProductModel> listOutwardProducts = new ArrayList<>(0);
  private Integer visibleListRecordLimit = isDebugApp ? 10 : 10;
  private Integer fromListIndex = 0;
  private Set<String> eans = new HashSet<>(0);
  private String type;
  private String destCode;
  private String cartonNo;
  private JSONObject extras;
  private OutwardTypes outType;
  private String batchId;
  private String listRefBatchId;
  private boolean isOffRange = false;
  private boolean isEmptyToteOutward = false;
  private InventoryDao inventoryDao;
  private UploadInventoryDao uploadInventoryDao;
  private OutwardBatchDao outwardBatchDao;
  private boolean isShowingSearchPickView;
  private boolean isActionPick;
  
  private ProductModel model;
  private ExecutorService executor = Executors.newSingleThreadExecutor(); // You can also use a cached thread pool or a custom executor
  
  /**
   * Instantiates a new Outward list fragment.
   */
  public OffRangeListFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    mainViewModel = ((MainActivity) context).getRfidViewModel();
    productDao = AppDatabase.getProductDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
    outwardBatchDao = AppDatabase.getOutwardBatchDao(context);
    if(isAllowDirectionalSearch) mainViewModel.getSensorAndStart();
    
    //Set Default Power
    SharedPrefManager.setInt(getSessionType().name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(),20);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OffRangeViewModel.class);
    binding = FragmentOffRangeListDetalisBinding.inflate(inflater, container, false);
    getActiveUsersAndSessionValidTill();
    if(isNonEmpty(getArguments())){
      isEmptyToteOutward = getArguments().containsKey(ParamConstants.IS_EMPTY_TOTE_OUTWARD) && getArguments().getBoolean(ParamConstants.IS_EMPTY_TOTE_OUTWARD, false);
      isOffRange = getArguments().getBoolean(ParamConstants.IS_OFF_RANGE, true);
      outType = (OutwardTypes) extractSerializable(getArguments(), OutwardTypes.class);
      cartonNo = extractString(getArguments(), ParamConstants.CARTON_NO, extractString(getArguments(), ParamConstants.CARTON_NUM, extractString(getArguments(), ParamConstants.CARTON_NUMBER)));
      destCode = extractString(getArguments(), ParamConstants.DESTINATION_SITE_CODE, extractString(getArguments(), ParamConstants.DESTINATION_SITE, extractString(getArguments(), ParamConstants.DESTINATION_CODE)));
      batchId = extractString(getArguments(), ParamConstants.BATCH_ID, extractString(getArguments(), ParamConstants.BATCH));
      listRefBatchId = extractString(getArguments(), ParamConstants.LIST_REF_BATCH_ID, extractString(getArguments(), ParamConstants.LIST_BATCH_ID, extractString(getArguments(), ParamConstants.LIST_ID)));
      type = outType != null ? outType.getName() : extractString(getArguments(), ParamConstants.OUTWARD_TOTE_TYPE, extractString(getArguments(), ParamConstants.TYPE));
      try{
        extras = new JSONObject();
        extras.put(ParamConstants.DESTINATION_SITE_CODE, destCode);
        extras.put(ParamConstants.DESTINATION_CODE, destCode);
        extras.put(ParamConstants.CARTON_NUMBER, cartonNo);
        extras.put(ParamConstants.OUTWARD_TOTE_TYPE, type);
        extras.put(ParamConstants.IS_OFF_RANGE, isOffRange);
        extras.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
        extras.put(ParamConstants.BATCH_ID, batchId);
        extras.put(ParamConstants.LIST_REF_BATCH_ID, listRefBatchId);
        if(outType != null)
          extras.put(ParamConstants.STORE_OUTWARD_TYPE_MASTER_ID, outType.getTypeId());
        //extras.put(ParamConstants.OUTWARD_TOTE_EANS,type);
      }
      catch(JSONException e){
        throw new RuntimeException(e);
      }
    }
    
    /*activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
    sessionValidTill = extractInt(getArguments(), AppConstants.SESSION_VALID_TILL, 48);*/
    
    List<String> listEans = productDao.getDistinctEans(sessionObject != null ? sessionObject.sessionType : getSessionType().getValue());
    if(isNonEmpty(listEans)) eans.addAll(listEans);
    
    binding.txtCartonNo.setMaxLines(cartonNo.length()>12?2:1);
    binding.txtCartonNo.setSingleLine(cartonNo.length()<=12);
    binding.txtCartonNo.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_carton), cartonNo.length()>12?"<br/>"+cartonNo:cartonNo), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    listBrands.clear();
    listBrands.add(AppConstants.ALL);
    List<String> brands = productDao.getBrandList();
    if(isNonEmpty(brands)) listBrands.addAll(brands);
    
    listCategories.clear();
    listCategories.add(AppConstants.ALL);
    List<String> categories = productDao.getCategoryList();
    if(isNonEmpty(categories)) listCategories.addAll(categories);
    binding.spinBrand.setAdapter(listBrands);
    binding.spinCategory.setAdapter(listCategories);
    
    binding.spinBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
    });
    
    binding.spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
    });

    binding.edtReplenishmentNameSearch.setHint(String.format(context.getString(R.string.hint_search_by__),SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name))+"/"+SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean))));
    binding.edtReplenishmentNameSearch.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void afterTextChanged(Editable editable){ updateLists(); }
    });
    
    binding.edtReplenishmentNameSearch.setOnEditorActionListener(new TextView.OnEditorActionListener(){
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent){
        if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
          hideKeyboard();
          return true;
        }
        return false;
      }
    });
    
    binding.tbListPick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        binding.tbListPick.setSwitchTextAppearance(context,isChecked?R.style.TextStyleExtraSmallWhite:R.style.TextStyleExtraSmall);
      }
    });
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        context.dismissCustomAlertDialog();
        if(binding.pdvOutwardStart!=null)binding.pdvOutwardStart.dismissAlerts();
        final Boolean isSessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
        if(!isSessionOn){
          if(sessionObject != null) mainViewModel.startSession(sessionObject, eans, true);
          else if(sessionObject == null){
            //Call API & Start Inventory Session
            apiCall(AppConstants.SESSION_ACTION_START);
          }
        }
        else if(isSessionOn){
          showLog("Random Pick",""+binding.tbListPick.isChecked());
          if(binding.tbListPick.isChecked()){
            if(!isProcessOn() && checkReaderConnected()){
              mainViewModel.performPick("");
            }
          }
          else{
            if(getSize() >= AppCommonMethods.invLimit){
              if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
                mainViewModel.stopInventory();
              context.showCustomErrDialog(R.string.err_inventory_max_limit);
            }
            else{
              if(checkReaderConnected() && (sessionObject != null || chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
                productDao.resetRssiPercentage(sessionObject != null ? sessionObject.sessionType : getSessionType().getValue());
              toggleInventory();
            }
          }
        }
      }
    });
    
    binding.btnComplete.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(isNonEmpty(cartonNo) && v.getVisibility() == View.VISIBLE){
          context.showCustomConfirmDialog(String.format(getString(R.string.msg_confirm_complete_carton), cartonNo), R.string.complete_carton, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              uploadCarton(true);
            }
          });
        }
      }
    });
    
    binding.rbPick.setSelected(true);
    binding.rbSearch.setSelected(true);
    binding.rgPickSearchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        if(isProcessOn()) mainViewModel.stopInventory();
        final int size = getSize();
        isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
       // binding.clOutwardPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.clOutwardSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
        binding.txtPicked.setVisibility(!isActionPick && size > 0 && !isProcessOn() ? View.VISIBLE : View.GONE);
      }
    });
    
    binding.llBtnStartPick.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(binding.pdvOutwardStart!=null)binding.pdvOutwardStart.dismissAlerts();
        if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if(!isShowingSearchPickView || model == null) return;
        if(isActionPick && chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) return;
        if(!isActionPick && chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) return;
        if(isActionPick && chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) return;
        if(!isActionPick && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject(extras.toString());
              jsonExtras.put(ParamConstants.EAN, model.getEan());
              jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, type);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", model.getEan(), model.getEanQty(), getSessionType().name(), type, batchId, jsonExtras);
          }
          if(checkReaderConnected()){
            if(searchLog != null && isNullOrEmpty(searchLog.getSearchDurationTime()))
              searchStartTime = System.currentTimeMillis();
            mainViewModel.performBarcodeBasedSearch(model.getSearchEan(), binding.clOutwardSearch.isSingleTagSearch());
          }
        }
        else if(isActionPick && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject(extras.toString());
              jsonExtras.put(ParamConstants.EAN, model.getEan());
              jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, type);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", model.getEan(), model.getEanQty(), getSessionType().name(), type, batchId, jsonExtras);
          }
          int size = getSize();
          //          if(size >= chkNull(model.eanQty, 0)){
          //            context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_qty_matched), getTypeCharCode()));
          //            return;
          //          }
          if(checkReaderConnected()){
            if(size > 0 && !isSinglePick){
              context.showCustomAlertDialog(getString(R.string.title_confirm_action), String.format(getString(R.string.msg_clear_and_start), "" + size), getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  inventoryDao.deleteInventory(sessionObject.sessionId);
                  mainViewModel.performPick(model.getEan());
                }
              }, getString(R.string.btn_no), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  mainViewModel.performPick(model.getEan());
                }
              });
            }
            else{
              mainViewModel.performPick(model.getEan());
            }
          }
        }
        else if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          mainViewModel.stopInventory();
        }
      }
    });
    
    binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        context.showCustomConfirmDialog(R.string.title_stock_correction_upload, R.string.btn_upload, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            uploadCarton(false);
          }
        });
      }
    });
    
    /*binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view.getVisibility() != View.VISIBLE) return;
        if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
          return;
        context.showCustomConfirmDialog(R.string.title_stock_correction_upload, R.string.btn_upload, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            uploadCarton(false);
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
          context.showCustomConfirmDialog(R.string.title_stock_correction_upload, R.string.btn_upload, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              uploadCarton(false);
            }
          });
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);*/
    
    setDefaultSearchViews();
    setSessionAction(AppConstants.SESSION_ACTION_START, null);
    
    setRecyclerViewAdapter();
    
    binding.header.imgConfigSync.setVisibility(!isShowingSearchPickView && !isProcessOn()?View.VISIBLE:View.GONE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(isShowingSearchPickView || isProcessOn()) return;
        binding.llSeekbarPower.setVisibility(binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
      }
    });
    
    binding.llSeekbarPower.setProgress(20);
    binding.llSeekbarPower.setupProgress(mainViewModel);
    
    return binding.getRoot();
  }
  
  
  public void setRecyclerViewAdapter(){
    binding.listProducts.setAdapter(new ProductListAdapter((MainActivity) context, OffRangeListFragment.this, listOutwardProducts));
    final RecyclerView.LayoutManager layoutManager = isLandscape ? new GridLayoutManager(context, 2){
      @Override
      public boolean canScrollVertically(){
        return !isProcessOn();
      }
    } : new LinearLayoutManager(context){
      @Override
      public boolean canScrollVertically(){
        return !isProcessOn();
      }
    };
    
    binding.listProducts.setLayoutManager(layoutManager);
    
    binding.listProducts.addOnScrollListener(new RecyclerView.OnScrollListener(){
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy){
        if(isProcessOn()) return;
        super.onScrolled(recyclerView, dx, dy);
        showLog("size", totalListCount + "_" + visibleListRecordLimit);
        if(totalListCount > visibleListRecordLimit){
            /*if (!recyclerView.canScrollVertically(1)){ //1 for down
              //loadMore();
            }*/
          LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
          //if (!isLoading) {
          showLog("size1", linearLayoutManager.findLastCompletelyVisibleItemPosition() + "_" + listOutwardProducts.size());
          if(linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == listOutwardProducts.size() - 1){
            //bottom of list!
            //loadMore();
            showLog("size2", totalListCount + "_" + listOutwardProducts.size());
            if(totalListCount > listOutwardProducts.size()) updateRecyclerView();
            //isLoading = true;
          }
          //}
        }
      }
    });
  }
  
  @Override
  protected void onReaderPowerChanged(Integer power){
    binding.llSeekbarPower.updateReaderPower(mainViewModel, power);
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    productDao.getPriorityChanged(getSessionType().getValue()).observe(getViewLifecycleOwner(), new Observer<Integer>(){
      @Override
      public void onChanged(Integer integer){
        showLog("onPriorityChanged", "" + integer);
        updateLists();
      }
    });
  }
  
  /**
   * Set default search views.
   */
  @Override
  protected void setDefaultSearchViews(){
    super.setDefaultSearchViews();
    binding.clOutwardSearch.resetToDefault();
  }
  
  @Override
  protected void isPickOnChanged(Boolean isPickOn){
    super.isPickOnChanged(isPickOn);
    if(isPickOn == null) return;
    else{
      updateUI();
      if(!isPickOn) stopTimer();
      binding.llBtnStart.toggle(isPickOn);
      binding.llBtnStartPick.toggle(isPickOn);
    }
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    if(isSearchOn == null) return;
    else{
      updateUI();
      binding.clOutwardSearch.setEnableCheck(!isSearchOn);
      if(!isSearchOn) stopTimer();
      else startTimer(binding.clOutwardSearch, binding.imgSearchDir);
      binding.llBtnStartPick.toggle(isSearchOn);
    }
  }
  
  @Override
  protected void onDataSizeChanged(Integer size1){
    super.onDataSizeChanged(size1);
    if(model != null && isShowingSearchPickView){
      final int size = productDao.getEANFoundCount(model.getEan());//getSize();
     // binding.txtOutwardScoreCount.setText("" + chkNull(size, 0));
      binding.ctwInventoryStart.setScore(chkNull(size, 0));
     // binding.txtOutwardStartTotal.setText("" + chkZero("" + model.getEanQty(), AppConstants.DEFAULT_NO_VALUE));
      binding.ctwInventoryStart.setTotal(chkZero("" + model.getEanQty(), AppConstants.DEFAULT_NO_VALUE));
     // long total = Long.parseLong(chkNull(binding.txtOutwardStartTotal.getText().toString().replace("-", ""), "0"));
      long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
      final boolean isInvCount = chkNull(size, 0) > 0;
     // binding.progressOutwardStart.setVisibility(isInvCount && total > 0 ? View.VISIBLE : View.GONE);
      binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty_total), String.valueOf(chkNull(size, 0)), model.getEanQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
      updateUI(true);
      //    if(size > model.getEanQty()){
      //      context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_extra_tag), getTypeCharCode(), (chkNull(size, 0) + "/" + model.getEanQtyStr())));
      //      mainViewModel.stopInventory();
      //      inventoryDao.deleteInventory(sessionObject.sessionId);
      //    }
      //insertSearchLog(model.getEan(),model.getEanQty(), getSessionType().name(), tripType, tripNum);
      double per = total > 0 ? (chkNull(size, 0) * 100) / total : 0;
      int percentage = (int) per;
     // binding.progressOutwardStart.setProgress(percentage);
    }
    //else updateLists();
  }
  
  @Override
  public void onDestroyView(){
    productDao.getPriorityChanged(getSessionType().getValue()).removeObservers(getViewLifecycleOwner());
    super.onDestroyView();
  }
  
  public void uploadCarton(){ uploadCarton(false); }
  
  public void uploadCarton(final boolean isComplete){
    try{
      if(isComplete) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
      JSONObject requestParams = extras != null ? extras : new JSONObject();
      requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
      requestParams.put(ParamConstants.IS_OFF_RANGE, isOffRange);
      requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
      requestParams.put(ParamConstants.TYPE, type);
      requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD);
      requestParams.put(ParamConstants.STATUS, isComplete ? AppConstants.TRIP_STATUS_COMPLETED : AppConstants.TRIP_STATUS_PENDING);
      if(sessionObject != null && sessionObject.sessionId != null){
        requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
        JSONArray js = new JSONArray();
        List<Inventory> dataList = inventoryDao.getAllInventoryData(sessionObject.sessionId);
        if(isNonEmpty(dataList)){
          for(Inventory inventory : dataList){
            if(inventory == null || (isComplete && isAllowDiscardOperationForPickedData && !inventory.isFound))
              continue;
            if(isComplete){
              try{
                UploadInventory uploadInventory = new UploadInventory(inventory, extras.toString());
                uploadInventory.remark = cartonNo;
                uploadInventory.fifoDate = batchId;
                uploadInventoryDao.insertUploadInventoryData(uploadInventory);
              }
              catch(Exception e){ e.printStackTrace(); }
            }
            else if(!isComplete && !inventory.isFound){
              try{
                inventory.isFound = true;
                inventoryDao.updateInventoryData(inventory);
              }
              catch(Exception e){ e.printStackTrace(); }
            }
            JSONObject dataobject = inventory.toJson(context);
            if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
              js.put(dataobject);
          }
          if(!isComplete){
            try{
              inventoryDao.updateFound(sessionObject.sessionId, model.getEan(), null);
            }
            catch(Exception e){ e.printStackTrace(); }
          }
        }
        requestParams.put(ParamConstants.ITEMS, js);
        showLog("uploadSavedCartons_SaveCount", "" + uploadInventoryDao.getNonUploadedCount(AppCommonMethods.SessionType.OFF_RANGE.getValue()));
        if(isComplete)
          callWebService(URLConstants.UPLOAD_OUTWARD_CARTON_DATA, requestParams, getString(R.string.progress_msg_uploading_data), true);
        else{
          model = null;
          toggleUI();
        }
      }
      else{
        if(isComplete)
          callWebService(URLConstants.UPLOAD_OUTWARD_CARTON_DATA, requestParams, getString(R.string.progress_msg_uploading_data), true);
        else{
          model = null;
          toggleUI();
        }
      }
    }
    catch(JSONException e){
      e.printStackTrace();
      hideProgressDialog();
    }
  }
  
  private void saveCarton(){
  
  }
  
  public void apiCall(String action){
    final Boolean isSessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
    if(isSessionOn){
      final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
      if(isUpload && isInternetConnected(context, false, isUpload)){
        if(isUpload) uploadCarton();
      }
      else{
        setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
      }
    }
    else if(!isSessionOn && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      setSessionAction(AppConstants.SESSION_ACTION_START, null);
    }
  }
  
  public void setupProductView(ProductModel productModel){
    if(isProcessOn()) return;
    if(productModel == null) return;
    hideKeyboard();
    this.model = productModel;
    final boolean hasZone = !(model.getDestZone().equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE) || model.getZone().equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
    binding.pdvOutwardStart.setProductModel(model, hasZone);
    
    final int size = productDao.getEANFoundCount(model.getEan());//getSize();
   // binding.txtOutwardScoreCount.setText("" + chkNull(size, 0));
    binding.ctwInventoryStart.setScore(chkNull(size,0));
  //  binding.txtOutwardStartTotal.setText("" + chkZero("" + model.getEanQty(), AppConstants.DEFAULT_NO_VALUE));
    binding.ctwInventoryStart.setTotal(chkZero("" + model.getEanQty(), AppConstants.DEFAULT_NO_VALUE));
   // long total = Long.parseLong(chkNull(binding.txtOutwardStartTotal.getText().toString().replace("-", ""), "0"));
    long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
    final boolean isInvCount = chkNull(size, 0) > 0;
   // binding.progressOutwardStart.setVisibility(isInvCount && total > 0 ? View.VISIBLE : View.GONE);
    binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty_total), String.valueOf(chkNull(size, 0)), model.getEanQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    binding.rbSearch.setChecked(true);
    if(!isShowingSearchPickView) toggleUI();
  }
  
  public void toggleUI(){
    isShowingSearchPickView = !isShowingSearchPickView;
    binding.header.imgConfigSync.setVisibility(!isShowingSearchPickView && !isProcessOn()?View.VISIBLE:View.GONE);
    if(binding.llSeekbarPower.getVisibility()==View.VISIBLE) binding.llSeekbarPower.setVisibility(View.GONE);
    
    binding.clSearchPick.setVisibility(isShowingSearchPickView ? View.VISIBLE : View.GONE);
    binding.clInventory.setVisibility(!isShowingSearchPickView ? View.VISIBLE : View.GONE);
    binding.llBtnStart.setVisibility(!isShowingSearchPickView ? View.VISIBLE : View.GONE);
    binding.btnComplete.setVisibility(!isShowingSearchPickView && !isProcessOn() && getSize() > 0 ? View.VISIBLE : View.GONE);
  }
  
  /**
   * Set active users.
   */
  public void setActiveUsers(final int activeUsers){
    int oldActiveUsers = -2;
    try{
      oldActiveUsers = AppCommonMethods.parseInt(binding.header.btnActiveDevices.getText().toString(), "-2");
    }
    catch(NumberFormatException e){ e.printStackTrace(); }
    final int activeCount = chkNull(activeUsers, -1) >= 0 ? activeUsers : oldActiveUsers;
    binding.header.flActiveDevices.setVisibility(activeUsers >= -1 ? View.VISIBLE : View.GONE);
    binding.header.btnActiveDevices.setSelected(true);
    binding.header.btnActiveDevices.setText(activeCount >= 0 ? "" + activeCount : "");
  }
  
  /**
   * Set session action.
   *
   * @param action    the action
   * @param sessionId the session id
   */
  void setSessionAction(String action, String sessionId){
    setSessionAction(action, sessionId, null, null, null);
  }
  
  /**
   * Set session action.
   *
   * @param action          the action
   * @param sessionId       the session id
   * @param sessionTime     the session time
   * @param inventoryCount  the inventory count
   * @param activeUserCount the active user count
   */
  void setSessionAction(String action, String sessionId, String sessionTime, Long inventoryCount, Integer activeUserCount){
    setActiveUsers(activeUserCount != null ? activeUserCount.intValue() : -2);
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.category = cartonNo;
      sessionObject.brands = type;
      sessionObject.eans = isNonEmpty(eans) ? eans.toString().replaceAll("\\s*,\\s*", ",").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim() : null;
      sessionObject.brandEan = "";//SharedPrefManager.listToStr(listBrandEans);
      sessionObject.extras = isNonEmpty(extras) ? extras.toString() : "";
      sessionObject.total = "";//totInvCount;
      sessionObject.sessionType = AppCommonMethods.SessionType.OFF_RANGE.getValue();
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
      cc.add(Calendar.HOUR_OF_DAY, chkZero(sessionValidTill, 48));
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = chkNull(sessionId, mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc));
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, eans, false);
      /*Bundle args = OffRangeListFragment.this.getArguments();
      args.putString(ParamConstants.SESSION_ID,sessionObject.sessionId);
      OffRangeListFragment.this.setArguments(args);*/
    }
    else if(sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_RESUME))
      mainViewModel.startSession(sessionObject, eans, true);
    else if(sessionObject != null && !action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")")){
      mainViewModel.stopSession(sessionObject, action.matches("(?i)(" + AppConstants.SESSION_ACTION_UPLOAD + "|" + AppConstants.SESSION_ACTION_DISCARD + ")"));
      if(action.equalsIgnoreCase(AppConstants.SESSION_ACTION_SAVE)){
        context.showCustomAlertDialog("", getString(R.string.success_session_save), true, true, getString(R.string.btn_ok), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            context.clearBackStack();
          }
        });
      }
      else context.popBackStack();
    }
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isProcessOn() && isTopInStack()) mainViewModel.stopInventory();
    //updateLists();
  }
  
  @Override
  public void onBackPressed(){
      if(isProcessOn())
        context.showCustomAlertDialog("", String.format(getString(R.string.err_op_back_press), getTypeCharCode(), getSessionType().name().replaceAll("_", " ")), false, true, getString(R.string.btn_ok), null);
      else{
        if(isShowingSearchPickView && model != null && (productDao.getTotalFoundCount() == 0 || !isAllowDiscardOperationForPickedData)){
          model = null;
          toggleUI();
        }
        else if(productDao.getTotalFoundCount() > 0){
          context.showCustomAlertDialog("", getString(R.string.msg_outward_pick_back), R.string.btn_no, null, R.string.btn_yes, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              if(isShowingSearchPickView && model != null && isAllowDiscardOperationForPickedData){
                inventoryDao.deleteNotFoundInventory(sessionObject.sessionId, model.getEan(), null);
                final List<String> listEpcs = inventoryDao.getFoundEPCs(sessionObject.sessionId, model.getEan(), null);
                productDao.updateFoundQty(model.getEan(), isNullOrEmpty(listEpcs) ? "" : String.join(",", listEpcs), isNullOrEmpty(listEpcs) ? 0 : listEpcs.size());
                model = null;
                toggleUI();
              }
              else{
                productDao.resetProducts(sessionObject != null ? sessionObject.sessionType : getSessionType().getValue());
                mainViewModel.stopSession(sessionObject, true);
                context.popBackStack();
              }
            }
          });
        }
        else{
          productDao.resetProducts(sessionObject != null ? sessionObject.sessionType : getSessionType().getValue());
          mainViewModel.stopSession(sessionObject, true);
          context.popBackStack();
        }
      }
  }
  
  @Override
  public void updateLists(){ updateLists(true); }
  
  public void updateLists(final boolean isResetIndexes){
    showLog("updateLists", "called");
    super.updateLists();
    final String selBrand = binding.spinBrand.getSelectedItem();
    final Set<String> selBrands = binding.spinBrand.getSelectedVals();
    final String selCategory = binding.spinCategory.getSelectedItem();
    final Set<String> selCategories = binding.spinCategory.getSelectedVals();
    final String searchName = chkNull(binding.edtReplenishmentNameSearch.getText().toString(), "").trim();
    
    final int totalFound = productDao.getTotalFoundQty(sessionObject != null ? sessionObject.sessionType : getSessionType().getValue());
    binding.txtCartonFound.setVisibility(totalFound > 0 ? View.VISIBLE : View.GONE);
    binding.txtCartonFound.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty), "" + totalFound), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.btnComplete.setVisibility(!isShowingSearchPickView && !isProcessOn() && totalFound > 0 ? View.VISIBLE : View.GONE);
    
    if(isResetIndexes){
      showLog("resetListIndexes", "called");
      //listOutwardProducts.clear();
      fromListIndex = 0;
    }
    if(binding != null && binding.listProducts != null && binding.listProducts.getAdapter() != null){
      executor.execute(new Runnable(){
        @Override
        public void run(){
          showLog("adapterUpdate", "Called");
          totalListCount = (!isResetIndexes) ? totalListCount : productDao.getOffRangeProductsTotalCount(selCategory, selCategories, selBrand, selBrands, searchName);
          showLog("listOutwardProducts(indexes)", fromListIndex + "_" + visibleListRecordLimit);
          final List<ProductModel> queryList = productDao.getOffRangeProducts(selCategory, selCategories, selBrand, selBrands, searchName, fromListIndex, chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())?5:visibleListRecordLimit);
          showLog("listOutwardProducts(total)", "" + totalListCount);
          showLog("listOutwardProducts(queryList)", "" + queryList.size());
          context.runOnUiThread(new Runnable(){
            @Override
            public void run(){
              if(isResetIndexes) listOutwardProducts.clear();
              showLog("listOutwardProducts0", "" + listOutwardProducts.size());
              listOutwardProducts.addAll(queryList);
              showLog("listOutwardProducts1", "" + listOutwardProducts.size());
              if(isResetIndexes) binding.listProducts.scrollToPosition(0);
              ((RecyclerView.Adapter) binding.listProducts.getAdapter()).notifyDataSetChanged();
              showLog("listOutwardProducts2", "" + listOutwardProducts.size());
            }
          });
        }
      });
    }
    
    final boolean isPicked = productDao.getTotalFoundCount() > 0;
    binding.btnUpload.setVisibility(isAllowDiscardOperationForPickedData && !isProcessOn() && isPicked ? View.VISIBLE : View.GONE);
    //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && isAllowDiscardOperationForPickedData && !isProcessOn() && isPicked ? View.VISIBLE : View.GONE);
  }
  
  private void updateRecyclerView(){
    showLog("updateRecyclerView", "called");
    fromListIndex += visibleListRecordLimit;
    updateLists(false);
  }
  
  @Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
  }
  
  @Override
  protected void isSessionOnChanged(final Boolean isInventorySessionOn){
    super.isSessionOnChanged(isInventorySessionOn);
    if(isInventorySessionOn == null) return;
    //    boolean isInvSessionOn = chkNotNullTrue(isInventorySessionOn);
    //    binding.llBtnStart.toggle(isInventorySessionOn);
    //    final boolean isInvCount = getSize() > 0;//AppCommonMethods.parseInt(chkNull(binding.txtInventoryStartScoreCount.getText().toString(), "0")) > 0;
  }
  
  @Override
  protected void isInventoryOnChanged(Boolean isSessionOn){
    super.isInventoryOnChanged(isSessionOn);
    final Boolean isInventoryOn = mainViewModel.getIsInventoryOn().getValue();
    if(isInventoryOn == null) return;
    else{
      binding.llBtnStart.toggle(isInventoryOn);
      updateUI();
    }
  }
  
  private void updateUI(){ updateUI(false); }
  
  private void updateUI(final boolean isUpdateList){
    final boolean isProcessOn = isProcessOn();
    final int size = getSize();
    binding.btnComplete.setEnabled(!isProcessOn);
    binding.btnComplete.setVisibility(!isShowingSearchPickView && !isProcessOn && size > 0 ? View.VISIBLE : View.GONE);
    binding.spinBrand.setEnabled(!isProcessOn);
    binding.spinCategory.setEnabled(!isProcessOn);
    binding.edtReplenishmentNameSearch.setEnabled(!isProcessOn);
    binding.listProducts.setEnabled(!isProcessOn);
    binding.tbListPick.setEnabled(!isProcessOn);
    
    binding.pdvOutwardStart.setEnabled(!isProcessOn);
    binding.rgPickSearchType.setEnabled(!isProcessOn);
    binding.rgPickSearchType.setVisibility(!isProcessOn?View.VISIBLE:View.GONE);
    binding.rgPickSearchType.setVisibility(!isProcessOn ? View.VISIBLE : View.GONE);
    binding.rbSearch.setEnabled(!isProcessOn);
    binding.rbPick.setEnabled(!isProcessOn);
    binding.txtPicked.setVisibility(!isActionPick && size > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
    if(isUpdateList) updateLists();
  }
  
  @Override
  protected void onTriggerPressed(){
    if(isTopInStack()){
      showLog(this.getClass().getSimpleName(), "onTriggerPressed");
      hideKeyboard();
      (isShowingSearchPickView ? binding.llBtnStartPick : binding.llBtnStart).performClick();
    }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.UPLOAD_OUTWARD_CARTON_DATA:
          if(true){
            //            if(isSuccess && jsonResponse != null && sessionObject != null){
            //              final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            //              if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
            //                hideProgressDialog();
            //                context.showCustomErrDialog(errMsg);
            //              }
            //            }
            final String status = extractString(jsonRequest, ParamConstants.STATUS, "").trim().replace(AppConstants.STATUS_PENDING, AppConstants.STATUS_SAVED).trim();
            final String statusMsg = extractString(jsonResponse, ParamConstants.MESSAGE, (!status.endsWith("ed") ? status + "ed" : status) + "!");
            final boolean isAccepted = status.equalsIgnoreCase(AppConstants.K_HU_STATUS_ACCEPT) || status.equalsIgnoreCase(AppConstants.TRIP_STATUS_COMPLETED) || status.equalsIgnoreCase(AppConstants.STATUS_COMPLETE) || status.equalsIgnoreCase(AppConstants.STATUS_COMPLETED) || status.equalsIgnoreCase(AppConstants.PICK_LIST_STATUS_COMPLETE);
            final ArrayList<String> listCompletedCartons = SharedPrefManager.getStringArrayList(batchId + ParamConstants.COMPLETED_CARTONS, new ArrayList<>(0));
            if(isAccepted){
              outwardBatchDao.updateAcceptedQty(batchId); //OR save completedCartons in outwardBatchDao as String
              if(!listCompletedCartons.contains(cartonNo)){
                listCompletedCartons.add(cartonNo);
                SharedPrefManager.setStringArrayList(batchId + ParamConstants.COMPLETED_CARTONS, listCompletedCartons);
              }
              if(isSuccess && jsonResponse != null && sessionObject != null){
                inventoryDao.updateUploaded(sessionObject.sessionId);
                uploadInventoryDao.updateUploaded(sessionObject.sessionId);
                //                if(!isSaveCompletedCartonAfterUpload && listCompletedCartons.contains(cartonNo)){
                //                  listCompletedCartons.remove(cartonNo);
                //                  SharedPrefManager.setStringArrayList(batchId+ParamConstants.COMPLETED_CARTONS,listCompletedCartons);
                //                }
              }
              showLog("uploadSavedCartons_SaveCount2", "" + uploadInventoryDao.getNonUploadedCount(AppCommonMethods.SessionType.OFF_RANGE.getValue()));
              productDao.resetProducts(sessionObject != null ? sessionObject.sessionType : getSessionType().getValue());
              mainViewModel.stopSession(sessionObject, true);
              final int batchCartons = uploadInventoryDao.getCartonCountFromBatchId(batchId);
              if(!isSaveCompletedCartonAfterUpload) uploadInventoryDao.deleteUploaded();
              else if(isSaveCompletedCartonAfterUpload && batchCartons > saveLimitForCompletedCartons){
                List<String> listUploadedCartons = uploadInventoryDao.getUploadedCartonsFromBatchId(batchId);
                if(isNonEmpty(listUploadedCartons)){
                  if(listUploadedCartons.size() <= (batchCartons - saveLimitForCompletedCartons))
                    uploadInventoryDao.deleteUploaded();
                  else
                    uploadInventoryDao.deleteUploaded(batchId, listUploadedCartons.subList(0, (batchCartons - saveLimitForCompletedCartons) + 1));
                }
                //uploadInventoryDao.deleteUploaded();
              }
              showLog("uploadSavedCartons_SaveCount3", "" + uploadInventoryDao.getNonUploadedCount(AppCommonMethods.SessionType.OFF_RANGE.getValue()));
            }
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, statusMsg), isAccepted);//String.format(getString(R.string.done_upload), getTypeCharCode())), isAccepted);
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