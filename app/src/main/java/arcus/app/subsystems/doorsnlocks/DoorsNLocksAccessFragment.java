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
package arcus.app.subsystems.doorsnlocks;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.subsystem.doorsnlocks.DoorsNLocksAccessController;
import arcus.cornea.subsystem.doorsnlocks.model.AccessSummary;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.LeftTextCard;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.view.Version1TextView;

import java.util.List;


public class DoorsNLocksAccessFragment extends BaseFragment implements DoorsNLocksAccessController.Callback {

    private DoorsNLocksAccessController mController;
    private ListenerRegistration mListener;

    private MaterialListView mListView;
    private LinearLayout mAccessView;
    private Version1TextView mShopButton;

    private List<AccessSummary> mAccessSummary;

    @NonNull
    public static DoorsNLocksAccessFragment newInstance() {
        DoorsNLocksAccessFragment fragment = new DoorsNLocksAccessFragment();

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mListView = (MaterialListView) view.findViewById(R.id.material_listview);
        mAccessView = (LinearLayout) view.findViewById(R.id.access_view);
        mShopButton = (Version1TextView) view.findViewById(R.id.doors_n_locks_no_device_shop_btn);

        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(CardItemView view, int position) {
                final AccessSummary accessSummary = mAccessSummary.get(position);
                BackstackManager.getInstance().navigateToFragment(DoorsNLocksAccessListFragment.newInstance(accessSummary),true);
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {

            }
        });

        mShopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.launchShopNow();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mController == null) {
            mController = DoorsNLocksAccessController.instance();
        }

        mListener = mController.setCallback(this);

    }

    @Override
    public void onPause() {
        super.onPause();

        mListener.remove();
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_doors_and_locks_access;
    }

    private void populateCards() {
        mListView.clear();

        if (mAccessSummary == null) return;

        for (AccessSummary summary : mAccessSummary) {
            LeftTextCard card = new LeftTextCard(getActivity());
            card.setTitle(summary.getName());
            card.setDescription(summary.getAccessCount() + " People Have Access");
            card.setDrawableResource(R.drawable.icon_cat_doors_locks);
            card.setDrawableInverted(true);
            card.showDivider();
            card.showChevron();
            mListView.add(card);
        }

    }

    /***
     * Access Callback
     */

    @Override
    public void showNoLocksCopy() {
        // Hide ListView
        mListView.setVisibility(View.GONE);

        // Show Copy View
        mAccessView.setVisibility(View.VISIBLE);

        mShopButton.setVisibility(View.VISIBLE);



    }

    @Override
    public void showAccessSummary(List<AccessSummary> accessSummary) {
        // Hide Copy View
        mAccessView.setVisibility(View.GONE);

        mShopButton.setVisibility(View.GONE);

        // Show ListView
        mListView.setVisibility(View.VISIBLE);

        // Populate ListView
        mAccessSummary = accessSummary;
        populateCards();
    }

    @Override
    public void updateAccessSummary(AccessSummary accessSummary) {
        // Populate ListView

    }
}
