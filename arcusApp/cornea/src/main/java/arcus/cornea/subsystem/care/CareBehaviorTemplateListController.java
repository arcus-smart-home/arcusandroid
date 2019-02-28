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
package arcus.cornea.subsystem.care;

import android.util.Pair;

import com.google.common.base.Optional;
import arcus.cornea.provider.CareBehaviorTemplateProvider;
import arcus.cornea.subsystem.care.model.BehaviorTemplate;
import arcus.cornea.subsystem.care.model.CareBehaviorTemplateModel;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.client.bean.CareBehaviorTemplate;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CareBehaviorTemplateListController extends BaseCareController<CareBehaviorTemplateListController.Callback> {
    private static final Logger logger = LoggerFactory.getLogger(CareBehaviorTemplateListController.class);

    public interface Callback {
        void showTemplates(List<BehaviorTemplate> satisfiableTemplates, List<BehaviorTemplate> nonSatisfiableTemplates);
    }

    private static final CareBehaviorTemplateListController INSTANCE;
    static {
        INSTANCE = new CareBehaviorTemplateListController(CareSubsystem.NAMESPACE);
        INSTANCE.init();
    }
    private final Listener<List<Map<String, Object>>> loadedListener = new Listener<List<Map<String, Object>>>() {
        @Override
        public void onEvent(List<Map<String, Object>> maps) {
            updateView();
        }
    };

    protected CareBehaviorTemplateListController(String namespace) {
        super(namespace);
    }

    protected CareBehaviorTemplateListController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    public static CareBehaviorTemplateListController instance() { return INSTANCE; }

    public void listBehaviorTemplates() {
        CareBehaviorTemplateProvider.instance().load().onSuccess(loadedListener);
    }

    @Override protected void updateView(final Callback callback) {
        if (!templatesLoaded()) {
            logger.debug("Not updating view - Templates not loaded.");
            return;
        }

        final Pair<List<BehaviorTemplate>, List<BehaviorTemplate>> templates = buildTemplates();
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.showTemplates(templates.first, templates.second);
            }
        });
    }

    @SuppressWarnings({"unchecked"}) protected Pair<List<BehaviorTemplate>, List<BehaviorTemplate>> buildTemplates() {
        Optional<List<Map<String, Object>>> templates = CareBehaviorTemplateProvider.instance().getAll();
        if (!templates.isPresent()) {
            return new Pair<>(Collections.<BehaviorTemplate>emptyList(), Collections.<BehaviorTemplate>emptyList());
        }

        List<BehaviorTemplate> satisfiable = new ArrayList<>(templates.get().size());
        List<BehaviorTemplate> notSatisfiable = new ArrayList<>(templates.get().size());
        for (Map<String, Object> templateMaps : templates.get()) {
            Collection<String> devices = (Collection<String>) templateMaps.get(CareBehaviorTemplate.ATTR_AVAILABLEDEVICES);
            if (devices == null || devices.isEmpty()) {
                notSatisfiable.add(CareBehaviorTemplateModel.fromMap(templateMaps));
            }
            else {
                satisfiable.add(CareBehaviorTemplateModel.fromMap(templateMaps));
            }
        }

        return new Pair<>(satisfiable, notSatisfiable);
    }
}
