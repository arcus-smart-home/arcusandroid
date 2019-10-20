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
package arcus.app.device.pairing.post.controller;

import android.app.Activity;

import com.google.common.collect.ImmutableSet;
import arcus.cornea.provider.DeviceModelProvider;
import com.iris.client.capability.Capability;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.GlobalSetting;


public class AddToFavoritesFragmentController extends FragmentController<AddToFavoritesFragmentController.Callbacks> {

    public interface Callbacks {
        void onSuccess();
        void onFailure(Throwable cause);
    }

    private Activity activity;

    private static final AddToFavoritesFragmentController instance = new AddToFavoritesFragmentController();
    private AddToFavoritesFragmentController () {}
    public static AddToFavoritesFragmentController getInstance () { return instance; }

    public void setIsFavorite (Activity activity, String deviceAddress, final boolean isFavorite) {
        this.activity = activity;

        // Load the device model
        DeviceModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {

                // ... and add or remove the favorite tag
                if (isFavorite) {
                    addFavoriteTag(deviceModel);
                } else {
                    removeFavoriteTag(deviceModel);
                }

            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnFailure(throwable);
            }
        });
    }

    private void addFavoriteTag (DeviceModel deviceModel) {
        deviceModel.addTags(ImmutableSet.of(GlobalSetting.FAVORITE_TAG)).onSuccess(new Listener<Capability.AddTagsResponse>() {
            @Override
            public void onEvent(Capability.AddTagsResponse addTagsResponse) {
                fireOnSuccess();
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnFailure(throwable);
            }
        });
    }

    private void removeFavoriteTag (DeviceModel deviceModel) {
        deviceModel.removeTags(ImmutableSet.of(GlobalSetting.FAVORITE_TAG)).onSuccess(new Listener<Capability.RemoveTagsResponse>() {
            @Override
            public void onEvent(Capability.RemoveTagsResponse removeTagsResponse) {
                fireOnSuccess();
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnFailure(throwable);
            }
        });
    }

    private void fireOnSuccess() {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onSuccess();
                    }
                }
            });
        }
    }

    private void fireOnFailure(final Throwable throwable) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onFailure(throwable);
                    }
                }
            });
        }
    }

}
