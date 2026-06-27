package com.itek.retail.ui.outward.tote;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.dp2px;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.extractStringArrayList;
import static com.itek.retail.common.AppCommonMethods.isAutoAcceptHUForHappyFlow;
import static com.itek.retail.common.AppCommonMethods.isAutoAcceptManualHUForHappyFlow;
import static com.itek.retail.common.AppCommonMethods.isAutoProcessHU;
import static com.itek.retail.common.AppCommonMethods.isAutoProcessManualHU;
import static com.itek.retail.common.AppCommonMethods.isHideEanExpQtyColumnForManualHU;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.HUProcessScanAdapter;
import com.itek.retail.adapter.ProdDisplayDataListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonInventoryFragment;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.HUDetailsDao;
import com.itek.retail.database.HUStatusDao;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.DialogSerialScanBinding;
import com.itek.retail.databinding.FragmentHuProcessStartBinding;
import com.itek.retail.model.HUStatus;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.LabelValues;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.TripInventory;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.customviews.MaxHeightRecyclerView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inward.grn.InwardGrnStartViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The HU Process start fragment.
 */
public class OutwardToteDCStartFragment extends RFIDSessionFragment {

  String batchId = "";
  String dcCode = "";
  String huNumber;
  String displayTripNo = "-";
  private int lowerTolerance;
  private int upperTolerance;
  private boolean allowMixTags;
  private boolean allowManualBarcodeScanning = false;
  private Integer totalQty = 0;
  private TripInventoryDao tripInventoryDao;
  private InwardGrnStartViewModel mViewModel;
  private FragmentHuProcessStartBinding binding;
  private DialogSerialScanBinding dialogSerialScanBinding;
  private AlertDialog huScanDialog = null;
  private String typeIO = "";
  private String labelTrip = "";
  private String labelHU = "";
  private String labelArticle = "";
  private String labelSku = "";
  private boolean hasArticleData = false;
  private boolean hasEpcData = false;
  private boolean isHappyFlow = false;
  private boolean isManualHappyFlow = false;
  private boolean isViewOnlyDetails = false;
  private boolean isCompleted = false;
  List<TripInventory> dataList = new ArrayList<>(0);
  Set<String> eans = new HashSet<>(0);

  /**
   * Instantiates a new Inward grn start fragment.
   */
  public OutwardToteDCStartFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripInventoryDao = AppDatabase.getTripInventoryDao(context);
  }
  
  /*@Override
  public void setSessionObject(RFIDSession sessionObject){
    super.setSessionObject(sessionObject);
    if(sessionObject != null && sessionObject instanceof RFIDSession)
      this.sessionObject = (RFIDSession) sessionObject;
    showLog("inwardSession", "" + (this.sessionObject != null));
  }*/
  
  @SuppressLint("NewApi")
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    binding = FragmentHuProcessStartBinding.inflate(inflater, container, false);
    //header title with setting btn with config_power
    binding.header.imgConfigSync.setVisibility(View.VISIBLE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())) return;
        if(chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())) return;
        binding.llSeekbarPower.setVisibility(binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
      }
    });
    
    if(getArguments() != null){
      getBundleData(getArguments());
    }
    else popBackStack();

    binding.txtTripNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt__no), labelTrip, displayTripNo), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtHUNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt__no), labelHU, huNumber), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    binding.txtHUNumberScan.setSelected(true);
    binding.txtTripNumberScan.setSelected(true);
    binding.txtTripNumberScan.setVisibility(View.GONE);

    // binding.txtHuTotalQty.setText("" + totalQty);
    binding.ctwInwardStart.setTotal(totalQty);
    
    binding.llManualScanBarcode.setVisibility(allowManualBarcodeScanning && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || getSize() < totalQty) ? View.VISIBLE : View.GONE);
    //binding.btnStartStop.setVisibility(!chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? View.VISIBLE : View.GONE);
    //binding.btnStartStop.toggle(isInventorySessionOn);
    binding.llUpload.setVisibility(!isProcessOn() && getSize() > 0 ? View.VISIBLE : View.GONE);
    
    binding.llListHeader.textScanEanNo.setText(hasArticleData ? labelArticle : labelSku);
    //binding.llListHeader.textScanSerialNo.setVisibility(isInwardWithSerial() ? View.INVISIBLE : View.GONE);
    binding.llListHeader.imgInfo.setVisibility(hasArticleData ? View.INVISIBLE : View.GONE);
    binding.llListHeader.textScanEanQty.setVisibility(eans.size() == 1 ? View.VISIBLE : View.GONE);
    
    binding.listTripHuScan.setAdapter(new HUProcessScanAdapter(context, OutwardToteDCStartFragment.this, dataList));
    binding.listTripHuScan.setLayoutManager(new LinearLayoutManager(context));
    
    //binding.btnUpload.setImageResource(R.drawable.ic_upload);
    binding.lblUpload.setText(getString(R.string.btn_upload));
    
    binding.swtHuScanDetails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        showLog("swtHuScanDetails", "onCheckChanged:" + isChecked);
        binding.swtHuScanDetails.setSwitchTextAppearance(context, isChecked ? R.style.TextStyleExtraSmallWhite : R.style.TextStyleExtraSmall);
        toggleUI(isChecked);
      }
    });
    
    notifyAdapter();
    
    binding.llSeekbarPower.setupProgress(mainViewModel);
    
    binding.llManualScanBarcode.setOnClickListener(v -> {
      
      if(context.customAlertDialog != null && context.customAlertDialog.isShowing()) return;
      if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
      if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())) return;
      if(chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())) return;
      if(getSize() >= totalQty)
        context.showCustomErrDialog(getString(R.string.msg_qty_match_barcode_scan_not_allowed));
      else mainViewModel.softScan();
    });
    
    binding.btnStartStop.setOnClickListener(v -> {
      if(context.customAlertDialog != null && context.customAlertDialog.isShowing()) return;
      if(binding.clScanResultAction.getVisibility() == View.VISIBLE) return;
      if(binding.clScan.getVisibility() != View.VISIBLE) return;
      if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
      if(chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())) return;
      toggleInventory();
    });
    
    /*binding.llUpload.setOnClickListener(v -> {
      if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())) return;
      if(chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())) return;
      if(getSize() > 0){
        if(isSetInwOnline){
          context.showCustomConfirmDialog(getString(R.string.msg_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              apiCall(AppConstants.SESSION_ACTION_UPLOAD);
            }
          });
        }
        else callAlert();
      }
    });*/
    
    binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        processUploadBtn();
      }
    });

        /*binding.btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
                    return;
                processUploadBtn();
            }
        });

        binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isSuccessfulSwipe) {
                if (!isTopInStack()) return;
                boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
                if (isSwiped) {
                    binding.btnSwipeUpload.reset();
                    processUploadBtn();
                }
            }
        });

        setUploadConstraints(binding.clScan, binding.btnUpload, binding.btnSwipeUpload);*/
    
    binding.txtTotExpQty.setText(String.format(getString(R.string.txt_exp_qty), "" + totalQty));
    binding.txtTotScanQty.setText(String.format(getString(R.string.txt_scan_qty), "" + 0));
    
    binding.btnRescan.setTextColor(context.getColorPrimaryDarkFromTheme());// ? getResources().getColor(R.color.txt_button) : colorPrimaryDarkFromTheme);
    binding.btnReject.setTextColor(context.getColorPrimaryDarkFromTheme());// ? getResources().getColor(R.color.txt_button) : colorPrimaryDarkFromTheme);
    
    binding.btnComplete.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        final String reason = binding.txtHuScanError.getText().toString().trim();
        if(!isHappyFlow && !isManualHappyFlow){
          context.showCustomAlertDialog("", reason + "\n" + getString(R.string.err_proceed_with_exception), null, false, false, getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
              callUploadAPI(AppConstants.HU_STATUS_ACCEPT, reason);
            }
          }, getString(R.string.btn_no), null);
        }
        else showCustomConfirmationDialog(reason, AppConstants.K_HU_STATUS_ACCEPT, isHappyFlow);
      }
    });
    
    binding.btnReject.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        final String reason = binding.txtHuScanError.getText().toString().trim();
        showCustomConfirmationDialog(reason, AppConstants.K_HU_STATUS_REJECT, isHappyFlow);
      }
    });
    
    binding.btnRescan.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        final String reason = binding.txtHuScanError.getText().toString().trim();
        showCustomConfirmationDialog(reason, AppConstants.K_HU_STATUS_RESCAN, isHappyFlow);
      }
    });
    
    setSessionAction(AppConstants.SESSION_ACTION_START);
    
    return binding.getRoot();
  }

  
  private void processUploadBtn(){
    /*context.showCustomConfirmDialog(getString(R.string.msg_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){*/
    processScannedData();
    toggleUI();//apiCall(AppConstants.SESSION_ACTION_UPLOAD);
      /*}
    });*/
  }
  
  /**
   * Get bundle data.
   *
   * @param args the args
   */
  private void getBundleData(Bundle args){
    typeIO = extractString(getArguments(), ParamConstants.TYPE, extractString(getArguments(), ParamConstants.OPERATION_TYPE, AppConstants.OUTWARD));

    dcCode = extractString(args, ParamConstants.DC_CODE, "");
    batchId = extractString(args, ParamConstants.BATCH_ID, "");
    huNumber = extractString(args, ParamConstants.LPN_NO,  "");
    totalQty = extractInt(args, ParamConstants.EXPECTED_QTY, 0);

    ArrayList<String> eanList = extractStringArrayList(args, ParamConstants.LIST_TOTE_EANS);
    eans.addAll(eanList);

    SharedPrefManager.setTripNo(displayTripNo);
    SharedPrefManager.setHuNo(huNumber);

    labelTrip = SharedPrefManager.getString(ParamConstants.LABEL_TRIP, getString(R.string.lbl_trip));
    labelHU = SharedPrefManager.getString(ParamConstants.LABEL_HU, getString(R.string.lbl_lpn));
    labelArticle = SharedPrefManager.getString(ParamConstants.LABEL_ARTICLE, getString(R.string.lbl_article_no));
    labelSku = SharedPrefManager.getString(ParamConstants.LABEL_SKUID, getString(R.string.lbl_ean));
    
    lowerTolerance = 0;//SharedPrefManager.getInt(huNumber + ParamConstants.LOWER_TOLERANCE, SharedPrefManager.getInt(displayTripNo + ParamConstants.LOWER_TOLERANCE, SharedPrefManager.getInt(ParamConstants.LOWER_TOLERANCE)));//SharedPrefManager.getInt(tripNumber+ParamConstants.LOWER_TOLERANCE,SharedPrefManager.getInt(ParamConstants.LOWER_TOLERANCE,0)));//tripStatus != null ? tripStatus.getLowerTolerance() : extractInt(args, ParamConstants.K_LOWER_TOLERANCE, 0);
    upperTolerance = 0;//SharedPrefManager.getInt(huNumber + displayTripNo + ParamConstants.UPPER_TOLERANCE, SharedPrefManager.getInt(displayTripNo + ParamConstants.UPPER_TOLERANCE, SharedPrefManager.getInt(ParamConstants.UPPER_TOLERANCE, 0)));//tripStatus != null ? tripStatus.getUpperTolerance() : extractInt(args, ParamConstants.K_UPPER_TOLERANCE, 0);
    allowMixTags = true;//SharedPrefManager.getBoolean(huNumber + displayTripNo + ParamConstants.ALLOW_MIX_TAG_TYPE, SharedPrefManager.getBoolean(displayTripNo + ParamConstants.ALLOW_MIX_TAG_TYPE, SharedPrefManager.getBoolean(ParamConstants.ALLOW_MIX_TAG_TYPE, AppCommonMethods.isAllowMixTagType)));//tripStatus != null ? tripStatus.isMixTagTypeCompulsion() : extractBoolean(args, ParamConstants.K_MIX_TAG_COMPULSION, false);
    allowManualBarcodeScanning = false;//SharedPrefManager.getBoolean(huNumber + displayTripNo + ParamConstants.ALLOW_BARCODE_SCANNING, SharedPrefManager.getBoolean(displayTripNo + ParamConstants.ALLOW_BARCODE_SCANNING, SharedPrefManager.getBoolean(ParamConstants.ALLOW_BARCODE_SCANNING, AppCommonMethods.isAllowBarcodeScanning)));//tripStatus != null ? tripStatus.isManualBarCodeCompulsion() : extractBoolean(args, ParamConstants.K_MANUAL_BARCODE_COMPULSION, false);
    
    showLog("lowerTolerance", "" + lowerTolerance);
    showLog("upperTolerance", "" + upperTolerance);
    showLog("allowMixTags", "" + allowMixTags);
    showLog("allowManualBarcodeScanning", "" + allowManualBarcodeScanning);
    
    //totalQty = 0;//huStatus.getExpQty();//tripInventoryDao.getCurrentHuQty(huNumber, tripNumber);
    //if(chkNull(totalQty, 0) <= 0) popBackStack();
  }


  @Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
  }
  
  @Override
  protected void onReaderPowerChanged(Integer power){
    binding.llSeekbarPower.updateReaderPower(mainViewModel, power);
  }
  
  private void processScannedData(){
    //final int expectedScanned = huStatus.getExpQty();//tripInventoryDao.getArticlesOriginalEanCount(huNumber, tripNumber);
    /*showLog("EXPECTEDQTYSCANNED", "" + expectedScanned);
    showLog("extraEanCount", "" + extracount);
    showLog("nonencodedcount", "" + nonencoded);*/
    isManualHappyFlow = false;
    boolean isHappy = tripInventoryDao.getIsHappyFlow(huNumber, displayTripNo);
    AppCommonMethods.showLog("isHappy", "" + isHappy);
    if(isHappy){
      List<Boolean> list = tripInventoryDao.getIsHappyFlowEanCount(huNumber, displayTripNo,false);
      if(list != null && !list.isEmpty() && list.size() > 0){
        for(Boolean b : list)
          if(!b){
            isHappy = false;
            break;
          }
      }
    }
    isHappyFlow = isHappy;
    
    //binding.btnComplete.setText(isHappyFlow?R.string.btn_ok:R.string.btn_complete);
    binding.btnComplete.setVisibility(View.VISIBLE);
    binding.txtHuScanError.setVisibility(View.VISIBLE);
    binding.txtHuScanError.setSelected(true);
    binding.txtHuScanError.setText("");
    
    binding.btnReject.setVisibility(!isHappyFlow ? View.VISIBLE : View.GONE);
    binding.btnRescan.setVisibility(!isHappyFlow ? View.VISIBLE : View.GONE);
    binding.txtHuScanError.setTextColor(getResources().getColor(isHappyFlow ? R.color.green : R.color.red));
    //binding.txtHuScanError.setGravity(isHappyFlow ? Gravity.CENTER : Gravity.CENTER_VERTICAL);
    
    if(isHappyFlow){
      binding.txtHuScanError.setText(getString(R.string.success_qty_match));
      if(isAutoAcceptHUForHappyFlow || isAutoProcessHU){
        callUploadAPI(AppConstants.HU_STATUS_ACCEPT, "");
      }
      return;
    }
    
    final int extraCount = tripInventoryDao.getExtraEanCount(huNumber, displayTripNo);
    final int nonencoded = tripInventoryDao.getNonEncodedCount(huNumber, displayTripNo);
    //final int unknown = tripInventoryDao.getUnknownCount(huNumber, tripNumber);
    
    final boolean isQuantityMatch = isHappyFlow || tripInventoryDao.getIsEanQtyMatched(huNumber, displayTripNo);
    final boolean isLessAcceptableCount = !isQuantityMatch && lowerTolerance > 0 && tripInventoryDao.getIsEanQtyLowerPercentMatched(huNumber, lowerTolerance, displayTripNo);
    final boolean isLessRejectedCount = !isQuantityMatch && lowerTolerance > 0 && !isLessAcceptableCount && tripInventoryDao.getIseanqtyLowerPercent(huNumber, displayTripNo);
    final boolean isGraterAcceptableCount = !isQuantityMatch && upperTolerance > 0 && tripInventoryDao.getIseanqtyUpperPercentMatched(huNumber, upperTolerance, displayTripNo);
    final boolean isGraterRejectedCount = !isQuantityMatch && upperTolerance > 0 && !isGraterAcceptableCount && tripInventoryDao.getIseanqtyUpperPercentExceed(huNumber, displayTripNo);
    final boolean isTagTypeMisMatch = !allowMixTags && tripInventoryDao.isMixedTagsPresent(huNumber, displayTripNo);
    final boolean isNonEncoded = nonencoded > 0;//tripInventoryDao.getIsNonEncoded(huNumber, tripNumber);
    //final boolean isUnknown = unknown>0;//tripInventoryDao.getIsEanExtra(huNumber, tripNumber);//tripInventoryDao.getIsUnknown(huNumber, tripNumber);
    final boolean isExtraEan = extraCount > 0;//tripInventoryDao.getIsEanExtra(huNumber, tripNumber);
    final Boolean isDuplicate = false;//isNonEmpty(dbAllHUsDublicateRfid) && dbAllHUsDublicateRfid.contains(huNumber);
    final boolean isRejected = !isHappyFlow && (isDuplicate || isTagTypeMisMatch || isNonEncoded || isLessRejectedCount || isGraterRejectedCount);// || isUnknown || !(isQuantityMatch || isGraterAcceptableCount || isLessAcceptableCount));
    
    final boolean isLessQty = lowerTolerance <= 0 && upperTolerance <= 0 && getSize() < totalQty;
    final boolean isExcessQty = lowerTolerance <= 0 && upperTolerance <= 0 && getSize() > totalQty;
    binding.btnComplete.setVisibility(!isRejected ? View.VISIBLE : View.GONE);
    
    String message = "";
    if(isNonEncoded){
      message = "\t" + String.format(getResources().getString(R.string.err_p2_hu_non_encoded), "" + nonencoded);
    }
    /*if(!tripStatus.isManualTrip() && isExtraEan){
      message += "\t" + String.format(getResources().getString(R.string.err_p2_hu_unknown), "" + extraCount);
    }*/
    if(isQuantityMatch){
      message += "\t" + getResources().getString(R.string.err_p2_hu_qty_mismatch);
    }
    if(isLessAcceptableCount || isLessQty){
      message += "\t" + getResources().getString(R.string.err_p2_hu_qty_mismatch_less);
    }
    if(isGraterAcceptableCount || isExcessQty){
      message += "\t" + getResources().getString(R.string.err_p2_hu_qty_mismatch_more);
    }
    if(isLessRejectedCount){
      message += "\t" + getResources().getString(R.string.err_p2_hu_qty_mismatch_less);
    }
    if(isGraterRejectedCount){
      message += "\t" + getResources().getString(R.string.err_p2_hu_qty_mismatch_more);
    }
    //Show Qty Matched in green for Manual Trip
    /*if(tripStatus.isManualTrip() && isNullOrEmpty(message)){
      isManualHappyFlow = true;
      binding.txtHuScanError.setTextColor(getResources().getColor(R.color.green));
      binding.txtHuScanError.setText(getString(R.string.success_qty_match));
      if(isAutoAcceptManualHUForHappyFlow || isAutoProcessManualHU)
        callUploadAPI(AppConstants.HU_STATUS_ACCEPT, "");
    }
    else*/ binding.txtHuScanError.setText(message.trim().replaceAll("\t", ", "));
    
    //reason = message;
  }
  
  /**
   * Set alert display data list recycler view.
   *
   * @param expectedScanned the expected scanned
   * @param nonencoded      the nonencoded
   * @param extracount      the extracount
   * @return the recycler view
   */
  RecyclerView setAlertDisplayDataList(int expectedScanned, int nonencoded, int unknown, int extracount){
    List<LabelValues> listData = new ArrayList<>(0);
    try{
      // listData.add(new LabelValues(getString(R.string.lbl_inward_alert_total_qty_), binding.txtHuTotalQty.getText().toString()));
      // listData.add(new LabelValues(getString(R.string.lbl_inward_alert_total_scanned_qty), binding.txtHuScanQty.getText().toString()));
      //if(expectedScanned>0)
      listData.add(new LabelValues(getString(R.string.lbl_inward_alert_expected_scanned_qty), "" + expectedScanned));
      if(nonencoded > 0)
        listData.add(new LabelValues(getString(R.string.lbl_inward_alert_unencoded_scanned_qty), "" + nonencoded));
      if(extracount > 0)
        listData.add(new LabelValues(getString(R.string.lbl_inward_alert_extra_scanned_qty), "" + extracount));
    }
    catch(Exception e){
      e.printStackTrace();
    }
    
    MaxHeightRecyclerView listDisplayData = isNonEmpty(listData) ? new MaxHeightRecyclerView(context) : null;
    if(listDisplayData != null){
      int margin = getResources().getDimensionPixelSize(R.dimen.dp_15);
      LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      llParams.setMargins(margin, 0, margin, 0);
      listDisplayData.setLayoutParams(llParams);
      listDisplayData.setPadding(margin, 0, margin, 0);
      listDisplayData.setAdapter(new ProdDisplayDataListAdapter((MainActivity) context, listData, true));
      listDisplayData.setLayoutManager(new LinearLayoutManager(context));
    }
    return listDisplayData;
  }
  
  /**
   * Notify adapter.
   */
  public void notifyAdapter(){
    if(dataList != null){
      dataList.clear();
      List<TripInventory> newList = tripInventoryDao.getCurrentHuDetails(huNumber, displayTripNo, false);
      if(isNonEmpty(newList)) dataList.addAll(newList);
      if(binding != null && binding.btnUpload != null /*&& binding.btnSwipeUpload != null*/){
        binding.btnUpload.setVisibility(isNonEmpty(dataList) && getSize() > 0 && !isProcessOn() ? View.VISIBLE : View.GONE);
        //binding.btnSwipeUpload.setVisibility(isNonEmpty(dataList) && getSize() > 0 && !isProcessOn() ? View.VISIBLE : View.GONE);
      }
      if(binding != null && binding.listTripHuScan != null && binding.listTripHuScan.getAdapter() != null && binding.listTripHuScan.getAdapter() instanceof RecyclerView.Adapter)
        binding.listTripHuScan.getAdapter().notifyDataSetChanged();
    }
  }
  
  public void discardData(){
    //tripInventoryDao.deleteHU(huNumber,tripNumber);
    tripInventoryDao.deleteAllTripInventory();
    SharedPrefManager.setTripNo("");
    SharedPrefManager.setHuNo("");
  }

  @Override
  public void updateLists(){
    super.updateLists();
    notifyAdapter();
  }
  
  @Override
  public void onBackPressed(){
    if(sessionObject != null && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())){
      if(isProcessOn()){
        context.showCustomAlertDialog("", String.format(getString(R.string.err_op_back_press), getTypeCharCode(), getSessionType().name().replaceAll("_", " ")), false, true, getString(R.string.btn_ok), null);
      }
      else{
        if(binding.clScanResultAction.getVisibility() == View.VISIBLE && (isViewOnlyDetails || (!isHappyFlow && !isManualHappyFlow && binding.btnComplete.getVisibility() == View.VISIBLE && getSize() < totalQty))){
          if(isViewOnlyDetails) binding.swtHuScanDetails.setChecked(false);
          else toggleUI();
        }
        else{
          if(getSize() > 0){
            context.showCustomAlertDialog(getString(R.string.title_inventory_stop_alert), getString(R.string.err_discard_scanned_data), getString(R.string.btn_yes), (dialogInterface, i) -> {
                mainViewModel.stopSession(sessionObject, true);
                discardData();
                context.clearIOConfig(displayTripNo, huNumber);
                context.popBackStack();

            }, getString(R.string.btn_no), null);
          }
          else{
              mainViewModel.stopSession(sessionObject, true);
              discardData();
              context.clearIOConfig(displayTripNo, huNumber);
              context.popBackStack();
          }
        }
      }
    }
    else popBackStack();
  }
  
  /**
   * Show custom confirmation dialog.
   *
   * @param reason           the reason
   * @param action           the action
   * @param isLocalHappyFlow the is local happy flow
   */
  public void showCustomConfirmationDialog(String reason, final String action, boolean isLocalHappyFlow){
    showCustomConfirmationDialog(reason, action, isLocalHappyFlow, null);
  }
  
  /**
   * Show custom confirmation dialog.
   *
   * @param reason           the reason
   * @param action           the action
   * @param isLocalHappyFlow the is local happy flow
   * @param duplicateList    the duplicate list
   */
  public void showCustomConfirmationDialog(String reason, final String action, boolean isLocalHappyFlow, List<String> duplicateList){
    context.showCustomConfirmDialog((action.matches("^(?i)(" + AppConstants.K_HU_STATUS_RESCAN + "|" + AppConstants.K_HU_STATUS_REJECT + ")$") ? getString(R.string.err_discard_scanned_data) : "") + "\n" + String.format(getString(R.string.msg__confirm_action), action, labelHU), R.string.btn_ok, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        if(action.equals(AppConstants.K_HU_STATUS_ACCEPT)){
          callUploadAPI(AppConstants.HU_STATUS_ACCEPT, reason);
          /*mainViewModel.deleteSession(sessionObject);
          tripInventoryDao.updateHUStatus(huNumber, "A", !isLocalHappyFlow, isLocalHappyFlow ? "" : reason, tripNumber);
          updateLists();
          popBackStack();*/
        }
        else if(action.equals(AppConstants.K_HU_STATUS_REJECT)){
          if(SharedPrefManager.getBoolean(ParamConstants.IS_API_CALL_FOR_REJECT_HU, AppCommonMethods.isAPIBasedRejectHU))
            callUploadAPI(AppConstants.HU_STATUS_REJECT, reason);
          else{
            if(AppCommonMethods.isShowErrorDialogForNonAPIBasedRejectHU){
              context.showCustomMsgDialog(String.format(getString(R.string.err__rejected_successfully), labelHU), false, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                  mainViewModel.stopSession(sessionObject, true);
                  //tripInventoryDao.updateHUStatus(huNumber, "R", !isLocalHappyFlow, reason, tripNumber);//add reason in query
                  discardData();
                  context.clearIOConfig(displayTripNo, huNumber);
                  popBackStack();
                }
              });
            }
            else{
              mainViewModel.stopSession(sessionObject, true);
              //tripInventoryDao.updateHUStatus(huNumber, "R", !isLocalHappyFlow, reason, tripNumber);//add reason in query
              discardData();
              context.clearIOConfig(displayTripNo, huNumber);
              popBackStack();
            }
          }
          //mainViewModel.deleteSession(sessionObject);
          /*if(isNonEmpty(duplicateList)){
            tripInventoryDao.deleteDuplicateHU(duplicateList, tripNumber);
            tripInventoryDao.updateDuplicateHUattemptcount(duplicateList, tripNumber);
          }
          else
            tripInventoryDao.updateHUStatus(huNumber, "R", !isLocalHappyFlow, reason, tripNumber);//add reason in query
          updateLists();
          popBackStack()*/
        }
        else if(action.equals(AppConstants.K_HU_STATUS_RESCAN)){
          //mainViewModel.deleteSession(sessionObject);
          tripInventoryDao.deleteHU(huNumber, displayTripNo);
          tripInventoryDao.updateHUattemptcount(huNumber, displayTripNo);
          notifyAdapter();
          toggleUI();
        }
      }
    });
  }
  
  private void callUploadAPI(String action, String reason){
    try{
      JSONObject request = new JSONObject();
      request.put(ParamConstants.DC_CODE,dcCode);
      request.put(ParamConstants.BATCH_ID,batchId);
      request.put(ParamConstants.EPC_EXPECTED_QTY,totalQty);
      request.put(ParamConstants.OPERATION_TYPE, typeIO);
      //request.put(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
      //request.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
      //request.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus != null && tripStatus.isManualTrip());
      //request.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus != null ? tripStatus.excelTripType : "");
      request.put(ParamConstants.REMARK, reason);
      request.put(ParamConstants.LPN_NO, huNumber);
      request.put(ParamConstants.STATUS, action.replace(AppConstants.HU_STATUS_ACCEPT,AppConstants.STATUS_MATCHED));
      //request.put(ParamConstants.EXP_QTY, huStatus.getExpQty());
      List<TripInventory> list = tripInventoryDao.getAllScannedData1(displayTripNo, huNumber);
      request.put(ParamConstants.EPC_SCAN_QTY,list.size());
      JSONArray jhuArray = new JSONArray();
      JSONArray jArrayRFIDItems = new JSONArray();
      JSONArray jArrayBarcodeItems = null;
      String barcode ="";
      if(isNonEmpty(list)){
        for(TripInventory tripInventory : list){
          /*if(!hu.equalsIgnoreCase(tripInventory.huNo)){
            if(huobj != null){
              huobj.put(ParamConstants.ITEMS, jArrayRFIDItems);
              huobj.put(ParamConstants.K_TRIP_ITEM_BARCODE, jArrayBarcodeItems);
              jhuArray.put(huobj);
              hurecivedqty = 0;
            }
            
            hu = tripInventory.huNo;
            hurecivedqty = hurecivedqty + tripInventory.eanQty;
            
            huobj = new JSONObject();
            jArrayRFIDItems = new JSONArray();
            jArrayBarcodeItems = new JSONArray();
            huobj.put(ParamConstants.K_TRIP_DELIVERY_NUMBER, tripInventory.deliveryNo);
            huobj.put(ParamConstants.K_TRIP_HU_NUMBER, hu);
            huobj.put(ParamConstants.K_TRIP_RECEIVED_QTY, hurecivedqty);
            huobj.put(ParamConstants.K_TRIP_HU_STATUS, tripInventory.status.replaceFirst("A", "C"));
            huobj.put(ParamConstants.K_TRIP_HU_REASON, tripInventory.reason);
          }
          else if(tripInventory.epc.isEmpty()){
            hurecivedqty = hurecivedqty + tripInventory.eanQty;
          }*/
          if(!barcode.equalsIgnoreCase(tripInventory.ean)){
            if(jArrayRFIDItems.length()>0){
              JSONObject jsonEanObj = new JSONObject();
              jsonEanObj.put(ParamConstants.EAN,barcode);
              jsonEanObj.put(ParamConstants.QUANTITY,jArrayRFIDItems.length());
              jsonEanObj.put(ParamConstants.RFID, jArrayRFIDItems);
              jhuArray.put(jsonEanObj);
            }
            barcode=tripInventory.ean;
          }
          
          if(tripInventory.epc.length() > 0){
            String userAction = tripInventory.userAction;
            
            JSONObject item = tripInventory.toJson(context, false);
            JSONObject barcodeItem = tripInventory.toBarcodeJson();
            
            if(userAction.equalsIgnoreCase("RFID")){
              jArrayRFIDItems.put(item);
            }
            else{
              jArrayBarcodeItems.put(barcodeItem);
            }
          }
        }
        if(jArrayRFIDItems.length()>0){
          JSONObject jsonEanObj = new JSONObject();
          jsonEanObj.put(ParamConstants.EAN,barcode);
          jsonEanObj.put(ParamConstants.QUANTITY,jArrayRFIDItems.length());
          jsonEanObj.put(ParamConstants.RFID, jArrayRFIDItems);
          jhuArray.put(jsonEanObj);
        }
        //request.put(ParamConstants.RFID, jArrayRFIDItems);
        request.put(ParamConstants.EAN_INFO, jhuArray);
      }
      showLog("Request_",request.toString());
      //callWebService(action.equalsIgnoreCase(AppConstants.HU_STATUS_ACCEPT) ? URLConstants.UPLOAD_LPN_FOR_OUTWARD : URLConstants.REJECT_HU, request, getString(R.string.progress_msg_uploading_data));
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    super.onDataSizeChanged(size);
    if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
    if(size == null) return;
    showLog("Size", "" + size);
    binding.llManualScanBarcode.setVisibility(allowManualBarcodeScanning && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || getSize() < totalQty) ? View.VISIBLE : View.GONE);
    binding.btnStartStop.setVisibility(!chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? View.VISIBLE : View.GONE);
    binding.llUpload.setVisibility(!isProcessOn() && getSize() > 0 ? View.VISIBLE : View.GONE);
    binding.ctwInwardStart.setScore(chkNull(size, 0));
    // binding.txtHuScanQty.setText("" + chkNull(size, 0));
    binding.txtTotScanQty.setText(String.format(getString(R.string.txt_scan_qty), "" + chkNull(size, 0)));
    double per = totalQty > 0 ? (chkNull(size, 0) * 100) / totalQty : 0;
    int percentage = (int) per;
    // binding.progressbarHuScore.setProgress(percentage);
    updateLists();
  }
  
  private void toggleUI(){
    toggleUI(false);
  }
  
  private void toggleUI(final boolean isViewOnly){
    isViewOnlyDetails = isViewOnly;
    if(isProcessOn()) return;
    if(binding.llSeekbarPower.getVisibility() == View.VISIBLE)
      binding.llSeekbarPower.setVisibility(View.GONE);
    boolean isShowingScanResultScreen = binding.clScanResultAction.getVisibility() == View.VISIBLE;
    binding.clScan.setVisibility(isShowingScanResultScreen ? View.VISIBLE : View.GONE);
    binding.clScanResultAction.setVisibility(!isShowingScanResultScreen ? View.VISIBLE : View.GONE);
    binding.header.imgConfigSync.setVisibility(isShowingScanResultScreen ? View.VISIBLE : View.GONE);
    binding.txtHuScanError.setVisibility(!isViewOnlyDetails ? View.VISIBLE : View.GONE);
    binding.llButtons.setVisibility(!isViewOnlyDetails ? View.VISIBLE : View.GONE);
    binding.swtHuScanDetails.setVisibility(isShowingScanResultScreen || isViewOnlyDetails ? View.VISIBLE : View.GONE);
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack() && mainViewModel != null && (allowManualBarcodeScanning || (dialogSerialScanBinding != null && huScanDialog != null && huScanDialog.isShowing()))){
      mainViewModel.getBarcodeReaderInstance(getSessionType());
    }
  }
  
  @Override
  protected void isInventoryOnChanged(Boolean isInventoryOn){
    super.isInventoryOnChanged(isInventoryOn);
    if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
    final boolean isProcessOn = isProcessOn();
    final boolean isInvOn = chkNotNullTrue(isInventoryOn);
    binding.btnStartStop.toggle(isInvOn);
    // binding.btnStartStopScan.setImageResource(chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? R.drawable.ic_start_disable : isInvOn ? R.drawable.ic_stop : R.drawable.ic_start);
    binding.textStartStop.setText(isInvOn ? R.string.lbl_stop : R.string.lbl_start);
    binding.swtHuScanDetails.setVisibility(!isProcessOn ? View.VISIBLE : View.GONE);
    binding.swtHuScanDetails.setEnabled(!isProcessOn);
    binding.llSeekbarPower.setVisibility(!isProcessOn && binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
    binding.llManualScanBarcode.setVisibility(allowManualBarcodeScanning && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || getSize() < totalQty) ? View.VISIBLE : View.GONE);
    binding.btnStartStop.setVisibility(!chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? View.VISIBLE : View.GONE);
    //binding.llUpload.setVisibility(!isProcessOn() && getSize() > 0 ? View.VISIBLE : View.GONE);
    binding.btnUpload.setVisibility(getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
    //binding.btnSwipeUpload.setVisibility(getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
  }
  
  @Override
  protected void onTriggerPressed(){
    if(dialogSerialScanBinding != null && dialogSerialScanBinding.edtSearch != null)
      dialogSerialScanBinding.edtSearch.performScan();
    else binding.btnStartStop.performClick();
  }
  
  @Override
  public void onDestroyView(){
    super.onDestroyView();
    mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
    sessionObject = null;
  }
  
  /**
   * Set session action.
   *
   * @param action the action
   */
  public void setSessionAction(String action){
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.sessionType = typeIO.equalsIgnoreCase(AppConstants.OUTWARD) ? AppCommonMethods.SessionType.OUTWARD.getValue() : AppCommonMethods.SessionType.INWARD.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.INVENTORY.getValue();
      sessionObject.eans = eans.toString();
      sessionObject.userId = SharedPrefManager.getUserID();
      Calendar cc = Calendar.getInstance();
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, 24);
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc);
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject,eans, false);
      
    }
    else if(sessionObject != null && !action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){

    }
  }

  public void setSerialNo(final String msg,final String serialNo, Bundle args) {
    if(isNonEmpty(msg)) context.showCustomSuccessDialog(chkNull(msg,""));
    final TripInventory tripInventory = (TripInventory) extractSerializable(args, TripInventory.class);
    if(tripInventory != null && isNonEmpty(serialNo)){
      if(!tripInventoryDao.isEpcPresent(displayTripNo, huNumber, tripInventory.epc)){
        tripInventory.serialNo=serialNo.toUpperCase();
        tripInventoryDao.insertTripInventoryData(tripInventory);
      }
      if(isNullOrEmpty(msg)) context.showCustomSuccessDialog(String.format(getString(R.string.msg_serial_saved),tripInventory.serialNo));
      if(huScanDialog != null && huScanDialog.isShowing()) huScanDialog.dismiss();
    }
  }
  
  @Override
  public void apiCall(final String action){
    new Handler().post(new Runnable(){
      @Override
      public void run(){
        try{
          showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          JSONObject requestParams = new JSONObject();
          requestParams.put(ParamConstants.SESSION_TYPE, AppCommonMethods.SessionType.INWARD.name());
          requestParams.put(ParamConstants.OPERATION_TYPE, AppConstants.INWARD);
          requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD);
          JSONArray tripJsonArray = new JSONArray();
          JSONObject jobj = new JSONObject();
          jobj.put(ParamConstants.K_TRIP_NUMBER, displayTripNo);
          //jobj.put(ParamConstants.K_TRIP_TYPE, tripType);
          jobj.put(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
          List<TripInventory> list = tripInventoryDao.getAllTripInventory(displayTripNo, huNumber);
          JSONArray jhuArray = new JSONArray();
          String hu = "";
          int hurecivedqty = 0;
          JSONObject huobj = null;
          JSONArray jArrayRFIDItems = null;
          JSONArray jArrayBarcodeItems = null;
          if(isNonEmpty(list)){
            for(TripInventory tripInventory : list){
              if(!hu.equalsIgnoreCase(tripInventory.huNo)){
                if(huobj != null){
                  huobj.put(ParamConstants.ITEMS, jArrayRFIDItems);
                  huobj.put(ParamConstants.K_TRIP_ITEM_BARCODE, jArrayBarcodeItems);
                  jhuArray.put(huobj);
                  hurecivedqty = 0;
                }
                
                hu = tripInventory.huNo;
                hurecivedqty = hurecivedqty + tripInventory.eanQty;
                
                huobj = new JSONObject();
                jArrayRFIDItems = new JSONArray();
                jArrayBarcodeItems = new JSONArray();
                huobj.put(ParamConstants.K_TRIP_DELIVERY_NUMBER, tripInventory.deliveryNo);
                huobj.put(ParamConstants.K_TRIP_HU_NUMBER, hu);
                huobj.put(ParamConstants.K_TRIP_RECEIVED_QTY, hurecivedqty);
                huobj.put(ParamConstants.K_TRIP_HU_STATUS, tripInventory.status.replaceFirst("A", "C"));
                huobj.put(ParamConstants.K_TRIP_HU_REASON, tripInventory.reason);
              }
              else if(tripInventory.epc.isEmpty()){
                hurecivedqty = hurecivedqty + tripInventory.eanQty;
              }
              
              if(tripInventory.epc.length() > 0){
                String userAction = tripInventory.userAction;
                
                JSONObject item = tripInventory.toJson(context, false);
                JSONObject barcodeItem = tripInventory.toBarcodeJson();
                
                if(userAction.equalsIgnoreCase("RFID")){
                  jArrayRFIDItems.put(item);
                }
                else{
                  jArrayBarcodeItems.put(barcodeItem);
                }
              }
            }
          }
          if(huobj != null){
            huobj.put(ParamConstants.ITEMS, jArrayRFIDItems);
            huobj.put(ParamConstants.K_TRIP_ITEM_BARCODE, jArrayBarcodeItems);
            
            jhuArray.put(huobj);
          }
          jobj.put(ParamConstants.K_TRIP_HU_DETAILS, jhuArray);
          tripJsonArray.put(jobj);
          requestParams.put(ParamConstants.K_TRIPS_DATA, tripJsonArray);
          
          callWebService(URLConstants.UPLOAD_INWARD, requestParams, getString(R.string.progress_msg_uploading_data));
        }
        catch(JSONException e){
          e.printStackTrace();
          hideProgressDialog();
        }
      }
    });
  }
  
  /*@Override
  public void popBackStack(){
    if(context.getSupportFragmentManager().findFragmentByTag(TripHUListFragment.class.getSimpleName())==null){ //tmp code
      context.loadFragment(new TripHUListFragment(),getArguments());
    }
    else super.popBackStack();
  }*/
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.SET_SESSION:
          break;
        case URLConstants.UPLOAD_INWARD:
          if(isSuccess){
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), true);
            mainViewModel.stopSession(sessionObject, true);
            discardData();
          }
          break;
        case URLConstants.ACCEPT_HU:
          if(isSuccess){
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialog, int which){
                mainViewModel.stopSession(sessionObject, true);
                //tripInventoryDao.updateHUStatus(huNumber, AppConstants.K_HU_STATUS_ACCEPT, !isLocalHappyFlow, reason, tripNumber);//add reason in query
                discardData();
                context.clearIOConfig(displayTripNo, huNumber);
                popBackStack();
              }
            });
          }
          break;
        case URLConstants.REJECT_HU:
          if(isSuccess){
            //Show as Error Dialog
            context.showCustomMsgDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), false, new DialogInterface.OnClickListener(){
              //context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialog, int which){
                mainViewModel.stopSession(sessionObject, true);
                //tripInventoryDao.updateHUStatus(huNumber, "R", !isLocalHappyFlow, reason, tripNumber);//add reason in query
                discardData();
                context.clearIOConfig(displayTripNo, huNumber);
                popBackStack();
              }
            });
          }
          break;
        case URLConstants.RELEASE_HU:
          if(isSuccess){
            mainViewModel.stopSession(sessionObject, true);
            discardData();
            context.clearIOConfig(displayTripNo, huNumber);
            popBackStack();
          }
          break;
        case URLConstants.VERIFY_PRODUCT_SERIAL:
          if(isSuccess){
            try{
              final String serialNo = extractString(jsonRequest,ParamConstants.SERIAL_NUMBER,"");
              final String msg = extractString(jsonResponse, ParamConstants.MESSAGE, "");
              final boolean isConfirmSerialUpdate = extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_UPDATE_SERIAL_NUMBER, false);
              if(isConfirmSerialUpdate){
                context.showCustomConfirmDialog(chkNull(msg, ""), R.string.btn_update, new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialog, int which){
                    try{
                      // Do not call the API, follow the same flow
                     /* jsonRequest.put(ParamConstants.IS_UPDATE_SERIAL_NUMBER, true);
                      callWebService(url, jsonRequest, args, getString(R.string.progress_msg_validate_data));*/
                      setSerialNo("",serialNo,args);
                    }
                    catch(Exception e){ e.printStackTrace(); }
                  }
                });
                return;
              }
              setSerialNo(msg,serialNo,args);
            }
            catch(Exception e){
              e.printStackTrace();
            }
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