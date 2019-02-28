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
package arcus.app.account.settings.contract;

import android.support.annotation.NonNull;

import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;
import com.iris.client.model.ProMonitoringSettingsModel;


public class CertificateDownloadContract {

    public interface CertificateDownloadView  extends PresentedView<ProMonitoringSettingsModel>{
        void onDownloadError();
        void onDownloadComplete(@NonNull String filename);
    }

    public interface CertificateDownloadPresenter extends Presenter<CertificateDownloadView> {
        void downloadCertificate(@NonNull String url);
        void requestUpdate(long downloadId);
    }
}
