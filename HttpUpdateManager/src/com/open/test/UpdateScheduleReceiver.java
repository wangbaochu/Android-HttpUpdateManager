package com.open.test;

import com.open.net.IUpdateListener;
import com.open.utils.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class UpdateScheduleReceiver extends BroadcastReceiver {

    private static final String TAG = "UpdateManager";
    public static final String ACTION_INTENT_UPDATE = "com.test.action.download";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "UpdateScheduleReceiver receive: " + intent.getAction());
        
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_INTENT_UPDATE)) {
                UpdateManager updateManger = new UpdateManager(context);
                updateManger.updateDatabaseFromServer(new IUpdateListener() {
                    @Override
                    public boolean onUpdate(Context context, String rootPath, int fileNumbers) {
                        Toast.makeText(context, "Update success: fileNumbers = " + fileNumbers,  Toast.LENGTH_LONG).show();
                        return false;
                    }
                });
            }
        }
    }
}
