package com.open.net;

import android.content.Context;
import android.os.Build;

import org.apache.http.NameValuePair;

import com.open.utils.Log;
import com.open.utils.Util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Executor;

public class HttpPoster implements Runnable {
    
    private static final String TAG = "UpdateManager";

    /** 从socket读数据时发生阻塞的超时时间 */
    public static final int DEFAULT_TIMEOUT_MS = 60 * 1000;
    /** 连接的超时时间 */
    public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 20 * 1000;

    private static int DEFAULT_POOL_SIZE = 4096;

    /** post数据 */
    private Map<String, String> postData = new HashMap<String, String>();

    private Context mContext;
    protected String url = "";
    public String mCookie = null;
    private byte[] binaryData = null;
    private TreeMap<String, String> headerData = new TreeMap<String, String>();
    private IRequstListenser mRequestCallBack = null;
    private HttpSerialExecutor mAliSerialExecutor = null;
    private ServerPostData mPostBodyData = null;

    public HttpPoster(Context context, String url, ServerPostData data, IRequstListenser callback) throws Exception {
        mContext = context;
        mAliSerialExecutor = new HttpSerialExecutor();
        mRequestCallBack = callback;
        
        if (url.contains("http://")) {
            this.url = url;
        } else {
            throw new Exception("download url = " + url + " is not a valid http://");
        }

        if (data != null) {
            mPostBodyData = data;
        }
    }

    public void setBinaryData(byte[] data) {
        binaryData = data;
    }

    public void setCookie(String cookie) {
        mCookie = cookie;
    }

    public void setHeaderData(String key, String value) {
        headerData.put(key, value);
    }

    private void onError(final Throwable e) {
        if (mRequestCallBack != null) {
            mRequestCallBack.error(e);
        } else {
            e.printStackTrace();
        }
    }

    private void onFinish() {
        if (mRequestCallBack != null) {
            if (mRequestCallBack != null) {
                mRequestCallBack.finish();
            }
        }
    }

    private void onNotNetConnection() {
        if (mRequestCallBack != null) {
            if (mRequestCallBack != null) {
                mRequestCallBack.notNetConnection();
            }
        }
    }

    /**
     * Handle the HTTP response data
     */
    private void onHandleData(DataInputStream dis) throws Exception {
        
        Log.d(TAG, "start onHandleData()");
        
        BufferedReader in = new BufferedReader(new InputStreamReader(dis, "UTF8"));
        StringBuilder inputLine = new StringBuilder();
        String tmp;
        while ((tmp = in.readLine()) != null) {
            inputLine.append(tmp);
        }

        String json = inputLine.toString();
        if (mRequestCallBack != null) {
            if (mRequestCallBack != null) {
                mRequestCallBack.handleData(json);
            }
        }

        dis.close();
    }

    /** 设置post参数 */
    public void setPostData(String key, String value) {
        postData.put(key, value);
    }

    public void setPostData(List<NameValuePair> datas) {
        for (NameValuePair nameValuePair : datas) {
            setPostData(nameValuePair.getName(), nameValuePair.getValue());
        }
    }

    /**
     * Execute the post HTTP request.
     */
    public void postRequest() {
        if (mAliSerialExecutor.getExecutor() == null) {
            mAliSerialExecutor.setExecutor(new Executor() {
                @Override
                public void execute(Runnable task) {
                    new Thread(task).start(); 
                }
            });
        }

        mAliSerialExecutor.execute(this);
    }

    /**
     * Start to connect to server and parsing the response data. 
     * @param context
     * @param strUrl
     * @return
     */
    protected byte[] performRequest(Context context, String strUrl) {
        
        Log.d(TAG, "start performRequest");
        
        HttpURLConnection conn = null;
        try {
            conn = initConn(strUrl);
            if(conn == null){
                return null;
            }
            try {
                conn.connect();
            } catch (UnknownHostException e){
                if (conn != null) {
                    conn.disconnect();
                }
                onError(e);
            }

            if (binaryData != null) {
                conn.getOutputStream().write(binaryData);
                conn.getOutputStream().flush();
                conn.getOutputStream().close();
            } else if (mPostBodyData != null) {
                String requestData = mPostBodyData.toString();
                byte[] tranData = requestData.getBytes("utf-8");
                conn.getOutputStream().write(tranData);
                conn.getOutputStream().flush();
                conn.getOutputStream().close();
            }

            int responseCode = conn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == responseCode) {
                byte[] bytes = entityToBytes(conn);
                return bytes;
            } else {
                onError(new Throwable("responseCode=" + responseCode));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            onError(ex);
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Initialize the server HTTP connection
     * @param strUrl
     * @return
     */
    private HttpURLConnection initConn(String strUrl){
        HttpURLConnection conn = null;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                System.setProperty("http.keepAlive", "false");
            }

            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(DEFAULT_TIMEOUT_MS);
            conn.setReadTimeout(DEFAULT_CONNECTION_TIMEOUT_MS);
            conn.setRequestMethod("GET");

            if (mCookie != null) {
                conn.addRequestProperty("Cookie", mCookie);
            }

            if (mPostBodyData != null || binaryData != null) {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setUseCaches(false);
            }

            if (binaryData != null) {
                conn.setRequestProperty("Content-Type", "application/octet-stream ");
            } else {
                conn.setRequestProperty("Content-Type", "text/plain");
            }
            // conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");

            // 设置header数据
            if (headerData.size() > 0) {
                Iterator<Entry<String, String>> iter = headerData.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, String> entry = iter.next();
                    String key = entry.getKey();
                    String val = "";
                    try {
                        val = URLEncoder.encode(entry.getValue(), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    conn.setRequestProperty(key, val);
                }
            }

            return conn;
        } catch (Exception ex) {
            ex.printStackTrace();
            onError(ex);
        } finally {
            try {
                if (conn != null)
                    conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /***
     * Convert the HTTP response into byte data.
     * @param conn
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    protected byte[] entityToBytes(HttpURLConnection conn) throws IllegalStateException, IOException {
        ByteArrayPool mPool = new ByteArrayPool(DEFAULT_POOL_SIZE);;
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool, (int) conn.getContentLength());
        byte[] buffer = null;
        InputStream in = null;
        try {
            in = conn.getInputStream();
            buffer = mPool.getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }

    @Override
    public void run() {
        
        Log.d(TAG, "start run http post request");
        
        if (this.url == null) {
            onError(new Throwable("url = null"));
            onFinish();
            return;
        }

        if (!Util.isConnNetwork(mContext)) {
            onNotNetConnection();
        } else {
            try {
                byte[] data = performRequest(mContext, url);
                if (data != null) {
                    DataInputStream dis = null;
                    dis = new DataInputStream(new ByteArrayInputStream(data));
                    onHandleData(dis);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                onError(ex);
            }
        }
        onFinish();
    }


    //    public byte[] performRequest(String url) {
    //        DefaultHttpClient httpclient = null;
    //        try {
    //            HttpResponse response = null;
    //            httpclient = new DefaultHttpClient();
    //            httpclient.setRedirectHandler(new RedirectHandler() {
    //                @Override
    //                public boolean isRedirectRequested(HttpResponse arg0, HttpContext arg1) {
    //                    return false;
    //                }
    //
    //                @Override
    //                public URI getLocationURI(HttpResponse arg0, HttpContext arg1) throws ProtocolException {
    //                    return null;
    //                }
    //            });
    //            
    //            HttpParams httpParams = httpclient.getParams();
    //            httpParams.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
    //            httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_TIMEOUT_MS); 
    //            httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT_MS);
    //
    //            if (postData != null && postData.size() > 0) {
    //                // 请求为post
    //                HttpPost httppost = new HttpPost(url);
    //                addCommonHeader(httppost);
    //                httppost.setEntity(new UrlEncodedFormEntity(buildPostData()));
    //                response = httpclient.execute(httppost);
    //            } else {
    //                // 请求为get
    //                HttpGet httpget = new HttpGet(url);
    //                addCommonHeader(httpget);
    //                response = httpclient.execute(httpget);
    //            }
    //
    //            int reponseCode = response.getStatusLine().getStatusCode();
    //            if (reponseCode == HttpStatus.SC_OK) {
    //                byte[] bytes = entityToBytes(response.getEntity());
    //                return bytes;
    //            } else {
    //                onError(null);
    //            }
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            onError(ex);
    //        } finally {
    //            if (httpclient != null) {
    //                httpclient.getConnectionManager().shutdown();
    //                httpclient = null;
    //            }
    //        }
    //        return null;
    //    }
}
