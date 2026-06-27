package com.itek.retail.ui.encoding;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.databinding.AchievmentBoxBinding;
import com.itek.retail.ui.home.MainActivity;

/**
 * The Enc achieve fragment.
 */
public class EncodingAchieveFragment extends DialogFragment{
  
  MainActivity context;
  
  /**
   * Instantiates a new Enc achieve fragment.
   */
  public EncodingAchieveFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    if(getActivity() != null && getActivity() instanceof MainActivity)
      context = (MainActivity) getActivity();
  }
  
  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
    if(context == null && getActivity() != null && getActivity() instanceof MainActivity)
      context = (MainActivity) getActivity();
    final AlertDialog encodingAchievementDialog = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    encodingAchievementDialog.setCancelable(false);
    EncodingAchieveFragment.this.setCancelable(false);
    context.setAlertDialogCustomTitle(encodingAchievementDialog, R.string.title_achievements, R.drawable.ic_achieve);
    final AchievmentBoxBinding binding = AchievmentBoxBinding.inflate(LayoutInflater.from(context));
    if(binding != null && getArguments() != null){
      final String defNoVal = getString(R.string.default_no_value);
      binding.txtAchievementMaxTagsEncodingPerDay.setText(AppCommonMethods.extractString(getArguments(), AppConstants.AVG_TAGS_ENCODE_PER_DAY, defNoVal));
      binding.txtAchievementMaxTagsEncodingPerDay.setText(AppCommonMethods.extractString(getArguments(), AppConstants.MAX_TAGS_ENCODE_PER_DAY, defNoVal));
      binding.txtAchievementAvgEncodingTimePerTag.setText(AppCommonMethods.extractString(getArguments(), AppConstants.AVG_ENCODE_TIME_PER_TAG, defNoVal));
      binding.txtAchievementEncodingSpentHours.setText(AppCommonMethods.extractString(getArguments(), AppConstants.HOURS_SPENT, defNoVal));
    }
    else EncodingAchieveFragment.this.dismiss();
    encodingAchievementDialog.setView(binding.getRoot());
    return encodingAchievementDialog;
  }
  
  @Override
  public void onCancel(@NonNull DialogInterface dialog){
    super.onCancel(dialog);
  }
}