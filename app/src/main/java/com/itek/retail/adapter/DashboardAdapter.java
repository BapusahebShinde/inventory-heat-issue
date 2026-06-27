package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.errorBeep;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isSetUserMgmt;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.MenuDao;
import com.itek.retail.databinding.GridviewAdapterLayoutBinding;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.customviews.quickaction.ActionItem;
import com.itek.retail.ui.customviews.quickaction.QuickAction;
import com.itek.retail.ui.decoding.DecodingStartFragment;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.TabFavouritesFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * The Dashboard adapter
 * used in Home Screen (TabHomeFragment)
 * for showing Grid of Home/Base/Main/Parent Menus.
 */
public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<MenuModel> listMenus = new ArrayList<>(0);
  private List<Integer> listRFIDSessions;
  private boolean isFavMenu;
  
  /**
   * Instantiates a new Dashboard adapter.
   *
   * @param context       the context
   * @param frag          the m frag
   * @param listGridMenus the list grid menus
   */
  public DashboardAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<MenuModel> listGridMenus){
    this.context = context;
    this.frag = frag;
    isFavMenu = frag instanceof TabFavouritesFragment;
    this.listMenus = listGridMenus;
    AppDatabase.getRIFDSessionDao(context).getAllSessionTypes().observe(frag.getViewLifecycleOwner(), listSessionTypes -> {
      listRFIDSessions = listSessionTypes;
      showLog("listRFIDSessions", "" + (listRFIDSessions == null ? 0 : listRFIDSessions.size()));
      DashboardAdapter.this.notifyDataSetChanged();
    });
    AppDatabase.getNotificationDao(context).getUnreadNotificationCount(isSetUserMgmt ? SharedPrefManager.getUserID() : "").observe(frag.getViewLifecycleOwner(), size -> {
      showLog("Unread Notification Count", "" + size);
      DashboardAdapter.this.notifyDataSetChanged();
    });
  }
  
  /**
   * Get item menu model.
   *
   * @param position the position
   * @return the menu model
   */
  public MenuModel getItem(int position){
    return listMenus.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    GridviewAdapterLayoutBinding binding = GridviewAdapterLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }
  
  @Override
  public long getItemId(int position){
    return position;
  }
  
  @Override
  public int getItemCount(){ return listMenus.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final MenuModel menuModel = getItem(position);
    if(menuModel == null) return;
    final int sessionType = menuModel.getSessionType();
    boolean isInRangeSessionType = sessionType > 0 && (sessionType < 3 || sessionType == 18);
    boolean isActiveRFIDSession = isNonEmpty(listRFIDSessions) && isInRangeSessionType && listRFIDSessions.contains(sessionType);
    boolean isUnreadNotifications = !isActiveRFIDSession && AppDatabase.getNotificationDao(context).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "", menuModel.getMenuCode());
    RFIDSession rfidSession = isActiveRFIDSession ? AppDatabase.getRIFDSessionDao(context).getCurrentSession(sessionType) : null;
    if(!isActiveRFIDSession && !isUnreadNotifications && sessionType > 0){
      for(String menuCode : AppDatabase.getMenuDao(context).getSubMenusCodes(menuModel.getMenuId())){
        int subSessionType = MenuModel.getSessionType(menuCode);
        if(isNonEmpty(listRFIDSessions) && sessionType > 0 && sessionType < 3 && subSessionType > 0 && (subSessionType < 3 || subSessionType == 12 || subSessionType == 13 || subSessionType == 28) && listRFIDSessions.contains(subSessionType)){
          isActiveRFIDSession = true;
          rfidSession = isActiveRFIDSession ? AppDatabase.getRIFDSessionDao(context).getCurrentSession(subSessionType) : null;
          break;
        }
        else if(AppDatabase.getNotificationDao(context).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "", menuCode)){
          isUnreadNotifications = true;
          break;
        }
      }
    }
    
    
    final boolean isActiveSession = isActiveRFIDSession;
    final RFIDSession activeRfidSession = isActiveSession ? rfidSession : null;
    holder.imgActiveSession.setVisibility(isActiveSession || isUnreadNotifications ? View.VISIBLE : View.GONE);
    holder.imgMenuSlash.setVisibility(!menuModel.getIsEnabled() ? View.VISIBLE : View.GONE);
    holder.lblMenuName.setText(HtmlCompat.fromHtml((isFavMenu ? menuModel.getFavMenuName() : menuModel.getMenuName()).replaceAll("\n", "<br/>").trim(), HtmlCompat.FROM_HTML_MODE_LEGACY));
    try{
      final int menuIconId = menuModel.getIconId(context);
      if(menuIconId > 0){ menuModel.setIconId(menuIconId); }
      final int favMenuIconId = menuModel.getFavIconId(context);
      if(favMenuIconId > 0){ menuModel.setFavIconId(favMenuIconId); }
      final int screenMenuIconId = menuModel.getScreenIconId(context);
      if(screenMenuIconId > 0){ menuModel.setScreenIconId(screenMenuIconId); }
      context.loadImage(holder.imgMenuLogo, chkNull(isFavMenu ? menuModel.getFavImageUrl() : menuModel.getImageUrl(), ""), chkZero(isFavMenu ? menuModel.getFavIconId() : menuModel.getIconId(), isFavMenu ? favMenuIconId : menuIconId));
      if(isNullOrEmpty(menuModel.getImageUrl()))
        holder.imgMenuLogo.setColorFilter(ContextCompat.getColor(context, !menuModel.getIsEnabled() ? R.color.colorDisabled : R.color.transparent), PorterDuff.Mode.SRC_ATOP);
    }
    catch(Exception e){ e.printStackTrace(); }
    
    holder.itemView.setLongClickable(isFavMenu && menuModel != null && !menuModel.equals(AppConstants.MENU_ADD_MORE));
    if(isFavMenu && menuModel != null && !menuModel.equals(AppConstants.MENU_ADD_MORE)){
      holder.itemView.setOnLongClickListener(v -> {
        if(isFavMenu && menuModel != null && !menuModel.equals(AppConstants.MENU_ADD_MORE)){
          doQuickActionStufs(position, menuModel, v);
        }
        return true;
      });
    }
    
    holder.itemView.setOnClickListener(v -> {
      if(!menuModel.equals(AppConstants.MENU_ADD_MORE) && !menuModel.getIsEnabled()){
        context.showShortToast(menuModel.getErrEnabledMsg(context));
        errorBeep();
        return;
      }
      Bundle args = new Bundle();
      args.putSerializable(menuModel.getClass().getSimpleName(), menuModel);
      args.putString(AppConstants.TITLE, menuModel.getScreenMenuName());
      args.putInt(AppConstants.TITLE_LOGO_RES_ID, menuModel.getScreenIconId());
      args.putString(AppConstants.TITLE_LOGO_URL, menuModel.getScreenImageUrl());
      takeActionAccordingToFragment(menuModel, isActiveSession, activeRfidSession, args);
    });
  }
  
  /**
   * Do quick action stufs.
   *
   * @param position  the position
   * @param menuModel the menu model
   * @param v         the v
   */
  private void doQuickActionStufs(int position, MenuModel menuModel, View v){
    if(isFavMenu){
      QuickAction quickAction = new QuickAction(context, QuickAction.HORIZONTAL);
      if(listMenus.contains(AppConstants.MENU_ADD_MORE)){
        quickAction.addActionItem(new ActionItem(101, "Replace", R.drawable.ic_replace));
      }
      quickAction.addActionItem(new ActionItem(102, "Remove", R.drawable.ic_remove));
      quickAction.setOnActionItemClickListener(item -> {
        switch(item.getActionId()){
          case 101:
            if(listMenus.contains(AppConstants.MENU_ADD_MORE)) showAddReplaceDialog(position);
            break;
          case 102:
            frag.updateLists(-1, true, menuModel);
            break;
          default:
            break;
        }
      });
      quickAction.show(v);
    }
  }
  
  /**
   * Show add replace dialog.
   *
   * @param replacePos the replace pos
   */
  public void showAddReplaceDialog(final int replacePos){
    if(isFavMenu){
      final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialog).create();
      frag.setAlertDialogCustomTitle(alertDialog, replacePos >= 0 ? R.string.title_favorites_replace : R.string.title_favorites_add);
      final View dialogMainLayout = LayoutInflater.from(context).inflate(R.layout.dialog_favourites_action_add_replace, null);
      final RecyclerView listFavoritesAction = dialogMainLayout.findViewById(R.id.list_dialog_favourites_action_add_replace);
      List<String> listSavedFavMenuCodes = SharedPrefManager.getSavedFavMenuCodes();
      MenuDao menuDao = AppDatabase.getMenuDao(context);
      List<MenuModel> listRemainingFavMenus = isNonEmpty(listSavedFavMenuCodes) ? menuDao.getRemainingFavMenus(listSavedFavMenuCodes) : menuDao.getAllFavMenus();
      showLog("listRemainingFavMenus", "" + (listRemainingFavMenus).size());
      listFavoritesAction.setAdapter(new FavoritesAddReplaceAdapter(context, (TabFavouritesFragment) frag, alertDialog, listRemainingFavMenus, replacePos));
      listFavoritesAction.setLayoutManager(new LinearLayoutManager(context));
      alertDialog.setView(dialogMainLayout);
      alertDialog.show();
    }
  }
  
  /**
   * Take action according to fragment.
   *
   * @param menuModel       the menu model
   * @param isActiveSession the is active session
   * @param args            the args
   */
  private void takeActionAccordingToFragment(final MenuModel menuModel, final Boolean isActiveSession, final RFIDSession activeRfidSession, final Bundle args){
    if(isFavMenu && menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_ADD))
      showAddReplaceDialog(-1);
    else if(isActiveSession && activeRfidSession != null){
      boolean isExpired = false;
      try{
        Date sessionValidDate = new SimpleDateFormat(DATE_TIME_FORMAT).parse(activeRfidSession.sessionValidTill);
        if(sessionValidDate != null && !Calendar.getInstance().getTime().before(sessionValidDate)){
          isExpired = true;
          context.showCustomAlertDialog("", activeRfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue() ? R.string.err_enc_session_expired: activeRfidSession.sessionType == AppCommonMethods.SessionType.DECODING.getValue() ? R.string.err_dec_session_expired : R.string.err_inv_session_expired, R.string.btn_ok, (dialogInterface, i) -> {
            context.getRfidViewModel().deleteSession(activeRfidSession);
            context.handleFragmentRedirection(menuModel);
          });
        }
      }
      catch(ParseException e){ e.printStackTrace(); }
      if(!isExpired){
        if(activeRfidSession.sessionType == AppCommonMethods.SessionType.INVENTORY.getValue() || activeRfidSession.sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY.getValue() || activeRfidSession.sessionType == AppCommonMethods.SessionType.ADD_INVENTORY.getValue() || activeRfidSession.sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY.getValue()){
          if(SharedPrefManager.getBoolean(ParamConstants.IS_OPTIMIZED_INVENTORY)){
            final InventoryDao inventoryDao = AppDatabase.getInventoryDao(context);
            int pendingUploadCount= inventoryDao.getNonUploadedCount(activeRfidSession.sessionId);
            if(pendingUploadCount>0){
              context.showCustomAlertDialog("", pendingUploadCount + " "+ AppCommonMethods.SessionType.get(activeRfidSession.sessionType).name() +" tags are pending for upload.\n" + "Do you want to upload ?\nOtherwise Data will be discarded.", context.getString(R.string.btn_upload), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                  context.callUploadInventory(menuModel,activeRfidSession);
                }
              }, context.getString(R.string.btn_discard), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                  inventoryDao.deleteInventory(activeRfidSession.sessionId);
                  context.getRfidViewModel().stopSession(activeRfidSession,true);
                  context.handleFragmentRedirection(menuModel);
                }
              });
              return;
            }
          }
          context.getActiveUsers(AppCommonMethods.SessionType.get(activeRfidSession.sessionType), AppConstants.SESSION_ACTION_RESUME, args);
        }
        else if(activeRfidSession.sessionType== AppCommonMethods.SessionType.DECODING.getValue())
        {
          if(!context.currentFragmentClassName.equalsIgnoreCase(DecodingStartFragment.class.getSimpleName())){
            args.putBoolean(ParamConstants.IS_ALLOW_DECODE, true);
            context.loadFragment(new DecodingStartFragment(), args);
          }
        }
        else context.handleFragmentRedirection(menuModel);
      }
    }
    else context.handleFragmentRedirection(menuModel);
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView lblMenuName;
    ImageView imgMenuLogo;
    ImageView imgMenuSlash;
    ImageView imgActiveSession;
    GridviewAdapterLayoutBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(GridviewAdapterLayoutBinding binding){
      super(binding.getRoot());
      lblMenuName = binding.lblMenuTitle;
      imgMenuLogo = binding.imgMenuLogo;
      imgMenuSlash = binding.imgMenuSlash;
      imgActiveSession = binding.imgActiveSession;
    }
    
    /**
     * Bind.
     *
     * @param menuModel the menu model
     */
    public void bind(final MenuModel menuModel){
      binding.setGridMenuViewModel(menuModel);
      binding.executePendingBindings();
    }
  }
  
}
