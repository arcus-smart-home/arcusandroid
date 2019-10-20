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
package arcus.app.launch;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.PersonModel;
import com.iris.client.service.InvitationService;
import arcus.app.R;
import arcus.app.account.registration.controller.AccountCreationSequenceController;
import arcus.app.account.registration.model.AccountTypeSequence;
import arcus.app.account.settings.SettingsUpdatePin;
import arcus.app.account.settings.SideNavSettingsFragment;
import arcus.app.activities.LaunchActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.backstack.TransitionEffect;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.InfoButtonPopup;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.validation.EmailValidator;
import arcus.app.common.validation.NotEmptyValidator;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1EditText;
import arcus.app.subsystems.alarm.AlertFloatingFragment;
import arcus.app.subsystems.people.model.DeviceContact;

import java.util.Map;


public class InvitationFragment extends BaseFragment {
    private static String EMAIL_ADDRESS="EMAIL_ADDRESS";
    private static String INVITATION_CODE="INVITATION_CODE";
    public static String INVITATION_FIRST_NAME="INVITATION_FIRST_NAME";
    public static String INVITATION_LAST_NAME="INVITATION_LAST_NAME";
    public static String IS_SETTINGS="IS_SETTINGS";

    Version1EditText etEmailAddress;
    Version1EditText etInvitation;
    Version1Button next;
    AlertFloatingFragment alertpopup;

    private boolean isSettingsVariant;
    final Listener<Throwable> genericErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            hideProgressBarAndEnable(next);
            if (throwable instanceof ErrorResponseException) {
                parseErrorException((ErrorResponseException) throwable);
            }
            else {
                onError(throwable);
            }
        }
    });

    @NonNull
    public static InvitationFragment newInstance(String emailAddress, String invitationCode, String firstName, String lastName){
        InvitationFragment fragment = new InvitationFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(EMAIL_ADDRESS, emailAddress);
        arguments.putString(INVITATION_CODE, invitationCode);
        arguments.putString(INVITATION_FIRST_NAME, firstName);
        arguments.putString(INVITATION_LAST_NAME, lastName);
        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull public static InvitationFragment newInstanceFromSettings() {
        InvitationFragment fragment = new InvitationFragment();
        final Bundle arguments = new Bundle(1);
        arguments.putBoolean(IS_SETTINGS, true);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isSettingsVariant = getArguments().getBoolean(IS_SETTINGS, false);
        View view = super.onCreateView(inflater, container, savedInstanceState);
        String emailAddress = getArguments().getString(EMAIL_ADDRESS);
        String invitationCode = getArguments().getString(INVITATION_CODE);

        etEmailAddress = (Version1EditText) view.findViewById(R.id.etEmail);
        etInvitation = (Version1EditText) view.findViewById(R.id.etInvitation);

        if(emailAddress != null) {
            etEmailAddress.setText(emailAddress);
        }
        if(invitationCode != null) {
            etInvitation.setText(invitationCode);
        }

        if (!isSettingsVariant) {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofDefaultWallpaper().lightend());
        }

        setTitleOfPage();
        next = (Version1Button) view.findViewById(R.id.btnNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean emailValid = new EmailValidator(etEmailAddress).isValid();
                boolean inviteCodeCheck = new NotEmptyValidator(getActivity(), etInvitation).isValid();
                if (!emailValid || !inviteCodeCheck) {
                    return;
                }



                    showProgressBarAndDisable(next);
                if(TextUtils.isEmpty(CorneaClientFactory.getClient().getConnectionURL())) {
                    CorneaClientFactory.getClient().setConnectionURL(PreferenceUtils.getPlatformUrl());
                }
                    CorneaClientFactory.getService(InvitationService.class)
                            .getInvitation(etInvitation.getText().toString(), etEmailAddress.getText().toString())
                            .onSuccess(Listeners.runOnUiThread(new Listener<InvitationService.GetInvitationResponse>() {
                                @Override
                                public void onEvent(InvitationService.GetInvitationResponse getInvitationResponse) {
                                    Map<String, Object> invite = getInvitationResponse.getInvitation();
                                    if (invite == null) {
                                        onError(new RuntimeException("Invite was null, but no error from server."));
                                        return;
                                    }

                                    String first = (String) invite.get("invitorFirstName");
                                    String last = (String) invite.get("invitorLastName");
                                    String personName = String.format("%s %s", first == null ? "" : first, last == null ? "" : last);

                                    String placeName = (String) invite.get("placeName");
                                    placeName = placeName == null ? "" : placeName;

                                    String inviteeEmail = (String) invite.get("inviteeEmail");

                                    String placeID = String.valueOf(invite.get("placeId"));
                                    if (isSettingsVariant) {
                                        showAccept(placeName, personName, placeID, inviteeEmail);
                                    }
                                    else {
                                        String inviteeFirst = (String) invite.get("inviteeFirstName");
                                        String inviteeLast = (String) invite.get("inviteeLastName");
                                        DeviceContact contact = new DeviceContact();
                                        contact.addEmailAddress(etEmailAddress.getText().toString(), getResources().getString(R.string.type_home));
                                        contact.setFirstName(inviteeFirst);
                                        contact.setLastName(inviteeLast);
                                        contact.setPlaceID(placeID);
                                        contact.setValidationCode(etInvitation.getText().toString());
                                        contact.setInvitationEmail(inviteeEmail);
                                        contact.setInvitedPlaceName(placeName);
                                        contact.setInvitorFirstName(first);
                                        contact.setInvitorLastName(last);
                                        BackstackManager.getInstance().navigateToFragment(InvitationSuccessFragment.newInstance(contact), true);
                                    }
                                }
                            }))
                            .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                                @Override
                                public void onEvent(Throwable throwable) {
                                    showInvalid();
                                }
                            }));
            }
        });
        return view;
    }

    protected void showAccept(String toPlace, String fromPerson, final String forPlaceID, final String inviteeEmail) {
        hideProgressBarAndEnable(next);


        InfoButtonPopup popup = InfoButtonPopup.newInstance(
                getString(R.string.accept_invitation_title).toUpperCase(),
                getString(R.string.accept_invitation_desc, toPlace, fromPerson),
                getString(R.string.accept),
                getString(R.string.decline),
                Version1ButtonColor.MAGENTA,
                Version1ButtonColor.BLACK
        );

        // popup callback
        popup.setCallback(new InfoButtonPopup.Callback() {
            @Override public void confirmationValue(boolean topButton) {

                final PersonModel personModel = SessionController.instance().getPerson();
                if (personModel == null) {
                    onError(new RuntimeException("Lost our logged in person. Cannot accept/decline invite."));
                    return;
                }

                showProgressBarAndDisable(next);
                String code = etInvitation.getText().toString();
                String email = etEmailAddress.getText().toString();

                if (topButton) { // Accept
                    personModel.acceptInvitation(code, email)
                               .onFailure(genericErrorListener)
                               .onSuccess(Listeners.runOnUiThread(new Listener<Person.AcceptInvitationResponse>() {
                                   @Override
                                   public void onEvent(Person.AcceptInvitationResponse acceptInvitationResponse) {
                                       DeviceContact contact = new DeviceContact();
                                       contact.addEmailAddress(etEmailAddress.getText().toString(),
                                               getResources().getString(R.string.type_home));
                                       contact.setFirstName(personModel.getFirstName());
                                       contact.setLastName(personModel.getLastName());
                                       contact.setPlaceID(forPlaceID);
                                       contact.setValidationCode(etInvitation.getText().toString());
                                       contact.setInvitationEmail(inviteeEmail);

                                       new AccountCreationSequenceController(
                                               AccountTypeSequence.CURRENT_USER_INVITE_ACCEPT,
                                               contact).startSequence(getActivity(), null, SettingsUpdatePin.class);
                                   }
                               }));
                } else { // Decline
                    personModel.rejectInvitation(code, email, null) // No "Reason" for now.
                               .onFailure(genericErrorListener)
                               .onSuccess(Listeners.runOnUiThread(new Listener<Person.RejectInvitationResponse>() {
                                   @Override public void onEvent(Person.RejectInvitationResponse rejectInvitationResponse) {
                                       hideProgressBar();BackstackManager.withAnimation(TransitionEffect.FADE).navigateBackToFragment(
                                               SideNavSettingsFragment.newInstance());
                                   }
                               }));
                }
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    protected void onError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    protected void parseErrorException(ErrorResponseException ex) {
        String NOT_FOUND = "request.destination.notfound";
        String prefix = "place";
        String code = String.valueOf(ex.getCode()).toLowerCase();
        String reason = String.valueOf(ex.getErrorMessage()).toLowerCase().trim();
        if (code.equals(NOT_FOUND) && reason.startsWith(prefix)) {
            AlertPopup alert = AlertPopup.newInstance(
                  getString(R.string.invite_not_valid_title),
                  getString(R.string.invite_not_valid_desc),
                  null, new AlertPopup.AlertButtonCallback() {
                      @Override public boolean topAlertButtonClicked() { return false; }
                      @Override public boolean bottomAlertButtonClicked() { return false; }
                      @Override public boolean errorButtonClicked() { return false; }
                      @Override public void close() {
                          BackstackManager.getInstance().navigateBack();
                      }
                  }
            );
            BackstackManager.getInstance().navigateToFloatingFragment(alert, alert.getClass().getCanonicalName(), true);
        }
        else {
            onError(ex);
        }
    }

    protected void showInvalid() {
        hideProgressBarAndEnable(next);
        String notValidText = isSettingsVariant ? getString(R.string.code_not_valid_desc) : getString(R.string.code_not_valid_short_desc);
        alertpopup = AlertFloatingFragment.newInstance(
              getString(R.string.code_not_valid_title),
              notValidText,
              null, null, null
        );
        BackstackManager.getInstance().navigateToFloatingFragment(alertpopup, alertpopup.getClass().getSimpleName(), true);
    }

    @Override public void onPause() {
        super.onPause();
        hideProgressBarAndEnable(next);
    }

    protected void setTitleOfPage() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.setTitle(getTitle());
    }

    @Nullable @Override public String getTitle() {
        return isSettingsVariant ? getString(R.string.invitation_code) : getResources().getString(R.string.invitation_title);
    }

    @Override
    public Integer getLayoutId() {
        return isSettingsVariant ? R.layout.invite_settings_fragment : R.layout.fragment_invitation;
    }

    public void handleBackPress() {
        if (popupVisible()) {
            BackstackManager.getInstance().navigateBack();
        } else {
            LaunchActivity.startLoginScreen(getActivity());
        }
    }

    public boolean popupVisible() {
        if (alertpopup != null && alertpopup.isVisible()) {
            return true;
        }
        return false;
    }
}
