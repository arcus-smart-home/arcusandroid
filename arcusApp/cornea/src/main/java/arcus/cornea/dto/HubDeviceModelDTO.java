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
package arcus.cornea.dto;

import arcus.cornea.helpers.HubHelpersExt;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;

public class HubDeviceModelDTO implements DeviceModel, HubModel {
    private final HubModel delegate;

    public HubDeviceModelDTO(HubModel hubModel) {
        this.delegate = hubModel;
    }

    @Override
    public String getAccount() {
        return delegate.getAccount();
    }

    @Override
    public String getPlace() {
        return delegate.getPlace();
    }

    @Override
    public String getDevtypehint() {
        return HubModel.NAME;
    }

    @Override
    public String getName() {
        if(delegate.getName() != null){
            return String.valueOf(delegate.getName());
        } else {
            return "";
        }
    }

    @Override
    public void setName(String s) {
        delegate.setName(s);
    }

    @Override
    public String getImage() {
        return delegate.getImage();
    }

    @Override
    public void setImage(String s) {
        delegate.setImage(s);
    }

    @Override
    public String getVendor() {
        return delegate.getVendor();
    }

    @Override
    public String getModel() {
        return delegate.getModel();
    }

    @Override
    public String getState() {
        return delegate.getState();
    }

    @Override
    public String getRegistrationState() {
        return delegate.getRegistrationState();
    }

    @Override
    public Long getTime() {
        return delegate.getTime();
    }

    @Override
    public String getTz() {
        return delegate.getTz();
    }

    @Override
    public void setTz(String s) {
        delegate.setTz(s);
    }

    @Override
    public ClientFuture<PairingRequestResponse> pairingRequest(String actionType, Long timeout, String productPairingMode) {
        return delegate.pairingRequest(actionType, timeout, productPairingMode);
    }

    @Override
    public ClientFuture<UnpairingRequestResponse> unpairingRequest(String s, Long aLong, String s1, String s2, Boolean aBoolean) {
        return delegate.unpairingRequest(s, aLong, s1, s2, aBoolean);
    }

    @Override
    public ClientFuture<ListHubsResponse> listHubs() {
        return delegate.listHubs();
    }

    @Override
    public ClientFuture<ResetLogLevelsResponse> resetLogLevels() {
        return delegate.resetLogLevels();
    }

    @Override
    public ClientFuture<SetLogLevelResponse> setLogLevel(String s, String s1) {
        return delegate.setLogLevel(s, s1);
    }

    @Override
    public ClientFuture<GetLogsResponse> getLogs() {
        return delegate.getLogs();
    }

    @Override
    public ClientFuture<GetConfigResponse> getConfig(Boolean aBoolean, String s) {
        return delegate.getConfig(aBoolean, s);
    }

    @Override
    public ClientFuture<SetConfigResponse> setConfig(Map<String, String> map) {
        return delegate.setConfig(map);
    }

    @Override
    public ClientFuture<DeleteResponse> delete() {
        return delegate.delete();
    }

    @Override
    public String getProductId() {
        return HubHelpersExt.getHubProudctId(this);
    }

    @Override
    public ClientFuture<ListHistoryEntriesResponse> listHistoryEntries(Integer integer, String s) {
        return Futures.failedFuture(new UnsupportedOperationException("Delegate does not support this action."));
    }

    @Override
    public ClientFuture<RemoveResponse> remove(Long aLong) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientFuture<ForceRemoveResponse> forceRemove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(String s) {
        return delegate.get(s);
    }

    @Override
    public Object set(String s, Object o) {
        return delegate.set(s, o);
    }

    @Override
    public boolean isDirty() {
        return delegate.isDirty();
    }

    @Override
    public boolean isDirty(String s) {
        return delegate.isDirty(s);
    }

    @Override
    public Map<String, Object> getChangedValues() {
        return delegate.getChangedValues();
    }

    @Override
    public void clearChanges() {
        delegate.clearChanges();
    }

    @Override
    public <T> boolean clearChange(String s) {
        return delegate.clearChange(s);
    }

    @Override
    public ClientFuture<ClientEvent> refresh() {
        return delegate.refresh();
    }

    @Override
    public ClientFuture<ClientEvent> commit() {
        return delegate.commit();
    }

    @Override
    public ClientFuture<ClientEvent> request(String s) {
        return delegate.request(s);
    }

    @Override
    public ClientFuture<ClientEvent> request(String s, Map<String, Object> map) {
        return delegate.request(s, map);
    }

    @Override
    public ClientFuture<ClientEvent> request(ClientRequest clientRequest) {
        return delegate.request(clientRequest);
    }

    @Override
    public void updateAttributes(Map<String, Object> map) {
        delegate.updateAttributes(map);
    }

    @Override
    public void onDeleted() {
        delegate.onDeleted();
    }

    @Override
    public Map<String, Object> toMap() {
        return delegate.toMap();
    }

    @Override
    public ListenerRegistration addListener(Listener<PropertyChangeEvent> listener) {
        return delegate.addListener(listener);
    }

    @Override
    public ListenerRegistration addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        return delegate.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getAddress() {
        return delegate.getAddress();
    }

    @Override
    public Collection<String> getTags() {
        return delegate.getTags();
    }

    @Override
    public Map<String, String> getImages() {
        return delegate.getImages();
    }

    @Override
    public Collection<String> getCaps() {
        return delegate.getCaps();
    }

    @Override
    public Map<String, Collection<String>> getInstances() {
        return delegate.getInstances();
    }

    @Override
    public ClientFuture<AddTagsResponse> addTags(Collection<String> collection) {
        return delegate.addTags(collection);
    }

    @Override
    public ClientFuture<RemoveTagsResponse> removeTags(Collection<String> collection) {
        return delegate.removeTags(collection);
    }

    @Override
    public ClientFuture<StreamLogsResponse> streamLogs(Long aLong, String s) {
        return delegate.streamLogs(aLong, s);
    }

    @Override
    public ClientFuture<ClientEvent> request(String command, Map<String, Object> attributes, boolean restful) {
        return delegate.request(command, attributes, restful);
    }
}
