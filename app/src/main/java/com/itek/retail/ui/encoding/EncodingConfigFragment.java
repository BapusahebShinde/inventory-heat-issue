package com.itek.retail.ui.encoding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.R;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentEncodingConfigBinding;

/**
 * The Encoding config fragment.
 */
public class EncodingConfigFragment extends CommonFragment implements View.OnClickListener{
  
  private EncodingConfigViewModel mViewModel;
  private FragmentEncodingConfigBinding binding;
  
  /**
   * Instantiates a new Encoding config fragment.
   */
  public EncodingConfigFragment(){
    // Required empty public constructor
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(EncodingConfigViewModel.class);
    binding = FragmentEncodingConfigBinding.inflate(inflater, container, false);
    
    binding.seekEncodingStartTarget.setThumb(getResources().getDrawable(R.drawable.ic_target));
    
    binding.btnPurposeNewstore.setOnClickListener(this);
    binding.btnPurposeDsd.setOnClickListener(this);
    binding.btnPurposeCsd.setOnClickListener(this);
    selectButtonClick(binding.llEncodingConfigPurpose, SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.ENCODING_PURPOSE, binding.btnPurposeNewstore.getText().toString()));
    
    binding.btnConfigEan.setOnClickListener(this);
    binding.btnConfigNonstd.setOnClickListener(this);
    binding.btnConfigBoth.setOnClickListener(this);
    selectButtonClick(binding.llEncodingConfigEantype, SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.EAN_TYPE, binding.btnConfigEan.getText().toString()));
    
    binding.btnConfigRelationOneToOne.setOnClickListener(this);
    binding.btnConfigRelationOneToMany.setOnClickListener(this);
    selectButtonClick(binding.llEncodingConfigRelation, SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.ENCODING_RELATION_TYPE, AppConstants.ENCODE_TYPE_ONE));
    
    binding.btnConfigTypeBarcodeRfid.setOnClickListener(this);
    binding.btnConfigTypeBarcodeBarcodeRfid.setOnClickListener(this);
    selectButtonClick(binding.llEncodingConfigBarcoderfidtype, SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.ENCODING_BARCODE_RFID_TYPE, AppConstants.ENCODE_TYPE_BARCODE_RFID));
    
    binding.seekEncodingStartTarget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
        //code here
      }
      
      @Override
      public void onStartTrackingTouch(SeekBar seekBar){
        /*Empty Method (Default Overridden)*/
      }
      
      @Override
      public void onStopTrackingTouch(SeekBar seekBar){
        /*Empty Method (Default Overridden)*/
      }
    });
    
    return binding.getRoot();
  }
  
  @Override
  public void onClick(View v){
    selectButtonClick((LinearLayout) v.getParent(), ((Button) v).getText().toString());
  }
  
  /**
   * Select button click.
   *
   * @param ll     the ll
   * @param btnStr the btn str
   */
  public void selectButtonClick(LinearLayout ll, String btnStr){
    btnStr = btnStr.replaceFirst(" to ", "-");
    showLog("btnstr:", btnStr);
    if(ll != null && ll.getVisibility() == View.VISIBLE && ll.getChildCount() > 0){
      for(int i = 0; i < ll.getChildCount(); i++)
        if(ll.getChildAt(i) instanceof Button){
          final Button b = (Button) ll.getChildAt(i);
          final String btnString = (b.getTag() != null && b.getTag() instanceof String ? b.getTag().toString() : b.getText().toString()).replaceFirst(" to ", "-").trim();
          showLog("b:", b.getText().toString());
          final boolean isClicked = btnString.equalsIgnoreCase(btnStr.replaceFirst(" to ", "-").trim());
          b.setSelected(isClicked);
          if(isClicked){
            final SharedPrefManager.SharedPrefKeys key = ll.getId() == binding.llEncodingConfigPurpose.getId() ? SharedPrefManager.SharedPrefKeys.ENCODING_PURPOSE : ll.getId() == binding.llEncodingConfigEantype.getId() ? SharedPrefManager.SharedPrefKeys.EAN_TYPE : ll.getId() == binding.llEncodingConfigRelation.getId() ? SharedPrefManager.SharedPrefKeys.ENCODING_RELATION_TYPE : ll.getId() == binding.llEncodingConfigBarcoderfidtype.getId() ? SharedPrefManager.SharedPrefKeys.ENCODING_BARCODE_RFID_TYPE : null;
            SharedPrefManager.setString(key, btnString);
            showLog(key.toString(), SharedPrefManager.getString(key, "-"));
          }
        }
    }
  }
}
