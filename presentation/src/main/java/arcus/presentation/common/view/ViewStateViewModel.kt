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
package arcus.presentation.common.view

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

abstract class ViewStateViewModel<T> : ViewModel() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    protected val _viewState: MutableLiveData<ViewState<T>> = object : MutableLiveData<ViewState<T>>() {
        override fun onActive() {
            super.onActive()
            loadData()
        }
    }
    val viewState: LiveData<ViewState<T>> get() = _viewState

    protected abstract fun loadData()

    protected inline fun <reified T : Any> MutableLiveData<ViewState<T>>.postLoadedValue(value: T) {
        postValue(ViewState.Loaded(value))
    }

    @MainThread
    protected fun emitLoading() {
        _viewState.value = ViewState.Loading()
    }

    /**
     * Launches a coroutine from within the [viewModelScope] and captures the output as a `ViewState.Error` if the
     * [block] fails with an exception.
     *
     * @see launch for explanation of params.
     */
    protected fun safeLaunch(
        context: CoroutineContext = CoroutineExceptionHandler { _, throwable ->
            _viewState.value = ViewState.Error(throwable, ViewError.GENERIC)
            logger.error(
                "Unhandled exception in ViewModel: ${this@ViewStateViewModel::class.java.simpleName}",
                throwable
            )
        },
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = viewModelScope.launch(context, start, block)
}
