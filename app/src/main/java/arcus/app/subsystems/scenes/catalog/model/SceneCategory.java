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
package arcus.app.subsystems.scenes.catalog.model;

import com.google.common.base.Strings;
import com.iris.client.capability.SceneTemplate;
import com.iris.client.model.SceneModel;
import com.iris.client.model.SceneTemplateModel;
import arcus.app.R;


public class SceneCategory {
    public static final String NIGHT     =   "SERV:" + SceneTemplate.NAMESPACE + ":night";
    public static final String AWAY      =   "SERV:" + SceneTemplate.NAMESPACE + ":away";
    public static final String VACATION  =   "SERV:" + SceneTemplate.NAMESPACE + ":vacation";
    public static final String HOME      =   "SERV:" + SceneTemplate.NAMESPACE + ":home";
    public static final String MORNING   =   "SERV:" + SceneTemplate.NAMESPACE + ":morning";
    public static final String SCENE_TPL_PREFIX    =   "SERV:" + SceneTemplate.NAMESPACE + ":";

    private final String name;
    private final String description;
    private final String templateAddress;
    private final int iconResId;

    public SceneCategory(String name, String description, String templateAddress) {
        this.name = name;
        this.description = description;
        this.templateAddress = templateAddress;

        this.iconResId = getIconResIdForTemplateName(templateAddress);
    }

    public static SceneCategory fromSceneTemplate(SceneTemplateModel sceneTemplateModel) {
        return new SceneCategory(sceneTemplateModel.getName(), sceneTemplateModel.getDescription(), sceneTemplateModel.getAddress());
    }

    public static SceneCategory fromSceneModel(SceneModel model) {
        return new SceneCategory(model.getName(), null, SCENE_TPL_PREFIX + model.getTemplate());
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTemplateAddress() { return templateAddress; }

    private int getIconResIdForTemplateName(String address) {
        if (Strings.isNullOrEmpty(address)) {
            return R.drawable.scene_icon_custom;
        }

        switch (address) {
            case NIGHT: return R.drawable.scene_icon_good_night;
            case AWAY: return R.drawable.scene_icon_away;
            case VACATION: return R.drawable.scene_icon_vacation;
            case HOME: return R.drawable.scene_icon_home;
            case MORNING: return R.drawable.scene_icon_good_morning;

            default:
                return R.drawable.scene_icon_custom;
        }
    }

}
