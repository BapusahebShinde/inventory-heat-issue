package com.itek.retail.ui.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.common.InventoryScanFragment;
import com.itek.retail.databinding.FragmentInventoryAddBinding;

/**
 * The Inventory start fragment.
 */
public class InventoryAddFragment extends InventoryScanFragment {

    //temp flags
    boolean isAPICallForSessionResume = false;
    private FragmentInventoryAddBinding binding;
    private InventoryAddViewModel mViewModel;

    /**
     * Instantiates a new Inventory start fragment.
     */
    public InventoryAddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(InventoryAddViewModel.class);

        binding = FragmentInventoryAddBinding.inflate(inflater, container, false);

        setViews(binding.header, binding.spinInventoryStartLocation, binding.llSeekbarPower, binding.llBtnStart, binding.ctwInventoryStart, binding.ctwAlien, binding.ctwUnencoded, binding.llInventoryStartSessionLbls, binding.btnUpload);

        return binding.getRoot();
    }
}