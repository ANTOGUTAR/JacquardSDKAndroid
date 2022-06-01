/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.jacquard.sdk.imu.parser;

import com.google.atap.jacquard.protocol.JacquardProtocol.DataCollectionActionHeader;
import com.google.protos.atap.jacquard.core.Jacquard.ImuSample;
import java.util.List;

/**
 * Interface to parse Imu Samples raw file.
 */
public interface ImuParser {

  /**
   * Parse to get {@link ImuSample}.
   */
  ImuSample parseImuSample(byte[] bytes);

  List<ImuSample> parseImuSamples(byte[] bytes);

  /**
   * Parse to get {@link DataCollectionActionHeader}.
   */
  DataCollectionActionHeader parseActionHeader(byte[] bytes);
}
