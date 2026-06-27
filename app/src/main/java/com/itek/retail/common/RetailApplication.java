package com.itek.retail.common;

import androidx.multidex.MultiDexApplication;

import com.itek.retail.BuildConfig;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.ACRA;
import org.acra.config.MailSenderConfigurationBuilder;

//import com.balsikandar.crashreporter.CrashReporter;
//import com.balsikandar.crashreporter.utils.CrashUtil;

/**
 * The Retail application.
 */
/*@ReportsCrashes(
  formUri = "https://your-backend-server.com/path", // Backend endpoint
  mode = ReportingInteractionMode.TOAST, // Interaction mode (Toast, Dialog, Silent)
  resToastText = R.string.crash_toast_text // Text to show on crash
)
@Acra(buildConfigClass = BuildConfig.class)
@AcraCore(
  mailTo = "your-email@example.com"
)
@AcraMailSender(
  mailTo = "your.email@example.com", // Replace with your email
  reportAsFile = true,
  reportFileName = "crash-report.txt"
)*/
public class RetailApplication extends MultiDexApplication{
  
  @Override
  public void onCreate(){
    super.onCreate();
    SharedPrefManager.init(this);
    if(BuildConfig.IS_DIAGNOSTIC_BUILD) return;
    //ACRA.init(this);
    ACRA.init(this, new CoreConfigurationBuilder()
      // Core configuration:
      .withBuildConfigClass(BuildConfig.class)
      .withReportFormat(StringFormat.JSON) // JSON format is recommended
      .withPluginConfigurations(
        // MailSender configuration:
        new MailSenderConfigurationBuilder()
          .withMailTo("bhupen.morgaonkar@infoteksoftware.com") // Required: the destination email address
          .withSubject("App Crash Report") // Optional: email subject
          .withBody("Please find the crash report attached.") // Optional: email body
          .withReportAsFile(true) // Optional: send report as an attachment
          .withReportFileName("Crash.txt") // Optional: attachment file name
          .build()
      )
    );
//    if(AppCommonMethods.isDebugApp || SharedPrefManager.getIsShowCrashLog())
//      CrashReporter.initialize(this, CrashUtil.getDefaultPath());
  }
}
