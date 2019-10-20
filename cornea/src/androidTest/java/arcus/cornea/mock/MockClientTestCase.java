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
package arcus.cornea.mock;

import com.google.common.base.Predicate;
import com.google.common.util.concurrent.MoreExecutors;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.LooperExecutor;
import com.iris.client.ClientRequest;
import com.iris.client.event.DefaultExecutor;
import com.iris.client.impl.BaseClientFactory;

import org.junit.After;
import org.junit.Before;

import java.util.Map;
import java.util.concurrent.Executor;


public abstract class MockClientTestCase {
    private Executor originalExecutor;
    private MockClient client;

    @Before
    public void initClient() {
        originalExecutor = LooperExecutor.getMainExecutor();
        LooperExecutor.setMainExecutor(MoreExecutors.directExecutor());
        // TODO make IrisClientFactory.getFactory() public so we can restore it
//        oldFactory = IrisClientFactory.getFactory();
        client = new MockClient();
        CorneaClientFactory.init(new BaseClientFactory(client) {
        });
        // TODO update listener list fire to return a future to block on until the events have been delivered
        // TODO restore the original default executor
        DefaultExecutor.setDefaultExecutor(MoreExecutors.directExecutor());
    }

    @After
    public void restoreClient() {
        LooperExecutor.setMainExecutor(originalExecutor);
        CorneaClientFactory.restore();
    }


    protected MockClient client() {
        return client;
    }

    public MockClient.ClientResponseBuilder expectRequestTo(String address) {
        return client().expectRequestTo(address);
    }

    public MockClient.ClientResponseBuilder expectRequestOfType(String name) {
        return client().expectRequestOfType(name);
    }

    public MockClient.ClientResponseBuilder expectRequest(String to, String command) {
        return client().expectRequest(to, command);
    }

    public MockClient.ClientResponseBuilder expectRequest(String to, String command, Map<String, Object> attributes) {
        return client().expectRequest(to, command, attributes);
    }

    public MockClient.ClientResponseBuilder expectRequest(ClientRequest request) {
        return client().expectRequest(request);
    }

    public MockClient.ClientResponseBuilder expectRequest(Predicate<? super ClientRequest> requestMatcher) {
        return client().expectRequest(requestMatcher);
    }

    public MockClient.AndThenBuilder<ClientRequest> expectSubmit(Predicate<? super ClientRequest> requestMatcher) {
        return client().expectSubmit(requestMatcher);
    }
}
