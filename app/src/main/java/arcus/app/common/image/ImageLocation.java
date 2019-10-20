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

import android.support.annotation.Nullable;

/**
 * Specifies the location of an image, either as a String (uri), integer (drawable resource), file
 * handle or URI.
 */
public class ImageLocation implements ImageLocationSpec {

    @Nullable
    private final Object location;
    private final boolean isUserGenerated;

    public ImageLocation (@Nullable Object location) {
        this.location = location;
        this.isUserGenerated = false;
    }

    public ImageLocation (@Nullable Object location, boolean isUserGenerated) {
        this.location = location;
        this.isUserGenerated = isUserGenerated;
    }

    @Nullable
    public Object getLocation () {
        return location;
    }

    public boolean isUserGenerated () { return isUserGenerated; }

    @Override
    public String toString() {
        return "ImageLocation{" +
                "location=" + location +
                ", isUserGenerated=" + isUserGenerated +
                '}';
    }
}
