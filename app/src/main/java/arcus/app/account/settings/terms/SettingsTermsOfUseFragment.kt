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
package arcus.app.account.settings.terms

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import arcus.app.R
import arcus.app.common.adapters.IconizedChevronListAdapter
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.fragments.NoViewModelFragment
import arcus.app.common.fragments.WebViewFragment
import arcus.app.common.models.ListItemModel
import arcus.app.common.utils.GlobalSetting
import java.util.ArrayList

// TODO: This class was just converted to Kotlin and still needs significant work to update it.
class SettingsTermsOfUseFragment : NoViewModelFragment() {
    private lateinit var listView: ListView

    override val title: String
        get() = getString(R.string.account_settings_terms_of_use)
    override val layoutId: Int = R.layout.fragment_settings_terms_of_use

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById<View>(R.id.settings_terms_of_use_listview) as ListView
        loadContent()
    }

    private fun loadContent() {
        val data = ArrayList<ListItemModel>()
        data.add(
            ListItemModel(
                getString(R.string.settings_terms_conditions),
                getString(R.string.settings_terms_conditions_instr)
            )
        )
        data.add(ListItemModel(getString(R.string.settings_privacy), getString(R.string.settings_privacy_instr)))
        val listAdapter = IconizedChevronListAdapter(activity, data)
        listView.adapter = listAdapter
        listAdapter.notifyDataSetChanged()
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapter, v, position, arg3 ->
            val webViewFragment = WebViewFragment()
            val bundle = Bundle(1)
            when (position) {
                0 -> {
                    val tAndC = Uri.parse(GlobalSetting.T_AND_C_LINK)
                    val intent = Intent(Intent.ACTION_VIEW, tAndC)
                    if (intent.resolveActivity(activity!!.packageManager) != null) {
                        startActivity(intent)
                    } else {
                        bundle.putString(
                            WebViewFragment.KEY_ARGUMENT_URL,
                            GlobalSetting.T_AND_C_LINK
                        )
                        webViewFragment.arguments = bundle
                        BackstackManager.getInstance().navigateToFragment(webViewFragment, true)
                    }
                }
                1 -> {
                    val privacy = Uri.parse(GlobalSetting.PRIVACY_LINK)
                    val privacyIntent = Intent(Intent.ACTION_VIEW, privacy)
                    if (privacyIntent.resolveActivity(activity!!.packageManager) != null) {
                        startActivity(privacyIntent)
                    } else {
                        bundle.putString(
                            WebViewFragment.KEY_ARGUMENT_URL,
                            GlobalSetting.PRIVACY_LINK
                        )
                        webViewFragment.arguments = bundle
                        BackstackManager.getInstance().navigateToFragment(webViewFragment, true)
                    }
                }
                else -> {
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): SettingsTermsOfUseFragment =
            SettingsTermsOfUseFragment()
    }
}
