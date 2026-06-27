package com.itek.retail.ui.home;

import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.adapter.DashboardAdapter;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.MenuDao;
import com.itek.retail.databinding.FragmentTabFavoritesBinding;
import com.itek.retail.model.MenuModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The Tab favourites fragment.
 */
public class TabFavouritesFragment extends CommonFragment{
  
  int totalFavMenus = 0;
  ArrayList<String> listSavedFavMenuCodes = new ArrayList<>(0);
  List<MenuModel> listSavedFavMenus = new ArrayList<>(0);
  MenuDao menuDao;
  private FragmentTabFavoritesBinding binding;
  
  /**
   * Instantiates a new Tab favourites fragment.
   */
  public TabFavouritesFragment(){
    // Required empty public constructor
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    binding = FragmentTabFavoritesBinding.inflate(inflater, container, false);
    menuDao = AppDatabase.getMenuDao(context);
    listSavedFavMenuCodes = SharedPrefManager.getSavedFavMenuCodes();
    totalFavMenus = menuDao.getTotalFavMenuSize();
    showLog("totalFavMenus", "" + totalFavMenus);
    
    listSavedFavMenus.clear();
    if(isNonEmpty(listSavedFavMenuCodes)){
      int i = 0;
      String orderBy = "Case";
      for(String id : listSavedFavMenuCodes)
        orderBy = orderBy.concat(" WHEN " + id + "' THEN " + (++i));
      orderBy = orderBy.concat(" END");
      listSavedFavMenus = menuDao.getCurrentFavMenus(listSavedFavMenuCodes, orderBy);
    }
    if(listSavedFavMenus.size() < totalFavMenus) listSavedFavMenus.add(AppConstants.MENU_ADD_MORE);
    binding.gridMenusFavorites.setAdapter(new DashboardAdapter((MainActivity) context, this, listSavedFavMenus));
    binding.gridMenusFavorites.setLayoutManager(new GridLayoutManager(context, isLandscape ? 5 : 3));
    return binding.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    notifyListChanges();
  }
  
  /**
   * Notify list changes.
   */
  void notifyListChanges(){
    if(binding.gridMenusFavorites != null && binding.gridMenusFavorites.getAdapter() != null && binding.gridMenusFavorites.getAdapter() instanceof RecyclerView.Adapter)
      ((RecyclerView.Adapter) binding.gridMenusFavorites.getAdapter()).notifyDataSetChanged();
  }
  
  @Override
  public void updateLists(int replacePos, boolean isRemove, final MenuModel itemModel){
    if(itemModel != null){
      final boolean isReplace = replacePos >= 0 && replacePos < listSavedFavMenus.size() - 1;
      if(isReplace && !listSavedFavMenus.contains(itemModel) && !listSavedFavMenuCodes.contains(itemModel.getMenuCode()) && replacePos >= 0 && replacePos < listSavedFavMenus.size() && replacePos < listSavedFavMenuCodes.size()){
        listSavedFavMenus.set(replacePos, itemModel);
        listSavedFavMenuCodes.set(replacePos, itemModel.getMenuCode());
        SharedPrefManager.setSavedFavMenuCodes(listSavedFavMenuCodes);
      }
      else if(!isReplace && !isRemove && !listSavedFavMenus.contains(itemModel) && !listSavedFavMenuCodes.contains(itemModel.getMenuCode())){
        if(listSavedFavMenus.contains(AppConstants.MENU_ADD_MORE))
          listSavedFavMenus.remove(AppConstants.MENU_ADD_MORE);
        listSavedFavMenus.add(itemModel);
        listSavedFavMenuCodes.add(itemModel.getMenuCode());
        SharedPrefManager.setSavedFavMenuCodes(listSavedFavMenuCodes);
        if(listSavedFavMenus.size() < menuDao.getTotalFavMenuSize() && !listSavedFavMenus.contains(AppConstants.MENU_ADD_MORE))
          listSavedFavMenus.add(AppConstants.MENU_ADD_MORE);
      }
      else if(!isReplace && isRemove && listSavedFavMenus.contains(itemModel) && listSavedFavMenuCodes.contains(itemModel.getMenuCode())){
        listSavedFavMenus.remove(itemModel);
        listSavedFavMenuCodes.remove(itemModel.getMenuCode());
        SharedPrefManager.setSavedFavMenuCodes(listSavedFavMenuCodes);
        if(listSavedFavMenus.size() < totalFavMenus && !listSavedFavMenus.contains(AppConstants.MENU_ADD_MORE))
          listSavedFavMenus.add(AppConstants.MENU_ADD_MORE);
      }
      notifyListChanges();
    }
  }
}