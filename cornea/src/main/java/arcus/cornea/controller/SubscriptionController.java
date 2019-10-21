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
package arcus.cornea.controller;

import arcus.cornea.provider.PlaceModelProvider;
import com.iris.client.capability.Place;


public class SubscriptionController {
    /**
     * This method checks if the account is ProMon level
     *
     * @return Returns true when account level is ProMon
     */
    public static boolean isProfessional() {
        return true;
    }

    /**
     * This method checks if the account is Premium level
     *
     * @return Returns true when account level is Premium
     */
    public static boolean isPremiumOrPro() {
        // Until the place model has loaded, assume the user has a basic subscription
        if(!PlaceModelProvider.getCurrentPlace().isLoaded()) {
            return false;
        }

        String serviceLevel = String.valueOf(PlaceModelProvider.getCurrentPlace().get().get(Place.ATTR_SERVICELEVEL));
        return  isProfessional() ||
                serviceLevel.contains(Place.SERVICELEVEL_PREMIUM) ||
                serviceLevel.contains(Place.SERVICELEVEL_PREMIUM_FREE) ||
                serviceLevel.contains(Place.SERVICELEVEL_PREMIUM_ANNUAL);
    }

    public static String getSubsciptionLevel() {

        try {
            if (!PlaceModelProvider.getCurrentPlace().isLoaded()) {
                return "UNKNOWN";
            }

            return String.valueOf(PlaceModelProvider.getCurrentPlace().get().get(Place.ATTR_SERVICELEVEL));
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}