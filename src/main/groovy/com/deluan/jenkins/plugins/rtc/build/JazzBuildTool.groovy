package com.deluan.jenkins.plugins.rtc.build

import com.deluan.jenkins.plugins.rtc.JazzConfiguration
import org.apache.commons.lang.builder.ToStringBuilder
import org.apache.tools.ant.PropertyHelper

/**
 * @author deluan
 */
class JazzBuildTool {

    private AntBuilder ant
    private String buildToolkitPath
    private String buildToolkitDefs
    private String buildEngineId
    private String buildDefinitionId
    private String repositoryLocation
    private String username
    private String password

    JazzBuildTool(JazzConfiguration config, String buildToolkitPath, String buildEngineId, String buildDefinitionId) {
        println("========= CRIANDO ANTBUILDER ==============");

        this.buildToolkitPath = buildToolkitPath
        this.buildEngineId = buildEngineId
        this.buildDefinitionId = buildDefinitionId
        this.buildToolkitDefs = "${buildToolkitPath}/BuildToolkitTaskDefs.xml"

        repositoryLocation = config.repositoryLocation
        username = config.username
        password = config.password

        ant = new AntBuilder()
        def PATH = 'task.path'
        ant.path(id: PATH) {
            fileset(dir: buildToolkitPath) {
                include(name: "com.ibm.*.jar")
                include(name: "org.eclipse.*.jar")
                include(name: "http*.jar")
                include(name: "org.apache.*.jar")
            }
        }
        def tasksDefFile = File.createTempFile('jazzbuildtools', '.properties')
        try {
            def tasks = new XmlSlurper().parse(new File(buildToolkitDefs))
            tasks.taskdef.each {
                def name = it.@name.text()
                def classname = it.@classname.text()
                tasksDefFile.append("${name}=${classname}\n");
            }
            ant.taskdef(file: tasksDefFile.absolutePath, format: "properties", classpathref: PATH)
        } finally {
            tasksDefFile.delete()
        }
        println("========= ANTBUILDER CRIADO ==============");
    }

    public static boolean checkForValidInstallation(String path) {
        return new File("${path}/BuildToolkitTaskDefs.xml").exists()
    }

    @SuppressWarnings("GroovyAccessibility")
    private removeAntProperty(String propertyName) {
        PropertyHelper.getPropertyHelper(ant.project).@properties.remove(propertyName)
    }

    private String doGetNextTeamBuildRequest() {
        removeAntProperty('requestUUID')

        ant.getNextTeamBuildRequest(
                repositoryAddress: repositoryLocation,
                userId: username,
                password: password,
                engineId: buildEngineId,
                buildDefinitionId: buildDefinitionId,
                requestUUIDProperty: "requestUUID",
                verbose: "false",
        )

        return ant.project.properties.requestUUID ?: null
    }

    synchronized public String requestTeamBuild() {
        removeAntProperty('requestUUID')

        ant.requestTeamBuild(
                buildDefinitionId: buildDefinitionId,
                repositoryAddress: repositoryLocation,
                userId: username,
                password: password,
                requestUUIDProperty: "requestUUID",
                verbose: "false",
        )

        // Irk! Any other way to prepare the build?
        return doGetNextTeamBuildRequest()
    }

    synchronized public String getNextTeamBuildRequest() {
        return doGetNextTeamBuildRequest();
    }

    public String startTeamBuild(String requestUUID, String label) {
        removeAntProperty('buildResultUUID')

        ant.startTeamBuild(
                repositoryAddress: repositoryLocation,
                userId: username,
                password: password,
                requestUUID: requestUUID,
                resultUUIDProperty: "buildResultUUID",
                label: label,
                autoComplete: "false",
                verbose: "true",
        )

        return ant.project.properties.buildResultUUID ?: null
    }

    public void completeTeamBuild(String buildResultUUID, String status) {
        ant.completeTeamBuild(
                repositoryAddress: repositoryLocation,
                userId: username,
                password: password,
                buildResultUUID: buildResultUUID,
                status: status,
                verbose: "true",
        )
    }

    public void logPublisher(String buildResultUUID, String label, String filePath) {
        ant.logPublisher(
                repositoryAddress: repositoryLocation,
                userId: username,
                password: password,
                buildResultUUID: buildResultUUID,
                label: label,
                filePath: filePath,
                verbose: "true",
        )
    }

    public void linkPublisher(String buildResultUUID, String label, String url) {
        ant.linkPublisher(
                repositoryAddress: repositoryLocation,
                userId: username,
                password: password,
                buildResultUUID: buildResultUUID,
                label: label,
                url: url,
                verbose: "true",
        )
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("buildEngineId", buildEngineId).
                append("buildDefinitionId", buildDefinitionId).
                toString();
    }
}
