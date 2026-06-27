package com.itek.retail.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentTabDashboardBinding;

import java.util.HashMap;

/**
 * The Tab dashboard fragment.
 */
public class TabDashboardFragment extends CommonFragment{
  
  private FragmentTabDashboardBinding binding;
  
  /**
   * Instantiates a new Tab dashboard fragment.
   */
  public TabDashboardFragment(){/*Default/Empty Constructor*/}
  
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    binding = FragmentTabDashboardBinding.inflate(inflater, container, false);
    //setup WebView
    WebView webView = binding.webView;
    webView.setWebViewClient(new WebViewClient());
    webView.getSettings().setLoadsImagesAutomatically(true);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setAllowContentAccess(true);
    webView.getSettings().setAllowFileAccess(true);
    webView.getSettings().setAllowFileAccessFromFileURLs(true);
    webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
    webView.getSettings().setDatabaseEnabled(true);
    webView.getSettings().setBuiltInZoomControls(true);
    webView.getSettings().setDomStorageEnabled(true);
    webView.getSettings().setGeolocationEnabled(true);
    webView.getSettings().setGeolocationEnabled(true);
    webView.getSettings().setSupportZoom(true);
    webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    final String url = AppCommonMethods.isValidUrl(SharedPrefManager.getDashboardUrl()) ? SharedPrefManager.getDashboardUrl() : (SharedPrefManager.getServerUrl().replaceFirst(AppCommonMethods.SERVER_URL_APPEND_API, "") + SharedPrefManager.getDashboardUrl()).trim();
    showLog("url", url);
    HashMap<String, String> headers = new HashMap<>(0);
    headers.put(ParamConstants.HEADER_ACCEPT, ParamConstants.HEADER_ACCEPT_VAL);
    headers.put(ParamConstants.HEADER_CONTENT_TYPE, ParamConstants.HEADER_CONTENT_TYPE_VAL);
    headers.put(ParamConstants.HEADER_AUTHORIZATION, SharedPrefManager.getAccessToken());
    headers.put(ParamConstants.TOKEN_GRANT_TYPE, ParamConstants.TOKEN_GRANT_TYPE_VAL);
    headers.put(ParamConstants.TOKEN_USER_NAME, !AppCommonMethods.isSetUserMgmt || AppCommonMethods.isStaticDebug() ? ParamConstants.TOKEN_USER_NAME_VAL : AppCommonMethods.chkNull(SharedPrefManager.getUserID(), ParamConstants.TOKEN_USER_NAME_VAL));
    headers.put(ParamConstants.TOKEN_PASSWORD, !AppCommonMethods.isSetUserMgmt || AppCommonMethods.isStaticDebug() ? ParamConstants.TOKEN_PASSWORD_VAL : AppCommonMethods.chkNull(SharedPrefManager.getPassword(), ParamConstants.TOKEN_PASSWORD_VAL));
    headers.put(ParamConstants.TOKEN_DEVICE_ID, SharedPrefManager.getIMEI());
    if(AppCommonMethods.isValidUrl(url)) webView.loadUrl(url, headers);
    return binding.getRoot();
  }
}