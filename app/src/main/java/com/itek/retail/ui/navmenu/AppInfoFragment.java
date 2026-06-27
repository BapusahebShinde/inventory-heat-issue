package com.itek.retail.ui.navmenu;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.BuildConfig;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.ReleaseLogs;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentAppInfoBinding;
import com.itek.retail.ui.home.MainActivity;

/**
 * The App info fragment.
 */
public class AppInfoFragment extends CommonFragment{
  
  private AppInfoViewModel mViewModel;
  private FragmentAppInfoBinding binding;
  
  /**
   * Instantiates a new App info fragment.
   */
  public AppInfoFragment(){
    
    // Required empty public constructor
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    context.invalidateOptionsMenu();
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(AppInfoViewModel.class);
    binding = FragmentAppInfoBinding.inflate(inflater, container, false);
    setText(binding.txtAppInfoImei, R.string.txt_device_id, SharedPrefManager.getIMEI());
    setText(binding.txtAppInfoMacId, R.string.txt_mac_id, SharedPrefManager.getMACAddress());
    setText(binding.txtAppInfoIp, R.string.txt_ip_address, AppCommonMethods.getIPAddress(context));
    setText(binding.txtAppInfoVersion, R.string.txt_app_version, BuildConfig.IS_DIAGNOSTIC_BUILD ? "Diagnostic Build - Version " + BuildConfig.VERSION_NAME : SharedPrefManager.getAppVersion());
    setText(binding.txtAppInfoDeviceType, R.string.txt_device_type, "" + SharedPrefManager.getDeviceType().getValue());
    setText(binding.txtAppInfoReaderSdkVersion, R.string.txt_reader_sdk_version, SharedPrefManager.getReaderSDKVersion());
    
    setText(binding.txtAppInfoDeviceSerial, R.string.txt_device_serial, chkNull(chkNull(Build.SERIAL, "").replaceFirst("(?i)(" + AppConstants.UNKNOWN + ")", ""),SharedPrefManager.getString(ParamConstants.DEVICE_SERIAL,"")));
    setText(binding.txtAppInfoInstallDate, R.string.txt_app_install_date, SharedPrefManager.getInstallDate());
    setText(binding.txtAppInfoReleaseDate, R.string.txt_app_release_date, ReleaseLogs.RELEASE_DATE);
    setText(binding.txtAppInfoReleaseNote, R.string.txt_app_release_notes, ReleaseLogs.RELEASE_NOTES);
    
    binding.llAppInfoExtra.setVisibility(isDebugApp || BuildConfig.IS_DIAGNOSTIC_BUILD ? View.VISIBLE : View.GONE);
    if(BuildConfig.IS_DIAGNOSTIC_BUILD){
      setText("Build : %s", "Diagnostic Build");
      setText("Version : %s", BuildConfig.VERSION_NAME);
      setText("Package : %s", BuildConfig.APPLICATION_ID);
    }
    if(isDebugApp){
      setText(R.string.txt_server_url, SharedPrefManager.getServerUrl());
      setText(R.string.txt_device_manufacturer, Build.MANUFACTURER);
      setText(R.string.txt_device_model, chkNull(SharedPrefManager.getDeviceModel(), Build.MODEL));
      setText("CPU : %s", chkNull(Build.CPU_ABI, "") + " " + chkNull(Build.CPU_ABI2, ""));
      setText("ID : %s", chkNull(Build.ID, ""));
      setText("PRODUCT : %s", chkNull(Build.PRODUCT, ""));
      setText("TYPE : %s", chkNull(Build.TYPE, ""));
      setText("BOARD : %s", chkNull(Build.BOARD, ""));
      setText("BOOTLOADER : %s", chkNull(Build.BOOTLOADER, ""));
      setText("TAGS : %s", chkNull(Build.TAGS, ""));
      setText("DEVICE : %s", chkNull(Build.DEVICE, ""));
      setText("HARDWARE : %s", chkNull(Build.HARDWARE, ""));
      setText("HOST : %s", chkNull(Build.HOST, ""));
      setText("Radio : %s", chkNull(Build.getRadioVersion(), ""));
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        setText("SKU : %s", chkNull(Build.SKU, ""));
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        setText("SUPPORTED_ABIS : %s", chkNull(String.valueOf(Build.SUPPORTED_ABIS), ""));
      
      //Code for checking CPU/Processor Info (commented)
      /*try {
        String[] DATA = {"/system/bin/cat", "/proc/cpuinfo"};
        ProcessBuilder processBuilder = new ProcessBuilder(DATA);
        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        byte[] byteArry = new byte[1024];
        String output = "";
        while (inputStream.read(byteArry) != -1) {
          output = output + new String(byteArry);
        }
        inputStream.close();
    
        setText("CPU_INFO:\n%s", output);
    
      } catch (Exception ex) {
        ex.printStackTrace();
      }*/
    }
    
    return binding.getRoot();
  }
  
  private void setText(final TextView textView, @StringRes final int strId, final String textValue){
    if(strId != 0) setText(textView, getString(strId), textValue);
    else if(textView != null) textView.setVisibility(View.GONE);
  }
  
  private void setText(@StringRes final int strId, final String textValue){
    if(strId != 0) setText(getString(strId), textValue);
  }
  
  private void setText(final String label, final String textValue){
    setText(null, label, textValue);
  }
  
  private void setText(TextView textView, final String label, final String textValue){
    if(textView == null && isNonEmpty(label) && isNonEmpty(textValue)){
      final int padding = getResources().getDimensionPixelOffset(R.dimen.dp_10);
      textView = new TextView(context);
      textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
      textView.setPadding(padding, padding, padding, padding);
      textView.setGravity(Gravity.CENTER_HORIZONTAL);
      textView.setBackgroundResource(R.drawable.border_bottom);
      context.setTextAppearance(textView, R.style.TextStyleSubHeader);
      binding.llAppInfoExtra.addView(textView);
    }
    if(textView != null){
      final boolean isNonEmpty = isNonEmpty(label) && isNonEmpty(textValue);
      if(isNonEmpty) textView.setText(String.format(label, textValue));
      textView.setVisibility(isNonEmpty ? View.VISIBLE : View.GONE);
    }
  }
  
  @Override
  public void onDetach(){
    context.invalidateOptionsMenu();
    super.onDetach();
  }
  
  @Override
  public void onBackPressed(){
    ((MainActivity) context).setUpHome();
    super.onBackPressed();
  }
}
