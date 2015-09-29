package com.open.test;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.open.net.DownLoadCallback;
import com.open.net.HttpDownloader;
import com.open.net.HttpPoster;
import com.open.net.IRequstListenser;
import com.open.net.IUpdateListener;
import com.open.net.ProtocolConfiguration;
import com.open.net.ServerPostData;
import com.open.utils.FileUtils;
import com.open.utils.Log;
import com.open.utils.MD5Util;
import com.open.utils.NetworkHelper;

/**
 * This class is take response to update client database from remote server. 
 *  1. First we will post http request to server, server will return the latest file version and download url.
 *  2. In client side we will check the version, if the server version is larger than local, we will start to
 * download the newest file via download url. 
 *  3. When the file download completely, we will up-zip it and then update the local db file.
 */
public class UpdateManager {

    private static final String TAG = "UpdateManager";
    private Context mContext = null;
    private int getLocalVersion() {
        //TODO: Save and get your local version from database or sharedPreference
        return 0; 
    }
    private void updateLocalVersion(int newVersion) {
        //TODO: Update your local version to database or sharedPreference
    }

    public UpdateManager(Context context) {
        mContext = context;
    }

    /** 
     * Trigger HTTP request to query the version from remote server
     * Note:If the time stamp is expired for one day, start to update
     * @param dataType Identify which kind of data will be updated
     */
    public void updateDatabaseFromServer(final IUpdateListener updateListener) {
        JSONObject data = new JSONObject();
        try {
            JSONObject jsonObj = new JSONObject();
            //TODO:Initialize the data that need send to server
            data.put("data", jsonObj);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        Log.i(TAG, "Start HttpPoster... ");
        try {
            ServerPostData postData = new ServerPostData(data);
            HttpPoster httpRequestSender = new HttpPoster(mContext, ProtocolConfiguration.getServerHost(mContext), postData, new IRequstListenser() {
                @Override
                public void handleData(String jsonString) {
                    if (!TextUtils.isEmpty(jsonString)) {
                        Log.d(TAG, "###### HttpPoster response = " + jsonString);

                        JSONObject fileResponse = null;
                        try {
                            fileResponse = new JSONObject(jsonString);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (fileResponse != null) {
                            String downloadUrl = null;
                            try {
                                downloadUrl = fileResponse.getString("downloadUrl");
                            } catch (JSONException e) {
                            }

                            String fileMD5 = null;
                            try {
                                fileMD5 = fileResponse.getString("md5");
                            } catch (JSONException e) {
                            }

                            int serverVersion = 0;
                            try {
                                serverVersion = fileResponse.getInt("version");
                            } catch (JSONException e) {
                            }

                            int localVersion = getLocalVersion(); 
                            if (serverVersion > localVersion && downloadUrl != null) {
                                if (NetworkHelper.isWifiConnected(mContext)) {
                                    downloadFile(downloadUrl, fileMD5, updateListener, serverVersion);
                                } else {
                                    Log.i(TAG, "###### Not wifi network, abort updateing ###### ");
                                }
                            } else {
                                Log.i(TAG, "###### Needn't download: localVersion = " + localVersion + ", serverVersion = " + serverVersion);
                            }
                        }
                    } else {
                        Log.e(TAG, "###### Error: jsonString = null");
                    }

                }

                @Override
                public void notNetConnection() {
                    Log.i(TAG, "HttpPoster error: notNetConnection");
                }

                @Override
                public void error(Throwable err) {
                    Log.i(TAG, "HttpPoster error: " + err.toString());
                }

                @Override
                public void finish() {
                    Log.i(TAG, "HttpPoster finished ");
                }
            });

            httpRequestSender.postRequest();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Start download database file from server 
     */
    public void downloadFile(final String downloadUrl, final String completeMD5, final IUpdateListener updateListener, final int serverVersion) {
        HttpDownloader downloadManager = HttpDownloader.getInstance(mContext);
        final DownLoadCallback downLoadCallback = new DownLoadCallback(downloadUrl) {
            @Override
            public void onSuccess(final String rootPath, final String downloadedFileName) {
                final String filePath = rootPath + File.separator + downloadedFileName;
                Log.d(TAG, "###### downloadFile success, file = " + filePath);

                if (FileUtils.isFileExist(filePath)) {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            // Verify the MD5 and de-compress the download file
                            final File fileZip = new File(filePath);
                            String fileMD5 = null;
                            try {
                                fileMD5 = MD5Util.getFileMD5String(fileZip);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (fileMD5 != null && fileMD5.equals(completeMD5) && updateListener != null) {
                                // De-compress and copy the file
                                int fileCounts = FileUtils.unzip(rootPath + File.separator, filePath);
                                boolean result = updateListener.onUpdate(mContext, rootPath, fileCounts);
                                if (result) {
                                    //Save the version of download file
                                    updateLocalVersion(serverVersion);
                                    Log.d(TAG, "###### update successfully ! ######");
                                } else {
                                    Log.d(TAG, "###### update failed ! ######");
                                }
                            } else {
                                Log.e(TAG, "Error: The MD5 of the file is incorrect");
                            }

                            // Delete the downloaded zip file
                            if (fileZip.exists()) {
                                fileZip.delete();
                            }
                        }
                    }).start();

                } else {
                    Log.e(TAG, "Error: The file is not existing");
                }
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onProgress(long totalSize, long currentSize, long speed) {
            }

            @Override
            public void onFailure(String strMsg) {
            }

            @Override
            public void onFinish() {
            }
        };

        downloadManager.startDownload(downloadUrl, downLoadCallback);
    }
}
