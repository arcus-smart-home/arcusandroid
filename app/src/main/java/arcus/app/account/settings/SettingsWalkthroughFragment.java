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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.account.settings.data.WalkthroughType;
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment;
import arcus.app.common.adapters.IconizedChevronListAdapter;
import arcus.app.common.backstack.TransitionEffect;
import arcus.app.common.controller.BackstackPopListener;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.WebViewFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;

import java.util.ArrayList;

public class SettingsWalkthroughFragment extends BaseFragment implements BackstackPopListener {

    private ListView tutorialsListView;

    @NonNull
    public static SettingsWalkthroughFragment newInstance() {
        return new SettingsWalkthroughFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view != null) {
            tutorialsListView = (ListView) view.findViewById(R.id.walkthrough_list);
            Version1Button moreInfoButton = (Version1Button) view.findViewById(R.id.more_information);
            moreInfoButton.setColorScheme(Version1ButtonColor.WHITE);
            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    WebViewFragment webViewFragment = new WebViewFragment();
                    Bundle arguments = new Bundle();
                    arguments.putString(WebViewFragment.KEY_ARGUMENT_URL, GlobalSetting.SUPPORT_URL);
                    webViewFragment.setArguments(arguments);
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(webViewFragment, true);
                }
            });

            setupView();
        }

        return view;
    }

    private void setupView() {

        ArrayList<ListItemModel> data = new ArrayList<>();
        data.add(new ListItemModel(getString(R.string.tutorials_security), ""));
        data.add(new ListItemModel(getString(R.string.tutorials_introduction), ""));
        data.add(new ListItemModel(getString(R.string.tutorials_climate), ""));
        data.add(new ListItemModel(getString(R.string.tutorials_rules), ""));
        data.add(new ListItemModel(getString(R.string.tutorials_scenes), ""));


        IconizedChevronListAdapter listAdapter = new IconizedChevronListAdapter(getActivity(), data);
        tutorialsListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        tutorialsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                switch (position) {
                    case 0: // security
                        WalkthroughBaseFragment security = WalkthroughBaseFragment.newInstance(WalkthroughType.SECURITY);
                        BackstackManager.getInstance().navigateToFloatingFragment(security, security.getClass().getName(), true);
                        break;
                    case 1: // introduction
                        WalkthroughBaseFragment intro = WalkthroughBaseFragment.newInstance(WalkthroughType.INTRO);
                        BackstackManager.getInstance().navigateToFloatingFragment(intro, intro.getClass().getName(), true);
                        break;
                    case 2: // climate
                        WalkthroughBaseFragment climate = WalkthroughBaseFragment.newInstance(WalkthroughType.CLIMATE);
                        BackstackManager.getInstance().navigateToFloatingFragment(climate, climate.getClass().getName(), true);
                        break;
                    case 3: // rules
                        WalkthroughBaseFragment rules = WalkthroughBaseFragment.newInstance(WalkthroughType.RULES);
                        BackstackManager.getInstance().navigateToFloatingFragment(rules, rules.getClass().getName(), true);
                        break;
                    case 4: // scenes
                        WalkthroughBaseFragment scenes = WalkthroughBaseFragment.newInstance(WalkthroughType.SCENES);
                        BackstackManager.getInstance().navigateToFloatingFragment(scenes, scenes.getClass().getName(), true);
                        break;
                    default:
                        break;
                }
            }
        });
    }


    public void onPopped() {
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
    }

    @Override
    public String getTitle() {
        return "SUPPORT";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_settings_walkthrough;
    }

}
