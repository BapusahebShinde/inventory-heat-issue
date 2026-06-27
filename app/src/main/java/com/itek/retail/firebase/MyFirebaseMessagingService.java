package com.itek.retail.firebase;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.SERVER_DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.getGSON;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.NotificationDao;
import com.itek.retail.model.Notification;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.landing.LandingActivity;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * The My firebase messaging service
 * used as Token Generator and Receiver for Firebase Push Notifications.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService{
  
  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage){
    final NotificationDao notificationDao = AppDatabase.getNotificationDao(MyFirebaseMessagingService.this);
    AppCommonMethods.showLog("notificationDao", "" + (notificationDao != null?notificationDao.getTableSize():"null"));
    AppCommonMethods.showLog("From", remoteMessage.getFrom());
    AppCommonMethods.showLog("SentTime", "" + remoteMessage.getSentTime());
    AppCommonMethods.showLog("MessageType", "" + remoteMessage.getMessageType());
    if(notificationDao != null /*&& SharedPrefManager.getIsLoggedIn()*/){
      // Check if message contains a data payload.
      final Map<String, String> dataMap = remoteMessage.getData();
      RemoteMessage.Notification notice = remoteMessage.getNotification();
      if(isNonEmpty(dataMap)){
        AppCommonMethods.showLog("Message data payload: ", dataMap.toString());
        JSONObject notiData = new JSONObject(dataMap);
        Notification notification = getGSON().fromJson(notiData.toString(), Notification.class);
        //temp/static bug fix(AB#4276)
        if(notification.getTypeId().equalsIgnoreCase(AppConstants.MENU_CODE_MOV_REPLENISH))
          notification.setTypeId(AppConstants.MENU_CODE_REPLENISH);
        notification.setReceivedOn(new SimpleDateFormat(DATE_TIME_FORMAT).format(new Date()));
        notification.setUserId(SharedPrefManager.getUserID());
        Calendar cc = Calendar.getInstance();
        cc.add(Calendar.HOUR_OF_DAY, extractInt(notiData, ParamConstants.VALID_TILL, 48));
        notification.setValidTill(new SimpleDateFormat(SERVER_DATE_TIME_FORMAT).format(cc.getTime()));
        notification.setRead(false);
        notificationDao.insert(notification);
        if(!SharedPrefManager.getIsLoggedIn() || AppCommonMethods.isAppInBackground(MyFirebaseMessagingService.this)){
          showLog("isAppInBackground", "true");
          //show Notification
          try{
            final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new NotificationCompat.Builder(this, getString(R.string.app_name)) : new NotificationCompat.Builder(this);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
              NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.app_name), notification.getType(), NotificationManager.IMPORTANCE_HIGH);
              mNotificationManager.createNotificationChannel(notificationChannel);
              builder.setChannelId(notificationChannel.getId());
            }
            builder.setSmallIcon(R.mipmap.itek_launcher_round);
            builder.setContentTitle(notice != null ? notice.getTitle() : notification.title);
            builder.setContentText(notice != null ? notice.getBody() : notification.message);
            builder.setAutoCancel(true);
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(notice != null ? notice.getTitle() : notification.title);
            inboxStyle.setSummaryText(notice != null ? notice.getBody() : notification.message);
            builder.setStyle(inboxStyle);
            Uri url = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? getRawUri("errorbeep1.mp3") : Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.errorbeep1);
            builder.setSound(url);
            builder.setVibrate(new long[]{100, 250, 100, 250});
            TypedValue tv = new TypedValue();
            builder.setLights(getTheme().resolveAttribute(R.attr.colorPrimaryDark,tv,true)? tv.data: getResources().getColor(R.color.colorPrimaryDarkDef), 10000, 12000);
            Intent activityIntent = new Intent(this, SharedPrefManager.getIsLoggedIn() ? MainActivity.class : LandingActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            builder.setContentIntent(PendingIntent.getActivity(this, 1001, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
            mNotificationManager.notify(1001, builder.build());
          }
          catch(Exception e){ e.printStackTrace(); }
        }
        else if(SharedPrefManager.getIsLoggedIn() && AppCommonMethods.mainActivity != null){
          AppCommonMethods.mainActivity.blink();
        }
      }
    }
  }
  
  /**
   * Get raw uri uri.
   *
   * @param filename the filename
   * @return the uri
   */
  public Uri getRawUri(String filename){
    return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + getPackageName() + "/raw/" + filename);
  }
  
  @Override
  public void onNewToken(@NonNull String token){
    super.onNewToken(token);
    if(AppCommonMethods.isNonEmpty(token)){
      SharedPrefManager.setFirebaseToken(token);
    }
  }
}
