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
package arcus.app.pairing.hub.original;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iris.client.capability.Place;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.HubModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.listener.DismissListener;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.Errors;
import arcus.app.common.utils.ImageUtils;
import arcus.app.common.utils.RegisterHubTask;
import arcus.app.common.utils.VideoUtils;
import arcus.app.common.view.NoSwipeViewPager;
import arcus.app.dashboard.HomeFragment;
import arcus.app.pairing.hub.original.adapter.StepsViewPagerAdapter;
import arcus.app.pairing.hub.original.model.ArcusStep;
import arcus.app.pairing.device.productcatalog.ProductCatalogActivity;
import arcus.cornea.provider.HubModelProvider;

import java.util.ArrayList;
import java.util.List;

// FIXME: What to do if we can't name the hub? Silently continue? see {@link #goNext}
public class HubParentFragment extends BaseFragment implements View.OnClickListener, HubPairFragment.Callback, ArcusStepFragment.ButtonCallback {

    private View errorBanner;
    private ImageView errorBannerIcon;
    private TextView errorBannerText;

    private Button nextBtn;
    private Button dashboardBtn;
    private RelativeLayout videoTab;
    private NoSwipeViewPager viewPager;
    private StepsViewPagerAdapter adapter;
    @Nullable
    private View mView;
    private MenuItem xMenuItem;
    private ViewPager mViewPager;
    @Nullable
    private RegisterHubTask registerHubTask;
    private HubPairFragment pairingFragment;
    private boolean showMenu = false;

    @NonNull
    private DismissListener dismissListener = new DismissListener() {
        @Override // No-Op, Intent will handle.
        public void dialogDismissedByReject() {}

        @Override
        public void dialogDismissedByAccept() {
            mView.post(new Runnable() {
                @Override
                public void run() {
                    setItemAndHideProgress(6);
                }
            });
        }
    };

    @NonNull
    public static HubParentFragment newInstance() {
        HubParentFragment fragment = new HubParentFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int position = viewPager.getCurrentItem();

        if (position > 8 || (position == 7 && showMenu)) {
            inflater.inflate(R.menu.menu_close, menu);
            xMenuItem = menu.findItem(R.id.menu_close);
            if (position > 8) {
                getActivity().invalidateOptionsMenu();
            }
        }

        if (position == 7 && showMenu) {
            xMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    cancelHubPairing();
                    BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());

                    return true;
                }
            });
        } else if (position > 8) {
            xMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    cancelHubPairing();
                    BackstackManager.getInstance().navigateBackToFragment(HomeFragment.class);

                    return true;
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mView = view;

        errorBanner = view.findViewById(R.id.error_banner);
        errorBannerIcon = (ImageView) view.findViewById(R.id.error_banner_icon) ;
        errorBannerText = view.findViewById(R.id.error_banner_text);

        nextBtn = view.findViewById(R.id.fragment_hub_parent_next_btn);
        dashboardBtn = view.findViewById(R.id.fragment_hub_parent_dashboard_btn);
        dashboardBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);

        videoTab = (RelativeLayout) view.findViewById(R.id.video_tab);
        videoTab.setBackgroundColor(getResources().getColor(R.color.arcus_pale_grey));
        videoTab.setMinimumHeight(ImageUtils.dpToPx(50));
        videoTab.setOnClickListener(this);

        viewPager = (NoSwipeViewPager) view.findViewById(R.id.fragment_hub_parent_viewpager);

        viewPager.setOffscreenPageLimit(1);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {

                updatePageUI(position);
            }
        });

        populate();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        hideProgressBar();
        cancelHubPairing();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (registerHubTask != null) {
            registerHubTask.registerHub();
            hideProgressBar();
            showProgressBar();
        }

        if(getActivity() != null) {
            ((BaseActivity) getActivity()).setKeepScreenOn(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        hideProgressBar();

        if(getActivity() != null) {
            ((BaseActivity) getActivity()).setKeepScreenOn(false);
        }
    }

    private void populate() {
        adapter = new StepsViewPagerAdapter(getActivity(), getChildFragmentManager(), getFragments());
        viewPager.setAdapter(adapter);

        mView.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(0, false);
                updatePageUI(0);
            }
        });
    }

    @Override
    public String getTitle() {
        int position = viewPager.getCurrentItem();
        final BaseFragment fragment = (BaseFragment) adapter.getFragment(position);
        return fragment.getTitle();
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_hub_parent;
    }

    @NonNull
    private List<Fragment> getFragments() {
        pairingFragment = new HubPairFragment();
        pairingFragment.setCallback(this);
        List<Fragment> fList = new ArrayList<>();

        fList.add(ArcusStepFragment.newInstance(new ArcusStep(R.drawable.hubpairing_illustration_step1, 1, getString(R.string.connect_the_hub_title), getString(R.string.hub_step_insert_batteries), ""), false, true));
        fList.add(ArcusStepFragment.newInstance(new ArcusStep(R.drawable.hubpairing_illustration_step2, 2, getString(R.string.connect_the_hub_title), getString(R.string.hub_step_plug_ethernet), ""), false, true));
        fList.add(ArcusStepFragment.newInstance(new ArcusStep(R.drawable.hubpairing_illustration_step3, 3, getString(R.string.connect_the_hub_title), getString(R.string.hub_step_plug_other_end), ""), false, true));
        fList.add(ArcusStepFragment.newInstance(new ArcusStep(R.drawable.hubpairing_illustration_step4, 4, getString(R.string.connect_the_hub_title), getString(R.string.hub_step_insert_supplied_power), ""), false, true));
        fList.add(ArcusStepFragment.newInstance(new ArcusStep(R.drawable.hubpairing_illustration_step5, 5, getString(R.string.connect_the_hub_title), getString(R.string.hub_step_plug_into_outlet), ""), false, true));
        fList.add(ArcusStepFragment.newInstance(new ArcusStep(R.drawable.hubpairing_illustration_step6, 6, getString(R.string.connect_the_hub_title), getString(R.string.hub_step_enter_hub_id), ""), true, true));
        fList.add(pairingFragment);
        fList.add(ArcusStepFragment.newInstance(new ArcusStep(R.drawable.device_list_placeholder, 7, getString(R.string.hub_title), getString(R.string.name_your_hub), ""), true, true));
        fList.add(HubSuccessFragment.newInstance());
        return fList;
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.video_tab:
                VideoUtils.launchV2HubTutorial();
                break;
            case R.id.fragment_hub_parent_next_btn:
                goNext();
                break;
            case R.id.fragment_hub_parent_dashboard_btn:
                // go to dashboard
                BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
                break;
        }
    }

    private void goNext() {
        final BaseFragment currentFragment = (BaseFragment) adapter.getFragment(viewPager.getCurrentItem());
        if (currentFragment.validate() && currentFragment.submit()) {
            // The "Enter hub ID" starts with a disabled button
            if (viewPager.getCurrentItem() == 4) {
                nextBtn.setEnabled(false);
            }
            if (viewPager.getCurrentItem() == 7) {
                EditText editText = mView.findViewById(R.id.step_view_device_name);
                if (editText != null) {
                    showProgressBar();
                    nextBtn.setEnabled(false);
                    editText.setEnabled(false);

                    HubModel hubModel = HubModelProvider.instance().getHubModel();
                    if (hubModel != null) {
                        hubModel.setName(editText.getText().toString());
                        hubModel.commit().onCompletion(result -> {
                            if (result.isError()) {
                                logger.debug("Unable to name hub, Reason:", result.getError());
                            }

                            mView.post(() -> {
                                hideProgressBar();
                                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                            });
                        });
                    }
                }
            } else if (currentFragment instanceof HubSuccessFragment) {
                Context context = getContext();
                if (context != null) {
                    startActivity(ProductCatalogActivity.createIntentClearTop(context));
                }
            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        // Don't allow back button on the "Name your hub fragment".
        if (viewPager.getCurrentItem() == 7) {
            return true;
        }
        // If we tap back from the hub pairing screen (w/progressbar), enable the next button, hide errors
        else if (viewPager.getCurrentItem() == 6) {
            pairingFragment.cancelTimers();
            cancelHubPairing();
            errorBannerVisible(false);
            nextBtn.setVisibility(View.VISIBLE);
            videoTab.setVisibility(View.VISIBLE);
        }
        else if (viewPager.getCurrentItem() == 5) {
            nextBtn.setEnabled(true);
        }

        //If not on name your hub, allow back button
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
            return true;
        }

        return super.onBackPressed();
    }

    private void updatePageUI(int position) {
        final BaseFragment fragment = (BaseFragment) adapter.getFragment(position);

        if (fragment != null) {
            getActivity().setTitle(getString(R.string.connect_the_hub_title));
            getActivity().invalidateOptionsMenu();

            if (fragment instanceof ArcusStepFragment) {
                ArcusStepFragment arcusStepFragment = (ArcusStepFragment) adapter.getFragment(position);
                ArcusStep step = arcusStepFragment.getStep();

                /// set button callback to this, where this is the fragment (hub pairing fragment)
                arcusStepFragment.setCallback(this);
                if(step == null) {
                    arcusStepFragment.updateIndexIcon(position - 1);
                } else {
                    arcusStepFragment.updateIndexIcon(step.getCurrentStep() - 1);
                }
            }

            if (position == 5) {
                nextBtn.setEnabled(false);
            } else if (position == 6) {
                videoTab.setVisibility(View.GONE);
                nextBtn.setVisibility(View.GONE);
                pairHub();
            } else {
                hideProgressBar();
                cancelHubPairing();
                nextBtn.setVisibility(View.VISIBLE);
                dashboardBtn.setVisibility(View.GONE);
                videoTab.setVisibility(View.VISIBLE);

                if (position > 6) {
                    videoTab.setVisibility(View.GONE);
                }
                if (position == 8) {
                    nextBtn.setText(getResources().getString(R.string.hub_pairing_pair_a_device));
                    nextBtn.setEnabled(true);
                    dashboardBtn.setText(R.string.hub_pairing_go_to_dashboard);
                    dashboardBtn.setVisibility(View.VISIBLE);
                }
            }
            if (fragment instanceof IShowedFragment) {
                ((IShowedFragment) fragment).onShowedFragment();
            }
            getActivity().setTitle(getTitle());
        }
    }

    public void pairHub() {
        hideProgressBar();
        errorBannerVisible(false);
        pairingFragment.startPairing();
        if (registerHubTask == null) {
            registerHubTask = new RegisterHubTask() {
                @Override
                public void onEvent(@NonNull final Place.RegisterHubV2Response response) {
                    onEventReceived(response);
                }

                @Override
                public void onError(@NonNull final Throwable throwable) {
                    onErrorReceived(throwable);
                }
            };
        }
        registerHubTask.registerHub();
    }

    private ViewPager getViewPager() {
        if (mViewPager == null) {
            mViewPager = (ViewPager) getActivity().findViewById(R.id.fragment_hub_parent_viewpager);
        }

        return mViewPager;
    }

    private void setItemAndHideProgress(int which) {
        hideProgressBar();
        getViewPager().setCurrentItem(which);
    }

    private void cancelHubPairing() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgressBar();
            }
        });

        if (registerHubTask != null) {
            registerHubTask.cancel();
            registerHubTask = null;
        }
    }

    public void onEventReceived(@NonNull final Place.RegisterHubV2Response response) {
        HubModel hubModel = SessionModelManager.instance().getHubModel();

        if((response.getState().equals(Place.RegisterHubV2Response.STATE_REGISTERED) || response.getState().equals(Place.RegisterHubV2Response.STATE_ONLINE)) && hubModel != null) {
            cancelHubPairing();
            if(mView == null) {
                return;
            }
            mView.post(new Runnable() {
                @Override
                public void run() {
                    pairingFragment.animateRegistered();
                }
            });
        } else {
            String state = response.getState();
            if(response.getState().equals(Place.RegisterHubV2Response.STATE_REGISTERED)) {
                state = Place.RegisterHubV2Response.STATE_OFFLINE;
            }
            if(Place.RegisterHubV2Response.STATE_OFFLINE.equals(state)) {
                showMenu = false;
            } else {
                showMenu = true;
            }
            int progress = 0;
            if(response.getProgress() != null) {
                progress = response.getProgress();
            }
            pairingFragment.updateState(response.getState(), progress);
            if(pairingFragment.hasInstallTimedOut()) {
                cancelHubPairing();
                pairingFragment.updateErrorState(Errors.Hub.INSTALL_TIMEDOUT);
            } else if(pairingFragment.hasDownloadTimedOut()) {
                cancelHubPairing();
                pairingFragment.updateErrorState(Errors.Hub.FWUPGRADE_FAILED);
            }
            getActivity().invalidateOptionsMenu();
        }
    }

    public void onErrorReceived(@NonNull final Throwable throwable) {
        errorBannerVisible(true);
        if(throwable instanceof RuntimeException) {
            cancelHubPairing();
            if(throwable.getMessage().equals(Errors.Process.CANCELLED)) {
                BackstackManager.getInstance().navigateBack();
            }
        } else {
            ErrorResponseException error = (ErrorResponseException) throwable;
            cancelHubPairing();
            // Don't show error banners on the "Enter Hub ID" screen
            if (viewPager.getCurrentItem() == 5) {
                errorBannerVisible(false);
                return;
            }
            pairingFragment.updateErrorState(error.getCode());
        }
    }

    @Override
    public void restartPairing(String hubId) {
        cancelHubPairing();
        ArcusApplication.getRegistrationContext().setHubID(hubId);
        pairHub();
    }

    @Override
    public void cancelPairing() {
        cancelHubPairing();
    }

    @Override
    public void navigateToNextStep() {
        setItemAndHideProgress(getViewPager().getCurrentItem() + 1);
    }

    @Override
    public void updateCloseButton(boolean show) {
        showMenu = show;
        getActivity().invalidateOptionsMenu();
    }

    public void errorBannerVisible(boolean visible) {
        errorBanner.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setHubErrorBanner(int bannerColor, int bannerText, @Nullable String hubID, @Nullable String errorCode) {

        BlackWhiteInvertTransformation btw = new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE);
        BlackWhiteInvertTransformation wtb = new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK);
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.icon_alert_noconnection_outline);

        if(bannerColor == R.color.sclera_warning){
            errorBannerText.setTextColor(getResources().getColor(R.color.sclera_text_color_dark));
            errorBannerIcon.setImageBitmap(wtb.transform(bitmap));
        } else {
            errorBannerText.setTextColor(getResources().getColor(R.color.white));
            errorBannerIcon.setImageBitmap(btw.transform(bitmap));
        }

        if(errorCode != null & hubID == null) {
            throw new IllegalStateException("If there's an Error Code, please pass in the Hub ID. Or, show a generic error banner by omitting both Error Code and Hub ID");
        }
        if(hubID == null & errorCode == null)  {
            genericErrorBanner(bannerColor, bannerText);
        }
        if(errorCode != null) {
            specificErrorBanner(bannerColor, bannerText, hubID, errorCode);
        }

        errorBanner.setBackgroundColor(getResources().getColor(bannerColor));
        errorBannerText.setText(String.format(getString(bannerText), hubID));
    }

    private void specificErrorBanner(int bannerColor, int bannerText, String hubID,  String errorCode){
        errorBanner.setBackgroundColor(getResources().getColor(bannerColor));
        errorBannerText.setText(String.format(getString(bannerText), hubID, errorCode));
    }

    private void genericErrorBanner(int bannerColor, int bannerText){
        errorBanner.setBackgroundColor(getResources().getColor(bannerColor));
        errorBannerText.setText(String.format(getString(bannerText)));
    }

    @Override
    public void enableButton() {
        nextBtn.setEnabled(true);
    }

    @Override
    public void disableButton() {
        nextBtn.setEnabled(false);
    }

    @Override
    public void searchForHub() {
        goNext();
    }
}
