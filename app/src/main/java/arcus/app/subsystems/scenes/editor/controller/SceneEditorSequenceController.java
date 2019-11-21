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
package arcus.app.subsystems.scenes.editor.controller;

import android.app.Activity;
import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import com.iris.client.bean.Action;
import com.iris.client.bean.ActionTemplate;
import com.iris.client.bean.ThermostatAction;
import com.iris.client.capability.Scene;
import com.iris.client.model.Model;
import com.iris.client.model.SceneModel;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.dashboard.HomeFragment;
import arcus.app.subsystems.scenes.catalog.controller.SceneCatalogSequenceController;
import arcus.app.subsystems.scenes.editor.SceneActionListFragment;
import arcus.app.subsystems.scenes.editor.SceneActionSelectionFragment;
import arcus.app.subsystems.scenes.editor.SceneEditorFragment;
import arcus.app.subsystems.scenes.editor.SceneNameEditorFragment;
import arcus.app.subsystems.scenes.editor.ThermostatActionEditFragment;
import arcus.app.subsystems.scenes.schedule.EditEventFragment;
import arcus.app.subsystems.scenes.schedule.WeeklySchedulerFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SceneEditorSequenceController extends AbstractSequenceController {

    private static final Logger logger = LoggerFactory.getLogger(SceneEditorSequenceController.class);
    private static final String SCENE_TMPL_PREFIX = "SERV:scenetmpl:";

    private Sequenceable lastSequence;
    private String sceneAddress;
    private String templateID;
    private Boolean isEditMode = false;
    private Boolean hasEdited = false;
    private ActionTemplate actionTemplate;

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        if (from instanceof SceneNameEditorFragment) {
            navigateBack(activity, SceneEditorFragment.newInstance());
        }
        else if (from instanceof SceneActionListFragment) {
            navigateForward(activity, SceneActionSelectionFragment.newInstance());
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {

        if (from instanceof SceneEditorFragment && !isEditMode) {
            ((SceneEditorFragment) from).confirmDeletion();
        }
        else if (from instanceof SceneEditorFragment && isEditMode) {
            BackstackManager.getInstance().navigateBack();
        }
        else if (from instanceof SceneNameEditorFragment) {
            navigateBack(activity, SceneEditorFragment.newInstance());
        }
        else if (from instanceof SceneActionListFragment) {
            navigateBack(activity, SceneEditorFragment.newInstance());
        }
        else if (from instanceof SceneActionSelectionFragment) {
            navigateBack(activity, SceneActionListFragment.newInstance());
        }
        else if (from instanceof EditEventFragment) {
            navigateBack(activity, WeeklySchedulerFragment.newInstance(isEditMode));
        }
        else if (from instanceof WeeklySchedulerFragment) {
            navigateBack(activity, SceneEditorFragment.newInstance());
        }
        else if (from instanceof ThermostatActionEditFragment) {
            navigateBack(activity, SceneActionSelectionFragment.newInstance());
        }
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {


        // We entered this sequence from another sequence; return to it
        if (lastSequence != null) {
            if (lastSequence instanceof SceneCatalogSequenceController) {
                boolean engageUser = false;
                if (data != null && data.length > 0) {
                    engageUser = Boolean.TRUE.equals(data[0]);
                }

                ((SceneCatalogSequenceController) lastSequence).setShouldEngage(engageUser);
                navigateBack(activity, lastSequence, data);
            } else {
                navigateBack(activity, lastSequence, data);
            }
        }

        // Entered from a non-sequence; go home
        else {
            BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
        }
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        this.lastSequence = from;
        sceneAddress = unpackArgument(0, String.class, data);
        isEditMode = sceneAddress != null && !sceneAddress.startsWith(SCENE_TMPL_PREFIX);
        navigateForward(activity, SceneEditorFragment.newInstance());
    }

    public void goActionEditor (Activity activity, Sequenceable from, Object... data) {
        logger.debug("Got goActionEditor from {}", from);
        navigateForward(activity, SceneActionListFragment.newInstance());
    }

    public void goScheduleEditor (Activity activity, Sequenceable from, Object... data) {
        logger.debug("Got goScheduleEditor from {}", from);
        navigateForward(activity, SceneEditorFragment.newInstance());
    }

    public void goWeeklyScheduleEditor (Activity activity, Sequenceable from, Object... data) {
        logger.debug("Got goWeeklyScheduleEditor from {}", from);
        navigateForward(activity, WeeklySchedulerFragment.newInstance(isEditMode));
    }

    public void goNameEditor (Activity activity, Sequenceable from, Object... data) {
        logger.debug("Got goNameEditor from {}", from);

        sceneAddress = unpackArgument(0, String.class, data);
        navigateForward(activity, SceneNameEditorFragment.newInstance());
    }

    public void goAddScheduleEvent (Activity activity, Sequenceable from, Object... data) {
        logger.debug("Got goAddScheduleEvent from {}", from);

        String selectedDayOfWeek = unpackArgument(0, String.class, data);
        navigateForward(activity, EditEventFragment.newInstance(selectedDayOfWeek));
    }

    public void goEditScheduleEvent (Activity activity, Sequenceable from, Object... data) {
        logger.debug("Got goEditScheduleEvent from {}", from);

        String selectedDayOfWeek = unpackArgument(0, String.class, data);
        String commandId = unpackArgument(1, String.class, data);
        String time = unpackArgument(2, String.class, data);
        Set<String> selectedDays = unpackArgument(3, Set.class, data);

        navigateForward(activity, EditEventFragment.newInstance(selectedDayOfWeek, commandId, time, new ArrayList<>(selectedDays)));
    }

    public void goThermostatActionEditor(Activity activity, Sequenceable from, Object... data) {
        logger.debug("Got goThermostatActionEditor from {}", from);

        String tStatID = unpackArgument(0, String.class, data);
        navigateForward(activity, ThermostatActionEditFragment.newInstance(tStatID));
    }

    public String getTemplateID() {
        if (templateID == null) {
            return "";
        }

        return templateID;
    }

    public void setTemplateID(String templateID) {
        if (templateID == null) {
            return;
        }

        if (templateID.startsWith(SCENE_TMPL_PREFIX)) {
            this.templateID = templateID;
        }
        else {
            this.templateID = SCENE_TMPL_PREFIX + templateID;
        }
    }

    public ActionTemplate getActionTemplate() {
        return actionTemplate;
    }

    public void setActionTemplate(ActionTemplate actionTemplate) {
        this.actionTemplate = actionTemplate;
    }

    public Boolean getHasEdited() {
        return hasEdited;
    }

    public void setHasEdited(Boolean hasEdited) {
        this.hasEdited = hasEdited;
    }

    public void setSceneAddress (String sceneAddress) {
        this.sceneAddress = sceneAddress;
    }

    public String getSceneAddress () {
        return sceneAddress;
    }

    public boolean isEditMode () {
        return isEditMode;
    }

    public @Nullable SceneModel getSceneModel() {
        Model model = CorneaClientFactory.getModelCache().get(getSceneAddress());
        if (model == null || model.getCaps() == null || !model.getCaps().contains(Scene.NAMESPACE)) {
            return null;
        }

        return (SceneModel) model;
    }

    public Object getSelectorForDevice(String address, String actionSelectorName) {
        SceneModel model = getSceneModel();
        if(model == null) {
            return null;
        }

        List<Map<String, Object>> actions = model.getActions();
        if(actions == null) {
            return null;
        }

        Action toGet = null;
        for(Map<String, Object> actionMap: actions) {
            Action action = new Action(actionMap);
            if (actionTemplate.getId().equals(action.getTemplate())) {
                toGet = action;
                break;
            }
        }

        if(toGet == null) {
            return null;
        }

        Map<String, Object> deviceSelectors = toGet.getContext().get(address);
        if(deviceSelectors == null) {
            return null;
        }

        return deviceSelectors.get(actionSelectorName);
    }

    public void removeAddressFromContext(String address) {
        SceneModel model = getSceneModel();
        if(model == null) {
            // TODO log an error;
            return;
        }


        Action toEdit = null;
        List<Map<String, Object>> newActions = new ArrayList<>();
        List<Map<String, Object>> oldActions = model.getActions();
        if(oldActions != null) {
            for (Map<String, Object> actionMap : oldActions) {
                Action action = new Action(actionMap);
                if (actionTemplate.getId().equals(action.getTemplate())) {
                    toEdit = action;
                } else {
                    newActions.add(actionMap);
                }
            }
        }

        if(toEdit == null) {
            toEdit = new Action();
            toEdit.setTemplate(getActionTemplate().getId());
            toEdit.setName(getActionTemplate().getName());
        }

        Map<String, Map<String, Object>> actionContext = toEdit.getContext();
        if(actionContext == null) {
            actionContext = new HashMap<>();
        }
        else {
            actionContext = new HashMap<>(actionContext);
            actionContext.remove(address);
        }

        toEdit.setContext(actionContext);
        newActions.add(toEdit.toMap());

        model.setActions(newActions);
        model.commit();
    }

    public void updateSelectorForDevice(String address, String actionSelectorName, Object value) {
        doUpdateSelectorForDevice(address, actionSelectorName, value);
    }

    public void removeSelectorForDevice(String address, String actionSelectorName) {
        doUpdateSelectorForDevice(address, actionSelectorName, null);
    }

    private void doUpdateSelectorForDevice(String address, String actionSelectorName, @Nullable Object value) {
        SceneModel model = getSceneModel();
        if(model == null) {
            // TODO log an error;
            return;
        }


        Action toEdit = null;
        List<Map<String, Object>> newActions = new ArrayList<>();
        List<Map<String, Object>> oldActions = model.getActions();
        if(oldActions != null) {
            for (Map<String, Object> actionMap : oldActions) {
                Action action = new Action(actionMap);
                if (actionTemplate.getId().equals(action.getTemplate())) {
                    toEdit = action;
                } else {
                    newActions.add(actionMap);
                }
            }
        }

        if(toEdit == null) {
            toEdit = new Action();
            toEdit.setTemplate(getActionTemplate().getId());
            toEdit.setName(getActionTemplate().getName());
        }

        Map<String, Map<String, Object>> actionContext = toEdit.getContext();
        if(actionContext == null) {
            actionContext = new HashMap<>();
        }
        else {
            actionContext = new HashMap<>(actionContext);
        }

        Map<String, Object> deviceContext = actionContext.get(address);
        if(deviceContext == null) {
            deviceContext = new HashMap<>();
        }
        else {
            deviceContext = new HashMap<>(deviceContext);
            deviceContext.remove(actionSelectorName);
        }
        if(value != null) {
            deviceContext.put(actionSelectorName, value);
        }
        actionContext.put(address, deviceContext);
        toEdit.setContext(actionContext);
        newActions.add(toEdit.toMap());

        model.setActions(newActions);
        model.commit();
    }

    public void updateActionContext(Map<String, Map<String, Object>> actionContext) {
        SceneModel model = getSceneModel();
        if(model == null || actionContext == null) {
            // TODO log an error;
            return;
        }

        Action toEdit = null;
        List<Map<String, Object>> newActions = new ArrayList<>();
        List<Map<String, Object>> oldActions = model.getActions();
        if(oldActions != null) {
            for (Map<String, Object> actionMap : oldActions) {
                Action action = new Action(actionMap);
                if (actionTemplate.getId().equals(action.getTemplate())) {
                    toEdit = action;
                } else {
                    newActions.add(actionMap);
                }
            }
        }

        if(toEdit == null) {
            toEdit = new Action();
            toEdit.setTemplate(getActionTemplate().getId());
            toEdit.setName(getActionTemplate().getName());
        }

        toEdit.setContext(actionContext);
        newActions.add(toEdit.toMap());

        model.setActions(newActions);
        model.commit();
    }

    public void commitModel(List<Map<String, Object>> actionMap) {
        SceneModel model = getSceneModel();
        if (model == null) {
            return;
        }
        model.setActions(actionMap);
        model.commit();
    }

    @SuppressWarnings({"unchecked"})
    public ThermostatAction getThermostatActions(String deviceAddress, String selectorName) {
        Object o = getSelectorForDevice(deviceAddress, selectorName);
        if(o == null || !(o instanceof  Map)) {
            return new ThermostatAction();
        }
        else {
            return new ThermostatAction((Map<String, Object>) o);
        }
    }
}
