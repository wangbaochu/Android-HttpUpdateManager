
package com.open.net;

public abstract class DownLoadCallback {

	private String mUrl;
	public String getUrl() {
	    return mUrl;
	}
	
	public DownLoadCallback(String url){
		mUrl = url;
	}

	public abstract void onStart();
	
	public abstract void onProgress(long totalSize, long currentSize, long speed);
	
	public abstract void onSuccess(final String rootPath, final String downloadedFileName);

	public abstract void onFailure(String strMsg);

	public abstract void onFinish();
}
