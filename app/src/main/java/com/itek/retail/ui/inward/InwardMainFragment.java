package com.itek.retail.ui.inward;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.toTitleCase;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.FragmentInwardMainBinding;
import com.itek.retail.ui.customviews.DashboardDataView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * The Inward main fragment.
 */
public class InwardMainFragment extends CommonFragment{
  
  private InwardMainViewModel mViewModel;
  private FragmentInwardMainBinding binding;
  private TripStatusDao tripStatusDao;
  private TripInventoryDao tripInventoryDao;
  
  /**
   * Instantiates a new Inward main fragment.
   */
  public InwardMainFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripStatusDao = AppDatabase.getDbInstance(context).TripStatusDao();
    tripInventoryDao = AppDatabase.getDbInstance(context).TripInventoryDao();
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(InwardMainViewModel.class);
    binding = FragmentInwardMainBinding.inflate(inflater, container, false);
    
    binding.dvInwMainTripsPending.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        final View btnInwGrn = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_INW_GRN);
        if(btnInwGrn != null) btnInwGrn.performClick();
      }
    });
    
    binding.dvInwMainHusPending.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        final View btnInwHu = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_INW_HU);
        if(btnInwHu != null) btnInwHu.performClick();
      }
    });
    
    binding.swipeLayout.setEnabled(AppCommonMethods.isSetInwOnline);
    binding.swipeLayout.setColorSchemeColors(context.getColorPrimaryDarkFromTheme());
    binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
      @Override
      public void onRefresh(){
        binding.swipeLayout.setRefreshing(false);
        callAPI();
      }
    });
    
    binding.header.imgConfigSync.setVisibility(AppCommonMethods.isSetInwOnline ? View.VISIBLE : View.GONE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_sync);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        callAPI();
      }
    });
    
    callAPI();
    return binding.getRoot();
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()) callAPI();
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    if(AppCommonMethods.isSetInwOnline){
      callWebService(URLConstants.GET_INWARD_DASHBOARD, new JSONObject(), getString(R.string.progress_msg_getting_data));
    }
    else{
      final int totalTrips = chkNull(tripStatusDao.getTripsAllCount(AppConstants.INWARD), 0);
      final int completeTrips = chkNull(tripStatusDao.getTripsCompletedCount(AppConstants.INWARD), 0);
      final int pendingTrips = chkNull(totalTrips - completeTrips, 0);
      
      if(binding != null && binding.dvInwMainTripsTotal != null)
        binding.dvInwMainTripsTotal.setText("" + totalTrips);
      if(binding != null && binding.dvInwMainTripsCompleted != null)
        binding.dvInwMainTripsCompleted.setText("" + completeTrips);
      if(binding != null && binding.dvInwMainTripsPending != null)
        binding.dvInwMainTripsPending.setText("" + pendingTrips);
      
      final int totalHus = chkNull(tripInventoryDao.getHUStatusAllCount(), 0);
      final int completeHUs = chkNull(tripInventoryDao.getHUStatusCompleteCount(), 0);
      final int pendingHus = chkNull(totalHus - completeHUs, 0);
      
      if(binding != null && binding.dvInwMainHusTotal != null)
        binding.dvInwMainHusTotal.setText("" + totalHus);
      if(binding != null && binding.dvInwMainHusCompleted != null)
        binding.dvInwMainHusCompleted.setText("" + completeHUs);
      if(binding != null && binding.dvInwMainHusPending != null)
        binding.dvInwMainHusPending.setText("" + pendingHus);
    }
  }
  
  /**
   * Set active users.
   *
   * @param activeUsers the active users
   */
  private void setActiveUsers(final int activeUsers){
    binding.header.flActiveDevices.setVisibility(AppCommonMethods.isSetInwOnline && activeUsers >= -1 ? View.VISIBLE : View.GONE);
    binding.header.btnActiveDevices.setSelected(true);
    binding.header.btnActiveDevices.setText(activeUsers >= 0 ? "" + activeUsers : "");
  }
  
  /**
   * Clear dashboard vals.
   */
  private void clearDashboardVals(){ clearDashboardVals(binding.llDashboardInw); }
  
  private void clearDashboardVals(LinearLayout ll){
    final String defNoVal = getString(R.string.default_no_value);
    if(ll.getChildCount() > 0){
      for(int i = 0; i < ll.getChildCount(); i++){
        final View child = ll.getChildAt(i);
        if(child != null && child instanceof DashboardDataView)
          ((DashboardDataView) child).setText(defNoVal);
        else if(child != null && !(child instanceof DashboardDataView) && child instanceof LinearLayout)
          clearDashboardVals((LinearLayout) child);
      }
    }
    else if(ll.getId() == binding.llDashboardInw.getId()){
      if(binding.dvInwMainTripsTotal != null) binding.dvInwMainTripsTotal.setText(defNoVal);
      if(binding.dvInwMainTripsPending != null) binding.dvInwMainTripsPending.setText(defNoVal);
      if(binding.dvInwMainTripsCompleted != null) binding.dvInwMainTripsCompleted.setText(defNoVal);
      if(binding.dvInwMainHusTotal != null) binding.dvInwMainHusTotal.setText(defNoVal);
      if(binding.dvInwMainHusPending != null) binding.dvInwMainHusPending.setText(defNoVal);
      if(binding.dvInwMainHusCompleted != null) binding.dvInwMainHusCompleted.setText(defNoVal);
    }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    try{
      switch(url){
        case URLConstants.GET_INWARD_DASHBOARD:
          if(isSuccess){
            final String defNoVal = getString(R.string.default_no_value);
            //New Code (Dynamic Dashboard) fully configurable Header, Label, Count & Order
            //Also Handles both JSON Array & JSON Object Responses for Dashboard
            final LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            llParams.weight = 1.0f;
            final JSONArray jsonItems = extractJSONArray(jsonResponse, ParamConstants.ITEMS, extractJSONArray(jsonResponse, ParamConstants.DASHBOARD_ITEMS, extractJSONArray(jsonResponse, ParamConstants.DATA, extractJSONArray(jsonResponse, ParamConstants.DASHBOARD_DATA))));
            if(isNonEmpty(jsonItems)){
              binding.llDashboardInw.removeAllViews();
              for(int i = 0; i < jsonItems.length(); i++){
                try{
                  final JSONObject jsonObjectOtw = jsonItems.get(i) != null && jsonItems.get(i) instanceof JSONObject ? (JSONObject) jsonItems.get(i) : null;
                  final LinearLayout llDashboardColumn = new LinearLayout(context);
                  llDashboardColumn.setOrientation(LinearLayout.VERTICAL);
                  llDashboardColumn.setLayoutParams(llParams);
                  llDashboardColumn.setBackgroundResource(i % 2 == 0 ? R.color.white : R.color.graywhite);
                  final TextView lblView = new TextView(context);
                  lblView.setGravity(Gravity.CENTER);
                  final int dp10 = context.getResources().getDimensionPixelSize(R.dimen.dp_8);//R.dimen.dp_10);
                  lblView.setPadding(0, dp10, 0, dp10);
                  lblView.setBackgroundResource(R.drawable.border_bottom);
                  context.setTextAppearance(lblView, R.style.TextStyleSubSubHeader);
                  showLog("padding", "" + lblView.getPaddingTop());
                  if(jsonObjectOtw != null){
                    showLog("jsonObjectOtw", jsonObjectOtw.toString());
                    final String header = AppCommonMethods.extractString(jsonObjectOtw, ParamConstants.HEADER, AppCommonMethods.extractString(jsonObjectOtw, ParamConstants.LABEL, ""));
                    if(isNonEmpty(header)) lblView.setText(header);
                    final Iterator<String> jsonObjectOtwKeys = jsonObjectOtw.keys();
                    while(jsonObjectOtwKeys.hasNext()){
                      final String type = AppCommonMethods.extractString(jsonObjectOtw, ParamConstants.DASHBOARD_VIEW_TYPE, jsonObjectOtwKeys.next());
                      final String count = AppCommonMethods.extractString(jsonObjectOtw, ParamConstants.COUNT, extractString(jsonObjectOtw, type, defNoVal));
                      if(AppConstants.DASHBOARD_VIEW_TYPES.contains(type.toLowerCase())){
                        final DashboardDataView dataView = new DashboardDataView(context);
                        dataView.setBackgroundResource(R.drawable.border_bottom);
                        dataView.setLabel(toTitleCase(type));
                        dataView.setText(count);
                        if(type.matches("(?i)(" + AppConstants.DASHBOARD_VIEW_TYPE_PENDING + "|" + AppConstants.DASHBOARD_VIEW_TYPE_TOTAL + ")"))
                          dataView.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v){
                              final View btn = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_OTW_PICK);
                              if(btn != null) btn.performClick();
                            }
                          });
                        llDashboardColumn.addView(dataView, llParams);
                      }
                    }
                  }
                  if(isNonEmpty(lblView.getText().toString()) && llDashboardColumn.getChildCount() > 0)
                    llDashboardColumn.addView(lblView, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                  showLog("llDashboardColumn", "" + llDashboardColumn.getChildCount());
                  binding.llDashboardInw.addView(llDashboardColumn, llParams);
                }
                catch(Exception e){
                  e.printStackTrace();
                }
              }
            }
            else{
              final Iterator<String> keys = jsonResponse.keys();
              if(keys != null && keys.hasNext()){
                binding.llDashboardInw.removeAllViews();
                int columnCount = 0;
                while(keys.hasNext()){
                  final String key = keys.next();
                  if(isNonEmpty(key)){
                    columnCount++;
                    final boolean is1stColumn = keys.hasNext();
                    final JSONArray jsonArrayOtw = extractJSONArray(jsonResponse, key);
                    final JSONObject jsonObjectOtw = extractJSONObject(jsonResponse, key);
                    final LinearLayout llDashboardColumn = new LinearLayout(context);
                    llDashboardColumn.setOrientation(LinearLayout.VERTICAL);
                    llDashboardColumn.setLayoutParams(llParams);
                    llDashboardColumn.setBackgroundResource(is1stColumn ? R.color.white : R.color.graywhite);
                    final TextView lblView = new TextView(context);
                    //lblView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    lblView.setGravity(Gravity.CENTER);
                    final int dp10 = context.getResources().getDimensionPixelSize(R.dimen.dp_8);//R.dimen.dp_10);
                    lblView.setPadding(0, dp10, 0, dp10);
                    lblView.setBackgroundResource(R.drawable.border_bottom);
                    context.setTextAppearance(lblView, R.style.TextStyleSubSubHeader);
                    //final TextView lblView = (TextView) getLayoutInflater().inflate(R.layout.view_dashboard_header_label,null,false);
                    //final TextView lblView = is1stColumn ? binding.lblOtwMainTrips : binding.lblOtwMainHus;
                    lblView.setText(toTitleCase(key));
                    showLog("padding", "" + lblView.getPaddingTop());
                    if(isNonEmpty(jsonArrayOtw)){
                      for(int i = 0; i < jsonArrayOtw.length(); i++){
                        final JSONObject tripDashboardItem = jsonArrayOtw.getJSONObject(i);
                        final String type = AppCommonMethods.extractString(tripDashboardItem, ParamConstants.DASHBOARD_VIEW_TYPE, "");
                        final String label = AppCommonMethods.extractString(tripDashboardItem, ParamConstants.LABEL, "");
                        final String count = AppCommonMethods.extractString(tripDashboardItem, ParamConstants.COUNT, "");
                        final String percent = AppCommonMethods.extractString(tripDashboardItem, ParamConstants.PERCENT, "");
                        final String percentLabel = AppCommonMethods.extractString(tripDashboardItem, ParamConstants.PERCENT_LABEL, "");
                        final Boolean isUpwardArrow = AppCommonMethods.extractBoolean(tripDashboardItem, ParamConstants.IS_UPWARD_ARROW);
                        if(AppConstants.DASHBOARD_VIEW_TYPES.contains(type.toLowerCase())){//matches("(?i)("+AppConstants.DASHBOARD_VIEW_TYPE_TOTAL+"|"+AppConstants.DASHBOARD_VIEW_TYPE_COMPLETED+"|"+AppConstants.DASHBOARD_VIEW_TYPE_PENDING+")")){
                          final DashboardDataView dataView = new DashboardDataView(context);
                          dataView.setBackgroundResource(R.drawable.border_bottom);
                          //final DashboardDataView dataView = type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_PENDING) ? keys.hasNext() ? binding.dvOtwMainListPending : binding.dvOtwMainHusPending : type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_COMPLETED) ? keys.hasNext() ? binding.dvOtwMainListCompleted : binding.dvOtwMainHusCompleted : keys.hasNext() ? binding.dvOtwMainListTotal : binding.dvOtwMainHusTotal;
                          dataView.setLabelTextPercent(type, label, count, percentLabel, percent, isUpwardArrow);
                          if(type.matches("(?i)(" + AppConstants.DASHBOARD_VIEW_TYPE_PENDING + "|" + AppConstants.DASHBOARD_VIEW_TYPE_TOTAL + ")"))
                            dataView.setOnClickListener(new View.OnClickListener(){
                              @Override
                              public void onClick(View v){
                                final View btn = binding.llSubHeader.llSubHeader.getChildAt(is1stColumn ? 0 : 1);
                                if(btn != null) btn.performClick();
                              }
                            });
                          llDashboardColumn.addView(dataView, llParams);
                        }
                      }
                    }
                    else if(jsonObjectOtw != null){
                      showLog("jsonObjectOtw", jsonObjectOtw.toString());
                      final String header = AppCommonMethods.extractString(jsonObjectOtw, ParamConstants.HEADER, AppCommonMethods.extractString(jsonObjectOtw, ParamConstants.LABEL, toTitleCase(key)));
                      if(isNonEmpty(header)) lblView.setText(header);
                      final Iterator<String> jsonObjectOtwKeys = jsonObjectOtw.keys();
                      while(jsonObjectOtwKeys.hasNext()){
                        final String type = AppCommonMethods.extractString(jsonObjectOtw, ParamConstants.DASHBOARD_VIEW_TYPE, jsonObjectOtwKeys.next());
                        final String count = AppCommonMethods.extractString(jsonObjectOtw, ParamConstants.COUNT, extractString(jsonObjectOtw, type, defNoVal));
                        if(AppConstants.DASHBOARD_VIEW_TYPES.contains(type.toLowerCase())){
                          final DashboardDataView dataView = new DashboardDataView(context);
                          //dataView.setLayoutParams(llParams);
                          dataView.setBackgroundResource(R.drawable.border_bottom);
                          //final DashboardDataView dataView = (DashboardDataView) getLayoutInflater().inflate(R.layout.view_dashboard_data,null);
                          //final DashboardDataView dataView = type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_PENDING) ? is1stColumn ? binding.dvOtwMainListPending : binding.dvOtwMainHusPending : type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_COMPLETED) ? is1stColumn ? binding.dvOtwMainListCompleted : binding.dvOtwMainHusCompleted : is1stColumn ? binding.dvOtwMainListTotal : binding.dvOtwMainHusTotal;
                          dataView.setLabel(toTitleCase(type));
                          dataView.setText(count);
                          if(type.matches("(?i)(" + AppConstants.DASHBOARD_VIEW_TYPE_PENDING + "|" + AppConstants.DASHBOARD_VIEW_TYPE_TOTAL + ")"))
                            dataView.setOnClickListener(new View.OnClickListener(){
                              @Override
                              public void onClick(View v){
                                final View btn = binding.llSubHeader.llSubHeader.getChildAt(is1stColumn ? 0 : 1);
                                if(btn != null) btn.performClick();
                              }
                            });
                          llDashboardColumn.addView(dataView, llParams);
                        }
                      }
                    }
                    if(isNonEmpty(lblView.getText().toString()) && llDashboardColumn.getChildCount() > 0)
                      llDashboardColumn.addView(lblView, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    showLog("llDashboardColumn", "" + llDashboardColumn.getChildCount());
                    binding.llDashboardInw.addView(llDashboardColumn, llParams);
                  }
                }
              }
              else clearDashboardVals();
            }
          }
          setActiveUsers(extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, -2));
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}