package com.itek.retail.ui.inward.tote;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isUploadSlider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.BarcodeScanFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentInwardToteMainBinding;
import com.itek.retail.ui.customviews.swipeButton.ProSwipeButtonVar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class InwardToteMainFragment extends BarcodeScanFragment {
  
  private FragmentInwardToteMainBinding binding;
  private boolean isEmptyToteInward = false;
  private boolean isEmptyToteOutward = false;
  private String totetype;
  private ArrayList<String> listEans = new ArrayList<>();
  private InwardToteViewModel mViewModel;

  /**
   * Instantiates a new Outward Tote Main Fragment.
   */
  public InwardToteMainFragment(){
    // Required empty public constructor
  }
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(InwardToteViewModel.class);
    binding = FragmentInwardToteMainBinding.inflate(inflater, container, false);
    
    isEmptyToteOutward = getArguments() != null && getArguments().containsKey(ParamConstants.IS_EMPTY_TOTE_OUTWARD) && getArguments().getBoolean(ParamConstants.IS_EMPTY_TOTE_OUTWARD, false);
    isEmptyToteInward = getArguments() != null && getArguments().containsKey(ParamConstants.IS_EMPTY_TOTE_INWARD) && getArguments().getBoolean(ParamConstants.IS_EMPTY_TOTE_INWARD, true);
    totetype = isEmptyToteOutward? "EMPTYTOTEOUTWARD" : "EMPTYTOTEINWARD";
    showLog("toteType", totetype);
    //final String savedChallanNo = extractString(getArguments(), ParamConstants.CHALLAN_NO, extractString(getArguments(), ParamConstants.DELIVERY_CHALLAN, SharedPrefManager.getString(ParamConstants.CHALLAN_NO)));
    
    /*if(isNonEmpty(savedChallanNo) && isNonEmpty(SharedPrefManager.getStringArrayList(savedChallanNo))){
      context.loadFragment(new InwardToteStartFragment(), getArguments());
    }*/

    if(isEmptyToteOutward){
      binding.ivDelvChallan.setLabel(getString(R.string.lbl_otw_ref_number));
      binding.ivDelvChallan.setHint(getString(R.string.hint_otw_ref_number));
    }
    
    binding.btnProceed.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick && binding.ivDelvChallan.validate()){
          //TypeEans selType = isEmptyToteOutward ? (TypeEans) binding.spinOutwardToteType.getSelectedObject() : null;
          final String challanNo = binding.ivDelvChallan.getText().toString().trim();
          Bundle args = chkNull(getArguments(), new Bundle());
          args.putString(ParamConstants.CHALLAN_NO, challanNo);
          try{
            JSONObject requestParams = new JSONObject();
            requestParams.put(ParamConstants.IS_EMPTY_TOTE_INWARD, isEmptyToteInward);
            requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
            requestParams.put(ParamConstants.TOTE_TYPE, totetype);
            requestParams.put(ParamConstants.CHALLAN_NO, challanNo);
            callWebService(URLConstants.GET_CHALLAN_DETAILS, requestParams, args, false, getString(R.string.progress_msg_getting_data));
          }
          catch(Exception e){
          
          }
        }
      }
    });

    setInputView(binding.ivDelvChallan,binding.btnProceed);
    
    return binding.getRoot();
  }
  
  /**
   * Set upload constraints.
   *
   * @param root       the root
   * @param btnCloud   the btn cloud
   * @param btnSwipeUp the btn swipe up
   */
  public void setUploadConstraints(final ConstraintLayout root, final FloatingActionButton btnCloud, final ProSwipeButtonVar btnSwipeUp){
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(root);
    constraintSet.clear(btnCloud.getId(), isUploadSlider ? ConstraintSet.BOTTOM : ConstraintSet.TOP);
    constraintSet.connect(btnCloud.getId(), isUploadSlider ? ConstraintSet.TOP : ConstraintSet.BOTTOM, isUploadSlider ? btnSwipeUp.getId() : ConstraintSet.PARENT_ID, isUploadSlider ? ConstraintSet.TOP : ConstraintSet.BOTTOM);
    constraintSet.applyTo(root);
  }
  
  private ArrayList<String> getEanList(JSONArray jsonArrayEans) throws JSONException{
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
        case URLConstants.GET_CHALLAN_DETAILS:
          if(isSuccess && jsonResponse != null){
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
              hideProgressDialog();
              context.showCustomErrDialog(errMsg);
            }
            else{
              JSONObject data = extractJSONObject(jsonResponse, ParamConstants.DATA, jsonResponse);
              final String challanNo = extractString(jsonRequest, ParamConstants.CHALLAN_NO, extractString(data, ParamConstants.CHALLAN_NO, extractString(data, ParamConstants.DELIVERY_CHALLAN)));
              JSONArray jsonArrayEans = extractJSONArray(data, ParamConstants.INWARD_TOTE_EANS, extractJSONArray(data, ParamConstants.OUTWARD_TOTE_EANS));
              listEans = getEanList(jsonArrayEans);
              if(isNonEmpty(listEans)){
                SharedPrefManager.setString(ParamConstants.CHALLAN_NO, challanNo);
                SharedPrefManager.setStringArrayList(challanNo, listEans);
                SharedPrefManager.setStringArrayList(ParamConstants.LIST_TOTE_EANS, listEans);
                context.loadFragment(new InwardToteStartFragment(), args);
                if(binding != null && binding.ivDelvChallan != null)
                  binding.ivDelvChallan.setText("");
              }
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
