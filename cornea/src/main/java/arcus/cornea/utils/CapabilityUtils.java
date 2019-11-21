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
package arcus.cornea.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.iris.capability.definition.AttributeDefinition;
import com.iris.capability.definition.CapabilityDefinition;
import com.iris.capability.definition.StaticDefinitionRegistry;
import com.iris.client.ClientEvent;
import com.iris.client.capability.Capability;
import com.iris.client.event.ClientFuture;
import com.iris.client.model.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CapabilityUtils {
    private static final Logger logger = LoggerFactory.getLogger(CapabilityUtils.class);
    private static final AndroidCapabilityRegistry CAPABILITY_REGISTRY = new AndroidCapabilityRegistry();
    private static final String NAMESPACE = "NAMESPACE";
    private final Model model;

    public <T extends Model> CapabilityUtils(@NonNull T model) {
        Preconditions.checkNotNull(model, "Model cannot be null");

        this.model = model;
    }

    /**
     *
     * Gets an UNMODIFIABLE set of instance names this model supports or an empty set.
     *
     * @return Set&lt;String&gt;
     */
    public @NonNull Collection<String> getInstanceNames() {
        if (model.getInstances() != null) {
            return Collections.unmodifiableSet(model.getInstances().keySet());
        }

        return Collections.emptySet();
    }

    /**
     *
     * Get the namespaces supported by an instance.
     *
     * @param instanceName
     *
     * @return
     */
    public @NonNull Collection<String> getInstanceNamespaces(String instanceName) {
        if (getInstanceNames().contains(instanceName)) {
            Set<String> instanceNamespaces = new HashSet<>(model.getInstances().get(instanceName));
            return Collections.unmodifiableSet(instanceNamespaces);
        }

        return Collections.emptySet();
    }

    /**
     *
     * Method to getSecuritySubsystem the instances off of the model.  Also see: {@link Model#getInstances()}
     *
     * @return
     */
    @NonNull
    public Map<String, Collection<String>> getInstances() {
        if (model.getInstances() == null) {
            return Collections.emptyMap();
        }
        else {
            return Collections.unmodifiableMap(model.getInstances());
        }
    }

    /**
     *
     * Checks to see if the instance supports the provided capability.
     *
     * @param instanceName
     * @param cap Capability class
     * @param <T> Capability class
     *
     * @return true if the Capability is supported, false if it's not or an error occurred while trying to getSecuritySubsystem the capability NAMESPACE
     */
    public <T extends Capability> boolean instanceSupports(String instanceName, Class<T> cap) {
        if (getInstanceNames().contains(instanceName)) {
            try {
                return getInstanceNamespaces(instanceName).contains(String.valueOf(cap.getField(NAMESPACE).get(null)));
            }
            catch (Exception ex) {
                logger.debug("Caught exception trying to getSecuritySubsystem namespace for capability: [{}]", cap.getSimpleName(), ex);
            }
        }

        return false;
    }

    /**
     * Convience method to getSecuritySubsystem the {@link Capability} from the device if it is present.
     *
     * **Returns {@code null} if the capability is NOT present.
     *
     * @param cap Capability to getSecuritySubsystem from the device model.
     * @param <C> Capability Class.
     *
     * @return Capability from the {@link #model} or {@code null}
     */
    @Nullable
    public <C extends Capability> C getModelAsCapability(Class<C> cap) {
        try {
            return cap.cast(model);
        }
        catch (Exception ex) {
            logger.warn("Device model does not contain capability <{}>; Model Caps: <{}>",
                  cap.getSimpleName(),
                  model.getCaps());
        }

        return null;
    }

    /**
     *
     * Checks to see if the supplied model supports the {@code capability}
     *
     * @param capability
     * @param <C>
     * @return
     */
    public <C extends Capability> boolean modelSupportsCapability(Class<C> capability) {
        return capability.isAssignableFrom(model.getClass());
    }

    /**
     *
     * Starts a call to set the instance name to a specified Key=>Value
     *
     * @param instanceName the name of the instance to modify
     *
     * @return {@link SetInstanceValueBuilder}
     */
    public SetInstanceValueBuilder setInstance(String instanceName) throws IllegalArgumentException {
        if (!getInstanceNames().contains(instanceName)) {
            throw new IllegalArgumentException("Instance was not found in model. Current instances: " + getInstanceNames());
        }
        else {
            return new SetInstanceValueBuilder(instanceName);
        }
    }

    /**
     *
     * Gets the value for the instance attribute or null if the instance does not exist or the value is not set.
     *
     * @param instanceName name of the instance
     * @param attributeName the attribute name
     *
     * @return
     */
    @Nullable
    public Object getInstanceValue(String instanceName, String attributeName) {
        if (!getInstanceNames().contains(instanceName)) {
            return null;
        }

        return model.get(attributeName + ":" + instanceName);
    }

    public class SetInstanceValueBuilder {
        private final String instanceName;
        private List<String> capabilitiesSupported;

        SetInstanceValueBuilder(String instanceName) {
            this.instanceName = instanceName;
            capabilitiesSupported = new ArrayList<>(model.getInstances().get(instanceName));
        }

        /**
         *
         * Set the specified {@code attribute} to {@code value}.  If the {@code attribute} is not supported, this method
         * will throw an {@link IllegalArgumentException}
         *
         * @param attribute Capability Attribute Key
         * @param value Capability Attribute Value
         *
         * @return {@link SetInstanceValueBuilder} for chaining.
         */
        public SetInstanceValueBuilder attriubuteToValue(String attribute, Object value) {
            for (String namespace : capabilitiesSupported) {
                if (CAPABILITY_REGISTRY.namespaceSupportsAttribute(namespace, attribute)) {
                    model.set(attribute + ":" + instanceName, value);
                    return this;
                }
            }

            logger.debug("Instance does not support setting: [{}]. Instance Capabilities: [{}].",
                  attribute,
                  getInstanceNamespaces(instanceName));
            return this;
        }

        /**
         *
         * Shortcut method to {@link Model#commit()}
         *
         * @return ClientFuture
         */
        public ClientFuture<ClientEvent> andSendChanges() {
            return model.commit();
        }
    }



    /**
     *
     * Helper class to getSecuritySubsystem an 'easy to use' map of namespace strings to attribute values supported.
     *
     */
    private static class AndroidCapabilityRegistry {
        private Map<String, List<String>> capsSupportedByNamespace;

        AndroidCapabilityRegistry() {
            capsSupportedByNamespace = new HashMap<>();
            for (CapabilityDefinition definition : StaticDefinitionRegistry.getInstance().getCapabilities()) {
                List<String> attributesSupported = new LinkedList<>();
                capsSupportedByNamespace.put(definition.getNamespace(), attributesSupported);
                for (AttributeDefinition attributeDefinition : definition.getAttributes()) {
                    attributesSupported.add(attributeDefinition.getName());
                }
            }
        }

        public boolean namespaceSupportsAttribute(String namespace, String attribute) {
            if (capsSupportedByNamespace.get(namespace) != null) {
                return capsSupportedByNamespace.get(namespace).contains(attribute);
            }

            return false;
        }
    }
}
