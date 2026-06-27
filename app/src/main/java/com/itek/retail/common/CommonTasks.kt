package com.itek.retail.common

import android.content.Context
import android.os.Environment
import android.util.Log
import com.itek.retail.apis.ParamConstants
import com.itek.retail.receiver.AppBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class CommonTasks {
  class WriteFileTask(context: Context) : CoroutineScope by MainScope() {
    var context: Context? = null
    var fragment: CommonFragment? = null
    var receiver: AppBroadcastReceiver? = null
    var url: String? = null
    var fileName: String? = null
    var isShowProgress: Boolean? = false;
    var responseMsg: String? = null;

    internal constructor(
      context: Context,
      fragment: CommonFragment? = null,
      receiver: AppBroadcastReceiver? = null,
      url: String? = null,
      fileName: String? = null,
      isShowProgress: Boolean? = false,
      responseMsg: String? = null,
    ) :
      this(context) {
      this.context = context;
      this.fragment = fragment;
      this.receiver = receiver;
      this.url = url;
      this.fileName = fileName;
      this.isShowProgress = isShowProgress;
      this.responseMsg = responseMsg;
    }

    suspend fun doInBackground(responseBody: ResponseBody): String {
      return async(Dispatchers.IO) {
        writeFile(
          responseBody,
          File(
            AppCommonMethods.getBaseDirectory(context, Environment.DIRECTORY_DOWNLOADS),
            fileName
          )
        )
      }.await()
    }

    private fun writeFile(responseBody: ResponseBody, output: File): String {
      val buffer = ByteArray(1024);
      var inputStream: InputStream? = null
      var outputStream: OutputStream? = null
      var length: Int
      try {
        if (output.exists() && output.length() > 0) output.delete();
        output.createNewFile();
        inputStream = responseBody.byteStream()
        outputStream = FileOutputStream(output)
        // transfer bytes from the inputfile to the
        while (inputStream.read(buffer).also { length = it } > 0) {
          outputStream.write(buffer, 0, length);
        }

        outputStream.flush()

      } catch (e: Exception) {
        e.printStackTrace()
        return "";
      } finally {
        outputStream?.close()
        inputStream?.close()
      }
      return output.getAbsolutePath()
    }

    fun onPostExecute(filePath: String) {
      if (AppCommonMethods.chkNotNullTrue(isShowProgress)) {
        AppCommonMethods.allowBtnClick = true;
        AppCommonMethods.hideProgressDialog(context);
      }
      if (filePath != null && filePath.trim().isNotEmpty()) {
        Log.e("filePath", filePath);
        if (filePath.endsWith(".apk", true)) {
          if (receiver == null) (context as? CommonActivity)?.installNewVersion(filePath);
          else {
            SharedPrefManager.setUpdateAPKPath(filePath);
            SharedPrefManager.setString(ParamConstants.APPLICATION_VERSION+"_"+ParamConstants.MESSAGE,responseMsg);
          }
        }
        /*else {
          if (receiver != null) receiver.postFileWrite(filePath);
          if (fragment != null) fragment.postFileWrite(filePath);
          if (context != null && context is CommonActivity) context.postFileWrite(filePath);
        }*/

      }
    }

    fun execute(responseBody: ResponseBody) {
      launch { onPostExecute(doInBackground(responseBody)) }
    }
  }
}