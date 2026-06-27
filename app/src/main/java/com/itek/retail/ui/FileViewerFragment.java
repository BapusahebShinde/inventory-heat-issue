package com.itek.retail.ui;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.FragmentFileViewerBinding;

/**
 * The Home fragment.
 */
public class FileViewerFragment extends CommonFragment{
  
  private FragmentFileViewerBinding binding;
  private int defOrientation = 0;
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    defOrientation = context.getRequestedOrientation();
    if(defOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
      context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    binding = FragmentFileViewerBinding.inflate(inflater, container, false);
    final String filePath = extractString(getArguments(), AppConstants.FILE_PATH);
    if(isNonEmpty(filePath)){
      final String fileName = extractString(getArguments(), AppConstants.FILE_NAME, filePath.substring(filePath.lastIndexOf("/") + 1));
      final String fileExt = extractString(getArguments(), AppConstants.FILE_EXT, chkNull(fileName, filePath).substring(chkNull(fileName, filePath).lastIndexOf(".") + 1));
      final String fileType = chkNull(MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt), "");
      if(fileType.matches("(?i)(.*vid.*)")){
        int vidId = getResources().getIdentifier("vid" + fileName.replaceAll("." + fileExt, ""), "raw", context.getPackageName());
        Uri vidFileUri = null;
        try{
          vidFileUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + vidId);
        }
        catch(Exception e){ e.printStackTrace(); }
        
        if(vidId != 0 && vidFileUri != null) binding.vidFileView.setVideoURI(vidFileUri);
        else binding.vidFileView.setVideoPath(filePath);
        binding.vidFileView.setVisibility(View.VISIBLE);
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(binding.vidFileView);
        binding.vidFileView.setMediaController(mediaController);
        binding.vidFileView.start();
      }
    }
    return binding.getRoot();
  }
  
  @Override
  public void onResume(){
    super.onResume();
  }
  
  @Override
  public void onDestroyView(){
    if(defOrientation != context.getRequestedOrientation())
      context.setRequestedOrientation(defOrientation);
    super.onDestroyView();
    binding = null;
  }
  
  /*@Override
  public void onBackPressed(){
    if(binding.vidFileView != null && binding.vidFileView.getVisibility() == View.VISIBLE){
      if(binding.vidFileView.isPlaying()) binding.vidFileView.stopPlayback();
    }
    super.onBackPressed();
  }*/
}