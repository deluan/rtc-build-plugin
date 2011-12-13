package com.deluan.jenkins.plugins.rtc.build;

import hudson.Plugin;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author deluan
 */
@SuppressWarnings("unused")

public class JazzBuildPlugin extends Plugin {
    private static final Logger logger = Logger.getLogger(JazzBuildPlugin.class.getName());
    private String buildToolkit;

    @Override
    public void start() throws Exception {
        super.start();
        load();
    }

    @Override
    public void configure(StaplerRequest req, JSONObject formData) throws IOException, ServletException, Descriptor.FormException {
        String oldBuildToolkit = buildToolkit;
        buildToolkit = Util.fixEmpty(req.getParameter("rtc.buildToolkit").trim());

        if (buildToolkit != null && !buildToolkit.equals(oldBuildToolkit)) {
            logger.info("BuildToolkit path changed to '" + buildToolkit + "'");
            JazzBuildToolFactory.clear();
        }

        save();
    }

    public String getBuildToolkit() {
        if (buildToolkit == null) {
            return StringUtils.EMPTY;
        } else {
            return buildToolkit;
        }
    }

    public FormValidation doCheckBuildtoolkit(@QueryParameter String value) {
        boolean ok = JazzBuildTool.checkForValidInstallation(value);

        if (ok) {
            return FormValidation.ok();
        } else {
            return FormValidation.error("Not a valid RTC build toolkit installation");
        }
    }


}
