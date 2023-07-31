package io.jenkins.plugins.json_editor_choice;

import com.fasterxml.jackson.jr.ob.JSON;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@EqualsAndHashCode(callSuper = true)
@Getter
public class JsonEditorParameterDefinition extends ParameterDefinition {
    private static final String EMPTY_MAP = "{}";

    private String options = EMPTY_MAP;
    private String schema = EMPTY_MAP;
    private String startval = EMPTY_MAP;

    @DataBoundConstructor
    public JsonEditorParameterDefinition(String name) {
        super(name);
    }

    private static String replaceEmpty(String json) {
        return json == null || json.isEmpty() ? EMPTY_MAP : json;
    }

    @DataBoundSetter
    public JsonEditorParameterDefinition setSchema(String schema) {
        this.schema = replaceEmpty(schema);
        return this;
    }

    @DataBoundSetter
    public JsonEditorParameterDefinition setOptions(String options) {
        this.options = replaceEmpty(options);
        return this;
    }

    @DataBoundSetter
    public JsonEditorParameterDefinition setStartVal(String startval) {
        this.startval = replaceEmpty(startval);
        return this;
    }

    public String getMergedOptions() throws IOException {
        JSON std = JSON.std;
        Map<String, Object> optionMap = std.mapFrom(options);
        optionMap.put("startval", std.mapFrom(startval));
        optionMap.put("schema", std.mapFrom(schema));
        return std.asString(optionMap);
    }

    @Override
    public ParameterValue createValue(StaplerRequest request, JSONObject jo) {
        StringParameterValue value = request.bindJSON(StringParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    @Override
    public ParameterValue createValue(StaplerRequest request) {
        String[] value = request.getParameterValues(getName());
        if (value == null) {
            return getDefaultParameterValue();
        } else if (value.length != 1) {
            throw new IllegalArgumentException(
                    "Illegal number of parameter values for " + getName() + ": " + value.length);
        } else {
            return createValue(value[0]);
        }
    }

    @SneakyThrows
    public ParameterValue createValue(String str) {
        Map map = JSON.std.mapFrom(str);
        return new MapParameterValue(getName(), getDescription(), map);
    }

    @Extension
    @Symbol({"jsonEditor"})
    public static class DescriptorImpl extends ParameterDescriptor {

        private static final Pattern OK_NAME = Pattern.compile("[A-Za-z][\\w-]{0,63}");

        private static FormValidation isValidJson(String options, String errorMessage) {
            try {
                JSON.std.mapFrom(options);
                return FormValidation.ok();
            } catch (IOException e) {
                return FormValidation.error(errorMessage);
            }
        }

        public FormValidation doCheckName(@QueryParameter String name) {
            if (OK_NAME.matcher(name).matches()) {
                return FormValidation.ok();
            }
            return FormValidation.error("Name should match regular expression [A-Za-z][\\w-]{0,63}");
        }

        public FormValidation doCheckOptions(@QueryParameter String options) {
            return isValidJson(options, "options must be valid json");
        }

        public FormValidation doCheckScheme(@QueryParameter String scheme) {
            return isValidJson(scheme, "scheme must be valid json");
        }

        public FormValidation doCheckStartVal(@QueryParameter String startVal) {
            return isValidJson(startVal, "startVal must be valid json");
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return "Json Editor Parameter Definition";
        }
    }
}
