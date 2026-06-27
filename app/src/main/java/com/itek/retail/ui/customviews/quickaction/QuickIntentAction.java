/*
 *  Copyright 2016 Piruin Panichphol
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.itek.retail.ui.customviews.quickaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.itek.retail.ui.customviews.quickaction.QuickAction.OnActionItemClickListener;

import java.util.List;

/**
 * The Quick intent action.
 */
public class QuickIntentAction{
  
  private static final int SERVICE = 1;
  private static final int ACTIVITY = 0;
  
  private Context context;
  private Intent intent;
  private int orientation;
  private int intentType = ACTIVITY;
  private OnActionItemClickListener onActionItemClick;
  private String type[] = {"Activity", "Service"};
  
  /**
   * Instantiates a new Quick intent action.
   *
   * @param context the context
   */
  public QuickIntentAction(Context context){
    this(context, QuickAction.VERTICAL);
  }
  
  /**
   * Instantiates a new Quick intent action.
   *
   * @param context     the context
   * @param orientation the orientation
   */
  public QuickIntentAction(Context context, int orientation){
    this.context = context;
    this.orientation = orientation;
  }
  
  /**
   * Set service intent quick intent action.
   *
   * @param services the services
   * @return the quick intent action
   */
  public QuickIntentAction setServiceIntent(Intent services){
    intent = services;
    intentType = SERVICE;
    return this;
  }
  
  /**
   * Set activity intent quick intent action.
   *
   * @param activity the activity
   * @return the quick intent action
   */
  public QuickIntentAction setActivityIntent(Intent activity){
    intent = activity;
    intentType = ACTIVITY;
    return this;
  }
  
  /**
   * Ser on action item click listener quick intent action.
   *
   * @param onClick the on click
   * @return the quick intent action
   */
  public QuickIntentAction serOnActionItemClickListener(OnActionItemClickListener onClick){
    onActionItemClick = onClick;
    return this;
  }
  
  /**
   * Create quick action.
   *
   * @return the quick action
   */
  public QuickAction create(){
    if(intent == null)
      throw new IllegalStateException("Must set intent be for create(), Use setActivityIntent() or " + "setServiceIntent()");
    
    QuickAction quickAction = new QuickAction(context, orientation);
    // Add List of Support Activity or Services
    final List<ResolveInfo> lists;
    PackageManager pm = context.getPackageManager();
    
    switch(intentType){
      case SERVICE:
        lists = pm.queryIntentServices(intent, 0);
        break;
      case ACTIVITY:
      default:
        lists = pm.queryIntentActivities(intent, 0);
        break;
    }
    // Add Action Item of support intent.
    if(lists.size() > 0){
      int index = 0;
      for(ResolveInfo info : lists){
        ActionItem item = new ActionItem(index++, (String) info.loadLabel(pm));
        item.setIconDrawable(info.loadIcon(pm));
        quickAction.addActionItem(item);
      }
      addOnActionItemClick(quickAction, lists);
    }
    else{
      ActionItem item = new ActionItem(0, "Not found support any" + type[intentType] + "!");
      quickAction.addActionItem(item);
    }
    
    return quickAction;
  }
  
  /**
   * Add on action item click.
   *
   * @param action the action
   * @param lists  the lists
   */
  private void addOnActionItemClick(QuickAction action, final List<ResolveInfo> lists){
    // If not explicit add then we'll Add Default OnActionItemClick
    if(onActionItemClick != null) action.setOnActionItemClickListener(onActionItemClick);
    else{
      setDefaultOnActionItemClick(action, lists);
    }
  }
  
  /**
   * Set default on action item click.
   *
   * @param action the action
   * @param lists  the lists
   */
  private void setDefaultOnActionItemClick(QuickAction action, final List<ResolveInfo> lists){
    switch(intentType){
      case SERVICE:
        action.setOnActionItemClickListener(item -> {
          ResolveInfo info = lists.get(item.getActionId());
          String name = info.serviceInfo.name;
          String packageName = info.serviceInfo.packageName;
          
          Intent service = new Intent(intent);
          service.setComponent(new ComponentName(packageName, name));
          context.startService(service);
        });
        break;
      case ACTIVITY:
      default:
        action.setOnActionItemClickListener(item -> {
          ResolveInfo info = lists.get(item.getActionId());
          String name = info.activityInfo.name;
          String packageName = info.activityInfo.packageName;
          
          Intent intent = new Intent(this.intent);
          intent.setComponent(new ComponentName(packageName, name));
          context.startActivity(intent);
        });
        break;
    }
  }
}
