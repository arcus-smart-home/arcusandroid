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
package arcus.app.subsystems.rules

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View

import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import arcus.app.R
import arcus.app.account.settings.WalkthroughType
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.error.ErrorManager.`in` as errorIn
import arcus.app.common.fragments.CoreFragment
import arcus.app.common.popups.AlertPopup
import arcus.app.common.utils.PreferenceCache
import arcus.app.common.utils.PreferenceUtils
import arcus.app.subsystems.rules.adapters.RuleListAdapter
import arcus.presentation.common.view.ViewState
import arcus.presentation.rules.list.ListItem
import arcus.presentation.rules.list.RuleError
import arcus.presentation.rules.list.RuleListViewModel

class RuleListFragment : CoreFragment<RuleListViewModel>(), RuleListAdapter.ClickListener {
    private var isEditMode = false

    private lateinit var noRulesGroup: Group
    private lateinit var haveRulesGroup: Group
    private lateinit var ruleListAdapter: RuleListAdapter

    override val viewModelClass: Class<RuleListViewModel> = RuleListViewModel::class.java
    override val title: String
        get() = getString(R.string.rules_rules)
    override val layoutId: Int
        get() = R.layout.fragment_rule_list
    override val menuId: Int?
        get() = if (haveRulesGroup.isVisible) R.menu.menu_edit_done_toggle else null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noRulesGroup = view.findViewById(R.id.noRulesLayoutGroup)
        haveRulesGroup = view.findViewById(R.id.rulesLayoutGroup)

        val rulesList = view.findViewById<RecyclerView>(R.id.rulesList)
        ruleListAdapter = RuleListAdapter(this)
        rulesList.adapter = ruleListAdapter

        viewModel = ViewModelProviders.of(this).get(RuleListViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        if (!PreferenceCache.getInstance().getBoolean(PreferenceUtils.RULES_WALKTHROUGH_DONT_SHOW_AGAIN, false)) {
            val rules = WalkthroughBaseFragment.newInstance(WalkthroughType.RULES)
            BackstackManager.getInstance().navigateToFloatingFragment(rules, rules.javaClass.name, true)
        }

        viewModel
                .viewState
                .observe(viewLifecycleOwner, Observer {
                    when (it) {
                        is ViewState.Loaded -> handleLoaded(it.item)
                        is ViewState.Error<*, *> -> handleError(it)
                    }
                })
    }

    private fun handleLoaded(items: List<ListItem>) {
        if (items.isEmpty()) {
            noRulesGroup.isVisible = true
            haveRulesGroup.isGone = true
        } else {
            noRulesGroup.isGone = true
            haveRulesGroup.isVisible = true

            ruleListAdapter.submitList(items)
            activity?.invalidateOptionsMenu()
        }
    }

    private fun handleError(viewError: ViewState.Error<*, *>) {
        when (viewError.errorType) {
            RuleError.INVALID -> {
                val popup = AlertPopup.newInstance(
                        "",
                        getString(R.string.rules_cannot_enable_error),
                        null,
                        null,
                        object : AlertPopup.DefaultAlertButtonCallback() {
                            override fun close() {
                                BackstackManager.getInstance().navigateBack()
                                viewModel.refreshRules()
                            }
                        }).apply { isCloseButtonVisible = true }
                BackstackManager
                        .getInstance()
                        .navigateToFloatingFragment(
                                popup,
                                popup.javaClass.simpleName,
                                true
                        )
            }
            else -> errorIn(activity).showGenericBecauseOf(viewError.error)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.menu_edit_done)?.let { item ->
            item.title = if (isEditMode) getString(R.string.card_menu_done) else getString(R.string.card_menu_edit)
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (progressContainer.isVisible) return false // Processing an action, ignore event.
        isEditMode = !isEditMode
        ruleListAdapter.isEditMode = isEditMode
        item.title = if (isEditMode) resources.getString(R.string.card_menu_done) else resources.getString(R.string.card_menu_edit)
        return true
    }

    override fun onRuleCheckboxClicked(item: ListItem.Rule) {
        if (isEditMode) viewModel.deleteRule(item) else viewModel.toggleRuleEnabled(item)
    }

    override fun onRuleItemClick(item: ListItem.Rule) {
        if (!isEditMode) {
            val fragment = with(item) {
                RuleEditorWithSchedulerFragment.newInstance(title, templateId, ruleAddress)
            }
            BackstackManager.getInstance().navigateToFragment(fragment, true)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): RuleListFragment = RuleListFragment()
    }
}
