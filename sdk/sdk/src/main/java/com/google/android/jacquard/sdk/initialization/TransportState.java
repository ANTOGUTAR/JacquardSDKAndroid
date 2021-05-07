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
package com.google.android.jacquard.sdk.initialization;

import com.google.android.jacquard.sdk.model.ProtocolSpec;
import com.google.android.jacquard.sdk.util.Fragmenter;

/** Tracks transport state for a connection. */
class TransportState {

  final Fragmenter commandFragmenter;
  final Fragmenter notificationFragmenter;
  final Fragmenter dataFragmenter;

  private int requestId = 0;

  TransportState(Fragmenter commandFragmenter, Fragmenter notificationFragmenter, Fragmenter dataFragmenter) {
    this.commandFragmenter = commandFragmenter;
    this.notificationFragmenter = notificationFragmenter;
    this.dataFragmenter = dataFragmenter;
  }

  TransportState() {
    this(new Fragmenter("commandFragmenter",
            ProtocolSpec.VERSION_2.getMtuSize()),
        new Fragmenter("notificationFragmenter",
            ProtocolSpec.VERSION_2.getMtuSize()),
        new Fragmenter("dataFragmenter",
            ProtocolSpec.VERSION_2.getMtuSize()));
  }

  /** Returns the last request Id. This is used when re-trying sending a request. */
  int getLastRequest() {
    return requestId;
  }

  /** Returns the next request Id. */
  int getNextRequest() {
    // Protocol v2 only uses 1...255 for request id.
    requestId = (requestId + 1) % 255;
    return requestId;
  }
}
