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
package arcus.app.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import arcus.app.ArcusApplication;
import arcus.cornea.CorneaClientFactory;
import arcus.app.R;
import arcus.app.common.image.IntentRequestCode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class FullscreenFragmentActivity extends FragmentActivity {

    private static final String FRAGMENT_CLASS = "FRAGMENT_CLASS";
    private static final String FRAGMENT_ORIENTATION = "FRAGMENT_ORIENTATION";
    private static final String FRAGMENT_ARGUMENTS = "FRAGMENT_ARGUMENTS";

    @IntDef({ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FragmentOrientation {}

    public static void launch(@NonNull Activity context, Class<? extends Fragment> clazz, ActivityResultListener listener) {
        launch(context, clazz, null, null, listener);
    }


    public static void launch(@NonNull Activity context, Class<? extends Fragment> clazz) {
        launch(context, clazz, null, null, null);
    }

    public static void launch(
            @NonNull Activity context, Class<? extends Fragment> clazz,
            @FragmentOrientation int orientation,
            @Nullable Bundle fragmentArguments) {

        launch(context, clazz, orientation, fragmentArguments, null);
    }

    public static void launch(
            @NonNull Activity context,
            @NonNull Class<? extends Fragment> clazz,
            @Nullable @FragmentOrientation Integer orientation,
            @Nullable Bundle fragmentArguments,
            @Nullable ActivityResultListener listener)
    {
        if (context != null) {
            Intent intent = new Intent(context, FullscreenFragmentActivity.class);

            intent.putExtra(FRAGMENT_CLASS, clazz);

            if (orientation != null) {
                intent.putExtra(FRAGMENT_ORIENTATION, orientation);
            }

            if (fragmentArguments != null) {
                intent.putExtra(FRAGMENT_ARGUMENTS, fragmentArguments);
            }

            if (listener != null && context instanceof BaseActivity) {
                ((BaseActivity) context).addActivityResultListener(listener);
            }

            context.startActivityForResult(intent, IntentRequestCode.FULLSCREEN_FRAGMENT.requestCode);
        }
    }

    public static void launchWithoutResult(
            @NonNull Class<? extends Fragment> clazz,
            @Nullable @FragmentOrientation Integer orientation,
            @Nullable Bundle fragmentArguments
    ) {
        Context context = ArcusApplication.getContext();

        Intent intent = new Intent();
        intent.setClassName(
                context.getPackageName(),
                FullscreenFragmentActivity.class.getName()
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(FRAGMENT_CLASS, clazz);

        if (orientation != null) {
            intent.putExtra(FRAGMENT_ORIENTATION, orientation);
        }

        if (fragmentArguments != null) {
            intent.putExtra(FRAGMENT_ARGUMENTS, fragmentArguments);
        }

        context.startActivity(intent);
    }


    @Override
    protected void onResume() {
        if (!CorneaClientFactory.isConnected()) {
            Intent intent = new Intent(this, LaunchActivity.class);
            startActivityForResult(intent, IntentRequestCode.FULLSCREEN_FRAGMENT.requestCode);
            finish();
        }

        super.onResume();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_error);
        Integer orientation = getIntent().getIntExtra(FRAGMENT_ORIENTATION, -1);
        if (orientation != -1) {
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        try {
            // Get an instance of the requested fragment...
            Class<? extends Fragment> fragmentClass = (Class) getIntent().getSerializableExtra(FRAGMENT_CLASS);
            Fragment fragmentInstance = fragmentClass.newInstance();
            Bundle arguments = getIntent().getBundleExtra(FRAGMENT_ARGUMENTS);
            if (arguments != null) {
                fragmentInstance.setArguments(arguments);
            }

            // ... and insert it into the activity
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragmentInstance).commit();
        }

        // If anything goes wrong, return the user to the previous activity
        catch (Exception e) {
            finish();
        }
    }
}
