/*
 * Created by zhangxiangwei on 2020/09/16.
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

package com.sensorsdata.abtest.entity;

public interface AppConstants {
    String AB_TEST_SUCCESS = "SUCCESS";
    String AB_TEST_FAILURE = "FAILED";
    String AB_TEST_CACHE_FILE_NAME = "com.sensorsdata.abtest.cache";

    interface Property {
        interface Key {
            String CUSTOM_IDS = "custom_ids";
            String EXPERIMENT_CACHE_KEY = "key_experiment_with_distinct_id";
            String ABTEST_TRIGGER = "abtest_trigger";
        }
    }
}
