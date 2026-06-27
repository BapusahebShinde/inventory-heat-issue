package com.itek.retail.common;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT_PATTERN;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isAllowInventoryUpload;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itek.retail.R;
import com.itek.retail.databinding.HeaderTitleLayoutBinding;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.customviews.BtnStartStopView;
import com.itek.retail.ui.customviews.BtnSwipeUploadView;
import com.itek.retail.ui.customviews.CountTotalView;
import com.itek.retail.ui.customviews.PowerView;
import com.itek.retail.ui.customviews.swipeButton.ProSwipeButtonVar;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class CommonInventoryFragment extends RFIDSessionFragment {

    protected Set<String> eans = new HashSet<>(0);
    protected JSONObject extras;
    protected CountTotalView ctwInventoryStart;
    protected BtnStartStopView llBtnStart;
    protected PowerView llSeekbarPower;

    protected BtnSwipeUploadView clUpload;
    protected FloatingActionButton btnUpload;
    protected ProSwipeButtonVar btnSwipeUpload;

    /**
     * Instantiates a new Common Inventory fragment.
     */
    public CommonInventoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setViews(final HeaderTitleLayoutBinding header, final PowerView llSeekbarPower, final BtnStartStopView llBtnStart, final CountTotalView ctwInventoryStart) {
        super.setViews(header, llSeekbarPower);
        if (llSeekbarPower != null) {
            this.llSeekbarPower = llSeekbarPower;
        }
        if (llBtnStart != null) {
            this.llBtnStart = llBtnStart;
            llBtnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBtnStartClick();
                }
            });
        }
        if (ctwInventoryStart != null) {
            this.ctwInventoryStart = ctwInventoryStart;
            ctwInventoryStart.setTotal(getInvCount());
        }
    }

    protected void setViews(final HeaderTitleLayoutBinding header, final PowerView llSeekbarPower, final BtnStartStopView llBtnStart, final CountTotalView ctwInventoryStart, final BtnSwipeUploadView swipeUpload) {
        setViews(header, llSeekbarPower, llBtnStart, ctwInventoryStart);
        setBtnUploadSwipe(swipeUpload);
    }
    protected void setViews(final HeaderTitleLayoutBinding header, final PowerView llSeekbarPower, final BtnStartStopView llBtnStart, final CountTotalView ctwInventoryStart, final FloatingActionButton btnUpload, final ProSwipeButtonVar btnSwipeUpload) {
        setViews(header, llSeekbarPower, llBtnStart, ctwInventoryStart);
        setBtnUploadSwipe(btnUpload, btnSwipeUpload);
    }

    protected boolean validateBeforeInvSessionStart() {
        return true;
    }

    protected void onBtnStartClick() {
        showLog("onBtnStartClick","start");
        context.dismissCustomAlertDialog();
        if (!validateBeforeInvSessionStart()) return;
        showLog("onBtnStartClick","validated");
        final Boolean isInventorySessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
        if (!isInventorySessionOn) {
            showLog("onBtnStartClick_sessionObject",""+(sessionObject != null));
            if (sessionObject != null) mainViewModel.startSession(sessionObject, eans,true);
            else if (sessionObject == null) apiCall(AppConstants.SESSION_ACTION_START);
        } else {
            if (getSize() >= AppCommonMethods.invLimit) {
                if (chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
                    mainViewModel.stopInventory();
                context.showCustomErrDialog(R.string.err_inventory_max_limit);
            } else toggleInventory();
        }
    }

    @Override
    protected void onReaderPowerChanged(Integer power) {
        if (llSeekbarPower != null && mainViewModel != null)
            llSeekbarPower.updateReaderPower(mainViewModel, power);
    }

    @Override
    protected void onReaderConfigured() {
        super.onReaderConfigured();
    }

    @Override
    protected void onTriggerPressed() {
        showLog("onTriggerPressed_llBtnStart",""+(llBtnStart != null));
        if (llBtnStart != null) llBtnStart.performClick();
    }

    @Override
    protected void onDataSizeChanged(Integer size) {
        if (chkNull(size, 0) > 0 && sessionObject != null && !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))
            mainViewModel.startSession(sessionObject, eans, false);
        final int totalTags = getInventoryTotalSize();
        final int alienTags = getAlignTagCount();
        final int unencodedTagCount = getUnencodedTagCount();
        final int invScore = totalTags - (alienTags + unencodedTagCount);
        final boolean isInvCount = chkNull(size, 0) > 0;
        if (ctwInventoryStart != null)
            ctwInventoryStart.setScore(chkNull(invScore < 0 ? 0 : invScore, 0));
        if (btnUpload != null)
            btnUpload.setVisibility(isAllowInventoryUpload && chkNotNullFalse(mainViewModel.getIsInventoryOn().getValue()) && isInvCount ? View.VISIBLE : View.GONE);
        if (btnSwipeUpload != null)
            btnSwipeUpload.setVisibility(isAllowInventoryUpload && AppCommonMethods.isUploadSlider && chkNotNullFalse(mainViewModel.getIsInventoryOn().getValue()) && isInvCount ? View.VISIBLE : View.GONE);
        if (clUpload != null)
            clUpload.setVisibility(isAllowInventoryUpload && AppCommonMethods.isUploadSlider && chkNotNullFalse(mainViewModel.getIsInventoryOn().getValue()) && isInvCount ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void isSessionOnChanged(final Boolean isInventorySessionOn) {
        super.isSessionOnChanged(isInventorySessionOn);
        if (isInventorySessionOn == null) return;
        final boolean isInvCount = getSize() > 0;
        if (llBtnStart != null) llBtnStart.toggle(isInventorySessionOn);
        if (btnUpload != null)
            btnUpload.setVisibility(isAllowInventoryUpload && chkNotNullFalse(isInventorySessionOn) && isInvCount ? View.VISIBLE : View.GONE);
        if (btnSwipeUpload != null)
            btnSwipeUpload.setVisibility(isAllowInventoryUpload && AppCommonMethods.isUploadSlider && chkNotNullFalse(isInventorySessionOn) && isInvCount ? View.VISIBLE : View.GONE);
        if (clUpload != null)
            clUpload.setVisibility(isAllowInventoryUpload && AppCommonMethods.isUploadSlider && chkNotNullFalse(isInventorySessionOn) && isInvCount ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void isInventoryOnChanged(Boolean isInventoryOn) {
        super.isInventoryOnChanged(isInventoryOn);
        if (isInventoryOn != null) {
            if (llSeekbarPower != null) {
                llSeekbarPower.setEnabled(!isInventoryOn);
                llSeekbarPower.setVisibility(!isInventoryOn && llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
            }
            if (llBtnStart != null) llBtnStart.toggle(isInventoryOn);
            final boolean isInvCount = getSize() > 0;
            if (btnUpload != null)
                btnUpload.setVisibility(isAllowInventoryUpload && chkNotNullFalse(isInventoryOn) && isInvCount ? View.VISIBLE : View.GONE);
            if (btnSwipeUpload != null)
                btnSwipeUpload.setVisibility(isAllowInventoryUpload && AppCommonMethods.isUploadSlider && chkNotNullFalse(isInventoryOn) && isInvCount ? View.VISIBLE : View.GONE);
            if (clUpload != null)
                clUpload.setVisibility(isAllowInventoryUpload && AppCommonMethods.isUploadSlider && chkNotNullFalse(isInventoryOn) && isInvCount ? View.VISIBLE : View.GONE);
        }
    }


    /**
     * Set session action.
     *
     * @param action    the action
     * @param sessionId the session id
     */
    protected void setSessionAction(String action, String sessionId) {
        setSessionAction(action, sessionId, null, null, null);
    }

    /**
     * Set session action.
     *
     * @param action          the action
     * @param sessionId       the session id
     * @param sessionTime     the session time
     * @param inventoryCount  the inventory count
     * @param activeUserCount the active user count
     */
    protected void setSessionAction(String action, String sessionId, String sessionTime, Long inventoryCount, Integer activeUserCount) {
        hideProgressDialog();
        setActiveUsers(activeUserCount != null ? activeUserCount.intValue() : -2);
        String totInvCount = sessionObject != null ? sessionObject.total : chkNull(inventoryCount, -1L) >= 0 ? chkZero(inventoryCount, "-") : getInvCount();
        showLog("totInvCount", totInvCount);
        if (ctwInventoryStart != null) ctwInventoryStart.setTotal(totInvCount);
        if (sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)) {
            RFIDSession sessionObject = new RFIDSession();
            final Object selZoneObj = getSelectedZoneObject();
            sessionObject.zoneId = selZoneObj != null && selZoneObj instanceof Zone ? ((Zone) selZoneObj).getZoneId() : "0";
            sessionObject.zone = selZoneObj != null && selZoneObj instanceof Zone ? ((Zone) selZoneObj).getZoneName() : getSelectedZone();
            sessionObject.brands = getBrandsForSession();
            sessionObject.eans = getEansForSession();
            sessionObject.brandEan = "";
            sessionObject.extras = getExtrasForSession();
            sessionObject.total = totInvCount;
            sessionObject.sessionType = getSessionType().getValue();
            sessionObject.sessionAction = AppCommonMethods.SessionAction.INVENTORY.getValue();
            sessionObject.userId = SharedPrefManager.getUserID();
            //TODO if(isNonEmpty(listIgnoreEpcs)) //Save Locally
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
            AppCommonMethods.showLog("sessionType_sessionId", sessionObject.sessionType + "_" + sessionObject.sessionId);
            setSessionObject(sessionObject);
            mainViewModel.startSession(sessionObject, eans, true);
        } else if (sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_RESUME))
            mainViewModel.startSession(sessionObject, eans, true);
        else if (sessionObject != null && !action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")")) {
            mainViewModel.stopSession(sessionObject, action.matches("(?i)(" + AppConstants.SESSION_ACTION_UPLOAD + "|" + AppConstants.SESSION_ACTION_DISCARD + ")"));
            if (action.equalsIgnoreCase(AppConstants.SESSION_ACTION_SAVE)) {
                context.showCustomAlertDialog("", getString(R.string.success_session_save), true, true, getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.clearBackStack();
                    }
                });
            } else context.popBackStack();
        }
    }


    protected String getEansForSession() {
        return isNullOrEmpty(eans) ? "" : eans.toString().replaceAll("\\s*,\\s*", ",").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim();
    }

    public String getInvCount() {
        return "";
    }


    protected String getBrandsForSession() {
        return "";
    }

    protected String getExtrasForSession() {
        return isNonEmpty(extras) ? extras.toString() : "";
    }

    protected String getSelectedZone() {
        return "";
    }

    protected Set<String> getSelectedZones() {
        return null;
    }

    protected Zone getSelectedZoneObject() {
        return null;
    }

    protected void onBtnUploadSwiped() {
    }

    protected void resetViewsOnUpload() {
        if (ctwInventoryStart != null) ctwInventoryStart.setScore(0);
        if (clUpload != null) clUpload.setVisibility(View.GONE);
        if (btnUpload != null) btnUpload.setVisibility(View.GONE);
        if (btnSwipeUpload != null) btnSwipeUpload.setVisibility(View.GONE);
    }
    
    protected void setBtnUploadSwipe(final BtnSwipeUploadView swipeUpload) {
        if (swipeUpload == null) return;
        this.clUpload=swipeUpload;
        swipeUpload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onBtnUploadSwiped();
            }
        });
    }

    protected void setBtnUploadSwipe(final FloatingActionButton btnUpload, ProSwipeButtonVar btnSwipeUpload) {
    
        if (btnUpload == null || btnSwipeUpload == null) return;
        if (btnUpload != null) {
            this.btnUpload = btnUpload;
            btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (btnSwipeUpload != null && btnSwipeUpload.getVisibility() == View.VISIBLE)
                        return;
                    onBtnUploadSwiped();
                }
            });
        }
        if (btnSwipeUpload != null) {
            this.btnSwipeUpload = btnSwipeUpload;
            btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean isSuccessfulSwipe) {
                    if (!isTopInStack()) return;
                    boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
                    if (isSwiped) {
                        btnSwipeUpload.reset();
                        onBtnUploadSwiped();
                    }
                }
            });
        }
        if (btnSwipeUpload.getParent() != null && btnSwipeUpload.getParent() instanceof ConstraintLayout)
            setUploadConstraints((ConstraintLayout) btnSwipeUpload.getParent(), btnUpload, btnSwipeUpload);
        else if (btnUpload.getParent() != null && btnUpload.getParent() instanceof ConstraintLayout)
            setUploadConstraints((ConstraintLayout) btnSwipeUpload.getParent(), btnUpload, btnSwipeUpload);
    }


}

