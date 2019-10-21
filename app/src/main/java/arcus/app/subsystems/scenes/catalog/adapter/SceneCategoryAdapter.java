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
package arcus.app.subsystems.scenes.catalog.adapter;

import android.content.Context;

import arcus.app.common.adapters.IconizedChevronListAdapter;
import arcus.app.common.models.ListItemModel;
import arcus.app.subsystems.scenes.catalog.model.SceneCategory;

import java.util.List;


public class SceneCategoryAdapter extends IconizedChevronListAdapter {

    List<SceneCategory> categories;

    public SceneCategoryAdapter(Context context, List<SceneCategory> categories) {
        super(context);
        this.categories = categories;

        for (SceneCategory thisSceneCategory : categories) {
            ListItemModel itemModel = new ListItemModel();

            itemModel.setText(thisSceneCategory.getName());
            itemModel.setSubText(thisSceneCategory.getDescription());
            itemModel.setImageResId(thisSceneCategory.getIconResId());

            add(itemModel);
        }
    }

    public SceneCategory getCategoryAt (int position) {
        return categories.get(position);
    }
}
