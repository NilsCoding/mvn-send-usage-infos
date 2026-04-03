package io.github.nilscoding.maven.sendusageinfos;

import io.github.nilscoding.maven.sendusageinfos.data.UsageData;
import io.github.nilscoding.maven.sendusageinfos.sink.FileSink;
import io.github.nilscoding.maven.sendusageinfos.sink.IDataSink;
import io.github.nilscoding.maven.sendusageinfos.sink.LoggingSink;
import io.github.nilscoding.maven.sendusageinfos.sink.SinkParameters;
import io.github.nilscoding.maven.sendusageinfos.sink.WebDataSink;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.License;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.settings.Settings;
import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Mojo to send usage infos (metadata and licenses) from referenced artifacts to a URL endpoint or save them to a file.
 *
 * @author NilsCoding
 */
@Mojo(
        name = "send-usage-infos",
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class SendUsageInfoMojo extends AbstractMojo {

    /**
     * URL method (optional).
     */
    @Parameter(property = "urlMethod", defaultValue = "POST")
    private String urlMethod;
    /**
     * URL location.
     */
    @Parameter(property = "urlLocation", required = true)
    private String urlLocation;
    /**
     * URL authentication header value (optional).
     */
    @Parameter(property = "urlAuthHeaderValue")
    private String urlAuthHeader;
    /**
     * Flag for pretty-printing.
     */
    @Parameter(property = "prettyPrint")
    private Boolean prettyPrint;
    /**
     * Custom parameter.
     */
    @Parameter(property = "customParameter")
    private String customParameter;
    /**
     * Flag to include snapshot versions.
     */
    @Parameter(property = "includeSnapshots")
    private Boolean includeSnapshots;

    /**
     * Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Maven Session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * Maven project builder.
     */
    @Inject
    private ProjectBuilder mavenProjectBuilder;
    /**
     * Maven settings.
     */
    @Inject
    private Settings mavenSettings;

    /**
     * Creates a new instance.
     */
    public SendUsageInfoMojo() {
    }

    /**
     * Executes the Maven Mojo.
     *
     * @throws MojoExecutionException Mojo execution exception
     * @throws MojoFailureException   Mojo failure exception
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        log.info("SendUsageInfo Mojo at work...");

        // check for snapshot builds and skip processing if not enabled otherwise
        boolean isSnapshot = this.project.getArtifact().isSnapshot();
        if (isSnapshot) {
            if (this.includeSnapshots == null || !this.includeSnapshots) {
                log.info("Skipping processing of SNAPSHOT version (bypass with 'includeSnapshots' option)");
                return;
            }
        }

        UsageData usageData = new UsageData();

        String tmpLocation = this.urlLocation;
        tmpLocation = (tmpLocation != null) ? tmpLocation.trim() : "";
        if (tmpLocation.isEmpty()) {
            return;
        }
        // split by space / line breaks
        String[] tmpLocations = tmpLocation.split("[\n|\r]");
        Set<String> locations = new LinkedHashSet<>();
        for (String oneTmpLocation : tmpLocations) {
            if (!oneTmpLocation.trim().isEmpty()) {
                oneTmpLocation = oneTmpLocation.trim();
                locations.add(oneTmpLocation);
            }
        }

        if (locations.isEmpty()) {
            log.warn("unsupported location, not sending any data");
            return;
        }

        // assign project artifact's data
        io.github.nilscoding.maven.sendusageinfos.data.Artifact projectArtifact
                = io.github.nilscoding.maven.sendusageinfos.data.Artifact.fromMavenProject(this.project);
        usageData.setProjectArtifact(projectArtifact);

        // assign data of referenced artifacts
        Set<Artifact> mavenRefArtifacts = this.project.getArtifacts();
        if ((mavenRefArtifacts != null) && (mavenRefArtifacts.isEmpty() == false)) {
            List<io.github.nilscoding.maven.sendusageinfos.data.Artifact> refArtifacts = new LinkedList<>();
            for (final Artifact oneRefArtifact : mavenRefArtifacts) {
                io.github.nilscoding.maven.sendusageinfos.data.Artifact refArtifact =
                        io.github.nilscoding.maven.sendusageinfos.data.Artifact.fromMavenArtifact(oneRefArtifact);
                MavenProject refProject = this.resolveProjectViaRepo(oneRefArtifact);
                if (refProject != null) {
                    List<License> mavenRefLicenses = refProject.getLicenses();
                    if ((mavenRefLicenses != null) && (mavenRefLicenses.isEmpty() == false)) {
                        List<io.github.nilscoding.maven.sendusageinfos.data.License> refLicenses = new LinkedList<>();
                        for (final License oneMavenRefLicense : mavenRefLicenses) {
                            io.github.nilscoding.maven.sendusageinfos.data.License oneRefLicense =
                                    io.github.nilscoding.maven.sendusageinfos.data.License.fromMavenLicense(oneMavenRefLicense);
                            if (oneRefLicense != null) {
                                refLicenses.add(oneRefLicense);
                            }
                        }
                        refArtifact.setLicenses(refLicenses);
                    }
                    refArtifact.setName(refProject.getName());
                    refArtifact.setDescription(refProject.getDescription());
                    refArtifact.setWebsiteUrl(refProject.getUrl());
                    refArtifacts.add(refArtifact);
                }
            }
            if (refArtifacts.isEmpty() == false) {
                usageData.setReferencedArtifacts(refArtifacts);
            }
        }

        // send data

        // find all sinks
        List<IDataSink> allSinks = new LinkedList<>();
        // add predefined sinks (but not the file-based one, which is implicitly used as the default)
        allSinks.add(new WebDataSink());
        allSinks.add(new LoggingSink());
        // load custom sinks, if available
        Iterable<IDataSink> customSinksItr = ServiceLoader.load(IDataSink.class);
        for (IDataSink oneCustomSink : customSinksItr) {
            allSinks.add(oneCustomSink);
        }
        // define default sink
        IDataSink defaultSink = new FileSink();

        SettingsWrapper settingsWrapper = new SettingsWrapper(this.mavenSettings);

        for (String oneLocation : locations) {
            if ((oneLocation == null) || (oneLocation.trim().isEmpty())) {
                continue;
            }
            SinkParameters sinkParams = new SinkParameters();
            sinkParams.setUrl(oneLocation);
            sinkParams.setHttpMethod(this.urlMethod);
            sinkParams.setAuthHeader(this.urlAuthHeader);
            sinkParams.setPrettyPrinting(this.prettyPrint != null ? this.prettyPrint : true);
            sinkParams.setCustomParameter(this.customParameter);

            boolean foundSupportedSink = false;
            for (IDataSink oneDataSink : allSinks) {
                if (oneDataSink.willWriteTo(oneLocation)) {
                    foundSupportedSink = true;
                    oneDataSink.sendData(usageData, sinkParams, settingsWrapper, log);
                }
            }
            if ((foundSupportedSink == false) && (defaultSink != null)) {
                defaultSink.sendData(usageData, sinkParams, settingsWrapper, log);
            }
        }
    }

    /**
     * Resolves a Maven project via Artifact info.
     *
     * @param artifact artifact used for resolving
     * @return Maven project
     */
    protected MavenProject resolveProjectViaRepo(Artifact artifact) {
        if (artifact == null) {
            return null;
        }
        try {
            ProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest(
                    this.session.getProjectBuildingRequest()
            );
            projectBuildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            projectBuildingRequest.setProcessPlugins(false);
            ProjectBuildingResult pbRes = this.mavenProjectBuilder.build(artifact, projectBuildingRequest);
            return pbRes.getProject();
        } catch (Exception ex) {
            getLog().warn("error building project for '" + artifact + "': " + ex);
            return null;
        }
    }

}
