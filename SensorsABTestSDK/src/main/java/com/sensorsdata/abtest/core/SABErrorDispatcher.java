/*
 * Created by zhangxiangwei on 2020/09/12.
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

package com.sensorsdata.abtest.core;

import com.sensorsdata.abtest.entity.SABErrorEnum;
import com.sensorsdata.analytics.android.sdk.SALog;

public class SABErrorDispatcher {

    private static final String TAG = "SAB.GlobalException";

    public static void dispatchSABException(SABErrorEnum exceptionEnum, Object... arrays) {
        try {
            String message = "";
            if (arrays != null && arrays.length > 0) {
                message = String.format(exceptionEnum.message, arrays);
            } else {
                message = exceptionEnum.message;
            }
            String printLog = "error code: " + exceptionEnum.code + " , message: " + message;
            SALog.i(TAG, printLog);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }
}
