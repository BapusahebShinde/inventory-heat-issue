package com.itek.retail.ui.inward1;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.dp2px;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isAutoAcceptHUForHappyFlow;
import static com.itek.retail.common.AppCommonMethods.isAutoAcceptManualHUForHappyFlow;
import static com.itek.retail.common.AppCommonMethods.isAutoProcessHU;
import static com.itek.retail.common.AppCommonMethods.isAutoProcessManualHU;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
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
import java.util.List;
import java.util.Objects;

/**
 * The HU Process start fragment.
 */
public class HuProcessStartFragment extends RFIDSessionFragment{
  
  private final String huNum = "";
  private boolean isInwSerialMandatory = false;
  List<TripInventory> dataList = new ArrayList<>(0);
  String tripNo;
  String displayTripNo;
  String tripType;
  String huNumber;
  String deliveryNumber;
  private int lowerTolerance;
  private int upperTolerance;
  private boolean allowMixTags;
  private boolean allowManualBarcodeScanning;
  private Integer totalQty = 0;
  private TripInventoryDao tripInventoryDao;
  private TripStatusDao tripStatusDao;
  private HUStatusDao huStatusDao;
  private HUDetailsDao huDetailsDao;
  private Inventory pickedTag;
  private InwardGrnStartViewModel mViewModel;
  private TripStatus tripStatus;
  private HUStatus huStatus;
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
  private boolean isInwWithSerial = false;

  /**
   * Instantiates a new Inward grn start fragment.
   */
  public HuProcessStartFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripStatusDao = AppDatabase.getTripStatusDao(context);
    huStatusDao = AppDatabase.getHUStatusDao(context);
    huDetailsDao = AppDatabase.getHUDetailsDao(context);
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
    
    if(AppCommonMethods.isLockAndRedirectToInProcessHU) updateInProgressHUStatus();
    
    binding.txtTripNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt__no), labelTrip, tripStatus != null ? chkNull(tripStatus.getRefTripNumber(), chkNull(tripStatus.getTripNumber(), displayTripNo)) : displayTripNo), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtHUNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt__no), labelHU, huNumber), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    binding.txtHUNumberScan.setSelected(true);
    binding.txtTripNumberScan.setSelected(true);
    
    // binding.txtHuTotalQty.setText("" + totalQty);
    binding.ctwInwardStart.setTotal(totalQty);
    
    binding.llManualScanBarcode.setVisibility(allowManualBarcodeScanning && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || getSize() < totalQty) ? View.VISIBLE : View.GONE);
    //binding.btnStartStop.setVisibility(!chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? View.VISIBLE : View.GONE);
    //binding.btnStartStop.toggle(isInventorySessionOn);
    binding.llUpload.setVisibility(!isProcessOn() && getSize() > 0 ? View.VISIBLE : View.GONE);
    
    binding.llListHeader.textScanEanNo.setText(hasArticleData ? labelArticle : labelSku);
    //binding.llListHeader.textScanSerialNo.setVisibility(isInwardWithSerial() ? View.INVISIBLE : View.GONE);
    binding.llListHeader.imgInfo.setVisibility(hasArticleData ? View.INVISIBLE : View.GONE);
    binding.llListHeader.textScanEanQty.setVisibility(!tripStatus.isManualTrip() || !isHideEanExpQtyColumnForManualHU ? View.VISIBLE : View.GONE);
    
    binding.listTripHuScan.setAdapter(new HUProcessScanAdapter(context, HuProcessStartFragment.this, dataList));
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
      if(isInwWithSerial){
        // Perform Single Operation, After single pick it will open dialog box to enter serial number
        mainViewModel.performPick("", 0, true, null);
      }
      else toggleInventory();
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
  
  private void updateInProgressHUStatus(){
    huStatusDao.updateInProgressHUStatus(typeIO, displayTripNo, huNumber);
  }
  
  /**
   * Get bundle data.
   *
   * @param args the args
   */
  private void getBundleData(Bundle args){
    typeIO = extractString(getArguments(), ParamConstants.TYPE, extractString(getArguments(), ParamConstants.OPERATION_TYPE, AppConstants.INWARD));
    final Object obj = extractSerializable(args, TripStatus.class);
    final Object obj1 = extractSerializable(args, HUStatus.class);
    tripStatus = obj != null && obj instanceof TripStatus ? (TripStatus) obj : null;
    huStatus = obj1 != null && obj1 instanceof HUStatus ? (HUStatus) obj1 : null;
    tripNo = tripStatus != null ? tripStatus.getTripNumber() : extractString(getArguments(), ParamConstants.TRIP_NUMBER, "");
    final String refTripNo = tripStatus != null ? tripStatus.getRefTripNumber() : extractString(getArguments(), ParamConstants.REFERENCE_TRIP_NUMBER, "");
    displayTripNo = tripStatus != null ? chkNull(tripStatus.getRefTripNumber(), chkNull(tripStatus.getTripNumber(), tripNo)) : chkNull(refTripNo, chkNull(tripNo, ""));
    
    isInwWithSerial = extractBoolean(args, ParamConstants.IS_INW_WITH_SERIAL_NUMBER, Objects.equals(getMenuCode(), AppConstants.MENU_CODE_INW_SERIAL));

    tripType = tripStatus != null ? tripStatus.getTripType() : extractString(args, AppConstants.TRIP_TYPE, "IN");
    deliveryNumber = extractString(args, AppConstants.DELIVERY_NUMBER, "");
    huNumber = huStatus != null ? huStatus.getHuNumber() : extractString(args, ParamConstants.K_TRIP_HU_NUMBER, extractString(args, AppConstants.HU_NUMBER, ""));
    if(isNonEmpty(displayTripNo) && isNonEmpty(huNumber)){
      SharedPrefManager.setTripNo(displayTripNo);
      SharedPrefManager.setHuNo(huNumber);
      if(tripStatus == null) tripStatus = tripStatusDao.getTripData(displayTripNo, typeIO);
      if(huStatus == null) huStatus = huStatusDao.getHUData(typeIO, displayTripNo, huNumber);
    }
    else popBackStack();
    
    labelTrip = SharedPrefManager.getString(ParamConstants.LABEL_TRIP, getString(R.string.lbl_trip));
    labelHU = SharedPrefManager.getString(ParamConstants.LABEL_HU, getString(R.string.lbl_hu));
    labelArticle = SharedPrefManager.getString(ParamConstants.LABEL_ARTICLE, getString(R.string.lbl_article_no));
    labelSku = SharedPrefManager.getString(ParamConstants.LABEL_SKUID, getString(R.string.lbl_ean));

    isInwSerialMandatory = SharedPrefManager.getBoolean(ParamConstants.IS_SERIAL_NUMBER_MANDATORY,false);
    
    hasArticleData = !tripStatus.isManualTrip() && tripStatus.isArticleBasedTrip() && huDetailsDao.hasArticleData(typeIO, displayTripNo, huNumber);
    hasEpcData = !tripStatus.isManualTrip() && huDetailsDao.hasEpcData(typeIO, displayTripNo, huNumber);
    if(hasEpcData){
      ArrayList<String> listEpcs = new ArrayList<>(0);
      for(String epc : huDetailsDao.getEpcData(typeIO, displayTripNo, huNumber)){
        if(isNonEmpty(epc)){
          if(epc.trim().contains(",")) listEpcs.addAll(Arrays.asList(epc.trim().split(",")));
          else listEpcs.add(epc.trim());
        }
      }
      if(isNonEmpty(listEpcs))
        SharedPrefManager.setStringArrayList(displayTripNo + huNumber + ParamConstants.RFIDS, listEpcs);
    }
    
    lowerTolerance = SharedPrefManager.getInt(huNumber + displayTripNo + ParamConstants.LOWER_TOLERANCE, SharedPrefManager.getInt(displayTripNo + ParamConstants.LOWER_TOLERANCE, SharedPrefManager.getInt(ParamConstants.LOWER_TOLERANCE)));//SharedPrefManager.getInt(tripNumber+ParamConstants.LOWER_TOLERANCE,SharedPrefManager.getInt(ParamConstants.LOWER_TOLERANCE,0)));//tripStatus != null ? tripStatus.getLowerTolerance() : extractInt(args, ParamConstants.K_LOWER_TOLERANCE, 0);
    upperTolerance = SharedPrefManager.getInt(huNumber + displayTripNo + ParamConstants.UPPER_TOLERANCE, SharedPrefManager.getInt(displayTripNo + ParamConstants.UPPER_TOLERANCE, SharedPrefManager.getInt(ParamConstants.UPPER_TOLERANCE, 0)));//tripStatus != null ? tripStatus.getUpperTolerance() : extractInt(args, ParamConstants.K_UPPER_TOLERANCE, 0);
    allowMixTags = SharedPrefManager.getBoolean(huNumber + displayTripNo + ParamConstants.ALLOW_MIX_TAG_TYPE, SharedPrefManager.getBoolean(displayTripNo + ParamConstants.ALLOW_MIX_TAG_TYPE, SharedPrefManager.getBoolean(ParamConstants.ALLOW_MIX_TAG_TYPE, AppCommonMethods.isAllowMixTagType)));//tripStatus != null ? tripStatus.isMixTagTypeCompulsion() : extractBoolean(args, ParamConstants.K_MIX_TAG_COMPULSION, false);
    allowManualBarcodeScanning = SharedPrefManager.getBoolean(huNumber + displayTripNo + ParamConstants.ALLOW_BARCODE_SCANNING, SharedPrefManager.getBoolean(displayTripNo + ParamConstants.ALLOW_BARCODE_SCANNING, SharedPrefManager.getBoolean(ParamConstants.ALLOW_BARCODE_SCANNING, AppCommonMethods.isAllowBarcodeScanning)));//tripStatus != null ? tripStatus.isManualBarCodeCompulsion() : extractBoolean(args, ParamConstants.K_MANUAL_BARCODE_COMPULSION, false);
    
    showLog("lowerTolerance", "" + lowerTolerance);
    showLog("upperTolerance", "" + upperTolerance);
    showLog("allowMixTags", "" + allowMixTags);
    showLog("allowManualBarcodeScanning", "" + allowManualBarcodeScanning);
    
    if(allowManualBarcodeScanning && mainViewModel != null){
      mainViewModel.getBarcodeReaderInstance(getSessionType());
      setBarcodeObservers();
    }
    
    totalQty = huStatus.getExpQty();//tripInventoryDao.getCurrentHuQty(huNumber, tripNumber);
    if(totalQty <= 0){
      totalQty = huDetailsDao.getTotalExpQty(typeIO, displayTripNo, huNumber);
      huStatusDao.updateHUStatusExpQty(displayTripNo, huNumber, typeIO, totalQty);
      huStatus.setExpQty(totalQty);
    }
    //if(chkNull(totalQty, 0) <= 0) popBackStack();
  }
  
  public boolean hasArticleData(){
    return hasArticleData;
  }
  
  public boolean isManualTrip(){
    return tripStatus != null && tripStatus.isManualTrip();
  }
  
  /**
   * Set barcode observers.
   */
  void setBarcodeObservers(){
    mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
    if(allowManualBarcodeScanning || (dialogSerialScanBinding != null && dialogSerialScanBinding.edtSearch != null))
      mainViewModel.getIsBarcodeOn().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
        @Override
        public void onChanged(Boolean isBarcodeOn){
          if(!isTopInStack()) return;
          showLog("isBarcodeOn", AppCommonMethods.chkVal(isBarcodeOn));
          insertAuditTrailsLog("Barcode_" + (chkNotNullTrue(isBarcodeOn) ? "ON" : "OFF"));
          if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
          final boolean isProcessOn = isProcessOn();
          if(dialogSerialScanBinding != null && dialogSerialScanBinding.edtSearch != null)
            dialogSerialScanBinding.edtSearch.setIsProcessOn(chkNotNullTrue(isBarcodeOn));
          //binding.btnStartStopScan.setImageResource(chkNotNullTrue(isBarcodeOn) ? R.drawable.ic_start_disable : chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) ? R.drawable.ic_stop : R.drawable.ic_start);
          binding.textStartStop.setText(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) ? R.string.lbl_stop : R.string.lbl_start);
          showLog("isBarcodeOn", "" + chkNotNullTrue(isBarcodeOn));
          binding.swtHuScanDetails.setVisibility(!isProcessOn ? View.VISIBLE : View.GONE);
          binding.swtHuScanDetails.setEnabled(!isProcessOn);//? View.VISIBLE : View.GONE);
          binding.llSeekbarPower.setVisibility(!isProcessOn && binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
          binding.llManualScanBarcode.setVisibility(allowManualBarcodeScanning && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || getSize() < totalQty) ? View.VISIBLE : View.GONE);
          binding.btnStartStop.setVisibility(!chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? View.VISIBLE : View.GONE);
          binding.llUpload.setVisibility(!isProcessOn() && getSize() > 0 ? View.VISIBLE : View.GONE);
          binding.btnScanBarcode.setImageResource(isProcessOn ? R.drawable.ic_scan_disabled : R.drawable.ic_scan_red);
        }
      });
    
    mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
    if(allowManualBarcodeScanning || (dialogSerialScanBinding != null && dialogSerialScanBinding.edtSearch != null))
      mainViewModel.getBarcodeData().observe(getViewLifecycleOwner(), new Observer<String>(){
        @Override
        public void onChanged(String barcode){
          if(!isTopInStack()) return;
          showLog(HuProcessStartFragment.this.getClass().getSimpleName() + "_barcodeData", chkNull(barcode, ""));
          if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
          if(isNonEmpty(barcode)){
            /*if(pickedTag!=null && pickedTag.ean.trim().equalsIgnoreCase(barcode.trim())){
              context.showCustomErrDialog(String.format(getString(R.string.err_serial_invalid_ean_matching),barcode));
              return;
            }*/
            if(dialogSerialScanBinding != null && dialogSerialScanBinding.edtSearch != null && huScanDialog != null && huScanDialog.isShowing() && huScanDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null){
              dialogSerialScanBinding.edtSearch.setText(barcode);
              //Commented this line as user confirmation is required for scanned serial number
              //huScanDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            }
            else
              context.showCustomConfirmDialog(String.format(getString(R.string.msg_inw_confirm_barcode_action), barcode), R.string.btn_yes, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  final String ean = chkNull(barcode, "");
                  final TripInventoryDao tripInventoryDao = AppDatabase.getTripInventoryDao(context);
                  final String articleCode = chkNull(tripInventoryDao.getArticleCode(ean, huNumber, displayTripNo), AppConstants.EXTRA_EAN);
                  final Integer originalEanQty = !chkNull(articleCode, AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.EXTRA_EAN + ")") ? tripInventoryDao.getOriginalArticleQty(displayTripNo, huNumber, articleCode) : 0;
                  try{
                    final TripInventory inventory = new TripInventory(displayTripNo, deliveryNumber, huNumber);
                    inventory.userAction = "MANUAL";
                    inventory.ean = ean;
                    inventory.eanQty = originalEanQty;
                    inventory.epc = barcode;
                    inventory.isOriginal = originalEanQty > 0;
                    inventory.articleCode = articleCode;
                    tripInventoryDao.insertTripInventoryData(inventory);
                    updateLists();
                  }
                  catch(Exception e){
                    e.printStackTrace();
                  }
                }
              });
          }
          if(isNonEmpty(barcode)){
            mainViewModel.getBarcodeData().postValue("");
          }
        }
      });
  }
  
  @Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
    setBarcodeObservers();
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
      List<Boolean> list = tripInventoryDao.getIsHappyFlowEanCount(huNumber, displayTripNo, tripStatus.isArticleBasedTrip());
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
    if(!tripStatus.isManualTrip() && isExtraEan){
      message += "\t" + String.format(getResources().getString(R.string.err_p2_hu_unknown), "" + extraCount);
    }
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
    if(tripStatus.isManualTrip() && isNullOrEmpty(message)){
      isManualHappyFlow = true;
      binding.txtHuScanError.setTextColor(getResources().getColor(R.color.green));
      binding.txtHuScanError.setText(getString(R.string.success_qty_match));
      if(isAutoAcceptManualHUForHappyFlow || isAutoProcessManualHU)
        callUploadAPI(AppConstants.HU_STATUS_ACCEPT, "");
    }
    else binding.txtHuScanError.setText(message.trim().replaceAll("\t", ", "));
    
    if(((tripStatus.isManualTrip() && isAutoProcessManualHU) || (!tripStatus.isManualTrip() && isAutoProcessHU)) && isRejected){
      if(SharedPrefManager.getBoolean(ParamConstants.IS_API_CALL_FOR_REJECT_HU, AppCommonMethods.isAPIBasedRejectHU))
        callUploadAPI(AppConstants.HU_STATUS_REJECT, message.trim().replaceAll("\t", ", "));
      else{
        mainViewModel.stopSession(sessionObject, true);
        //tripInventoryDao.updateHUStatus(huNumber, "R", !isLocalHappyFlow, reason, tripNumber);//add reason in query
        discardData();
        if(SharedPrefManager.getBoolean(ParamConstants.IS_API_CALL_FOR_REJECT_HU, AppCommonMethods.isAPIBasedRejectHU))
          huStatusDao.updateRejectHUStatus(displayTripNo, huNumber, typeIO);
        context.clearIOConfig(displayTripNo, huNumber);
        popBackStack();
      }
    }
    
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
      List<TripInventory> newList = tripInventoryDao.getCurrentHuDetails(huNumber, displayTripNo, tripStatus.isArticleBasedTrip());
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
  
  public void scanSerialNo(final TripInventory tripInventory){
    if(huScanDialog == null || !huScanDialog.isShowing()){
      if(!allowManualBarcodeScanning) mainViewModel.getBarcodeReaderInstance(getSessionType());
      
      final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialog).create();
      setAlertDialogCustomTitle(alertDialog, R.string.msg_set_serial);
      
      DialogSerialScanBinding binding = DialogSerialScanBinding.inflate(LayoutInflater.from(context), null, false);
      //temp code may need to add/set isMandatory field
      binding.edtSearch.setValidationRegex(isInwSerialMandatory ? "[0-9A-Za-z\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\-]{1,50}" : "[0-9A-Za-z\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\-]{0,50}");
      binding.edtSearch.setText(chkNull(tripInventory.serialNo,""));
      alertDialog.setView(binding.getRoot());
      alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.btn_submit), (DialogInterface.OnClickListener) null);
      alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.btn_cancel), (DialogInterface.OnClickListener) null);
      binding.edtSearch.setImgScanOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          if(isNullOrEmpty(binding.edtSearch.getText()) && mainViewModel != null && !chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())){
            context.dismissCustomAlertDialog();
            showLog("Fragment softScan", "softScan");
            mainViewModel.softScan(binding.edtSearch.getLabel());
          }
        }
      });
      
      binding.edtSearch.setGoBtn(alertDialog.getButton(AlertDialog.BUTTON_POSITIVE));
      
      alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){
        @Override
        public void onShow(DialogInterface dialogInterface){
          huScanDialog = alertDialog;
          dialogSerialScanBinding = binding;
          //setTriggerDataObserver();
          setBarcodeObservers();
          LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(dp2px(85), dp2px(35));
          int margin = getResources().getDimensionPixelSize(R.dimen.dp_5);
          llParams.setMargins(margin, 0, margin, 0);
          llParams.gravity = Gravity.CENTER_HORIZONTAL;
          final Button pos = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
          final Button neg = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
          ((LinearLayout) pos.getParent()).setGravity(Gravity.CENTER);
          pos.setLayoutParams(llParams);
          ((LinearLayout) neg.getParent()).setGravity(Gravity.CENTER);
          neg.setLayoutParams(llParams);
          pos.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
              mainViewModel.getIsBarcodeOn().postValue(false);
              if(binding.edtSearch.validate() || !isInwSerialMandatory){
                if(pickedTag!=null && pickedTag.ean.trim().equalsIgnoreCase(binding.edtSearch.getText().trim())){
                  context.showCustomErrDialog(String.format(getString(R.string.err_serial_invalid_ean_matching),binding.edtSearch.getText().trim()));
                  return;
                }
                binding.edtSearch.setIsViewControlEnabled(false, true);
                final String serialNo = binding.edtSearch.getText().trim();
                if(!isInwSerialMandatory && isNullOrEmpty(serialNo)){
                  //save locally
                  if(!tripInventoryDao.isEpcPresent(displayTripNo, huNumber, tripInventory.epc))
                    tripInventoryDao.insertTripInventoryData(tripInventory);
                  alertDialog.dismiss();
                  return;
                }
                //For internal validation
                if(tripInventoryDao.isSerialPresent(displayTripNo, huNumber, serialNo)){
                  context.showCustomErrDialog("Duplicate Serial No.");
                  // alertDialog.dismiss();
                  return;
                }
                
                //call Verify Serial API
                try{
                  //tripInventory.serialNo = serialNo;
                  Bundle args = chkNull(getArguments(), new Bundle());
                  args.putSerializable(tripInventory.getClass().getSimpleName(), tripInventory);
                  JSONObject requestParams = new JSONObject();
                  requestParams.put(ParamConstants.EAN, tripInventory.ean);
                  requestParams.put(ParamConstants.ARTICLE_NO, tripInventory.articleCode);
                  requestParams.put(ParamConstants.EPC, tripInventory.epc);
                  requestParams.put(ParamConstants.TID, tripInventory.tid);
                  requestParams.put(ParamConstants.SERIAL_NUMBER, serialNo.toUpperCase());
                  requestParams.put(ParamConstants.IS_UPDATE_SERIAL_NUMBER, false);
                  callWebService(URLConstants.VERIFY_PRODUCT_SERIAL, requestParams, args, getString(R.string.progress_msg_validate_data));
                }
                catch(Exception e){
                  e.printStackTrace();
                }
              }
            }
          });
        }
      });
      alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
        @Override
        public void onDismiss(DialogInterface dialogInterface){
          dialogSerialScanBinding = null;
          huScanDialog = null;
          if(mainViewModel != null) mainViewModel.onPause();
          setBarcodeObservers();
          //setTriggerDataObserver();
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
  protected void isPickOnChanged(Boolean isPickOn){
    super.isPickOnChanged(isPickOn);
    if(isPickOn == null){
    }
    else{
      // updateViews();
      if(!isPickOn) stopTimer();
      binding.btnStartStop.toggle(isPickOn);
    }
  }
  
  @Override
  protected void onPickDataChanged(Inventory pickData){
    super.onPickDataChanged(pickData);
    if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
    if(isInwWithSerial && pickData != null){
      showLog("EAN", pickData.ean);
      showLog("EPC", pickData.epc);
      showLog("TID", pickData.tid);
      showLog("PC", pickData.pcdata);
      try{
        // When we pick the tag, it will open the dialog box for inserting serial no
        pickedTag = pickData;
        showLog("Pick_SCAN", "End");
        AppCommonMethods.logInFile(context, getSessionType().name(), "_PickData_Observed (" + pickedTag.toString() + ")");
        showLog("Pick_Done", "" + pickedTag);
        //show Error/Confirmation if tag other then ean is found
        
        //generate & set TripInventory object
        final TripInventory tripInventory = getTripInventoryObject(pickData);
        if(tripInventory != null) scanSerialNo(tripInventory);
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  
  TripInventory getTripInventoryObject(final Inventory inventory){
    try{
      final boolean hasArticleData = (tripStatus == null || tripStatus.isArticleBasedTrip()) && tripInventoryDao.hasArticleData(displayTripNo, huNumber);
      //Apply condition based on rfid list
      ArrayList<String> epcs = SharedPrefManager.getStringArrayList(displayTripNo + huNumber + ParamConstants.RFIDS, new ArrayList<>(0));
      final boolean isEpcBasedChecking = isNonEmpty(epcs);
      
      String serial = "";
      if(isEpcBasedChecking && epcs.contains(inventory.epc)){
        // Get serial from 3rd table which has mapping with epc, ean & serial & set serial as default
        serial = chkNull(AppDatabase.getSerialDetailsDao(context).getSerialFromEpc(inventory.epc),"");
      }
      final String ean = /*isEpcBased && !epcs.contains(inventory.epc)?AppConstants.UNKNOWN:*/chkNull(inventory.ean, "").replaceFirst(AppConstants.UNKNOWN, AppConstants.NON_ENCODED);
      final String articleCode = !chkNull(ean, AppConstants.NON_ENCODED).equalsIgnoreCase(AppConstants.NON_ENCODED) ? ean.equalsIgnoreCase(AppConstants.UNKNOWN) ? AppConstants.UNKNOWN : chkNull(tripInventoryDao.getArticleCode(ean, huNumber, displayTripNo), AppConstants.EXTRA_EAN) : AppConstants.NON_ENCODED;
      final Integer originalEanQty = chkNull(ean, AppConstants.NON_ENCODED).equalsIgnoreCase(AppConstants.NON_ENCODED) ? 0 : isEpcBasedChecking && !epcs.contains(inventory.epc) ? 0 : hasArticleData && !chkNull(articleCode, AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.EXTRA_EAN + ")") ? chkNull(tripInventoryDao.getOriginalArticleQty(displayTripNo, huNumber, articleCode), 0) : !hasArticleData ? chkNull(tripInventoryDao.getOriginalEanQty(displayTripNo, huNumber, ean), 0) : 0;
      
      TripInventory tripInventory = new TripInventory(displayTripNo, deliveryNumber, huNumber);
      tripInventory.userAction = "RFID";
      tripInventory.ean = ean;
      tripInventory.eanQty = originalEanQty;
      tripInventory.tid = inventory.tid;
      tripInventory.epc = inventory.epc;
      tripInventory.rssi = inventory.rssi;
      tripInventory.isOriginal = originalEanQty > 0;
      tripInventory.articleCode = articleCode;
      tripInventory.isHardTag = inventory.isHardTag;
      tripInventory.serialNo = chkNull(serial,"").toUpperCase();
      if(!tripInventoryDao.isEpcPresent(displayTripNo, huNumber, inventory.epc))
        if(originalEanQty <= 0){
          //context.showCustomErrDialog("Unknown tag found");
          context.showCustomConfirmDialog(R.string.msg_hu_alert_extraEan, R.string.btn_inward_alert_accept, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              scanSerialNo(tripInventory);
            }
          });
          return null;
        }
      return tripInventory;
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return null;
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
              if(SharedPrefManager.getBoolean(ParamConstants.IS_HU_DEVICE_LOCK)) callReleaseHU();
              else{
                mainViewModel.stopSession(sessionObject, true);
                discardData();
                huStatusDao.updatePendingHUStatus(displayTripNo, huNumber, typeIO);
                context.clearIOConfig(displayTripNo, huNumber);
                context.popBackStack();
              }
            }, getString(R.string.btn_no), null);
          }
          else{
            if(SharedPrefManager.getBoolean(ParamConstants.IS_HU_DEVICE_LOCK)){
              callReleaseHU();
            }
            else{
              mainViewModel.stopSession(sessionObject, true);
              discardData();
              huStatusDao.updatePendingHUStatus(displayTripNo, huNumber, typeIO);
              context.clearIOConfig(displayTripNo, huNumber);
              context.popBackStack();
            }
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
                  if(SharedPrefManager.getBoolean(ParamConstants.IS_API_CALL_FOR_REJECT_HU, AppCommonMethods.isAPIBasedRejectHU))
                    huStatusDao.updateRejectHUStatus(displayTripNo, huNumber, typeIO, getSize());
                  context.clearIOConfig(displayTripNo, huNumber);
                  popBackStack();
                }
              });
            }
            else{
              mainViewModel.stopSession(sessionObject, true);
              //tripInventoryDao.updateHUStatus(huNumber, "R", !isLocalHappyFlow, reason, tripNumber);//add reason in query
              discardData();
              if(SharedPrefManager.getBoolean(ParamConstants.IS_API_CALL_FOR_REJECT_HU, AppCommonMethods.isAPIBasedRejectHU))
                huStatusDao.updateRejectHUStatus(displayTripNo, huNumber, typeIO);
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
      request.put(ParamConstants.OPERATION_TYPE, typeIO);
      request.put(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
      request.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
      request.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus != null && tripStatus.isManualTrip());
      request.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus != null ? tripStatus.excelTripType : "");
      request.put(ParamConstants.REMARK, reason);
      request.put(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
      request.put(ParamConstants.K_TRIP_HU_STATUS, action);
      request.put(ParamConstants.EXP_QTY, huStatus.getExpQty());
      List<TripInventory> list = tripInventoryDao.getAllScannedData(displayTripNo, huNumber);
      JSONArray jhuArray = new JSONArray();
      JSONArray jArrayRFIDItems = new JSONArray();
      JSONArray jArrayBarcodeItems = null;
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
          
          if(tripInventory.epc.length() > 0){
            String userAction = tripInventory.userAction;
            
            JSONObject item = tripInventory.toJson(context, isInwWithSerial);
            JSONObject barcodeItem = tripInventory.toBarcodeJson();
            
            if(userAction.equalsIgnoreCase("RFID")){
              jArrayRFIDItems.put(item);
            }
            else{
              jArrayBarcodeItems.put(barcodeItem);
            }
          }
        }
        request.put(ParamConstants.DATA, jArrayRFIDItems);
      }
      callWebService(action.equalsIgnoreCase(AppConstants.HU_STATUS_ACCEPT) ? URLConstants.ACCEPT_HU : URLConstants.REJECT_HU, request, getString(R.string.progress_msg_uploading_data));
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
  
  private void callReleaseHU(){
    if(huStatus == null) return;
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
      jsonRequest.put(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
      jsonRequest.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
      jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus != null && tripStatus.isManualTrip());
      jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus != null ? tripStatus.excelTripType : "");
      jsonRequest.put(ParamConstants.K_TRIP_HU_NUMBER, huStatus != null ? huStatus.getHuNumber() : huNumber);
      Bundle arg = chkNull(getArguments(), new Bundle());
      arg.putString(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
      arg.putString(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
      arg.putString(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
      if(tripStatus != null) arg.putSerializable(tripStatus.getClass().getSimpleName(), tripStatus);
      if(huStatus != null) arg.putSerializable(huStatus.getClass().getSimpleName(), huStatus);
      callWebService(URLConstants.RELEASE_HU, jsonRequest, arg, getString(R.string.progress_msg_uploading_data));
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack() && mainViewModel != null && (allowManualBarcodeScanning || (dialogSerialScanBinding != null && huScanDialog != null && huScanDialog.isShowing()))){
      mainViewModel.getBarcodeReaderInstance(getSessionType());
      setBarcodeObservers();
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
      sessionObject.userId = SharedPrefManager.getUserID();
      Calendar cc = Calendar.getInstance();
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, 24);
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc);
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, false);
      
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
          jobj.put(ParamConstants.K_TRIP_TYPE, tripType);
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
                
                JSONObject item = tripInventory.toJson(context, isInwWithSerial);
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
                huStatusDao.updateCompleteHUStatus(displayTripNo, huNumber, typeIO, getSize());
                tripStatusDao.updateTripHUCompletedCount(displayTripNo, typeIO);
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
                if(SharedPrefManager.getBoolean(ParamConstants.IS_API_CALL_FOR_REJECT_HU, AppCommonMethods.isAPIBasedRejectHU))
                  huStatusDao.updateRejectHUStatus(displayTripNo, huNumber, typeIO, getSize());
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
            huStatusDao.updatePendingHUStatus(displayTripNo, huNumber, typeIO);
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
  
  public boolean isInwardWithSerial(){
    return isInwWithSerial;
  }
}