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
package arcus.app.common.popups;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.backstack.ScleraTransitionManager;
import arcus.app.common.fragments.ScleraSheet;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.ScleraTextView;

public class UseFingerPrintPopup extends ScleraSheet {

    private ScleraTextView fingerprintTitle;
    private ScleraTextView fingerprintSubtitle;
    private Button useTouchId;
    private Button notNow;
    private ImageView fingerprintSuccessImg;
    private View contents;

    public static UseFingerPrintPopup newInstance() { return new UseFingerPrintPopup(); }

    @Override
    public int getSheetLayoutId() {
        return R.layout.fragment_use_finger_print;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if(view != null) {
            fingerprintTitle = (ScleraTextView) view.findViewById(R.id.fingerprint_title);
            fingerprintSubtitle = (ScleraTextView) view.findViewById(R.id.fingerprint_subtitle);
            useTouchId = view.findViewById(R.id.use_touch_id);
            notNow = view.findViewById(R.id.not_now);
            fingerprintSuccessImg = (ImageView) view.findViewById(R.id.fingerprint_success);
            contents = view.findViewById(R.id.fingerprint_buttons);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setEmptyTitle();
        hideActionBar();

        useTouchId.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                contents.setVisibility(View.GONE);
                fingerprintSubtitle.setVisibility(View.GONE);
                fingerprintSuccessImg.setVisibility(View.VISIBLE);

                PreferenceUtils.setUseFingerPrint(true);
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce_anim);animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ScleraTransitionManager.dismissSheet();
                            }
                        }, 1000);
                    }
                });

                fingerprintTitle.setText(getString(R.string.fingerprint_login_confirmed));
                fingerprintSuccessImg.startAnimation(animation);
            }
        });
        notNow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PreferenceUtils.setUseFingerPrint(false);
                ScleraTransitionManager.dismissSheet();
            }
        });
    }

    @Override
    public void onPause() {
        showActionBar();
        super.onPause();
    }
}
