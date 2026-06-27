package com.itek.retail.ui.outward.tote;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isSaveCompletedCartonAfterUpload;
import static com.itek.retail.common.AppCommonMethods.saveLimitForCompletedCartons;
import static com.itek.retail.common.AppCommonMethods.toTitleCase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.BarcodeScanFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.databinding.FragmentOutwardCartonScanBinding;
import com.itek.retail.model.OutwardTypes;
import com.itek.retail.ui.outward.offrange.OffRangeListFragment;

import java.util.ArrayList;
import java.util.List;

public class OutwardToteScanCartonFragment extends BarcodeScanFragment {
  
  private FragmentOutwardCartonScanBinding binding;
  private OutwardToteViewModel mViewModel;
  
  /**
   * Instantiates a new Outward Tote Main Fragment.
   */
  public OutwardToteScanCartonFragment(){
    // Required empty public constructor
  }
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OutwardToteViewModel.class);
    binding = FragmentOutwardCartonScanBinding.inflate(inflater, container, false);
    
    final boolean isEmptyToteOutward = getArguments() != null && getArguments().containsKey(ParamConstants.IS_EMPTY_TOTE_OUTWARD) && getArguments().getBoolean(ParamConstants.IS_EMPTY_TOTE_OUTWARD, false);
    final boolean isOffRange = getArguments() != null && getArguments().containsKey(ParamConstants.IS_OFF_RANGE) && getArguments().getBoolean(ParamConstants.IS_OFF_RANGE, false);
    final OutwardTypes outType = (OutwardTypes) extractSerializable(getArguments(), OutwardTypes.class);
    final String outwardType = outType != null ? outType.getName() : extractString(getArguments(), ParamConstants.OUTWARD_TOTE_TYPE, extractString(getArguments(), ParamConstants.TYPE));
    final String batchId = extractString(getArguments(), ParamConstants.BATCH_ID, extractString(getArguments(), ParamConstants.BATCH));

    setInputView(binding.edtCartonNumber,binding.btnGo);
    
    binding.btnGo.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick && binding.edtCartonNumber.validate()){
          final String cartonNo = binding.edtCartonNumber.getText().toString().toUpperCase().trim();
          ArrayList<String> listCompletedCartons = SharedPrefManager.getStringArrayList(batchId + ParamConstants.COMPLETED_CARTONS, new ArrayList<>(0));
          if(isOffRange && AppDatabase.getUploadInventoryDao(context).getCartonCountFromBatchId(batchId) >= AppCommonMethods.saveLimitForCompletedCartons){
            if(isSaveCompletedCartonAfterUpload){
              final UploadInventoryDao uploadInventoryDao= AppDatabase.getUploadInventoryDao(context);
              final int batchCartons = uploadInventoryDao.getCartonCountFromBatchId(batchId);
              List<String> listUploadedCartons = uploadInventoryDao.getUploadedCartonsFromBatchId(batchId);
              if(isNonEmpty(listUploadedCartons)){
                if(listUploadedCartons.size() <= (batchCartons - saveLimitForCompletedCartons))
                  uploadInventoryDao.deleteUploaded();
                else
                  uploadInventoryDao.deleteUploaded(batchId,listUploadedCartons.subList(0,(batchCartons - saveLimitForCompletedCartons)+1));
                
                if(uploadInventoryDao.getCartonCountFromBatchId(batchId)<AppCommonMethods.saveLimitForCompletedCartons){
                  Bundle args = chkNull(getArguments(), new Bundle());
                  args.putString(ParamConstants.CARTON_NUM, cartonNo);
                  AppDatabase.getProductDao(context).resetProducts(AppCommonMethods.SessionType.OFF_RANGE.getValue());
                  context.loadFragment(new OffRangeListFragment(), args);
                  binding.edtCartonNumber.setText("");
                  return;
                }
              }
            }
            final String errLbl = binding.edtCartonNumber.getErrLbl().replaceAll("Please ", "").replaceAll("Enter ", "").trim().toLowerCase();
            context.showCustomErrDialog(String.format(getString(R.string.msg_limit_reached), toTitleCase(errLbl)));
          }
          else if(listCompletedCartons.contains(cartonNo)){
            context.showCustomErrDialog(String.format(getString(R.string.msg_completed_carton),cartonNo));
//            context.showCustomMsgDialog(String.format(getString(R.string.msg_completed_carton),cartonNo), false, new DialogInterface.OnClickListener(){
//              @Override
//              public void onClick(DialogInterface dialog, int which){
//                binding.edtCartonNumber.setText("");
//              }
//            });
          }
          else{
            Bundle args = chkNull(getArguments(), new Bundle());
            args.putString(ParamConstants.CARTON_NUM, cartonNo);
            if(isEmptyToteOutward && outwardType.matches("(?i)^.*(Empty|Tote).*$"))
              context.loadFragment(new OutwardToteStartFragment(), args);
            if(isOffRange && outwardType.matches("(?i)^.*(Range|OffRange|OFRNG).*$")){
              AppDatabase.getProductDao(context).resetProducts(AppCommonMethods.SessionType.OFF_RANGE.getValue());
              context.loadFragment(new OffRangeListFragment(), args);
            }
            binding.edtCartonNumber.setText("");
          }
        }
      }
    });

    return binding.getRoot();
  }
}
