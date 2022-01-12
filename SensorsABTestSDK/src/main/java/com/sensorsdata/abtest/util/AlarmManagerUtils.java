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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sensorsdata.abtest.service.GlobalLoopService;
import com.sensorsdata.analytics.android.sdk.SALog;

public class AlarmManagerUtils {
    //闹钟执行任务的时间间隔
    private static final int TIME_INTERVAL = 10 * 60 * 1000;
    private AlarmManager mAlarm;
    private PendingIntent mPendingIntent;
    private static AlarmManagerUtils instance = null;

    private AlarmManagerUtils(Context context) {
        try {
            mAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, GlobalLoopService.class);
            int flag = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flag = PendingIntent.FLAG_IMMUTABLE;
            }
            mPendingIntent = PendingIntent.getService(context, 0, intent, flag);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    public static AlarmManagerUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (AlarmManagerUtils.class) {
                if (instance == null) {
                    instance = new AlarmManagerUtils(context);
                }
            }
        }
        return instance;
    }

    public void setUpAlarm() {
        try {
            cancelAlarm();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TIME_INTERVAL, mPendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mAlarm.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TIME_INTERVAL, mPendingIntent);
            } else {
                mAlarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), TIME_INTERVAL, mPendingIntent);
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    public void cancelAlarm() {
        try {
            mAlarm.cancel(mPendingIntent);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    public void setUpAlarmOnReceiver() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TIME_INTERVAL, mPendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mAlarm.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TIME_INTERVAL, mPendingIntent);
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }
}