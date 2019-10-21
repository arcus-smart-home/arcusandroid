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
package arcus.cornea.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NonNullValues {
    public static String string(Object item) {
        return string(item, "");
    }

    public static String string(Object item, String dflt) {
        if (item == null) {
            return dflt;
        }

        if (item instanceof String) {
            return (String) item;
        }

        String value = item.toString(); // In the event toString was overridden to be null...
        return (value == null) ? dflt : value;
    }

    public static int integer(@Nullable Object attribute) {
        return integer(attribute, 0);
    }

    public static int integer(@Nullable Object attribute, int dflt) {
        if(attribute == null || !(attribute instanceof  Number)) {
            return dflt;
        }

        return ((Number) attribute).intValue();
    }

    public static boolean bool(@Nullable Object attribute) {
        return bool(attribute, false);
    }

    public static boolean bool(@Nullable Object attribute, boolean dflt) {
        if(attribute == null || !(attribute instanceof  Boolean)) {
            return dflt;
        }

        return (Boolean) attribute;
    }

    public static int count(@Nullable Collection<?> attribute) {
        if(attribute == null) {
            return 0;
        }
        return  attribute.size();
    }

    public static <M> Set<M> set(@Nullable Collection<M> attribute) {
        if(attribute == null || attribute.isEmpty()) {
            return ImmutableSet.of();
        }
        if(attribute instanceof  Set) {
            return (Set<M>) attribute;
        }
        return new HashSet<>(attribute);
    }

    public static <M> List<M> list(@Nullable Collection<M> attribute) {
        if(attribute == null) {
            return ImmutableList.of();
        }
        if(attribute instanceof List) {
            return (List<M>) attribute;
        }
        return new ArrayList<>(attribute);
    }

    public static Date date(Date date) {
        return date == null ? new Date() : date;
    }
}
