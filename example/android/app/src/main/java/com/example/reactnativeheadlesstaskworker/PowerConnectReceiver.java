package com.example.reactnativeheadlesstaskworker;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.List;

public class PowerConnectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        /*
         This part will be called every time power is connected
         */
        if (!isAppOnForeground((context))) {
            /*
             We will submit work request with extra message
             */
            Data inputData = new Data.Builder()
                    .putString("message", "Power has connected")
                    .build();
            WorkRequest headlessJsTaskWorkRequest =
                    new OneTimeWorkRequest.Builder(MyTaskWorker.class)
                            .setInputData(inputData)
                            .build();
            WorkManager
                    .getInstance(context)
                    .enqueue(headlessJsTaskWorkRequest);
        }
    }

    private boolean isAppOnForeground(Context context) {
        /*
         We need to check if app is in foreground otherwise the app will crash.
         http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
         */
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
