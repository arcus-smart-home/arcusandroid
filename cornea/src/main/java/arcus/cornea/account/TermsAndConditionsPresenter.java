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
package arcus.cornea.account;

import androidx.annotation.VisibleForTesting;

import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.WrappedRunnable;
import com.iris.client.event.ListenerRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

public class TermsAndConditionsPresenter implements
      TermsAndConditionsController.Callback,
      TermsAndConditionsContract.Presenter {
    private static final Logger logger = LoggerFactory.getLogger(TermsAndConditionsPresenter.class);
    private Reference<TermsAndConditionsContract.View> viewRef = new SoftReference<>(null);
    private TermsAndConditionsController termsController;
    private final ListenerRegistration termsControllerListener;

    public TermsAndConditionsPresenter(TermsAndConditionsContract.View view) {
        this(view, new TermsAndConditionsController());
    }

    @VisibleForTesting TermsAndConditionsPresenter(
          TermsAndConditionsContract.View view,
          TermsAndConditionsController conditionsController
    ) {
        viewRef = new SoftReference<>(view);
        termsController = conditionsController;
        termsControllerListener = termsController.setCallback(this);
    }

    @Override public void recheckNeedToAccept() {
        // In the event they accepted, navigated away (phone call) and then come back to this screen.
        if (termsController != null && !termsController.stillRequiresUpdate()) {
            onSuccess();
        }
        else {
            acceptRequired();
        }
    }

    @Override public void acceptTermsAndConditions() {
        if (termsController != null) {
            termsController.acceptTermsAndConditions();
        }
        else {
            onFailure(new RuntimeException("Lost reference to the terms controller."));
        }
    }

    @Override public void clearReferences() {
        Listeners.clear(termsControllerListener);
        termsController = null;
    }

    @Override public void onSuccess() {
        final TermsAndConditionsContract.View view = viewRef.get();
        if (view == null) {
            logger.debug("View Reference was null - dropping callback for successfully accepting terms.");
            return;
        }

        LooperExecutor.getMainExecutor().execute(new WrappedRunnable() {
            @Override public void onRun() throws Exception {
                view.onAcceptSuccess();
            }
        });
    }

    public void acceptRequired() {
        final TermsAndConditionsContract.View view = viewRef.get();
        if (view == null) {
            logger.debug("View Reference was null - dropping callback for accept required callback.");
            return;
        }

        LooperExecutor.getMainExecutor().execute(new WrappedRunnable() {
            @Override public void onRun() throws Exception {
                view.acceptRequired();
            }
        });
    }

    @Override public void onFailure(final Throwable throwable) {
        final TermsAndConditionsContract.View view = viewRef.get();
        if (view == null) {
            logger.debug("View Reference was null - dropping callback for failure to accept terms.");
            return;
        }

        LooperExecutor.getMainExecutor().execute(new WrappedRunnable() {
            @Override public void onRun() throws Exception {
                view.onError(throwable);
            }
        });
    }

    @VisibleForTesting boolean referencesCleared() {
        return !Listeners.isRegistered(termsControllerListener) && termsController == null;
    }
}
