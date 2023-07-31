package io.jenkins.plugins.json_editor_choice;

import hudson.model.ParameterValue;
import java.io.Serializable;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class MapParameterValue extends ParameterValue {

    Map<String, ? extends Serializable> value;

    public MapParameterValue(String name, String description, Map<String, ? extends Serializable> value) {
        super(name, description);
        this.value = value;
    }
}
