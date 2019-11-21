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
package arcus.app.common.image;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.activities.PermissionsActivity;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.CameraErrorType;
import arcus.app.common.utils.GlobalSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for building a request to select a user-generated image. Provides a dialog letting
 * the user select "gallery" or "camera", then transitions to the user to appropriate Android
 * {@link Intent}.
 *
 * Using example from https://commonsware.com/blog/2017/06/27/fileprovider-libraries.html
 * to deal with FileProvider instead of using Uri.fromFile (required when targeting 24+
 * and running on 7+)
 */
public class UGCImageSelectionBuilder implements ImageRequestExecutor, PermissionsActivity.PermissionCallback {

    public static final String LAST_CAMERA_IMAGE = "last-camera-image";

    private final Logger logger = LoggerFactory.getLogger(UGCImageSelectionBuilder.class);

    private final Activity activity;
    private boolean fromCamera, fromGallery;

    public UGCImageSelectionBuilder(Activity activity, ImageCategory category, String placeId, String imageId) {
        this.activity = activity;
        ImageCategory category1 = category;
        String placeId1 = placeId;
        String imageId1 = imageId;
    }

    @NonNull
    public UGCImageSelectionBuilder fromCamera() {
        this.fromCamera = true;
        return this;
    }

    @NonNull
    public UGCImageSelectionBuilder fromGallery() {
        this.fromGallery = true;
        return this;
    }

    @NonNull
    public UGCImageSelectionBuilder fromCameraOrGallery() {
        this.fromCamera = true;
        this.fromGallery = true;
        return this;
    }

    @NonNull
    public UGCImageSelectionBuilder withCallback (UGCImageSelectionListener receiver) {
        UGCImageIntentResultHandler.getInstance().registerReceiver(receiver);
        return this;
    }

    public void execute () {
        if (fromCamera && fromGallery) {
            selectImage(activity);
        } else if (fromCamera) {
            takePhoto(activity);
        } else if (fromGallery) {
            selectImageFromGallery(activity);
        } else {
            throw new IllegalStateException("UGC image request made without specifying source. Please call .fromCamera(), .fromGallery, or .fromCameraOrGallery() first.");
        }
    }

    private void selectImage(@NonNull final Activity activity) {
        ((PermissionsActivity)activity).setPermissionCallback(this);
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        ((PermissionsActivity)activity).checkPermission(permissions, GlobalSetting.PERMISSION_CAMERA, R.string.permission_rationale_camera_and_storage);
    }

    private void takePhoto(@NonNull Activity activity) {
        ArcusApplication.selectingGalleryImage();
        Intent pictureCaptureIntent;

        try {
            Uri storageFile = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".arcus.app.provider", createFileForImage());

            //  android.media.action.IMAGE_CAPTURE
            pictureCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureCaptureIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pictureCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, storageFile);

            //  returns the first Activity that can handle the request
            if (pictureCaptureIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(pictureCaptureIntent, IntentRequestCode.TAKE_PHOTO.requestCode);
            } else {
                ErrorManager.in(activity).show(CameraErrorType.NO_CAMERA_AVAILABLE);
            }

        } catch (IOException e) {
            logger.error("Failed to take photo because a storage file wasn't available: " + e.getMessage());
            // TODO: Alert the user of this issue?
        }
    }

    private void selectImageFromGallery(@NonNull Activity activity) {
        // Delay timeout to 10 minutes
        ArcusApplication.selectingGalleryImage();

        Intent imageGalleryIntent;

        imageGalleryIntent = new Intent();

        // Show only images, no videos or anything else
        imageGalleryIntent.setType("image/*");
        imageGalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        imageGalleryIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Always show the chooser (if there are multiple options available)
        activity.startActivityForResult(Intent.createChooser(imageGalleryIntent, activity.getString(R.string.camera_select_an_image)), IntentRequestCode.SELECT_IMAGE_FROM_GALLERY.requestCode);
    }

    @NonNull
    private File createFileForImage () throws IOException {
        File file = new File(Environment.getExternalStorageDirectory(), LAST_CAMERA_IMAGE);

        // Create new file if one doesn't exist; otherwise, we'll overwrite the existing one.
        file.createNewFile();
        return file;
    }

    @Override
    public void permissionsUpdate(int permissionType, ArrayList<String> permissionsDenied, ArrayList<String> permissionsDeniedNeverAskAgain) {
        String message = "";
        if(permissionsDenied.contains(Manifest.permission.CAMERA) && permissionsDenied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            message = activity.getString(R.string.permission_camera_and_storage_denied_message);
        } else if(permissionsDenied.contains(Manifest.permission.CAMERA)) {
            message = activity.getString(R.string.permission_camera_denied_message);
        } else if(permissionsDenied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            message = activity.getString(R.string.permission_storage_denied_message);
        }

        if(message.length() > 0) {
            ((PermissionsActivity)activity).showSnackBarForPermissions(message);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getText(R.string.camera_select_image_source));
        final String galleryButtonName = activity.getResources().getString(R.string.camera_gallery);
        final String cameraButtonName = activity.getResources().getString(R.string.camera);
        final List<String> options = new ArrayList<String>();

        if(!permissionsDenied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            options.add(galleryButtonName);
        }
        if(!permissionsDenied.contains(Manifest.permission.CAMERA) && !permissionsDenied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            options.add(cameraButtonName);
        }
        if(options.size() == 0) {
            return;
        }
        builder.setItems(options.toArray(new String[options.size()]),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int selection) {
                        if(options.get(selection).equals(galleryButtonName)) {
                            selectImageFromGallery(activity);
                        }
                        else if(options.get(selection).equals(cameraButtonName)) {
                            takePhoto(activity);
                        }
                    }
                });

        builder.show();
    }
}
