package com.itek.retail.ui.outward.tote;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.BarcodeScanFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.databinding.FragmentInwardGrnHuScanBinding;
import com.itek.retail.model.TripInventory;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.inward.grn.InwardGrnHuScanViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * The Inward grn hu scan fragment.
 */
public class OutwardToteDCFragment extends BarcodeScanFragment {

  public TripStatus tripStatus;
  String tripNumber = "";
  String huNumber = "";
  private InwardGrnHuScanViewModel mViewModel;

  private ArrayList<String> listToteEans = new ArrayList<>();
  private FragmentInwardGrnHuScanBinding binding;
  private TripInventoryDao tripInventoryDao;


  /**
   * Instantiates a new Inward grn hu scan fragment.
   */
  public OutwardToteDCFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripInventoryDao = AppDatabase.getTripInventoryDao(context);
    callAPI();
  }

  /**
   * Call api.
   */
  private void callAPI(){
    try{
      JSONObject requestParams = new JSONObject();
      requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, true);
      callWebService(URLConstants.GET_OUTWARD_TOTE_EANS,requestParams, getString(R.string.progress_msg_getting_data),true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(InwardGrnHuScanViewModel.class);
    binding = FragmentInwardGrnHuScanBinding.inflate(inflater, container, false);
    
    /*if(getArguments() != null){
      Object obj = extractSerializable(getArguments(), TripStatus.class);
      tripStatus = obj != null && obj instanceof TripStatus ? (TripStatus) obj : null;
      showLog("tripStatus", String.valueOf(tripStatus));
      tripNumber = tripStatus != null ? tripStatus.getTripNumber() : extractString(getArguments(), AppConstants.TRIP_NUMBER, "");
      binding.edtHuNumber.setText(AppCommonMethods.isSetInwOnline ? extractString(getArguments(), AppConstants.HU_NUMBER, "") : "");
      if(binding.edtHuNumber.getText().length() > 0) binding.btnHUGo.performClick();
    }*/
    binding.edtHuNumber.setHint(String.format(getString(R.string.scan__),getString(R.string.lbl_lpn)));
    binding.edtHuNumber.setLabel(String.format(getString(R.string.lbl__number),getString(R.string.lbl_lpn)));
    //binding.edtHuNumber.setValidationRegex("(?i)(^ET.*)");

    //showLog("tripNumber", tripNumber);
    binding.txtTripNumber.setText(HtmlCompat.fromHtml(isNonEmpty(tripNumber) ? String.format(getString(R.string.txt_trip), tripNumber) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtTripNumber.setVisibility(isNonEmpty(tripNumber) ? View.VISIBLE : View.GONE);
    
    binding.btnHUGo.setOnClickListener(v -> {
      if(binding.edtHuNumber.validate()){
        binding.edtHuNumber.setIsViewControlEnabled(false, true);
        final String huNumber = binding.edtHuNumber.getText().toString().trim().toUpperCase();
          try{
            JSONObject requestParams = new JSONObject();
            requestParams.put("lpnNo", huNumber);
            callWebService(URLConstants.GET_LPN_STATUS_FOR_OUTWARD, requestParams, getString(R.string.progress_msg_getting_data));
          }
          catch(Exception e){ e.printStackTrace(); }
      }
      //else binding.edtHuNumber.validate();
    });

    setInputView(binding.edtHuNumber,binding.btnHUGo);
    
    return binding.getRoot();
  }
  
  /**
   * Clear barcode.
   */
  public void clearBarcode(){
    binding.edtHuNumber.setText("");
    //binding.edtHuNumber.setIsViewControlEnabled(true);
  }

  private ArrayList<String> getEanList(JSONArray jsonArrayEans) throws JSONException {
    ArrayList<String> listEans = new ArrayList<>();
    if(isNonEmpty(jsonArrayEans)){
      for(int i = 0; i < jsonArrayEans.length(); i++){
        Object obj = jsonArrayEans.get(i);
        if(obj instanceof String && isNonEmpty((String) obj)){
          listEans.add(obj.toString());
        }
        else if(obj instanceof JSONObject){
          JSONObject jObj = (JSONObject) obj;
          final String ean = extractString(jObj, ParamConstants.EAN, extractString(jObj, ParamConstants.SKU_ID, extractString(jObj, ParamConstants.OUTWARD_TOTE_EAN)));
          if(isNonEmpty(ean)) listEans.add(ean);
        }
      }
    }
    return listEans;
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_OUTWARD_TOTE_EANS:
          if(isSuccess && jsonResponse != null){
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
              hideProgressDialog();
              context.showCustomErrDialog(errMsg);
            }
            else{
              JSONArray jsonArrayData = extractJSONArray(jsonResponse,ParamConstants.DATA);
              JSONObject data = isNonEmpty(jsonArrayData) && jsonArrayData.get(0) instanceof JSONObject?jsonArrayData.getJSONObject(0):extractJSONObject(jsonResponse, ParamConstants.DATA, jsonResponse);
              JSONArray jsonArrayEans = extractJSONArray(data, ParamConstants.OUTWARD_TOTE_EANS);
              listToteEans = getEanList(jsonArrayEans);
              //TODO ADD LoG
              SharedPrefManager.setStringArrayList(ParamConstants.LIST_TOTE_EANS,listToteEans);
            }
          }
          else {
            ArrayList<String> toteEans = SharedPrefManager.getStringArrayList(ParamConstants.LIST_TOTE_EANS,listToteEans);
            if(toteEans.isEmpty()) toteEans.add("2050029908762");
            if(listToteEans.isEmpty()) listToteEans.addAll(toteEans);
          }
        break;
        case URLConstants.GET_LPN_STATUS_FOR_OUTWARD:
          showLog("inward", "info:" + jsonResponse);
          if(isSuccess){
            final JSONObject data0 = extractJSONObject(jsonResponse,ParamConstants.DATA,jsonResponse);
            final JSONObject data = extractJSONObject(data0,ParamConstants.DATA,data0);

            String dcCode = extractString(data,ParamConstants.DC_CODE,"");
            String lpnNo = extractString(data,ParamConstants.LPN_NO,"");
            int expectedQty = extractInt(data,ParamConstants.EPC_EXPECTED_QTY,0);
            String batchId = extractString(data,ParamConstants.BATCH_ID,"");
            Boolean isCompleted = extractBoolean(data,ParamConstants.IS_COMPLETED,false);

            if(isCompleted){
              context.showCustomErrDialog(String.format(getString(R.string.err__already_completed),getString(R.string.lbl_lpn)+" ("+lpnNo+")"));
              return;
            }

            args = args == null ? new Bundle() : args;
            args.putString(ParamConstants.DC_CODE, dcCode);
            args.putString(ParamConstants.LPN_NO, lpnNo);
            args.putInt(ParamConstants.EXPECTED_QTY, expectedQty);
            args.putString(ParamConstants.BATCH_ID, batchId);
            if(listToteEans.isEmpty()){
              ArrayList<String> toteEans = SharedPrefManager.getStringArrayList(ParamConstants.LIST_TOTE_EANS);
              if(toteEans.isEmpty()) toteEans.add("2050029908762");
              listToteEans.addAll(toteEans);
            }
            //TEMP Code
            if (isDebugApp && listToteEans.size() > 0){
              listToteEans.clear();
              listToteEans.add("2050029908762");
              //listToteEans.add("2061055669336");
            }
            args.putStringArrayList(ParamConstants.LIST_TOTE_EANS,listToteEans);

            for(String toteEan:listToteEans) {
              //TODO insert trip"
              TripInventory tripInventory = new TripInventory("-","-",lpnNo);
              tripInventory.ean=toteEan;
              tripInventory.huQty=expectedQty;
              tripInventory.isServerEntry=true;
              if (listToteEans.size() == 1) tripInventory.eanQty = expectedQty;
              //TEMP Code
              //if (listToteEans.size() == 2) tripInventory.eanQty = expectedQty / 2;
              tripInventoryDao.insertTripInventoryData(tripInventory);
            }

            showLog("args",args.toString());
            showLog("listToteEans", listToteEans.toString());
            //TODO Put Hardcoded LPN If API Fails
            context.loadFragment(new OutwardToteDCStartFragment(), args);
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