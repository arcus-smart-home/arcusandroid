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
package arcus.app.account.settings.presenter;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.common.BasePresenter;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.account.settings.contract.CertificateDownloadContract;
import arcus.app.common.utils.PreferenceCache;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;




public class CertificateDownloadPresenter extends BasePresenter<CertificateDownloadContract.CertificateDownloadView> implements CertificateDownloadContract.CertificateDownloadPresenter {

    public static final String CERTIFICATE_DOWNLOAD_FILE_NAME = "ArcusProfessionalMonitoringCertificate.pdf";
    private static final String CERTIFICATE_DOWNLOAD_DESCRIPTION = "";
    private static final String CERTIFICATE_DOWNLOAD_ID = "DOWNLOAD_ID";
    private static final String AUTHORIZATION = "Authorization";

    private DownloadManager downloadManager;
    private IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

    private final AtomicBoolean receiversRegistered = new AtomicBoolean(false);
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            long downloadId = getDownloadID();
            if (downloadId != -1) {
                requestUpdate(downloadId);
            }
        }
    };

    @Override
    public void downloadCertificate(@NonNull String url) {
        Uri downloadUri = Uri.parse(url);

        downloadManager = (DownloadManager) ArcusApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.addRequestHeader(AUTHORIZATION, CorneaClientFactory.getClient().getSessionInfo().getSessionToken());
        request.setTitle(ArcusApplication.getContext().getString(R.string.settings_promon_certificate_download_notification_title));
        request.setTitle(ArcusApplication.getContext().getString(R.string.settings_promon_certificate_download_notification_text));
        request.setDescription(CERTIFICATE_DOWNLOAD_DESCRIPTION);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, CERTIFICATE_DOWNLOAD_FILE_NAME);
        long downloadId = downloadManager.enqueue(request);

        saveDownloadID(downloadId);
        registerBroadcastReceivers();
    }


    @Override
    public void requestUpdate(long downloadId) {
        DownloadManager.Query downloadQuery = new DownloadManager.Query();
        downloadQuery.setFilterById(downloadId);

        Cursor cursor = downloadManager.query(downloadQuery);
        if(cursor.moveToFirst()) {
            getDownloadStatus(cursor);
        }
    }

    private void saveDownloadID(long downloadId) {
        PreferenceCache.getInstance().putLong(CERTIFICATE_DOWNLOAD_ID, downloadId);
    }

    private long getDownloadID() {
        return PreferenceCache.getInstance().getLong(CERTIFICATE_DOWNLOAD_ID, -1);
    }

    private void getDownloadStatus(Cursor cursor) {

        int columnStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnStatus);

        int fileUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
        String fileUri = cursor.getString(fileUriIdx);
        String fileName = null;
        if (fileUri != null) {
            File mFile = new File(Uri.parse(fileUri).getPath());
            fileName = mFile.getAbsolutePath();
        }



        switch(status){
            case DownloadManager.STATUS_FAILED:
                downloadError();
                PreferenceCache.getInstance().removeKey(CERTIFICATE_DOWNLOAD_ID);
                removeBroadcastReceivers();
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                PreferenceCache.getInstance().removeKey(CERTIFICATE_DOWNLOAD_ID);
                removeBroadcastReceivers();
                downloadComplete(fileName);
                break;
        }

    }

    private void downloadError() {
        getPresentedView().onDownloadError();
    }


    private void downloadComplete(String downloadId) {
        getPresentedView().onDownloadComplete(downloadId);
    }

    private void registerBroadcastReceivers() {
        if (!receiversRegistered.getAndSet(true)) {
            ArcusApplication.getContext().registerReceiver(receiver, filter);
        }
    }

    private void removeBroadcastReceivers() {
        if (receiversRegistered.getAndSet(false)) {
            ArcusApplication.getContext().unregisterReceiver(receiver);
        }
    }

}
