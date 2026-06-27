package com.itek.retail.ui.encoding;

import static com.itek.retail.apis.ParamConstants.OLD_ACCESS_PASSWORDS;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT_PATTERN;
import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.getEanRegex;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;
import static com.itek.retail.common.AppCommonMethods.isUse24LengthTIDForUpload;
import static com.itek.retail.common.AppConstants.ENCODE_CONFIG_QTY_ONE_MANY;
import static com.itek.retail.common.AppConstants.ENCODE_TYPE_MANY;
import static com.itek.retail.common.AppConstants.SESSION_ACTION_RESUME;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.EncodingHistoryAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.databinding.FragmentEncodingScanScanWriteBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.customviews.InputView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.productsearch.ProductDetailsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Encoding start fragment.
 */
public class EncodingScanScanWriteFragment extends RFIDSessionFragment {

    //boolean isOneToOneRelation = false, isBarcodeBarcodeRFID = false;
    int activeUsers = 0, sessionValidTill = 48, target = -1;
    InventoryDao inventoryDao;
    UploadInventoryDao uploadInventoryDao;
    Inventory pickedTag;
    Boolean is1stSessionStart = false;
    private boolean isBulkEncoding = false;
    private EncodingScanScanWriteViewModel mViewModel;
    private FragmentEncodingScanScanWriteBinding binding;
    private final List<EanQty> listEncodedEans = new ArrayList<>(0);
    private final List<String> scannedRFIDQRCodes = new ArrayList<String>(0);
    private final List<Inventory> listPickedTags = new ArrayList<Inventory>(0);
    //Changes
    private String mappedEan;

    /**
     * Instantiates a new Encoding start fragment.
     */
    public EncodingScanScanWriteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ((MainActivity) context).getRfidViewModel();
        inventoryDao = AppDatabase.getInventoryDao(context);
        uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
        mainViewModel.getBarcodeReaderInstance(getSessionType());
        isBulkEncoding = SharedPrefManager.getBoolean(ParamConstants.IS_BULK_ENCODE, false);
        SharedPrefManager.setInt(AppConstants.ENCODE_CONFIG_QTY_ONE_MANY, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(EncodingScanScanWriteViewModel.class);
        binding = FragmentEncodingScanScanWriteBinding.inflate(inflater, container, false);

        activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
        sessionValidTill = extractInt(getArguments(), AppConstants.SESSION_VALID_TILL, 48);
        target = extractInt(getArguments(), AppConstants.TARGET, -1);

        setActiveUsers(activeUsers);
        binding.scanEncodingStartBarcode.edtCode.requestFocus();
        hideKeyboard();

        binding.divFifo.setVisibility(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) ? View.VISIBLE : View.GONE);

        binding.seekEncodingStartTarget.setThumb(getResources().getDrawable(R.drawable.ic_target));

        //isOneToOneRelation = true;
        //isBarcodeBarcodeRFID = true;

        //SharedPrefManager.setEncodeRelationType(AppConstants.ENCODE_TYPE_MANY);
        //binding.llEncodingStartBarcoderfidScan.setVisibility(isBarcodeBarcodeRFID ? View.VISIBLE : View.GONE);
        //binding.clStartEncodingLlScore.setVisibility(!isBarcodeBarcodeRFID ? View.VISIBLE : View.GONE);
        //binding.lblEncodingTotal.setVisibility(!isBarcodeBarcodeRFID ? View.VISIBLE : View.GONE);

        //binding.progressEncodingStart.setVisibility(isOneToOneRelation ? View.VISIBLE : View.GONE);
        //binding.txtEncodingStartScoreTotal.setVisibility(isOneToOneRelation ? View.VISIBLE : View.GONE);
        //binding.divEncodingStartScore.setVisibility(isOneToOneRelation ? View.VISIBLE : View.GONE);
        //binding.seekEncodingStartTarget.setVisibility(isOneToOneRelation ? View.VISIBLE : View.GONE);
        //binding.lblEncodingTotal.setText(isOneToOneRelation ? R.string.lbl_encode_target : R.string.lbl_total);

        binding.seekEncodingStartTarget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //code here
                // int encoded = AppCommonMethods.parseInt(binding.txtEncodingStartScore.getText().toString());
                int total = progress * 10;
                if (binding.ctwInventoryStart != null) binding.ctwInventoryStart.setTotal(total);
                //binding.txtEncodingStartScoreTotal.setText("" + total);
                // showLog("encoded", "" + encoded);
                showLog("target", "" + total);
        /*binding.progressEncodingStart.setProgress(encoded * 100 / (total > 0 ? total : 1));
        binding.progressEncodingStart.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
        binding.txtEncodingStartScoreTotal.setVisibility(*//*isOneToOneRelation &&*//* total > 0 ? View.VISIBLE : View.GONE);
        binding.divEncodingStartScore.setVisibility(*//*isOneToOneRelation &&*//* total > 0 ? View.VISIBLE : View.GONE);*/
                binding.lblEncodingTotal.setText(total > 0 ? R.string.lbl_encode_target : R.string.lbl_total);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //unused method (Default Overridden)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //unused method (Default Overridden)
            }
        });

        //Power bar
        binding.llSeekbarPower.setupProgress(mainViewModel);

        binding.scanEncodingStartBarcode.setImgScanOnClickListener(view -> {
            final boolean isProcessOn = isProcessOn();
            if (!isProcessOn) {
                if (isNullOrEmpty(binding.scanEncodingStartBarcode.getText())) {
                    context.dismissCustomAlertDialog();
                    final Boolean isSessionOn = mainViewModel.getIsSessionOn().getValue();
                    if (!chkNotNullTrue(isSessionOn) && sessionObject == null)
                        apiCall(AppConstants.SESSION_ACTION_START);
                    else mainViewModel.softScan(binding.scanEncodingStartBarcode.getLabel());
                } else if (isNonEmpty(binding.scanEncodingStartBarcode.getText())) {
                    scannedRFIDQRCodes.clear();
                    SharedPrefManager.setInt(AppConstants.ENCODE_CONFIG_QTY_ONE_MANY, 0);
                    updateTidQtyLabel();
                    if (isNonEmpty(binding.scanEncodingStartTid.getText()))
                        binding.scanEncodingStartTid.setText("");
                }
            }
        });

        binding.scanEncodingStartTid.setImgScanOnClickListener(view -> {
            if (isNullOrEmpty(binding.scanEncodingStartTid.getText())) {
                final boolean isProcessOn = isProcessOn();
                if (!isProcessOn) {
                    context.dismissCustomAlertDialog();
                    mainViewModel.softScan(binding.scanEncodingStartTid.getLabel());
                }
            }
        });

        binding.llBtnStart.setOnClickListener(v -> {
            context.dismissCustomAlertDialog();
            showLog("llBtnStart_isProcessOn", "" + isProcessOn());
            final Boolean isSessionOn = mainViewModel.getIsSessionOn().getValue();
            if (!chkNotNullTrue(isSessionOn) && sessionObject == null)
                apiCall(AppConstants.SESSION_ACTION_START);
            else if (chkNotNullTrue(isSessionOn) && sessionObject != null && !isProcessOn() && checkReaderConnected()) {
                String barcode = AppCommonMethods.getLeftZeroReplacedString(context, chkNull(binding.scanEncodingStartBarcode.getText(), ""));
                String tid = AppCommonMethods.getLeftZeroReplacedString(context, chkNull(binding.scanEncodingStartTid.getText(), ""));
                showLog("barcode_tid", barcode + "_" + tid);
                if (isNullOrEmpty(barcode)) binding.scanEncodingStartBarcode.performScan();
                else if (barcode.matches(AppCommonMethods.getEanRegex())) {//AppConstants.REGEX_NUM_BARCODE) && (SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_BOTH) || SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_STD) == SGTIN96.IsValidGtin(ApplicationCommonMethods.getZeroFilledString(barcode, 14))))
                    binding.scanEncodingStartBarcode.setIsViewControlEnabled(false, true);
                    if (isNullOrEmpty(tid) && SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(ENCODE_TYPE_MANY) && isBulkEncoding && SharedPrefManager.getEncodeType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_MANY) && scannedRFIDQRCodes.size() <= 0 && SharedPrefManager.getInt(AppConstants.ENCODE_CONFIG_QTY_ONE_MANY, 0) <= 0) {
                        updateTidQtyLabel();
                        showTidQtyDialog();
                    } else if (isNullOrEmpty(tid) && !(SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(ENCODE_TYPE_MANY) && isBulkEncoding && scannedRFIDQRCodes.size() == SharedPrefManager.getInt(ENCODE_CONFIG_QTY_ONE_MANY)))
                        binding.scanEncodingStartTid.performScan();
                    else if (tid.matches(AppCommonMethods.getTidRegex()) || (SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(ENCODE_TYPE_MANY) && isBulkEncoding && scannedRFIDQRCodes.size() == SharedPrefManager.getInt(ENCODE_CONFIG_QTY_ONE_MANY)))
                        startRfidReading(barcode, tid, false);
                    else {
                        context.showCustomErrDialog(String.format(getString(R.string.err_invalid_scan), getTypeCharCode(), binding.scanEncodingStartTid.getLabel(), tid));
                        clearBarcode();
                    }
                } else {
                    context.showCustomErrDialog(String.format(getString(R.string.err_invalid_barcode), getTypeCharCode(), barcode));
                    clearBarcode(true);
                }
            }
        });

        binding.scanEncodingStartBarcode.setGoBtn(binding.llBtnStart);
        binding.scanEncodingStartTid.setGoBtn(binding.llBtnStart);

        binding.listEncodingStartHistory.setAdapter(new EncodingHistoryAdapter((MainActivity) context, EncodingScanScanWriteFragment.this, listEncodedEans));
        binding.listEncodingStartHistory.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));

        if (/*isOneToOneRelation &&*/ !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && sessionObject == null && chkNull(target, -1) >= 0) {
            setTarget(target);
            is1stSessionStart = null;
            if (!AppDatabase.getMenuDao(context).hasMenu(AppConstants.MENU_CODE_ENC_CONFIG))
                SharedPrefManager.setString(SharedPrefManager.SharedPrefKeys.ENCODING_RELATION_TYPE, isBulkEncoding ? ENCODE_TYPE_MANY : AppConstants.ENCODE_TYPE_ONE);
            binding.llBtnStart.performClick();
        } else if (/*isOneToOneRelation &&*/ !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && sessionObject == null && chkNull(target, -1) < 0) {
            if (!AppDatabase.getMenuDao(context).hasMenu(AppConstants.MENU_CODE_ENC_CONFIG))
                SharedPrefManager.setString(SharedPrefManager.SharedPrefKeys.ENCODING_RELATION_TYPE, isBulkEncoding ? ENCODE_TYPE_MANY : AppConstants.ENCODE_TYPE_ONE);
            binding.seekEncodingStartTarget.setProgress(AppCommonMethods.parseInt(binding.ctwInventoryStart.getTotal()) / 10);
        } else if (/*isOneToOneRelation &&*/ !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && sessionObject != null && chkNull(target, -1) < 0) {
            final String totEncCount = sessionObject != null ? sessionObject.total : chkZero(binding.ctwInventoryStart.getTotal(), "0");
            showLog("totEncCount", totEncCount);
            // binding.txtEncodingStartScoreTotal.setText(totEncCount);
            if (binding.ctwInventoryStart != null) binding.ctwInventoryStart.setTotal(totEncCount);
            int total = -1;
            try {
                total = Integer.parseInt(totEncCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sessionObject != null && total >= 0)
                binding.seekEncodingStartTarget.setProgress(total / 10);
            else setTarget(target);
        }

        binding.header.imgConfigSync.setVisibility(SharedPrefManager.getBoolean(ParamConstants.IS_CONFIG_ENC_SSW, false) ? View.VISIBLE : View.GONE);
        binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
        binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())) return;
                binding.llEncodingConfig.setVisibility(binding.llEncodingConfig.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        binding.btnConfigRelationOneToOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null && v.getVisibility() == View.VISIBLE && !SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(v.getTag().toString().trim())) {
                    selectButtonClick((LinearLayout) v.getParent(), v.getTag().toString());
                }
            }
        });

        binding.btnConfigRelationOneToMany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (v != null && v.getVisibility() == View.VISIBLE && !SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(v.getTag().toString().trim())) {
                    selectButtonClick((LinearLayout) v.getParent(), v.getTag().toString());
                }
            }
        });

        selectButtonClick(binding.llEncodingConfigRelation, SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.ENCODING_RELATION_TYPE, isBulkEncoding ? ENCODE_TYPE_MANY : AppConstants.ENCODE_TYPE_ONE));
    
    /*if(isBulkEncoding) binding.btnConfigRelationOneToMany.performClick();
    else binding.btnConfigRelationOneToOne.performClick();*/

        return binding.getRoot();
    }

    private void showTidQtyDialog() {
        final InputView inputQty = new InputView(context);
        inputQty.setHint(R.string.hint_enc_config_one_many_qty);
        inputQty.setLabel(R.string.lbl_enc_config_one_many_qty);
        inputQty.setMinLen(1);
        inputQty.setMaxLen(2);
        inputQty.setDigits(R.string.onlyDigits);
        inputQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputQty.setValidationRegex("^([1-9][0-9]{0,1})$");
        inputQty.setButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputQty.validate()) {
                    int encQty = Integer.parseInt(chkZero(inputQty.getText().trim(), "0"));
                    scannedRFIDQRCodes.clear();
                    SharedPrefManager.setInt(AppConstants.ENCODE_CONFIG_QTY_ONE_MANY, encQty);
                    updateTidQtyLabel();
                    //selectButtonClick((LinearLayout) v.getParent(), ((Button) v).getTag().toString());
                }
            }
        });
        context.showCustomAlertDialog(getString(R.string.lbl_enc_config_one_many_qty), "", inputQty, getString(R.string.btn_set), null, getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearBarcode(true);
            }
        });
    }

    public void selectButtonClick(LinearLayout ll, String btnStr) {
        showLog("btnstr:", btnStr);
        if (ll != null && ll.getVisibility() == View.VISIBLE && ll.getChildCount() > 0) {
            for (int i = 0; i < ll.getChildCount(); i++)
                if (ll.getChildAt(i) instanceof Button b) {
                    final String btnString = (b.getTag() != null && b.getTag() instanceof String ? b.getTag().toString() : b.getText().toString()).replaceFirst(" to ", "-").trim();
                    showLog("b:", btnString);
                    final boolean isClicked = btnString.equalsIgnoreCase(btnStr.replaceFirst(" to ", "-").trim());
                    b.setSelected(isClicked);
                    if (isClicked) {
                        final SharedPrefManager.SharedPrefKeys key = ll.getId() == binding.llEncodingConfigRelation.getId() ? SharedPrefManager.SharedPrefKeys.ENCODING_RELATION_TYPE : null;
                        SharedPrefManager.setString(key, btnString);
                        showLog(key.toString(), SharedPrefManager.getString(key, "-"));
                        if (key.equals(SharedPrefManager.SharedPrefKeys.ENCODING_RELATION_TYPE) && SharedPrefManager.getString(key).equalsIgnoreCase(AppConstants.ENCODE_TYPE_ONE)) {
                            SharedPrefManager.setInt(AppConstants.ENCODE_CONFIG_QTY_ONE_MANY, 0);
                            scannedRFIDQRCodes.clear();
                        }
                        updateTidQtyLabel();
                    }
                }
        }
    }

    /**
     * Set target.
     *
     * @param target the target
     */
    public void setTarget(int target) {
        //if(!isOneToOneRelation) return;
        try {
            target = sessionObject != null ? AppCommonMethods.parseInt(sessionObject.total) : chkNull(EncodingScanScanWriteFragment.this.target, -1) >= 0 ? EncodingScanScanWriteFragment.this.target : target;
            if (sessionObject == null && chkNull(target, -1) >= 0) {
                //binding.txtEncodingStartScoreTotal.setText("" + target);
                if (binding.ctwInventoryStart != null) binding.ctwInventoryStart.setTotal(target);
                binding.seekEncodingStartTarget.setProgress(target / 10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set active users.
     */
    public void setActiveUsers(final int activeUsers) {
        int oldActiveUsers = -2;
        try {
            oldActiveUsers = AppCommonMethods.parseInt(binding.header.btnActiveDevices.getText().toString(), "-2");
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        final int activeCount = chkNull(activeUsers, -1) >= 0 ? activeUsers : oldActiveUsers;
        binding.header.flActiveDevices.setVisibility(activeUsers >= -1 ? View.VISIBLE : View.GONE);
        binding.header.btnActiveDevices.setSelected(true);
        binding.header.btnActiveDevices.setText(activeCount >= 0 ? "" + activeCount : "");
    }

    @Override
    protected void setObservers() {
        super.setObservers();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setBarcodeObservers();
    }

    /**
     * Set barcode observers.
     */
    void setBarcodeObservers() {
        mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsBarcodeOn().observe(getViewLifecycleOwner(), isBarcodeOn -> {
            if (!isTopInStack()) return;
            showLog("isBarcodeOn", AppCommonMethods.chkVal(isBarcodeOn));
            if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
      /*final boolean isProcessOn = isProcessOn();
      binding.llBtnStart.setEnabled(!isProcessOn);
      binding.scanEncodingStartBarcode.setIsProcessOn(isProcessOn);
      binding.scanEncodingStartTid.setIsProcessOn(isProcessOn);
      //binding.scanEncodingStartBarcode.setIsViewControlEnabled(chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
      //binding.scanEncodingStartTid.setIsViewControlEnabled(chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
      binding.divFifo.setEnabled(binding.divFifo.getVisibility() == View.VISIBLE && !isProcessOn);*/
            updateView();
        });

        mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getBarcodeData().observe(getViewLifecycleOwner(), barcode -> {
            //Update Method with Type
            showLog(EncodingScanScanWriteFragment.this.getClass().getSimpleName() + "_barcodeData", chkNull(barcode, ""));
            if (!isTopInStack()) return;
            //showLog("barcodeData1", "" + chkNull(barcode, ""));
            if (isNonEmpty(barcode)) {
                mainViewModel.getBarcodeData().setValue("");
                String type = "";
                if (barcode.contains(";;")) {
                    type = barcode.split(";;")[1];
                    barcode = barcode.split(";;")[0];
                }
                showLog("barcode_type", barcode + "_" + type);
                AppCommonMethods.logInFile(context, getSessionType() + "_Barcode_Observed (" + barcode + ")");
                if (binding.scanEncodingStartBarcode.getText().length() > 0 && chkNull(type, "").matches("(?i)(TID|RFID QR)")) {
                    if (!barcode.matches(AppCommonMethods.getTidRegex())) {
                        context.showCustomErrDialog(String.format(getString(R.string.err_invalid_scan), getTypeCharCode(), type, barcode));
                        clearBarcode();
                    } else {//SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_BOTH) || SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_STD) == SGTIN96.IsValidGtin(ApplicationCommonMethods.getZeroFilledString(barcode, 14))){
                        if (!(SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(ENCODE_TYPE_MANY) && isBulkEncoding)) {
                            binding.scanEncodingStartTid.setText(barcode);
                            scannedRFIDQRCodes.clear();
                            scannedRFIDQRCodes.add(barcode);
                        } else if (!scannedRFIDQRCodes.contains(barcode) && scannedRFIDQRCodes.size() < SharedPrefManager.getInt(ENCODE_CONFIG_QTY_ONE_MANY, 0))
                            scannedRFIDQRCodes.add(barcode);
                        updateTidQtyLabel();
                        if (sessionObject == null && !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))
                            return;
                        if (checkReaderConnected()) {
                            showLog("llBtnStart_click", barcode);
                            showLog("llBtnStart_click_isProcessOn", "" + isProcessOn());
                            if (SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_ONE) || !isBulkEncoding || SharedPrefManager.getInt(ENCODE_CONFIG_QTY_ONE_MANY, 0) <= 0 || scannedRFIDQRCodes.size() == SharedPrefManager.getInt(ENCODE_CONFIG_QTY_ONE_MANY)) {
                                if (isProcessOn())
                                    startRfidReading(binding.scanEncodingStartBarcode.getText(), barcode, true);
                                else binding.llBtnStart.performClick();
                            }
                        }
                    }
                } else {
                    if (!barcode.matches(getEanRegex())) {
                        context.showCustomErrDialog(String.format(getString(R.string.err_invalid_barcode), getTypeCharCode(), barcode));
                        clearBarcode(true);
                    } else {//SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_BOTH) || SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_STD) == SGTIN96.IsValidGtin(ApplicationCommonMethods.getZeroFilledString(barcode, 14))){
                        binding.scanEncodingStartBarcode.setText(barcode);
                        if (isBulkEncoding && SharedPrefManager.getEncodeType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_MANY)) {
                            scannedRFIDQRCodes.clear();
                            SharedPrefManager.setInt(AppConstants.ENCODE_CONFIG_QTY_ONE_MANY, 0);
                            updateTidQtyLabel();
                            showTidQtyDialog();
                        }
                        //if(checkReaderConnected()) startRfidReading(barcode, "", true);
                        //binding.llBtnStart.performClick();
                    }
                }
            }
            //if(isNonEmpty(barcode)){mainViewModel.getBarcodeData().postValue("");}
        });
    }

    /**
     * Start encoding.
     *
     * @param pickedTag the picked tag
     */
    private void startEncoding(final Inventory pickedTag, final String currentTagPassword) {
        final String sgtin = pickedTag.newEpc;
        boolean access_to_write = context.epcEncoderDecoder.isValidHeader(sgtin);

        if (access_to_write) {
            if (SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) && binding.divFifo.getVisibility() == View.VISIBLE) {
                mainViewModel.setFifoDate(binding.divFifo.getServerDate());
            }
            mainViewModel.performEncoding(pickedTag, currentTagPassword);
        } else {
            hideProgressDialog();
            context.showCustomErrDialog(R.string.err_encoding_write_fail);
            clearBarcode();
            //CANNOT WRITE
        }
    }

    /**
     * Start encoding.
     *
     * @param listPickedTags the list picked tags
     */
    private void startEncoding(final List<Inventory> listPickedTags, final String currentTagPassword) {
        if (SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) && binding.divFifo.getVisibility() == View.VISIBLE) {
            mainViewModel.setFifoDate(binding.divFifo.getServerDate());
        }
        mainViewModel.performEncoding(listPickedTags);
    /*else{
      hideProgressDialog();
      context.showCustomErrDialog(R.string.err_encoding_write_fail);
      clearBarcode();
      //CANNOT WRITE
    }*/
    }

    /**
     * Clear barcode.
     */
    public void clearBarcode() {
        clearBarcode(false);
    }

    public void clearBarcode(final boolean isClearBarcode) {
        context.runOnUiThread(() -> {
            if (pickedTag != null) pickedTag = null;
            if (isNonEmpty(listPickedTags)) listPickedTags.clear();
            if (isClearBarcode || SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_ONE)) {
                if (binding != null && binding.scanEncodingStartBarcode != null) {
                    binding.scanEncodingStartBarcode.setText("");
                    //binding.scanEncodingStartBarcode.setIsViewControlEnabled(true);
                    SharedPrefManager.setInt(AppConstants.ENCODE_CONFIG_QTY_ONE_MANY, 0);
                    updateTidQtyLabel();
                }
            }
            if (binding != null && binding.scanEncodingStartTid != null) {
                binding.scanEncodingStartTid.setText("");
                //binding.scanEncodingStartTid.setIsViewControlEnabled(true);
            }
        });
    }

    private void updateTidQtyLabel() {
        int encQty = SharedPrefManager.getInt(AppConstants.ENCODE_CONFIG_QTY_ONE_MANY, 0);
        binding.scanEncodingStartTid.setLabel(binding.scanEncodingStartTid.getLabel() + (isBulkEncoding && SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_MANY) && encQty > 0 ? "  (" + scannedRFIDQRCodes.size() + " / " + encQty + ")" : ""));
    }

    /**
     * Start rfid reading.
     *
     * @param barcode the barcode
     */
    private void startRfidReading(String barcode, String tid, boolean isDelay) {
        AppCommonMethods.showLog("startRfidReading", barcode + "_" + tid);
        mappedEan = null;
        if (SharedPrefManager.getIsEANMapped()) {
            try {
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put(ParamConstants.EAN, barcode);
                callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, getString(R.string.progress_msg_check_map_data));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            new Handler().postDelayed(() -> {
                pickedTag = null;
                listPickedTags.clear();
                AppCommonMethods.showLog("performTidPick_barcode_tid", barcode + "_" + tid);
                mainViewModel.performTidPick("", scannedRFIDQRCodes);
            }, /*isDelay ? 50 :*/ 0);
        }
    }

    @Override
    protected void onReaderConfigured() {
        super.onReaderConfigured();
        setBarcodeObservers();
    }

    @Override
    public void apiCall(String action) {
        apiCall(action, null);
    }

    @Override
    public void apiCall(String action, Bundle args) {
        showLog("API", action);
        final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);

        if (isInternetConnected(context, false, true)) {
            try {
                if (isUpload)
                    showProgressDialog(getString(R.string.progress_msg_check_upload_data));
                JSONObject requestParams = new JSONObject();
                requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
                requestParams.put(ParamConstants.TYPE, AppConstants.ENCODE);
                requestParams.put(ParamConstants.EAN_TYPE, SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.EAN_TYPE, getString(R.string.btn_encode_config_ean_std)));
                requestParams.put(ParamConstants.IS_ONE_TO_MANY_RELATION, SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_MANY));//!isOneToOneRelation);
                requestParams.put(ParamConstants.IS_BARCODE_BARCODE_RFID, true);// isBarcodeBarcodeRFID);
                requestParams.put(ParamConstants.TARGET, sessionObject != null ? sessionObject.total : chkZero(binding.ctwInventoryStart.getTotal(), "-"));
                requestParams.put(ParamConstants.ACTION, action);
                requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
                if (sessionObject != null) {
                    requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
                    if (isUpload) {
                        JSONArray js = new JSONArray();
                        for (Inventory inventory : inventoryDao.getAllInventoryData(sessionObject.sessionId)) {
                            if (inventory != null) {
                                JSONObject dataobject = inventory.toJson(context);
                                if (dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                                    js.put(dataobject);
                            }
                        }
                        requestParams.put(ParamConstants.ITEMS, js);
                        //add Other Parameters for bulk uploading
                    }
                }
                callWebService(isUpload ? URLConstants.UPLOAD_ENCODING : URLConstants.SET_SESSION, requestParams, args, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload, false);
            } catch (JSONException e) {
                e.printStackTrace();
                hideProgressDialog();
            }
        }
    }

    @Override
    protected void isSessionOnChanged(final Boolean isSessionOn) {
        if (isSessionOn == null) return;
        if (chkNotNullTrue(isSessionOn) && is1stSessionStart) {
            is1stSessionStart = false;
            binding.llBtnStart.performClick();
        }
    /*final boolean isProcessOn = isProcessOn();
    
    binding.llBtnStart.setEnabled(!isProcessOn);
    showLog("isSessionOn_isProcessOn", isSessionOn + "_" + isProcessOn);
    binding.seekEncodingStartTarget.setEnabled(!isSessionOn);
    binding.scanEncodingStartBarcode.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(isSessionOn && !isProcessOn);
    binding.scanEncodingStartTid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(isSessionOn && !isProcessOn);
    binding.divFifo.setEnabled(binding.divFifo.getVisibility() == View.VISIBLE && !isProcessOn);*/
        updateView();
    }

    /**
     * Is session on boolean.
     *
     * @return the boolean
     */
    public boolean isSessionOn() {
        return chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
    }

    @Override
    protected void isPickOnChanged(Boolean isPickOn) {
        super.isPickOnChanged(isPickOn);
        if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if (isPickOn == null) {
        }
        else {
      /*final boolean isProcessOn = isProcessOn();
      binding.llBtnStart.setEnabled(!isProcessOn);
      binding.scanEncodingStartBarcode.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null,chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
      binding.scanEncodingStartTid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null,chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
      binding.divFifo.setEnabled(binding.divFifo.getVisibility() == View.VISIBLE && !isProcessOn);*/
            updateView();
        }
    }

    @Override
    protected void onPickDataChanged(Inventory pickData) {
        super.onPickDataChanged(pickData);
        if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if (pickData != null) {
            pickedTag = pickData;
            showLog("EAN", chkNull(mappedEan, binding.scanEncodingStartBarcode.getText()));
            showLog("EPC", pickedTag.epc);
            showLog("TID", pickedTag.tid);
            showLog("PC", pickedTag.pcdata);
            try {
                final boolean isCheckPasswordBasedOnEPC = !SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase("00000000") && SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, AppCommonMethods.isCheckEncPasswordBasedOnEPC) && (!pickedTag.epc.matches("(?i)^(00|30|0[A-C]|7[A-B]|BC).*$") || pickedTag.ean.matches("(?i)(" + AppConstants.UNKNOWN + "|" + AppConstants.NON_ENCODED + ")"));
                final boolean isCheckPasswordBeforeAPI = !SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase("00000000") && SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_API, AppCommonMethods.isCheckEncPasswordBeforeAPI);
                if (isCheckPasswordBeforeAPI && (!SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, AppCommonMethods.isCheckEncPasswordBasedOnEPC) || isCheckPasswordBasedOnEPC) && SharedPrefManager.getDeviceTypeValue() == AppCommonMethods.DeviceType.ZEBRA.getValue()) {
                    pickedTag.ean = binding.scanEncodingStartBarcode.getText().trim();
                    mainViewModel.readTagCurrentPassword(pickedTag);
                } else {
                    pickedTag.ean = chkNull(mappedEan, binding.scanEncodingStartBarcode.getText()).trim();
                    JSONObject requestParams = new JSONObject();
                    requestParams.put(ParamConstants.TYPE, AppConstants.ENCODE);
                    requestParams.put(ParamConstants.QTY, 1);
                    requestParams.put(ParamConstants.EAN, pickedTag.ean);
                    JSONArray js = new JSONArray();
                    JSONObject jsonObj = pickedTag.toJson(context);
                    if (jsonObj != null) js.put(jsonObj);
                    requestParams.put(ParamConstants.ITEMS, js);

                    if (AppCommonMethods.isStaticDebug()) {
                        final String sgtin = context.epcEncoderDecoder.getEpcFromBarcode(pickedTag.ean, true);//;SR_NO);

                        JSONObject jsonResponse = new JSONObject();
                        jsonResponse.put(ParamConstants.EPC, sgtin);
                        if (pickedTag.tid.length() > 24) {
                            jsonResponse.put(ParamConstants.TID, pickedTag.tid.substring(0, 24));
                        } else {
                            jsonResponse.put(ParamConstants.TID, pickedTag.tid);
                        }
                        jsonResponse.put(ParamConstants.IS_CHECK_PASSWORD_FIRST, AppCommonMethods.isCheckEncPasswordFirst);//!chkNull(pickedTag.epc, "").matches("^(30|BC|BD|7A|7B|(0[0A-D])).*$") || chkNull(pickData.ean, "").matches("(?i)(" + AppConstants.UNKNOWN + "|" + AppConstants.NON_ENCODED + ")"));//true);//temp settings (to be deleted later)
                        jsonResponse.put(ParamConstants.CURRENT_ACCESS_PASSWORD, "12345678");
                        jsonResponse.put(OLD_ACCESS_PASSWORDS, new JSONArray("[\"20141111\",\"20162222\",\"20183333\",\"20204444\",\"88888888\"]"));
                        handleResponse(URLConstants.GET_EPC_FOR_ENCODING, requestParams, jsonResponse, -1, true, null);
                    } else
                        callWebService(getEPCForEncodeUrl(), requestParams, getString(R.string.progress_msg_getting_data));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPickedListDataChanged(List<Inventory> listPicked) {
        super.onPickedListDataChanged(listPicked);
        if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if (isNonEmpty(listPicked)) showLog("listPicked", listPicked.toString());
        if (isNonEmpty(listPicked) && listPicked.size() == scannedRFIDQRCodes.size()) {
            try {
                listPickedTags.clear();
                listPickedTags.addAll(listPicked);
                AppCommonMethods.showLog("listPicked_sessionAction", "" + listPicked.get(0).sessionAction);
                //pickedTag.ean = chkNull(mappedEan, binding.scanEncodingStartBarcode.getText().toString());
                JSONObject requestParams = new JSONObject();
                requestParams.put(ParamConstants.TYPE, AppConstants.ENCODE);
                requestParams.put(ParamConstants.QTY, listPicked.size());
                requestParams.put(ParamConstants.EAN, chkNull(mappedEan, binding.scanEncodingStartBarcode.getText()));
                JSONArray js = new JSONArray();
                for (Inventory pickedTag : listPicked) {
                    JSONObject jsonObj = pickedTag.toJson(context);
                    if (jsonObj != null) js.put(jsonObj);
                }
                requestParams.put(ParamConstants.ITEMS, js);

                if (AppCommonMethods.isStaticDebug()) {
                    JSONObject jsonResponse = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    for (Inventory pickedTag : listPicked) {
                        JSONObject jsonObject = new JSONObject();
                        final String sgtin = context.epcEncoderDecoder.getEpcFromBarcode(pickedTag.ean, true);//SR_NO);
                        jsonObject.put(ParamConstants.EPC, sgtin);
                        if (pickedTag.tid.length() > 24) {
                            jsonObject.put(ParamConstants.TID, pickedTag.tid.substring(0, 24));
                        } else {
                            jsonObject.put(ParamConstants.TID, pickedTag.tid);
                        }
                        jsonArray.put(jsonObject);
                    }
                    jsonResponse.put(ParamConstants.SGTINS, jsonArray);
                    jsonResponse.put(ParamConstants.IS_CHECK_PASSWORD_FIRST, AppCommonMethods.isCheckEncPasswordFirst);//!chkNull(pickedTag.epc, "").matches("^(30|BC|BD|7A|7B|(0[0A-D])).*$") || chkNull(pickData.ean, "").matches("(?i)(" + AppConstants.UNKNOWN + "|" + AppConstants.NON_ENCODED + ")"));//true);//temp settings (to be deleted later)
                    jsonResponse.put(ParamConstants.CURRENT_ACCESS_PASSWORD, "12345678");
                    jsonResponse.put(OLD_ACCESS_PASSWORDS, new JSONArray("[\"20141111\",\"20162222\",\"20183333\",\"20204444\",\"88888888\"]"));
                    AppCommonMethods.showLog("requestParams", requestParams.toString());
                    handleResponse(URLConstants.GET_EPC_FOR_ENCODING, requestParams, jsonResponse, -1, true, null);
                } else
                    callWebService(getEPCForEncodeUrl(), requestParams, getString(R.string.progress_msg_getting_data));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void isEncodeOnChanged(Boolean isEncodeOn) {
        super.isEncodeOnChanged(isEncodeOn);
        if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if (isEncodeOn == null) {
        }
        else {
            showLog("isEncodeOn", "" + isEncodeOn);
            final boolean isProcessOn = isProcessOn();
            binding.llBtnStart.setEnabled(!isProcessOn);
            binding.scanEncodingStartBarcode.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null,chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
            binding.scanEncodingStartTid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null,chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
            binding.divFifo.setEnabled(binding.divFifo.getVisibility() == View.VISIBLE && !isProcessOn);
            if (chkNotNullFalse(isEncodeOn)) {
                if (sessionObject != null && chkNotNullTrue(mainViewModel.getIsEncodeDone().getValue())) {//Case Tag Write Success
                    showShortToast(R.string.success_encoding);
                    long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
                    if (total > 0 && getSize() == total)
                        context.showCustomSuccessDialog(R.string.success_enc_target_achieved);
                    apiCall(AppConstants.SESSION_ACTION_UPLOAD);
                    mainViewModel.getIsEncodeDone().postValue(false);
                } else clearBarcode();
            }
        }
    }

    private void updateView() {
        final boolean isProcessOn = isProcessOn();
        binding.llBtnStart.setEnabled(!isProcessOn);
        if (isProcessOn) binding.llEncodingConfig.setVisibility(View.GONE);
        binding.scanEncodingStartBarcode.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null,chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
        binding.scanEncodingStartTid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null,chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
        binding.divFifo.setEnabled(binding.divFifo.getVisibility() == View.VISIBLE && !isProcessOn);
    }

    @Override
    protected void onTriggerPressed() {
        showLog(this.getClass().getSimpleName(), "onTriggerPressed");
        binding.llBtnStart.performClick();
    }

    @Override
    protected void onDataSizeChanged(Integer size) {
        if (chkNull(size, 0) > 0 && sessionObject != null && !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))
            mainViewModel.startSession(sessionObject, false);
        listEncodedEans.clear();
        if (sessionObject != null) {
            listEncodedEans.addAll(inventoryDao.getEncodedEans(sessionObject.sessionId));
        }
        if (binding != null) {
            if (binding.listEncodingStartHistory != null && binding.listEncodingStartHistory.getAdapter() != null && binding.listEncodingStartHistory.getAdapter() instanceof RecyclerView.Adapter)
                binding.listEncodingStartHistory.getAdapter().notifyDataSetChanged();
            if (isBulkEncoding && SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(ENCODE_TYPE_MANY)) {
                updateTidQtyLabel();
            }
            //binding.txtEncodingStartScore.setText("" + chkNull(size, 0));
            binding.ctwInventoryStart.setScore(chkNull(size, 0));
            long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
     /* final boolean isEncCount = chkNull(size, 0) > 0;
      binding.progressEncodingStart.setVisibility(isEncCount && total > 0 ? View.VISIBLE : View.GONE);
      double per = total > 0 ? (chkNull(size, 0) * 100) / total : 0;
      int percentage = (int) per;
      binding.progressEncodingStart.setProgress(percentage);*/
            //binding.ctwInventoryStart(total > 0 && size >= total ? R.color.txtGreen : R.color.txt_number)
            binding.ctwInventoryStart.setTextColorScore(total > 0 && size >= total ? R.color.txtGreen : R.color.txt_number);
            // binding.txtEncodingStartScore.setTextColor(ContextCompat.getColor(context, total > 0 && size >= total ? R.color.txtGreen : R.color.txt_number));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isTopInStack()) {
            mainViewModel.getBarcodeReaderInstance(getSessionType());
            setBarcodeObservers();
            if (sessionObject != null) apiCall(AppConstants.SESSION_ACTION_RESUME);
        }
    }

    @Override
    public void onDestroyView() {
        sessionObject = null;
        mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
        super.onDestroyView();
    }

    /**
     * Set session action.
     *
     * @param action          the action
     * @param sessionId       the session id
     * @param sessionTime     the session time
     * @param activeUserCount the active user count
     * @param target          the target
     * @param args            the args
     */
    void setSessionAction(String action, String sessionId, String sessionTime, Integer activeUserCount, int target, Bundle args) {
        setActiveUsers(activeUserCount != null ? activeUserCount.intValue() : -2);
        final String totEncCount = sessionObject != null ? sessionObject.total : chkZero(binding.ctwInventoryStart.getTotal(), "0");
        showLog("totEncCount", totEncCount);
        //binding.txtEncodingStartScoreTotal.setText(totEncCount);
        if (binding.ctwInventoryStart != null) binding.ctwInventoryStart.setTotal(totEncCount);
        int total = -1;
        try {
            total = Integer.parseInt(totEncCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sessionObject != null && total >= 0)
            binding.seekEncodingStartTarget.setProgress(total / 10);
        else setTarget(target);
        if (sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)) {
            RFIDSession sessionObject = new RFIDSession();
            sessionObject.total = totEncCount;
            sessionObject.sessionType = AppCommonMethods.SessionType.ENCODING.getValue();
            sessionObject.sessionAction = AppCommonMethods.SessionAction.ENCODE.getValue();
            try {
                JSONObject jsonExtras = new JSONObject();
                jsonExtras.put(ParamConstants.TYPE, AppConstants.ENCODE);
                jsonExtras.put(ParamConstants.EAN_TYPE, SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.EAN_TYPE, getString(R.string.btn_encode_config_ean_std)));
                jsonExtras.put(ParamConstants.IS_ONE_TO_MANY_RELATION, SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_MANY));//!isOneToOneRelation);
                jsonExtras.put(ParamConstants.IS_BARCODE_BARCODE_RFID, true);// isBarcodeBarcodeRFID);
                jsonExtras.put(ParamConstants.TARGET, sessionObject != null ? sessionObject.total : chkZero(binding.ctwInventoryStart.getTotal(), "-"));
                sessionObject.extras = jsonExtras.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sessionObject.userId = SharedPrefManager.getUserID();
            Calendar cc = Calendar.getInstance();
            if (chkNull(sessionTime, "").length() > 0 && sessionTime.matches(DATE_TIME_FORMAT_PATTERN)) {
                try {
                    cc.setTime(new SimpleDateFormat(DATE_TIME_FORMAT).parse(sessionTime));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
            cc.add(Calendar.HOUR_OF_DAY, chkZero(sessionValidTill, 48));
            sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
            sessionObject.sessionId = chkNull(sessionId, mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc));
            setSessionObject(sessionObject);
            is1stSessionStart = is1stSessionStart != null;
            mainViewModel.startSession(sessionObject, false);
        } else if (sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_RESUME))
            mainViewModel.startSession(sessionObject, false);
        else if (sessionObject != null && !action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")")) {
            mainViewModel.stopSession(sessionObject, action.matches("(?i)(" + AppConstants.SESSION_ACTION_DISCARD + ")"));
            if (args != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_PAUSE)) {
                showLog("allowBtnClick111", "" + allowBtnClick);
                final Serializable obj = extractSerializable(args, MenuModel.class);
                final MenuModel menuModel = obj != null && obj instanceof MenuModel ? (MenuModel) obj : null;
                final String ean = extractString(args, AppConstants.EAN, "");
                if (menuModel != null)
                    handleFragmentRedirection(menuModel == null ? new ProductDetailsFragment() : null, menuModel, args);
                else if (isNonEmpty(ean)) {
                    try {
                        JSONObject jsonRequest = new JSONObject();
                        jsonRequest.put(ParamConstants.EAN, ean);
                        jsonRequest.put(ParamConstants.EPC, "");
                        jsonRequest.put(ParamConstants.TID, "");
                        Bundle args1 = new Bundle();
                        final MenuModel menuSearchDetails = AppDatabase.getMenuDao(context).getMenuByCode(AppConstants.MENU_CODE_SER_PROD);
                        args1.putString(AppConstants.EAN, ean);
                        args1.putString(AppConstants.TITLE, "Product Details");
                        args1.putString(AppConstants.TITLE_LOGO_URL, menuSearchDetails != null ? menuSearchDetails.getScreenImageUrl() : "");
                        args1.putInt(AppConstants.TITLE_LOGO_RES_ID, menuSearchDetails != null ? menuSearchDetails.getScreenIconId(context) : R.drawable.ic_ser_prod);
                        final String productInfoUrl = getProductInfoUrl();
                        if (productInfoUrl.equalsIgnoreCase(URLConstants.GET_PRODUCT_INFO_BY_SKU)) {
                            JSONArray js = new JSONArray();
                            js.put(jsonRequest.get(ParamConstants.EAN));
                            jsonRequest.put(ParamConstants.ITEMS, js);
                        }
                        callWebService(productInfoUrl, jsonRequest, args1, getString(R.string.progress_msg_getting_data), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.equalsIgnoreCase(AppConstants.SESSION_ACTION_SAVE)) {
                context.showCustomAlertDialog("", getString(R.string.success_session_save), true, true, getString(R.string.btn_ok), (dialogInterface, i) -> popBackStack());
            } else context.popBackStack();
        }
    }

    @Override
    public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args) {
        super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
        try {
            switch (url) {
                case URLConstants.GET_PRODUCT_INFO:
                case URLConstants.GET_PRODUCT_INFO_BY_SKU:
                    if (isSuccess) {
                        ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
                        if (productModel != null) {
                            if (isStaticDebug())
                                productModel.setEan(extractString(jsonRequest, ParamConstants.EAN, ""));
                            productModel.setSessionType(AppCommonMethods.SessionType.SEARCH.getValue());
                            if (args == null) args = new Bundle();
                            args.putSerializable(productModel.getClass().getSimpleName(), productModel);
                            handleFragmentRedirection(new ProductDetailsFragment(), args);
                        }
                    } else apiCall(SESSION_ACTION_RESUME);
                    break;
                case URLConstants.SET_SESSION:
                    final String action = extractString(jsonRequest, ParamConstants.ACTION, sessionObject == null ? AppConstants.SESSION_ACTION_START : AppConstants.SESSION_ACTION_STOP);
                    String sessionId = null, sessionTime = null;
                    showLog("allowBtnClick111", "" + allowBtnClick);
                    if (isSuccess && jsonResponse != null) {
                        sessionId = extractString(jsonResponse, ParamConstants.SESSION_ID);
                        sessionTime = extractString(jsonResponse, ParamConstants.SESSION_TIME);
                        sessionValidTill = extractInt(jsonResponse, ParamConstants.SESSION_VALID_TILL, 48);
                        activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, activeUsers);
                        target = extractInt(jsonResponse, ParamConstants.TARGET, target);
                    }
                    setSessionAction(action, sessionId, sessionTime, activeUsers, target, args);
                    break;
                case URLConstants.GET_MAPPED_EAN:
                    if (isSuccess) {
                        final String ean = extractString(jsonResponse, ParamConstants.EAN, extractString(jsonRequest, ParamConstants.EAN));
                        mappedEan = extractString(jsonResponse, ParamConstants.MAPPED_EAN);
                        //use this Mapped EAN for Search/Encode
                        pickedTag = null;
                        mainViewModel.performPick("");
                    }
                    break;
                case URLConstants.GET_EPC_FOR_ENCODING:
                case URLConstants.GET_EPC_FOR_ENCODE:
                    if (isSuccess) {
                        final String currentTagPassword = extractString(args, ParamConstants.CURRENT_ACCESS_PWD, "");
                        final JSONArray jsonArraySgtins = extractJSONArray(jsonResponse, ParamConstants.SGTINS);
                        final JSONObject jsonSgtins = jsonArraySgtins != null && jsonArraySgtins.length() > 0 ? jsonArraySgtins.getJSONObject(0) : null;
                        final String newTid = extractString(jsonSgtins, ParamConstants.TID, extractString(jsonResponse, ParamConstants.TID));
                        final String newEpc = extractString(jsonSgtins, ParamConstants.EPC, extractString(jsonResponse, ParamConstants.EPC));

                        context.saveTagWritePasswords(jsonResponse);

                        final String barcode = extractString(jsonRequest, ParamConstants.EAN, "").trim();
                        if (pickedTag != null && isNonEmpty(newEpc)) {
                            if (isNonEmpty(barcode)) pickedTag.ean = barcode;
                            pickedTag.newEpc = newEpc;
                            startEncoding(pickedTag, currentTagPassword);
                        } else if (isNonEmpty(listPickedTags)) {
                            List<Inventory> pickedTags = new ArrayList<>(0);
                            for (int i = 0; i < jsonArraySgtins.length(); i++) {
                                final JSONObject jsonSgtin = jsonArraySgtins.getJSONObject(i);
                                String tid = extractString(jsonSgtin, ParamConstants.TID, extractString(jsonResponse, ParamConstants.TID));
                                showLog("newTid", tid);
                                int index = listPickedTags.indexOf(new Inventory(sessionObject.sessionId, sessionObject.sessionType, AppCommonMethods.SessionAction.PICK.getValue(), "", tid));
                                showLog("index", "" + index);
                                final Inventory pickedTag = index >= 0 ? listPickedTags.get(index) : null;
                                if (pickedTag != null) {
                                    if (isNonEmpty(barcode)) pickedTag.ean = barcode;
                                    pickedTag.newEpc = extractString(jsonSgtin, ParamConstants.EPC, extractString(jsonResponse, ParamConstants.EPC));
                                    showLog("newEPC", newEpc);
                                    pickedTags.add(pickedTag);
                                }
                            }
                            startEncoding(pickedTags, currentTagPassword);
                        } else {
                            hideProgressDialog();
                            context.showCustomErrDialog(R.string.err_encoding_write_fail);
                            clearBarcode();
                        }
                    } else {
                        clearBarcode();
                    }
                    break;
                case URLConstants.UPLOAD_ENCODING:
                    final JSONArray js = AppCommonMethods.isUpdateUploadStatusBasedOnTID ? extractJSONArray(jsonRequest, ParamConstants.ITEMS) : null;
                    final Set<String> tids = new HashSet<String>(0);
                    if (isNonEmpty(js)) {
                        for (int i = 0; i < js.length(); i++) {
                            final String tid = extractString(js.getJSONObject(i), ParamConstants.TID, "").trim();
                            if (isNonEmpty(tid))
                                tids.add(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid);
                        }
                    }
                    if (isSuccess) {
                        final long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
                        String dialogMsg = extractString(jsonResponse, ParamConstants.MESSAGE, getString(total > 0 && getSize() == total ? R.string.success_enc_target_achieved : R.string.success_encoding));
                        if (total > 0 && getSize() == total && !dialogMsg.toLowerCase().contains("target") && !dialogMsg.toLowerCase().contains("achieve"))
                            dialogMsg = dialogMsg + "\n" + getString(R.string.success_enc_target_achieved);
                        //context.showCustomSuccessDialog(dialogMsg);
                        if (isNonEmpty(tids)) {
                            inventoryDao.updateUploaded(sessionObject.sessionId, tids);
                            uploadInventoryDao.updateUploaded(sessionObject.sessionId, tids);
                        } else {
                            inventoryDao.updateUploaded(sessionObject.sessionId);
                            uploadInventoryDao.updateUploaded(sessionObject.sessionId);
                        }
                        uploadInventoryDao.deleteUploaded();
                        clearBarcode();
                        //clearBarcode(isNonEmpty(scannedRFIDQRCodes) && inventoryDao.isTidsPresent(sessionObject.sessionId, scannedRFIDQRCodes, scannedRFIDQRCodes.size()));
                    } else {
                        //clearBarcode();
                        clearBarcode(isNonEmpty(scannedRFIDQRCodes) && inventoryDao.isTidsPresent(sessionObject.sessionId, scannedRFIDQRCodes, scannedRFIDQRCodes.size()));
                        if (isNonEmpty(tids)) {
                            inventoryDao.updateUploadRetryCount(sessionObject.sessionId, tids);
                            uploadInventoryDao.updateUploadRetryCount(sessionObject.sessionId, tids);
                        } else {
                            inventoryDao.updateUploadRetryCount(sessionObject.sessionId);
                            uploadInventoryDao.updateUploadRetryCount(sessionObject.sessionId);
                        }
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