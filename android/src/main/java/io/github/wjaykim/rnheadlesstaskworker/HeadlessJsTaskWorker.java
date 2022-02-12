package io.github.wjaykim.rnheadlesstaskworker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.jstasks.HeadlessJsTaskContext;
import com.facebook.react.jstasks.HeadlessJsTaskEventListener;
import com.google.common.util.concurrent.ListenableFuture;

public abstract class HeadlessJsTaskWorker extends ListenableWorker implements HeadlessJsTaskEventListener {
    private int taskId;
    private CallbackToFutureAdapter.Completer<Result> mCompleter;

    public HeadlessJsTaskWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            HeadlessJsTaskConfig taskConfig = this.getTaskConfig(this.getInputData());
            mCompleter = completer;
            if (taskConfig != null) {
                this.startTask(taskConfig);
            } else {
                mCompleter.set(Result.failure());
            }
            return "";
        });
    }

    @Nullable
    protected abstract HeadlessJsTaskConfig getTaskConfig(Data data);

    protected void startTask(final HeadlessJsTaskConfig taskConfig) {
        final ReactInstanceManager reactInstanceManager = this.getReactNativeHost().getReactInstanceManager();
        ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
        if (reactContext == null) {
            reactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                public void onReactContextInitialized(ReactContext reactContext) {
                    HeadlessJsTaskWorker.this.invokeStartTask(reactContext, taskConfig);
                    reactInstanceManager.removeReactInstanceEventListener(this);
                }
            });
            reactInstanceManager.createReactContextInBackground();
        } else {
            this.invokeStartTask(reactContext, taskConfig);
        }
    }

    private void invokeStartTask(ReactContext reactContext, final HeadlessJsTaskConfig taskConfig) {
        final HeadlessJsTaskContext headlessJsTaskContext = HeadlessJsTaskContext.getInstance(reactContext);
        headlessJsTaskContext.addTaskEventListener(this);
        UiThreadUtil.runOnUiThread(() -> this.taskId = headlessJsTaskContext.startTask(taskConfig));
    }

    private void cleanUpTask() {
        if (this.getReactNativeHost().hasInstance()) {
            ReactInstanceManager reactInstanceManager = this.getReactNativeHost().getReactInstanceManager();
            ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
            if (reactContext != null) {
                HeadlessJsTaskContext headlessJsTaskContext = HeadlessJsTaskContext.getInstance(reactContext);
                headlessJsTaskContext.removeTaskEventListener(this);
            }
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
        cleanUpTask();
    }

    @Override
    public void onHeadlessJsTaskStart(int taskId) {

    }

    @Override
    public void onHeadlessJsTaskFinish(int taskId) {
        if (this.taskId == taskId) {
            if (this.mCompleter != null) {
                this.mCompleter.set(Result.success());
                cleanUpTask();
            }
        }
    }

    protected ReactNativeHost getReactNativeHost() {
        return ((ReactApplication) this.getApplicationContext()).getReactNativeHost();
    }
}
