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
package arcus.app.account.registration.controller.task;

import android.content.Context;
import android.os.AsyncTask;
import androidx.fragment.app.Fragment;

import arcus.cornea.CorneaService;
import com.iris.client.event.SettableClientFuture;
import arcus.app.common.models.RegistrationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ArcusTask extends AsyncTask<Void, Void, Void> {

    public Logger logger = LoggerFactory.getLogger(ArcusTask.class);

    public interface ArcusTaskListener {
        void onComplete(boolean result);
        void onError(Exception e);
    }

    protected Context context;
    protected Exception exception;
    protected Fragment fragment;
    protected ArcusTaskListener listener;
    protected CorneaService corneaService;
    protected RegistrationContext registrationContext;

    protected SettableClientFuture<Boolean> futureState;

    protected boolean isResultOk = false;

    public ArcusTask(Context context, Fragment fragment, ArcusTaskListener listener, CorneaService corneaService, RegistrationContext registrationContext){
        this.context = context;
        this.fragment = fragment;
        this.listener = listener;
        this.corneaService = corneaService;
        this.registrationContext = registrationContext;
        futureState = new SettableClientFuture<>();
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if(listener!=null) {
            if (exception != null) {
                listener.onError(exception);
            } else {
                listener.onComplete(isResultOk);
            }
        }
    }
}
