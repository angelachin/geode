/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.statistics;

import org.apache.geode.stats.common.statistics.GFSStatsImplementer;
import org.apache.geode.stats.common.statistics.StatisticDescriptor;
import org.apache.geode.stats.common.statistics.Statistics;
import org.apache.geode.stats.common.statistics.StatisticsFactory;
import org.apache.geode.stats.common.statistics.StatisticsType;

/**
 * Statistics related to the statistic sampler.
 */
public class StatSamplerStatsImpl implements StatSampleStats, GFSStatsImplementer {
  private static final String SAMPLE_COUNT = "sampleCount"; // int
  private static final String SAMPLE_TIME = "sampleTime"; // long
  private static final String DELAY_DURATION = "delayDuration"; // int
  private static final String STAT_RESOURCES = "statResources"; // int
  private static final String JVM_PAUSES = "jvmPauses"; // int
  private static final String SAMPLE_CALLBACKS = "sampleCallbacks"; // int
  private static final String SAMPLE_CALLBACK_ERRORS = "sampleCallbackErrors"; // int
  private static final String SAMPLE_CALLBACK_DURATION = "sampleCallbackDuration"; // long
  private final Statistics samplerStats;
  private StatisticsType samplerType;
  private int sampleCountId;
  private int sampleTimeId;
  private int delayDurationId;
  private int statResourcesId;
  private int jvmPausesId;
  private int sampleCallbacksId;
  private int sampleCallbackErrorsId;
  private int sampleCallbackDurationId;

  public StatSamplerStatsImpl(StatisticsFactory factory, String id) {
    initializeStats(factory);
    this.samplerStats = factory.createStatistics(samplerType, "statSampler", Long.parseLong(id));
  }

  public void initializeStats(StatisticsFactory factory) {
    samplerType = factory.createType("StatSampler", "Stats on the statistic sampler.",
        new StatisticDescriptor[] {
            factory.createIntCounter(SAMPLE_COUNT, "Total number of samples taken by this sampler.",
                "samples", false),
            factory.createLongCounter(SAMPLE_TIME, "Total amount of time spent taking samples.",
                "milliseconds", false),
            factory.createIntGauge(DELAY_DURATION,
                "Actual duration of sampling delay taken before taking this sample.",
                "milliseconds", false),
            factory.createIntGauge(STAT_RESOURCES,
                "Current number of statistic resources being sampled by this sampler.", "resources",
                false),
            factory.createIntCounter(JVM_PAUSES,
                "Total number of JVM pauses (which may or may not be full GC pauses) detected by this sampler. A JVM pause is defined as a system event which kept the statistics sampler thread from sampling for 3000 or more milliseconds. This threshold can be customized by setting the system property gemfire.statSamplerDelayThreshold (units are milliseconds).",
                "jvmPauses", false),
            factory.createIntGauge(SAMPLE_CALLBACKS,
                "Current number of statistics that are sampled using callbacks.", "resources",
                false),
            factory.createIntCounter(SAMPLE_CALLBACK_ERRORS,
                "Total number of exceptions thrown by callbacks when performing sampling", "errors",
                false),
            factory.createLongCounter(SAMPLE_CALLBACK_DURATION,
                "Total amount of time invoking sampling callbacks", "milliseconds", false),});
    sampleCountId = samplerType.nameToId(SAMPLE_COUNT);
    sampleTimeId = samplerType.nameToId(SAMPLE_TIME);
    delayDurationId = samplerType.nameToId(DELAY_DURATION);
    statResourcesId = samplerType.nameToId(STAT_RESOURCES);
    jvmPausesId = samplerType.nameToId(JVM_PAUSES);
    sampleCallbacksId = samplerType.nameToId(SAMPLE_CALLBACKS);
    sampleCallbackErrorsId = samplerType.nameToId(SAMPLE_CALLBACK_ERRORS);
    sampleCallbackDurationId = samplerType.nameToId(SAMPLE_CALLBACK_DURATION);
  }

  public void tookSample(long nanosSpentWorking, int statResources, long nanosSpentSleeping) {
    this.samplerStats.incInt(sampleCountId, 1);
    this.samplerStats.incLong(sampleTimeId, nanosSpentWorking / 1000000);
    this.samplerStats.setInt(delayDurationId, (int) (nanosSpentSleeping / 1000000));
    this.samplerStats.setInt(statResourcesId, statResources);
  }

  @Override
  public void incJvmPauses() {
    this.samplerStats.incInt(jvmPausesId, 1);
  }

  @Override
  public void incSampleCallbackErrors(int delta) {
    this.samplerStats.incInt(sampleCallbackErrorsId, delta);
  }

  @Override
  public void setSampleCallbacks(int count) {
    this.samplerStats.setInt(sampleCallbacksId, count);
  }

  @Override
  public void incSampleCallbackDuration(long delta) {
    this.samplerStats.incLong(sampleCallbackDurationId, delta);
  }

  @Override
  public int getSampleCount() {
    return this.samplerStats.getInt(SAMPLE_COUNT);
  }

  @Override
  public long getSampleTime() {
    return this.samplerStats.getLong(SAMPLE_TIME);
  }

  @Override
  public int getDelayDuration() {
    return this.samplerStats.getInt(DELAY_DURATION);
  }

  @Override
  public int getStatResources() {
    return this.samplerStats.getInt(STAT_RESOURCES);
  }

  @Override
  public int getJvmPauses() {
    return this.samplerStats.getInt(JVM_PAUSES);
  }

  public void close() {
    this.samplerStats.close();
  }

  public Statistics getStats() {
    return this.samplerStats;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append("@").append(System.identityHashCode(this)).append("{");
    sb.append("isClosed=").append(this.samplerStats.isClosed());
    sb.append(", ").append(SAMPLE_COUNT + "=").append(this.samplerStats.getInt(SAMPLE_COUNT));
    sb.append(", ").append(SAMPLE_TIME + "=").append(this.samplerStats.getLong(SAMPLE_TIME));
    sb.append(", ").append(DELAY_DURATION + "=").append(this.samplerStats.getInt(DELAY_DURATION));
    sb.append(", ").append(STAT_RESOURCES + "=").append(this.samplerStats.getInt(STAT_RESOURCES));
    sb.append(", ").append(JVM_PAUSES + "=").append(this.samplerStats.getInt(JVM_PAUSES));
    sb.append("}");
    return sb.toString();
  }
}