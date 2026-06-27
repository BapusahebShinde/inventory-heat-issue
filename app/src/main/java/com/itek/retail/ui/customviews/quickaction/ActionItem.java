/*
 *  Copyright 2016 Piruin Panichphol
 *  Copyright 2011 Lorensius W. L. T
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.itek.retail.ui.customviews.quickaction;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;

/**
 * The Action item.
 */
public class ActionItem{
  
  private Bitmap thumb;
  private String title;
  private int icon = -1;
  private Drawable iconDrawable;
  private int actionId = -1;
  private boolean selected;
  private boolean sticky;
  
  /**
   * Instantiates a new Action item.
   *
   * @param actionId the action id
   * @param title    the title
   */
  public ActionItem(int actionId, String title){
    this(actionId, title, -1);
  }
  
  /**
   * Instantiates a new Action item.
   *
   * @param actionId the action id
   * @param title    the title
   * @param icon     the icon
   */
  public ActionItem(int actionId, String title, @DrawableRes int icon){
    this.actionId = actionId;
    this.title = title;
    this.icon = icon;
  }
  
  /**
   * Instantiates a new Action item.
   *
   * @param icon the icon
   */
  public ActionItem(@DrawableRes int icon){
    this(-1, null, icon);
  }
  
  /**
   * Instantiates a new Action item.
   *
   * @param actionId the action id
   * @param icon     the icon
   */
  public ActionItem(int actionId, @DrawableRes int icon){
    this(actionId, null, icon);
  }
  
  /**
   * Get title string.
   *
   * @return the string
   */
  public String getTitle(){
    return this.title;
  }
  
  /**
   * Set title.
   *
   * @param title the title
   */
  public void setTitle(String title){
    this.title = title;
  }
  
  /**
   * Have title boolean.
   *
   * @return the boolean
   */
  public boolean haveTitle(){
    return !TextUtils.isEmpty(title);
  }
  
  /**
   * Get icon int.
   *
   * @return the int
   */
  @DrawableRes
  public int getIcon(){
    return this.icon;
  }
  
  /**
   * Set icon.
   *
   * @param icon the icon
   */
  public void setIcon(@DrawableRes int icon){
    this.icon = icon;
  }
  
  /**
   * Have icon boolean.
   *
   * @return the boolean
   */
  public boolean haveIcon(){
    return icon > 0 || iconDrawable != null;
  }
  
  /**
   * Get action id int.
   *
   * @return the int
   */
  public int getActionId(){
    return actionId;
  }
  
  /**
   * Set action id.
   *
   * @param actionId the action id
   */
  public void setActionId(int actionId){
    this.actionId = actionId;
  }
  
  /**
   * Is sticky boolean.
   *
   * @return the boolean
   */
  public boolean isSticky(){
    return sticky;
  }
  
  /**
   * Set sticky.
   *
   * @param sticky the sticky
   */
  public void setSticky(boolean sticky){
    this.sticky = sticky;
  }
  
  /**
   * Is selected boolean.
   *
   * @return the boolean
   */
  public boolean isSelected(){
    return this.selected;
  }
  
  /**
   * Set selected.
   *
   * @param selected the selected
   */
  public void setSelected(boolean selected){
    this.selected = selected;
  }
  
  /**
   * Get thumb bitmap.
   *
   * @return the bitmap
   */
  public Bitmap getThumb(){
    return this.thumb;
  }
  
  /**
   * Set thumb.
   *
   * @param thumb the thumb
   */
  public void setThumb(Bitmap thumb){
    this.thumb = thumb;
  }
  
  /**
   * Get icon drawable drawable.
   *
   * @param context the context
   * @return the drawable
   */
  public Drawable getIconDrawable(Context context){
    if(iconDrawable == null) iconDrawable = context.getResources().getDrawable(icon);
    return iconDrawable;
  }
  
  /**
   * Get icon drawable resource id int.
   *
   * @return the int
   */
  public int getIconDrawableResourceId(){
    return icon;
  }
  
  /**
   * Set icon drawable.
   *
   * @param iconDrawable the icon drawable
   */
  public void setIconDrawable(Drawable iconDrawable){
    this.iconDrawable = iconDrawable;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    ActionItem that = (ActionItem) o;
    return actionId == that.actionId;
  }
  
  @Override
  public int hashCode(){
    return actionId;
  }
}
