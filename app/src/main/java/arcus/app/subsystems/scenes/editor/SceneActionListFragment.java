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
package arcus.app.subsystems.scenes.editor;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.LooperExecutor;
import com.iris.client.bean.Action;
import com.iris.client.bean.ActionTemplate;
import com.iris.client.capability.Scene;
import com.iris.client.model.Model;
import com.iris.client.model.SceneModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.ImageUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.scenes.editor.adapter.SceneActionListAdapter;
import arcus.app.subsystems.scenes.editor.controller.SceneActionsFragmentController;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SceneActionListFragment
        extends SequencedFragment<SceneEditorSequenceController>
        implements SceneActionsFragmentController.Callbacks {

    private ListView sceneActionList;
    private Version1TextView actionListHeading;

    public static SceneActionListFragment newInstance() {
        return new SceneActionListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        sceneActionList = (ListView) view.findViewById(R.id.scene_action_list);
        actionListHeading = (Version1TextView) view.findViewById(R.id.action_list_heading);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        showProgressBar();
        SceneActionsFragmentController.getInstance().setListener(this);
        SceneActionsFragmentController.getInstance().resolveActions(getController().getTemplateID());
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgressBar();
        SceneActionsFragmentController.getInstance().removeListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideProgressBar();
        SceneActionsFragmentController.getInstance().removeListener();
    }

    @Nullable
    @Override
    public String getTitle() {
        return "ACTIONS";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_scene_action_list;
    }

    @Override
    public void onActionsResolved(final List<ActionTemplate> satisfiable, final List<ActionTemplate> unsatisfiable) {
        hideProgressBar();

        if (sceneActionList == null) {
            return;
        }
        if (actionListHeading != null) {
            actionListHeading.setText(getString(R.string.scene_action_list_default_heading));
            if (getController().isEditMode()) {
                actionListHeading.setTextColor(Color.WHITE);
            }
        }

        SceneModel sceneModel = getSceneModel();
        final List<ListItemModel> satisfiableActionList = new ArrayList<>();
        List<ListItemModel> notSatisfiableActionList = new ArrayList<>();
        for (ActionTemplate actionTemplate : satisfiable) {
            ListItemModel model = new ListItemModel(actionTemplate.getName());
            if (sceneModel != null) {
                List<Map<String, Object>> actionList = sceneModel.getActions();
                for (Map<String, Object> actionItemList : actionList) {
                    Action action = new Action(actionItemList);
                    if (action.getTemplate().equals(actionTemplate.getId())) {
                        model.setCount(action.getContext() == null ? 0 : action.getContext().size());
                    }
                }
            }
            model.setAddress(actionTemplate.getTypehint());
            satisfiableActionList.add(model);
        }
        for (ActionTemplate actionTemplate : unsatisfiable) {
            ListItemModel model = new ListItemModel(actionTemplate.getName());
            model.setAddress(actionTemplate.getTypehint());
            notSatisfiableActionList.add(model);
        }

        final List<ListItemModel> adapterList = new ArrayList<>();
        ListItemModel satisfiableHeader = new ListItemModel(getString(R.string.scene_recommended_for_you));
        satisfiableHeader.setIsHeadingRow(true);
        satisfiableHeader.setCount(satisfiableActionList.size());
        adapterList.add(satisfiableHeader);

        Collections.sort(satisfiableActionList, new Comparator<ListItemModel>() {
            @Override
            public int compare(ListItemModel lim1, ListItemModel lim2) {
                return lim1.getText().compareTo(lim2.getText());
            }
        });
        adapterList.addAll(satisfiableActionList);

        if (!notSatisfiableActionList.isEmpty()) {
            ListItemModel notSatisfiableHeader = new ListItemModel(getString(R.string.scene_more_devices_needed));
            notSatisfiableHeader.setIsHeadingRow(true);
            notSatisfiableHeader.setCount(notSatisfiableActionList.size());

            adapterList.add(notSatisfiableHeader);

            Collections.sort(notSatisfiableActionList, new Comparator<ListItemModel>() {
                @Override
                public int compare(ListItemModel lim1, ListItemModel lim2) {
                    return lim1.getText().compareTo(lim2.getText());
                }
            });
            adapterList.addAll(notSatisfiableActionList);
        }

        sceneActionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItemModel model = adapterList.get(position);
                for (ActionTemplate selector : satisfiable) {
                    if (selector.getTypehint().equals(adapterList.get(position).getAddress())) {
                        getController().setActionTemplate(selector);
                        goNext();
                        return;
                    }
                }

                for (ActionTemplate selector : unsatisfiable) {
                    InfoTextPopup popup;
                    if (selector.getTypehint().equals(adapterList.get(position).getAddress())) {
                        popup = InfoTextPopup.newInstance(R.string.more_deivces_for_action, R.string.water_heater_oops, true);
                        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
                        return;
                    }
                }
            }
        });

        sceneActionList.setAdapter(new SceneActionListAdapter(getActivity(), adapterList, getController().isEditMode()));
        sceneActionList.setDivider(new ColorDrawable(getController().isEditMode() ? getResources().getColor(R.color.white_with_10) : getResources().getColor(R.color.black_with_10)));
        sceneActionList.setDividerHeight(ImageUtils.dpToPx(getActivity(), 1));
    }

    @Override
    public void onCorneaError(final Throwable cause) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                hideProgressBar();
                ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
            }
        });
    }

    private
    @Nullable
    SceneModel getSceneModel() {
        Model model = CorneaClientFactory.getModelCache().get(getController().getSceneAddress());
        if (model == null || model.getCaps() == null || !model.getCaps().contains(Scene.NAMESPACE)) {
            return null;
        }

        return (SceneModel) model;
    }
}
