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
package arcus.app.subsystems.debug

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import arcus.cornea.SessionController
import arcus.cornea.subsystem.cameras.ClipPreviewImageGetter
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.LooperExecutor
import com.iris.client.capability.Device
import com.iris.client.capability.MobileDevice
import com.iris.client.event.ListenerRegistration
import com.iris.client.model.PersonModel
import arcus.app.BuildConfig
import arcus.app.ArcusApplication
import arcus.app.R
import arcus.app.common.utils.PreferenceCache
import arcus.app.common.utils.PreferenceUtils
import com.squareup.picasso.Picasso
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.properties.Delegates

class DebugMenuFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var devMock by Delegates.notNull<DevMock>()
    private var mockListener: ListenerRegistration? = null
    private val buildString : String = "%s (%s)".format(BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE)
    private val prefsUrlKey : String
        get() = getString(R.string.debug_menu_custom_environments_pref)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_debug_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.debug_menu_recycler_view)
        (rv.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        rv.layoutManager = LinearLayoutManager(context)

        val options = listOf(
                HeaderOption(getString(R.string.debug_menu_general)),
                TextOption(getString(R.string.debug_menu_build_version), buildString),
                HeaderOption(getString(R.string.debug_menu_login)),
                getSetupForEnvironmentSwitch(),
                getSetupForAddEnvironment(),
                getSetupForRemoveEnvironment(),
                getSetupForCookieDispose(),
                HeaderOption(getString(R.string.debug_menu_other)),
                getSetupForClearClipCache(),
                getSetupForAddMock(),
                getSetupForDeleteMobileDevices(),
                HeaderOption(getString(R.string.debug_menu_preferences)),
                getSetupForAnimationsToggle(),
                getSetupForCleanPreferences(),
                getSetupForClearKeyFromCache(),
                HeaderOption(getString(R.string.debug_menu_images)),
                getSetupForPicassoIndicators(),
                getSetupForPicassoDisableCache(),
                getSetupForPicassoDebug(),
                getSetupForDumpStats(),
                getSetupForChangePicassoHeapSize()
        )
        val adapter = DebugMenuAdapter(options)

        rv.adapter = adapter
    }

    private fun getSetupForEnvironmentSwitch() = ButtonOption(
            getString(R.string.debug_menu_platform_button_text),
            getString(R.string.debug_menu_platform_title),
            getString(R.string.debug_menu_platform_desc),
            "Current: ${PreferenceUtils.getPlatformUrl()}"
    ) { current, rvAdapter ->
        onMainThreadWithActivity {
            val adapter = ArrayAdapter<String>(it, android.R.layout.select_dialog_item)
            val urls = resources
                    .getStringArray(R.array.debug_menu_environments)
                    .toMutableSet()

            urls.addAll(PreferenceCache.getInstance().getStringSet(prefsUrlKey, mutableSetOf()))
            val choices = urls
                    .map {
                        val split = it.split(KEY_DELIMITER)
                        split[0] to split[1]
                    }.toMap()

            adapter.addAll(choices.keys)

            AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.debug_menu_platform_title))
                    .setAdapter(adapter) { _, which ->
                        PreferenceUtils.putPlatformUrl(choices[adapter.getItem(which)])
                        current.detailsText = "Current: ${PreferenceUtils.getPlatformUrl()}"
                        rvAdapter.notifyDataSetChanged()
                    }
                    .setNegativeButton(R.string.debug_menu_cancel) { _, _ -> }
                    .setCancelable(true)
                    .show()
        }
        true
    }

    private fun getSetupForAddEnvironment() = ButtonOption(
            getString(R.string.debug_menu_add_custom_platform_button_text),
            getString(R.string.debug_menu_add_custom_platform_title),
            getString(R.string.debug_menu_add_custom_platform_desc)
    ) { _, _ ->
        onMainThreadWithActivity {
            val layoutInflater = LayoutInflater.from(it)
            val view = layoutInflater.inflate(R.layout.debug_menu_custom_environment, null)
            val name = view.findViewById<EditText>(R.id.debug_menu_friendly_name)
            val url = view.findViewById<EditText>(R.id.debug_menu_url)

            AlertDialog.Builder(it)
                    .setView(view)
                    .setCancelable(true)
                    .setNegativeButton(R.string.debug_menu_cancel) { _, _ -> }
                    .setPositiveButton(R.string.debug_menu_ok) { _, _ ->
                        if (!name.text.isNullOrBlank() && !url.text.isNullOrBlank()) {
                            val key = getString(R.string.debug_menu_custom_environments_pref)
                            val existing = PreferenceCache
                                    .getInstance()
                                    .getStringSet(key, setOf())
                                    .toMutableSet()
                            val addName = "${name.text}|${url.text}"

                            // If the cache doesn't have this key already
                            if (existing.firstOrNull { it.toLowerCase() == addName.toLowerCase() } == null) {
                                existing.add(addName)
                                PreferenceCache.getInstance().putStringSet(key, existing)
                            }
                        }
                    }
                    .show()
        }
        false
    }

    private fun getSetupForRemoveEnvironment() = ButtonOption(
            getString(R.string.debug_menu_remove_custom_platform_button_text),
            getString(R.string.debug_menu_remove_custom_platform_title),
            getString(R.string.debug_menu_remove_custom_platform_desc)
    ) { _, _ ->
        onMainThreadWithActivity {
            val adapter = ArrayAdapter<String>(it, android.R.layout.select_dialog_item)
            val environments = PreferenceCache.getInstance().getStringSet(prefsUrlKey, mutableSetOf())
            val choices = environments
                .map {
                    val item = it.split(KEY_DELIMITER)[0]
                    if (item.isBlank()) {
                        "Blank"
                    } else {
                        item
                    }
                }
            adapter.addAll(choices)

            AlertDialog.Builder(it)
                    .setTitle(R.string.debug_menu_remove_custom_platform_button_text)
                    .setCancelable(true)
                    .setAdapter(adapter) { _, which ->
                        val existing = PreferenceCache.getInstance().getStringSet(prefsUrlKey, setOf())
                        val selectedFromAdapter = choices[which]

                        val updated = existing
                            .filterNot { it.startsWith(selectedFromAdapter) }
                            .toSet()

                        PreferenceCache
                            .getInstance()
                            .putStringSet(prefsUrlKey, updated)
                    }
                    .setNegativeButton(R.string.debug_menu_cancel) { _, _ -> }
                    .show()
        }
        false
    }

    private fun getSetupForAnimationsToggle() = BinaryOption(
            getString(R.string.debug_menu_animation_title),
            getString(R.string.debug_menu_animation_desc),
            PreferenceUtils.isAnimationEnabled()
    ) { _, isChecked ->
        PreferenceUtils.setAnimationEnabled(isChecked)
        false
    }

    private fun getSetupForCookieDispose() = ButtonOption(
            getString(R.string.debug_menu_discard_token_button_text),
            getString(R.string.debug_menu_discard_token_title),
            getString(R.string.debug_menu_discard_token_desc)
    ) { _, _ ->
        onMainThreadWithActivity {
            AlertDialog.Builder(it)
                    .setTitle(R.string.debug_menu_are_you_sure)
                    .setMessage(R.string.debug_menu_discard_token_dialog_desc)
                    .setNegativeButton(R.string.debug_menu_cancel) { _, _ -> }
                    .setPositiveButton(R.string.debug_menu_ok) { _, _ ->
                        PreferenceUtils.removeLoginToken()
                    }
                    .show()
        }

        false
    }

    private fun getSetupForCleanPreferences() = ButtonOption(
            getString(R.string.debug_menu_pref_toss_button_text),
            getString(R.string.debug_menu_pref_toss_title),
            getString(R.string.debug_menu_pref_toss_desc)
    ) { _, _ ->
        onMainThreadWithActivity {
            AlertDialog.Builder(it)
                    .setTitle(R.string.debug_menu_are_you_sure)
                    .setMessage(R.string.debug_menu_pref_toss_dialog_desc)
                    .setNegativeButton(R.string.debug_menu_cancel) { _, _ -> }
                    .setPositiveButton(R.string.debug_menu_ok) { _, _ ->
                        ArcusApplication.getSharedPreferences().edit().clear().apply()
                        PreferenceCache.getInstance().clear()
                    }
                    .show()
        }

        false
    }

    private fun getSetupForPicassoIndicators() = BinaryOption(
            getString(R.string.debug_menu_picasso_indicators_title),
            getString(R.string.debug_menu_picasso_indicators_desc),
            PreferenceUtils.arePicassoIndicatorsEnabled()
    ) { current, isChecked ->
        current.isChecked = isChecked
        PreferenceUtils.setPicassoIndicatorsEnabled(isChecked)
        false
    }

    private fun getSetupForPicassoDisableCache() = BinaryOption(
            getString(R.string.debug_menu_no_image_cache_title),
            getString(R.string.debug_menu_no_image_cache_desc),
            PreferenceUtils.isPicassoCacheDisabled()
    ) { current, isChecked ->
        current.isChecked = isChecked
        PreferenceUtils.setPicassoCacheDisabled(isChecked)
        false
    }

    private fun getSetupForPicassoDebug() = BinaryOption(
            getString(R.string.debug_menu_picasso_debug_log_title),
            getString(R.string.debug_menu_picasso_debug_log_desc),
            PreferenceUtils.isPicassoInDebugMode()
    ) { current, isChecked ->
        current.isChecked = isChecked
        PreferenceUtils.setPicassoInDebugMode(isChecked)
        false
    }

    private fun getSetupForDumpStats() = ButtonOption(
            getString(R.string.debug_menu_picasso_load_stats_button_text),
            getString(R.string.debug_menu_picasso_load_stats_title),
            getString(R.string.debug_menu_picasso_load_stats_desc)
    ) { _, _ ->
        val imageStats = Picasso
                .with(ArcusApplication.getContext())
                .snapshot
                .toString()
                .replace(",", "\n")
                .replace("{", " {\n ")
                .replace("}", "\n}")

        logger.debug("Image Stats: {}", imageStats)

        activity?.let {
            AlertDialog.Builder(it)
                    .setTitle("Image Stats")
                    .setMessage(imageStats)
                    .setCancelable(true)
                    .setNegativeButton("Done") { _, _ -> }
                    .show()
        }

        false
    }

    private fun getSetupForChangePicassoHeapSize() = ButtonOption(
            getString(R.string.debug_menu_picasso_memory_size_button_text),
            getString(R.string.debug_menu_picasso_memory_size_title),
            getString(R.string.debug_menu_picasso_memory_size_desc),
            "Current Setting: ${PreferenceUtils.getPicassoMemoryCacheSize()}"
    ) { current, rvAdapter ->
        onMainThreadWithActivity { activity ->
            val adapter = ArrayAdapter<String>(activity, android.R.layout.select_dialog_item)
            adapter.addAll(*resources.getStringArray(R.array.debug_menu_picasso_heap_sizes))

            AlertDialog.Builder(activity)
                .setTitle(getString(R.string.debug_menu_picasso_memory_size_dialog_body))
                .setAdapter(adapter) { _, which ->
                    adapter.getItem(which)?.replace("%", "")?.toInt()?.let {
                        PreferenceUtils.setPicassoMemoryCacheSize(it)
                    }
                    current.detailsText = "Current Setting: ${PreferenceUtils.getPicassoMemoryCacheSize()}"
                    rvAdapter.notifyDataSetChanged()
                }
                .setNegativeButton(R.string.debug_menu_cancel) { _, _ -> }
                .setCancelable(true)
                .show()
        }
        false
    }

    @SuppressLint("VisibleForTests")
    private fun getSetupForClearClipCache() = ButtonOption(
            getString(R.string.debug_menu_clear_clip_cache_button_text),
            getString(R.string.debug_menu_clear_clip_cache_title),
            getString(R.string.debug_menu_clear_clip_cache_desc)
    ) { _, _ ->
        val deletedNotDeleted = ClipPreviewImageGetter.instance().clearDiskCache()
        val message = "Deleted %d files%n%nCould not delete %d files.".format(
                deletedNotDeleted[0],
                deletedNotDeleted[1]
        )
        activity?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }

        false
    }

    private fun getSetupForAddMock() = ButtonOption(
            getString(R.string.debug_menu_add_mock_button_text),
            getString(R.string.debug_menu_add_mock_title),
            getString(R.string.debug_menu_add_mock_desc)
    ) { _, _ ->
        Listeners.clear(mockListener)
        devMock = DevMock()
        onMainThreadWithActivity { activity ->
            val adapter = ArrayAdapter<String>(activity, android.R.layout.select_dialog_item)
            adapter.addAll(devMock.mockSelectionsAsList)

            AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.debug_menu_add_mock_dialog_title))
                    .setAdapter(adapter) { _, which ->
                        val selection = adapter.getItem(which)
                        mockListener = devMock.createMockDevice(selection, object : DevMock.Callback {
                            override fun onSuccess(attributes: Map<String, Any>) {
                                val name = attributes[Device.ATTR_NAME] as String
                                val model = attributes[Device.ATTR_MODEL] as String
                                val typeHint = attributes[Device.ATTR_DEVTYPEHINT] as String
                                val vendor = attributes[Device.ATTR_VENDOR] as String
                                val all = String.format(
                                        "Success!\nName: %s\nModel: %s\nType Hint: %s\nVendor: %s\n",
                                        name, model, typeHint, vendor
                                )
                                onMainThreadWithActivity {
                                    Toast.makeText(it, all, Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(throwable: Throwable) {
                                onMainThreadWithActivity {
                                    Toast.makeText(it, throwable.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        })
                    }
                    .setNegativeButton(getString(R.string.debug_menu_cancel)) { _, _ -> }
                    .setCancelable(true)
                    .show()
        }
        false
    }

    private fun getSetupForDeleteMobileDevices() = ButtonOption(
            getString(R.string.debug_menu_delete_mobile_device_button_text),
            getString(R.string.debug_menu_delete_mobile_device_title),
            getString(R.string.debug_menu_delete_mobile_device_desc)
    ) { _, _ ->
        val personModel = SessionController.instance().person
        personModel
                ?.listMobileDevices()
                ?.onFailure { throwable ->
                    onMainThreadWithActivity {
                        Toast.makeText(it, throwable.message ?: "", Toast.LENGTH_SHORT).show()
                    }
                }
                ?.onSuccess { response ->
                    val devices = response.mobileDevices
                    if (devices == null || devices.isEmpty()) {
                        onMainThreadWithActivity {
                            Toast.makeText(it, "No mobile devices found for the person response.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val deviceIndexes = devices.map {
                            it[MobileDevice.ATTR_DEVICEINDEX] as Number
                        }.toList()
                        deleteMobileDevices(personModel, deviceIndexes)
                    }
                }

        false
    }

    private fun getSetupForClearKeyFromCache() = ButtonOption(
            getString(R.string.debug_menu_clear_key_cache_button_text),
            getString(R.string.debug_menu_clear_key_cache_title),
            getString(R.string.debug_menu_clear_key_cache_desc)
    ) { _, _ ->
        onMainThreadWithActivity { activity ->
            val adapter = ArrayAdapter<String>(activity, android.R.layout.select_dialog_item)
            val prefs = ArcusApplication.getSharedPreferences().all
            prefs?.let {
                adapter.addAll(it.keys)
            }

            if (adapter.count == 0) {
                AlertDialog.Builder(activity)
                        .setTitle(getString(R.string.debug_menu_clear_key_cache_no_entries_dialog_title))
                        .setMessage(getString(R.string.debug_menu_clear_key_cache_no_entries_dialog_body))
                        .setCancelable(true)
                        .show()
            } else {
                AlertDialog.Builder(activity)
                        .setTitle(getString(R.string.debug_menu_clear_key_cache_entries_dialog_title))
                        .setAdapter(adapter) { _, which ->
                            ArcusApplication.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
                            PreferenceCache.getInstance().removeKey(adapter.getItem(which))
                        }
                        .setCancelable(true)
                        .setNegativeButton(getString(R.string.debug_menu_cancel)) { _, _ -> }
                        .show()
            }
        }

        false
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val prefs = sharedPreferences.all
        if (prefs != null) {
            onMainThreadWithActivity {
                AlertDialog.Builder(it)
                        .setTitle(getString(R.string.debug_menu_clear_key_results_dialog_title))
                        .setMessage(getString(R.string.debug_menu_clear_key_results_dialog_body, key, !prefs.containsKey(key)))
                        .setCancelable(true)
                        .show()
            }
        }
        ArcusApplication.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.debug_menu_title)
    }

    override fun onPause() {
        super.onPause()
        Listeners.clear(mockListener)
        ArcusApplication.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun deleteMobileDevices(personModel: PersonModel, deviceIndexes: List<Number>) {
        onMainThreadWithActivity {
            Toast.makeText(it, "Starting to delete [${deviceIndexes.size}] devices", Toast.LENGTH_SHORT).show()
        }

        Thread {
            val deleted = ArrayList<Int>()
            val failed = ArrayList<Int>()

            deviceIndexes.forEach { index ->
                try {
                    val intIndex = index.toInt()
                    personModel
                        .removeMobileDevice(intIndex)
                        .onFailure { _ -> failed.add(intIndex) }
                        .onSuccess { _ -> deleted.add(intIndex) }
                        .get() // BLOCKING
                } catch (ex: Exception) {
                    failed.add(index.toInt())
                    logger.error("Failed to remove device", ex)
                }
            }

            onMainThreadWithActivity {
                Toast.makeText(
                    it,
                    "Total: %d, Deleted: %d, Failed: %d".format(deviceIndexes.size, deleted.size, failed.size),
                    Toast.LENGTH_LONG
                ).show()
            }
        }.start()
    }

    private inline fun onMainThreadWithActivity(crossinline action: (Activity) -> Unit) {
        activity?.let {
            LooperExecutor.getMainExecutor().execute({ action(it) })
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DebugMenuFragment::class.java)
        private const val KEY_DELIMITER = "|"
    }
}
