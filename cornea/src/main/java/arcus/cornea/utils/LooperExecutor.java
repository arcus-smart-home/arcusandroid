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
package arcus.cornea.utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

// TODO move the statics to AndroidExecutor
public class LooperExecutor implements Executor {
    public static Executor getMainExecutor() {
        return MainExecutorRef.INSTANCE;
    }

    /**
     * For test use only!
     */
    // TODO move to package scope
    @Deprecated
    public static void setMainExecutor(Executor executor) {
        MainExecutorRef.INSTANCE = executor;
    }

    private static class MainExecutorRef {
        private static volatile Executor INSTANCE = new LooperExecutor(Looper.getMainLooper());
    }

    private final Handler handler;

    public LooperExecutor(Looper looper) {
        this.handler = new Handler(looper);
    }

    @Override
    public void execute(@NonNull Runnable command) {
        if(Thread.currentThread() == handler.getLooper().getThread()) {
            command.run();
        }
        else {
            handler.post(command);
        }
    }

}
