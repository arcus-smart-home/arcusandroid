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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.model.PersonModelProxy;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.view.CarouselViewPager;
import arcus.app.common.view.CarouselViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SettingsPeopleDetailsList extends BaseFragment {
    private static final String PEOPLE_LIST     = "PEOPLE_LIST";
    private static final String SELECTED_PERSON = "SELECTED_PERSON";
    private static final String PLACE           = "PLACE";

    int viewing = 0;
    CarouselViewPager pager;
    CarouselViewPagerAdapter<String> adapter;
    View leftHandle, rightHandle;
    ArrayList<PersonModelProxy> peopleAddresses;
    PlaceAndRoleModel placeAndRoleModel;
    boolean multiPage = false;

    public static SettingsPeopleDetailsList newInstance(List<PersonModelProxy> addresses, @Nullable PersonModelProxy selected, PlaceAndRoleModel placeAndRoleModel) {
        SettingsPeopleDetailsList fragment = new SettingsPeopleDetailsList();
        Bundle args = new Bundle(3);
        ArrayList<PersonModelProxy> people = (addresses != null) ? new ArrayList<>(addresses) : new ArrayList<PersonModelProxy>();

        args.putParcelableArrayList(PEOPLE_LIST, people);
        args.putParcelable(PLACE, placeAndRoleModel);
        args.putInt(SELECTED_PERSON, people.indexOf(selected));

        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (savedInstanceState != null) {
            viewing = savedInstanceState.getInt(SELECTED_PERSON, -1);
            peopleAddresses = savedInstanceState.getParcelableArrayList(PEOPLE_LIST);
        }
        else if (args != null) {
            viewing = args.getInt(SELECTED_PERSON, -1);
            peopleAddresses = args.getParcelableArrayList(PEOPLE_LIST);
            placeAndRoleModel = args.getParcelable(PLACE);
        }

        if (peopleAddresses == null) {
            peopleAddresses = new ArrayList<>();
        }

        if (viewing < 0 || viewing > (peopleAddresses.size() - 1)) {
            viewing = 0;
        }
    }

    @Nullable @Override public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState
    ) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            return null;
        }

        multiPage = peopleAddresses.size() > 1;
        pager = (CarouselViewPager) rootView.findViewById(R.id.people_view_pager);
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(new CarouselViewPagerAdapter<PersonModelProxy>(getChildFragmentManager(), peopleAddresses) {
            @Override protected Fragment getFragmentFor(int position) {
                return SettingsPersonFragment.newInstance(getItems().get(position), placeAndRoleModel);
            }
        });

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageScrollStateChanged(int state) {
                if (multiPage) {
                    leftHandle.setVisibility( ViewPager.SCROLL_STATE_IDLE == state ? View.VISIBLE : View.INVISIBLE);
                    rightHandle.setVisibility(ViewPager.SCROLL_STATE_IDLE == state ? View.VISIBLE : View.INVISIBLE);
                }
            }

            @Override public void onPageSelected(int position) {
                Fragment previousFrag = pager.getLastFragment();
                if (previousFrag != null && previousFrag instanceof IClosedFragment) {
                    ((IClosedFragment)previousFrag).onClosedFragment();
                }

                Fragment centerFrag = pager.getCurrentFragment();
                if (centerFrag != null && centerFrag instanceof IShowedFragment) {
                    ((IShowedFragment)centerFrag).onShowedFragment();
                }
            }
        });
        pager.post(new Runnable() {
            @Override public void run() {
                pager.setCurrentItem(viewing);
            }
        });

        leftHandle = rootView.findViewById(R.id.left_handle_image);
        rightHandle = rootView.findViewById(R.id.right_handle_image);
        if (pager.getRealCount() == 1) {
            leftHandle.setVisibility(View.GONE);
            rightHandle.setVisibility(View.GONE);
            Fragment centerFrag = pager.getCurrentFragment();
            if (centerFrag != null && centerFrag instanceof IShowedFragment) {
                ((IShowedFragment)centerFrag).onShowedFragment();
            }
        }
        else {
            leftHandle.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    pager.scrollLeftOne();
                }
            });
            rightHandle.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    pager.scrollRightOne();
                }
            });
        }

        return rootView;

    }

    @Override public void onResume() {
        super.onResume();
        setTitle();
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        if (outState == null) {
            outState = new Bundle();
        }

        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_PERSON, pager == null ? 0 : pager.getCurrentItem());
        outState.putParcelableArrayList(PEOPLE_LIST, new ArrayList<>(peopleAddresses));
    }

    @Override public void onPause() {
        super.onPause();
        viewing = (pager != null) ? pager.getCurrentItem() : 0;
    }

    @Nullable @Override public String getTitle() {
        return placeAndRoleModel != null ? String.valueOf(placeAndRoleModel.getName()).toUpperCase() : getString(R.string.people_people);
    }

    @Override public Integer getLayoutId() {
        return R.layout.people_detail_view_pager;
    }
}
