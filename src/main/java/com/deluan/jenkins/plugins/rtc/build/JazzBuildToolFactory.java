package com.deluan.jenkins.plugins.rtc.build;

import com.deluan.jenkins.plugins.rtc.JazzConfiguration;
import com.deluan.jenkins.plugins.rtc.JazzSCM;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.scm.SCM;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author deluan
 */
public class JazzBuildToolFactory {
    private static final Logger logger = Logger.getLogger(JazzBuildToolFactory.class.getName());

    private Map<AbstractProject, JazzBuildTool> buildToolInstances = new HashMap<AbstractProject, JazzBuildTool>();
    private static final JazzBuildToolFactory INSTANCE = new JazzBuildToolFactory();

    private JazzBuildToolFactory() {
    }

    synchronized static JazzBuildTool getBuildToolFor(AbstractProject project) {
        JazzBuildTool buildTool = INSTANCE.buildToolInstances.get(project);

        if (buildTool == null) {
            logger.info("Creating JazzBuildTool for project " + project.getDisplayName());
            JazzConfiguration config = getConfiguration(project);

            String buildToolkitPath = getBuildToolkitPath();

            JazzBuildTrigger buildTrigger = getBuildTrigger(project);
            String buildEngineId = buildTrigger.getBuildEngineId();
            String buildDefinitionId = buildTrigger.getBuildDefinitionId();

            buildTool = new JazzBuildTool(config, buildToolkitPath, buildEngineId, buildDefinitionId);
            INSTANCE.buildToolInstances.put(project, buildTool);
        }

        return buildTool;
    }

    synchronized public static void releaseBuildTool(JazzBuildTool buildTool) {
        logger.info("Releasing JazzBuildTool " + buildTool);
        INSTANCE.buildToolInstances.values().remove(buildTool);
    }

    synchronized static void clear() {
        logger.info("Releasing ALL instances of JazzBuildTool");
        INSTANCE.buildToolInstances.clear();
    }

    private static String getBuildToolkitPath() {
        JazzBuildPlugin plugin = Hudson.getInstance().getPlugin(JazzBuildPlugin.class);
        if (plugin == null) {
            throw new IllegalStateException("RTC Build Plugin not found!");
        }
        return plugin.getBuildToolkit();
    }

    @SuppressWarnings("unchecked")
    private static JazzBuildTrigger getBuildTrigger(AbstractProject project) {
        JazzBuildTrigger buildTrigger = (JazzBuildTrigger) project.getTrigger(JazzBuildTrigger.class);
        if (buildTrigger == null) {
            throw new IllegalStateException("RTC Build Trigger not configured for " + project.getDisplayName());
        }
        return buildTrigger;
    }

    private static JazzConfiguration getConfiguration(AbstractProject project) {
        SCM scm = project.getScm();
        if (!(scm instanceof JazzSCM)) {
            throw new IllegalStateException("RTC Build Trigger used with a non RTC SCM. Please configure your project to use RTC SCM.");
        }
        return ((JazzSCM) scm).getConfiguration();
    }
}
