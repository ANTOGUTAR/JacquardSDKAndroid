/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.jacquard.sdk.command;

import androidx.annotation.Nullable;
import com.google.atap.jacquard.protocol.JacquardProtocol.Notification;

/** Interface for subscribing to notifications from the tag. */
public interface NotificationSubscription<Type> {

  /**
   * Extracts the payload from the notification. Returns null if the notification is not the
   * expected notification.
   */
  @Nullable
  Type extract(Notification notification);
}
