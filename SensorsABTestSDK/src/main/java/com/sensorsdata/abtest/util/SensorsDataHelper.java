/*
 * Created by luweibin on 2021/10/22.
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

import android.text.TextUtils;
import android.util.Pair;

import com.sensorsdata.abtest.exception.DataInvalidException;
import com.sensorsdata.analytics.android.sdk.util.TimeUtils;

import org.json.JSONArray;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SensorsDataHelper {

    private static final Pattern KEY_PATTERN = Pattern.compile(
            "^((?!^distinct_id$|^original_id$|^time$|^properties$|^id$|^first_id$|^second_id$|^users$|^events$|^event$|^user_id$|^date$|^datetime$|^device_id$|^event_id$|^user_group[\\S]*$|^user_tag[\\S]*$)[a-zA-Z_][a-zA-Z\\d_]{0,100})$",
            Pattern.CASE_INSENSITIVE);
    private static final int PROPERTY_VALUE_MAX_LENGTH = 8191;

    public static Map<String, String> checkPropertiesAndToString(Map<String, Object> properties) throws DataInvalidException {
        if (properties != null && properties.size() != 0) {
            Map<String, String> stringProperties = new HashMap<>(properties.size());
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                // step1.校验自定义参数名和参数值
                checkProperty(entry);

                // step2.转换自定义参数值为 String 类型
                Pair<String, String> stringProperty = propertiesToString(entry);

                // step3.将转换的 Key-Value 作为返回值
                stringProperties.put(stringProperty.first, stringProperty.second);
            }
            return stringProperties;
        }
        return null;
    }

    private static Pair<String, String> propertiesToString(Map.Entry<String, Object> entry) throws DataInvalidException {
        Object value = entry.getValue();
        String stringValue;
        if (value instanceof Date) {
            stringValue = TimeUtils.formatDate((Date) value);
        } else if (value instanceof List) {
            JSONArray jsonArray = new JSONArray();
            for (Object item : (List<?>) value) {
                jsonArray.put(item.toString());
            }
            stringValue = jsonArray.toString();
        } else {
            stringValue = value.toString();
        }
        if (stringValue.length() > PROPERTY_VALUE_MAX_LENGTH) {
            throw new DataInvalidException(createStringValueInvalidMsg(entry.getKey(), stringValue));
        }
        return new Pair<>(entry.getKey(), stringValue);
    }

    public static boolean isKeyMatch(String key) {
        return KEY_PATTERN.matcher(key).matches();
    }

    private static void checkProperty(Map.Entry<String, Object> entry) throws DataInvalidException {
        String key = entry.getKey();
        Object value = entry.getValue();
        if (TextUtils.isEmpty(key)) {
            throw new DataInvalidException(createKeyInvalidMsg(key));
        }
        if (key.length() > 100) {
            throw new DataInvalidException(createKeyInvalidMsg(key));
        }
        if (!isKeyMatch(key)) {
            throw new DataInvalidException(createKeyInvalidMsg(key));
        }
        if (value == null) {
            throw new DataInvalidException(createValueInvalidMsg(key, null));
        }
        if (!(value instanceof CharSequence || value instanceof Number || value instanceof List ||
                value instanceof Boolean || value instanceof Date)) {
            throw new DataInvalidException(createValueInvalidMsg(key, value));
        }
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                if (!(item instanceof String)) {
                    throw new DataInvalidException(createValueInvalidMsg(key, value));
                }
            }
        }
    }

    private static String createKeyInvalidMsg(String key) {
        return "property name [ " + key + " ] is not valid";
    }

    private static String createValueInvalidMsg(String key, Object value) {
        return "property values must be String, Number, List<String>, Boolean or Date. property " +
                "[ " + key + " ] of value " +
                "[ " + (value == null ? "null" : value.toString()) + " ] is not valid";
    }

    private static String createStringValueInvalidMsg(String key, Object value) {
        return "property [ " + key + " ] of value [ " + (value == null ? "null" : value.toString()) + " ] is not valid";
    }
}
