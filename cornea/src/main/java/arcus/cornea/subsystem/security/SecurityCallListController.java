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
package arcus.cornea.subsystem.security;

import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.calllist.CallListController;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.model.PersonModel;
import com.iris.client.model.SubsystemModel;


public class SecurityCallListController extends CallListController {
    private static final SecurityCallListController INSTANCE;

    static {
        INSTANCE = new SecurityCallListController(
                SubsystemController.instance().getSubsystemModel(SecuritySubsystem.NAMESPACE),
                PersonModelProvider.instance().newModelList()
        );
    }

    public static SecurityCallListController instance() {
        return INSTANCE;
    }

    SecurityCallListController(ModelSource<SubsystemModel> subsystem, AddressableListSource<PersonModel> persons) {
        super(
                subsystem,
                persons,
                SecuritySubsystem.ATTR_CALLTREEENABLED,
                SecuritySubsystem.ATTR_CALLTREE
        );
        init();
    }


}
