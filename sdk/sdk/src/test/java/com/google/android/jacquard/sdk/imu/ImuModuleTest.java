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

package com.google.android.jacquard.sdk.imu;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.jacquard.sdk.FakeJacquardManagerImpl;
import com.google.android.jacquard.sdk.JacquardManagerInitialization;
import com.google.android.jacquard.sdk.imu.InitState.Type;
import com.google.android.jacquard.sdk.imu.model.ImuSessionInfo;
import com.google.android.jacquard.sdk.imu.parser.ImuSessionData.ImuSampleCollection;
import com.google.android.jacquard.sdk.model.FakeDCTrialListNotification;
import com.google.android.jacquard.sdk.rx.Signal;
import com.google.android.jacquard.sdk.rx.Signal.Subscription;
import com.google.android.jacquard.sdk.tag.JacquardTagFactory;
import com.google.atap.jacquard.protocol.JacquardProtocol.DataCollectionStatus;
import com.google.atap.jacquard.protocol.JacquardProtocol.ImuSample;
import java.io.File;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Unit tests for {@link ImuModule}.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {VERSION_CODES.P}, manifest = Config.NONE)
public class ImuModuleTest {

  private static final String TARGET_IMU_SESSION_ID = "1627344030";
  private static final int TARGET_IMU_SESSION_SIZE = 2900;

  private ImuModule imuModule;

  @Before
  public void setup() {
    JacquardManagerInitialization.initJacquardManager(new FakeJacquardManagerImpl(
        ApplicationProvider.getApplicationContext()));
    imuModule = new ImuModule(JacquardTagFactory.createConnectedJacquardTag());
  }

  @Test
  public void testInitialize() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    initialize().onNext(initDone -> latch.countDown());
    boolean countReached = latch.await(2, TimeUnit.SECONDS);
    assertThat(countReached).isTrue();
  }

  @Test
  public void testInitializeDfu() {
    JacquardManagerInitialization.initJacquardManager(new FakeJacquardManagerImpl(
        ApplicationProvider.getApplicationContext(), false));
    ImuModule imuModule = new ImuModule(JacquardTagFactory.createConnectedJacquardTag(false));
    List<InitState> states = new ArrayList<>();
    imuModule.initialize().onNext(state -> states.add(state));
    shadowOf(getMainLooper()).idleFor(Duration.ofSeconds(3));
    assertThat(states.size()).isEqualTo(7);
    assertThat(states.get(0).getType()).isEqualTo(Type.INIT);
    assertThat(states.get(1).getType()).isEqualTo(Type.CHECK_FOR_UPDATES);
    assertThat(states.get(2).getType()).isEqualTo(Type.TAG_DFU);
    assertThat(states.get(3).getType()).isEqualTo(Type.CHECK_FOR_UPDATES);
    assertThat(states.get(4).getType()).isEqualTo(Type.MODULE_DFU);
    assertThat(states.get(5).getType()).isEqualTo(Type.ACTIVATE);
    assertThat(states.get(6).getType()).isEqualTo(Type.INITIALIZED);
  }

  @Test
  public void testStartImuSession() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    initialize().flatMap(ignore -> imuModule.startImuSession())
        .filter(id -> !TextUtils.isEmpty(id))
        .onNext(initDone -> latch.countDown());
    boolean countReached = latch.await(2, TimeUnit.SECONDS);
    assertThat(countReached).isTrue();
  }

  @Test
  public void testStopImuSession() {
    AtomicBoolean result = new AtomicBoolean();
    initialize().flatMap(ignore -> imuModule.stopImuSession())
        .filter(stopped -> stopped)
        .onNext(initDone -> result.set(true));
    shadowOf(getMainLooper()).idleFor(Duration.ofSeconds(2));
    assertThat(result.get()).isTrue();
  }

  @Test
  public void testGetDataCollectionStatus() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    initialize().flatMap(ignore -> imuModule.getDataCollectionStatus())
        .filter(status -> status.equals(DataCollectionStatus.DATA_COLLECTION_IDLE))
        .onNext(initDone -> latch.countDown());
    boolean countReached = latch.await(2, TimeUnit.SECONDS);
    assertThat(countReached).isTrue();
  }

  @Test
  public void testParseImuData() throws URISyntaxException, InterruptedException {
    AtomicInteger count = new AtomicInteger();
    CountDownLatch latch = new CountDownLatch(1);
    File imuSession = new File(this.getClass().getClassLoader()
        .getResource("1627344030.bin").toURI());
    List<ImuSample> samplesList = new ArrayList<>();
    ImuModule.parseImuData(imuSession.getAbsolutePath())
        .onNext(imuTrialData -> {
          List<ImuSampleCollection> samples = imuTrialData.getImuSampleCollections();
          for (ImuSampleCollection g : samples) {
            List<ImuSample> imuSamples = g.getImuSamples();
            if (!imuSamples.isEmpty()) {
              samplesList.addAll(imuSamples);
            }
          }
          count.set(samplesList.size());
          latch.countDown();
        });
    boolean countReached = latch.await(2, TimeUnit.SECONDS);
    assertThat(countReached).isTrue();
    assertThat(samplesList.size()).isEqualTo(174); // Hardcoded.
  }

  @Test
  public void testDownloadImuSession() {
    AtomicInteger received = new AtomicInteger();
    AtomicInteger expectedBytes = new AtomicInteger();
    initialize().flatMap(ignore -> imuModule.getImuSessionsList())
        .map(list -> list.get(0))
        .tap(sessionInfo -> expectedBytes.set(sessionInfo.imuSize()))
        .flatMap(sessionInfo -> imuModule.downloadImuData(sessionInfo))
        .onNext(progress -> {
          if (progress.first == 100) { // complete
            received.set((int) progress.second.length());
          }
        });
    shadowOf(getMainLooper()).idleFor(Duration.ofSeconds(5));
    assertThat(received.get()).isEqualTo(expectedBytes.get());
  }

  @Test
  public void testDownloadImuSessionFromId() {
    AtomicInteger received = new AtomicInteger();
    initialize().flatMap(sessionInfo -> imuModule.downloadImuData(TARGET_IMU_SESSION_ID))
        .onNext(progress -> {
          if (progress.first == 100) {
            received.set((int) progress.second.length());
          }
        });
    shadowOf(getMainLooper()).idleFor(Duration.ofSeconds(5));
    assertThat(received.get()).isEqualTo(TARGET_IMU_SESSION_SIZE);
  }

  @Test
  public void testDownloadImuSessionCancelled() {
    AtomicInteger received = new AtomicInteger();
    AtomicInteger progress = new AtomicInteger();
    AtomicReference<Subscription> subscription = new AtomicReference<>();
    subscription.set(initialize().flatMap(sessionInfo -> imuModule.downloadImuData(
            ImuSessionInfo.of(FakeDCTrialListNotification.getDCTrialListNotification())))
        .onNext(data -> {
          progress.set(data.first);
          received.set((int) data.second.length());
          if (data.first >= 20) {
            subscription.get().unsubscribe();
          }
        }));
    shadowOf(getMainLooper()).idleFor(Duration.ofSeconds(5));
    assertThat(progress.get()).isNotEqualTo(100);
    assertThat(received.get()).isLessThan(TARGET_IMU_SESSION_SIZE);
  }

  @Test
  public void testEraseSessionBySessionId() {
    AtomicBoolean result = new AtomicBoolean();
    initialize().flatMap(sessionInfo -> imuModule.erase(TARGET_IMU_SESSION_ID))
        .filter(erased -> erased)
        .onNext(erased -> result.set(true));
    shadowOf(getMainLooper()).idleFor(Duration.ofSeconds(2));
    assertThat(result.get()).isTrue();
  }

  @Test
  public void testEraseSessionByInvalidSessionId() {
    AtomicBoolean errorThrown = new AtomicBoolean();
    initialize().flatMap(sessionInfo -> imuModule.erase("random-session-id"))
        .filter(erased -> erased)
        .onError(error -> errorThrown.set(true));
    shadowOf(getMainLooper()).idleFor(Duration.ofSeconds(2));
    assertThat(errorThrown.get()).isTrue();
  }

  @Test
  public void testUnloadModule() {
    AtomicBoolean result = new AtomicBoolean();
    initialize().flatMap(sessionInfo -> imuModule.unloadModule())
        .filter(erased -> erased)
        .onNext(erased -> result.set(true));
    shadowOf(getMainLooper()).idleFor(Duration.ofSeconds(2));
    assertThat(result.get()).isTrue();
  }

  @Test
  public void testGetImuSessionList() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    initialize().flatMap(ignore -> imuModule.getImuSessionsList())
        .filter(list -> list.size() == 1)
        .onNext(list -> {
          ImuSessionInfo info = list.get(0);
          if (info.equals(
              ImuSessionInfo.of(FakeDCTrialListNotification.getDCTrialListNotification()))) {
            latch.countDown();
          }
        });
    boolean countReached = latch.await(2, TimeUnit.SECONDS);
    assertThat(countReached).isTrue();
  }

  @Test
  public void testEraseAllImuSessions() {
    AtomicBoolean result = new AtomicBoolean();
    initialize().flatMap(ignore -> imuModule.eraseAll())
        .filter(erased -> erased)
        .onNext(erased -> result.set(true));
    shadowOf(getMainLooper()).idleFor(Duration.ofSeconds(2));
    assertThat(result.get()).isTrue();
  }

  @Test
  public void testEraseImuSession() {
    AtomicBoolean result = new AtomicBoolean();
    initialize().flatMap(ignore -> imuModule.erase(ImuSessionInfo.of(
        FakeDCTrialListNotification.getDCTrialListNotification())))
        .filter(erased -> erased)
        .onNext(erased -> result.set(true));
    shadowOf(getMainLooper()).idleFor(Duration.ofSeconds(2));
    assertThat(result.get()).isTrue();
  }

  private Signal<InitState> initialize() {
    return imuModule.initialize()
        .filter(initDone -> initDone.isType(Type.INITIALIZED));
  }
}