package io.jenkins.plugins.json_editor_choice;

import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;

/**
 * Explicitly declare methods that Jelly will invoke
 */
public interface DescriptorChecks {
    FormValidation doCheckName(@QueryParameter String name);

    FormValidation doCheckOptions(@QueryParameter String options);

    FormValidation doCheckSchema(@QueryParameter String schema);

    FormValidation doCheckStartval(@QueryParameter String startval);
}
