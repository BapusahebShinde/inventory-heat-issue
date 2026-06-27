package com.itek.retail.common;

public interface DownloadProgressListener {
  void update(long bytesRead, long contentLength, boolean done);
}
