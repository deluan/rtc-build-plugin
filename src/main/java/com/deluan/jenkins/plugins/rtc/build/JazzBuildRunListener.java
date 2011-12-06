package com.deluan.jenkins.plugins.rtc.build;

import com.deluan.jenkins.plugins.rtc.JazzSCM;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.util.LogTaskListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author deluan
 */
@Extension
public class JazzBuildRunListener extends RunListener<Run> {
    private static final Logger logger = Logger.getLogger(JazzBuildRunListener.class.getName());

    @Override
    public void onStarted(Run run, TaskListener listener) {
        if (isJazzProject(run)) {
            if (getJazzAction(run) == null) {
                prepareTeamBuild(run);
            }
            notifyBuildStart(run);
        }
    }

    @Override
    public void onFinalized(Run run) {
        if (isJazzProject(run)) {
            publishBuildResults(run);
            notifyBuildComplete(run);
        }
    }

    private JazzBuildAction getJazzAction(Run run) {
        return run.getAction(JazzBuildAction.class);
    }

    protected JazzBuildTool getBuildTool(Run run) {
        AbstractProject project = (AbstractProject) run.getParent();
        JazzSCM scm = (JazzSCM) project.getScm();
        return JazzBuildToolFactory.getBuildToolFor(scm);
    }

    private boolean isJazzProject(Run run) {
        AbstractProject project = (AbstractProject) run.getParent();
        return (project.getScm() instanceof JazzSCM);
    }

    private void prepareTeamBuild(Run run) {
        JazzBuildTool buildTool = getBuildTool(run);
        JazzBuildAction jazzAction;

        String requestId = buildTool.requestTeamBuild();
        if (requestId != null) {
            jazzAction = new JazzBuildAction(requestId);
            run.addAction(jazzAction);
        } else {
            logger.log(Level.SEVERE, "Error requesting team build.");
        }
    }

    private void notifyBuildStart(Run run) {
        JazzBuildAction jazzAction = getJazzAction(run);
        JazzBuildTool buildTool = getBuildTool(run);

        String buildResultId = buildTool.startTeamBuild(jazzAction.getRequestId(), run.getFullDisplayName());
        jazzAction.setBuildResultId(buildResultId);
    }

    private void notifyBuildComplete(Run run) {
        JazzBuildAction jazzAction = getJazzAction(run);
        JazzBuildTool buildTool = getBuildTool(run);
        String buildResultId = jazzAction.getBuildResultId();
        String status = convertJenkinsResultToJazzStatus(run.getResult());
        buildTool.completeTeamBuild(buildResultId, status);
    }

    private void publishBuildResults(Run run) {
        JazzBuildAction jazzAction = getJazzAction(run);
        JazzBuildTool buildTool = getBuildTool(run);
        String buildResultId = jazzAction.getBuildResultId();
        try {
            EnvVars envVars = run.getEnvironment(new LogTaskListener(logger, Level.INFO));

            buildTool.logPublisher(buildResultId, "Full build log",
                    run.getLogFile().getAbsolutePath());
            buildTool.linkPublisher(buildResultId,
                    "Jenkins Build Result: " + run.getFullDisplayName(),
                    envVars.get("BUILD_URL"));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not publish build results: " + e.getMessage(), e);
        }

    }

    private String convertJenkinsResultToJazzStatus(Result result) {
        String status;
        if (result == Result.SUCCESS) {
            status = "OK";
        } else if (result == Result.UNSTABLE) {
            status = "WARNING";
        } else {
            status = "ERROR";
        }
        return status;
    }
}
