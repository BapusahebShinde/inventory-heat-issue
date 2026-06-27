package com.itek.retail.ui.resetpassword;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppConstants.REGEX_PASSWORD;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentResetPasswordBinding;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONObject;

/**
 * The Reset password fragment.
 */
public class ResetPasswordFragment extends CommonFragment{
  
  private static final boolean useOldPassword = true;
  private static final boolean validateOldPassword = useOldPassword && false;
  private static final boolean isAllowSameOldAndNewPassword = validateOldPassword && false;
  ResetPasswordViewModel mViewModel;
  private FragmentResetPasswordBinding binding;
  final TextWatcher clear = new TextWatcher(){
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
    
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
    
    @Override
    public void afterTextChanged(Editable editable){ clearError(); }
  };
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    // Inflate the layout for this fragment
    context.invalidateOptionsMenu();
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ResetPasswordViewModel.class);
    binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
    
    binding.edtOldPassword.addTextChangedListener(clear);
    binding.edtNewPassword.addTextChangedListener(clear);
    binding.edtConfirmPassword.addTextChangedListener(clear);
    
    binding.tilOldPassword.setVisibility(useOldPassword ? View.VISIBLE : View.GONE);
    
    binding.btnResetPassword.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        clearError();
        String oldPassword = binding.edtOldPassword.getText().toString();
        String newPassword = binding.edtNewPassword.getText().toString();
        String confirmPassword = binding.edtConfirmPassword.getText().toString();
        
        //Code to compare & check old password with existing stored password from last login (if required)
        if(validateOldPassword && !oldPassword.matches(SharedPrefManager.getPassword())){
          binding.tilOldPassword.setErrorEnabled(true);
          binding.tilOldPassword.setError(getString(R.string.field_error_password_old));
        }
        else if(validateOldPassword && !isAllowSameOldAndNewPassword && oldPassword.matches(newPassword)){
          binding.tilOldPassword.setErrorEnabled(true);
          binding.tilOldPassword.setError(getString(R.string.field_error_password_old_new));
          binding.tilConfirmPassword.setErrorEnabled(true);
          binding.tilConfirmPassword.setError(getString(R.string.field_error_password_old_new));
        }
        else if(!newPassword.matches(REGEX_PASSWORD)){//password length less
          binding.tilNewPassword.setErrorEnabled(true);
          binding.tilNewPassword.setError(getString(R.string.password_length));
          //showShortToast(getString(R.string.password_length));
        }
        else if(!newPassword.equals(confirmPassword)){//password miss match
          binding.tilNewPassword.setErrorEnabled(true);
          binding.tilNewPassword.setError(getString(R.string.field_err_password_miss_match));
          binding.tilConfirmPassword.setErrorEnabled(true);
          binding.tilConfirmPassword.setError(getString(R.string.field_err_password_miss_match));
          //showShortToast(getString(R.string.field_err_password_miss_match));
        }
        else if(newPassword.equals(confirmPassword)){//successfully
          //showShortToast(getString(R.string.field_err_password_success_change));
          //TODO call API for UPDATE_PASSWORD
          try{
            JSONObject request = new JSONObject();
            if(useOldPassword){
              request.put(ParamConstants.OLD_USER_PASSWORD, oldPassword);
              request.put(ParamConstants.New_USER_PASSWORD, confirmPassword);
            }
            request.put(ParamConstants.PASSWORD, confirmPassword);
            callWebService(URLConstants.UPDATE_PASSWORD, request, getString(R.string.progress_msg_verifying_data));
          }
          catch(Exception e){ e.printStackTrace(); }
        }
      }
    });
    
    return binding.getRoot();
  }
  
  /**
   * Is valid boolean.
   *
   * @return the boolean
   */
  boolean isValid(){
    clearError();
    String new_password = binding.edtNewPassword.getText().toString();
    String confirm_password = binding.edtConfirmPassword.getText().toString();
    
    if(binding.tilOldPassword.getVisibility() == View.VISIBLE && isNullOrEmpty(binding.edtOldPassword.getText().toString().trim())){
      binding.tilOldPassword.setErrorEnabled(true);
      binding.tilOldPassword.setError(String.format(getString(R.string.field_err_empty), chkNull(binding.tilOldPassword.getPlaceholderText(), binding.tilOldPassword.getHint())));
      return false;
      
    }
    else if(binding.tilNewPassword.getVisibility() == View.VISIBLE && isNullOrEmpty(binding.edtNewPassword.getText().toString().trim())){
      binding.tilNewPassword.setErrorEnabled(true);
      binding.tilNewPassword.setError(String.format(getString(R.string.field_err_empty), chkNull(binding.tilNewPassword.getPlaceholderText(), binding.tilNewPassword.getHint())));
      return false;
      
    }
    else if(binding.tilConfirmPassword.getVisibility() == View.VISIBLE && isNullOrEmpty(binding.edtConfirmPassword.getText().toString().trim())){
      binding.tilConfirmPassword.setErrorEnabled(true);
      binding.tilConfirmPassword.setError(String.format(getString(R.string.field_err_empty), chkNull(binding.tilConfirmPassword.getPlaceholderText(), binding.tilConfirmPassword.getHint())));
      return false;
      
    }
    else if(binding.tilNewPassword.getVisibility() == View.VISIBLE && !binding.edtNewPassword.getText().toString().matches(REGEX_PASSWORD)){//password length less
      showShortToast(getString(R.string.password_length));
      return false;
    }
    else if(binding.tilOldPassword.getVisibility() == View.VISIBLE && binding.tilNewPassword.getVisibility() == View.VISIBLE && binding.edtOldPassword.getText().toString().equals(binding.edtNewPassword.getText().toString())){//old & new password
      showShortToast(getString(R.string.field_error_password_old_new));
      return false;
    }
    else if(binding.tilNewPassword.getVisibility() == View.VISIBLE && binding.tilConfirmPassword.getVisibility() == View.VISIBLE && !binding.edtNewPassword.getText().toString().equals(binding.edtConfirmPassword.getText().toString())){//password miss match
      showShortToast(getString(R.string.field_err_password_miss_match));
      return false;
    }
    return true;
  }
  
  /**
   * Clear error.
   */
  public void clearError(){
    binding.tilOldPassword.setError(null);
    binding.tilOldPassword.setErrorEnabled(false);
    binding.tilNewPassword.setError(null);
    binding.tilNewPassword.setErrorEnabled(false);
    binding.tilConfirmPassword.setError(null);
    binding.tilConfirmPassword.setErrorEnabled(false);
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
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.UPDATE_PASSWORD:
          if(isSuccess){
            final String msg = extractString(jsonResponse, ParamConstants.MESSAGE, extractString(jsonResponse, ParamConstants.MSG, getString(R.string.field_password_success_change)));
            //context.showCustomSuccessDialog(msg);//extractString(jsonResponse, ParamConstants.MESSAGE, getString(R.string.field_password_success_change)));
            context.showCustomSuccessDialog(msg, new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialog, int which){
                if(context instanceof MainActivity)
                  ((MainActivity) context).clearSavedDataOnLogout();
                else context.popBackStack();
              }
            });
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}