package com.itek.retail.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.databinding.ListDialogFavoritesAddReplaceBinding;
import com.itek.retail.model.MenuModel;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.TabFavouritesFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The Favorites options adapter.
 * used for Adding a New/Replacing an Existing Favourites menu from Favourites Adapter using available options
 */
public class FavoritesAddReplaceAdapter extends RecyclerView.Adapter<FavoritesAddReplaceAdapter.ViewHolder>{
  
  private MainActivity context;
  private TabFavouritesFragment frag;
  private AlertDialog alert;
  private List<MenuModel> listMenus = new ArrayList<>(0);
  private int replacePos = -1;
  private boolean isReplace = false;
  
  /**
   * Instantiates a new Favorites options adapter.
   *
   * @param context    the context
   * @param frag       the m frag
   * @param alert      the m alert
   * @param listMenus  the list menus
   * @param replacePos the replace pos
   */
  public FavoritesAddReplaceAdapter(@NonNull MainActivity context, @NonNull TabFavouritesFragment frag, AlertDialog alert, List<MenuModel> listMenus, int replacePos){
    this.context = context;
    this.frag = frag;
    this.alert = alert;
    this.listMenus = listMenus != null ? listMenus : this.listMenus;
    this.replacePos = replacePos;
    this.isReplace = replacePos >= 0;
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
    ListDialogFavoritesAddReplaceBinding binding = ListDialogFavoritesAddReplaceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }
  
  @Override
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listMenus.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final MenuModel itemModel = getItem(position);
    if(itemModel == null) return;
    holder.itemView.setBackgroundResource(position % 2 == 0 ? R.color.txtSuperExtraLighterGray : R.color.white);
    holder.txtMenu.setText(itemModel.getFavMenuName());
    holder.txtAction.setTextColor(context.getColorPrimaryDarkFromTheme());
    holder.txtAction.setText(isReplace ? R.string.action_favorites_replace : R.string.action_favorites_add);
    holder.txtAction.setOnClickListener(v -> {
      frag.updateLists(replacePos, false, itemModel);
      if(alert != null) alert.dismiss();
    });
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtMenu;
    TextView txtAction;
    ListDialogFavoritesAddReplaceBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListDialogFavoritesAddReplaceBinding binding){
      super(binding.getRoot());
      txtMenu = binding.txtItemTitle;
      txtAction = binding.txtItemAction;
    }
    
    /**
     * Bind.
     *
     * @param itemModel the item model
     */
    public void bind(final MenuModel itemModel){
      binding.setMenuFavoritesItemViewModel(itemModel);
      binding.executePendingBindings();
    }
  }
}
