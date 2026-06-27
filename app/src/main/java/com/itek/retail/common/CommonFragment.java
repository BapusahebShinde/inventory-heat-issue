package com.itek.retail.common;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.errorBeep;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUploadSlider;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.TooltipCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.model.BrandWiseZoneInventory;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.SearchLog;
import com.itek.retail.ui.actionmenu.ActionMenuSearchFragment;
import com.itek.retail.ui.customviews.swipeButton.ProSwipeButtonVar;
import com.itek.retail.ui.encoding.EncodingMainFragment;
import com.itek.retail.ui.encoding.EncodingStartFragment;
import com.itek.retail.ui.encoding.EncodingVerifyFragment;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inventory.InventoryAddFragment;
import com.itek.retail.ui.inventory.InventoryBrandFragment;
import com.itek.retail.ui.inventory.InventoryMainFragment;
import com.itek.retail.ui.inventory.InventoryStartFragment;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionMainFragment;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionStartFragment;
import com.itek.retail.ui.inward.InwardMainFragment;
import com.itek.retail.ui.inward.grn.InwardGrnHuScanFragment;
import com.itek.retail.ui.inward.grn.InwardGrnStartFragment;
import com.itek.retail.ui.inward.grn.InwardGrnTripDetailsFragment;
import com.itek.retail.ui.inward.grn.InwardGrnTripsDataFragment;
import com.itek.retail.ui.inward.grn.InwardTripsFragment;
import com.itek.retail.ui.inward.huverification.InwardHuVerificationFragment;
import com.itek.retail.ui.landing.LandingActivity;
import com.itek.retail.ui.movement.MovementMainFragment;
import com.itek.retail.ui.movement.MovementStartFragment;
import com.itek.retail.ui.movement.replenishment.ReplenishmentListFragment;
import com.itek.retail.ui.movement.replenishment.ReplenishmentStartFragment;
import com.itek.retail.ui.outward.OutwardMainFragment;
import com.itek.retail.ui.outward.OutwardPickDataFragment;
import com.itek.retail.ui.outward.OutwardPickListDetailsFragment;
import com.itek.retail.ui.outward.OutwardPickListsFragment;
import com.itek.retail.ui.outward.OutwardPickStartFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuDataFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuDetailsFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuListFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuStartFragment;
import com.itek.retail.ui.replenishondemand.ReplenishmentArticleListFragment;
import com.itek.retail.ui.replenishondemand.ReplenishmentBatchListFragment;
import com.itek.retail.ui.replenishondemand.ReplenishmentEanStartFragment;
import com.itek.retail.ui.search.SearchMainFragment;
import com.itek.retail.ui.search.ageing.AgeingSearchListFragment;
import com.itek.retail.ui.search.ageing.AgeingSearchStartFragment;
import com.itek.retail.ui.search.alien.SearchAlienFragment;
import com.itek.retail.ui.search.assortment.SearchAssortListFragment;
import com.itek.retail.ui.search.assortment.SearchAssortMainFragment;
import com.itek.retail.ui.search.assortment.SearchAssortStartFragment;
import com.itek.retail.ui.search.listsearch.SearchListFragment;
import com.itek.retail.ui.search.listsearch.SearchListStartFragment;
import com.itek.retail.ui.search.listsearch.SearchListsFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelListDetailsFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelListFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelOrderStatsFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelStartFragment;
import com.itek.retail.ui.search.omnichannel.TabOmniChannelOrderStatsFragment;
import com.itek.retail.ui.search.productsearch.ProductSearchFragment;
import com.itek.retail.ui.search.unencoded.SearchUnencodedFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * The Common fragment is a parent fragment
 * which is extended by all fragments throughout the app
 * contains common constants/methods and common code implementation
 */
public class CommonFragment extends Fragment{
  
  public boolean isLandscape = false;
  protected CommonActivity context;
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    context = (CommonActivity) getActivity();
    insertAuditTrailsLog("Created");
    
    hideKeyboard();
    AppCommonMethods.allowBtnClick = true;
    isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    //isTablet = getResources().getBoolean(R.bool.isTablet);
    showLog("allowBtnClick", "" + allowBtnClick);
    final String name = this.getClass().getSimpleName().trim();
    //LogFileUtility.writeLog(context);
    LogFileUtilityHHD.writeLog(context);
    showLog(name, "\n_________________________" + name + "__________________________________________\n");
  }
  
  @Override
  public void onAttach(@NonNull Context context1){
    super.onAttach(context1);
    final Object contextObj = chkNull(context, context1);
    final CommonActivity ca = contextObj instanceof CommonActivity ? (CommonActivity) contextObj : null;
    if(ca != null) ca.selectMenuItem(getMenuId(ca), true);
    hideKeyboard();
  }
  //public boolean isTablet=false;
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    showLog("onCreateView", this.getClass().getSimpleName());
    insertAuditTrailsLog("CreateView");
    return super.onCreateView(inflater, container, savedInstanceState);
  }
  
  @Override
  public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    showLog("onViewCreated", this.getClass().getSimpleName());
    view.setBackgroundResource(R.color.white);
    view.setClickable(true);
    final ViewGroup llHeader = view.findViewById(R.id.header);
    final ViewGroup llSubHeader = view.findViewById(R.id.ll_sub_header);
    if(llHeader != null){
      llHeader.findViewById(R.id.img_back).setOnClickListener(v -> context.onBackPressed());
      final MenuModel menuModel = getMenuModel();
      if(getArguments() != null || menuModel != null){
        final String title = menuModel != null ? menuModel.getScreenMenuName() : extractString(getArguments(), AppConstants.TITLE, "");
        final String logoURL = menuModel != null ? menuModel.getScreenImageUrl() : extractString(getArguments(), AppConstants.TITLE_LOGO_URL, "");
        final int logoId = menuModel != null ? chkZero(menuModel.getScreenIconId(), getResources().getIdentifier(menuModel.getScreenMenuIconName(), AppConstants.RES_DRAWABLE, context.getPackageName())) : extractInt(getArguments(), AppConstants.TITLE_LOGO_RES_ID, 0);
        if(isNonEmpty(title)){
          ((TextView) llHeader.findViewById(R.id.txt_title)).setText(title);
          ((TextView) llHeader.findViewById(R.id.txt_title)).setSelected(true);
        }
        if(isNonEmpty(logoURL) || chkNull(logoId, 0) > 0)
          context.loadImage((ImageView) llHeader.findViewById(R.id.img_title_logo), logoURL, logoId);
        if(menuModel != null && llSubHeader != null){
          final List<MenuModel> listSubMenus = AppDatabase.getMenuDao(context).getSubMenus(menuModel.getMenuId());
          llSubHeader.setVisibility(isNonEmpty(listSubMenus) ? View.VISIBLE : View.GONE);
          if(isNonEmpty(listSubMenus)){
            llSubHeader.removeAllViews();
            final LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            llParams.gravity = Gravity.CENTER;
            final FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            flParams.gravity = Gravity.CENTER;
            for(MenuModel subMenu : listSubMenus){
              final View root = context.getLayoutInflater().inflate(R.layout.view_menu, null, false);
              root.setLayoutParams(llParams);
              ImageView imgSlash = root.findViewById(R.id.imgMenuSlash);
              imgSlash.setVisibility(!menuModel.getIsEnabled() || !subMenu.getIsEnabled() ? View.VISIBLE : View.GONE);
              ImageView imgLogo = root.findViewById(R.id.imgMenuLogo);
              root.setTag(subMenu.menuCode);
              TooltipCompat.setTooltipText(root, subMenu.getMenuName());
              context.loadImage(imgLogo, subMenu.imageUrl, chkZero(subMenu.getIconId(), getResources().getIdentifier(subMenu.getMenuIconName(), AppConstants.RES_DRAWABLE, context.getPackageName())));
              if(isNullOrEmpty(menuModel.getImageUrl()))
                imgLogo.setColorFilter(ContextCompat.getColor(context, !menuModel.getIsEnabled() || !subMenu.getIsEnabled() ? R.color.colorDisabled : R.color.transparent), PorterDuff.Mode.SRC_ATOP);
              root.setOnClickListener(view1 -> {
                if((!menuModel.getIsEnabled() || !subMenu.getIsEnabled())){
                  context.showShortToast(subMenu.getErrEnabledMsg(context));
                  errorBeep();
                }
                else{
                  Bundle b = new Bundle();
                  if(CommonFragment.this instanceof MovementMainFragment)
                    b.putString(AppConstants.REPLENISHMENT_TYPE, ((MovementMainFragment) CommonFragment.this).getReplenishmentType());
                  if(CommonFragment.this instanceof OmniChannelFragment)
                    b.putString(AppConstants.OMNICHANNEL_TYPE, ((OmniChannelFragment) CommonFragment.this).getOmnichannelType());
                  handleFragmentRedirection(subMenu, b);
                }
              });
              llSubHeader.addView(root);
            }
          }
        }
      }
    }
  }
  
  /**
   * Set upload constraints.
   *
   * @param root       the root
   * @param btnCloud   the btn cloud
   * @param btnSwipeUp the btn swipe up
   */
  public void setUploadConstraints(final ConstraintLayout root, final FloatingActionButton btnCloud, final ProSwipeButtonVar btnSwipeUp){
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(root);
    constraintSet.clear(btnCloud.getId(), isUploadSlider ? ConstraintSet.BOTTOM : ConstraintSet.TOP);
    constraintSet.connect(btnCloud.getId(), isUploadSlider ? ConstraintSet.TOP : ConstraintSet.BOTTOM, isUploadSlider ? btnSwipeUp.getId() : ConstraintSet.PARENT_ID, isUploadSlider ? ConstraintSet.TOP : ConstraintSet.BOTTOM);
    constraintSet.applyTo(root);
  }
  
  /**
   * Hide keyboard.
   */
  public void hideKeyboard(){ AppCommonMethods.hideKeyboard(context); }
  
  /**
   * Insert search log.
   *
   * @param ean  the ean
   * @param type the type
   */
  public SearchLog insertSearchLog(String sessionId, String ean, Integer eanQty, String type){
    return context.insertSearchLog(sessionId, ean, eanQty, type);
  }
  
  /**
   * Insert search log.
   *
   * @param ean     the ean
   * @param type    the type
   * @param subType the sub type
   */
  public SearchLog insertSearchLog(String sessionId, String ean, Integer eanQty, String type, String subType, JSONObject params){
    return context.insertSearchLog(sessionId, ean, eanQty, type, subType, params);
  }
  
  /**
   * Insert search log.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @param eanQty    the ean qty
   * @param type      the type
   * @param subType   the sub type
   * @param typeId    the type id
   */
  public SearchLog insertSearchLog(String sessionId, String ean, Integer eanQty, String type, String subType, String typeId, JSONObject params){
    return context.insertSearchLog(sessionId, ean, eanQty, type, subType, typeId, params);
  }
  
  /**
   * Insert search log.
   *
   * @param searchLog the search log
   */
  public SearchLog insertSearchLog(SearchLog searchLog){
    return context.insertSearchLog(searchLog);
  }
  
  /**
   * Show log.
   *
   * @param tag the tag
   * @param msg the msg
   */
  public void showLog(String tag, String msg){ context.showLog(tag, msg); }
  
  /**
   * Show short toast.
   *
   * @param res the res
   */
  public void showShortToast(int res){ context.showShortToast(res); }
  
  /**
   * Show long toast.
   *
   * @param res the res
   */
  public void showLongToast(int res){ context.showLongToast(res); }
  
  /**
   * Show short toast.
   *
   * @param msg the msg
   */
  public void showShortToast(String msg){ context.showShortToast(msg); }
  
  /**
   * Show long toast.
   *
   * @param msg the msg
   */
  public void showLongToast(String msg){ context.showLongToast(msg); }
  
  /**
   * Get base directory file.
   *
   * @param subDir the sub dir
   * @return the file
   */
  public File getBaseDirectory(final String subDir){
    return context.getBaseDirectory(subDir);
  }
  
  /**
   * Get uri from file uri.
   *
   * @param file the file
   * @return the uri
   */
  public Uri getUriFromFile(final File file){
    return context.getUriFromFile(file);
  }
  
  /**
   * Dp to px int.
   *
   * @param dp the dp
   * @return the int
   */
  public int dpToPx(int dp){
    DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }
  
  /**
   * Set alert dialog custom title.
   *
   * @param alertDialog the alert dialog
   * @param resId       the res id
   */
  public void setAlertDialogCustomTitle(final AlertDialog alertDialog, final int resId){
    context.setAlertDialogCustomTitle(alertDialog, chkNull(getString(resId), ""));
  }
  
  /**
   * Set alert dialog custom title.
   *
   * @param alertDialog    the alert dialog
   * @param resId          the res id
   * @param titleLogoResId the title logo res id
   */
  public void setAlertDialogCustomTitle(final AlertDialog alertDialog, final int resId, final int titleLogoResId){
    context.setAlertDialogCustomTitle(alertDialog, chkNull(getString(resId), ""), titleLogoResId);
  }
  
  /**
   * Set alert dialog custom title.
   *
   * @param alertDialog the alert dialog
   * @param title       the title
   */
  public void setAlertDialogCustomTitle(final AlertDialog alertDialog, final String title){
    context.setAlertDialogCustomTitle(alertDialog, title, 0);
  }
  
  /**
   * Set alert dialog custom title.
   *
   * @param alertDialog    the alert dialog
   * @param title          the title
   * @param titleLogoResId the title logo res id
   */
  public void setAlertDialogCustomTitle(final AlertDialog alertDialog, final String title, final int titleLogoResId){
    context.setAlertDialogCustomTitle(alertDialog, title, titleLogoResId);
    final View dialogTitleLayout = LayoutInflater.from(context).inflate(R.layout.dialog_cust_title, null);
    dialogTitleLayout.findViewById(R.id.btn_dialog_title_close).setOnClickListener(v -> alertDialog.dismiss());
    final ImageView imgLogo = dialogTitleLayout.findViewById(R.id.img_dialog_title_logo);
    final TextView txtTitle = dialogTitleLayout.findViewById(R.id.txt_dialog_title);
    txtTitle.setText(chkNull(title, ""));
    alertDialog.setCustomTitle(dialogTitleLayout);
    imgLogo.setVisibility(titleLogoResId > 0 ? View.VISIBLE : View.GONE);
    if(titleLogoResId > 0) imgLogo.setImageResource(titleLogoResId);
  }
  
  @Override
  public void onPause(){
    showLog("onPause", this.getClass().getSimpleName());
    insertAuditTrailsLog("Paused");
    super.onPause();
  }
  
  @Override
  public void onDestroy(){
    showLog("onDestroy", this.getClass().getSimpleName());
    insertAuditTrailsLog("Destroyed");
    super.onDestroy();
  }
  
  @Override
  public void onResume(){
    super.onResume();
    showLog("onResume", this.getClass().getSimpleName());
    insertAuditTrailsLog("Resumed");
    if(!isTopInStack()) return;
  }
  
  /**
   * Is top in stack boolean.
   *
   * @return the boolean
   */
  public boolean isTopInStack(){
    if(context instanceof MainActivity && isNonEmpty(((MainActivity) context).currentFragmentClassName))
      return ((MainActivity) context).currentFragmentClassName.equalsIgnoreCase(this.getClass().getSimpleName());
    else{
      final String name = context != null && context.getSupportFragmentManager() != null && context.getSupportFragmentManager().getBackStackEntryCount() > 0 ? context.getSupportFragmentManager().getBackStackEntryAt(context.getSupportFragmentManager().getBackStackEntryCount() - 1).getName() : "";
      return chkNull(name, "").equalsIgnoreCase(this.getClass().getSimpleName());
    }
  }
  
  /**
   * Show progress dialog.
   *
   * @param messageResId the message res id
   */
  public void showProgressDialog(@StringRes int messageResId){
    context.showProgressDialog(messageResId);
  }
  
  /**
   * Show progress dialog.
   *
   * @param message the message
   */
  public void showProgressDialog(String message){
    context.showProgressDialog(message);
  }
  
  /**
   * Hide progress dialog.
   */
  public void hideProgressDialog(){
    context.hideProgressDialog();
  }
  
  protected void insertAuditTrailsLog(String action){
    context.insertAuditTrailsLog(action, CommonFragment.this.getClass().getSimpleName());
  }
  
  protected void insertAuditTrailsLog(String action, String data){
    context.insertAuditTrailsLog(action, CommonFragment.this.getClass().getSimpleName(), data);
  }
  
  @Override
  public void onSaveInstanceState(@NonNull Bundle outState){
    super.onSaveInstanceState(outState);
    showLog("onViewStateRestored", this.getClass().getSimpleName());
  }
  
  @Override
  public void onViewStateRestored(@Nullable Bundle savedInstanceState){
    showLog("onViewStateRestored", this.getClass().getSimpleName());
    super.onViewStateRestored(savedInstanceState);
  }
  
  /**
   * Update lists.
   */
  public void updateLists(){
    /*
     * This method will be called in child fragments*/
  }
  
  /**
   * Update lists.
   *
   * @param replacePos the replace pos
   * @param isRemove   the is remove
   * @param itemModel  the item model
   */
  public void updateLists(int replacePos, boolean isRemove, final MenuModel itemModel){
    /*
     * This method will be called in child fragments*/
  }
  
  @Override
  public void onDetach(){
    showLog("onDetach", this.getClass().getSimpleName());
    insertAuditTrailsLog("Detached");
    if(context != null) context.selectMenuItem(getMenuId(context), false);
    super.onDetach();
  }
  
  /**
   * Gets menu id.
   *
   * @param context the context
   * @return the menu id
   */
  public int getMenuId(final Context context){
    final String menuCode = getMenuCode();
    return extractInt(getArguments(), AppConstants.MENU_ICON_ID, isNonEmpty(menuCode) && context != null ? getResources().getIdentifier(menuCode.replaceFirst("(?i)TOP_", "ACT_").toLowerCase(), AppConstants.RES_ID, context.getPackageName()) : 0);
  }
  
  /**
   * On key down.
   */
  public void onKeyDown(){
    /*
     * This method will be called in child fragments*/
  }
  
  /**
   * Call web service.
   *
   * @param url the url
   */
  public void callWebService(String url){
    callWebService(url, "");
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param progressMsg the progress msg
   */
  public void callWebService(String url, final String progressMsg){
    callWebService(url, new JSONObject(), null, progressMsg);
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param args        the args
   * @param progressMsg the progress msg
   */
  public void callWebService(String url, final Bundle args, final String progressMsg){
    callWebService(url, new JSONObject(), args, false, progressMsg);
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param jsonRequest the json request
   */
  public void callWebService(String url, JSONObject jsonRequest){
    callWebService(url, jsonRequest, "");
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param jsonRequest the json request
   * @param progressMsg the progress msg
   */
  public void callWebService(String url, JSONObject jsonRequest, final String progressMsg){
    callWebService(url, jsonRequest, null, progressMsg);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(String url, JSONObject jsonRequest, final String progressMsg, final boolean isOfflineProcess){
    callWebService(url, jsonRequest, null, false, progressMsg, isOfflineProcess, false);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(String url, JSONObject jsonRequest, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    callWebService(url, jsonRequest, null, false, progressMsg, isOfflineProcess, isDBProcess);
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param jsonRequest the json request
   * @param isRetry     the is retry
   * @param progressMsg the progress msg
   */
  public void callWebService(String url, JSONObject jsonRequest, final boolean isRetry, final String progressMsg){
    callWebService(url, jsonRequest, null, isRetry, progressMsg);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param isRetry          the is retry
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(String url, JSONObject jsonRequest, final boolean isRetry, final String progressMsg, final boolean isOfflineProcess){
    callWebService(url, jsonRequest, null, isRetry, progressMsg, isOfflineProcess, false);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param isRetry          the is retry
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(String url, JSONObject jsonRequest, final boolean isRetry, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    callWebService(url, jsonRequest, null, isRetry, progressMsg, isOfflineProcess, isDBProcess);
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param progressMsg the progress msg
   */
  public void callWebService(String url, JSONObject jsonRequest, final Bundle args, final String progressMsg){
    callWebService(url, jsonRequest, args, false, progressMsg);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param args             the args
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(String url, JSONObject jsonRequest, final Bundle args, final String progressMsg, final boolean isOfflineProcess){
    callWebService(url, jsonRequest, args, false, progressMsg, isOfflineProcess, false);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param args             the args
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(String url, JSONObject jsonRequest, final Bundle args, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    callWebService(url, jsonRequest, args, false, progressMsg, isOfflineProcess, isDBProcess);
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param isRetry     the is retry
   * @param progressMsg the progress msg
   */
  public void callWebService(final String url, final JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg){
    callWebService(url, jsonRequest, args, isRetry, progressMsg, false, false);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param args             the args
   * @param isRetry          the is retry
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(final String url, final JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    if(context instanceof LandingActivity)
      ((LandingActivity) context).getPinViewModel().callWebService(context, this, url, jsonRequest, args, isRetry, progressMsg, isOfflineProcess, isDBProcess);
    else if(context instanceof MainActivity)
      ((MainActivity) context).getRfidViewModel().callWebService(context, this, url, jsonRequest, args, isRetry, progressMsg, isOfflineProcess, isDBProcess);
  }
  
  /**
   * Pop back stack.
   */
  public void popBackStack(){
    context.popBackStack(this.getClass().getSimpleName());
  }
  
  /**
   * Pop back stack.
   */
  public void removeFromBackStack(){
    removeFromBackStack(this);
  }
  
  /**
   * Pop back stack.
   */
  public void removeFromBackStack(Fragment fragment){
    context.removeFromBackStack(fragment);
  }
  
  /**
   * Clear back stack.
   */
  public void clearBackStack(){
    context.clearBackStack();
  }
  
  /**
   * On back pressed.
   */
  public void onBackPressed(){
    popBackStack();
  }
  
  protected AppCommonMethods.SessionType getSessionType(){
    final String name = this.getClass().getSimpleName();
    if(name.matches("(?i)^.*Correction.*$")) return AppCommonMethods.SessionType.STOCK_CORRECTION;
    else if(name.matches("(?i)^.*Inv.*Brand.*$")) return AppCommonMethods.SessionType.BRAND_INVENTORY;
    else if(name.matches("(?i)^.*Inv.*Filter.*$")) return AppCommonMethods.SessionType.FILTER_INVENTORY;
    else if(name.matches("(?i)^.*Inv.*Add.*$")) return AppCommonMethods.SessionType.ADD_INVENTORY;
    else if(name.matches("(?i)^.*Inv.*$")) return AppCommonMethods.SessionType.INVENTORY;
    else if(name.matches("(?i)^.*Than.*Enc.*$")) return AppCommonMethods.SessionType.ENCODING_THAN;
    else if(name.matches("(?i)^.*Enc.*Verify.*$")) return AppCommonMethods.SessionType.ENCODING;
    else if(name.matches("(?i)^.*Enc.*$")) return AppCommonMethods.SessionType.ENCODING;
    else if(name.matches("(?i)^.*Replenish.*$")) return AppCommonMethods.SessionType.REPLENISHMENT;
    else if(name.matches("(?i)^.*Mov.*$")) return AppCommonMethods.SessionType.MOVEMENT;
    else if(name.matches("(?i)^.*Omni.*$")) return AppCommonMethods.SessionType.OMNICHANNEL;
    else if(name.matches("(?i)^.*Search.*$")) return AppCommonMethods.SessionType.SEARCH;
    else if(name.matches("(?i)^.*Inward.*$")) return AppCommonMethods.SessionType.INWARD;
    else if(name.matches("(?i)^.*Outward.*$")) return AppCommonMethods.SessionType.OUTWARD;
    return AppCommonMethods.SessionType.OTHER;
  }
  
  /**
   * Get type char code string.
   *
   * @return the string
   */
  public String getTypeCharCode(){ return getTypeCharCode(null); }
  
  /**
   * Get type char code string.
   *
   * @param sessionType the session type
   * @return the string
   */
  public String getTypeCharCode(AppCommonMethods.SessionType sessionType){
    if(sessionType != null && sessionType.getValue() > 0)
      return sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION ? "C" : sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD ? sessionType.name().substring(1, 2).toUpperCase() : sessionType.name().substring(0, 1);
    else if(this instanceof EncodingVerifyFragment) return "V";
    else if(this instanceof EncodingMainFragment || this instanceof EncodingStartFragment)
      return "E";
    else if(this instanceof StockCorrectionMainFragment || this instanceof StockCorrectionStartFragment)
      return "C";
    else if(this instanceof InventoryAddFragment) return "A";
    else if(this instanceof InventoryBrandFragment) return "B";
    else if(this instanceof InventoryMainFragment || this instanceof InventoryStartFragment)
      return "I";
    else if(this instanceof ActionMenuSearchFragment || this instanceof SearchMainFragment || this instanceof ProductSearchFragment || this instanceof SearchListsFragment || this instanceof SearchListFragment || this instanceof SearchListStartFragment || this instanceof AgeingSearchListFragment || this instanceof AgeingSearchStartFragment || this instanceof SearchAssortMainFragment || this instanceof SearchAssortListFragment || this instanceof SearchAssortStartFragment || this instanceof SearchUnencodedFragment || this instanceof SearchAlienFragment)
      return "S";
    else if(this instanceof OmniChannelFragment || this instanceof OmniChannelOrderStatsFragment || this instanceof TabOmniChannelOrderStatsFragment || this instanceof OmniChannelListFragment || this instanceof OmniChannelListDetailsFragment || this instanceof OmniChannelStartFragment)
      return "O";
    else if(this instanceof InwardMainFragment || this instanceof InwardHuVerificationFragment || this instanceof InwardGrnTripsDataFragment || this instanceof InwardTripsFragment || this instanceof InwardGrnTripDetailsFragment || this instanceof InwardGrnHuScanFragment || this instanceof InwardGrnStartFragment)
      return "N";
    else if(this instanceof ReplenishmentListFragment || this instanceof ReplenishmentStartFragment || this instanceof ReplenishmentBatchListFragment || this instanceof ReplenishmentArticleListFragment || this instanceof ReplenishmentEanStartFragment)
      return "R";
    else if(this instanceof MovementMainFragment || this instanceof MovementStartFragment)
      return "M";
    else if(this instanceof OutwardMainFragment || this instanceof OutwardPickDataFragment || this instanceof OutwardPickListsFragment || this instanceof OutwardPickListDetailsFragment || this instanceof OutwardPickStartFragment || this instanceof OutwardHuDataFragment || this instanceof OutwardHuListFragment || this instanceof OutwardHuDetailsFragment || this instanceof OutwardHuStartFragment)
      return "U";
    else return "";
  }
  
  public String getProductInfoUrl(){
    return context.getProductInfoUrl();
  }
  
  public String getEPCForEncodeUrl(){
    return context.getEPCForEncodeUrl();
  }
  
  public String getUploadEncodeUrl(){
    return context.getUploadEncodeUrl();
  }
  
  public ProductModel getProductModelFromResponse(final JSONObject jsonRequest, final JSONObject jsonResponse){
    return context != null && context instanceof MainActivity ? ((MainActivity) context).getProductModelFromResponse(jsonRequest, jsonResponse) : null;
  }
  
  /**
   * Get gson gson.
   *
   * @return the gson
   */
  public Gson getGSON(){ return AppCommonMethods.getGSON(); }
  
  /**
   * Get menu model menu model.
   *
   * @return the menu model
   */
  public MenuModel getMenuModel(){
    MenuModel menuModel = null;
    if(getArguments() != null){
      Serializable obj = extractSerializable(getArguments(), MenuModel.class);
      menuModel = obj instanceof MenuModel ? (MenuModel) obj : null;
    }
    if(menuModel == null && context != null)
      menuModel = AppDatabase.getMenuDao(context).getMenuByCode(getMenuCode());
    return menuModel;
  }
  
  /**
   * Get menu code string.
   *
   * @return the string
   */
  public String getMenuCode(){
    //Class redirectFrag=Class.forName()
    switch(CommonFragment.this.getClass().getSimpleName()){
      case "AppInfoFragment":
        return AppConstants.MENU_CODE_ACT_APP_INFO;
      case "ActionMenuSearchFragment":
        return AppConstants.MENU_CODE_ACT_SER;
      case "ActionMenuMsgFragment":
        return AppConstants.MENU_CODE_ACT_MSG;
      case "ActionMenuCompareFragment":
        return AppConstants.MENU_CODE_ACT_COMPARE;
      case "ActionMenuNotifyFragment":
        return AppConstants.MENU_CODE_ACT_NOTIFY;
      case "ActionMenuNotifyTypeListFragment":
        return AppConstants.MENU_CODE_ACT_NOTIFY;
      case "EncodingMainFragment":
        return AppConstants.MENU_CODE_ENC;
      case "EncodingConfigFragment":
        return AppConstants.MENU_CODE_ENC_CONFIG;
      case "EncodingAchieveFragment":
        return AppConstants.MENU_CODE_ENC_ACHIEVE;
      case "EncodingVerifyFragment":
        return AppConstants.MENU_CODE_ENC_VERIFY;
      case "EncodingStartFragment":
      case "EncodingScanScanWriteFragment":
        return AppConstants.MENU_CODE_ENC_START;
      case "DecodingStartFragment":
        return AppConstants.MENU_CODE_DEC;
      case "PreScanCountFragment":
      case "ScanCountFragment":
        return AppConstants.MENU_CODE_SCN_CNT;
      case "InventoryMainFragment":
        return AppConstants.MENU_CODE_INV;
      case "InventoryBrandFragment":
        return AppConstants.MENU_CODE_INV_BRAND;
      case "InventoryFilterFragment":
        return AppConstants.MENU_CODE_INV_FILTER;
      case "InventoryAddFragment":
        return AppConstants.MENU_CODE_INV_ADD;
      case "InventoryStartFragment":
        return AppConstants.MENU_CODE_INV_START;
      case "StockCorrectionMainFragment":
        return AppConstants.MENU_CODE_STOCK_CORRECT;
      case "StockCorrectionStartFragment":
        return AppConstants.MENU_CODE_STOCK_CORRECT;
      case "SearchMainFragment":
        return AppConstants.MENU_CODE_SER;
      case "ProductSearchFragment":
        return AppConstants.MENU_CODE_SER_PROD;
      case "OmniChannelFragment":
        return AppConstants.MENU_CODE_SER_OMNI;
      case "OmniChannelOrderStatsFragment":
        return AppConstants.MENU_CODE_SER_OMNI_ACHIEVE;
      case "OmniChannelListFragment":
        return AppConstants.MENU_CODE_SER_OMNI_START;
      case "OmniChannelListDetailsFragment":
        return AppConstants.MENU_CODE_SER_OMNI_START;
      case "OmniChannelStartFragment":
        return AppConstants.MENU_CODE_SER_OMNI_START;
      case "AgeingSearchMainFragment":
        return AppConstants.MENU_CODE_SER_AGEING;
      case "AgeingSearchFragment":
        return AppConstants.MENU_CODE_SER_AGEING;
      case "AgeingSearchListFragment":
        return AppConstants.MENU_CODE_SER_AGEING;
      case "AgeingSearchStartFragment":
        return AppConstants.MENU_CODE_SER_AGEING;
      case "SearchListsFragment":
        return AppConstants.MENU_CODE_SER_LIST;
      case "SearchListFragment":
        return AppConstants.MENU_CODE_SER_LIST;
      case "SearchListStartFragment":
        return AppConstants.MENU_CODE_SER_LIST;
      case "SearchAssortMainFragment":
        return AppConstants.MENU_CODE_SER_ASSORT;
      case "SearchAssortStartFragment":
        return AppConstants.MENU_CODE_SER_ASSORT;
      case "SearchUnencodedFragment":
        return AppConstants.MENU_CODE_SER_UNENCODED;
      case "SearchAlienFragment":
        return AppConstants.MENU_CODE_SER_ALIEN;
      case "SearchFileBasedFragment":
        return AppConstants.MENU_CODE_SER_FILE;
      case "SearchFIFOFragment":
      case "SearchFIFOStartFragment":
        return AppConstants.MENU_CODE_SER_FIFO;
      case "InwardMainFragment":
        return AppConstants.MENU_CODE_INW;
      case "InwardHuVerificationFragment":
        return AppConstants.MENU_CODE_INW_HU;
      case "InwardGrnTripsDataFragment":
        return AppConstants.MENU_CODE_INW_GRN;
      case "MovementMainFragment":
        return AppConstants.MENU_CODE_MOV;
      case "ReplenishmentListFragment":
        return AppConstants.MENU_CODE_REPLENISH;
      case "ReplenishmentStartFragment":
        return AppConstants.MENU_CODE_REPLENISH;
      case "MovementStartFragment":
        return AppConstants.MENU_CODE_MOV_START;
      case "OutwardMainFragment":
        return AppConstants.MENU_CODE_OTW;
      case "OutwardPickDataFragment":
        return AppConstants.MENU_CODE_OTW_PICK;
      case "OutwardPickListsFragment":
        return AppConstants.MENU_CODE_OTW_PICK;
      case "OutwardPickListDetailsFragment":
        return AppConstants.MENU_CODE_OTW_PICK;
      case "OutwardPickStartFragment":
        return AppConstants.MENU_CODE_OTW_PICK;
      case "OutwardHuDataFragment":
        return AppConstants.MENU_CODE_OTW_HU;
      case "OutwardHuListFragment":
        return AppConstants.MENU_CODE_OTW_HU;
      case "OutwardHuDetailsFragment":
        return AppConstants.MENU_CODE_OTW_HU;
      case "OutwardHuStartFragment":
        return AppConstants.MENU_CODE_OTW_HU;
      case "OutwardToteMainFragment":
        return AppConstants.MENU_CODE_OTW_TOTE;
      case "OutwardToteStartFragment":
        return AppConstants.MENU_CODE_OTW_TOTE;
      case "OffRangeMainFragment":
        return AppConstants.MENU_CODE_OFF_RANGE;
      case "OffRangeListFragment":
        return AppConstants.MENU_CODE_OFF_RANGE;
      case "OffRangeStartFragment":
        return AppConstants.MENU_CODE_OFF_RANGE;
      case "ThanMainFragment":
        return AppConstants.MENU_CODE_THAN;
      case "ThanEncodingFragment":
        return AppConstants.MENU_CODE_THAN_ENC;
      case "ThanCuttingFragment":
        return AppConstants.MENU_CODE_THAN_CUTTING;
      case "ThanClosureFragment":
        return AppConstants.MENU_CODE_THAN_CLOSURE;
      case "InwardToteMainFragment":
        return AppConstants.MENU_CODE_INW_TOTE;
      case "InwardToteStartFragment":
        return AppConstants.MENU_CODE_INW_TOTE;
      case "TripListFragment":
        return AppConstants.MENU_CODE_INW1;
      case "TripCreationFragment":
        return AppConstants.MENU_CODE_INW1;
      case "TripHUListFragment":
        return AppConstants.MENU_CODE_INW1;
      case "SaveSerialFragment":
        return AppConstants.MENU_CODE_SERIAL_SAVE;
      case "ReplenishmentBatchListFragment":
        return AppConstants.MENU_CODE_REPLENISH_DEMAND;
      case "ReplenishmentArticleListFragment":
        return AppConstants.MENU_CODE_REPLENISH_DEMAND;
      case "ReplenishmentEanStartFragment":
        return AppConstants.MENU_CODE_REPLENISH_DEMAND;
      case "ReplenishEOSSFragment":
        return AppConstants.MENU_CODE_REPLENISH_EOSS;
      case "SearchListExcelFragment":
        return AppConstants.MENU_CODE_SER_EXCEL;
      case "SearchListExcelStartFragment":
        return AppConstants.MENU_CODE_SER_EXCEL_START;
      default:
        return null;
    }
  }
  
  /**
   * Handle fragment redirection.
   *
   * @param fragment the fragment
   */
  public void handleFragmentRedirection(Fragment fragment){ handleFragmentRedirection(fragment, null, null); }
  
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
    ((MainActivity) context).handleFragmentRedirection(fragment, menuModel, args);
  }
  
  protected void parseDashboardResponse(final String categoryName, final String brandName, final JSONArray jsonSubArray, final boolean isZoneArray){
    if(isZoneArray){// || (isNonEmpty(brandName) && isNonEmpty(categoryName) && isNonEmpty(jsonSubArray))){
      if(jsonSubArray != null && jsonSubArray.length() > 0){
        try{
          AppCommonMethods.showLog("zonewise", "" + jsonSubArray.length());
          for(int j = 0; j < jsonSubArray.length(); j++){
            final BrandWiseZoneInventory brandWiseZoneInventory = getGSON().fromJson(jsonSubArray.getJSONObject(j).toString(), BrandWiseZoneInventory.class);
            if(brandWiseZoneInventory != null){
              if(isNonEmpty(brandName)) brandWiseZoneInventory.brandName = brandName;
              if(isNonEmpty(categoryName)) brandWiseZoneInventory.categoryName = categoryName;
              AppDatabase.getBrandWiseZoneInventoryDao(context).insert(brandWiseZoneInventory);
            }
          }
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    }
    else if(isNonEmpty(jsonSubArray) && (isNonEmpty(brandName) || isNonEmpty(categoryName))){
      if(jsonSubArray != null && jsonSubArray.length() > 0){
        try{
          for(int c = 0; c < jsonSubArray.length(); c++){
            final JSONObject jobj = jsonSubArray.getJSONObject(c);
            final JSONArray zonewise = extractJSONArray(jobj, ParamConstants.ZONES);
            final String brand = extractString(jobj, ParamConstants.BRAND_NAME, extractString(jobj, ParamConstants.NAME, "")).trim();
            final String category = extractString(jobj, ParamConstants.CATEGORY_NAME, extractString(jobj, ParamConstants.NAME, "")).trim();
            if(isNonEmpty(zonewise))
              parseDashboardResponse(chkNull(categoryName, category), chkNull(brandName, brand), zonewise, isNonEmpty(zonewise));
          }
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    }
  }
  
  /**
   * Handle response.
   *
   * @param url          the url
   * @param jsonRequest  the json request
   * @param jsonResponse the json response
   * @param responseCode the response code
   * @param isSuccess    the is success
   * @param args         the args
   */
  /*
   * This method will be called in child fragments whenever we call API*/
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    /*
     * This method will be called in child fragments whenever we call API*/
  }
  
  public void postFileWrite(final String filePath){
  
  }
  
  public String get20DigitHUNumber(String barcode){
    int len = barcode.length();
    int newln = 20 - len;
    
    String bar = barcode;
    for(int i = 0; i < newln; i++){
      bar = "0" + bar;
    }
    return bar;
  }
}
