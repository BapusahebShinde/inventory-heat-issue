package com.itek.retail.ui.inward1;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itek.retail.R;
import com.itek.retail.adapter.HUDetailsAdapter;
import com.itek.retail.adapter.HUListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.BarcodeScanFragment;
import com.itek.retail.common.InsertDBHUDetails;
import com.itek.retail.common.InsertDBHUs;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.HUDetailsDao;
import com.itek.retail.database.HUStatusDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.DialogHuDetailsBinding;
import com.itek.retail.databinding.DialogTripHuDetailsBinding;
import com.itek.retail.databinding.FragmentTripHuListBinding;
import com.itek.retail.model.HUStatus;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Trip HU List fragment.
 */
public class TripHUListFragment extends BarcodeScanFragment {

    public TripStatus tripStatus;
    public String tripNo;
    public String displayTripNo;
    List<HUStatus> dataList = new ArrayList<>(0);
    private int noOfHu;
    private TripStatusDao tripStatusDao;
    private HUStatusDao huStatusDao;
    private HUDetailsDao huDetailsDao;
    private FragmentTripHuListBinding binding;
    private final List<MultiQtyModel> listHuDetails = new ArrayList<>(0);
    private AlertDialog huDetailsAlert;
    private DialogHuDetailsBinding dialogHuDetailsBinding;
    private String huNum;
    private final List<HUStatus> listTripHus = new ArrayList<>(0);
    private AlertDialog tripHuListAlert;
    private DialogTripHuDetailsBinding dialogTripHuDetailsBinding;

    private String typeIO = "";
    private String labelTrip = "";
    private String labelHU = "";
    private String labelArticle = "";
    private String labelSku = "";
    private boolean isOnDemandTripHuList = false;
    private String sortByValues = "";
    private String sortByValues1 = "";
    private String sortByValues2 = "";
    private boolean isDirectlyLoaded = false;

    /**
     * Instantiates a new Inward grn trip details fragment.
     */
    public TripHUListFragment() {/*Default/Empty Constructor*/}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripStatusDao = AppDatabase.getDbInstance(context).TripStatusDao();
        huStatusDao = AppDatabase.getDbInstance(context).HUStatusDao();
        huDetailsDao = AppDatabase.getDbInstance(context).HUDetailsDao();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentTripHuListBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            isDirectlyLoaded = extractBoolean(getArguments(), "isDirectlyLoaded", false);
            typeIO = extractString(getArguments(), ParamConstants.TYPE, extractString(getArguments(), ParamConstants.OPERATION_TYPE, AppConstants.INWARD));
            final Object obj = extractSerializable(getArguments(), TripStatus.class);
            tripStatus = obj != null && obj instanceof TripStatus ? (TripStatus) obj : null;
            tripNo = tripStatus != null ? tripStatus.getTripNumber() : extractString(getArguments(), ParamConstants.TRIP_NUMBER, "");
            final String refTripNo = tripStatus != null ? tripStatus.getRefTripNumber() : extractString(getArguments(), ParamConstants.REFERENCE_TRIP_NUMBER, "");
            displayTripNo = tripStatus != null ? chkNull(tripStatus.getRefTripNumber(), chkNull(tripStatus.getTripNumber(), tripNo)) : chkNull(refTripNo, chkNull(tripNo, ""));
            //if(tripStatus==null) popBackStack();
            //noOfHu = tripStatus != null ? tripStatus.getNumberOfHu() : extractInt(activityBundle, AppConstants.HU_NUMBERS, 0);
            if(tripStatus!=null){
                binding.txtTripNumber.setText(displayTripNo);
                binding.textTotalHu.setText(String.valueOf(tripStatus.getNumberOfHu()));
                binding.textCompletedHu.setText(tripStatus.getCompletedHu() + "/" + tripStatus.getNumberOfHu());
                if(AppCommonMethods.isLockAndRedirectToProcessingTrip || (tripStatus.isManualTrip() && AppCommonMethods.isLockAndRedirectToProcessingManualTrip))
                    updateProcessingTripStatus();
                binding.btnCompleteTrip.setVisibility(tripStatus.getCompletedHu() > 0 ? View.VISIBLE : View.GONE);
            }
        }

        binding.ivHuNo.setTextChangeEvent(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLists();
            }
        });

        binding.btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null && v.getVisibility() == ViewGroup.VISIBLE && allowBtnClick && !isShowingDialog() && !isProcessOn() && binding.ivHuNo.validate()) {
                    final String huNo = binding.ivHuNo.getText().trim();
                    final HUStatus huStatus = huStatusDao.getHUData(typeIO, displayTripNo, huNo);
                    if (huStatus != null) {
                        processHuStatus(huStatus);
                    } else {
                        if (!tripStatus.isManualTrip() && isOnDemandTripHuList)
                            processHuStatus(huNo);
                        else if (tripStatus.isManualTrip() || SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_MANUAL_HU_ENTRY, tripStatus.isManualTrip()))
                            context.showCustomMsgDialog(String.format(context.getResources().getString(R.string.err__not_exist_manual_input), labelHU + ":" + huNo), null, false, false, false, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Bundle args = chkNull(getArguments(), new Bundle());
                                    args.putString(ParamConstants.K_TRIP_HU_NUMBER, huNo);
                                    context.loadFragment(new HuCreationFragment(), args);
                                    clearField();
                                }
                            }, getString(R.string.btn_cancel));
                        else
                            context.showCustomErrDialog(String.format(context.getResources().getString(R.string.err__not_exist), labelHU + ":" + huNo));
                    }
                }
            }
        });

        setInputView(binding.ivHuNo, binding.btnGo);

        setHeader();

        binding.listTripDetails.setAdapter(new HUListAdapter(context, TripHUListFragment.this, dataList, !tripStatus.isManualTrip(), true, false));
        binding.listTripDetails.setLayoutManager(new LinearLayoutManager(context));

        binding.btnTripDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null && v.getVisibility() == ViewGroup.VISIBLE && allowBtnClick && !isShowingDialog())
                    callTripHUList(true);
            }
        });

        binding.btnCompleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null && v.getVisibility() == ViewGroup.VISIBLE && allowBtnClick && !isShowingDialog() && tripStatus.getCompletedHu() > 0) {
                    showTripCompleteConfirmDialog(getTripInfoMsg());
                }
            }
        });

        initUI();

        return binding.getRoot();
    }

    public TripStatus getTripStatus() {
        return tripStatus;
    }

    public boolean isManualTrip() {
        return tripStatus != null && tripStatus.isManualTrip();
    }

    private boolean isShowingDialog() {
        return (tripHuListAlert != null && tripHuListAlert.isShowing()) || (huDetailsAlert != null && huDetailsAlert.isShowing());
    }

    public void processHuStatus(final String huNumber) {
        processHuStatus(huNumber, false);
    }

    public void processHuStatus(final String huNumber, final boolean isShowDialog) {
        processHuStatus(huNumber, null, isShowDialog);
    }

    public void processHuStatus(final HUStatus huStatus) {
        processHuStatus(huStatus, false);
    }

    public void processHuStatus(final HUStatus huStatus, final boolean isShowDialog) {
        processHuStatus("", huStatus, isShowDialog);
    }

    public void processHuStatus(final String huNumber, final HUStatus huStatus, final boolean isShowDialog) {
        if ((huStatus == null && isNullOrEmpty(huNumber)) || isProcessOn()) return;
        if (!isShowDialog && huStatus != null && (huStatus.getStatus().equalsIgnoreCase(AppConstants.HU_STATUS_COMPLETE) || huStatus.getStatus().equalsIgnoreCase(AppConstants.STATUS_COMPLETE) || huStatus.getStatus().equalsIgnoreCase(AppConstants.STATUS_COMPLETED)))
            context.showCustomErrDialog(String.format(context.getResources().getString(R.string.err__already_completed), labelHU + ":" + huStatus.getHuNumber()));
        else if (!isShowDialog && huStatus != null && huStatus.getExpQty() <= 0)
            context.showCustomErrDialog(String.format(context.getResources().getString(R.string.err_no_associated__found_for__), labelArticle, labelHU + ":" + huStatus.getHuNumber()));
        else {
            Bundle arg = chkNull(getArguments(), new Bundle());
            arg.putString(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
            if (tripStatus != null)
                arg.putSerializable(tripStatus.getClass().getSimpleName(), tripStatus);
            if (huStatus != null) {
                arg.putString(ParamConstants.K_TRIP_HU_NUMBER, huStatus.getHuNumber());
                arg.putSerializable(huStatus.getClass().getSimpleName(), huStatus);
            }
            if (isNonEmpty(huNumber)) {
                arg.putString(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
            }
            if (isManualTrip() && !isShowDialog) {
                context.loadFragment(new HuProcessStartFragment(), arg);
                clearField();
            } else if (isShowDialog && huDetailsDao.hasHUDetails(typeIO, displayTripNo, chkNull(huNumber, huStatus.getHuNumber()))) {
                if (isShowDialog) showHUInfoDialog(huStatus.getHuNumber());
                else {
                    context.loadFragment(new HuProcessStartFragment(), arg);
                    clearField();
                }
            } else callHUDetails(huNumber, huStatus, arg, isShowDialog);
        }
    }

    /*private void callHUDetails(final String huNumber, final Bundle args, final boolean isShowDialog){callHUDetails("",null,args,isShowDialog);}
    private void callHUDetails(final HUStatus huStatus, final Bundle args, final boolean isShowDialog){callHUDetails("",huStatus,args,isShowDialog);}*/
    private void callHUDetails(final String huNumber, final HUStatus huStatus, final Bundle args, final boolean isShowDialog) {
        if (tripStatus == null) return;
        if (tripStatus != null && tripStatus.isManualTrip()) return;
        if (isNullOrEmpty(huNumber) && huStatus == null) return;
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
            jsonRequest.put(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
            jsonRequest.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
            jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus != null && tripStatus.isManualTrip());
            jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus != null ? tripStatus.excelTripType : "");
            jsonRequest.put(ParamConstants.K_TRIP_HU_NUMBER, huStatus != null ? huStatus.getHuNumber() : huNumber);

            Bundle arg = chkNull(args, chkNull(getArguments(), new Bundle()));
            arg.putBoolean(ParamConstants.IS_SHOW_HU_INFO_DIALOG, isShowDialog);
            arg.putString(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
            arg.putString(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
            if (tripStatus != null)
                arg.putSerializable(tripStatus.getClass().getSimpleName(), tripStatus);
            if (huStatus != null) {
                arg.putString(ParamConstants.K_TRIP_HU_NUMBER, huStatus.getHuNumber());
                arg.putSerializable(huStatus.getClass().getSimpleName(), huStatus);
            } else if (isNonEmpty(huNumber))
                arg.putString(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
            callWebService(URLConstants.GET_HU_DETAILS, jsonRequest, arg, getString(R.string.progress_msg_getting_data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callTripHUList(final boolean isShowDialog) {
        if (tripStatus == null) return;
        if (tripStatus != null && tripStatus.isManualTrip()) return;
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
            jsonRequest.put(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
            jsonRequest.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
            jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus != null && tripStatus.isManualTrip());
            jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus != null ? tripStatus.excelTripType : "");
            Bundle arg = chkNull(getArguments(), new Bundle());
            arg.putBoolean(ParamConstants.IS_SHOW_HU_INFO_DIALOG, isShowDialog);
            arg.putString(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
            arg.putString(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
            if (tripStatus != null)
                arg.putSerializable(tripStatus.getClass().getSimpleName(), tripStatus);
            callWebService(URLConstants.GET_HU_DATA, jsonRequest, arg, getString(R.string.progress_msg_getting_data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callReleaseTrip() {
        if (tripStatus == null) return;
        if (tripStatus != null && tripStatus.isManualTrip()) return;
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
            jsonRequest.put(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
            jsonRequest.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
            jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus != null && tripStatus.isManualTrip());
            jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus != null ? tripStatus.excelTripType : "");

            Bundle arg = chkNull(getArguments(), new Bundle());
            arg.putString(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
            arg.putString(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
            if (tripStatus != null)
                arg.putSerializable(tripStatus.getClass().getSimpleName(), tripStatus);
            callWebService(URLConstants.RELEASE_TRIP, jsonRequest, arg, getString(R.string.progress_msg_uploading_data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callUploadTripAPI() {
        if (tripStatus == null && tripStatus.getCompletedHu() <= 0) return;
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
            jsonRequest.put(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
            jsonRequest.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
            jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus != null && tripStatus.isManualTrip());
            jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus != null ? tripStatus.excelTripType : "");

            Bundle arg = chkNull(getArguments(), new Bundle());
            arg.putString(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
            arg.putString(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
            if (tripStatus != null)
                arg.putSerializable(tripStatus.getClass().getSimpleName(), tripStatus);
            if (tripStatus != null && tripStatus.isManualTrip())
                arg.putBoolean(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus.isManualTrip);
            if (tripStatus != null && isNonEmpty(tripStatus.excelTripType))
                arg.putString(ParamConstants.EXCEL_TRIP_TYPE, tripStatus.excelTripType);
            callWebService(URLConstants.COMPLETE_TRIP, jsonRequest, arg, getString(R.string.progress_msg_uploading_data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateView() {
        tripStatus = tripStatusDao.getTripData(displayTripNo, typeIO);
        if(tripStatus!=null){
        binding.textTotalHu.setText(String.valueOf(tripStatus.getNumberOfHu()));
        binding.textCompletedHu.setText(tripStatus.getCompletedHu() + "/" + tripStatus.getNumberOfHu());
        binding.btnCompleteTrip.setVisibility(tripStatus.getCompletedHu() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void clearField() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (binding != null && binding.ivHuNo != null) binding.ivHuNo.setText("");
            }
        }, 30);
    }

    private void initUI() {
        labelTrip = SharedPrefManager.getString(ParamConstants.LABEL_TRIP, getString(R.string.lbl_trip));
        labelHU = SharedPrefManager.getString(ParamConstants.LABEL_HU, getString(R.string.lbl_hu));
        labelArticle = SharedPrefManager.getString(ParamConstants.LABEL_ARTICLE, getString(R.string.lbl_article_no));
        labelSku = SharedPrefManager.getString(ParamConstants.LABEL_SKUID, getString(R.string.lbl_ean));
        isOnDemandTripHuList = !tripStatus.isManualTrip() && SharedPrefManager.getBoolean(ParamConstants.IS_ON_DEMAND_TRIP_HU_LIST, AppCommonMethods.isOnDemandTripHuList);

        binding.ivHuNo.setLabel(String.format(getString(R.string.lbl__no), labelHU));
        binding.ivHuNo.setHint(String.format(getString(R.string.hint__no), labelHU));

        binding.txtTripNo.setText(String.format(getString(R.string.lbl__no), labelTrip));
        binding.txtTotalHus.setText(String.format(getString(R.string.lbl_total__s), labelHU));
        binding.txtCompletedHus.setText(String.format(getString(R.string.lbl_completed__s), labelHU));

        binding.huListHeader.txtHuNumber.setText(String.format(getString(R.string.lbl__no), labelHU));
        binding.textNoData.setText(String.format(getString(R.string.err_no__found), labelHU));
        binding.huListHeader.imgAction.setVisibility(View.INVISIBLE);
        binding.huListHeader.imgInfo.setVisibility(!isManualTrip() ? View.INVISIBLE : View.GONE);

        binding.txtTotalHus.setVisibility(AppCommonMethods.isShowTotalAndCompletedCount ? View.VISIBLE : View.GONE);
        binding.textTotalHu.setVisibility(AppCommonMethods.isShowTotalAndCompletedCount ? View.VISIBLE : View.GONE);
        binding.txtTripSrcType.setVisibility(AppCommonMethods.isShowTotalAndCompletedCount ? View.VISIBLE : View.GONE);
        binding.textTripSrcType.setVisibility(AppCommonMethods.isShowTotalAndCompletedCount ? View.VISIBLE : View.GONE);

        binding.btnTripDetails.setText(String.format(getString(R.string.btn_view__details), labelTrip));
        binding.btnTripDetails.setVisibility(isOnDemandTripHuList ? View.VISIBLE : View.GONE);
        binding.huListHeader.llHeader.setVisibility(!isOnDemandTripHuList ? View.VISIBLE : View.GONE);
        binding.listTripDetails.setVisibility(!isOnDemandTripHuList ? View.VISIBLE : View.GONE);
        binding.textNoData.setVisibility(!isOnDemandTripHuList ? View.VISIBLE : View.GONE);
        binding.btnCompleteTrip.setText(String.format(getString(R.string.lbl_complete__), labelTrip));

        final boolean isInward = typeIO.equalsIgnoreCase(AppConstants.INWARD);
        binding.txtTripSrcCode.setText(isInward ? R.string.lbl_src : R.string.lbl_dest);
        binding.txtTripSrcType.setText(isInward ? R.string.lbl_src_type : R.string.lbl_dest_type);
        binding.txtTripType.setText(String.format(getString(R.string.lbl__type), labelTrip));

        binding.textTripSrcCode.setText(isInward ? tripStatus.getSrcLocCode() : tripStatus.getDestLocCode());
        binding.textTripSrcType.setText(isInward ? tripStatus.getSrcLocType() : tripStatus.getDestLocType());
        binding.textTripType.setText(tripStatus.getTripType());

        binding.llTripHeaderExtraTitles.setVisibility(AppCommonMethods.isShowTripLocAndType ? View.VISIBLE : View.GONE);
        binding.llTripHeaderExtraVal.setVisibility(AppCommonMethods.isShowTripLocAndType ? View.VISIBLE : View.GONE);

        sortByValues = "";
        resetHeader(0);
        updateLists();
    }

    private void refreshTripCountAPI() {
        if (tripStatus == null) return;
        if (tripStatus != null && tripStatus.isManualTrip()) return;
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
            jsonRequest.put(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNo);
            jsonRequest.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus != null ? tripStatus.getRefTripNumber() : displayTripNo);
            jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus != null && tripStatus.isManualTrip());
            jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus != null ? tripStatus.excelTripType : "");
            callWebService(URLConstants.GET_TRIP_HU_COUNT, jsonRequest, getString(R.string.progress_msg_getting_data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set header.
     */
    public void setHeader() {
        final LinearLayoutCompat llHeader = binding.huListHeader.llHeader;
        final int childCount = llHeader.getChildCount();
        if (childCount > 0) for (int i = 0; i < childCount; i++) {
            final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
            if (sortView != null) {
                sortView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (llHeader.getVisibility() != View.VISIBLE) return;
                        final SortHeaderView sortView = view != null && view instanceof SortHeaderView ? (SortHeaderView) view : null;
                        if (sortView != null) {
                            resetHeader(sortView.getId());
                            setSortBy(sortView.getSortColumn(), sortView.getSortOrder());
                        }
                    }
                });
            }
        }
    }

    /**
     * Reset header.
     *
     * @param viewId the view id
     */
    public void resetHeader(@IdRes final int viewId) {
        final LinearLayoutCompat llHeader = binding.huListHeader.llHeader;
        final int childCount = llHeader.getChildCount();
        if (childCount > 0) for (int i = 0; i < childCount; i++) {
            final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
            if (sortView != null) {
                if (viewId != 0 && sortView.getId() == viewId) sortView.updateDescOrder();
                else sortView.reset();
            }
        }
    }

    /**
     * Set sort by.
     *
     * @param column  the column
     * @param orderBy the order by
     */
    private void setSortBy(String column, String orderBy) {
        sortByValues = isNonEmpty(column) && isNonEmpty(orderBy) ? column + " " + orderBy : "";
        updateLists();
    }

    /**
     * Set header.
     */
    public void setHeader1() {
        if (dialogTripHuDetailsBinding == null) return;
        final LinearLayoutCompat llHeader = dialogTripHuDetailsBinding.llListHeader.llHeader;
        final int childCount = llHeader.getChildCount();
        if (childCount > 0) for (int i = 0; i < childCount; i++) {
            final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
            if (sortView != null) {
                sortView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (llHeader.getVisibility() != View.VISIBLE) return;
                        final SortHeaderView sortView = view != null && view instanceof SortHeaderView ? (SortHeaderView) view : null;
                        if (sortView != null) {
                            resetHeader1(sortView.getId());
                            setSortBy1(sortView.getSortColumn(), sortView.getSortOrder());
                        }
                    }
                });
            }
        }
    }

    /**
     * Reset header.
     *
     * @param viewId the view id
     */
    public void resetHeader1(@IdRes final int viewId) {
        if (dialogTripHuDetailsBinding == null) return;
        final LinearLayoutCompat llHeader = dialogTripHuDetailsBinding.llListHeader.llHeader;
        final int childCount = llHeader.getChildCount();
        if (childCount > 0) for (int i = 0; i < childCount; i++) {
            final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
            if (sortView != null) {
                if (viewId != 0 && sortView.getId() == viewId) sortView.updateDescOrder();
                else sortView.reset();
            }
        }
    }

    /**
     * Set sort by.
     *
     * @param column  the column
     * @param orderBy the order by
     */
    private void setSortBy1(String column, String orderBy) {
        sortByValues1 = isNonEmpty(column) && isNonEmpty(orderBy) ? column + " " + orderBy : "";
        updateLists1();
    }

    /**
     * Set header.
     */
    public void setHeader2() {
        if (dialogHuDetailsBinding == null) return;
        final LinearLayoutCompat llHeader = dialogHuDetailsBinding.llListHeader.llHuDetails;
        final int childCount = llHeader.getChildCount();
        if (childCount > 0) for (int i = 0; i < childCount; i++) {
            final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
            if (sortView != null) {
                sortView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (llHeader.getVisibility() != View.VISIBLE) return;
                        final SortHeaderView sortView = view != null && view instanceof SortHeaderView ? (SortHeaderView) view : null;
                        if (sortView != null) {
                            resetHeader2(sortView.getId());
                            setSortBy2(sortView.getSortColumn(), sortView.getSortOrder());
                        }
                    }
                });
            }
        }
    }

    /**
     * Reset header.
     *
     * @param viewId the view id
     */
    public void resetHeader2(@IdRes final int viewId) {
        if (dialogHuDetailsBinding == null) return;
        final LinearLayoutCompat llHeader = dialogHuDetailsBinding.llListHeader.llHuDetails;
        final int childCount = llHeader.getChildCount();
        if (childCount > 0) for (int i = 0; i < childCount; i++) {
            final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
            if (sortView != null) {
                if (viewId != 0 && sortView.getId() == viewId) sortView.updateDescOrder();
                else sortView.reset();
            }
        }
    }

    /**
     * Set sort by.
     *
     * @param column  the column
     * @param orderBy the order by
     */
    private void setSortBy2(String column, String orderBy) {
        sortByValues2 = isNonEmpty(column) && isNonEmpty(orderBy) ? column + " " + orderBy : "";
        updateLists2();
    }

    @Override
    public void updateLists() {
        super.updateLists();
        updateView();

        if (!isOnDemandTripHuList) {
            final String searchName = binding.ivHuNo.getText().trim();
            dataList.clear();
            List<HUStatus> listTripData = huStatusDao.getHuList(typeIO, displayTripNo, searchName, sortByValues);
            if (isNonEmpty(listTripData)) dataList.addAll(listTripData);
            final boolean hasData = dataList.size() > 0;
            if (binding != null) {
                binding.huListHeader.llHeader.setVisibility(hasData ? View.VISIBLE : View.GONE);
                binding.listTripDetails.setVisibility(hasData ? View.VISIBLE : View.GONE);
                binding.textNoData.setVisibility(!hasData ? View.VISIBLE : View.GONE);
            }
            if (binding != null && binding.listTripDetails != null && binding.listTripDetails.getAdapter() != null)
                binding.listTripDetails.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isTopInStack()) {
    /*  mainViewModel.getBarcodeReaderInstance(sessionType);
      setObservers();
      new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
        @Override
        public void run(){
          mainViewModel.onResume(sessionType);
        }
      }, 300);*/
            refreshTripCountAPI();
            updateLists();
        }
    }
  
/*  @Override
  public void onPause(){
    super.onPause();
    mainViewModel.onPause();
  }
  
  @Override
  public void onDestroyView(){
    mainViewModel.getIsDeviceConfigured().removeObservers(getViewLifecycleOwner());
    mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
    mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
    mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
    super.onDestroyView();
  }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mainViewModel.onDestroy();
    }

    @Override
    public void onBackPress() {
        tripStatus = tripStatusDao.getTripData(displayTripNo, typeIO);
        if (tripStatus!=null && (tripStatus.getCompletedHu() > 0 || (tripStatus.isManualTrip() && tripStatus.getNumberOfHu() > tripStatus.getCompletedHu()))) {
            showTripProgressConfirmDialog(getTripInfoMsg());
        } else {
            if (SharedPrefManager.getBoolean(ParamConstants.IS_TRIP_DEVICE_LOCK)) callReleaseTrip();
            else updateProcessingTripAndGoBack();
        }
    }

    private String getTripInfoMsg() {
        //final boolean hasHuData = isNonEmpty(huStatusDao.getHuList(typeIO, displayTripNo, ""));
        //int tripCompletedHuCount = hasHuData ? huStatusDao.getCompletedHUsCount(typeIO, displayTripNo) : tripStatus.getCompletedHu();
        int tripCompletedHuCount = /*hasHuData ? huStatusDao.getCompletedHUsCount(typeIO, displayTripNo) :*/ tripStatus.getCompletedHu();
        int tripRejectedHuCount = 0;//hasHuData ? huStatusDao.getRejectedHUsCount(typeIO, displayTripNo) : 0;
        int tripPendingHuCount = tripStatus.getNumberOfHu() - (tripCompletedHuCount + tripRejectedHuCount);
        return String.format(getString(R.string.msg__information), labelTrip + " (" + displayTripNo + ")") + "\n" + AppConstants.K_TRIP_COMPLETE_COUNT + tripCompletedHuCount + /*"\n" + AppConstants.K_TRIP_REJECT_COUNT + tripRejectedHuCount +*/ " \n" + AppConstants.K_TRIP_PENDING_COUNT + tripPendingHuCount;
    }

    /**
     * Show trip complete confirm dialog.
     *
     * @param msg the msg
     */
    public void showTripCompleteConfirmDialog(final String msg) {
        context.showCustomAlertDialog("", msg + "\n" + String.format(getString(R.string.msg__complete), labelTrip + " (" + displayTripNo + "):"), R.string.btn_yes, (dialog, which) -> {
            callUploadTripAPI();
        }, R.string.btn_no, (dialog, which) -> dialog.dismiss());
    }

    /**
     * Show trip all rejected dialog.
     *
     * @param msg the msg
     */
    public void showTripAllRejectedDialog(final String msg) {
        context.showCustomErrDialog(msg + "\n" + getString(R.string.msg_all_reject_trip_alert));
    }

    private void updateProcessingTripStatus() {
        tripStatusDao.updateProcessingTripStatus(displayTripNo, typeIO);
    }

    private void updateInProgressTripStatus() {
        tripStatusDao.updateInProgressTripStatus(displayTripNo, typeIO);
    }

    /**
     * Show trip inprogress confirm dialog.
     *
     * @param msg the msg
     */
    public void showTripProgressConfirmDialog(final String msg) {
        context.showCustomAlertDialog("", msg + "\n" + String.format(getString(R.string.msg__in_progress_backpress), labelTrip + ":" + displayTripNo) + (tripStatus.isManualTrip() && tripStatus.getNumberOfHu() > tripStatus.getCompletedHu() ? "\n" + String.format(getString(R.string.msg_unsaved__data_lost), labelHU) : ""), R.string.btn_yes, (dialog, which) -> {
            if (SharedPrefManager.getBoolean(ParamConstants.IS_TRIP_DEVICE_LOCK)) callReleaseTrip();
            else updateProcessingTripAndGoBack();
        }, R.string.btn_no, (dialog, which) -> dialog.dismiss());
    }

    private void updateTripStatusAndGoBack() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (tripStatus.getCompletedHu() > 0)
                        tripStatusDao.updateInProgressTripStatus(displayTripNo, typeIO);
                    else tripStatusDao.updatePendingTripStatus(displayTripNo, typeIO);
                    //tripStatusDao.updatePendingTripStatus(displayTripNo, typeIO);

                    if (isDirectlyLoaded) {
                        Bundle args = new Bundle();
                        args.putString(ParamConstants.OPERATION_TYPE, typeIO);
                        args.putString(ParamConstants.TYPE, typeIO);
                        context.loadFragment(new TripListFragment(), args);
            /*new Handler().post(new Runnable(){
              @Override
              public void run(){*/
                        removeFromBackStack(TripHUListFragment.this);
              /*}
            });*/
                    } else popBackStack();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateProcessingTripAndGoBack() {
        try {
            if (tripStatus.isManualTrip() && tripStatus.getNumberOfHu() > tripStatus.getCompletedHu()) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            huStatusDao.deleteAllNonCompletedTripHus(typeIO, displayTripNo);
                            tripStatusDao.updateTripHUCounts(displayTripNo, typeIO, tripStatus.getCompletedHu(), tripStatus.getCompletedHu());
                            updateTripStatusAndGoBack();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                updateTripStatusAndGoBack();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void popBackStack() {
        AppCommonMethods.showLog("popBackStack", "called");
        context.clearIOConfig(displayTripNo);
        super.popBackStack();
    }

    private void updateLists1() {
        if (dialogTripHuDetailsBinding == null) return;
        List<HUStatus> listTripHuDtls = huStatusDao.getHuList(typeIO, displayTripNo, "", sortByValues1);
        listTripHus.clear();
        listTripHus.addAll(listTripHuDtls);
        if (dialogTripHuDetailsBinding != null && dialogTripHuDetailsBinding.listDialogHuData != null && dialogTripHuDetailsBinding.listDialogHuData.getAdapter() != null)
            dialogTripHuDetailsBinding.listDialogHuData.getAdapter().notifyDataSetChanged();
    }

    private void updateLists2() {
        if (isNullOrEmpty(huNum) || dialogHuDetailsBinding == null) return;
        List<MultiQtyModel> listHuDtls = huDetailsDao.getHUDisplayDetails(typeIO, displayTripNo, huNum, tripStatus.isArticleBasedTrip(), sortByValues2);
        listHuDetails.clear();
        listHuDetails.addAll(listHuDtls);
        final boolean hasArticleData = tripStatus.isArticleBasedTrip() && huDetailsDao.hasArticleData(typeIO, displayTripNo, huNum);
        final String lblTitle = hasArticleData ? labelArticle : labelSku;
        if (dialogHuDetailsBinding != null && dialogHuDetailsBinding.llListHeader != null && dialogHuDetailsBinding.llListHeader.txtTitle != null)
            dialogHuDetailsBinding.llListHeader.txtTitle.setText(lblTitle);
        if (dialogHuDetailsBinding != null && dialogHuDetailsBinding.listDialogHuDetails != null && dialogHuDetailsBinding.listDialogHuDetails.getAdapter() != null)
            dialogHuDetailsBinding.listDialogHuDetails.getAdapter().notifyDataSetChanged();
    }

    private void showHUInfoDialog(final String huNo) {
        //if(isShowingDialog()) return;
        if (huDetailsAlert == null || !huDetailsAlert.isShowing()) {
            List<MultiQtyModel> listHuDtls = huDetailsDao.getHUDisplayDetails(typeIO, displayTripNo, huNo, tripStatus.isArticleBasedTrip());//!tripStatus.isEanBasedTrip());
            if (isNonEmpty(listHuDtls)) {
                listHuDetails.clear();
                listHuDetails.addAll(listHuDtls);
                final boolean hasArticleData = tripStatus.isArticleBasedTrip() && huDetailsDao.hasArticleData(typeIO, displayTripNo, huNo);
                final String lblTitle = hasArticleData ? labelArticle : labelSku;
                huDetailsAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
                setAlertDialogCustomTitle(huDetailsAlert, String.format(getString(R.string.title__details), labelHU));
                final DialogHuDetailsBinding binding = DialogHuDetailsBinding.inflate(LayoutInflater.from(context));
                binding.llListHeader.txtTitle.setText(lblTitle);
                binding.llListHeader.imgNext.setVisibility(View.GONE);
                binding.listDialogHuDetails.setAdapter(new HUDetailsAdapter((MainActivity) context, TripHUListFragment.this, huDetailsAlert, listHuDetails, lblTitle));
                binding.listDialogHuDetails.setLayoutManager(new LinearLayoutManager(context));
                binding.listDialogHuDetails.setVisibility(View.VISIBLE);
                huDetailsAlert.setView(binding.getRoot());
                huDetailsAlert.setCancelable(false);
                huDetailsAlert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        dialogHuDetailsBinding = binding;
                        huNum = huNo;
                        setHeader2();
                        sortByValues2 = "";
                        resetHeader2(0);
                    }
                });
                huDetailsAlert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialogHuDetailsBinding = null;
                        huNum = "";
                    }
                });
                huDetailsAlert.show();
            }
        }
    }

    private void showTripHUListDialog() {
        if (isShowingDialog()) return;
        if (tripHuListAlert == null || !tripHuListAlert.isShowing()) {
            List<HUStatus> listHuDtls = huStatusDao.getHuList(typeIO, displayTripNo, "");
            if (isNonEmpty(listHuDtls)) {
                listTripHus.clear();
                listTripHus.addAll(listHuDtls);
                tripHuListAlert = new AlertDialog.Builder(context, R.style.AlertDialog).create();
                setAlertDialogCustomTitle(tripHuListAlert, String.format(getString(R.string.title__details), labelTrip));
                final DialogTripHuDetailsBinding binding = DialogTripHuDetailsBinding.inflate(LayoutInflater.from(context));
                binding.llListHeader.imgInfo.setVisibility(AppCommonMethods.isShowOnDemandTripHuDetails ? View.INVISIBLE : View.GONE);
                binding.llListHeader.imgAction.setVisibility(View.GONE);
                binding.llListHeader.txtHuNumber.setText(labelHU);
                binding.listDialogHuData.setAdapter(new HUListAdapter(context, this, listTripHus, AppCommonMethods.isShowOnDemandTripHuDetails, false, true));
                binding.listDialogHuData.setLayoutManager(new LinearLayoutManager(context));
                binding.listDialogHuData.setVisibility(View.VISIBLE);
                tripHuListAlert.setView(binding.getRoot());
                tripHuListAlert.setCancelable(false);
                tripHuListAlert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        dialogTripHuDetailsBinding = binding;
                        setHeader1();
                        sortByValues1 = "";
                        resetHeader1(0);
                    }
                });
                tripHuListAlert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialogTripHuDetailsBinding = null;
                    }
                });
                tripHuListAlert.show();
            }
        }
    }

    public void onPostData(final Bundle args) {
        final boolean isShowDialog = extractBoolean(args, ParamConstants.IS_SHOW_HU_INFO_DIALOG, false);
        if (isShowDialog) showTripHUListDialog();
        else updateLists();
    }

    public void onPostData(final String huNum, final Bundle args) {
        final boolean isShowDialog = extractBoolean(args, ParamConstants.IS_SHOW_HU_INFO_DIALOG, false);
        if (isShowDialog) showHUInfoDialog(huNum);
        else {
            HUStatus huStatus = huStatusDao.getHUData(typeIO, displayTripNo, huNum);
            if (huStatus != null && huStatus.getExpQty() <= 0) {
                //update exp qty from hu details
                //huStatus.expQty = huDetailsDao.getTotalExpQty(typeIO, displayTripNo, huNum);
                final int expQty = huDetailsDao.getTotalExpQty(typeIO, displayTripNo, huNum);
                huStatus.expQty = expQty;
                huStatusDao.updateHUStatusExpQty(displayTripNo, huNum, typeIO, expQty);
                args.putSerializable(huStatus.getClass().getSimpleName(), huStatus);
            }
            if (huStatus != null && (huStatus.getStatus().equalsIgnoreCase(AppConstants.HU_STATUS_COMPLETE) || huStatus.getStatus().equalsIgnoreCase(AppConstants.STATUS_COMPLETE) || huStatus.getStatus().equalsIgnoreCase(AppConstants.STATUS_COMPLETED)))
                context.showCustomErrDialog(String.format(context.getResources().getString(R.string.err__already_completed), labelHU + ":" + huStatus.getHuNumber()));
            else if (huStatus != null && huStatus.getExpQty() <= 0)
                context.showCustomErrDialog(String.format(context.getResources().getString(R.string.err_no_associated__found_for__), labelArticle, labelHU + ":" + huStatus.getHuNumber()));
            else {
                context.loadFragment(new HuProcessStartFragment(), args);
                clearField();
            }
        }
    }

    @Override
    public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args) {
        super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
        try {
            switch (url) {
                case URLConstants.RELEASE_TRIP:
                    if (isSuccess) {
                        updateProcessingTripAndGoBack();
                    }
                    break;
                case URLConstants.COMPLETE_TRIP:
                    if (isSuccess) {
                        context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tripStatusDao.updateCompleteTripStatus(displayTripNo, typeIO);
                                popBackStack();
                            }
                        });
                    }
                    break;
                case URLConstants.GET_TRIP_HU_COUNT:
                    if (isSuccess && jsonResponse != null) {
                        TripStatus dummy = getGSON().fromJson(jsonResponse.toString(), TripStatus.class);
                        if (displayTripNo.equalsIgnoreCase(chkNull(dummy.refTripNumber, dummy.tripNumber))) {
                            tripStatusDao.updateTripHUCounts(displayTripNo, typeIO, dummy.getNumberOfHu(), dummy.getCompletedHu());
                            updateView();
                        }
                    }
                    break;
                case URLConstants.GET_HU_DATA:
                    if (isSuccess && jsonResponse != null) {
                        final JSONObject config = extractJSONObject(jsonResponse, ParamConstants.CONFIG, jsonResponse);
                        final String tripNum = extractString(config, ParamConstants.TRIP_NUMBER, extractString(jsonResponse, ParamConstants.TRIP_NUMBER, extractString(jsonRequest, ParamConstants.TRIP_NUMBER, extractString(args, ParamConstants.TRIP_NUMBER, ""))));
                        final String tripRefNum = extractString(config, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(jsonResponse, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(jsonRequest, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(args, ParamConstants.REFERENCE_TRIP_NUMBER, ""))));
                        final String useTripNum = !tripRefNum.equalsIgnoreCase(displayTripNo) && tripNum.equalsIgnoreCase(tripNum) ? displayTripNo : chkNull(tripRefNum, tripNum);
                        context.saveIOConfig(config, useTripNum);
                        final JSONArray huDataArray = extractJSONArray(jsonResponse, ParamConstants.HU_DATA, extractJSONArray(jsonResponse, ParamConstants.HUS, extractJSONArray(jsonResponse, ParamConstants.DATA)));
                        if (isNonEmpty(huDataArray))
                            new InsertDBHUs(context, this, url, typeIO, useTripNum, jsonResponse, args).execute(huDataArray);
                        else hideProgressDialog();
                    }
                    break;
                case URLConstants.GET_HU_DETAILS:
                    if (isSuccess && jsonResponse != null) {
                        final JSONObject config = extractJSONObject(jsonResponse, ParamConstants.CONFIG, jsonResponse);
                        final String tripNum = extractString(config, ParamConstants.TRIP_NUMBER, extractString(jsonResponse, ParamConstants.TRIP_NUMBER, extractString(jsonRequest, ParamConstants.TRIP_NUMBER, extractString(args, ParamConstants.TRIP_NUMBER, ""))));
                        final String tripRefNum = extractString(config, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(jsonResponse, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(jsonRequest, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(args, ParamConstants.REFERENCE_TRIP_NUMBER, ""))));
                        final String useTripNum = !tripRefNum.equalsIgnoreCase(displayTripNo) && tripNum.equalsIgnoreCase(tripNum) ? displayTripNo : chkNull(tripRefNum, tripNum);
                        final String huNum = extractString(config, ParamConstants.K_TRIP_HU_NUMBER, extractString(jsonResponse, ParamConstants.K_TRIP_HU_NUMBER, extractString(jsonRequest, ParamConstants.K_TRIP_HU_NUMBER, extractString(args, ParamConstants.K_TRIP_HU_NUMBER, ""))));
                        final String status = extractString(config, ParamConstants.K_TRIP_HU_STATUS, extractString(jsonResponse, ParamConstants.STATUS, AppConstants.HU_STATUS_PENDING));
                        final boolean isShowDialog = extractBoolean(args, ParamConstants.IS_SHOW_HU_INFO_DIALOG, false);
                        if (!isShowDialog && (status.equalsIgnoreCase(AppConstants.STATUS_COMPLETE) || status.equalsIgnoreCase(AppConstants.STATUS_COMPLETED) || status.equalsIgnoreCase(AppConstants.HU_STATUS_COMPLETE))) {
                            context.showCustomErrDialog(String.format(context.getResources().getString(R.string.err__already_completed), labelHU + ":" + huNum));
                            return;
                        }

                        context.saveIOConfig(config, useTripNum, huNum);
                        final JSONArray huDetailsArray = extractJSONArray(jsonResponse, ParamConstants.HU_DATA, extractJSONArray(jsonResponse, ParamConstants.K_TRIP_HU_DETAILS, extractJSONArray(jsonResponse, ParamConstants.DATA)));
                        if (isNonEmpty(huDetailsArray)) {
                            //TODO check/update huStatus
                            if (isOnDemandTripHuList) {
                                HUStatus huStat = huStatusDao.getHUData(typeIO, displayTripNo, huNum);
                                if (huStat == null) {
                                    HUStatus huStatus = new HUStatus();
                                    huStatus.setType(typeIO);
                                    huStatus.setTripNumber(useTripNum);
                                    huStatus.setHuNumber(huNum);
                                    huStatus.setStatus(status);
                                    huStatus.setExpQty(0);
                                    huStatus.setScanQty(0);
                                    huStatus.setReason("");
                                    huStatus.setUploaded(false);
                                    huStatus.setManualHU(false);
                                    huStatusDao.insertHUStatusData(huStatus);
                                    huStat = huStatus;
                                } else if (huStat != null && !huStat.status.equalsIgnoreCase(status)) {
                                    huStat.status = status;
                                    huStatusDao.updateHUStatusData(huStat);
                                }
                                if (huStat != null) {
                                    if (args == null) args = chkNull(getArguments(), new Bundle());
                                    if (args.containsKey(huStat.getClass().getSimpleName()))
                                        args.putSerializable(huStat.getClass().getSimpleName(), huStat);
                                }
                            }
                            new InsertDBHUDetails(context, this, url, typeIO, useTripNum, huNum, jsonResponse, args).execute(huDetailsArray);
                        } else hideProgressDialog();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
        }
    }
}