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
package arcus.app.device.settings.core;

/**
 * This abstract class is used instead of a listener that may be serialized and passed into a bundle
 * This works well for a listener (such as SettingChangedParcelizedListener) that has no members and just requires a callback.
 * If you have a listener that has members, DO NOT use this class as the parcel contract
 * methods have been implemented in the adapter with no members.
 */
public abstract class SettingChangedParcelizedListener extends ParcelableNoMembersAdapter {

    public abstract  void onSettingChanged(Setting setting, Object newValue);
}
