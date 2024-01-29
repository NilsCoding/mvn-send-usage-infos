package io.github.nilscoding.maven.sendusageinfos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.nilscoding.maven.sendusageinfos.data.UsageData;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Mojo to send usage infos (metadata and licenses) from referenced artifacts to a URL endpoint.
 * @author NilsCoding
 */
@Mojo(
        name = "send-usage-infos",
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class SendUsageInfoMojo extends AbstractMojo {

    /**
     * URL method.
     */
    @Parameter(name = "urlMethod", defaultValue = "POST")
    private String urlMethod;
    /**
     * URL location.
     */
    @Parameter(name = "urlLocation", required = true)
    private String urlLocation;
    /**
     * URL authentication header value (optional).
     */
    @Parameter(name = "urlAuthHeaderValue")
    private String urlAuthHeader;
    /**
     * URL content type to send (application/json).
     */
//    @Parameter(name = "urlContentType", defaultValue = "application/json", readonly = true)
    private String urlContentType = "application/json";

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
    @Component
    private ProjectBuilder mavenProjectBuilder;

    /**
     * Creates a new instance.
     */
    public SendUsageInfoMojo() {
    }

    /**
     * Executes the Maven Mojo.
     * @throws MojoExecutionException Mojo execution exception
     * @throws MojoFailureException   Mojo failure exception
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        log.info("SendUsageInfo Mojo at work...");

        UsageData usageData = new UsageData();

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
                    refArtifacts.add(refArtifact);
                }
            }
            if (refArtifacts.isEmpty() == false) {
                usageData.setReferencedArtifacts(refArtifacts);
            }
        }

        // send data
        try {
            Gson gson = new GsonBuilder().create();
            String bodyContent = gson.toJson(usageData);

            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .hostnameVerifier((hostname, sslSession) -> true)
                    .build();
            Request.Builder reqBuilder = new Request.Builder()
                    .url(this.urlLocation);
            if ((this.urlAuthHeader != null) && (this.urlAuthHeader.isEmpty() == false)) {
                reqBuilder.addHeader("Authorization", this.urlAuthHeader);
            }
            RequestBody reqBody = RequestBody.create(bodyContent, MediaType.get(this.urlContentType));
            reqBuilder.method(this.urlMethod, reqBody);
            Request req = reqBuilder.build();
            log.info("sending usage info data to '" + this.urlLocation + "' ...");
            Call call = client.newCall(req);
            Response resp = call.execute();
            int respCode = resp.code();
            log.info("data sent (http " + respCode + ")");
        } catch (Exception ex) {
            log.error("exception sending data: " + ex);
        }
    }

    /**
     * Resolves a Maven project via Artifact info.
     * @param artifact artifact used for resolving
     * @return Maven project
     */
    protected MavenProject resolveProjectViaRepo(Artifact artifact) {
        if (artifact == null) {
            return null;
        }
        try {
            ProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest();
            projectBuildingRequest.setLocalRepository(this.session.getLocalRepository());
            projectBuildingRequest.setRepositorySession(this.session.getRepositorySession());
            ProjectBuildingResult pbRes = this.mavenProjectBuilder.build(artifact, projectBuildingRequest);
            return pbRes.getProject();
        } catch (Exception ex) {
            return null;
        }
    }

}
