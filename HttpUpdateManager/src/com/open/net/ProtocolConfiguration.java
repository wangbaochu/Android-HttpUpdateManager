package com.open.net;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class ProtocolConfiguration {

    /**
     * Please configure your server host here
     */
    private static final String ONLINE_HOST = "http://xxxx.com";
    private static final String DAILY_HOST = "http://xxxx.com";
    private static final String PRERELEASE_HOST = "http://xxxx.com";
	
    /** 线上环境 */
    public static final int ENVIRONMENT_ONLINE = 0;
    /** 日常环境 */
    public static final int ENVIRONMENT_DAILY = 1;
    /** 预发环境 */
    public static final int ENVIRONMENT_PRE = 2;
    
    /** 
     * Please set this element in AndroidManifest.xml:
     *  <meta-data android:name="run_env" android:value="0"/> 
     */
    public static String getServerHost(Context context) {
        try {
            Bundle meta = getEnvironmentConfig(context);
            if (meta != null && meta.containsKey("run_env")) {
                int env = meta.getInt("run_env");
                if (env == ENVIRONMENT_ONLINE) {
                    return ONLINE_HOST;
                } else if (env == ENVIRONMENT_DAILY) {
                    return DAILY_HOST;
                } else if (env == ENVIRONMENT_PRE) {
                    return PRERELEASE_HOST;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return ONLINE_HOST;
    }
    
    public static boolean isOnlineEnvironment(Context context) {
        Bundle meta = getEnvironmentConfig(context);
        if (meta != null && meta.getInt("run_env") == ENVIRONMENT_ONLINE) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isDailyEnvironment(Context context) {
        Bundle meta = getEnvironmentConfig(context);
        if (meta != null && meta.getInt("run_env") == ENVIRONMENT_DAILY) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isPrereleaseEnvironment(Context context) {
        Bundle meta = getEnvironmentConfig(context);
        if (meta != null && meta.getInt("run_env") == ENVIRONMENT_PRE) {
            return true;
        } else {
            return false;
        }
    }
    
    private static Bundle getEnvironmentConfig(Context context) {
        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
