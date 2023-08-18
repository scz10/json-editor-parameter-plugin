package io.jenkins.plugins.json_editor_choice;

import hudson.model.ParameterValue;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;

@EqualsAndHashCode(callSuper = true)
@Log
public class JsonEditorParameterValue extends ParameterValue {

    private Map<String, Object> value;

    public JsonEditorParameterValue(String name, String description, Map<String, Object> value) {
        super(name, description);
        this.value = value;
    }

    public void setValue(String value) {
        log.info("setValue " + value);
        this.value = JsonEditorParameterDefinition.toMap(value);
    }

    @Override
    public String getValue() {
        String json = JsonEditorParameterDefinition.toJsonString(value);
        log.info("getValue " + json);
        return json;
    }
}
