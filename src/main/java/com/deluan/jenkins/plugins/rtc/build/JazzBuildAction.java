package com.deluan.jenkins.plugins.rtc.build;

import hudson.model.InvisibleAction;

/**
 * Partial {@link hudson.model.Action} implementation that doesn't have any UI presence and
 * is used to store and pass the RTC "Build Result UUID" value between the
 * various elements which comprise the RTC plugin bundle.
 *
 * @author deluan
 */
public class JazzBuildAction extends InvisibleAction {
    private String requestId;
    private String buildResultId;

    public JazzBuildAction(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setBuildResultId(String buildResultId) {
        this.buildResultId = buildResultId;
    }

    public String getBuildResultId() {
        return buildResultId;
    }

}
