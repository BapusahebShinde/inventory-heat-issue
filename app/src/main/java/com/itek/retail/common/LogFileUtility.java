package com.itek.retail.common;

import android.os.AsyncTask;

import com.itek.retail.R;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class LogFileUtility{
  //TODO compare with Shell file & update accordingly..
  
  public static final int fileCount = 5;
  public static final Long maxFileSize = 30 * 1000 * 1000l;
  
  public static void writeLog(final CommonActivity activity){
    if(activity == null || activity.isFinishing()) return;
    new AsyncTask<Void, Void, Void>(){
      @Override
      protected Void doInBackground(Void... voids){
        try{
          final String fileName = activity.getString(R.string.app_name).replaceAll(" ", "_").trim() + "Log";
          final File baseDir = new File(activity.getBaseDirectory(""), "Logs");
          baseDir.mkdirs();
          int size = baseDir.listFiles().length;
          if(size == 0){
            Runtime.getRuntime().exec("logcat -f" + baseDir.getAbsolutePath() + File.separator + fileName + "-" + fileCount + ".txt");
          }
          else if(size < fileCount){
            final File file = baseDir.listFiles(new FilenameFilter(){
              @Override
              public boolean accept(File dir, String name){
                return name.contains("-" + (fileCount - (size - 1)));
              }
            })[0];
            if(file.exists() && file.length() >= maxFileSize){
              Runtime.getRuntime().exec("logcat -f" + baseDir.getAbsolutePath() + File.separator + fileName + "-" + (fileCount - size) + ".txt");
            }
            else
              Runtime.getRuntime().exec("logcat -f" + baseDir.getAbsolutePath() + File.separator + file.getName());
          }
          else if(size >= fileCount){
            final File file1 = baseDir.listFiles(new FilenameFilter(){
              @Override
              public boolean accept(File dir, String name){
                return name.contains("-1");
              }
            })[0];
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
                  AppCommonMethods.showLog("file", file.getName());
                  AppCommonMethods.showLog("fileCount", "" + count);
                  file.renameTo(new File(baseDir, fileName + "-" + (count + 1) + ".txt"));
                }
                catch(Exception e){ e.printStackTrace(); }
              }
              Runtime.getRuntime().exec("logcat -f" + baseDir.getAbsolutePath() + File.separator + file1.getName());
            }
            else
              Runtime.getRuntime().exec("logcat -f" + File.separator + baseDir.getAbsolutePath() + File.separator + file1.getName());
          }
          //Runtime.getRuntime().exec("logcat -f" + " /sdcard/Logcat.txt");
          // Runtime.getRuntime().exec("logcat -f" + " /sdcard/" + fileName);
        }
        catch(IOException e){
          e.printStackTrace();
        }
        return null;
      }
    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }
  
}
