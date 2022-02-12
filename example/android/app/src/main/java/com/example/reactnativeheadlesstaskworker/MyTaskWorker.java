package com.example.reactnativeheadlesstaskworker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import io.github.wjaykim.rnheadlesstaskworker.HeadlessJsTaskWorker;

public class MyTaskWorker extends HeadlessJsTaskWorker {
    private static final String TASK_KEY = "MyTask";
    private static final long TIMEOUT_MILLIS = 5000;

    public MyTaskWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Nullable
    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Data data) {
        if (data != null) {
            return new HeadlessJsTaskConfig(
                    TASK_KEY,
                    Arguments.makeNativeMap(data.getKeyValueMap()),
                    TIMEOUT_MILLIS,
                    true
            );
        }
        return null;
    }
}
