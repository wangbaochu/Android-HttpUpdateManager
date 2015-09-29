package com.open.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.open.utils.FileUtils;
import com.open.utils.Log;

public class HttpTaskWrapper implements Runnable {

    private static final String TAG = "UpdateManager";
    
    /** The download file buffer size */
    private int FILESIZE = 4 * 1024;

    private String mUrl;
    private String mFileName;
    private String mFilePath;
    private String mRootPath;
    private DownLoadCallback mDownLoadCallback;

    public String getUrl() {
        return mUrl;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getFilePath() {
        return mFilePath;
    }

    /**
     * Constructor function
     * @param url The download url for downloading files.
     * @param rootPath The root directory where to save the downloaded file
     * @param mDownLoadCallback
     */
    public HttpTaskWrapper(String url, String rootPath, DownLoadCallback downLoadCallback) {
        mUrl = url;
        mRootPath = rootPath;
        mFileName = FileUtils.getFileNameFromUrl(url);
        mFilePath = rootPath + File.separator + mFileName;
        mDownLoadCallback = downLoadCallback;
    }

    private void onSuccess(String filePath) {
        if (mDownLoadCallback != null) {
            mDownLoadCallback.onSuccess(mRootPath, mFileName);
        } else {
            Log.i(TAG, "HttpTaskWrapper: onSuccess callback is null");
        }
    }

    private void onError(String errMsg) {
        if (mDownLoadCallback != null) {
            if (errMsg != null) {
                mDownLoadCallback.onFailure(errMsg);
            } else {
                mDownLoadCallback.onFailure(null);
            }
        } else {
            Log.i(TAG, "HttpTaskWrapper: onError callback is null");
        }
    }

    /**
     * 根据URL下载文件,前提是这个文件当中的内容是文本,函数的返回值就是文本当中的内容
     * 1.创建一个URL对象
     * 2.通过URL对象,创建一个HttpURLConnection对象
     * 3.得到InputStream
     * 4.从InputStream当中读取数据
     */
    @Override
    public void run() {
        InputStream inputStream = null;
        OutputStream output = null;
        HttpURLConnection urlConn = null;
        
        try {
            //1. create HttpURLConnection
            URL url = new URL(mUrl);
            urlConn = (HttpURLConnection)url.openConnection();
            inputStream = urlConn.getInputStream();

            //2. delete file if it already exists
            File file = new File(mFilePath);
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }
            
            //3. write the InputStream data to a file
            output = new FileOutputStream(file);
            byte[] buffer = new byte[FILESIZE];
            int length;
            while ((length = (inputStream.read(buffer))) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            output = null;
            onSuccess(mFilePath);
        } catch (Exception e) {
            onError("Download file failed: " + e.toString());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }
    }

    //    private void parseHttpResponse(HttpResponse response) {
    //        StatusLine status = response.getStatusLine();
    //        String responseBody = null;
    //        try {
    //            HttpEntity entity = null;
    //            HttpEntity temp = response.getEntity();
    //            if (temp != null) {
    //                entity = new BufferedHttpEntity(temp);
    //                responseBody = EntityUtils.toString(entity, "UTF-8");
    //            }
    //        } catch (IOException e) {
    //            onFailure(e);
    //        }
    //
    //        if (status.getStatusCode() >= 300) {
    //            onFailure(new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()));
    //        } else {
    //            onSuccess(status.getStatusCode(), response.getAllHeaders(), responseBody);
    //        }
    //    }
}
