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
@file:JvmMultifileClass
package arcus.cornea.presenter

import arcus.cornea.utils.LooperExecutor
import java.lang.ref.Reference
import java.lang.ref.WeakReference

/**
 * Base presenter type of contract that can take and set views.
 */
interface BasePresenterContract<in T> {
    /**
     * Sets the view in the Presenter.
     */
    fun setView(view: T)

    /**
     * Clears any references to the view in the Presenter.
     */
    fun clearView()
}

/**
 * Base implementation of the Presenter Contract that uses weak references to store view references.
 */
abstract class KBasePresenter<T> : BasePresenterContract<T> {
    private var _viewRef : Reference<T> = WeakReference(null)
    val presentedView : T?
        get() = _viewRef.get()

    override fun setView(view: T) {
        _viewRef = WeakReference(view)
    }

    override fun clearView() {
        _viewRef.clear()
    }

    /**
     * Helper method that will execute [action] only if [viewRef] returns a non-null value from get()
     *
     * **Note this method is inlined or copied into the body of the caller at runtime to reduce
     * object creation costs.
     *
     * @param action the block of code to execute if the [viewRef] is not null
     */
    protected inline fun onlyIfView(action: (T) -> Unit) {
        presentedView?.let { view ->
            action(view)
        }
    }

    /**
     * Helper method that will execute [action] on the main thread only if [viewRef] returns a
     * non-null value from get()
     *
     * **Note these methods are inlined (copied) into the body of the caller at runtime to reduce
     * object creation costs.
     *
     * @param action the block of code to execute if the [viewRef] is not null
     */
    protected inline fun onMainWithView(crossinline action: T.() -> Unit) {
        LooperExecutor
            .getMainExecutor()
            .execute {
                presentedView?.let { view ->
                    action.invoke(view)
                }
            }
    }
}
