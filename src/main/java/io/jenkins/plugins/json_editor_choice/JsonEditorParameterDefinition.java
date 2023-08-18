package io.jenkins.plugins.json_editor_choice;

import com.fasterxml.jackson.jr.ob.JSON;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

@EqualsAndHashCode(callSuper = true)
@Log
public class JsonEditorParameterDefinition extends ParameterDefinition {

    private static final Pattern OK_NAME = Pattern.compile("[A-Za-z][\\w-]{0,63}");

    private String schema;
    private String startval = "{}";
    private String options = "{}";

    @DataBoundConstructor
    public JsonEditorParameterDefinition(String name) {
        super(name);
        log.fine("JsonEditorParameterDefinition: " + name);
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Invalid Name - " + name);
        }
    }

    @SneakyThrows
    static String toJsonString(Map<String, Object> object) {
        return object == null || object.isEmpty() ? null : JSON.std.asString(object);
    }

    @SneakyThrows
    static Map<String, Object> toMap(String json) {
        return json == null || json.isEmpty() ? Map.of() : JSON.std.mapFrom(json);
    }

    private static void checkValidJson(String options, String errorMessage) {
        try {
            JSON.std.mapFrom(options);
        } catch (IOException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static boolean isValidName(String name) {
        return OK_NAME.matcher(name).matches();
    }

    public String getSchema() {
        log.fine("getSchema: " + schema);
        return schema;
    }

    @DataBoundSetter
    public void setSchema(String schema) {
        log.fine("setSchema: " + schema);
        checkValidJson(schema, "schema must be valid json");
        this.schema = schema;
    }

    public String getStartval() {
        log.fine("getStartval: " + startval);
        return startval;
    }

    @DataBoundSetter
    public void setStartval(String startval) {
        log.fine("setStartval: " + startval);
        checkValidJson(startval, "startval must be valid json");
        this.startval = startval;
    }

    public String getOptions() {
        log.fine("getOptions: " + options);
        return options;
    }

    @DataBoundSetter
    public void setOptions(String options) {
        log.fine("setOptions: " + options);
        checkValidJson(options, "options must be valid json");
        this.options = options;
    }

    @Restricted(DoNotUse.class) // index.jelly
    public String getMergedOptions() throws IOException {
        Map<String, Object> optionMap = new HashMap<>(toMap(options));
        optionMap.put("startval", toMap(startval));
        optionMap.put("schema", toMap(schema));
        return JSON.std.asString(optionMap);
    }

    @Override
    public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
        if (defaultValue instanceof JsonEditorParameterValue) {
            JsonEditorParameterDefinition def = new JsonEditorParameterDefinition(getName());
            def.setDescription(getDescription());
            JsonEditorParameterValue value = (JsonEditorParameterValue) defaultValue;
            def.setStartval(value.getValue());
            return def;
        } else {
            return this;
        }
    }

    @Override
    public ParameterValue createValue(StaplerRequest request, JSONObject jo) {
        log.info("createValue jo=" + jo);
        StringParameterValue value = request.bindJSON(StringParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    @Override
    public ParameterValue createValue(StaplerRequest request) {
        log.info("createValue request=" + request.getRequestURIWithQueryString());
        String[] value = request.getParameterValues(getName());
        if (value == null) {
            log.info("returning default null value");
            return getDefaultParameterValue();
        } else if (value.length != 1) {
            throw new IllegalArgumentException(
                    "Illegal number of parameter values for " + getName() + ": " + value.length);
        } else {
            log.info("returning " + Arrays.toString(value));
            return createValue(value[0]);
        }
    }

    @SneakyThrows
    public ParameterValue createValue(String str) {
        log.info("createValue " + str);
        Map<String, Object> map = JSON.std.mapFrom(str);
        return new JsonEditorParameterValue(getName(), getDescription(), map);
    }

    @Extension
    @Symbol({"jsonEditor"})
    public static class DescriptorImpl extends ParameterDescriptor {

        public DescriptorImpl() {
            log.fine("DescriptorImpl");
        }

        private static FormValidation isValidJson(String options, String errorMessage) {
            try {
                JSON.std.mapFrom(options);
                return FormValidation.ok();
            } catch (IOException e) {
                return FormValidation.error(errorMessage);
            }
        }

        @POST
        public FormValidation doCheckName(@QueryParameter String name) {
            log.fine("doCheckName " + name);
            return isValidName(name)
                    ? FormValidation.ok()
                    : FormValidation.error("Name should match regular expression [A-Za-z][\\w-]{0,63}");
        }

        @POST
        public FormValidation doCheckOptions(@QueryParameter String options) {
            log.fine("doCheckOptions " + options);
            return isValidJson(options, "options must be valid json");
        }

        @POST
        public FormValidation doCheckSchema(@QueryParameter String schema) {
            log.fine("doCheckSchema " + schema);
            return isValidJson(schema, "schema must be valid json");
        }

        @POST
        public FormValidation doCheckStartval(@QueryParameter String startval) {
            log.fine("doCheckStartval " + startval);
            return isValidJson(startval, "startval must be valid json");
        }

        @Override
        public JsonEditorParameterDefinition newInstance(StaplerRequest req, JSONObject formData) {
            log.fine("newInstance");
            log.fine("formData=" + formData);

            String name = formData.getString("name");
            JsonEditorParameterDefinition def = new JsonEditorParameterDefinition(name);

            def.setSchema(formData.getString("schema"));
            def.setStartval(formData.getString("startval"));
            def.setOptions(formData.getString("options"));

            return def;
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return "Json Editor Parameter Definition";
        }
    }
}
