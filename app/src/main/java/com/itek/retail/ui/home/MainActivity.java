package com.itek.retail.ui.home;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.errorBeep;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractLong;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isCheckEncPasswordBeforeAPI;
import static com.itek.retail.common.AppCommonMethods.isCheckExistingZoneDataForInv;
import static com.itek.retail.common.AppCommonMethods.isConfirmUserActionIfNewListAvailable;
import static com.itek.retail.common.AppCommonMethods.isDemoApp;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isLockAndRedirectToInProcessHU;
import static com.itek.retail.common.AppCommonMethods.isLockAndRedirectToProcessingManualTrip;
import static com.itek.retail.common.AppCommonMethods.isLockAndRedirectToProcessingTrip;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNotifyUserActionIfNewListAvailable;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isSetUserMgmt;
import static com.itek.retail.common.AppCommonMethods.isShowErrorForNoZonesInAPIResponse;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;
import static com.itek.retail.common.AppCommonMethods.isUseNewUIForLBS;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.navigation.NavigationView;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.InsertDBBrandZones;
import com.itek.retail.common.InsertDBBrandsCategories;
import com.itek.retail.common.InsertDBProducts;
import com.itek.retail.common.InsertDBTrips;
import com.itek.retail.common.InsertDBZones;
import com.itek.retail.common.LogFileUtilityHHD;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.BrandDao;
import com.itek.retail.database.CategoryDao;
import com.itek.retail.database.HUDetailsDao;
import com.itek.retail.database.HUStatusDao;
import com.itek.retail.database.MenuDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.ActivityMainBinding;
import com.itek.retail.databinding.NavHeaderMainBinding;
import com.itek.retail.model.DecodeType;
import com.itek.retail.model.HUStatus;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.LabelIds;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.OutwardBatch;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.SiteCode;
import com.itek.retail.model.SiteType;
import com.itek.retail.model.TripStatus;
import com.itek.retail.model.Zone;
import com.itek.retail.reader.RFIDHandler;
import com.itek.retail.sensors.HardwareChecker;
import com.itek.retail.ui.actionmenu.ActionMenuMsgFragment;
import com.itek.retail.ui.actionmenu.ActionMenuNotifyFragment;
import com.itek.retail.ui.actionmenu.ActionMenuSearchFragment;
import com.itek.retail.ui.decoding.DecodingStartFragment;
import com.itek.retail.ui.encoding.EncodingAchieveFragment;
import com.itek.retail.ui.encoding.EncodingScanScanWriteFragment;
import com.itek.retail.ui.encoding.EncodingStartFragment;
import com.itek.retail.ui.inventory.InventoryAddFragment;
import com.itek.retail.ui.inventory.InventoryBrandFragment;
import com.itek.retail.ui.inventory.InventoryFilterFragment;
import com.itek.retail.ui.inventory.InventoryStartFragment;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionMainFragment;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionStartFragment;
import com.itek.retail.ui.inward.InwardMainFragment;
import com.itek.retail.ui.inward.grn.InwardGrnHuScanFragment;
import com.itek.retail.ui.inward.grn.InwardGrnStartFragment;
import com.itek.retail.ui.inward.tote.InwardToteMainFragment;
import com.itek.retail.ui.inward1.HuProcessStartFragment;
import com.itek.retail.ui.inward1.TripHUListFragment;
import com.itek.retail.ui.inward1.TripListFragment;
import com.itek.retail.ui.landing.LandingActivity;
import com.itek.retail.ui.movement.MovementMainFragment;
import com.itek.retail.ui.movement.MovementStartFragment;
import com.itek.retail.ui.movement.replenishment.ReplenishmentListFragment;
import com.itek.retail.ui.movement.replenishment.ReplenishmentStartFragment;
import com.itek.retail.ui.navmenu.AppInfoFragment;
import com.itek.retail.ui.outward.OutwardPickStartFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuDataFragment;
import com.itek.retail.ui.outward.offrange.OffRangeMainFragment;
import com.itek.retail.ui.outward.tote.OutwardToteDCFragment;
import com.itek.retail.ui.outward.tote.OutwardToteMainFragment;
import com.itek.retail.ui.replenishondemand.ReplenishmentArticleListFragment;
import com.itek.retail.ui.replenishondemand.ReplenishmentBatchListFragment;
import com.itek.retail.ui.resetpassword.ResetPasswordFragment;
import com.itek.retail.ui.search.alien.SearchAlienFragment;
import com.itek.retail.ui.search.assortment.SearchAssortMainFragment;
import com.itek.retail.ui.search.fifo.SearchFIFOStartFragment;
import com.itek.retail.ui.search.filesearch.SearchFileBasedFragment;
import com.itek.retail.ui.search.listnewsearch.SearchListExcelFragment;
import com.itek.retail.ui.search.listnewsearch.SearchListExcelStartFragment;
import com.itek.retail.ui.search.listsearch.SearchListFragment;
import com.itek.retail.ui.search.listsearch.SearchListStartFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelStartFragment;
import com.itek.retail.ui.search.productsearch.ProductDetailsFragment;
import com.itek.retail.ui.search.productsearch.ProductSearchDetailsFragment;
import com.itek.retail.ui.search.unencoded.SearchUnencodedFragment;
import com.itek.retail.ui.than.ThanCuttingFragment;
import com.itek.retail.ui.than.ThanEncodingFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Main activity.
 */
public class MainActivity extends CommonActivity{
  
  public Object mReader;
  MainViewModel mainViewModel;
  private ActivityMainBinding binding;
  private boolean isMenusLocked = false;
  private ActionBarDrawerToggle toggle;
  
  @Override
  protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    showLog("onCreate", "" + (savedInstanceState != null));
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    binding.appBarMain.toolbar.setNavigationIcon(R.drawable.ic_nav_menu);
    setSupportActionBar(binding.appBarMain.toolbar);
    
    try{
      AppCommonMethods.mainActivity = this;
      if(savedInstanceState == null){
        try{
          new HardwareChecker(((SensorManager) getSystemService(SENSOR_SERVICE)));
        }
        catch(Exception e){
          e.printStackTrace();
        }
        
        AppDatabase db = AppDatabase.getDbInstance(MainActivity.this);
        //db.ProductDao().deleteAll();
        db.BrandWiseZoneInventoryDao().deleteAll();
        db.InventoryDao().deleteAllExcept();
        db.RFIDSessionDao().deleteAllExcept();
        deleteEmptySessions(db);
        showLog("hasCurrentSession", "" + (db.RFIDSessionDao().getCurrentSession(AppCommonMethods.SessionType.SEARCH_UNENCODED.getValue()) != null));
        if(SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_API, isCheckEncPasswordBeforeAPI) || SharedPrefManager.getBoolean(ParamConstants.IS_OFFLINE_ENCODE, AppCommonMethods.isOfflineEncode))
          callWebService(URLConstants.GET_ACCESS_PWD, new JSONObject(), null);
      }
      
      // final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
      //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER | Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
      
      mainViewModel = new MainViewModel(this);
      if(mainViewModel != null){
        if(binding != null){
          binding.setMainViewModel(mainViewModel);
          binding.executePendingBindings();
        }
        
        mainViewModel.readerInstance.removeObservers(this);
        mainViewModel.readerInstance.observe(this, new Observer<Object>(){
          @Override
          public void onChanged(Object obj){
            if(obj != null){
              //proceed
              mReader = obj;
            }
            else{/*unused*/}
          }
        });
        
        mainViewModel.readerUHFInstance.removeObservers(this);
        mainViewModel.readerUHFInstance.observe(this, obj -> {
          if(obj != null){
            //proceed
            mReader = obj;
          }
          else{/*unused*/}
        });
        
        mainViewModel.getPorgressStatus().observe(this, obj -> {
          if(obj != null && obj.isShowDialog()) showProgressDialog(obj.getMessage());
          else hideProgressDialog();
        });
        
        AppDatabase.getNotificationDao(this).getTotalNotificationCount(isSetUserMgmt ? SharedPrefManager.getUserID() : "").observe(this, new Observer<Integer>(){
          @Override
          public void onChanged(Integer size){
            final MenuItem notify = menu != null ? menu.findItem(R.id.act_notify) : null;
            if(notify != null && notify.isEnabled()){
              boolean isFragSelected = getTopFragment() != null && getTopFragment().getMenuId(MainActivity.this) == notify.getItemId();
              if(notify.getActionView() != null && notify.getActionView().findViewById(R.id.imgMenuLogo) != null){
                ((ImageView) notify.getActionView().findViewById(R.id.imgMenuLogo)).setImageResource(AppDatabase.getNotificationDao(MainActivity.this).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "") ? R.drawable.ic_act_notify_dot : R.drawable.ic_act_notify);
                ((ImageView) notify.getActionView().findViewById(R.id.imgMenuLogo)).setColorFilter(isFragSelected ? getColorPrimaryDarkFromTheme() : getResources().getColor(R.color.transparent), PorterDuff.Mode.SRC_ATOP);
              }
              else
                notify.setIcon(AppDatabase.getNotificationDao(MainActivity.this).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "") ? isFragSelected ? R.drawable.ic_act_notify_dot_sel : R.drawable.ic_act_notify_dot : isFragSelected ? R.drawable.ic_act_notify_sel : R.drawable.ic_act_notify);
            }
            
            //}
          }
        });
        
        AppDatabase.getNotificationDao(this).getUnreadNotificationCount(isSetUserMgmt ? SharedPrefManager.getUserID() : "").observe(this, new Observer<Integer>(){
          @Override
          public void onChanged(Integer size){
            if(size == 0) stopBlink();
          }
        });
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    
    //final DrawerLayout drawer = binding.drawerLayout;
    final NavigationView navigationView = binding.navView;
    
    if(navigationView.getHeaderView(0) != null){
      NavHeaderMainBinding navHeaderMainBinding = NavHeaderMainBinding.bind(navigationView.getHeaderView(0));
      loadImage(navHeaderMainBinding.imgNavUser, SharedPrefManager.getUserProfileUrl(), R.drawable.ic_temp_person);
      navHeaderMainBinding.txtNavStoreName.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_store_name), chkNull(SharedPrefManager.getStoreName(), "")), HtmlCompat.FROM_HTML_MODE_LEGACY));
      navHeaderMainBinding.txtNavStoreName.setVisibility(isSetUserMgmt && isNonEmpty(SharedPrefManager.getStoreName()) ? View.VISIBLE : View.GONE);
      navHeaderMainBinding.txtNavUserName.setText(HtmlCompat.fromHtml(chkNull(SharedPrefManager.getUserName(), ""), HtmlCompat.FROM_HTML_MODE_LEGACY));
      navHeaderMainBinding.txtNavUserName.setVisibility(isNonEmpty(SharedPrefManager.getUserName()) ? View.VISIBLE : View.GONE);
      navHeaderMainBinding.txtNavStoreId.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_store_id), chkNull(SharedPrefManager.getStoreID(), "0")), HtmlCompat.FROM_HTML_MODE_LEGACY));
      navHeaderMainBinding.txtNavStoreId.setVisibility(isSetUserMgmt && isNonEmpty(SharedPrefManager.getStoreID()) ? View.VISIBLE : View.GONE);
      loadImage(navHeaderMainBinding.imgNavUser, SharedPrefManager.getUserProfileUrl(), R.drawable.ic_temp_person);
    }
    //setupNavMenus();
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    //mAppBarConfiguration = new AppBarConfiguration.Builder(allFragments).setDrawerLayout(drawer).build();
    
    //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    //NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    //NavigationUI.setupWithNavController(navigationView, navController);
    
    //Code to set Drawer without Navigation Controller (Does not crash on Activity Restart)
    toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.appBarMain.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    binding.drawerLayout.addDrawerListener(toggle);
    toggle.syncState();
    toggle.setHomeAsUpIndicator(R.drawable.ic_nav_menu_locked);
    toggle.setToolbarNavigationClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        onSupportNavigateUp();
      }
    });
    
    navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item){
        final int menuId = item.getItemId();
        final String menuCode = getResources().getResourceEntryName(menuId).toUpperCase();
        final MenuModel menuModel = AppDatabase.getMenuDao(MainActivity.this).getMenuByCode(menuCode);
        final CommonFragment currentFrag = getTopFragment();
        boolean isFragSelected = currentFrag != null && currentFrag.getMenuId(MainActivity.this) == menuId;
        if(!item.isEnabled()){
          showShortToast(menuModel != null && isNonEmpty(menuModel.getErrEnabledMsg()) ? menuModel.getErrEnabledMsg(MainActivity.this) : getString(R.string.feature_not_available));
          errorBeep();
        }
        else{
          if(item.getItemId() > 0){
            clearBackStack();
            clearMenuSelection();
            item.setChecked(true);
          }
          //setup handle Redirection here (fully dynamic menu)
          switch(item.getItemId()){
            case R.id.nav_home:
              //loadFragment(new HomeFragment());
              break;
            case R.id.nav_app_info:
              //temp solution
              loadFragment(new AppInfoFragment());
              break;
            case R.id.nav_log_info:
              LogFileUtilityHHD.zipLogsAndShare(MainActivity.this);
              break;
            case R.id.nav_change_pass:
              //temp solution
              loadFragment(new ResetPasswordFragment());
              break;
            default:
              if(!isFragSelected && currentFrag != null){
                //setUpHome();
                Bundle args = new Bundle();
                if(currentFrag != null && currentFrag instanceof MovementMainFragment)
                  args.putString(AppConstants.REPLENISHMENT_TYPE, ((MovementMainFragment) currentFrag).getReplenishmentType());
                if(currentFrag != null && currentFrag instanceof OmniChannelFragment)
                  args.putString(AppConstants.OMNICHANNEL_TYPE, ((OmniChannelFragment) currentFrag).getOmnichannelType());
                args.putInt(AppConstants.MENU_ICON_ID, menuId);
                handleFragmentRedirection(menuModel, args);
              }
              break;
          }
          //NavigationUI.onNavDestinationSelected(item, navController);
          //This is for closing the drawer after acting on it
        }
        
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return false;
      }
    });
    
    hideNavItems(true);
    
    if(savedInstanceState == null) loadFragment(new HomeFragment());
  }
  
  void clearMenuSelection(){
    for(int i = 0; i < binding.navView.getMenu().size(); i++)
      binding.navView.getMenu().getItem(i).setChecked(false);
  }
  
  /**
   * Is reader present boolean.
   *
   * @return the boolean
   */
  public boolean isReaderPresent(){
    return mainViewModel != null && mainViewModel.isReaderPresent(mReader != null);
  }
  
  /**
   * Is reader connected boolean.
   *
   * @return the boolean
   */
  public boolean isReaderConnected(){
    return mainViewModel != null && mainViewModel.isReaderConnected();
  }
  
  /**
   * Check and connect reader.
   */
  public void checkAndConnectReader(){
    if(mainViewModel != null) mainViewModel.checkAndConnectReader();
  }
  
  /**
   * Check and set reader.
   */
  public void checkAndSetReader(){
    if(mainViewModel != null) mainViewModel.checkAndSetReader();
  }
  
  /**
   * Lock drawer.
   *
   * @param isLockDrawer the is lock drawer
   */
  public void lockDrawer(final boolean isLockDrawer){
    isMenusLocked = isLockDrawer;
    updateOptionMenuIcons(isMenusLocked);
    if(isLockDrawer && binding.drawerLayout.isOpen()) binding.drawerLayout.close();
    binding.drawerLayout.setDrawerLockMode(isLockDrawer ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
    if(toggle != null) toggle.setDrawerIndicatorEnabled(!isLockDrawer);
  }
  
  /**
   * Set up home.
   */
  public void setUpHome(){ setUpHome(true); }
  
  /**
   * Set up home.
   *
   * @param isPopBackStack the is pop back stack
   */
  public void setUpHome(boolean isPopBackStack){
    if(isPopBackStack){
      //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
      //navController.popBackStack(R.id.nav_home, false);
      
      //Code to set Drawer without Navigation Controller (Does not crash on Activity Restart)
      clearBackStack();
      //loadFragment(new HomeFragment());
    }
    clearMenuSelection();
    binding.navView.getMenu().getItem(0).setChecked(true);
  }
  
  /*private void setupNavMenus(){
    final Menu navMenu = binding.navView.getMenu();
    final MenuDao menuDao = AppDatabase.getDbInstance(MainActivity.this).MenuDao();
    List<MenuModel> listNavMenus = menuDao.getNavMenus();
    if(isNonEmpty(listNavMenus)){
      AppCommonMethods.showLog("NavMenus", "" + listNavMenus.size());
      MenuItem home = navMenu.findItem(R.id.nav_home);
      navMenu.clear();
      int orderIndex = 0;
      final MenuModel navHome = menuDao.getMenuByCode(AppConstants.MENU_CODE_NAV_HOME);
      if(navHome == null || !listNavMenus.contains(navHome)){
        navMenu.add(1, R.id.nav_home, ++orderIndex, navHome != null ? navHome.getMenuName() : home.getTitle());
        final MenuItem menuItemHome = navMenu.findItem(R.id.nav_home);
        if(menuItemHome != null){
          menuItemHome.setEnabled(true);
          menuItemHome.setTitle(home.getTitle());
          menuItemHome.setTitleCondensed(home.getTitleCondensed());
          menuItemHome.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
          menuItemHome.setIcon(home.getIcon());
        }
      }
      for(MenuModel menuModel : listNavMenus){
        final int menuId = menuModel.getActionMenuId(this);
        navMenu.add(1, menuId, ++orderIndex, menuModel.getMenuName());
        final MenuItem menuItem = navMenu.findItem(menuId);
        if(menuItem != null){
          menuItem.setEnabled(menuModel.isEnabled);
          menuItem.setTitle(menuModel.getMenuName());
          menuItem.setTitleCondensed(menuModel.getMenuName());
          menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
          if(isNonEmpty(menuModel.getImageUrl())){
            Glide.with(this).asDrawable().load(menuModel.imageUrl).fitCenter().listener(new RequestListener<Drawable>(){
              @Override
              public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource){
                menuItem.setIcon(chkZero(menuModel.getIconId(), getResources().getIdentifier(menuModel.getMenuIconName(), AppConstants.RES_DRAWABLE, getPackageName())));
                return false;
              }
              
              @Override
              public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource){
                menuItem.setIcon(resource);
                return false;
              }
            });
          }
          else if(isNullOrEmpty(menuModel.getImageUrl())){
            menuItem.setIcon(chkZero(menuModel.getIconId(), getResources().getIdentifier(menuModel.getMenuIconName(), AppConstants.RES_DRAWABLE, getPackageName())));
          }
          *//*menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item){
              if(!menuModel.getIsEnabled()){
                showShortToast(menuModel.getErrEnabledMsg(MainActivity.this));
                errorBeep();
              }
              else{
                final CommonFragment currentFrag = getTopFragment();
                boolean isFragSelected = currentFrag != null && currentFrag.getMenuId(MainActivity.this) == menuItem.getItemId();
                //do noting if Fragment already in stack
                if(!isFragSelected){
                  if(menuItem.getItemId() > 0){
                    clearBackStack();
                    clearMenuSelection();
                    menuItem.setChecked(true);
                  }
                  //detach all other menu fragments
                  detachAllActionMenuFragments();
                  //setUpHome();
                  Bundle args = new Bundle();
                  if(currentFrag != null && currentFrag instanceof MovementMainFragment)
                    args.putString(AppConstants.REPLENISHMENT_TYPE, ((MovementMainFragment) currentFrag).getReplenishmentType());
                  if(currentFrag != null && currentFrag instanceof OmniChannelFragment)
                    args.putString(AppConstants.OMNICHANNEL_TYPE, ((OmniChannelFragment) currentFrag).getOmnichannelType());
                  args.putInt(AppConstants.MENU_ICON_ID, menuId);
                  handleFragmentRedirection(menuModel, args);
                  binding.drawerLayout.closeDrawer(GravityCompat.START);
                }
              }
              return false;
            }
          });*//*
        }
      }
    }
  }*/
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu){
    //set Top/Action/Overflow/Options Menu Dynamically
    int orderIndex = 0;
    List<MenuModel> listActionMenus = AppDatabase.getDbInstance(MainActivity.this).MenuDao().getActionMenus();
    if(isNonEmpty(listActionMenus)){
      final int size = listActionMenus.size();
      for(MenuModel menuModel : listActionMenus){
        final int menuId = menuModel.getActionMenuId(this);
        menu.add(100 + (orderIndex > 3 ? orderIndex : 0), menuId, ++orderIndex, menuModel.getMenuName());
        final MenuItem menuItem = menu.findItem(menuId);
        if(menuItem != null){
          menuItem.setEnabled(menuModel.isEnabled);
          menuItem.setTitle(menuModel.getMenuName());
          menuItem.setTitleCondensed(menuModel.getMenuName());
          menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
          final boolean isNotifyMenu = menuId == R.id.action_menu_notify;
          final View root = getLayoutInflater().inflate(R.layout.view_menu, null, false);
          
          final int padding = getResources().getDimensionPixelOffset(R.dimen.dp_10);
          int actionBarHeight = getResources().getDimensionPixelOffset(R.dimen.dp_80);
          TypedValue tv = new TypedValue();
          if(getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
          
          root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, actionBarHeight));
          root.setPadding(padding, 0, padding, 0);
          final ImageView imgSlash = root.findViewById(R.id.imgMenuSlash);
          
          imgSlash.setVisibility(!menuModel.getIsEnabled() ? View.VISIBLE : View.GONE);
          
          final ImageView imgLogo = root.findViewById(R.id.imgMenuLogo);
          
          root.setTag(menuModel.getMenuCode());
          TooltipCompat.setTooltipText(root, menuModel.getMenuName());
          loadImage(imgLogo, menuModel.imageUrl, isNotifyMenu && AppDatabase.getNotificationDao(this).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "") ? R.drawable.ic_act_notify_dot : chkZero(menuModel.getIconId(), getResources().getIdentifier(menuModel.getMenuIconName(), AppConstants.RES_DRAWABLE, getPackageName())));
          if(isNullOrEmpty(menuModel.getImageUrl()))
            imgLogo.setColorFilter(getResources().getColor(!menuModel.getIsEnabled() ? R.color.colorDisabled : R.color.transparent), PorterDuff.Mode.SRC_ATOP);
          root.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
              if(!menuModel.getIsEnabled()){
                showShortToast(menuModel.getErrEnabledMsg(MainActivity.this));
                errorBeep();
              }
              else if(isMenusLocked){
                AppCommonMethods.showShortToast(MainActivity.this, R.string.not_allowed);
                errorBeep();
              }
              else{
                final CommonFragment currentFrag = getTopFragment();
                boolean isFragSelected = currentFrag != null && currentFrag.getMenuId(MainActivity.this) == menuItem.getItemId();
                //do noting if Fragment already in stack
                if(isNotifyMenu) stopBlink();
                if(!isFragSelected){
                  //detach all other menu fragments
                  detachAllActionMenuFragments();
                  setUpHome();
                  Bundle args = new Bundle();
                  if(currentFrag != null && currentFrag instanceof MovementMainFragment)
                    args.putString(AppConstants.REPLENISHMENT_TYPE, ((MovementMainFragment) currentFrag).getReplenishmentType());
                  if(currentFrag != null && currentFrag instanceof OmniChannelFragment)
                    args.putString(AppConstants.OMNICHANNEL_TYPE, ((OmniChannelFragment) currentFrag).getOmnichannelType());
                  args.putInt(AppConstants.MENU_ICON_ID, menuId);
                  handleFragmentRedirection(menuModel, args);
                }
              }
            }
          });
          menuItem.setActionView(root);
        }
      }
      //add Logout menu
      if(menu.findItem(R.id.act_logout) == null){
        menu.add(100 + (orderIndex > 3 ? orderIndex : 0), R.id.act_logout, ++orderIndex, R.string.action_logout);
        final MenuItem menuItem = menu.findItem(R.id.act_logout);
        if(menuItem != null){
          menuItem.setEnabled(true);
          menuItem.setTitle(R.string.action_logout);
          menuItem.setTitleCondensed(getString(R.string.action_logout));
          menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            menuItem.setTooltipText(getString(R.string.action_logout));
          menuItem.setIcon(R.drawable.ic_act_logout);
        }
      }
    }
    else{
      //this adds menus to the action bar by inflating the menu file in resource.
      getMenuInflater().inflate(R.menu.main, menu);
    }
    
    final MenuItem notify = menu.findItem(R.id.act_notify);
    if(notify != null && notify.isEnabled()){
      boolean isFragSelected = getTopFragment() != null && getTopFragment().getMenuId(MainActivity.this) == notify.getItemId();
      if(notify.getActionView() != null && notify.getActionView().findViewById(R.id.imgMenuLogo) != null){
        ((ImageView) notify.getActionView().findViewById(R.id.imgMenuLogo)).setImageResource(AppDatabase.getNotificationDao(this).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "") ? R.drawable.ic_act_notify_dot : R.drawable.ic_act_notify);
        ((ImageView) notify.getActionView().findViewById(R.id.imgMenuLogo)).setColorFilter(isFragSelected ? getColorPrimaryDarkFromTheme() : getResources().getColor(R.color.transparent), PorterDuff.Mode.SRC_ATOP);
      }
      else
        notify.setIcon(AppDatabase.getNotificationDao(this).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "") ? isFragSelected ? R.drawable.ic_act_notify_dot_sel : R.drawable.ic_act_notify_dot : isFragSelected ? R.drawable.ic_act_notify_sel : R.drawable.ic_act_notify);
    }
    this.menu = menu;
    return true;
  }
  
  private void updateOptionMenuIcons(final boolean isMenusLocked){
    try{
      for(int i = 0; i < menu.size(); i++){
        final MenuItem menuItem = menu.getItem(i);
        if(menuItem != null && menuItem.isEnabled()){
          if(menuItem.getActionView() != null){
            final ImageView imgMenuLogo = menuItem.getActionView().findViewById(R.id.imgMenuLogo);
            //final ImageView imgSlash = menuItem.getActionView().findViewById(R.id.imgMenuSlash);
            if(imgMenuLogo != null)
              imgMenuLogo.setColorFilter(getResources().getColor(isMenusLocked ? R.color.colorDisabled : R.color.icon_grey), PorterDuff.Mode.SRC_ATOP);
            //if(imgSlash != null) imgSlash.setVisibility(isMenusLocked ? View.VISIBLE : View.GONE);
          }
          else if(menuItem.getItemId() == R.id.act_logout)
            menuItem.setIcon(isMenusLocked ? R.drawable.ic_act_logout_locked : R.drawable.ic_act_logout);
        }
      }
    }
    catch(Exception e){ }
  }
  
  /**
   * Stop blink.
   */
  public void stopBlink(){
    runOnUiThread(new Runnable(){
      @Override
      public void run(){
        if(notifyBlinkTimer != null) notifyBlinkTimer.cancel();
        AppCommonMethods.stopBeep();
        final MenuItem notify = menu != null ? menu.findItem(R.id.act_notify) : null;
        if(notify != null && notify.isEnabled()){
          boolean isFragSelected = getTopFragment() != null && getTopFragment().getMenuId(MainActivity.this) == notify.getItemId();
          if(notify.getActionView() != null && notify.getActionView().findViewById(R.id.imgMenuLogo) != null){
            ((ImageView) notify.getActionView().findViewById(R.id.imgMenuLogo)).setImageResource(AppDatabase.getNotificationDao(MainActivity.this).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "") ? R.drawable.ic_act_notify_dot : R.drawable.ic_act_notify);
            ((ImageView) notify.getActionView().findViewById(R.id.imgMenuLogo)).setColorFilter(isFragSelected ? getColorPrimaryDarkFromTheme() : getResources().getColor(R.color.transparent), PorterDuff.Mode.SRC_ATOP);
          }
          else
            notify.setIcon(AppDatabase.getNotificationDao(MainActivity.this).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "") ? isFragSelected ? R.drawable.ic_act_notify_dot_sel : R.drawable.ic_act_notify_dot : isFragSelected ? R.drawable.ic_act_notify_sel : R.drawable.ic_act_notify);
        }
        
      }
    });
  }
  
  /**
   * Blink.
   */
  public void blink(){//take boolean to start and stop
    runOnUiThread(new Runnable(){
      @Override
      public void run(){
        final MenuItem notify = menu != null ? menu.findItem(R.id.act_notify) : null;
        if(notify != null && notify.isEnabled()){
          stopBlink();
          notifyBlinkTimer.start();
          AppCommonMethods.beepNotification();
        }
      }
    });
  }
  
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item){
    showLog("isMenusLocked", "" + isMenusLocked);
    if(isMenusLocked){
      AppCommonMethods.showShortToast(this, R.string.not_allowed);
      errorBeep();
      return false;
    }
    if(item != null && !item.isEnabled()){
      showShortToast(R.string.feature_not_available);
      errorBeep();
      return false;
    }
    
    Bundle args = new Bundle();
    args.putInt(AppConstants.MENU_ICON_ID, item.getItemId());
    switch(item.getItemId()){
      case R.id.act_ser:
        setUpHome();
        detachAllActionMenuFragments();
        loadFragment(new ActionMenuSearchFragment(), args);
        item.setIcon(R.drawable.ic_act_ser_sel);
        
        return true;
      case R.id.act_msg:
        setUpHome();
        detachAllActionMenuFragments();
        loadFragment(new ActionMenuMsgFragment(), args);
        item.setIcon(R.drawable.ic_act_msg_sel);
        
        return true;
      case R.id.act_notify:
        
        setUpHome();
        stopBlink();
        detachAllActionMenuFragments();
        loadFragment(new ActionMenuNotifyFragment(), args);
        item.setIcon(AppDatabase.getNotificationDao(this).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "") ? R.drawable.ic_act_notify_dot_sel : R.drawable.ic_act_notify_sel);
        
        return true;
      case R.id.act_logout:
        
        showCustomAlertDialog("", R.string.msg_logout, R.string.btn_logout, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialog, int which){
            if(AppCommonMethods.isCallLogoutAPI && isInternetConnected(MainActivity.this, false, false)){
              callWebService(URLConstants.LOGOUT, new JSONObject(), getString(R.string.progress_msg_logout), true);
            }
            else{
              clearSavedDataOnLogout();
            }
            item.setIcon(R.drawable.ic_act_logout);
            
          }
        }, R.string.btn_cancel, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialog, int which){
            item.setIcon(R.drawable.ic_act_logout);
            
          }
        });
        item.setIcon(R.drawable.ic_act_logout_sel);
        return true;
      default:
        final int menuId = item.getItemId();
        final String menuCode = getResources().getResourceEntryName(menuId).toUpperCase();
        final MenuModel menuModel = AppDatabase.getMenuDao(this).getMenuByCode(menuCode);
        final boolean isNotifyMenu = item.getItemId() == R.id.action_menu_notify;
        final CommonFragment currentFrag = getTopFragment();
        boolean isFragSelected = currentFrag != null && currentFrag.getMenuId(MainActivity.this) == menuId;
        if(isNotifyMenu) stopBlink();
        
        //do noting if Fragment already in stack
        if(!isFragSelected && menuModel != null){
          //detach all other menu fragments
          detachAllActionMenuFragments();
          setUpHome();
          
          if(currentFrag != null && currentFrag instanceof MovementMainFragment)
            args.putString(AppConstants.REPLENISHMENT_TYPE, ((MovementMainFragment) currentFrag).getReplenishmentType());
          args.putInt(AppConstants.MENU_ICON_ID, menuId);
          handleFragmentRedirection(menuModel, args);
          return true;
        }
        return super.onOptionsItemSelected(item);
    }
  }
  
  /**
   * Handle fragment redirection.
   *
   * @param fragment the fragment
   */
  public void handleFragmentRedirection(Fragment fragment){
    handleFragmentRedirection(fragment, null, null); }
  
  /**
   * Handle fragment redirection.
   *
   * @param fragment the fragment
   * @param args     the args
   */
  public void handleFragmentRedirection(Fragment fragment, Bundle args){ handleFragmentRedirection(fragment, null, args); }
  
  /**
   * Handle fragment redirection.
   *
   * @param fragment  the fragment
   * @param menuModel the menu model
   */
  public void handleFragmentRedirection(Fragment fragment, MenuModel menuModel){ handleFragmentRedirection(fragment, menuModel, null); }
  
  /**
   * Handle fragment redirection.
   *
   * @param menuModel the menu model
   */
  public void handleFragmentRedirection(MenuModel menuModel){ handleFragmentRedirection(null, menuModel, null); }
  
  /**
   * Handle fragment redirection.
   *
   * @param menuModel the menu model
   * @param args      the args
   */
  public void handleFragmentRedirection(MenuModel menuModel, Bundle args){ handleFragmentRedirection(null, menuModel, args); }
  
  /**
   * Handle fragment redirection.
   *
   * @param fragment  the fragment
   * @param menuModel the menu model
   * @param args      the args
   */
  public void handleFragmentRedirection(Fragment fragment, MenuModel menuModel, Bundle args){
    args = args != null ? args : new Bundle();
    if(menuModel != null){
      args.putSerializable(menuModel.getClass().getSimpleName(), menuModel);
    }
    if(fragment == null){
      final Class redirectFragment = menuModel.getRedirectFragment();
      if(redirectFragment != null && CommonFragment.class.isAssignableFrom(redirectFragment)){
        try{
          fragment = (CommonFragment) menuModel.getRedirectFragment().newInstance();
        }
        catch(IllegalAccessException e){
          e.printStackTrace();
        }
        catch(Fragment.InstantiationException e){
          e.printStackTrace();
        }
        catch(java.lang.InstantiationException e){ e.printStackTrace(); }
      }
      else if(redirectFragment != null && DialogFragment.class.isAssignableFrom(redirectFragment)){
        try{
          fragment = (DialogFragment) menuModel.getRedirectFragment().newInstance();
        }
        catch(IllegalAccessException e){
          e.printStackTrace();
        }
        catch(Fragment.InstantiationException e){
          e.printStackTrace();
        }
        catch(java.lang.InstantiationException e){ e.printStackTrace(); }
      }
    }
    //TODO handle 'accessPin' parameter
    if(fragment != null){
      args.putString("FragmentClass", fragment.getClass().getSimpleName());
      args.putString("FragmentRedirectionClass", fragment.getClass().getName());
      if(fragment instanceof EncodingAchieveFragment){
        callWebService(URLConstants.GET_ENCODING_ACHIEVEMENTS, new JSONObject(), args, getString(R.string.progress_msg_getting_data));
      }
      else if(fragment instanceof EncodingStartFragment){
        if(SharedPrefManager.getBoolean(ParamConstants.IS_OFFLINE_ENCODE))
          loadSessionFragment(AppCommonMethods.SessionType.ENCODING.name(), -2, -1, -1, args);
        else
          getActiveUsers(AppCommonMethods.SessionType.ENCODING, AppConstants.SESSION_ACTION_RESUME, args);
      }
      else if(fragment instanceof EncodingScanScanWriteFragment){
        getActiveUsers(AppCommonMethods.SessionType.ENCODING, AppConstants.SESSION_ACTION_RESUME, args);
      }
      else if(fragment instanceof DecodingStartFragment){
        if(args == null) args = new Bundle();
        args.putBoolean(ParamConstants.IS_ALLOW_DECODE, true);
        callWebService(URLConstants.GET_ACCESS_PWD, new JSONObject(), args, getString(R.string.progress_msg_getting_data));
        //        if(isShowDecodeTypeSelectionDialog && (isNullOrEmpty(SharedPrefManager.getArrayList(ParamConstants.DECODE_TYPES)) || !SharedPrefManager.getBoolean(ParamConstants.DECODE_TYPES+"_"+ParamConstants.IS_SAVED_FROM_LOGIN,false))){
        //          if(isDebugApp) handleResponse(URLConstants.GET_DECODE_TYPES,new JSONObject(),getSampleJSON(this,URLConstants.GET_DECODE_TYPES),200,true,args);
        //          else callWebService(URLConstants.GET_DECODE_TYPES, new JSONObject(), args, getString(R.string.progress_msg_getting_data),isNonEmpty(SharedPrefManager.getArrayList(ParamConstants.DECODE_TYPES)));
        //        }
      }
      else if(fragment instanceof InventoryStartFragment){
        if(SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_ALL_ZONE_INVENTORY_FOR_TAKE_STOCK, AppCommonMethods.isAllowAllZoneInventoryForTakeStock) || !isCheckExistingZoneDataForInv || hasZonesData(args)) getActiveUsers(AppCommonMethods.SessionType.INVENTORY, AppConstants.SESSION_ACTION_RESUME, args);
      }
      else if(fragment instanceof InventoryAddFragment){
        if(!isCheckExistingZoneDataForInv || hasZonesData(args)) getActiveUsers(AppCommonMethods.SessionType.ADD_INVENTORY, AppConstants.SESSION_ACTION_RESUME, args);
      }
      else if(fragment instanceof InventoryFilterFragment){
        if(!isCheckExistingZoneDataForInv || hasZonesData(args)) getActiveUsers(AppCommonMethods.SessionType.FILTER_INVENTORY, AppConstants.SESSION_ACTION_RESUME, args);
          /*if(args == null){
            args = new Bundle();
          }
          args.putSerializable(MenuModel.class.getSimpleName(), AppDatabase.getMenuDao(this).getMenuByCode(AppConstants.MENU_CODE_INV_BRAND));
          args.putInt(AppConstants.ACTIVE_USERS, activeUsers);
          args.putInt(AppConstants.SESSION_VALID_TILL, sessionValidTill > 0 ? sessionValidTill : 48);*/
          //checkReaderConnection(new InventoryFilterFragment(), args);
      }
      else if(fragment instanceof InventoryBrandFragment){
        final AppDatabase db = AppDatabase.getDbInstance(MainActivity.this);
        if(!db.BrandWiseZoneInventoryDao().hasData() && !db.BrandDao().hasData() && !isInternetConnected(this, false, true)){
          //Don't Allow
        }
        else{
          if(SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_ALL_ZONE_INVENTORY_FOR_TAKE_STOCK, AppCommonMethods.isAllowAllZoneInventoryForTakeStock) || !isCheckExistingZoneDataForInv ||  hasZonesData(args)){
            final boolean hasBrandsData = db.BrandDao().hasData();
            if((hasBrandsData && !isInternetConnected(this, false, false)) || db.RFIDSessionDao().hasCurrentSession(AppCommonMethods.SessionType.BRAND_INVENTORY.getValue()))
              getActiveUsers(AppCommonMethods.SessionType.BRAND_INVENTORY, AppConstants.SESSION_ACTION_RESUME, args);
            else{
              try{
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put(ParamConstants.SESSION_TYPE, AppCommonMethods.SessionType.BRAND_INVENTORY.name());
                jsonRequest.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_RESUME);
                callWebService(URLConstants.GET_BRANDS, jsonRequest, args, getString(R.string.progress_msg_getting_data), hasBrandsData, true);
              }
              catch(Exception e){ e.printStackTrace(); }
            }
          }
        }
      }
      else if(fragment instanceof StockCorrectionMainFragment){
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.STOCK_CORRECTION);
        AppCommonMethods.logInFile(MainActivity.this, "\n-------------------------STOCK-CORRECTION---------------------------\n");
        callWebService(URLConstants.GET_STOCK_CORRECTION_DASHBOARD, new JSONObject(), args, getString(R.string.progress_msg_getting_data), false, true);
      }
      else if(fragment instanceof StockCorrectionStartFragment){
        checkReaderConnection((CommonFragment) fragment, args);
      }
      else if(fragment instanceof ProductSearchDetailsFragment)
        checkReaderConnection((CommonFragment) fragment, args);
      else if(fragment instanceof SearchListFragment){
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.SEARCH_LIST);
        callWebService(URLConstants.GET_PICK_LIST, new JSONObject(), args, getString(R.string.progress_msg_getting_data), false, true);
      }
      else if(fragment instanceof SearchListStartFragment)
        checkReaderConnection((CommonFragment) fragment, args);
      else if(fragment instanceof SearchAssortMainFragment){
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.SEARCH_ASSORTMENT);
        callWebService(URLConstants.GET_ASSORTMENT_LIST, new JSONObject(), args, getString(R.string.progress_msg_getting_data), false, true);
      }
      else if(fragment instanceof SearchListExcelFragment){
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.SER_EXCEL);
        AppDatabase.getProductDao(this).resetRssiPercentage(AppCommonMethods.SessionType.SER_EXCEL.getValue());
        //TODO handle existing data + related confirmation dialogs
        /*if(isDebugApp)
          handleResponse(URLConstants.GET_EXCEL_SEARCH_LIST, new JSONObject(), getSampleJSON(this, URLConstants.GET_EXCEL_SEARCH_LIST), 200, true, args);
        else*/
          callWebService(URLConstants.GET_EXCEL_SEARCH_LIST, new JSONObject(), args, getString(R.string.progress_msg_getting_data), false, true);
      }
      else if(fragment instanceof SearchListExcelStartFragment)
        checkReaderConnection((CommonFragment) fragment, args);
      else if(fragment instanceof SearchUnencodedFragment){
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.SEARCH_UNENCODED);
        checkReaderConnection((CommonFragment) fragment, args);
      }
      else if(fragment instanceof SearchFileBasedFragment){
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.SEARCH_FILE);
        checkReaderConnection((CommonFragment) fragment, args);
      }
      else if(fragment instanceof SearchAlienFragment){
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.SEARCH_ALIEN);
        checkReaderConnection((CommonFragment) fragment, args);
      }
      else if(fragment instanceof SearchFIFOStartFragment){
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.SEARCH_FIFO);
        checkReaderConnection((CommonFragment) fragment, args);
      }
      else if(fragment instanceof OmniChannelStartFragment)
        checkReaderConnection((CommonFragment) fragment, args);
      else if(fragment instanceof InwardMainFragment){
        if(AppCommonMethods.isSetInwOnline){
          //Webservice call
          loadFragment(new InwardGrnHuScanFragment(), args);
        }
        else{
          final TripStatusDao tripStatusDao = AppDatabase.getDbInstance(this).TripStatusDao();
          if(tripStatusDao.hasTripData(AppConstants.INWARD)){
            if(tripStatusDao.isAllTripsPending(AppConstants.INWARD) && isInternetConnected(this, false, false)){
              final Bundle arg = args;
              showCustomAlertDialog("_", getString(R.string.msg_inward_pending_trips), R.string.btn_continue, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                  loadFragment(new InwardMainFragment(), arg);
                }
              }, R.string.btn_sync, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                  showCustomAlertDialog("", getString(R.string.msg_confirm_sync), R.string.btn_continue, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                      try{
                        JSONObject requestParams = new JSONObject();
                        requestParams.put(ParamConstants.TYPE, AppConstants.INWARD);
                        callWebService(URLConstants.GET_INWARD_TRIP_DATA, requestParams, arg, getString(R.string.progress_msg_getting_data), false, true);
                      }
                      catch(Exception e){ e.printStackTrace(); }
                    }
                  }, R.string.btn_cancel, null);
                  
                }
              });
            }
            else loadFragment(new InwardMainFragment(), args);
          }
          else{
            try{
              JSONObject requestParams = new JSONObject();
              requestParams.put(ParamConstants.TYPE, AppConstants.INWARD);
              callWebService(URLConstants.GET_INWARD_TRIP_DATA, requestParams, args, getString(R.string.progress_msg_getting_data), false, true);
            }
            catch(Exception e){ e.printStackTrace(); }
          }
        }
      }
      else if(fragment instanceof InwardGrnStartFragment)
        checkReaderConnection((CommonFragment) fragment, args);
      else if(fragment instanceof MovementMainFragment){
        final MenuDao menuDao = AppDatabase.getMenuDao(this);
        final List<MenuModel> movSubMenuCodes = menuDao.getSubMenus(menuModel.menuId);
        if(movSubMenuCodes.size() == 1 && movSubMenuCodes.get(0).getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_MOV_START)){
          getRfidViewModel().deleteSession(AppCommonMethods.SessionType.MOVEMENT);
          handleFragmentRedirection(new MovementStartFragment(), args);
        }
        else loadFragment(fragment, args);
      }
      else if(fragment instanceof MovementStartFragment){
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.MOVEMENT);
        if(hasZonesData(args)) checkReaderConnection((CommonFragment) fragment, args);
      }
      else if(fragment instanceof ReplenishmentListFragment){
        if(hasZonesData(args)) loadFragment(fragment, args);
      }
      else if(fragment instanceof ReplenishmentStartFragment){
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.REPLENISHMENT);
        checkReaderConnection((CommonFragment) fragment, args);
      }
      else if(fragment instanceof OutwardHuDataFragment){
        
        final TripStatusDao tripStatusDao = AppDatabase.getDbInstance(this).TripStatusDao();
        if(tripStatusDao.hasTripData(AppConstants.OUTWARD)){
          if(tripStatusDao.isAllTripsPending(AppConstants.OUTWARD) && isInternetConnected(this, false, false)){
            final Bundle arg = args;
            showCustomAlertDialog("_", getString(R.string.msg_inward_pending_trips), R.string.btn_continue, new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialog, int which){
                loadFragment(new OutwardHuDataFragment(), arg);
              }
            }, R.string.btn_sync, new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialog, int which){
                showCustomAlertDialog("", getString(R.string.msg_confirm_sync), R.string.btn_continue, new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialog, int which){
                    try{
                      JSONObject requestParams = new JSONObject();
                      requestParams.put(ParamConstants.TYPE, AppConstants.OUTWARD);
                      callWebService(URLConstants.GET_OUTWARD_TRIP_DATA, requestParams, arg, getString(R.string.progress_msg_getting_data), false, true);
                    }
                    catch(Exception e){ e.printStackTrace(); }
                  }
                }, R.string.btn_cancel, null);
                
              }
            });
          }
          else loadFragment(new OutwardHuDataFragment(), args);
        }
        else{
          try{
            JSONObject requestParams = new JSONObject();
            requestParams.put(ParamConstants.TYPE, AppConstants.OUTWARD);
            callWebService(URLConstants.GET_OUTWARD_TRIP_DATA, requestParams, args, getString(R.string.progress_msg_getting_data), false, true);
          }
          catch(Exception e){ e.printStackTrace(); }
        }
        
      }
      //else if(fragment instanceof OutwardMainFragment){//TODO getData via API then Redirect}
      else if(fragment instanceof OutwardPickStartFragment){
        checkReaderConnection((CommonFragment) fragment, args);
      }
      else if(fragment instanceof InwardToteMainFragment){
        args.putBoolean(ParamConstants.IS_EMPTY_TOTE_OUTWARD, menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_OTW_TOTE_MT));
        args.putBoolean(ParamConstants.IS_EMPTY_TOTE_INWARD, menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_INW_TOTE));
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.INWARD_TOTE);
        final String challanNo = SharedPrefManager.getString(ParamConstants.CHALLAN_NO, "");
        if(isNonEmpty(challanNo) && isNonEmpty(SharedPrefManager.getStringArrayList(challanNo)))
          args.putString(ParamConstants.CHALLAN_NO, challanNo);
        loadFragment((CommonFragment) fragment, args);
      }
      else if(fragment instanceof OutwardToteMainFragment){
        args.putBoolean(ParamConstants.IS_EMPTY_TOTE_OUTWARD, menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_OTW_TOTE));
        args.putBoolean(ParamConstants.IS_OFF_RANGE, menuModel.getMenuCode().matches("(?i)(" + AppConstants.MENU_CODE_OTW_OFF_RANGE + "|" + AppConstants.MENU_CODE_OFF_RANGE + ")"));
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.OUTWARD_TOTE);
        loadFragment((CommonFragment) fragment, args);
      }
      else if(fragment instanceof OutwardToteDCFragment){
        loadFragment((CommonFragment) fragment, args);
      }
      else if(fragment instanceof OffRangeMainFragment){
        args.putBoolean(ParamConstants.IS_EMPTY_TOTE_OUTWARD, menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_OTW_TOTE));
        args.putBoolean(ParamConstants.IS_OFF_RANGE, menuModel.getMenuCode().matches("(?i)(" + AppConstants.MENU_CODE_OTW_OFF_RANGE + "|" + AppConstants.MENU_CODE_OFF_RANGE + ")"));
        getRfidViewModel().deleteSession(AppCommonMethods.SessionType.OFF_RANGE);
        AppCommonMethods.logInFile(MainActivity.this, "\n-------------------------Off-Range---------------------------\n");
        //Don't call Get Products API if Pending Batch
        if(AppDatabase.getProductDao(this).getAllTotal(AppCommonMethods.SessionType.OFF_RANGE.getValue()) > 0 && getLastPendingOutwardBatchId(new String[]{"Range", "OffRange", "OFRNG"}))
          loadFragment(new OffRangeMainFragment(), args);
        else{
          callWebService(URLConstants.GET_OFF_RANGE_PRODUCTS, new JSONObject(), args, getString(R.string.progress_msg_getting_data), false, true);
        }
      }
      else if(fragment instanceof ThanCuttingFragment){
        args.putBoolean(ParamConstants.IS_THAN_CUTTING, menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_THAN_CUTTING));
        args.putBoolean(ParamConstants.IS_THAN_CLOSURE, menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_THAN_CLOSURE));
        loadFragment(new ThanCuttingFragment(), args);
      }
      else if(fragment instanceof ThanEncodingFragment){
        if(SharedPrefManager.getBoolean(ParamConstants.IS_OFFLINE_ENCODE))
          loadSessionFragment(AppCommonMethods.SessionType.ENCODING_THAN.name(), -2, -1, -1, args);
        else
          getActiveUsers(AppCommonMethods.SessionType.ENCODING_THAN, AppConstants.SESSION_ACTION_RESUME, args);
      }
      else if(fragment instanceof TripListFragment){
        final String typeIO = menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_INW1) || menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_INW_SERIAL) ? AppConstants.INWARD : AppConstants.OUTWARD;
        if(args == null) args = new Bundle();
        args.putString(ParamConstants.OPERATION_TYPE, typeIO);
        args.putString(ParamConstants.TYPE, typeIO);
        if(menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_INW_SERIAL))
          args.putBoolean(ParamConstants.IS_INW_WITH_SERIAL_NUMBER, true);
        final TripStatusDao tripStatusDao = AppDatabase.getDbInstance(this).TripStatusDao();
        final HUStatusDao huStatusDao = AppDatabase.getDbInstance(this).HUStatusDao();
        final HUDetailsDao huDetailsDao = AppDatabase.getDbInstance(this).HUDetailsDao();
        final TripInventoryDao tripInventoryDao = AppDatabase.getDbInstance(this).TripInventoryDao();
        final TripStatus inProcessTrip = tripStatusDao.getProcessingTripData(typeIO);
        //TODO make Configurable (Globally)
        if(inProcessTrip != null && huStatusDao.hasHUData(typeIO, inProcessTrip.tripNumber) && (isLockAndRedirectToProcessingTrip || (inProcessTrip.isManualTrip && isLockAndRedirectToProcessingManualTrip))){
          args.putSerializable(inProcessTrip.getClass().getSimpleName(), inProcessTrip);
          final HUStatus pendingHU = huStatusDao.getInProgressHUData(typeIO, inProcessTrip.tripNumber);
          //TODO make Configurable (Globally)
          if(pendingHU != null && huDetailsDao.hasHUDetails(typeIO, pendingHU.tripNumber, pendingHU.huNumber) && tripInventoryDao.hasData(pendingHU.tripNumber, pendingHU.huNumber) && isLockAndRedirectToInProcessHU){
            args.putSerializable(pendingHU.getClass().getSimpleName(), pendingHU);
            loadFragment(new HuProcessStartFragment(), args);
          }
          else{
            tripInventoryDao.deleteAllTripInventory();
            loadFragment(new TripHUListFragment(), args);
          }
        }
        else{
          if(SharedPrefManager.getBoolean(ParamConstants.IS_RFID_STORE, AppCommonMethods.isAllowManualTripCreation) && !AppDatabase.getSiteTypeDao(this).hasData()){
            try{
              JSONObject requestParams = new JSONObject();
              requestParams.put(ParamConstants.OPERATION_TYPE, typeIO);
              requestParams.put(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_ID, SharedPrefManager.getLong(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_ID));
              requestParams.put(ParamConstants.IS_RFID_STORE, SharedPrefManager.getBoolean(ParamConstants.IS_RFID_STORE, AppCommonMethods.isAllowManualTripCreation));
              callWebService(URLConstants.GET_SUPPLY_CHAIN_TYPES, requestParams, args, getString(R.string.progress_msg_getting_data));
            }
            catch(Exception e){ e.printStackTrace(); }
          }
          else{
            try{
              JSONObject requestParams = new JSONObject();
              requestParams.put(ParamConstants.OPERATION_TYPE, typeIO);
              callWebService(URLConstants.GET_IO_CONFIGURATION, requestParams, args, getString(R.string.progress_msg_getting_data));
            }
            catch(Exception e){ e.printStackTrace(); }
          }
        }
      }
      else if(fragment instanceof ReplenishmentBatchListFragment){
        if(hasZonesData(args)) loadFragment(fragment, args);
      }
      else if(fragment instanceof ReplenishmentArticleListFragment){
        if(hasZonesData(args)) loadFragment(fragment, args);
      }
      else loadFragment(fragment, args);
      //TODO remaining Add More + Shop Cluster/Nearby Shops
    }
  }
  
  /**
   * Clear saved data on logout.
   */
  public void clearSavedDataOnLogout(){
    unsubscribeFromFirebaseTopics();
    SharedPrefManager.setTripNo("");
    SharedPrefManager.setHuNo("");
    SharedPrefManager.setDeliveryNo("");
    SharedPrefManager.setString(ParamConstants.LABEL, "");
    SharedPrefManager.setString(ParamConstants.LABEL_BRANDS, "");
    SharedPrefManager.setString(ParamConstants.LABEL_CATEGORIES, "");
    SharedPrefManager.setString(ParamConstants.LABEL_ZONES, "");
    SharedPrefManager.setString(ParamConstants.LABEL_EANS, "");
    SharedPrefManager.setString(ParamConstants.LABEL_EPC, "");
    SharedPrefManager.setString(ParamConstants.LABEL_TID, "");
    SharedPrefManager.setString(ParamConstants.LABEL_PRIORITY, "");
    SharedPrefManager.setString(ParamConstants.LABEL_COLORS, "");
    SharedPrefManager.setString(ParamConstants.LABEL_SIZES, "");
    SharedPrefManager.setString(ParamConstants.LABEL_SUB_CATEGORY, "");
    SharedPrefManager.setString(ParamConstants.LABEL_ARTICLES, "");
    SharedPrefManager.setString(ParamConstants.LABEL_MATKL, "");
    SharedPrefManager.setString(ParamConstants.LABEL_NAME, "");
    SharedPrefManager.setString(ParamConstants.LABEL_DESCRIPTION, "");
    SharedPrefManager.setString(ParamConstants.LABEL_ORDER, "");
    SharedPrefManager.setString(ParamConstants.DECODE_TYPE, "");
    SharedPrefManager.clearArrayList(ParamConstants.OUTWARD_TOTE_EANS);
    SharedPrefManager.clearArrayList(ParamConstants.OUTWARD_TOTE_TYPES);
    SharedPrefManager.clearArrayList(ParamConstants.DECODE_TYPES);
    SharedPrefManager.setBoolean(ParamConstants.DECODE_TYPES + "_" + ParamConstants.IS_SAVED_FROM_LOGIN, true);
    SharedPrefManager.setString(ParamConstants.SEARCH_LIST_ID,"");
    SharedPrefManager.setString(ParamConstants.SEARCH_LIST_TYPE,"");
    SharedPrefManager.setString(ParamConstants.DEVICE_SERIAL,"");
    SharedPrefManager.setIsLoggedIn(false);
    AppDatabase db = AppDatabase.getDbInstance(MainActivity.this);
    db.BrandWiseZoneInventoryDao().deleteAll();
    db.MenuDao().deleteAll();
    db.ProductDao().deleteAll();
    db.TripStatusDao().deleteAll();
    db.TripInventoryDao().deleteAll();
    db.BrandDao().deleteAll();
    db.BrandEansDao().deleteAll();
    db.CategoryDao().deleteAll();
    db.ZoneDao().deleteAll();
    //New Addition
    db.FIFODao().deleteAll();
    db.ListDao().deleteAll();
    db.OutwardToteEansDao().deleteAll();
    db.OutwardBatchDao().deleteAll();
    db.HUStatusDao().deleteAll();
    db.HUDetailsDao().deleteAll();
    db.SiteTypeDao().deleteAll();
    db.SiteCodeDao().deleteAll();
    db.BatchEpcDao().deleteAll();
    db.ReplenishBatchDetailsDao().deleteAll();
    if(!isSetUserMgmt){
      db.InventoryDao().deleteAll();
      db.RFIDSessionDao().deleteAll();
    }
    else if(isSetUserMgmt){
      db.InventoryDao().deleteAllExcept();
      db.RFIDSessionDao().deleteAllExcept();
      deleteEmptySessions(db);
    }
    
    Intent intent = new Intent(MainActivity.this, LandingActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
  }
  
  /**
   * Get top fragment common fragment.
   *
   * @return the common fragment
   */
  public CommonFragment getTopFragment(){
    if(isNonEmpty(currentFragmentClassName)){
      final Fragment fragment = getSupportFragmentManager().findFragmentByTag(currentFragmentClassName);
      if(fragment != null && fragment instanceof CommonFragment) return (CommonFragment) fragment;
    }
    final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
    if(fragment != null && fragment instanceof CommonFragment) return (CommonFragment) fragment;
    return null;
  }
  
  @Override
  public void onBackPressed(){
    if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
      binding.drawerLayout.closeDrawer(GravityCompat.START);
    }
    else{
      int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
      showLog("backStackCount", "" + backStackCount);
      if(backStackCount == 1){
        uploadSearchLogs();
        //uploadAuditTrailsLogs();
        uploadWrittenInventoryTags();
        copyDataBase();
      }
      final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
      if(fragment != null) showLog("fragment", "" + fragment.getClass().getSimpleName());
      if(fragment != null && fragment instanceof CommonFragment){
        ((CommonFragment) fragment).onBackPressed();
        return;
      }
      else if(fragment != null && fragment instanceof NavHostFragment){
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        if(navController.getCurrentDestination().getId() == R.id.nav_home) finish();
        else setUpHome(true);
        return;
      }
      super.onBackPressed();
    }
  }
  
  /**
   * Check reader connection.
   *
   * @param view the view
   */
  public void checkReaderConnection(final View view){
    if(view != null) checkReaderConnection(null, null, view);
  }
  
  /**
   * Check reader connection.
   *
   * @param fragment the fragment
   * @param args     the args
   */
  public void checkReaderConnection(final CommonFragment fragment, final Bundle args){
    if(SharedPrefManager.getDeviceTypeValue() <= 0)
      AppCommonMethods.showShortToast(this, R.string.feature_not_available);
    else if(fragment != null) checkReaderConnection(fragment, args, null);
  }
  
  /**
   * Check reader connection.
   *
   * @param fragment the fragment
   * @param args     the args
   * @param view     the view
   */
  public void checkReaderConnection(final CommonFragment fragment, final Bundle args, final View view){
    showLog("mReader", "" + (mReader != null) + "_" + mainViewModel.isReaderPresent(mReader != null));
    if(mainViewModel != null && mainViewModel.isReaderPresent(mReader != null)){
      if(fragment != null){
        showProgressDialog(R.string.msg_init_reader_connection);
        loadFragment(fragment, args);
      }
      else if(view != null){
        view.performClick();
      }
    }
    else{
      showCustomAlertDialog("", String.format(getString(R.string.err_reader_connection), fragment.getTypeCharCode()), getString(R.string.btn_ok), new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialogInterface, int i){
          if(mainViewModel != null && mainViewModel.isReaderPresent(mReader != null)){
            if(fragment != null){
              showProgressDialog(getString(R.string.msg_init_reader_connection));
              loadFragment(fragment, args);
            }
            else if(view != null){
              view.performClick();
            }
          }
          else checkAndSetReader();
        }
      });
    }
  }
  
  /**
   * Get active users.
   *
   * @param type   the type
   * @param action the action
   * @param args   the args
   */
  public void getActiveUsers(AppCommonMethods.SessionType type, String action, Bundle args){
    if(isInternetConnected(this, false, false)){
      try{
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put(ParamConstants.SESSION_TYPE, type.name());
        jsonRequest.put(ParamConstants.ACTION, action);
        if(AppCommonMethods.isCheckActiveUsersBeforeLoadingSession)
          loadSessionFragment(type.name(), -2, -1, -1, args);
        else
          callWebService(URLConstants.GET_ACTIVE_USERS, jsonRequest, args, getString(R.string.progress_msg_getting_data), true);
      }
      catch(Exception e){ e.printStackTrace(); }
    }
    else loadSessionFragment(type.name(), -2, -1, -1, args);
  }
  
  /**
   * Load session fragment.
   *
   * @param type             the type
   * @param activeUsers      the active users
   * @param sessionValidTill the session valid till
   * @param target           the target
   * @param args             the args
   */
  @Override
  public void loadSessionFragment(String type, int activeUsers, int sessionValidTill, int target, Bundle args){
    if(isNonEmpty(type)) switch(AppCommonMethods.SessionType.valueOf(type)){
      case INVENTORY:
        if(args == null){
          args = new Bundle();
        }
        args.putSerializable(MenuModel.class.getSimpleName(), AppDatabase.getMenuDao(this).getMenuByCode(AppConstants.MENU_CODE_INV_START));
        args.putInt(AppConstants.ACTIVE_USERS, activeUsers);
        args.putInt(AppConstants.SESSION_VALID_TILL, sessionValidTill > 0 ? sessionValidTill : 48);
        checkReaderConnection(new InventoryStartFragment(), args);
        break;
      case ADD_INVENTORY:
        if(args == null){
          args = new Bundle();
        }
        args.putSerializable(MenuModel.class.getSimpleName(), AppDatabase.getMenuDao(this).getMenuByCode(AppConstants.MENU_CODE_INV_ADD));
        args.putInt(AppConstants.ACTIVE_USERS, activeUsers);
        args.putInt(AppConstants.SESSION_VALID_TILL, sessionValidTill > 0 ? sessionValidTill : 48);
        checkReaderConnection(new InventoryAddFragment(), args);
        break;
      case FILTER_INVENTORY:
        if(args == null){
          args = new Bundle();
        }
        args.putSerializable(MenuModel.class.getSimpleName(), AppDatabase.getMenuDao(this).getMenuByCode(AppConstants.MENU_CODE_INV_FILTER));
        args.putInt(AppConstants.ACTIVE_USERS, activeUsers);
        args.putInt(AppConstants.SESSION_VALID_TILL, sessionValidTill > 0 ? sessionValidTill : 48);
        checkReaderConnection(new InventoryFilterFragment(), args);
        break;
      case BRAND_INVENTORY:
        if(args == null){
          args = new Bundle();
        }
        args.putSerializable(MenuModel.class.getSimpleName(), AppDatabase.getMenuDao(this).getMenuByCode(AppConstants.MENU_CODE_INV_BRAND));
        args.putInt(AppConstants.ACTIVE_USERS, activeUsers);
        args.putInt(AppConstants.SESSION_VALID_TILL, sessionValidTill > 0 ? sessionValidTill : 48);
        checkReaderConnection(new InventoryBrandFragment(), args);
        break;
      case ENCODING:
      case ENCODING_THAN:
        if(args == null){
          args = new Bundle();
        }
        if(!args.containsKey(MenuModel.class.getSimpleName()))
          args.putSerializable(MenuModel.class.getSimpleName(), AppDatabase.getMenuDao(this).getMenuByCode(type.equalsIgnoreCase(AppCommonMethods.SessionType.ENCODING_THAN.name()) ? AppConstants.MENU_CODE_THAN_ENC : AppConstants.MENU_CODE_ENC_START));
        args.putInt(AppConstants.ACTIVE_USERS, activeUsers);
        args.putInt(AppConstants.SESSION_VALID_TILL, sessionValidTill > 0 ? sessionValidTill : 24);
        args.putInt(AppConstants.TARGET, target);
        if(args.containsKey("FragmentClass") && args.getString("FragmentClass").matches("(?i)^[A-Za-z]*Scan[A-Za-z]*$"))
          checkReaderConnection(new EncodingScanScanWriteFragment(), args);
        else if(args.containsKey("FragmentClass") && args.getString("FragmentClass").matches("(?i)^[A-Za-z]*Than[A-Za-z]*$"))
          checkReaderConnection(new ThanEncodingFragment(), args);
        else checkReaderConnection(new EncodingStartFragment(), args);
        break;
      default:
        break;
    }
  }
  
  @Override
  public boolean onSupportNavigateUp(){
    if(isMenusLocked) AppCommonMethods.showShortToast(this, R.string.not_allowed);
    //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    //return !isMenusLocked && binding.drawerLayout.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED && (NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp());
    //Code to set Drawer without Navigation Controller (Does not crash on Activity Restart)
    return !isMenusLocked && binding.drawerLayout.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED /*&& (NavigationUI.navigateUp(navController, mAppBarConfiguration)*/ || super.onSupportNavigateUp();//);
  }
  
  public void hideNavItems(boolean isHomeScreen){
    showLog("hideNavItems", "" + isHomeScreen);
    final Menu navMenu = binding.navView.getMenu();
    navMenu.findItem(R.id.nav_change_pass).setVisible(isSetUserMgmt && isStaticDebug() && isHomeScreen);
    navMenu.findItem(R.id.nav_log_info).setVisible(SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_SHARE_LOG,AppCommonMethods.isAllowShareLog) && isHomeScreen);
    //navMenu.findItem(R.id.nav_reset_demo).setVisible(isHomeScreen);
    //navMenu.findItem(R.id.nav_set_default).setVisible(AppDatabase.getStockDefaultDao(this).hasData() && isHomeScreen);
    //navMenu.findItem(R.id.nav_set_tag_password).setVisible(isShowResetTagPassword && isHomeScreen);
  }
  
  @Override
  protected void onPause(){
    super.onPause();
  }
  
  @Override
  protected void onResume(){
    super.onResume();
  }
  
  @Override
  protected void onDestroy(){
    super.onDestroy();
    if(mainViewModel != null) mainViewModel.onDestroy();
  }
  
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event){
    //showLog("EVENT", "" + keyCode);
    handleTriggerKeyEvent(keyCode, event);
    return super.onKeyDown(keyCode, event);
  }
  
  @Override
  public void handleTriggerKeyEvent(int keyCode, KeyEvent event){
    if(isTablet && SharedPrefManager.getDeviceType() == AppCommonMethods.DeviceType.CHAINWAY){
      if(SharedPrefManager.getTriggerKeyCodes().length() > 0 && SharedPrefManager.getTriggerKeyCodes().contains("," + keyCode + ",") && event.getRepeatCount() == 0){
        showLog("TRIGGER1", "PRESSED1");
        if(mainViewModel != null) mainViewModel.setTriggerValue(true);
      }
    }
    if(SharedPrefManager.getDeviceType() == AppCommonMethods.DeviceType.ALPS){
      showLog("EVENT", "ALPS11");
      if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == 131 && event.getRepeatCount() == 0){
        showLog("EVENT", "ALPS");
        if(mainViewModel != null) mainViewModel.setTriggerValue(true);
      }
    }
    if(SharedPrefManager.getDeviceType() == AppCommonMethods.DeviceType.SEUIC){
      showLog("EVENT", "SEUIC11");
      if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == 142 && event.getRepeatCount() == 0){
        showLog("EVENT", "SEUIC");
        if(mainViewModel != null) mainViewModel.setTriggerValue(true);
      }
    }
    if(SharedPrefManager.getDeviceType() == AppCommonMethods.DeviceType.CIPHERLAB){
      showLog("EVENT", "CIPHERLAB11");
      if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == 545 && event.getRepeatCount() == 0){
        showLog("EVENT", "CIPHERLAB");
        if(mainViewModel != null) mainViewModel.setTriggerValue(true);
      }
    }
  }
  
  /**
   * Get rfid view model main view model.
   *
   * @return the main view model
   */
  public MainViewModel getRfidViewModel(){
    try{
      if(mainViewModel == null){
        mainViewModel = new MainViewModel(this);
        if(binding != null){
          binding.setMainViewModel(mainViewModel);
          binding.executePendingBindings();
        }
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return mainViewModel;
  }
  
  public ProductModel getProductModelFromResponse(final JSONObject jsonRequest, JSONObject jsonResponse){
    //Commented Code (to be used for Demo Only)
    final String ean = extractString(jsonRequest, ParamConstants.EAN, "");
    if(isDemoApp && isStaticDebug(this)){
      if(ean.matches("(890779629737[4-6]|890779629738[1-3])")){
        try{
          jsonResponse = AppCommonMethods.getSampleJSON(this, URLConstants.GET_PRODUCT_INFO + "_" + ean);
          AppCommonMethods.showLog("result", jsonResponse.toString());
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }
    }
    ProductModel productModel = null;
    try{
      final JSONArray dataArray = extractJSONArray(jsonResponse, ParamConstants.DATA);
      final JSONObject dataObject = dataArray != null && dataArray.length() > 0 ? dataArray.getJSONObject(0) : extractJSONObject(jsonResponse, ParamConstants.DATA, jsonResponse);
      productModel = jsonResponse != null ? getGSON().fromJson(dataObject.toString(), ProductModel.class) : null;
    }
    catch(Exception e){
      e.printStackTrace();
    }
    final String epc = extractString(jsonRequest, ParamConstants.EPC, "");
    if(productModel == null) productModel = new ProductModel();
    if(productModel != null){
      if(isNullOrEmpty(productModel.getEan()))
        productModel.setEan(extractString(jsonRequest, ParamConstants.EAN, ""));
      //if(isDebugApp && SharedPrefManager.getServerUrl().matches("(?i).*pmj.*")) jsonResponse=getSampleJSON(this,"getProductDetailsPMJ");
      productModel.setItemImgUrl(extractString(jsonResponse, ParamConstants.IMG_URL, "").replaceAll(AppConstants.IMAGE_URL_REPLACE_REGEX, "").trim());
      productModel.setDisplayData(extractString(jsonResponse, ParamConstants.DISPLAY_DATA));
      productModel.setDisplayDataDetails(extractString(jsonResponse, ParamConstants.DISPLAY_DATA_DETAILS,extractString(jsonResponse, ParamConstants.DISPLAY_DATA_DETAILS1)));
      if(isNonEmpty(epc)){
        productModel.setEpc(epc);
        if(!extractBoolean(jsonResponse, ParamConstants.IS_SOLD, false))
          productModel.setSold(epcEncoderDecoder.isSold(epc));
      }
    }
    return productModel;
  }
  
  private void showDecodeTypeAlert(Bundle arguments){
    final ArrayList<DecodeType> listDecodeTypes = (ArrayList<DecodeType>) SharedPrefManager.getArrayList(ParamConstants.DECODE_TYPES);
    final String title = extractString(arguments, AppConstants.TITLE, getString(R.string.decoding));
    final String label = extractString(arguments, ParamConstants.LABEL, String.format(getString(R.string.lbl_custom_type), title));
    final int margin5dp = getResources().getDimensionPixelOffset(R.dimen.dp_5);
    final LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
    llParams.setMargins(margin5dp, margin5dp, margin5dp, margin5dp);
    
    final RadioGroup rg = new RadioGroup(this);
    rg.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    final String selDecodeType = SharedPrefManager.getString(ParamConstants.DECODE_TYPE);
    int i = 0;
    for(DecodeType dt : listDecodeTypes){
      if(dt == null) return;
      final int index = i;
      RadioButton rb = new RadioButton(this);
      rb.setId(Integer.parseInt("123" + (++i)));
      rb.setSingleLine(true);
      rb.setEllipsize(TextUtils.TruncateAt.END);
      rb.setPadding(0, margin5dp, 0, margin5dp);
      if(dt != null){
        rb.setTag(dt);
        rb.setText(dt.toString());
        rb.setChecked(isNullOrEmpty(selDecodeType) ? index == 0 : selDecodeType.equalsIgnoreCase(dt.getType()));
      }
      rg.addView(rb, llParams);
    }
    final Bundle args1 = arguments;
    showCustomAlertDialog(String.format(getString(R.string.title_select_type), label.replaceAll("(?i)Type", "").trim()), "", rg, getString(R.string.btn_select), new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialog, int which){
        Object tag = rg.findViewById(rg.getCheckedRadioButtonId()).getTag();
        if(tag != null && tag instanceof DecodeType){
          final String selDecodeTypeLabel = ((DecodeType) tag).getLabel();
          final String selDecodeType = ((DecodeType) tag).getType();
          SharedPrefManager.setString(ParamConstants.DECODE_TYPE, selDecodeType);
          try{
            args1.putString(ParamConstants.DECODE_TYPE, selDecodeType);
            loadFragment(new DecodingStartFragment(), args1);
          }
          catch(Exception e){
            e.printStackTrace();
          }
        }
      }
    }, getString(R.string.btn_cancel), null);
  }
  
  private boolean hasZonesData(final Bundle args){
    if(AppDatabase.getZoneDao(this).hasData()) return true;
    //check & get zones if zoneDao is empty
    callWebService(URLConstants.GET_ZONES, new JSONObject(), args, getString(R.string.progress_msg_getting_data), false, true);
    return false;
  }
  
  public void handlePostRedirection(final String url, final Fragment frgment, final Bundle arguments){
    try{
      switch(url){
        case URLConstants.GET_ZONES:
          if(AppDatabase.getZoneDao(this).hasData()){
            if(frgment != null && frgment instanceof CommonFragment){
              if(frgment.getClass().getSimpleName().matches("(?i)(Start)"))
                checkReaderConnection((CommonFragment) frgment, arguments);
              else loadFragment(frgment, arguments);
            }
            else{
              final String fragmentClass = extractString(arguments, "FragmentRedirectionClass");
              final MenuModel menuModel = (MenuModel) extractSerializable(arguments, MenuModel.class);
              if(isNonEmpty(fragmentClass)){
                final Class fragClass = Class.forName(fragmentClass);
                if(fragClass != null && CommonFragment.class.isAssignableFrom(fragClass)){
                  final Fragment fragment1 = (Fragment) fragClass.newInstance();
                  handleFragmentRedirection(fragment1,menuModel,arguments);
                  /*if(fragmentClass.matches("(?i)(Start)"))
                    checkReaderConnection((CommonFragment) fragment1, arguments);
                  else loadFragment(fragment1, arguments);*/
                }
              }
              else if(menuModel!=null && isNonEmpty(menuModel.getMenuCode())) handleFragmentRedirection(menuModel,arguments);
            }
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_BRANDS:
          final BrandDao brandDao = AppDatabase.getBrandDao(this);
          if(isSuccess){
            final Bundle arguments = args;
            arguments.putString(ParamConstants.LABEL, extractString(jsonResponse, ParamConstants.LABEL));
            SharedPrefManager.setString(ParamConstants.LABEL_BRANDS, extractString(jsonResponse, ParamConstants.LABEL_BRANDS));
            SharedPrefManager.setInt(ParamConstants.MAX_SELECTION_BRANDS, chkZero(chkZero(extractInt(jsonResponse, ParamConstants.MAX_SELECTION_BRANDS, 0), extractInt(jsonResponse, ParamConstants.MAX_SELECTION, 0)), 5));
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_ADVANCE_FILTERS_FOR_BRAND_INVENTORY, extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_ADVANCE_FILTERS_FOR_BRAND_INVENTORY, AppCommonMethods.isAllowAdvanceFilterForBrand));
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_ADVANCE_FILTERS_FOR_MULTI_BRANDS, extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_ADVANCE_FILTERS_FOR_MULTI_BRANDS, AppCommonMethods.isAllowAdvanceFilterForMultiBrands));
            SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_BRAND_INV, extractBoolean(jsonResponse, ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_BRAND_INV, SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_BRAND_INV)));
            final JSONArray jsonArrayBrands = extractJSONArray(jsonResponse, ParamConstants.BRANDS);
            final JSONArray jsonArrayAttributes = extractJSONArray(jsonResponse, ParamConstants.BRANDS, jsonArrayBrands);
            final boolean isMultiAttributesSelection = extractBoolean(jsonResponse, ParamConstants.IS_MULTI_ATTRIBUTES_SELECTION, false);
            final String type = extractString(jsonRequest, ParamConstants.SESSION_TYPE);
            final int activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, -2);
            final int sessionValidTill = extractInt(jsonResponse, ParamConstants.SESSION_VALID_TILL, -1);
            final int target = extractInt(jsonResponse, ParamConstants.TARGET, -1);
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg)) showCustomErrDialog(errMsg);
            else{
              if(isMultiAttributesSelection && jsonArrayAttributes != null){
                final String title = extractString(arguments, AppConstants.TITLE, getString(R.string.inventory));
                final String label = extractString(arguments, ParamConstants.LABEL, String.format(getString(R.string.lbl_custom_type), title));
                final int margin5dp = getResources().getDimensionPixelOffset(R.dimen.dp_5);
                final LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                llParams.setMargins(margin5dp, margin5dp, margin5dp, margin5dp);
                
                final RadioGroup rg = new RadioGroup(this);
                rg.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                final String selAttribute = SharedPrefManager.getString(ParamConstants.ATTRIBUTE_NAME);
                for(int i = 0; i < jsonArrayAttributes.length(); i++){
                  Object obj = jsonArrayAttributes.get(i);
                  if(obj == null) continue;
                  RadioButton rb = new RadioButton(this);
                  rb.setId(Integer.parseInt("123" + i));
                  rb.setSingleLine(true);
                  rb.setEllipsize(TextUtils.TruncateAt.END);
                  rb.setPadding(0, margin5dp, 0, margin5dp);
                  if(obj instanceof String){
                    rb.setTag(obj);
                    rb.setText(obj.toString());
                    rb.setChecked(isNullOrEmpty(selAttribute) ? i == 0 : selAttribute.equalsIgnoreCase(obj.toString()));
                  }
                  else if(obj instanceof JSONObject){
                    LabelIds lblIds = getGSON().fromJson(jsonArrayBrands.get(i).toString(), LabelIds.class);
                    if(lblIds != null){
                      rb.setTag(lblIds);
                      rb.setText(lblIds.toString());
                      rb.setChecked(isNullOrEmpty(selAttribute) ? i == 0 : selAttribute.equalsIgnoreCase(lblIds.toString()));
                    }
                  }
                  rg.addView(rb, llParams);
                }
                final Bundle args1 = args;
                showCustomAlertDialog(String.format(getString(R.string.title_select_type), label.replaceAll("(?i)Type", "").trim()), "", rg, getString(R.string.btn_select), new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialog, int which){
                    Object tag = rg.findViewById(rg.getCheckedRadioButtonId()).getTag();
                    if(tag != null && (tag instanceof String || tag instanceof LabelIds)){
                      final String attributeName = tag.toString();
                      final Long attributeId = tag instanceof LabelIds ? ((LabelIds) tag).getId() : null;
                      SharedPrefManager.setString(ParamConstants.ATTRIBUTE_NAME, attributeName);
                      try{
                        JSONObject jsonRequest = new JSONObject();
                        jsonRequest.put(ParamConstants.SESSION_TYPE, AppCommonMethods.SessionType.BRAND_INVENTORY.name());
                        jsonRequest.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_RESUME);
                        if(attributeId != null)
                          jsonRequest.put(ParamConstants.ATTRIBUTE_ID, attributeId);
                        jsonRequest.put(ParamConstants.ATTRIBUTE_NAME, attributeName);
                        callWebService(URLConstants.GET_BRANDS, jsonRequest, args1, getString(R.string.progress_msg_getting_data), AppDatabase.getBrandDao(MainActivity.this).hasData(), true);
                      }
                      catch(Exception e){ e.printStackTrace(); }
                    }
                  }
                }, getString(R.string.btn_cancel), null);
              }
              else{
                //Save Brands
                if(jsonArrayBrands != null)
                  new InsertDBBrandsCategories(this, url, type, activeUsers, sessionValidTill, target, errMsg, args).execute(jsonArrayBrands);
                  //new InsertDBTask(url, type, activeUsers, sessionValidTill, target, errMsg, args).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jsonArrayBrands);
                else if(brandDao.hasData()){
                  loadSessionFragment(type, activeUsers, sessionValidTill, target, arguments);
                }
              }
            }
          }
          else if(brandDao.hasData())
            getActiveUsers(AppCommonMethods.SessionType.BRAND_INVENTORY, AppConstants.SESSION_ACTION_RESUME, args);
          break;
        case URLConstants.GET_CATEGORIES:
          if(isSuccess){
            final Bundle arguments = args;
            final CategoryDao categoryDao = AppDatabase.getCategoryDao(this);
            //Save Categories
            arguments.putString(ParamConstants.LABEL, extractString(jsonResponse, ParamConstants.LABEL));
            SharedPrefManager.setString(ParamConstants.LABEL_CATEGORIES, extractString(jsonResponse, ParamConstants.LABEL_CATEGORIES));
            SharedPrefManager.setInt(ParamConstants.MAX_SELECTION_CATEGORIES, chkZero(chkZero(extractInt(jsonResponse, ParamConstants.MAX_SELECTION_CATEGORIES, 0), extractInt(jsonResponse, ParamConstants.MAX_SELECTION_CATEGORIES, 0)), 5));
            String type = extractString(jsonRequest, ParamConstants.SESSION_TYPE);
            int activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, -2);
            int sessionValidTill = extractInt(jsonResponse, ParamConstants.SESSION_VALID_TILL, -1);
            int target = extractInt(jsonResponse, ParamConstants.TARGET, -1);
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            final JSONArray jsonArrayCategories = extractJSONArray(jsonResponse, ParamConstants.CATEGORIES);
            if(jsonArrayCategories != null)
              new InsertDBBrandsCategories(this, url, type, activeUsers, sessionValidTill, target, errMsg, args).execute(jsonArrayCategories);
              //new InsertDBTask(url, type, activeUsers, sessionValidTill, target, errMsg, args).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jsonArrayCategories);
            else if(categoryDao.hasData()){
              loadSessionFragment(type, activeUsers, sessionValidTill, target, arguments);
            }
          }
          break;
        case URLConstants.GET_ZONES:
          final String fragmentClass = extractString(args, "FragmentRedirectionClass");
          final Class fragClass = isNonEmpty(fragmentClass) ? Class.forName(fragmentClass) : null;
          final CommonFragment fragment = fragClass != null && CommonFragment.class.isAssignableFrom(fragClass) ? (CommonFragment) fragClass.newInstance() : null;
          if(isSuccess){
            final String labelZones = extractString(jsonResponse, ParamConstants.LABEL);
            if(args == null) args = new Bundle();
            args.putString(ParamConstants.LABEL, labelZones);
            SharedPrefManager.setString(ParamConstants.LABEL_ZONES, labelZones);
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg)){
              hideProgressDialog();
              showCustomErrDialog(errMsg);
            }
            else{
              final JSONArray jsonArrayZones = extractJSONArray(jsonResponse, ParamConstants.ZONES, extractJSONArray(jsonResponse, ParamConstants.DATA));
              if(isNonEmpty(jsonArrayZones))
                new InsertDBZones(this, fragment, jsonResponse, url, errMsg, args).execute(jsonArrayZones);
              else{
                hideProgressDialog();
                if(isShowErrorForNoZonesInAPIResponse && !AppDatabase.getZoneDao(this).hasData()) showCustomErrDialog(String.format(getString(R.string.err_no__available),SharedPrefManager.getString(ParamConstants.LABEL_ZONES,getString(R.string.lbl_location))));
                else handlePostRedirection(url, fragment, args);
              }
            }
          }
          else hideProgressDialog();
          break;
        case URLConstants.GET_ENCODING_ACHIEVEMENTS:
          if(isSuccess){
            if(args == null) args = new Bundle();
            args.putString(AppConstants.AVG_TAGS_ENCODE_PER_DAY, extractString(jsonResponse, ParamConstants.AVG_TAGS_ENCODE_PER_DAY));
            args.putString(AppConstants.MAX_TAGS_ENCODE_PER_DAY, extractString(jsonResponse, ParamConstants.MAX_TAGS_ENCODE_PER_DAY));
            args.putString(AppConstants.AVG_ENCODE_TIME_PER_TAG, extractString(jsonResponse, ParamConstants.AVG_ENCODE_TIME_PER_TAG));
            args.putString(AppConstants.HOURS_SPENT, extractString(jsonResponse, ParamConstants.HOURS_SPENT));
            loadFragment(new EncodingAchieveFragment(), args);
          }
          break;
        case URLConstants.GET_ACTIVE_USERS:
          String type = extractString(jsonRequest, ParamConstants.SESSION_TYPE);
          int activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, -2);
          int sessionValidTill = extractInt(jsonResponse, ParamConstants.SESSION_VALID_TILL, -1);
          int target = extractInt(jsonResponse, ParamConstants.TARGET, -1);
          loadSessionFragment(type, activeUsers, sessionValidTill, target, args);
          break;
        case URLConstants.UPLOAD_INVENTORY:
          if(isSuccess && jsonResponse != null){
            final RFIDSession sessionObject = args != null ? args.getParcelable(RFIDSession.class.getSimpleName()) : null;
            final MenuModel menuModel = args != null ? args.getParcelable(MenuModel.class.getSimpleName()) : null;
            if(sessionObject != null) mainViewModel.stopSession(sessionObject, true);
            showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode(sessionObject.sessionType))), false);
            handleFragmentRedirection(menuModel);
          }
          break;
        case URLConstants.GET_STOCK_CORRECTION_DASHBOARD:
          if(isSuccess && jsonResponse != null){
            showProgressDialog(getString(R.string.progress_msg_check_data));
            JSONArray shoartageProductsArray = extractJSONArray(jsonResponse, ParamConstants.SHORTAGE_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            AppDatabase.getProductDao(this).deleteAllExcept();
            AppDatabase.getInventoryDao(this).deleteInventory(AppCommonMethods.SessionType.STOCK_CORRECTION.getValue());
            if(shoartageProductsArray != null)
              callInsertProductDBTask(url, AppCommonMethods.SessionType.STOCK_CORRECTION.getValue(), jsonResponse, args, shoartageProductsArray);
            else hideProgressDialog();
          }
          break;
        case URLConstants.GET_PICK_LIST:
          if(isSuccess && jsonResponse != null){
            args = args != null ? args : new Bundle();
            args.putString(AppConstants.SEARCH_LIST_ID, extractString(jsonRequest, ParamConstants.SEARCH_LIST_ID, extractString(jsonResponse, ParamConstants.SEARCH_LIST_ID, extractString(jsonResponse, ParamConstants.SEARCH_LIST_NAME, extractString(jsonResponse, ParamConstants.NAME, extractString(jsonResponse, ParamConstants.CODE, extractString(jsonResponse, ParamConstants.ORDER_NO, "")))))));
            args.putString(AppConstants.SEARCH_LIST_TYPE, extractString(jsonRequest, ParamConstants.SEARCH_LIST_TYPE, extractString(jsonResponse, ParamConstants.SEARCH_LIST_TYPE, extractString(jsonResponse, ParamConstants.TYPE, ""))));
            args.putString(ParamConstants.OMNI_UPLOAD_TYPE, extractString(jsonResponse, ParamConstants.OMNI_UPLOAD_TYPE, ""));
            final boolean isAllowDecode = extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE, false);
            args.putBoolean(AppConstants.IS_ALLOW_DECODE, isAllowDecode);
            args.putBoolean(AppConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecode && extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE_ON_PICK, false));
            if(isAllowDecode) saveTagWritePasswords(jsonResponse);
            SharedPrefManager.setInt(ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, extractInt(jsonResponse, ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, AppCommonMethods.markFoundPercentLBS));
            JSONArray searchProductsArray = extractJSONArray(jsonResponse, ParamConstants.SEARCH_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(searchProductsArray != null)
              callInsertProductDBTask(url, AppCommonMethods.SessionType.SEARCH_LIST.getValue(), jsonResponse, args, searchProductsArray);
            else if(isNonEmpty(searchProductsArray)){
              
              final ProductDao productDao = AppDatabase.getProductDao(this);
              productDao.deleteAll(AppCommonMethods.SessionType.SEARCH_LIST.getValue());
              AppDatabase.getInventoryDao(this).deleteAllExcept();
              int insertCount = 0;
              showProgressDialog(getString(R.string.progress_msg_check_data));
              for(int i = 0; i < searchProductsArray.length(); i++){
                final JSONObject product = searchProductsArray.getJSONObject(i);
                final ProductModel productModel = product != null ? getGSON().fromJson(product.toString(), ProductModel.class) : null;
                if(productModel != null){
                  productModel.setSessionType(AppCommonMethods.SessionType.SEARCH_LIST.getValue());
                  productModel.setItemImgUrl(extractString(product, ParamConstants.IMG_URL, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim());
                  final JSONArray jsonZones = extractJSONArray(product, ParamConstants.ZONES);
                  if(jsonZones != null && jsonZones.length() > 0){
                    int totalQty = 0;
                    for(int j = 0; j < jsonZones.length(); j++){
                      JSONObject zone = jsonZones.getJSONObject(j);
                      final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                      final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, extractInt(zone, ParamConstants.QTY, 0));
                      final String zoneName = extractString(zone, ParamConstants.ZONE_NAME, extractString(zone, ParamConstants.ZONE, Integer.parseInt(chkZero(zoneId, "0")) > 0 && chkNull(eanQty, 0) > 0 ? AppDatabase.getZoneDao(MainActivity.this).getZoneNameById(zoneId) : ""));
                      if(isNonEmpty(zoneName) && chkNull(eanQty, 0) > 0){
                        productModel.setZone(zoneName);
                        productModel.setZoneId(zoneId);
                        productModel.setEanQty(eanQty);
                        totalQty += chkNull(eanQty, 0);
                        productDao.insert(productModel);
                        insertCount++;
                      }
                    }
                    if(insertCount > 0 && totalQty > 0){
                      if(!isUseNewUIForLBS && totalQty < productModel.getQty()){
                        final String zoneName = AppConstants.DEFAULT_NO_VALUE;
                        final String zoneId = "0";
                        final Integer eanQty = productModel.getQty() - totalQty;
                        productModel.setZone(zoneName);
                        productModel.setZoneId(zoneId);
                        productModel.setEanQty(eanQty);
                        totalQty += chkNull(eanQty, 0);
                        productDao.insert(productModel);
                        insertCount++;
                      }
                      productDao.updateTotalQty(productModel.ean, totalQty, productModel.getSessionType());
                    }
                  }
                  else if(!isUseNewUIForLBS){
                    productModel.eanQty = productModel.qty;
                    productModel.totalQty = productModel.qty;
                    productDao.insert(productModel);
                    insertCount++;
                  }
                }
              }
              if(insertCount > 0){
                //SharedPrefManager.setInt(ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, extractInt(jsonResponse, ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, AppCommonMethods.markFoundPercentLBS));
               /* Bundle bundle = args != null ? args : new Bundle();
                bundle.putString(AppConstants.SEARCH_LIST_ID, extractString(jsonRequest, ParamConstants.SEARCH_LIST_ID, extractString(jsonResponse, ParamConstants.SEARCH_LIST_ID, extractString(jsonResponse, ParamConstants.SEARCH_LIST_NAME, extractString(jsonResponse, ParamConstants.NAME, extractString(jsonResponse, ParamConstants.CODE, extractString(jsonResponse, ParamConstants.ORDER_NO, "")))))));
                bundle.putString(AppConstants.SEARCH_LIST_TYPE, extractString(jsonRequest, ParamConstants.SEARCH_LIST_TYPE, extractString(jsonResponse, ParamConstants.SEARCH_LIST_TYPE, "")));*/
                loadFragment(new SearchListFragment(), args);//bundle);
              }
              hideProgressDialog();
            }
            else hideProgressDialog();
          }
          break;
        case URLConstants.GET_EXCEL_SEARCH_LIST:
          final boolean hasExistingSearchListData = AppDatabase.getProductDao(this).getAllTotal(AppCommonMethods.SessionType.SER_EXCEL.getValue())>0;
          if(isSuccess && jsonResponse != null){
            final ProductDao productDao =  AppDatabase.getProductDao(this);
            final Bundle arg = args != null ? args : new Bundle();
            final String serListId = extractString(jsonRequest, ParamConstants.SEARCH_LIST_ID, extractString(jsonResponse, ParamConstants.SEARCH_LIST_ID, extractString(jsonResponse, ParamConstants.SEARCH_LIST_NAME, extractString(jsonResponse, ParamConstants.NAME, extractString(jsonResponse, ParamConstants.CODE, extractString(jsonResponse, ParamConstants.ORDER_NO,extractString(jsonResponse, ParamConstants.BATCH_ID,extractString(jsonResponse, ParamConstants.BATCH, ""))))))));
            final String serListType = extractString(jsonRequest, ParamConstants.SEARCH_LIST_TYPE, extractString(jsonResponse, ParamConstants.SEARCH_LIST_TYPE, extractString(jsonResponse, ParamConstants.TYPE, isDebugApp?AppConstants.MENU_CODE_SER_LIST:"")));
            arg.putString(ParamConstants.SEARCH_LIST_ID, serListId);
            arg.putString(ParamConstants.SEARCH_LIST_TYPE, serListType);
            arg.putString(ParamConstants.OMNI_UPLOAD_TYPE, extractString(jsonResponse, ParamConstants.OMNI_UPLOAD_TYPE, ""));
            final boolean isAllowDecode = extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE, false);
            arg.putBoolean(ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
            arg.putBoolean(ParamConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecode && extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE_ON_PICK, false));
            if(isAllowDecode) saveTagWritePasswords(jsonResponse);
            SharedPrefManager.setInt(ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED_NEW, extractInt(jsonResponse, ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED_NEW, SharedPrefManager.getInt(ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED_NEW,AppCommonMethods.markFoundPercentNewLBS)));
            JSONArray searchProductsArray = extractJSONArray(jsonResponse, ParamConstants.SEARCH_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(isNonEmpty(searchProductsArray)){
              //check if previously saved list available
              if(hasExistingSearchListData){
                //check if new search list is available (i.e. different search list id then before)
               if(!SharedPrefManager.getString(ParamConstants.SEARCH_LIST_ID,"").equalsIgnoreCase(serListId)){
                 if(isConfirmUserActionIfNewListAvailable){
                   showCustomAlertDialog(getString(R.string.title_available_new_list), getString(R.string.msg_available_new_list), getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i){
                       callInsertProductDBTask(url, AppCommonMethods.SessionType.SER_EXCEL.getValue(), jsonResponse, arg, searchProductsArray);
                     }
                   }, getString(R.string.btn_no), new DialogInterface.OnClickListener(){
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i){
                       hideProgressDialog();
                       arg.putString(ParamConstants.SEARCH_LIST_ID, SharedPrefManager.getString(ParamConstants.SEARCH_LIST_ID, serListId));
                       arg.putString(ParamConstants.SEARCH_LIST_TYPE, SharedPrefManager.getString(ParamConstants.SEARCH_LIST_TYPE, serListType));
                       loadFragment(new SearchListExcelFragment(), arg);
                     }
                   });
                 }
                 else{
                   if(isNotifyUserActionIfNewListAvailable) showLongToast(getString(R.string.msg_loaded_new_list));
                   callInsertProductDBTask(url, AppCommonMethods.SessionType.SER_EXCEL.getValue(), jsonResponse, arg, searchProductsArray);
                 }
               }
               else{
                 hideProgressDialog();
                 arg.putString(ParamConstants.SEARCH_LIST_ID, SharedPrefManager.getString(ParamConstants.SEARCH_LIST_ID,serListId));
                 arg.putString(ParamConstants.SEARCH_LIST_TYPE, SharedPrefManager.getString(ParamConstants.SEARCH_LIST_TYPE,serListType));
                 loadFragment(new SearchListExcelFragment(), arg);
               }
              }
              else
                callInsertProductDBTask(url, AppCommonMethods.SessionType.SER_EXCEL.getValue(), jsonResponse, arg, searchProductsArray);
            }
            else if(hasExistingSearchListData){
              hideProgressDialog();
              arg.putString(ParamConstants.SEARCH_LIST_ID, SharedPrefManager.getString(ParamConstants.SEARCH_LIST_ID,serListId));
              arg.putString(ParamConstants.SEARCH_LIST_TYPE, SharedPrefManager.getString(ParamConstants.SEARCH_LIST_TYPE,serListType));
              loadFragment(new SearchListExcelFragment(), args);
            }
            else hideProgressDialog();
          }
          //Redirect to existing if data is available
          else if(hasExistingSearchListData){
            hideProgressDialog();
            loadFragment(new SearchListExcelFragment(), args);
          }
          break;
        case URLConstants.GET_ASSORTMENT_LIST:
          if(isSuccess && jsonResponse != null){
            final JSONArray assortmentCodesArray = extractJSONArray(jsonResponse, ParamConstants.DATA, extractJSONArray(jsonResponse, ParamConstants.SEARCH_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS)));
            if(isNonEmpty(assortmentCodesArray)){
              final ProductDao productDao = AppDatabase.getProductDao(this);
              productDao.deleteAll(AppCommonMethods.SessionType.SEARCH_ASSORTMENT.getValue());
              AppDatabase.getInventoryDao(this).deleteAllExcept();
              int insertCount = 0;
              showProgressDialog(getString(R.string.progress_msg_check_data));
              for(int a = 0; a < assortmentCodesArray.length(); a++){
                final JSONObject assortment = assortmentCodesArray.getJSONObject(a);
                final String code = extractString(assortment, ParamConstants.ASSORTMENT_CODE, "");
                final int priority = extractInt(assortment, ParamConstants.ASSORTMENT_PRIORITY, 0);
                JSONArray searchProductsArray = extractJSONArray(assortment, ParamConstants.ITEMS, extractJSONArray(assortment, ParamConstants.SEARCH_PRODUCTS, extractJSONArray(assortment, ParamConstants.PRODUCTS)));
                if(isNonEmpty(searchProductsArray))
                  for(int i = 0; i < searchProductsArray.length(); i++){
                    final JSONObject product = searchProductsArray.getJSONObject(i);
                    final ProductModel productModel = product != null ? getGSON().fromJson(product.toString(), ProductModel.class) : null;
                    if(productModel != null){
                      productModel.setOrderNo(code);
                      productModel.setPriority(priority);
                      productModel.setSessionType(AppCommonMethods.SessionType.SEARCH_ASSORTMENT.getValue());
                      productModel.setItemImgUrl(extractString(product, ParamConstants.IMG_URL, extractString(product, ParamConstants.IMAGE, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim()));
                      final JSONArray jsonZones = extractJSONArray(product, ParamConstants.ZONES);
                      if(jsonZones != null && jsonZones.length() > 0){
                        int totalQty = 0;
                        for(int j = 0; j < jsonZones.length(); j++){
                          JSONObject zone = jsonZones.getJSONObject(j);
                          final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                          final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, extractInt(zone, ParamConstants.QTY, 0));
                          String zoneName = extractString(zone, ParamConstants.ZONE_NAME, extractString(zone, ParamConstants.ZONE, Integer.parseInt(chkZero(zoneId, "0")) > 0 && chkNull(eanQty, 0) > 0 ? AppDatabase.getZoneDao(MainActivity.this).getZoneNameById(zoneId) : ""));
                          if(isNullOrEmpty(zoneName) && Integer.parseInt(zoneId) > 0 && chkNull(eanQty, 0) > 0)
                            zoneName = AppDatabase.getZoneDao(this).getZoneNameById(zoneId);
                          
                          if(isNonEmpty(zoneName) && chkNull(eanQty, 0) > 0){
                            productModel.setZone(zoneName);
                            productModel.setZoneId(zoneId);
                            productModel.setEanQty(eanQty);
                            totalQty += chkNull(eanQty, 0);
                            productDao.insert(productModel);
                            insertCount++;
                          }
                        }
                        if(insertCount > 0 && totalQty > 0){
                          if(!isUseNewUIForLBS && totalQty < productModel.getQty()){
                            final String zoneName = AppConstants.DEFAULT_NO_VALUE;
                            final String zoneId = "0";
                            final Integer eanQty = productModel.getQty() - totalQty;
                            productModel.setZone(zoneName);
                            productModel.setZoneId(zoneId);
                            productModel.setEanQty(eanQty);
                            totalQty += chkNull(eanQty, 0);
                            productDao.insert(productModel);
                            insertCount++;
                          }
                          productDao.updateTotalQty(productModel.ean, totalQty, productModel.getSessionType());
                        }
                      }
                      else{
                        productModel.eanQty = productModel.qty;
                        productModel.totalQty = productModel.qty;
                        productDao.insert(productModel);
                        insertCount++;
                      }
                    }
                  }
              }
              if(insertCount > 0){
                SharedPrefManager.setInt(ParamConstants.MARK_FOUND_PERCENT_SER_ASSORTMENT, extractInt(jsonResponse, ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, AppCommonMethods.markFoundPercentAssortmentSearch));
                //Bundle bundle = args != null ? args : new Bundle();
                args.putString(AppConstants.SEARCH_LIST_ID, extractString(jsonRequest, ParamConstants.SEARCH_LIST_ID, extractString(jsonResponse, ParamConstants.SEARCH_LIST_ID, extractString(jsonResponse, ParamConstants.SEARCH_LIST_NAME, extractString(jsonResponse, ParamConstants.NAME, extractString(jsonResponse, ParamConstants.CODE, extractString(jsonResponse, ParamConstants.ORDER_NO, "0")))))));
                args.putString(AppConstants.SEARCH_LIST_TYPE, extractString(jsonRequest, ParamConstants.SEARCH_LIST_TYPE, extractString(jsonResponse, ParamConstants.SEARCH_LIST_TYPE, "Assortment")));
                loadFragment(new SearchAssortMainFragment(), args);
              }
              hideProgressDialog();
            }
            else{
              final String errorMsg = extractString(jsonResponse, ParamConstants.ERROR, "");
              if(isNonEmpty(errorMsg)) showCustomErrDialog(errorMsg);
              hideProgressDialog();
            }
          }
          break;
        case URLConstants.GET_SUPPLY_CHAIN_TYPES:
          if(isSuccess && jsonResponse != null){
            JSONObject jsonObjSiteTypes = extractJSONObject(jsonResponse, ParamConstants.DATA, extractJSONObject(jsonResponse, ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_LIST, extractJSONObject(jsonResponse, ParamConstants.SUPPLY_CHAIN_TYPE_LIST)));
            JSONArray jsonArraySiteTypes = jsonObjSiteTypes != null ? null : extractJSONArray(jsonResponse, ParamConstants.DATA, extractJSONArray(jsonResponse, ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_LIST, extractJSONArray(jsonResponse, ParamConstants.SUPPLY_CHAIN_TYPE_LIST)));
            List<SiteType> listSiteTypes = new ArrayList<>(0);
            if(isNonEmpty(jsonArraySiteTypes)){
              for(int i = 0; i < jsonArraySiteTypes.length(); i++){
                Object obj = jsonArraySiteTypes.get(i);
                SiteType siteType = null;
                if(obj != null && obj instanceof JSONObject){
                  siteType = getGSON().fromJson(obj.toString(), SiteType.class);
                }
                else if(obj != null && obj instanceof String && isNonEmpty(obj.toString())){
                  siteType = new SiteType((long) i, obj.toString());
                }
                if(siteType != null) listSiteTypes.add(siteType);
              }
            }
            if(isNonEmpty(jsonObjSiteTypes)){
              for(Iterator<String> it = jsonObjSiteTypes.keys(); it.hasNext(); ){
                final String key = it.next();
                if(isNonEmpty(key) && key.matches("^[0-9]+$")){
                  final Long id = Long.parseLong(key);
                  final String val = jsonObjSiteTypes.getString(key);
                  if(isNonEmpty(val)) listSiteTypes.add(new SiteType(id, val));
                }
              }
            }
            if(isNonEmpty(listSiteTypes)){
              AppDatabase.getSiteTypeDao(this).insertAll(listSiteTypes);//Note: can be store in SharedPref also
              //TODO save this using backend coroutine
              List<SiteCode> listSiteTypeCodes = new ArrayList<>(0);
              final JSONObject jsonSiteTypeWiseCodeList = extractJSONObject(jsonResponse, ParamConstants.STORE_LIST);
              if(isNonEmpty(jsonSiteTypeWiseCodeList)){
                for(Iterator<String> it = jsonSiteTypeWiseCodeList.keys(); it.hasNext(); ){
                  final String key = it.next();
                  if(isNonEmpty(key) && key.matches("^[0-9]+$")){
                    final Long id = Long.parseLong(key);
                    final JSONArray siteCodes = jsonSiteTypeWiseCodeList.getJSONArray(key);
                    if(isNonEmpty(siteCodes)){
                      for(int i = 0; i < siteCodes.length(); i++){
                        Object obj = siteCodes.get(i);
                        SiteCode siteCode = null;
                        if(obj != null && obj instanceof JSONObject){
                          siteCode = getGSON().fromJson(obj.toString(), SiteCode.class);
                          if(siteCode != null && siteCode.getSiteTypeId() <= 0)
                            siteCode.setSiteTypeId(id);
                        }
                        else if(obj != null && obj instanceof String && isNonEmpty(obj.toString())){
                          siteCode = new SiteCode(id, obj.toString());
                        }
                        if(siteCode != null) listSiteTypeCodes.add(siteCode);
                      }
                    }
                  }
                }
              }
              //TODO insert storeCodes in List/DB table based in id from listSiteType
              if(isNonEmpty(listSiteTypeCodes))
                AppDatabase.getSiteCodeDao(this).insertAll(listSiteTypeCodes);
            }
            allowBtnClick = true;
            callWebService(URLConstants.GET_IO_CONFIGURATION, jsonRequest, args, getString(R.string.progress_msg_getting_data), true);
          }
          break;
        case URLConstants.GET_IO_CONFIGURATION:
          if(isSuccess && jsonResponse != null){
            final JSONObject config = extractJSONObject(jsonResponse, ParamConstants.CONFIG, jsonResponse);
            saveIOConfig(config);
            allowBtnClick = true;
          }
          callWebService(URLConstants.GET_TRIPS_DATA, jsonRequest, args, getString(R.string.progress_msg_getting_data), false, true);
          break;
        case URLConstants.GET_INWARD_TRIP_DATA:
        case URLConstants.GET_TRIPS_DATA:
          if(isSuccess && jsonResponse != null){
            final JSONObject config = extractJSONObject(jsonResponse, ParamConstants.CONFIG, jsonResponse);
            final String typeIO = extractString(config, ParamConstants.TYPE, extractString(config, ParamConstants.OPERATION_TYPE, extractString(jsonResponse, ParamConstants.TYPE, extractString(jsonResponse, ParamConstants.OPERATION_TYPE, extractString(jsonRequest, ParamConstants.TYPE, extractString(jsonRequest, ParamConstants.OPERATION_TYPE, extractString(args, ParamConstants.TYPE, AppConstants.INWARD)))))));
            saveIOConfig(config);
            JSONArray tripDataArray = extractJSONArray(jsonResponse, ParamConstants.K_TRIPS_DATA, extractJSONArray(jsonResponse, ParamConstants.DATA));
            if(isNonEmpty(tripDataArray))
              new InsertDBTrips(this, url, typeIO, jsonResponse, args).execute(tripDataArray);
            else{
              hideProgressDialog();
              AppDatabase.getTripStatusDao(this).deleteAllTripStatus(typeIO);
              AppDatabase.getTripInventoryDao(this).deleteAllTripInventory();
              loadFragment(new TripListFragment(), args);
            }
          }
          break;
        case URLConstants.GET_OUTWARD_TRIP_DATA:
          if(isSuccess && jsonResponse != null){
            JSONArray tripDataArray = jsonResponse.getJSONArray(ParamConstants.K_TRIPS_DATA);
            if(tripDataArray != null)
              //new InsertTripDBTask(url, extractString(jsonResponse, ParamConstants.TYPE, AppConstants.OUTWARD), jsonResponse, args).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tripDataArray);
              new InsertDBTrips(this, url, extractString(jsonResponse, ParamConstants.TYPE, AppConstants.OUTWARD), jsonResponse, args).execute(tripDataArray);
            /*else if(tripDataArray != null && tripDataArray.length() > 0){
              final TripStatusDao tripStatusDao = AppDatabase.getDbInstance(this).TripStatusDao();
              final TripInventoryDao tripInventoryDao = AppDatabase.getDbInstance(this).TripInventoryDao();
              tripStatusDao.deleteAllTripStatus(AppConstants.OUTWARD);
              tripInventoryDao.deleteAllTripInventory();
              int tripStatusCount = 0, tripInvCount = 0;
              for(int i = 0; i < tripDataArray.length(); i++){
                final JSONObject trip = tripDataArray.getJSONObject(i);
                if(trip != null){
                  TripStatus tripStatus = new Gson().fromJson(trip.toString(), TripStatus.class);
                  if(tripStatus != null){
                    JSONArray tripDtlsArray = extractJSONArray(trip, ParamConstants.K_HU_DATA);
                    if(isNonEmpty(tripDtlsArray)){
                      for(int j = 0; j < tripDtlsArray.length(); j++){
                        TripInventory tripInventory = new Gson().fromJson(tripDtlsArray.getJSONObject(j).toString(), TripInventory.class);
                        if(tripInventory != null){
                          tripInventoryDao.insertTripInventoryData(tripInventory);
                          tripInvCount++;
                        }
                      }
                    }
                    tripStatusDao.insertTripStatusData(tripStatus);
                    tripStatusCount++;
                  }
                }
              }
              if(tripStatusCount > 0 && tripInvCount > 0){
                loadFragment(new OutwardHuDataFragment());
              }
              hideProgressDialog();
            }*/
            else hideProgressDialog();
          }
          break;
        case URLConstants.GET_PRODUCT_INFO:
        case URLConstants.GET_PRODUCT_INFO_BY_SKU:
          ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
          if(productModel != null){
            if(isStaticDebug())
              productModel.setEan(extractString(jsonRequest, ParamConstants.EAN, ""));
            if(args == null) args = new Bundle();
            args.putSerializable(productModel.getClass().getSimpleName(), productModel);
            popBackStack();
            loadFragment(new ProductDetailsFragment(), args);
          }
          break;
        case URLConstants.GET_OFF_RANGE_PRODUCTS:
          if(isSuccess && jsonResponse != null){
            final JSONObject data = extractJSONObject(jsonResponse, ParamConstants.DATA, jsonResponse);
            final String listRefBatchId = extractString(data, ParamConstants.LIST_REF_BATCH_ID, extractString(data, ParamConstants.LIST_BATCH_ID, extractString(data, ParamConstants.LIST_ID, "")));
            final Long listExpectedQty = extractLong(data, ParamConstants.EXPECTED_QTY, extractLong(data, ParamConstants.EXP_QTY, 0l));
            if(args == null) args = new Bundle();
            args.putLong(ParamConstants.EXPECTED_QTY, listExpectedQty);
            args.putString(ParamConstants.LIST_REF_BATCH_ID, listRefBatchId);
            JSONArray outwardProductsArray = extractJSONArray(data, ParamConstants.OFF_RANGE_PRODUCTS, extractJSONArray(data, ParamConstants.OUTWARD_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS)));
            if(isNonEmpty(outwardProductsArray)){
              callInsertProductDBTask(url, AppCommonMethods.SessionType.OFF_RANGE.getValue(), jsonResponse, args, outwardProductsArray);
            }
            else{
              hideProgressDialog();
              if(AppDatabase.getProductDao(this).getAllTotal(AppCommonMethods.SessionType.OFF_RANGE.getValue()) > 0 && getLastPendingOutwardBatchId(new String[]{"Range", "OffRange", "OFRNG"}))
                loadFragment(new OffRangeMainFragment(), args);
            }
          }
          else{
            hideProgressDialog();
            if(AppDatabase.getProductDao(this).getAllTotal(AppCommonMethods.SessionType.OFF_RANGE.getValue()) > 0 && getLastPendingOutwardBatchId(new String[]{"Range", "OffRange", "OFRNG"}))
              loadFragment(new OffRangeMainFragment(), args);
          }
          break;
        case URLConstants.LOGOUT:
          clearSavedDataOnLogout();
          break;
        case URLConstants.GET_DECODE_TYPES:
          if(isSuccess){
            final JSONArray decodeTypes = extractJSONArray(jsonResponse, ParamConstants.DECODE_TYPES);
            if(isNonEmpty(decodeTypes)){
              ArrayList<DecodeType> listDecodingTypes = new ArrayList<>(0);
              for(int i = 0; i < decodeTypes.length(); i++){
                DecodeType decodeType = getGSON().fromJson(decodeTypes.getJSONObject(i).toString(), DecodeType.class);
                if(decodeType != null && isNonEmpty(decodeType.type))
                  listDecodingTypes.add(decodeType);
              }
              if(isNonEmpty(listDecodingTypes))
                SharedPrefManager.setArrayList(ParamConstants.DECODE_TYPES, listDecodingTypes);
              
              //To be used if single API is called for both Decode Types & Passwords
              //              final Bundle arguments = args;
              //              final JSONObject jsonAccessPassword = extractJSONObject(jsonResponse, ParamConstants.ACCESS_PASSWORD, jsonResponse);
              //              JSONArray oldPasswords = extractJSONArray(jsonAccessPassword, OLD_ACCESS_PASSWORDS, extractJSONArray(jsonResponse, OLD_ACCESS_PASSWORDS, extractJSONArray(jsonAccessPassword, OLD_ACCESS_PWDS, extractJSONArray(jsonResponse, OLD_ACCESS_PWDS))));
              //              if(isNonEmpty(oldPasswords)){
              //                new Handler().post(new Runnable(){
              //                  @Override
              //                  public void run(){
              //                    saveTagWritePasswords(jsonResponse);
              //                    if(arguments != null && arguments.containsKey(ParamConstants.IS_ALLOW_DECODE) && arguments.getBoolean(ParamConstants.IS_ALLOW_DECODE, false)){
              //                      if(isShowDecodeTypeSelectionDialog && isNonEmpty(SharedPrefManager.getArrayList(ParamConstants.DECODE_TYPES))){
              //                        showDecodeTypeAlert(arguments);
              //                      }
              //                      else loadFragment(new DecodingStartFragment(), arguments);
              //                    }
              //                  }
              //                });
              //              }
              //              else
              //                callWebService(URLConstants.GET_ACCESS_PWD, new JSONObject(), args, getString(R.string.progress_msg_getting_data));
            }
          }
          else if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_DECODE_TYPE_SELECTION_DIALOG, AppCommonMethods.isShowDecodeTypeSelectionDialog) && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_STATIC_DECODE_TYPES_FOR_SELECTION_IF_API_FAILS, AppCommonMethods.isShowStaticDecodeTypesForSelectionIfApiFails)){
            handleResponse(url, jsonRequest, AppCommonMethods.getSampleJSON(this, url), 200, true, args);
            return;
          }
          if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_DECODE_TYPE_SELECTION_DIALOG, AppCommonMethods.isShowDecodeTypeSelectionDialog) && isNonEmpty(SharedPrefManager.getArrayList(ParamConstants.DECODE_TYPES)))
            showDecodeTypeAlert(args);
          else if(!SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_DECODE_TYPE_SELECTION_DIALOG, AppCommonMethods.isShowDecodeTypeSelectionDialog))
            loadFragment(new DecodingStartFragment(), args);
          break;
        case URLConstants.GET_ACCESS_PWD:
          if(isSuccess){
            final Bundle arguments = args;
            new Handler().post(new Runnable(){
              @Override
              public void run(){
                saveTagWritePasswords(jsonResponse);
                if(arguments != null && arguments.containsKey(ParamConstants.IS_ALLOW_DECODE) && arguments.getBoolean(ParamConstants.IS_ALLOW_DECODE, false)){
                  if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_DECODE_TYPE_SELECTION_DIALOG, AppCommonMethods.isShowDecodeTypeSelectionDialog) && (isNullOrEmpty(SharedPrefManager.getArrayList(ParamConstants.DECODE_TYPES)) || !SharedPrefManager.getBoolean(ParamConstants.DECODE_TYPES + "_" + ParamConstants.IS_SAVED_FROM_LOGIN, false))){
                    callWebService(URLConstants.GET_DECODE_TYPES, new JSONObject(), arguments, getString(R.string.progress_msg_getting_data), true);//isNonEmpty(SharedPrefManager.getArrayList(ParamConstants.DECODE_TYPES)));
                  }
                  else if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_DECODE_TYPE_SELECTION_DIALOG, AppCommonMethods.isShowDecodeTypeSelectionDialog) && isNonEmpty(SharedPrefManager.getArrayList(ParamConstants.DECODE_TYPES)))
                    showDecodeTypeAlert(arguments);
                  else if(!SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_DECODE_TYPE_SELECTION_DIALOG, AppCommonMethods.isShowDecodeTypeSelectionDialog))
                    loadFragment(new DecodingStartFragment(), arguments);
                }
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
  
  private boolean getLastPendingOutwardBatchId(final String[] keys){
    OutwardBatch ob = null;
    if(keys != null && keys.length > 0) for(String key : keys){
      ob = AppDatabase.getOutwardBatchDao(this).getLastPendingOutwardBatchByKeys(key.trim());
      if(ob != null) break;
    }
    return ob != null && ob.getAcceptedCartons() > 0;
  }
  
  public void callInsertProductDBTask(String url, int sessionType, JSONObject jsonResponse, Bundle args, JSONArray jsonArrayProducts){
    //new InsertProductDBTask(url, sessionType, jsonResponse, args).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jsonArrayProducts);
    new InsertDBProducts(this, url, sessionType, jsonResponse, args).execute(jsonArrayProducts);
  }
  
  public void callInsertProductDBTask(CommonFragment fragment, String url, int sessionType, JSONObject jsonRequest, JSONObject jsonResponse, Bundle args, JSONArray jsonArrayProducts){
    //new InsertProductDBTask(fragment, url, sessionType, jsonRequest, jsonResponse, args).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jsonArrayProducts);
    new InsertDBProducts(this, fragment, url, sessionType, jsonRequest, jsonResponse, args).execute(jsonArrayProducts);
  }
  
  public void callInsertBrandwiseDBTask(CommonFragment fragment, String url, JSONObject jsonRequest, JSONObject jsonResponse, Bundle args, JSONArray jsonArrayBrandwise){
    //new InsertBrandwiseDBTask(fragment, url, jsonRequest, jsonResponse, args).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jsonArrayBrandwise);
    new InsertDBBrandZones(this, fragment, url, jsonRequest, jsonResponse, args).execute(jsonArrayBrandwise);
  }
  
  public void callUploadInventory(final MenuModel menuModel, final RFIDSession sessionObject){
    if(sessionObject == null || menuModel == null) return;
    if(isInternetConnected(this, false, true)){
      allowBtnClick = false;
      try{
        showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.SESSION_TYPE, sessionObject.sessionType);
        requestParams.put(ParamConstants.TYPE, getInvType(sessionObject.sessionType));
        final String selZone = chkNull(sessionObject.zone, AppConstants.ALL);
        if(selZone.equalsIgnoreCase(AppConstants.ALL)){
          requestParams.put(ParamConstants.ZONE_ID, 0);
          requestParams.put(ParamConstants.ZONE, selZone);
        }
        else{
          List<Zone> listZones = AppDatabase.getZoneDao(this).getZoneByName(chkNull(sessionObject.zone, AppConstants.ALL));
          if(isNonEmpty(listZones) && listZones.size() == 1){
            Zone zone = listZones.get(0);
            requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
            requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : chkNull(sessionObject.zone, AppConstants.ALL));
            requestParams.put(ParamConstants.ZONE_TYPE, zone != null ? zone.zoneType : null);
            requestParams.put(ParamConstants.IS_DEFAULT_ZONE, zone != null ? zone.isDefault : false);
          }
          else{
            JSONArray zones = new JSONArray();
            for(Zone zone : listZones){
              if(zone != null){
                JSONObject jsonZone = zone.toJson();
                if(jsonZone != null){
                  zones.put(jsonZone);
                }
              }
            }
            if(zones != null && zones.length() > 0) requestParams.put(ParamConstants.ZONES, zones);
          }
        }
        requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD);
        requestParams.put(ParamConstants.STATUS, AppConstants.SESSION_ACTION_STOP);
        requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
        new Handler().post(new Runnable(){
          @Override
          public void run(){
            try{
              JSONArray js = new JSONArray();
              List<Inventory> dataList = AppDatabase.getInventoryDao(MainActivity.this).getAllInventoryData(sessionObject.sessionId);
              if(isNonEmpty(dataList)) for(Inventory inventory : dataList){
                JSONObject dataobject = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_INVENTORY_UPLOAD) ? inventory.toOnlyEpcJson() : inventory.toJson(MainActivity.this);
                if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                  js.put(dataobject);
              }
              requestParams.put(ParamConstants.ITEMS, js);
              allowBtnClick = true;
              Bundle args = new Bundle();
              args.putParcelable(sessionObject.getClass().getSimpleName(), (Parcelable) sessionObject);
              args.putParcelable(menuModel.getClass().getSimpleName(), (Parcelable) menuModel);
              callWebService(URLConstants.UPLOAD_INVENTORY, requestParams, args, getString(R.string.progress_msg_uploading_data), false);
            }
            catch(Exception e){
              e.printStackTrace();
              hideProgressDialog();
              allowBtnClick = true;
            }
          }
        });
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
        allowBtnClick = true;
      }
    }
  }
  
  final CountDownTimer notifyBlinkTimer = new CountDownTimer(2500, 500){
    @Override
    public void onTick(long millisUntilFinished){
      if(menu != null){
        final MenuItem notify = menu.findItem(R.id.act_notify);
        if(notify != null && notify.isEnabled()){
          boolean isFragSelected = getTopFragment() != null && getTopFragment().getMenuId(MainActivity.this) == notify.getItemId();
          if(notify.getActionView() != null && notify.getActionView().findViewById(R.id.imgMenuLogo) != null)
            ((ImageView) notify.getActionView().findViewById(R.id.imgMenuLogo)).setImageResource(millisUntilFinished % 2 == 0 ? R.drawable.ic_act_notify : R.drawable.ic_act_notify_dot);
          else
            notify.setIcon(millisUntilFinished % 2 == 0 ? isFragSelected ? R.drawable.ic_act_notify_sel : R.drawable.ic_act_notify : isFragSelected ? R.drawable.ic_act_notify_dot_sel : R.drawable.ic_act_notify_dot).getActionView();
        }
      }
    }
    
    @Override
    public void onFinish(){
      stopBlink();
    }
  };
  
}

