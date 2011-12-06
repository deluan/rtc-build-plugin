package com.deluan.jenkins.plugins.rtc.build;

import com.deluan.jenkins.plugins.rtc.JazzSCM;
import hudson.EnvVars;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author deluan
 */
@SuppressWarnings("unchecked")
public class JazzBuildRunListenerTest {

    @Mock
    Run run;
    @Mock
    EnvVars envVars;
    @Mock
    JazzBuildAction jazzAction;
    @Mock
    JazzBuildTool buildTool;
    @Mock
    AbstractProject<?, ?> project;
    @Mock
    JazzSCM jazzSCM;

    @Mock
    SCM otherSCM;

    JazzBuildRunListener runListener;

    @Before
    public void setUp() throws Exception {
        runListener = mock(JazzBuildRunListener.class, CALLS_REAL_METHODS);

        MockitoAnnotations.initMocks(this);
        when(run.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(run.getLogFile()).thenReturn(new File("."));

        when(run.getParent()).thenReturn(project);
        when(project.getScm()).thenReturn(jazzSCM);
        doReturn(buildTool).when(runListener).getBuildTool(any(Run.class));
    }

    @Test
    public void notJazzBuildStarted() {
        when(project.getScm()).thenReturn(otherSCM);

        runListener.onStarted(run, null);

        verify(project).getScm();
        verify(run, never()).getAction(JazzBuildAction.class);
    }

    @Test
    public void notJazzBuildFinished() {
        when(project.getScm()).thenReturn(otherSCM);

        runListener.onFinalized(run);

        verify(project).getScm();
        verify(run, never()).getAction(JazzBuildAction.class);
    }

    @Test
    public void jazzBuildStartedByBuildTrigger() {
        when(run.getAction(JazzBuildAction.class)).thenReturn(jazzAction);
        when(buildTool.startTeamBuild(anyString(), anyString())).thenReturn("1111");

        runListener.onStarted(run, null);

        verify(run, atLeastOnce()).getAction(JazzBuildAction.class);
        verify(jazzAction).setBuildResultId("1111");
    }

    @Test
    public void jazzBuildStartedByScmTrigger() {
        Run scmTriggeredRun = mock(Run.class, CALLS_REAL_METHODS);
        doReturn(project).when(scmTriggeredRun).getParent();
        doReturn("test").when(scmTriggeredRun).getFullDisplayName();
        doReturn("1111").when(buildTool).requestTeamBuild();
        doReturn("XXXX").when(buildTool).startTeamBuild(anyString(), anyString());

        runListener.onStarted(scmTriggeredRun, null);

        JazzBuildAction action = scmTriggeredRun.getAction(JazzBuildAction.class);
        assertNotNull(action);
        assertThat(action.getRequestId(), is("1111"));
        assertThat(action.getBuildResultId(), is("XXXX"));
    }

    @Test
    public void jazzBuildFinishedWithSuccess() {
        testJazzBuildFinished(Result.SUCCESS, "OK");
    }

    @Test
    public void jazzBuildFinishedWithFailure() {
        testJazzBuildFinished(Result.FAILURE, "ERROR");
    }

    @Test
    public void jazzBuildFinishedUnstable() {
        testJazzBuildFinished(Result.UNSTABLE, "WARNING");
    }

    @Test
    public void jazzBuildAborted() {
        testJazzBuildFinished(Result.ABORTED, "ERROR");
    }

    @Test
    public void jazzBuildNotBuilt() {
        testJazzBuildFinished(Result.NOT_BUILT, "ERROR");
    }

    private void testJazzBuildFinished(Result result, String expectedStatus) {
        when(project.getScm()).thenReturn(jazzSCM);
        when(run.getAction(JazzBuildAction.class)).thenReturn(jazzAction);
        when(run.getResult()).thenReturn(result);
        when(jazzAction.getBuildResultId()).thenReturn("2222");

        runListener.onFinalized(run);

        verify(run, atLeastOnce()).getAction(JazzBuildAction.class);
        verify(buildTool).linkPublisher(eq("2222"), anyString(), anyString());
        verify(buildTool).logPublisher(eq("2222"), eq("Full build log"), anyString());
        verify(buildTool).completeTeamBuild("2222", expectedStatus);
    }
}
