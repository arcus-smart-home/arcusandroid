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

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Represents an observer of image save events; used to notify a listener that a user-generated
 * image has been saved to the {@link ImageRepository}.
 */
public interface ImageSaveListener {

    /**
     * Called to indicate an image has been saved. Fires after the save process has completed and
     * the same image can be retrieved from the repository.
     *
     * @param success True if the image saved successfully, false if an error occured
     * @param image The bitmap of the saved image
     * @param savedFileUri The URI referencing the location of the saved image.
     */
    void onImageSaveComplete (boolean success, Bitmap image, Uri savedFileUri);
}
