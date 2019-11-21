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
package arcus.cornea.common;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ ViewRenderType.NONE, ViewRenderType.HEADER_VIEW, ViewRenderType.PLACE_VIEW, ViewRenderType.PERSON_VIEW, ViewRenderType.CLIP_VIEW, ViewRenderType.CLIP_DEVICE_FILTER, ViewRenderType.CLIP_PINNED_FILTER})
@Retention(RetentionPolicy.SOURCE)
public @interface ViewRenderType {
    int NONE = 0, HEADER_VIEW = 0x0A, PLACE_VIEW = 0x0B, PERSON_VIEW = 0x0C, CLIP_VIEW = 0x0D, CLIP_DEVICE_FILTER = 0x0E, CLIP_PINNED_FILTER = 0x0F;
}
