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
package arcus.app.account.settings;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import arcus.cornea.SessionController;
import arcus.cornea.platformcall.PPARemovalController;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.Account;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.model.AccountModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.activities.LaunchActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.InfoButtonPopup;
import arcus.app.common.utils.LoginUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1EditText;
import arcus.app.dashboard.HomeFragment;
import arcus.app.subsystems.alarm.AlertFloatingFragment;


public class SettingsRemoveFragment extends BaseFragment implements PPARemovalController.RemovedCallback {
    static String TARGET_REMOVAL_ADDRESS = "TARGET_REMOVAL_ADDRESS";
    static String TYPE_OF_REMOVAL = "TYPE_OF_REMOVAL";
    static String TARGET_PERSON_ADDRESS = "TARGET_PERSON_ADDRESS";
    static final int UNKNOWN = 0x0A, ACCOUNT = 0x0B, PLACE = 0x0C, ACCESS = 0x0D, ACCOUNT_FULL_ACCESS = 0x0E;

    String REMOVE = "REMOVE", DELETE = "DELETE";

    Version1EditText editText;
    Version1Button removeBtn;

    String alertPromptTitle, alertButtonTopText, alertButtonBottomText,
           alertPopupSubTitle, hintKeyword, hintText, fragmentTitle, removeInstructions, removePromptFragment;
    Fragment popup;

    String targetAddress, targetPersonAddress;
    PPARemovalController ppaRemovalController;

    int removalType;
    transient AccountModel accountModel;
    transient PlaceModel   placeModel;
    transient PersonModel personModel;

    final TextWatcher removeTypedWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            if (s == null || removeBtn == null) {
                return;
            }

            removeBtn.setEnabled(hintKeyword.equalsIgnoreCase(s.toString().trim()));
        }
    };

    @NonNull public static SettingsRemoveFragment removeAccountInstance(String placeAddress) {
        SettingsRemoveFragment fragment = new SettingsRemoveFragment();

        Bundle args = new Bundle(1);
        args.putInt(TYPE_OF_REMOVAL, ACCOUNT);
        args.putString(TARGET_REMOVAL_ADDRESS, placeAddress);
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull public static SettingsRemoveFragment removeFullAccessAccountInstance() {
        SettingsRemoveFragment fragment = new SettingsRemoveFragment();

        Bundle args = new Bundle(1);
        args.putInt(TYPE_OF_REMOVAL, ACCOUNT_FULL_ACCESS);
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull public static SettingsRemoveFragment removePlace(@NonNull String placeAddress) {
        SettingsRemoveFragment fragment = new SettingsRemoveFragment();

        Bundle args = new Bundle(2);
        args.putString(TARGET_REMOVAL_ADDRESS, placeAddress);
        args.putInt(TYPE_OF_REMOVAL, PLACE);
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull public static SettingsRemoveFragment removeAccess(@NonNull String placeAddress, @NonNull String personAddress) {
        SettingsRemoveFragment fragment = new SettingsRemoveFragment();

        Bundle args = new Bundle(3);
        args.putString(TARGET_REMOVAL_ADDRESS, placeAddress);
        args.putString(TARGET_PERSON_ADDRESS, personAddress);
        args.putInt(TYPE_OF_REMOVAL, ACCESS);
        fragment.setArguments(args);

        return fragment;
    }
    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            removalType = UNKNOWN;
            return;
        }

        removalType = args.getInt(TYPE_OF_REMOVAL, UNKNOWN);
        targetAddress = args.getString(TARGET_REMOVAL_ADDRESS, null);
        targetPersonAddress = args.getString(TARGET_PERSON_ADDRESS, null);
    }

    @Override public void onPause() {
        super.onPause();
    }

    @Override public void onResume() {
        super.onResume();
        if (removalType == UNKNOWN) {
            return;
        }

        alertPromptTitle = getString(R.string.are_you_sure).toUpperCase();
        alertPopupSubTitle = getString(R.string.action_cannot_be_reversed);
        alertButtonTopText = getString(R.string.remove_text).toUpperCase();
        alertButtonBottomText = getString(R.string.cancel_text).toUpperCase();
        hintKeyword = REMOVE;

        ppaRemovalController = new PPARemovalController(this);
        switch (removalType) {
            case ACCOUNT:
                if (!TextUtils.isEmpty(targetAddress)) {
                    loadPlaceAccount();
                }
                break;

            case PLACE:
                if (!TextUtils.isEmpty(targetAddress)) {
                    loadPlace();
                }
                break;

            case ACCESS:
                if (!TextUtils.isEmpty(targetAddress) && !TextUtils.isEmpty(targetPersonAddress)) {
                    loadPersonPlace();
                }
                break;

            case ACCOUNT_FULL_ACCESS:
                targetPersonAddress = SessionController.instance().getPersonId();
                if (!TextUtils.isEmpty(targetPersonAddress)) {
                    targetPersonAddress = Addresses.toObjectAddress(Person.NAMESPACE, Addresses.getId(targetPersonAddress));
                    setupStrings();
                }
                break;

            default:
            case UNKNOWN:
                break;
        }

        logger.debug("REMOVING the [{}] to [{}] for [{}]", removalType, targetAddress, targetPersonAddress);
    }

    @Override public String getTitle() {
        return fragmentTitle;
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_remove;
    }

    protected void loadPlace() {
        CachedModelSource.<PlaceModel>get(targetAddress).load()
              .onSuccess(Listeners.runOnUiThread(new Listener<PlaceModel>() {
                  @Override public void onEvent(PlaceModel model) {
                      placeModel = model;
                      setupStrings();
                  }
              }));
    }

    protected void loadAccount(String accountID) {
        accountID = Addresses.toObjectAddress(Account.NAMESPACE, Addresses.getId(accountID));
        CachedModelSource.<AccountModel>get(accountID).load()
              .onSuccess(Listeners.runOnUiThread(new Listener<AccountModel>() {
                  @Override public void onEvent(AccountModel model) {
                      accountModel = model;
                      setupStrings();
                  }
              }));
    }

    protected void loadPersonPlace() {
        targetPersonAddress = Addresses.toObjectAddress(Person.NAMESPACE, Addresses.getId(targetPersonAddress));
        CachedModelSource.<PersonModel>get(targetPersonAddress).load()
              .onSuccess(Listeners.runOnUiThread(new Listener<PersonModel>() {
                  @Override public void onEvent(PersonModel model) {
                      personModel = model;
                      loadPlace();
                  }
              }));
    }

    protected void loadPlaceAccount() {
        targetAddress = Addresses.toObjectAddress(Place.NAMESPACE, Addresses.getId(targetAddress));
        CachedModelSource.<PlaceModel>get(targetAddress).load()
              .onSuccess(Listeners.runOnUiThread(new Listener<PlaceModel>() {
                  @Override public void onEvent(PlaceModel model) {
                      placeModel = model;
                      loadAccount(placeModel.getAccount());
                  }
              }));
    }

    protected void setupStrings() {
        switch (removalType) {
            case ACCOUNT:
                setupRemoveAccountLayoutElements(); // Sets up Strings, Onclick listeners, etc.
                break;

            case PLACE:
                setupRemovePlaceLayoutElements(); // Sets up Strings, Onclick listeners, etc.
                break;

            case ACCESS:
                setupRemoveAccessLayoutElements(); // Sets up Strings, Onclick listeners, etc.
                break;

            case ACCOUNT_FULL_ACCESS:
                setupRemoveAccessLogoutLayoutElements(); // Sets up Strings, Onclick listeners, etc.
                break;

            default:
                break;
        }

        hintText = getString(R.string.type_keyword_here, hintKeyword); // Set late since account uses DELETE and others use REMOVE
        renderLayout(); // applys the text values etc to the UI elements (shows to user what we think we're doing)
    }

    protected void setupRemoveAccountLayoutElements() {
        removeInstructions = getString(R.string.remove_account_instructions);
        removePromptFragment = getString(R.string.remove_account_prompt);
        fragmentTitle = getString(R.string.remove_account_title);
        hintKeyword = DELETE;
        popup = AlertFloatingFragment.newInstance(
              alertPromptTitle, removePromptFragment, alertButtonTopText, alertButtonBottomText, alertPopupSubTitle,
              new AlertFloatingFragment.AlertButtonCallback() {
                  @Override public boolean topAlertButtonClicked() {
                      disableFieldsAndShowProgress();
                      ppaRemovalController.removeAccountAndLogin(accountModel.getAddress());
                      return true;
                  }

                  @Override public boolean bottomAlertButtonClicked() {
                      return true;
                  }
              }
        );
    }

    protected void setupRemovePlaceLayoutElements() {
        removeInstructions = getString(R.string.remove_place_top_text, placeModel.getName());
        removePromptFragment = getString(R.string.settings_remove_place);
        fragmentTitle = getString(R.string.remove_text);
        popup = AlertFloatingFragment.newInstance(
              alertPromptTitle, removePromptFragment, alertButtonTopText, alertButtonBottomText, alertPopupSubTitle,
              new AlertFloatingFragment.AlertButtonCallback() {
                  @Override public boolean topAlertButtonClicked() {
                      disableFieldsAndShowProgress();
                      ppaRemovalController.removePlace(placeModel.getAddress());
                      return true;
                  }

                  @Override public boolean bottomAlertButtonClicked() {
                      new EnableRemoveButton().execute();
                      return true;
                  }
              },
                new AlertFloatingFragment.AlertDismissedCallback() {

                    @Override
                    public void dismissed() {
                        new EnableRemoveButton().execute();
                    }
                }
        );
    }

    protected void setupRemoveAccessLayoutElements() {
        removeInstructions = getString(R.string.remove_access_instructions, placeModel.getName(), hintKeyword);
        removePromptFragment = getString(R.string.remove_access_prompt_text);
        fragmentTitle = getString(R.string.remove_text);
        popup = InfoButtonPopup.newInstance(
              alertPromptTitle, alertPopupSubTitle, removePromptFragment, alertButtonTopText, alertButtonBottomText,
              Version1ButtonColor.MAGENTA, Version1ButtonColor.BLACK
        );
        ((InfoButtonPopup) popup).setCallback(new InfoButtonPopup.Callback() {
            @Override public void confirmationValue(boolean correct) {
                if (correct) {
                    disableFieldsAndShowProgress();
                    ppaRemovalController.removeAccessToPlaceFor(targetAddress, targetPersonAddress);
                }
            }
        });
    }

    protected void setupRemoveAccessLogoutLayoutElements() {
        hintKeyword = DELETE;
        removeInstructions = getString(R.string.remove_account_instructions);
        removePromptFragment = getString(R.string.remove_account_last_access_prompt);
        fragmentTitle = getString(R.string.remove_account_title);
        popup = AlertFloatingFragment.newInstance(
              alertPromptTitle, removePromptFragment, alertButtonTopText, alertButtonBottomText, alertPopupSubTitle,
              new AlertFloatingFragment.AlertButtonCallback() {
                  @Override public boolean topAlertButtonClicked() {
                      disableFieldsAndShowProgress();
                      ppaRemovalController.deletePersonLogin(targetPersonAddress);
                      return true;
                  }

                  @Override public boolean bottomAlertButtonClicked() {
                      return true;
                  }
              }
        );
    }

    protected void renderLayout() {
        View view = getView();
        if (view == null) {
            return;
        }

        removeBtn = (Version1Button) view.findViewById(R.id.fragment_remove_btn);
        editText = (Version1EditText) view.findViewById(R.id.remove_text_entry);
        editText.addTextChangedListener(removeTypedWatcher);
        editText.setHint(hintText);

        TextView topRemoveText = (TextView) view.findViewById(R.id.top_remove_text);
        TextView bottomRemoveText = (TextView) view.findViewById(R.id.bottom_remove_text);
        topRemoveText.setText(removeInstructions);
        bottomRemoveText.setText(removePromptFragment);

        removeBtn.setEnabled(false);
        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                removeBtn.setEnabled(false);
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
            }
        });

        setTitle();
    }

    protected void disableFieldsAndShowProgress() {
        showProgressBar();
        editText.removeTextChangedListener(removeTypedWatcher);
        removeBtn.setEnabled(false);
    }

    @SuppressWarnings("all") protected void logout() {
        SessionController.instance().logout();
        LoginUtils.completeLogout();
        ArcusApplication.getRegistrationContext().setHubID(null); // FIXME: (eanderso) 4/21/16 Remove ref's to Reg Context..

        Activity activity = getActivity();
        if (activity != null) {
            LaunchActivity.startLoginScreen(activity);
            activity.finishAffinity();
        }
    }

    @Override public void onSuccess() {
        hideProgressBar();
        switch(removalType) {
            case ACCOUNT:
                logout();
                break;
            case ACCOUNT_FULL_ACCESS:
                logout();
                break;
            case PLACE:
            case ACCESS:
                hideProgressBar();
                BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
                break;

            default:
            case UNKNOWN:
                break;
        }
    }

    @Override public void onError(Throwable throwable) {
        hideProgressBar();
        if (removeBtn != null && editText != null) {
            removeBtn.setEnabled(true);
            editText.addTextChangedListener(removeTypedWatcher);
        }
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    private class EnableRemoveButton extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while(BackstackManager.getInstance().isFragmentOnStack(AlertFloatingFragment.class)) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            removeBtn.setEnabled(true);
        }
    }
}
