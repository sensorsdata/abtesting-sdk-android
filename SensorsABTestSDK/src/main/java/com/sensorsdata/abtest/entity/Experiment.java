/*
 * Created by zhangxiangwei on 2020/09/09.
 * Copyright 2015－2020 Sensors Data Inc.
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


import android.text.TextUtils;

import com.sensorsdata.analytics.android.sdk.SALog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Experiment {

    private static final String TAG = "SAB.Experiment";
    public String experimentId;
    public String experimentGroupId;
    public boolean isControlGroup;
    public boolean isWhiteList;
    public List<Variable> variables;

    public static class Variable {
        public String name;
        public String value;
        public String type;

        @Override
        public String toString() {
            return "Variable{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    public <T> boolean checkTypeIsValid(String paramName, T defaultValue) {
        if (defaultValue == null) {
            return true;
        }

        Variable variable = getVariableByParamName(paramName);
        if (variable == null || TextUtils.isEmpty(variable.type)) {
            SALog.i(TAG, "checkTypeIsValid type is null");
            return false;
        }

        SALog.i(TAG, "checkTypeIsValid params type: " + defaultValue.getClass().toString() + ", experiment type:" + variable.type);
        switch (variable.type) {
            case "INTEGER":
                return defaultValue instanceof Integer;
            case "BOOLEAN":
                return defaultValue instanceof Boolean;
            case "STRING":
                return defaultValue instanceof String;
            case "JSON":
                return defaultValue instanceof JSONObject;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T> T getVariableValue(String paramName, T defaultValue) {
        T result = null;

        Variable variable = getVariableByParamName(paramName);
        if (variable == null || TextUtils.isEmpty(variable.type)) {
            SALog.i(TAG, "getExperimentVariable is null");
            return null;
        }

        // 如果用户传入 null，则以服务端类型为准
        try {
            if (defaultValue == null) {
                switch (variable.type) {
                    case "INTEGER":
                        Integer integer = Integer.parseInt(variable.value);
                        result = (T) integer;
                        break;
                    case "BOOLEAN":
                        Boolean aBoolean = Boolean.parseBoolean(variable.value);
                        result = (T) aBoolean;
                        break;
                    case "STRING":
                        String s = String.valueOf(variable.value);
                        result = (T) s;
                        break;
                    case "JSON":
                        try {
                            JSONObject jsonObject = new JSONObject(variable.value);
                            result = (T) jsonObject;
                        } catch (JSONException e) {
                            SALog.printStackTrace(e);
                        }
                        break;
                }
                return result;
            }
            // 如果用户传入类型不为 null ，则以传入类型为准
            if (defaultValue instanceof Integer) {
                Integer integer = Integer.parseInt(variable.value);
                result = (T) integer;
            } else if (defaultValue instanceof Boolean) {
                Boolean aBoolean = Boolean.parseBoolean(variable.value);
                result = (T) aBoolean;
            } else if (defaultValue instanceof String) {
                String s = String.valueOf(variable.value);
                result = (T) s;
            } else if (defaultValue instanceof JSONObject) {
                try {
                    JSONObject jsonObject = new JSONObject(variable.value);
                    result = (T) jsonObject;
                } catch (JSONException e) {
                    SALog.printStackTrace(e);
                }
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return result;
    }

     public Variable getVariableByParamName(String paramName) {
        if (variables != null && variables.size() > 0) {
            for (Variable variable : variables) {
                if (TextUtils.equals(paramName, variable.name)) {
                    return variable;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Experiment{" +
                "experimentId='" + experimentId + '\'' +
                ", experimentGroupId='" + experimentGroupId + '\'' +
                ", isControlGroup=" + isControlGroup +
                ", isWhiteList=" + isWhiteList +
                ", variables=" + variables +
                '}';
    }
}
