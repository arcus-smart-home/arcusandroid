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
package arcus.cornea.subsystem.security.model;

import arcus.cornea.CorneaClientFactory;
import com.iris.client.capability.Device;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.RuleModel;

import java.util.Date;
import java.util.Map;


public class Trigger {
    public static final String CAUSE_PANIC = "panic";

    public static Trigger empty() {
        return new Trigger("", "Security Device", "sensor", new Date());
    }

    protected static Trigger emptySince(Date date) {
        return new Trigger("", "Security Device", "sensor", date);
    }

    public static Trigger panic(Date date) {
        return new Trigger("", CAUSE_PANIC, CAUSE_PANIC, date);
    }

    public static Trigger wrap(Model model, Date date) {
        if(model == null) {
            Trigger trigger = empty();
            trigger.setTriggeredSince(date);
            return trigger;
        }
        else if (model instanceof DeviceModel) {
            return new Trigger(
                  model.getId(),
                  ((Device) model).getName(),
                  ((Device) model).getDevtypehint(),
                  date
            );
        }
        else {
            return Trigger.panic(date);
        }
    }

    public static Trigger wrapRuleModel(Model model, Date date) {
        if (date == null) {
            date = new Date();
        }

        if (model == null || !(model instanceof RuleModel)) {
            return emptySince(date);
        }

        Map<String, Object> deviceMap = ((RuleModel)model).getContext();
        if (deviceMap == null) {
            return emptySince(date);
        }

        for (Map.Entry<String, Object> entries : deviceMap.entrySet()) { // Use the first device, whatever it may be.
            String addressMaybe = String.valueOf(entries.getValue());
            if (addressMaybe == null || !addressMaybe.startsWith("DRIV:dev:")) {
                continue;
            }

            Model maybeLoaded = CorneaClientFactory.getModelCache().get(addressMaybe);
            if (maybeLoaded == null) {
                break;
            }

            return new Trigger(
                  maybeLoaded.getId(),
                  ((Device) maybeLoaded).getName(),
                  ((Device) maybeLoaded).getDevtypehint(),
                  date
            );
        }

        return emptySince(date);
    }


    private String id;
    private String name;
    private String type;
    private Date   triggeredSince;

    public Trigger() {

    }

    public Trigger(String id, String name, String type, Date triggeredSince) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.triggeredSince = triggeredSince;
    }

    public boolean isPanic() {
        return CAUSE_PANIC.equals(type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTriggeredSince() {
        return triggeredSince;
    }

    public void setTriggeredSince(Date triggeredSince) {
        this.triggeredSince = triggeredSince;
    }
}
