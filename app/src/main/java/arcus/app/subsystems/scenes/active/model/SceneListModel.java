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
package arcus.app.subsystems.scenes.active.model;

import androidx.annotation.DrawableRes;

import com.google.common.base.Strings;
import com.iris.client.bean.Action;
import com.iris.client.model.SceneModel;
import arcus.app.R;

import java.util.Map;

public class SceneListModel {
    public static final String NIGHT     =   "night";
    public static final String AWAY      =   "away";
    public static final String VACATION  =   "vacation";
    public static final String HOME      =   "home";
    public static final String MORNING   =   "morning";

    private boolean isEnabled;
    private boolean hasSchedule;
    private String templateCreatedWith;
    private String modelAddress;
    private @DrawableRes int sceneIconResourceID;
    private int actionCount;
    private String nameOfScene;

    private int getIconResIdForTemplateName (String id) {
        switch (id) {
            case NIGHT: return R.drawable.scene_icon_good_night;
            case AWAY: return R.drawable.scene_icon_away;
            case VACATION: return R.drawable.scene_icon_vacation;
            case HOME: return R.drawable.scene_icon_home;
            case MORNING: return R.drawable.scene_icon_good_morning;

            default:
                return R.drawable.scene_icon_custom;
        }
    }

    public SceneListModel() {}

    public SceneListModel(SceneModel sceneModel) {
        this.nameOfScene = Strings.isNullOrEmpty(sceneModel.getName()) ? "" : sceneModel.getName();
        this.isEnabled = Boolean.TRUE.equals(sceneModel.getEnabled());
        this.hasSchedule = !Strings.isNullOrEmpty(sceneModel.getScheduler());
        this.templateCreatedWith = Strings.isNullOrEmpty(sceneModel.getTemplate()) ? "" : sceneModel.getTemplate();
        this.modelAddress = Strings.isNullOrEmpty(sceneModel.getAddress()) ? "" : sceneModel.getAddress();
        this.actionCount = 0;
        // Only parse context with device addresses in them
        // eg doesn't count.
        /**
         *
         *{
         * "context": { },
         * "name": "Lock or Unlock Doors",
         * "template": "doorlocks"
         *}
         *
         */
        if (sceneModel.getActions() != null) {
            for (Map<String, Object> item : sceneModel.getActions()) {
                Action action = new Action(item);
                if (!action.getContext().isEmpty()) {
                    this.actionCount += 1;
                }
            }
        }
        this.sceneIconResourceID = getIconResIdForTemplateName(templateCreatedWith);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean hasSchedule() {
        return hasSchedule;
    }

    public String getTemplateCreatedWith() {
        return templateCreatedWith;
    }

    public String getModelAddress() {
        return modelAddress;
    }

    public @DrawableRes int getSceneIconResourceID() {
        return sceneIconResourceID;
    }

    public int getActionCount() {
        return actionCount;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isHasSchedule() {
        return hasSchedule;
    }

    public void setHasSchedule(boolean hasSchedule) {
        this.hasSchedule = hasSchedule;
    }

    public void setTemplateCreatedWith(String templateCreatedWith) {
        this.templateCreatedWith = templateCreatedWith;
    }

    public void setModelAddress(String modelAddress) {
        this.modelAddress = modelAddress;
    }

    public void setSceneIconResourceID(int sceneIconResourceID) {
        this.sceneIconResourceID = sceneIconResourceID;
    }

    public void setActionCount(int actionCount) {
        this.actionCount = actionCount;
    }

    public String getNameOfScene() {
        return nameOfScene;
    }

    public void setNameOfScene(String nameOfScene) {
        this.nameOfScene = nameOfScene;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SceneListModel that = (SceneListModel) o;

        if (isEnabled != that.isEnabled) {
            return false;
        }
        if (hasSchedule != that.hasSchedule) {
            return false;
        }
        if (sceneIconResourceID != that.sceneIconResourceID) {
            return false;
        }
        if (actionCount != that.actionCount) {
            return false;
        }
        if (templateCreatedWith != null ? !templateCreatedWith.equals(that.templateCreatedWith) : that.templateCreatedWith != null) {
            return false;
        }
        if (modelAddress != null ? !modelAddress.equals(that.modelAddress) : that.modelAddress != null) {
            return false;
        }
        return !(nameOfScene != null ? !nameOfScene.equals(that.nameOfScene) : that.nameOfScene != null);

    }

    @Override
    public int hashCode() {
        int result = (isEnabled ? 1 : 0);
        result = 31 * result + (hasSchedule ? 1 : 0);
        result = 31 * result + (templateCreatedWith != null ? templateCreatedWith.hashCode() : 0);
        result = 31 * result + (modelAddress != null ? modelAddress.hashCode() : 0);
        result = 31 * result + sceneIconResourceID;
        result = 31 * result + actionCount;
        result = 31 * result + (nameOfScene != null ? nameOfScene.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SceneListModel{" +
              "isEnabled=" + isEnabled +
              ", hasSchedule=" + hasSchedule +
              ", templateCreatedWith='" + templateCreatedWith + '\'' +
              ", modelAddress='" + modelAddress + '\'' +
              ", sceneIconResourceID=" + sceneIconResourceID +
              ", actionCount=" + actionCount +
              ", nameOfScene='" + nameOfScene + '\'' +
              '}';
    }
}
