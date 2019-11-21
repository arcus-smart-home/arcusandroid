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
@file:JvmName("DeviceModelExt")
package arcus.cornea.helpers

import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures
import com.iris.client.model.DeviceModel


/**
 * Attempts to use [DeviceModel] as [clazz] and if that's not possible, will return a failed future
 */
fun <T, U> DeviceModel.laterAs(clazz: Class<T>, action: (T) -> ClientFuture<U>) : ClientFuture<U> {
    return if (clazz.isAssignableFrom(this::class.java)) {
        action(clazz.cast(this)!!)
    } else {
        Futures.failedFuture(RuntimeException("[${this::class.java.name}] is not assignable from [${clazz.name}]"))
    }
}

/**
 * Attempts to use [DeviceModel] as [clazz] and if that's not possible, will return null
 *
 * @param clazz the class to try to use the device model as
 * @param action the action to perform which receives the casted type as it's value
 */
fun <T, U> DeviceModel.nowAs(clazz: Class<T>, action: (T) -> U?) : U? {
    return if (clazz.isAssignableFrom(this::class.java)) {
        action(clazz.cast(this)!!)
    } else {
        null
    }
}
