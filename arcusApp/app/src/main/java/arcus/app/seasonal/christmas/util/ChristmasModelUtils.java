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

import android.content.Context;
import android.support.annotation.NonNull;

import arcus.app.ArcusApplication;
import arcus.app.seasonal.christmas.model.ChristmasModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ChristmasModelUtils {
    private static final Logger logger = LoggerFactory.getLogger(ChristmasModelUtils.class);
    public static final String CHRISTMAS_DIR = SantaEventTiming.instance().getSantaSeasonYear() + ".CMAS";
    public static final String CHRISTMAS_FILE = SantaEventTiming.instance().getSantaSeasonYear() + ".CMAS.SRL";

    public static boolean modelCacheExists() {
        File f = new File(getContext().getDir(CHRISTMAS_DIR, Context.MODE_PRIVATE), CHRISTMAS_FILE);
        return f.exists();
    }

    /**
    public static boolean deleteModel() {
        if (modelCacheExists()) {
            File f = new File(getContext().getDir(CHRISTMAS_DIR, Context.MODE_PRIVATE), CHRISTMAS_FILE);
            new SantaPhoto().deletePhoto();
            return f.delete();
        }

        return false;
    }
    **/

    public static @NonNull ChristmasModel getModelCacheFromDisk() {
        ChristmasModel model = new ChristmasModel();
        FileInputStream inStream = null;
        try {
            File f = new File(getContext().getDir(CHRISTMAS_DIR, Context.MODE_PRIVATE), CHRISTMAS_FILE);
            if (f.exists()) {
                inStream = new FileInputStream(f);
                ObjectInputStream objectInStream = new ObjectInputStream(inStream);

                model = (ChristmasModel) objectInStream.readObject();
                objectInStream.close();
            }
        }
        catch (Exception ex) {
            logger.debug("Failed to load ChristmasModel. [{}]", ex.getClass().getSimpleName());
        }

        return model;
    }

    public static boolean cacheModelToDisk(ChristmasModel model) {
        FileOutputStream outStream = null;
        try {
            File f = new File(getContext().getDir(CHRISTMAS_DIR, Context.MODE_PRIVATE), CHRISTMAS_FILE);
            outStream = new FileOutputStream(f);
            ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream);
            objectOutStream.writeObject(model);
            objectOutStream.close();
        }
        catch (Exception ex) {
            return false;
        }

        return true;
    }

    private static Context getContext() {
        return ArcusApplication.getArcusApplication().getApplicationContext();
    }
}
