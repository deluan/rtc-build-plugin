package com.deluan.jenkins.plugins.rtc.build;

import antlr.ANTLRException;
import com.deluan.jenkins.plugins.rtc.JazzSCM;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.BuildableItem;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.scm.SCM;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author deluan
 */
@SuppressWarnings("unused")
public class JazzBuildTrigger extends Trigger<BuildableItem> implements Serializable {

    transient private JazzBuildTool buildTool;

    @DataBoundConstructor
    public JazzBuildTrigger() throws ANTLRException {
        super("* * * * *");
    }

    @Override
    public void run() {
        AbstractProject project = (AbstractProject) this.job;
        prepareBuildTool(project);

        if (!Hudson.getInstance().isQuietingDown() && project.isBuildable()) {
            String requestId = buildTool.getNextTeamBuildRequest();
            if (requestId != null) {
                project.scheduleBuild(0,
                        new JazzBuildTriggerCause(requestId),
                        new JazzBuildAction(requestId));
            }
        }
    }

    private void prepareBuildTool(AbstractProject project) {
        if (buildTool == null) {
            SCM scm = project.getScm();
            if (scm instanceof JazzSCM) {
                buildTool = JazzBuildToolFactory.getBuildToolFor((JazzSCM) scm);
            } else {
                throw new IllegalStateException("Jazz Build Trigger used on a non Jazz SCM");
            }
        }
    }

    public TriggerDescriptor getDescriptor() {
        return (TriggerDescriptor) Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension
    public static class ScriptTriggerDescriptor extends TriggerDescriptor {
        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Poll for RTC Build Requests";
        }

    }

}
