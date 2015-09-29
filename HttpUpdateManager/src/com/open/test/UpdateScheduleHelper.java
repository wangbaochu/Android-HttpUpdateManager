package com.open.test;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class UpdateScheduleHelper {
    
    /**
     * Start to schedule Alarm. Alarm will happen at time in SystemClock.elapsedRealtime() (time since boot, including sleep).
     * @param context
     * @param action Action to perform when the alarm goes off; typically comes from IntentSender.getBroadcast().
     * @param intervalMillis Interval in milliseconds between subsequent repeats of the alarm.
     */
    public static void startRepeatedSchedule(Context context, String action, long intervalMillis) {
        long firstime = SystemClock.elapsedRealtime() + intervalMillis;
        startRepeatedSchedule(context, action, firstime, intervalMillis);
    }

    /**
     * Start to schedule Alarm. Alarm will happen at time in SystemClock.elapsedRealtime() (time since boot, including sleep).
     * @param context
     * @param action Action to perform when the alarm goes off; typically comes from IntentSender.getBroadcast().
     * @param firstime  Time in milliseconds that the alarm should first go off, using the appropriate clock (depending on the alarm type).
     * @param intervalMillis Interval in milliseconds between subsequent repeats of the alarm.
     */
    public static void startRepeatedSchedule(Context context, String action, long firstime, long intervalMillis) {
        try {
            PendingIntent sender = buildSender(context, action);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, intervalMillis, sender);
        } catch (NullPointerException ex) {
        }
    }
    
    public static void startOneShotSchedule(Context context, String action, long intervalMillis) {
        try {
            PendingIntent sender = buildSender(context, action);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + intervalMillis, sender);
        } catch (NullPointerException ex) {
        }
    }

    /**
     * Stop the scheduled Alarm
     */
    public static void stopSchedule(Context context, String action) {
        PendingIntent sender = buildSender(context, action);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(sender);
    }

    private static PendingIntent buildSender(Context context, String action) {
        Intent intent = new Intent(context, UpdateScheduleReceiver.class);
        intent.setAction(action);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return sender;
    }
}
