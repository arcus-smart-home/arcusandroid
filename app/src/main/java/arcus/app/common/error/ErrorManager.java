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
package arcus.app.common.error;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;

import arcus.app.R;
import arcus.app.common.error.base.RejectableError;
import arcus.app.common.error.base.AcceptableError;
import arcus.app.common.error.base.Error;
import arcus.app.common.error.listener.DismissListener;
import arcus.app.common.error.popup.ArcusErrorPopup;
import arcus.app.common.error.popup.NoNetworkConnectionPopup;
import arcus.app.common.error.type.ErrorType;
import arcus.app.common.error.type.NoNetworkConnectionErrorType;
import arcus.app.common.backstack.BackstackManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ErrorManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Activity activity;
    private DismissListener dialogDismissedListener;
    private boolean allowCancel = false;

    private ErrorManager (Activity activity) {
        this.activity = activity;
    }

    @NonNull
    public static ErrorManager in (Activity activity) {
        return new ErrorManager(activity);
    }

    /**
     *
     * Since the buttons are setup by default with the appropriate action,
     * this allows custom behavior after the intended action is taken.
     *
     * IE. User hits "OK" but needs to be taken back a screen, this could be used to achieve that.
     *
     * @param dialogDismissedListener
     */
    @NonNull
    public ErrorManager withDialogDismissedListener(DismissListener dialogDismissedListener) {
        this.dialogDismissedListener = dialogDismissedListener;
        return this;
    }

    /**
     *
     * If the user should be able to touch outside of the
     * screen to cancel the dailog instead of using a button.
     *
     * @param allowCancel
     * @return
     */
    @NonNull
    public ErrorManager allowCancel(boolean allowCancel) {
        this.allowCancel = allowCancel;
        return this;
    }

    /**
     *
     * Pass in the throwable received here to get a {@see #Builder}
     * returned which will be used to set, and then
     * try to determine the correct error to display.
     *
     * @param throwable
     * @return
     */
    @NonNull
    public Builder got(Throwable throwable) {
        return new Builder(throwable);
    }

    public void showGenericBecauseOf(Throwable throwable) {
        logger.debug("Showing generic error message because of: [{}]", throwable);
        allowCancel = true;
        render(ErrorLocator.genericFatalError);
    }

    /**
     *
     * Can be called directly if you know the error you want
     * to display. IE BillingErrorType.INVALID_CARD_NUMBER
     *
     * @param error
     */
    public void show(@NonNull ErrorType error) {
        logger.debug("Rendering [{}] by user request", error.getError() == null ? "(null)" : error.getError().getClass().getSimpleName());

        if(error instanceof NoNetworkConnectionErrorType){
            renderAsNoConnectionPopup();
        }else {
            render(error.getError());
        }
    }

    /**
     *
     * Can be called directly if you are creating a custom error or passing
     * in an ErrorType that you have called getError() on.
     *
     * @param error
     */
    public void show(@NonNull Error error) {
        logger.debug("Rendering [{}] by user request", error.getClass().getSimpleName());

        render(error);
    }

    private void render(@NonNull Error error) {
        if (error.isSystemDialog()) {
            renderAsDialog(error);
        }
        else {
            renderAsPopup(error);
        }
    }

    private void renderAsDialog(@NonNull final Error error) {
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog dialog = new AlertDialog.Builder(activity).create();

                // Configure base error properties (title and text)
                dialog.setTitle(error.getTitle(activity.getResources()));
                dialog.setMessage(error.getText(activity.getResources()));

                // If the error is "acceptable", then configure the accept button/condition
                if (AcceptableError.class.isAssignableFrom(error.getClass())) {
                    final AcceptableError acceptableError = (AcceptableError) error;
                    dialog.setButton(AlertDialog.BUTTON_NEUTRAL, acceptableError.getAcceptButtonTitle(activity.getResources()),
                          new DialogInterface.OnClickListener() {
                              public void onClick(DialogInterface dialog, int i) {
                                  acceptableError.onAccept(dialog, activity);

                                  if (dialogDismissedListener != null) {
                                      dialogDismissedListener.dialogDismissedByAccept();
                                  }
                              }
                          });
                }
                else { // Error is not acceptable; configure a default dismiss button
                    dialog.setButton(AlertDialog.BUTTON_NEUTRAL, activity.getString(R.string.error_generic_accept),
                          new DialogInterface.OnClickListener() {
                              public void onClick(@NonNull DialogInterface dialog, int i) {
                                  dialog.dismiss();

                                  if (dialogDismissedListener != null) {
                                      dialogDismissedListener.dialogDismissedByAccept();
                                  }
                              }
                          });
                }

                // If the error is "rejectable", then configure the reject button
                if (RejectableError.class.isAssignableFrom(error.getClass())) {
                    final RejectableError rejectableError = (RejectableError) error;
                    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, rejectableError.getRejectButtonTitle(activity.getResources()),
                          new DialogInterface.OnClickListener() {
                              public void onClick(DialogInterface dialog, int i) {
                                  rejectableError.onReject(dialog, activity);

                                  // Allows custom action upon reject. IE Navigating back a page.
                                  if (dialogDismissedListener != null) {
                                      dialogDismissedListener.dialogDismissedByReject();
                                  }
                              }
                          });
                }

                dialog.setCancelable(allowCancel);
                dialog.show();
            }
        });
    }

    private void renderAsPopup(final Error error) {
        ArcusErrorPopup popup = ArcusErrorPopup.newInstance(error, dialogDismissedListener);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
    }

    private void renderAsNoConnectionPopup(){
        NoNetworkConnectionPopup popup = NoNetworkConnectionPopup.newInstance();
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
    }

    public final class Builder {
        private final Throwable throwable;

        private Builder(final Throwable throwable) {
            this.throwable = throwable;
        }

        /**
         *
         * Describes what you was happening while the error occurred. IE: ErrorDuring.HUB_PAIRING
         *
         * @param errorDuring
         */
        public final void during(@NonNull ErrorDuring errorDuring) {
            Error error = ErrorLocator.locate(errorDuring, throwable);
            logger.debug("Rendering [{}] due to: [{}]", error.getClass().getSimpleName(), throwable);
            ErrorManager.this.render(error);
        }
    }
}
