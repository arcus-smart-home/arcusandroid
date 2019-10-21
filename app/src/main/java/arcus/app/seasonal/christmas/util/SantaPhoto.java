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
package arcus.app.seasonal.christmas.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.support.annotation.NonNull;

import arcus.cornea.utils.LooperExecutor;
import arcus.app.ArcusApplication;
import arcus.app.R;

import java.io.File;
import java.io.FileOutputStream;

public class SantaPhoto {
    private static final int WIDTH_BY_HEIGHT_PX = 1000;
    private static final String SANTA_FILE_NAME = "SANTA.png";
    private static final String SANTA_FILE_NAME2 = "SANTA_" + SantaEventTiming.instance().getSantaSeasonYear() + ".jpg";

    public SantaPhoto() {}

    public Bitmap getSantaPhoto() {
        File file = getSantaFile();
        if (!file.exists()) {
            return null;
        }

        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    public Bitmap getSantaOverlayPhoto () {

        try {
            Bitmap userPhoto = getSantaPhoto();
            if (userPhoto != null) {
                return overlayImages(userPhoto, SantaOverlaySpec.forCurrentSeason());
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public boolean saveFile(Bitmap bmp) {
        return saveFile(bmp, false);
    }

    private boolean saveFile(Bitmap bmp, boolean toPhotoDir) {
        FileOutputStream fos;
        try {
            if (toPhotoDir) {
                fos = new FileOutputStream(getSavedSantaFileName());
            }
            else {
                int width, originalWidth;
                int height, originalHeight;

                width = originalWidth = bmp.getWidth();
                height = originalHeight = bmp.getHeight();
                if (width > WIDTH_BY_HEIGHT_PX || height > WIDTH_BY_HEIGHT_PX) {
                    while (width > WIDTH_BY_HEIGHT_PX || height > WIDTH_BY_HEIGHT_PX) {
                        width /= 2;
                        height /= 2;
                    }

                    Matrix matrix = new Matrix();
                    float sx = width / (float) originalWidth;
                    float sy = height / (float) originalHeight;
                    matrix.setScale(sx, sy);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, originalWidth, originalHeight, matrix, true);
                }
                fos = new FileOutputStream(getSantaFile());
            }
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return true;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    public interface SaveCallback {
        void onSaveSuccess(File file);
        void onSaveFailure(Throwable throwable);
    }

    public void saveFileToPhotos(final Bitmap original, @NonNull final SaveCallback saveCallback) {
        if (original == null) {
            saveCallback.onSaveFailure(new RuntimeException("Could not save images."));
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap saveThis = null;
                try {
                    saveThis = overlayImages(original, SantaOverlaySpec.forCurrentSeason());
                    boolean results = saveFile(saveThis, true);

                    if (!saveThis.isRecycled()) { saveThis.recycle(); }
                    if (!original.isRecycled()) { original.recycle(); }
                    if (results) {
                        emitSuccess(saveCallback, getSavedSantaFileName());
                    }
                    else {
                        emitFailure(saveCallback, new RuntimeException("Could not save image to storage."));
                    }
                }
                catch (Exception ex) {
                    emitFailure(saveCallback, ex);
                }
            }
        }).start();
    }

    private void emitSuccess(@NonNull final SaveCallback saveCallback, final File file) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                saveCallback.onSaveSuccess(file);
            }
        });
    }

    private void emitFailure(@NonNull final SaveCallback saveCallback, final Throwable error) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                saveCallback.onSaveFailure(error);
            }
        });
    }

    public static Bitmap overlayImages(Bitmap source, SantaOverlaySpec overlaySpec) throws Exception {

        // Blank bitmap on which to create a composite
        Bitmap composite = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());

        // Blurred Santa image to be overlaid on user photo
        Bitmap overlay = overlaySpec.getOverlayBitmap();

        // Resize the blurred santa to fit the source image
        int targetWidth = (source.getHeight() * overlay.getWidth()) / overlay.getHeight();
        Bitmap resizedOverlay = Bitmap.createScaledBitmap(overlay, targetWidth, source.getHeight(), false);

        // Create a new canvas and draw the user photo on it
        Canvas canvas = new Canvas(composite);
        canvas.drawBitmap(source, 0, 0, null);

        // Place the blurred santa atop; align to far left or far right per overlaySpec
        switch (overlaySpec.alignment) {
            case SantaOverlaySpec.ALIGN_LEFT:
                canvas.drawBitmap(resizedOverlay, 0, 0, null);
                break;
            case SantaOverlaySpec.ALIGN_RIGHT:
                canvas.drawBitmap(resizedOverlay, composite.getWidth() - resizedOverlay.getWidth(), 0, null);
                break;

            case SantaOverlaySpec.ALIGN_CENTER:
            default:
                canvas.drawBitmap(resizedOverlay, (composite.getWidth() / 2) - (resizedOverlay.getWidth() / 2), 0, null);
                break;
        }

        // Recycle our scraps
        if (!resizedOverlay.isRecycled()) { resizedOverlay.recycle(); }
        if (!overlay.isRecycled()) { overlay.recycle(); }

        return composite;
    }

    private File getSantaFile() {
        return new File(ArcusApplication.getArcusApplication().getExternalFilesDir(null), SANTA_FILE_NAME);
    }

    private File getSavedSantaFileName() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), SANTA_FILE_NAME2);
    }

    private static class SantaOverlaySpec {

        public static final int ALIGN_LEFT = 0;
        public static final int ALIGN_RIGHT = 1;
        public static final int ALIGN_CENTER = 2;

        final int santaOverlayBitmapResId;
        final int alignment;

        public static SantaOverlaySpec forCurrentSeason() {
            return new SantaOverlaySpec();
        }

        public Bitmap getOverlayBitmap() {
            return BitmapFactory.decodeResource(ArcusApplication.getContext().getResources(), santaOverlayBitmapResId);
        }

        private SantaOverlaySpec() {
            santaOverlayBitmapResId = R.drawable.santa_blurred_2017;
            alignment = ALIGN_CENTER;
        }

    }
}
