package com.deluan.jenkins.plugins.rtc.build;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.BuildableItem;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;

/**
 * @author deluan
 */
//@SuppressWarnings("unused")
public class JazzBuildTrigger extends Trigger<BuildableItem> implements Serializable {

    transient private JazzBuildTool buildTool;

    private String buildEngineId;
    private String buildDefinitionId;

    @DataBoundConstructor
    public JazzBuildTrigger(String buildDefinitionId, String buildEngineId) throws ANTLRException {
        super("* * * * *");
        this.buildDefinitionId = buildDefinitionId;
        this.buildEngineId = buildEngineId;

    }

    @Override
    public void stop() {
        if (buildTool != null) {
            JazzBuildToolFactory.releaseBuildTool(buildTool);
        }
    }

    @Override
    public void run() {
        AbstractProject project = (AbstractProject) this.job;
        buildTool = JazzBuildToolFactory.getBuildToolFor(project);

        if (!Hudson.getInstance().isQuietingDown() && project.isBuildable()) {
            String requestId = buildTool.getNextTeamBuildRequest();
            if (requestId != null) {
                project.scheduleBuild(0,
                        new JazzBuildTriggerCause(requestId),
                        new JazzBuildAction(requestId));
            }
        }
    }

    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    public String getBuildEngineId() {
        return buildEngineId;
    }

    public String getBuildDefinitionId() {
        return buildDefinitionId;
    }

    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Poll for RTC Build Requests";
        }

        public FormValidation doCheckBuildDefinitionId(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckBuildEngineId(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }
    }
}
