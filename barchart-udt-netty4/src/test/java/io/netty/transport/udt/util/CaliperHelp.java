/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.transport.udt.util;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.caliper.Environment;
import com.google.caliper.EnvironmentGetter;
import com.google.caliper.Measurement;
import com.google.caliper.MeasurementSet;
import com.google.caliper.Result;
import com.google.caliper.Run;
import com.google.caliper.Runner;
import com.google.caliper.Scenario;
import com.google.caliper.ScenarioResult;

public class CaliperHelp {

    public static void publishResults(final Result result) throws Exception {

        final Runner runner = new Runner();

        final Method method = runner.getClass().getDeclaredMethod(
                "postResults", Result.class);

        method.setAccessible(true);

        method.invoke(runner, result);

    }

    public static void main(final String[] args) throws Exception {

        final Map<String, String> variables = new HashMap<String, String>();
        variables.put("size", "10");
        variables.put("vm", "java");

        final Scenario scenario = new Scenario(variables);

        final Map<String, Integer> units = new HashMap<String, Integer>();
        units.put("BB/sec", 1);
        units.put("KB/sec", 1024);
        units.put("MB/sec", 1024 * 1024);

        final MeasurementSet timeMeasurementSet = new MeasurementSet(
                new Measurement(units, 10000, 10000), new Measurement(units,
                        20000, 20000), new Measurement(units, 22000, 22000),
                new Measurement(units, 30000, 30000));
        final String timeEventLog = null;
        final MeasurementSet instanceMeasurementSet = null;
        final String instanceEventLog = null;
        final MeasurementSet memoryMeasurementSet = null;
        final String memoryEventLog = null;

        final ScenarioResult scenarioResult = new ScenarioResult(
                timeMeasurementSet, timeEventLog, instanceMeasurementSet,
                instanceEventLog, memoryMeasurementSet, memoryEventLog);

        final Map<Scenario, ScenarioResult> measurements = new HashMap<Scenario, ScenarioResult>();
        measurements.put(scenario, scenarioResult);

        final String benchmarkName = "test-1";
        final Date executedTimestamp = new Date();
        final Run run = new Run(measurements, benchmarkName, executedTimestamp);

        final Environment env = new EnvironmentGetter()
                .getEnvironmentSnapshot();

        final Result result = new Result(run, env);

        CaliperHelp.publishResults(result);

    }

}
