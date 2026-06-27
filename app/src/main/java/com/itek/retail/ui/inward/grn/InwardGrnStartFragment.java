package com.itek.retail.ui.inward.grn;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isSetInwOnline;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.ProdDisplayDataListAdapter;
import com.itek.retail.adapter.StoreInwardScanAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.databinding.DialogInwardAlertOptionsBinding;
import com.itek.retail.databinding.FragmentInwardGrnStartBinding;
import com.itek.retail.model.LabelValues;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.TripInventory;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.customviews.MaxHeightRecyclerView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The Inward grn start fragment.
 */
public class InwardGrnStartFragment extends RFIDSessionFragment{
  
  List<TripInventory> dataList = new ArrayList<>(0);
  String tripNumber;
  String tripType;
  String huNumber;
  String deliveryNumber;
  private int lowerTolerance;
  private int upperTolerance;
  private boolean allowMixTags;
  private boolean allowManualBarcodeScanning;
  private Integer totalQty = 0;
  private TripInventoryDao tripInventoryDao;
  private InwardGrnStartViewModel mViewModel;
  private FragmentInwardGrnStartBinding binding;
  
  /**
   * Instantiates a new Inward grn start fragment.
   */
  public InwardGrnStartFragment(){/*Default/Empty Constructor*/}
  
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
    binding = FragmentInwardGrnStartBinding.inflate(inflater, container, false);
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
    
    binding.txtTripNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_trip), tripNumber), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtHUNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_hu), huNumber), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtHUNumberScan.setSelected(true);
    binding.txtTripNumberScan.setSelected(true);
    
    binding.txtHuTotalQty.setText("" + totalQty);
    
    binding.llManualScanBarcode.setVisibility(allowManualBarcodeScanning && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || getSize() < totalQty) ? View.VISIBLE : View.GONE);
    binding.llBtnStartStop.setVisibility(!chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? View.VISIBLE : View.GONE);
    binding.llUpload.setVisibility(!isProcessOn() && getSize() > 0 ? View.VISIBLE : View.GONE);
    
    binding.listTripHuScan.setAdapter(new StoreInwardScanAdapter((MainActivity) context, InwardGrnStartFragment.this, dataList));
    binding.listTripHuScan.setLayoutManager(new LinearLayoutManager(context));
    
    binding.btnUpload.setImageResource(isSetInwOnline ? R.drawable.ic_upload : R.drawable.ic_save);
    binding.lblUpload.setText(getString(isSetInwOnline ? R.string.btn_upload : R.string.btn_save));
    
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
    
    binding.llBtnStartStop.setOnClickListener(v -> {
      
      if(context.customAlertDialog != null && context.customAlertDialog.isShowing()) return;
      if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
      if(chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())) return;
      toggleInventory();
    });
    
    binding.llUpload.setOnClickListener(v -> {
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
    });
    
    setSessionAction(AppConstants.SESSION_ACTION_START);
    return binding.getRoot();
  }
  
  /**
   * Get bundle data.
   *
   * @param args the args
   */
  private void getBundleData(Bundle args){
    final Object obj = extractSerializable(args, TripStatus.class);
    TripStatus tripStatus = obj != null && obj instanceof TripStatus ? (TripStatus) obj : null;
    tripNumber = tripStatus != null ? tripStatus.getTripNumber() : extractString(args, AppConstants.TRIP_NUMBER, "");
    tripType = tripStatus != null ? tripStatus.getTripType() : extractString(args, AppConstants.TRIP_TYPE, "IN");
    deliveryNumber = extractString(args, AppConstants.DELIVERY_NUMBER, "");
    huNumber = extractString(args, AppConstants.HU_NUMBER, "");
    if(isNonEmpty(tripNumber) && isNonEmpty(huNumber)){
      SharedPrefManager.setTripNo(tripNumber);
      SharedPrefManager.setHuNo(huNumber);
      if(tripStatus == null)
        tripStatus = AppDatabase.getTripStatusDao(context).getTripData(tripNumber, AppConstants.INWARD);
    }
    else popBackStack();
    lowerTolerance = tripStatus != null ? tripStatus.getLowerTolerance() : extractInt(args, ParamConstants.K_LOWER_TOLERANCE, 0);
    upperTolerance = tripStatus != null ? tripStatus.getUpperTolerance() : extractInt(args, ParamConstants.K_UPPER_TOLERANCE, 0);
    allowMixTags = tripStatus != null ? tripStatus.isMixTagTypeCompulsion() : extractBoolean(args, ParamConstants.K_MIX_TAG_COMPULSION, false);
    allowManualBarcodeScanning = tripStatus != null ? tripStatus.isManualBarCodeCompulsion() : extractBoolean(args, ParamConstants.K_MANUAL_BARCODE_COMPULSION, false);
    showLog("lowerTolerance", "" + lowerTolerance);
    showLog("upperTolerance", "" + upperTolerance);
    showLog("allowMixTags", "" + allowMixTags);
    showLog("allowManualBarcodeScanning", "" + allowManualBarcodeScanning);
    
    if(allowManualBarcodeScanning && mainViewModel != null){
      mainViewModel.getBarcodeReaderInstance(getSessionType());
      setBarcodeObservers();
    }
    
    totalQty = tripInventoryDao.getCurrentHuQty(huNumber, tripNumber);
    if(chkNull(totalQty, 0) <= 0) popBackStack();
  }
  
  /**
   * Set barcode observers.
   */
  void setBarcodeObservers(){
    mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getIsBarcodeOn().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isBarcodeOn){
        if(!isTopInStack()) return;
        showLog("isBarcodeOn", "" + AppCommonMethods.chkVal(isBarcodeOn));
        insertAuditTrailsLog("Barcode_" + (chkNotNullTrue(isBarcodeOn) ? "ON" : "OFF"));
        if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        final boolean isProcessOn = isProcessOn();
        binding.btnStartStopScan.setImageResource(chkNotNullTrue(isBarcodeOn) ? R.drawable.ic_start_disable : chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) ? R.drawable.ic_stop : R.drawable.ic_start);
        binding.textStartStop.setText(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) ? R.string.lbl_stop : R.string.lbl_start);
        showLog("isBarcodeOn", "" + chkNotNullTrue(isBarcodeOn));
        binding.llSeekbarPower.setVisibility(!isProcessOn && binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        binding.llManualScanBarcode.setVisibility(allowManualBarcodeScanning && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || getSize() < totalQty) ? View.VISIBLE : View.GONE);
        binding.llBtnStartStop.setVisibility(!chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.llUpload.setVisibility(!isProcessOn() && getSize() > 0 ? View.VISIBLE : View.GONE);
        binding.btnScanBarcode.setImageResource(isProcessOn ? R.drawable.ic_scan_disabled : R.drawable.ic_scan_red);
      }
    });
    
    mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
    mainViewModel.getBarcodeData().observe(getViewLifecycleOwner(), new Observer<String>(){
      @Override
      public void onChanged(String barcode){
        if(!isTopInStack()) return;
        showLog(InwardGrnStartFragment.this.getClass().getSimpleName() + "_barcodeData", "" + chkNull(barcode, ""));
        if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if(isNonEmpty(barcode)){
          context.showCustomConfirmDialog(String.format(getString(R.string.msg_inw_confirm_barcode_action), barcode), R.string.btn_yes, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              final String ean = chkNull(barcode, "");
              final TripInventoryDao tripInventoryDao = AppDatabase.getTripInventoryDao(context);
              final String articleCode = chkNull(tripInventoryDao.getArticleCode(ean, huNumber, tripNumber), AppConstants.EXTRA_EAN);
              final Integer originalEanQty = !chkNull(articleCode, AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.EXTRA_EAN + ")") ? tripInventoryDao.getOriginalArticleQty(tripNumber, huNumber, articleCode) : 0;
              try{
                final TripInventory inventory = new TripInventory(tripNumber, deliveryNumber, huNumber);
                inventory.userAction = "MANUAL";
                inventory.ean = ean;
                inventory.eanQty = originalEanQty;
                inventory.epc = barcode;
                inventory.isOriginal = originalEanQty > 0;
                inventory.articleCode = articleCode;
                tripInventoryDao.insertTripInventoryData(inventory);
                updateLists();
              }
              catch(Exception e){ e.printStackTrace(); }
            }
          });
        }
        if(isNonEmpty(barcode)){ mainViewModel.getBarcodeData().postValue(""); }
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
  
  /**
   * Call alert.
   */
  public void callAlert(){
    final int expectedScanned = tripInventoryDao.getArticlesOriginalEanCount(huNumber, tripNumber);
    final int extracount = tripInventoryDao.getExtraEanCount(huNumber, tripNumber);
    final int nonencoded = tripInventoryDao.getNonEncodedCount(huNumber, tripNumber);
    final int unknown = tripInventoryDao.getExtraEanCount(huNumber, tripNumber);
    showLog("EXPECTEDQTYSCANNED", "" + expectedScanned);
    showLog("extraEanCount", "" + extracount);
    showLog("nonencodedcount", "" + nonencoded);
    
    boolean isHappy = tripInventoryDao.getIsHappyFlow(huNumber, tripNumber);
    if(isHappy){
      List<Boolean> list = tripInventoryDao.getIsHappyFlowEanCount(huNumber, tripNumber);
      if(list != null && !list.isEmpty() && list.size() > 0){
        for(Boolean b : list)
          if(!b){
            isHappy = false;
            break;
          }
      }
    }
    
    final boolean isHappyFlow = isHappy;
    final List<String> dbAllHUsDublicateRfid = tripInventoryDao.getAllHusDuplicateRfid(tripNumber);
    
    final boolean isQuantityMatch = isHappyFlow || tripInventoryDao.getIsEanQtyMatched(huNumber, tripNumber);
    final boolean isLessAcceptableCount = !isQuantityMatch && tripInventoryDao.getIsEanQtyLowerPercentMatched(huNumber, lowerTolerance, tripNumber);
    final boolean isLessARejectedCount = !isQuantityMatch && !isLessAcceptableCount && tripInventoryDao.getIseanqtyLowerPercent(huNumber, tripNumber);
    final boolean isGraterAcceptableCount = !isQuantityMatch && tripInventoryDao.getIseanqtyUpperPercentMatched(huNumber, upperTolerance, tripNumber);
    final boolean isGraterRejectedCount = !isQuantityMatch && !isGraterAcceptableCount && tripInventoryDao.getIseanqtyUpperPercentExceed(huNumber, tripNumber);
    final boolean isTagTypeMisMatch = !allowMixTags && tripInventoryDao.isMixedTagsPresent(huNumber, tripNumber);
    final boolean isNonEncoded = tripInventoryDao.getIsNonEncoded(huNumber, tripNumber);
    final boolean isUnknown = tripInventoryDao.getIsEanExtra(huNumber, tripNumber);//tripInventoryDao.getIsUnknown(huNumber, tripNumber);
    final boolean isExtraEan = tripInventoryDao.getIsEanExtra(huNumber, tripNumber);
    final Boolean isDuplicate = isNonEmpty(dbAllHUsDublicateRfid) && dbAllHUsDublicateRfid.contains(huNumber);
    final boolean isRejected = (!isHappyFlow || isDuplicate || isTagTypeMisMatch || isNonEncoded || isUnknown || !(isQuantityMatch || isGraterAcceptableCount || isLessAcceptableCount));
    boolean isLessThenAcceptMisMatched = false;
    final AlertDialog builder = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    DialogInwardAlertOptionsBinding alertbinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_inward_alert_options, null, false);
    if(isDuplicate){
      //duplicate condition delete and restart
      String message = "";
      alertbinding.imgLogo.setImageResource(R.drawable.ic_error);
      alertbinding.txtAlertTitle.setText(R.string.err_hu_alert_duplicate_tag_found);
      alertbinding.txtAlertSubtitle.setVisibility(View.VISIBLE);
      message = getResources().getString(R.string.err_hu_alert_duplicate_tag_found);
      String hus = dbAllHUsDublicateRfid.toString().replace("[", "").replace("]", "");
      message = message + "\n" + hus;
      final String reason = message;
      alertbinding.txtAlertSubtitle.setText(message);
      alertbinding.rescan.setVisibility(View.VISIBLE);
      alertbinding.reject.setVisibility(View.VISIBLE);
      alertbinding.llDialogQty.setVisibility(View.GONE);
    }
    else if(isHappyFlow){
      alertbinding.imgLogo.setImageResource(R.drawable.ic_success);
      alertbinding.txtAlertTitle.setText(R.string.success_hu_alert_qty_match);
      alertbinding.accept.setVisibility(View.VISIBLE);
      alertbinding.llDialogQty.setVisibility(View.GONE);
    }
    else if(isNonEncoded){
      String message = "";
      alertbinding.imgLogo.setImageResource(R.drawable.ic_error);
      alertbinding.txtAlertTitle.setText(R.string.err_hu_alert_qty_non_encoded_msg);
      alertbinding.txtAlertSubtitle.setVisibility(View.GONE);
      message = getResources().getString(R.string.err_hu_alert_qty_non_encoded_msg);
      message = "" + nonencoded + " " + message;
      alertbinding.txtAlertSubtitle.setText(message);
      alertbinding.reject.setVisibility(View.VISIBLE);
      alertbinding.rescan.setVisibility(View.VISIBLE);
      
      alertbinding.llDialogQty.setVisibility(View.VISIBLE);
      alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
      alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
      alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
      alertbinding.llUnencoded.setVisibility(View.VISIBLE);
      alertbinding.textUnencodedScannedQty.setText("" + nonencoded);
      
      if(extracount > 0){
        alertbinding.llExtra.setVisibility(View.VISIBLE);
        alertbinding.textExtraScannedQty.setText("" + extracount);
      }
    }
    else if(isUnknown){
      String message = "";
      alertbinding.imgLogo.setImageResource(R.drawable.ic_error);
      alertbinding.txtAlertTitle.setText(R.string.msg_hu_alert_extraEan);
      alertbinding.txtAlertSubtitle.setVisibility(View.GONE);
      message = getResources().getString(R.string.msg_hu_alert_extraEan);
      message = "" + extracount + " " + message;
      alertbinding.txtAlertSubtitle.setText(message);
      alertbinding.reject.setVisibility(View.VISIBLE);
      alertbinding.rescan.setVisibility(View.VISIBLE);
      
      alertbinding.llDialogQty.setVisibility(View.VISIBLE);
      alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
      alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
      alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
      alertbinding.llUnknown.setVisibility(View.VISIBLE);
      alertbinding.textUnknownScannedQty.setText("" + unknown);
      
      if(extracount > 0){
        alertbinding.llExtra.setVisibility(View.VISIBLE);
        alertbinding.textExtraScannedQty.setText("" + extracount);
      }
    }
    else if(isQuantityMatch){
      String message = "";
      alertbinding.imgLogo.setImageResource(R.drawable.ic_error);
      alertbinding.txtAlertTitle.setText(R.string.err_hu_alert_qty_match);
      alertbinding.txtAlertSubtitle.setVisibility(View.GONE);
      alertbinding.reject.setVisibility(View.VISIBLE);
      alertbinding.rescan.setVisibility(View.VISIBLE);
      alertbinding.accept.setVisibility(View.VISIBLE);
      
      if(isExtraEan){
        message = message + getResources().getString(R.string.msg_hu_alert_extraEan);
        alertbinding.llDialogQty.setVisibility(View.VISIBLE);
        alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
        alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
        alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
        if(extracount > 0){
          alertbinding.llExtra.setVisibility(View.VISIBLE);
          alertbinding.textExtraScannedQty.setText("" + extracount);
        }
      }
      alertbinding.txtAlertSubtitle.setText(message);
      
      /*if(isDebugApp){
        context.showCustomAlertDialog(getString(R.string.txt_hualert_qty_match), reason, listDisplayData, false, false, getString(R.string.txt_inward_alert_accept), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            showCustomConfirmationDialog(reason, getString(R.string.txt_inward_alert_accept), isHappyFlow);
          }
        }, getString(R.string.txt_inward_alert_reject), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            showCustomConfirmationDialog(reason, getString(R.string.txt_inward_alert_reject), isHappyFlow);
          }
        }, getString(R.string.txt_inward_alert_rescan), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            showCustomConfirmationDialog(reason, getString(R.string.txt_inward_alert_rescan), isHappyFlow);
          }
        });
        return;
      }*/
    }
    else if(isLessAcceptableCount){ //(Continue case)
      showLog(">=90%", "True");
      String message = "";
      alertbinding.imgLogo.setImageResource(R.drawable.ic_error);
      alertbinding.txtAlertTitle.setText(R.string.err_hu_alert_qty_less_match_limit);
      alertbinding.txtAlertSubtitle.setVisibility(View.GONE);
      
      alertbinding.reject.setVisibility(View.VISIBLE);
      alertbinding.rescan.setVisibility(View.VISIBLE);
      
      alertbinding.accept.setVisibility(View.VISIBLE);
      
      if(isExtraEan){
        message = message + getResources().getString(R.string.msg_hu_alert_extraEan);
        
        alertbinding.llDialogQty.setVisibility(View.VISIBLE);
        alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
        alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
        alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
        if(extracount > 0){
          alertbinding.llExtra.setVisibility(View.VISIBLE);
          alertbinding.textExtraScannedQty.setText("" + extracount);
        }
      }
      else{
        //check ean wise qty
        
        Boolean isMissmatched = false;
        isLessThenAcceptMisMatched = false;
        List<Boolean> listeanmismached = tripInventoryDao.getMismatchingEancount(huNumber, tripNumber);
        if(listeanmismached != null && listeanmismached.size() > 0){
          for(Boolean b : listeanmismached){
            if(b){
              isMissmatched = true;
              isLessThenAcceptMisMatched = true;
              break;
            }
          }
        }
        //true from db
        if(isMissmatched){
          message = message + getResources().getString(R.string.msg_hu_alert_reject_extraEan);
          
          alertbinding.llDialogQty.setVisibility(View.VISIBLE);
          alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
          alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
          alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
        }
        else{
          message = message + getResources().getString(R.string.msg_hu_alert_qty_less);
        }
      }
      alertbinding.txtAlertSubtitle.setText(message);
    }
    else if(isGraterAcceptableCount){
      showLog(">120%", "True");
      String message = "";
      alertbinding.imgLogo.setImageResource(R.drawable.ic_error);
      alertbinding.txtAlertTitle.setText(R.string.err_hu_alert_qty_excess_match_limit);
      alertbinding.txtAlertSubtitle.setVisibility(View.GONE);
      if(isExtraEan){
        alertbinding.llDialogQty.setVisibility(View.VISIBLE);
        alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
        alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
        alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
        if(extracount > 0){
          alertbinding.llExtra.setVisibility(View.VISIBLE);
          alertbinding.textExtraScannedQty.setText("" + extracount);
        }
      }
      else{
        alertbinding.llDialogQty.setVisibility(View.VISIBLE);
        alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
        alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
        alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
      }
      if(extracount > expectedScanned){
        message = message + getResources().getString(R.string.txt_hualert_excess_qty_of_unknown_qrticle);
      }
      else if(expectedScanned > totalQty){
        message = message + getResources().getString(R.string.txt_hualert_excess_qty_of_expected_article);
      }
      else{
        if(extracount == 0){
          message = message + getResources().getString(R.string.msg_hu_alert_ean_qty_mismatch);
        }
        else{
          message = message + getResources().getString(R.string.msg_hu_alert_extraEan);
        }
      }
      alertbinding.txtAlertSubtitle.setText(message);
      alertbinding.reject.setVisibility(View.VISIBLE);
      alertbinding.rescan.setVisibility(View.VISIBLE);
      alertbinding.accept.setVisibility(View.VISIBLE);
    }
    else if(isRejected){//rescan case
      showLog("Rejected", "True");
      String message = "";
      alertbinding.imgLogo.setImageResource(R.drawable.ic_error);
      alertbinding.txtAlertTitle.setText(R.string.err_hu_alert_qty_not_match);
      alertbinding.txtAlertSubtitle.setVisibility(View.GONE);
      alertbinding.rescan.setVisibility(View.VISIBLE);
      alertbinding.reject.setVisibility(View.VISIBLE);
      
      if(isGraterRejectedCount){
        alertbinding.txtAlertTitle.setText(R.string.err_hu_alert_qty_excess_mismatch);
        
        alertbinding.llDialogQty.setVisibility(View.VISIBLE);
        alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
        alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
        alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
        if(extracount > 0){
          alertbinding.llExtra.setVisibility(View.VISIBLE);
          alertbinding.textExtraScannedQty.setText("" + extracount);
        }
        if(extracount > expectedScanned){
          message = message + getResources().getString(R.string.txt_hualert_excess_qty_of_unknown_qrticle);
        }
        else if(expectedScanned > totalQty){
          message = message + getResources().getString(R.string.txt_hualert_excess_qty_of_expected_article);
        }
        else{
          if(extracount == 0){
            message = message + getResources().getString(R.string.msg_hu_alert_ean_qty_mismatch);
          }
          else{
            message = message + getResources().getString(R.string.msg_hu_alert_extraEan);
          }
        }
      }
      else if(isLessARejectedCount){
        alertbinding.txtAlertTitle.setText(R.string.err_hu_alert_qty_less_mismatch);
        
        message = message + getResources().getString(R.string.hu_qty_below_lower_threshold);
        if(isExtraEan){
          message = message + " and " + getResources().getString(R.string.msg_hu_alert_extraEan);
          alertbinding.llDialogQty.setVisibility(View.VISIBLE);
          alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
          alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
          alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
          if(extracount > 0){
            alertbinding.llExtra.setVisibility(View.VISIBLE);
            alertbinding.textExtraScannedQty.setText("" + extracount);
          }
        }
        
        alertbinding.llDialogQty.setVisibility(View.VISIBLE);
        alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
        alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
        alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
      }
      else if(isExtraEan){
        message = message + getResources().getString(R.string.msg_hu_alert_extraEan);
        
        alertbinding.llDialogQty.setVisibility(View.VISIBLE);
        alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
        alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
        alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
        if(extracount > 0){
          alertbinding.llExtra.setVisibility(View.VISIBLE);
          alertbinding.textExtraScannedQty.setText("" + extracount);
        }
      }
      else if(isQuantityMatch){
        alertbinding.accept.setVisibility(View.VISIBLE);
        alertbinding.txtAlertTitle.setText(R.string.err_hu_alert_qty_match);
        message = message + getResources().getString(R.string.msg_hu_alert_ean_qty_mismatch);
        
        alertbinding.llDialogQty.setVisibility(View.VISIBLE);
        alertbinding.textTotalQty.setText(binding.txtHuTotalQty.getText());
        alertbinding.textTotalScannedQty.setText(binding.txtHuScanQty.getText());
        alertbinding.textExpectedScannedQty.setText("" + expectedScanned);
      }
      alertbinding.txtAlertSubtitle.setText(message);
    }
    else{
      showLog("Else", "True");
    }
    
    if(true){
      final String title = alertbinding.txtAlertTitle.getText().toString();
      final String reason = isHappyFlow ? "" : alertbinding.txtAlertSubtitle.getText().toString();
      final boolean hasAcceptButton = !isDuplicate && !isNonEncoded && !isUnknown && (isHappyFlow || isQuantityMatch || isLessAcceptableCount || isGraterAcceptableCount);
      if(hasAcceptButton){
        context.showCustomAlertDialog(title, isDuplicate ? reason : "", isDuplicate || isHappyFlow || (!isNonEncoded && !isUnknown && ((isQuantityMatch && extracount <= 0) || (isLessAcceptableCount && extracount <= 0 && !isLessThenAcceptMisMatched))) ? null : setAlertDisplayDataList(expectedScanned, nonencoded, unknown, extracount), isHappyFlow, false, hasAcceptButton ? getString(R.string.btn_inward_alert_accept) : "", new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            
            context.showCustomConfirmDialog(getString(R.string.msg_inw_confirm_accept), getString(R.string.btn_inward_alert_accept), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                mainViewModel.deleteSession(sessionObject);
                tripInventoryDao.updateHUStatus(huNumber, "A", !isHappyFlow, isHappyFlow ? "" : reason, tripNumber);
                updateLists();
                popBackStack();
              }
            });
          }
        }, !isHappyFlow ? getString(R.string.btn_inward_alert_reject) : "", new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            showCustomConfirmationDialog(reason, getString(R.string.btn_inward_alert_reject), isHappyFlow);
            context.showCustomConfirmDialog(getString(R.string.msg_inw_confirm_reject), getString(R.string.btn_inward_alert_reject), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                mainViewModel.deleteSession(sessionObject);
                if(isNonEmpty(dbAllHUsDublicateRfid)){
                  tripInventoryDao.deleteDuplicateHU(dbAllHUsDublicateRfid, tripNumber);
                  tripInventoryDao.updateDuplicateHUattemptcount(dbAllHUsDublicateRfid, tripNumber);
                }
                else
                  tripInventoryDao.updateHUStatus(huNumber, "R", !isHappyFlow, reason, tripNumber);//add reason in query
                updateLists();
                popBackStack();
              }
            });
          }
        }, !isHappyFlow ? getString(R.string.btn_inward_alert_rescan) : "", new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            
            context.showCustomAlertDialog("", getString(R.string.msg_inw_confirm_rescan), getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                mainViewModel.deleteSession(sessionObject);
                tripInventoryDao.deleteHU(huNumber, tripNumber);
                tripInventoryDao.updateHUattemptcount(huNumber, tripNumber);
                notifyAdapter();
              }
            }, getString(R.string.btn_no), null);
          }
        });
      }
      else{
        context.showCustomAlertDialog(title, isDuplicate ? reason : "", isDuplicate || isHappyFlow || (!isNonEncoded && !isUnknown && ((isQuantityMatch && extracount <= 0) || (isLessAcceptableCount && extracount <= 0 && !isLessThenAcceptMisMatched))) ? null : setAlertDisplayDataList(expectedScanned, nonencoded, unknown, extracount), isHappyFlow, false, !isHappyFlow ? getString(R.string.btn_inward_alert_rescan) : "", new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            
            context.showCustomAlertDialog("", getString(R.string.msg_inw_confirm_rescan), getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                mainViewModel.deleteSession(sessionObject);
                tripInventoryDao.deleteHU(huNumber, tripNumber);
                tripInventoryDao.updateHUattemptcount(huNumber, tripNumber);
                notifyAdapter();
              }
            }, getString(R.string.btn_no), null);
          }
        }, getString(R.string.btn_inward_alert_reject), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            showCustomConfirmationDialog(reason, getString(R.string.btn_inward_alert_reject), isHappyFlow);
            context.showCustomConfirmDialog(getString(R.string.msg_inw_confirm_reject), getString(R.string.btn_inward_alert_reject), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                mainViewModel.deleteSession(sessionObject);
                if(isNonEmpty(dbAllHUsDublicateRfid)){
                  tripInventoryDao.deleteDuplicateHU(dbAllHUsDublicateRfid, tripNumber);
                  tripInventoryDao.updateDuplicateHUattemptcount(dbAllHUsDublicateRfid, tripNumber);
                }
                else
                  tripInventoryDao.updateHUStatus(huNumber, "R", !isHappyFlow, reason, tripNumber);//add reason in query
                updateLists();
                popBackStack();
              }
            });
          }
        });
      }
      return;
    }
    
    alertbinding.imgClose.setOnClickListener(v -> {
      builder.dismiss();
    });
    alertbinding.accept.setOnClickListener(v -> {
      builder.dismiss();
      String reason = alertbinding.txtAlertSubtitle.getText().toString();
      showCustomConfirmationDialog(reason, AppConstants.K_HU_STATUS_ACCEPT, isHappyFlow);
    });
    alertbinding.reject.setOnClickListener(v -> {
      /*if(isDuplicate){
        builder.dismiss();
        showCustomConfirmationDuplicateDialog(AppConstants.K_HU_STATUS_REJECT, dbAllHUsDublicateRfid);
      }
      else{*/
      builder.dismiss();
      String reason = alertbinding.txtAlertSubtitle.getText().toString();
      showCustomConfirmationDialog(reason, AppConstants.K_HU_STATUS_REJECT, isHappyFlow, isDuplicate ? dbAllHUsDublicateRfid : null);
      //}
    });
    alertbinding.rescan.setOnClickListener(v -> {
      String reason = alertbinding.txtAlertSubtitle.getText().toString();
      builder.dismiss();
      showCustomConfirmationDialog(reason, AppConstants.K_HU_STATUS_RESCAN, isHappyFlow);
      showLog("STARTINV", "a");
    });
    
    builder.setView(alertbinding.getRoot());
    builder.show();
    builder.setCancelable(false);
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
      listData.add(new LabelValues(getString(R.string.lbl_inward_alert_total_qty_), binding.txtHuTotalQty.getText().toString()));
      listData.add(new LabelValues(getString(R.string.lbl_inward_alert_total_scanned_qty), binding.txtHuScanQty.getText().toString()));
      //if(expectedScanned>0)
      listData.add(new LabelValues(getString(R.string.lbl_inward_alert_expected_scanned_qty), "" + expectedScanned));
      if(nonencoded > 0)
        listData.add(new LabelValues(getString(R.string.lbl_inward_alert_unencoded_scanned_qty), "" + nonencoded));
      if(extracount > 0)
        listData.add(new LabelValues(getString(R.string.lbl_inward_alert_extra_scanned_qty), "" + extracount));
    }
    catch(Exception e){ e.printStackTrace(); }
    
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
      List<TripInventory> newList = tripInventoryDao.getCurrentHuDetails(huNumber, tripNumber);
      if(newList != null) dataList.addAll(newList);
      if(binding != null && binding.listTripHuScan != null && binding.listTripHuScan.getAdapter() != null && binding.listTripHuScan.getAdapter() instanceof RecyclerView.Adapter)
        ((RecyclerView.Adapter) binding.listTripHuScan.getAdapter()).notifyDataSetChanged();
    }
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    notifyAdapter();
  }
  
  public void onBackPressed(){
    super.onBackPressed();
  }
  
  /**
   * Show custom confirmation dialog.
   *
   * @param reason           the reason
   * @param action           the action
   * @param isLocalHappyFlow the is local happy flow
   */
  public void showCustomConfirmationDialog(String reason, final String action, boolean isLocalHappyFlow){ showCustomConfirmationDialog(reason, action, isLocalHappyFlow, null); }
  
  /**
   * Show custom confirmation dialog.
   *
   * @param reason           the reason
   * @param action           the action
   * @param isLocalHappyFlow the is local happy flow
   * @param duplicateList    the duplicate list
   */
  public void showCustomConfirmationDialog(String reason, final String action, boolean isLocalHappyFlow, List<String> duplicateList){
    context.showCustomConfirmDialog(String.format(getString(R.string.msg_inw_confirm_action), action), R.string.btn_ok, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        if(action.equals(AppConstants.K_HU_STATUS_ACCEPT)){
          mainViewModel.deleteSession(sessionObject);
          tripInventoryDao.updateHUStatus(huNumber, "A", !isLocalHappyFlow, isLocalHappyFlow ? "" : reason, tripNumber);
          updateLists();
          popBackStack();
        }
        else if(action.equals(AppConstants.K_HU_STATUS_REJECT)){
          mainViewModel.deleteSession(sessionObject);
          if(isNonEmpty(duplicateList)){
            tripInventoryDao.deleteDuplicateHU(duplicateList, tripNumber);
            tripInventoryDao.updateDuplicateHUattemptcount(duplicateList, tripNumber);
          }
          else
            tripInventoryDao.updateHUStatus(huNumber, "R", !isLocalHappyFlow, reason, tripNumber);//add reason in query
          updateLists();
          popBackStack();
        }
        else if(action.equals(AppConstants.K_HU_STATUS_RESCAN)){
          mainViewModel.deleteSession(sessionObject);
          tripInventoryDao.deleteHU(huNumber, tripNumber);
          tripInventoryDao.updateHUattemptcount(huNumber, tripNumber);
          notifyAdapter();
        }
      }
    });
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    super.onDataSizeChanged(size);
    if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
    if(size == null) return;
    binding.llManualScanBarcode.setVisibility(allowManualBarcodeScanning && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || getSize() < totalQty) ? View.VISIBLE : View.GONE);
    binding.llBtnStartStop.setVisibility(!chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? View.VISIBLE : View.GONE);
    binding.llUpload.setVisibility(!isProcessOn() && getSize() > 0 ? View.VISIBLE : View.GONE);
    binding.txtHuScanQty.setText("" + chkNull(size, 0));
    double per = totalQty > 0 ? (chkNull(size, 0) * 100) / totalQty : 0;
    int percentage = (int) per;
    binding.progressbarHuScore.setProgress(percentage);
    updateLists();
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack() && allowManualBarcodeScanning && mainViewModel != null){
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
    binding.btnStartStopScan.setImageResource(chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? R.drawable.ic_start_disable : isInvOn ? R.drawable.ic_stop : R.drawable.ic_start);
    binding.textStartStop.setText(isInvOn ? R.string.lbl_stop : R.string.lbl_start);
    binding.llSeekbarPower.setVisibility(!isProcessOn && binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
    binding.llManualScanBarcode.setVisibility(allowManualBarcodeScanning && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || getSize() < totalQty) ? View.VISIBLE : View.GONE);
    binding.llBtnStartStop.setVisibility(!chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) ? View.VISIBLE : View.GONE);
    binding.llUpload.setVisibility(!isProcessOn() && getSize() > 0 ? View.VISIBLE : View.GONE);
    
  }
  
  @Override
  protected void onTriggerPressed(){
    binding.llBtnStartStop.performClick();
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
      sessionObject.sessionType = AppCommonMethods.SessionType.INWARD.getValue();
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
      //TODO
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
          requestParams.put(ParamConstants.TYPE, AppConstants.INWARD);
          requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD);
          JSONArray tripJsonArray = new JSONArray();
          JSONObject jobj = new JSONObject();
          jobj.put(ParamConstants.K_TRIP_NUMBER, tripNumber);
          jobj.put(ParamConstants.K_TRIP_TYPE, tripType);
          jobj.put(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
          List<TripInventory> list = tripInventoryDao.getAllTripInventory(tripNumber, huNumber);
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
                
                JSONObject item = tripInventory.toJson(context);
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
            tripInventoryDao.deleteAllTripInventory();
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}