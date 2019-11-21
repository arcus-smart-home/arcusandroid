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
package arcus.app.device.more.presenter;

import androidx.annotation.NonNull;

import arcus.cornea.common.Presenter;
import arcus.cornea.device.smokeandco.WeatherAlertModel;
import arcus.app.device.more.view.WeatherAlertCategoryView;

import java.util.List;

public interface WeatherAlertCategoryPresenter extends Presenter<WeatherAlertCategoryView> {
    /**
     * Gets the defined EAS Selections along
     */
    void getEASCodes();

    /**
     * Set the specified EAS Codes the user is interested in.
     *
     * This should be "The world as you see it" meaning:
     * If the user enters with [a, b, c] and adds d you need to send [a, b, c, d]
     * If the user enters with [a, b, c] and wants to remove [a] you need to send [b, c]
     *
     * @param selectedEASAlerts
     */
    void setSelections(@NonNull List<WeatherAlertModel> selectedEASAlerts);
}
