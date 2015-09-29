package com.open.net;

import org.json.JSONObject;

public class ServerPostData {

	JSONObject mBody;

	public ServerPostData(JSONObject data) {
	    mBody = new JSONObject();
		try {
			if (data != null) {
			    mBody.put("data", data);
			}

			//mBody.put("token", Util.getSecurityToken(MainApplication.getContext()));
			mBody.put("timestamp", String.valueOf(System.currentTimeMillis()));
			//body.put("appKey", Constants.getAppKey());
			mBody.put("sign", "");
			// if (!AliuserSdkManager.getInstance().getIsProtected()) {
			//String sToken = AliuserSdkManager.getInstance().getToken();
			//if (!TextUtils.isEmpty(sToken)) {
			//	body.put("stoken", sToken);
			//
			// }
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void putData(String key, Object value) {
		if (mBody != null) {
			try {
			    mBody.put(key, value);
			} catch (Exception e) {
			}
		}
	}

	public String toString() {
		return mBody.toString();
	}
}
