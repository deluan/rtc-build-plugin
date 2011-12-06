package com.deluan.jenkins.plugins.rtc.build;

import hudson.model.Cause;

public class JazzBuildTriggerCause extends Cause {
    private String requestId;

    public JazzBuildTriggerCause(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String getShortDescription() {
        return "A build request was issued by RTC. RequestID = (" + requestId + ")";
    }
}

