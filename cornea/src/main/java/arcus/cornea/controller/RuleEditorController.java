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
package arcus.cornea.controller;

import androidx.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.events.TimeSelectedEvent;
import arcus.cornea.model.RuleConversionUtils;
import arcus.cornea.model.RuleDisplayModel;
import arcus.cornea.model.RuleEditorCallbacks;
import arcus.cornea.model.SelectorType;
import arcus.cornea.model.TemplateTextField;
import arcus.cornea.model.editors.ShowEditor;
import arcus.cornea.model.editors.ShowTupleEditor;
import arcus.cornea.model.editors.ShowNonIdentifierEditor;
import arcus.cornea.model.editors.ShowModelListEditor;
import arcus.cornea.model.editors.ShowTextEditor;
import arcus.cornea.model.editors.ShowUnknownEditor;
import arcus.cornea.provider.RuleModelProvider;
import arcus.cornea.utils.AddressableModelSource;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientEvent;
import com.iris.client.IrisClient;
import com.iris.client.capability.Device;
import com.iris.client.capability.Person;
import com.iris.client.capability.Rule;
import com.iris.client.capability.RuleTemplate;
import com.iris.client.capability.Scene;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.RuleModel;
import com.iris.client.model.RuleTemplateModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RuleEditorController {
    private static final Logger logger = LoggerFactory.getLogger(RuleEditorController.class);
    private static final RuleEditorController INSTANCE;
    private static final String TYPE_KEY = "type";
    private static final String RULE_TMPL_PREFIX = "SERV:" + RuleTemplate.NAMESPACE + ":";
    private static final String RULE_NAMESPACE_PREFIX = "SERV:" + Rule.NAMESPACE + ":";
    private static final String PERSON_PREFIX = "SERV:" + Person.NAMESPACE + ":";
    private static final String SCENE_PREFIX = "SERV:" + Scene.NAMESPACE + ":";
    private static final String DEVICE_PREFIX = "DRIV:" + Device.NAMESPACE + ":";

    static {
        INSTANCE = new RuleEditorController(CorneaClientFactory.getClient());
    }

    private IrisClient client;
    private String ruleTemplateAddress;
    private ClientFuture<ClientEvent> operation;
    private Map<String, ShowEditor> editors;
    private Map<String, Map<String, String>> optionsLookup;
    private Map<String, Object> values;
    private String currentFieldEditing;
    private String title;
    private String description;
    private ListenerRegistration modelAddedListener;

    private RuleConversionUtils ruleConversionUtils;
    private AddressableModelSource<RuleModel> existingRule = CachedModelSource.newSource();
    private AddressableModelSource<RuleTemplateModel> addressableModelSource = CachedModelSource.newSource();
    private Optional<RuleDisplayModel> modelRef = Optional.absent();
    private WeakReference<RuleEditorCallbacks> callbackRef = new WeakReference<>(null);
    private Listener<ClientEvent> successListener = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            onResolveSuccess(new RuleTemplate.ResolveResponse(clientEvent));
        }
    });
    private Listener<Throwable> failureListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onResolveFailed(throwable);
        }
    });
    private Listener<ClientEvent> saveSuccessListener = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            saveSuccessAction();
        }
    });
    private Listener<Rule.UpdateContextResponse> updateRuleSuccessListener = Listeners.runOnUiThread(new Listener<Rule.UpdateContextResponse>() {
        @Override
        public void onEvent(Rule.UpdateContextResponse updateContextResponse) {
            saveSuccessAction();
        }
    });

    RuleEditorController(IrisClient client) {
        this.client = client;
        this.addressableModelSource.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent modelEvent) {
                if(modelEvent instanceof ModelAddedEvent) {
                    updateView();
                }
            }
        }));

        ruleConversionUtils = new RuleConversionUtils();
    }

    public static RuleEditorController getInstance() {
        return INSTANCE;
    }

    public void select(String ruleTemplateAddress, @Nullable String existingRuleAddress, RuleEditorCallbacks callback) {
        if (Strings.isNullOrEmpty(ruleTemplateAddress)) {
            logger.error("Cannot process a null/empty address. [{}]", ruleTemplateAddress);
            return;
        }

        ruleTemplateAddress = getRuleTemplateAddress(ruleTemplateAddress);
        if (!ruleTemplateAddress.equals(this.ruleTemplateAddress)) {
            logger.debug("Resetting -- Previous template: [{}], This template [{}]", this.ruleTemplateAddress, ruleTemplateAddress);
            reset();
            this.ruleTemplateAddress = ruleTemplateAddress;
        }

        setExistingRule(existingRuleAddress);
        setCallbacks(callback);
        load();
        updateView();
    }

    private void setCallbacks(RuleEditorCallbacks callbacks) {
        if (callbackRef.get() != null) {
            logger.info("Replacing existing RuleEditorCallbacks.");
        }
        this.callbackRef = new WeakReference<>(callbacks);
    }

    protected String getRuleTemplateAddress(String ruleTemplateAddress) {
        if (!ruleTemplateAddress.startsWith(RULE_TMPL_PREFIX)) {
            ruleTemplateAddress = RULE_TMPL_PREFIX + ruleTemplateAddress;
        }

        return ruleTemplateAddress;
    }

    protected void setExistingRule(String existingRuleAddress) {
        if (!Strings.isNullOrEmpty(existingRuleAddress)) {
            if (!existingRuleAddress.startsWith(RULE_NAMESPACE_PREFIX)) {
                existingRuleAddress = RULE_NAMESPACE_PREFIX + existingRuleAddress;
            }

            if (!existingRuleAddress.equals(existingRule.getAddress())) {
                existingRule.setAddress(existingRuleAddress);
                if (existingRule.get() != null && existingRule.get().getContext() != null) {
                    values.putAll(existingRule.get().getContext());
                }
            }
        }
    }

    public void edit(String field) {
        RuleEditorCallbacks callbacks = callbackRef.get();
        if (Strings.isNullOrEmpty(field) || callbacks == null) {
            logger.error("Callbacks/Field are null/empty. Cannot edit.");
            return;
        }

        ShowEditor editor = editors.get(field);
        if(editor == null) {
            logger.warn("The selected field is not editable.");
            return;
        }

        this.currentFieldEditing = field;
        editor.show(callbacks);
    }

    public void save() {
        logger.debug("Saving/updating rule using CONTEXT of: [{}]", values);

        if (existingRule.get() != null) {
            updateExistingRule();
        }
        else {

            RuleEditorCallbacks callbacks = callbackRef.get();
            if (callbacks != null){
                callbacks.showScheduleDialog();
            }

        }
    }

    private void saveSuccessAction() {
        RuleEditorCallbacks callbacks = callbackRef.get();
        if (callbacks != null) {
            callbacks.saveSuccess();
        }

        reset();
    }

    private void updateExistingRule() {
        RuleEditorCallbacks callbacks = callbackRef.get();
        if (callbacks != null) {
            callbacks.showLoading();
        }

        String newName = Strings.isNullOrEmpty(title) ? existingRule.get().getName() : title;
        String newDescription = Strings.isNullOrEmpty(description) ? existingRule.get().getDescription() : description;
        existingRule.get().setName(newName);
        existingRule.get().setDescription(newDescription);
        existingRule.get()
              .commit()
              .onSuccess(new Listener<ClientEvent>() {
                  @Override
                  public void onEvent(ClientEvent clientEvent) {
                      existingRule.get().updateContext(values, null).onSuccess(updateRuleSuccessListener).onFailure(failureListener);
                  }
              })
              .onFailure(failureListener);
    }



    protected void removeModelAddedListener() {
        if (modelAddedListener != null && modelAddedListener.isRegistered()) {
            modelAddedListener.remove();
        }
    }

    public void createNewRule(final String strState ) {


        RuleEditorCallbacks callbacks = callbackRef.get();
        if (callbacks != null) {
            callbacks.showLoading();
        }

        RuleTemplate.CreateRuleRequest request = new RuleTemplate.CreateRuleRequest();
        request.setAddress(ruleTemplateAddress);
        request.setPlaceId(client.getActivePlace().toString());
        request.setContext(values);

        if (!Strings.isNullOrEmpty(title)) {
            request.setName(title);
        }
        else {
            request.setName(addressableModelSource.get().getName());
        }

        if (!Strings.isNullOrEmpty(description)) {
            request.setDescription(description);
        }
        else {
            request.setDescription(addressableModelSource.get().getDescription());
        }

       removeModelAddedListener();
        modelAddedListener = RuleModelProvider.instance().getStore().addListener(ModelAddedEvent.class, new Listener<ModelAddedEvent>() {
            @Override
            public void onEvent(ModelAddedEvent modelAddedEvent) {
                RuleModel model = (RuleModel) modelAddedEvent.getModel();
                    RuleEditorCallbacks callbacks = callbackRef.get();
                    if (callbacks != null){
                        callbacks.allowScheduling(strState, model.getAddress(), model.getName());
                    }
            }
        });

        client.request(request)
              .onSuccess(saveSuccessListener)
              .onFailure(failureListener);
        logger.info("Sending request to save rule: [{}]", request);
    }

    public String getSelectedValue() {
        return String.valueOf(values.get(this.currentFieldEditing));
    }

    public void set(String value) {
        values.put(currentFieldEditing, doConversionForSaving(value));
        replaceTemplateParts(doConversionForEditing(value));
        updateView();
    }

    public void set(TimeSelectedEvent event) {
        ShowEditor editor = editors.get(currentFieldEditing);
        if (editor == null || !(editor instanceof ShowNonIdentifierEditor)) {
            set(event.getAsTime());
            return;
        }

        String newValue =
              ruleConversionUtils.convertStringToContextSaveTime(
                    event,
                    values.get(currentFieldEditing),
                    ((ShowNonIdentifierEditor)editor).getType());
        set(newValue);
    }

    private void replaceTemplateParts(String value) {
        RuleDisplayModel ruleDisplayModel = modelRef.get();
        for (TemplateTextField field : modelRef.get().getTemplateTextFields()) {
            if (field.getFieldName().equalsIgnoreCase(currentFieldEditing)) {
                // Only translate items that have a different internal value than then one we show the user.
                if (optionsLookup.get(currentFieldEditing) != null) {
                    String replacementValue = optionsLookup.get(currentFieldEditing).get(value);
                    if (!Strings.isNullOrEmpty(replacementValue)) {
                        field.setText(replacementValue);
                        ruleDisplayModel.edited(currentFieldEditing);
                    }
                }
                else if (values.containsKey(currentFieldEditing)) {
                    field.setText(value);
                    ruleDisplayModel.edited(currentFieldEditing);
                }
                else {
                    logger.debug("Tried to replace [{}] with [{}], but have no conversion or value set.", currentFieldEditing, value);
                }
            }
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() { return this.title; }

    public String getDescription() { return this.description; }

    /**
     *
     * Convert date/timestamps that may be displayed to the user one way, but need to be sent to the server in another.
     *
     * @param value
     * @return
     */
    private String doConversionForSaving(String value) {
        ShowEditor editor = editors.get(currentFieldEditing);
        if (editor instanceof ShowNonIdentifierEditor) {
            switch (((ShowNonIdentifierEditor) editor).getType()) {
                case DURATION:
                    String[] hhMM = value.split(":");
                    if (hhMM.length == 2) {
                        int hoursAsSeconds = Integer.valueOf(hhMM[0]) * 3600;
                        int minutesAsSeconds = Integer.valueOf(hhMM[1]) * 60;
                        value = String.valueOf(hoursAsSeconds + minutesAsSeconds);
                    }
                    break;
            }
        }

        return value;
    }

    /**
     *
     * Convert date/timestamps that may be displayed to the user one way, but need to be sent to the server in another.
     *
     * @param value
     * @return
     */
    private String doConversionForEditing(String value) {
        ShowEditor editor = editors.get(currentFieldEditing);
        if (editor instanceof ShowNonIdentifierEditor) {
            switch (((ShowNonIdentifierEditor) editor).getType()) {
                case DURATION:
                    String[] hhMM = value.split(":");
                    if (hhMM.length == 2) {
                        return ruleConversionUtils.getDurationFromHourMinute(hhMM[0], hhMM[1]);
                    }
                    else if (value.equals("0")) { // Indefinite selected.
                        return ruleConversionUtils.getDurationForInfinity(value);
                    }
                    break;
                case TIME_RANGE:
                    return ruleConversionUtils.convertStringToDisplayTime(value);
            }
        }

        return value;
    }

    public void reset() {
        modelRef = Optional.absent();
        ruleTemplateAddress = null;
        currentFieldEditing = null;
        operation = null;
        editors = new HashMap<>();
        values = new HashMap<>();
        optionsLookup = new HashMap<>();
        title = null;
        description = null;
        existingRule = CachedModelSource.newSource();
    }

    protected void load() {
        if(operation != null) {
            return;
        }

        if(Strings.isNullOrEmpty(ruleTemplateAddress)) {
            logger.warn("Unable to show view because no template is selected");
            return;
        }

        RuleTemplate.ResolveRequest request = new RuleTemplate.ResolveRequest();
        request.setAddress(ruleTemplateAddress);
        request.setPlaceId(client.getActivePlace().toString());
        operation = client.request(request)
              .onSuccess(successListener)
              .onFailure(failureListener);
        addressableModelSource.setAddress(ruleTemplateAddress);
    }

    protected void onResolveSuccess(RuleTemplate.ResolveResponse response) {
        modelRef = Optional.of(getDisplayModel());
        editors = buildEditors(response);
        updateView();
    }

    protected void onResolveFailed(Throwable cause) {
        RuleEditorCallbacks callback = callbackRef.get();
        if(callback == null) {
            return;
        }

        callback.errorOccurred(cause);
    }

    protected RuleDisplayModel getDisplayModel () {
        List<TemplateTextField> templateTextFields = new ArrayList<>();

        // Split template on '${' '}' tokens and walk each token
        String[] tokens = addressableModelSource.get().getTemplate().split("\\$\\{|\\}");
        for (int index = 0; index < tokens.length; index++) {
            String token = tokens[index];

            if (token.length() == 0)
                continue;

            // Even numbered tokens are the uneditable strings
            if (index % 2 == 0) {
                templateTextFields.add(new TemplateTextField(token, token, false));
            }

            // Odd numbered tokens are the editable ones
            else {
                String displayText = getTemplateFieldDisplayText(token);
                templateTextFields.add(new TemplateTextField(displayText, token, true));
            }
        }

        return new RuleDisplayModel(templateTextFields);
    }

    private String getTemplateFieldDisplayText(String templateName) {
        return templateName.replaceAll("_", " ");
    }

    protected void updateView() {
        RuleEditorCallbacks callback = callbackRef.get();
        if(callback == null) {
            return;
        }

        updateView(callback);
    }

    protected void updateView(RuleEditorCallbacks callback) {
        RuleDisplayModel model = modelRef.orNull();
        if(model == null) {
            callback.showLoading();
        }
        else {
            if (Boolean.TRUE.equals(addressableModelSource.get().getSatisfiable())) {
                callback.showEditable(model);
            }
            else {
                callback.showUnavailable(model);
            }
        }
    }

    protected Map<String, ShowEditor> buildEditors(RuleTemplate.ResolveResponse response) {
        if (response == null || response.getSelectors() == null) {
            logger.error("Response, or selectors, were null. Returning empty map.");
            return Collections.emptyMap();
        }

        Map<String, ShowEditor> editorMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> item : response.getSelectors().entrySet()) {
            editorMap.put(item.getKey(), buildEditor(item.getKey(), item.getValue()));
        }
        return editorMap;
    }

    @SuppressWarnings({"unchecked"})
    protected ShowEditor buildEditor(String fieldName, Map<String, Object> fieldDefinition) {
        SelectorType selectorType = SelectorType.fromString(String.valueOf(fieldDefinition.get(TYPE_KEY)));
        switch (selectorType) {
            case LIST:
                try {
                    List<List<String>> options = (List<List<String>>) fieldDefinition.get("options");
                    LinkedHashMap<String, String> optionsLookupAddition = ruleConversionUtils.convertOptionsMap(options);

                    optionsLookup.put(fieldName, optionsLookupAddition);
                    if (values.containsKey(fieldName)) {
                        this.currentFieldEditing = fieldName;
                        replaceTemplateParts(String.valueOf(values.get(fieldName)));
                    }
                    else if (options != null && options.size() == 1) {
                        this.currentFieldEditing = fieldName;
                        values.put(fieldName, options.get(0).get(1));
                        replaceTemplateParts(options.get(0).get(1));
                    }
                    List<String> values = new ArrayList<>(optionsLookupAddition.keySet());
                    if (options == null || (options.isEmpty() || options.get(0).isEmpty())) {
                        return new ShowUnknownEditor();
                    }
                    else if (options.get(0).get(1).startsWith(DEVICE_PREFIX)) {
                        return new ShowModelListEditor(values);
                    }
                    else if (options.get(0).get(1).startsWith(PERSON_PREFIX)) {
                        RuleDisplayModel model = modelRef.get();
                        if (model != null) {
                            model.setFieldAsProperName(fieldName);
                        }

                        return new ShowModelListEditor(values);
                    }
                    else if (options.get(0).get(1).startsWith(SCENE_PREFIX)) {
                        return new ShowModelListEditor(values);
                    }
                    else {
                        return new ShowTupleEditor(options);
                    }
                }
                catch (Exception ex) {
                    logger.debug("Caught exception trying to parse the first item in the list of values.", ex);
                    return new ShowUnknownEditor();
                }

            case DURATION:
                this.currentFieldEditing = fieldName;
                String timeReplacement = ruleConversionUtils.getDurationStringForDisplay(values.get(fieldName));
                if (!Strings.isNullOrEmpty(timeReplacement)) {
                    replaceTemplateParts(timeReplacement);
                }
                return new ShowNonIdentifierEditor(selectorType);

            case TIME_OF_DAY:
                return new ShowNonIdentifierEditor(selectorType);

            case TIME_RANGE:
                this.currentFieldEditing = fieldName;
                String replacementValue = ruleConversionUtils.convertStringToDisplayTime(values.get(fieldName));
                if (!Strings.isNullOrEmpty(replacementValue)) {
                    replaceTemplateParts(replacementValue);
                }
                return new ShowNonIdentifierEditor(selectorType);

            case TEXT:
                return new ShowTextEditor();

            case DAY_OF_WEEK: // Ignored, should be moving this to an option outside of the tmpls
            case UNKNOWN:
            default:
                logger.warn("Unknown selector type for [{}] will do nothing.", selectorType);
                break;
        }

        return new ShowUnknownEditor();
    }

    public AddressableModelSource<RuleModel> getAddressableModelSource() {
        return existingRule;
    }



}
