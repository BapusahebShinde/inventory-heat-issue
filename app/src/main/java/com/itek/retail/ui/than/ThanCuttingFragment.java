package com.itek.retail.ui.than;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.dp2px;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.formatDoubleStr2Decimals;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.successBeep;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.EanListAdapter;
import com.itek.retail.adapter.ProdDisplayDataListAdapter;
import com.itek.retail.adapter.ProductColorsListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.DialogPrintPreviewBinding;
import com.itek.retail.databinding.DialogSizeChartBinding;
import com.itek.retail.databinding.FragmentThanCuttingBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.LabelValues;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.actionmenu.ActionMenuSearchFragment;
import com.itek.retail.ui.customviews.InputView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;
import com.rscja.team.qcom.deviceapi.G;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Than Cutting fragment.
 */
public class ThanCuttingFragment extends CommonFragment{
  
  private final AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.SCAN;
  private ThanViewModel mViewModel;
  private FragmentThanCuttingBinding binding;
  private MainViewModel mainViewModel;
  private boolean isThanCutting = false;
  private boolean isThanClosure = false;
  private boolean isShowingProdDtls = false;
  private List<ProductModel> listProducts = new ArrayList<>(0);
  private ProductModel model;
  private Inventory tagData;
  private AlertDialog printPreviewAlert;
  private boolean isUploaded = false;
  
  /**
   * Instantiates a new Decoding start fragment.
   */
  public ThanCuttingFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    mainViewModel = ((MainActivity) context).getRfidViewModel();
    mainViewModel.getReaderUHFInstance(sessionType);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ThanViewModel.class);
    binding = FragmentThanCuttingBinding.inflate(inflater, container, false);
    
    isThanCutting = extractBoolean(getArguments(), ParamConstants.IS_THAN_CUTTING, getMenuModel() != null ? getMenuModel().getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_THAN_CUTTING) : false);
    isThanClosure = extractBoolean(getArguments(), ParamConstants.IS_THAN_CLOSURE, getMenuModel() != null ? getMenuModel().getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_THAN_CLOSURE) : false);
    
    if(!isThanCutting && !isThanClosure) popBackStack();
    
    hideKeyboard();
    
    
    binding.llBtnStart.setOnClickListener(v -> {
      if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick && !isProcessOn()){
        context.dismissCustomAlertDialog();
        if(binding.pdvThan!=null) binding.pdvThan.dismissAlerts();
        if(checkReaderConnected()){
          tagData = null;
          mainViewModel.performPick("",7);
        }
      }
    });
    
    binding.btnSet.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick && !isProcessOn() && !context.isPrinting && binding.ivThanLen.validate()){
          hideKeyboard();
          if(printPreviewAlert!= null && printPreviewAlert.isShowing()) return;
          final String length = formatDoubleStr2Decimals(binding.ivThanLen.getText().toString().trim());
          final String unit = binding.ivThanLen.getUnit();
          if(Double.parseDouble(chkNull(length,"0"))>Double.parseDouble(chkNull(model.getLengthBalance(),"0"))){
            hideKeyboard();
            binding.ivThanLen.updateError(getString(R.string.err_than_length_greater_than_balance));
          }
          else{
            showPrintPreviewAlert(length,unit);
            /*context.showCustomConfirmDialog(String.format(getString(R.string.msg_confirm_lbl_val),getString(isThanClosure?R.string.lbl_than_length_closure:R.string.lbl_than_length_cutting),length), getString(R.string.btn_submit),new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialog, int which){
                callUpdateAPI(length);
              }
            });*/
          }
        }
      }
    });
    
    binding.ivThanLen.setLabel(isThanClosure?R.string.lbl_than_length_closure:R.string.lbl_than_length_cutting);
    binding.ivThanLen.setHint(isThanClosure?R.string.hint_than_length_closure:R.string.hint_than_length_cutting);
    binding.ivThanLen.setUnit(SharedPrefManager.getString(ParamConstants.LENGTH_UNIT,AppCommonMethods.defLengthUnitThan));
    binding.ivThanLen.setValidationRegex(isThanClosure && SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_ZERO_LENGTH_FOR_THAN_CLOSURE,AppCommonMethods.isAllowZeroLengthForThanClosure)? AppConstants.REGEX_THAN_LENGTH_CLOSURE : AppConstants.REGEX_THAN_LENGTH);
    binding.ivThanLen.setButton(binding.btnSet);
    
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
    //List of Done Products (No DB) Maintain List Only.. (Latest Record above)
    binding.listHistory.setAdapter(new EanListAdapter(context, ThanCuttingFragment.this, listProducts));
    binding.listHistory.setLayoutManager(layoutManager);
    
    binding.header.imgConfigSync.setVisibility(View.GONE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())) return;
        //binding.llEncodingConfig.setVisibility(binding.llEncodingConfig.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
      }
    });
    
    binding.clPickHistory.setVisibility(View.VISIBLE);
    binding.clProdDtls.setVisibility(View.GONE);
    
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
        if(((MainActivity) context).isReaderConnected()){
          tagData = null;
          mainViewModel.performPick("");
        }
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
          binding.llBtnStart.setEnabled(!isPickOn);
          binding.listHistory.setEnabled(!isPickOn);
          
        }
      }
    });
    
    mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
    mainViewModel.getPickData().observe(getViewLifecycleOwner(), new Observer<Inventory>(){
      @Override
      public void onChanged(Inventory inventory){
        if(!isTopInStack()) return;
        if(inventory != null /*&& chkNull(inventory.ean, "").matches(getEanRegex(true))*/){
          //binding.searchProduct.setText(inventory.ean);
          successBeep();
          callAPIThanDetails(inventory);
        }
        if(inventory != null) mainViewModel.getPickData().postValue(null);
      }
    });
    
    setTriggerDataObserver();
    /*mainViewModel.getIsDeviceConfigured().observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isReaderConfigured){
        if(isTopInStack() && chkNotNullTrue(isReaderConfigured)){
          setTriggerDataObserver();
        }
      }
    });*/
  }
  
  public boolean isProcessOn(){
    return mainViewModel != null && chkNotNullTrue(mainViewModel.getIsProcessOn().getValue());
  }
  
  public void setupView(final ProductModel productModel, boolean isViewOnly){
    hideKeyboard();
    binding.pdvThan.setProductModel(productModel,!chkNull(productModel.getZone(), AppConstants.DEFAULT_NO_VALUE).equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
    toggleUI(true);
  }
  
  private void callAPIThanDetails(final Inventory inventory){
    try{
      JSONObject jsonRequest = new JSONObject();
      final Inventory tagInfo = inventory;
      this.tagData = tagInfo;
      jsonRequest.put(ParamConstants.EAN, tagInfo != null ? chkNull(tagInfo.ean, "") : "");
      jsonRequest.put(ParamConstants.EPC, tagInfo != null ? chkNull(tagInfo.epc, "") : "");
      jsonRequest.put(ParamConstants.TID, tagInfo != null ? chkNull(tagInfo.tid, "") : "");
      final String productInfoUrl = /*isDebugApp?getProductInfoUrl():*/URLConstants.GET_PRODUCT_INFO_THAN;
      if(productInfoUrl.equalsIgnoreCase(URLConstants.GET_PRODUCT_INFO_BY_SKU)){
        JSONArray js = new JSONArray();
        js.put(jsonRequest.get(ParamConstants.EAN));
        jsonRequest.put(ParamConstants.ITEMS, js);
      }
      callWebService(productInfoUrl, jsonRequest, getString(R.string.progress_msg_getting_data), true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Set trigger data observer.
   */
  private void setTriggerDataObserver(){
    mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
    mainViewModel.isTriggerPressed().observe(getViewLifecycleOwner(), triggerPressed -> {
      if(!isTopInStack()) return;
      if(triggerPressed != null && getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && allowBtnClick && isTopInStack()){
        if(triggerPressed && !isProcessOn()){
          if(isShowingProdDtls && binding.btnSet.getVisibility()==View.VISIBLE && !context.isPrinting)  binding.btnSet.performClick();
          else if(!isShowingProdDtls) binding.llBtnStart.performClick();
        }
      }
    });
  }
  
  public void callUpdateAPI(final String length){
    if(isNullOrEmpty(length)) return;
    if(model==null || model.isUploaded()) return;
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.EAN, tagData != null ? chkNull(tagData.ean, "") : model != null ? model.getEan() : "");
      jsonRequest.put(ParamConstants.EPC, tagData != null ? chkNull(tagData.epc, "") : "");
      jsonRequest.put(ParamConstants.TID, tagData != null ? chkNull(tagData.tid, "") : "");
      jsonRequest.put(ParamConstants.STATUS, isThanCutting ? AppConstants.THAN_STATUS_CUTTING : isThanClosure ?AppConstants.THAN_STATUS_CLOSURE : "");
      if(model != null){
        jsonRequest.put(ParamConstants.ARTICLE_NO, model.getArticleNo());
        jsonRequest.put(ParamConstants.LENGTH_ORIGINAL, model.getLengthOriginal());
        jsonRequest.put(ParamConstants.LENGTH_OLD, model.getLengthBalance());
        jsonRequest.put(ParamConstants.LENGTH_BALANCE, model.getLengthBalance());
        if(isThanClosure)
          jsonRequest.put(ParamConstants.LENGTH_CLOSURE, length);
        if(isThanCutting)
          jsonRequest.put(ParamConstants.LENGTH_CUTTING, length);
      }
      final String uploadUrl = isThanClosure ? URLConstants.UPLOAD_CLOSURE_THAN : isThanCutting ? URLConstants.UPLOAD_CUTTING_THAN : "";
      if(isNonEmpty(uploadUrl))
        callWebService(uploadUrl, jsonRequest, getString(R.string.progress_msg_uploading_data), true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  public boolean isThanCutting(){return  isThanCutting;}
  public boolean isThanClosure(){return  isThanClosure;}
  
  @Override
  public void onBackPressed(){
    if(mainViewModel != null && chkNotNullTrue(mainViewModel.getIsPickOn().getValue()))
      context.showShortToast(String.format(getString(R.string.err_op_back_press), getTypeCharCode(), sessionType.name()));
    else if(isShowingProdDtls) toggleUI();
    else super.onBackPressed();
  }
  
  private void toggleUI(){toggleUI(false);}
  private void toggleUI(final boolean isViewOnly){
    if(isShowingProdDtls && printPreviewAlert!=null && printPreviewAlert.isShowing()) return;
    hideKeyboard();
    isShowingProdDtls = !isShowingProdDtls;
    if(!isShowingProdDtls && model != null) model = null;
    final boolean isFixedHeightView = isShowingProdDtls && !isViewOnly && model!=null && !model.isSold && !model.isClosed;
    if(isShowingProdDtls){
      binding.pdvThan.setVerticalView(!isFixedHeightView);
      binding.ivThanLen.setText("");
      ConstraintLayout.LayoutParams clp = ((ConstraintLayout.LayoutParams)binding.pdvThan.getLayoutParams());
      clp.height= isFixedHeightView?ViewGroup.LayoutParams.WRAP_CONTENT:ViewGroup.LayoutParams.MATCH_PARENT;
      clp.constrainedHeight=isFixedHeightView;
      binding.pdvThan.setLayoutParams(clp);
    }
    binding.clProdDtls.setVisibility(isShowingProdDtls ? View.VISIBLE : View.GONE);
    binding.clProdDtls.setBackgroundResource(isFixedHeightView?R.drawable.border_bottom_dark:R.color.transparent);
    
    binding.ivThanLen.setVisibility(isFixedHeightView?View.VISIBLE:View.GONE);
    binding.btnSet.setVisibility(isFixedHeightView ?View.VISIBLE:View.GONE);
    binding.clPickHistory.setVisibility(!isShowingProdDtls ? View.VISIBLE : View.GONE);
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()) mainViewModel.onResume(sessionType);
  }
  
  @Override
  public void onPause(){
    super.onPause();
    mainViewModel.onPause();
  }
  
  @Override
  public void onDestroyView(){
    //mainViewModel.getIsDeviceConfigured().removeObservers(getViewLifecycleOwner());
    mainViewModel.getIsPickOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
    mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
    super.onDestroyView();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    if(binding!=null && binding.txtTotal!=null){
      binding.txtTotal.setVisibility(listProducts.size()>0? View.VISIBLE: View.GONE);
      binding.txtTotal.setText(getString(R.string.lbl_encode_history)+": "+listProducts.size());
    }
    if(binding!=null && binding.listHistory!=null && binding.listHistory.getAdapter()!=null)
      binding.listHistory.getAdapter().notifyDataSetChanged();
  }
  
  public void showPrintPreviewAlert(final String length, final String unit){
    if(model==null) return;
    printPreviewAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    setAlertDialogCustomTitle(printPreviewAlert, isThanClosure?R.string.title_slip_closure:R.string.title_slip_billing);
    final DialogPrintPreviewBinding dialogBinding = DialogPrintPreviewBinding.inflate(LayoutInflater.from(context));
    final ImageView ivBarcode = dialogBinding.imgBarcode;
    final RecyclerView listFields = dialogBinding.listDialogPrintFields;
    final List<LabelValues> listPrintFields = model.getLabelValueListForThanPrinting(context,isThanClosure,length, unit);
    listFields.setAdapter(new ProdDisplayDataListAdapter((MainActivity) context, listPrintFields));
    listFields.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
    printPreviewAlert.setView(dialogBinding.getRoot());
    printPreviewAlert.setCancelable(false);
    printPreviewAlert.setButton(DialogInterface.BUTTON_POSITIVE, getString(model.getSold()?R.string.btn_print:R.string.btn_print_upload),(DialogInterface.OnClickListener) null);
    printPreviewAlert.setButton(DialogInterface.BUTTON_NEGATIVE,getString(R.string.btn_cancel),(DialogInterface.OnClickListener) null);
    printPreviewAlert.setOnShowListener(new DialogInterface.OnShowListener(){
      @Override
      public void onShow(DialogInterface dialog){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //final int wid = (isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / 7;
        //final int imgWid = (isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels)/3;
        //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(wid, wid);
        //int mar = getResources().getDimensionPixelSize(R.dimen.dp_5);
        //lp.setMargins(mar, mar, mar, mar);
        //lp.gravity = Gravity.CENTER;
        //ivBarcode.setLayoutParams(lp);
        ivBarcode.setVisibility(!isThanClosure?View.VISIBLE:View.GONE);
        if(!isThanClosure && model!=null){
          //context.generateBarcode(model.getEan(),ivBarcode);
          ivBarcode.setImageBitmap(context.generateBarcode(model.getEan(),dpToPx(300),dpToPx(100)));
          //ivBarcode.setImageBitmap(context.generateBarcode(model.getEan(), imgWid, imgWid/3));
        }
        final Button btnPos = printPreviewAlert.getButton(DialogInterface.BUTTON_POSITIVE);
        final Button btnNeg = printPreviewAlert.getButton(DialogInterface.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(dp2px(85), dp2px(35));
        int margin = getResources().getDimensionPixelSize(R.dimen.dp_5);
        llParams.setMargins(margin, 0, margin, 0);
        llParams.gravity = Gravity.CENTER_HORIZONTAL;
        if(btnPos != null){
          ((LinearLayout) btnPos.getParent()).setGravity(Gravity.CENTER);
          btnPos.setLayoutParams(llParams);
          btnPos.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
              if(!isProcessOn() && isInternetConnected(context,true,false)){
                if(context.isPrinting) {showLongToast(R.string.progress_msg_printing); return;}
                //showProgressDialog(R.string.progress_msg_printing);
                boolean isPrintSuccess = context.print(context.getString(isThanClosure ? R.string.title_slip_closure : R.string.title_slip_billing), isThanClosure ? "" : model!=null?model.getEan():"", listPrintFields,length,unit);
                showLog("isPrintSuccess_isUploaded", isPrintSuccess + "_" + model.isUploaded());
                hideProgressDialog();
                if(isPrintSuccess && !model.isUploaded()) callUpdateAPI(length); //TODO integrate
                if(isPrintSuccess) dialog.dismiss();
              }
            }
          });
        }
        if(btnNeg != null){
          ((LinearLayout) btnNeg.getParent()).setGravity(Gravity.CENTER);
          btnNeg.setLayoutParams(llParams);
        }
      }
    });
    printPreviewAlert.setOnDismissListener(new DialogInterface.OnDismissListener(){
      @Override
      public void onDismiss(DialogInterface dialog){
        printPreviewAlert=null;
      }
    });
    printPreviewAlert.show();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_PRODUCT_INFO:
        case URLConstants.GET_PRODUCT_INFO_BY_SKU:
        case URLConstants.GET_PRODUCT_INFO_THAN:
          if(isSuccess){
            ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
            if(productModel != null){
              model = productModel;
              binding.pdvThan.setProductModel(productModel, !chkNull(productModel.getZone(), AppConstants.DEFAULT_NO_VALUE).equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
              //binding.pdvThan.setFragmentButton(this, R.string.tab_check_availability);
              toggleUI();
              //TODO show Dialog/Field for Entering Cut Length (No Pick Operation for this)
              //OR Dialog with PDV + Cut Length Field
              //OR toggleUI();
              successBeep();
            }
          }
          break;
        case URLConstants.UPLOAD_CUTTING_THAN:
        case URLConstants.UPLOAD_CLOSURE_THAN:
          if(isSuccess){
            //TODO display dialog add model to List & Re-Show Previous Screen
            final String message =  extractString(jsonResponse,ParamConstants.MESSAGE,"");
            if(isNonEmpty(message)) context.showCustomSuccessDialog(message);
            //OR toggleUI(); (0 = StartBtn + List, 1 =  PDV + Enter Length Field)
            if(isThanCutting) {
              model.setLengthCutting(extractString(jsonRequest,ParamConstants.LENGTH_CUTTING,""));
              model.setUploaded(true);
            }
            if(isThanClosure) {
              model.setLengthClosure(extractString(jsonRequest,ParamConstants.LENGTH_CLOSURE,""));
              model.setClosed(true);
              model.setUploaded(true);
            }
            /*if(listProducts.contains(model))
              listProducts.remove(model);*/
            listProducts.add(0,model);
            updateLists();
            toggleUI();
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}