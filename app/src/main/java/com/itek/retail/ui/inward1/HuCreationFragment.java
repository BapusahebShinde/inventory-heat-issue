package com.itek.retail.ui.inward1;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.BarcodeScanFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.HUStatusDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.FragmentTripHuCreationBinding;
import com.itek.retail.model.HUStatus;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;

import java.io.Serializable;

/**
 * The Hu Creation fragment.
 */
public class HuCreationFragment extends BarcodeScanFragment {

  private final AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.SCAN;

  public TripStatus tripStatus;
  String tripNo = "";
  String displayTripNo = "";
  private FragmentTripHuCreationBinding binding;
  private TripStatusDao tripStatusDao;
  private HUStatusDao huStatusDao;
  private boolean isScanHu = false;
  private String typeIO = "";
  private String labelTrip = "";
  private String labelHU = "";
  private String labelArticle = "";
  private String labelSku = "";
  private MainViewModel mainViewModel;
  
  /**
   * Instantiates a new Inward grn hu scan fragment.
   */
  public HuCreationFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    if(isScanHu){
      mainViewModel = ((MainActivity) context).getRfidViewModel();
      mainViewModel.getReaderUHFInstance(sessionType);
      mainViewModel.getBarcodeReaderInstance(sessionType);
    }
    tripStatusDao = AppDatabase.getTripStatusDao(context);
    huStatusDao = AppDatabase.getHUStatusDao(context);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    binding = FragmentTripHuCreationBinding.inflate(inflater, container, false);
    
    if(getArguments() != null){
      Object obj = extractSerializable(getArguments(), TripStatus.class);
      tripStatus = obj != null && obj instanceof TripStatus ? (TripStatus) obj : null;
      showLog("tripStatus", String.valueOf(tripStatus));
      typeIO = extractString(getArguments(), ParamConstants.TYPE, extractString(getArguments(), ParamConstants.OPERATION_TYPE, AppConstants.INWARD));
      tripNo = tripStatus != null ? tripStatus.getTripNumber() : extractString(getArguments(), ParamConstants.TRIP_NUMBER, "");
      final String refTripNo = tripStatus != null ? tripStatus.getRefTripNumber() : extractString(getArguments(), ParamConstants.REFERENCE_TRIP_NUMBER, "");
      displayTripNo = tripStatus != null ? chkNull(tripStatus.getRefTripNumber(), chkNull(tripStatus.getTripNumber(), tripNo)) : chkNull(refTripNo,chkNull(tripNo,""));
      final String huNum = extractString(getArguments(), AppConstants.HU_NUMBER, "");
      if(isNonEmpty(huNum)){
        binding.ivHuNo.setText(huNum);
        binding.ivHuNo.setEnabled(false);
      }
      //if(binding.ivHuNo.getText().length() > 0) binding.btnGo.performClick();
    }
    
    showLog("tripNumber", tripNo);
    //binding.txtTripNumber.setText(HtmlCompat.fromHtml(isNonEmpty(tripNumber) ? String.format(getString(R.string.txt_trip), tripNumber) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
    //binding.txtTripNumber.setVisibility(isNonEmpty(tripNumber) ? View.VISIBLE : View.GONE);
    
    initUI();
    
    binding.btnGo.setOnClickListener(v -> {
      if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick /*&& !chkNotNullTrue(mainViewModel.getIsProcessOn().getValue())*/ && binding.ivHuNo.validate() && binding.ivExpQty.validate()){
        //binding.ivHuNo.setIsViewControlEnabled(false, true);
        final String huNumber = binding.ivHuNo.getText().toString().trim();
        final int expQty = Integer.parseInt(binding.ivExpQty.getText().toString().trim());
        if(huStatusDao.isHuPresent(typeIO, displayTripNo, huNumber)){
          //context.showCustomErrDialog(String.format(getResources().getString(R.string.err_duplicate__for__), labelHU+":"+huNumber, labelTrip+":"+displayTripNo));
          context.showCustomErrDialog(String.format(getResources().getString(R.string.err__already_exist_for__), labelHU + ":" + huNumber, labelTrip + ":" + displayTripNo));
          clearBarcode();
        }
        else{
          HUStatus huStatus = new HUStatus();
          huStatus.setType(typeIO);
          huStatus.setTripNumber(displayTripNo);
          huStatus.setHuNumber(huNumber);
          huStatus.setExpQty(expQty);
          huStatus.setScanQty(0);
          huStatus.setStatus(AppConstants.HU_STATUS_PENDING);
          huStatus.setReason("");
          huStatus.setUploaded(false);
          huStatus.setManualHU(true);
          huStatusDao.insertHUStatusData(huStatus);
          tripStatusDao.updateTripHUCount(displayTripNo,typeIO);
          tripStatus.numberOfHu+=1;
          Bundle arg = chkNull(getArguments(), new Bundle());
          arg.putString(ParamConstants.TRIP_NUMBER, tripNo);
          arg.putString(ParamConstants.REFERENCE_TRIP_NUMBER, displayTripNo);
          if(tripStatus != null)
            arg.putSerializable(tripStatus.getClass().getSimpleName(), (Serializable) tripStatus);
          if(isNonEmpty(huNumber))
            arg.putString(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
          //popBackStack();
          //Change for Auto-Redirection to HuProcessStartFragment
          context.loadFragment(new HuProcessStartFragment(),arg);
          /*new Handler().post(new Runnable(){
            @Override
            public void run(){*/
              removeFromBackStack(HuCreationFragment.this);
            /*}
          });*/
        }
      }
    });

    if (isScanHu) setInputView(binding.ivHuNo,binding.btnGo);

    return binding.getRoot();
  }
  
  /**
   * Clear barcode.
   */
  public void clearBarcode(){
    binding.ivHuNo.setText("");
  }
  
  private void initUI(){
    labelTrip = SharedPrefManager.getString(ParamConstants.LABEL_TRIP, getString(R.string.lbl_trip));
    labelHU = SharedPrefManager.getString(ParamConstants.LABEL_HU, getString(R.string.lbl_hu));
    labelArticle = SharedPrefManager.getString(ParamConstants.LABEL_ARTICLE, getString(R.string.lbl_article_no));
    labelSku = SharedPrefManager.getString(ParamConstants.LABEL_SKUID, getString(R.string.lbl_ean));
    
    binding.ivHuNo.setLabel(String.format(getString(R.string.lbl__no), labelHU));
    binding.ivHuNo.setHint(String.format(getString(R.string.hint__no), labelHU));
    
    binding.txtTripNo.setText(String.format(getString(R.string.lbl__no), labelTrip));
    binding.txtTotalHus.setText(String.format(getString(R.string.lbl_total__s), labelHU));
    binding.txtCompletedHus.setText(String.format(getString(R.string.lbl_completed__s), labelHU));
    
    binding.txtTripNumber.setText(displayTripNo);
    binding.textTotalHu.setText(String.valueOf(tripStatus.getNumberOfHu()));
    binding.textCompletedHu.setText(String.valueOf(tripStatus.getCompletedHu()) + "/" + tripStatus.getNumberOfHu());
    
    binding.txtTotalHus.setVisibility(AppCommonMethods.isShowTotalAndCompletedCount? View.VISIBLE: View.GONE);
    binding.textTotalHu.setVisibility(AppCommonMethods.isShowTotalAndCompletedCount? View.VISIBLE: View.GONE);
  }
  
}