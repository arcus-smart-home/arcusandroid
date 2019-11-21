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
package arcus.cornea.subsystem.cameras;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

public class DiskCache {
    private static final Logger logger = LoggerFactory.getLogger(DiskCache.class);
    private File contextDir;

    public DiskCache(Context context, String directoryName, int mode) {
        Preconditions.checkNotNull(context);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(directoryName));
        this.contextDir = context.getDir(directoryName, mode);
    }

    public DiskCache(Context context, String directoryName) {
        this(context, directoryName, Context.MODE_PRIVATE);
    }

    public boolean saveImage(Bitmap bmd, String fileName) {
        if (contextDir == null) {
            return false;
        }

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(contextDir, fileName));
            bmd.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return true;
        }
        catch (Exception ex) {
            logger.debug("Could not save image to disk cache.", ex);
            return false;
        }
    }

    @Nullable
    public File getExistingFileRef(String fileName) {
        try {
            return getIfExists(fileName);
        }
        catch (Exception ex) {
            logger.debug("Disk cache is not happy.", ex);
        }

        return null;
    }

    public File getIfExists(String fileName) {
        File file = new File(contextDir, fileName);

        return file.exists() ? file : null;
    }

    public boolean delete(String fileName) {
        File name = getExistingFileRef(fileName);

        return name != null && name.delete();
    }

    public String[] listFileNames() {
        String[] names = contextDir.list();
        return names != null ? names : new String[0];
    }

    public File[] listFiles() {
        File[] files = contextDir.listFiles();
        return files != null ? files : new File[0];
    }

    public int[] clearCache() {
        File[] filesSaved = listFiles();
        int deleted = 0;
        int couldNotDelete = 0;
        for (File file : filesSaved) {
            try {
                logger.debug("Deleting [{}]; Freed: [{}]; Result [{}]", file.getName(), file.length(), file.delete());
                deleted++;
            }
            catch (Exception ex) {
                logger.debug("Error while trying to remove cache for [{}]", file.getAbsolutePath(), ex);
                couldNotDelete++;
            }
        }

        return new int[]{ deleted, couldNotDelete };
    }
}
