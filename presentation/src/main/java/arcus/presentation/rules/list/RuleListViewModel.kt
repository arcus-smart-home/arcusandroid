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
package arcus.presentation.rules.list

import arcus.cornea.CorneaClientFactory
import arcus.cornea.helpers.transformNonNull
import arcus.cornea.provider.RuleModelProvider
import arcus.cornea.provider.RuleTemplateModelProvider
import arcus.cornea.provider.SchedulerModelProvider
import arcus.presentation.common.view.ViewError
import arcus.presentation.common.view.ViewState
import arcus.presentation.common.view.ViewStateViewModel
import com.iris.capability.util.Addresses
import com.iris.client.capability.Rule
import com.iris.client.capability.RuleTemplate
import com.iris.client.exception.ErrorResponseException
import com.iris.client.model.ModelCache
import com.iris.client.model.RuleModel
import com.iris.client.model.RuleTemplateModel
import com.iris.client.model.SchedulerModel
import java.util.TreeMap

// TODO: @Inject...
class RuleListViewModel constructor(
    private val ruleProvider: RuleModelProvider = RuleModelProvider.instance(),
    private val templateProvider: RuleTemplateModelProvider = RuleTemplateModelProvider.instance(),
    private val modelCache: ModelCache = CorneaClientFactory.getModelCache(),
    private val schedulerModelProvider: SchedulerModelProvider = SchedulerModelProvider.instance()
) : ViewStateViewModel<List<ListItem>>() {
    fun refreshRules() {
        loadData()
    }

    override fun loadData() {
        _viewState.postValue(ViewState.Loading())
        templateProvider
                .load()
                .chain { schedulerModelProvider.load() }
                .transform { schedules -> schedules?.map { it.target to it }?.toMap() ?: emptyMap() }
                .chain { schedulers ->
                    ruleProvider
                            .load()
                            .transformNonNull { rules ->
                                Pair(rules, schedulers ?: emptyMap<String, SchedulerModel>())
                            }
                }
                .transformNonNull { (ruleModels, schedulers) ->
                    val ruleMappings = TreeMap<String, MutableList<RuleModel>>()
                    val scheduleMappings = mutableMapOf<String, Boolean>()

                    ruleModels.forEach { ruleModel ->
                        val model = modelCache[ruleModel.templateAddress()] as RuleTemplateModel?
                        val categories = model?.categories ?: emptyList<String>()
                        categories.forEach { category ->
                            ruleMappings.getOrPut(category) { mutableListOf() }.add(ruleModel)
                        }
                        scheduleMappings[ruleModel.address] = schedulers[ruleModel.address]?.commands?.isNotEmpty()
                                ?: false
                    }

                    ruleMappings.values.forEach { list ->
                        list.sortBy { it.name.orEmpty() }
                    }

                    Pair(ruleMappings, scheduleMappings)
                }
                .transform { pair ->
                    val (ruleMappings, scheduleMappings) = pair!!
                    val listItems = mutableListOf<ListItem>()
                    ruleMappings
                            .forEach { (category, rules) ->
                                listItems.add(ListItem.Header(category, rules.size))

                                rules.mapTo(listItems) {
                                    ListItem.Rule(
                                            it.name,
                                            it.description,
                                            scheduleMappings[it.address] ?: false,
                                            it.address,
                                            it.template,
                                            Rule.STATE_ENABLED == it[Rule.ATTR_STATE]
                                    )
                                }
                            }

                    listItems
                }
                .onFailure {
                    if (it is ErrorResponseException && "request.invalid" == it.code) {
                        _viewState.postValue(ViewState.Error(it, RuleError.INVALID))
                    } else {
                        _viewState.postValue(ViewState.Error(it, ViewError.GENERIC))
                    }
                }
                .onSuccess { _viewState.postValue(ViewState.Loaded(it)) }
    }

    fun toggleRuleEnabled(item: ListItem.Rule) {
        withRuleFrom(item) {
            if (item.isEnabled) {
                disable()
            } else {
                enable()
            }.onSuccess { loadData() }
        }
    }

    fun deleteRule(item: ListItem.Rule) {
        withRuleFrom(item) { delete().onSuccess { loadData() } }
    }

    private fun withRuleFrom(item: ListItem.Rule, action: RuleModel.() -> Unit) {
        val rule = modelCache.get(item.ruleAddress) as? RuleModel?
        if (rule == null) {
            _viewState.value = ViewState.Error(
                    RuntimeException("Model could not be found."),
                    ViewError.GENERIC
            )
        } else {
            _viewState.value = ViewState.Loading()
            rule.action()
        }
    }

    private fun RuleModel.templateAddress() = Addresses.toObjectAddress(RuleTemplate.NAMESPACE, template)
}
