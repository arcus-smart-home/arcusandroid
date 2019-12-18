/*
 *  Copyright 2019 Arcus Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package arcus.presentation.pairing.device.customization.halo

import arcus.cornea.device.smokeandco.halo.HaloRadioController
import arcus.cornea.utils.LooperExecutor
import arcus.presentation.dynamite
import arcus.presentation.pairing.device.customization.halo.station.HaloStationSelectPresenterImpl
import arcus.presentation.pairing.device.customization.halo.station.HaloStationSelectView
import arcus.presentation.pairing.device.customization.halo.station.RadioStation
import arcus.presentation.success
import com.google.common.truth.Truth
import com.iris.client.ClientEvent
import com.iris.client.capability.WeatherRadio
import com.iris.client.event.ClientFuture
import com.nhaarman.mockito_kotlin.argumentCaptor
import kotlin.properties.Delegates
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class HaloStationSelectPresenterImplTest {
    private val mockController = MockStationController()
    private val mockView = Mockito.mock(HaloStationSelectView::class.java)
    private val presenter by lazy {
        HaloStationSelectPresenterImpl(
            { mockController })
    }
    private val blankStation =
        RadioStation(
            1,
            "",
            "",
            0.0,
            false
        )

    @Before
    fun doBefore() {
        LooperExecutor.setMainExecutor({ it.run() })
        presenter.setView(mockView)
        presenter.loadFromDeviceAddress("")
    }

    private val scanResults4 = listOf(
        RadioStation(
            1,
            "Station 1",
            "103.3 Mhz",
            22.0,
            true
        ),
        RadioStation(
            2,
            "Station 2",
            "108.3 Mhz",
            50.0,
            false
        ),
        RadioStation(
            4,
            "Station 4",
            "102.3 Mhz",
            65.0,
            false
        ),
        RadioStation(
            3,
            "Station 3",
            "107.3 Mhz",
            75.0,
            false
        )
    )

    private val scanResults3 = listOf(
        RadioStation(
            1,
            "Station 1",
            "103.3 Mhz",
            22.0,
            true
        ),
        RadioStation(
            2,
            "Station 2",
            "108.3 Mhz",
            50.0,
            false
        ),
        RadioStation(
            3,
            "Station 3",
            "102.3 Mhz",
            75.0,
            false
        )
    )

    @Test
    fun loadRadioStationsSuccessMoreThanThree() {
        mockController.currentStation = 1
        mockController.getAvailable = success(listOf(
            Triple(2, "108.3 Mhz", 50.0),
            Triple(1, "103.3 Mhz", 22.0),
            Triple(4, "102.3 Mhz", 65.0),
            Triple(3, "107.3 Mhz", 75.0)
        ))

        val firstThreeCaptor = argumentCaptor<List<RadioStation>>()
        val secondThreeCaptor = argumentCaptor<List<RadioStation>>()

        presenter.loadRadioStations()

        Mockito.verify(mockView).onStationsFound(firstThreeCaptor.capture(), secondThreeCaptor.capture())

        Truth.assertThat(firstThreeCaptor.allValues).containsExactly(scanResults4.subList(0, 3))
        Truth.assertThat(secondThreeCaptor.allValues).containsExactly(scanResults4.subList(3, scanResults4.size))
    }

    @Test
    fun loadRadioStationsSuccessThreeOrLess() {
        mockController.currentStation = 1
        mockController.getAvailable = success(listOf(
            Triple(2, "108.3 Mhz", 50.0),
            Triple(1, "103.3 Mhz", 22.0),
            Triple(3, "102.3 Mhz", 75.0)
        ))

        val firstThreeCaptor = argumentCaptor<List<RadioStation>>()

        presenter.loadRadioStations()

        Mockito.verify(mockView).onStationsFound(firstThreeCaptor.capture())

        Truth.assertThat(firstThreeCaptor.allValues).containsExactly(scanResults3)
    }

    @Test
    fun loadRadioStationsSuccessNothingFound() {
        mockController.currentStation = 1
        mockController.getAvailable = success(emptyList())
        presenter.loadRadioStations()

        Mockito.verify(mockView).onNoStationsFound()
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    fun loadRadioStationsFailure() {
        mockController.getAvailable = dynamite()
        presenter.loadRadioStations()

        Mockito.verify(mockView).onScanStationsFailed()
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    fun playStationSuccess() {
        mockController.startPlaying = success(WeatherRadio.PlayStationResponse("", ""))
        presenter.playStation(blankStation)

        Mockito.verifyZeroInteractions(mockView)
    }

    @Test
    fun playStationFailure() {
        mockController.startPlaying = dynamite()
        presenter.playStation(blankStation)

        Mockito.verify(mockView).onPlayStationFailed()
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    fun stopPlayingStationsSuccess() {
        mockController.stopPlaying = success(WeatherRadio.StopPlayingStationResponse("", ""))
        presenter.stopPlayingStations()

        Mockito.verifyZeroInteractions(mockView)
    }

    @Test
    fun stopPlayingStationsFailure() {
        mockController.stopPlaying = dynamite()
        presenter.stopPlayingStations()

        Mockito.verify(mockView).onStopPlayingStationFailed()
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    fun testSetStationSuccess() {
        mockController.setSelected = success(ClientEvent("", ""))
        presenter.setSelectedStation(blankStation)

        Mockito.verifyZeroInteractions(mockView)
    }

    @Test
    fun testSetStationFailure() {
        mockController.setSelected = dynamite()
        presenter.setSelectedStation(blankStation)

        Mockito.verify(mockView).onSetSelectionFailed()
        Mockito.verifyNoMoreInteractions(mockView)
    }

    class MockStationController : HaloRadioController {
        var currentStation by Delegates.notNull<Int>()
        var setSelected by Delegates.notNull<ClientFuture<ClientEvent>>()
        var getAvailable by Delegates.notNull<ClientFuture<List<Triple<Int, String, Double>>>>()
        var startPlaying by Delegates.notNull<ClientFuture<WeatherRadio.PlayStationResponse>>()
        var stopPlaying by Delegates.notNull<ClientFuture<WeatherRadio.StopPlayingStationResponse>>()

        override fun getRadioState() = ""
        override fun getSelectedStation() = currentStation
        override fun setSelectedStation(station: Int): ClientFuture<ClientEvent> = setSelected
        override fun getAvailableStations() = getAvailable
        override fun playStation(station: Int, seconds: Int) = startPlaying
        override fun stopPlayingStation() = stopPlaying
    }
}
