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
package arcus.app.common.utils;

import android.net.Uri;
import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import com.iris.client.service.VideoService;

import static arcus.app.common.utils.ActivityUtils.launchUrl;

public class VideoUtils {
    public static void launchV2HubTutorial(){
        launchUrl(Uri.parse(GlobalSetting.V2_HUB_TUTORIAL));
    }

    public static void launchV3HubTutorial() {
        //todo: analytics?
        launchUrl(Uri.parse(GlobalSetting.V3_HUB_TUTORIAL));
    }

    public static void sendStopStreaming(@Nullable String recordingID) {
        if (recordingID == null) {
            return;
        }

        try {
            CorneaClientFactory
                    .getService(VideoService.class)
                    .stopRecording(SessionController.instance().getActivePlace(), recordingID);
        } catch (Exception ex) {
            // Log
        }
    }
}
