/*
 * Created by zhangxiangwei on 2020/09/09.
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

package com.sensorsdata.abtest.util;

import android.os.Handler;

import com.sensorsdata.abtest.core.SensorsABTestApiRequestHelper;
import com.sensorsdata.analytics.android.sdk.SALog;

public class SABAlarmManager implements Runnable {
    private static final String TAG = "SAB." + SABAlarmManager.class.getSimpleName();
    //闹钟执行任务的时间间隔
    private static final int DEFAULT_TIME_INTERVAL = 10 * 60 * 1000;
    private int timeInterval = DEFAULT_TIME_INTERVAL;
    private static SABAlarmManager instance = null;
    private Handler alarmHandler;

    private SABAlarmManager() {
        alarmHandler = TaskRunner.getBackHandler();
    }

    public static SABAlarmManager getInstance() {
        if (instance == null) {
            synchronized (SABAlarmManager.class) {
                if (instance == null) {
                    instance = new SABAlarmManager();
                }
            }
        }
        return instance;
    }

    public void refreshInterval() {
        refreshInterval(DEFAULT_TIME_INTERVAL);
    }

    /**
     * 更新定时任务
     *
     * @param interval 延迟事件单位是毫秒
     */
    public synchronized void refreshInterval(int interval) {
        cancelAlarm();
        timeInterval = interval;
        alarmHandler.postDelayed(this, interval);
    }

    public synchronized void cancelAlarm() {
        alarmHandler.removeCallbacks(this);
    }

    @Override
    public void run() {
        synchronized (this) {
            SALog.i(TAG, "AlarmManager requestExperimentsAndUpdateCache");
            new SensorsABTestApiRequestHelper<>().requestExperimentsAndUpdateCache();
            alarmHandler.postDelayed(this, timeInterval);
        }
    }
}