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
package arcus.app.subsystems.people;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.SessionController;
import arcus.cornea.utils.Listeners;
import com.iris.client.bean.Invitation;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.account.registration.controller.AccountCreationSequenceController;
import arcus.app.account.registration.model.AccountTypeSequence;
import arcus.app.account.settings.pin.SettingsUpdatePin;
import arcus.app.common.adapters.PendingInvitationListItemAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.InfoButtonPopup;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.device.ota.controller.FirmwareUpdateController;
import arcus.app.subsystems.people.model.DeviceContact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class InvitationsPendingFragment extends BaseFragment {

    private static Logger logger = LoggerFactory.getLogger(InvitationsPendingFragment.class);
    final Listener<Throwable> genericErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });

    private ListView invitationList;
    PendingInvitationListItemAdapter listAdapter;

    @NonNull
    public static InvitationsPendingFragment newInstance () {
        return new InvitationsPendingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        invitationList = (ListView) view.findViewById(R.id.pending_invitation_list);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        loadInvitationList();

        invitationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Invitation invite = (Invitation)listAdapter.getItem(position);
                showAccept(invite);

            }
        });
    }

    private void loadInvitationList() {
        final PersonModel personModel = SessionController.instance().getPerson();
        personModel.pendingInvitations()
                //TODO: APAP - what should we do here?
                //.onFailure(genericErrorListener)
                .onSuccess(Listeners.runOnUiThread(new Listener<Person.PendingInvitationsResponse>() {
                    @Override public void onEvent(Person.PendingInvitationsResponse pendingInvitationResponse) {
                        List<Map<String, Object>> pending = pendingInvitationResponse.getInvitations();
                        ArrayList<Invitation> invites = new ArrayList<>();
                        for(Map<String, Object> item : pending) {
                            Invitation invite = new Invitation();
                            invite.setStreetAddress1((String)item.get("streetAddress1"));
                            invite.setStreetAddress2((String)item.get("streetAddress2"));
                            invite.setCity((String)item.get("city"));
                            invite.setStateProv((String)item.get("stateProv"));
                            invite.setZipCode((String)item.get("zipCode"));

                            invite.setInvitorFirstName((String)item.get("invitorFirstName"));
                            invite.setInvitorLastName((String)item.get("invitorLastName"));
                            invite.setCode((String)item.get("code"));
                            invite.setInviteeEmail((String)item.get("inviteeEmail"));
                            invite.setPlaceId((String)item.get("placeId"));
                            invite.setPlaceName((String)item.get("placeName"));

                            invites.add(invite);
                        }
                        listAdapter = new PendingInvitationListItemAdapter(getActivity(), invites);
                        invitationList.setAdapter(listAdapter);
                    }
                }));
    }

    protected void showAccept(final Invitation invite) {
        String toPlace = invite.getPlaceName();
        String fromPerson = (invite.getInvitorFirstName()+" "+invite.getInvitorLastName()).trim();
        final String forPlaceID = invite.getPlaceId();
        hideProgressBar();
        InfoButtonPopup popup = InfoButtonPopup.newInstance(
                getString(R.string.accept_invitation_title).toUpperCase(),
                getString(R.string.accept_invitation_desc, toPlace, fromPerson),
                getString(R.string.accept),
                getString(R.string.decline),
                Version1ButtonColor.BLACK,
                Version1ButtonColor.BLACK
        );

        // What happens with back button?
        popup.setCallback(new InfoButtonPopup.Callback() {
            @Override public void confirmationValue(boolean topButton) {
                final PersonModel personModel = SessionController.instance().getPerson();
                if (personModel == null) {
                    onError(new RuntimeException("Lost our logged in person. Cannot accept/decline invite."));
                    return;
                }

                showProgressBar();
                final String code = invite.getCode();
                final String email = invite.getInviteeEmail();
                if (topButton) { // Accept
                    personModel.acceptInvitation(code, email)
                            .onFailure(genericErrorListener)
                            .onSuccess(Listeners.runOnUiThread(new Listener<Person.AcceptInvitationResponse>() {
                                @Override public void onEvent(Person.AcceptInvitationResponse acceptInvitationResponse) {
                                    hideProgressBar();
                                    DeviceContact contact = new DeviceContact();
                                    contact.addEmailAddress(email, getResources().getString(R.string.type_home));
                                    contact.setFirstName(personModel.getFirstName());
                                    contact.setLastName(personModel.getLastName());
                                    contact.setPlaceID(forPlaceID);
                                    contact.setValidationCode(code);
                                    loadInvitationList();
                                    new AccountCreationSequenceController(AccountTypeSequence.CURRENT_USER_INVITE_ACCEPT, contact).startSequence(getActivity(), null, SettingsUpdatePin.class);
                                }
                            }));
                }
                else { // Decline
                    personModel.rejectInvitation(code, email, null) // No "Reason" for now.
                            .onFailure(genericErrorListener)
                            .onSuccess(Listeners.runOnUiThread(new Listener<Person.RejectInvitationResponse>() {
                                @Override public void onEvent(Person.RejectInvitationResponse rejectInvitationResponse) {
                                    hideProgressBar();
                                    loadInvitationList();
                                }
                            }));
                }
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    protected void onError(Throwable throwable) {
        hideProgressBar();
        if (throwable instanceof ErrorResponseException) {
            parseErrorException((ErrorResponseException) throwable);
        }
        else {
            showGenericError(throwable);
        }
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
            showGenericError(ex);
        }
    }

    protected void showGenericError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void onPause () {
        super.onPause();
        FirmwareUpdateController.getInstance().stopFirmwareUpdateStatusMonitor();
    }

    @Override
    public String getTitle() {
        return getString(R.string.invitations);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_invitations_pending;
    }

}
