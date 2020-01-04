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
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.google.common.collect.ImmutableMap;

import arcus.app.account.settings.contact.SettingsContactInfoFragment;
import arcus.app.account.settings.pin.SettingsUpdatePin;
import arcus.cornea.model.PersonModelProxy;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.cornea.platformcall.PPARemovalController;
import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.model.PersonModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.popups.InfoButtonPopup;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.view.CircularImageView;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.settings.adapter.SettingsListAdapter;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;


public class SettingsPersonFragment extends BaseFragment implements IShowedFragment {
    static String PERSON = "PERSON", PLACE = "PLACE";

    CircularImageView personImage;
    View imageClickableRegion, cameraImage, listViewContainer;
    Version1TextView personName, phoneNumber, personEmail;
    ListView settingsList;
    PersonModelProxy personModelProxy;
    PlaceAndRoleModel placeAndRoleModel;
    Button removeButton;
    final PPARemovalController ppaRemoveController = new PPARemovalController(new PPARemovalController.RemovedCallback() {
        @Override public void onSuccess() {
            hideProgressBar();
            saving.set(false);
            BackstackManager.getInstance().navigateBack();
        }

        @Override public void onError(Throwable throwable) {
            hideProgressBar();
            saving.set(false);
            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        }
    });
    private AtomicBoolean saving = new AtomicBoolean(false);

    @NonNull public static SettingsPersonFragment newInstance(@Nullable PersonModelProxy person, PlaceAndRoleModel placeAndRoleModel) {
        SettingsPersonFragment instance = new SettingsPersonFragment();
        Bundle arguments = new Bundle(2);
        arguments.putParcelable(PERSON, person);
        arguments.putParcelable(PLACE, placeAndRoleModel);
        instance.setArguments(arguments);
        return instance;
    }

    @NonNull public static SettingsPersonFragment newInstance(@Nullable String personAddress, String placeID) {
        PersonModelProxy personModelProxy = new PersonModelProxy(personAddress);
        PlaceAndRoleModel placeAndRoleModel = new PlaceAndRoleModel(ImmutableMap.<String, Object>of(Place.ATTR_ID, placeID));
        return newInstance(personModelProxy, placeAndRoleModel);
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        personModelProxy  = getArguments().getParcelable(PERSON);
        placeAndRoleModel = getArguments().getParcelable(PLACE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null || personModelProxy == null || placeAndRoleModel == null) {
            return view;
        }

        imageClickableRegion = view.findViewById(R.id.photo_layout);
        cameraImage = view.findViewById(R.id.camera_image);
        personImage = (CircularImageView) view.findViewById(R.id.fragment_account_camera);
        personName = (Version1TextView) view.findViewById(R.id.person_name);
        phoneNumber = (Version1TextView) view.findViewById(R.id.phone_number);
        personEmail = (Version1TextView) view.findViewById(R.id.email_address);
        listViewContainer = view.findViewById(R.id.list_view_container);
        settingsList = (ListView) view.findViewById(R.id.settings_list);
        settingsList.setDivider(null);
        removeButton = (Button) view.findViewById(R.id.remove_button);

        if (personModelProxy.isInvited()) {
            showInvited();
        }
        else {
            loadPerson();
        }

        return view;
    }

    @Override public void onResume() {
        super.onResume();
        View view = getView();
        if (view == null || personModelProxy == null || placeAndRoleModel == null) {
            return;
        }

        setImage(personModelProxy.getPersonAddress());
    }

    protected void loadPerson() {
        PersonModelProvider.instance()
              .getModel(personModelProxy.getPersonAddress()).load()
              .onSuccess(Listeners.runOnUiThread(new Listener<PersonModel>() {
                  @Override public void onEvent(PersonModel person) {
                      selectRendering(person);
                  }
              }));
    }

    protected void selectRendering(PersonModel person) {
        if (personModelProxy.isOwner()) {
            showOwner(person);
        }
        else if (personModelProxy.isClone()) {
            showFullAccess(person);
        }
        else if (personModelProxy.isHobbit()) {
            showHobbit(person);
        }
        else {
            showError();
        }
    }

    protected void showInvited() {
        personImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.icon_user_large_white));
        imageClickableRegion.setClickable(false);
        cameraImage.setVisibility(View.GONE);
        personName.setText(personModelProxy.getFullName().toUpperCase());
        personEmail.setText(String.format(getResources().getString(R.string.invitation_date), DateUtils.format(new Date(personModelProxy.getInvitedDate()))));
        phoneNumber.setVisibility(View.GONE);

        SettingsList items = new SettingsList();
        items.add(getPermissionsSetting(getString(R.string.full_access_camel_case)));
        removeButton.setText(getString(R.string.cancel_invitation));
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AlertFloatingFragment popup = AlertFloatingFragment.newInstance(
                      getString(R.string.are_you_sure).toUpperCase(),
                      getString(R.string.will_have_to_reinvite_text),
                      getString(R.string.cancel_invitation),
                      null,
                      getString(R.string.action_cannot_be_reversed),
                      new AlertFloatingFragment.AlertButtonCallback() {
                          @Override public boolean topAlertButtonClicked() {
                              if (!saving.getAndSet(true)) {
                                  showProgressBar();
                                  ppaRemoveController.cancelInvite(placeAndRoleModel.getAddress(), personModelProxy.getCode());
                              }
                              return true;
                          }

                          @Override public boolean bottomAlertButtonClicked() { return false; }
                      }
                );
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
            }
        });
        settingsList.setAdapter(new SettingsListAdapter(getActivity(), items));
    }

    protected void showOwner(@NonNull PersonModel person) {
        setImageAndText(person);
        listViewContainer.setVisibility(View.GONE);
    }

    protected void showFullAccess(@NonNull PersonModel person) {
        setImageAndText(person);
        SettingsList items = new SettingsList();
        items.add(getPermissionsSetting(getString(R.string.full_access_camel_case)));
        removeButton.setText(getString(R.string.people_remove_person));
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                InfoButtonPopup popup = InfoButtonPopup.newInstance(
                      getString(R.string.are_you_sure).toUpperCase(),
                      getString(R.string.remove_person_text, placeAndRoleModel.getName()),
                      getString(R.string.remove_text),
                      getString(R.string.cancel_text),
                      Version1ButtonColor.MAGENTA,
                      Version1ButtonColor.BLACK
                );
                popup.setCallback(new InfoButtonPopup.Callback() {
                    @Override public void confirmationValue(boolean correct) {
                        if (correct && !saving.getAndSet(true)) {
                            showProgressBar();
                            ppaRemoveController.removeAccessToPlaceFor(placeAndRoleModel.getAddress(), personModelProxy.getPersonAddress());
                        }
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
            }
        });
        settingsList.setAdapter(new SettingsListAdapter(getActivity(), items));
    }

    protected void showHobbit(@NonNull PersonModel person) {
        setImageAndText(person);
        SettingsList items = new SettingsList();
        items.add(getSettingFor(getString(R.string.people_contact_info), null, null,
              SettingsContactInfoFragment.newInstance(personModelProxy.getPersonAddress(), SettingsContactInfoFragment.ScreenVariant.HIDE_PASSWORD_EDIT))
        );
        items.add(getSettingFor(getString(R.string.people_pin_code), null, null,
              SettingsUpdatePin.newInstance(SettingsUpdatePin.ScreenVariant.SETTINGS, personModelProxy.getPersonAddress(), placeAndRoleModel.getPlaceId()))
        );
        removeButton.setText(getString(R.string.people_remove_person));
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String placeName = TextUtils.isEmpty(placeAndRoleModel.getName()) ? "" : placeAndRoleModel.getName();
                InfoButtonPopup popup = InfoButtonPopup.newInstance(
                      getString(R.string.are_you_sure).toUpperCase(),
                      getString(R.string.remove_person_text, placeName),
                      getString(R.string.remove_text),
                      getString(R.string.cancel),
                      Version1ButtonColor.MAGENTA,
                      Version1ButtonColor.BLACK
                );
                popup.setCallback(new InfoButtonPopup.Callback() {
                    @Override public void confirmationValue(boolean correct) {
                        if (correct) {
                            showProgressBar();
                            ppaRemoveController.removePerson(placeAndRoleModel.getPlaceId(), personModelProxy.getPersonAddress());
                        }
                    }
                });

                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getTag(), true);
            }
        });
        settingsList.setAdapter(new SettingsListAdapter(getActivity(), items));
    }

    protected void showError() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        personImage.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.icon_user_large_white));
        imageClickableRegion.setVisibility(View.GONE);
        listViewContainer.setVisibility(View.GONE);
    }

    protected void setImageAndText(@NonNull final PersonModel person) {
        ImageManager.with(getActivity())
              .putLargePersonImage(person.getAddress())
              .withPlaceholder(R.drawable.icon_user_large_white)
              .withTransform(new CropCircleTransformation())
              .into(personImage)
              .execute();

        setImage(person.getAddress());

        String first = TextUtils.isEmpty(person.getFirstName()) ? "" : person.getFirstName();
        String last = TextUtils.isEmpty(person.getLastName()) ? "" : person.getLastName();
        personName.setText(String.format("%s %s", first, last).trim().toUpperCase());

        String ph = person.getMobileNumber();
        phoneNumber.setVisibility(TextUtils.isEmpty(ph) ? View.GONE : View.VISIBLE);
        phoneNumber.setText(ph);

        String email = person.getEmail();
        personEmail.setVisibility(TextUtils.isEmpty(email) ? View.GONE : View.VISIBLE);
        personEmail.setText(email);
    }

    protected void setImage(@NonNull final String personAddress) {
        imageClickableRegion.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ImageManager.with(getActivity())
                        .putUserGeneratedPersonImage(personAddress)
                        .fromCameraOrGallery()
                        .withTransform(new CropCircleTransformation())
                        .into(personImage)
                        .execute();
            }
        });

    }

    @Override @Nullable public String getTitle() {
        return getString(R.string.people_people);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_person_settings;
    }

    @Override public void onShowedFragment() {
        if (personModelProxy == null || placeAndRoleModel == null) {
            return;
        }

        ImageManager.with(ArcusApplication.getContext())
              .putPersonBackgroundImage(personModelProxy.getPersonAddress(), placeAndRoleModel.getPlaceId())
              .intoWallpaper(AlphaPreset.DARKEN)
              .execute();
    }

    protected Setting getPermissionsSetting(String accessLevel) {
        return new OnClickActionSetting(getString(R.string.permissions).toUpperCase(), null, accessLevel, new View.OnClickListener() {
            @Override public void onClick(View v) {
                InfoTextPopup popup = InfoTextPopup.newInstance(R.string.people_with_full_access_blurb, R.string.people_full_access);
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
            }
        });
    }

    protected Setting getSettingFor(String title, String subText, String abstractText, final Fragment fragment) {
        return new OnClickActionSetting(title.toUpperCase(), null, abstractText, new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (fragment != null) {
                    BackstackManager.getInstance().navigateToFragment(fragment, true);
                }
            }
        });
    }
}
