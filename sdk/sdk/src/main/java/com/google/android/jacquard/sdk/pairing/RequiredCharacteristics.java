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
package com.google.android.jacquard.sdk.pairing;

import android.bluetooth.BluetoothGattCharacteristic;

/** Data class for holding the required characteristic for a jacquard tag. */
public class RequiredCharacteristics {

  public final BluetoothGattCharacteristic commandCharacteristic;
  public final BluetoothGattCharacteristic responseCharacteristic;
  public final BluetoothGattCharacteristic notifyCharacteristic;
  public final BluetoothGattCharacteristic batteryCharacteristic;
  public final BluetoothGattCharacteristic rawCharacteristic;

  public RequiredCharacteristics(
      BluetoothGattCharacteristic commandCharacteristic,
      BluetoothGattCharacteristic responseCharacteristic,
      BluetoothGattCharacteristic notifyCharacteristic,
      BluetoothGattCharacteristic batteryCharacteristic,
      BluetoothGattCharacteristic rawCharacteristic) {
    this.commandCharacteristic = commandCharacteristic;
    this.responseCharacteristic = responseCharacteristic;
    this.notifyCharacteristic = notifyCharacteristic;
    this.batteryCharacteristic = batteryCharacteristic;
    this.rawCharacteristic = rawCharacteristic;
  }
}
