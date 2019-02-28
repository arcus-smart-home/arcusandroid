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
package arcus.cornea.subsystem.safety;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.SubsystemModel;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class DashboardCardController extends AbstractSafetyController<DashboardCardController.Callback> {

    public interface Callback extends AbstractSafetyController.SafetyCallback<String> {
    }

    private static final DashboardCardController instance = new DashboardCardController(
            SubsystemController.instance().getSubsystemModel(SafetySubsystem.NAMESPACE),
            DeviceModelProvider.instance().getModels(Collections.<String>emptySet()));

    public static DashboardCardController instance() {
        return instance;
    }

    DashboardCardController(ModelSource<SubsystemModel> subsystem, AddressableListSource<DeviceModel> devices) {
        super(subsystem, devices);
        attachListeners();
    }

    @Override
    protected Object getSummary(SafetySubsystem safety) {
        Map<String,String> sensorStates = safety.getSensorState();

        List<String> safe = new LinkedList<>();
        List<String> clearing = new LinkedList<>();

        if (sensorStates != null) {
            // enforce ordering shown in invision
            addSensor("SMOKE", sensorStates.get("SMOKE"), safe, clearing);
            addSensor("CO", sensorStates.get("CO"), safe, clearing);
            addSensor("WATER", sensorStates.get("WATER"), safe, clearing);
        }

        // TODO:  should localize this
        String safeMsg = join("No", safe, "or", "detected");
        String clearingMsg = join("Clearing", clearing, "and", "");
        if (StringUtils.isBlank(safeMsg)) {
            return clearingMsg;
        }
        return safeMsg + (StringUtils.isBlank(clearingMsg) ? "" : "  ") + clearingMsg;
    }

    private static String join(String prefix, List<String> sensors, String joinClause, String postfix) {
        if(sensors.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(prefix).append(" ");
        int i = 0;
        for(String s : sensors) {
            sb.append(s);
            if(i < sensors.size() - 2) {
                sb.append(", ");
            } else  if(i == sensors.size() - 2) {
                sb.append(" ").append(joinClause).append(" ");
            }
            i++;
        }
        if(!StringUtils.isBlank(postfix)) {
            sb.append(" ").append(postfix);
        }
        sb.append(".");
        return sb.toString();
    }

    private void addSensor(String sensor, String state,  List<String> safe, List<String> clearing) {
        switch(state) {
            // not sure what to render for offline
            case "DETECTED": clearing.add(translate(sensor));
            case "NONE": return;
            default: safe.add(translate(sensor));
        }
    }

    // TODO:  should localize this
    private String translate(String sensor) {
        switch(sensor) {
            case "WATER": return "water leaks";
            case "CO":  return sensor;
            default:
                return sensor.toLowerCase();
        }
    }
}
