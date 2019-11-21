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
package arcus.cornea.subsystem.connection;

import androidx.annotation.NonNull;

import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.connection.model.CellBackupModel;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.CellBackupSubsystem;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

public class CellularBackup extends BaseSubsystemController<CellularBackup.Callback> {
    public interface Callback {
        void show(CellBackupModel backupModel);
    }

    private static final CellularBackup INSTANCE;
    static {
        INSTANCE = new CellularBackup();
        INSTANCE.init();
    }

    public static CellularBackup instance() {
        return INSTANCE;
    }

    protected CellularBackup() {
        this(CellBackupSubsystem.NAMESPACE);
    }

    protected CellularBackup(String namespace) {
        super(namespace);
    }

    protected CellularBackup(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    @Override protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);
        updateView();
    }

    @Override protected void updateView(Callback callback) {
        callback.show(getStatus());
    }

    public @NonNull CellBackupModel getStatus() {
        CellBackupSubsystem subsystem = (CellBackupSubsystem) getModel();

        if (isLoaded() && subsystem != null) {
            return new CellBackupModel(
                  subsystem.getStatus(),
                  subsystem.getErrorState(),
                  subsystem.getNotReadyState(),
                  null // Cap shows Map being returned... ?
            );
        }

        return CellBackupModel.empty();
    }
}
