/*
 * Created by luweibin on 2021/10/21.
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

package com.sensorsdata.abtest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorsABTestExperiment<T> {
    protected final String paramName;
    protected final T defaultValue;
    protected Map<String, Object> properties;
    protected int timeoutMillSeconds = SensorsABTest.TIMEOUT_REQUEST;

    private SensorsABTestExperiment(String paramName, T defaultValue) {
        this.paramName = paramName;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "SensorsABTestExperiment{" +
                "paramName='" + paramName + '\'' +
                ", defaultValue=" + defaultValue +
                ", properties=" + properties +
                ", timeoutMillSeconds=" + timeoutMillSeconds +
                '}';
    }

    public static <T> ExperimentBuilder<T> newBuilder(String paramName, T defaultValue) {
        return new ExperimentBuilder<>(paramName, defaultValue);
    }

    public static class ExperimentBuilder<T> {
        private final SensorsABTestExperiment<T> experiment;

        private ExperimentBuilder(String paramName, T defaultValue) {
            experiment = new SensorsABTestExperiment<>(paramName, defaultValue);
        }

        public ExperimentBuilder<T> addProperty(String propertyKey, CharSequence propertyValue) {
            addObjectProperty(propertyKey, propertyValue);
            return this;
        }

        public ExperimentBuilder<T> addProperty(String propertyKey, boolean propertyValue) {
            addObjectProperty(propertyKey, propertyValue);
            return this;
        }

        public ExperimentBuilder<T> addProperty(String propertyKey, Number propertyValue) {
            addObjectProperty(propertyKey, propertyValue);
            return this;
        }

        public ExperimentBuilder<T> addProperty(String propertyKey, List<String> propertyValue) {
            addObjectProperty(propertyKey, propertyValue);
            return this;
        }

        public ExperimentBuilder<T> addProperty(String propertyKey, Date propertyValue) {
            addObjectProperty(propertyKey, propertyValue);
            return this;
        }

        private void addObjectProperty(String propertyKey, Object propertyValue) {
            if (experiment.properties == null) {
                experiment.properties = new HashMap<>();
            }
            experiment.properties.put(propertyKey, propertyValue);
        }

        public ExperimentBuilder<T> setTimeoutMillSeconds(int timeoutMillSeconds) {
            experiment.timeoutMillSeconds = timeoutMillSeconds;
            return this;
        }

        public SensorsABTestExperiment<T> create() {
            return experiment;
        }
    }
}
