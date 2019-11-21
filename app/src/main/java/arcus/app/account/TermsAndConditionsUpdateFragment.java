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
package arcus.app.account;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import arcus.cornea.account.TermsAndConditionsContract;
import arcus.cornea.account.TermsAndConditionsPresenter;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.ViewUtils;
import arcus.app.dashboard.HomeFragment;

public class TermsAndConditionsUpdateFragment extends BaseFragment implements TermsAndConditionsContract.View {
    TermsAndConditionsContract.Presenter presenter;
    Button acceptButton;

    public static TermsAndConditionsUpdateFragment newInstance() {
        return new TermsAndConditionsUpdateFragment();
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view == null) {
            getActivity().finish();
            return;
        }

        String privacyString = getLinkString(GlobalSetting.PRIVACY_LINK, getString(R.string.privacy_statement));
        String termsString = getLinkString(GlobalSetting.T_AND_C_LINK, getString(R.string.terms_of_service));
        Spannable madeChangesCopy = (Spannable) Html.fromHtml(getString(R.string.made_some_changes_with_placeholders, termsString, privacyString));
        Spannable clickAcceptCopy = (Spannable) Html.fromHtml(getString(R.string.clicking_accept_agree, termsString, privacyString));
        ViewUtils.removeUnderlines(madeChangesCopy, clickAcceptCopy);

        TextView madeChanges = (TextView) view.findViewById(R.id.made_changes_copy);
        TextView clickAccept = (TextView) view.findViewById(R.id.click_accept_copy);

        madeChanges.setText(madeChangesCopy);
        madeChanges.setMovementMethod(LinkMovementMethod.getInstance());
        clickAccept.setText(clickAcceptCopy);
        clickAccept.setMovementMethod(LinkMovementMethod.getInstance());

        acceptButton = (Button) view.findViewById(R.id.terms_accept_button);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (presenter != null) {
                    showProgressBarAndDisable(acceptButton);
                    presenter.acceptTermsAndConditions();
                }
            }
        });
    }

    @Override public void onResume() {
        super.onResume();
        hideActionBar();
        if (presenter == null) {
            presenter = new TermsAndConditionsPresenter(this);
        }

        // If user clicks accept but then gets a call, when we resume we want to check to see if we still need to accept
        presenter.recheckNeedToAccept();
    }

    @Override public void onPause() {
        super.onPause();
        if (presenter != null) {
            presenter.clearReferences();
        }

        presenter = null;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        hideProgressBar();
        showActionBar();
    }

    @Nullable @Override public String getTitle() {
        return null;
    }

    @Override public Integer getLayoutId() {
        return R.layout.terms_and_conditions;
    }

    @Override public boolean onBackPressed() {
        return true; // Consume this event.
    }

    @Override public void acceptRequired() {
        hideProgressBar();
        if (acceptButton != null) {
            acceptButton.setEnabled(true);
        }
    }

    @Override public void onAcceptSuccess() {
        exitFragment();
    }

    @Override public void onError(Throwable throwable) {
        hideProgressBarAndEnable(acceptButton);
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    void exitFragment() {
        BackstackManager.getInstance().rewindToFragment(HomeFragment.newInstance());
    }

    protected void setText(@Nullable Spannable text, @NonNull TextView... views) {
        for (TextView view : views) {
            view.setText(text);
        }
    }

    protected String getLinkString(String httpUrl, String displayText) {
        return "<a href=\"" + httpUrl + "\">" + displayText + "</a>";
    }
}
