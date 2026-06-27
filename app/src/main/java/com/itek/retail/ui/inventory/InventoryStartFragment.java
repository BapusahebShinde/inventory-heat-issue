package com.itek.retail.ui.inventory;

import static com.itek.retail.common.AppCommonMethods.extractInt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.common.AppConstants;
import com.itek.retail.common.InventoryScanFragment;
import com.itek.retail.databinding.FragmentInventoryStartBinding;
import com.itek.retail.model.Zone;

import java.util.Set;

/**
 * The Inventory start fragment.
 */
public class InventoryStartFragment extends InventoryScanFragment {

    //temp flags
    boolean isAPICallForSessionResume = false;
    private FragmentInventoryStartBinding binding;
    private InventoryStartViewModel mViewModel;

    /**
     * Instantiates a new Inventory start fragment.
     */
    public InventoryStartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(InventoryStartViewModel.class);

        binding = FragmentInventoryStartBinding.inflate(inflater, container, false);

        activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
        sessionValidTill = extractInt(getArguments(), AppConstants.SESSION_VALID_TILL, 48);

        //Redirection to Unencoded Search
        setViews(binding.header, binding.spinInventoryStartLocation, binding.llSeekbarPower, binding.llBtnStart, binding.ctwInventoryStart, binding.ctwAlien, binding.ctwUnencoded, binding.llInventoryStartSessionLbls, binding.btnUpload);

        return binding.getRoot();
    }

    @Override
    protected boolean validateBeforeInvSessionStart() {
        return super.validateBeforeInvSessionStart();
    }

    @Override
    protected String getSelectedZone() {
        return super.getSelectedZone();
    }

    @Override
    protected Zone getSelectedZoneObject() {
        return super.getSelectedZoneObject();
    }

    @Override
    protected Set<String> getSelectedZones() {
        return super.getSelectedZones();
    }
}