package com.itek.retail.common;

import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;
import static com.itek.retail.common.AppCommonMethods.showToast;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The type Log file utility hhd.
 */
public class LogFileUtilityHHD{
  
  //public static final int fileCount=5;
  //public static final String fileName="ShellAndroidLog";
  public static final String APP_TAG = "Android-HHD";
  public static final String TAG = "PDF-Logs";
  public static final String fileName = "AndroidHHDLog-" + SharedPrefManager.getIMEI();
  public static int fileSize = 30;
  public static Long maxFileSize = fileSize * 1000 * 1000l;
  public static int fileCount = 5;//SharedPrefManager.getMaximumNumberOfLogFiles();
  //public static final Long maxFileSize=100*1000l;
  
  public static void setMaxFileCount(){
    fileCount = SharedPrefManager.getMaxLogFiles();
    fileSize = SharedPrefManager.getLogFileMaxSize();
    maxFileSize = fileSize * 1000 * 1000l;
  }
  
  /**
   * Write log.
   *
   * @param activity the activity
   */
  public static void writeLog(final CommonActivity activity){ writeLog(activity,APP_TAG,"",fileName,fileCount);}

  /*public static void writeLog(final CommonActivity activity,String tag,String dirName, String fileName, int fileCount){
    if(activity == null || activity.isFinishing()) return;
    new AsyncTask<Void, Void, String>(){
      @Override
      protected String doInBackground(Void... voids){
        try{
          final File baseDir = activity.getBaseDirectory(isNonEmpty(dirName)?dirName+"_Logs":"Logs");
          // ApplicationCommonMethods.showLog("BASEDIR",""+baseDir.getAbsolutePath());
          
          int size = baseDir.listFiles().length;
          // ApplicationCommonMethods.showLog("BASEDIRSIZE",""+size);
          if(size == 0){
            File f = new File(baseDir, fileName + "-" + fileCount + ".txt");
            if(!f.exists()) f.createNewFile();
            String filePath = f.getAbsolutePath();//baseDir.getAbsolutePath() + File.separator + fileName + "-" + fileCount + ".txt";
            Runtime.getRuntime().exec(new String[]{"logcat -f", filePath, tag + ":I " + "System.err:D", "*:S"});
          }
          else if(size > 0 && size < fileCount){
        *//*final File file = baseDir.listFiles(new FilenameFilter(){
          @Override
          public boolean accept(File dir, String name){
            return name.contains("-" + (fileCount - (size - 1)));
          }
        })[0];*//*
            File file = new File(baseDir, fileName + "-" + (fileCount - (size - 1)) + ".txt");
            if(file.exists() && file.length() >= maxFileSize){
              File f = new File(baseDir, fileName + "-" + (fileCount - size) + ".txt");
              if(!f.exists()) f.createNewFile();
              String filePath = f.getAbsolutePath();//baseDir.getAbsolutePath() + File.separator + fileName + "-" + (fileCount - size) + ".txt";
              //Runtime.getRuntime().exec("logcat -f" + baseDir.getAbsolutePath() + File.separator + fileName + "-" + (fileCount - size) + ".txt");
              Runtime.getRuntime().exec(new String[]{"logcat", "-f", filePath, tag + ":I " + "System.err:D", "*:S"});
            }
            else{
              String filePath = file.getAbsolutePath();//baseDir.getAbsolutePath() + File.separator + file.getName();
              //Runtime.getRuntime().exec(Filtered_Log_Command +baseDir.getAbsolutePath()+File.separator+file1.getName());
              //Runtime.getRuntime().exec("logcat -f" + baseDir.getAbsolutePath() + File.separator + file.getName());
              Runtime.getRuntime().exec(new String[]{"logcat", "-f", filePath, tag + ":I " + "System.err:D", "*:S"});
            }
            
          }
          else if(size >= fileCount){
            final File file1 = new File(baseDir, fileName + "-1" + ".txt");*//*baseDir.listFiles(new FilenameFilter(){
          @Override
          public boolean accept(File dir, String name){
            return name.contains("-1");
          }
        })[0];*//*
            if(file1.exists() && file1.length() >= maxFileSize){
              final File file5 = baseDir.listFiles(new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name){
                  return name.contains("-" + (fileCount));
                }
              })[0];
              file5.delete();
              File[] files = baseDir.listFiles();
              
              Arrays.sort(files, new Comparator<File>(){
                public int compare(File f1, File f2){
                  return f2.getName().compareTo(f1.getName());
                }
              });
              
              for(File file : files){
                try{
                  final String name = file.getName();
                  final int count = Integer.parseInt(name.substring(name.lastIndexOf('-') + 1, name.lastIndexOf('.')));
                  showLog(tag, "FILENAME " + file.getName());
                  showLog(tag, "FILECOUNT " + count);
                  file.renameTo(new File(baseDir, fileName + "-" + (count + 1) + ".txt"));
                }
                catch(Exception e){ e.printStackTrace(); }
              }
              String filePath = file1.getAbsolutePath();//baseDir.getAbsolutePath() + File.separator + file1.getName();
              Runtime.getRuntime().exec(new String[]{"logcat", "-f", filePath, tag + ":I " + "System.err:D", "*:S"});
              //Runtime.getRuntime().exec("logcat -f" +baseDir.getAbsolutePath()+File.separator+file1.getName());
            }
            else{
              String filePath = file1.getAbsolutePath();//baseDir.getAbsolutePath() + File.separator + file1.getName();
              //Runtime.getRuntime().exec("logcat -f" + File.separator + baseDir.getAbsolutePath() + File.separator + file1.getName());
              Runtime.getRuntime().exec(new String[]{"logcat", "-f", filePath, tag + ":I " + "System.err:D", "*:S"});
            }
          }
          //Runtime.getRuntime().exec("logcat -f" + " /sdcard/Logcat.txt");
          //Runtime.getRuntime().exec("logcat -f" + " /sdcard/" + fileName);
        }
        catch(IOException e){
          e.printStackTrace();
        }
        return null;
      }
    }.execute();
  }*/

  public static void writeLog(final CommonActivity activity, final String tag, final String dirName, final String fileName, final int fileCount) {
    if (activity == null || activity.isFinishing()) return;

    new AsyncTask<Void, Void, String>() {
      @Override
      protected String doInBackground(Void... voids) {
        try {
          final File baseDir = activity.getBaseDirectory(isNonEmpty(dirName) ? dirName + "_Logs" : "Logs");
          // Safely handle missing directory
          if (baseDir == null) return null;
          if (!baseDir.exists()) {
            baseDir.mkdirs();
          }

          File[] existingFiles = baseDir.listFiles();
          int size = (existingFiles != null) ? existingFiles.length : 0;

          String filePath = "";

          if (size == 0) {
            File f = new File(baseDir, fileName + "-" + fileCount + ".txt");
            if (!f.exists()) f.createNewFile();
            filePath = f.getAbsolutePath();
          }
          else if (size > 0 && size < fileCount) {
            File file = new File(baseDir, fileName + "-" + (fileCount - (size - 1)) + ".txt");
            if (file.exists() && file.length() >= maxFileSize) {
              File f = new File(baseDir, fileName + "-" + (fileCount - size) + ".txt");
              if (!f.exists()) f.createNewFile();
              filePath = f.getAbsolutePath();
            } else {
              filePath = file.getAbsolutePath();
            }
          }
          else if (size >= fileCount) {
            final File file1 = new File(baseDir, fileName + "-1" + ".txt");
            if (file1.exists() && file1.length() >= maxFileSize) {
              // Safely find and delete the oldest file (fileCount)
              File[] targetToDelete = baseDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                  return name.contains("-" + fileCount + ".");
                }
              });
              if (targetToDelete != null && targetToDelete.length > 0) {
                targetToDelete[0].delete();
              }

              // Re-fetch remaining files to sort and shift safely
              File[] files = baseDir.listFiles();
              if (files != null) {
                Arrays.sort(files, new Comparator<File>() {
                  public int compare(File f1, File f2) {
                    return f2.getName().compareTo(f1.getName());
                  }
                });

                for (File file : files) {
                  try {
                    String name = file.getName();
                    int count = Integer.parseInt(name.substring(name.lastIndexOf('-') + 1, name.lastIndexOf('.')));
                    file.renameTo(new File(baseDir, fileName + "-" + (count + 1) + ".txt"));
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                }
              }
              filePath = file1.getAbsolutePath();
            } else {
              filePath = file1.getAbsolutePath();
            }
          }

          // Execute logcat with correct arguments and split filters
          if (!filePath.isEmpty()) {
            String[] command = {
                    "logcat",
                    "-f",
                    filePath,
                    tag + ":I",
                    "System.err:D",
                    "*:S"
            };
            Runtime.getRuntime().exec(command);
          }

        } catch (IOException e) {
          e.printStackTrace();
        }
        return null;
      }
    }.execute();
  }

  public static void writeReaderLog(final CommonActivity activity, String tag, String folderName, String fileName){
    writeReaderLog(activity,tag,folderName,fileName,fileCount);
    //AppCommonMethods.writeReaderLog(activity,folderName,fileName,tag);
  }
  public static void writeReaderLog(final CommonActivity activity, String tag, String folderName, String fileName, int fileCount){
    writeLog(activity,tag,folderName,fileName,fileCount);
  }


  public static void zipLogsAndShare(Context context) {
    try {
      // 1. Logs folder inside external cache
      File logsDir = new File(context.getExternalCacheDir(), "Logs");
      if (!logsDir.exists() || logsDir.listFiles() == null || logsDir.listFiles().length == 0) {
        showToast(context, "No logs to share", true);
        return;
      }

      File[] logFiles = logsDir.listFiles();
      showLog("Logs", "Found " + logFiles.length + " log files");

      // 2. Create zip inside Logs folder (overwrite if exists)
      File cacheZip = new File(logsDir, "logs.zip");
      if (cacheZip.exists()) {
        boolean deleted = cacheZip.delete();
        showLog("Logs", "Old zip deleted? " + deleted);
      }
      createZip(logFiles, cacheZip);
      showLog("Logs", "Zip created at: " + cacheZip.getAbsolutePath());

      // 3. Get URI from FileProvider
      Uri uri = FileProvider.getUriForFile(
              context,
              context.getPackageName(),
              cacheZip
      );

      // 4. Share intent
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("application/zip");
      shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        shareIntent.setClipData(ClipData.newRawUri("logs", uri));
      }

      context.startActivity(Intent.createChooser(shareIntent, "Share logs via"));

    } catch (Exception e) {
      showLog("Logs", "Error sharing logs: " + e);
      showToast(context, "Error sharing logs: " + e.getMessage(), true);
    }
  }

  /**
   * Utility: zip multiple files into one zip file.
   */
  private static void createZip(File[] files, File outFile) throws IOException {
    byte[] buffer = new byte[4096];
    try (FileOutputStream fos = new FileOutputStream(outFile);
         ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {

      for (File f : files) {
        if (!f.isFile()) continue;

        showLog("Logs", "Adding file: " + f.getName());

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
          ZipEntry entry = new ZipEntry(f.getName());
          entry.setTime(f.lastModified());
          zos.putNextEntry(entry);

          int len;
          while ((len = bis.read(buffer)) != -1) {
            zos.write(buffer, 0, len);
          }
          zos.closeEntry();
        }
      }
    }
    showLog("Logs", "Zip successfully created: " + outFile.getAbsolutePath());
  }

}
