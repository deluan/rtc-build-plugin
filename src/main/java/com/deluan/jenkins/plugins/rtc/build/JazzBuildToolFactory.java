package com.deluan.jenkins.plugins.rtc.build;

import com.deluan.jenkins.plugins.rtc.JazzSCM;

import java.util.HashMap;
import java.util.Map;

/**
 * @author deluan
 */
public class JazzBuildToolFactory {
    private Map<JazzSCM, JazzBuildTool> buildToolInstances = new HashMap<JazzSCM, JazzBuildTool>();
    private static final JazzBuildToolFactory INSTANCE = new JazzBuildToolFactory();

    private JazzBuildToolFactory() {
    }

    synchronized static JazzBuildTool getBuildToolFor(JazzSCM scmInstance) {
        JazzBuildTool buildTool = INSTANCE.buildToolInstances.get(scmInstance);

        if (buildTool == null) {
            buildTool = new JazzBuildTool(scmInstance.getConfiguration());
            INSTANCE.buildToolInstances.put(scmInstance, buildTool);
        }

        return buildTool;
    }
}
