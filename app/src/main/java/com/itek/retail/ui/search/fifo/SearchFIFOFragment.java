package com.itek.retail.ui.search.fifo;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
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
import com.itek.retail.common.InsertDBFIFOs;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.FIFODao;
import com.itek.retail.databinding.FragmentFifoSearchBinding;
import com.itek.retail.model.FIFOModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The Product search fragment.
 */
public class SearchFIFOFragment extends BarcodeScanFragment {
  
  private SearchFIFOViewModel mViewModel;
  private FragmentFifoSearchBinding binding;

  /**
   * Instantiates a new Product search fragment.
   */
  public SearchFIFOFragment(){
    // Required empty public constructor
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(SearchFIFOViewModel.class);
    binding = FragmentFifoSearchBinding.inflate(inflater, container, false);

    final String ean=extractString(getArguments(),ParamConstants.EAN,"");

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

    if(isNonEmpty(ean)){
      binding.scanSearchProduct.setText(ean);
      binding.btnSearchProduct.performClick();
    }

    return binding.getRoot();
  }
  
  /**
   * Call api.
   */
  void callAPI(){
    //if(!isTopInStack()) return;
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
  
  public void redirectToSearch(final Bundle args, final String ean, final boolean isAllowDecode, final boolean isAllowDecodeOnPick, final FIFOModel oldest1){
    args.putString(ParamConstants.EAN, ean);
    final FIFOModel oldest = oldest1 == null? AppDatabase.getFIFODao(context).getOldestDateObj(ean) : oldest1;
    if (oldest == null) return;
    ProductModel pm = (ProductModel) extractSerializable(getArguments(), ProductModel.class);
    pm.setFifoDate(oldest.fifoDate);
    pm.setTotalQty(oldest.totalQty);
    pm.setQty(oldest.totalQty);
    pm.setZone(oldest.zone);
    pm.setZoneId(oldest.zoneId);
    pm.setEpc(oldest.epc);
    pm.setStockAge(oldest.age);
    args.putSerializable(ProductModel.class.getSimpleName(), pm);
    args.putString(ParamConstants.FIFO_DATE, oldest.fifoDate);
    args.putString(ParamConstants.ZONE_NAME, oldest.zone);
    args.putString(ParamConstants.ZONE_ID, oldest.zoneId);
    args.putSerializable(oldest.getClass().getSimpleName(), oldest);
    args.putBoolean(ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
    args.putBoolean(ParamConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecodeOnPick);
    ((MainActivity) context).checkReaderConnection(new SearchFIFOStartFragment(), args);
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_PRODUCT_INFO:
        case URLConstants.GET_PRODUCT_INFO_BY_SKU:
          ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
          if(productModel != null){
            if(isStaticDebug())
              productModel.setEan(extractString(jsonRequest, ParamConstants.EAN, ""));
            productModel.setSessionType(AppCommonMethods.SessionType.SEARCH.getValue());
            if(args == null) args = getArguments();
            if(args == null) args = new Bundle();
            args.putSerializable(productModel.getClass().getSimpleName(), productModel);
          }
          try{
            JSONObject requestParams = new JSONObject();
            requestParams.put(ParamConstants.EAN, productModel != null ? productModel.getEan() : extractString(jsonResponse, ParamConstants.EAN, extractString(jsonRequest, ParamConstants.EAN, "")));
            AppCommonMethods.showLog("requestParams", URLConstants.GET_FIFO_SEARCH_LIST + "_" + requestParams.toString());
            allowBtnClick = true;
            callWebService(URLConstants.GET_FIFO_SEARCH_LIST, requestParams, args, getString(R.string.progress_msg_getting_data));
          }
          catch(Exception e){ e.printStackTrace(); }
          break;
        case URLConstants.GET_FIFO_SEARCH_LIST:
          AppCommonMethods.showLog("responseCode", "" + responseCode);
          if(isSuccess){
            try{
              final String ean = extractString(jsonRequest, ParamConstants.EAN, "");
              final boolean isAllowDecode = extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE, false);
              final boolean isAllowDecodeOnPick = isAllowDecode && extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE_ON_PICK, false);
              final FIFODao fifoDao = AppDatabase.getFIFODao(context);
              if(isAllowDecode) context.saveTagWritePasswords(jsonResponse);
              JSONArray jsonFIFOProducts = extractJSONArray(jsonResponse, ParamConstants.PRODUCTS);
              if(jsonFIFOProducts != null){
                new InsertDBFIFOs((MainActivity) context, this, getSessionType().getValue(), url, jsonRequest, jsonResponse, args).execute(jsonFIFOProducts);
              }
              /*else if(isNonEmpty(jsonFIFOProducts)){
                ArrayList<FIFOModel> list = new ArrayList<FIFOModel>(0);
                //AppDatabase.getRIFDSessionDao(context).deleteAll(AppCommonMethods.SessionType.SEARCH_FIFO.getValue());
                fifoDao.deleteAll();
                for(int i = 0; i < jsonFIFOProducts.length(); i++){
                  showLog("FIFOProducts[" + i + "]", jsonFIFOProducts.getJSONObject(i).toString());
                  final FIFOModel fifoModel = getGSON().fromJson(jsonFIFOProducts.getJSONObject(i).toString(), FIFOModel.class);
                  if(fifoModel != null && isNonEmpty(fifoModel.ean) && isNonEmpty(fifoModel.epc) && isNonEmpty(fifoModel.fifoDate) && !fifoModel.fifoDate.trim().startsWith("0") && Integer.parseInt(chkZero(fifoModel.zoneId, "0")) > 0){
                    fifoModel.setFifoDate(fifoModel.getFifoDate());
                    list.add(fifoModel);
                  }
                }
                if(list.size() > 0){
                  fifoDao.insertAll(list);
                  redirectToSearch(args, ean, isAllowDecode, isAllowDecodeOnPick, fifoDao.getOldestDateObj(ean));
                }
              }*/
            }
            catch(Exception e){ e.printStackTrace(); }
          }
          binding.scanSearchProduct.setText("");
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