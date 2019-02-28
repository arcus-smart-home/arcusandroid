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
package arcus.app.account.settings.walkthroughs;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.print.PrintHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.app.ArcusApplication;
import arcus.app.R;

import arcus.app.account.settings.presenter.CertificateDownloadPresenter;
import arcus.app.activities.DashboardActivity;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.ThrottledDelayedExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;



public class ProMonSettingsCertificateFragment extends BaseFragment {

    public static final String FILE_NAME = "FILE_NAME";

    private static final Logger logger = LoggerFactory.getLogger(ThrottledDelayedExecutor.class);

    private ImageView pdfRendererView, pdfIconView;
    private File pdfFile;
    private Bitmap certificateBitmap = null;


    public static ProMonSettingsCertificateFragment newInstance(@NonNull String placeID) {
        ProMonSettingsCertificateFragment fragment = new ProMonSettingsCertificateFragment();
        Bundle args = new Bundle(1);
        args.putString(FILE_NAME, placeID);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        String certificateFileName = getArguments().getString(FILE_NAME);
        pdfFile = new File(certificateFileName);

        pdfRendererView = (ImageView) rootView.findViewById(R.id.pdf_renderer_view);
        pdfIconView = (ImageView) rootView.findViewById(R.id.pdf_icon_view);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        setEmptyTitle();

        // DId this to provide custom overflow icon.  Current one is only white.
        ((DashboardActivity)getActivity()).setToolbarOverflowMenuIcon(ArcusApplication.getContext().getResources().getDrawable(R.drawable.moredots_icon_small));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            pdfRendererView.setVisibility(View.VISIBLE);
            pdfIconView.setVisibility(View.GONE);
            try {
                openPDF(pdfFile);
            } catch (IOException exception) {
                exception.printStackTrace();
                logger.error("Exception Rendering Certificate PDF", exception);
            }
        } else{
            pdfRendererView.setVisibility(View.GONE);
            pdfIconView.setVisibility(View.VISIBLE);
        }

        getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.share:
                shareCertificate();
                return true;
            case R.id.print:
                printCertificate();
                return true;
            default:
                break;
        }

        return false;
    }

    @TargetApi(21)
    private void openPDF(File pdfFile) throws IOException {

        ParcelFileDescriptor certificateFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);

        PdfRenderer pdfRenderer = new PdfRenderer(certificateFileDescriptor);
        PdfRenderer.Page rendererPage = pdfRenderer.openPage(0);
        int rendererPageWidth = rendererPage.getWidth();
        int rendererPageHeight = rendererPage.getHeight();
        certificateBitmap = Bitmap.createBitmap(
                rendererPageWidth,
                rendererPageHeight,
                Bitmap.Config.ARGB_8888);
        rendererPage.render(certificateBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

        pdfRendererView.setImageBitmap(certificateBitmap);

        rendererPage.close();
        pdfRenderer.close();
        certificateFileDescriptor.close();
    }

    private void shareCertificate() {
        Uri pdfFileUri = Uri.parse("file://" + pdfFile.getAbsolutePath());
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfFileUri);
        shareIntent.setType("application/pdf");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.settings_promon_certificate_send_to)));
    }

    private void printCertificate() {
        if (certificateBitmap != null) {
            PrintHelper photoPrinter = new PrintHelper(this.getActivity());
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FILL);
            photoPrinter.printBitmap(CertificateDownloadPresenter.CERTIFICATE_DOWNLOAD_FILE_NAME, certificateBitmap );
        }
    }

    @Nullable
    @Override
    public Integer getMenuId() {
        if(certificateBitmap != null) {
            return R.menu.menu_certificate_share_print;
        } else {
            return R.menu.menu_certificate_share;
        }
    }

    @Nullable
    @Override
    public String getTitle( ) {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pro_monitoring_download_certificate;
    }

}
