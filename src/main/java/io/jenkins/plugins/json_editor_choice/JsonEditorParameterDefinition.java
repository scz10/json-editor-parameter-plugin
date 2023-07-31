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
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@EqualsAndHashCode(callSuper = true)
public class JsonEditorParameterDefinition extends ParameterDefinition {

    @Getter
    @Setter(onMethod_ = {@DataBoundSetter})
    @Accessors(chain = true)
    private String schema;

    @Getter
    @Setter(onMethod_ = {@DataBoundSetter})
    @Accessors(chain = true)
    private String options;

    @DataBoundConstructor
    public JsonEditorParameterDefinition(String name) {
        super(name);
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

        public FormValidation doCheckName(@QueryParameter String name) {
            if (OK_NAME.matcher(name).matches()) {
                return FormValidation.ok();
            }
            return FormValidation.error("Name should match regular expression [A-Za-z][\\w-]{0,63}");
        }

        public FormValidation doCheckScheme(@QueryParameter String scheme) {
            try {
                JSON.std.mapFrom(scheme);
                return FormValidation.ok();
            } catch (IOException e) {
                return FormValidation.error("Scheme must be valid json");
            }
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return "Json Editor Parameter Definition";
        }
    }
}
