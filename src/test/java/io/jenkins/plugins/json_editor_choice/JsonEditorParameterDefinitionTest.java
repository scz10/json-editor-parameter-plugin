package io.jenkins.plugins.json_editor_choice;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.ParametersDefinitionProperty;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

public class JsonEditorParameterDefinitionTest {
    private static final String FIRST_PARAM = "first";

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * Run a build from view with value.
     *
     * @param p job to run
     * @param paramName parameter name to set
     * @param paramValue parameter value to set
     * @throws Exception any exceptions
     */
    private void runBuildFromView(final Job<?, ?> p, final String paramName, final String paramValue) throws Exception {
        final WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        final HtmlPage page = wc.getPage(p, "build?delay=0sec");
        final HtmlElement paramBlock = page.querySelector(String.format("[data-parameter='%s']", paramName));
        final HtmlTextInput input = paramBlock.getOneHtmlElementByAttribute("input", "name", "value");
        input.setValueAttribute(paramValue);
        j.submit(page.getFormByName("parameters"));
        j.waitUntilNoActivity();
    }

    @Test
    public void configSimple() throws Exception {
        URL url = getClass().getResource("car.json");
        Assertions.assertNotNull(url);
        String schema = Files.readString(Paths.get(url.toURI()));

        final FreeStyleProject p = j.createFreeStyleProject();
        p.addProperty(
                new ParametersDefinitionProperty(new JsonEditorParameterDefinition(FIRST_PARAM).setSchema(schema)));

        j.configRoundtrip(p);
    }
}
