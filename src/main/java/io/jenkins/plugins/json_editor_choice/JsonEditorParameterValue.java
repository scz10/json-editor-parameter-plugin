package io.jenkins.plugins.json_editor_choice;

import hudson.model.ParameterValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class JsonEditorParameterValue extends ParameterValue {

    private String json;

    public JsonEditorParameterValue(String name, String description, String json) {
        super(name, description);
        this.json = json;
    }
}
