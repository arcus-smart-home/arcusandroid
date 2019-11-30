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
package arcus.cornea;

import com.iris.client.IrisClient2;
import com.iris.client.IrisClientFactory;
import com.iris.client.connection.ConnectionState;
import com.iris.client.impl.okhttp.OkHttpIrisClientFactory;

public abstract class CorneaClientFactory extends IrisClientFactory {
    private static final OkHttpIrisClientFactory delegate = new OkHttpIrisClientFactory();

    static {
        init(delegate);
    }

    public static void init() {

    }

    public static void init(String agent, String version) {
        CorneaClientFactory.getClient().setClientAgent(agent);
        CorneaClientFactory.getClient().setClientVersion(version);
    }

    public static boolean isConnected() {
        return ConnectionState.CONNECTED.equals(getClient().getConnectionState());
    }

    public static void restore() {
        init(delegate);
    }

    public static IrisClient2 getClient2() {
        return delegate.getClient2();
    }
}
