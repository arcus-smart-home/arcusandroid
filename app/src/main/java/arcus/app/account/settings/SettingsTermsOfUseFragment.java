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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.common.adapters.IconizedChevronListAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.WebViewFragment;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.utils.GlobalSetting;

import java.util.ArrayList;


public class SettingsTermsOfUseFragment extends BaseFragment {

    private ListView listView;

    @NonNull
    public static SettingsTermsOfUseFragment newInstance() {
        SettingsTermsOfUseFragment fragment = new SettingsTermsOfUseFragment();
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        listView = (ListView) view.findViewById(R.id.settings_terms_of_use_listview);

        loadContent();
        return view;
    }

    private void loadContent(){
        ArrayList<ListItemModel> data = new ArrayList<>();
        data.add(new ListItemModel(getString(R.string.settings_terms_conditions),getString(R.string.settings_terms_conditions_instr)));
        data.add(new ListItemModel(getString(R.string.settings_privacy),getString(R.string.settings_privacy_instr)));

        IconizedChevronListAdapter listAdapter = new IconizedChevronListAdapter(getActivity(), data);
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                WebViewFragment webViewFragment = new WebViewFragment();
                Bundle bundle = new Bundle(1);

                switch (position) {
                    case 0: // terms
                        Uri tAndC = Uri.parse(GlobalSetting.T_AND_C_LINK);
                        Intent intent = new Intent(Intent.ACTION_VIEW, tAndC);
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                        else {
                            bundle.putString(WebViewFragment.KEY_ARGUMENT_URL, GlobalSetting.T_AND_C_LINK);
                            webViewFragment.setArguments(bundle);
                            BackstackManager.getInstance().navigateToFragment(webViewFragment, true);
                        }
                        break;
                    case 1: // privacy
                        Uri privacy = Uri.parse(GlobalSetting.PRIVACY_LINK);
                        Intent privacyIntent = new Intent(Intent.ACTION_VIEW, privacy);
                        if (privacyIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(privacyIntent);
                        }
                        else {
                            bundle.putString(WebViewFragment.KEY_ARGUMENT_URL, GlobalSetting.PRIVACY_LINK);
                            webViewFragment.setArguments(bundle);
                            BackstackManager.getInstance().navigateToFragment(webViewFragment, true);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle().toUpperCase());
    }

    @Override
    public String getTitle() {
        return getString(R.string.account_settings_terms_of_use);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_settings_terms_of_use;
    }
}
