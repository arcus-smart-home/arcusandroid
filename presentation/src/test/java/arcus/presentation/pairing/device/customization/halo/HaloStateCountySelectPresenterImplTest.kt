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

import arcus.cornea.device.smokeandco.halo.HaloLocationController
import arcus.cornea.utils.LooperExecutor
import arcus.presentation.dynamite
import arcus.presentation.pairing.device.customization.halo.statecounty.HaloCounty
import arcus.presentation.pairing.device.customization.halo.statecounty.HaloStateAndCode
import arcus.presentation.pairing.device.customization.halo.statecounty.HaloStateCountySelectPresenterImpl
import arcus.presentation.pairing.device.customization.halo.statecounty.HaloStateCountySelectView
import arcus.presentation.success
import com.google.common.truth.Truth
import com.iris.client.ClientEvent
import com.iris.client.bean.SameState
import com.iris.client.event.ClientFuture
import com.nhaarman.mockito_kotlin.argumentCaptor
import kotlin.properties.Delegates
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class HaloStateCountySelectPresenterImplTest {
    private val statesForBean = listOf(
        SameState(mapOf(SameState.ATTR_STATE to "California", SameState.ATTR_STATECODE to "CA")),
        SameState(mapOf(SameState.ATTR_STATE to "North Carolina", SameState.ATTR_STATECODE to "NC")),
        SameState(mapOf(SameState.ATTR_STATE to "Federated States of Micronesia", SameState.ATTR_STATECODE to "FM"))
    )
    private val states = listOf(
        HaloStateAndCode("California", "CA"),
        HaloStateAndCode("North Carolina", "NC", true),
        HaloStateAndCode("Federated States of Micronesia", "FM")
    )
    private val counties = listOf("Chuuk*", "Kosrae", "Pohnpeit*", "Yap")
    private val haloCounties = listOf(
        HaloCounty(
            "Chuuk*",
            false
        ),
        HaloCounty(
            "Kosrae",
            false
        ),
        HaloCounty(
            "Pohnpeit*",
            false
        ),
        HaloCounty(
            "Yap",
            false
        )
    )
    private val mockController = MockHaloLocationController()
    private val haloPresenter by lazy {
        HaloStateCountySelectPresenterImpl(
            { mockController },
            { "NC" },
            { "" })
    }
    private val haloView = mock(HaloStateCountySelectView::class.java)

    @Before
    fun doBefore() {
        LooperExecutor.setMainExecutor({ it.run() })
        haloPresenter.setView(haloView)
        haloPresenter.loadFromDeviceAddress("")
    }

    @Test
    fun loadStatesSuccess() {
        mockController.stateResponse = success(statesForBean)
        haloPresenter.loadStates()

        val statesCaptor = argumentCaptor<List<HaloStateAndCode>>()

        Mockito.verify(haloView).onStatesLoaded(statesCaptor.capture())
        Mockito.verifyNoMoreInteractions(haloView)

        Truth.assertThat(statesCaptor.allValues).containsExactly(states)
    }

    @Test
    fun loadStatesFailure() {
        mockController.stateResponse = dynamite()
        haloPresenter.loadStates()

        Mockito.verify(haloView).onStatesFailedToLoad()
        Mockito.verifyNoMoreInteractions(haloView)
    }

    @Test
    fun loadCounties() {
        mockController.countiesResponse = success(counties)
        haloPresenter.loadCounties(states[2])

        val countyCaptor = argumentCaptor<List<HaloCounty>>()

        Mockito.verify(haloView).onCountiesLoaded(countyCaptor.capture())
        Mockito.verifyNoMoreInteractions(haloView)

        Truth.assertThat(countyCaptor.allValues).containsExactly(haloCounties)
    }

    @Test
    fun loadCountiesFailure() {
        mockController.countiesResponse = dynamite()
        haloPresenter.loadCounties(states[2])

        Mockito.verify(haloView).onCountiesFailedToLoad()
        Mockito.verifyNoMoreInteractions(haloView)
    }

    @Test
    fun setSelectedStateAndCountySuccess() {
        mockController.setLocationResponse = success(ClientEvent("", ""))
        haloPresenter.setView(haloView)

        haloPresenter.loadFromDeviceAddress("")
        haloPresenter.setSelectedStateAndCounty(states[2], counties[1])

        Mockito.verify(haloView).onSelectionSaved()
        Mockito.verifyNoMoreInteractions(haloView)
    }

    @Test
    fun setSelectedStateAndCountyFailure() {
        mockController.setLocationResponse = dynamite()
        haloPresenter.setSelectedStateAndCounty(states[2], counties[1])

        Mockito.verify(haloView).onSelectionSaveFailed()
        Mockito.verifyNoMoreInteractions(haloView)
    }

    class MockHaloLocationController : HaloLocationController {
        var stateResponse by Delegates.notNull<ClientFuture<List<SameState>>>()
        var countiesResponse by Delegates.notNull<ClientFuture<List<String>>>()
        var setLocationResponse by Delegates.notNull<ClientFuture<ClientEvent>>()

        override fun getStateNames(): ClientFuture<List<SameState>> = stateResponse
        override fun getCountiesFor(stateCode: String): ClientFuture<List<String>> = countiesResponse
        override fun setLocationUsing(stateCode: String, county: String): ClientFuture<ClientEvent> =
            setLocationResponse
    }
}
