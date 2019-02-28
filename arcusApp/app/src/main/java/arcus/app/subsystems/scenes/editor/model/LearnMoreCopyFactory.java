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
package arcus.app.subsystems.scenes.editor.model;

import android.content.Context;

import com.google.common.base.Strings;
import com.iris.client.capability.SceneTemplate;
import arcus.app.ArcusApplication;
import arcus.app.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LearnMoreCopyFactory {

    private static final Logger logger = LoggerFactory.getLogger(LearnMoreCopyFactory.class);

    public static final String NIGHT     =   "SERV:" + SceneTemplate.NAMESPACE + ":night";
    public static final String AWAY      =   "SERV:" + SceneTemplate.NAMESPACE + ":away";
    public static final String VACATION  =   "SERV:" + SceneTemplate.NAMESPACE + ":vacation";
    public static final String HOME      =   "SERV:" + SceneTemplate.NAMESPACE + ":home";
    public static final String MORNING   =   "SERV:" + SceneTemplate.NAMESPACE + ":morning";
    public static final String CUSTOM    =   "SERV:" + SceneTemplate.NAMESPACE + ":custom";

    public static String createLearnMoreCopy(String address, String defaultText) {
        if (Strings.isNullOrEmpty(address)) {
            return defaultText;
        }

        Context context = ArcusApplication.getArcusApplication().getApplicationContext();
        // TODO: Should probably use ID rather than address here
        switch (address) {
            case NIGHT: return context.getString(R.string.scenes_learnmore_goodnight);
            case AWAY: return context.getString(R.string.scenes_learnmore_away);
            case VACATION: return context.getString(R.string.scenes_learnmore_vacation);
            case HOME: return context.getString(R.string.scenes_learnmore_welcomehome);
            case MORNING: return context.getString(R.string.scenes_learnmore_goodmorning);
            case CUSTOM: return context.getString(R.string.scenes_custom_desc);

            // TODO: Not all templates have "learn more" copy in InVision; falling back to description for the time being
            default:
                logger.warn("Using default text, No COPY for [{}]", address);
                return defaultText;
        }
    }

}
