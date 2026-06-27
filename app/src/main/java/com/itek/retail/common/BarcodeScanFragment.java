package com.itek.retail.common;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;

import com.itek.retail.R;
import com.itek.retail.ui.customviews.ScanCodesInputAndKeypadView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;

public class BarcodeScanFragment extends CommonFragment {
    private final AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.SCAN;
    private MainViewModel mainViewModel;
    private ScanCodesInputAndKeypadView inputView;
    private View btnGo;

    /**
     * Instantiates a new Barcode scan fragment.
     */
    public BarcodeScanFragment() {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTriggerDataObserver();
        setObservers();
        mainViewModel.getIsDeviceConfigured().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isReaderConfigured) {
                if (!isTopInStack()) return;

                AppCommonMethods.showLog(this.getClass().getSimpleName() + " isReaderConfigured", AppCommonMethods.chkVal(isReaderConfigured));
                if (chkNotNullTrue(isReaderConfigured)) {
                    setObservers();
                }
            }
        });
    }

    @Override
    public AppCommonMethods.SessionType getSessionType() {
        return sessionType;
    }

    protected void setInputView(final ScanCodesInputAndKeypadView inputView) {
        setInputView(inputView, null);
    }

    protected void setInputView(final ScanCodesInputAndKeypadView inputView, final View btnGo) {
        if (inputView == null) return;
        this.inputView = inputView;
        if (btnGo != null) this.btnGo = btnGo;
        inputView.setImgScanOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNullOrEmpty(inputView.getText()) && mainViewModel != null && !chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())) {
                    context.dismissCustomAlertDialog();
                    showLog("Fragment softScan", "softScan");
                    mainViewModel.softScan();
                }
            }
        });

        if (btnGo != null) inputView.setGoBtn(btnGo);

    }

    /**
     * Set observers.
     */
    void setObservers() {
        mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsBarcodeOn().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isBarcodeOn) {
                if (!isTopInStack()) return;
                showLog("isBarcodeOn", "" + chkNotNullTrue(isBarcodeOn));
                insertAuditTrailsLog("Barcode_" + (chkNotNullTrue(isBarcodeOn) ? "ON" : "OFF"));
                ((MainActivity) context).lockDrawer(chkNotNullTrue(isBarcodeOn));
                if (inputView != null)
                    inputView.setIsProcessOn(chkNotNullTrue(isBarcodeOn));//setIsViewControlEnabled(!chkNotNullTrue(isBarcodeOn));
            }
        });

        mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getBarcodeData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String barcode) {
                if (!isTopInStack()) return;
                //  showLog(ProductSearchFragment.this.getClass().getSimpleName() + "_barcodeData", "" + chkNull(barcode, ""));
                if (isNonEmpty(barcode)) {
                    if (inputView != null) inputView.setText(barcode);
                    if (inputView != null && btnGo != null) btnGo.performClick();
                }
                if (isNonEmpty(barcode)) {
                    mainViewModel.getBarcodeData().postValue("");
                }
            }
        });
        showLog("setObservers", "true");
    }

    /**
     * Set trigger data observer.
     */
    private void setTriggerDataObserver() {
        mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
        mainViewModel.isTriggerPressed().observe(getViewLifecycleOwner(), triggerPressed -> {
            if (!isTopInStack()) return;

            if (triggerPressed != null && getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && allowBtnClick && isTopInStack()) {
                AppCommonMethods.showLog("isTriggerPressed", "" + triggerPressed);
                if (triggerPressed && !chkNotNullTrue(mainViewModel.getIsProcessOn().getValue()) && !chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())) {
                    if (inputView != null) inputView.performScan();
                    else mainViewModel.softScan();
                }
            }
        });
    }

    protected boolean checkReaderConnected(){
        if(mainViewModel.isReaderConnected()) return true;
        else{
            context.showCustomAlertDialog("", String.format(getString(R.string.err_reader_connection), getTypeCharCode()), getString(R.string.btn_ok), (dialogInterface, i) -> {
                if(((MainActivity) context).isReaderConnected()) mainViewModel.performPick("");
                else mainViewModel.checkAndConnectReader();
            });
            return false;
        }
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
        mainViewModel.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mainViewModel.getIsDeviceConfigured().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsBarcodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getBarcodeData().removeObservers(getViewLifecycleOwner());
        mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
        showLog("removeObservers", "true");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mainViewModel.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mainViewModel != null && chkNotNullTrue(mainViewModel.getIsBarcodeOn().getValue())) {
            context.showCustomAlertDialog("", String.format(getString(R.string.err_op_back_press), getTypeCharCode(), sessionType.name()), false, true, getString(R.string.btn_ok), null);
        }
        else onBackPress();
    }

    protected void onBackPress() {super.onBackPressed();}

    protected boolean isProcessOn(){
        return mainViewModel != null && chkNotNullTrue(mainViewModel.getIsProcessOn().getValue());
    }
}
