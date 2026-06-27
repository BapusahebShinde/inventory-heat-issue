package com.itek.retail.ui.encoding;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.errorBeep;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.getEanRegex;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentEncodingVerifyBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The Encoding verify fragment.
 */
public class EncodingVerifyFragment extends CommonFragment {

    private final AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.VERIFY_ENCODING;
    private EncodingVerifyViewModel mViewModel;
    private FragmentEncodingVerifyBinding binding;
    private MainViewModel mainViewModel;
    private boolean isLockBarcode = false;

    /**
     * Instantiates a new Encoding verify fragment.
     */
    public EncodingVerifyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ((MainActivity) context).getRfidViewModel();
        mainViewModel.getReaderUHFInstance(sessionType);
        mainViewModel.getBarcodeReaderInstance(sessionType);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(EncodingVerifyViewModel.class);
        binding = FragmentEncodingVerifyBinding.inflate(inflater, container, false);

        final String barcode = AppCommonMethods.getLeftZeroReplacedString(context, extractString(getArguments(), AppConstants.EAN, ""));
        isLockBarcode = isValidBarcode(barcode, true);
        binding.scanEncodingVerifyBarcode.setIsShowClearLogo(!isLockBarcode);
        binding.scanEncodingVerifyBarcode.setText(barcode);
        if (isLockBarcode)
            binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(!isLockBarcode);
        binding.btnVerifyEncoding.setVisibility(!isLockBarcode ? View.VISIBLE : View.GONE);
        if (isLockBarcode) callAPI(barcode);

        if (isLandscape) {
            binding.ll2.setVisibility(binding.btnVerifyEncoding.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
            binding.v1.setVisibility(binding.btnVerifyEncoding.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
            binding.v2.setVisibility(binding.btnVerifyEncoding.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        }

        binding.scanEncodingVerifyBarcode.setImgScanOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLockBarcode || chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) || chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()))
                    return;
                context.dismissCustomAlertDialog();
                if (binding.scanEncodingVerifyBarcode != null)
                    binding.pdvEncodingVerifyBarcode.dismissAlerts();
                if (binding.scanEncodingVerifyRfid != null)
                    binding.pdvEncodingVerifyRfid.dismissAlerts();
                if (isNonEmpty(binding.scanEncodingVerifyBarcode.getText())) {
                    binding.scanEncodingVerifyRfid.setText("");
                    binding.scanEncodingVerifyRfid.setTag(null);
                    binding.scanEncodingVerifyRfid.setVisibility(View.GONE);
                    binding.pdvEncodingVerifyRfid.setCompareProductModel(null);
                    binding.pdvEncodingVerifyRfid.setProductModel(null);
                    binding.pdvEncodingVerifyRfid.setVisibility(View.GONE);
                    binding.pdvEncodingVerifyBarcode.setCompareProductModel(null);
                    binding.pdvEncodingVerifyBarcode.setProductModel(null);
                    binding.pdvEncodingVerifyBarcode.setVisibility(View.GONE);
                    binding.btnVerifyEncoding.setVisibility(View.VISIBLE);
                    if (isLandscape) {
                        binding.ll2.setVisibility(binding.btnVerifyEncoding.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
                        binding.v1.setVisibility(binding.btnVerifyEncoding.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                        binding.v2.setVisibility(binding.btnVerifyEncoding.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                    }
                } else if (isNullOrEmpty(binding.scanEncodingVerifyBarcode.getText()) && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) && !chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())) {
                    mainViewModel.softScan();
                }
            }
        });

        binding.scanEncodingVerifyRfid.setImgScanOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) || chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()))
                    return;
                context.dismissCustomAlertDialog();
                if (binding.scanEncodingVerifyBarcode != null)
                    binding.pdvEncodingVerifyBarcode.dismissAlerts();
                if (binding.scanEncodingVerifyRfid != null)
                    binding.pdvEncodingVerifyRfid.dismissAlerts();
                if (isNonEmpty(binding.scanEncodingVerifyRfid.getText())) {
                    binding.scanEncodingVerifyRfid.setText("");
                    binding.scanEncodingVerifyRfid.setTag(null);
                    binding.pdvEncodingVerifyRfid.setCompareProductModel(null);
                    binding.pdvEncodingVerifyRfid.setProductModel(null);
                    binding.pdvEncodingVerifyRfid.setVisibility(View.GONE);
                    final ProductModel prodBarcode = binding.pdvEncodingVerifyBarcode.getProductModel();
                    if (prodBarcode != null) {
                        prodBarcode.setSold(false);
                        prodBarcode.setIsCompleted(false);
                        prodBarcode.setIsMismatched(false);
                        binding.pdvEncodingVerifyBarcode.setCompareProductModel(null);
                        binding.pdvEncodingVerifyBarcode.setProductModel(prodBarcode);
                    }
                    binding.pdvEncodingVerifyBarcode.setVisibility(binding.pdvEncodingVerifyBarcode.getProductModel() != null ? View.VISIBLE : View.GONE);
                } else if (checkReaderConnected() && isNullOrEmpty(binding.scanEncodingVerifyRfid.getText()) && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) && !chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) && isRfidScan()) {
                    mainViewModel.performPick("");
                }
            }
        });

        binding.btnVerifyEncoding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.scanEncodingVerifyBarcode != null)
                    binding.pdvEncodingVerifyBarcode.dismissAlerts();
                if (binding.scanEncodingVerifyRfid != null)
                    binding.pdvEncodingVerifyRfid.dismissAlerts();
                final String barcode = chkNull(binding.scanEncodingVerifyBarcode.getText(), "");
                final String rfid = chkNull(binding.scanEncodingVerifyRfid.getText(), "");
                final String rfidEPC = binding.scanEncodingVerifyRfid.getTag() != null && binding.scanEncodingVerifyRfid.getTag() instanceof Inventory ? chkNull(((Inventory) binding.scanEncodingVerifyRfid.getTag()).epc, "") : "";
                final String rfidTid = binding.scanEncodingVerifyRfid.getTag() != null && binding.scanEncodingVerifyRfid.getTag() instanceof Inventory ? chkNull(((Inventory) binding.scanEncodingVerifyRfid.getTag()).tid, "") : "";
                final ProductModel prodBarcode = binding.pdvEncodingVerifyBarcode.getProductModel();
                final ProductModel prodRfid = binding.pdvEncodingVerifyRfid.getProductModel();
                final boolean isValidBarcode = isValidBarcode(barcode, true);
                final boolean isValidRfid = isValidBarcode(rfid, false);
                if (isValidBarcode && prodBarcode != null && isValidRfid && prodRfid != null) {
                    //do nothing
                } else if (isValidBarcode && prodBarcode != null && isValidRfid && prodRfid == null) {
                    if (!rfid.equalsIgnoreCase(barcode)) callAPI(rfid, rfidEPC, rfidTid);
                } else if (isValidBarcode && prodBarcode == null) {
                    callAPI(barcode);
                } else if (!isValidBarcode) {
                    context.showCustomErrDialog(isNonEmpty(barcode) ? String.format(getString(R.string.err_invalid_barcode), getTypeCharCode(sessionType), barcode) : String.format(getString(R.string.field_err_empty), binding.scanEncodingVerifyBarcode.getErrLbl()));
                    clearBarcode();
                }
            }
        });

        binding.scanEncodingVerifyBarcode.setGoBtn(binding.btnVerifyEncoding);
        binding.scanEncodingVerifyRfid.setGoBtn(binding.btnVerifyEncoding);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel.getIsPickOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsPickOn().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPickOn) {
                if (!isTopInStack()) return;
                showLog("isPickOn", "" + chkNotNullTrue(isPickOn));
                if (isPickOn == null) {
                }
                else {
                    final boolean isProcessOn = isPickOn || chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue());
                    ((MainActivity) context).lockDrawer(isProcessOn);
                    binding.scanEncodingVerifyBarcode.setIsProcessOn(isProcessOn);
                    binding.scanEncodingVerifyRfid.setIsProcessOn(isProcessOn);
                }
            }
        });

        mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getPickData().observe(getViewLifecycleOwner(), new Observer<Inventory>() {
            @Override
            public void onChanged(Inventory inventory) {
                if (!isTopInStack()) return;
                if (inventory != null && isNonEmpty(inventory.ean) /*&& chkNull(inventory.ean, "").matches(getEanRegex(true))*/) {
                    binding.scanEncodingVerifyRfid.setTag(inventory);
                    if (!isLockBarcode && SharedPrefManager.getIsEANMapped()) {
                        try {
                            Bundle args = new Bundle();
                            args.putSerializable(inventory.getClass().getSimpleName(), inventory);
                            args.putString(ParamConstants.EPC, inventory.epc);
                            args.putString(ParamConstants.TID, inventory.tid);
                            JSONObject jsonRequest = new JSONObject();
                            jsonRequest.put(ParamConstants.MAPPED_EAN, inventory.ean);
                            jsonRequest.put(ParamConstants.EPC, inventory.epc);
                            jsonRequest.put(ParamConstants.TID, inventory.tid);
                            callWebService(URLConstants.GET_UNMAPPED_EAN, jsonRequest, args, getString(R.string.progress_msg_check_map_data));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        binding.scanEncodingVerifyRfid.setText(chkNull(inventory.ean, ""));
                        //binding.scanEncodingVerifyRfid.setIsViewControlEnabled(false, true);
                        final boolean isEanMatching = isNonEmpty(inventory.ean) && (inventory.ean.equalsIgnoreCase(chkNull(binding.scanEncodingVerifyBarcode.getText(), "")) || inventory.ean.equalsIgnoreCase(binding.pdvEncodingVerifyBarcode.getProductModel().getSearchEan()));
                        if (isEanMatching) {
                            final ProductModel prodBarcode = binding.pdvEncodingVerifyBarcode.getProductModel();
                            if (prodBarcode != null) {
                                final String epc = inventory.epc;
                                if (isNonEmpty(epc)) {
                                    prodBarcode.setEpc(epc);
                                    prodBarcode.setSold(epc.length() >= 24 && epc.startsWith("0"));
                                }
                                prodBarcode.setIsCompleted(true);
                                prodBarcode.setIsMismatched(false);
                            }
                            if (SharedPrefManager.getBoolean(ParamConstants.IS_USE_REFERENCE_BARCODE, AppCommonMethods.isUseReferenceBarcode) && binding.scanEncodingVerifyRfid.getText().equalsIgnoreCase(prodBarcode.getRefEan())) {
                                binding.scanEncodingVerifyRfid.setText(prodBarcode.getEan());
                            }
                            binding.pdvEncodingVerifyRfid.setCompareProductModel(null);
                            binding.pdvEncodingVerifyRfid.setProductModel(prodBarcode);
                            binding.pdvEncodingVerifyRfid.setVisibility(View.VISIBLE);
                            binding.pdvEncodingVerifyBarcode.setCompareProductModel(null);
                            binding.pdvEncodingVerifyBarcode.setProductModel(prodBarcode);
                            binding.pdvEncodingVerifyBarcode.setVisibility(isLandscape ? View.VISIBLE : View.GONE);
                            AppCommonMethods.successBeep();
                        } else
                            callAPI(chkNull(inventory.ean, ""), chkNull(inventory.epc, ""), chkNull(inventory.tid, ""));
                    }
                }
                if (inventory != null) mainViewModel.getPickData().postValue(null);
            }
        });

        if (!isLockBarcode) setObservers();
        setTriggerDataObserver();

        mainViewModel.getIsDeviceConfigured().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isReaderConfigured) {
                if (isTopInStack() && chkNotNullTrue(isReaderConfigured) && !isLockBarcode)
                    setObservers();
            }
        });
    }

    @Override
    public AppCommonMethods.SessionType getSessionType() {
        return sessionType;
    }

    /**
     * Check reader connected boolean.
     *
     * @return the boolean
     */
    protected boolean checkReaderConnected() {
        if (mainViewModel.isReaderConnected()) return true;
        else {
            context.showCustomAlertDialog("", String.format(getString(R.string.err_reader_connection), getTypeCharCode()), getString(R.string.btn_ok), (dialogInterface, i) -> {
                if (((MainActivity) context).isReaderConnected()) mainViewModel.performPick("");
                else mainViewModel.checkAndConnectReader();
            });
            return false;
        }
    }

    /**
     * Set observers.
     */
    protected void setObservers() {
        if (isLockBarcode) return;
        mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsBarcodeOn().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isBarcodeOn) {
                if (!isTopInStack()) return;
                showLog("isBarcodeOn", AppCommonMethods.chkVal(isBarcodeOn));
                insertAuditTrailsLog("Barcode_" + (chkNotNullTrue(isBarcodeOn) ? "ON" : "OFF"));
                final boolean isProcessOn = chkNotNullTrue(isBarcodeOn) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue());
                ((MainActivity) context).lockDrawer(isProcessOn);
                binding.scanEncodingVerifyBarcode.setIsProcessOn(isProcessOn);
                binding.scanEncodingVerifyRfid.setIsProcessOn(isProcessOn);
                //if(isLockBarcode) binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(false);
                //else binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(isNonEmpty(binding.scanEncodingVerifyBarcode.getText().toString()) || !isProcessOn, !isLockBarcode);
            }
        });

        mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getBarcodeData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String barcode) {
                if (!isTopInStack()) return;
                showLog(EncodingVerifyFragment.this.getClass().getSimpleName() + "_barcodeData", chkNull(barcode, ""));
                if (isNonEmpty(barcode)) {
                    if (isValidBarcode(barcode, true)) {//SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_BOTH) || SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_STD) == SGTIN96.IsValidGtin(ApplicationCommonMethods.getZeroFilledString(barcode, 14))){
                        binding.scanEncodingVerifyBarcode.setText(barcode);
                        if (isLockBarcode)
                            binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(false);
                        //else binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(false, !isLockBarcode);
                        callAPI(chkNull(binding.scanEncodingVerifyBarcode.getText(), ""));
                    } else {
                        context.showCustomErrDialog(String.format(getString(R.string.err_invalid_barcode), getTypeCharCode(sessionType), barcode));
                        clearBarcode();
                    }
                }
                if (isNonEmpty(barcode)) {
                    mainViewModel.getBarcodeData().postValue("");
                }
            }
        });
    }

    /**
     * Call api.
     *
     * @param ean the ean
     */
    void callAPI(final String ean) {
        callAPI(ean, "", "");
    }

    /**
     * Call api.
     *
     * @param ean the ean
     * @param epc the epc
     */
    void callAPI(final String ean, final String epc, final String tid) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put(ParamConstants.EAN, chkNull(ean, ""));
            jsonRequest.put(ParamConstants.EPC, chkNull(epc, ""));
            jsonRequest.put(ParamConstants.TID, chkNull(tid, ""));
            final String productInfoUrl = getProductInfoUrl();
            if (productInfoUrl.equalsIgnoreCase(URLConstants.GET_PRODUCT_INFO_BY_SKU)) {
                JSONArray js = new JSONArray();
                js.put(jsonRequest.get(ParamConstants.EAN));
                jsonRequest.put(ParamConstants.ITEMS, js);
            }
            callWebService(productInfoUrl, jsonRequest, getString(R.string.progress_msg_getting_data), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set trigger data observer.
     */
    private void setTriggerDataObserver() {
        mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
        mainViewModel.isTriggerPressed().observe(getViewLifecycleOwner(), triggerPressed -> {

            if (!isTopInStack()) return;
            if (triggerPressed != null && getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && allowBtnClick) {
                if (triggerPressed && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) && !chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())) {
                    context.dismissCustomAlertDialog();
                    if(binding.pdvEncodingVerifyBarcode!=null) binding.pdvEncodingVerifyBarcode.dismissAlerts();
                    if(binding.pdvEncodingVerifyRfid!=null) binding.pdvEncodingVerifyRfid.dismissAlerts();
                    if (checkReaderConnected() && isNonEmpty(binding.scanEncodingVerifyBarcode.getText()) && binding.pdvEncodingVerifyBarcode.getProductModel() != null && isNullOrEmpty(binding.scanEncodingVerifyRfid.getText()) && binding.pdvEncodingVerifyRfid.getProductModel() == null) {
                        mainViewModel.performPick("");
                    } else if (isNullOrEmpty(binding.scanEncodingVerifyBarcode.getText()) && binding.pdvEncodingVerifyBarcode.getProductModel() == null) {
                        mainViewModel.softScan();
                    }
                }
            }
        });
    }

    /**
     * Clear barcode.
     */
    public void clearBarcode() {
        if (isLockBarcode) binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(false);
        else {
            binding.scanEncodingVerifyBarcode.setText("");
            //binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(true, !isLockBarcode);
        }
    }

    /**
     * Is valid barcode boolean.
     *
     * @param barcode   the barcode
     * @param isBarcode the is barcode
     * @return the boolean
     */
    boolean isValidBarcode(final String barcode, final boolean isBarcode) {
        ////AppCommonMethods.getEanRegex(!isBarcode)))//!isBarcode ? AppConstants.REGEX_HEX_BARCODE : AppConstants.REGEX_NUM_BARCODE))
        return AppCommonMethods.getLeftZeroReplacedString(context, barcode).matches(getEanRegex(!isBarcode));//!isBarcode || SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_BOTH) || SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_STD) == SGTIN96.IsValidGtin(ApplicationCommonMethods.getZeroFilledString(barcode, 14));
    }

    /**
     * Is rfid scan boolean.
     *
     * @return the boolean
     */
    boolean isRfidScan() {
        final String barcode = chkNull(binding.scanEncodingVerifyBarcode.getText(), "");
        if (!isValidBarcode(barcode, true)) {
            context.showCustomErrDialog(isNonEmpty(barcode) ? String.format(getString(R.string.err_invalid_barcode), getTypeCharCode(sessionType), barcode) : String.format(getString(R.string.field_err_empty), binding.scanEncodingVerifyBarcode.getErrLbl()));
            clearBarcode();
            return false;
        } else return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isTopInStack()) {
            mainViewModel.getBarcodeReaderInstance(sessionType);
            setObservers();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainViewModel.onResume(sessionType);
                }
            }, 300);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mainViewModel.onPause();
    }

    @Override
    public void onDestroyView() {
        mainViewModel.getIsDeviceConfigured().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsPickOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
        mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        //mainViewModel.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        ((MainActivity) context).lockDrawer(false);
        super.onDetach();
    }

    public void onBackPressed() {
        if (mainViewModel != null && (chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue()) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue()))) {

            context.showCustomAlertDialog("", String.format(getString(R.string.err_op_back_press), getTypeCharCode(), sessionType.name()), false, true, getString(R.string.btn_ok), null);
        } else super.onBackPressed();
    }

    public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args) {
        super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
        try {
            switch (url) {
                case URLConstants.GET_PRODUCT_INFO:
                case URLConstants.GET_PRODUCT_INFO_BY_SKU:
                    ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
                    if (isStaticDebug())
                        productModel.setEan(extractString(jsonRequest, ParamConstants.EAN, ""));
                    productModel.setSessionType(AppCommonMethods.SessionType.VERIFY_ENCODING.getValue());
                    final ProductModel prodBarcode = binding.pdvEncodingVerifyBarcode.getProductModel();
                    if (isNonEmpty(binding.scanEncodingVerifyRfid.getText()) && prodBarcode != null) {
                        prodBarcode.setIsCompleted(false);
                        prodBarcode.setIsMismatched(true);
                        productModel.setIsCompleted(false);
                        productModel.setIsMismatched(true);
                        binding.pdvEncodingVerifyRfid.setCompareProductModel(prodBarcode);
                        binding.pdvEncodingVerifyRfid.setProductModel(productModel);
                        if (productModel != null && isNonEmpty(productModel.getEan()) && !productModel.getEan().equalsIgnoreCase(binding.scanEncodingVerifyRfid.getText().trim()))
                            binding.scanEncodingVerifyRfid.setText(productModel.getEan());
                        binding.pdvEncodingVerifyRfid.setVisibility(View.VISIBLE);
                        binding.pdvEncodingVerifyBarcode.setCompareProductModel(productModel);
                        binding.pdvEncodingVerifyBarcode.setProductModel(prodBarcode);
                        errorBeep();
                    } else {
                        productModel.setIsCompleted(false);
                        productModel.setIsMismatched(false);
                        binding.pdvEncodingVerifyBarcode.setCompareProductModel(null);
                        binding.pdvEncodingVerifyBarcode.setProductModel(productModel);
                        binding.pdvEncodingVerifyBarcode.setVisibility(View.VISIBLE);
                        if (isLockBarcode)
                            binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(false);
                        else
                            binding.scanEncodingVerifyBarcode.setIsViewControlEnabled(false, !isLockBarcode);
                        binding.btnVerifyEncoding.setVisibility(View.GONE);
                        binding.scanEncodingVerifyRfid.setVisibility(View.VISIBLE);
                        if (isLandscape) {
                            binding.ll2.setVisibility(binding.btnVerifyEncoding.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
                            binding.v1.setVisibility(binding.btnVerifyEncoding.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                            binding.v2.setVisibility(binding.btnVerifyEncoding.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                        }
                    }
                    break;
                case URLConstants.GET_UNMAPPED_EAN:
                    if (isSuccess) {
                        final String mappedEan = extractString(jsonResponse, ParamConstants.MAPPED_EAN, extractString(jsonRequest, ParamConstants.EAN));
                        final String ean = extractString(jsonResponse, ParamConstants.EAN, "");
                        final String epc = extractString(args, ParamConstants.EPC, "");
                        final String tid = extractString(args, ParamConstants.TID, "");
                        binding.scanEncodingVerifyRfid.setText(chkNull(ean, ""));
                        //binding.scanEncodingVerifyRfid.setIsViewControlEnabled(false, true);
                        final boolean isEanMatching = isNonEmpty(ean) && ean.equalsIgnoreCase(chkNull(binding.scanEncodingVerifyBarcode.getText(), ""));
                        if (isEanMatching) {
                            final ProductModel prodBarcode1 = binding.pdvEncodingVerifyBarcode.getProductModel();
                            if (prodBarcode1 != null) {
                                //final String epc = inventory.epc;
                                if (isNonEmpty(epc)) {
                                    prodBarcode1.setEpc(epc);
                                    prodBarcode1.setSold(epc.length() >= 24 && epc.startsWith("0"));
                                }
                                prodBarcode1.setIsCompleted(true);
                                prodBarcode1.setIsMismatched(false);
                            }
                            binding.pdvEncodingVerifyRfid.setCompareProductModel(null);
                            binding.pdvEncodingVerifyRfid.setProductModel(prodBarcode1);
                            binding.pdvEncodingVerifyRfid.setVisibility(View.VISIBLE);
                            binding.pdvEncodingVerifyBarcode.setCompareProductModel(null);
                            binding.pdvEncodingVerifyBarcode.setProductModel(prodBarcode1);
                            binding.pdvEncodingVerifyBarcode.setVisibility(isLandscape ? View.VISIBLE : View.GONE);
                            AppCommonMethods.successBeep();
                        } else callAPI(chkNull(ean, ""), chkNull(epc, ""), chkNull(tid, ""));
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}