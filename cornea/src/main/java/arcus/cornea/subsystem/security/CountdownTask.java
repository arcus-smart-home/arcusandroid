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
package arcus.cornea.subsystem.security;

import androidx.annotation.Nullable;

import arcus.cornea.utils.LooperExecutor;
import com.iris.client.event.DefaultExecutor;

import java.util.TimerTask;

public class CountdownTask extends TimerTask {

    public interface CountdownDelegate {
        void onTimerTicked(int remainingSeconds);
    }

    private int remainingSec;
    private final CountdownDelegate delegate;

    public CountdownTask(int remainingSec, @Nullable CountdownDelegate delegate) {
        this.remainingSec = remainingSec;
        this.delegate = delegate;
    }

    public synchronized int getRemainingSec() {
        return remainingSec;
    }

    public synchronized void setRemainingSec(int remainingSec) {
        this.remainingSec = remainingSec;
    }

    public synchronized int decrementRemainingSec() {
        this.remainingSec--;
        return this.remainingSec;
    }

    @Override
    public void run() {
        // run updates on the Main UI thread
        DefaultExecutor.getDefaultExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final int remainingSec = decrementRemainingSec();

                LooperExecutor.getMainExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (delegate != null) {
                            delegate.onTimerTicked(remainingSec);
                        }
                    }
                });

                if (remainingSec <= 0) {
                    cancel();
                }
            }
        });
    }
}
