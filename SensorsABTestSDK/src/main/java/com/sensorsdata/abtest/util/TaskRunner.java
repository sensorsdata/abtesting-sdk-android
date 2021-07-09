/*
 * Created by zhangxiangwei on 2020/09/11.
 * Copyright 2015－2021 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sensorsdata.abtest.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;


public class TaskRunner {
    private static final String TAG = "SAB.TaskRunner";

    private Handler mBackHandler;
    private Handler mUiThreadHandler;

    private static class SingletonHolder {
        static TaskRunner sRunner = new TaskRunner();
    }

    public static synchronized Handler getBackHandler() {
        Handler backHandler = SingletonHolder.sRunner.mBackHandler;
        if (null == backHandler) {
            HandlerThread handlerThread = new HandlerThread("TaskRunner");
            handlerThread.start();
            backHandler = new Handler(handlerThread.getLooper());
            SingletonHolder.sRunner.mBackHandler = backHandler;
        }
        return backHandler;
    }

    public static synchronized Handler getUiThreadHandler() {
        Handler uiHandler = SingletonHolder.sRunner.mUiThreadHandler;
        if (uiHandler == null) {
            uiHandler = new Handler(Looper.getMainLooper());
            SingletonHolder.sRunner.mUiThreadHandler = uiHandler;
        }
        return uiHandler;
    }
}

