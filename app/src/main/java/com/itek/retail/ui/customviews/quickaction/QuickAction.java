/*
 *  Copyright 2016 Piruin Panichphol
 *  Copyright 2011 Lorensius W. L. T
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 
 *     http://www.apache.org/licenses/LICENSE-2.0
 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.itek.retail.ui.customviews.quickaction;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.itek.retail.ui.customviews.quickaction.ArrowDrawable.ARROW_DOWN;
import static com.itek.retail.ui.customviews.quickaction.ArrowDrawable.ARROW_UP;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.itek.retail.R;

import java.util.ArrayList;
import java.util.List;

/**
 * The Quick action.
 */
public class QuickAction extends PopupWindows implements OnDismissListener{
  
  public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
  public static final int VERTICAL = LinearLayout.VERTICAL;
  
  private final int shadowSize;
  private final int shadowColor;
  private boolean enabledDivider = true;
  private WindowManager windowManager;
  private View rootView;
  private View arrowUp;
  private View arrowDown;
  private LayoutInflater inflater;
  private Resources resource;
  private LinearLayout track;
  private ViewGroup scroller;
  private OnActionItemClickListener mItemClickListener;
  private OnDismissListener dismissListener;
  private List<ActionItem> actionItems = new ArrayList<>(0);
  private Animation animation = Animation.AUTO;
  private boolean didAction;
  private int orientation;
  private int rootWidth = 0;
  
  /**
   * Instantiates a new Quick action.
   *
   * @param context the context
   */
  public QuickAction(@NonNull Context context){
    this(context, VERTICAL);
  }
  
  /**
   * Instantiates a new Quick action.
   *
   * @param context     the context
   * @param orientation the orientation
   */
  public QuickAction(@NonNull Context context, int orientation){
    super(context);
    this.orientation = HORIZONTAL;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    resource = context.getResources();
    
    shadowSize = resource.getDimensionPixelSize(R.dimen.quick_action_shadow_size);
    shadowColor = resource.getColor(R.color.shadow_quick_action);
    
    setRootView();//
    
    enabledDivider = true;
  }
  
  /**
   * Set root view.
   */
  private void setRootView(){
    rootView = inflater.inflate(R.layout.quick_action_horizontal, null);
    rootView.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    track = (LinearLayout) rootView.findViewById(R.id.tracks);
    track.setOrientation(orientation);
    
    arrowDown = rootView.findViewById(R.id.arrow_down);
    arrowUp = rootView.findViewById(R.id.arrow_up);
    
    scroller = (ViewGroup) rootView.findViewById(R.id.scroller);
    
    setContentView(rootView);
    setColor(resource.getColor(R.color.txtExtraLighterGray));
  }
  
  /**
   * Set color.
   *
   * @param popupColor the popup color
   */
  public void setColor(@ColorInt int popupColor){
    arrowDown.setBackground(new ArrowDrawable(ARROW_DOWN, popupColor, shadowSize, shadowColor));
    arrowUp.setBackground(new ArrowDrawable(ARROW_UP, popupColor, shadowSize, shadowColor));
    scroller.setBackgroundResource(R.drawable.border_cust_tooltip);
  }
  
  /**
   * Set enabled divider.
   *
   * @param enabled the enabled
   */
  public void setEnabledDivider(boolean enabled){
    this.enabledDivider = enabled;
  }
  
  /**
   * Set anim style.
   *
   * @param mAnimStyle the m anim style
   */
  public void setAnimStyle(Animation mAnimStyle){
    this.animation = mAnimStyle;
  }
  
  /**
   * Set on action item click listener.
   *
   * @param listener the listener
   */
  public void setOnActionItemClickListener(OnActionItemClickListener listener){
    mItemClickListener = listener;
  }
  
  /**
   * Add action item.
   *
   * @param actions the actions
   */
  public void addActionItem(final ActionItem... actions){
    for(ActionItem item : actions){
      addActionItem(item);
    }
  }
  
  /**
   * Add action item.
   *
   * @param action the action
   */
  public void addActionItem(final ActionItem action){
    int position = actionItems.size();
    actionItems.add(action);
    addActionView(position, createViewFrom(action));
  }
  
  /**
   * Add action view.
   *
   * @param position   the position
   * @param actionView the action view
   */
  private void addActionView(int position, View actionView){
    if(enabledDivider && position != 0){
      position *= 2;
      int separatorPos = position - 1;
      View separator = new View(getContext());
      
      separator.setBackgroundResource(R.color.viewline);
      int width = resource.getDimensionPixelOffset(R.dimen.quick_action_separator_width);
      LayoutParams layoutParams = null;
      switch(orientation){
        case VERTICAL:
          layoutParams = new LayoutParams(MATCH_PARENT, width);
          break;
        case HORIZONTAL:
          layoutParams = new LayoutParams(width, MATCH_PARENT);
          break;
        default:
          break;
      }
      track.addView(separator, separatorPos, layoutParams);
    }
    track.addView(actionView, position);
  }
  
  /**
   * Create view from view.
   *
   * @param action the action
   * @return the view
   */
  @NonNull
  private View createViewFrom(final ActionItem action){
    View actionView;
    if(action.haveTitle()){
      TextView textView = (TextView) inflater.inflate(R.layout.quick_action_item, track, false);
      textView.setText(String.format(" %s ", action.getTitle()));
      if(action.haveIcon()){
        int icon = action.getIconDrawableResourceId();
        if(orientation == HORIZONTAL){
          textView.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0);
        }
        else{
          textView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        }
      }
      actionView = textView;
    }
    else{
      ImageView imageView = (ImageView) inflater.inflate(R.layout.quick_action_image_item, track, false);
      imageView.setId(action.getActionId());
      imageView.setImageDrawable(action.getIconDrawable(getContext()));
      actionView = imageView;
    }
    
    actionView.setId(action.getActionId());
    actionView.setOnClickListener(v -> {
      action.setSelected(true);
      if(mItemClickListener != null){
        mItemClickListener.onItemClick(action);
      }
      if(!action.isSticky()){
        didAction = true;
        dismiss();
      }
    });
    actionView.setFocusable(true);
    actionView.setClickable(true);
    return actionView;
  }
  
  /**
   * Add action item.
   *
   * @param position the position
   * @param action   the action
   */
  public void addActionItem(int position, final ActionItem action){
    actionItems.add(position, action);
    addActionView(position, createViewFrom(action));
  }
  
  /**
   * Get action item action item.
   *
   * @param index the index
   * @return the action item
   */
  public ActionItem getActionItem(int index){
    return actionItems.get(index);
  }
  
  /**
   * Remove action item.
   *
   * @param actionId the action id
   * @return the action item
   */
  public ActionItem remove(int actionId){
    return remove(getActionItemById(actionId));
  }
  
  /**
   * Remove action item.
   *
   * @param action the action
   * @return the action item
   */
  public ActionItem remove(ActionItem action){
    int index = actionItems.indexOf(action);
    if(index == -1) throw new MyRUntimeException("Not found action");
    
    if(!enabledDivider){
      track.removeViewAt(index);
    }
    else{
      int viewPos = index * 2;
      track.removeViewAt(viewPos);
      track.removeViewAt(index == 0 ? 0 : viewPos - 1); //remove divider
    }
    return actionItems.remove(index);
  }
  
  /**
   * Get action item by id action item.
   *
   * @param actionId the action id
   * @return the action item
   */
  @Nullable
  public ActionItem getActionItemById(int actionId){
    for(ActionItem action : actionItems){
      if(action.getActionId() == actionId) return action;
    }
    return null;
  }
  
  /**
   * Show.
   *
   * @param activity the activity
   * @param anchorId the anchor id
   */
  public void show(@NonNull Activity activity, @IdRes int anchorId){
    show(activity.findViewById(anchorId));
  }
  
  /**
   * Show.
   *
   * @param anchor the anchor
   */
  public void show(@NonNull View anchor){
    if(getContext() == null)
      throw new IllegalStateException("Why context is null? It shouldn't be.");
    
    preShow();
    
    int xPos, yPos, arrowPos;
    
    didAction = false;
    
    int[] location = new int[2];
    anchor.getLocationOnScreen(location);
    Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + (anchor.getHeight() - anchor.getHeight() / 5));
    
    rootView.measure(WRAP_CONTENT, WRAP_CONTENT);
    
    int rootHeight = rootView.getMeasuredHeight();
    
    if(rootWidth == 0){
      rootWidth = rootView.getMeasuredWidth();
    }
    
    DisplayMetrics displaymetrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(displaymetrics);
    int screenWidth = displaymetrics.widthPixels;
    int screenHeight = displaymetrics.heightPixels;
    
    // automatically get X coord of quick_action_vertical (top left)
    if((anchorRect.left + rootWidth) > screenWidth){
      xPos = anchorRect.left - (rootWidth - anchor.getWidth());
      xPos = (xPos < 0) ? 0 : xPos;
      
      arrowPos = anchorRect.centerX() - xPos;
    }
    else{
      if(anchor.getWidth() > rootWidth){
        xPos = anchorRect.centerX() - (rootWidth / 2);
      }
      else{
        xPos = anchorRect.left;
      }
      
      arrowPos = anchorRect.centerX() - xPos;
    }
    
    int dyTop = anchorRect.top;
    int dyBottom = screenHeight - anchorRect.bottom;
    
    boolean onTop = dyTop > dyBottom;
    
    if(onTop){
      if(rootHeight > dyTop){
        yPos = 15;
        LayoutParams l = scroller.getLayoutParams();
        l.height = dyTop - anchor.getHeight();
      }
      else{
        yPos = anchorRect.top - rootHeight;
      }
    }
    else{
      yPos = anchorRect.bottom;
      
      if(rootHeight > dyBottom){
        LayoutParams l = scroller.getLayoutParams();
        l.height = dyBottom;
      }
    }
    
    showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);
    
    setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
    
    mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
  }
  
  /**
   * Show arrow.
   *
   * @param whichArrow the which arrow
   * @param requestedX the requested x
   */
  private void showArrow(@IdRes int whichArrow, int requestedX){
    final View showArrow = (whichArrow == R.id.arrow_up) ? arrowUp : arrowDown;
    final View hideArrow = (whichArrow == R.id.arrow_up) ? arrowDown : arrowUp;
    
    final int arrowWidth = arrowUp.getMeasuredWidth();
    
    showArrow.setVisibility(View.VISIBLE);
    
    ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();
    
    param.leftMargin = requestedX - arrowWidth / 2;
    
    hideArrow.setVisibility(View.GONE);
  }
  
  /**
   * Set animation style.
   *
   * @param screenWidth the screen width
   * @param requestedX  the requested x
   * @param onTop       the on top
   */
  private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop){
    int arrowPos = requestedX - arrowUp.getMeasuredWidth() / 2;
    switch(animation){
      case AUTO:
        if(arrowPos <= screenWidth / 4)
          mWindow.setAnimationStyle(Animation.GROW_FROM_LEFT.get(onTop));
        else if(arrowPos > screenWidth / 4 && arrowPos < 3 * (screenWidth / 4))
          mWindow.setAnimationStyle(Animation.GROW_FROM_CENTER.get(onTop));
        else mWindow.setAnimationStyle(Animation.GROW_FROM_RIGHT.get(onTop));
        break;
      default:
        mWindow.setAnimationStyle(animation.get(onTop));
    }
  }
  
  @Override
  public void onDismiss(){
    if(!didAction && dismissListener != null){
      dismissListener.onDismiss();
    }
  }
  
  /**
   * The enum Animation.
   */
  public enum Animation{
    GROW_FROM_LEFT{
      @Override
      int get(boolean onTop){
        return (onTop) ? R.style.Animation_PopUpMenu_Left : R.style.Animation_PopDownMenu_Left;
      }
    }, GROW_FROM_RIGHT{
      @Override
      int get(boolean onTop){
        return (onTop) ? R.style.Animation_PopUpMenu_Right : R.style.Animation_PopDownMenu_Right;
      }
    }, GROW_FROM_CENTER{
      @Override
      int get(boolean onTop){
        return (onTop) ? R.style.Animation_PopUpMenu_Center : R.style.Animation_PopDownMenu_Center;
      }
    }, REFLECT{
      @Override
      int get(boolean onTop){
        return (onTop) ? R.style.Animation_PopUpMenu_Reflect : R.style.Animation_PopDownMenu_Reflect;
      }
    }, AUTO{
      @Override
      int get(boolean onTop){
        throw new UnsupportedOperationException("Can't use this");
      }
    };
    
    /**
     * Get int.
     *
     * @param onTop the on top
     * @return the int
     */
    @StyleRes
    abstract int get(boolean onTop);
  }
  
  /**
   * The interface On action item click listener.
   */
  public interface OnActionItemClickListener{
    
    /**
     * On item click.
     *
     * @param item the item
     */
    void onItemClick(ActionItem item);
  }
  
  /**
   * The interface On dismiss listener.
   */
  public interface OnDismissListener{
    
    /**
     * On dismiss.
     */
    void onDismiss();
  }
  
  /**
   * The My r untime exception.
   */
  private class MyRUntimeException extends RuntimeException{
    
    /**
     * Instantiates a new My r untime exception.
     *
     * @param not_found_action the not found action
     */
    public MyRUntimeException(String not_found_action){
      /*Empty Method*/
    }
  }
}
