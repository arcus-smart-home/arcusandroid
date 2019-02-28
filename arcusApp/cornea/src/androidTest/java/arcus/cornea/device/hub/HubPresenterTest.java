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
package arcus.cornea.device.hub;

import arcus.cornea.utils.Listeners;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class HubPresenterTest {
    private static final String HUB_ID = "HUB-1111";

    @Mock HubMVPContract.View view;
    @Mock HubController hubController;
    HubPresenter presenter;
    private final HubProxyModel hubProxy = new HubProxyModel(HUB_ID);

    @Before public void setUp() throws Exception {
        presenter = new HubPresenter(view, hubController);
    }

    @Test public void testShowLoad() throws Exception {
        Mockito.doAnswer(new LoadSuccess()).when(hubController).load();

        presenter.load();

        Mockito.verify(view, Mockito.atMost(1)).show(hubProxy);
        Mockito.verifyNoMoreInteractions(view);
    }

    @Test public void testShowRefresh() throws Exception {
        Mockito.doAnswer(new LoadSuccess()).when(hubController).refresh();

        presenter.refresh();

        Mockito.verify(view, Mockito.atMost(1)).show(hubProxy);
        Mockito.verifyNoMoreInteractions(view);
    }

    @Test public void testClear() throws Exception {
        assertTrue(Listeners.isRegistered(presenter.listenerRegistration));

        presenter.clear();

        assertFalse(Listeners.isRegistered(presenter.listenerRegistration));
    }

    @Test public void testOnError() throws Exception {
        Mockito.doAnswer(new LoadFailure()).when(hubController).load();

        presenter.load();

        Mockito.verify(view, Mockito.atMost(1)).onError(Matchers.any(Throwable.class));
        Mockito.verifyNoMoreInteractions(view);
    }

    public class LoadSuccess implements Answer<Void> {
        @Override public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            hubController.show(new HubProxyModel(HUB_ID));
            return null;
        }
    }

    public class LoadFailure implements Answer<Void> {
        @Override public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            hubController.onError(new RuntimeException("Failed to load."));
            return null;
        }
    }
}