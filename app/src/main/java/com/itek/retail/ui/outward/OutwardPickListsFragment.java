package com.itek.retail.ui.outward;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.StoreOutwardDataAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.FragmentOutwardPickListsBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Outward trips fragment.
 */
public class OutwardPickListsFragment extends CommonFragment{
  
  private FragmentOutwardPickListsBinding binding;
  private TripStatusDao tripStatusDao;
  private String tripType = "";
  private List<TripStatus> dataList = new ArrayList<>(0);
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripStatusDao = AppDatabase.getDbInstance(this.context).TripStatusDao();
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    binding = FragmentOutwardPickListsBinding.inflate(inflater, container, false);
    
    binding.list.setAdapter(new StoreOutwardDataAdapter((MainActivity) context, this, dataList));
    binding.list.setLayoutManager(new LinearLayoutManager(context));
    
    tripType = AppCommonMethods.extractString(getArguments(), AppConstants.TRIP_TYPE, AppCommonMethods.isDebugApp ? "dc_to_store" : "");
    if(AppCommonMethods.isNullOrEmpty(tripType)) popBackStack();
    
    try{
      tripStatusDao.getTripTypeList(tripType, AppConstants.OUTWARD_PICK).observe(getViewLifecycleOwner(), storeOutwardDataList -> {
        if(storeOutwardDataList != null && storeOutwardDataList.size() > 0){
          if(dataList != null){
            dataList.clear();
            dataList.addAll(storeOutwardDataList);
            if(dataList.size() > 0){
              binding.llHeader.setVisibility(View.VISIBLE);
              binding.list.setVisibility(View.VISIBLE);
              binding.textNoData.setVisibility(View.GONE);
            }
            else{
              binding.llHeader.setVisibility(View.GONE);
              binding.list.setVisibility(View.GONE);
              binding.textNoData.setVisibility(View.VISIBLE);
            }
          }
          else{
            binding.llHeader.setVisibility(View.GONE);
            binding.list.setVisibility(View.GONE);
            binding.textNoData.setVisibility(View.VISIBLE);
          }
        }
        else{
          binding.llHeader.setVisibility(View.GONE);
          binding.list.setVisibility(View.GONE);
          binding.textNoData.setVisibility(View.VISIBLE);
        }
        if(binding != null && binding.list != null && binding.list.getAdapter() != null)
          ((RecyclerView.Adapter) binding.list.getAdapter()).notifyDataSetChanged();
      });
    }
    catch(Exception e){
      e.printStackTrace();
    }
    
    binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
          return;
        context.showCustomConfirmDialog(getString(R.string.msg_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            //apiCall(AppConstants.SESSION_ACTION_UPLOAD);
          }
        });
      }
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          binding.btnSwipeUpload.reset();
          context.showCustomConfirmDialog(getString(R.string.msg_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              //apiCall(AppConstants.SESSION_ACTION_UPLOAD);
            }
          });
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);
    
    return binding.getRoot();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    try{
      switch(url){
        case URLConstants.GET_OUTWARD_PICK_LIST_DETAILS:
          if(isSuccess && jsonResponse != null){
            JSONArray outwardProductsArray = extractJSONArray(jsonResponse, ParamConstants.OUTWARD_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(isNonEmpty(outwardProductsArray)){
              final ProductDao productDao = AppDatabase.getProductDao(context);
              productDao.deleteAllExcept();
              AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.OUTWARD.getValue());
              int insertCount = 0;
              for(int i = 0; i < outwardProductsArray.length(); i++){
                JSONObject outwardProduct = outwardProductsArray.getJSONObject(i);
                ProductModel productModel = outwardProduct != null ? getGSON().fromJson(outwardProduct.toString(), ProductModel.class) : null;
                if(productModel != null){
                  if(isDebugApp && isNullOrEmpty(productModel.getZone()))
                    productModel.setZone("BOH");
                  productModel.setSessionType(AppCommonMethods.SessionType.OUTWARD_PICK.getValue());
                  productModel.setItemImgUrl(extractString(outwardProduct, ParamConstants.IMG_URL, "").replaceAll(AppConstants.IMAGE_URL_REPLACE_REGEX, "").trim());
                  final JSONArray jsonZones = extractJSONArray(outwardProduct, ParamConstants.ZONES);
                  productModel.setTotalQty(productModel.getEanQty());
                  if(jsonZones != null && jsonZones.length() > 0){
                    for(int j = 0; j < jsonZones.length(); j++){
                      JSONObject zone = jsonZones.getJSONObject(j);
                      final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                      final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, extractInt(zone, ParamConstants.QTY, 0));
                      final String zoneName = extractString(zone, ParamConstants.ZONE_NAME, extractString(zone, ParamConstants.ZONE, Integer.parseInt(chkZero(zoneId, "0")) > 0 && chkNull(eanQty, 0) > 0 ? AppDatabase.getZoneDao(context).getZoneNameById(zoneId) : ""));
                      if(isNonEmpty(zoneName) && chkNull(eanQty, 0) > 0){
                        productModel.setZone(zoneName);
                        productModel.setZoneId(zoneId);
                        productModel.setEanQty(eanQty);
                        productDao.insert(productModel);
                        insertCount++;
                      }
                    }
                  }
                  else{
                    productDao.insert(productModel);
                    insertCount++;
                  }
                }
              }
              if(insertCount > 0){
                context.loadFragment(new OutwardPickListDetailsFragment(), args);
              }
              hideProgressDialog();
            }
            else hideProgressDialog();
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
  
  @Override
  public void onDestroyView(){
    super.onDestroyView();
    tripStatusDao.getTripTypeList(tripType, AppConstants.OUTWARD_PICK).removeObservers(getViewLifecycleOwner());
  }
}