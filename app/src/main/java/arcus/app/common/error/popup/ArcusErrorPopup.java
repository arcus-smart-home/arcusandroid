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
package arcus.app.common.error.popup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.error.base.Error;
import arcus.app.common.error.base.RejectableError;
import arcus.app.common.error.base.StyleablePopup;
import arcus.app.common.error.listener.DismissListener;
import arcus.app.common.utils.GlobalSetting;

public class ArcusErrorPopup extends Fragment implements View.OnClickListener {
    protected ImageView closeBtn;
    protected TextView title;
    protected TextView text;
    protected RelativeLayout supportLink;

    private static final String ERROR_ID = "ERROR";
    private static final String DISMISS_ID = "DISMISS_ID";
    private DismissListener dismissListener;

    @NonNull
    public static ArcusErrorPopup newInstance(Error error, DismissListener dismissListener) {
        ArcusErrorPopup popup = new ArcusErrorPopup();

        Bundle bundle = new Bundle(2);
        bundle.putSerializable(ERROR_ID, error);
        bundle.putSerializable(DISMISS_ID, dismissListener);
        popup.setArguments(bundle);

        return popup;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_arcus_pop_up_error, container, false);

        closeBtn = (ImageView) view.findViewById(R.id.accept_close_button);
        Button acceptContainer = (Button) view.findViewById(R.id.accept_button);
        Button rejectContainer = (Button) view.findViewById(R.id.reject_button);

        closeBtn.setOnClickListener(this);
        acceptContainer.setOnClickListener(this);
        rejectContainer.setOnClickListener(this);
        text = (TextView) view.findViewById(R.id.error_text);
        title = (TextView) view.findViewById(R.id.error_title);
        supportLink = (RelativeLayout) view.findViewById(R.id.call_support_button);

        if (getArguments() != null && getArguments().getSerializable(ERROR_ID) != null) {
            Error error = (Error) getArguments().getSerializable(ERROR_ID);
            title.setText(error.getTitle(getActivity().getResources()));
            text.setText(error.getText(getActivity().getResources()));
            dismissListener = (DismissListener) getArguments().getSerializable(DISMISS_ID);

            if (error instanceof StyleablePopup) {
                int backgroundColor = ((StyleablePopup) error).getBackgroundColor();

                view.findViewById(R.id.fragment_popup_content_container).setBackgroundColor(backgroundColor);
                text.setTextColor(((StyleablePopup) error).getTextColor());
                title.setTextColor(((StyleablePopup) error).getTextColor());

                if (backgroundColor == Color.WHITE) {
                    closeBtn.setImageResource(R.drawable.button_close_black_x);
                }

                if (((StyleablePopup) error).isSupportLinkVisible()) {
                    supportLink.setVisibility(View.VISIBLE);
                    supportLink.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callSupportIntent = new Intent(Intent.ACTION_DIAL, GlobalSetting.SUPPORT_NUMBER_URI);
                            getActivity().startActivity(callSupportIntent);
                        }
                    });
                }
            }

            if (!(error instanceof RejectableError)) {
                view.findViewById(R.id.error_cta_buttons_container).setVisibility(View.GONE);
            }
        }
        else {
            title.setText(R.string.error_generic_title);
            text.setText(R.string.error_generic_text);
        }

        return view;
    }

    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.accept_button:
            case R.id.accept_close_button:
                if (dismissListener != null) {
                        dismissListener.dialogDismissedByAccept();
                }
                getFragmentManager().findFragmentById(R.id.floating).getFragmentManager().popBackStackImmediate();
                break;
            case R.id.reject_button:
                if (dismissListener != null) {
                    dismissListener.dialogDismissedByReject();
                }
                getFragmentManager().findFragmentById(R.id.floating).getFragmentManager().popBackStackImmediate();
                break;
        }
    }
}
