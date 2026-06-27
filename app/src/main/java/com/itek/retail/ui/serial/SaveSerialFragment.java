package com.itek.retail.ui.serial;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.dp2px;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.databinding.DialogSerialScanBinding;
import com.itek.retail.databinding.FragmentActionMenuSearchBinding;
import com.itek.retail.databinding.FragmentSaveSerialBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.TripInventory;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;
import com.itek.retail.ui.inward1.HuProcessStartFragment;

import org.json.JSONObject;

public class SaveSerialFragment extends CommonFragment{
  private final AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.SCAN;
  private MainViewModel mainViewModel;
  private FragmentSaveSerialBinding binding;
  private DialogSerialScanBinding dialogSerialScanBinding;
  private AlertDialog huScanDialog = null;
  private boolean isInwSerialMandatory = false;
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    mainViewModel = ((MainActivity) context).getRfidViewModel();
    mainViewModel.getReaderUHFInstance(sessionType);
  }
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    binding = FragmentSaveSerialBinding.inflate(inflater, container, false);
    
    isInwSerialMandatory = false;//SharedPrefManager.getBoolean(ParamConstants.IS_SERIAL_NUMBER_MANDATORY,false);
    
    binding.btnGo.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
       if(v!=null && v.getVisibility()==View.VISIBLE && allowBtnClick){
         if(mainViewModel.getIsProcessOn().getValue()) return;
         if(dialogSerialScanBinding!=null) return;
         context.dismissCustomAlertDialog();
         if(checkReaderConnected()) mainViewModel.performPick("");
       }
      }
    });
    
    return binding.getRoot();
  }
  
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
          updateViews(isPickOn);
        }
      }
    });
    
    mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
    mainViewModel.getPickData().observe(getViewLifecycleOwner(), new Observer<Inventory>(){
      @Override
      public void onChanged(Inventory pickData){
        if(!isTopInStack()) return;
        if(pickData != null){
          try{
            Bundle args = chkNull(getArguments(), new Bundle());
            args.putSerializable(pickData.getClass().getSimpleName(), pickData);
            JSONObject requestParams = new JSONObject();
            requestParams.put(ParamConstants.EAN, pickData.ean);
            requestParams.put(ParamConstants.EPC, pickData.epc);
            requestParams.put(ParamConstants.TID, pickData.tid);
            callWebService(URLConstants.GET_SERIAL_NUMBER, requestParams,args,getString(R.string.progress_msg_getting_data));
          }catch(Exception e){e.printStackTrace();}
        }
        if(pickData != null) mainViewModel.getPickData().postValue(null);
      }
    });
    
    /*mainViewModel.getIsProcessOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getIsProcessOn().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isProcessOn){
        updateViews(chkNull(isProcessOn,false));
      }
    });*/
    
    setTriggerDataObserver();
    mainViewModel.getIsDeviceConfigured().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isReaderConfigured){
        if(isTopInStack() && chkNotNullTrue(isReaderConfigured)){
        
        }
      }
    });
  }
  
  private void updateViews(final boolean isProcessOn){
    ((MainActivity) context).lockDrawer(isProcessOn);
    binding.btnGo.setBackgroundResource(isProcessOn?R.drawable.button_background_gray:R.drawable.button_background);
    binding.btnGo.setEnabled(!isProcessOn);
  }
  
  /**
   * Set trigger data observer.
   */
  private void setTriggerDataObserver(){
    mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
    mainViewModel.isTriggerPressed().observe(getViewLifecycleOwner(), triggerPressed -> {
      if(!isTopInStack()) return;
      if(triggerPressed != null && getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && allowBtnClick){
        if(triggerPressed && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          if(dialogSerialScanBinding != null && dialogSerialScanBinding.edtSearch != null)
            dialogSerialScanBinding.edtSearch.performScan();
          else binding.btnGo.performClick();
        }
      }
    });
  }
  
  /**
   * Set barcode observers.
   */
  void setBarcodeObservers(){
    mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
    if(dialogSerialScanBinding != null && dialogSerialScanBinding.edtSearch != null)
      mainViewModel.getIsBarcodeOn().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
        @Override
        public void onChanged(Boolean isBarcodeOn){
          if(!isTopInStack()) return;
          showLog("isBarcodeOn", AppCommonMethods.chkVal(isBarcodeOn));
          insertAuditTrailsLog("Barcode_" + (chkNotNullTrue(isBarcodeOn) ? "ON" : "OFF"));
          //if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
          if(dialogSerialScanBinding != null && dialogSerialScanBinding.edtSearch != null)
            dialogSerialScanBinding.edtSearch.setIsProcessOn(chkNotNullTrue(isBarcodeOn));
        }
      });
    
    mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
    if(dialogSerialScanBinding != null && dialogSerialScanBinding.edtSearch != null)
      mainViewModel.getBarcodeData().observe(getViewLifecycleOwner(), new Observer<String>(){
        @Override
        public void onChanged(String barcode){
          if(!isTopInStack()) return;
          showLog(SaveSerialFragment.this.getClass().getSimpleName() + "_barcodeData", chkNull(barcode, ""));
          //if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
          if(isNonEmpty(barcode)){
            if(dialogSerialScanBinding != null && dialogSerialScanBinding.edtSearch != null && huScanDialog != null && huScanDialog.isShowing() && huScanDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null){
              dialogSerialScanBinding.edtSearch.setText(barcode);
              //Commented this line as user confirmation is required for scanned serial number
              //huScanDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            }
          }
          if(isNonEmpty(barcode)){
            mainViewModel.getBarcodeData().postValue("");
          }
        }
      });
  }
  
  private void scanSerialNo(final Bundle args,final String serialNo){
    if(huScanDialog == null || !huScanDialog.isShowing()){
      final Inventory pickedTag = (Inventory) extractSerializable(args, Inventory.class);
      mainViewModel.getBarcodeReaderInstance(getSessionType());
      
      final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialog).create();
      setAlertDialogCustomTitle(alertDialog, R.string.msg_set_serial);
      
      DialogSerialScanBinding binding = DialogSerialScanBinding.inflate(LayoutInflater.from(context), null, false);
      //temp code may need to add/set isMandatory field
      binding.edtSearch.setValidationRegex(isInwSerialMandatory ? "[0-9A-Za-z\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\-]{1,50}" : "[0-9A-Za-z\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\-]{0,50}");
      binding.edtSearch.setText(chkNull(serialNo,""));
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
                  alertDialog.dismiss();
                  return;
                }
                //call Verify Serial API
                try{
                  Bundle args = chkNull(getArguments(), new Bundle());
                  args.putSerializable(pickedTag.getClass().getSimpleName(), pickedTag);
                  JSONObject requestParams = new JSONObject();
                  requestParams.put(ParamConstants.EAN, pickedTag.ean);
                  requestParams.put(ParamConstants.EPC, pickedTag.epc);
                  requestParams.put(ParamConstants.TID, pickedTag.tid);
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
  
  private void setSerialNo(final String msg,final String serialNo, Bundle args) {
    //if(isNonEmpty(msg)) context.showCustomSuccessDialog(chkNull(msg,""));
    final Inventory pickedTag = (Inventory) extractSerializable(args, Inventory.class);
    if(pickedTag != null && isNonEmpty(serialNo)){
      try{
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.EAN, pickedTag.ean);
        requestParams.put(ParamConstants.EPC, pickedTag.epc);
        requestParams.put(ParamConstants.TID, pickedTag.tid);
        requestParams.put(ParamConstants.SERIAL_NUMBER, serialNo.toUpperCase());
        requestParams.put(ParamConstants.IS_UPDATE_SERIAL_NUMBER, isNullOrEmpty(msg));
        callWebService(URLConstants.SAVE_SERIAL_NUMBER, requestParams,getString(R.string.progress_msg_saving_data));
      }catch(Exception e){e.printStackTrace();}
      //if(isNullOrEmpty(msg)) context.showCustomSuccessDialog(String.format(getString(R.string.msg_serial_saved),serialNo));
      //if(huScanDialog != null && huScanDialog.isShowing()) huScanDialog.dismiss();
    }
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
        @Override
        public void run(){
          mainViewModel.onResume(sessionType);
        }
      }, 300);
    }
  }
  
  @Override
  public AppCommonMethods.SessionType getSessionType(){ return sessionType; }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_SERIAL_NUMBER:
          if(isSuccess){
            final String serialNo = extractString(jsonResponse,ParamConstants.SERIAL_NUMBER,"");
            scanSerialNo(args,serialNo);
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
        case URLConstants.SAVE_SERIAL_NUMBER:
          if(isSuccess){
            if(huScanDialog != null && huScanDialog.isShowing()) huScanDialog.dismiss();
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), true);
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
