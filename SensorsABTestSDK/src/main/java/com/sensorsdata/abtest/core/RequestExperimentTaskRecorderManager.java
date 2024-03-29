/*
 * Created by luweibin on 2021/11/15.
 * Copyright 2020－2022 Sensors Data Inc.
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

package com.sensorsdata.abtest.core;

import com.sensorsdata.abtest.OnABTestReceivedData;
import com.sensorsdata.abtest.entity.RequestingExperimentInfo;
import com.sensorsdata.analytics.android.sdk.SALog;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RequestExperimentTaskRecorderManager {
    private static final String TAG = "SAB.RequestExperimentTaskManager";
    private final List<RequestExperimentTaskRecorder> mTaskList = new LinkedList<>();

    private static class SingleHolder {
        private static final RequestExperimentTaskRecorderManager INSTANCE = new RequestExperimentTaskRecorderManager();
    }

    private RequestExperimentTaskRecorderManager() {
    }

    static RequestExperimentTaskRecorderManager getInstance() {
        return SingleHolder.INSTANCE;
    }

    synchronized <T> RequestExperimentTaskRecorder createRequest(String loginId, String anonymousId, String customIDs, String paramName,
                                                                 Map<String, Object> properties, int timeoutMillSeconds,
                                                                 OnABTestReceivedData<T> onABTestReceivedData, T defaultValue) {
        SALog.i(TAG, "create new request task");
        RequestExperimentTaskRecorder currentTask = new RequestExperimentTaskRecorder(loginId, anonymousId, customIDs, paramName, properties, timeoutMillSeconds);
        mTaskList.add(currentTask);
        currentTask.addRequestingExperimentInfo(new RequestingExperimentInfo(onABTestReceivedData, paramName, defaultValue));
        currentTask.setIsMergedTask(false);
        return currentTask;
    }

    synchronized <T> RequestExperimentTaskRecorder mergeRequest(String loginId, String anonymousId, String customIDs, String paramName,
                                                                Map<String, Object> properties, int timeoutMillSeconds,
                                                                OnABTestReceivedData<T> onABTestReceivedData, T defaultValue) {
        boolean isTaskExist = false;
        RequestExperimentTaskRecorder currentTask = null;
        for (RequestExperimentTaskRecorder task : mTaskList) {
            if (task.isSameExperimentTask(loginId, anonymousId, customIDs, paramName, properties, timeoutMillSeconds)) {
                currentTask = task;
                isTaskExist = true;
                break;
            }
        }
        if (currentTask == null) {
            currentTask = new RequestExperimentTaskRecorder(loginId, anonymousId, customIDs, paramName, properties, timeoutMillSeconds);
            mTaskList.add(currentTask);
            isTaskExist = false;
        }
        SALog.i(TAG, "create new request task if not exist, task is exist = " + isTaskExist);
        currentTask.addRequestingExperimentInfo(new RequestingExperimentInfo(onABTestReceivedData, paramName, defaultValue));
        currentTask.setIsMergedTask(isTaskExist);
        return currentTask;
    }

    synchronized void removeTask(RequestExperimentTaskRecorder task) {
        mTaskList.remove(task);
    }
}
