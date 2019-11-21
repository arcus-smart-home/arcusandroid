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
package arcus.cornea.mock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MockDeviceModel implements DeviceModel {
    private final String address;
    private final Set<String> caps;

    // TODO Builder?
    public MockDeviceModel(@NonNull String deviceAddress, @Nullable Set<String> capsToInclude) {
        this.address = deviceAddress;
        this.caps = capsToInclude == null ? ImmutableSet.<String>of() : capsToInclude;
    }

    @Override
    public String getAccount() {
        return null;
    }

    @Override
    public String getPlace() {
        return null;
    }

    @Override
    public String getDevtypehint() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String s) {

    }

    @Override
    public String getVendor() {
        return null;
    }

    @Override
    public String getModel() {
        return null;
    }

    @Override
    public String getProductId() {
        return null;
    }

    @Override
    public ClientFuture<ListHistoryEntriesResponse> listHistoryEntries(Integer integer, String s) {
        return null;
    }

    @Override
    public ClientFuture<RemoveResponse> remove(Long aLong) {
        return null;
    }

    @Override
    public ClientFuture<ForceRemoveResponse> forceRemove() {
        return null;
    }

    @Override
    public Object get(String s) {
        return null;
    }

    @Override
    public Object set(String s, Object o) {
        return null;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isDirty(String s) {
        return false;
    }

    @Override
    public Map<String, Object> getChangedValues() {
        return null;
    }

    @Override
    public void clearChanges() {

    }

    @Override
    public <T> boolean clearChange(String s) {
        return false;
    }

    @Override
    public ClientFuture<ClientEvent> refresh() {
        return null;
    }

    @Override
    public ClientFuture<ClientEvent> commit() {
        return null;
    }

    @Override
    public ClientFuture<ClientEvent> request(String s) {
        return null;
    }

    @Override
    public ClientFuture<ClientEvent> request(String s, Map<String, Object> map) {
        return null;
    }

    @Override
    public ClientFuture<ClientEvent> request(ClientRequest clientRequest) {
        return null;
    }

    @Override
    public void updateAttributes(Map<String, Object> map) {

    }

    @Override
    public void onDeleted() {

    }

    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    public ListenerRegistration addListener(Listener<PropertyChangeEvent> listener) {
        return null;
    }

    @Override
    public ListenerRegistration addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getAddress() {
        return this.address;
    }

    @Override
    public Collection<String> getTags() {
        return ImmutableSet.<String>of();
    }

    @Override
    public Map<String, String> getImages() {
        return ImmutableMap.<String, String>of();
    }

    @Override
    public Collection<String> getCaps() {
        return caps;
    }

    @Override
    public Map<String, Collection<String>> getInstances() {
        return null;
    }

    @Override
    public ClientFuture<AddTagsResponse> addTags(Collection<String> collection) {
        return null;
    }

    @Override
    public ClientFuture<RemoveTagsResponse> removeTags(Collection<String> collection) {
        return null;
    }
}
