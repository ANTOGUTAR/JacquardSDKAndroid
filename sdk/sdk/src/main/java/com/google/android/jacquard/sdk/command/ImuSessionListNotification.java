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

package com.google.android.jacquard.sdk.command;

import androidx.annotation.Nullable;
import com.google.android.jacquard.sdk.util.JQUtils;
import com.google.atap.jacquard.protocol.JacquardProtocol.DataCollectionTrialListNotification;

/**
 * Ujt will send this notification per IMU session when {@link ImuSessionListCommand} is submitted.
 */
public class ImuSessionListNotification
    implements NotificationSubscription<DataCollectionTrialListNotification> {

  private final InnerImuSessionListNotificationSubscription notificationSubscription =
      new InnerImuSessionListNotificationSubscription();

  @Nullable
  @Override
  public DataCollectionTrialListNotification extract(byte[] packet) {
    return notificationSubscription.extract(JQUtils.getNotification(packet));
  }
}
