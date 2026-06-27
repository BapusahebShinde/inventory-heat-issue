package com.itek.retail.ui.search.productsearch;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.BarcodeScanFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentProductSearchBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The Product search fragment.
 */
public class ProductSearchFragment extends BarcodeScanFragment {
  
  private ProductSearchViewModel mViewModel;
  private FragmentProductSearchBinding binding;
  /**
   * Instantiates a new Product search fragment.
   */
  public ProductSearchFragment(){
    // Required empty public constructor
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ProductSearchViewModel.class);
    binding = FragmentProductSearchBinding.inflate(inflater, container, false);

    setInputView(binding.scanSearchProduct,binding.btnSearchProduct);

    binding.btnSearchProduct.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        //mainViewModel.getIsBarcodeOn().postValue(false);
        if(binding.scanSearchProduct.validate() && allowBtnClick){
          binding.scanSearchProduct.setIsViewControlEnabled(false, true);
          callAPI();
        }
      }
    });

    /*if(isStaticDebug()){
      binding.scanSearchProduct.setText("8901198204053");
      new Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
         binding.btnSearchProduct.performClick();
        }
      },50);
    }*/
    
    return binding.getRoot();
  }
  
  /**
   * Call api.
   */
  void callAPI(){
    if(!isTopInStack()) return;
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.EAN, binding.scanSearchProduct.getText().toString());
      jsonRequest.put(ParamConstants.EPC, "");
      jsonRequest.put(ParamConstants.TID, "");
      if(SharedPrefManager.getIsEANMapped())
        callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, getString(R.string.progress_msg_check_map_data));
      else{
        final String productInfoUrl = getProductInfoUrl();
        if(productInfoUrl.equalsIgnoreCase(URLConstants.GET_PRODUCT_INFO_BY_SKU)){
          JSONArray js = new JSONArray();
          js.put(jsonRequest.get(ParamConstants.EAN));
          jsonRequest.put(ParamConstants.ITEMS, js);
        }
        callWebService(productInfoUrl, jsonRequest, getString(R.string.progress_msg_getting_data), true);
      }
      
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_PRODUCT_INFO:
        case URLConstants.GET_PRODUCT_INFO_BY_SKU:
          if(AppCommonMethods.isShowOnlyErrorForProdSearchAPIError && !isSuccess) return;
          ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
          if(productModel != null){
            if(isStaticDebug())
              productModel.setEan(extractString(jsonRequest, ParamConstants.EAN, ""));
            productModel.setSessionType(AppCommonMethods.SessionType.SEARCH.getValue());
            if(args == null) args = getArguments();
            if(args == null) args = new Bundle();
            args.putSerializable(productModel.getClass().getSimpleName(), productModel);
            //mainViewModel.onPause();
            onPause();
            if(AppCommonMethods.isShowOnlyProdDetailsForProdSearchAPIError && !isSuccess)
              context.loadFragment(new ProductDetailsFragment(), args);
            else ((MainActivity) context).checkReaderConnection(new ProductSearchDetailsFragment(), args);
            binding.scanSearchProduct.setText("");
          }
          break;
        case URLConstants.GET_MAPPED_EAN:
          if(isSuccess){
            final String ean = extractString(jsonResponse, ParamConstants.EAN, extractString(jsonRequest, ParamConstants.EAN));
            final String mappedEan = extractString(jsonResponse, ParamConstants.MAPPED_EAN, "");
            if(args == null) args = getArguments();
            if(args == null) args = new Bundle();
            //use this Mapped EAN for Search/Encode
            args.putString(ParamConstants.MAPPED_EAN, mappedEan);
            final String productInfoUrl = getProductInfoUrl();
            if(productInfoUrl.equalsIgnoreCase(URLConstants.GET_PRODUCT_INFO_BY_SKU)){
              JSONArray js = new JSONArray();
              js.put(jsonRequest.get(ParamConstants.EAN));
              jsonRequest.put(ParamConstants.ITEMS, js);
            }
            callWebService(productInfoUrl, jsonRequest, args, getString(R.string.progress_msg_getting_data), false);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}