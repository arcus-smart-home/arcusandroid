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
package arcus.app.common.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Saves a user-generated image to the {@link ImageRepository} within an {@link AsyncTask}.
 */
public class ImageSaveBuilder {

    private final Context context;
    private final ImageCategory category;
    private final Bitmap image;
    private final String imageId;
    private final String placeId;

    private final List<ImageSaveListener> listeners = new ArrayList<>();

    public ImageSaveBuilder (Context context, ImageCategory category, Bitmap image, String placeId, String imageId) {
        this.context = context;
        this.category = category;
        this.image = image;
        this.imageId = imageId;
        this.placeId = placeId;
    }

    @NonNull
    public ImageSaveBuilder withCallback (@Nullable ImageSaveListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }

        return this;
    }

    public void commit () {

        AsyncTask<ImageSaveBuilder, Void, Void> task = new AsyncTask<ImageSaveBuilder, Void, Void> () {

            @Nullable
            @Override
            protected Void doInBackground(@NonNull ImageSaveBuilder... params) {

                for (ImageSaveBuilder builder : params) {
                    Uri fileUri = ImageRepository.saveImage(builder.context, builder.image, builder.category, builder.placeId, builder.imageId);
                    for (ImageSaveListener thisListener : builder.listeners) {
                        thisListener.onImageSaveComplete(true, builder.image, fileUri);
                    }
                }

                return null;
            }
        };

        task.execute(this);
    }

}
