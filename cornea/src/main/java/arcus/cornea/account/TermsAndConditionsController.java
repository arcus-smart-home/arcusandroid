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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import androidx.annotation.VisibleForTesting;

import arcus.cornea.SessionController;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.PersonModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TermsAndConditionsController {
    public static final int TERMS = 1, PRIVACY = 1 << 1, TERMS_N_PRIVACY = TERMS | PRIVACY;

    public interface Callback {
        void onSuccess();
        void onFailure(Throwable throwable);
    }

    @StringDef({
          PersonModel.AcceptPolicyRequest.TYPE_TERMS,
          PersonModel.AcceptPolicyRequest.TYPE_PRIVACY
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface AcceptTypes {}

    private static final Logger logger = LoggerFactory.getLogger(TermsAndConditionsController.class);
    private volatile PersonModel personModel;
    private Reference<Callback> callbackRef = new WeakReference<>(null);
    private final AtomicBoolean aborted = new AtomicBoolean(false);
    private CountDownLatch policiesUpdatedLatch;
    private final Callback noopCallback = new Callback() {
        @Override public void onSuccess() {}
        @Override public void onFailure(Throwable throwable) {}
    };

    @VisibleForTesting TermsAndConditionsController(@Nullable PersonModel personModel) {
        this.personModel = personModel;
    }

    public TermsAndConditionsController() {
        this(SessionController.instance().getPerson());
    }

    public ListenerRegistration setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);

        return Listeners.wrap(callbackRef);
    }

    public void acceptTermsAndConditions() {
        if (personModel == null) {
            getCallback().onFailure(new RuntimeException("Unable to get person object. Are we still connected?"));
            return;
        }

        aborted.set(false);
        int policiesNeeded = policiesNeeded();
        policiesUpdatedLatch = new CountDownLatch((policiesNeeded & TERMS_N_PRIVACY) == TERMS_N_PRIVACY ? 2 : 1);
        startLatchMonitor();

        if ((policiesNeeded & TERMS) != 0) {
            logger.debug("Need terms update.");
            accept(personModel, PersonModel.AcceptPolicyRequest.TYPE_TERMS);
        }

        if ((policiesNeeded & PRIVACY) != 0) {
            logger.debug("Need privacy update.");
            accept(personModel, PersonModel.AcceptPolicyRequest.TYPE_PRIVACY);
        }
    }

    protected void accept(@NonNull final PersonModel model, @NonNull @AcceptTypes final String acceptType) {
        logger.debug("Calling to accept [{}]", acceptType);
        model
              .acceptPolicy(acceptType)
              .onFailure(new Listener<Throwable>() {
                  @Override public void onEvent(final Throwable throwable) {
                      if (!aborted.getAndSet(true)) { // So we only notify once.
                          getCallback().onFailure(throwable);
                      }

                      policiesUpdatedLatch.countDown();
                  }
              })
              .onSuccess(new Listener<Person.AcceptPolicyResponse>() {
                  @Override public void onEvent(Person.AcceptPolicyResponse acceptPolicyResponse) {
                      if (PersonModel.AcceptPolicyRequest.TYPE_PRIVACY.equals(acceptType)) {
                          SessionController.instance().hasUpdatedPrivacy();
                      }
                      else if (PersonModel.AcceptPolicyRequest.TYPE_TERMS.equals(acceptType)) {
                          SessionController.instance().hasUpdatedTerms();
                      }
                      policiesUpdatedLatch.countDown();
                  }
              });
    }

    protected @NonNull Callback getCallback() {
        Callback callback = callbackRef.get();
        if (callback == null) {
            logger.warn("Callback was null, using no-op callback.");
            return getNoOpCallback();
        }

        return callback;
    }

    @VisibleForTesting Callback getNoOpCallback() {
        return noopCallback;
    }

    protected int policiesNeeded() {
        int terms = SessionController.instance().needsTermsAndConditionsUpdate() ? TERMS : 0;
        int privacy = SessionController.instance().needsPrivacyUpdate() ? PRIVACY : 0;
        return terms | privacy;
    }

    public boolean stillRequiresUpdate() {
        return policiesNeeded() != 0;
    }

    protected void startLatchMonitor() {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    policiesUpdatedLatch.await(30, TimeUnit.SECONDS);
                    if (!aborted.get()) {
                        getCallback().onSuccess();
                    }
                }
                catch (Exception ex) {
                    if (!aborted.get()) { // One of the calls failed we've already notified the user.
                        try {
                            getCallback().onFailure(ex);
                        }
                        catch (Exception ex2) {
                            logger.error("Could not finish policy updates", ex2);
                        }
                    }
                }
            }
        }).start();
    }
}
