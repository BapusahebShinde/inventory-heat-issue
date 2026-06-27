package com.itek.retail.ui.encoding;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.itek.retail.R;
import com.itek.retail.adapter.EncodingHistoryAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.CustomTypefaceSpan;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.FragmentEncodingMainBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.productsearch.ProductDetailsFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Encoding main fragment.
 */
public class EncodingMainFragment extends CommonFragment{
  
  private EncodingMainViewModel mViewModel;
  private FragmentEncodingMainBinding binding;
  
  private List<EanQty> listEncHistoryEans = new ArrayList<>(0);
  
  /**
   * Instantiates a new Encoding main fragment.
   */
  public EncodingMainFragment(){
    // Required empty public constructor
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(EncodingMainViewModel.class);
    binding = FragmentEncodingMainBinding.inflate(inflater, container, false);
    
    showLog("Density", "" + getResources().getSystem().getDisplayMetrics().density);
    
    binding.ddvEncodeSession.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        final View btnEncodingStart = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_ENC_START);
        final View btnEncodingSSW = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_ENC_SSW);
        if(btnEncodingStart != null && btnEncodingStart.getVisibility() == View.VISIBLE && (btnEncodingSSW == null || btnEncodingSSW.getVisibility() != View.VISIBLE))
          btnEncodingStart.performClick();
        else if((btnEncodingStart == null || btnEncodingStart.getVisibility() != View.VISIBLE) && btnEncodingSSW != null && btnEncodingSSW.getVisibility() == View.VISIBLE)
          btnEncodingSSW.performClick();
        else if(btnEncodingStart != null && btnEncodingSSW != null){
          final RFIDSession session = ((MainActivity) context).getRfidViewModel().getCurrentSession(AppCommonMethods.SessionType.ENCODING);
          if(session != null && isNonEmpty(session.extras)){
            try{
              if(extractBoolean(new JSONObject(session.extras), ParamConstants.IS_BARCODE_BARCODE_RFID, false))
                btnEncodingSSW.performClick();
              else btnEncodingStart.performClick();
            }
            catch(Exception e){ }
          }
        }
      }
    });
    
    binding.swipeLayout.setColorSchemeColors(context.getColorPrimaryDarkFromTheme());
    binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
      @Override
      public void onRefresh(){
        binding.swipeLayout.setRefreshing(false);
        callAPI();
      }
    });
    
    binding.header.imgConfigSync.setVisibility(View.VISIBLE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_sync);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        callAPI();
      }
    });
    
    binding.listEncodingHistory.setAdapter(new EncodingHistoryAdapter((MainActivity) context, EncodingMainFragment.this, listEncHistoryEans));
    binding.listEncodingHistory.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
    
    //commented by Bhupen Morgaonkar on 26-09-2023 for 'On Demand Dashboard'
    //if(savedInstanceState == null) callAPI();
    
    //code for setting label
    final String msgInvRefresh = getString(R.string.msg_refresh_dashboard);
    int index = msgInvRefresh.indexOf("'");
    int indexEnd = msgInvRefresh.lastIndexOf("'") - 1;
    SpannableString ss = new SpannableString(msgInvRefresh.replaceAll("\'", ""));
    ss.setSpan(new CustomTypefaceSpan("", ResourcesCompat.getFont(context, R.font.font_awesome)), index, indexEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    ss.setSpan(new StyleSpan(Typeface.BOLD), indexEnd, indexEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    ss.setSpan(new RelativeSizeSpan(2f), indexEnd, indexEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    //ss.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.red)),indexEnd,indexEnd,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    binding.lblRefresh.setText(ss);
    
    //binding.lblRefresh.setText(HtmlCompat.fromHtml(getString(R.string.msg_refresh_inv_dashboard), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    return binding.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    AppDatabase.getInventoryDao(context).getAllInventoryDataSize(AppCommonMethods.SessionType.ENCODING.getValue()).observe(getViewLifecycleOwner(), new Observer<Integer>(){
      @Override
      public void onChanged(Integer size){
        binding.ddvEncodeSession.setText("" + chkNull(size, 0));
      }
    });
    updateSessionMenus();
  }
  
  private void updateSessionMenus(){
    final View btnEncodingStart = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_ENC_START);
    final View btnEncodingSSW = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_ENC_SSW);
    if(btnEncodingStart != null && btnEncodingSSW != null){
      final RFIDSession session = ((MainActivity) context).getRfidViewModel().getCurrentSession(AppCommonMethods.SessionType.ENCODING);
      if(session != null && isNonEmpty(session.extras)){
        try{
          final boolean isSSW = extractBoolean(new JSONObject(session.extras), ParamConstants.IS_BARCODE_BARCODE_RFID, false);
          btnEncodingStart.setVisibility(!isSSW ? View.VISIBLE : View.GONE);
          btnEncodingSSW.setVisibility(isSSW ? View.VISIBLE : View.GONE);
        }
        catch(Exception e){ }
      }
      else{
        btnEncodingStart.setVisibility(View.VISIBLE);
        btnEncodingSSW.setVisibility(View.VISIBLE);
      }
    }
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      updateSessionMenus();
      //commented by Bhupen Morgaonkar on 26-09-2023 for 'On Demand Dashboard'
      if(SharedPrefManager.getBoolean(ParamConstants.IS_DASHBOARD_AUTO_REFRESH, false)) callAPI();
    }
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    callWebService(URLConstants.GET_ENCODING_DASHBOARD, new JSONObject(), getString(R.string.progress_msg_getting_data), false, true);
  }
  
  /**
   * Set active users.
   *
   * @param activeUsers the active users
   */
  private void setActiveUsers(final int activeUsers){
    binding.header.flActiveDevices.setVisibility(activeUsers >= -1 ? View.VISIBLE : View.GONE);
    binding.header.btnActiveDevices.setSelected(true);
    binding.header.btnActiveDevices.setText(activeUsers >= 0 ? "" + activeUsers : "");
  }
  
  @Override
  public void onDestroyView(){
    AppDatabase.getInventoryDao(context).getAllInventoryDataSize(AppCommonMethods.SessionType.ENCODING.getValue()).removeObservers(getViewLifecycleOwner());
    super.onDestroyView();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_ENCODING_DASHBOARD:
          if(isSuccess){
            listEncHistoryEans.clear();
            JSONArray jsonEncHistory = AppCommonMethods.extractJSONArray(jsonResponse, ParamConstants.ENCODE_HISTORY_TAGS);
            if(isNonEmpty(jsonEncHistory)){
              new Handler().post(new Runnable(){
                @Override
                public void run(){
                  for(int i = 0; i < jsonEncHistory.length(); i++){
                    try{
                      EanQty eanQty = getGSON().fromJson(jsonEncHistory.getJSONObject(i).toString(), EanQty.class);
                      if(eanQty != null && isNonEmpty(eanQty.getEan()))
                        listEncHistoryEans.add(eanQty);
                    }
                    catch(Exception e){ e.printStackTrace(); }
                  }
                  hideProgressDialog();
                }
              });
            }
            else hideProgressDialog();
            if(binding != null && binding.ddvEncodeToday != null)
              binding.ddvEncodeToday.setText("" + extractString(jsonResponse, ParamConstants.ENCODE_TODAY, ""));
            if(binding != null && binding.listEncodingHistory != null && binding.listEncodingHistory.getAdapter() != null && binding.listEncodingHistory.getAdapter() instanceof RecyclerView.Adapter)
              ((RecyclerView.Adapter) binding.listEncodingHistory.getAdapter()).notifyDataSetChanged();
          }
          setActiveUsers(extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, -2));
          break;
        case URLConstants.GET_PRODUCT_INFO:
        case URLConstants.GET_PRODUCT_INFO_BY_SKU:
          if(isSuccess){
            ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
            if(productModel != null){
              if(isStaticDebug())
                productModel.setEan(extractString(jsonRequest, ParamConstants.EAN, ""));
              productModel.setSessionType(AppCommonMethods.SessionType.SEARCH.getValue());
              if(args == null) args = new Bundle();
              args.putSerializable(productModel.getClass().getSimpleName(), productModel);
              handleFragmentRedirection(new ProductDetailsFragment(), args);
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