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
package arcus.app.device.pairing.post;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.subsystem.model.TimeZoneModel;
import arcus.cornea.utils.TimeZoneLoader;
import arcus.app.R;
import arcus.app.common.adapters.CheckableRecyclewViewAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.TimezonePickerPopup;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;
import arcus.app.integrations.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressValidationFragment extends BaseFragment implements CheckableRecyclewViewAdapter.CheckableRecyclerViewAdapterCallback,
        TimezonePickerPopup.Callback, TimeZoneLoader.Callback {
    private static final String PLACE_NAME = "PLACE_NAME";
    private static final String ADDRESS = "ADDRESS";
    private static final String SERVICE_LEVEL = "SERVICE_LEVEL";
    private static final String TIME_ZONE = "TIME_ZONE";
    private static final String SUGGESTIONS = "SUGGESTIONS";
    private static final String LIGHT_THEME = "LIGHT_THEME";

    private String placeName;
    private String timeZoneId;
    private RecyclerView recyclerViewSuggestions;
    private View recyclerLayout;
    private Version1Button saveButton;
    private CheckableRecyclewViewAdapter adapter;
    private Version1TextView screenTitle;
    private View screenSubTitleLayout;
    private View divider;
    private Version1TextView screenSubTitle;
    private Version1TextView screenSubTitleAddressLine1;
    private Version1TextView screenSubTitleAddressLine2;
    private Version1TextView screenSubTitleAddressCityStateZip;
    private boolean isPromon = false;
    private AddressValidationCallback callback;
    private ImageView checkbox;
    private View checkBoxLayout;
    private Version1TextView checkBoxDescription;
    private TimeZoneModel timeZone;
    private boolean isCheckBoxChecked = false;
    private boolean isLightMode = false;
    private Address initialAddress;

    private View useWhatITypedAddressBlock;
    private Version1TextView street1;
    private Version1TextView street2;
    private Version1TextView cityStateZip;

    ArrayList<Address> suggestions = new ArrayList<>();

    @Override
    public void itemChecked() {
        if(!isPromon) {
            if(isLightMode) {
                checkbox.setImageResource(R.drawable.circle_hollow_black);
            } else {
                checkbox.setImageResource(R.drawable.circle_hollow_white);
            }

            isCheckBoxChecked = false;
        }
    }

    public interface AddressValidationCallback {
        void noSuggestionsPromon();
        void noSuggestionsNonPromon();
        void useEnteredAddress(Address address, TimeZoneModel timeZone, boolean navigateBack);
        void useSuggestedAddress(Address address, TimeZoneModel timeZone);
    }

    public void setCallback(AddressValidationCallback callback) {
        this.callback = callback;
    }

    @NonNull
    public static AddressValidationFragment newInstance(@NonNull Address address, String placeName, String serviceLevel, String timeZoneId, ArrayList<Address> suggestions, boolean lightTheme) {
        AddressValidationFragment fragment = new AddressValidationFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PLACE_NAME, placeName);
        bundle.putParcelable(ADDRESS, address);
        bundle.putString(SERVICE_LEVEL, serviceLevel);
        bundle.putString(TIME_ZONE, timeZoneId);
        bundle.putParcelableArrayList(SUGGESTIONS, suggestions);
        bundle.putBoolean(LIGHT_THEME, lightTheme);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();

        //private CheckableRecyclewViewAdapter adapter;
        if (isLightMode) {
            screenTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            screenSubTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            screenSubTitleAddressLine1.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            screenSubTitleAddressLine2.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            screenSubTitleAddressCityStateZip.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            checkBoxDescription.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            street1.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            street2.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            cityStateZip.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            saveButton.setColorScheme(Version1ButtonColor.BLACK);
            checkbox.setImageResource(R.drawable.circle_hollow_black);
            divider.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black_with_60));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            initialAddress = bundle.getParcelable(ADDRESS);
            timeZoneId = bundle.getString(TIME_ZONE);
            placeName = bundle.getString(PLACE_NAME);
            String serviceLevel = bundle.getString(SERVICE_LEVEL);
            if(SubscriptionController.isProfessional()) {
                isPromon = true;
            } else {
                isPromon = false;
            }

            suggestions = bundle.getParcelableArrayList(SUGGESTIONS);
            isLightMode = bundle.getBoolean(LIGHT_THEME);
            //verify();
        } else {

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        recyclerViewSuggestions = (RecyclerView) view.findViewById(R.id.recyclerview_addresses);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewSuggestions.setLayoutManager(layoutManager);
        recyclerLayout = view.findViewById(R.id.recyclerview_layout);
        screenTitle = (Version1TextView) view.findViewById(R.id.screen_title);
        screenSubTitle = (Version1TextView) view.findViewById(R.id.screen_subtitle);
        screenSubTitleLayout = view.findViewById(R.id.subtitle_layout);
        screenSubTitleAddressLine1 = (Version1TextView) view.findViewById(R.id.screen_subtitle_address_line1);
        screenSubTitleAddressLine2 = (Version1TextView) view.findViewById(R.id.screen_subtitle_address_line2);
        screenSubTitleAddressCityStateZip = (Version1TextView) view.findViewById(R.id.screen_subtitle_address_citystatezip);
        checkBoxLayout = view.findViewById(R.id.checkbox_layout);
        checkBoxDescription = (Version1TextView) view.findViewById(R.id.checkbox_description);
        checkbox = (ImageView) view.findViewById(R.id.checkbox_address_verification);
        divider = view.findViewById(R.id.divider);

        checkBoxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPromon) {
                    if(isCheckBoxChecked) {
                        if(isLightMode) {
                            checkbox.setImageResource(R.drawable.circle_hollow_black);
                        } else {
                            checkbox.setImageResource(R.drawable.circle_hollow_white);
                        }
                    } else {
                        if(isLightMode) {
                            checkbox.setImageResource(R.drawable.circle_check_black_filled);
                        } else {
                            checkbox.setImageResource(R.drawable.circle_check_white_filled);
                        }
                    }
                    isCheckBoxChecked = !isCheckBoxChecked;
                    saveButton.setEnabled(isCheckBoxChecked);
                } else {
                    if(adapter.getItemCount() > 0) {
                        adapter.setSelection(-1);
                        adapter.notifyDataSetChanged();
                    }
                    if(isLightMode) {
                        checkbox.setImageResource(R.drawable.circle_check_black_filled);
                    } else {
                        checkbox.setImageResource(R.drawable.circle_check_white_filled);
                    }
                }
            }
        });

        saveButton = (Version1Button) view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user has chosen something other than user entered, prompt for timezone
                saveButton.setEnabled(false);
                if(adapter.getSelection() >= 0) {
                    if(isPromon) {
                        if(isCheckBoxChecked) {
                            Address address = adapter.getSelectedItem();
                            if(address != null) {
                                Address validatedAddress = removeNullAddressData(address);
                                callback.useSuggestedAddress(validatedAddress, timeZone);
                            } else {
                                callback.noSuggestionsPromon();
                            }
                        }
                    } else {
                        Address address = adapter.getSelectedItem();
                        if(address != null) {
                            Address validatedAddress = removeNullAddressData(address);
                            callback.useSuggestedAddress(validatedAddress, timeZone);
                        } else {
                            callback.noSuggestionsNonPromon();
                        }
                    }

                } else {
                    TimeZoneLoader.instance().setCallback(AddressValidationFragment.this);
                    TimeZoneLoader.instance().loadTimezones();
                }
            }
        });
        if(isPromon) {
            saveButton.setEnabled(isCheckBoxChecked);
        }

        useWhatITypedAddressBlock = view.findViewById(R.id.address_block);
        street1 = (Version1TextView) view.findViewById(R.id.line1);
        street2 = (Version1TextView) view.findViewById(R.id.line2);
        cityStateZip = (Version1TextView) view.findViewById(R.id.city_state_zip);

        updateWithSuggestions(suggestions);

        return view;
    }

    @Override
    public String getTitle() {
        return getString(R.string.account_registration_home_verify_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_verify_address;
    }

    private void updateWithSuggestions(ArrayList<Address> suggestions) {
        adapter = new CheckableRecyclewViewAdapter(getActivity(), suggestions, this, isLightMode);
        recyclerViewSuggestions.setAdapter(adapter);
        if(suggestions.size() == 0) {
            adapter.setSelection(-1);
            if(isPromon) {
                if(callback != null) {
                    callback.noSuggestionsPromon();
                }
            } else {
                checkBoxLayout.setVisibility(View.INVISIBLE);
                screenTitle.setText(getContext().getString(R.string.address_verification_no_suggestions_title));
                screenSubTitleLayout.setVisibility(View.VISIBLE);
                recyclerLayout.setVisibility(View.INVISIBLE);
                screenSubTitle.setText(getContext().getString(R.string.address_verification_no_suggestions_subtitle, placeName));
                fillAddressData();
                checkBoxDescription.setText(getContext().getString(R.string.address_verification_use_what_i_typed));
            }
        } else {
            checkBoxLayout.setVisibility(View.VISIBLE);
            recyclerLayout.setVisibility(View.VISIBLE);
            screenTitle.setText(getContext().getString(R.string.address_verification_suggestions_title));
            if(isPromon) {
                checkBoxDescription.setText(getContext().getString(R.string.address_verification_residential_confirmation));
                useWhatITypedAddressBlock.setVisibility(View.GONE);
            } else {
                checkBoxDescription.setText(getContext().getString(R.string.address_verification_use_what_i_typed));

                if(initialAddress == null) {
                    useWhatITypedAddressBlock.setVisibility(View.GONE);
                }
                street1.setText(initialAddress.getStreet());
                if(!TextUtils.isEmpty(initialAddress.getStreet2())) {
                    street2.setText(initialAddress.getStreet2());
                } else {
                    street2.setVisibility(View.GONE);
                }
                cityStateZip.setText(getContext().getString(R.string.address_verification_display, initialAddress.getCity(), initialAddress.getState(), initialAddress.getZipCode()));
            }
        }
    }

    private void fillAddressData() {
        screenSubTitleAddressLine1.setText(initialAddress.getStreet());
        if(!TextUtils.isEmpty(initialAddress.getStreet2())) {
            screenSubTitleAddressLine2.setText(initialAddress.getStreet2());
        } else {
            screenSubTitleAddressLine2.setVisibility(View.GONE);
        }
        screenSubTitleAddressCityStateZip.setText(getContext().getString(R.string.address_verification_display, initialAddress.getCity(), initialAddress.getState(), initialAddress.getZipCode()));
    }

    private Address removeNullAddressData(Address address) {
        if(address.getStreet() == null) {
            address.setStreet("");
        }
        if(address.getStreet2() == null) {
            address.setStreet2("");
        }
        if(address.getCity() == null) {
            address.setCity("");
        }
        if(address.getState() == null) {
            address.setState("");
        }
        if(address.getZipCode() == null) {
            address.setZipCode("");
        }
        return address;
    }

    @Override
    public void timeZoneSelected(TimeZoneModel timeZone) {
        this.timeZone = timeZone;
        if(callback == null) {
            return;
        }
        if(isPromon) {

        } else {
            if(adapter.getSelection() >= 0) {
                Address address = adapter.getSelectedItem();
                if(address != null) {
                    callback.useSuggestedAddress(address, timeZone);
                } else {
                    callback.noSuggestionsNonPromon();
                }
            } else {
                if(initialAddress != null) {
                    callback.useEnteredAddress(initialAddress, timeZone, true);
                }
            }
        }
    }

    @Override
    public void loaded(List<TimeZoneModel> timeZones) {
        TimezonePickerPopup popup = TimezonePickerPopup.newInstance(timeZoneId, timeZones, true);
        popup.setCallback(this);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void failed(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }
}
