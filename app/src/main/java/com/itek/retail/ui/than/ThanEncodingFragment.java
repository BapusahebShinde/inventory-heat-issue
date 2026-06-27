package com.itek.retail.ui.than;

import static com.itek.retail.apis.ParamConstants.EPC;
import static com.itek.retail.apis.ParamConstants.OLD_ACCESS_PASSWORDS;
import static com.itek.retail.apis.ParamConstants.TID;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT_PATTERN;
import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.formatDoubleStr2Decimals;
import static com.itek.retail.common.AppCommonMethods.getEanRegex;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;
import static com.itek.retail.common.AppCommonMethods.isUse24LengthTIDForUpload;
import static com.itek.retail.common.AppCommonMethods.isUseAPICallForSessionEncode;
import static com.itek.retail.common.AppCommonMethods.isVerifyEncodeRedirection;
import static com.itek.retail.common.AppConstants.SESSION_ACTION_RESUME;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.EncodingHistoryAdapter;
import com.itek.retail.adapter.OmniPickedListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.databinding.DialogOmniEpcSearchBinding;
import com.itek.retail.databinding.FragmentEncodingThanBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.sgtin.EPCEncoderDecoder;
import com.itek.retail.ui.customviews.DashboardDataView;
import com.itek.retail.ui.customviews.InputView;
import com.itek.retail.ui.encoding.EncodingStartViewModel;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.productsearch.ProductDetailsFragment;
import com.itek.retail.ui.search.unencoded.SearchUnencodedFragment;

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
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Encoding start fragment.
 */
public class ThanEncodingFragment extends RFIDSessionFragment {

    //boolean isOneToOneRelation = true, isBarcodeBarcodeRFID = false;
    int activeUsers = 0, sessionValidTill = 48, target = -1;
    InventoryDao inventoryDao;
    UploadInventoryDao uploadInventoryDao;
    Inventory pickedTag;
    Boolean is1stSessionStart = false;
    private String encBarcode = "";
    private EncodingStartViewModel mViewModel;
    private FragmentEncodingThanBinding binding;
    private final List<EanQty> listEncodedEans = new ArrayList<>(0);
    private final List<Inventory> listEncodedTags = new ArrayList<>(0);
    //Changes
    //private String mappedEan;
    private boolean isEpcForEncodingAPICalled = false;
    private DialogOmniEpcSearchBinding dialogOmniEpcSearchBinding;
    private boolean showMarkFoundBtn = false;
    private boolean isVerifyMode = false;
    private Timer uploadTimer;
    private String originalThanLength = "";
    private final boolean isThanEncoding = true;
    private boolean isGetOriginalLengthFromField = false;
    private boolean isClearOriginalLengthInEncForEachTag = false;
    private boolean isGetOriginalLengthBeforeAPI = false;

    /**
     * Instantiates a new Encoding start fragment.
     */
    public ThanEncodingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ((MainActivity) context).getRfidViewModel();
        inventoryDao = AppDatabase.getInventoryDao(context);
        uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
        mainViewModel.getBarcodeReaderInstance(getSessionType());

        if (isAllowDirectionalSearch && SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY))
            mainViewModel.getSensorAndStart();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(EncodingStartViewModel.class);
        binding = FragmentEncodingThanBinding.inflate(inflater, container, false);

        activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
        sessionValidTill = extractInt(getArguments(), AppConstants.SESSION_VALID_TILL, 48);
        target = extractInt(getArguments(), AppConstants.TARGET, -1);

        isGetOriginalLengthFromField = extractBoolean(getArguments(), ParamConstants.IS_GET_ORIGINAL_LENGTH_IN_THAN_ENC_FROM_FIELD, SharedPrefManager.getBoolean(ParamConstants.IS_GET_ORIGINAL_LENGTH_IN_THAN_ENC_FROM_FIELD, AppCommonMethods.isGetOriginalLengthInEncFromField));
        isGetOriginalLengthBeforeAPI = !isGetOriginalLengthFromField && extractBoolean(getArguments(), ParamConstants.IS_GET_ORIGINAL_LENGTH_IN_THAN_ENC_BEFORE_API, SharedPrefManager.getBoolean(ParamConstants.IS_GET_ORIGINAL_LENGTH_IN_THAN_ENC_BEFORE_API, AppCommonMethods.isGetOriginalLengthInEncBeforeAPI));
        isClearOriginalLengthInEncForEachTag = extractBoolean(getArguments(), ParamConstants.IS_CLEAR_ORIGINAL_LENGTH_IN_THAN_ENC_FOR_EACH_TAG, SharedPrefManager.getBoolean(ParamConstants.IS_CLEAR_ORIGINAL_LENGTH_IN_THAN_ENC_FOR_EACH_TAG, AppCommonMethods.isClearOriginalLengthInEncForEachTag));

        setActiveUsers(activeUsers);
        binding.scanEncodingStartRfid.edtCode.requestFocus();
        hideKeyboard();

        toggleVerifyMode(isVerifyMode);

        binding.divFifo.setVisibility(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) ? View.VISIBLE : View.GONE);

        binding.seekEncodingStartTarget.setThumb(getResources().getDrawable(R.drawable.ic_target));

        binding.ivThanLen.setUnit(SharedPrefManager.getString(ParamConstants.LENGTH_UNIT, AppCommonMethods.defLengthUnitThan));

        //SharedPrefManager.setEncodeRelationType(AppConstants.ENCODE_TYPE_ONE);
        //isOneToOneRelation =true;// SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_ONE);
        //isBarcodeBarcodeRFID = false;//SharedPrefManager.getEncodeType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_BARCODE_BARCODE_RFID);

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
                //int encoded = getSize();//AppCommonMethods.parseInt(binding.txtEncodingStartScore.getText().toString());
                int total = progress * 10;
                //binding.txtEncodingStartScoreTotal.setText("" + total);
                if (binding.ctwInventoryStart != null) binding.ctwInventoryStart.setTotal(total);
                //showLog("encoded", "" + encoded);
                showLog("target", "" + total);
             //   binding.progressEncodingStart.setProgress(encoded * 100 / (total > 0 ? total : 1));
              //  binding.progressEncodingStart.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
                //binding.txtEncodingStartScoreTotal.setVisibility(/*isOneToOneRelation &&*/ total > 0 ? View.VISIBLE : View.GONE);
//                if (binding.ctwInventoryStart != null) binding.ctwInventoryStart.setVisibility(/*isOneToOneRelation &&*/ total > 0 ? View.VISIBLE : View.GONE);
                //binding.divEncodingStartScore.setVisibility(/*isOneToOneRelation &&*/ total > 0 ? View.VISIBLE : View.GONE);
                binding.lblEncodingTotal.setText(total > 0 ? R.string.lbl_encode_target : R.string.lbl_total);

               /* //code to update constraints
                final ConstraintLayout root = binding.clStartEncodingScore;
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(root);
                if (binding.ctwInventoryStart != null) {
                    // constraintSet.clear(binding.txtEncodingStartScore.getId(), ConstraintSet.TOP);
                    constraintSet.clear(binding.ctwInventoryStart.getId(), ConstraintSet.TOP);
                    // constraintSet.clear(binding.txtEncodingStartScore.getId(), ConstraintSet.BOTTOM);
                    constraintSet.clear(binding.ctwInventoryStart.getId(), ConstraintSet.BOTTOM);
                }
                if (total <= 0 && binding.ctwInventoryStart != null) {
                    constraintSet.connect(binding.ctwInventoryStart.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                    constraintSet.connect(binding.ctwInventoryStart.getId(), ConstraintSet.BOTTOM, total > 0 ? binding.divEncodingStartScore.getId() : ConstraintSet.PARENT_ID, total > 0 ? ConstraintSet.TOP : ConstraintSet.BOTTOM);
                    constraintSet.applyTo(root);
                }*/
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

        binding.scanEncodingStartRfid.setImgScanOnClickListener(view -> {
            final boolean isProcessOn = isProcessOn();
            if (!isProcessOn) {
                if (isNullOrEmpty(binding.scanEncodingStartRfid.getText())) {
                    context.dismissCustomAlertDialog();
                    final Boolean isSessionOn = mainViewModel.getIsSessionOn().getValue();
                    if (!chkNotNullTrue(isSessionOn) && sessionObject == null)
                        apiCall(AppConstants.SESSION_ACTION_START);
                    else mainViewModel.softScan();
                } else if (isNonEmpty(binding.scanEncodingStartRfid.getText())) {
                    encBarcode = "";
                    if (!isGetOriginalLengthFromField) clearThanOriginalLength();
                    binding.scanEncodingStartRfid.setText("");
                }
            }
        });

        binding.llBtnStart.setOnClickListener(v -> {
            context.dismissCustomAlertDialog();
            final Boolean isSessionOn = mainViewModel.getIsSessionOn().getValue();
            if (!chkNotNullTrue(isSessionOn) && sessionObject == null)
                apiCall(AppConstants.SESSION_ACTION_START);
            else if (chkNotNullTrue(isSessionOn) && sessionObject != null && !isProcessOn() && checkReaderConnected()) {
                if (chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
                    mainViewModel.stopInventory();
                else if (!isProcessOn() && (!isGetOriginalLengthFromField || binding.ivThanLen.validate())) {
                    AppCommonMethods.showLog(ThanEncodingFragment.this.getClass().getSimpleName(), "llBtnStart->Clicked");
                    pickedTag = null;
                    if (!isGetOriginalLengthFromField) clearThanOriginalLength();
                    else
                        originalThanLength = formatDoubleStr2Decimals(binding.ivThanLen.getText().trim());
                    String barcode = AppCommonMethods.getLeftZeroReplacedString(context, chkNull(binding.scanEncodingStartRfid.getText(), ""));
                    if (isNullOrEmpty(barcode)) {
                        showLog("ENCODING_Barcode_SCAN", "Start");
                        binding.scanEncodingStartRfid.performScan();
                        startRfidReading("");
                    } else if (barcode.matches(AppCommonMethods.getEanRegex())) {//AppConstants.REGEX_NUM_BARCODE) && (SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_BOTH) || SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_STD) == SGTIN96.IsValidGtin(ApplicationCommonMethods.getZeroFilledString(barcode, 14))))
                        encBarcode = barcode;
                        binding.scanEncodingStartRfid.setIsViewControlEnabled(false, true);
                        startRfidReading(barcode);
                    } else {
                        context.showCustomErrDialog(String.format(getString(R.string.err_invalid_barcode), getTypeCharCode(), barcode));
                        clearBarcode();
                    }
                }
            }
        });

        binding.scanEncodingStartRfid.setGoBtn(binding.llBtnStart);

        binding.listEncodingStartHistory.setAdapter(new EncodingHistoryAdapter((MainActivity) context, ThanEncodingFragment.this, listEncodedEans));
        binding.listEncodingStartHistory.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));

        if (/*isOneToOneRelation &&*/ !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && sessionObject == null && chkNull(target, -1) >= 0) {
            setTarget(target);
            is1stSessionStart = null;
            if (!AppDatabase.getMenuDao(context).hasMenu(AppConstants.MENU_CODE_ENC_CONFIG))
                SharedPrefManager.setString(SharedPrefManager.SharedPrefKeys.ENCODING_RELATION_TYPE, AppConstants.ENCODE_TYPE_ONE);
            binding.llBtnStart.performClick();
        } else if (/*isOneToOneRelation &&*/ !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && sessionObject == null && chkNull(target, -1) < 0) {
            if (!AppDatabase.getMenuDao(context).hasMenu(AppConstants.MENU_CODE_ENC_CONFIG))
                SharedPrefManager.setString(SharedPrefManager.SharedPrefKeys.ENCODING_RELATION_TYPE, AppConstants.ENCODE_TYPE_ONE);
            //binding.seekEncodingStartTarget.setProgress(AppCommonMethods.parseInt(binding.txtEncodingStartScoreTotal.getText().toString()) / 10);
            assert binding.ctwInventoryStart != null;
            binding.seekEncodingStartTarget.setProgress(AppCommonMethods.parseInt(binding.ctwInventoryStart.getTotal().toString()) / 10);
        } else if (/*isOneToOneRelation &&*/ !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && sessionObject != null && chkNull(target, -1) < 0) {
           // final String totEncCount = sessionObject != null ? sessionObject.total : chkZero(binding.txtEncodingStartScoreTotal.getText().toString(), "0");
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

        binding.header.imgConfigSync.setVisibility(SharedPrefManager.getBoolean(ParamConstants.IS_CONFIG_ENC_START, false) ? View.VISIBLE : View.GONE);
        binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
        binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isProcessOn()) return;
                binding.llEncodingConfig.setVisibility(binding.llEncodingConfig.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        binding.llEncodingConfigRelation.setVisibility(SharedPrefManager.getBoolean(ParamConstants.IS_CONFIG_ENC_START, false) ? View.VISIBLE : View.GONE);
        selectButtonClick(binding.llEncodingConfigRelation, SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.ENCODING_RELATION_TYPE, AppConstants.ENCODE_TYPE_ONE));

        binding.btnConfigRelationOneToOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButtonClick((LinearLayout) v.getParent(), ((Button) v).getText().toString());
            }
        });
        binding.btnConfigRelationOneToMany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButtonClick((LinearLayout) v.getParent(), ((Button) v).getText().toString());
            }
        });

        binding.btnVerifyEncoding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null && v.getVisibility() == View.VISIBLE && SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY) && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && sessionObject != null && !isProcessOn() && checkReaderConnected() && getSize() > 0) {
                    if (inventoryDao.getNonVerifiedCount(sessionObject.sessionId) <= 0) {
                        context.showCustomErrDialog("Already Verified!");
                        return;
                    }
                    //TODO start Inventory OR Redirect to another fragment
                    //Option 1 (start Inventory for verify)
                    if (!isVerifyEncodeRedirection) mainViewModel.startInventory();
                    else if (true) {
                        //Option 2 (toggle 2 UI on same fragment (Visible + Gone)
                        if (!isVerifyMode) toggleVerifyMode(true);
                    } else {
                        //Option 3 (call another fragment)
                        Bundle args = ThanEncodingFragment.this.getArguments();
                        args.putString(AppConstants.UNENCODED_SEARCH_TYPE, AppConstants.UNENCODED_SEARCH_TYPE_OFFLINE);
                        args.putString(ParamConstants.ZONE, AppConstants.ALL);
                        args.putString(ParamConstants.SESSION_ID, sessionObject.sessionId);
                        args.putInt(ParamConstants.SESSION_TYPE, sessionObject.sessionType);
                        context.loadFragment(new SearchUnencodedFragment(), args);
                        //context.loadFragment(new VerifyEncodingFragment());//, args);
                    }
                }
            }
        });

        binding.listVerifyEncoded.setAdapter(new OmniPickedListAdapter(context, ThanEncodingFragment.this, listEncodedTags));
        binding.listVerifyEncoded.setLayoutManager(new LinearLayoutManager(context));
        binding.llBtnStartInv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null && v.getVisibility() == View.VISIBLE) {
                    context.dismissCustomAlertDialog();
                    final Boolean isSessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
                    if (isSessionOn) toggleInventory();
                }
            }
        });

        binding.ivThanLen.setVisibility(isGetOriginalLengthFromField ? View.VISIBLE : View.GONE);

        callScheduler();

        return binding.getRoot();
    }

    private void callScheduler() {
        if (!SharedPrefManager.getBoolean(ParamConstants.IS_ENC_UPLOAD_BY_SCHEDULER, AppCommonMethods.isUseSchedulerForWritenTagUpload))
            return;
        if (uploadTimer == null) {
            uploadTimer = new Timer();
            uploadTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //showLog("scheduler", "uploadTimer");
                    if (isInternetConnected(context, false, false) && (SharedPrefManager.getBoolean(ParamConstants.IS_ENC_UPLOAD_BY_WHILE_PROCESSING, AppCommonMethods.isAllowBackgroundWritenTagUploadWhileProcessing) || !isProcessOn()))
                        context.uploadWrittenInventoryTags(true);
                }
            }, 1000, 15000);
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
            target = sessionObject != null ? AppCommonMethods.parseInt(sessionObject.total) : chkNull(ThanEncodingFragment.this.target, -1) >= 0 ? ThanEncodingFragment.this.target : target;
            if (sessionObject == null && chkNull(target, -1) >= 0) {
                if (binding.ctwInventoryStart != null) binding.ctwInventoryStart.setScore(target);
                binding.seekEncodingStartTarget.setProgress(target / 10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    }
                }
        }
    }

    void generateSession() {
        RFIDSession sessionObject = mainViewModel.updateActiveSessionFlag(this.getSessionType(), true);
        if (sessionObject == null || isNullOrEmpty(sessionObject.sessionId) || sessionObject.sessionType <= 0 || sessionObject.sessionType != this.getSessionType().getValue()) {
            Calendar cc = Calendar.getInstance();
            sessionObject = new RFIDSession();
            sessionObject.sessionType = this.getSessionType().getValue();
            sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
            cc.add(Calendar.HOUR_OF_DAY, 24);
            sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
            sessionObject.sessionId = mainViewModel.generateOfflineSessionId(this.getSessionType(), cc);
            sessionObject.isRunning = true;
            sessionObject.isUploading = false;
            AppDatabase.getRIFDSessionDao(context).insert(sessionObject);
        }
        setSessionAction(AppConstants.SESSION_ACTION_START, sessionObject.sessionId, sessionObject.sessionStartTime, activeUsers, target, null);
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
        setUploadCountObserver();
        setVerifyCountObserver();
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
      if(isProcessOn) binding.llEncodingConfig.setVisibility(View.GONE);
      binding.scanEncodingStartRfid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
      binding.divFifo.setEnabled(!isProcessOn);*/
            updateView();
        });

        mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getBarcodeData().observe(getViewLifecycleOwner(), barcode -> {
            showLog(ThanEncodingFragment.this.getClass().getSimpleName() + "_barcodeData", chkNull(barcode, ""));
            if (!isTopInStack()) return;
            //showLog("barcodeData1", "" + chkNull(barcode, ""));
            if (isNonEmpty(barcode)) {
                mainViewModel.getBarcodeData().setValue("");
                setBarcode(barcode);
            }
        });
    }

    @Override
    public void startEPCSearch(Inventory inventory) {
        if (!isProcessOn() && inventory != null) {
            final boolean isVerifyEncoding = true;
            final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            setAlertDialogCustomTitle(alertDialog, R.string.search);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            final int wid = (context.isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / 2;

            JSONObject jsonExtras1 = null;
            try {
                jsonExtras1 = new JSONObject();
                jsonExtras1.put(ParamConstants.EAN, chkNull(inventory.newEpc, inventory.epc));
                jsonExtras1.put(ParamConstants.EAN_QTY, 1);
                jsonExtras1.put(ParamConstants.IS_EAN_SEARCH, false);
                jsonExtras1.put(ParamConstants.SESSION_TYPE, getSessionType().name());
                jsonExtras1.put(ParamConstants.TYPE, AppConstants.ENCODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            final JSONObject jsonExtras = jsonExtras1;
            DialogOmniEpcSearchBinding binding = DialogOmniEpcSearchBinding.inflate(LayoutInflater.from(context), null, false);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(wid, wid);
            binding.rgSearchType.setVisibility(isVerifyEncoding ? View.VISIBLE : View.GONE);
            binding.clOmniEPCSearch.setLayoutParams(llParams);
            binding.btnDecode.setText(R.string.btn_mark_found);
            binding.btnDecode.setTag(inventory);
            showMarkFoundBtn = false;
            binding.btnDecode.setVisibility(!isProcessOn() && showMarkFoundBtn && !inventory.isFound ? View.VISIBLE : View.GONE);
            binding.btnDecode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Mark as found
                    if (view != null && view.getVisibility() == View.VISIBLE && !inventory.isFound) {
                        try {
                            inventory.isFound = true;
                            inventoryDao.updateInventoryData(inventory);
                            binding.btnDecode.setTag(inventory);
                            showMarkFoundBtn = false;
                            binding.btnDecode.setVisibility(showMarkFoundBtn && !isProcessOn() && !inventory.isFound ? View.VISIBLE : View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            binding.llBtnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.dismissCustomAlertDialog();
                    if (!isProcessOn() && checkReaderConnected()) {
                        if (binding.rgSearchType.getVisibility() == View.VISIBLE && binding.rgSearchType.findViewById(binding.rgSearchType.getCheckedRadioButtonId()).getTag().equals(getString(R.string.search_type_tid)) && isNonEmpty(inventory.tid)) {
                            //update search log with tid
                            if(jsonExtras!=null) {
                                try {
                                    jsonExtras.put(ParamConstants.EAN, inventory.tid);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", inventory.tid, 1, getSessionType().name(), "", jsonExtras);
                            mainViewModel.performTIDBasedSearch(inventory.tid);
                        }else {
                            if(jsonExtras!=null) {
                                try {
                                    jsonExtras.put(ParamConstants.EAN, chkNull(inventory.newEpc, inventory.epc));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(inventory.newEpc, inventory.epc), 1, getSessionType().name(), "", jsonExtras);
                            mainViewModel.performEPCBasedSearch(chkNull(inventory.newEpc, inventory.epc));
                        }
                        searchStartTime = System.currentTimeMillis();
                    } else if (chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
                        mainViewModel.stopInventory();
                }
            });
            alertDialog.setView(binding.getRoot());
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    dialogOmniEpcSearchBinding = binding;
                    /*if (searchLog == null) {
                        JSONObject jsonExtras = null;
                        try {
                            jsonExtras = new JSONObject();
                            jsonExtras.put(ParamConstants.EAN, chkNull(inventory.newEpc,inventory.epc));
                            jsonExtras.put(ParamConstants.EAN_QTY, 1);
                            jsonExtras.put(ParamConstants.IS_EAN_SEARCH, false);
                            jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
                            jsonExtras.put(ParamConstants.TYPE, AppConstants.ENCODE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(inventory.newEpc,inventory.epc), 1, getSessionType().name(), "", jsonExtras);
                    }*/
                }
            });
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    if (chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
                        mainViewModel.stopInventory();
                    if (searchLog != null) searchLog = null;
                    dialogOmniEpcSearchBinding = null;
                }
            });
            alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    context.handleTriggerKeyEvent(keyCode, event);
                    return false;
                }
            });
            alertDialog.show();
        } else if (isProcessOn() && inventory != null) showShortToast(R.string.not_allowed);
        //showShortToast(String.format(getString(R.string.err_op_not_allowed),getTypeCharCode(), AppCommonMethods.SessionType.INVENTORY.name()));
    }

    @Override
    protected void isSearchOnChanged(Boolean isSearchOn) {
        super.isSearchOnChanged(isSearchOn);
        if (isSearchOn == null) {
        }
        else {
            updateView();
            updateLists();
            if (!isSearchOn) stopTimer();
            else
                startTimer(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null ? dialogOmniEpcSearchBinding.clOmniChannelEpcSearch : null, dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.imgSearchDir != null ? dialogOmniEpcSearchBinding.imgSearchDir : null);
            if (dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
                dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.setEnableCheck(false);
            if (dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null && dialogOmniEpcSearchBinding.btnDecode != null) {
                dialogOmniEpcSearchBinding.llBtnStart.toggle(isSearchOn);
                final Object tag = dialogOmniEpcSearchBinding.btnDecode.getTag();
                final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
                dialogOmniEpcSearchBinding.btnDecode.setVisibility(showMarkFoundBtn && inventory != null && !inventory.isFound && !isSearchOn ? View.VISIBLE : View.GONE);
            }
        }
    }

    /**
     * Set default search views.
     */
    @Override
    protected void setDefaultSearchViews() {
        super.setDefaultSearchViews();
        if (dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
            dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.resetToDefault();
    }

    @Override
    protected void updateSearchUI(int result) {
        super.updateSearchUI(result);
        final Object tag = dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.btnDecode != null ? dialogOmniEpcSearchBinding.btnDecode.getTag() : null;
        final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
    /*if(!showMarkFoundBtn && result >= AppCommonMethods.markFoundPercentAlienSearch && inventory != null && !inventory.isFound)
      showMarkFoundBtn = true;*/
    }

    @Override
    protected void onSearchPercentageChanged(Integer searchPercent, String searchRssi) {
        super.onSearchPercentageChanged(searchPercent, searchRssi);
        updateView();
        updateLists();
    }

    @Override
    public void updateLists() {
        super.updateLists();
        if (sessionObject != null) {
            AppCommonMethods.showLog("sessionId", sessionObject.sessionId);
            AppCommonMethods.showLog("sessionType", AppCommonMethods.SessionType.get(sessionObject.sessionType).name());
            listEncodedTags.clear();
            listEncodedTags.addAll(inventoryDao.getEncVerifyList(sessionObject.sessionId));
            setFoundTotalText(binding.ddvVerifyEncodingTotalFound, inventoryDao.getZonewiseFound(sessionObject.sessionId, null), listEncodedTags.size());
        }
        binding.listVerifyEncoded.getAdapter().notifyDataSetChanged();
        // binding.llBtnStart.setVisibility(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) || (!chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) ? View.VISIBLE : View.GONE);
    }

    private void setFoundTotalText(DashboardDataView ddv, int found, int total) {
        final int len = String.valueOf(AppCommonMethods.greater(found, total)).length();
        if (len > 3) {
            final int loopLimit = len / 2;
            final String format = "<small>%s</small>";
            String appendFormat = format;
            for (int i = 0; i < loopLimit; i++)
                appendFormat = appendFormat.replaceFirst(">%s</", ">" + format + "</");
            ddv.setText(String.format(appendFormat, found) + "/" + String.format(appendFormat, total));
        } else ddv.setText(found + "/" + total);
    }

    @Override
    protected void isInventoryOnChanged(Boolean isInvOn) {
        super.isInventoryOnChanged(isInvOn);
        final Boolean isInventoryOn = mainViewModel.getIsInventoryOn().getValue();
        if (isInventoryOn == null) {
        }
        else updateView(isInventoryOn);
    }

    public void setBarcode(final String barcode) {
        AppCommonMethods.logInFile(context, getSessionType().name(), "_Barcode_Observed (" + barcode + ")");
        showLog("ENCODING_Barcode_SCAN", "End->" + barcode);
        if (barcode.matches(getEanRegex())) {//SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_BOTH) || SharedPrefManager.getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_STD) == SGTIN96.IsValidGtin(ApplicationCommonMethods.getZeroFilledString(barcode, 14))){
            binding.scanEncodingStartRfid.setText(barcode);
            encBarcode = AppCommonMethods.getLeftZeroReplacedString(context, chkNull(binding.scanEncodingStartRfid.getText(), ""));
            if (sessionObject == null && !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))
                return;
            if (checkReaderConnected()) {
                if (pickedTag != null) callEpcForEncoding();
                //else startRfidReading(barcode);
            }
            //binding.llBtnStart.performClick();
        } else {
            context.showCustomErrDialog(String.format(getString(R.string.err_invalid_barcode), getTypeCharCode(), barcode));
            clearBarcode();
        }
    }

    void setUploadCountObserver() {
        setUploadCountObserver(false);
    }

    void setUploadCountObserver(final boolean isRemove) {
        if (inventoryDao != null && sessionObject != null) {
            inventoryDao.getUploadedCount(sessionObject.sessionId).removeObservers(getViewLifecycleOwner());
            if (!isRemove)
                inventoryDao.getUploadedCount(sessionObject.sessionId).observe(getViewLifecycleOwner(), new Observer<MultiQtyModel>() {
                    @Override
                    public void onChanged(MultiQtyModel multiQtyModel) {
                        if (multiQtyModel != null && multiQtyModel.found != null && multiQtyModel.total != null) {
                            if (multiQtyModel.total > 0 && binding.txtEncodeUploadedCount.getVisibility() != View.VISIBLE)
                                binding.txtEncodeUploadedCount.setVisibility(View.VISIBLE);
                            binding.txtEncodeUploadedCount.setText(String.format(getString(R.string.txt_enc_count_uploaded), "" + multiQtyModel.found, "" + multiQtyModel.total));
                        }
                    }
                });
        }
    }

    void setVerifyCountObserver() {
        setVerifyCountObserver(false);
    }

    void setVerifyCountObserver(final boolean isRemove) {
        if (inventoryDao != null && sessionObject != null && SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY)) {
            inventoryDao.getEncVerifiedCount(sessionObject.sessionId).removeObservers(getViewLifecycleOwner());
            if (!isRemove)
                inventoryDao.getEncVerifiedCount(sessionObject.sessionId).observe(getViewLifecycleOwner(), new Observer<MultiQtyModel>() {
                    @Override
                    public void onChanged(MultiQtyModel multiQtyModel) {
                        if (multiQtyModel != null && multiQtyModel.found != null && multiQtyModel.total != null) {
                            if (binding != null && binding.ctwInventoryStart != null)
                                //  binding.txtEncodingStartScore.setText(multiQtyModel.found + " / " + multiQtyModel.total);
                                binding.ctwInventoryStart.setTotal(multiQtyModel.found + " / " + multiQtyModel.total);
                            if (binding != null && binding.btnVerifyEncoding != null)
                                binding.btnVerifyEncoding.setVisibility(multiQtyModel.total > 0 && multiQtyModel.found != multiQtyModel.total ? View.VISIBLE : View.GONE);
                            if (binding != null && binding.ddvVerifyEncodingTotalFound != null)
                                setFoundTotalText(binding.ddvVerifyEncodingTotalFound, multiQtyModel.found, multiQtyModel.total);
                            updateLists();
                        }
                    }
                });
        }
    }

    private void getThanOriginalLength() {
        getThanOriginalLength(null, null, null);
    }

    private void getThanOriginalLength(final Inventory pickedTag, final String currentTagPassword, final Bundle args) {
        InputView inputView = new InputView(context);
        inputView.setHint(R.string.hint_than_length_original);
        inputView.setLabel(R.string.lbl_than_length_original);
        inputView.setUnit(SharedPrefManager.getString(ParamConstants.LENGTH_UNIT, AppCommonMethods.defLengthUnitThan));
        inputView.setMinLen(1);
        inputView.setMaxLen(8);
        inputView.setMaxLines(1);
        inputView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputView.setDigits(R.string.onlyDigitsDecimal);
        inputView.setText(chkNull(originalThanLength, ""));//in case if it needs to be saved
        inputView.setValidationRegex(AppConstants.REGEX_THAN_LENGTH);
        inputView.setInputRegex(AppConstants.REGEX_THAN_LENGTH_INPUT);
        inputView.setButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                originalThanLength = formatDoubleStr2Decimals(inputView.getText().trim());
                showLog("originalThanLength", originalThanLength);
                if (pickedTag != null && isNonEmpty(args)) {
                    args.putString(ParamConstants.LENGTH_ORIGINAL, originalThanLength);
                    startEncoding(pickedTag, currentTagPassword, args);
                } else callEpcForEncoding();
            }
        });
        context.showCustomAlertDialog(getString(R.string.title_than_length_original), "", inputView, getString(R.string.btn_submit), getString(R.string.btn_cancel));
    }

    /**
     * Start encoding.
     *
     * @param pickedTag the picked tag
     */
    private void startEncoding(final Inventory pickedTag, final String currentTagPassword) {
        startEncoding(pickedTag, currentTagPassword, null);
    }

    private void startEncoding(final Inventory pickedTag, final String currentTagPassword, final Bundle args) {
        final String sgtin = pickedTag.newEpc;
        final String header = pickedTag.newEpc.substring(0, 2);
        boolean access_to_write = context.epcEncoderDecoder.isValidHeader(sgtin);

        if (access_to_write) {
            //pickedTag=context.epcEncoderDecoder.setPCDataBeforeEncoding(pickedTag);
            if (SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) && binding.divFifo.getVisibility() == View.VISIBLE) {
                mainViewModel.setFifoDate(binding.divFifo.getServerDate());
            }
            if (isNonEmpty(args))
                mainViewModel.performEncoding(pickedTag, currentTagPassword, args);
            else mainViewModel.performEncoding(pickedTag, currentTagPassword);
        } else {
            hideProgressDialog();
            context.showCustomErrDialog(R.string.err_encoding_write_fail);
            clearBarcode();
            //CANNOT WRITE
        }
    }

    /**
     * Clear Than Original Length.
     */
    private void clearThanOriginalLength() {
        if (!isClearOriginalLengthInEncForEachTag) return;
        originalThanLength = "";
        binding.ivThanLen.setText("");
    }

    /**
     * Clear barcode.
     */
    public void clearBarcode() {
        showLog("clearBarcode", "clearBarcode");
        pickedTag = null;
        if (!isGetOriginalLengthFromField) clearThanOriginalLength();
        if (SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_ONE)) {
            context.runOnUiThread(() -> {
                if (pickedTag != null) pickedTag = null;
                encBarcode = "";
                if (!isGetOriginalLengthFromField) clearThanOriginalLength();
                if (binding != null && binding.scanEncodingStartRfid != null) {
                    binding.scanEncodingStartRfid.setText("");
                    //binding.scanEncodingStartRfid.setIsViewControlEnabled(true);
                }
            });
        }
    }

    /**
     * Start rfid reading.
     *
     * @param barcode the barcode
     */
    private void startRfidReading(String barcode) {
        //    mappedEan = null;
        //    if(SharedPrefManager.getIsEANMapped()){
        //      try{
        //        JSONObject jsonRequest = new JSONObject();
        //        jsonRequest.put(ParamConstants.EAN, barcode);
        //        callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, getString(R.string.progress_msg_check_map_data));
        //      }
        //      catch(Exception e){ e.printStackTrace(); }
        //    }
        //    else{
        //new Handler().postDelayed(() -> {
        pickedTag = null;
        if (!isGetOriginalLengthFromField) clearThanOriginalLength();
        showLog("ENCODING_PICK_SCAN", "START");
        mainViewModel.performPick("");
        //}, /*isDelay ? 50 :*/ 0);
        //}
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
        if (!isUseAPICallForSessionEncode && !SharedPrefManager.getBoolean(ParamConstants.IS_OFFLINE_ENCODE) && isInternetConnected(context, false, isUpload)) {
            try {
                if (isUpload)
                    showProgressDialog(getString(R.string.progress_msg_check_upload_data));
                JSONObject requestParams = new JSONObject();
                requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
                requestParams.put(ParamConstants.TYPE, AppConstants.ENCODE);
                requestParams.put(ParamConstants.EAN_TYPE, SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.EAN_TYPE, getString(R.string.btn_encode_config_ean_std)));
                requestParams.put(ParamConstants.IS_ONE_TO_MANY_RELATION, SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_MANY));// !isOneToOneRelation);
                requestParams.put(ParamConstants.IS_BARCODE_BARCODE_RFID, false);// isBarcodeBarcodeRFID);
               // requestParams.put(ParamConstants.TARGET, sessionObject != null ? sessionObject.total : chkZero(binding.txtEncodingStartScoreTotal.getText().toString(), "-"));
                requestParams.put(ParamConstants.TARGET, sessionObject != null ? sessionObject.total : chkZero(binding.ctwInventoryStart.getTotal(), "-"));
                requestParams.put(ParamConstants.ACTION, action);
                //if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING))
                //requestParams.put(ParamConstants.FIFO_DATE, binding.divFifo.getServerDate());
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
                callWebService(isUpload ? URLConstants.UPLOAD_ENCODING_THAN : URLConstants.SET_SESSION, requestParams, args, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload, false);
            } catch (JSONException e) {
                e.printStackTrace();
                hideProgressDialog();
            }
        } else if (!isUpload)
            setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null, null, activeUsers, target, args);
    }

    @Override
    protected void isSessionOnChanged(final Boolean isSessionOn) {
        if (isSessionOn == null) return;
        if (chkNotNullTrue(isSessionOn) && is1stSessionStart) {
            is1stSessionStart = false;
            binding.llBtnStart.performClick();
        }
        //final boolean isProcessOn = isProcessOn();
        // binding.llBtnStart.setEnabled(!isProcessOn);
        binding.seekEncodingStartTarget.setEnabled(!isSessionOn);
        //binding.scanEncodingStartRfid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(isSessionOn && !isProcessOn);
        //binding.divFifo.setEnabled(!isProcessOn);
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

    public void toggleVerifyMode(final boolean isVerifyMode) {
        ThanEncodingFragment.this.isVerifyMode = isVerifyMode;

        if (isVerifyMode) mainViewModel.setReaderPower(30);//Set Max Power

        binding.header.imgConfigSync.setVisibility(isVerifyMode || SharedPrefManager.getBoolean(ParamConstants.IS_CONFIG_ENC_START, false) ? View.VISIBLE : View.GONE);
        binding.llEncodingConfigRelation.setVisibility(!isVerifyMode && SharedPrefManager.getBoolean(ParamConstants.IS_CONFIG_ENC_START, false) ? View.VISIBLE : View.GONE);
        binding.llSeekbarPower.setVisibility(isVerifyMode ? View.VISIBLE : View.GONE);

        binding.listVerifyEncoded.setVisibility(isVerifyMode ? View.VISIBLE : View.GONE);
        binding.llVerifyEncodingFoundTotal.setVisibility(isVerifyMode ? View.VISIBLE : View.GONE);

        binding.llStartEncodingScore.setVisibility(!isVerifyMode ? View.VISIBLE : View.GONE);
        binding.llVerifyEncodingStartMain.setVisibility(!isVerifyMode ? View.VISIBLE : View.GONE);
        binding.lblEncodingStartHistory.setVisibility(!isVerifyMode ? View.VISIBLE : View.GONE);
        binding.listEncodingStartHistory.setVisibility(!isVerifyMode ? View.VISIBLE : View.GONE);
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
      if(isProcessOn) binding.llEncodingConfig.setVisibility(View.GONE);
      binding.scanEncodingStartRfid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null, chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
      binding.divFifo.setEnabled(!isProcessOn);*/
            updateView();
        }
    }

    @Override
    public void onPickDataChanged(Inventory pickData) {
        super.onPickDataChanged(pickData);
        if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if (pickData != null) {
            pickedTag = pickData;
            showLog("ENCODING_Pick_SCAN", "End");
            AppCommonMethods.logInFile(context, getSessionType().name(), "_PickData_Observed (" + pickedTag.toString() + ")");
            showLog("Pick_Done", "" + pickedTag);
            if (isNonEmpty(encBarcode)) callEpcForEncoding();
        }
    }

    private void callEpcForEncoding() {
        if (isGetOriginalLengthBeforeAPI && isNullOrEmpty(chkZero(originalThanLength, ""))) {
            getThanOriginalLength();
            return;
        }
        showLog("callEpcForEncoding", "callEpcForEncoding");
        if (isEpcForEncodingAPICalled && !isInternetConnected(context, false, true)) return;
        isEpcForEncodingAPICalled = true;
        showLog("EAN", encBarcode);
        showLog("EPC", pickedTag.epc);
        showLog("TID", pickedTag.tid);
        showLog("PC", pickedTag.pcdata);
        try {
            final boolean isCheckPasswordBasedOnEPC = !SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase("00000000") && SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, AppCommonMethods.isCheckEncPasswordBasedOnEPC) && (!pickedTag.epc.matches("(?i)^(00|30|0[A-C]|7[A-B]|BC).*$") || pickedTag.ean.matches("(?i)(" + AppConstants.UNKNOWN + "|" + AppConstants.NON_ENCODED + ")"));
            final boolean isCheckPasswordBeforeAPI = !SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase("00000000") && SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_API, AppCommonMethods.isCheckEncPasswordBeforeAPI);
            if (isCheckPasswordBeforeAPI && (!SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, AppCommonMethods.isCheckEncPasswordBasedOnEPC) || isCheckPasswordBasedOnEPC) && SharedPrefManager.getDeviceTypeValue() == AppCommonMethods.DeviceType.ZEBRA.getValue()) {
                pickedTag.ean = encBarcode;
                mainViewModel.readTagCurrentPassword(pickedTag);
            } else {
                pickedTag.ean = encBarcode;
                AppCommonMethods.logInFile(context, getSessionType().name(), "_PickData_Observed (" + pickedTag.toString() + ")");
                JSONObject requestParams = new JSONObject();
                requestParams.put(ParamConstants.TYPE, AppConstants.ENCODE);
                requestParams.put(ParamConstants.QTY, 1);
                requestParams.put(ParamConstants.EAN, pickedTag.ean);
                JSONArray js = new JSONArray();
                JSONObject jsonObj = pickedTag.toJson(context);
                if (jsonObj != null) js.put(jsonObj);
                requestParams.put(ParamConstants.ITEMS, js);
                if (isNonEmpty(originalThanLength))
                    requestParams.put(ParamConstants.LENGTH_ORIGINAL, originalThanLength);

                if (AppCommonMethods.isStaticDebug()) {
                    final String sgtin = context.epcEncoderDecoder.getEpcFromBarcode(pickedTag.ean, true);

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
                    handleResponse(URLConstants.GET_EPC_FOR_ENCODING_THAN, requestParams, jsonResponse, -1, true, null);
                } else if (SharedPrefManager.getBoolean(ParamConstants.IS_OFFLINE_ENCODE)) {
                    JSONArray tagArray = new JSONArray();
                    JSONObject tagObject = new JSONObject();
                    tagObject.put(TID, pickedTag.tid);
                    if (!SharedPrefManager.getBoolean(ParamConstants.IS_OFFLINE_REENCODE)) {
                        String previousTagBarcode = EPCEncoderDecoder.getInstance().getBarcodeFromEPC(pickedTag.epc);
                        if (previousTagBarcode == null || !previousTagBarcode.equalsIgnoreCase(AppConstants.NON_ENCODED)) {
                            context.showCustomErrDialog(R.string.err_encoding_already_encoded);
                            return;
                        }
                    }
                    String epc = EPCEncoderDecoder.getInstance().getSgtinFromBarcode(pickedTag.ean, pickedTag.tid);
                    if (epc != null && !(epc.length() == 24 || epc.length() == 32)) {
                        context.showCustomErrDialog(R.string.err_encoding_write_fail_sgtin_not_generated);
                        return;
                    }
                    tagObject.put(EPC, epc);
                    tagArray.put(tagObject);

                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put(ParamConstants.EAN, pickedTag.ean);
                    jsonResponse.put(ParamConstants.ACCESS_PASSWORD, null);
                    jsonResponse.put(ParamConstants.SGTINS, tagArray);
                    handleResponse(URLConstants.GET_EPC_FOR_ENCODING_THAN, requestParams, jsonResponse, 200, true, getArguments());
                } else {
                    final String url = URLConstants.GET_EPC_FOR_ENCODING_THAN;//getEPCForEncodeUrl();
                    AppCommonMethods.logInFile(context, getSessionType().name(), url + "_Request =>" + requestParams);
                    callWebService(url, requestParams, getString(R.string.progress_msg_getting_data));
                    showLog("API_CALL_START", url);
                    showLog("ENCODING_API_START", "Start");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void isEncodeOnChanged(Boolean isEncodeOn) {
        super.isEncodeOnChanged(isEncodeOn);
        if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;

        if (isEncodeOn == null) {
        }
        else {
      /*final boolean isProcessOn = isProcessOn();
      binding.llBtnStart.setEnabled(!isProcessOn);
      if(isProcessOn) binding.llEncodingConfig.setVisibility(View.GONE);
      binding.scanEncodingStartRfid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null, chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
      binding.divFifo.setEnabled(!isProcessOn);*/
            updateView();
            if (chkNotNullFalse(isEncodeOn)) {
                if (sessionObject != null && chkNotNullTrue(mainViewModel.getIsEncodeDone().getValue())) {//Case Tag Write Success
                    showShortToast(R.string.success_encoding);
                    clearBarcode();
                    clearThanOriginalLength();
                   // long total = Long.parseLong(chkNull(binding.txtEncodingStartScoreTotal.getText().toString().replace("-", ""), "0"));
                    assert binding.ctwInventoryStart != null;
                    long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().toString().replace("-", ""), "0"));
                    if (total > 0 && getSize() == total)
                        context.showCustomSuccessDialog(R.string.success_enc_target_achieved);
                    showLog("ENCODING_Encode", "END");
                    //apiCall(SESSION_ACTION_UPLOAD);
                    mainViewModel.getIsEncodeDone().postValue(false);
                    if (isInternetConnected(context, false, false) && (SharedPrefManager.getBoolean(ParamConstants.IS_ENC_UPLOAD_BY_WHILE_PROCESSING, AppCommonMethods.isAllowBackgroundWritenTagUploadWhileProcessing) || !isProcessOn()) && (SharedPrefManager.getBoolean(ParamConstants.IS_ENC_UPLOAD_BY_BOTH_IMMEDIATE_SCHEDULER, AppCommonMethods.isAllowBothImmediateUploadAndUploadSchedulerForWrittenTags) || uploadTimer == null))
                        context.uploadWrittenInventoryTags(true);
                } else if (pickedTag != null && mainViewModel.getPickData().getValue() != null) {
                    clearBarcode();
                    clearThanOriginalLength();
                }
            }
        }
    }

    private void updateView() {
        updateView(false);
    }

    private void updateView(boolean isInventoryOn) {
        final boolean isProcessOn = isProcessOn();
        if (dialogOmniEpcSearchBinding != null) {
            dialogOmniEpcSearchBinding.llBtnStart.setEnabled(!chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()));
            dialogOmniEpcSearchBinding.rgSearchType.setEnabled(!isProcessOn);
            dialogOmniEpcSearchBinding.rbEpc.setEnabled(!isProcessOn);
            dialogOmniEpcSearchBinding.rbTid.setEnabled(!isProcessOn);
        }
        binding.llBtnStart.setEnabled(!isProcessOn);
        binding.llBtnStartInv.setEnabled(!isProcessOn || isInventoryOn);
        binding.llBtnStartInv.toggle(isInventoryOn);
        if (isProcessOn) binding.llEncodingConfig.setVisibility(View.GONE);
        binding.scanEncodingStartRfid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null, chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
        binding.divFifo.setEnabled(!isProcessOn);
        binding.ivThanLen.setEnabled(!isProcessOn);
    }

    @Override
    protected void onTriggerPressed() {
        showLog(this.getClass().getSimpleName(), "onTriggerPressed");
        if (dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null)
            dialogOmniEpcSearchBinding.llBtnStart.performClick();
        else if (isVerifyMode && binding.llBtnStartInv != null)
            binding.llBtnStartInv.performClick();
        else binding.llBtnStart.performClick();
    }

    @Override
    protected void onDataSizeChanged(Integer size) {
        if (chkNull(size, 0) > 0 && sessionObject != null && !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))
            mainViewModel.startSession(sessionObject, false);
        listEncodedEans.clear();
        if (sessionObject != null) {
            listEncodedEans.addAll(inventoryDao.getEncodedEans(sessionObject.sessionId));
        }
        showLog("EncodingSize", "" + size);
        if (binding != null) {
            if (binding.listEncodingStartHistory != null && binding.listEncodingStartHistory.getAdapter() != null && binding.listEncodingStartHistory.getAdapter() instanceof RecyclerView.Adapter)
                binding.listEncodingStartHistory.getAdapter().notifyDataSetChanged();
           // binding.txtEncodingStartScore.setText("" + chkNull(size, 0));
           if (binding.ctwInventoryStart != null) binding.ctwInventoryStart.setScore(chkNull(size, 0));
            final boolean isEncCount = chkNull(size, 0) > 0;
            showLog("EncodingSizeisEncCount", "" + isEncCount);
            showLog("EncodingVerify", "" + SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY, true));

            if (size > 0 && SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY)) { // if isShow
                int verifiedTags = inventoryDao.getVerifiedCount(sessionObject.sessionId);
                //binding.txtEncodingStartScore.setText(verifiedTags + " / " + chkNull(size, 0));
                if (binding.ctwInventoryStart != null) binding.ctwInventoryStart.setScore(chkNull(size, 0),verifiedTags + " / " + chkNull(size, 0));
                binding.btnVerifyEncoding.setVisibility(isEncCount && verifiedTags != chkNull(size, 0) ? View.VISIBLE : View.GONE);
            }
            //binding.txtEncodeUploadedCount.setVisibility(chkZero(size, 0) > 0 ? View.VISIBLE : View.GONE);
           // long total = Long.parseLong(chkNull(binding.txtEncodingStartScoreTotal.getText().toString().replace("-", ""), "0"));
            assert binding.ctwInventoryStart != null;
            long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
            binding.ctwInventoryStart.setTextColorScore(total > 0 && size >= total ? R.color.txtGreen : R.color.txt_number);
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
        if (uploadTimer != null) {
            uploadTimer.cancel();
            uploadTimer = null;
        }
        setUploadCountObserver(true);
        setVerifyCountObserver(true);
        sessionObject = null;
        mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
        super.onDestroyView();
    }

    public boolean isVerifyMode() {
        return isVerifyMode;
    }

    @Override
    public void onBackPressed() {
        if (sessionObject != null && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) {
            if (isProcessOn())
                context.showCustomAlertDialog("", String.format(getString(R.string.err_op_back_press), getTypeCharCode(), getSessionType().name().replaceAll("_", " ")), false, true, getString(R.string.btn_ok), null);
            else if (SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY)) {
                if (isVerifyMode) toggleVerifyMode(false);
                else if (getVerifiedTagCount() < getSize() && isNonEmpty(SharedPrefManager.getString(ParamConstants.SESSION_FORCE_END_PASSWORD)))
                    showForcePasswordDialog();
                else super.onBackPressed();
            } else super.onBackPressed();
        } else super.onBackPressed();
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
        //final String totEncCount = sessionObject != null ? sessionObject.total : chkZero(binding.txtEncodingStartScoreTotal.getText().toString(), "0");
        final String totEncCount = sessionObject != null ? sessionObject.total : chkZero(binding.ctwInventoryStart.getTotal(), "0");
        showLog("totEncCount", totEncCount);
        //binding.txtEncodingStartScoreTotal.setText(totEncCount);
        binding.ctwInventoryStart.setTotal(totEncCount);
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
            sessionObject.sessionType = AppCommonMethods.SessionType.ENCODING_THAN.getValue();
            sessionObject.sessionAction = AppCommonMethods.SessionAction.ENCODE.getValue();
            try {
                JSONObject jsonExtras = new JSONObject();
                jsonExtras.put(ParamConstants.TYPE, AppConstants.ENCODE);
                jsonExtras.put(ParamConstants.EAN_TYPE, SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.EAN_TYPE, getString(R.string.btn_encode_config_ean_std)));
                jsonExtras.put(ParamConstants.IS_ONE_TO_MANY_RELATION, SharedPrefManager.getEncodeRelationType().equalsIgnoreCase(AppConstants.ENCODE_TYPE_MANY));// !isOneToOneRelation);
                jsonExtras.put(ParamConstants.IS_BARCODE_BARCODE_RFID, false);// isBarcodeBarcodeRFID);
                //jsonExtras.put(ParamConstants.TARGET, sessionObject != null ? sessionObject.total : chkZero(binding.txtEncodingStartScoreTotal.getText().toString(), "-"));
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
            setUploadCountObserver();
            setVerifyCountObserver();
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
                        final String productInfoUrl = URLConstants.GET_PRODUCT_INFO_THAN;//getProductInfoUrl();
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
                case URLConstants.GET_EPC_FOR_ENCODING_THAN:
                case URLConstants.GET_EPC_FOR_ENCODING:
                case URLConstants.GET_EPC_FOR_ENCODE:
                    isEpcForEncodingAPICalled = false;
                    showLog("API_CALL_RESULT", url + "_" + isSuccess);
                    showLog("ENCODING_API_END", "End");
                    AppCommonMethods.logInFile(context, getSessionType().name(), "_" + url + "_Response =>" + jsonResponse.toString());
                    if (isSuccess) {
                        final String currentTagPassword = extractString(args, ParamConstants.CURRENT_ACCESS_PWD, "");
                        final JSONArray jsonArraySgtins = extractJSONArray(jsonResponse, ParamConstants.SGTINS);
                        final JSONObject jsonSgtins = jsonArraySgtins != null && jsonArraySgtins.length() > 0 ? jsonArraySgtins.getJSONObject(0) : null;
                        final String newTid = extractString(jsonSgtins, ParamConstants.TID, extractString(jsonResponse, ParamConstants.TID));
                        final String newEpc = extractString(jsonSgtins, ParamConstants.EPC, extractString(jsonResponse, ParamConstants.EPC));
                        if (!SharedPrefManager.getBoolean(ParamConstants.IS_OFFLINE_ENCODE))
                            context.saveTagWritePasswords(jsonResponse);
                        showLog("passwords", SharedPrefManager.getOldAccessPasswords().toString());
                        final String barcode = extractString(jsonRequest, ParamConstants.EAN, "").trim();
                        final String articleNo = extractString(jsonResponse, ParamConstants.ARTICLE_NO, "").trim();
                        if (pickedTag != null && isNonEmpty(newEpc)) {
                            if (isNonEmpty(barcode)) pickedTag.ean = barcode;
                            pickedTag.newEpc = newEpc;
                            showLog("ENCODING_ENCODE", "Start");
                            if (isThanEncoding) {
                                args = chkNull(args, new Bundle());
                                args.putString(ParamConstants.ARTICLE_NO, articleNo);
                                args.putString(ParamConstants.LENGTH_ORIGINAL, originalThanLength);
                                if (!isGetOriginalLengthFromField && !isGetOriginalLengthBeforeAPI && isNullOrEmpty(chkZero(originalThanLength, "")))
                                    getThanOriginalLength(pickedTag, currentTagPassword, args);
                                else startEncoding(pickedTag, currentTagPassword, args);
                            }
                            //else startEncoding(pickedTag, currentTagPassword);
                        } else {
                            hideProgressDialog();
                            context.showCustomErrDialog(R.string.err_encoding_write_fail);
                            clearBarcode();
                        }
                    } else {
                        clearBarcode();
                    }
                    break;
                case URLConstants.UPLOAD_ENCODING_THAN:
                case URLConstants.UPLOAD_ENCODING:
                case URLConstants.UPLOAD_ENCODE:
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
                       // final long total = Long.parseLong(chkNull(binding.txtEncodingStartScoreTotal.getText().toString().replace("-", ""), "0"));
                        assert binding.ctwInventoryStart != null;
                        final long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().toString().replace("-", ""), "0"));
                        String dialogMsg = extractString(jsonResponse, ParamConstants.MESSAGE, getString(total > 0 && getSize() == total ? R.string.success_enc_target_achieved : R.string.success_encoding));
                        if (total > 0 && getSize() == total && !dialogMsg.toLowerCase().contains("target") && !dialogMsg.toLowerCase().contains("achieve"))
                            dialogMsg = dialogMsg + "\n" + getString(R.string.success_enc_target_achieved);
                        context.showCustomSuccessDialog(dialogMsg);
                        if (isNonEmpty(tids)) {
                            inventoryDao.updateUploaded(sessionObject.sessionId, tids);
                            uploadInventoryDao.updateUploaded(sessionObject.sessionId, tids);
                        } else {
                            inventoryDao.updateUploaded(sessionObject.sessionId);
                            uploadInventoryDao.updateUploaded(sessionObject.sessionId);
                        }
                        uploadInventoryDao.deleteUploaded();
                        clearBarcode();
                    } else {
                        clearBarcode();
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