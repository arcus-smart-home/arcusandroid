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
package arcus.app.common.utils;

import android.content.SharedPreferences;
import android.text.TextUtils;

import arcus.app.ArcusApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PreferenceCache {

    private final static PreferenceCache instance = new PreferenceCache();

    private final static String ATTRS_SUFFIX = "-attributes";
    private final static String VALUES_SUFFIX = "-values";
    private final static String STRING_DELIMINATOR = "%%%%";

    private final HashMap<String, Object> cache = new HashMap<>();

    private PreferenceCache () {}

    public static PreferenceCache getInstance () {return instance;}

    public void clear() {
        cache.clear();
    }

    public void putStringMap(String key, Map<String,String> map) {
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (String thisKey : map.keySet()) {
            keys.add(thisKey);
            values.add(String.valueOf(map.get(thisKey)));
        }

        putStringList(key + ATTRS_SUFFIX, keys);
        putStringList(key + VALUES_SUFFIX, values);
    }

    public Map<String,String> getStringMap(String key) {
        String[] attributes = getStringList(key + ATTRS_SUFFIX);
        String[] values = getStringList(key + VALUES_SUFFIX);
        Map<String,String> map = new HashMap<>();

        if (attributes == null || values == null || attributes.length != values.length) {
            return map;
        }

        for (int index = 0; index < attributes.length; index++) {
            map.put(attributes[index], values[index]);
        }

        return map;
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = ArcusApplication.getSharedPreferences().edit();
        editor.putString(key, value);
        editor.apply();

        cache.put(key, value);
    }

    public String getString(String key, String dflt) {
        if (cache.containsKey(key)) {
            return (String) cache.get(key);
        } else {
            String value = ArcusApplication.getSharedPreferences().getString(key, dflt);

            cache.put(key, value);
            return value;
        }
    }

    public void putStringSet(String key, Set<String> value) {
        SharedPreferences.Editor editor = ArcusApplication.getSharedPreferences().edit();
        editor.putStringSet(key, value);
        editor.apply();

        cache.put(key, value);
    }

    public Set<String> getStringSet(String key, Set<String> dflt) {
        if (cache.containsKey(key)) {
            return (Set<String>) cache.get(key);
        } else {
            Set<String> value = ArcusApplication.getSharedPreferences().getStringSet(key, dflt);

            cache.put(key, value);
            return value;
        }
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = ArcusApplication.getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.apply();

        cache.put(key, value);
    }

    public boolean getBoolean(String key, boolean dflt) {
        if (cache.containsKey(key)) {
            return (boolean) cache.get(key);
        } else {
            boolean value = ArcusApplication.getSharedPreferences().getBoolean(key, dflt);

            cache.put(key, value);
            return value;
        }
    }

    public void putInteger(String key, Integer integer) {
        SharedPreferences.Editor editor = ArcusApplication.getSharedPreferences().edit();
        editor.putInt(key, integer);
        editor.apply();

        cache.put(key, integer);
    }

    public Integer getInteger (String key, Integer dflt) {
        if (cache.containsKey(key)) {
            return (Integer) cache.get(key);
        } else {
            Integer value = ArcusApplication.getSharedPreferences().getInt(key, dflt);

            cache.put(key, value);
            return value;
        }
    }

    public void putStringList(String key, List values) {
        putString(key, TextUtils.join(STRING_DELIMINATOR, values));
    }

    public String[] getStringList(String key) {
        String value = getString(key, "");
        if (!value.equals("")) {
            return TextUtils.split(value, STRING_DELIMINATOR);
        }
        return new String[]{};
    }

    public void putLong(String key, long value) {
        SharedPreferences.Editor editor = ArcusApplication.getSharedPreferences().edit();
        editor.putLong(key, value).apply();

        cache.put(key, value);
    }

    public long getLong(String key, long defaultValue) {
        Object value = cache.get(key);
        if (value != null) {
            return (Long) value;
        }

        Long prefValue = ArcusApplication.getSharedPreferences().getLong(key, defaultValue);
        cache.put(key, prefValue);

        return  prefValue;
    }

    public void removeKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }

        cache.remove(key);
        ArcusApplication.getSharedPreferences().edit().remove(key).apply();
    }
}
